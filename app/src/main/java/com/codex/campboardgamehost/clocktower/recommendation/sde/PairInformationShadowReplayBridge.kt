package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext

internal data class PairInformationShadowReplayRequest(
    val decisionId: String,
    val context: TroubleBrewingFirstNightPairDecisionContext,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
    val lifecycleStage: SdeDecisionLifecycleStage.Interaction,
) {
    init {
        require(decisionId.isNotBlank()) { "Pair shadow/replay decision ID cannot be blank." }
        require(sourceSeat > 0) { "Pair shadow/replay source seat must be positive." }
        require(reliability == ReliabilityState.RELIABLE) {
            "C5-B pair shadow/replay is limited to functioning truthful pair information."
        }
        require(lifecycleStage.phase == StorytellerPhase.FIRST_NIGHT && lifecycleStage.round == 1) {
            "C5-B pair shadow/replay requires a first-night round-one interaction."
        }
        require(
            abilityRole in setOf(
                RoleId("Washerwoman"),
                RoleId("Librarian"),
                RoleId("Investigator"),
            ),
        ) {
            "C5-B pair shadow/replay supports only authoritative first-night pair abilities."
        }
    }
}

internal data class PairInformationShadowReplayEvaluation(
    val legalCandidates: List<PairInformationLegalCandidate>,
    val exactCandidates: List<ExactConsequenceCandidate>,
    val sdeCandidates: List<SdeDecisionCandidate>,
    val exactConsequences: ExactConsequenceEvaluation,
    val featureEvaluation: DecisionFeatureEvaluation,
    val policyEvaluation: BeginnerConservativePolicyEvaluation,
    val policySelection: PolicySelection?,
    val decisionTrace: DecisionTrace,
    val replayInput: MultiPolicyReplayInput,
) {
    init {
        val legalIds = legalCandidates.map(PairInformationLegalCandidate::candidateId)
        require(legalIds.isNotEmpty()) { "Pair shadow/replay requires legal candidates." }
        require(exactCandidates.map(ExactConsequenceCandidate::candidateId) == legalIds) {
            "Pair exact candidates must preserve the complete legal order."
        }
        require(sdeCandidates.map(SdeDecisionCandidate::candidateId) == legalIds) {
            "Pair SDE candidates must preserve the complete legal order."
        }
        require(featureEvaluation.candidateIds == legalIds) {
            "Pair feature evaluation must preserve the complete legal order."
        }
        require(policyEvaluation.candidateIds == legalIds) {
            "Pair policy evaluation must preserve the complete legal order."
        }
        require(decisionTrace.legalCandidateIds == legalIds) {
            "Pair DecisionTrace must preserve the complete legal order."
        }
        require(replayInput.legalCandidateIds == legalIds) {
            "Pair replay input must preserve the complete legal order."
        }
    }
}

/**
 * C5-B read-only bridge from snapshot-backed pair context to the existing SDE trace/replay seam.
 *
 * PairInformationLegalDomain remains the sole candidate authority. This bridge materializes
 * already-legal outcomes into exact epistemic consequences, projects descriptive features, runs
 * frozen V1 in shadow, and packages a canonical DecisionTrace/replay input. It never writes game
 * history or changes current pair recommendation/manual authority.
 */
internal object PairInformationShadowReplayBridge {
    private const val LEGALITY_OWNER = "pair-information-legal-domain-v1"

    fun evaluate(
        request: PairInformationShadowReplayRequest,
        exactContext: ExactConsequenceContext,
        inputBindings: SdeDecisionInputBindings = SdeDecisionInputBindings.NotCaptured,
    ): PairInformationShadowReplayEvaluation {
        validateFreshness(request, exactContext)

        val game = request.context.naturalPairGameState
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = request.context.roleDefinitions,
            sourceSeat = request.sourceSeat,
            abilityRole = request.abilityRole,
            reliability = request.reliability,
        )
        require(legalCandidates.isNotEmpty()) {
            "Pair shadow/replay requires a non-empty rules-owned legal domain."
        }
        require(legalCandidates.all { it.semanticTruth == SemanticTruth.TRUE }) {
            "C5-B functioning pair shadow/replay must remain truth-only."
        }

        val historyPrefix = canonicalPrefix(request, exactContext)
        val formalSnapshotId = FormalGameState.from(
            exactContext.exactContext.initialSnapshot,
            exactContext.exactContext.initialPhase,
            exactContext.exactContext.initialRound,
        ).snapshotId

        val observations = legalCandidates.map { candidate ->
            materializeObservation(request, candidate, formalSnapshotId)
        }
        val exactCandidates = legalCandidates.zip(observations).map { (candidate, observation) ->
            PairInformationExactConsequenceAdapter.fromLegalCandidate(
                candidate = candidate,
                observation = observation,
            )
        }
        val sdeCandidates = legalCandidates.zip(exactCandidates).map { (legal, exact) ->
            SdeDecisionCandidate(
                decisionId = request.decisionId,
                candidateId = legal.candidateId,
                lifecycleStage = request.lifecycleStage,
                sourceInteraction = SdeDecisionSourceInteraction(
                    interactionId = request.decisionId,
                    sourceSeat = request.sourceSeat,
                    abilityRole = request.abilityRole,
                    abilityState = AbilityState.FUNCTIONING,
                ),
                sourceRevision = exactContext.sourceRevision,
                inputBindings = inputBindings,
                historyPrefixRef = historyPrefix,
                legalOutcomeIdentity = legal.candidateId,
                hypotheticalRef = SdeDecisionHypotheticalRef(
                    observationRecordIds = exact.observations.map(EpistemicObservation::observationId),
                ),
                legalityProvenance = SdeDecisionLegalityProvenance(
                    ownerId = LEGALITY_OWNER,
                    candidateSpaceIdentity = request.decisionId,
                ),
            )
        }

        val exactConsequences = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest(
                decisionId = request.decisionId,
                candidates = exactCandidates,
            ),
            context = exactContext,
        )
        val baseFeatures = ExactConsequenceDecisionFeaturesProjector.project(
            evaluation = exactConsequences,
            legalCandidateIds = legalCandidates.map(PairInformationLegalCandidate::candidateId),
            playerCount = game.players.size,
            semanticTruthByCandidateId = legalCandidates.associate { candidate ->
                candidate.candidateId to candidate.semanticTruth
            },
        )
        val featureEvaluation = enrichPairFeatures(
            base = baseFeatures,
            game = game,
            request = request,
            legalCandidates = legalCandidates,
            exactCandidates = exactCandidates,
        )

        val policyEvaluation = BeginnerConservativeV1Policy.evaluate(featureEvaluation)
        val policySelection = BeginnerConservativeV1Selector.select(
            evaluation = policyEvaluation,
            decisionId = request.decisionId,
            selectionSeed = request.context.snapshot.gameSeed,
        )
        val definition = StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1
        val legalCandidateIds = legalCandidates.map(PairInformationLegalCandidate::candidateId)
        val decisionTrace = DecisionTrace(
            evidenceCheckpoint = definition.evidenceCheckpoint,
            decisionId = request.decisionId,
            lifecycleStage = request.lifecycleStage,
            sourceRevision = exactContext.sourceRevision,
            historyPrefixRef = historyPrefix,
            legalCandidateIds = legalCandidateIds,
            featureEvaluation = featureEvaluation,
            policySnapshot = policyEvaluation.toDecisionTracePolicySnapshot(),
            policySelection = policySelection,
            actualChoice = DecisionTraceActualChoice.Pending,
        )
        val replayInput = MultiPolicyReplayInput(
            decisionId = request.decisionId,
            lifecycleStage = request.lifecycleStage,
            sourceRevision = exactContext.sourceRevision,
            historyPrefixRef = historyPrefix,
            legalCandidateIds = legalCandidateIds,
            featureEvaluation = featureEvaluation,
            selectionSeed = request.context.snapshot.gameSeed,
            policyScope = DecisionPolicyReplayScope.PairInformation(
                script = game.script,
                phase = request.lifecycleStage.phase,
                round = request.lifecycleStage.round,
                targetType = targetTypeFor(request.abilityRole),
                reliability = request.reliability,
                truthfulLegalOutcomes = legalCandidates.all { candidate ->
                    candidate.semanticTruth == SemanticTruth.TRUE
                },
            ),
        )

        return PairInformationShadowReplayEvaluation(
            legalCandidates = legalCandidates,
            exactCandidates = exactCandidates,
            sdeCandidates = sdeCandidates,
            exactConsequences = exactConsequences,
            featureEvaluation = featureEvaluation,
            policyEvaluation = policyEvaluation,
            policySelection = policySelection,
            decisionTrace = decisionTrace,
            replayInput = replayInput,
        )
    }

    private fun validateFreshness(
        request: PairInformationShadowReplayRequest,
        exactContext: ExactConsequenceContext,
    ) {
        val snapshot = request.context.snapshot
        val gameStateRevision = snapshot.position.gameStateRevision.requireKnown("game-state revision")
        val playerInputRevision = snapshot.position.playerInputRevision.requireKnown("player-input revision")
        require(
            exactContext.sourceRevision.gameStateRevision == gameStateRevision &&
                exactContext.sourceRevision.playerInputRevision == playerInputRevision
        ) {
            "Pair shadow/replay requires the exact snapshot-backed source revision."
        }
        require(exactContext.exactContext.initialSnapshot.gameId == snapshot.gameId) {
            "Pair shadow/replay exact context must belong to the snapshot game."
        }
        require(exactContext.exactContext.initialSnapshot.gameSeed == snapshot.gameSeed) {
            "Pair shadow/replay exact context must use the snapshot game seed."
        }
        require(exactContext.exactContext.initialSnapshot.gameState.script == snapshot.script) {
            "Pair shadow/replay exact context must use the snapshot script."
        }
        require(
            exactContext.exactContext.initialPhase == StorytellerPhase.FIRST_NIGHT &&
                exactContext.exactContext.initialRound == 1
        ) {
            "Pair shadow/replay exact context must use the first-night baseline."
        }
    }

    private fun canonicalPrefix(
        request: PairInformationShadowReplayRequest,
        exactContext: ExactConsequenceContext,
    ): SdeHistoricalPrefixRef.Global {
        val exact = exactContext.exactContext
        val actionRefs = exact.actionTimeline.entries.map { entry ->
            require(entry.point.isStrictlyBeforeSdeDecision(request.lifecycleStage)) {
                "Pair shadow/replay action history must be strictly before the decision."
            }
            SdeHistoricalActionRef(
                actionId = entry.fact.actionId,
                globalSequence = entry.point.globalSequence,
            )
        }
        val observationRefs = exact.observationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: throw IllegalArgumentException(
                    "Pair shadow/replay requires globally bound historical observations.",
                )
            require(binding.point.isStrictlyBeforeSdeDecision(request.lifecycleStage)) {
                "Pair shadow/replay observation history must be strictly before the decision."
            }
            SdeHistoricalObservationRef(
                recordId = record.recordId,
                globalSequence = binding.point.globalSequence,
            )
        }
        return SdeHistoricalPrefixRef.Global(
            gameId = exact.initialSnapshot.gameId,
            actionRefs = actionRefs,
            observationRefs = observationRefs,
        )
    }

    private fun materializeObservation(
        request: PairInformationShadowReplayRequest,
        candidate: PairInformationLegalCandidate,
        formalSnapshotId: String,
    ): EpistemicObservation {
        val informationValue = if (candidate.outcome.shownRole == null) {
            InformationValue.NoCharacters(CharacterType.OUTSIDER)
        } else {
            InformationValue.PlayerPair(
                shownRole = candidate.outcome.shownRole,
                seats = candidate.outcome.candidateSeats,
            )
        }
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = request.context.naturalPairGameState,
            information = EffectDraft.PlayerInformation(
                recipientSeat = request.sourceSeat,
                sourceAbility = request.abilityRole,
                value = informationValue,
            ),
            roleDefinitions = request.context.roleDefinitions,
        )
        return EpistemicObservation(
            observationId = "c5b:${request.decisionId}:${candidate.candidateId}",
            snapshotId = formalSnapshotId,
            phase = request.lifecycleStage.phase,
            round = request.lifecycleStage.round,
            sequence = request.lifecycleStage.sequence,
            sourceSeat = request.sourceSeat,
            sourceAbility = request.abilityRole,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(request.sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
    }

    private fun enrichPairFeatures(
        base: DecisionFeatureEvaluation,
        game: com.codex.campboardgamehost.clocktower.domain.GameState,
        request: PairInformationShadowReplayRequest,
        legalCandidates: List<PairInformationLegalCandidate>,
        exactCandidates: List<ExactConsequenceCandidate>,
    ): DecisionFeatureEvaluation {
        if (base !is DecisionFeatureEvaluation.Ready) return base

        val candidateIds = legalCandidates.map(PairInformationLegalCandidate::candidateId)
        require(base.candidateIds == candidateIds) {
            "Pair feature enrichment must preserve the exact legal candidate order."
        }
        val futureById = PairInformationFutureFlexibilityProjector.project(
            game = game,
            legalCandidates = legalCandidates,
        )
        val naturalCandidates = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = request.sourceSeat,
            abilityRole = request.abilityRole,
            roleDefinitions = request.context.roleDefinitions,
        )
        val exposureEvidence = PairInformationRegistrationAmbiguityExposureProjector.project(
            game = game,
            naturalCandidates = naturalCandidates,
            legalCandidates = legalCandidates,
            exactCandidates = exactCandidates,
        )
        val exposureById = RoleFunctionExposureFeaturesProjector.project(exposureEvidence)
        require(futureById.keys == candidateIds.toSet()) {
            "Pair future-flexibility projection must cover every legal candidate."
        }
        require(exposureById.keys == candidateIds.toSet()) {
            "Pair role-exposure projection must cover every legal candidate."
        }

        return DecisionFeatureEvaluation.Ready(
            candidates = base.candidates.map { candidate ->
                CandidateDecisionFeatures(
                    candidateId = candidate.candidateId,
                    features = candidate.features.copy(
                        roleFunctionExposure = FeatureProjection.Projected(
                            exposureById.getValue(candidate.candidateId),
                        ),
                        futureFlexibility = futureById.getValue(candidate.candidateId),
                    ),
                )
            },
        )
    }

    private fun targetTypeFor(abilityRole: RoleId): CharacterType =
        when (abilityRole) {
            RoleId("Washerwoman") -> CharacterType.TOWNSFOLK
            RoleId("Librarian") -> CharacterType.OUTSIDER
            RoleId("Investigator") -> CharacterType.MINION
            else -> throw IllegalArgumentException(
                "Unsupported pair-information ability '${abilityRole.value}'.",
            )
        }

    private fun <T> SnapshotField<T>.requireKnown(label: String): T =
        (this as? SnapshotField.Known<T>)?.value
            ?: throw IllegalArgumentException("Pair shadow/replay requires known $label.")
}
