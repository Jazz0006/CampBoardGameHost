package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerGameSessionGameStateBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `accepted game-state boundary replaces state and advances revision exactly once`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "d6-1d-changed-state",
            gameSeed = initialState.seed,
            initialState = initialState,
        )
        val nextState = initialState.copy(
            players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(alive = false) else player
            },
        )

        session.commitGameStateBoundary(nextState)

        assertEquals(nextState, session.state.gameState)
        assertEquals(1L, session.state.gameStateRevision)
    }

    @Test
    fun `accepted game-state boundary preserves production revision cadence when state is equal`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "d6-1d-equal-state",
            gameSeed = initialState.seed,
            initialState = initialState,
        )

        session.commitGameStateBoundary(initialState)
        session.commitGameStateBoundary(initialState)

        assertEquals(initialState, session.state.gameState)
        assertEquals(2L, session.state.gameStateRevision)
    }
}
