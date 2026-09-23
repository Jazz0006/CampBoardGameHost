package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.session.InformationDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot

/**
 * Non-authoritative shadow output for one already-validated structured information decision.
 *
 * The existing information snapshot remains the legality/recommendation/confirmation authority.
 * SDE diagnostics and planned references are attached beside it only; they cannot select, confirm,
 * commit, or mutate a session.
 */
internal data class StructuredInformationShadowEvaluation(
    val informationSnapshot: InformationDecisionSnapshot,
    val sdeCandidates: List<SdeDecisionCandidate>,
    val plannedDecisions: List<PlannedDecisionRef>,
    val consequences: ExactConsequenceEvaluation,
    val featureEvaluation: DecisionFeatureEvaluation,
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
        val projectedCandidates = decisionContext.legalCandidates.map { candidate ->
            val draft = candidate.draft
            require(draft.visibility == ObservationVisibility.PRIVATE && draft.recipientSeats.size == 1) {
                "The first structured-information shadow slice requires one private recipient per candidate."
            }
            require(draft.phase == historical.initialPhase && draft.round == historical.initialRound) {
                "Structured information shadow candidates must belong to the exact historical phase and round."
            }
            val exact = ExactConsequenceCandidate(
                candidateId = candidate.candidateId,
                recipientSeat = draft.recipientSeats.single(),
                observations = listOf(draft.toHypotheticalObservation(formalSnapshotId)),
            )
            val sde = SdeDecisionCandidate(
                decisionId = decisionId,
                candidateId = candidate.candidateId,
                lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                    phase = draft.phase,
                    round = draft.round,
                    sequence = draft.sequence,
                ),
                sourceInteraction = SdeDecisionSourceInteraction(
                    interactionId = decisionId,
                    sourceSeat = draft.sourceSeat,
                    abilityRole = draft.sourceAbility,
                ),
                sourceRevision = informationSnapshot.revision,
                inputBindings = SdeDecisionInputBindings.NotCaptured,
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
        val featureEvaluation = ExactConsequenceDecisionFeaturesProjector.project(
            evaluation = consequences,
            legalCandidateIds = informationSnapshot.legalCandidateIds,
            playerCount = historical.initialSnapshot.gameState.players.size,
        )
        return StructuredInformationShadowEvaluation(
            informationSnapshot = informationSnapshot,
            sdeCandidates = sdeCandidates,
            plannedDecisions = planned,
            consequences = consequences,
            featureEvaluation = featureEvaluation,
        )
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
