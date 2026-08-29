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
import org.junit.Test

/** SNE-7.1 executable poison-carry planner contracts. */
class NightDawnResolutionPlannerPoisonContractTest {
    @Test
    fun `living current Poisoner carries confirmed poison into Dawn`() {
        val intent = NightDawnResolutionPlanner.planPoisonCarry(
            baseGameState = gameState(),
            checkpoint = checkpoint(),
            input = NightDawnPoisonResolutionInput(
                poisonerSeat = 1,
                poisonerRoleId = RoleId("Poisoner"),
                effectiveNightState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = setOf(1, 2),
                    effectiveRoleIdsBySeat = mapOf(
                        1 to RoleId("Poisoner"),
                        2 to RoleId("Monk"),
                    ),
                ),
            ),
        )

        assertEquals(2, intent?.previousTargetSeat)
        assertEquals(2, intent?.targetSeat)
    }

    @Test
    fun `Poisoner promoted to Imp explicitly clears old poison into Dawn`() {
        val intent = NightDawnResolutionPlanner.planPoisonCarry(
            baseGameState = gameState(),
            checkpoint = checkpoint(),
            input = NightDawnPoisonResolutionInput(
                poisonerSeat = 1,
                poisonerRoleId = RoleId("Poisoner"),
                effectiveNightState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = setOf(1, 2),
                    effectiveRoleIdsBySeat = mapOf(
                        1 to RoleId("Imp"),
                        2 to RoleId("Monk"),
                    ),
                ),
            ),
        )

        assertEquals(2, intent?.previousTargetSeat)
        assertNull(intent?.targetSeat)
    }

    private fun gameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            PlayerState(
                seat = 1,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = true,
            ),
            PlayerState(
                seat = 2,
                name = "Monk",
                actualRole = RoleId("Monk"),
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
        nightStepIndex = 4,
        confirmedAttackTarget = null,
        attackDraftTarget = null,
        confirmedPoisonTarget = "Monk",
        poisonDraftTarget = "Monk",
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
