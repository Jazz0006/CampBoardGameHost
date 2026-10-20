package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

/** Actual Death is accepted at its cause; Dawn merely publishes confirmed Session history. */
class DawnCommitBoundaryTest {
    private fun session(): ClocktowerGameSession {
        val roster = TroubleBrewingFixtures.eightPlayerExample()
        val s = ClocktowerGameSession.createProduction(
            "dawn-boundary", roster.seed, roster, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-day", StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence,
            StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-to-night", StorytellerPhase.DAY, 1, s.nextInputSequence,
            StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)
        return s
    }

    private fun dawn(s: ClocktowerGameSession, id: String = "dawn"): TimelineBoundActionFact =
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            id, StorytellerPhase.NIGHT, 2, s.nextInputSequence,
            StorytellerPhase.DAWN, 2,
        ), s.state.gameStateRevision)

    @Test fun `Imp death precedes Dawn and is made public only at Dawn`() {
        val s = session()
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(s.state.gameState.playerAt(6)!!.alive)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        val dead = s.state.actionTimeline.entries.single { it.fact is ActionFact.Death }
        val atDawn = dawn(s)
        assertTrue(dead.point.globalSequence < atDawn.point.globalSequence)
        assertEquals(StorytellerPhase.DAWN, s.state.phase)
        val announcement = s.state.epistemicObservationLog.records.single()
        assertEquals(StorytellerPhase.DAWN, announcement.phase)
        assertEquals(ObservationVisibility.PUBLIC, announcement.visibility)
        assertEquals(InformationProposition.AliveAt(6, false), announcement.proposition)
        assertTrue((announcement.timelineBinding as ObservationTimelineBinding.Global)
            .point.globalSequence > atDawn.point.globalSequence)
        val accepted = s.state
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "dawn", StorytellerPhase.NIGHT, 2, atDawn.point.sequence,
            StorytellerPhase.DAWN, 2,
        ), accepted.gameStateRevision)
        assertSame(accepted, s.state)
    }

    @Test fun `protected target produces no night Death and Dawn does not invent one`() {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val roster = base.copy(players = base.players.map {
            if (it.seat == 2) it.copy(actualRole = RoleId("Soldier"), shownRole = RoleId("Soldier"))
            else it
        })
        val s = ClocktowerGameSession.createProduction("protected-dawn", roster.seed,
            roster, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT,
            1, s.nextInputSequence, StorytellerPhase.DAY, 1), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night", StorytellerPhase.DAY,
            1, s.nextInputSequence, StorytellerPhase.NIGHT, 2), s.state.gameStateRevision)
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, "imp-step")
        val before = s.state
        assertTrue(before.gameState.playerAt(2)!!.alive)
        assertTrue(before.actionTimeline.entries.none { it.fact is ActionFact.Death })
        dawn(s)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        assertEquals(StorytellerPhase.DAWN, s.state.phase)
        assertTrue(s.state.gameState.playerAt(2)!!.alive)
    }

    @Test fun `conflicting public announcement is rejected without half committing Dawn`() {
        val s = session()
        s.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            DawnCommitIdentity.deathObservation(s.state.gameId, 2, 6),
            StorytellerPhase.NIGHT, 2, s.nextInputSequence, null, null,
            ObservationVisibility.PUBLIC, emptySet(),
            ObservationReliability.NOT_ABILITY_INFORMATION,
            InformationProposition.AliveAt(6, true),
        ))
        s.confirmNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) { dawn(s) }
        assertSame(before, s.state)
        assertEquals(StorytellerPhase.NIGHT, s.state.phase)
    }

    @Test fun `two real night death causes publish their facts exactly once at Dawn`() {
        val s = session()
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "cause-one", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 3,
        ), s.state.gameStateRevision)
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "cause-two", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 4,
        ), s.state.gameStateRevision)
        assertEquals(0, s.state.epistemicObservationLog.records.size)
        val phase = dawn(s)
        assertEquals(2, s.state.epistemicObservationLog.records.size)
        assertTrue(s.state.epistemicObservationLog.records.all {
            it.visibility == ObservationVisibility.PUBLIC && it.phase == StorytellerPhase.DAWN
        })
        assertEquals(2, s.state.actionTimeline.entries.count { it.fact is ActionFact.Death })
        val accepted = s.state
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "dawn", StorytellerPhase.NIGHT, 2, phase.point.sequence,
            StorytellerPhase.DAWN, 2,
        ), accepted.gameStateRevision)
        assertSame(accepted, s.state)
    }
}
