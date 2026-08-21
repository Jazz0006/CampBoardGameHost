package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding

/** Explicit per-game production history mode. Legacy chronology is never inferred into Global. */
enum class ClocktowerSemanticHistoryMode {
    LEGACY_LOCAL,
    GLOBAL_V1,
}

internal fun ClocktowerSemanticHistoryMode.requireCompatible(
    observationLog: EpistemicObservationLog,
    nextTimelineGlobalSequence: Long,
) {
    require(nextTimelineGlobalSequence >= 0L) {
        "Semantic-history timeline cursor cannot be negative."
    }

    val firstBinding = observationLog.records.firstOrNull()?.timelineBinding
    when (this) {
        ClocktowerSemanticHistoryMode.LEGACY_LOCAL -> require(
            firstBinding == null || firstBinding === ObservationTimelineBinding.LegacyLocal,
        ) {
            "LegacyLocal semantic history cannot contain Global observations."
        }

        ClocktowerSemanticHistoryMode.GLOBAL_V1 -> {
            require(firstBinding == null || firstBinding is ObservationTimelineBinding.Global) {
                "Global semantic history cannot contain LegacyLocal observations."
            }
            val maxCommittedGlobalSequence = observationLog.records.maxOfOrNull { record ->
                (record.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence
            }
            if (maxCommittedGlobalSequence != null) {
                require(nextTimelineGlobalSequence > maxCommittedGlobalSequence) {
                    "Global timeline cursor must be strictly beyond all committed observations."
                }
            }
        }
    }
}
