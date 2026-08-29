package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SNE-7.9D typed contracts for complete canonical succession-resolution consumption. */
class NightDawnResolutionPlannerSuccessionResolutionTest {
    @Test
    fun `forced succession ignores stale choice and awaits forced Demon identity`() {
        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            successionResolution = DemonSuccessionResolution.Forced(targetSeat = 2),
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.AWAIT_NEW_DEMON_IDENTITY, transition.continuation)
        assertEquals("Scarlet Woman", transition.checkpoint.pendingNewDemonName)
        assertNull(transition.checkpoint.demonSuccessorDraftTarget)
        assertNull(transition.checkpoint.confirmedDemonSuccessorTarget)
        assertNull(transition.dawnCommitIntent)
        assertFalse(transition.outcomeEvaluationAllowed)
    }

    @Test
    fun `no succession clears stale choice and permits Dawn outcome evaluation`() {
        val transition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = gameState(),
            checkpoint = checkpoint(
                demonSuccessorDraftTarget = "Poisoner",
                confirmedDemonSuccessorTarget = "Poisoner",
            ),
            successionResolution = DemonSuccessionResolution.None,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(NightResolutionContinuation.DAWN, transition.continuation)
        assertNull(transition.checkpoint.pendingNewDemonName)
        assertNull(transition.checkpoint.demonSuccessorDraftTarget)
        assertNull(transition.checkpoint.confirmedDemonSuccessorTarget)
        assertNull(transition.dawnCommitIntent)
        assertTrue(transition.outcomeEvaluationAllowed)
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
        ),
        seed = 23L,
    )

    private fun checkpoint(
        demonSuccessorDraftTarget: String?,
        confirmedDemonSuccessorTarget: String?,
    ) = ClocktowerNightCheckpoint(
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
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
