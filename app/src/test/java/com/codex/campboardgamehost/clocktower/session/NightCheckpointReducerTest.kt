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

    @Test
    fun `editing Poison draft does not invalidate confirmed successor`() {
        val checkpoint = checkpoint(
            confirmedPoisonTarget = "Player 2",
            poisonDraftTarget = "Player 2",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.EditPoisonDraft("Player 6"),
        )

        assertEquals("Player 2", reduced.confirmedPoisonTarget)
        assertEquals("Player 6", reduced.poisonDraftTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `reconfirming changed Poison commits draft and invalidates dependent successor`() {
        val checkpoint = checkpoint(
            confirmedPoisonTarget = "Player 2",
            poisonDraftTarget = "Player 6",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmPoison,
        )

        assertEquals("Player 6", reduced.confirmedPoisonTarget)
        assertNull(reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `reconfirming unchanged Poison preserves dependent successor`() {
        val checkpoint = checkpoint(
            confirmedPoisonTarget = "Player 2",
            poisonDraftTarget = "Player 2",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmPoison,
        )

        assertEquals("Player 2", reduced.confirmedPoisonTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `editing Monk draft does not invalidate confirmed successor`() {
        val checkpoint = checkpoint(
            confirmedMonkTarget = "Player 3",
            monkDraftTarget = "Player 3",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.EditMonkProtectionDraft("Player 8"),
        )

        assertEquals("Player 3", reduced.confirmedMonkTarget)
        assertEquals("Player 8", reduced.monkDraftTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `reconfirming changed Monk protection commits draft and invalidates dependent successor`() {
        val checkpoint = checkpoint(
            confirmedMonkTarget = "Player 3",
            monkDraftTarget = "Player 8",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmMonkProtection,
        )

        assertEquals("Player 8", reduced.confirmedMonkTarget)
        assertNull(reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `reconfirming unchanged Monk protection preserves dependent successor`() {
        val checkpoint = checkpoint(
            confirmedMonkTarget = "Player 3",
            monkDraftTarget = "Player 3",
            confirmedDemonSuccessorTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmMonkProtection,
        )

        assertEquals("Player 3", reduced.confirmedMonkTarget)
        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
    }

    @Test
    fun `editing Mayor redirect draft leaves confirmed redirect unchanged`() {
        val checkpoint = checkpoint(
            confirmedMayorRedirectTarget = "Player 5",
            mayorRedirectDraftTarget = "Player 5",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.EditMayorRedirectDraft("Player 6"),
        )

        assertEquals("Player 5", reduced.confirmedMayorRedirectTarget)
        assertEquals("Player 6", reduced.mayorRedirectDraftTarget)
    }

    @Test
    fun `confirming Mayor redirect commits the current draft`() {
        val checkpoint = checkpoint(
            confirmedMayorRedirectTarget = "Player 5",
            mayorRedirectDraftTarget = "Player 6",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmMayorRedirect,
        )

        assertEquals("Player 6", reduced.confirmedMayorRedirectTarget)
        assertEquals("Player 6", reduced.mayorRedirectDraftTarget)
    }

    @Test
    fun `editing Demon successor draft leaves confirmed successor unchanged`() {
        val checkpoint = checkpoint(
            confirmedDemonSuccessorTarget = "Player 7",
            demonSuccessorDraftTarget = "Player 7",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.EditDemonSuccessorDraft("Player 8"),
        )

        assertEquals("Player 7", reduced.confirmedDemonSuccessorTarget)
        assertEquals("Player 8", reduced.demonSuccessorDraftTarget)
    }

    @Test
    fun `confirming Demon successor commits the current draft`() {
        val checkpoint = checkpoint(
            confirmedDemonSuccessorTarget = "Player 7",
            demonSuccessorDraftTarget = "Player 8",
        )

        val reduced = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmDemonSuccessor,
        )

        assertEquals("Player 8", reduced.confirmedDemonSuccessorTarget)
        assertEquals("Player 8", reduced.demonSuccessorDraftTarget)
    }

    private fun checkpoint(
        nightStepIndex: Int = 4,
        confirmedAttackTarget: String? = null,
        attackDraftTarget: String? = null,
        confirmedPoisonTarget: String? = "Player 2",
        poisonDraftTarget: String? = "Player 2",
        confirmedMonkTarget: String? = "Player 3",
        monkDraftTarget: String? = "Player 3",
        confirmedMayorRedirectTarget: String? = null,
        mayorRedirectDraftTarget: String? = confirmedMayorRedirectTarget,
        confirmedDemonSuccessorTarget: String? = null,
        demonSuccessorDraftTarget: String? = confirmedDemonSuccessorTarget,
    ): ClocktowerNightCheckpoint = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = nightStepIndex,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = attackDraftTarget,
        confirmedPoisonTarget = confirmedPoisonTarget,
        poisonDraftTarget = poisonDraftTarget,
        confirmedMonkTarget = confirmedMonkTarget,
        monkDraftTarget = monkDraftTarget,
        confirmedMayorRedirectTarget = confirmedMayorRedirectTarget,
        mayorRedirectDraftTarget = mayorRedirectDraftTarget,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
