package com.codex.campboardgamehost.clocktower.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StorytellerRecommendationUxPolicyTest {
    @Test
    fun `normal product ux ignores every legacy global mode`() {
        StorytellerAutomationMode.entries.forEach { legacyMode ->
            val policy = StorytellerRecommendationUxPolicy.fromLegacyMode(legacyMode)

            assertFalse(policy.automaticExecution)
            assertEquals(RecommendationStyle.BALANCED, policy.recommendationStyle)
        }
    }

    @Test
    fun `legacy automatic preferences cannot restore automatic execution`() {
        val legacyAutomaticModes = StorytellerAutomationMode.entries.filter { it.isAutomatic }
        assertEquals(3, legacyAutomaticModes.size)

        legacyAutomaticModes.forEach { legacyMode ->
            val policy = StorytellerRecommendationUxPolicy.fromLegacyMode(legacyMode)

            assertFalse(policy.automaticExecution)
            assertEquals(RecommendationStyle.BALANCED, policy.recommendationStyle)
        }
    }
}
