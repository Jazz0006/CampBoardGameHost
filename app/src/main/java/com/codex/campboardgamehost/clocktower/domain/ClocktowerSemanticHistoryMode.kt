package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.requireCompatibleWith

/** Explicit per-game production history mode. Legacy chronology is never inferred into Global. */
enum class ClocktowerSemanticHistoryMode {
    LEGACY_LOCAL,
    GLOBAL_V1,
}

internal fun ClocktowerSemanticHistoryMode.requireCompatible(
    actionTimeline: ActionFactTimeline,
    observationLog: EpistemicObservationLog,
    nextTimelineGlobalSequence: Long,
) {
    require(nextTimelineGlobalSequence >= 0L) {
        "Semantic-history timeline cursor cannot be negative."
    }

    val bindings = observationLog.records.map { it.timelineBinding }
    when (this) {
        ClocktowerSemanticHistoryMode.LEGACY_LOCAL -> {
            require(actionTimeline.entries.isEmpty()) {
                "LegacyLocal semantic history cannot contain globally bound actions."
            }
            require(bindings.all { it === ObservationTimelineBinding.LegacyLocal }) {
                "LegacyLocal semantic history cannot contain Global observations."
            }
        }

        ClocktowerSemanticHistoryMode.GLOBAL_V1 -> {
            require(bindings.all { it is ObservationTimelineBinding.Global }) {
                "Global semantic history cannot contain LegacyLocal observations."
            }
            actionTimeline.requireCompatibleWith(observationLog)

            val actionMax = actionTimeline.entries.maxOfOrNull { it.point.globalSequence }
            val observationMax = bindings
                .filterIsInstance<ObservationTimelineBinding.Global>()
                .maxOfOrNull { it.point.globalSequence }
            val maxCommittedGlobalSequence = listOfNotNull(actionMax, observationMax).maxOrNull()
            if (maxCommittedGlobalSequence != null) {
                require(nextTimelineGlobalSequence > maxCommittedGlobalSequence) {
                    "Global timeline cursor must be strictly beyond all committed actions and observations."
                }
            }
        }
    }
}

/** Compatibility overload for adapters that have not yet been wired to durable action capture. */
internal fun ClocktowerSemanticHistoryMode.requireCompatible(
    observationLog: EpistemicObservationLog,
    nextTimelineGlobalSequence: Long,
) = requireCompatible(
    actionTimeline = ActionFactTimeline(),
    observationLog = observationLog,
    nextTimelineGlobalSequence = nextTimelineGlobalSequence,
)
