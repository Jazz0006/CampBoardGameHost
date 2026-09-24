package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

/**
 * Read-only historical enrichment for contextual role-function exposure.
 *
 * Canonical ActionFactTimeline / EpistemicObservationLog remain the only durable history owners.
 * This projector reads only the committed prefix already bound to the SDE candidates, recognizes
 * typed positive role claims visible to the affected recipient, and reuses confirmation-chain
 * provenance for distinct-source authentication. It never persists exposure state or re-evaluates
 * confirmation worlds.
 */
internal object HistoricalRoleFunctionExposureFeatureProjector {
    fun project(
        baseEvidence: List<RoleFunctionExposureCandidateEvidence>,
        confirmationByCandidateId: Map<String, FeatureProjection<ConfirmationChainFeatures>>,
        sdeCandidates: List<SdeDecisionCandidate>,
        context: ExactConsequenceContext,
    ): Map<String, FeatureProjection<RoleFunctionExposureFeatures>> {
        if (baseEvidence.isEmpty()) return emptyMap()

        val candidateIds = baseEvidence.map(RoleFunctionExposureCandidateEvidence::candidateId)
        require(candidateIds.distinct().size == candidateIds.size) {
            "Historical role-exposure candidate IDs must be unique."
        }
        require(sdeCandidates.map(SdeDecisionCandidate::candidateId) == candidateIds) {
            "Historical role-exposure SDE candidates must preserve exposure candidate order."
        }
        require(confirmationByCandidateId.keys == candidateIds.toSet()) {
            "Historical role-exposure projection requires confirmation evidence for every legal candidate."
        }
        require(sdeCandidates.all { it.sourceRevision == context.sourceRevision }) {
            "Historical role-exposure candidates must share the exact source revision."
        }

        if (sdeCandidates.any { it.historyPrefixRef is SdeHistoricalPrefixRef.NotCaptured }) {
            return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
        }

        val decisionPoints = sdeCandidates.map { candidate ->
            candidate.lifecycleStage as? SdeDecisionLifecycleStage.Interaction
                ?: return unavailable(candidateIds, FeatureUnavailableReason.NOT_APPLICABLE)
        }.distinct()
        require(decisionPoints.size == 1) {
            "One role-exposure decision must share one lifecycle point across all candidates."
        }
        val decisionPoint = decisionPoints.single()
        val historical = context.exactContext
        val observationRefs = historical.observationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            require(binding.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical role-exposure evaluation requires a committed prefix; observation " +
                    record.recordId + " is not before the decision point."
            }
            SdeHistoricalObservationRef(
                recordId = record.recordId,
                globalSequence = binding.point.globalSequence,
            )
        }
        val actionRefs = historical.actionTimeline.entries.map { entry ->
            require(entry.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical role-exposure evaluation requires a committed prefix; action " +
                    entry.fact.actionId + " is not before the decision point."
            }
            SdeHistoricalActionRef(
                actionId = entry.fact.actionId,
                globalSequence = entry.point.globalSequence,
            )
        }
        sdeCandidates.forEach { candidate ->
            val prefix = candidate.historyPrefixRef as SdeHistoricalPrefixRef.Global
            require(prefix.gameId == historical.initialSnapshot.gameId) {
                "Historical role-exposure prefix must reference the exact game."
            }
            require(prefix.actionRefs == actionRefs && prefix.observationRefs == observationRefs) {
                "Historical role-exposure projection requires the canonical committed prefix."
            }
        }

        val confirmationUnavailableByCandidateId = confirmationByCandidateId
            .mapNotNull { (candidateId, projection) ->
                (projection as? FeatureProjection.Unavailable)?.let { candidateId to it.reason }
            }
            .toMap()

        val enrichedEvidence = baseEvidence.map { candidate ->
            val visibleExposureRecordIdsByTarget = candidate.directlyExposedTargets.associateWith { target ->
                historical.observationLog.records
                    .asSequence()
                    .filter { record -> record.isVisibleTo(target.recipientSeat) }
                    .filter { record -> record.proposition.exposes(target) }
                    .mapTo(linkedSetOf(), RecordedEpistemicObservation::recordId)
            }
            val derivedAlready = visibleExposureRecordIdsByTarget
                .filterValues(Set<String>::isNotEmpty)
                .keys
            val confirmation = confirmationByCandidateId.getValue(candidate.candidateId)
            val authenticatingRecordIds = when (confirmation) {
                is FeatureProjection.Projected ->
                    confirmation.value.historicalObservationImpacts
                        .filter(HistoricalObservationConfirmationImpact::authenticatesDistinctSource)
                        .mapTo(linkedSetOf()) { impact ->
                            impact.provenance.observationRef.recordId
                        }
                is FeatureProjection.Unavailable -> emptySet()
            }
            val derivedAmplified = visibleExposureRecordIdsByTarget
                .filterValues { recordIds -> recordIds.any(authenticatingRecordIds::contains) }
                .keys

            candidate.copy(
                alreadyExposedTargets = candidate.alreadyExposedTargets + derivedAlready,
                confirmationAmplifiedTargets =
                    candidate.confirmationAmplifiedTargets + derivedAmplified,
            )
        }

        val projected = RoleFunctionExposureFeaturesProjector.project(enrichedEvidence)
        return candidateIds.associateWith { candidateId ->
            val unavailableReason = confirmationUnavailableByCandidateId[candidateId]
            if (unavailableReason != null) {
                FeatureProjection.Unavailable(unavailableReason)
            } else {
                FeatureProjection.Projected(projected.getValue(candidateId))
            }
        }
    }

    private fun RecordedEpistemicObservation.isVisibleTo(recipientSeat: Int): Boolean =
        visibility == ObservationVisibility.PUBLIC || recipientSeat in recipientSeats

    private fun InformationProposition.exposes(target: RoleFunctionExposureTargetRef): Boolean =
        when (this) {
            is InformationProposition.RoleAt ->
                seat == target.seat && role == target.role
            is InformationProposition.AnyOf ->
                alternatives.any { proposition -> proposition.exposes(target) }
            is InformationProposition.AllOf ->
                propositions.any { proposition -> proposition.exposes(target) }
            else -> false
        }

    private fun unavailable(
        candidateIds: List<String>,
        reason: FeatureUnavailableReason,
    ): Map<String, FeatureProjection<RoleFunctionExposureFeatures>> =
        candidateIds.associateWith { FeatureProjection.Unavailable(reason) }
}
