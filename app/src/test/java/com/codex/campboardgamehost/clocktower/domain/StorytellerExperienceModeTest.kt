package com.codex.campboardgamehost.clocktower.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerExperienceModeTest {
    @Test
    fun `beginner is zero-decision automatic aggressive presentation`() {
        val policy = StorytellerRecommendationUxPolicy.fromExperienceMode(
            StorytellerExperienceMode.BEGINNER,
        )

        assertTrue(policy.automaticExecution)
        assertEquals(RecommendationStyle.AGGRESSIVE, policy.recommendationStyle)
        assertFalse(policy.showManualAlternatives)
        assertEquals(1, policy.recommendedOptionLimit)
    }

    @Test
    fun `experienced keeps same aggressive recommendation but exposes manual control`() {
        val policy = StorytellerRecommendationUxPolicy.fromExperienceMode(
            StorytellerExperienceMode.EXPERIENCED,
        )

        assertFalse(policy.automaticExecution)
        assertEquals(RecommendationStyle.AGGRESSIVE, policy.recommendationStyle)
        assertTrue(policy.showManualAlternatives)
        assertEquals(3, policy.recommendedOptionLimit)
    }

    @Test
    fun `missing or unknown persisted value defaults to beginner`() {
        assertEquals(StorytellerExperienceMode.BEGINNER, StorytellerExperienceMode.fromPrefsValue(null))
        assertEquals(StorytellerExperienceMode.BEGINNER, StorytellerExperienceMode.fromPrefsValue(""))
        assertEquals(StorytellerExperienceMode.BEGINNER, StorytellerExperienceMode.fromPrefsValue("legacy-auto"))
    }

    @Test
    fun `persisted experienced value explicitly enables experienced mode`() {
        assertEquals(
            StorytellerExperienceMode.EXPERIENCED,
            StorytellerExperienceMode.fromPrefsValue(StorytellerExperienceMode.EXPERIENCED.prefsValue),
        )
    }
}
