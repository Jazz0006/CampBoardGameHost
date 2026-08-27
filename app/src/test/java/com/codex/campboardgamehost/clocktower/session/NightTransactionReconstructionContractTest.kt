package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * SNE-7.5 typed contracts for restore/reconstruction safety.
 *
 * These contracts are now active against the real reconstruction boundary. Each reconstruction
 * behavior must fail closed from durable checkpoint + canonical-plan inputs without replaying UI
 * commands or mutating public/base GameState.
 */
class NightTransactionReconstructionContractTest {
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")

    @Test
    fun `out of range restored nightStepIndex fails closed without stale role effect`() {
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                nightStepIndex = 99,
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertNull(reconstruction.currentInteractionId)
        assertEquals(RoleId("Poisoner"), reconstruction.effectiveState.currentRoleId(2))
    }

    @Test
    fun `missing successor interaction ignores stale confirmed role effect instead of crashing`() {
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                nightStepIndex = 1,
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            canonicalInteractionIds = listOf(impInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(empathInteraction, reconstruction.currentInteractionId)
        assertEquals(RoleId("Poisoner"), reconstruction.effectiveState.currentRoleId(2))
    }

    @Test
    fun `draft only restored successor never becomes effective Demon`() {
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                nightStepIndex = 2,
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = null,
            ),
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(empathInteraction, reconstruction.currentInteractionId)
        assertEquals(RoleId("Poisoner"), reconstruction.effectiveState.currentRoleId(2))
    }

    @Test
    fun `stale confirmed non Minion successor fails closed without role effect`() {
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                nightStepIndex = 2,
                demonSuccessorDraftTarget = "Empath",
                confirmedDemonSuccessorTarget = "Empath",
            ),
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(empathInteraction, reconstruction.currentInteractionId)
        assertEquals(RoleId("Empath"), reconstruction.effectiveState.currentRoleId(3))
    }

    @Test
    fun `valid confirmed successor reconstructs effective Demon from same durable inputs`() {
        val input = checkpoint(
            nightStepIndex = 2,
            demonSuccessorDraftTarget = "Poisoner",
            confirmedDemonSuccessorTarget = "Poisoner",
        )
        val persisted = input.persistedValues()
        val restoredFirst = ClocktowerNightCheckpoint.fromPersistedValues(persisted)
        val restoredSecond = ClocktowerNightCheckpoint.fromPersistedValues(persisted)
        val canonicalPlan = listOf(impInteraction, successorInteraction, empathInteraction)

        val first = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = restoredFirst,
            canonicalInteractionIds = canonicalPlan,
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )
        val second = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = restoredSecond,
            canonicalInteractionIds = canonicalPlan,
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(first, second)
        assertEquals(empathInteraction, first.currentInteractionId)
        assertEquals(RoleId("Imp"), first.effectiveState.currentRoleId(2))
        assertEquals(RoleId("Poisoner"), gameState().playerAt(2)?.actualRole)
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
                name = "Empath",
                actualRole = RoleId("Empath"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 11L,
    )

    private fun checkpoint(
        nightStepIndex: Int,
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
