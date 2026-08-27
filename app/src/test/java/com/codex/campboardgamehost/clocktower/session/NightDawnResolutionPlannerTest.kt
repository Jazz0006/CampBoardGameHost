package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NightDawnResolutionPlannerTest {
    @Test
    fun `resolved Imp self kill preserves exact confirmed successor until identity confirmation`() {
        val checkpoint = checkpoint(
            demonSuccessorDraftTarget = "Poisoner",
            confirmedDemonSuccessorTarget = "Poisoner",
        )

        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint,
            successionResolution = DemonSuccessionResolution.Choice(setOf(2, 3)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_NEW_DEMON_IDENTITY, transition.continuation)
        assertEquals("Poisoner", transition.checkpoint.pendingNewDemonName)
        assertEquals("Poisoner", transition.checkpoint.confirmedDemonSuccessorTarget)
        assertEquals("Poisoner", transition.checkpoint.demonSuccessorDraftTarget)
        assertNull(transition.dawnCommitIntent)
        assertTrue(!transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `draft successor without confirmation cannot become pending Demon`() {
        val checkpoint = checkpoint(
            demonSuccessorDraftTarget = "Poisoner",
            confirmedDemonSuccessorTarget = null,
        )

        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint,
            successionResolution = DemonSuccessionResolution.Choice(setOf(2, 3)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR, transition.continuation)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertNull(transition.dawnCommitIntent)
        assertTrue(!transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `restored confirmed successor outside current legal choice fails closed`() {
        val checkpoint = checkpoint(
            demonSuccessorDraftTarget = "Monk",
            confirmedDemonSuccessorTarget = "Monk",
        )

        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint,
            successionResolution = DemonSuccessionResolution.Choice(setOf(2)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR, transition.continuation)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertEquals("Monk", transition.checkpoint.confirmedDemonSuccessorTarget)
        assertNull(transition.dawnCommitIntent)
        assertTrue(!transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `identity confirmation materializes exact pending confirmed successor then permits Dawn`() {
        val checkpoint = checkpoint(
            pendingNewDemonName = "Poisoner",
            demonSuccessorDraftTarget = "Poisoner",
            confirmedDemonSuccessorTarget = "Poisoner",
        )

        val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = gameState(),
            checkpoint = checkpoint,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.DAWN, transition.continuation)
        val intent = assertNotNull(transition.dawnCommitIntent)
        assertEquals(1, intent.roleChanges.size)
        assertEquals(2, intent.roleChanges.single().targetSeat)
        assertEquals(RoleId("Imp"), intent.roleChanges.single().roleId)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertNull(transition.checkpoint.confirmedDemonSuccessorTarget)
        assertNull(transition.checkpoint.demonSuccessorDraftTarget)
        assertTrue(transition.outcomeEvaluationAllowed)
    }

    private fun gameState(): GameState = GameState(
        script = ScriptId("TroubleBrewing"),
        seed = 42L,
        players = listOf(
            PlayerState(
                seat = 1,
                name = "Imp",
                actualRole = RoleId("Imp"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.DEMON,
            ),
            PlayerState(
                seat = 2,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
            ),
            PlayerState(
                seat = 3,
                name = "Monk",
                actualRole = RoleId("Monk"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
            ),
        ),
    )

    private fun checkpoint(
        pendingNewDemonName: String? = null,
        demonSuccessorDraftTarget: String? = null,
        confirmedDemonSuccessorTarget: String? = null,
    ): ClocktowerNightCheckpoint = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = "Imp",
        attackDraftTarget = "Imp",
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = pendingNewDemonName,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
