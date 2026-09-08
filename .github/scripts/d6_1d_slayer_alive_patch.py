from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

old = """                                recordClocktowerAction(ActionFactDraft.Death(
                                    actionId = clocktowerActionId(
                                        kind = "slayer-death",
                                        localSequence = localSequence,
                                        targetSeat = targetSeat,
                                    ),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = localSequence,
                                    targetSeat = targetSeat,
                                ))
                                cards[targetIndex] = targetCard.copy(eliminatedRound = round)
                                recordEpistemicObservation(EpistemicObservationDraft(
"""
new = """                                recordClocktowerAction(ActionFactDraft.Death(
                                    actionId = clocktowerActionId(
                                        kind = "slayer-death",
                                        localSequence = localSequence,
                                        targetSeat = targetSeat,
                                    ),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = localSequence,
                                    targetSeat = targetSeat,
                                ))
                                requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                    targetSeat = targetSeat,
                                )
                                publishClocktowerSessionView()
                                cards[targetIndex] = targetCard.copy(eliminatedRound = round)
                                recordEpistemicObservation(EpistemicObservationDraft(
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one Slayer death materialization anchor, found {count}")
text = text.replace(old, new, 1)

if text.count("synchronizePlayerDeathWithinCurrentRevision(") != 3:
    raise SystemExit("Unexpected Clocktower death synchronization call count after Slayer slice")
required = "recordClocktowerAction(ActionFactDraft.Death("
slayer_start = text.find('kind = "slayer-death"')
if slayer_start < 0:
    raise SystemExit("Missing Slayer action anchor after patch")
window = text[slayer_start:slayer_start + 1800]
if window.find("synchronizePlayerDeathWithinCurrentRevision") < 0 or window.find("cards[targetIndex] = targetCard.copy(eliminatedRound = round)") < 0:
    raise SystemExit("Missing Slayer canonical/UI death materialization after action")
if window.find("synchronizePlayerDeathWithinCurrentRevision") > window.find("cards[targetIndex] = targetCard.copy(eliminatedRound = round)"):
    raise SystemExit("Slayer session death must precede cards projection")

PATH.write_text(text, encoding="utf-8", newline="\n")
