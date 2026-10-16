package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Real confirmed action order: typed facts and accepted-step revision must never diverge. */
class ClocktowerGameSessionConfirmedActionBoundaryTest {
    private val base = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `attack and protection confirmations are ordered without duplicating mechanical state`() {
        val session = create("a1-night-actions")
        val original = session.state.gameState
        val attack = ActionFactDraft.Attack(
            actionId = "night-two-attack-eight",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            targetSeat = 8,
        )
        val protect = ActionFactDraft.Protect(
            actionId = "night-two-protect-eight",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 2,
            targetSeat = 8,
        )
        val first = confirm(session, attack)
        val second = confirm(session, protect)
        assertEquals(original, session.state.gameState)
        assertEquals(2L, session.state.gameStateRevision)
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(listOf(first, second), session.state.actionTimeline.entries)
        assertEquals(listOf(0L, 1L), session.state.actionTimeline.entries.map { it.fact.sequence })
        assertEquals(1, session.state.actionTimeline.entries.count { it.fact is ActionFact.Attack })
        assertEquals(1, session.state.actionTimeline.entries.count { it.fact is ActionFact.Protect })

        val accepted = session.state
        assertEquals(first, session.commitConfirmedGameAction(attack, original, 0L))
        assertEquals(accepted, session.state)
        assertNotNull(runCatching {
            session.commitConfirmedGameAction(
                attack.copy(targetSeat = 7),
                session.state.gameState,
                session.state.gameStateRevision,
            )
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
    }

    @Test
    fun `nomination vote slayer miss and no execution commit as four actual steps`() {
        val session = create("a1-public-actions")
        val facts = listOf(
            ActionFactDraft.Nomination(
                actionId = "day-nomination", phase = StorytellerPhase.DAY, round = 2,
                sequence = 1, nominatorSeat = 1, nomineeSeat = 8,
                firstVirginNomination = false,
            ),
            ActionFactDraft.Vote(
                actionId = "day-vote", phase = StorytellerPhase.DAY, round = 2,
                sequence = 2, nominatorSeat = 1, nomineeSeat = 8,
                voterSeats = listOf(1, 3, 5), ghostVoterSeats = emptyList(),
            ),
            ActionFactDraft.SlayerShot(
                actionId = "slayer-miss", phase = StorytellerPhase.DAY, round = 2,
                sequence = 3, claimantSeat = 2, targetSeat = 5,
                abilityConsumed = true, hit = false,
            ),
            ActionFactDraft.NoExecution(
                actionId = "no-execution-day-two", phase = StorytellerPhase.DAY,
                round = 2, sequence = 4,
            ),
        )
        val committed = facts.map { confirm(session, it) }
        assertEquals(4L, session.state.gameStateRevision)
        assertEquals(4L, session.state.nextTimelineGlobalSequence)
        assertEquals(committed, session.state.actionTimeline.entries)
        assertEquals(base, session.state.gameState)
        assertEquals(1, committed.count { it.fact is ActionFact.NoExecution })
        assertEquals(1, committed.count { it.fact is ActionFact.Vote })
        assertEquals(1, committed.count { it.fact is ActionFact.Nomination })
        assertEquals(1, committed.count { it.fact is ActionFact.SlayerShot })

        val accepted = session.state
        assertNotNull(runCatching {
            session.commitConfirmedGameAction(
                ActionFactDraft.NoExecution(
                    actionId = "stale-day-close", phase = StorytellerPhase.DAY,
                    round = 2, sequence = 5,
                ),
                base,
                expectedGameStateRevision = 0L,
            )
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
    }

    private fun confirm(session: ClocktowerGameSession, draft: ActionFactDraft) =
        session.commitConfirmedGameAction(
            draft = draft,
            nextGameState = session.state.gameState,
            expectedGameStateRevision = session.state.gameStateRevision,
        )

    private fun create(id: String) = ClocktowerGameSession.createProduction(
        gameId = id,
        gameSeed = base.seed,
        initialState = base,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
}
