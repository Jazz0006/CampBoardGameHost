package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * P1 regression: ordinary Dawn poison expiry must use the same stable materialization owner as
 * successor Dawn.
 */
class NightDawnOrdinaryPoisonOwnershipTest {
    @Test
    fun `dead Poisoner ordinary Dawn clear converges state-first and history-first`() {
        val poisonIntent = requireNotNull(
            NightDawnResolutionPlanner.planPoisonCarry(
                baseGameState = gameState(),
                checkpoint = checkpoint(),
                input = deadPoisonerInput(),
            ),
        )
        assertEquals(EMPATH_SEAT, poisonIntent.previousTargetSeat)
        assertNull(poisonIntent.targetSeat)

        val intent = DawnCommitIntent(poisonCarry = poisonIntent)
        val first = NightDawnDurableMaterializationPlanner.plan(
            gameId = GAME_ID,
            round = ROUND,
            intent = intent,
            state = materializationState(currentPoisonTargetSeat = EMPATH_SEAT),
            advanceToDawn = false,
        )
        val firstPoison = requireNotNull(first.poison)
        assertTrue(firstPoison.stateMutationRequired)
        assertEquals(CLEAR_ACTION_ID, firstPoison.actionIdToCommit)

        val stateFirst = NightDawnDurableMaterializationPlanner.plan(
            gameId = GAME_ID,
            round = ROUND,
            intent = intent,
            state = materializationState(currentPoisonTargetSeat = null),
            advanceToDawn = false,
        )
        val stateFirstPoison = requireNotNull(stateFirst.poison)
        assertFalse(stateFirstPoison.stateMutationRequired)
        assertEquals(CLEAR_ACTION_ID, stateFirstPoison.actionIdToCommit)

        val historyFirst = NightDawnDurableMaterializationPlanner.plan(
            gameId = GAME_ID,
            round = ROUND,
            intent = intent,
            state = materializationState(
                currentPoisonTargetSeat = EMPATH_SEAT,
                committedActionIds = setOf(CLEAR_ACTION_ID),
            ),
            advanceToDawn = false,
        )
        val historyFirstPoison = requireNotNull(historyFirst.poison)
        assertTrue(historyFirstPoison.stateMutationRequired)
        assertNull(historyFirstPoison.actionIdToCommit)
    }

    @Test
    fun `state-first restore rebuilds ordinary clear from durable previous poison target`() {
        val restoredCheckpoint = checkpoint().copy(
            confirmedPoisonTarget = null,
            poisonDraftTarget = null,
        )

        val poisonIntent = requireNotNull(
            NightDawnResolutionPlanner.planPoisonCarry(
                baseGameState = gameState(),
                checkpoint = restoredCheckpoint,
                input = deadPoisonerInput(),
                durablePreviousPoisonTargetSeat = EMPATH_SEAT,
            ),
        )

        assertEquals(EMPATH_SEAT, poisonIntent.previousTargetSeat)
        assertNull(poisonIntent.targetSeat)

        val retry = NightDawnDurableMaterializationPlanner.plan(
            gameId = GAME_ID,
            round = ROUND,
            intent = DawnCommitIntent(poisonCarry = poisonIntent),
            state = materializationState(currentPoisonTargetSeat = null),
            advanceToDawn = false,
        )
        val retryPoison = requireNotNull(retry.poison)
        assertFalse(retryPoison.stateMutationRequired)
        assertEquals(CLEAR_ACTION_ID, retryPoison.actionIdToCommit)
    }

    private fun deadPoisonerInput() = NightDawnPoisonResolutionInput(
        poisonerSeat = POISONER_SEAT,
        poisonerRoleId = RoleId("Poisoner"),
        effectiveNightState = ClocktowerEffectiveNightState(
            effectiveAliveSeats = setOf(EMPATH_SEAT),
            effectiveRoleIdsBySeat = mapOf(
                POISONER_SEAT to RoleId("Poisoner"),
                EMPATH_SEAT to RoleId("Empath"),
            ),
        ),
    )

    private fun materializationState(
        currentPoisonTargetSeat: Int?,
        committedActionIds: Set<String> = emptySet(),
    ) = DawnDurableMaterializationState(
        aliveSeats = setOf(EMPATH_SEAT),
        roleIdsBySeat = mapOf(
            POISONER_SEAT to RoleId("Poisoner"),
            EMPATH_SEAT to RoleId("Empath"),
        ),
        currentPhase = StorytellerPhase.NIGHT,
        committedActionIds = committedActionIds,
        committedObservationRecordIds = emptySet(),
        currentPoisonTargetSeat = currentPoisonTargetSeat,
    )

    private fun gameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            PlayerState(
                seat = POISONER_SEAT,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = false,
            ),
            PlayerState(
                seat = EMPATH_SEAT,
                name = "Empath",
                actualRole = RoleId("Empath"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 29L,
    )

    private fun checkpoint() = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = ROUND,
        gameStateRevision = 8L,
        playerInputRevision = 5L,
        nightStarted = true,
        nightStepIndex = 3,
        confirmedAttackTarget = "Poisoner",
        attackDraftTarget = "Poisoner",
        confirmedPoisonTarget = "Empath",
        poisonDraftTarget = "Empath",
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = null,
        confirmedDemonSuccessorTarget = null,
        nextTimelineGlobalSequence = 11L,
    )

    private companion object {
        const val GAME_ID = "game-ordinary"
        const val ROUND = 4
        const val POISONER_SEAT = 1
        const val EMPATH_SEAT = 2
        const val CLEAR_ACTION_ID = "dawn-game-ordinary-4-poison-seat-2-to-none"
    }
}
