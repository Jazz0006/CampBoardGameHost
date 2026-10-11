package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerOperationModeContractTest {
    @Test
    fun `manual mode never consumes an AI response`() {
        for (mode in StorytellerOperationMode.entries) {
            assertFalse(storytellerAiResponseMayApply(mode, StorytellerOperationMode.MANUAL))
        }
    }

    @Test
    fun `switching from assisted to automatic cannot escalate an old response`() {
        assertFalse(storytellerAiResponseMayApply(
            StorytellerOperationMode.AI_ASSISTED, StorytellerOperationMode.AI_AUTOMATIC,
        ))
    }

    @Test
    fun `switching from automatic to assisted cannot commit the old automatic response`() {
        assertFalse(storytellerAiResponseMayApply(
            StorytellerOperationMode.AI_AUTOMATIC, StorytellerOperationMode.AI_ASSISTED,
        ))
    }

    @Test
    fun `unchanged explicit AI mode may process its current response`() {
        assertTrue(storytellerAiResponseMayApply(
            StorytellerOperationMode.AI_ASSISTED, StorytellerOperationMode.AI_ASSISTED,
        ))
        assertTrue(storytellerAiResponseMayApply(
            StorytellerOperationMode.AI_AUTOMATIC, StorytellerOperationMode.AI_AUTOMATIC,
        ))
    }
}
