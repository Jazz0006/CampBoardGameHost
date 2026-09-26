package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionTraceAuthoritativeChoiceCorrelationTest {
    @Test
    fun `correlation requires complete exact ordered precommit history`() {
        val confirmed = confirmedDecision(InformationDecisionSource.MANUAL).let {
            it.copy(draft = it.draft.copy(sequence = 3))
        }
        fun observation(id: String, local: Int, global: Long) = confirmed.draft
            .copy(recordId = id, sequence = local)
            .bindGlobal(TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, local, global))
        val earlier = observation("earlier", 0, 0)
        val second = observation("second", 2, 2)
        val committed = confirmed.draft.bindGlobal(
            TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 3, 3),
        )
        val later = observation("later", 4, 4)
        val action = TimelineBoundActionFact(
            ActionFact.Poison("poison", 1, 2),
            TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 1, 1),
        )
        val session = postCommitSession(committed, nextTimelineGlobalSequence = 5).copy(
            actionTimeline = ActionFactTimeline(listOf(action)),
            epistemicObservationLog = EpistemicObservationLog(listOf(earlier, second, committed, later)),
        )
        val prefix = SdeHistoricalPrefixRef.Global(
            "game-1", listOf(SdeHistoricalActionRef("poison", 1)),
            listOf(SdeHistoricalObservationRef("earlier", 0), SdeHistoricalObservationRef("second", 2)),
        )
        val trace = pendingTrace().copy(
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(StorytellerPhase.FIRST_NIGHT, 1, 3),
            historyPrefixRef = prefix,
        )
        assertTrue(DecisionTraceAuthoritativeChoiceCorrelator.finalize(
            trace, confirmed, committed, session,
        ).actualChoice is DecisionTraceActualChoice.Committed)

        val invalidPrefixes = listOf(
            prefix.copy(actionRefs = emptyList()),
            prefix.copy(actionRefs = listOf(SdeHistoricalActionRef("missing", 1))),
            prefix.copy(actionRefs = listOf(SdeHistoricalActionRef("poison", 6))),
            prefix.copy(observationRefs = emptyList()),
            prefix.copy(observationRefs = prefix.observationRefs.take(1)),
            prefix.copy(observationRefs = prefix.observationRefs.reversed()),
            prefix.copy(observationRefs = listOf(SdeHistoricalObservationRef("missing", 0), prefix.observationRefs[1])),
            prefix.copy(observationRefs = listOf(SdeHistoricalObservationRef("earlier", 6), prefix.observationRefs[1])),
            prefix.copy(observationRefs = prefix.observationRefs + SdeHistoricalObservationRef(committed.recordId, 3)),
            prefix.copy(observationRefs = prefix.observationRefs + SdeHistoricalObservationRef("later", 4)),
        )
        invalidPrefixes.forEach { invalid ->
            assertThrows("Must reject $invalid", IllegalArgumentException::class.java) {
                DecisionTraceAuthoritativeChoiceCorrelator.finalize(
                    trace.copy(historyPrefixRef = invalid), confirmed, committed, session,
                )
            }
        }
        // A globally earlier record with same/future decision-local time is not valid evidence.
        listOf(3, 4).forEach { local ->
            val invalidEarlier = observation("earlier", local, 0)
            assertThrows(IllegalArgumentException::class.java) {
                DecisionTraceAuthoritativeChoiceCorrelator.finalize(trace, confirmed, committed,
                    session.copy(epistemicObservationLog = EpistemicObservationLog(
                        listOf(invalidEarlier, second, committed, later),
                    )),
                )
            }
        }
        val invalidAction = action.copy(point = action.point.copy(sequence = 3))
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceAuthoritativeChoiceCorrelator.finalize(trace, confirmed, committed,
                session.copy(actionTimeline = ActionFactTimeline(listOf(invalidAction))),
            )
        }
    }

    @Test
    fun `missing history is rejected before durable archive write`() {
        val trace = pendingTrace().copy(historyPrefixRef = SdeHistoricalPrefixRef.Global(
            "game-1", emptyList(), listOf(SdeHistoricalObservationRef("missing", 0)),
        ))
        val confirmed = confirmedDecision(InformationDecisionSource.MANUAL)
        val committed = confirmed.draft.bindGlobal(TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 0, 1))
        val raw = DecisionTraceArchiveJsonCodec.encode(DecisionTraceArchive().append(trace))
        var writes = 0
        val store = DecisionTraceArchiveStore(readRaw = { raw }, writeRaw = { writes++; true })
        assertThrows(IllegalArgumentException::class.java) {
            store.correlateCommittedChoice(trace.archiveKey, confirmed, committed,
                postCommitSession(committed, nextTimelineGlobalSequence = 2))
        }
        assertEquals(0, writes)
        assertEquals(trace, store.load().find(trace.archiveKey))
    }

    @Test
    fun `optional runtime correlation is a no-op when shadow trace is absent`() {
        val confirmed = confirmedDecision(InformationDecisionSource.RECOMMENDATION_ACCEPTED)
        val committed = committedObservation(confirmed)
        var writes = 0
        val store = DecisionTraceArchiveStore(readRaw = { null }, writeRaw = { writes++; true })

        assertTrue(store.correlateCommittedChoiceIfPresent(
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
            confirmed = confirmed,
            committedObservation = committed,
            postCommitSession = postCommitSession(committed),
        ))
        assertEquals(0, writes)
    }

    @Test
    fun `diagnostic write rejection is isolated after canonical commit evidence exists`() {
        val trace = pendingTrace()
        val confirmed = confirmedDecision(InformationDecisionSource.RECOMMENDATION_ACCEPTED)
        val committed = committedObservation(confirmed)
        val postCommit = postCommitSession(committed)
        val raw = DecisionTraceArchiveJsonCodec.encode(DecisionTraceArchive().append(trace))
        val store = DecisionTraceArchiveStore(readRaw = { raw }, writeRaw = { false })

        val report = SdePostCommitCorrelationCoordinator.correlate(
            store = store,
            confirmed = confirmed,
            committedObservation = committed,
            postCommitSession = postCommit,
        )

        assertFalse(report.completed)
        assertTrue(postCommit.epistemicObservationLog.records.contains(committed))
        assertEquals(trace, store.load().find(trace.archiveKey))
    }

    @Test
    fun `matching authoritative commit finalizes pending trace`() {
        val trace = pendingTrace()
        val confirmed = confirmedDecision(source = InformationDecisionSource.RECOMMENDATION_ACCEPTED)
        val committed = committedObservation(confirmed)

        val finalized = DecisionTraceAuthoritativeChoiceCorrelator.finalize(
            trace = trace,
            confirmed = confirmed,
            committedObservation = committed,
            postCommitSession = postCommitSession(committed),
        )

        assertEquals(
            DecisionTraceActualChoice.Committed(
                candidateId = "candidate-a",
                source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
                manualOverride = false,
            ),
            finalized.actualChoice,
        )
        assertEquals(trace.copy(actualChoice = finalized.actualChoice), finalized)
    }

    @Test
    fun `manual authoritative commit records override and optional reason`() {
        val reason = DecisionTraceOverrideReason(
            code = "storyteller-context",
            text = "Prefer the alternate legal story.",
        )
        val confirmed = confirmedDecision(source = InformationDecisionSource.MANUAL)
        val committed = committedObservation(confirmed)
        val finalized = DecisionTraceAuthoritativeChoiceCorrelator.finalize(
            trace = pendingTrace(),
            confirmed = confirmed,
            committedObservation = committed,
            postCommitSession = postCommitSession(committed),
            overrideReason = reason,
        )

        assertEquals(
            DecisionTraceActualChoice.Committed(
                candidateId = "candidate-a",
                source = InformationDecisionSource.MANUAL,
                manualOverride = true,
                overrideReason = reason,
            ),
            finalized.actualChoice,
        )
    }

    @Test
    fun `correlation rejects stale or mismatched decision identity revision domain and lifecycle`() {
        val trace = pendingTrace()
        val confirmed = confirmedDecision(source = InformationDecisionSource.RECOMMENDATION_ACCEPTED)
        val committed = committedObservation(confirmed)

        val mismatches = listOf(
            trace.copy(decisionId = "other-decision"),
            trace.copy(sourceRevision = InformationDecisionRevision(9, 5)),
            trace.copy(
                legalCandidateIds = listOf("candidate-a"),
                featureEvaluation = DecisionFeatureEvaluation.Deferred(
                    candidateIds = listOf("candidate-a"),
                    missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
                ),
                policySnapshot = DecisionTracePolicySnapshot.Deferred(
                    policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                    candidateIds = listOf("candidate-a"),
                    reasons = setOf(PolicyDeferralCode("test-deferred")),
                ),
            ),
            trace.copy(
                lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    sequence = 1,
                ),
            ),
        )

        mismatches.forEach { mismatched ->
            assertThrows(IllegalArgumentException::class.java) {
                DecisionTraceAuthoritativeChoiceCorrelator.finalize(
                    trace = mismatched,
                    confirmed = confirmed,
                    committedObservation = committed,
                    postCommitSession = postCommitSession(committed),
                )
            }
        }
    }

    @Test
    fun `correlation requires exact post-commit canonical observation evidence`() {
        val trace = pendingTrace()
        val confirmed = confirmedDecision(source = InformationDecisionSource.RECOMMENDATION_ACCEPTED)

        val mismatched = committedObservation(confirmed).copy(
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat = 2,
                subjectSeats = listOf(1, 3),
                value = 1,
            ),
        )
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceAuthoritativeChoiceCorrelator.finalize(
                trace,
                confirmed,
                mismatched,
                postCommitSession(mismatched),
            )
        }

        val legacy = committedObservation(confirmed).copy(
            timelineBinding = ObservationTimelineBinding.LegacyLocal,
        )
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceAuthoritativeChoiceCorrelator.finalize(
                trace,
                confirmed,
                legacy,
                postCommitSession(legacy),
            )
        }

        val preflightOnly = committedObservation(confirmed)
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceAuthoritativeChoiceCorrelator.finalize(
                trace,
                confirmed,
                preflightOnly,
                postCommitSession(
                    record = preflightOnly,
                    playerInputRevision = REVISION.playerInputRevision,
                    includeRecord = false,
                    nextTimelineGlobalSequence = 0,
                ),
            )
        }
    }

    @Test
    fun `archive allows only pending to committed transition and exact retry`() {
        val pending = pendingTrace()
        val confirmed = confirmedDecision(source = InformationDecisionSource.RECOMMENDATION_ACCEPTED)
        val committed = committedObservation(confirmed)
        val finalized = DecisionTraceAuthoritativeChoiceCorrelator.finalize(
            pending,
            confirmed,
            committed,
            postCommitSession(committed),
        )
        val original = DecisionTraceArchive().append(pending)
        val updated = original.finalizeActualChoice(finalized)

        assertEquals(finalized, updated.find(pending.archiveKey))
        assertEquals(updated, updated.finalizeActualChoice(finalized))

        val conflicting = finalized.copy(
            evidenceCheckpoint = EvidenceCheckpointId("different-evidence"),
        )
        assertThrows(IllegalArgumentException::class.java) {
            updated.finalizeActualChoice(conflicting)
        }
    }

    @Test
    fun `durable store correlation writes once and identical retry is idempotent`() {
        val pending = pendingTrace()
        val confirmed = confirmedDecision(source = InformationDecisionSource.MANUAL)
        val committed = committedObservation(confirmed)
        var raw: String? = DecisionTraceArchiveJsonCodec.encode(
            DecisionTraceArchive().append(pending),
        )
        var writes = 0
        val store = DecisionTraceArchiveStore(
            readRaw = { raw },
            writeRaw = { next ->
                writes += 1
                raw = next
                true
            },
        )

        assertTrue(
            store.correlateCommittedChoice(
                key = pending.archiveKey,
                confirmed = confirmed,
                committedObservation = committed,
                postCommitSession = postCommitSession(committed),
            ),
        )
        assertEquals(1, writes)
        val actual = requireNotNull(store.load().find(pending.archiveKey)).actualChoice
        assertEquals(
            DecisionTraceActualChoice.Committed(
                candidateId = "candidate-a",
                source = InformationDecisionSource.MANUAL,
                manualOverride = true,
            ),
            actual,
        )

        assertTrue(
            store.correlateCommittedChoice(
                key = pending.archiveKey,
                confirmed = confirmed,
                committedObservation = committed,
                postCommitSession = postCommitSession(committed),
            ),
        )
        assertEquals(1, writes)

        val conflicting = confirmedDecision(
            source = InformationDecisionSource.MANUAL,
            candidateId = "candidate-b",
        )
        val conflictingCommitted = committedObservation(conflicting)
        assertThrows(IllegalArgumentException::class.java) {
            store.correlateCommittedChoice(
                key = pending.archiveKey,
                confirmed = conflicting,
                committedObservation = conflictingCommitted,
                postCommitSession = postCommitSession(conflictingCommitted),
            )
        }
        assertEquals(1, writes)
    }

    private fun pendingTrace(): DecisionTrace {
        val candidateIds = listOf("candidate-a", "candidate-b")
        return DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
            decisionId = DECISION_ID,
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
            ),
            sourceRevision = REVISION,
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = "game-1",
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidateIds,
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("test-deferred")),
            ),
            policySelection = null,
        )
    }

    private fun confirmedDecision(
        source: InformationDecisionSource,
        candidateId: String = "candidate-a",
    ): ConfirmedInformationDecision {
        val draft = observationDraft(candidateId)
        return ConfirmedInformationDecision(
            candidateId = candidateId,
            source = source,
            warnings = emptyList(),
            draft = draft,
            contextSnapshot = InformationDecisionSnapshot(
                requestIdentity = InformationDecisionRequestIdentity("game-1", DECISION_ID),
                revision = REVISION,
                legalCandidateIds = listOf("candidate-a", "candidate-b"),
                recommendedCandidateIds = setOf("candidate-a"),
            ),
        )
    }

    private fun observationDraft(candidateId: String): EpistemicObservationDraft {
        val value = if (candidateId == "candidate-a") 0 else 1
        return EpistemicObservationDraft(
            recordId = "private-game-1-empath-$candidateId",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(2),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat = 2,
                subjectSeats = listOf(1, 3),
                value = value,
            ),
        )
    }

    private fun committedObservation(
        confirmed: ConfirmedInformationDecision,
    ): RecordedEpistemicObservation = confirmed.draft.bindGlobal(
        TimelinePoint(
            phase = confirmed.draft.phase,
            round = confirmed.draft.round,
            sequence = confirmed.draft.sequence,
            globalSequence = 0,
        ),
    )

    private fun postCommitSession(
        record: RecordedEpistemicObservation,
        playerInputRevision: Long = REVISION.playerInputRevision + 1,
        includeRecord: Boolean = true,
        nextTimelineGlobalSequence: Long = 1,
    ): ClocktowerSessionView = ClocktowerSessionView(
        gameId = "game-1",
        scriptId = ScriptId("tb"),
        gameStateRevision = REVISION.gameStateRevision,
        playerInputRevision = playerInputRevision,
        gameSeed = 7L,
        actionTimeline = ActionFactTimeline(),
        epistemicObservationLog = if (includeRecord) {
            EpistemicObservationLog(listOf(record))
        } else {
            EpistemicObservationLog()
        },
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        nextTimelineGlobalSequence = nextTimelineGlobalSequence,
    )

    private companion object {
        const val DECISION_ID =
            "numeric|Empath|game-1|FirstNight|1|0|2|LIVING_EVIL_NEIGHBOURS"
        val REVISION = InformationDecisionRevision(
            gameStateRevision = 3,
            playerInputRevision = 5,
        )
    }
}
