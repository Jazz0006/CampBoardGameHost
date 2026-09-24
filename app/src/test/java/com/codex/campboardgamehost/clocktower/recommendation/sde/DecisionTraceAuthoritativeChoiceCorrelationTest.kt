package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
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
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionTraceAuthoritativeChoiceCorrelationTest {
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
                semanticIdentity = DECISION_ID,
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
