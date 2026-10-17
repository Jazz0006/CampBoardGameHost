package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerNumericInformationOptionPreparationTest {
    private val empath = RoleId("Empath")
    private val empathMetric = NumericMetric.LIVING_EVIL_NEIGHBOURS

    @Test
    fun `previously shown unreliable number comes from confirmed typed information`() {
        val observations = listOf(
            shown(0, 1, 1),
            shown(1, 2, 2),
            shown(2, 2, 0, actorSeat = 3),
            shown(3, 2, 1, role = RoleId("Chef")),
            shown(4, 2, 0, reliable = true),
        )
        assertEquals(2, previousClocktowerUnreliableNumber(observations, 2, empath, empathMetric))
        assertEquals(0, previousClocktowerUnreliableNumber(observations, 3, empath, empathMetric))
        assertNull(previousClocktowerUnreliableNumber(observations, 4, empath, empathMetric))
    }

    @Test
    fun `only actual numeric information for the same role and metric counts`() {
        val observations = listOf(
            shown(0, 1, 2, metric = NumericMetric.ADJACENT_EVIL_PAIRS),
            shown(1, 1, 1),
            shown(2, 2, 0, reliable = true),
        )
        assertEquals(1, previousClocktowerUnreliableNumber(observations, 2, empath, empathMetric))
        assertNull(previousClocktowerUnreliableNumber(
            observations.filter { it.proposition is InformationProposition.NumericResult &&
                (it.proposition as InformationProposition.NumericResult).metric == NumericMetric.ADJACENT_EVIL_PAIRS },
            2, empath, empathMetric,
        ))
    }

    private fun shown(
        globalSequence: Long,
        round: Int,
        value: Int,
        actorSeat: Int = 2,
        role: RoleId = empath,
        metric: NumericMetric = empathMetric,
        reliable: Boolean = false,
    ) = RecordedEpistemicObservation(
        recordId = "shown-$globalSequence",
        phase = StorytellerPhase.NIGHT,
        round = round,
        sequence = globalSequence.toInt(),
        sourceSeat = actorSeat,
        sourceAbility = role,
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(actorSeat),
        reliability = if (reliable) ObservationReliability.RECEIVED_AS_FUNCTIONING
            else ObservationReliability.KNOWN_MALFUNCTIONING,
        proposition = InformationProposition.NumericResult(metric, actorSeat, listOf(1, 3), value),
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(StorytellerPhase.NIGHT, round, globalSequence.toInt(), globalSequence)),
    )
}
