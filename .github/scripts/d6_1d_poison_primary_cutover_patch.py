from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

import_old = """import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision
"""
import_new = """import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitPoisonTargetBoundary
import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision
import com.codex.campboardgamehost.clocktower.session.synchronizePoisonTargetWithinCurrentRevision
"""

confirm_old = """                                clocktowerConfirmedPoisonTarget = transaction.checkpoint.confirmedPoisonTarget
                                clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
                                clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
                                advanceClocktowerGameStateRevision()
"""
confirm_new = """                                requireClocktowerGameSession().commitPoisonTargetBoundary(targetSeat)
                                publishClocktowerSessionView()
                                invalidateA4RevisionScope()
                                clocktowerConfirmedPoisonTarget = transaction.checkpoint.confirmedPoisonTarget
                                clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
                                clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
"""

dusk_old = """        if (materialization.stateMutationRequired) {
            clocktowerPoisonTarget = null
            clocktowerConfirmedPoisonTarget = null
        }
"""
dusk_new = """        if (materialization.stateMutationRequired) {
            requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(targetSeat = null)
            publishClocktowerSessionView()
            clocktowerPoisonTarget = null
            clocktowerConfirmedPoisonTarget = null
        }
"""

ordinary_dawn_old = """                                if (poisonMaterialization.stateMutationRequired) {
                                    val poisonTargetName = poisonMaterialization.intent.targetSeat
                                        ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                                    clocktowerConfirmedPoisonTarget = poisonTargetName
                                    clocktowerPoisonTarget = poisonTargetName
                                }
"""
ordinary_dawn_new = """                                if (poisonMaterialization.stateMutationRequired) {
                                    requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
                                        targetSeat = poisonMaterialization.intent.targetSeat,
                                    )
                                    publishClocktowerSessionView()
                                    val poisonTargetName = poisonMaterialization.intent.targetSeat
                                        ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                                    clocktowerConfirmedPoisonTarget = poisonTargetName
                                    clocktowerPoisonTarget = poisonTargetName
                                }
"""

replacements = [
    (import_old, import_new, "poison boundary imports"),
    (confirm_old, confirm_new, "Poisoner confirmation boundary"),
    (dusk_old, dusk_new, "Dusk poison expiry boundary"),
    (ordinary_dawn_old, ordinary_dawn_new, "ordinary Dawn poison boundary"),
]
for old, _new, label in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")
for old, new, _label in replacements:
    text = text.replace(old, new, 1)

required = [
    "requireClocktowerGameSession().commitPoisonTargetBoundary(targetSeat)",
    "requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(targetSeat = null)",
    "targetSeat = poisonMaterialization.intent.targetSeat,",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Missing required post-patch token: {token}")

if text.count("commitPoisonTargetBoundary(") != 1:
    raise SystemExit("Unexpected commit poison boundary occurrence count")
if text.count("synchronizePoisonTargetWithinCurrentRevision(") != 2:
    raise SystemExit("Unexpected poison synchronization occurrence count")

PATH.write_text(text, encoding="utf-8", newline="\n")
