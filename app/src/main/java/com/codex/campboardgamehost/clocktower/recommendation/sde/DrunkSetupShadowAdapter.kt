package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateSpaceAudit
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightBundleCandidateSpaceAuditor
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
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
 * Non-authoritative setup shadow for rules-owned Drunk candidates and deterministic feature evidence.
 *
 * This adapter owns no policy evaluation, ranking, selection, trace persistence, or replay authority.
 */
internal data class DrunkSetupShadowEvaluation(
    val decisionContext: DrunkAssignmentDecisionContext,
    val candidates: List<DrunkSetupShadowCandidate>,
    val drunkAssignmentFeatureEvaluation: DrunkAssignmentFeatureEvaluation,
    val featureEvaluation: DecisionFeatureEvaluation,
) {
    val sdeCandidates: List<SdeDecisionCandidate>
        get() = candidates.map(DrunkSetupShadowCandidate::sdeCandidate)

    init {
        require(candidates.isNotEmpty()) { "Drunk setup shadow requires legal candidates." }
        require(
            candidates.map { candidate -> candidate.candidate.seat to candidate.candidate.shownRoleId } ==
                decisionContext.legalCandidates.map { candidate -> candidate.seat to candidate.shownRoleId },
        ) {
            "Drunk setup shadow candidates must preserve the snapshot-backed legal domain."
        }
        val candidateIds = sdeCandidates.map(SdeDecisionCandidate::candidateId)
        require(candidateIds == decisionContext.legalCandidateIds) {
            "Drunk setup shadow candidate IDs must preserve the typed decision context."
        }
        require(candidateIds.distinct().size == candidateIds.size) {
            "Drunk setup shadow candidate IDs must be unique."
        }
        require(drunkAssignmentFeatureEvaluation.candidateIds == candidateIds) {
            "Drunk-assignment feature surface must preserve legal-candidate order."
        }
        require(featureEvaluation.candidateIds == candidateIds) {
            "Drunk setup shadow features must preserve legal-candidate order."
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

        val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
            gameId = gameId,
            intermediateSetup = intermediateSetup,
        )
        val decisionContext = DrunkAssignmentDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = characterRegistry,
            sourceRevision = sourceRevision,
        )
        val assignmentsBySeat = intermediateSetup.shownSeatAssignments.associateBy { it.seat }
        val legalCandidates = decisionContext.legalCandidates.map { candidateRef ->
            val assignment = requireNotNull(assignmentsBySeat[candidateRef.seat]) {
                "Snapshot-backed Drunk candidate must map to the intermediate setup seat."
            }
            require(assignment.shownRoleId == candidateRef.shownRoleId) {
                "Snapshot-backed Drunk candidate shown role must match the intermediate setup."
            }
            TroubleBrewingDrunkCandidate(
                seat = candidateRef.seat,
                playerName = assignment.playerName,
                shownRoleId = candidateRef.shownRoleId,
            )
        }

        val decisionId = decisionContext.decisionId
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
                sourceRevision = decisionContext.sourceRevision,
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
        return DrunkSetupShadowEvaluation(
            decisionContext = decisionContext,
            candidates = projectedCandidates,
            drunkAssignmentFeatureEvaluation = drunkAssignmentFeatureEvaluation,
            featureEvaluation = featureEvaluation,
        )
    }

    private fun TroubleBrewingDrunkCandidate.candidateId(): String =
        "setup:drunk-seat:seat-$seat"

    private const val LEGALITY_OWNER_ID = "TroubleBrewingDrunkCandidateDomain"
    private const val LEGALITY_SPACE_ID = "trouble-brewing:drunk-seat"
    private const val DRUNK_COMMIT_OWNER_ID = "setup:drunk-seat"
}
