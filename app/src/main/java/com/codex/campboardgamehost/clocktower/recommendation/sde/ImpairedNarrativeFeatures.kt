package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState

internal enum class ImpairmentLifetime {
    PERSISTENT_SETUP_BOUND,
    TEMPORARY_ACTION_BOUND,
}

internal enum class ImpairedNarrativeRelation {
    NO_PRIOR_IMPAIRED_NARRATIVE,
    COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE,
    BREAKS_PRIOR_IMPAIRED_NARRATIVE,
    PRIOR_NARRATIVE_ALREADY_INFEASIBLE,
}

internal enum class NarrativeTransitionNecessity {
    NONE,
    AVOIDABLE,
    FORCED,
}

internal enum class ImpairedNarrativeDetectabilitySignal {
    NONE,
    AVOIDABLE_NARRATIVE_BREAK,
    FORCED_NARRATIVE_BREAK,
    PRIOR_NARRATIVE_ALREADY_INFEASIBLE,
}

/**
 * Score-free description of how one currently impaired candidate relates to the already-committed
 * impaired narrative visible to the same recipient.
 *
 * This is a derived feature only. It owns no persistent narrative state and carries no probability,
 * weight, player-count threshold or policy ordering.
 */
internal data class ImpairedNarrativeFeatures(
    val currentAbilityState: AbilityState,
    val impairmentLifetime: ImpairmentLifetime,
    val priorImpairedObservationIds: Set<String>,
    val contradictoryPriorObservationIds: Set<String>,
    val relation: ImpairedNarrativeRelation,
    val transitionNecessity: NarrativeTransitionNecessity,
    val detectabilitySignal: ImpairedNarrativeDetectabilitySignal,
) {
    init {
        require(currentAbilityState != AbilityState.FUNCTIONING) {
            "Impaired narrative features require a malfunctioning current ability."
        }
        require(priorImpairedObservationIds.none(String::isBlank)) {
            "Prior impaired observation IDs cannot be blank."
        }
        require(contradictoryPriorObservationIds.all { it in priorImpairedObservationIds }) {
            "Contradictory impaired observations must belong to the current impairment narrative."
        }
        require(
            (relation == ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE) ==
                contradictoryPriorObservationIds.isNotEmpty(),
        ) {
            "Only a narrative break may carry contradictory prior impaired observations."
        }
        require(
            relation != ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE ||
                transitionNecessity != NarrativeTransitionNecessity.NONE,
        ) {
            "A narrative break must be classified as avoidable or forced."
        }
        require(
            relation == ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE ||
                transitionNecessity == NarrativeTransitionNecessity.NONE,
        ) {
            "A non-breaking candidate cannot require a narrative transition."
        }
    }
}

internal data class ImpairedNarrativeCandidateEvidence(
    val candidateId: String,
    val abilityState: AbilityState,
    val impairmentLifetime: ImpairmentLifetime?,
    val priorImpairedObservationIds: Set<String>,
    /**
     * Confirmation/contradiction evidence in the recipient's source-scoped perceived-functioning
     * narrative. This must not be the ordinary mechanically-credible confirmation projection:
     * malfunction may keep a mechanically legal world alive while still breaking the story the
     * impaired player believes about their own functioning ability.
     */
    val perceivedConfirmation: FeatureProjection<ConfirmationChainFeatures>,
) {
    init {
        require(candidateId.isNotBlank()) {
            "Impaired narrative evidence requires a stable candidate ID."
        }
        require(priorImpairedObservationIds.none(String::isBlank)) {
            "Prior impaired observation IDs cannot be blank."
        }
        when (abilityState) {
            AbilityState.FUNCTIONING -> {
                require(impairmentLifetime == null) {
                    "A functioning ability cannot own an impairment lifetime."
                }
                require(priorImpairedObservationIds.isEmpty()) {
                    "A functioning ability cannot own an impaired narrative prefix."
                }
            }

            AbilityState.MALFUNCTIONING_DRUNK,
            AbilityState.MALFUNCTIONING_POISONED,
            -> requireNotNull(impairmentLifetime) {
                "A malfunctioning ability requires an impairment lifetime."
            }
        }
    }
}

/**
 * Pure projection from authoritative impairment evidence plus source-scoped perceived-functioning
 * confirmation relationships. Mechanical legality remains separate; exact world replay remains
 * owned by the confirmation/exact layers.
 */
internal object ImpairedNarrativeFeaturesProjector {
    fun project(
        candidates: List<ImpairedNarrativeCandidateEvidence>,
    ): Map<String, FeatureProjection<ImpairedNarrativeFeatures>> {
        require(candidates.map(ImpairedNarrativeCandidateEvidence::candidateId).distinct().size == candidates.size) {
            "Impaired narrative candidates must have unique IDs."
        }

        val provisional = candidates.associate { evidence ->
            evidence.candidateId to provisional(evidence)
        }

        return candidates.associate { evidence ->
            val current = provisional.getValue(evidence.candidateId)
            val completed = if (
                current is FeatureProjection.Projected &&
                current.value.relation == ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE
            ) {
                val hasCompatibleAlternative = candidates.any { alternative ->
                    alternative.candidateId != evidence.candidateId &&
                        alternative.narrativeContextKey() == evidence.narrativeContextKey() &&
                        (provisional[alternative.candidateId] as? FeatureProjection.Projected)
                            ?.value
                            ?.relation
                            ?.let { relation ->
                                relation == ImpairedNarrativeRelation.COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE ||
                                    relation == ImpairedNarrativeRelation.NO_PRIOR_IMPAIRED_NARRATIVE
                            } == true
                }
                val necessity = if (hasCompatibleAlternative) {
                    NarrativeTransitionNecessity.AVOIDABLE
                } else {
                    NarrativeTransitionNecessity.FORCED
                }
                FeatureProjection.Projected(
                    current.value.copy(
                        transitionNecessity = necessity,
                        detectabilitySignal = when (necessity) {
                            NarrativeTransitionNecessity.AVOIDABLE ->
                                ImpairedNarrativeDetectabilitySignal.AVOIDABLE_NARRATIVE_BREAK
                            NarrativeTransitionNecessity.FORCED ->
                                ImpairedNarrativeDetectabilitySignal.FORCED_NARRATIVE_BREAK
                            NarrativeTransitionNecessity.NONE ->
                                error("A narrative break cannot have NONE transition necessity.")
                        },
                    ),
                )
            } else {
                current
            }
            evidence.candidateId to completed
        }
    }

    private fun provisional(
        evidence: ImpairedNarrativeCandidateEvidence,
    ): FeatureProjection<ImpairedNarrativeFeatures> {
        if (evidence.abilityState == AbilityState.FUNCTIONING) {
            return FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE)
        }
        val lifetime = requireNotNull(evidence.impairmentLifetime)
        if (evidence.priorImpairedObservationIds.isEmpty()) {
            return projected(
                evidence = evidence,
                lifetime = lifetime,
                contradictoryIds = emptySet(),
                relation = ImpairedNarrativeRelation.NO_PRIOR_IMPAIRED_NARRATIVE,
                transition = NarrativeTransitionNecessity.NONE,
                detectability = ImpairedNarrativeDetectabilitySignal.NONE,
            )
        }
        val confirmation = when (val value = evidence.perceivedConfirmation) {
            is FeatureProjection.Projected -> value.value
            is FeatureProjection.Unavailable -> return value
        }

        val impactById = confirmation.historicalObservationImpacts.associateBy {
            it.provenance.observationRef.recordId
        }
        require(evidence.priorImpairedObservationIds.all { it in impactById }) {
            "Every impaired narrative observation must be present in the exact confirmation history."
        }

        if (
            evidence.priorImpairedObservationIds.any { recordId ->
                impactById.getValue(recordId).relation ==
                    ConfirmationObservationRelation.BASELINE_ALREADY_COLLAPSED
            }
        ) {
            return projected(
                evidence = evidence,
                lifetime = lifetime,
                contradictoryIds = emptySet(),
                relation = ImpairedNarrativeRelation.PRIOR_NARRATIVE_ALREADY_INFEASIBLE,
                transition = NarrativeTransitionNecessity.NONE,
                detectability =
                    ImpairedNarrativeDetectabilitySignal.PRIOR_NARRATIVE_ALREADY_INFEASIBLE,
            )
        }

        val contradictoryIds =
            confirmation.contradictedObservationIds intersect evidence.priorImpairedObservationIds
        return if (contradictoryIds.isEmpty()) {
            projected(
                evidence = evidence,
                lifetime = lifetime,
                contradictoryIds = emptySet(),
                relation = ImpairedNarrativeRelation.COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE,
                transition = NarrativeTransitionNecessity.NONE,
                detectability = ImpairedNarrativeDetectabilitySignal.NONE,
            )
        } else {
            projected(
                evidence = evidence,
                lifetime = lifetime,
                contradictoryIds = contradictoryIds,
                relation = ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE,
                transition = NarrativeTransitionNecessity.FORCED,
                detectability = ImpairedNarrativeDetectabilitySignal.FORCED_NARRATIVE_BREAK,
            )
        }
    }

    private fun projected(
        evidence: ImpairedNarrativeCandidateEvidence,
        lifetime: ImpairmentLifetime,
        contradictoryIds: Set<String>,
        relation: ImpairedNarrativeRelation,
        transition: NarrativeTransitionNecessity,
        detectability: ImpairedNarrativeDetectabilitySignal,
    ): FeatureProjection.Projected<ImpairedNarrativeFeatures> =
        FeatureProjection.Projected(
            ImpairedNarrativeFeatures(
                currentAbilityState = evidence.abilityState,
                impairmentLifetime = lifetime,
                priorImpairedObservationIds = evidence.priorImpairedObservationIds,
                contradictoryPriorObservationIds = contradictoryIds,
                relation = relation,
                transitionNecessity = transition,
                detectabilitySignal = detectability,
            ),
        )

    private data class NarrativeContextKey(
        val abilityState: AbilityState,
        val lifetime: ImpairmentLifetime?,
        val priorObservationIds: Set<String>,
    )

    private fun ImpairedNarrativeCandidateEvidence.narrativeContextKey() =
        NarrativeContextKey(
            abilityState = abilityState,
            lifetime = impairmentLifetime,
            priorObservationIds = priorImpairedObservationIds,
        )
}
