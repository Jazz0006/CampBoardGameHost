package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactRegistrationWitnessBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate

/**
 * Pure SDE projection from an already-legal pair-information candidate to exact consequence input.
 *
 * Registration legality and proposition materialization are intentionally upstream. This adapter
 * only binds the selected candidate's existing registration witness to the already-materialized
 * observation. An empty registration set is an explicit natural/no-special witness rather than
 * existential registration semantics.
 */
internal object PairInformationExactConsequenceAdapter {
    fun fromLegalCandidate(
        candidate: PairInformationLegalCandidate,
        observation: EpistemicObservation,
    ): ExactConsequenceCandidate {
        require(candidate.semanticTruth == SemanticTruth.TRUE) {
            "SDE-2B registration witness projection currently accepts truthful pair-information candidates only."
        }
        require(
            observation.visibility == ObservationVisibility.PRIVATE &&
                observation.recipientSeats.size == 1,
        ) {
            "First-night pair-information exact projection requires one private recipient."
        }

        return ExactConsequenceCandidate(
            candidateId = candidate.candidateId,
            recipientSeat = observation.recipientSeats.single(),
            observations = listOf(observation),
            registrationWitnessBindings = listOf(
                ExactRegistrationWitnessBinding(
                    observationId = observation.observationId,
                    registrations = candidate.registrations.toSet(),
                ),
            ),
        )
    }
}
