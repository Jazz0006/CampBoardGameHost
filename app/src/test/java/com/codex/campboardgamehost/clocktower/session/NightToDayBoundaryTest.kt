package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

/** Night deaths are real when accepted. Day is a phase change, not a publication system. */
class NightToDayBoundaryTest {
    private fun session(roster: GameState = TroubleBrewingFixtures.eightPlayerExample()): ClocktowerGameSession {
        val session = ClocktowerGameSession.createProduction(
            "night-day", roster.seed, roster, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-day", StorytellerPhase.FIRST_NIGHT, 1,
            session.nextInputSequence, StorytellerPhase.DAY, 1,
        ), session.state.gameStateRevision)
        session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-night", StorytellerPhase.DAY, 1,
            session.nextInputSequence, StorytellerPhase.NIGHT, 2,
        ), session.state.gameStateRevision)
        return session
    }

    private fun day(s: ClocktowerGameSession, id: String = "night-to-day") =
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            id, StorytellerPhase.NIGHT, 2, s.nextInputSequence,
            StorytellerPhase.DAY, 2,
        ), s.state.gameStateRevision)

    @Test fun `confirmed Imp death is effective before Day and is not separately republished`() {
        val s = session()
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(s.state.gameState.playerAt(6)!!.alive)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        val death = s.state.actionTimeline.entries.single { it.fact is ActionFact.Death }
        val atDay = day(s)
        assertEquals(StorytellerPhase.DAY, s.state.phase)
        assertEquals(2, s.state.round)
        assertTrue(death.point.globalSequence < atDay.point.globalSequence)
        assertEquals(listOf(6), s.state.actionTimeline.entries.mapNotNull {
            (it.fact as? ActionFact.Death)?.targetSeat
        })
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        assertEquals(ClocktowerNightTargets(), s.state.nightTargets)
        val accepted = s.state
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "night-to-day", StorytellerPhase.NIGHT, 2, atDay.point.sequence,
            StorytellerPhase.DAY, 2,
        ), accepted.gameStateRevision)
        assertSame(accepted, s.state)
    }

    @Test fun `protected attack causes no Death and Day does not invent one`() {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val roster = base.copy(players = base.players.map { player ->
            if (player.seat == 2) player.copy(
                actualRole = RoleId("Soldier"), shownRole = RoleId("Soldier"))
            else player
        })
        val s = session(roster)
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, "imp-step")
        day(s)
        assertTrue(s.state.gameState.playerAt(2)!!.alive)
        assertTrue(s.state.actionTimeline.entries.none { it.fact is ActionFact.Death })
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
    }

    @Test fun `two confirmed night deaths survive Day transition without duplicate observations`() {
        val s = session()
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "cause-one", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 3,
        ), s.state.gameStateRevision)
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "cause-two", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 4,
        ), s.state.gameStateRevision)
        day(s)
        assertEquals(listOf(3, 4), s.state.actionTimeline.entries.mapNotNull {
            (it.fact as? ActionFact.Death)?.targetSeat
        })
        assertFalse(s.state.gameState.playerAt(3)!!.alive)
        assertFalse(s.state.gameState.playerAt(4)!!.alive)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
    }

    @Test fun `night cannot transition to another night without a day`() {
        val s = session()
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) {
            s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
                "invalid-second-night", StorytellerPhase.NIGHT, 2,
                s.nextInputSequence, StorytellerPhase.NIGHT, 2,
            ), s.state.gameStateRevision)
        }
        assertSame(before, s.state)
    }
}
