from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

old = """                                    if (dawnDeathMaterialization.stateMutationRequired) {
                                        cards[index] = nightDeathCard.copy(eliminatedRound = round)
                                        records.add(EliminationRecord(round, deathName, context.getString(R.string.clocktower_record_night_death)))
"""
new = """                                    if (dawnDeathMaterialization.stateMutationRequired) {
                                        requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                            targetSeat = dawnDeathMaterialization.intent.targetSeat,
                                        )
                                        publishClocktowerSessionView()
                                        cards[index] = nightDeathCard.copy(eliminatedRound = round)
                                        records.add(EliminationRecord(round, deathName, context.getString(R.string.clocktower_record_night_death)))
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one dawn death anchor, found {count}")
text = text.replace(old, new, 1)

required = """requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                            targetSeat = dawnDeathMaterialization.intent.targetSeat,
                                        )
                                        publishClocktowerSessionView()
                                        cards[index] = nightDeathCard.copy(eliminatedRound = round)"""
if required not in text:
    raise SystemExit("Missing required dawn session synchronization sequence")
if text.count("synchronizePlayerDeathWithinCurrentRevision(") != 4:
    raise SystemExit("Expected exactly four production death synchronization calls after dawn cutover")

PATH.write_text(text, encoding="utf-8", newline="\n")
