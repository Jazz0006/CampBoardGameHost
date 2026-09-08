from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")

old = """    fun applyValidatedRecoveryPlan(plan: ValidatedRecoveryPlan) {
        val game = plan.snapshot.game
        val restoredCards = game.cards.map(::localizedRestoredCard)

        playerNames.clear()
        playerNames.addAll(restoredCards.map(PlayerCard::name))
        cards.clear()
"""
new = """    fun applyValidatedRecoveryPlan(plan: ValidatedRecoveryPlan) {
        val game = plan.snapshot.game
        val restoredCards = game.cards.map(::localizedRestoredCard)
        val restoredPlayerNames = restoredCards.map(PlayerCard::name)

        playerNames.clear()
        playerNames.addAll(restoredPlayerNames)
        hostSeatingSetupFlow = HostSeatingSetupFlow.recoveredActiveGame(
            playerNames = restoredPlayerNames,
            game = game.gameKind,
        )
        cards.clear()
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Recovery apply anchor count was {count}, expected exactly 1")
if "HostSeatingSetupFlow.recoveredActiveGame(" in text:
    raise SystemExit("Recovery seating owner wiring already exists; refusing duplicate patch")

text = text.replace(old, new, 1)
TARGET.write_text(text, encoding="utf-8", newline="\n")
