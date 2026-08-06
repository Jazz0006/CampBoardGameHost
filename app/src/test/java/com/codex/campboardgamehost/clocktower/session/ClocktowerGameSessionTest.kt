package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerGameSessionTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = initialState.script,
        scriptContentHash = "e12f6425ece137da02477a642235c797",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.VERIFIED,
    )

    @Test
    fun `game state and player input revisions advance independently`() {
        val session = newSession()

        session.recordPlayerInput()
        session.updateGameState(
            initialState.copy(players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(alive = false) else player
            }),
        )

        assertEquals(1, session.snapshot.playerInputRevision)
        assertEquals(1, session.snapshot.gameStateRevision)
        assertEquals(initialState.seed, session.snapshot.gameSeed)
    }

    @Test
    fun `unchanged game state does not create a revision`() {
        val session = newSession()
        val before = session.snapshot

        val after = session.updateGameState(initialState)

        assertSame(before, after)
        assertEquals(0, after.gameStateRevision)
    }

    @Test
    fun `restored session retains the persisted game seed`() {
        val original = newSession().also { it.recordPlayerInput() }
        val restored = ClocktowerGameSession.restore(original.snapshot)

        assertEquals(original.snapshot, restored.snapshot)
        assertEquals(initialState.seed, restored.snapshot.gameSeed)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `session rejects attempts to replace the persisted game seed`() {
        newSession().updateGameState(initialState.copy(seed = initialState.seed + 1))
    }

    private fun newSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "game-2026-08-06-001",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
    )
}
