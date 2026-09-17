package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery

/**
 * Read-only orchestration context for exact Storyteller consequence evaluation.
 *
 * Canonical state, revisions and semantic history remain owned by the session/epistemic inputs
 * referenced here. This type intentionally does not copy or mutate game state.
 */
internal data class StorytellerDecisionContext(
    val validatedRuleset: ValidatedClocktowerRuleset,
    val exactContext: ExactHistoricalHypotheticalContext,
) {
    val gameStateRevision: Long get() = exactContext.initialSnapshot.gameStateRevision
    val playerInputRevision: Long get() = exactContext.initialSnapshot.playerInputRevision
}

/**
 * One already-legal, already-materialized hypothetical candidate.
 *
 * Candidate legality and observation semantics are owned by their existing producers/materializers;
 * the SDE only orchestrates consequence evaluation.
 */
internal data class StorytellerDecisionCandidate(
    val candidateId: String,
    val recipientSeat: Int,
    val observation: EpistemicObservation,
) {
    init {
        require(candidateId.isNotBlank()) { "Storyteller decision candidate ID cannot be blank." }
        require(recipientSeat > 0) { "Storyteller decision candidate recipient seat must be positive." }
    }
}

internal data class StorytellerDecisionRequest(
    val decisionId: String,
    val candidates: List<StorytellerDecisionCandidate>,
) {
    init {
        require(decisionId.isNotBlank()) { "Storyteller decision ID cannot be blank." }
        require(candidates.isNotEmpty()) { "Storyteller decision request requires at least one candidate." }
        require(candidates.map(StorytellerDecisionCandidate::candidateId).distinct().size == candidates.size) {
            "Storyteller decision candidate IDs must be unique within a request."
        }
    }
}

internal data class CandidateConsequence(
    val candidateId: String,
    val diagnostics: ExactHypotheticalObservationBundleDiagnostics,
)

internal sealed interface StorytellerDecisionEvaluation {
    data class Ready(
        val consequences: List<CandidateConsequence>,
    ) : StorytellerDecisionEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : StorytellerDecisionEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred Storyteller decision evaluation must identify missing capabilities."
            }
        }
    }
}

/**
 * Thin recommendation-owned orchestration seam over the existing exact epistemic authority.
 *
 * This first SDE slice performs consequence evaluation only. It does not select a candidate, commit
 * an observation, mutate session state, alter interaction ordering, or introduce heuristic fallback.
 */
internal object StorytellerDecisionEngine {
    fun evaluate(
        request: StorytellerDecisionRequest,
        context: StorytellerDecisionContext,
    ): StorytellerDecisionEvaluation {
        val queries = request.candidates.mapIndexed { index, candidate ->
            ExactHypotheticalObservationBundleQuery(
                bundleId = queryId(request.decisionId, index, candidate.candidateId),
                recipientSeat = candidate.recipientSeat,
                observations = listOf(candidate.observation),
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
                StorytellerDecisionEvaluation.Deferred(
                    missingCapabilities = evaluation.missingCapabilities,
                )

            is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                require(evaluation.diagnostics.size == request.candidates.size) {
                    "Exact evaluator returned ${evaluation.diagnostics.size} diagnostics for " +
                        "${request.candidates.size} Storyteller decision candidates."
                }
                StorytellerDecisionEvaluation.Ready(
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
