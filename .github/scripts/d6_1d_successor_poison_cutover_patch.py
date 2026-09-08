from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

old = """                                            if (materialization.stateMutationRequired) {
                                                val poisonTargetName = materialization.intent.targetSeat
                                                    ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                                                clocktowerConfirmedPoisonTarget = poisonTargetName
                                                clocktowerPoisonTarget = poisonTargetName
                                            }
"""
new = """                                            if (materialization.stateMutationRequired) {
                                                requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
                                                    targetSeat = materialization.intent.targetSeat,
                                                )
                                                publishClocktowerSessionView()
                                                val poisonTargetName = materialization.intent.targetSeat
                                                    ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                                                clocktowerConfirmedPoisonTarget = poisonTargetName
                                                clocktowerPoisonTarget = poisonTargetName
                                            }
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one successor poison anchor, found {count}")
text = text.replace(old, new, 1)

required = """requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
                                                    targetSeat = materialization.intent.targetSeat,
                                                )
                                                publishClocktowerSessionView()"""
if required not in text:
    raise SystemExit("Missing successor poison session synchronization")
if text.count("synchronizePoisonTargetWithinCurrentRevision(") != 3:
    raise SystemExit("Expected exactly three production poison synchronization calls after successor cutover")
if text.count("commitPoisonTargetBoundary(") != 1:
    raise SystemExit("Expected exactly one production poison commit boundary call")

PATH.write_text(text, encoding="utf-8", newline="\n")
