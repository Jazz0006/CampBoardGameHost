package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerGameSessionAliveBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `death synchronization clears poison without advancing current revision or changing other state`() {
        val targetSeat = 2
        val session = ClocktowerGameSession.createProduction(
            gameId = "d6-1d-alive-in-boundary",
            gameSeed = initialState.seed,
            initialState = initialState,
        )
        session.commitPoisonTargetBoundary(targetSeat = targetSeat)
        val beforeState = session.state.gameState
        val beforeTarget = requireNotNull(beforeState.playerAt(targetSeat))
        val beforeOtherPlayers = beforeState.players.filterNot { it.seat == targetSeat }
        val beforeRevision = session.state.gameStateRevision

        session.synchronizePlayerDeathWithinCurrentRevision(targetSeat = targetSeat)

        val afterState = session.state.gameState
        assertEquals(
            beforeTarget.copy(alive = false, poisoned = false),
            requireNotNull(afterState.playerAt(targetSeat)),
        )
        assertEquals(
            beforeOtherPlayers,
            afterState.players.filterNot { it.seat == targetSeat },
        )
        assertEquals(beforeRevision, session.state.gameStateRevision)
    }
}
