package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase

/**
 * Knowledge-safe per-world night replay structure.
 *
 * The canonical schedule is rule-derived. Durable observations retain their existing GLOBAL_V1
 * identities and are only associated with the schedule slot after which their received information
 * can constrain the world. No synthetic TimelinePoint or merged total order is created here.
 */
internal data class EnumeratedWorldNightReplayPlan(
    val schedule: List<NightOrderToken>,
    val observationsAfterScheduleIndex: Map<Int, List<RecordedEpistemicObservation>>,
)

internal object EnumeratedWorldNightReplayPlanning {
    fun planAbilityObservationsOrNull(
        ruleset: ValidatedClocktowerRuleset,
        phase: ClocktowerNightFlowPhase,
        world: EnumeratedWorld,
        observations: List<RecordedEpistemicObservation>,
    ): EnumeratedWorldNightReplayPlan? {
        if (observations.map(RecordedEpistemicObservation::round).distinct().size > 1) {
            return null
        }

        val schedule = EnumeratedWorldNightSchedule.plan(
            ruleset = ruleset,
            phase = phase,
            world = world,
        )

        val globallyOrdered = observations.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global ?: return null
            binding.point.globalSequence to record
        }.sortedBy { (globalSequence, _) -> globalSequence }

        if (globallyOrdered.map { (globalSequence, _) -> globalSequence }.distinct().size != globallyOrdered.size) {
            return null
        }

        val anchored = globallyOrdered.map { (_, record) ->
            val anchor = EnumeratedWorldNightObservationAnchoring.anchorOrNull(
                ruleset = ruleset,
                phase = phase,
                world = world,
                record = record,
            ) ?: return null
            anchor to record
        }

        if (anchored.zipWithNext().any { (left, right) ->
                right.first.scheduleIndex < left.first.scheduleIndex
            }
        ) {
            return null
        }

        return EnumeratedWorldNightReplayPlan(
            schedule = schedule,
            observationsAfterScheduleIndex = anchored.groupBy(
                keySelector = { (anchor, _) -> anchor.scheduleIndex },
                valueTransform = { (_, record) -> record },
            ),
        )
    }
}
