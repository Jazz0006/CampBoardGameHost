package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

class ClocktowerNightVoteOwnershipTest {
    private val base = TroubleBrewingFixtures.eightPlayerExample()
    private fun session() = ClocktowerGameSession.createProduction(
        "night-vote", base.seed, base, ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
    private fun commit(s: ClocktowerGameSession, draft: ActionFactDraft) =
        s.commitConfirmedGameAction(draft, s.state.gameState, s.state.gameStateRevision)
    private fun attack(s: ClocktowerGameSession, seat: Int) = ActionFactDraft.Attack(
        "attack-${s.nextInputSequence}", StorytellerPhase.NIGHT, 2, s.nextInputSequence, seat,
    )
    private fun dependents(s: ClocktowerGameSession) {
        commit(s, ActionFactDraft.MayorRedirectChoice("mayor-${s.nextInputSequence}",
            StorytellerPhase.NIGHT, 2, s.nextInputSequence, 1, 3))
    }

    @Test fun `changed upstream confirmation invalidates dependents while same targets preserve them`() {
        for (kind in listOf("attack", "protection", "poison")) {
            val s = session()
            fun draft(seat: Int): ActionFactDraft = when (kind) {
                "attack" -> attack(s, seat)
                "protection" -> ActionFactDraft.Protect("protect-${s.nextInputSequence}",
                    StorytellerPhase.NIGHT, 2, s.nextInputSequence, seat)
                else -> ActionFactDraft.Poison("poison-${s.nextInputSequence}",
                    StorytellerPhase.NIGHT, 2, s.nextInputSequence, seat)
            }
            commit(s, draft(4))
            dependents(s)
            commit(s, draft(4))
            assertEquals(kind, 3, s.state.nightTargets.mayorRedirectSeat)
            commit(s, draft(5))
            assertNull(kind, s.state.nightTargets.mayorRedirectSeat)
        }
    }

    @Test fun `retry and rejected stale action cannot change accepted targets or facts`() {
        val s = session()
        val original = attack(s, 4)
        commit(s, original)
        dependents(s)
        val accepted = s.state
        s.commitConfirmedGameAction(original, base, 0)
        assertEquals(accepted, s.state)
        assertNotNull(runCatching { s.commitConfirmedGameAction(attack(s, 5), base, 0) }.exceptionOrNull())
        assertEquals(accepted, s.state)
    }

    @Test fun `empty attack confirmation invalidates its dependent Mayor choice`() {
        val s = session()
        commit(s, attack(s, 4))
        val beforeChoice = s.state
        dependents(s)
        assertEquals(beforeChoice.playerInputRevision, s.state.playerInputRevision)
        // The one game revision belongs to the Mayor fact, not pending succession.
        assertEquals(beforeChoice.gameStateRevision + 1, s.state.gameStateRevision)
        assertEquals(base, s.state.gameState)
        val accepted = s.state
        assertEquals(accepted, s.state)
        s.clearConfirmedNightAttack(s.state.gameStateRevision)
        assertEquals(ClocktowerNightTargets(), s.state.nightTargets)
        assertEquals(accepted.gameStateRevision + 1, s.state.gameStateRevision)
        assertEquals(accepted.actionTimeline, s.state.actionTimeline)
        commit(s, ActionFactDraft.Protect("protect", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 3))
        dependents(s)
        s.clearConfirmedNightProtection(s.state.gameStateRevision)
        assertEquals(ClocktowerNightTargets(), s.state.nightTargets)
        assertEquals(base, s.state.gameState)
    }

    @Test fun `night cleanup and new night retain only their intended pending facts`() {
        val s = session()
        commit(s, attack(s, 4))
        commit(s, ActionFactDraft.Protect("protect", StorytellerPhase.NIGHT, 2, s.nextInputSequence, 3))
        dependents(s)
        s.finishNightResolution()
        assertEquals(ClocktowerNightTargets(attackSeat = 4), s.state.nightTargets)
        commit(s, attack(s, 4))
        commit(s, ActionFactDraft.PhaseAdvance("next-night", StorytellerPhase.DAY, 2,
            s.nextInputSequence, StorytellerPhase.NIGHT, 3))
        assertEquals(ClocktowerNightTargets(), s.state.nightTargets)
    }

    @Test fun `explicit poison expiry at Dawn preserves the separate Mayor choice`() {
        val s = session()
        s.commitConfirmedPoisonTransition(ActionFactDraft.Poison("poison", StorytellerPhase.NIGHT,
            2, s.nextInputSequence, 4), s.state.gameStateRevision)
        dependents(s)
        s.commitConfirmedPoisonTransition(ActionFactDraft.Poison("expiry", StorytellerPhase.NIGHT,
            2, s.nextInputSequence, null), s.state.gameStateRevision, preserveNightChoices = true)
        assertEquals(3, s.state.nightTargets.mayorRedirectSeat)
        assertNull(s.state.confirmedPoisonTargetName(2))
    }

    private fun vote(s: ClocktowerGameSession, nominee: Int, count: Int) = ActionFactDraft.Vote(
        "vote-${s.nextInputSequence}", StorytellerPhase.DAY, 2, s.nextInputSequence,
        8, nominee, (1..count).toList(), emptyList(),
    )

    @Test fun `accepted votes own highest standing tie retry and phase reset`() {
        val s = session()
        commit(s, vote(s, 1, 3))
        assertEquals(ClocktowerVoteStanding(), s.state.voteStanding)
        val high = vote(s, 1, 5)
        commit(s, high)
        assertEquals(ClocktowerVoteStanding(1, 5), s.state.voteStanding)
        commit(s, vote(s, 2, 4))
        assertEquals(ClocktowerVoteStanding(1, 5), s.state.voteStanding)
        commit(s, vote(s, 2, 5))
        assertEquals(ClocktowerVoteStanding(null, 5), s.state.voteStanding)
        val accepted = s.state
        s.commitConfirmedGameAction(high, base, 0)
        assertEquals(accepted, s.state)
        assertNotNull(runCatching { s.commitConfirmedGameAction(vote(s, 3, 6), base, 0) }.exceptionOrNull())
        assertEquals(accepted, s.state)
        commit(s, vote(s, 3, 6))
        assertEquals(ClocktowerVoteStanding(3, 6), s.state.voteStanding)
        commit(s, ActionFactDraft.PhaseAdvance("night", StorytellerPhase.DAY, 2,
            s.nextInputSequence, StorytellerPhase.NIGHT, 3))
        assertEquals(ClocktowerVoteStanding(), s.state.voteStanding)
    }

    @Test fun `threshold is read when vote is confirmed`() {
        val s = session()
        commit(s, vote(s, 1, 3))
        assertEquals(ClocktowerVoteStanding(), s.state.voteStanding)
        for (seat in listOf(7, 8)) {
            s.commitConfirmedPlayerDeath(ActionFactDraft.Death("death-$seat", StorytellerPhase.DAY,
                2, s.nextInputSequence, seat), s.state.gameStateRevision)
        }
        commit(s, vote(s, 2, 3))
        assertEquals(ClocktowerVoteStanding(2, 3), s.state.voteStanding)
    }
}
