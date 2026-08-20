package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class A4DeviceBenchmarkHarnessTest {
    @Test fun `report separates native and fallback filter paths in a pasteable line`() {
        val snapshot = A4RuntimeFixtures.snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val knowledge = PlayerKnowledgeSnapshot("device-benchmark-knowledge", formal.snapshotId, 1, RoleId("Chef"))
        val report = A4DeviceBenchmarkHarness.run(
            deviceLabel = "test-device",
            formal = formal,
            knowledge = knowledge,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            cases = listOf(
                A4DeviceBenchmarkCase("alive", publicObservation(formal, InformationProposition.AliveAt(2, true)), ZddFilterStrategy.NATIVE_RESTRICTION),
                A4DeviceBenchmarkCase("chef", privateChefObservation(formal), ZddFilterStrategy.DECODE_REBUILD),
            ),
            samples = 3,
        )

        assertTrue(report.nodeCount > 0)
        assertEquals(report.worldCardinality.value.toLong(), report.constructionPhases.exactWorldCount)
        assertEquals(2, report.filters.size)
        assertTrue(report.toLogLine().contains("generationP50P95Us="))
        assertTrue(report.toLogLine().contains("prefixInsertP50P95Us="))
        assertTrue(report.toLogLine().contains("canonicalizeP50P95Us="))
        assertTrue(report.toLogLine().contains("filter[alive]=NATIVE_RESTRICTION"))
        assertTrue(report.toLogLine().contains("filter[chef]=DECODE_REBUILD"))
        assertTrue(report.toLogLine().contains("retainedWorlds="))
        assertTrue(report.toLogLine().contains("evalP50P95Us="))
        assertTrue(report.toLogLine().contains("rebuildP50P95Us="))
    }

    private fun publicObservation(formal: FormalGameState, proposition: InformationProposition) = EpistemicObservation(
        "device-public", formal.snapshotId, StorytellerPhase.FIRST_NIGHT, 1, 1, null, null,
        ObservationVisibility.PUBLIC, emptySet(), ObservationReliability.NOT_ABILITY_INFORMATION, proposition,
    )

    private fun privateChefObservation(formal: FormalGameState) = EpistemicObservation(
        "device-chef", formal.snapshotId, StorytellerPhase.FIRST_NIGHT, 1, 2, 1, RoleId("Chef"),
        ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
        InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 1),
    )
}
