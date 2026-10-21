package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ClocktowerPoisonSourceExitTest {
    private fun session(monk: Boolean = false): ClocktowerGameSession {
        val game = TroubleBrewingFixtures.eightPlayerExample().let { game -> game.copy(players = game.players.map {
            when {
                it.seat == 7 -> it.copy(actualRole = RoleId("Poisoner"), shownRole = RoleId("Poisoner"))
                monk && it.seat == 4 -> it.copy(actualRole = RoleId("Monk"), shownRole = RoleId("Monk"))
                else -> it
            }
        }) }
        return ClocktowerGameSession.createProduction("poison-source", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
    private fun advance(s: ClocktowerGameSession, phase: StorytellerPhase, round: Int = s.state.round) {
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("phase-${s.nextInputSequence}", s.state.phase,
            s.state.round, s.nextInputSequence, phase, round), s.state.gameStateRevision)
    }
    private fun night(s: ClocktowerGameSession) {
        advance(s, StorytellerPhase.DAY)
        advance(s, StorytellerPhase.NIGHT, 2)
    }
    private fun poison(s: ClocktowerGameSession) = s.confirmNightPoison(2, s.state.phase, s.state.round, "poison-step")
    private fun poisons(s: ClocktowerGameSession) = s.state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.Poison }
    private fun assertEnded(s: ClocktowerGameSession) {
        assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
        assertNull(s.state.confirmedPoisonTargetSeat(s.state.round))
        assertEquals(listOf(2, null), poisons(s).map { it.targetSeat })
    }

    @Test fun `Imp killing Poisoner ends poison immediately without changing information already received`() {
        val s = session()
        s.commitGlobalEpistemicObservation(EpistemicObservationDraft("chef-received", s.state.phase, 1,
            s.nextInputSequence, 1, RoleId("Chef"), ObservationVisibility.PRIVATE, setOf(1),
            ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, emptyList(), 0)))
        night(s); poison(s)
        val prior = s.state
        s.confirmNightAttack(7, s.state.phase, 2, "imp-step")
        assertFalse(s.state.gameState.playerAt(7)!!.alive)
        assertEnded(s)
        assertEquals(prior.epistemicObservationLog, s.state.epistemicObservationLog)
        assertEquals(listOf("Attack", "Death", "Poison"), s.state.actionTimeline.entries.takeLast(3).map { it.fact.javaClass.simpleName })
        assertEquals(prior.nextTimelineGlobalSequence + 3, s.state.nextTimelineGlobalSequence)
        val accepted = s.state
        s.confirmNightAttack(7, s.state.phase, 2, "imp-step")
        assertSame(accepted, s.state)
    }

    @Test fun `day execution ends source poison once and does not fail or duplicate expiry at Dusk`() {
        val s = session(); poison(s)
        advance(s, StorytellerPhase.DAY)
        s.confirmNomination(1, 7, null)
        s.confirmVote(1, 7, listOf(1, 2, 3, 4))
        s.confirmEndDay(1)
        assertFalse(s.state.gameState.playerAt(7)!!.alive)
        assertFalse(s.state.gameState.playerAt(2)!!.poisoned)
        assertNull(s.state.confirmedPoisonTargetSeat(1))
        assertEquals(listOf(2, null), poisons(s).map { it.targetSeat })
        assertEquals(StorytellerPhase.NIGHT, s.state.phase)
        assertEquals(2, s.state.round)
    }

    @Test fun `confirmed day death clears the source effect before any later day action`() {
        val s = session(); poison(s)
        advance(s, StorytellerPhase.DAY)
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death("day-source-death", s.state.phase, 1,
            s.nextInputSequence, 7), s.state.gameStateRevision)
        assertEnded(s)
        assertEquals(StorytellerPhase.DAY, s.state.phase)
    }

    @Test fun `confirmed role exit ends poison and regaining Poisoner cannot revive the old choice`() {
        val s = session(); night(s); poison(s)
        val draft = ActionFactDraft.RoleChange("source-becomes-spy", s.state.phase, 2,
            s.nextInputSequence, 7, RoleId("Spy"), Alignment.EVIL, CharacterType.MINION)
        val revision = s.state.gameStateRevision
        s.commitConfirmedActualRoleChange(draft, revision)
        assertEnded(s)
        val accepted = s.state
        s.commitConfirmedActualRoleChange(draft, revision)
        assertSame(accepted, s.state)
        s.commitConfirmedActualRoleChange(ActionFactDraft.RoleChange("source-becomes-poisoner", s.state.phase, 2,
            s.nextInputSequence, 7, RoleId("Poisoner"), Alignment.EVIL, CharacterType.MINION), s.state.gameStateRevision)
        assertEnded(s)
    }

    @Test fun `Poisoner succeeding a self-killed Imp loses poison in the same confirmation`() {
        val s = session(); night(s); poison(s)
        s.confirmNightAttack(8, s.state.phase, 2, "imp-self-kill", successorSeat = 7)
        assertEquals(RoleId("Imp"), s.state.gameState.playerAt(7)!!.actualRole)
        assertEnded(s)
        assertEquals(7, s.state.pendingImpNotification()!!.targetSeat)
        val accepted = s.state
        s.confirmNightAttack(8, s.state.phase, 2, "imp-self-kill", successorSeat = 7)
        assertSame(accepted, s.state)
    }

    @Test fun `other deaths and attacks prevented by protection do not end source poison`() {
        val other = session(); night(other); poison(other)
        other.confirmNightAttack(6, other.state.phase, 2, "imp-step")
        assertTrue(other.state.gameState.playerAt(2)!!.poisoned)
        assertEquals(listOf(2), poisons(other).map { it.targetSeat })
        val protected = session(monk = true); night(protected); poison(protected)
        protected.confirmNightProtection(7, protected.state.phase, 2, "monk-step")
        protected.confirmNightAttack(7, protected.state.phase, 2, "imp-step")
        assertTrue(protected.state.gameState.playerAt(7)!!.alive)
        assertTrue(protected.state.gameState.playerAt(2)!!.poisoned)
        assertEquals(listOf(2), poisons(protected).map { it.targetSeat })
    }

    @Test fun `source death without an active choice does not invent a poison expiry`() {
        val s = session(); night(s)
        s.confirmNightAttack(7, s.state.phase, 2, "imp-step")
        assertTrue(poisons(s).isEmpty())
    }

    @Test fun `stale source role change cannot expire poison or change any confirmed fact`() {
        val s = session(); night(s); poison(s)
        val before = s.state
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedActualRoleChange(ActionFactDraft.RoleChange("stale-source-exit", s.state.phase, 2,
                s.nextInputSequence, 7, RoleId("Spy"), Alignment.EVIL, CharacterType.MINION), before.gameStateRevision - 1)
        }
        assertSame(before, s.state)
    }

    @Test fun `failed persistence rolls back attack death and expiry together then retry saves once`() {
        val s = session(); night(s); poison(s)
        val before = s.state
        s.persistWith { if (!it.gameState.playerAt(7)!!.alive) throw IOException("disk full") }
        assertThrows(IOException::class.java) { s.confirmNightAttack(7, s.state.phase, 2, "imp-step") }
        assertSame(before, s.state)
        assertTrue(s.state.gameState.playerAt(2)!!.poisoned)
        val writes = mutableListOf<ClocktowerSessionState>()
        s.persistWith { writes.add(it) }
        s.confirmNightAttack(7, s.state.phase, 2, "imp-step")
        assertEquals(listOf(before, s.state), writes)
        assertEnded(s)
    }
}
