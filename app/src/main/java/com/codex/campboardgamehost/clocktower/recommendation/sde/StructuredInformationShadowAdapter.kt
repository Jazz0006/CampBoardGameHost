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
    val plannedDecisions: List<PlannedDecisionRef>,
    val consequences: ExactConsequenceEvaluation,
) {
    init {
        require(plannedDecisions.map(PlannedDecisionRef::candidateId) == informationSnapshot.legalCandidateIds) {
            "Structured information shadow plans must preserve the source legal-candidate order."
        }
        require(plannedDecisions.all { it.isCurrentFor(informationSnapshot) }) {
            "Structured information shadow plans must be bound to the source information snapshot."
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
        val exactCandidates = decisionContext.legalCandidates.map { candidate ->
            val draft = candidate.draft
            require(draft.visibility == ObservationVisibility.PRIVATE && draft.recipientSeats.size == 1) {
                "The first structured-information shadow slice requires one private recipient per candidate."
            }
            require(draft.phase == historical.initialPhase && draft.round == historical.initialRound) {
                "Structured information shadow candidates must belong to the exact historical phase and round."
            }
            ExactConsequenceCandidate(
                candidateId = candidate.candidateId,
                recipientSeat = draft.recipientSeats.single(),
                observations = listOf(draft.toHypotheticalObservation(formalSnapshotId)),
            )
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
        return StructuredInformationShadowEvaluation(
            informationSnapshot = informationSnapshot,
            plannedDecisions = planned,
            consequences = consequences,
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
