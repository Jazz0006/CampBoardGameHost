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

/**
 * SNE-7.6 high-value JVM integration smokes across the real typed Host/session seams.
 *
 * These tests deliberately stop before App-owned durable side effects. They prove that the
 * production Host transaction adapter, checkpoint persistence, reconstruction, and Dawn planner
 * agree on the same confirmed mechanics without introducing a second coordinator/state owner.
 */
class NightTransactionHostIntegrationSmokeTest {
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")
    private val canonicalPlan = listOf(impInteraction, successorInteraction, empathInteraction)

    @Test
    fun `confirmed successor survives Previous persistence and reconstruction`() {
        val baseGameState = gameState()
        val initial = checkpoint(nightStepIndex = 2)

        val edited = NightCheckpointHostTransaction.editDemonSuccessor(initial, "Poisoner")
        assertEquals(NightCheckpointRevisionIntent.PLAYER_INPUT, edited.revisionIntent)

        val confirmed = NightCheckpointHostTransaction.confirmDemonSuccessor(edited.checkpoint)
        assertEquals(NightCheckpointRevisionIntent.GAME_STATE, confirmed.revisionIntent)
        assertEquals("Poisoner", confirmed.checkpoint.confirmedDemonSuccessorTarget)

        val previous = NightCheckpointHostTransaction.movePrevious(confirmed.checkpoint)
        assertEquals(NightCheckpointRevisionIntent.NONE, previous.revisionIntent)
        assertEquals(1, previous.checkpoint.nightStepIndex)
        assertEquals("Poisoner", previous.checkpoint.confirmedDemonSuccessorTarget)

        val restored = ClocktowerNightCheckpoint.fromPersistedValues(previous.checkpoint.persistedValues())
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = baseGameState,
            checkpoint = restored,
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
    fun `restored host transaction reaches Dawn planner with explicit obsolete Poisoner clear`() {
        val baseGameState = gameState()
        val initial = checkpoint(
            nightStepIndex = 2,
            confirmedPoisonTarget = "Empath",
        )
        val confirmed = NightCheckpointHostTransaction.confirmDemonSuccessor(
            NightCheckpointHostTransaction.editDemonSuccessor(initial, "Poisoner").checkpoint,
        )
        val restored = ClocktowerNightCheckpoint.fromPersistedValues(confirmed.checkpoint.persistedValues())
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = baseGameState,
            checkpoint = restored,
            canonicalInteractionIds = canonicalPlan,
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        val succession = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = baseGameState,
            checkpoint = restored,
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
