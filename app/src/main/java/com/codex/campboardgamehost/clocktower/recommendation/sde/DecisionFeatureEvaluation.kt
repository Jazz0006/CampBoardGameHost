package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability

internal data class CandidateDecisionFeatures(
    val candidateId: String,
    val features: DecisionFeatures,
) {
    init {
        require(candidateId.isNotBlank()) { "Feature projection candidate ID cannot be blank." }
    }
}

/**
 * Candidate-aligned feature projection result.
 *
 * Exact consequence capability is all-or-nothing for the current evaluator. A deferred result keeps
 * the legal candidate identity/order and exact missing capability set, but deliberately does not
 * manufacture feature values.
 */
internal sealed interface DecisionFeatureEvaluation {
    val candidateIds: List<String>

    data class Ready(
        val candidates: List<CandidateDecisionFeatures>,
    ) : DecisionFeatureEvaluation {
        init {
            require(candidates.isNotEmpty()) { "Ready feature evaluation requires candidates." }
            require(candidates.map(CandidateDecisionFeatures::candidateId).distinct().size == candidates.size) {
                "Ready feature evaluation candidate IDs must be unique."
            }
        }

        override val candidateIds: List<String>
            get() = candidates.map(CandidateDecisionFeatures::candidateId)
    }

    data class Deferred(
        override val candidateIds: List<String>,
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : DecisionFeatureEvaluation {
        init {
            require(candidateIds.isNotEmpty()) { "Deferred feature evaluation requires legal candidate IDs." }
            require(candidateIds.all(String::isNotBlank) && candidateIds.distinct().size == candidateIds.size) {
                "Deferred feature evaluation candidate IDs must be non-blank and unique."
            }
            require(missingCapabilities.isNotEmpty()) {
                "Deferred feature evaluation must identify missing capabilities."
            }
        }
    }
}

/**
 * Pure adapter from existing exact consequence authority into the score-free SDE feature contract.
 */
internal object ExactConsequenceDecisionFeaturesProjector {
    fun project(
        evaluation: ExactConsequenceEvaluation,
        legalCandidateIds: List<String>,
        playerCount: Int,
    ): DecisionFeatureEvaluation {
        require(legalCandidateIds.isNotEmpty()) { "Feature projection requires legal candidates." }
        require(legalCandidateIds.all(String::isNotBlank) && legalCandidateIds.distinct().size == legalCandidateIds.size) {
            "Legal candidate IDs must be non-blank and unique."
        }
        require(playerCount > 0) { "Player count must be positive." }

        return when (evaluation) {
            is ExactConsequenceEvaluation.Ready -> {
                val consequenceIds = evaluation.consequences.map(CandidateConsequence::candidateId)
                require(consequenceIds == legalCandidateIds) {
                    "Exact consequence order must match the upstream legal candidate authority."
                }
                DecisionFeatureEvaluation.Ready(
                    candidates = evaluation.consequences.map { consequence ->
                        CandidateDecisionFeatures(
                            candidateId = consequence.candidateId,
                            features = DecisionFeaturesProjector.project(
                                diagnostic = consequence.diagnostics,
                                playerCount = playerCount,
                            ),
                        )
                    },
                )
            }

            is ExactConsequenceEvaluation.Deferred ->
                DecisionFeatureEvaluation.Deferred(
                    candidateIds = legalCandidateIds.toList(),
                    missingCapabilities = evaluation.missingCapabilities,
                )
        }
    }
}
