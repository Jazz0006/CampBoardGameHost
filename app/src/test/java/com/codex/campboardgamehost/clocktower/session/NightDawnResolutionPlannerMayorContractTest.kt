package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SNE-7.1 executable Mayor/Dawn planner contracts. */
class NightDawnResolutionPlannerMayorContractTest {
    @Test
    fun `confirmed Mayor redirect to dead non Demon leaves no Dawn death`() {
        val transition = NightDawnResolutionPlanner.planValidatedNightDeath(
            baseGameState = gameState(),
            checkpoint = checkpoint(confirmedMayorRedirectTarget = "Monk"),
            input = NightDawnDeathResolutionInput(
                originalDeathSeat = 2,
                mayorSeat = 2,
                mayorRedirectMayApply = true,
                effectiveNightState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = setOf(1, 2),
                    effectiveRoleIdsBySeat = mapOf(
                        1 to RoleId("Imp"),
                        2 to RoleId("Mayor"),
                        3 to RoleId("Monk"),
                    ),
                ),
                demonRoleIds = setOf(RoleId("Imp")),
            ),
        )

        assertEquals(NightResolutionContinuation.DAWN, transition.continuation)
        assertTrue(transition.dawnCommitIntent?.roleChanges?.isEmpty() == true)
        assertNull(transition.dawnCommitIntent?.death)
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
                alive = true,
            ),
            PlayerState(
                seat = 2,
                name = "Mayor",
                actualRole = RoleId("Mayor"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
            PlayerState(
                seat = 3,
                name = "Monk",
                actualRole = RoleId("Monk"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = false,
            ),
        ),
        seed = 11L,
    )

    private fun checkpoint(
        confirmedMayorRedirectTarget: String?,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = "Mayor",
        attackDraftTarget = "Mayor",
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = confirmedMayorRedirectTarget,
        mayorRedirectDraftTarget = confirmedMayorRedirectTarget,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = null,
        confirmedDemonSuccessorTarget = null,
        nextTimelineGlobalSequence = 17L,
    )
}
