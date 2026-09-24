package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

/**
 * Production-owned orchestration for whole-table healthy-information utility.
 *
 * Canonical history remains the only history owner. A historical ability observation is considered
 * healthy only when authoritative replay says its source was FUNCTIONING at that point. For a
 * functioning ability, actual truth or legal registered truth is a rules/legality invariant of the
 * committed observation;
 * player-facing ObservationReliability is never used to infer hidden impairment state.
 *
 * Route independence is candidate-relative and score-free. Current-recipient history reuses the
 * existing exact confirmation leave-one-out evidence. A functioning route delivered to another
 * recipient is a separate whole-table information channel and does not trigger another world
 * enumeration merely to prove that recipient boundary.
 */
internal object HistoricalHealthyInformationUtilityFeatureProjector {
    fun project(
        fullEvaluation: ExactConsequenceEvaluation.Ready,
        confirmationByCandidateId: Map<String, FeatureProjection<ConfirmationChainFeatures>>,
        exactCandidates: List<ExactConsequenceCandidate>,
        sdeCandidates: List<SdeDecisionCandidate>,
        truthRelationByCandidateId: Map<String, TruthRelation>,
        context: ExactConsequenceContext,
    ): Map<String, FeatureProjection<HealthyInformationUtilityFeatures>> {
        require(exactCandidates.isNotEmpty()) {
            "Historical healthy-information projection requires legal candidates."
        }
        val candidateIds = exactCandidates.map(ExactConsequenceCandidate::candidateId)
        require(candidateIds.distinct().size == candidateIds.size) {
            "Historical healthy-information candidate IDs must be unique."
        }
        require(fullEvaluation.consequences.map(CandidateConsequence::candidateId) == candidateIds) {
            "Full exact consequences must preserve healthy-information candidate order."
        }
        require(sdeCandidates.map(SdeDecisionCandidate::candidateId) == candidateIds) {
            "SDE healthy-information candidates must preserve exact candidate order."
        }
        require(confirmationByCandidateId.keys == candidateIds.toSet()) {
            "Healthy-information projection requires confirmation evidence for every legal candidate."
        }
        require(truthRelationByCandidateId.keys.all(candidateIds::contains)) {
            "Healthy-information truth relation may reference only legal candidates."
        }
        require(sdeCandidates.all { it.sourceRevision == context.sourceRevision }) {
            "Historical healthy-information candidates must share the exact source revision."
        }
        exactCandidates.zip(sdeCandidates).forEach { (exact, sde) ->
            require(
                exact.observations.map(EpistemicObservation::observationId) ==
                    sde.hypotheticalRef.observationRecordIds,
            ) {
                "SDE healthy-information hypothetical refs must match exact candidate semantics."
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
            "One healthy-information decision must share one lifecycle point across all candidates."
        }
        val decisionPoint = decisionPoints.single()
        val historical = context.exactContext
        val observationRefs = historical.observationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            require(binding.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical healthy-information evaluation requires a committed prefix; observation " +
                    record.recordId + " is not before the decision point."
            }
            SdeHistoricalObservationRef(record.recordId, binding.point.globalSequence)
        }
        val actionRefs = historical.actionTimeline.entries.map { entry ->
            require(entry.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical healthy-information evaluation requires a committed prefix; action " +
                    entry.fact.actionId + " is not before the decision point."
            }
            SdeHistoricalActionRef(entry.fact.actionId, entry.point.globalSequence)
        }
        sdeCandidates.forEach { candidate ->
            val prefix = candidate.historyPrefixRef as SdeHistoricalPrefixRef.Global
            require(prefix.gameId == historical.initialSnapshot.gameId) {
                "Historical healthy-information prefix must reference the exact game."
            }
            require(prefix.actionRefs == actionRefs && prefix.observationRefs == observationRefs) {
                "Historical healthy-information projection requires the canonical committed prefix."
            }
        }

        val allSeats = historical.initialSnapshot.gameState.players.mapTo(sortedSetOf()) { it.seat }
        val healthyHistoricalRoutes = mutableListOf<HealthyHistoricalRoute>()
        historical.observationLog.records.forEach { record ->
            if (record.reliability == ObservationReliability.NOT_ABILITY_INFORMATION) {
                return@forEach
            }
            if (record.sourceSeat == null || record.sourceAbility == null) {
                return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            }
            val abilityState = HistoricalInformationAbilityStateResolver.resolve(record, context)
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            if (abilityState != AbilityState.FUNCTIONING) {
                return@forEach
            }
            val recipients = when (record.visibility) {
                ObservationVisibility.PRIVATE -> record.recipientSeats
                ObservationVisibility.PUBLIC -> allSeats
            }
            if (recipients.isEmpty()) {
                return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            }
            recipients.sorted().forEach { recipientSeat ->
                healthyHistoricalRoutes += HealthyHistoricalRoute(
                    record = record,
                    routeRef = HealthyInformationRouteRef.HistoricalObservation(
                        recordId = record.recordId,
                        recipientSeat = recipientSeat,
                    ),
                )
            }
        }

        val exactByCandidateId = exactCandidates.associateBy(ExactConsequenceCandidate::candidateId)
        val sdeByCandidateId = sdeCandidates.associateBy(SdeDecisionCandidate::candidateId)
        val consequenceByCandidateId =
            fullEvaluation.consequences.associateBy(CandidateConsequence::candidateId)

        return candidateIds.associateWith { candidateId ->
            val exact = exactByCandidateId.getValue(candidateId)
            val sde = sdeByCandidateId.getValue(candidateId)
            val consequence = consequenceByCandidateId.getValue(candidateId)
            val currentAbilityState = sde.sourceInteraction.abilityState
                ?: return@associateWith FeatureProjection.Unavailable(
                    FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED,
                )
            val truthRelation = truthRelationByCandidateId[candidateId]
            if (currentAbilityState == AbilityState.FUNCTIONING && truthRelation == null) {
                return@associateWith FeatureProjection.Unavailable(
                    FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED,
                )
            }

            val confirmation = when (
                val projection = confirmationByCandidateId.getValue(candidateId)
            ) {
                is FeatureProjection.Projected -> projection.value
                is FeatureProjection.Unavailable ->
                    return@associateWith FeatureProjection.Unavailable(projection.reason)
            }
            val impactByRecordId = confirmation.historicalObservationImpacts.associateBy {
                it.provenance.observationRef.recordId
            }

            val routeEvidence = healthyHistoricalRoutes.map { route ->
                val sameRecipientImpact = if (route.routeRef.recipientSeat == exact.recipientSeat) {
                    impactByRecordId[route.record.recordId]
                        ?: return@associateWith FeatureProjection.Unavailable(
                            FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED,
                        )
                } else {
                    null
                }
                HistoricalHealthyInformationRouteEvidence(
                    routeRef = route.routeRef,
                    independentlyUsableBefore =
                        sameRecipientImpact?.wasIndependentlyConstrainingBefore ?: true,
                    relationToCurrentCandidate = sameRecipientImpact?.relation,
                )
            }

            val currentHealthy =
                currentAbilityState == AbilityState.FUNCTIONING &&
                    truthRelation in setOf(
                        TruthRelation.TRUE_TO_ACTUAL_STATE,
                        TruthRelation.TRUE_TO_REGISTERED_STATE,
                    ) &&
                    sde.sourceInteraction.abilityRole != null
            FeatureProjection.Projected(
                HealthyInformationUtilityFeaturesProjector.project(
                    HealthyInformationCandidateEvidence(
                        candidateId = candidateId,
                        recipientSeat = exact.recipientSeat,
                        historyFeasibleBefore =
                            consequence.diagnostics.before.value.signum() > 0,
                        candidateHistoryFeasibleAfter =
                            consequence.diagnostics.after.value.signum() > 0,
                        currentCandidateHealthy = currentHealthy,
                        currentCandidateIndependentlyConstraining =
                            currentHealthy &&
                                consequence.diagnostics.before.value >
                                    consequence.diagnostics.after.value,
                        historicalRoutes = routeEvidence,
                    ),
                ),
            )
        }
    }

    private data class HealthyHistoricalRoute(
        val record: RecordedEpistemicObservation,
        val routeRef: HealthyInformationRouteRef.HistoricalObservation,
    )

    private fun unavailable(
        candidateIds: List<String>,
        reason: FeatureUnavailableReason,
    ): Map<String, FeatureProjection<HealthyInformationUtilityFeatures>> =
        candidateIds.associateWith { FeatureProjection.Unavailable(reason) }
}
