package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerHistoricalActionObservationCaptureTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = initialState.script,
        scriptContentHash = "e12f6425ece137da02477a642235c797",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.VERIFIED,
    )

    @Test
    fun `action then observation share one session-owned global timeline and survive restore`() {
        val session = newGlobalSession()

        val action = session.commitGlobalActionFact(
            ActionFactDraft.Poison(
                actionId = "night-one-poison-seat-two",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 4,
                targetSeat = 2,
            ),
        )
        val observation = session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "night-one-chef-result",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 5,
                sourceSeat = 1,
                sourceAbility = RoleId("Chef"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.PlayerCount(8),
            ),
        )

        assertEquals(0L, action.point.globalSequence)
        assertEquals(1L, (observation.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence)
        assertEquals(2L, session.snapshot.nextTimelineGlobalSequence)
        assertEquals(listOf("night-one-poison-seat-two"), session.snapshot.actionTimeline.reducerFacts().map { it.actionId })

        val restored = ClocktowerGameSession.restore(session.snapshot)
        assertEquals(session.snapshot.actionTimeline, restored.snapshot.actionTimeline)
        assertEquals(2L, restored.snapshot.nextTimelineGlobalSequence)
    }

    @Test
    fun `observation then action also consumes the same global cursor without collision`() {
        val session = newGlobalSession()

        val observation = session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "first-observation",
                phase = StorytellerPhase.DAY,
                round = 1,
                sequence = 0,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(2, false),
            ),
        )
        val action = session.commitGlobalActionFact(
            ActionFactDraft.Execution(
                actionId = "day-one-execution-seat-two",
                phase = StorytellerPhase.DAY,
                round = 1,
                sequence = 1,
                targetSeat = 2,
            ),
        )

        assertEquals(0L, (observation.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence)
        assertEquals(1L, action.point.globalSequence)
        assertEquals(2L, session.snapshot.nextTimelineGlobalSequence)
    }

    @Test
    fun `global action commit is idempotent by stable action id and rejects conflicting reuse`() {
        val session = newGlobalSession()
        val draft = ActionFactDraft.Death(
            actionId = "night-two-death-seat-three",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 7,
            targetSeat = 3,
        )

        val first = session.commitGlobalActionFact(draft)
        val duplicate = session.commitGlobalActionFact(draft)

        assertEquals(first, duplicate)
        assertEquals(1, session.snapshot.actionTimeline.entries.size)
        assertEquals(1L, session.snapshot.nextTimelineGlobalSequence)

        assertThrows(IllegalArgumentException::class.java) {
            session.commitGlobalActionFact(draft.copy(targetSeat = 4))
        }
        assertEquals(1L, session.snapshot.nextTimelineGlobalSequence)
    }

    @Test
    fun `legacy local session cannot invent global identity for a new action`() {
        val legacy = ClocktowerGameSession.create(
            gameId = "legacy-history",
            gameSeed = initialState.seed,
            rulesetRef = rulesetRef,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        )

        assertThrows(IllegalArgumentException::class.java) {
            legacy.commitGlobalActionFact(
                ActionFactDraft.PhaseAdvance(
                    actionId = "legacy-phase-advance",
                    phase = StorytellerPhase.DAY,
                    round = 1,
                    sequence = 0,
                    nextPhase = StorytellerPhase.DAY,
                    nextRound = 1,
                ),
            )
        }
        assertEquals(0L, legacy.snapshot.nextTimelineGlobalSequence)
    }

    private fun newGlobalSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "game-2026-08-23-r6-history",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
}
