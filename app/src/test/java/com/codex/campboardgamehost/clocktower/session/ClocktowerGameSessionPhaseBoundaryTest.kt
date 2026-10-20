package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** A1: phase progress fact and revision belong to the same confirmed Session step. */
class ClocktowerGameSessionPhaseBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `first night to day atomically commits chronology and only one revision`() {
        val session = newSession()
        assertEquals(StorytellerPhase.FIRST_NIGHT, session.view.phase)
        assertEquals(1, session.view.round)
        val beforeState = session.state.gameState
        val draft = phase("first-day", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1)

        val committed = session.commitConfirmedPhaseAdvance(draft, expectedGameStateRevision = 0L)
        assertEquals(StorytellerPhase.DAY, session.view.phase)
        assertEquals(1, session.view.round)
        val accepted = session.state
        assertEquals(beforeState, accepted.gameState)
        assertEquals(1L, accepted.gameStateRevision)
        assertEquals(1L, accepted.nextTimelineGlobalSequence)
        assertEquals(committed, accepted.actionTimeline.entries.single())
        assertEquals(
            ActionFact.PhaseAdvance("first-day", 0L, StorytellerPhase.DAY, 1),
            committed.fact,
        )

        assertEquals(committed, session.commitConfirmedPhaseAdvance(draft, expectedGameStateRevision = 0L))
        assertEquals(accepted, session.state)
    }

    @Test
    fun `accepted phases must progress from last global history phase and expected revision`() {
        val session = newSession()
        session.commitConfirmedPhaseAdvance(
            phase("day-1", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1), 0L,
        )
        val acceptedFirst = session.state
        assertNotNull(runCatching {
            session.commitConfirmedPhaseAdvance(
                phase("wrong-from-night", StorytellerPhase.NIGHT, 1, StorytellerPhase.DAY, 1), 1L,
            )
        }.exceptionOrNull())
        assertEquals(acceptedFirst, session.state)
        assertNotNull(runCatching {
            session.commitConfirmedPhaseAdvance(
                phase("stale", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2), 0L,
            )
        }.exceptionOrNull())
        assertEquals(acceptedFirst, session.state)

        // A Dusk poison expiry in this same callback advances the live Session revision.
        // The following phase commit must use that new revision rather than a captured UI value.
        session.commitConfirmedPoisonTransition(
            ActionFactDraft.Poison(
                actionId = "dusk-expiry-1",
                phase = StorytellerPhase.DAY,
                round = 1,
                sequence = 2,
                targetSeat = null,
            ),
            expectedGameStateRevision = session.state.gameStateRevision,
        )
        session.commitConfirmedPhaseAdvance(
            phase("night-2", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2),
            expectedGameStateRevision = session.state.gameStateRevision,
        )
        session.commitConfirmedPhaseAdvance(
            phase("day-2", StorytellerPhase.NIGHT, 2, StorytellerPhase.DAY, 2),
            expectedGameStateRevision = session.state.gameStateRevision,
        )
        assertEquals(StorytellerPhase.DAY, session.view.phase)
        assertEquals(2, session.view.round)
        assertEquals(4L, session.state.gameStateRevision)
        assertEquals(4L, session.state.nextTimelineGlobalSequence)
        assertEquals(4, session.state.actionTimeline.entries.size)
    }

    @Test
    fun `illegal no-op or skipped round does not write any phase history`() {
        val session = newSession()
        val before = session.state
        for (draft in listOf(
            phase("not-night", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.NIGHT, 1),
            phase("skip-round", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 3),
            phase("same-phase", StorytellerPhase.DAY, 1, StorytellerPhase.DAY, 1),
        )) {
            assertNotNull(runCatching {
                session.commitConfirmedPhaseAdvance(draft, 0L)
            }.exceptionOrNull())
            assertEquals(before, session.state)
        }
    }

    private fun phase(
        actionId: String,
        from: StorytellerPhase,
        round: Int,
        to: StorytellerPhase,
        nextRound: Int,
    ) = ActionFactDraft.PhaseAdvance(
        actionId = actionId,
        phase = from,
        round = round,
        sequence = 1,
        nextPhase = to,
        nextRound = nextRound,
    )

    private fun newSession() = ClocktowerGameSession.createProduction(
        gameId = "a1-phase-boundary",
        gameSeed = initialState.seed,
        initialState = initialState,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
}
