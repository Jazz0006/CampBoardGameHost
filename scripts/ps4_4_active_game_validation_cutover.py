from pathlib import Path

APP = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
text = APP.read_text(encoding="utf-8")

old_init = '''    val activeGamePersistenceCoordinator = remember(baseContext) {
        ActiveGamePersistenceCoordinator.fromContext(baseContext)
    }
'''
new_init = '''    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
    val activeGameWerewolfSaveValidator = remember {
        WerewolfActiveGameSaveValidator(WerewolfRoleRegistry.builtIn())
    }
'''

old_validation = '''    activeGamePersistenceCoordinator.identityForSave(
        ActiveGamePersistenceInputs(
            gameKind = currentGameKind,
            clocktowerScript = currentClocktowerScript,
            assignedClocktowerRoleIds = if (currentGameKind == GameKind.Clocktower) {
                cards.map { card ->
                    requireNotNull(card.clocktowerRole?.enName?.let(::RoleId)) {
                        "Clocktower recovery requires assigned role IDs."
                    }
                }
            } else {
                emptyList()
            },
            assignedWerewolfRoles = if (currentGameKind == GameKind.Werewolf) {
                cards.map { it.role }
            } else {
                emptyList()
            },
            werewolfCount = werewolfCount,
            includeSeer = includeSeer,
            includeWitch = includeWitch,
            includeHunter = includeHunter,
            lastWordsMode = lastWordsMode,
        ),
    )
'''
new_validation = '''    when (currentGameKind) {
        GameKind.Undercover -> Unit
        GameKind.Clocktower -> {
            ClocktowerActiveSessionValidator.validateForRecoverySave(
                script = activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript).script,
                assignedRoleIds = cards.map { card ->
                    requireNotNull(card.clocktowerRole?.enName?.let(::RoleId)) {
                        "Clocktower recovery requires assigned role IDs."
                    }
                },
            )
        }
        GameKind.Werewolf -> {
            activeGameWerewolfSaveValidator.validate(
                assignedRoles = cards.map { card -> card.role },
                werewolfCount = werewolfCount,
                includeSeer = includeSeer,
                includeWitch = includeWitch,
                includeHunter = includeHunter,
            )
        }
    }
'''

for label, anchor in (("coordinator initialization", old_init), ("identityForSave validation block", old_validation)):
    count = text.count(anchor)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}.")

for label, anchor in (("new catalog initialization", new_init), ("new validation dispatch", new_validation)):
    if anchor in text:
        raise SystemExit(f"Unexpected pre-existing {label}; refusing non-idempotent cutover.")

text = text.replace(old_init, new_init, 1)
text = text.replace(old_validation, new_validation, 1)

for forbidden in (
    "activeGamePersistenceCoordinator",
    "ActiveGamePersistenceInputs(",
    "ActiveGamePersistenceCoordinator.fromContext",
):
    if forbidden in text:
        raise SystemExit(f"Legacy App anchor still present after cutover: {forbidden}")

for required in (
    "activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript).script",
    "ClocktowerActiveSessionValidator.validateForRecoverySave(",
    "activeGameWerewolfSaveValidator.validate(",
):
    if text.count(required) != 1:
        raise SystemExit(f"Expected exactly one new App anchor after cutover: {required}")

APP.write_text(text, encoding="utf-8")
print("PS4.4 App validation cutover applied exactly once.")
