package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every player-choice Next is its only commit boundary; there is no staged Session cache. */
class ConfirmedNightOperationTest {
    private fun nightSession(): ClocktowerGameSession {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val s = ClocktowerGameSession.createProduction(
            "direct-night", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-night-day", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-night", StorytellerPhase.DAY, 1,
            s.nextInputSequence, StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)
        return s
    }

    @Test fun `Poison immediately updates state and cannot be modified after Next`() {
        val s = nightSession()
        val start = s.state.gameStateRevision
        val first = s.confirmNightPoison(5, StorytellerPhase.NIGHT, 2, "poison-step")
        assertEquals(start + 1, s.state.gameStateRevision)
        assertTrue(s.state.gameState.playerAt(5)!!.poisoned)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Poison })
        val accepted = s.state
        assertSame(first, s.confirmNightPoison(5, StorytellerPhase.NIGHT, 2, "poison-step"))
        assertSame(accepted, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightPoison(6, StorytellerPhase.NIGHT, 2, "poison-step")
        }
        assertSame(accepted, s.state)
    }

    @Test fun `Monk protection and Imp attack happen with no begin-next-step callback`() {
        val s = nightSession()
        s.confirmNightProtection(6, StorytellerPhase.NIGHT, 2, "monk-step")
        assertEquals(6, s.state.nightTargets.monkSeat)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Protect })
        val accepted = s.state
        s.confirmNightProtection(6, StorytellerPhase.NIGHT, 2, "monk-step")
        assertSame(accepted, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightProtection(5, StorytellerPhase.NIGHT, 2, "monk-step")
        }
        s.confirmNightAttack(5, StorytellerPhase.NIGHT, 2, "imp-step")
        assertFalse(s.state.gameState.playerAt(5)!!.alive)
    }

    @Test fun `Butler first-night choice is committed immediately and next night is independent`() {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val game = base.copy(players = base.players.map { player ->
            if (player.seat == 4) player.copy(
                actualRole = RoleId("Butler"), shownRole = RoleId("Butler"),
                actualType = CharacterType.OUTSIDER,
            ) else player
        })
        val s = ClocktowerGameSession.createProduction(
            "direct-butler", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.confirmNightButlerMaster(4, 2, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        val first = s.state
        s.confirmNightButlerMaster(4, 2, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        assertSame(first, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightButlerMaster(4, 3, StorytellerPhase.FIRST_NIGHT, 1, "butler-step")
        }
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "first-to-day", StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence,
            StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-to-night", StorytellerPhase.DAY, 1, s.nextInputSequence,
            StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)
        s.confirmNightButlerMaster(4, 3, StorytellerPhase.NIGHT, 2, "butler-other-night")
        assertEquals(listOf(2, 3), s.state.actionTimeline.entries.mapNotNull {
            (it.fact as? ActionFact.ButlerMasterChoice)?.masterSeat
        })
    }

    @Test fun `Poisoner Monk Imp Butler commit in order with one authoritative Session`() {
        // Built-in other-night ordering is Poisoner -> Monk -> Imp -> Butler;
        // the poisoned Monk still chooses a target but cannot prevent the death.
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val game = base.copy(players = base.players.map { player ->
            when (player.seat) {
                3 -> player.copy(
                    actualRole = RoleId("Monk"), shownRole = RoleId("Monk"),
                    actualType = CharacterType.TOWNSFOLK,
                )
                4 -> player.copy(
                    actualRole = RoleId("Butler"), shownRole = RoleId("Butler"),
                    actualType = CharacterType.OUTSIDER,
                )
                7 -> player.copy(
                    actualRole = RoleId("Poisoner"), shownRole = RoleId("Poisoner"),
                    actualType = CharacterType.MINION,
                )
                else -> player
            }
        })
        val s = ClocktowerGameSession.createProduction(
            "night-vertical-slice", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "night-slice-first-day", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "night-slice-day-night", StorytellerPhase.DAY, 1,
            s.nextInputSequence, StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)

        s.confirmNightPoison(3, StorytellerPhase.NIGHT, 2, "slice-poison")
        assertTrue(s.state.gameState.playerAt(3)!!.poisoned)
        s.confirmNightProtection(5, StorytellerPhase.NIGHT, 2, "slice-monk")
        assertEquals(5, s.state.nightTargets.monkSeat)
        s.confirmNightAttack(5, StorytellerPhase.NIGHT, 2, "slice-imp")
        assertFalse(s.state.gameState.playerAt(5)!!.alive)
        s.confirmNightButlerMaster(4, 1, StorytellerPhase.NIGHT, 2, "slice-butler")

        val facts = s.state.actionTimeline.entries.map { it.fact }.filter {
            it is ActionFact.Poison || it is ActionFact.Protect ||
                it is ActionFact.Attack || it is ActionFact.Death ||
                it is ActionFact.ButlerMasterChoice
        }
        assertEquals(5, facts.size)
        assertTrue(facts[0] is ActionFact.Poison)
        assertTrue(facts[1] is ActionFact.Protect)
        assertTrue(facts[2] is ActionFact.Attack)
        assertTrue(facts[3] is ActionFact.Death)
        assertTrue(facts[4] is ActionFact.ButlerMasterChoice)

        val after = s.state
        s.confirmNightButlerMaster(4, 1, StorytellerPhase.NIGHT, 2, "slice-butler")
        assertSame(after, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.confirmNightButlerMaster(4, 2, StorytellerPhase.NIGHT, 2, "slice-butler")
        }
    }
}
