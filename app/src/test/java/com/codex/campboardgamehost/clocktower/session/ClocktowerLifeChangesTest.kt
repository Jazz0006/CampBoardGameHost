package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.ClocktowerGhostVoteAuthority
import com.codex.campboardgamehost.ClocktowerSeatId
import org.junit.Assert.*
import org.junit.Test

class ClocktowerLifeChangesTest {
    private fun session(demonRole: String = "Zombuul"): ClocktowerGameSession {
        val layout = BadMoonRisingOpening.generate((1..8).map { "P$it" }, 12L)
        val zombuul = BadMoonRisingOpening.assign(layout, layout.players.single { it.actualType == CharacterType.DEMON }.seat, demonRole)
        return ClocktowerGameSession.createProduction("bmr-life", zombuul.seed, zombuul, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
    private fun ClocktowerGameSession.day() = commitConfirmedPhaseAdvance(
        ActionFactDraft.PhaseAdvance("day", state.phase, state.round, nextInputSequence, StorytellerPhase.DAY, 1), state.gameStateRevision)

    @Test fun resurrectionRestoresVotingAndSecondDeathCanBeConfirmedAndReloaded() {
        val s = session()
        val victim = s.state.gameState.players.first { it.actualType == CharacterType.TOWNSFOLK }.seat
        fun kill(id: String) = s.commitConfirmedPlayerDeath(ActionFactDraft.Death(id, s.state.phase, s.state.round, s.nextInputSequence, victim), s.state.gameStateRevision)
        kill("death-1")
        s.day()
        val pair = s.state.gameState.players.filter { it.publiclyAlive }.take(2)
        s.confirmNomination(pair[0].seat, pair[1].seat, null)
        s.confirmVote(pair[0].seat, pair[1].seat, listOf(victim))
        assertTrue(victim in s.state.actionTimeline.spentGhostVoterSeats())
        s.confirmRevival("revival", victim)
        s.confirmRevival("revival", victim)
        assertTrue(s.state.gameState.playerAt(victim)!!.publiclyAlive)
        assertFalse(victim in s.state.actionTimeline.spentGhostVoterSeats())
        assertFalse(ClocktowerSeatId(victim) in ClocktowerGhostVoteAuthority.from(s.state.actionTimeline).spentSeatIds)
        kill("death-2")
        val second = s.state.gameState.players.filter { it.publiclyAlive && it.seat !in pair.map { p -> p.seat } }.take(2)
        s.confirmNomination(second[0].seat, second[1].seat, null)
        s.confirmVote(second[0].seat, second[1].seat, listOf(victim))
        val restored = ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state))
        assertEquals(s.state, restored)
        assertEquals(setOf(victim), restored.actionTimeline.spentGhostVoterSeats())
        assertEquals(1, restored.actionTimeline.entries.count { it.fact is ActionFact.Revival })
    }

    @Test fun apparentDeathAndNonLethalExecutionNeverBecomeActualDeathOnReload() {
        val s = session()
        val zombie = s.state.gameState.players.single { it.actualRole.value == "Zombuul" }.seat
        s.confirmDeathRegistration("apparent-death", zombie)
        s.confirmDeathRegistration("apparent-death", zombie)
        assertTrue(s.state.gameState.playerAt(zombie)!!.alive)
        assertFalse(s.state.gameState.playerAt(zombie)!!.publiclyAlive)
        assertFalse(com.codex.campboardgamehost.clocktowerDayOverviewTableState(s.state.gameState).seats.single {
            it.seatId.number == zombie }.isAlive)
        assertThrows(IllegalArgumentException::class.java) { s.confirmRevival("not-a-revival", zombie) }
        s.day()
        val target = s.state.gameState.players.first { it.seat != zombie }.seat
        assertThrows(IllegalArgumentException::class.java) { s.confirmNomination(zombie, target, null) }
        s.commitConfirmedGameAction(ActionFactDraft.Execution("execution-no-death", s.state.phase, s.state.round,
            s.nextInputSequence, target, causedDeath = false), s.state.gameState, s.state.gameStateRevision)
        val restored = ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state))
        assertEquals(s.state, restored)
        assertTrue(restored.gameState.playerAt(target)!!.alive)
        assertTrue(restored.gameState.playerAt(zombie)!!.alive)
        assertTrue(restored.gameState.playerAt(zombie)!!.registersAsDead)
    }

    @Test fun pukkaSelfPoisonClearsOldVictimAtomicallyAndPersistsInertLaterChoices() {
        val s = session("Pukka")
        val layout = s.state.gameState
        val demon = layout.players.single { it.actualType == CharacterType.DEMON }.seat
        val old = layout.players.first { it.seat != demon }.seat
        val later = layout.players.last { it.seat != demon && it.seat != old }.seat
        fun nextNight(round: Int) {
            s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day-$round", s.state.phase, s.state.round,
                s.nextInputSequence, StorytellerPhase.DAY, round - 1), s.state.gameStateRevision)
            s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night-$round", s.state.phase, s.state.round,
                s.nextInputSequence, StorytellerPhase.NIGHT, round), s.state.gameStateRevision)
        }
        s.confirmPukkaChoice(old, s.state.gameStateRevision)
        assertTrue(s.state.gameState.playerAt(old)!!.poisoned)
        nextNight(2)
        val before = s.state
        var rejectSave = true
        s.persistWith { if (it != before && rejectSave) error("disk full") }
        assertThrows(IllegalStateException::class.java) { s.confirmPukkaChoice(demon, before.gameStateRevision) }
        assertEquals(before, s.state)
        rejectSave = false
        s.confirmPukkaChoice(demon, before.gameStateRevision)
        s.confirmPukkaChoice(demon, before.gameStateRevision) // Repeated Next is idempotent, even with the old revision.
        assertTrue(s.state.gameState.playerAt(demon)!!.poisoned)
        assertFalse(s.state.gameState.playerAt(old)!!.poisoned)
        assertTrue(s.state.gameState.playerAt(old)!!.alive)
        assertTrue(s.state.pukkaPoisonsDue().isEmpty())
        nextNight(3)
        val revision = s.state.gameStateRevision
        assertThrows(IllegalStateException::class.java) { s.confirmPukkaChoice(later, revision - 1) }
        s.confirmPukkaChoice(later, revision)
        assertFalse(s.state.gameState.playerAt(later)!!.poisoned)
        val restored = ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state))
        assertEquals(s.state, restored)
        assertTrue(restored.gameState.playerAt(demon)!!.poisoned)
        assertTrue(restored.gameState.players.all { it.alive })
        assertEquals(3, restored.actionTimeline.entries.count { it.fact is ActionFact.PukkaChoice })
        assertFalse((restored.actionTimeline.entries.last().fact as ActionFact.PukkaChoice).poisonApplied)
        val resumed = ClocktowerGameSession.restore(restored)
        resumed.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("resumed-day", resumed.state.phase,
            resumed.state.round, resumed.nextInputSequence, StorytellerPhase.DAY, 3), resumed.state.gameStateRevision)
        resumed.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("resumed-night", resumed.state.phase,
            resumed.state.round, resumed.nextInputSequence, StorytellerPhase.NIGHT, 4), resumed.state.gameStateRevision)
        resumed.confirmPukkaChoice(old, resumed.state.gameStateRevision)
        assertTrue(resumed.state.gameState.playerAt(demon)!!.poisoned)
        assertFalse(resumed.state.gameState.playerAt(old)!!.poisoned)
        assertTrue(resumed.state.pukkaPoisonsDue().isEmpty())
    }


    @Test fun pukkaRepeatedTargetKeepsNewPoisonAfterProtectedAndLethalOldApplications() {
        val s = session("Pukka")
        val victim = s.state.gameState.players.first { it.actualType == CharacterType.TOWNSFOLK }.seat
        fun nextNight(round: Int) {
            s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day-$round", s.state.phase, s.state.round,
                s.nextInputSequence, StorytellerPhase.DAY, round - 1), s.state.gameStateRevision)
            s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night-$round", s.state.phase, s.state.round,
                s.nextInputSequence, StorytellerPhase.NIGHT, round), s.state.gameStateRevision)
        }
        s.confirmPukkaChoice(victim, s.state.gameStateRevision)
        nextNight(2)
        s.confirmPukkaChoice(victim, s.state.gameStateRevision)
        val first = s.state.pukkaPoisonsDue().single().fact.actionId
        val revision = s.state.gameStateRevision
        s.confirmPukkaPoisonResolution(first, false, revision) // Protected, but tonight's new poison remains.
        s.confirmPukkaPoisonResolution(first, false, revision)
        assertTrue(s.state.gameState.playerAt(victim)!!.alive)
        assertTrue(s.state.gameState.playerAt(victim)!!.poisoned)
        assertEquals(1, s.state.pukkaPoisons().size)
        nextNight(3)
        s.confirmPukkaChoice(victim, s.state.gameStateRevision)
        s.confirmPukkaPoisonResolution(s.state.pukkaPoisonsDue().single().fact.actionId, true, s.state.gameStateRevision)
        assertFalse(s.state.gameState.playerAt(victim)!!.alive)
        assertTrue(s.state.gameState.playerAt(victim)!!.poisoned)
        assertEquals(s.state, ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))
        nextNight(4)
        // No new choice (e.g. exorcism): the old application's delayed settlement still occurs.
        s.confirmPukkaPoisonResolution(s.state.pukkaPoisonsDue().single().fact.actionId, false, s.state.gameStateRevision)
        assertFalse(s.state.gameState.playerAt(victim)!!.poisoned)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Death })
    }

}
