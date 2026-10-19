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

class PendingNightOperationTest {
    private fun nightSession(): ClocktowerGameSession {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val session = ClocktowerGameSession.createProduction(
            "night-operation", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-night-day", StorytellerPhase.FIRST_NIGHT, 1,
            session.nextInputSequence, StorytellerPhase.DAY, 1,
        ), session.state.gameStateRevision)
        session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-night", StorytellerPhase.DAY, 1,
            session.nextInputSequence, StorytellerPhase.NIGHT, 2,
        ), session.state.gameStateRevision)
        return session
    }

    @Test fun `poison protect and attack are one serial pending operation with accepted mechanical effects`() {
        val s = nightSession()
        val startRevision = s.state.gameStateRevision
        val startCursor = s.state.nextTimelineGlobalSequence
        s.stageNightPoison(3, StorytellerPhase.NIGHT, 2, "poison-step")
        val beforePoison = s.state
        assertEquals(NightOperationKind.POISON, beforePoison.pendingOperation?.kind)
        assertEquals(startRevision, beforePoison.gameStateRevision)
        assertEquals(startCursor, beforePoison.nextTimelineGlobalSequence)
        assertFalse(s.state.gameState.playerAt(3)!!.poisoned)
        assertThrows(IllegalStateException::class.java) {
            s.stageNightProtection(2, StorytellerPhase.NIGHT, 2, "monk-step")
        }
        assertSame(beforePoison, s.state)
        s.stageNightPoison(5, StorytellerPhase.NIGHT, 2, "poison-step")
        assertNotNull(s.startNextNightStep("monk-step"))
        assertNull(s.state.pendingOperation)
        assertTrue(s.state.gameState.playerAt(5)!!.poisoned)
        assertFalse(s.state.gameState.playerAt(3)!!.poisoned)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Poison })

        s.stageNightProtection(2, StorytellerPhase.NIGHT, 2, "monk-step")
        assertNull(s.state.nightTargets.monkSeat)
        assertNotNull(s.startNextNightStep("imp-step"))
        assertEquals(2, s.state.nightTargets.monkSeat)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Protect })

        s.stageNightAttack(6, StorytellerPhase.NIGHT, 2, "imp-step")
        assertNull(s.state.nightTargets.attackSeat)
        assertNotNull(s.startNextNightStep("night-resolution"))
        assertEquals(6, s.state.nightTargets.attackSeat)
        assertNull(s.state.pendingOperation)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Attack })
        assertTrue(s.state.gameState.playerAt(6)!!.alive)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
    }

    @Test fun `first night Butler also follows next step boundary and remains immutable afterwards`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val s = ClocktowerGameSession.createProduction(
            "butler", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        // Fixture seat 4 is the actual Butler.
        s.stageNightButlerMaster(4, 2, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        assertEquals(NightOperationKind.BUTLER, s.state.pendingOperation?.kind)
        assertTrue(s.state.actionTimeline.entries.isEmpty())
        assertThrows(IllegalStateException::class.java) {
            s.startNextNightStep("butler-step")
        }
        s.stageNightButlerMaster(4, 3, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        assertNotNull(s.startNextNightStep("next-step"))
        val accepted = s.state
        val butler = accepted.actionTimeline.entries.single().fact as ActionFact.ButlerMasterChoice
        assertEquals(4, butler.butlerSeat)
        assertEquals(3, butler.masterSeat)
        s.stageNightButlerMaster(4, 3, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        assertSame(accepted, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.stageNightButlerMaster(4, 2, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        }
        assertSame(accepted, s.state)
    }
}
