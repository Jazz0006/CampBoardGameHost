package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SNE-7.1 typed RED contracts for the Dawn planner.
 *
 * The planner computes transition/commit intent only. It must not mutate GameState or become a
 * second timeline/persistence authority.
 */
class NightDawnResolutionPlannerContractTest {
    @Test
    fun `legal confirmed Imp successor is preserved until identity confirmation`() {
        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            successionResolution = DemonSuccessionResolution.Choice(setOf(2, 3)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_NEW_DEMON_IDENTITY, transition.continuation)
        assertEquals("Poisoner", transition.checkpoint.pendingNewDemonName)
        assertEquals("Poisoner", transition.checkpoint.demonSuccessorDraftTarget)
        assertEquals("Poisoner", transition.checkpoint.confirmedDemonSuccessorTarget)
        assertNull(transition.dawnCommitIntent)
        assertFalse(transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `successor draft without confirmation cannot become pending Demon`() {
        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = null,
            ),
            successionResolution = DemonSuccessionResolution.Choice(setOf(2, 3)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR, transition.continuation)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertNull(transition.dawnCommitIntent)
        assertFalse(transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `restored confirmed successor outside current legal choice fails closed`() {
        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Monk",
                confirmedDemonSuccessorTarget = "Monk",
            ),
            successionResolution = DemonSuccessionResolution.Choice(setOf(2)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR, transition.continuation)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertEquals("Monk", transition.checkpoint.confirmedDemonSuccessorTarget)
        assertNull(transition.dawnCommitIntent)
        assertFalse(transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `identity confirmation materializes exact pending successor then permits Dawn`() {
        val base = gameState()
        val checkpoint = checkpoint(
            pendingNewDemonName = "Poisoner",
            demonSuccessorDraftTarget = "Poisoner",
            confirmedDemonSuccessorTarget = "Poisoner",
        )

        val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = base,
            checkpoint = checkpoint,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(base, gameState())
        assertEquals(NightResolutionContinuation.DAWN, transition.continuation)
        assertEquals(1, transition.dawnCommitIntent?.roleChanges?.size)
        assertEquals(2, transition.dawnCommitIntent?.roleChanges?.single()?.targetSeat)
        assertEquals(RoleId("Imp"), transition.dawnCommitIntent?.roleChanges?.single()?.roleId)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertNull(transition.checkpoint.demonSuccessorDraftTarget)
        assertNull(transition.checkpoint.confirmedDemonSuccessorTarget)
        assertTrue(transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `Poisoner successor identity confirmation ends poison in the same Dawn transaction`() {
        val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                pendingNewDemonName = "Poisoner",
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            demonRoleId = RoleId("Imp"),
            poisonResolutionInput = NightDawnPoisonResolutionInput(
                poisonerSeat = 2,
                poisonerRoleId = RoleId("Poisoner"),
                effectiveNightState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = setOf(2, 3),
                    effectiveRoleIdsBySeat = mapOf(
                        2 to RoleId("Poisoner"),
                        3 to RoleId("Monk"),
                    ),
                ),
            ),
        )

        val intent = transition.dawnCommitIntent
        assertEquals(NightResolutionContinuation.DAWN, transition.continuation)
        assertEquals(1, intent?.roleChanges?.size)
        assertEquals(2, intent?.roleChanges?.single()?.targetSeat)
        assertEquals(RoleId("Imp"), intent?.roleChanges?.single()?.roleId)
        assertNull(intent?.poisonCarry)
        assertTrue(transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `pending Demon identity keeps outcome evaluation closed`() {
        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            successionResolution = DemonSuccessionResolution.Choice(setOf(2, 3)),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_NEW_DEMON_IDENTITY, transition.continuation)
        assertFalse(transition.outcomeEvaluationAllowed)
        assertNull(transition.dawnCommitIntent)
    }

    private fun gameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            PlayerState(
                seat = 1,
                name = "Imp",
                actualRole = RoleId("Imp"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.DEMON,
                alive = false,
            ),
            PlayerState(
                seat = 2,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = true,
            ),
            PlayerState(
                seat = 3,
                name = "Monk",
                actualRole = RoleId("Monk"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 7L,
    )

    private fun checkpoint(
        pendingNewDemonName: String? = null,
        demonSuccessorDraftTarget: String? = null,
        confirmedDemonSuccessorTarget: String? = null,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = "Imp",
        attackDraftTarget = "Imp",
        confirmedPoisonTarget = "Monk",
        poisonDraftTarget = "Monk",
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
