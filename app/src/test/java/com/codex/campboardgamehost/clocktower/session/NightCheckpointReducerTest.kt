package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NightCheckpointReducerTest {
    @Test
    fun `previous only moves navigation and preserves confirmed mechanics`() {
        val checkpoint = checkpoint(
            nightStepIndex = 6,
            confirmedAttackTarget = "Player 4",
            attackDraftTarget = "Player 4",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.MovePrevious,
        )

        assertEquals(5, reduced.nightStepIndex)
        assertEquals("Player 4", reduced.confirmedAttackTarget)
        assertEquals("Player 4", reduced.attackDraftTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `editing Demon attack draft does not invalidate confirmed successor`() {
        val checkpoint = checkpoint(
            confirmedAttackTarget = "Player 4",
            attackDraftTarget = "Player 4",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.EditDemonAttackDraft("Player 5"),
        )

        assertEquals("Player 4", reduced.confirmedAttackTarget)
        assertEquals("Player 5", reduced.attackDraftTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `reconfirming changed Demon attack commits draft and invalidates dependent successor`() {
        val checkpoint = checkpoint(
            confirmedAttackTarget = "Player 4",
            attackDraftTarget = "Player 5",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmDemonAttack,
        )

        assertEquals("Player 5", reduced.confirmedAttackTarget)
        assertEquals("Player 5", reduced.attackDraftTarget)
        assertNull(reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `reconfirming unchanged Demon attack preserves dependent successor`() {
        val checkpoint = checkpoint(
            confirmedAttackTarget = "Player 4",
            attackDraftTarget = "Player 4",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmDemonAttack,
        )

        assertEquals("Player 4", reduced.confirmedAttackTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    private fun checkpoint(
        nightStepIndex: Int = 4,
        confirmedAttackTarget: String? = null,
        attackDraftTarget: String? = null,
        confirmedDemonSuccessorTarget: String? = null,
    ): ClocktowerNightCheckpoint = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = nightStepIndex,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = attackDraftTarget,
        confirmedPoisonTarget = "Player 2",
        poisonDraftTarget = "Player 2",
        confirmedMonkTarget = "Player 3",
        monkDraftTarget = "Player 3",
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = confirmedDemonSuccessorTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
