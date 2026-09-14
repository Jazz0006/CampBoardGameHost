from pathlib import Path


path = Path(
    "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
)
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

old = """                onSelectName = { name ->
                    when (currentStep.action) {
                        ClocktowerNightAction.RedHerring -> onSelectRedHerring(if (redHerring == name) null else name)
                        ClocktowerNightAction.Poison -> onSelectPoisonTarget(if (poisonTarget == name) null else name)
                        ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(if (butlerMaster == name) null else name)
                        ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(if (monkProtectedTarget == name) null else name)
                        ClocktowerNightAction.DemonKill -> onSelectNightDeath(if (demonAttackDraftTarget == name) null else name)
                        ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(if (mayorRedirectDraftTarget == name) null else name)
                        ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(if (demonSuccessorTarget == name) null else name)
                        ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(if (ravenkeeperTarget == name) null else name)
                        else -> Unit
                    }
                },
"""
new = """                onSelectName = { name ->
                    val nextSelection = clocktowerToggledSingleTargetSelection(
                        currentSelection = selectedNightName,
                        tappedSelection = name,
                    )
                    when (currentStep.action) {
                        ClocktowerNightAction.RedHerring -> onSelectRedHerring(nextSelection)
                        ClocktowerNightAction.Poison -> onSelectPoisonTarget(nextSelection)
                        ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(nextSelection)
                        ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(nextSelection)
                        ClocktowerNightAction.DemonKill -> onSelectNightDeath(nextSelection)
                        ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(nextSelection)
                        ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(nextSelection)
                        ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(nextSelection)
                        else -> Unit
                    }
                },
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one patch anchor, found {count}")

text = text.replace(old, new, 1)

if old in text:
    raise SystemExit("Original patch anchor remains after replacement")
if text.count("currentSelection = selectedNightName") != 1:
    raise SystemExit("Expected exactly one visible-draft toggle call")
for legacy in (
    "onSelectPoisonTarget(if (poisonTarget == name) null else name)",
    "onSelectMonkProtectedTarget(if (monkProtectedTarget == name) null else name)",
):
    if legacy in text:
        raise SystemExit(f"Legacy confirmed-state comparison remains: {legacy}")

path.write_text(text, encoding="utf-8", newline="\n")
