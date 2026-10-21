package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ClocktowerDayFlowTest {
    private fun session(vararg roles: String, ngj: Boolean = false): ClocktowerGameSession {
        val script = if (ngj) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing
        val defs = clocktowerRoleDefinitionsForScript(script).associateBy { it.id.value }
        val game = GameState(script.toRecommendationScriptId(), roles.mapIndexed { i, role ->
            val d = requireNotNull(defs[role])
            PlayerState(i + 1, "P${i + 1}", d.id, d.alignment, d.type)
        }, 81)
        return ClocktowerGameSession.createProduction("day-test", 81, game, ClocktowerSemanticHistoryMode.GLOBAL_V1).also {
            it.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day-one", StorytellerPhase.FIRST_NIGHT, 1,
                it.nextInputSequence, StorytellerPhase.DAY, 1), it.state.gameStateRevision)
        }
    }
    private fun poison(s: ClocktowerGameSession, seat: Int) {
        // Test fixture supplies an already-confirmed effect from the preceding night.
        s.commitConfirmedGameAction(ActionFactDraft.Poison("fixture-poison", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, seat), s.state.gameState.copy(players = s.state.gameState.players.map {
                it.copy(poisoned = it.seat == seat) }), s.state.gameStateRevision)
    }
    private fun kill(s: ClocktowerGameSession, seat: Int) = s.commitConfirmedPlayerDeath(
        ActionFactDraft.Death("fixture-death-$seat", s.state.phase, s.state.round, s.nextInputSequence, seat), s.state.gameStateRevision)
    private fun vote(s: ClocktowerGameSession, actor: Int, target: Int, vararg voters: Int) {
        assertFalse(s.confirmNomination(actor, target, null))
        s.confirmVote(actor, target, voters.toList())
    }
    private fun clone(s: ClocktowerGameSession) = ClocktowerGameSession.restore(ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(s.state)))

    @Test fun `one end-day confirmation executes standing and immediately enters next night exactly once`() {
        val s = session("Imp", "Soldier", "Chef", "Saint", "Empath")
        vote(s, 3, 2, 1, 2, 3)
        s.confirmEndDay(1)
        val accepted = s.state
        s.confirmEndDay(1)
        assertSame(accepted, s.state)
        assertEquals(StorytellerPhase.NIGHT, s.state.phase)
        assertEquals(2, s.state.round)
        assertFalse(s.state.gameState.playerAt(2)!!.alive)
        assertEquals(1, s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Execution>().size)
        s.confirmVote(3, 2, listOf(1, 2, 3), 1)
        assertSame(accepted, s.state)
    }
    @Test fun `tie records no execution rather than arbitrarily killing a nominee`() {
        val s = session("Imp", "Soldier", "Chef", "Saint", "Empath")
        vote(s, 3, 2, 1, 2, 3)
        vote(s, 4, 5, 1, 2, 3)
        assertNull(s.state.voteStanding.nomineeSeat)
        s.confirmEndDay(1)
        assertTrue(s.state.gameState.players.all { it.alive })
        assertEquals(1, s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.NoExecution>().size)
    }
    @Test fun `pending confirmed nomination survives serialization and blocks another nomination or end-day`() {
        val s = session("Imp", "Soldier", "Chef", "Saint", "Empath")
        s.confirmNomination(3, 2, null)
        val restored = clone(s)
        assertEquals(2, restored.state.pendingVote()?.nomineeSeat)
        val before = restored.state
        assertThrows(IllegalStateException::class.java) { restored.confirmEndDay(1) }
        assertThrows(IllegalStateException::class.java) { restored.confirmNomination(4, 5, null) }
        assertSame(before, restored.state)
        restored.confirmNomination(3, 2, null)
        restored.confirmVote(3, 2, listOf(1, 2, 3))
        val after = restored.state
        restored.confirmVote(3, 2, listOf(1, 2, 3))
        assertSame(after, restored.state)
        assertNull(restored.state.pendingVote())
        assertEquals(2, clone(restored).state.voteStanding.nomineeSeat)
    }
    @Test fun `ghost vote is accepted once and repeat on a later nomination is rejected without mutation`() {
        val s = session("Imp", "Soldier", "Chef", "Saint", "Empath")
        kill(s, 2)
        vote(s, 3, 4, 1, 2)
        s.confirmNomination(4, 5, null)
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) { s.confirmVote(4, 5, listOf(1, 2)) }
        assertSame(before, s.state)
        s.confirmVote(4, 5, listOf(1, 3))
        assertEquals(listOf(2), s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Vote>().first().ghostVoterSeats)
    }
    @Test fun `functioning Butler needs the actual committed master vote`() {
        val s = session("Imp", "Butler", "Chef", "Saint", "Empath")
        s.commitConfirmedGameAction(ActionFactDraft.ButlerMasterChoice("master", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, 2, 3), s.state.gameState, s.state.gameStateRevision)
        s.confirmNomination(3, 4, null)
        assertThrows(IllegalArgumentException::class.java) { s.confirmVote(3, 4, listOf(1, 2)) }
        s.confirmVote(3, 4, listOf(2, 3))
        assertEquals(listOf(2, 3), s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Vote>().single().voterSeats)
    }
    @Test fun `Saint execution ends evil immediately but a poisoned Saint does not`() {
        for (poisoned in listOf(false, true)) {
            val s = session("Imp", "Saint", "Chef", "Soldier", "Empath")
            if (poisoned) poison(s, 2)
            vote(s, 3, 2, 1, 2, 3)
            s.confirmEndDay(1)
            assertEquals(if(poisoned) null else GameEndReason.SAINT, s.state.outcome?.reason)
            assertEquals(if(poisoned) StorytellerPhase.NIGHT else StorytellerPhase.DAY, s.state.phase)
            if(poisoned) assertTrue(s.state.gameState.players.none { it.poisoned })
        }
    }
    @Test fun `Mayor no-execution win requires exactly three living and a functioning Mayor`() {
        for (poisoned in listOf(false, true)) {
            val s = session("Imp", "Mayor", "Chef", "Soldier", "Empath")
            kill(s, 4); kill(s, 5)
            if(poisoned) poison(s, 2)
            s.confirmEndDay(1)
            assertEquals(if(poisoned) null else GameEndReason.MAYOR, s.state.outcome?.reason)
        }
    }
    @Test fun `executed Imp promotes Scarlet Woman only with five alive before death`() {
        for (aliveBefore in listOf(4, 5)) {
            val s = session("Imp", "Scarlet Woman", "Chef", "Soldier", "Empath")
            if(aliveBefore == 4) kill(s, 5)
            vote(s, 3, 1, 1, 2, 3)
            s.confirmEndDay(1)
            assertEquals(if(aliveBefore == 5) RoleId("Imp") else RoleId("Scarlet Woman"), s.state.gameState.playerAt(2)!!.actualRole)
            assertEquals(if(aliveBefore == 5) 2 else null, s.state.pendingImpNotification()?.targetSeat)
            assertEquals(if(aliveBefore == 5) null else GameEndReason.NO_DEMON, s.state.outcome?.reason)
        }
    }
    @Test fun `Virgin first Townsfolk nomination executes and closes Day without voting or second confirmation`() {
        val s = session("Imp", "Virgin", "Chef", "Soldier", "Empath")
        assertTrue(s.confirmNomination(3, 2, null))
        assertFalse(s.state.gameState.playerAt(3)!!.alive)
        assertEquals(StorytellerPhase.NIGHT, s.state.phase)
        val accepted = s.state
        assertTrue(s.confirmNomination(3, 2, null, 1))
        assertSame(accepted, s.state)
        assertEquals(0, s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Vote>().size)
    }
    @Test fun `Spy registration is explicit and poisoned Virgin consumes first nomination without executing`() {
        val s = session("Imp", "Virgin", "Spy", "Soldier", "Empath")
        val before = s.state
        assertThrows(IllegalStateException::class.java) { s.confirmNomination(3, 2, null) }
        assertSame(before, s.state)
        assertFalse(s.confirmNomination(3, 2, false))
        assertEquals(false, s.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Nomination>().single().nominatorRegistersAsTownsfolk)
        val poisoned = session("Imp", "Virgin", "Chef", "Soldier", "Empath")
        poison(poisoned, 2)
        assertFalse(poisoned.confirmNomination(3, 2, null))
        assertTrue(poisoned.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Nomination>().single().firstVirginNomination)
    }
    @Test fun `Slayer fixed Demon hit and succession are one idempotent responsibility`() {
        val s = session("Imp", "Scarlet Woman", "Slayer", "Soldier", "Empath")
        s.confirmSlayerShot(3, 1, null)
        val after = s.state
        s.confirmSlayerShot(3, 1, null)
        assertSame(after, s.state)
        assertFalse(s.state.gameState.playerAt(1)!!.alive)
        assertEquals(RoleId("Imp"), s.state.gameState.playerAt(2)!!.actualRole)
        assertEquals(StorytellerPhase.DAY, s.state.phase)
        assertThrows(IllegalStateException::class.java) { s.confirmSlayerShot(3, 2, null) }
    }
    @Test fun `poisoned or false Slayer cannot kill and real Recluse alone needs registration`() {
        val s = session("Imp", "Recluse", "Slayer", "Soldier", "Empath")
        assertThrows(IllegalStateException::class.java) { s.confirmSlayerShot(3, 2, null) }
        s.confirmSlayerShot(3, 2, true)
        assertFalse(s.state.gameState.playerAt(2)!!.alive)
        val p = session("Imp", "Recluse", "Slayer", "Soldier", "Empath")
        poison(p, 3)
        p.confirmSlayerShot(3, 1, null)
        p.confirmSlayerShot(4, 1, null)
        assertTrue(p.state.gameState.playerAt(1)!!.alive)
    }
    @Test fun `Klutz execution waits for real choice then finishes that same day once`() {
        val s = session("Imp", "Klutz", "Artist", "Empath", "Clockmaker", ngj = true)
        vote(s, 3, 2, 1, 2, 3)
        s.confirmEndDay(1)
        assertEquals(StorytellerPhase.DAY, s.state.phase)
        val death = requireNotNull(s.state.pendingKlutzDeath())
        val restored = clone(s)
        restored.confirmKlutzChoice(death.actionId, 3)
        assertEquals(StorytellerPhase.NIGHT, restored.state.phase)
        assertEquals(2, restored.state.round)
        assertNull(restored.state.pendingKlutzDeath())
    }
    @Test fun `Artist actual private answer and consumption survive restore with no fabricated truth observation`() {
        val s = session("Imp", "Klutz", "Artist", "Empath", "Clockmaker", ngj = true)
        assertThrows(IllegalArgumentException::class.java) { s.confirmArtistAnswer(3, true, false) }
        s.confirmArtistAnswer(3, true, true)
        val after = s.state
        s.confirmArtistAnswer(3, true, true)
        assertSame(after, s.state)
        val restored = clone(s)
        val answer = restored.state.epistemicObservationLog.records.single()
        assertEquals(setOf(3), answer.recipientSeats)
        assertEquals(ObservationVisibility.PRIVATE, answer.visibility)
        assertTrue(restored.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.ArtistAnswer>().single().abilityConsumed)
        assertThrows(IllegalStateException::class.java) { restored.confirmArtistAnswer(3, true, false) }
    }
    @Test fun `failed end-day persistence restores the full pre-confirmation state and no partial outcome`() {
        val s = session("Imp", "Saint", "Chef", "Soldier", "Empath")
        vote(s, 3, 2, 1, 2, 3)
        var fail = false
        var saves = 0
        s.persistWith { if(fail) throw IOException("disk full") else saves++ }
        val before = s.state
        fail = true
        assertThrows(IOException::class.java) { s.confirmEndDay(1) }
        assertSame(before, s.state)
        assertTrue(s.state.gameState.playerAt(2)!!.alive)
        fail = false
        s.confirmEndDay(1)
        assertEquals(GameEndReason.SAINT, s.state.outcome?.reason)
        assertEquals(2, saves) // attach + one complete Day responsibility
    }
}
