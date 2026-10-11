package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerFortuneTellerRuleOnlyAuthorityTest {
    @Test
    fun `manual and assisted never auto choose even a singleton`() {
        for (mode in listOf(StorytellerOperationMode.MANUAL, StorytellerOperationMode.AI_ASSISTED)) {
            assertNull(ruleDeterminedFortuneTellerResult(
                allowRuleDeterminedAutoUi = mode == StorytellerOperationMode.AI_AUTOMATIC,
                legalResults = setOf(true),
            ))
        }
    }

    @Test
    fun `automatic with more than one lawful answer refuses unverified suggested value`() {
        assertNull(ruleDeterminedFortuneTellerResult(true, setOf(false, true)))
    }

    @Test
    fun `automatic preserves sole Host legal result including false`() {
        assertEquals(false, ruleDeterminedFortuneTellerResult(true, setOf(false)))
        assertEquals(true, ruleDeterminedFortuneTellerResult(true, setOf(true)))
        assertNull(ruleDeterminedFortuneTellerResult(true, emptySet()))
    }
}
