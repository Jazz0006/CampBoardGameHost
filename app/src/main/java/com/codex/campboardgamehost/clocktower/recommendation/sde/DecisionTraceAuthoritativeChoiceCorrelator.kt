package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource

/**
 * Correlates one already-authoritative structured information commit back to a pending diagnostic trace.
 *
 * This object never confirms a candidate and never commits canonical history. Correlation requires
 * both the returned committed observation and a post-commit canonical session view that contains it;
 * a preflight-only globally bound record is therefore insufficient evidence.
 */
internal object DecisionTraceAuthoritativeChoiceCorrelator {
    fun finalize(
        trace: DecisionTrace,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
        overrideReason: DecisionTraceOverrideReason? = null,
    ): DecisionTrace {
        require(trace.actualChoice is DecisionTraceActualChoice.Pending) {
            "Authoritative-choice correlation requires a pending DecisionTrace."
        }

        val snapshot = confirmed.contextSnapshot
        require(trace.decisionId == snapshot.semanticIdentity) {
            "DecisionTrace decision identity does not match the authoritative confirmation."
        }
        require(trace.sourceRevision == snapshot.revision) {
            "DecisionTrace source revision does not match the authoritative confirmation."
        }
        require(trace.legalCandidateIds == snapshot.legalCandidateIds) {
            "DecisionTrace legal candidate domain does not match the authoritative confirmation."
        }

        val expectedLifecycle = SdeDecisionLifecycleStage.Interaction(
            phase = confirmed.draft.phase,
            round = confirmed.draft.round,
            sequence = confirmed.draft.sequence,
        )
        require(trace.lifecycleStage == expectedLifecycle) {
            "DecisionTrace lifecycle does not match the authoritative confirmation."
        }
        require(confirmed.candidateId in trace.legalCandidateIds) {
            "Authoritative candidate must belong to the traced legal candidate domain."
        }

        val globalPrefix = trace.historyPrefixRef as? SdeHistoricalPrefixRef.Global
            ?: throw IllegalArgumentException(
                "Authoritative-choice correlation requires a canonical global history prefix.",
            )
        require(postCommitSession.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            "DecisionTrace correlation requires GLOBAL_V1 post-commit session history."
        }
        require(postCommitSession.gameId == globalPrefix.gameId) {
            "Post-commit session game identity does not match the DecisionTrace."
        }
        require(postCommitSession.gameStateRevision == trace.sourceRevision.gameStateRevision) {
            "Game-state revision changed between DecisionTrace evaluation and authoritative commit."
        }
        require(postCommitSession.playerInputRevision > trace.sourceRevision.playerInputRevision) {
            "Post-commit player-input revision must advance beyond the DecisionTrace source revision."
        }

        val globalBinding = committedObservation.timelineBinding as? ObservationTimelineBinding.Global
            ?: throw IllegalArgumentException(
                "DecisionTrace correlation requires a globally committed observation.",
            )
        require(confirmed.draft.matches(committedObservation)) {
            "Committed observation does not match the authoritative confirmation draft."
        }
        val persisted = postCommitSession.epistemicObservationLog.records
            .singleOrNull { it.recordId == committedObservation.recordId }
        require(persisted == committedObservation) {
            "Post-commit canonical observation log does not contain the committed observation."
        }
        require(postCommitSession.nextTimelineGlobalSequence > globalBinding.point.globalSequence) {
            "Post-commit semantic timeline cursor has not advanced beyond the committed observation."
        }

        // Reconstruct only the prefix before this commit, not the entire current session:
        // an idempotent archive retry may arrive after subsequent observations/actions.
        val commitSequence = globalBinding.point.globalSequence
        val actionRefs = postCommitSession.actionTimeline.entries
            .filter { it.point.globalSequence < commitSequence }
            .map { entry ->
                require(entry.point.isStrictlyBeforeSdeDecision(expectedLifecycle)) {
                    "Pre-commit action history is not strictly before the traced decision."
                }
                SdeHistoricalActionRef(entry.fact.actionId, entry.point.globalSequence)
            }
        val observationRefs = postCommitSession.epistemicObservationLog.records.mapNotNull { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: throw IllegalArgumentException("Canonical correlation history must be globally bound.")
            if (binding.point.globalSequence >= commitSequence) return@mapNotNull null
            require(binding.point.isStrictlyBeforeSdeDecision(expectedLifecycle)) {
                "Pre-commit observation history is not strictly before the traced decision."
            }
            SdeHistoricalObservationRef(record.recordId, binding.point.globalSequence)
        }
        require(globalPrefix.actionRefs == actionRefs && globalPrefix.observationRefs == observationRefs) {
            "DecisionTrace history prefix does not match the complete canonical pre-commit history."
        }

        val manualOverride = confirmed.source == InformationDecisionSource.MANUAL
        require(overrideReason == null || manualOverride) {
            "Override rationale requires an explicit manual authoritative choice."
        }

        return trace.copy(
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = confirmed.candidateId,
                source = confirmed.source,
                manualOverride = manualOverride,
                overrideReason = overrideReason,
            ),
        )
    }
}
