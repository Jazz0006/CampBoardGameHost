package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** P1: the night action becomes a historical fact only as the next step actually begins. */
class PendingNightAttackTest {
    private fun sessionAtSecondNight(): ClocktowerGameSession {
        val roster = TroubleBrewingFixtures.eightPlayerExample()
        val session = ClocktowerGameSession.createProduction(
            gameId = "pending-night-action", gameSeed = roster.seed, initialState = roster,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitConfirmedPhaseAdvance(
            ActionFactDraft.PhaseAdvance(
                "first-night-to-day", StorytellerPhase.FIRST_NIGHT, 1,
                session.nextInputSequence, StorytellerPhase.DAY, 1,
            ),
            session.state.gameStateRevision,
        )
        session.commitConfirmedPhaseAdvance(
            ActionFactDraft.PhaseAdvance(
                "day-to-night", StorytellerPhase.DAY, 1,
                session.nextInputSequence, StorytellerPhase.NIGHT, 2,
            ),
            session.state.gameStateRevision,
        )
        assertEquals(StorytellerPhase.NIGHT, session.state.phase)
        return session
    }

    private fun attacks(session: ClocktowerGameSession): List<ActionFact.Attack> =
        session.state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.Attack }

    @Test fun `amend before next step keeps only final target and no early facts`() {
        val session = sessionAtSecondNight()
        val baseRevision = session.state.gameStateRevision
        val aliveBefore = session.state.gameState.players.map { it.alive }
        val cursorBefore = session.state.nextTimelineGlobalSequence
        session.stageNightAttack(3, StorytellerPhase.NIGHT, 2, "imp-step")
        val stagedRevision = session.state.playerInputRevision
        assertNull(session.state.nightTargets.attackSeat)
        assertTrue(attacks(session).isEmpty())
        assertEquals(cursorBefore, session.state.nextTimelineGlobalSequence)
        session.stageNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(session.state.playerInputRevision > stagedRevision)
        assertTrue(attacks(session).isEmpty())
        assertEquals(baseRevision, session.state.gameStateRevision)
        assertEquals(aliveBefore, session.state.gameState.players.map { it.alive })

        assertThrows(IllegalStateException::class.java) {
            session.startNextNightStep("imp-step")
        }
        assertNotNull(session.startNextNightStep("ravenkeeper-step"))
        assertEquals(listOf(6), attacks(session).map { it.targetSeat })
        assertEquals(6, session.state.nightTargets.attackSeat)
        assertNull(session.state.pendingNightAttack)
        assertNull(session.startNextNightStep("ravenkeeper-step"))
        assertEquals(1, attacks(session).size)
    }

    @Test fun `identical staged choice is idempotent and rejected target is atomic`() {
        val session = sessionAtSecondNight()
        session.stageNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        val before = session.state
        session.stageNightAttack(4, StorytellerPhase.NIGHT, 2, "imp-step")
        assertSame(before, session.state)
        assertThrows(IllegalArgumentException::class.java) {
            session.stageNightAttack(99, StorytellerPhase.NIGHT, 2, "imp-step")
        }
        assertSame(before, session.state)
        assertEquals(4, session.state.pendingNightAttack?.targetSeat)
    }

    @Test fun `staging invalidates stale recommendation without killing anyone`() {
        val session = sessionAtSecondNight()
        val previousRequest = StorytellerDecisionRevision(
            session.state.gameStateRevision, session.state.playerInputRevision,
        )
        session.stageNightAttack(5, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(previousRequest == StorytellerDecisionRevision(
            session.state.gameStateRevision, session.state.playerInputRevision,
        ))
        assertTrue(session.state.gameState.players.all { it.alive })
        assertTrue(session.state.epistemicObservationLog.records.isEmpty())
        assertTrue(attacks(session).isEmpty())
    }
}
