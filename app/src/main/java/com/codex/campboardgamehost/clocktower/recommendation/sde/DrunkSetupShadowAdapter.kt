package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateSpaceAudit
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightBundleCandidateSpaceAuditor
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup

internal data class DrunkSetupShadowCandidate(
    val candidate: TroubleBrewingDrunkCandidate,
    val hypotheticalSetup: TroubleBrewingDrunkHypotheticalSetup,
    val ecologyAudit: FirstNightBundleCandidateSpaceAudit,
    val consequenceEnvelope: DrunkSetupConsequenceEnvelope,
    val sdeCandidate: SdeDecisionCandidate,
    val proposedCommitRef: CommittedDecisionInputRef,
) {
    init {
        require(candidate == hypotheticalSetup.candidate) {
            "Drunk shadow candidate must preserve the DLB-2 hypothetical candidate identity."
        }
        require(consequenceEnvelope.candidateSeat == candidate.seat) {
            "Drunk shadow consequence must preserve the candidate seat."
        }
        require(consequenceEnvelope.shownRoleId == candidate.shownRoleId) {
            "Drunk shadow consequence must preserve the candidate shown role."
        }
        require(sdeCandidate.lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
            "Drunk shadow candidate must remain at SetupPrecommit lifecycle."
        }
        require(proposedCommitRef.kind == SdeCommittedDecisionInputKind.DRUNK_SEAT) {
            "Drunk shadow candidate requires a DRUNK_SEAT proposed commit reference."
        }
    }
}

/**
 * Non-authoritative DLB-3A setup shadow.
 *
 * Ecology evidence is retained beside each legal candidate, but it is intentionally not projected
 * into BEGINNER_CONSERVATIVE_V1 yet. The frozen policy therefore defers rather than manufacturing
 * an evidence-free Drunk-seat preference.
 */
internal data class DrunkSetupShadowEvaluation(
    val candidates: List<DrunkSetupShadowCandidate>,
    val drunkAssignmentFeatureEvaluation: DrunkAssignmentFeatureEvaluation,
    val featureEvaluation: DecisionFeatureEvaluation,
    val policyEvaluation: BeginnerConservativePolicyEvaluation,
    val policySelection: PolicySelection?,
    val decisionTrace: DecisionTrace,
    val replayInput: MultiPolicyReplayInput,
) {
    val sdeCandidates: List<SdeDecisionCandidate>
        get() = candidates.map(DrunkSetupShadowCandidate::sdeCandidate)

    init {
        require(candidates.isNotEmpty()) { "Drunk setup shadow requires legal candidates." }
        val candidateIds = sdeCandidates.map(SdeDecisionCandidate::candidateId)
        require(candidateIds.distinct().size == candidateIds.size) {
            "Drunk setup shadow candidate IDs must be unique."
        }
        require(drunkAssignmentFeatureEvaluation.candidateIds == candidateIds) {
            "Drunk-assignment feature surface must preserve legal-candidate order."
        }
        require(featureEvaluation.candidateIds == candidateIds) {
            "Drunk setup shadow features must preserve legal-candidate order."
        }
        require(policyEvaluation.candidateIds == candidateIds) {
            "Drunk setup shadow policy must preserve legal-candidate order."
        }
        require(policyEvaluation.policyVersion == PolicyVersions.BEGINNER_CONSERVATIVE_V1) {
            "Drunk setup shadow must use the frozen BEGINNER_CONSERVATIVE_V1 definition."
        }
        require(policySelection == null) {
            "DLB-3A must not select a Drunk candidate before strategic projection is authorized."
        }
        require(decisionTrace.legalCandidateIds == candidateIds) {
            "Drunk setup shadow trace must preserve legal-candidate order."
        }
        require(decisionTrace.actualChoice == DecisionTraceActualChoice.Pending) {
            "Drunk setup shadow trace must remain pending."
        }
        require(replayInput.legalCandidateIds == candidateIds) {
            "Drunk setup shadow replay input must preserve legal-candidate order."
        }
    }
}

/**
 * Pure DLB-3A adapter from the DLB-2 legality/projector authority into the existing SDE lifecycle.
 *
 * This object does not rank, select, bind, commit, persist, or mutate a Drunk seat.
 */
internal object DrunkSetupShadowAdapter {
    fun evaluateIfNeeded(
        gameId: String,
        intermediateSetup: TroubleBrewingIntermediateSetup,
        characterRegistry: ClocktowerCharacterRegistry,
        roleDefinitions: List<RoleDefinition>,
        sourceRevision: InformationDecisionRevision,
    ): DrunkSetupShadowEvaluation? {
        if (!intermediateSetup.visibleRoster.hasDrunk) return null
        return evaluate(
            gameId = gameId,
            intermediateSetup = intermediateSetup,
            characterRegistry = characterRegistry,
            roleDefinitions = roleDefinitions,
            sourceRevision = sourceRevision,
        )
    }

    fun evaluate(
        gameId: String,
        intermediateSetup: TroubleBrewingIntermediateSetup,
        characterRegistry: ClocktowerCharacterRegistry,
        roleDefinitions: List<RoleDefinition>,
        sourceRevision: InformationDecisionRevision,
    ): DrunkSetupShadowEvaluation {
        require(gameId.isNotBlank()) { "Drunk setup shadow requires a game ID." }
        require(intermediateSetup.visibleRoster.hasDrunk) {
            "Drunk setup shadow requires an intermediate setup that contains Drunk."
        }

        val legalCandidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediateSetup)
        require(legalCandidates.isNotEmpty()) {
            "Drunk setup shadow requires at least one rules-legal dealt Townsfolk candidate."
        }

        val decisionId = "setup:drunk-seat:$gameId"
        val historyPrefix = SdeHistoricalPrefixRef.Global(
            gameId = gameId,
            actionRefs = emptyList(),
            observationRefs = emptyList(),
        )
        val projectedCandidates = legalCandidates.map { candidate ->
            val candidateId = candidate.candidateId()
            val hypothetical = TroubleBrewingDrunkHypotheticalProjector.project(
                intermediateSetup = intermediateSetup,
                candidate = candidate,
                characterRegistry = characterRegistry,
            )
            val ecology = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(
                game = hypothetical.gameState,
                roleDefinitions = roleDefinitions,
            )
            val consequenceEnvelope = DrunkSetupConsequenceProjector.project(
                candidate = candidate,
                hypotheticalSetup = hypothetical,
                ecologyAudit = ecology,
            )
            val proposedCommitRef = CommittedDecisionInputRef(
                inputId = candidateId,
                ownerId = DRUNK_COMMIT_OWNER_ID,
                kind = SdeCommittedDecisionInputKind.DRUNK_SEAT,
            )
            val sdeCandidate = SdeDecisionCandidate(
                decisionId = decisionId,
                candidateId = candidateId,
                lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
                sourceInteraction = SdeDecisionSourceInteraction(
                    interactionId = decisionId,
                ),
                sourceRevision = sourceRevision,
                inputBindings = SdeDecisionInputBindings.Captured(),
                historyPrefixRef = historyPrefix,
                legalOutcomeIdentity = candidateId,
                hypotheticalRef = SdeDecisionHypotheticalRef(
                    effectRefs = listOf("$candidateId:projected-setup"),
                ),
                legalityProvenance = SdeDecisionLegalityProvenance(
                    ownerId = LEGALITY_OWNER_ID,
                    candidateSpaceIdentity = LEGALITY_SPACE_ID,
                    candidateSchemaVersion = "dlb-2",
                ),
            )
            DrunkSetupShadowCandidate(
                candidate = candidate,
                hypotheticalSetup = hypothetical,
                ecologyAudit = ecology,
                consequenceEnvelope = consequenceEnvelope,
                sdeCandidate = sdeCandidate,
                proposedCommitRef = proposedCommitRef,
            )
        }

        val legalCandidateIds = projectedCandidates.map { it.sdeCandidate.candidateId }
        val drunkAssignmentFeatureEvaluation = DrunkAssignmentFeatureEvaluation(
            candidates = projectedCandidates.map { projected ->
                CandidateDrunkAssignmentFeatures(
                    candidateId = projected.sdeCandidate.candidateId,
                    features = DrunkAssignmentFeaturesProjector.project(
                        projected.consequenceEnvelope,
                    ),
                )
            },
        )
        val featureEvaluation = DecisionFeatureEvaluation.Ready(
            candidates = legalCandidateIds.map { candidateId ->
                CandidateDecisionFeatures(
                    candidateId = candidateId,
                    features = DecisionFeatures.unavailable(
                        FeatureUnavailableReason.NOT_PROJECTED_YET,
                    ),
                )
            },
        )
        val policyEvaluation = BeginnerConservativeV1Policy.evaluate(featureEvaluation)
        require(policyEvaluation is BeginnerConservativePolicyEvaluation.Deferred) {
            "DLB-3A must defer frozen V1 until strategic Drunk-assignment projection exists."
        }
        require(
            policyEvaluation.reasons ==
                setOf(BeginnerConservativePolicyDeferralReason.STRATEGIC_FEATURE_UNAVAILABLE),
        ) {
            "DLB-3A frozen V1 must defer specifically because strategic projection is unavailable."
        }

        val policySelection = BeginnerConservativeV1Selector.select(
            evaluation = policyEvaluation,
            decisionId = decisionId,
            selectionSeed = intermediateSetup.gameSeed,
        )
        require(policySelection == null) {
            "DLB-3A deferred policy must not manufacture a deterministic Drunk-seat selection."
        }

        val definition = StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1
        val decisionTrace = DecisionTrace(
            evidenceCheckpoint = definition.evidenceCheckpoint,
            decisionId = decisionId,
            lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
            sourceRevision = sourceRevision,
            historyPrefixRef = historyPrefix,
            legalCandidateIds = legalCandidateIds,
            featureEvaluation = featureEvaluation,
            policySnapshot = policyEvaluation.toDecisionTracePolicySnapshot(),
            policySelection = null,
            actualChoice = DecisionTraceActualChoice.Pending,
        )
        val replayInput = MultiPolicyReplayInput(
            decisionId = decisionId,
            lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
            sourceRevision = sourceRevision,
            historyPrefixRef = historyPrefix,
            legalCandidateIds = legalCandidateIds,
            featureEvaluation = featureEvaluation,
            selectionSeed = intermediateSetup.gameSeed,
        )

        return DrunkSetupShadowEvaluation(
            candidates = projectedCandidates,
            drunkAssignmentFeatureEvaluation = drunkAssignmentFeatureEvaluation,
            featureEvaluation = featureEvaluation,
            policyEvaluation = policyEvaluation,
            policySelection = null,
            decisionTrace = decisionTrace,
            replayInput = replayInput,
        )
    }

    private fun TroubleBrewingDrunkCandidate.candidateId(): String =
        "setup:drunk-seat:seat-$seat"

    private const val LEGALITY_OWNER_ID = "TroubleBrewingDrunkCandidateDomain"
    private const val LEGALITY_SPACE_ID = "trouble-brewing:drunk-seat"
    private const val DRUNK_COMMIT_OWNER_ID = "setup:drunk-seat"
}
