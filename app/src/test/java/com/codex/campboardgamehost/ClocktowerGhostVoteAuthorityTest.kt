package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import org.junit.Assert.*
import org.junit.Test

class ClocktowerGhostVoteAuthorityTest {
    @Test
    fun `only accepted ghost voters become spent across subsequent votes and nights`() {
        val initial = TroubleBrewingFixtures.eightPlayerExample()
        val session = ClocktowerGameSession.createProduction(
            "ghost-votes", initial.seed,
            initial.copy(players = initial.players.map { if (it.seat in setOf(2, 4)) it.copy(alive = false) else it }),
            ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val seats = session.state.gameState.players.map {
            HostSeatPresentation(ClocktowerSeatId(it.seat), it.name, isAlive = it.alive)
        }
        fun authority() = ClocktowerGhostVoteAuthority.from(session.state.actionTimeline)
        val pending = clocktowerTableVoteState(seats, ClocktowerSeatId(3),
            ghostVoteAuthority = authority()).togglePendingVoter(ClocktowerSeatId(2))
        assertTrue(pending.selectedVoterSeatIds.contains(ClocktowerSeatId(2)))
        assertTrue(authority().spentSeatIds.isEmpty())
        val vote = ActionFactDraft.Vote("vote-1", StorytellerPhase.DAY, 1, 1,
            1, 3, listOf(1, 2), listOf(2))
        assertThrows(IllegalStateException::class.java) {
            session.commitConfirmedGameAction(vote, session.state.gameState, 99L)
        }
        assertTrue(authority().spentSeatIds.isEmpty())
        session.commitConfirmedGameAction(vote, session.state.gameState, session.state.gameStateRevision)
        session.commitConfirmedGameAction(vote, session.state.gameState, session.state.gameStateRevision)
        assertEquals(setOf(ClocktowerSeatId(2)), authority().spentSeatIds)
        assertFalse(authority().canVote(ClocktowerSeatId(2), seats))
        assertTrue(authority().canVote(ClocktowerSeatId(4), seats))
        assertTrue(authority().canVote(ClocktowerSeatId(1), seats))
        session.commitConfirmedGameAction(
            ActionFactDraft.Vote("vote-2", StorytellerPhase.DAY, 2, 1, 1, 3,
                listOf(1, 4), listOf(4)), session.state.gameState, session.state.gameStateRevision)
        assertEquals(setOf(ClocktowerSeatId(2), ClocktowerSeatId(4)), authority().spentSeatIds)
    }
}
