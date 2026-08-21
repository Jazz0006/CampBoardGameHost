package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ClocktowerNightCheckpointTest {
    @Test fun `unfinished night restores confirmed facts drafts and timeline cursor without conflating them`() {
        val interrupted = ClocktowerNightCheckpoint(
            phaseName = "Night",
            round = 3,
            gameStateRevision = 12,
            playerInputRevision = 7,
            nightStarted = true,
            nightStepIndex = 5,
            confirmedAttackTarget = "Player 8",
            attackDraftTarget = "Player 1",
            confirmedPoisonTarget = "Player 2",
            poisonDraftTarget = "Player 3",
            confirmedMonkTarget = "Player 4",
            monkDraftTarget = "Player 5",
            confirmedMayorRedirectTarget = "Player 6",
            mayorRedirectDraftTarget = "Player 7",
            pendingNewDemonName = "Player 7",
            demonSuccessorDraftTarget = "Player 7",
            nextTimelineGlobalSequence = 17L,
        )

        val restored = ClocktowerNightCheckpoint.fromPersistedValues(interrupted.persistedValues())

        assertEquals(interrupted, restored)
    }

    @Test fun `legacy night save promotes old single targets and starts timeline cursor at zero`() {
        val restored = ClocktowerNightCheckpoint.fromPersistedValues(mapOf(
            "clocktowerPhase" to "Night",
            "round" to 2,
            "clocktowerPendingNightDeath" to "Player 8",
            "clocktowerPoisonTarget" to "Player 2",
            "clocktowerMonkProtectedTarget" to "Player 3",
            "clocktowerMayorRedirectTarget" to "Player 4",
        ))

        assertEquals("Player 8", restored.confirmedAttackTarget)
        assertEquals("Player 8", restored.attackDraftTarget)
        assertEquals("Player 2", restored.confirmedPoisonTarget)
        assertEquals("Player 3", restored.confirmedMonkTarget)
        assertEquals("Player 4", restored.confirmedMayorRedirectTarget)
        assertEquals(0L, restored.nextTimelineGlobalSequence)
    }

    @Test fun `negative persisted timeline cursor is rejected instead of normalized`() {
        try {
            ClocktowerNightCheckpoint.fromPersistedValues(mapOf(
                "clocktowerPhase" to "Night",
                "round" to 2,
                "clocktowerNextTimelineGlobalSequence" to -1L,
            ))
            fail("negative persisted timeline cursor must be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected: persisted global identity state is fail-closed.
        }
    }
}
