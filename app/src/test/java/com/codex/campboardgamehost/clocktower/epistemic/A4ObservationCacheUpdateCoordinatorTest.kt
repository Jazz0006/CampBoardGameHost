package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class A4ObservationCacheUpdateCoordinatorTest {
    @Test fun `public reaches all while private remains recipient scoped`() {
        val public = record(ObservationVisibility.PUBLIC, emptySet(), InformationProposition.AliveAt(2, false))
        val private = record(ObservationVisibility.PRIVATE, setOf(2), InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 2, value = 1))
        assertEquals(setOf(1, 2, 3), A4ObservationCacheUpdateCoordinator.affectedSeats(public, setOf(1, 2, 3)))
        assertEquals(setOf(2), A4ObservationCacheUpdateCoordinator.affectedSeats(private, setOf(1, 2, 3)))
    }

    @Test fun `only native structural facts are incrementally filterable`() {
        assertEquals(A4ObservationCacheUpdateStrategy.NATIVE_FILTER, A4ObservationCacheUpdateCoordinator.strategyFor(record(ObservationVisibility.PUBLIC, emptySet(), InformationProposition.RoleInPlay(RoleId("Spy"), false))))
        assertEquals(A4ObservationCacheUpdateStrategy.REPLAY_REQUIRED, A4ObservationCacheUpdateCoordinator.strategyFor(record(ObservationVisibility.PRIVATE, setOf(1), InformationProposition.RoleAt(2, RoleId("Imp")))))
    }

    @Test fun `classifies native updates replay rebuilds and unavailable seats`() {
        val publicAlive = record(ObservationVisibility.PUBLIC, emptySet(), InformationProposition.AliveAt(2, false))
        assertEquals(
            listOf(A4ObservationCacheUpdateOutcome.UPDATED, A4ObservationCacheUpdateOutcome.MISSING),
            A4ObservationCacheUpdateCoordinator.classify(publicAlive, setOf(1, 2), setOf(1), setOf(1, 2)).map { it.outcome },
        )
        val privateRole = record(ObservationVisibility.PRIVATE, setOf(2), InformationProposition.RoleAt(2, RoleId("Imp")))
        assertEquals(A4ObservationCacheUpdateOutcome.REBUILT,
            A4ObservationCacheUpdateCoordinator.classify(privateRole, setOf(1, 2), setOf(1, 2), setOf(1, 2)).single().outcome)
    }

    private fun record(visibility: ObservationVisibility, recipients: Set<Int>, proposition: InformationProposition) = RecordedEpistemicObservation(
        "record-${proposition.hashCode()}", StorytellerPhase.DAY, 1, 0, null, null, visibility, recipients,
        ObservationReliability.NOT_ABILITY_INFORMATION, proposition,
    )
}
