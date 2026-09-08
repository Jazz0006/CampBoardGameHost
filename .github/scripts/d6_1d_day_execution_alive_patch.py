from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

import_old = """import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
"""
import_new = """import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
"""

execution_old = """                                if (index >= 0 && executedCard != null && executedCard.eliminatedRound == null) {
                                    cards[index] = executedCard.copy(eliminatedRound = round)
                                    records.add(EliminationRecord(round, executionName, context.getString(R.string.clocktower_record_execution)))
"""
execution_new = """                                if (index >= 0 && executedCard != null && executedCard.eliminatedRound == null) {
                                    requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                        targetSeat = index + 1,
                                    )
                                    publishClocktowerSessionView()
                                    cards[index] = executedCard.copy(eliminatedRound = round)
                                    records.add(EliminationRecord(round, executionName, context.getString(R.string.clocktower_record_execution)))
"""

replacements = [
    (import_old, import_new, "alive-boundary import"),
    (execution_old, execution_new, "ordinary day execution death"),
]

for old, _new, label in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")

for old, new, _label in replacements:
    text = text.replace(old, new, 1)

required = [
    "import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision",
    "requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(\n                                        targetSeat = index + 1,\n                                    )",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Missing required post-patch token: {token}")

if text.count("synchronizePlayerDeathWithinCurrentRevision(") != 1:
    raise SystemExit("Expected exactly one production death synchronization call in this slice")

PATH.write_text(text, encoding="utf-8", newline="\n")
