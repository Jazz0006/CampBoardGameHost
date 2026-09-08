from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

old = """                                if (index >= 0 && nominatorCard != null && nominatorCard.eliminatedRound == null) {
                                    cards[index] = nominatorCard.copy(eliminatedRound = round)
                                    records.add(
"""
new = """                                if (index >= 0 && nominatorCard != null && nominatorCard.eliminatedRound == null) {
                                    requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                        targetSeat = index + 1,
                                    )
                                    publishClocktowerSessionView()
                                    cards[index] = nominatorCard.copy(eliminatedRound = round)
                                    records.add(
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one Virgin execution death anchor, found {count}")
text = text.replace(old, new, 1)

required = "requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(\n                                        targetSeat = index + 1,\n                                    )"
if text.count(required) != 2:
    raise SystemExit("Expected exactly two day/Virgin session death synchronization calls after patch")
if text.count("synchronizePlayerDeathWithinCurrentRevision(") != 2:
    raise SystemExit("Unexpected Clocktower death synchronization call count after Virgin slice")

PATH.write_text(text, encoding="utf-8", newline="\n")
