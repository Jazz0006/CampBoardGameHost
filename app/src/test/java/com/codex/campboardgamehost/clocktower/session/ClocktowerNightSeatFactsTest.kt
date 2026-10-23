package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.*
import org.junit.Test

class ClocktowerNightSeatFactsTest {
    private fun session(vararg roles: String, poisonedSeat: Int? = null, drunkShownAs: String? = null): ClocktowerGameSession {
        val defs = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing).associateBy { it.id.value }
        val game = GameState(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), roles.mapIndexed { i, role ->
            val d = requireNotNull(defs[role])
            PlayerState(i + 1, "P${i + 1}", d.id, d.alignment, d.type,
                shownRole = drunkShownAs?.takeIf { role == "Drunk" }?.let(::RoleId), poisoned = poisonedSeat == i + 1)
        }, 11)
        return ClocktowerGameSession.createProduction("facts", 11, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }
    private fun ClocktowerGameSession.advance(phase: StorytellerPhase, round: Int) = commitConfirmedPhaseAdvance(
        ActionFactDraft.PhaseAdvance("advance-$phase-$round", state.phase, state.round, nextInputSequence, phase, round),
        state.gameStateRevision)
    private fun ClocktowerGameSession.kill(seat: Int) = commitConfirmedPlayerDeath(
        ActionFactDraft.Death("death-$seat", state.phase, state.round, nextInputSequence, seat), state.gameStateRevision)

    @Test fun `living seats, Demon and neighbours come from Session, skipping the dead`() {
        val s = session("Imp", "Empath", "Poisoner", "Soldier", "Saint")
        s.kill(3)
        assertEquals(setOf(1, 2, 4, 5), s.state.aliveSeats())
        assertEquals(setOf(1, 2, 3, 4, 5), s.state.allSeats())
        assertEquals(1, s.state.livingDemonSeat())
        assertEquals(listOf(3), s.state.actualTypeSeats(CharacterType.MINION))
        assertEquals(listOf(1, 4), s.state.livingNeighbourSeats(2))
        s.kill(1)
        assertNull(s.state.livingDemonSeat())
    }

    @Test fun `poisoned and drunk subjects are read from the waking seat`() {
        val poisoned = session("Imp", "Empath", "Poisoner", "Soldier", "Saint", poisonedSeat = 2)
        assertEquals(2, poisoned.state.poisonedSeatForRole("Empath"))
        assertEquals(AbilityFunctioningState.POISONED, poisoned.state.abilitySubjectForRole("Empath", 2)
            ?.let { AbilityFunctioningSemantics.stateFor(it, "Empath") })
        val drunk = session("Imp", "Drunk", "Poisoner", "Soldier", "Saint", drunkShownAs = "Empath")
        assertNull(drunk.state.poisonedSeatForRole("Empath"))
        assertEquals(AbilityFunctioningState.DRUNK, drunk.state.abilitySubjectForRole("Empath", 2)
            ?.let { AbilityFunctioningSemantics.stateFor(it, "Empath") })
        assertNull(drunk.state.abilitySubjectForRole("Empath", null))
    }

    @Test fun `Mayor redirect seats exclude only the attacked Mayor awaiting its ruling`() {
        val s = session("Imp", "Mayor", "Empath", "Soldier", "Saint")
        s.advance(StorytellerPhase.DAY, 1)
        s.advance(StorytellerPhase.NIGHT, 2)
        assertEquals(setOf(1, 2, 3, 4, 5), s.state.mayorRedirectEligibleSeats())
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, "other_night:role:Imp")
        assertEquals(2, s.state.mayorRulingSeat())
        assertEquals(setOf(1, 3, 4, 5), s.state.mayorRedirectEligibleSeats())
    }
}
