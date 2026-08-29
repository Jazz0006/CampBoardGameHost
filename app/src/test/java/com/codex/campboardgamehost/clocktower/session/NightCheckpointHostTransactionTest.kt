package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Test

/** SNE-7.6A typed callable boundary between Host/App callbacks and checkpoint reducer semantics. */
class NightCheckpointHostTransactionTest {
    @Test
    fun `successor edit preserves confirmed mechanics and requests player input revision`() {
        val result = NightCheckpointHostTransaction.editDemonSuccessor(
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            selectedTarget = "Scarlet Woman",
        )

        assertEquals("Scarlet Woman", result.checkpoint.demonSuccessorDraftTarget)
        assertEquals("Poisoner", result.checkpoint.confirmedDemonSuccessorTarget)
        assertEquals(NightCheckpointRevisionIntent.PLAYER_INPUT, result.revisionIntent)
    }

    @Test
    fun `successor confirm commits current draft and requests game state revision only when changed`() {
        val changed = NightCheckpointHostTransaction.confirmDemonSuccessor(
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Scarlet Woman",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
        )
        val unchanged = NightCheckpointHostTransaction.confirmDemonSuccessor(
            checkpoint = changed.checkpoint,
        )

        assertEquals("Scarlet Woman", changed.checkpoint.confirmedDemonSuccessorTarget)
        assertEquals(NightCheckpointRevisionIntent.GAME_STATE, changed.revisionIntent)
        assertEquals(NightCheckpointRevisionIntent.NONE, unchanged.revisionIntent)
    }

    @Test
    fun `Previous changes navigation only and never invalidates confirmed successor`() {
        val result = NightCheckpointHostTransaction.movePrevious(
            checkpoint = checkpoint(
                nightStepIndex = 4,
                demonSuccessorDraftTarget = "Scarlet Woman",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
        )

        assertEquals(3, result.checkpoint.nightStepIndex)
        assertEquals("Scarlet Woman", result.checkpoint.demonSuccessorDraftTarget)
        assertEquals("Poisoner", result.checkpoint.confirmedDemonSuccessorTarget)
        assertEquals(NightCheckpointRevisionIntent.NONE, result.revisionIntent)
    }

    @Test
    fun `Previous at first step remains idempotent without revision`() {
        val result = NightCheckpointHostTransaction.movePrevious(
            checkpoint = checkpoint(
                nightStepIndex = 0,
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
        )

        assertEquals(0, result.checkpoint.nightStepIndex)
        assertEquals("Poisoner", result.checkpoint.confirmedDemonSuccessorTarget)
        assertEquals(NightCheckpointRevisionIntent.NONE, result.revisionIntent)
    }

    private fun checkpoint(
        nightStepIndex: Int = 2,
        demonSuccessorDraftTarget: String?,
        confirmedDemonSuccessorTarget: String?,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = nightStepIndex,
        confirmedAttackTarget = "Imp",
        attackDraftTarget = "Imp",
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
