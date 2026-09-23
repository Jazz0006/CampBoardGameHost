package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

/**
 * Derives impairment-narrative inputs from canonical setup/action/observation history.
 *
 * The current candidate's [AbilityState] remains authoritative for the current interaction. Prior
 * observation impairment is reconstructed from role identity plus the globally ordered poison
 * action episode at the observation point. This projector owns no durable state.
 */
internal object HistoricalImpairedNarrativeFeatureProjector {
    fun project(
        confirmationByCandidateId: Map<String, FeatureProjection<ConfirmationChainFeatures>>,
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
        require(confirmationByCandidateId.keys == candidateIds.toSet()) {
            "Impaired-narrative projection requires confirmation evidence for every legal candidate."
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

        val evidence = exactCandidates.zip(sdeCandidates).map { (exact, sde) ->
            val currentState = sde.sourceInteraction.abilityState
                ?: return unavailable(candidateIds, FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED)

            if (currentState == AbilityState.FUNCTIONING) {
                return@map ImpairedNarrativeCandidateEvidence(
                    candidateId = sde.candidateId,
                    abilityState = currentState,
                    impairmentLifetime = null,
                    priorImpairedObservationIds = emptySet(),
                    confirmation = confirmationByCandidateId.getValue(sde.candidateId),
                )
            }

            val lifetime = when (currentState) {
                AbilityState.MALFUNCTIONING_DRUNK -> ImpairmentLifetime.PERSISTENT_SETUP_BOUND
                AbilityState.MALFUNCTIONING_POISONED -> ImpairmentLifetime.TEMPORARY_ACTION_BOUND
                AbilityState.FUNCTIONING -> error("Handled above.")
            }
            val sourceSeat = sde.sourceInteraction.sourceSeat
            val sourceAbility = sde.sourceInteraction.abilityRole
            if (sourceSeat == null || sourceAbility == null) {
                return@map unavailableEvidence(
                    candidateId = sde.candidateId,
                    abilityState = currentState,
                    lifetime = lifetime,
                )
            }

            val episodeStart = when (lifetime) {
                ImpairmentLifetime.PERSISTENT_SETUP_BOUND -> null
                ImpairmentLifetime.TEMPORARY_ACTION_BOUND -> {
                    val latestPoisonEntry = historical.actionTimeline.entries
                        .asReversed()
                        .firstOrNull { it.fact is ActionFact.Poison }
                    val latestPoison = latestPoisonEntry?.fact as? ActionFact.Poison
                    if (latestPoison?.targetSeat != sourceSeat) {
                        return@map unavailableEvidence(
                            candidateId = sde.candidateId,
                            abilityState = currentState,
                            lifetime = lifetime,
                        )
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
                    abilityStateAtObservation(
                        record = record,
                        context = context,
                    ) == currentState
                }
                .mapTo(linkedSetOf(), RecordedEpistemicObservation::recordId)

            ImpairedNarrativeCandidateEvidence(
                candidateId = sde.candidateId,
                abilityState = currentState,
                impairmentLifetime = lifetime,
                priorImpairedObservationIds = priorIds,
                confirmation = confirmationByCandidateId.getValue(sde.candidateId),
            )
        }

        return ImpairedNarrativeFeaturesProjector.project(evidence)
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
        confirmation = FeatureProjection.Unavailable(
            FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED,
        ),
    )

    private fun abilityStateAtObservation(
        record: RecordedEpistemicObservation,
        context: ExactConsequenceContext,
    ): AbilityState? {
        val sourceSeat = record.sourceSeat ?: return null
        val sourceAbility = record.sourceAbility ?: return null
        val binding = record.timelineBinding as? ObservationTimelineBinding.Global ?: return null
        val historical = context.exactContext
        val actionsBeforeObservation = historical.actionTimeline.entries
            .filter { it.point.globalSequence < binding.point.globalSequence }

        val reduced = DynamicActionReducer.reduce(
            initialSnapshot = historical.initialSnapshot,
            initialPhase = historical.initialPhase,
            initialRound = historical.initialRound,
            facts = actionsBeforeObservation.map { it.fact },
        )
        val player = reduced.snapshot.gameState.playerAt(sourceSeat) ?: return null

        val activePoisonTarget = actionsBeforeObservation
            .asReversed()
            .firstOrNull { it.fact is ActionFact.Poison }
            ?.fact
            ?.let { it as ActionFact.Poison }
            ?.targetSeat

        val state = AbilityFunctioningSemantics.stateForEstablishedInteraction(
            subject = AbilitySubject(
                actualRole = player.actualRole.value,
                shownRole = player.shownRole?.value,
                isPoisoned = activePoisonTarget == sourceSeat,
                isAlive = player.alive,
            ),
            role = sourceAbility.value,
        ) ?: return null

        return state.toSdeAbilityState()
    }

    private fun AbilityFunctioningState.toSdeAbilityState(): AbilityState = when (this) {
        AbilityFunctioningState.FUNCTIONING -> AbilityState.FUNCTIONING
        AbilityFunctioningState.DRUNK -> AbilityState.MALFUNCTIONING_DRUNK
        AbilityFunctioningState.POISONED -> AbilityState.MALFUNCTIONING_POISONED
    }

    private fun RecordedEpistemicObservation.isVisibleTo(recipientSeat: Int): Boolean =
        visibility == ObservationVisibility.PUBLIC || recipientSeat in recipientSeats

    private fun unavailable(
        candidateIds: List<String>,
        reason: FeatureUnavailableReason,
    ): Map<String, FeatureProjection<ImpairedNarrativeFeatures>> =
        candidateIds.associateWith { FeatureProjection.Unavailable(reason) }
}
