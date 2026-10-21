package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

/** Poison survives Night-to-Day and expires only when Day ends. */
class ClocktowerPoisonSettlementTest {
    private fun session(): ClocktowerGameSession {
        val roster = TroubleBrewingFixtures.eightPlayerExample()
        return ClocktowerGameSession.createProduction(
            "poison-settle", roster.seed, roster, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
    private fun advance(s: ClocktowerGameSession, phase: StorytellerPhase, round: Int = s.state.round) {
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "phase-${s.nextInputSequence}", s.state.phase, s.state.round,
            s.nextInputSequence, phase, round,
        ), s.state.gameStateRevision)
    }
    private fun night(s: ClocktowerGameSession) {
        advance(s, StorytellerPhase.DAY)
        advance(s, StorytellerPhase.NIGHT, 2)
    }

    @Test fun `confirmed poison continues into Day and expires once at Dusk`() {
        val s = session(); night(s)
        s.confirmNightPoison(2, StorytellerPhase.NIGHT, 2, "poison-step")
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        advance(s, StorytellerPhase.DAY)
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        val before = s.state
        assertTrue(s.expirePoisonAtDusk(before.gameStateRevision))
        assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
        assertNull(s.state.confirmedPoisonTargetSeat(2))
        val settled = s.state
        assertFalse(s.expirePoisonAtDusk(settled.gameStateRevision))
        assertSame(settled, s.state)
    }

    @Test fun `a dead poisoned target has its accepted target fact safely expire`() {
        val s = session(); night(s)
        s.confirmNightPoison(2, StorytellerPhase.NIGHT, 2, "poison-step")
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "actual-death", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 2,
        ), s.state.gameStateRevision)
        assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
        assertEquals(2, s.state.confirmedPoisonTargetSeat(2))
        advance(s, StorytellerPhase.DAY)
        assertTrue(s.expirePoisonAtDusk(s.state.gameStateRevision))
        assertNull(s.state.confirmedPoisonTargetSeat(2))
    }

    @Test fun `stale revision cannot expire poison or partially edit Session`() {
        val s = session(); night(s)
        s.confirmNightPoison(2, StorytellerPhase.NIGHT, 2, "poison-step")
        advance(s, StorytellerPhase.DAY)
        val before = s.state
        assertThrows(IllegalStateException::class.java) {
            s.expirePoisonAtDusk(before.gameStateRevision - 1)
        }
        assertSame(before, s.state)
    }

    @Test fun `empty day expiry is a no-op and other phases are rejected`() {
        val s = session()
        assertThrows(IllegalStateException::class.java) {
            s.expirePoisonAtDusk(s.state.gameStateRevision)
        }
        advance(s, StorytellerPhase.DAY)
        val accepted = s.state
        assertFalse(s.expirePoisonAtDusk(accepted.gameStateRevision))
        assertSame(accepted, s.state)
    }

    @Test fun `poison mechanic mismatch is not silently rewritten`() {
        val s = session(); night(s)
        s.confirmNightPoison(2, StorytellerPhase.NIGHT, 2, "poison-step")
        advance(s, StorytellerPhase.DAY)
        s.updateGameState(s.state.gameState.copy(
            players = s.state.gameState.players.map { it.copy(poisoned = false) }))
        val before = s.state
        assertThrows(IllegalStateException::class.java) {
            s.expirePoisonAtDusk(before.gameStateRevision)
        }
        assertSame(before, s.state)
    }
}
