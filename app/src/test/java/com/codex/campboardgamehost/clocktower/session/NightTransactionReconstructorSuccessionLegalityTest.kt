package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import org.junit.Assert.assertEquals
import org.junit.Test

/** SNE-7.9D RED: restore must consume the current canonical Demon succession resolution. */
class NightTransactionReconstructorSuccessionLegalityTest {
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")

    @Test
    fun `restored forced Scarlet Woman succession overrides stale ordinary Minion confirmation`() {
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = gameState(),
            checkpoint = checkpoint(),
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(empathInteraction, reconstruction.currentInteractionId)
        assertEquals(RoleId("Imp"), reconstruction.effectiveState.currentRoleId(2))
        assertEquals(RoleId("Poisoner"), reconstruction.effectiveState.currentRoleId(3))
    }

    @Test
    fun `restored confirmed Imp self kill remains mechanically dead when canonical succession is None`() {
        val reconstruction = NightTransactionReconstructor.reconstruct(
            baseGameState = gameStateWithoutLivingSuccessor(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = null,
                confirmedDemonSuccessorTarget = null,
            ),
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(empathInteraction, reconstruction.currentInteractionId)
        assertEquals(false, reconstruction.effectiveState.isMechanicallyAlive(1))
        assertEquals(RoleId("Poisoner"), reconstruction.effectiveState.currentRoleId(2))
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
                name = "Scarlet Woman",
                actualRole = RoleId("Scarlet Woman"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = true,
            ),
            PlayerState(
                seat = 3,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = true,
            ),
            PlayerState(
                seat = 4,
                name = "Empath",
                actualRole = RoleId("Empath"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
            PlayerState(
                seat = 5,
                name = "Monk",
                actualRole = RoleId("Monk"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 17L,
    )

    private fun gameStateWithoutLivingSuccessor() = GameState(
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
                alive = false,
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
        seed = 19L,
    )

    private fun checkpoint(
        demonSuccessorDraftTarget: String? = "Poisoner",
        confirmedDemonSuccessorTarget: String? = "Poisoner",
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
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
