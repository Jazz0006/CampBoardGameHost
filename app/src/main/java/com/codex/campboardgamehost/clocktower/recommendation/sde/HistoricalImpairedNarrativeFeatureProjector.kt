package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

/**
 * Derives impairment-narrative inputs from canonical setup/action/observation history.
 *
 * The current candidate's [AbilityState] remains authoritative for the current interaction. Prior
 * observation impairment is reconstructed from role identity plus the globally ordered poison
 * action episode at the observation point. This projector owns no durable state.
 */
internal object HistoricalImpairedNarrativeFeatureProjector {
    fun project(
        exactCandidates: List<ExactConsequenceCandidate>,
        sdeCandidates: List<SdeDecisionCandidate>,
        context: ExactConsequenceContext,
    ): Map<String, FeatureProjection<ImpairedNarrativeFeatures>> {
        require(exactCandidates.isNotEmpty()) {
            "Historical impaired-narrative projection requires legal candidates."
        }
        val candidateIds = exactCandidates.map(ExactConsequenceCandidate::candidateId)
        require(candidateIds.distinct().size == candidateIds.size) {
            "Historical impaired-narrative candidate IDs must be unique."
        }
        require(sdeCandidates.map(SdeDecisionCandidate::candidateId) == candidateIds) {
            "SDE impaired-narrative candidates must preserve exact candidate order."
        }
        require(sdeCandidates.all { it.sourceRevision == context.sourceRevision }) {
            "Historical impaired-narrative candidates must share the exact source revision."
        }
        exactCandidates.zip(sdeCandidates).forEach { (exact, sde) ->
            require(
                exact.observations.map(EpistemicObservation::observationId) ==
                    sde.hypotheticalRef.observationRecordIds,
            ) {
                "SDE impaired-narrative hypothetical refs must match exact candidate semantics."
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
            "One impaired-narrative decision must share one lifecycle point across all candidates."
        }
        val decisionPoint = decisionPoints.single()
        val historical = context.exactContext

        val observationRefs = historical.observationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)
            require(binding.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical impaired-narrative evaluation requires a committed prefix; " +
                    "observation ${record.recordId} is not before the decision point."
            }
            SdeHistoricalObservationRef(record.recordId, binding.point.globalSequence)
        }
        val actionRefs = historical.actionTimeline.entries.map { entry ->
            require(entry.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical impaired-narrative evaluation requires a committed prefix; " +
                    "action ${entry.fact.actionId} is not before the decision point."
            }
            SdeHistoricalActionRef(entry.fact.actionId, entry.point.globalSequence)
        }
        sdeCandidates.forEach { candidate ->
            val prefix = candidate.historyPrefixRef as SdeHistoricalPrefixRef.Global
            require(prefix.gameId == historical.initialSnapshot.gameId) {
                "Historical impaired-narrative prefix must reference the exact game."
            }
            require(prefix.actionRefs == actionRefs && prefix.observationRefs == observationRefs) {
                "Historical impaired-narrative projection requires the canonical committed prefix."
            }
        }

        val evidenceByCandidateId = linkedMapOf<String, ImpairedNarrativeCandidateEvidence>()
        val pendingPerceivedNarrative = mutableListOf<PendingPerceivedNarrativeEvidence>()

        exactCandidates.zip(sdeCandidates).forEach { (exact, sde) ->
            val currentState = sde.sourceInteraction.abilityState
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)

            if (currentState == AbilityState.FUNCTIONING) {
                evidenceByCandidateId[sde.candidateId] = ImpairedNarrativeCandidateEvidence(
                    candidateId = sde.candidateId,
                    abilityState = currentState,
                    impairmentLifetime = null,
                    priorImpairedObservationIds = emptySet(),
                    perceivedConfirmation = FeatureProjection.Unavailable(
                        FeatureUnavailableReason.NOT_APPLICABLE,
                    ),
                )
                return@forEach
            }

            val lifetime = when (currentState) {
                AbilityState.MALFUNCTIONING_DRUNK -> ImpairmentLifetime.PERSISTENT_SETUP_BOUND
                AbilityState.MALFUNCTIONING_POISONED -> ImpairmentLifetime.TEMPORARY_ACTION_BOUND
                AbilityState.FUNCTIONING -> error("Handled above.")
            }
            val sourceSeat = sde.sourceInteraction.sourceSeat
            val sourceAbility = sde.sourceInteraction.abilityRole
            if (sourceSeat == null || sourceAbility == null) {
                evidenceByCandidateId[sde.candidateId] = unavailableEvidence(
                    candidateId = sde.candidateId,
                    abilityState = currentState,
                    lifetime = lifetime,
                )
                return@forEach
            }

            val episodeStart = when (lifetime) {
                ImpairmentLifetime.PERSISTENT_SETUP_BOUND -> null
                ImpairmentLifetime.TEMPORARY_ACTION_BOUND -> {
                    val latestPoisonEntry = historical.actionTimeline.entries
                        .asReversed()
                        .firstOrNull { it.fact is ActionFact.Poison }
                    val latestPoison = latestPoisonEntry?.fact as? ActionFact.Poison
                    if (latestPoison?.targetSeat != sourceSeat) {
                        evidenceByCandidateId[sde.candidateId] = unavailableEvidence(
                            candidateId = sde.candidateId,
                            abilityState = currentState,
                            lifetime = lifetime,
                        )
                        return@forEach
                    }
                    latestPoisonEntry.point.globalSequence
                }
            }

            val priorIds = historical.observationLog.records
                .asSequence()
                .filter { record -> record.isVisibleTo(exact.recipientSeat) }
                .filter { record ->
                    record.sourceSeat == sourceSeat && record.sourceAbility == sourceAbility
                }
                .filter { record ->
                    val globalSequence =
                        (record.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence
                    episodeStart == null || globalSequence > episodeStart
                }
                .filter { record ->
                    HistoricalInformationAbilityStateResolver.resolve(
                        record = record,
                        context = context,
                    ) == currentState
                }
                .mapTo(linkedSetOf(), RecordedEpistemicObservation::recordId)

            if (priorIds.isEmpty()) {
                evidenceByCandidateId[sde.candidateId] = ImpairedNarrativeCandidateEvidence(
                    candidateId = sde.candidateId,
                    abilityState = currentState,
                    impairmentLifetime = lifetime,
                    priorImpairedObservationIds = emptySet(),
                    perceivedConfirmation = FeatureProjection.Unavailable(
                        FeatureUnavailableReason.NOT_APPLICABLE,
                    ),
                )
                return@forEach
            }

            pendingPerceivedNarrative += PendingPerceivedNarrativeEvidence(
                exactCandidate = exact,
                sdeCandidate = sde,
                abilityState = currentState,
                impairmentLifetime = lifetime,
                sourceSeat = sourceSeat,
                sourceAbility = sourceAbility,
                recipientSeat = exact.recipientSeat,
                priorImpairedObservationIds = priorIds,
            )
        }

        pendingPerceivedNarrative
            .groupBy(PendingPerceivedNarrativeEvidence::contextKey)
            .values
            .forEach { group ->
                val key = group.first().contextKey
                val perceivedObservationLog = historical.observationLog.copy(
                    records = historical.observationLog.records.map { record ->
                        if (record.recordId in key.priorImpairedObservationIds) {
                            record.asPerceivedFunctioning(
                                sourceSeat = key.sourceSeat,
                                sourceAbility = key.sourceAbility,
                            )
                        } else {
                            record
                        }
                    },
                )
                val perceivedContext = context.copy(
                    exactContext = historical.copy(observationLog = perceivedObservationLog),
                )
                val perceivedExactCandidates = group.map { pending ->
                    pending.exactCandidate.copy(
                        observations = pending.exactCandidate.observations.map { observation ->
                            observation.asPerceivedFunctioning(
                                sourceSeat = key.sourceSeat,
                                sourceAbility = key.sourceAbility,
                            )
                        },
                    )
                }
                val perceivedSdeCandidates = group.map(PendingPerceivedNarrativeEvidence::sdeCandidate)
                val decisionIds = perceivedSdeCandidates.map(SdeDecisionCandidate::decisionId).distinct()
                require(decisionIds.size == 1) {
                    "One perceived impaired narrative group must belong to one decision."
                }
                val perceivedConfirmationByCandidateId:
                    Map<String, FeatureProjection<ConfirmationChainFeatures>> = when (
                    val evaluation = StorytellerDecisionEngine.evaluateExactConsequences(
                        request = ExactConsequenceRequest(
                            decisionId = decisionIds.single() + ":perceived-functioning-narrative",
                            candidates = perceivedExactCandidates,
                        ),
                        context = perceivedContext,
                    )
                ) {
                    is ExactConsequenceEvaluation.Deferred ->
                        perceivedExactCandidates.associate { candidate ->
                            candidate.candidateId to FeatureProjection.Unavailable(
                                FeatureUnavailableReason.MISSING_CAPABILITY,
                            )
                        }

                    is ExactConsequenceEvaluation.Ready ->
                        HistoricalConfirmationChainFeatureProjector.project(
                            fullEvaluation = evaluation,
                            exactCandidates = perceivedExactCandidates,
                            sdeCandidates = perceivedSdeCandidates,
                            context = perceivedContext,
                        )
                }

                group.forEach { pending ->
                    evidenceByCandidateId[pending.sdeCandidate.candidateId] =
                        ImpairedNarrativeCandidateEvidence(
                            candidateId = pending.sdeCandidate.candidateId,
                            abilityState = pending.abilityState,
                            impairmentLifetime = pending.impairmentLifetime,
                            priorImpairedObservationIds = pending.priorImpairedObservationIds,
                            perceivedConfirmation = perceivedConfirmationByCandidateId.getValue(
                                pending.sdeCandidate.candidateId,
                            ),
                        )
                }
            }

        return ImpairedNarrativeFeaturesProjector.project(
            candidateIds.map(evidenceByCandidateId::getValue),
        )
    }

    private data class PerceivedNarrativeContextKey(
        val sourceSeat: Int,
        val sourceAbility: RoleId,
        val recipientSeat: Int,
        val priorImpairedObservationIds: Set<String>,
    )

    private data class PendingPerceivedNarrativeEvidence(
        val exactCandidate: ExactConsequenceCandidate,
        val sdeCandidate: SdeDecisionCandidate,
        val abilityState: AbilityState,
        val impairmentLifetime: ImpairmentLifetime,
        val sourceSeat: Int,
        val sourceAbility: RoleId,
        val recipientSeat: Int,
        val priorImpairedObservationIds: Set<String>,
    ) {
        val contextKey: PerceivedNarrativeContextKey = PerceivedNarrativeContextKey(
            sourceSeat = sourceSeat,
            sourceAbility = sourceAbility,
            recipientSeat = recipientSeat,
            priorImpairedObservationIds = priorImpairedObservationIds,
        )
    }

    private fun EpistemicObservation.asPerceivedFunctioning(
        sourceSeat: Int,
        sourceAbility: RoleId,
    ): EpistemicObservation {
        require(this.sourceSeat == sourceSeat && this.sourceAbility == sourceAbility) {
            "Perceived narrative candidate observations must belong to the impaired source."
        }
        // Ephemeral derived replay only: bypass the malfunction short-circuit so the proposition is
        // evaluated as the player believes their functioning ability would produce it. Canonical
        // impairment state and durable observation reliability are not changed.
        return copy(reliability = ObservationReliability.NOT_ABILITY_INFORMATION)
    }

    private fun RecordedEpistemicObservation.asPerceivedFunctioning(
        sourceSeat: Int,
        sourceAbility: RoleId,
    ): RecordedEpistemicObservation {
        require(this.sourceSeat == sourceSeat && this.sourceAbility == sourceAbility) {
            "Perceived narrative history must belong to the impaired source."
        }
        // Keep record identity/timeline/proposition intact; only the derived replay interpretation
        // changes. The canonical observation log remains the sole durable history owner.
        return copy(reliability = ObservationReliability.NOT_ABILITY_INFORMATION)
    }


    private fun unavailableEvidence(
        candidateId: String,
        abilityState: AbilityState,
        lifetime: ImpairmentLifetime,
    ) = ImpairedNarrativeCandidateEvidence(
        candidateId = candidateId,
        abilityState = abilityState,
        impairmentLifetime = lifetime,
        priorImpairedObservationIds = emptySet(),
        unavailableReason = FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED,
        perceivedConfirmation = FeatureProjection.Unavailable(
            FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED,
        ),
    )

    private fun RecordedEpistemicObservation.isVisibleTo(recipientSeat: Int): Boolean =
        visibility == ObservationVisibility.PUBLIC || recipientSeat in recipientSeats

    private fun unavailable(
        candidateIds: List<String>,
        reason: FeatureUnavailableReason,
    ): Map<String, FeatureProjection<ImpairedNarrativeFeatures>> =
        candidateIds.associateWith { FeatureProjection.Unavailable(reason) }
}
