package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SNE-7.9E: persisted unfinished-night state must activate derived mechanics on restore. */
class NightTransactionRestoreCompositionTest {
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")

    @Test
    fun `restored confirmed Imp self kill reconstructs derived successor state without mutating base game`() {
        val baseGameState = gameState()
        val persistedValues = checkpoint().persistedValues()

        val restored = NightTransactionRestoreComposition.restore(
            persistedCheckpointValues = persistedValues,
            baseGameState = baseGameState,
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(ClocktowerNightCheckpoint.fromPersistedValues(persistedValues), restored.checkpoint)
        assertEquals(empathInteraction, restored.reconstruction.currentInteractionId)
        assertFalse(restored.reconstruction.effectiveState.isMechanicallyAlive(1))
        assertEquals(RoleId("Imp"), restored.reconstruction.effectiveState.currentRoleId(2))

        assertTrue(baseGameState.playerAt(1)?.alive == true)
        assertEquals(RoleId("Imp"), baseGameState.playerAt(1)?.actualRole)
        assertEquals(RoleId("Poisoner"), baseGameState.playerAt(2)?.actualRole)
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

    private fun checkpoint() = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 2,
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
        demonSuccessorDraftTarget = "Poisoner",
        confirmedDemonSuccessorTarget = "Poisoner",
        nextTimelineGlobalSequence = 17L,
    )
}
