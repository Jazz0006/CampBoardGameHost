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
    fun `confirmed poison writes mechanical state and fact at one revision and cursor`() {
        val session = newSession("a1-poison-confirm")
        val beforePlayers = session.state.gameState.players
        val draft = poison("poison-choice", targetSeat = 2)

        val committed = session.commitConfirmedPoisonTransition(draft, expectedGameStateRevision = 0L)

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
        val duplicate = session.commitConfirmedPoisonTransition(draft, expectedGameStateRevision = 0L)
        assertEquals(committed, duplicate)
        assertEquals(acceptedState, session.state)
    }

    @Test
    fun `stale or conflicting confirmation never partially writes state or history`() {
        val session = newSession("a1-poison-reject")
        session.commitConfirmedPoisonTransition(poison("first", 2), expectedGameStateRevision = 0L)
        val accepted = session.state

        assertNotNull(runCatching {
            session.commitConfirmedPoisonTransition(poison("different", 3), expectedGameStateRevision = 0L)
        }.exceptionOrNull())
        assertEquals(accepted, session.state)

        assertNotNull(runCatching {
            session.commitConfirmedPoisonTransition(poison("first", 3), expectedGameStateRevision = 1L)
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
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

        session.commitConfirmedPoisonTransition(poison("dead-target", deadSeat), expectedGameStateRevision = 0L)

        assertEquals(false, requireNotNull(session.state.gameState.playerAt(deadSeat)).poisoned)
        assertEquals(emptyList<Int>(), session.state.gameState.players.filter { it.poisoned }.map { it.seat })
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1, session.state.actionTimeline.entries.size)
    }

    @Test
    fun `Dusk poison expiry is recorded even when dead target has no active poison flag`() {
        val deadSeat = 2
        val seeded = initialState.copy(
            players = initialState.players.map { player ->
                if (player.seat == deadSeat) player.copy(alive = false, poisoned = false) else player
            },
        )
        val session = ClocktowerGameSession.createProduction(
            gameId = "a1-dusk-expiry",
            gameSeed = seeded.seed,
            initialState = seeded,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val before = session.state.gameState
        val clear = ActionFactDraft.Poison(
            actionId = "dusk-stable-clear",
            phase = StorytellerPhase.DAY,
            round = 2,
            sequence = 5,
            targetSeat = null,
        )
        val committed = session.commitConfirmedPoisonTransition(clear, 0L)
        assertEquals(before, session.state.gameState)
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(committed, session.state.actionTimeline.entries.single())

        val accepted = session.state
        assertEquals(committed, session.commitConfirmedPoisonTransition(clear, 0L))
        assertEquals(accepted, session.state)
    }

    @Test
    fun `night carry clears old poisoned seat and atomically records stable transition`() {
        val seeded = initialState.copy(
            players = initialState.players.map { player ->
                player.copy(poisoned = player.seat == 2)
            },
        )
        val session = ClocktowerGameSession.createProduction(
            gameId = "a1-dawn-carry",
            gameSeed = seeded.seed,
            initialState = seeded,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val draft = ActionFactDraft.Poison("dawn-carry-seat-3", StorytellerPhase.NIGHT, 2, 9, 3)
        session.commitConfirmedPoisonTransition(draft, 0L)
        assertEquals(setOf(3), session.state.gameState.players.filter { it.poisoned }.map { it.seat }.toSet())
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1L, session.state.nextTimelineGlobalSequence)
        assertEquals(1, session.state.actionTimeline.entries.size)
    }

    @Test
    fun `outgoing Day cannot secretly choose a new poison target`() {
        val session = newSession("a1-day-poison-illegal")
        val before = session.state
        val invalid = ActionFactDraft.Poison(
            actionId = "invalid-day-choice",
            phase = StorytellerPhase.DAY,
            round = 2,
            sequence = 1,
            targetSeat = 3,
        )
        assertNotNull(runCatching {
            session.commitConfirmedPoisonTransition(invalid, expectedGameStateRevision = 0L)
        }.exceptionOrNull())
        assertEquals(before, session.state)
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
