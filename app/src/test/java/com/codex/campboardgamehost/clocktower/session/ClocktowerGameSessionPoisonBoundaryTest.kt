package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerGameSessionPoisonBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `in-boundary game-state synchronization does not advance an already accepted revision`() {
        val session = newSession("d6-1d-in-boundary-sync")
        session.advanceGameStateRevision()
        val nextState = initialState.copy(
            players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(alive = false) else player
            },
        )

        session.synchronizeGameStateWithinCurrentRevision(nextState)

        assertEquals(nextState, session.state.gameState)
        assertEquals(1L, session.state.gameStateRevision)
    }

    @Test
    fun `poison boundary changes only poison projection and advances revision once`() {
        val session = newSession("d6-1d-poison-boundary")
        val beforePlayers = session.state.gameState.players

        session.commitPoisonTargetBoundary(targetSeat = 2)

        assertEquals(
            beforePlayers.map { player -> player.copy(poisoned = player.alive && player.seat == 2) },
            session.state.gameState.players,
        )
        assertEquals(1L, session.state.gameStateRevision)
    }

    @Test
    fun `poison synchronization inside current boundary switches target without another revision`() {
        val session = newSession("d6-1d-poison-in-boundary")
        session.commitPoisonTargetBoundary(targetSeat = 2)
        val beforePlayers = session.state.gameState.players
        val beforeRevision = session.state.gameStateRevision

        session.synchronizePoisonTargetWithinCurrentRevision(targetSeat = 3)

        assertEquals(
            beforePlayers.map { player -> player.copy(poisoned = player.alive && player.seat == 3) },
            session.state.gameState.players,
        )
        assertEquals(beforeRevision, session.state.gameStateRevision)
    }

    @Test
    fun `dead poison target remains mechanically unpoisoned in canonical projection`() {
        val deadSeat = 2
        val seeded = initialState.copy(
            players = initialState.players.map { player ->
                if (player.seat == deadSeat) player.copy(alive = false) else player
            },
        )
        val session = ClocktowerGameSession.createProduction(
            gameId = "d6-1d-dead-poison-target",
            gameSeed = seeded.seed,
            initialState = seeded,
        )

        session.commitPoisonTargetBoundary(targetSeat = deadSeat)

        assertEquals(false, requireNotNull(session.state.gameState.playerAt(deadSeat)).poisoned)
        assertEquals(emptyList<Int>(), session.state.gameState.players.filter { it.poisoned }.map { it.seat })
        assertEquals(1L, session.state.gameStateRevision)
    }

    private fun newSession(gameId: String): ClocktowerGameSession = ClocktowerGameSession.createProduction(
        gameId = gameId,
        gameSeed = initialState.seed,
        initialState = initialState,
    )
}
