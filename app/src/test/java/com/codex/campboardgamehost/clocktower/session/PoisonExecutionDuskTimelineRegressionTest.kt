package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

/** Real-device regression shape, now exercised through the actual atomic Session boundaries. */
class PoisonExecutionDuskTimelineRegressionTest {
    @Test fun `first night poison survives another players execution until Dusk`() = checkTimeline(6)

    @Test fun `executing Poisoner ends poison before Dusk without a duplicate expiry`() = checkTimeline(7)

    private fun checkTimeline(executedSeat: Int) {
        val fixture = TroubleBrewingFixtures.eightPlayerExample()
        val base = fixture.copy(players = fixture.players.map {
            if (it.seat == 7) it.copy(actualRole = RoleId("Poisoner"), shownRole = RoleId("Poisoner")) else it
        })
        val session = ClocktowerGameSession.createProduction("execution-dusk", base.seed, base,
            ClocktowerSemanticHistoryMode.GLOBAL_V1)
        session.commitConfirmedPoisonTransition(ActionFactDraft.Poison("first-poison",
            StorytellerPhase.FIRST_NIGHT, 1, session.nextInputSequence, 2), session.state.gameStateRevision)
        session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT,
            1, session.nextInputSequence, StorytellerPhase.DAY, 1), session.state.gameStateRevision)
        session.commitConfirmedPlayerDeath(ActionFactDraft.Execution("execution", StorytellerPhase.DAY,
            1, session.nextInputSequence, executedSeat), session.state.gameStateRevision)
        assertEquals(executedSeat != 7, session.state.gameState.playerAt(2)!!.poisoned)
        val before = session.state
        assertEquals(executedSeat != 7, session.expirePoisonAtDusk(before.gameStateRevision))
        val settled = session.state
        assertEquals(before.gameStateRevision + if (executedSeat == 7) 0 else 1, settled.gameStateRevision)
        if (executedSeat == 7) assertSame(before, settled)
        assertFalse(session.expirePoisonAtDusk(settled.gameStateRevision))
        assertEquals(settled, session.state)
        session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night", StorytellerPhase.DAY,
            1, session.nextInputSequence, StorytellerPhase.NIGHT, 2), session.state.gameStateRevision)

        val actions = session.state.actionTimeline.entries
        val execution = actions.single { it.fact is ActionFact.Execution }
        val clear = actions.single { it.fact is ActionFact.Poison && (it.fact as ActionFact.Poison).targetSeat == null }
        val nextNight = actions.last()
        // The execution is the public event; a second AliveAt(false) observation is redundant.
        assertTrue(session.state.epistemicObservationLog.records.isEmpty())
        assertTrue(execution.point.globalSequence < clear.point.globalSequence)
        assertTrue(clear.point.globalSequence < nextNight.point.globalSequence)
        assertEquals(StorytellerPhase.DAY, clear.point.phase)
        assertEquals(1, clear.point.round)
        assertEquals(StorytellerPhase.NIGHT, session.state.phase)
        assertEquals(2, session.state.round)
        assertFalse(session.state.gameState.playerAt(executedSeat)!!.alive)
        assertFalse(session.state.gameState.playerAt(2)!!.poisoned)
        assertNull(session.state.confirmedPoisonTargetName(1))
    }
}
