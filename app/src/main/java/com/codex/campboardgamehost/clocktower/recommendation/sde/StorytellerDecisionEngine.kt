package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactRegistrationWitnessBinding
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * Read-only bounded context for the first exact-consequence SDE seam.
 *
 * This intentionally does not reuse the legacy domain StorytellerDecisionRequest because that model
 * embeds DynamicGameState. Canonical state, revisions and semantic history remain owned by the
 * session/epistemic inputs referenced here.
 *
 * [sourceRevision] is deliberately separate from [ExactHistoricalHypotheticalContext.initialSnapshot]:
 * historical replay starts from its setup/baseline snapshot, while a planned structured decision must
 * be freshness-bound to the current production session revision. Tests and callers that evaluate the
 * baseline itself may rely on the default; historical production callers must pass the current revision.
 */
internal data class ExactConsequenceContext(
    val validatedRuleset: ValidatedClocktowerRuleset,
    val exactContext: ExactHistoricalHypotheticalContext,
    val sourceRevision: InformationDecisionRevision = InformationDecisionRevision(
        gameStateRevision = exactContext.initialSnapshot.gameStateRevision,
        playerInputRevision = exactContext.initialSnapshot.playerInputRevision,
    ),
) {
    val gameStateRevision: Long get() = sourceRevision.gameStateRevision
    val playerInputRevision: Long get() = sourceRevision.playerInputRevision
}

/** One already-legal, already-materialized hypothetical observation-bundle candidate. */
internal data class ExactConsequenceCandidate(
    val candidateId: String,
    val recipientSeat: Int,
    val observations: List<EpistemicObservation>,
    val registrationWitnessBindings: List<ExactRegistrationWitnessBinding> = emptyList(),
) {
    init {
        require(candidateId.isNotBlank()) { "Exact-consequence candidate ID cannot be blank." }
        require(recipientSeat > 0) { "Exact-consequence candidate recipient seat must be positive." }
        require(observations.isNotEmpty()) { "Exact-consequence candidate observations cannot be empty." }
        require(
            registrationWitnessBindings
                .map(ExactRegistrationWitnessBinding::observationId)
                .distinct()
                .size == registrationWitnessBindings.size,
        ) {
            "An exact-consequence observation may have at most one selected registration witness."
        }
        val observationIds = observations.map(EpistemicObservation::observationId).toSet()
        require(registrationWitnessBindings.all { it.observationId in observationIds }) {
            "Every registration witness binding must reference an observation in the same exact-consequence candidate."
        }
    }
}

/**
 * Bounded request envelope for exact consequence evaluation only.
 *
 * This is not the final global Storyteller request model and deliberately carries no DynamicGameState,
 * selection weights, mutable lifecycle state or legacy heuristic summaries.
 */
internal data class ExactConsequenceRequest(
    val decisionId: String,
    val candidates: List<ExactConsequenceCandidate>,
) {
    init {
        require(decisionId.isNotBlank()) { "Exact-consequence decision ID cannot be blank." }
        require(candidates.isNotEmpty()) { "Exact-consequence request requires at least one candidate." }
        require(candidates.map(ExactConsequenceCandidate::candidateId).distinct().size == candidates.size) {
            "Exact-consequence candidate IDs must be unique within a request."
        }
    }
}

internal data class CandidateConsequence(
    val candidateId: String,
    val diagnostics: ExactHypotheticalObservationBundleDiagnostics,
)

internal sealed interface ExactConsequenceEvaluation {
    data class Ready(
        val consequences: List<CandidateConsequence>,
    ) : ExactConsequenceEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : ExactConsequenceEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred exact-consequence evaluation must identify missing capabilities."
            }
        }
    }
}

/**
 * Thin recommendation-owned orchestration seam over the existing exact epistemic authority.
 *
 * This first SDE slice evaluates consequences only. It does not select a candidate, commit an
 * observation, mutate session state, alter interaction ordering, or introduce heuristic fallback.
 */
internal object StorytellerDecisionEngine {
    fun evaluateExactConsequences(
        request: ExactConsequenceRequest,
        context: ExactConsequenceContext,
    ): ExactConsequenceEvaluation {
        val queries = request.candidates.mapIndexed { index, candidate ->
            ExactHypotheticalObservationBundleQuery(
                bundleId = queryId(request.decisionId, index, candidate.candidateId),
                recipientSeat = candidate.recipientSeat,
                observations = candidate.observations,
                registrationWitnessBindings = candidate.registrationWitnessBindings,
            )
        }

        return when (
            val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                validatedRuleset = context.validatedRuleset,
                context = context.exactContext,
                queries = queries,
            )
        ) {
            is ExactHypotheticalObservationBundleEvaluation.Deferred ->
                ExactConsequenceEvaluation.Deferred(
                    missingCapabilities = evaluation.missingCapabilities,
                )

            is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                require(evaluation.diagnostics.size == request.candidates.size) {
                    "Exact evaluator returned ${evaluation.diagnostics.size} diagnostics for " +
                        "${request.candidates.size} exact-consequence candidates."
                }
                ExactConsequenceEvaluation.Ready(
                    consequences = request.candidates.zip(evaluation.diagnostics).map { (candidate, diagnostic) ->
                        CandidateConsequence(
                            candidateId = candidate.candidateId,
                            diagnostics = diagnostic.copy(bundleId = candidate.candidateId),
                        )
                    },
                )
            }
        }
    }

    private fun queryId(
        decisionId: String,
        index: Int,
        candidateId: String,
    ): String = "sde:$decisionId:$index:$candidateId"
}
