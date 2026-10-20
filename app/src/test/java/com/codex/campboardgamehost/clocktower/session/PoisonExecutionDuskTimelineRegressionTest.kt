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
    @Test fun `first night poison and day execution precede dusk expiry and next night`() {
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
            1, session.nextInputSequence, 7), session.state.gameStateRevision)
        assertTrue(session.state.gameState.playerAt(2)!!.poisoned)
        val before = session.state
        assertTrue(session.expirePoisonAtDusk(before.gameStateRevision))
        val settled = session.state
        assertEquals(before.gameStateRevision + 1, settled.gameStateRevision)
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
        assertFalse(session.state.gameState.playerAt(7)!!.alive)
        assertFalse(session.state.gameState.playerAt(2)!!.poisoned)
        assertNull(session.state.confirmedPoisonTargetName(1))
    }
}
