package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Session successor selection, live night projection and Dawn planning share accepted mechanics. */
class NightTransactionHostIntegrationSmokeTest {
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")
    private val canonicalPlan = listOf(impInteraction, successorInteraction, empathInteraction)

    @Test
    fun `confirmed successor survives Previous and typed reconstruction`() {
        val baseGameState = gameState()
        val initial = checkpoint(nightStepIndex = 2)

        val session = ClocktowerGameSession.createProduction("successor", baseGameState.seed, baseGameState)
        session.confirmDemonSuccessor(2)
        val confirmed = initial.copy(confirmedDemonSuccessorTarget =
            session.state.nightTargets.successorSeat?.let { baseGameState.playerAt(it)?.name })
        val previous = confirmed.copy(nightStepIndex = confirmed.nightStepIndex - 1)
        assertEquals(1, previous.nightStepIndex)
        assertEquals("Poisoner", previous.confirmedDemonSuccessorTarget)

        val reconstruction = NightTransactionRestoreComposition.compose(
            baseGameState = baseGameState,
            checkpoint = previous,
            canonicalInteractionIds = canonicalPlan,
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(successorInteraction, reconstruction.currentInteractionId)
        assertFalse(reconstruction.effectiveState.isMechanicallyAlive(1))
        assertEquals(RoleId("Imp"), reconstruction.effectiveState.currentRoleId(2))
        assertTrue(baseGameState.playerAt(1)?.alive == true)
        assertEquals(RoleId("Poisoner"), baseGameState.playerAt(2)?.actualRole)
    }

    @Test
    fun `confirmed host transaction reaches Dawn planner with explicit obsolete Poisoner clear`() {
        val baseGameState = gameState()
        val initial = checkpoint(
            nightStepIndex = 2,
            confirmedPoisonTarget = "Empath",
        )
        val session = ClocktowerGameSession.createProduction("successor-poison", baseGameState.seed, baseGameState)
        session.confirmDemonSuccessor(2)
        val confirmed = initial.copy(confirmedDemonSuccessorTarget =
            session.state.nightTargets.successorSeat?.let { baseGameState.playerAt(it)?.name })
        val reconstruction = NightTransactionRestoreComposition.compose(
            baseGameState = baseGameState,
            checkpoint = confirmed,
            canonicalInteractionIds = canonicalPlan,
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        val succession = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = baseGameState,
            checkpoint = confirmed,
            successionResolution = DemonSuccessionResolution.Choice(setOf(2)),
            demonRoleId = RoleId("Imp"),
        )
        assertEquals(NightResolutionContinuation.AWAIT_NEW_DEMON_IDENTITY, succession.continuation)
        assertEquals("Poisoner", succession.checkpoint.pendingNewDemonName)

        val dawn = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = baseGameState,
            checkpoint = succession.checkpoint,
            demonRoleId = RoleId("Imp"),
            poisonResolutionInput = NightDawnPoisonResolutionInput(
                poisonerSeat = 2,
                poisonerRoleId = RoleId("Poisoner"),
                effectiveNightState = reconstruction.effectiveState,
            ),
        )

        assertEquals(NightResolutionContinuation.DAWN, dawn.continuation)
        assertEquals(listOf(DawnRoleChangeIntent(2, RoleId("Imp"))), dawn.dawnCommitIntent?.roleChanges)
        assertEquals(3, dawn.dawnCommitIntent?.poisonCarry?.previousTargetSeat)
        assertEquals(null, dawn.dawnCommitIntent?.poisonCarry?.targetSeat)
        assertTrue(dawn.outcomeEvaluationAllowed)
        assertEquals(RoleId("Poisoner"), baseGameState.playerAt(2)?.actualRole)
        assertTrue(baseGameState.playerAt(3)?.alive == true)
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
                alive = true,
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
                name = "Empath",
                actualRole = RoleId("Empath"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 17L,
    )

    private fun checkpoint(
        nightStepIndex: Int,
        confirmedPoisonTarget: String? = null,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = nightStepIndex,
        confirmedAttackTarget = "Imp",
        attackDraftTarget = "Imp",
        confirmedPoisonTarget = confirmedPoisonTarget,
        poisonDraftTarget = confirmedPoisonTarget,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = null,
        confirmedDemonSuccessorTarget = null,
        nextTimelineGlobalSequence = 17L,
    )
}
