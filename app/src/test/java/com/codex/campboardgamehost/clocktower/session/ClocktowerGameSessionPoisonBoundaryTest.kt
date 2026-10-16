package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun `confirmed poison writes mechanical state and fact at one revision and cursor`() {
        val session = newSession("a1-poison-confirm")
        val beforePlayers = session.state.gameState.players
        val draft = poison("poison-choice", targetSeat = 2)

        val committed = session.commitConfirmedPoisonTarget(draft, expectedGameStateRevision = 0L)

        assertEquals(
            beforePlayers.map { player -> player.copy(poisoned = player.alive && player.seat == 2) },
            session.state.gameState.players,
        )
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1L, session.state.nextTimelineGlobalSequence)
        assertEquals(1, session.state.actionTimeline.entries.size)
        assertEquals(0L, committed.point.globalSequence)
        assertEquals(committed, session.state.actionTimeline.entries.single())
        assertEquals("poison-choice", committed.fact.actionId)

        val acceptedState = session.state
        val duplicate = session.commitConfirmedPoisonTarget(draft, expectedGameStateRevision = 0L)
        assertEquals(committed, duplicate)
        assertEquals(acceptedState, session.state)
    }

    @Test
    fun `stale or conflicting confirmation never partially writes state or history`() {
        val session = newSession("a1-poison-reject")
        session.commitConfirmedPoisonTarget(poison("first", 2), expectedGameStateRevision = 0L)
        val accepted = session.state

        assertNotNull(runCatching {
            session.commitConfirmedPoisonTarget(poison("different", 3), expectedGameStateRevision = 0L)
        }.exceptionOrNull())
        assertEquals(accepted, session.state)

        assertNotNull(runCatching {
            session.commitConfirmedPoisonTarget(poison("first", 3), expectedGameStateRevision = 1L)
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
    }

    @Test
    fun `poison synchronization inside an existing boundary changes target without another revision`() {
        val session = newSession("d6-1d-poison-in-boundary")
        session.synchronizePoisonTargetWithinCurrentRevision(targetSeat = 2)
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
            gameId = "a1-dead-poison",
            gameSeed = seeded.seed,
            initialState = seeded,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )

        session.commitConfirmedPoisonTarget(poison("dead-target", deadSeat), expectedGameStateRevision = 0L)

        assertEquals(false, requireNotNull(session.state.gameState.playerAt(deadSeat)).poisoned)
        assertEquals(emptyList<Int>(), session.state.gameState.players.filter { it.poisoned }.map { it.seat })
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1, session.state.actionTimeline.entries.size)
    }

    private fun poison(actionId: String, targetSeat: Int?) = ActionFactDraft.Poison(
        actionId = actionId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        targetSeat = targetSeat,
    )

    private fun newSession(gameId: String): ClocktowerGameSession = ClocktowerGameSession.createProduction(
        gameId = gameId,
        gameSeed = initialState.seed,
        initialState = initialState,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
}
