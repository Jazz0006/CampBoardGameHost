from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
text = path.read_text(encoding="utf-8")

old = """                committedTroubleBrewingSetupSelection = preparedSetup.selection
                troubleBrewingFirstNightPrecomputeCoordinator.prewarm(
"""
new = """                committedTroubleBrewingSetupSelection = preparedSetup.selection
                persistActiveGameStateIfNeeded()
                troubleBrewingFirstNightPrecomputeCoordinator.prewarm(
"""

if text.count(old) != 1:
    raise SystemExit(
        f"Expected exactly one TB provenance commit anchor, found {text.count(old)}"
    )

text = text.replace(old, new, 1)

if old in text:
    raise SystemExit("Original TB provenance commit anchor remains after patch")
if text.count("committedTroubleBrewingSetupSelection = preparedSetup.selection") != 1:
    raise SystemExit("TB committed setup selection assignment changed unexpectedly")

path.write_text(text, encoding="utf-8")
