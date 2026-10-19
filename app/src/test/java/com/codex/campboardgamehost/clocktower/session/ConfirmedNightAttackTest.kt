package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Imp is the irreversible night boundary: one confirmed Attack and its forced Death. */
class ConfirmedNightAttackTest {
    private fun session(roster: GameState = TroubleBrewingFixtures.eightPlayerExample()): ClocktowerGameSession {
        val s = ClocktowerGameSession.createProduction(
            gameId = "confirmed-imp", gameSeed = roster.seed, initialState = roster,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-night-day", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-night", StorytellerPhase.DAY, 1,
            s.nextInputSequence, StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)
        return s
    }

    private fun withRole(seat: Int, role: String): GameState =
        TroubleBrewingFixtures.eightPlayerExample().let { base ->
            base.copy(players = base.players.map { player ->
                if (player.seat == seat) player.copy(
                    actualRole = RoleId(role), shownRole = RoleId(role),
                ) else player
            })
        }

    private fun facts(s: ClocktowerGameSession) = s.state.actionTimeline.entries

    @Test fun `confirming once commits Attack and forced Death atomically and only privately`() {
        val s = session()
        val before = s.state
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertEquals(before.gameStateRevision + 2, s.state.gameStateRevision)
        assertEquals(before.nextTimelineGlobalSequence + 2, s.state.nextTimelineGlobalSequence)
        assertEquals(6, s.state.nightTargets.attackSeat)
        assertFalse(s.state.gameState.playerAt(6)!!.alive)
        assertEquals(listOf(6), facts(s).mapNotNull { (it.fact as? ActionFact.Death)?.targetSeat })
        assertEquals(listOf(6), facts(s).mapNotNull { (it.fact as? ActionFact.Attack)?.targetSeat })
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        assertNull(s.state.pendingOperation)

        val accepted = s.state
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertSame(accepted, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightAttack(5, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(accepted, s.state)
        assertThrows(IllegalArgumentException::class.java) {
            s.confirmNightAttack(99, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(accepted, s.state)
    }

    @Test fun `Monk protection prevents Death and cannot require reverting Imp`() {
        val s = session(withRole(2, "Monk"))
        s.stageNightProtection(3, StorytellerPhase.NIGHT, 2, "monk-step")
        s.startNextNightStep("imp-step")
        s.confirmNightAttack(3, StorytellerPhase.NIGHT, 2, "imp-step")
        assertEquals(1, facts(s).count { it.fact is ActionFact.Attack })
        assertEquals(0, facts(s).count { it.fact is ActionFact.Death })
        assertTrue(s.state.gameState.playerAt(3)!!.alive)
    }

    @Test fun `poisoned Monk cannot protect the attacked seat`() {
        val s = session(withRole(2, "Monk"))
        s.stageNightPoison(2, StorytellerPhase.NIGHT, 2, "poison-step")
        s.startNextNightStep("monk-step")
        s.stageNightProtection(3, StorytellerPhase.NIGHT, 2, "monk-step")
        s.startNextNightStep("imp-step")
        s.confirmNightAttack(3, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(s.state.gameState.playerAt(3)!!.alive)
    }

    @Test fun `Soldier is safe until poisoned`() {
        val safe = session(withRole(4, "Soldier"))
        safe.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(safe.state.gameState.playerAt(4)!!.alive)
        assertEquals(0, facts(safe).count { it.fact is ActionFact.Death })
        val poisoned = session(withRole(4, "Soldier"))
        poisoned.stageNightPoison(4, StorytellerPhase.NIGHT, 2, "poison-step")
        poisoned.startNextNightStep("imp-step")
        poisoned.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(poisoned.state.gameState.playerAt(4)!!.alive)
    }

    @Test fun `Mayor and Imp self kill preserve actual choice boundary instead of guessing deaths`() {
        val mayor = session(withRole(4, "Mayor"))
        mayor.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(mayor.state.gameState.playerAt(4)!!.alive)
        assertEquals(0, facts(mayor).count { it.fact is ActionFact.Death })
        val self = session()
        self.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(self.state.gameState.playerAt(8)!!.alive)
        assertEquals(0, facts(self).count { it.fact is ActionFact.Death })
    }

    @Test fun `unaccepted preceding choice blocks Imp commit rather than being dropped`() {
        val s = session()
        s.stageNightPoison(4, StorytellerPhase.NIGHT, 2, "poison-step")
        val before = s.state
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(before, s.state)
    }
}
