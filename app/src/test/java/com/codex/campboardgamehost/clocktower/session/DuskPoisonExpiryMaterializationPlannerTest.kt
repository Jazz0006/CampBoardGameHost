package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** P1 typed contract for end-of-day poison expiry retry convergence. */
class DuskPoisonExpiryMaterializationPlannerTest {
    @Test
    fun `initial expiry requests stable history and mechanical clear`() {
        val plan = requireNotNull(
            DuskPoisonExpiryMaterializationPlanner.plan(
                gameId = GAME_ID,
                round = ROUND,
                previousTargetSeat = TARGET_SEAT,
                state = state(currentPoisonTargetSeat = TARGET_SEAT),
            ),
        )

        assertEquals(TARGET_SEAT, plan.previousTargetSeat)
        assertTrue(plan.stateMutationRequired)
        assertEquals(CLEAR_ACTION_ID, plan.actionIdToCommit)
    }

    @Test
    fun `state-first retry repairs missing stable history only`() {
        val plan = requireNotNull(
            DuskPoisonExpiryMaterializationPlanner.plan(
                gameId = GAME_ID,
                round = ROUND,
                previousTargetSeat = TARGET_SEAT,
                state = state(currentPoisonTargetSeat = null),
            ),
        )

        assertFalse(plan.stateMutationRequired)
        assertEquals(CLEAR_ACTION_ID, plan.actionIdToCommit)
    }

    @Test
    fun `history-first retry repairs mechanical poison only`() {
        val plan = requireNotNull(
            DuskPoisonExpiryMaterializationPlanner.plan(
                gameId = GAME_ID,
                round = ROUND,
                previousTargetSeat = TARGET_SEAT,
                state = state(
                    currentPoisonTargetSeat = TARGET_SEAT,
                    committedActionIds = setOf(CLEAR_ACTION_ID),
                ),
            ),
        )

        assertTrue(plan.stateMutationRequired)
        assertNull(plan.actionIdToCommit)
    }

    @Test
    fun `fully durable retry is a no-op`() {
        val plan = requireNotNull(
            DuskPoisonExpiryMaterializationPlanner.plan(
                gameId = GAME_ID,
                round = ROUND,
                previousTargetSeat = TARGET_SEAT,
                state = state(
                    currentPoisonTargetSeat = null,
                    committedActionIds = setOf(CLEAR_ACTION_ID),
                ),
            ),
        )

        assertFalse(plan.stateMutationRequired)
        assertNull(plan.actionIdToCommit)
    }

    @Test
    fun `no previous poison has no expiry materialization responsibility`() {
        assertNull(
            DuskPoisonExpiryMaterializationPlanner.plan(
                gameId = GAME_ID,
                round = ROUND,
                previousTargetSeat = null,
                state = state(currentPoisonTargetSeat = null),
            ),
        )
    }

    private fun state(
        currentPoisonTargetSeat: Int?,
        committedActionIds: Set<String> = emptySet(),
    ) = DuskPoisonExpiryMaterializationState(
        currentPoisonTargetSeat = currentPoisonTargetSeat,
        committedActionIds = committedActionIds,
    )

    private companion object {
        const val GAME_ID = "Game 17"
        const val ROUND = 3
        const val TARGET_SEAT = 3
        const val CLEAR_ACTION_ID = "dusk-game-17-3-poison-seat-3-to-none"
    }
}
