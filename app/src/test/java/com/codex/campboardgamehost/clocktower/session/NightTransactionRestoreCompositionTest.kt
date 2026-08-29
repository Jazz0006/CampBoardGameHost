package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightCursor
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.ResolvedNightMechanicalEvent
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
        val baseGameState = choiceGameState()
        val persistedValues = checkpoint(
            demonSuccessorDraftTarget = "Poisoner",
            confirmedDemonSuccessorTarget = "Poisoner",
        ).persistedValues()

        val restored = restore(persistedValues, baseGameState)

        assertEquals(ClocktowerNightCheckpoint.fromPersistedValues(persistedValues), restored.checkpoint)
        assertEquals(empathInteraction, restored.reconstruction.currentInteractionId)
        assertFalse(restored.reconstruction.effectiveState.isMechanicallyAlive(1))
        assertEquals(RoleId("Imp"), restored.reconstruction.effectiveState.currentRoleId(2))

        assertTrue(baseGameState.playerAt(1)?.alive == true)
        assertEquals(RoleId("Imp"), baseGameState.playerAt(1)?.actualRole)
        assertEquals(RoleId("Poisoner"), baseGameState.playerAt(2)?.actualRole)
    }

    @Test
    fun `restored forced Scarlet Woman succession exposes canonical events without base mutation`() {
        val baseGameState = forcedGameState()
        val persistedValues = checkpoint(
            pendingNewDemonName = "Scarlet Woman",
            demonSuccessorDraftTarget = null,
            confirmedDemonSuccessorTarget = null,
        ).persistedValues()

        val restored = restore(persistedValues, baseGameState)

        assertEquals(
            listOf(
                ResolvedNightMechanicalEvent.MechanicalDeath(
                    targetSeat = 1,
                    effectiveAt = ClocktowerEffectiveNightCursor(
                        interactionId = successorInteraction,
                        boundary = ClocktowerInteractionBoundary.BEFORE,
                    ),
                ),
                ResolvedNightMechanicalEvent.RoleChanged(
                    targetSeat = 2,
                    roleId = RoleId("Imp"),
                    effectiveAt = ClocktowerEffectiveNightCursor(
                        interactionId = successorInteraction,
                        boundary = ClocktowerInteractionBoundary.AFTER,
                    ),
                ),
            ),
            restored.reconstruction.confirmedEvents,
        )
        assertEquals(empathInteraction, restored.reconstruction.currentInteractionId)
        assertFalse(restored.reconstruction.effectiveState.isMechanicallyAlive(1))
        assertEquals(RoleId("Imp"), restored.reconstruction.effectiveState.currentRoleId(2))
        assertEquals(RoleId("Poisoner"), restored.reconstruction.effectiveState.currentRoleId(3))

        assertTrue(baseGameState.playerAt(1)?.alive == true)
        assertEquals(RoleId("Scarlet Woman"), baseGameState.playerAt(2)?.actualRole)
        assertEquals(RoleId("Poisoner"), baseGameState.playerAt(3)?.actualRole)
    }

    private fun restore(
        persistedValues: Map<String, Any?>,
        baseGameState: GameState,
    ) = NightTransactionRestoreComposition.restore(
        persistedCheckpointValues = persistedValues,
        baseGameState = baseGameState,
        canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
        demonSuccessorInteractionId = successorInteraction,
        demonRoleId = RoleId("Imp"),
    )

    private fun choiceGameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            player(1, "Imp", "Imp", CharacterType.DEMON, Alignment.EVIL),
            player(2, "Poisoner", "Poisoner", CharacterType.MINION, Alignment.EVIL),
            player(3, "Empath", "Empath", CharacterType.TOWNSFOLK, Alignment.GOOD),
        ),
        seed = 17L,
    )

    private fun forcedGameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            player(1, "Imp", "Imp", CharacterType.DEMON, Alignment.EVIL),
            player(2, "Scarlet Woman", "Scarlet Woman", CharacterType.MINION, Alignment.EVIL),
            player(3, "Poisoner", "Poisoner", CharacterType.MINION, Alignment.EVIL),
            player(4, "Empath", "Empath", CharacterType.TOWNSFOLK, Alignment.GOOD),
            player(5, "Monk", "Monk", CharacterType.TOWNSFOLK, Alignment.GOOD),
        ),
        seed = 19L,
    )

    private fun player(
        seat: Int,
        name: String,
        role: String,
        type: CharacterType,
        alignment: Alignment,
    ) = PlayerState(
        seat = seat,
        name = name,
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
        alive = true,
    )

    private fun checkpoint(
        pendingNewDemonName: String? = null,
        demonSuccessorDraftTarget: String?,
        confirmedDemonSuccessorTarget: String?,
    ) = ClocktowerNightCheckpoint(
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
        pendingNewDemonName = pendingNewDemonName,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
