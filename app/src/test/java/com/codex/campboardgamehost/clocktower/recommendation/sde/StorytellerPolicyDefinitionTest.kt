package com.codex.campboardgamehost.clocktower.recommendation.sde

import org.junit.Assert.assertEquals
import org.junit.Test

class StorytellerPolicyDefinitionTest {
    @Test
    fun `production V1 definition freezes version evidence checkpoint and selector method`() {
        val definition = StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1

        assertEquals(PolicyVersions.BEGINNER_CONSERVATIVE_V1, definition.policyVersion)
        assertEquals(
            EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
            definition.evidenceCheckpoint,
        )
        assertEquals(PolicySelectionMethod.SEEDED_HASH_V1, definition.selectionMethod)
    }
}
