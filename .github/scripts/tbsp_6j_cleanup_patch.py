from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
text = path.read_text(encoding="utf-8")

old_signature = """        preparedClocktowerSeed: Long? = null,\n        preparedSetupPlan: RecommendationPlan? = null,\n    ) {"""
new_signature = """        preparedClocktowerSeed: Long? = null,\n    ) {"""
old_call = "resetDealState(GameKind.Clocktower, script, preparedSeed, preparedSetupPlan)"
new_call = "resetDealState(GameKind.Clocktower, script, preparedSeed)"

if text.count(old_signature) != 1:
    raise SystemExit(f"Expected exactly one dead resetDealState parameter anchor, found {text.count(old_signature)}")
if text.count(old_call) != 1:
    raise SystemExit(f"Expected exactly one four-argument resetDealState call, found {text.count(old_call)}")

text = text.replace(old_signature, new_signature, 1)
text = text.replace(old_call, new_call, 1)

if old_signature in text or old_call in text:
    raise SystemExit("Dead pass-through anchors still present after patch")
if "val preparedSetupPlan = if (assignments.any { it.actualRole.enName == \"Drunk\" })" not in text:
    raise SystemExit("Legacy/NGJ local preparedSetupPlan calculation was unexpectedly changed")
if "val recommendedDrunkShownRole = preparedSetupPlan" not in text:
    raise SystemExit("Legacy/NGJ Drunk shown-role consumer was unexpectedly changed")

path.write_text(text, encoding="utf-8")
