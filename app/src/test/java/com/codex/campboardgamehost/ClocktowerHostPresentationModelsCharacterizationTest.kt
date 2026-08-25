package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerHostPresentationModelsCharacterizationTest {
    @Test
    fun `night step ui defaults preserve legacy empty presentation state`() {
        val step = ClocktowerNightStepUi(
            title = "Test",
            actor = null,
            isRealAction = false,
            reason = "reason",
            storytellerAction = "action",
            tellPlayer = null,
            explanation = "explanation",
        )

        assertEquals(ClocktowerNightAction.None, step.action)
        assertEquals(ClocktowerDisplayKind.None, step.displayKind)
        assertEquals("Test", step.displayTitle)
        assertNull(step.displayPrimary)
        assertNull(step.displaySecondary)
        assertNull(step.displayFooter)
        assertNull(step.displayProposition)
        assertTrue(step.displayOptions.isEmpty())
        assertTrue(step.recommendedDisplayOptions.isEmpty())
        assertTrue(step.legacyInformationCandidates.isEmpty())
        assertTrue(step.decisionOptions.isEmpty())
        assertNull(step.wakeText)
        assertNull(step.roleEnName)
        assertEquals(
            com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability.RELIABLE,
            step.informationReliability,
        )
        assertEquals(0, step.recentMisinformationStreak)
        assertNull(step.previousShownNumber)
        assertNull(step.selectedInformationTruthful)
        assertNull(step.informationDecisionConfirmation)
        assertNull(step.informationDecisionExpectedSnapshot)
        assertNull(step.spyRegistrationKey)
        assertTrue(step.spyRegistrationTeams.isEmpty())
        assertEquals(ClocktowerRegistrationDetail.Role, step.spyRegistrationDetail)
        assertNull(step.spyRegistrationHint)
        assertNull(step.recluseRegistrationKey)
        assertTrue(step.recluseRegistrationTeams.isEmpty())
    }

    @Test
    fun `registration and pair information enums preserve legacy values`() {
        assertEquals(listOf("AlignmentOnly", "Role"), ClocktowerRegistrationDetail.entries.map { it.name })
        assertEquals(
            listOf("Washerwoman", "Librarian", "Investigator"),
            ClocktowerPairInformationAbility.entries.map { it.name },
        )
    }

    @Test
    fun `recommendation reason labels preserve language and unknown fallback`() {
        assertEquals(
            "Distance from truth fits the selected style",
            recommendationReasonLabel("truth-distance", "en"),
        )
        assertEquals(
            "结果与真实值的距离符合当前风格",
            recommendationReasonLabel("truth-distance", "zh"),
        )
        assertEquals("unknown-code", recommendationReasonLabel("unknown-code", "en"))
        assertFalse(recommendationReasonLabel("special-registration", "en").isBlank())
    }
}
