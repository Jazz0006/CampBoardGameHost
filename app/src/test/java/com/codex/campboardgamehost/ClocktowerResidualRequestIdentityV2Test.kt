package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Assisted adoption is never a transferable automatic-mode authorization. */
class ClocktowerResidualRequestIdentityV2Test {
    private fun key(
        mode: StorytellerOperationMode,
        revision: Long = 3,
        sequence: Long = 9,
    ) = ClocktowerResidualRequestIdentityV2.build(
        gameId = "same-game", round = 2, stepIndex = 3,
        requestId = "host-open-current", gameStateRevision = revision,
        playerInputRevision = 5, exclusiveGlobalSequence = sequence, mode = mode,
    )

    @Test fun modeSwitchAlwaysInvalidatesPreviouslyAcceptedJointPackage() {
        val assisted = key(StorytellerOperationMode.AI_ASSISTED)
        val automatic = key(StorytellerOperationMode.AI_AUTOMATIC)
        assertNotEquals(assisted, automatic)
        assertEquals(assisted, key(StorytellerOperationMode.AI_ASSISTED))
        assertEquals(automatic, key(StorytellerOperationMode.AI_AUTOMATIC))
    }

    @Test fun HostCandidateAndChronologyChangesInvalidateRequestIdentity() {
        val original = key(StorytellerOperationMode.AI_AUTOMATIC)
        assertNotEquals(original, key(StorytellerOperationMode.AI_AUTOMATIC,
            revision = 4))
        assertNotEquals(original, key(StorytellerOperationMode.AI_AUTOMATIC,
            sequence = 10))
        assertTrue(runCatching {
            key(StorytellerOperationMode.MANUAL)
        }.isFailure)
    }
}
