package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerGlobalObservationCommitTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = initialState.script,
        scriptContentHash = "e12f6425ece137da02477a642235c797",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.VERIFIED,
    )

    @Test
    fun `global session atomically assigns first timeline identity and commits observation`() {
        val session = newGlobalSession()
        val beforeGameRevision = session.snapshot.gameStateRevision

        val committed = session.commitGlobalEpistemicObservation(privateDraft("private-chef", localSequence = 7))

        assertEquals(
            ObservationTimelineBinding.Global(
                TimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 7, globalSequence = 0L),
            ),
            committed.timelineBinding,
        )
        assertEquals(listOf(committed), session.snapshot.epistemicObservationLog.records)
        assertEquals(1L, session.snapshot.nextTimelineGlobalSequence)
        assertEquals(1L, session.snapshot.playerInputRevision)
        assertEquals(beforeGameRevision, session.snapshot.gameStateRevision)
    }

    @Test
    fun `stateless session transition needs no ruleset or game state`() {
        val committed = ClocktowerGameSession.commitGlobalEpistemicObservation(
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            observationLog = EpistemicObservationLog(),
            nextTimelineGlobalSequence = 0L,
            playerInputRevision = 0L,
            draft = privateDraft("ngj-safe", localSequence = 3),
        )

        assertEquals(0L, committed.record.globalSequence())
        assertEquals(1L, committed.nextTimelineGlobalSequence)
        assertEquals(1L, committed.playerInputRevision)
        assertEquals(listOf(committed.record), committed.observationLog.records)
    }

    @Test
    fun `private and public observations share one global allocator regardless of local sequence`() {
        val session = newGlobalSession()

        val privateRecord = session.commitGlobalEpistemicObservation(
            privateDraft("private-chef", localSequence = 12),
        )
        val publicRecord = session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "public-death",
                phase = StorytellerPhase.DAWN,
                round = 1,
                sequence = 99,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(3, false),
            ),
        )

        assertEquals(0L, privateRecord.globalSequence())
        assertEquals(1L, publicRecord.globalSequence())
        assertEquals(2L, session.snapshot.nextTimelineGlobalSequence)
        assertEquals(2L, session.snapshot.playerInputRevision)
    }

    @Test
    fun `restored global session continues persisted cursor`() {
        val original = newGlobalSession()
        original.commitGlobalEpistemicObservation(privateDraft("first", localSequence = 2))
        val restored = ClocktowerGameSession.restore(original.snapshot)

        val second = restored.commitGlobalEpistemicObservation(privateDraft("second", localSequence = 0))

        assertEquals(1L, second.globalSequence())
        assertEquals(2L, restored.snapshot.nextTimelineGlobalSequence)
    }

    @Test
    fun `exact duplicate record id is idempotent and consumes no global slot`() {
        val session = newGlobalSession()
        val draft = privateDraft("same-record", localSequence = 4)
        val first = session.commitGlobalEpistemicObservation(draft)
        val afterFirst = session.snapshot

        val second = session.commitGlobalEpistemicObservation(draft)

        assertSame(first, second)
        assertSame(afterFirst, session.snapshot)
        assertEquals(1L, session.snapshot.nextTimelineGlobalSequence)
        assertEquals(1L, session.snapshot.playerInputRevision)
    }

    @Test
    fun `same record id with different content fails without consuming cursor`() {
        val session = newGlobalSession()
        session.commitGlobalEpistemicObservation(privateDraft("same-record", localSequence = 4))
        val before = session.snapshot

        assertFails {
            session.commitGlobalEpistemicObservation(
                privateDraft("same-record", localSequence = 5),
            )
        }

        assertSame(before, session.snapshot)
    }

    @Test
    fun `legacy local session cannot use global commit API`() {
        val session = ClocktowerGameSession.create(
            gameId = "legacy-session",
            gameSeed = initialState.seed,
            rulesetRef = rulesetRef,
            initialState = initialState,
        )
        val before = session.snapshot

        assertFails {
            session.commitGlobalEpistemicObservation(privateDraft("private-chef", localSequence = 1))
        }

        assertSame(before, session.snapshot)
    }

    @Test
    fun `global session rejects direct injection of a prebound durable record`() {
        val snapshot = newGlobalSession().snapshot.copy(nextTimelineGlobalSequence = 5L)
        val session = ClocktowerGameSession.restore(snapshot)
        val before = session.snapshot
        val externallyBound = RecordedEpistemicObservation(
            recordId = "externally-bound",
            phase = StorytellerPhase.DAWN,
            round = 1,
            sequence = 3,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, false),
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(StorytellerPhase.DAWN, 1, 3, 3L),
            ),
        )

        assertFails { session.recordEpistemicObservation(externallyBound) }

        assertSame(before, session.snapshot)
    }

    @Test
    fun `global cursor exhaustion fails without partial allocation or commit`() {
        val session = ClocktowerGameSession.restore(
            newGlobalSession().snapshot.copy(nextTimelineGlobalSequence = Long.MAX_VALUE),
        )
        val before = session.snapshot

        assertFails {
            session.commitGlobalEpistemicObservation(privateDraft("overflow", localSequence = 0))
        }

        assertSame(before, session.snapshot)
    }

    @Test
    fun `player input revision exhaustion fails before log or cursor changes`() {
        val beforeLog = EpistemicObservationLog()

        assertFails {
            ClocktowerGameSession.commitGlobalEpistemicObservation(
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                observationLog = beforeLog,
                nextTimelineGlobalSequence = 9L,
                playerInputRevision = Long.MAX_VALUE,
                draft = privateDraft("revision-overflow", localSequence = 0),
            )
        }

        assertTrue(beforeLog.records.isEmpty())
    }

    private fun newGlobalSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "global-session",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )

    private fun privateDraft(recordId: String, localSequence: Int): EpistemicObservationDraft =
        EpistemicObservationDraft(
            recordId = recordId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = localSequence,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS,
                1,
                (1..8).toList(),
                1,
            ),
        )

    private fun RecordedEpistemicObservation.globalSequence(): Long =
        (timelineBinding as ObservationTimelineBinding.Global).point.globalSequence

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected global observation commit contract to fail closed.", failed)
    }
}
