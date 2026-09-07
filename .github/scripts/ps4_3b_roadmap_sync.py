from pathlib import Path

path = Path('docs/CURRENT_DEVELOPMENT_ROADMAP.md')
text = path.read_text(encoding='utf-8')


def replace_once(old: str, new: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'roadmap anchor drift: expected 1, found {count}: {old[:120]!r}')
    text = text.replace(old, new, 1)

replace_once(
    '> **PS4.3 — typed Recovery wire cleanup / format bump**',
    '> **PS4.4 — retire old ActiveGame identity/coordinator infrastructure**',
)
replace_once(
    'PS4.1 and PS4.2 are complete. Do not begin PS5, Werewolf module deletion or D6 decomposition as part of PS4.3.',
    'PS4.1 through PS4.3 are complete. Do not begin PS5, Werewolf module deletion or D6 decomposition as part of PS4.4.',
)
replace_once(
    'PS4  retire superseded active-save infrastructure   IN PROGRESS (PS4.1–PS4.2 COMPLETE)',
    'PS4  retire superseded active-save infrastructure   IN PROGRESS (PS4.1–PS4.3 COMPLETE)',
)
replace_once(
    'Status: **in progress — PS4.1 and PS4.2 complete; PS4.3 next**.',
    'Status: **in progress — PS4.1 through PS4.3 complete; PS4.4 next**.',
)
replace_once(
    'Recovery compatibility is now derived from `RecoverySnapshot.CURRENT_FORMAT_VERSION`, producing `recovery-v1:<GameKind>` for format v1. The token no longer depends on `ActiveGamePersistenceCoordinator.CURRENT_VERSION`. No migration support was added, and the 4-hour/fail-closed policy is unchanged.',
    'Recovery compatibility is derived from `RecoverySnapshot.CURRENT_FORMAT_VERSION`. At the PS4.2 checkpoint format v1 produced `recovery-v1:<GameKind>`; PS4.3 subsequently advanced the current format to v2, producing `recovery-v2:<GameKind>`. The token no longer depends on `ActiveGamePersistenceCoordinator.CURRENT_VERSION`. No migration support was added, and the 4-hour/fail-closed policy is unchanged.',
)

start = '#### PS4.3 — Typed Recovery wire cleanup / format bump\n'
end = '#### PS4.4 — Retire ActiveGame identity/coordinator infrastructure\n'
if text.count(start) != 1 or text.count(end) != 1:
    raise SystemExit('PS4.3/PS4.4 section anchors drifted')
start_i = text.index(start)
end_i = text.index(end, start_i)
completed = '''#### PS4.3 — Typed Recovery wire cleanup / format bump

Status: **COMPLETE**.

PS4.3a first moved genuine durable `TroubleBrewingSetupRotationRecord` bookkeeping into typed `ClocktowerRecovery` ownership and preserved its existing wire key. PS4.3b then deliberately advanced current Recovery format `v1 -> v2` and removed obsolete ActiveGame-shaped Recovery metadata.

Tests-first / production checkpoints:

```text
1e856f0fe7c246bbb5f977810eb95fb34d8d08d1
test: define typed Clocktower rotation recovery ownership

e4cde20f8449eed49e36ef6e0162f696f3901096
test: define Recovery v2 schema contract

58d687cc8c8082c040cf07eabcf5c1cfbd4dda15
refactor: cut Recovery over to v2 schema
```

Current Recovery v2 no longer persists:

```text
legacy active-state version
gameContentIdentity / PersistedActiveGameIdentity
committedClocktowerSetup
persisted clocktowerRulesetRoleIds
persisted clocktowerRulesetRef
```

`LegacyRestoreCompatibility` is deleted. Previous v1 Recovery fails the current-format gate as `UnsupportedFormat`; no migration framework was introduced. Current Clocktower ruleset basis/ref are reconstructed from recovered actual roles and current rules. The existing `activeGamePersistenceCoordinator.identityForSave(...)` call is temporarily retained only for its save-time validation behavior and is the key PS4.4 ownership question.

'''
text = text[:start_i] + completed + text[end_i:]
replace_once(
    '#### PS4.4 — Retire ActiveGame identity/coordinator infrastructure\n\nAfter PS4.2/PS4.3, perform a new reference audit.',
    '#### PS4.4 — Retire ActiveGame identity/coordinator infrastructure\n\nStatus: **NEXT — NOT STARTED**.\n\nAfter PS4.2/PS4.3, perform a new reference audit.',
)

path.write_text(text, encoding='utf-8')
print('PS4.3b roadmap sync complete')
