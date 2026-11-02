package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.ClocktowerGhostVoteAuthority
import com.codex.campboardgamehost.ClocktowerSeatId
import org.junit.Assert.*
import org.junit.Test

class ClocktowerLifeChangesTest {
    private fun session(): ClocktowerGameSession {
        val layout = BadMoonRisingOpening.generate((1..8).map { "P$it" }, 12L)
        val zombuul = BadMoonRisingOpening.assign(layout, layout.players.single { it.actualType == CharacterType.DEMON }.seat, "Zombuul")
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
}
