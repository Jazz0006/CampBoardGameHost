package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.session.InformationDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot

/**
 * Non-authoritative shadow output for one already-validated structured information decision.
 *
 * The existing information snapshot remains the legality/recommendation/confirmation authority.
 * SDE diagnostics, planned references, and shadow policy selection are attached beside it only;
 * they cannot replace the visible recommendation, confirm, commit, or mutate a session.
 */
internal data class StructuredInformationShadowEvaluation(
    val informationSnapshot: InformationDecisionSnapshot,
    val sdeCandidates: List<SdeDecisionCandidate>,
    val plannedDecisions: List<PlannedDecisionRef>,
    val consequences: ExactConsequenceEvaluation,
    val featureEvaluation: DecisionFeatureEvaluation,
    val policyEvaluation: BeginnerConservativePolicyEvaluation,
    val policySelection: PolicySelection?,
) {
    init {
        require(sdeCandidates.map(SdeDecisionCandidate::candidateId) == informationSnapshot.legalCandidateIds) {
            "Structured SDE candidates must preserve the source legal-candidate order."
        }
        require(plannedDecisions.map(PlannedDecisionRef::candidateId) == informationSnapshot.legalCandidateIds) {
            "Structured information shadow plans must preserve the source legal-candidate order."
        }
        require(plannedDecisions.all { it.isCurrentFor(informationSnapshot) }) {
            "Structured information shadow plans must be bound to the source information snapshot."
        }
        require(sdeCandidates.all { it.sourceRevision == informationSnapshot.revision }) {
            "Structured SDE candidates must preserve the source information revision."
        }
        require(featureEvaluation.candidateIds == informationSnapshot.legalCandidateIds) {
            "Structured feature projection must preserve the source legal-candidate order."
        }
        require(policyEvaluation.candidateIds == informationSnapshot.legalCandidateIds) {
            "Structured policy evaluation must preserve the source legal-candidate order."
        }
        require(policyEvaluation.policyVersion == PolicyVersions.BEGINNER_CONSERVATIVE_V1) {
            "Structured policy evaluation must use BEGINNER_CONSERVATIVE_V1."
        }
        when (policyEvaluation) {
            is BeginnerConservativePolicyEvaluation.Ready -> {
                val selection = requireNotNull(policySelection) {
                    "Ready structured policy evaluation requires a shadow policy selection."
                }
                require(selection.policyVersion == policyEvaluation.policyVersion) {
                    "Structured shadow selection must use the evaluated policy version."
                }
                val selectedEvaluation = policyEvaluation.evaluations.singleOrNull {
                    it.candidateId == selection.candidateId
                }
                require(selectedEvaluation?.disposition == PolicyDisposition.SURVIVOR) {
                    "Structured shadow selection must select a policy survivor."
                }
            }

            is BeginnerConservativePolicyEvaluation.Deferred ->
                require(policySelection == null) {
                    "Deferred structured policy evaluation cannot expose a shadow policy selection."
                }
        }
        when (consequences) {
            is ExactConsequenceEvaluation.Ready ->
                require(featureEvaluation is DecisionFeatureEvaluation.Ready) {
                    "Ready exact consequences require ready DecisionFeatures."
                }

            is ExactConsequenceEvaluation.Deferred -> {
                require(featureEvaluation is DecisionFeatureEvaluation.Deferred) {
                    "Deferred exact consequences require deferred DecisionFeatures."
                }
                require(featureEvaluation.missingCapabilities == consequences.missingCapabilities) {
                    "Deferred feature evaluation must preserve exact missing capabilities."
                }
            }
        }
    }
}

/**
 * Pure adapter from the established structured-information authority into SDE exact consequence
 * evaluation. It deliberately accepts an already-built [InformationDecisionContext], so candidate
 * legality and semantic draft materialization remain owned by the existing Foundation pipeline.
 */
internal object StructuredInformationShadowAdapter {
    fun <T : DynamicInformationOutcome> evaluate(
        decisionContext: InformationDecisionContext<T>,
        exactContext: ExactConsequenceContext,
        inputBindings: SdeDecisionInputBindings = SdeDecisionInputBindings.NotCaptured,
    ): StructuredInformationShadowEvaluation {
        val informationSnapshot = decisionContext.snapshot
        val exactRevision = InformationDecisionRevision(
            gameStateRevision = exactContext.gameStateRevision,
            playerInputRevision = exactContext.playerInputRevision,
        )
        require(informationSnapshot.revision == exactRevision) {
            "Structured information shadow evaluation requires the same source revisions as the exact context."
        }

        val historical = exactContext.exactContext
        val formalSnapshotId = FormalGameState.from(
            snapshot = historical.initialSnapshot,
            phase = historical.initialPhase,
            round = historical.initialRound,
        ).snapshotId
        val decisionId = informationSnapshot.semanticIdentity
        val lifecycleStages = decisionContext.legalCandidates.map { candidate ->
            SdeDecisionLifecycleStage.Interaction(
                phase = candidate.draft.phase,
                round = candidate.draft.round,
                sequence = candidate.draft.sequence,
            )
        }.distinct()
        require(lifecycleStages.size == 1) {
            "One structured information decision must share one lifecycle point across every legal candidate."
        }
        val lifecycleStage = lifecycleStages.single()
        require(
            lifecycleStage.round > historical.initialRound ||
                (
                    lifecycleStage.round == historical.initialRound &&
                        lifecycleStage.phase.ordinal >= historical.initialPhase.ordinal
                    ),
        ) {
            "Structured information shadow candidates cannot precede the exact historical baseline."
        }
        val historyPrefixRef = exactContext.toHistoricalPrefixRef(lifecycleStage)
        val projectedCandidates = decisionContext.legalCandidates.map { candidate ->
            val draft = candidate.draft
            require(draft.visibility == ObservationVisibility.PRIVATE && draft.recipientSeats.size == 1) {
                "The first structured-information shadow slice requires one private recipient per candidate."
            }
            val exact = ExactConsequenceCandidate(
                candidateId = candidate.candidateId,
                recipientSeat = draft.recipientSeats.single(),
                observations = listOf(draft.toHypotheticalObservation(formalSnapshotId)),
            )
            val sde = SdeDecisionCandidate(
                decisionId = decisionId,
                candidateId = candidate.candidateId,
                lifecycleStage = lifecycleStage,
                sourceInteraction = SdeDecisionSourceInteraction(
                    interactionId = decisionId,
                    sourceSeat = draft.sourceSeat,
                    abilityRole = draft.sourceAbility,
                    abilityState = candidate.evaluation.candidate.abilityState,
                ),
                sourceRevision = informationSnapshot.revision,
                inputBindings = inputBindings,
                historyPrefixRef = historyPrefixRef,
                legalOutcomeIdentity = candidate.candidateId,
                hypotheticalRef = SdeDecisionHypotheticalRef(
                    observationRecordIds = listOf(draft.recordId),
                ),
                legalityProvenance = SdeDecisionLegalityProvenance(
                    ownerId = "information-decision-context-v1",
                    candidateSpaceIdentity = informationSnapshot.semanticIdentity,
                    candidateSchemaVersion = candidate.evaluation.candidate.metadata.candidateSchemaVersion,
                ),
            )
            exact to sde
        }
        val exactCandidates = projectedCandidates.map { it.first }
        val sdeCandidates = projectedCandidates.map { it.second }
        val semanticTruthByCandidateId = decisionContext.legalCandidates.mapNotNull { candidate ->
            candidate.evaluation.candidate.truthRelation.toSemanticTruthOrNull()?.let { semanticTruth ->
                candidate.candidateId to semanticTruth
            }
        }.toMap()
        val truthRelationByCandidateId = decisionContext.legalCandidates.associate { candidate ->
            candidate.candidateId to candidate.evaluation.candidate.truthRelation
        }
        val planned = informationSnapshot.legalCandidateIds.map { candidateId ->
            PlannedDecisionRef.fromInformationSnapshot(
                decisionId = decisionId,
                candidateId = candidateId,
                snapshot = informationSnapshot,
            )
        }
        val consequences = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest(
                decisionId = decisionId,
                candidates = exactCandidates,
            ),
            context = exactContext,
        )
        val baseFeatureEvaluation = ExactConsequenceDecisionFeaturesProjector.project(
            evaluation = consequences,
            legalCandidateIds = informationSnapshot.legalCandidateIds,
            playerCount = historical.initialSnapshot.gameState.players.size,
            semanticTruthByCandidateId = semanticTruthByCandidateId,
        )
        val featureEvaluation =
            if (
                consequences is ExactConsequenceEvaluation.Ready &&
                baseFeatureEvaluation is DecisionFeatureEvaluation.Ready
            ) {
                val confirmationByCandidateId =
                    HistoricalConfirmationChainFeatureProjector.project(
                        fullEvaluation = consequences,
                        exactCandidates = exactCandidates,
                        sdeCandidates = sdeCandidates,
                        context = exactContext,
                    )
                val impairedNarrativeByCandidateId =
                    HistoricalImpairedNarrativeFeatureProjector.project(
                        confirmationByCandidateId = confirmationByCandidateId,
                        exactCandidates = exactCandidates,
                        sdeCandidates = sdeCandidates,
                        context = exactContext,
                    )
                val healthyInformationByCandidateId =
                    HistoricalHealthyInformationUtilityFeatureProjector.project(
                        fullEvaluation = consequences,
                        confirmationByCandidateId = confirmationByCandidateId,
                        exactCandidates = exactCandidates,
                        sdeCandidates = sdeCandidates,
                        truthRelationByCandidateId = truthRelationByCandidateId,
                        context = exactContext,
                    )
                DecisionFeatureEvaluation.Ready(
                    candidates = baseFeatureEvaluation.candidates.map { candidate ->
                        candidate.copy(
                            features = candidate.features.copy(
                                confirmationChainImpact =
                                    confirmationByCandidateId.getValue(candidate.candidateId),
                                healthyInformationUtility =
                                    healthyInformationByCandidateId.getValue(candidate.candidateId),
                                impairedNarrative =
                                    impairedNarrativeByCandidateId.getValue(candidate.candidateId),
                            ),
                        )
                    },
                )
            } else {
                baseFeatureEvaluation
            }
        val policyEvaluation = BeginnerConservativeV1Policy.evaluate(featureEvaluation)
        val policySelection = BeginnerConservativeV1Selector.select(
            evaluation = policyEvaluation,
            decisionId = decisionId,
            selectionSeed = historical.initialSnapshot.gameSeed,
        )
        return StructuredInformationShadowEvaluation(
            informationSnapshot = informationSnapshot,
            sdeCandidates = sdeCandidates,
            plannedDecisions = planned,
            consequences = consequences,
            featureEvaluation = featureEvaluation,
            policyEvaluation = policyEvaluation,
            policySelection = policySelection,
        )
    }

    private fun ExactConsequenceContext.toHistoricalPrefixRef(
        decisionPoint: SdeDecisionLifecycleStage.Interaction,
    ): SdeHistoricalPrefixRef {
        val historical = exactContext
        val observationRefs = historical.observationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: return SdeHistoricalPrefixRef.NotCaptured
            require(binding.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical SDE evaluation requires a committed prefix; observation ${record.recordId} is not before the decision point."
            }
            SdeHistoricalObservationRef(
                recordId = record.recordId,
                globalSequence = binding.point.globalSequence,
            )
        }
        val actionRefs = historical.actionTimeline.entries.map { entry ->
            require(entry.point.isStrictlyBeforeSdeDecision(decisionPoint)) {
                "Historical SDE evaluation requires a committed prefix; action ${entry.fact.actionId} is not before the decision point."
            }
            SdeHistoricalActionRef(
                actionId = entry.fact.actionId,
                globalSequence = entry.point.globalSequence,
            )
        }
        return SdeHistoricalPrefixRef.Global(
            gameId = historical.initialSnapshot.gameId,
            actionRefs = actionRefs,
            observationRefs = observationRefs,
        )
    }

    private fun TruthRelation.toSemanticTruthOrNull(): SemanticTruth? = when (this) {
        TruthRelation.TRUE_TO_ACTUAL_STATE -> SemanticTruth.TRUE
        TruthRelation.FALSE_TO_ACTUAL_STATE -> SemanticTruth.FALSE
        else -> null
    }

    private fun EpistemicObservationDraft.toHypotheticalObservation(
        formalSnapshotId: String,
    ): EpistemicObservation = EpistemicObservation(
        observationId = recordId,
        snapshotId = formalSnapshotId,
        phase = phase,
        round = round,
        sequence = sequence,
        sourceSeat = sourceSeat,
        sourceAbility = sourceAbility,
        visibility = visibility,
        recipientSeats = recipientSeats,
        reliability = reliability,
        proposition = proposition,
        schemaVersion = schemaVersion,
    )
}
