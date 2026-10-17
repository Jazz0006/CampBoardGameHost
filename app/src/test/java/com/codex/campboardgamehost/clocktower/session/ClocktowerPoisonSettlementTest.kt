package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

class ClocktowerPoisonSettlementTest {
    private fun session(): ClocktowerGameSession {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        return ClocktowerGameSession.createProduction("poison-settle", base.seed, base,
            ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
    private fun advance(s: ClocktowerGameSession, phase: StorytellerPhase, round: Int = s.state.round) {
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("phase-${s.nextInputSequence}",
            s.state.phase, s.state.round, s.nextInputSequence, phase, round), s.state.gameStateRevision)
    }
    private fun night(s: ClocktowerGameSession) {
        advance(s, StorytellerPhase.DAY)
        advance(s, StorytellerPhase.NIGHT, 2)
    }
    private fun poison(s: ClocktowerGameSession, target: Int?) {
        s.commitConfirmedPoisonTransition(ActionFactDraft.Poison("poison-${s.nextInputSequence}",
            s.state.phase, s.state.round, s.nextInputSequence, target), s.state.gameStateRevision)
    }

    @Test fun `unchanged carry adds no history and expiry preserves pending successor`() {
        val s = session(); night(s); poison(s, 2)
        s.confirmDemonSuccessor(3)
        val before = s.state
        assertFalse(s.commitDawnPoisonCarry(DawnPoisonCarryIntent(2, 2), before.gameStateRevision))
        assertEquals(before, s.state)
        assertTrue(s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), before.gameStateRevision))
        assertEquals(3, s.state.nightTargets.successorSeat)
        assertEquals(before.gameStateRevision + 1, s.state.gameStateRevision)
        assertEquals(before.nextTimelineGlobalSequence + 1, s.state.nextTimelineGlobalSequence)
        assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
        val settled = s.state
        assertFalse(s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), settled.gameStateRevision))
        assertEquals(settled, s.state)
    }

    @Test fun `dead target still has a confirmed choice that expires exactly once`() {
        for (atDusk in listOf(false, true)) {
            val s = session(); night(s); poison(s, 2)
            s.commitConfirmedPlayerDeath(ActionFactDraft.Death("death", StorytellerPhase.NIGHT,
                2, s.nextInputSequence, 2), s.state.gameStateRevision)
            assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
            assertEquals(2, s.state.confirmedPoisonTargetSeat(2))
            if (atDusk) advance(s, StorytellerPhase.DAY)
            val before = s.state
            assertTrue(if (atDusk) s.expirePoisonAtDusk(before.gameStateRevision)
                else s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), before.gameStateRevision))
            assertNull(s.state.confirmedPoisonTargetSeat(2))
            assertEquals(before.nextTimelineGlobalSequence + 1, s.state.nextTimelineGlobalSequence)
        }
    }

    @Test fun `stale revision changed choice and retarget intent fail without mutation`() {
        val s = session(); night(s); poison(s, 2)
        val revision = s.state.gameStateRevision
        poison(s, 3)
        val accepted = s.state
        assertNotNull(runCatching { s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), revision) }.exceptionOrNull())
        assertNotNull(runCatching { s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), accepted.gameStateRevision) }.exceptionOrNull())
        assertNotNull(runCatching { s.commitDawnPoisonCarry(DawnPoisonCarryIntent(4, 3), accepted.gameStateRevision) }.exceptionOrNull())
        assertEquals(accepted, s.state)
    }

    @Test fun `empty day expiry is no-op and phase guards reject wrong boundaries`() {
        val s = session()
        val before = s.state
        assertNotNull(runCatching { s.expirePoisonAtDusk(0) }.exceptionOrNull())
        assertNotNull(runCatching { s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null), 0) }.exceptionOrNull())
        assertEquals(before, s.state)
        advance(s, StorytellerPhase.DAY)
        val day = s.state
        assertFalse(s.expirePoisonAtDusk(day.gameStateRevision))
        assertEquals(day, s.state)
    }

    @Test fun `a later confirmed choice can expire again without colliding with an earlier action`() {
        val s = session(); night(s); poison(s, 2)
        assertTrue(s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), s.state.gameStateRevision))
        poison(s, 3)
        assertTrue(s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 3), s.state.gameStateRevision))
        val ids = s.state.actionTimeline.entries.map { it.fact.actionId }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test fun `inconsistent mechanics are rejected rather than repaired`() {
        val s = session(); night(s); poison(s, 2)
        s.updateGameState(s.state.gameState.copy(players = s.state.gameState.players.map { it.copy(poisoned = false) }))
        val before = s.state
        assertNotNull(runCatching { s.commitDawnPoisonCarry(DawnPoisonCarryIntent(null, 2), before.gameStateRevision) }.exceptionOrNull())
        assertEquals(before, s.state)
    }
}
