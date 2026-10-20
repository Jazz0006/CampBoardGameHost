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
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmedNightAttackTest {
    private fun session(roster: GameState = TroubleBrewingFixtures.eightPlayerExample()): ClocktowerGameSession {
        val s = ClocktowerGameSession.createProduction(
            "confirmed-imp", roster.seed, roster, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-night-day", StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence,
            StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-night", StorytellerPhase.DAY, 1, s.nextInputSequence,
            StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)
        return s
    }

    private fun withRole(seat: Int, role: String): GameState =
        TroubleBrewingFixtures.eightPlayerExample().let { base ->
            base.copy(players = base.players.map { p ->
                if (p.seat == seat) p.copy(actualRole = RoleId(role), shownRole = RoleId(role)) else p
            })
        }

    private fun facts(s: ClocktowerGameSession) = s.state.actionTimeline.entries

    @Test fun `Imp Next atomically records attack and forced private death`() {
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
        val accepted = s.state
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertSame(accepted, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightAttack(5, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(accepted, s.state)
    }

    @Test fun `confirmed Imp Death is final and Dawn does not choose another victim`() {
        val s = session()
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "dawn", StorytellerPhase.NIGHT, 2, s.nextInputSequence,
            StorytellerPhase.DAWN, 2,
        ), s.state.gameStateRevision)
        assertEquals(listOf(6), facts(s).mapNotNull { (it.fact as? ActionFact.Death)?.targetSeat })
        assertEquals(1, s.state.epistemicObservationLog.records.size)
        assertFalse(s.state.gameState.playerAt(6)!!.alive)
        assertTrue(s.state.gameState.playerAt(5)!!.alive)
    }

    @Test fun `Monk confirmed protection prevents Imp death`() {
        val s = session(withRole(2, "Monk"))
        s.confirmNightProtection(3, StorytellerPhase.NIGHT, 2, "monk-step")
        s.confirmNightAttack(3, StorytellerPhase.NIGHT, 2, "imp-step")
        assertEquals(0, facts(s).count { it.fact is ActionFact.Death })
        assertTrue(s.state.gameState.playerAt(3)!!.alive)
    }

    @Test fun `Poisoner Next immediately poisons Monk before Monk protects`() {
        val s = session(withRole(2, "Monk"))
        s.confirmNightPoison(2, StorytellerPhase.NIGHT, 2, "poison-step")
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        s.confirmNightProtection(3, StorytellerPhase.NIGHT, 2, "monk-step")
        s.confirmNightAttack(3, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(s.state.gameState.playerAt(3)!!.alive)
    }

    @Test fun `unpoisoned Soldier survives but poisoned Soldier dies`() {
        val safe = session(withRole(4, "Soldier"))
        safe.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(safe.state.gameState.playerAt(4)!!.alive)
        val poisoned = session(withRole(4, "Soldier"))
        poisoned.confirmNightPoison(4, StorytellerPhase.NIGHT, 2, "poison-step")
        poisoned.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(poisoned.state.gameState.playerAt(4)!!.alive)
    }

    @Test fun `Mayor attack remains a real choice until its ruling`() {
        val mayor = session(withRole(4, "Mayor"))
        mayor.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(mayor.state.gameState.playerAt(4)!!.alive)
        assertEquals(0, facts(mayor).count { it.fact is ActionFact.Death })
    }

    @Test fun `Imp self kill chooses living Minion and writes Attack Death RoleChange atomically`() {
        val s = session()
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) {
            s.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(before, s.state)
        s.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step", successorSeat = 7)
        assertFalse(s.state.gameState.playerAt(8)!!.alive)
        assertEquals(RoleId("Imp"), s.state.gameState.playerAt(7)!!.actualRole)
        assertEquals(com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON,
            s.state.gameState.playerAt(7)!!.actualType)
        assertEquals(before.gameStateRevision + 3, s.state.gameStateRevision)
        assertEquals(before.nextTimelineGlobalSequence + 3, s.state.nextTimelineGlobalSequence)
        assertEquals(listOf(8), facts(s).mapNotNull { (it.fact as? ActionFact.Death)?.targetSeat })
        assertEquals(listOf(7), facts(s).mapNotNull { (it.fact as? ActionFact.RoleChange)?.targetSeat })
        assertEquals(1, facts(s).count { it.fact is ActionFact.Attack })
        val accepted = s.state
        s.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step", successorSeat = 7)
        assertSame(accepted, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step", successorSeat = 6)
        }
        assertSame(accepted, s.state)
    }

    @Test fun `five alive Scarlet Woman must succeed instead of alternative Minion`() {
        val s = session(withRole(7, "Scarlet Woman"))
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) {
            s.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(before, s.state)
        s.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step", successorSeat = 7)
        assertFalse(s.state.gameState.playerAt(8)!!.alive)
        assertEquals(RoleId("Imp"), s.state.gameState.playerAt(7)!!.actualRole)
    }

    @Test fun `Imp alone may die without successor but a poisoned Imp cannot self kill`() {
        val noMinion = TroubleBrewingFixtures.eightPlayerExample().let { roster ->
            roster.copy(players = roster.players.map {
                if (it.seat == 7) it.copy(actualRole = RoleId("Chef"),
                    actualType = com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK)
                else it
            })
        }
        val alone = session(noMinion)
        alone.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(alone.state.gameState.playerAt(8)!!.alive)
        assertTrue(facts(alone).none { it.fact is ActionFact.RoleChange })

        val poisoned = session()
        poisoned.confirmNightPoison(8, StorytellerPhase.NIGHT, 2, "poison-step")
        poisoned.confirmNightAttack(8, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(poisoned.state.gameState.playerAt(8)!!.alive)
        assertTrue(facts(poisoned).none { it.fact is ActionFact.Death || it.fact is ActionFact.RoleChange })
    }
}
