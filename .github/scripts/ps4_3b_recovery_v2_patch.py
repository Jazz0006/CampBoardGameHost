from pathlib import Path
import subprocess

ROOT = Path('.')

APP = Path('app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt')
MODEL = Path('app/src/main/java/com/codex/campboardgamehost/persistence/RecoverySnapshot.kt')
ENCODER = Path('app/src/main/java/com/codex/campboardgamehost/persistence/RecoverySnapshotJsonCodec.kt')
DECODER = Path('app/src/main/java/com/codex/campboardgamehost/persistence/RecoverySnapshotStrictDecoder.kt')
TOKEN_TEST = Path('app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryCompatibilityTokenTest.kt')
V2_TEST = Path('app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryV2SchemaContractTest.kt')


def read(path: Path) -> str:
    return path.read_text(encoding='utf-8')


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding='utf-8')


def replace_once(path: Path, old: str, new: str) -> None:
    text = read(path)
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{path}: expected one exact anchor, found {count}: {old[:100]!r}')
    write(path, text.replace(old, new, 1))


def remove_between(path: Path, start: str, end: str) -> None:
    text = read(path)
    if text.count(start) != 1 or text.count(end) < 1:
        raise SystemExit(f'{path}: missing/non-unique range anchors')
    start_index = text.index(start)
    end_index = text.index(end, start_index)
    write(path, text[:start_index] + text[end_index:])


def remove_legacy_call_block(path: Path, marker: str) -> bool:
    text = read(path)
    index = text.find(marker)
    if index < 0:
        return False
    line_start = text.rfind('\n', 0, index) + 1
    call_index = text.find('LegacyRestoreCompatibility(', index)
    if call_index < 0:
        raise SystemExit(f'{path}: marker does not lead to LegacyRestoreCompatibility call')
    open_index = text.find('(', call_index)
    depth = 0
    close_index = None
    for cursor in range(open_index, len(text)):
        char = text[cursor]
        if char == '(':
            depth += 1
        elif char == ')':
            depth -= 1
            if depth == 0:
                close_index = cursor
                break
    if close_index is None:
        raise SystemExit(f'{path}: unterminated LegacyRestoreCompatibility call')
    end = close_index + 1
    while end < len(text) and text[end] in ' \t':
        end += 1
    if end >= len(text) or text[end] != ',':
        raise SystemExit(f'{path}: expected trailing comma after LegacyRestoreCompatibility call')
    end += 1
    if end < len(text) and text[end] == '\r':
        end += 1
    if end < len(text) and text[end] == '\n':
        end += 1
    write(path, text[:line_start] + text[end:])
    return True


def git_lines(*args: str) -> list[str]:
    result = subprocess.run(['git', *args], text=True, capture_output=True)
    if result.returncode not in (0, 1):
        raise SystemExit(result.stderr)
    return [line for line in result.stdout.splitlines() if line]


initial_legacy_paths = set(git_lines('grep', '-l', 'LegacyRestoreCompatibility', '--', 'app'))
required_owners = {str(APP), str(MODEL), str(ENCODER), str(DECODER)}
if not required_owners.issubset(initial_legacy_paths):
    missing = sorted(required_owners - initial_legacy_paths)
    raise SystemExit(f'Expected live legacy owners missing before PS4.3b: {missing}')

# Recovery model: remove the bridge entirely and deliberately advance the current-only format.
for obsolete_import in (
    'import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup\n',
    'import com.codex.campboardgamehost.clocktower.domain.RoleId\n',
    'import com.codex.campboardgamehost.clocktower.domain.RulesetRef\n',
):
    replace_once(MODEL, obsolete_import, '')
replace_once(MODEL, '    val legacyRestoreCompatibility: LegacyRestoreCompatibility,\n', '')
replace_once(
    MODEL,
    '        require(legacyRestoreCompatibility.identity.gameKind == game.gameKind) {\n'
    '            "Recovery identity game kind must match the game payload."\n'
    '        }\n',
    '',
)
replace_once(MODEL, '        const val CURRENT_FORMAT_VERSION: Int = 1\n', '        const val CURRENT_FORMAT_VERSION: Int = 2\n')
remove_between(
    MODEL,
    '/** Temporary PS4 bridge while obsolete ActiveGame-shaped Recovery metadata is retired. */\n',
    'internal enum class RecoveryEntryPoint',
)

# Encoder: v2 emits only typed Recovery facts, no old ActiveGame compatibility shell.
replace_once(
    ENCODER,
    '/**\n * Typed emergency-recovery encoder.\n *\n * The v1 output still carries the remaining obsolete ActiveGame-shaped compatibility fields until\n * PS4.3b deliberately bumps the Recovery format and retires that shell.\n */',
    '/** Typed current-format emergency-recovery encoder. */',
)
replace_once(ENCODER, '        put("version", snapshot.legacyRestoreCompatibility.activeGameStateVersion)\n', '')
remove_between(
    ENCODER,
    '        val legacy = snapshot.legacyRestoreCompatibility\n',
    '        when (val game = snapshot.game) {',
)
replace_once(ENCODER, '            is ClocktowerRecovery -> encodeClocktower(game, legacy)\n', '            is ClocktowerRecovery -> encodeClocktower(game)\n')
replace_once(
    ENCODER,
    '    private fun JSONObject.encodeClocktower(\n        game: ClocktowerRecovery,\n        legacy: LegacyRestoreCompatibility,\n    ) {\n',
    '    private fun JSONObject.encodeClocktower(game: ClocktowerRecovery) {\n',
)
remove_between(
    ENCODER,
    '        if (legacy.clocktowerRulesetRoleIds.isEmpty()) {\n',
    '        ClocktowerNightCheckpoint(',
)

# Decoder: current v2 no longer requires or reconstructs old ActiveGame metadata.
for obsolete_import in (
    'import com.codex.campboardgamehost.clocktower.domain.RoleId\n',
    'import com.codex.campboardgamehost.clocktower.domain.RuleCoverage\n',
    'import com.codex.campboardgamehost.clocktower.domain.RulesetRef\n',
    'import com.codex.campboardgamehost.clocktower.domain.ScriptId\n',
):
    replace_once(DECODER, obsolete_import, '')
replace_once(DECODER, '        val activeGameStateVersion = json.requiredInt("version")\n', '')
replace_once(
    DECODER,
    '        val identity = PersistedActiveGameIdentityJsonCodec.decode(\n'
    '            json.requiredObject(PersistedActiveGameIdentityJsonCodec.ROOT_KEY),\n'
    '        )\n'
    '        require(identity.gameKind == gameKind) {\n'
    '            "Recovery game kind does not match persisted content identity."\n'
    '        }\n\n',
    '',
)
replace_once(DECODER, '        val committedSetup = CommittedClocktowerSetupPersistence.decodeOrNull(json)\n', '')
replace_once(DECODER, '        val clocktowerRulesetRoleIds = json.decodeRulesetRoleIdsStrict()\n', '')
replace_once(DECODER, '        val clocktowerRulesetRef = json.decodeRulesetRefStrict()\n', '')
replace_once(
    DECODER,
    '\n        val legacy = LegacyRestoreCompatibility(\n'
    '            activeGameStateVersion = activeGameStateVersion,\n'
    '            identity = identity,\n'
    '            committedClocktowerSetup = committedSetup,\n'
    '            clocktowerRulesetRoleIds = clocktowerRulesetRoleIds,\n'
    '            clocktowerRulesetRef = clocktowerRulesetRef,\n'
    '        )\n',
    '',
)
replace_once(
    DECODER,
    '                require(committedSetup == null && setupRotationRecord == null) {\n'
    '                    "Undercover recovery cannot carry Clocktower setup metadata."\n'
    '                }\n'
    '                require(clocktowerRulesetRoleIds.isEmpty() && clocktowerRulesetRef == null) {\n'
    '                    "Undercover recovery cannot carry Clocktower ruleset metadata."\n'
    '                }\n',
    '                require(setupRotationRecord == null) {\n'
    '                    "Undercover recovery cannot carry Clocktower setup metadata."\n'
    '                }\n',
)
replace_once(DECODER, '            legacyRestoreCompatibility = legacy,\n', '')
remove_between(
    DECODER,
    '    private fun JSONObject.decodeRulesetRoleIdsStrict(): Set<RoleId> {\n',
    '    private fun JSONArray.decodeCardsStrict(',
)
remove_between(
    DECODER,
    '    private fun JSONObject.requiredObject(key: String): JSONObject {\n',
    '    private fun JSONObject.requiredArray(key: String): JSONArray {',
)

# App: keep identityForSave() as the existing validation bridge, but stop persisting its result.
replace_once(APP, 'private const val ACTIVE_GAME_STATE_VERSION = ActiveGamePersistenceCoordinator.CURRENT_VERSION\n', '')
replace_once(
    APP,
    '        val gameContentIdentity = activeGamePersistenceCoordinator.identityForSave(\n',
    '        activeGamePersistenceCoordinator.identityForSave(\n',
)
if not remove_legacy_call_block(APP, '        val legacyRestoreCompatibility = LegacyRestoreCompatibility('):
    raise SystemExit('App legacyRestoreCompatibility declaration not found')
replace_once(APP, '            legacyRestoreCompatibility = legacyRestoreCompatibility,\n', '')

# Remove the obsolete constructor argument from every Recovery fixture that still carries it.
for raw_path in sorted(initial_legacy_paths):
    path = Path(raw_path)
    if path in {APP, MODEL, ENCODER, DECODER}:
        continue
    removed = 0
    while remove_legacy_call_block(path, 'legacyRestoreCompatibility = LegacyRestoreCompatibility('):
        removed += 1
    if removed == 0:
        raise SystemExit(f'{path}: legacy reference was not a removable RecoverySnapshot fixture')

# Tests: current token authority advances naturally with Recovery format v2.
compatibility_text = read(TOKEN_TEST)
if compatibility_text.count('recovery-v1:') != 2:
    raise SystemExit('RecoveryCompatibilityTokenTest expected exactly two v1 literals before bump')
write(TOKEN_TEST, compatibility_text.replace('recovery-v1:', 'recovery-v2:'))

# Keep the v2 schema test independent from soon-to-be-retired implementation classes.
replace_once(V2_TEST, 'assertFalse(raw.has(PersistedActiveGameIdentityJsonCodec.ROOT_KEY))', 'assertFalse(raw.has("gameContentIdentity"))')
replace_once(V2_TEST, 'assertFalse(raw.has(CommittedClocktowerSetupPersistence.ROOT_KEY))', 'assertFalse(raw.has("committedClocktowerSetup"))')

# Fail closed if any legacy shell reference survived anywhere in app sources/tests.
remaining_legacy = git_lines('grep', '-n', 'LegacyRestoreCompatibility', '--', 'app')
remaining_field = git_lines('grep', '-n', 'legacyRestoreCompatibility', '--', 'app')
if remaining_legacy or remaining_field:
    raise SystemExit('Legacy Recovery shell survived:\n' + '\n'.join(remaining_legacy + remaining_field))

model_text = read(MODEL)
encoder_text = read(ENCODER)
decoder_text = read(DECODER)
app_text = read(APP)
if 'const val CURRENT_FORMAT_VERSION: Int = 2' not in model_text:
    raise SystemExit('Recovery v2 authority not established')
for forbidden in (
    'legacyRestoreCompatibility',
    'PersistedActiveGameIdentityJsonCodec',
    'CommittedClocktowerSetupPersistence',
    'clocktowerRulesetRoleIds',
    'clocktowerRulesetRef',
    'put("version"',
):
    if forbidden in encoder_text:
        raise SystemExit(f'Encoder still contains obsolete Recovery wire coupling: {forbidden}')
for forbidden in (
    'LegacyRestoreCompatibility',
    'PersistedActiveGameIdentityJsonCodec',
    'CommittedClocktowerSetupPersistence',
    'decodeRulesetRoleIdsStrict',
    'decodeRulesetRefStrict',
    'requiredInt("version")',
):
    if forbidden in decoder_text:
        raise SystemExit(f'Decoder still contains obsolete Recovery wire coupling: {forbidden}')
if app_text.count('identityForSave(') != 1:
    raise SystemExit('PS4.3b must retain exactly one save-time identityForSave validation bridge')
for forbidden in ('gameContentIdentity', 'legacyRestoreCompatibility', 'ACTIVE_GAME_STATE_VERSION'):
    if forbidden in app_text:
        raise SystemExit(f'App still contains obsolete Recovery writer coupling: {forbidden}')

# Exact changed-file allowlist is derived from the audited pre-patch legacy references plus v2 contract tests.
allowed = initial_legacy_paths | {str(MODEL), str(ENCODER), str(DECODER), str(APP), str(TOKEN_TEST), str(V2_TEST)}
changed = set(git_lines('diff', '--name-only'))
unexpected = sorted(changed - allowed)
if unexpected:
    raise SystemExit(f'Unexpected PS4.3b changed files: {unexpected}')

print('PS4.3b patch complete')
print('Initial legacy owners:')
for path in sorted(initial_legacy_paths):
    print(f'  {path}')
print('Changed files:')
for path in sorted(changed):
    print(f'  {path}')
