package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

/**
 * Production-owned historical orchestration for confirmation-chain features.
 *
 * Canonical ActionFactTimeline / EpistemicObservationLog remain the only history owners. This
 * projector removes one already-committed observation at a time from a copied evaluation context,
 * batches every recipient-visible candidate through the exact evaluator, and feeds only exact
 * diagnostics into [ConfirmationChainFeaturesProjector]. It never mutates or persists history.
 */
internal object HistoricalConfirmationChainFeatureProjector {
    fun project(
        fullEvaluation: ExactConsequenceEvaluation.Ready,
        exactCandidates: List<ExactConsequenceCandidate>,
        sdeCandidates: List<SdeDecisionCandidate>,
        context: ExactConsequenceContext,
    ): Map<String, FeatureProjection<ConfirmationChainFeatures>> {
        require(exactCandidates.isNotEmpty()) {
            "Historical confirmation-chain projection requires legal candidates."
        }
        val candidateIds = exactCandidates.map(ExactConsequenceCandidate::candidateId)
        require(candidateIds.distinct().size == candidateIds.size) {
            "Historical confirmation-chain candidate IDs must be unique."
        }
        require(fullEvaluation.consequences.map(CandidateConsequence::candidateId) == candidateIds) {
            "Full exact consequences must preserve confirmation-chain candidate order."
        }
        require(sdeCandidates.map(SdeDecisionCandidate::candidateId) == candidateIds) {
            "SDE confirmation-chain candidates must preserve exact candidate order."
        }
        require(sdeCandidates.all { it.sourceRevision == context.sourceRevision }) {
            "Historical confirmation-chain candidates must share the exact source revision."
        }
        exactCandidates.zip(sdeCandidates).forEach { (exact, sde) ->
            require(exact.observations.map { it.observationId } == sde.hypotheticalRef.observationRecordIds) {
                "SDE hypothetical observation refs must match exact candidate semantics."
            }
        }

        if (sdeCandidates.any { it.historyPrefixRef is SdeHistoricalPrefixRef.NotCaptured }) {
            return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
        }

        val decisionPoints = sdeCandidates.map { candidate ->
            candidate.lifecycleStage as? SdeDecisionLifecycleStage.Interaction
                ?: return unavailable(candidateIds, FeatureUnavailableReason.NOT_APPLICABLE)
        }.distinct()
        require(decisionPoints.size == 1) {
            "One confirmation-chain decision must share one lifecycle point across all candidates."
        }
        val decisionPoint = decisionPoints.single()
        val historical = context.exactContext
        val observationRefs = historical.observationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            require(binding.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical confirmation-chain evaluation requires a committed prefix; " +
                    "observation ${record.recordId} is not before the decision point."
            }
            SdeHistoricalObservationRef(
                recordId = record.recordId,
                globalSequence = binding.point.globalSequence,
            )
        }
        val actionRefs = historical.actionTimeline.entries.map { entry ->
            require(entry.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical confirmation-chain evaluation requires a committed prefix; " +
                    "action ${entry.fact.actionId} is not before the decision point."
            }
            SdeHistoricalActionRef(
                actionId = entry.fact.actionId,
                globalSequence = entry.point.globalSequence,
            )
        }
        sdeCandidates.forEach { candidate ->
            val prefix = candidate.historyPrefixRef as SdeHistoricalPrefixRef.Global
            require(prefix.gameId == historical.initialSnapshot.gameId) {
                "Historical confirmation-chain prefix must reference the exact game."
            }
            require(prefix.actionRefs == actionRefs && prefix.observationRefs == observationRefs) {
                "Historical confirmation-chain projection requires the canonical committed prefix."
            }
        }

        val fullByCandidateId = fullEvaluation.consequences.associateBy(CandidateConsequence::candidateId)
        exactCandidates.forEach { candidate ->
            require(fullByCandidateId.getValue(candidate.candidateId).diagnostics.recipientSeat == candidate.recipientSeat) {
                "Full confirmation-chain diagnostics must preserve the exact recipient."
            }
        }

        val evidenceByCandidateId = candidateIds.associateWith {
            mutableListOf<ConfirmationLeaveOneOutEvidence>()
        }

        historical.observationLog.records.forEachIndexed { historyIndex, record ->
            val binding = record.timelineBinding as ObservationTimelineBinding.Global
            val visibleCandidates = exactCandidates.filter { candidate ->
                record.isVisibleTo(candidate.recipientSeat)
            }
            if (visibleCandidates.isEmpty()) return@forEachIndexed

            val omittedContext = historical.copy(
                observationLog = historical.observationLog.copy(
                    records = historical.observationLog.records.filterNot { it.recordId == record.recordId },
                ),
            )
            val queries = visibleCandidates.mapIndexed { candidateIndex, candidate ->
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "sde-confirmation-loo:$historyIndex:$candidateIndex:${candidate.candidateId}",
                    recipientSeat = candidate.recipientSeat,
                    observations = candidate.observations,
                    registrationWitnessBindings = candidate.registrationWitnessBindings,
                )
            }
            when (
                val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                    validatedRuleset = context.validatedRuleset,
                    context = omittedContext,
                    queries = queries,
                )
            ) {
                is ExactHypotheticalObservationBundleEvaluation.Deferred ->
                    return unavailable(candidateIds, FeatureUnavailableReason.MISSING_CAPABILITY)

                is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                    require(evaluation.diagnostics.size == visibleCandidates.size) {
                        "Leave-one-out exact evaluation must preserve visible candidate count."
                    }
                    visibleCandidates.zip(evaluation.diagnostics).forEach { (candidate, diagnostic) ->
                        evidenceByCandidateId.getValue(candidate.candidateId) +=
                            ConfirmationLeaveOneOutEvidence(
                                observationRef = SdeHistoricalObservationRef(
                                    recordId = record.recordId,
                                    globalSequence = binding.point.globalSequence,
                                ),
                                sourceSeat = record.sourceSeat,
                                sourceAbility = record.sourceAbility,
                                withoutObservation = diagnostic,
                            )
                    }
                }
            }
        }

        val sdeByCandidateId = sdeCandidates.associateBy(SdeDecisionCandidate::candidateId)
        return candidateIds.associateWith { candidateId ->
            FeatureProjection.Projected(
                ConfirmationChainFeaturesProjector.project(
                    currentSource = sdeByCandidateId.getValue(candidateId).sourceInteraction,
                    candidateId = candidateId,
                    full = fullByCandidateId.getValue(candidateId).diagnostics,
                    leaveOneOut = evidenceByCandidateId.getValue(candidateId),
                ),
            )
        }
    }

    private fun RecordedEpistemicObservation.isVisibleTo(recipientSeat: Int): Boolean =
        visibility == ObservationVisibility.PUBLIC || recipientSeat in recipientSeats

    private fun unavailable(
        candidateIds: List<String>,
        reason: FeatureUnavailableReason,
    ): Map<String, FeatureProjection<ConfirmationChainFeatures>> =
        candidateIds.associateWith { FeatureProjection.Unavailable(reason) }
}
