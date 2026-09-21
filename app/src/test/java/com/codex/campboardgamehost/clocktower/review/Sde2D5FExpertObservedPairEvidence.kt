package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingWorldObservationEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer

internal data class Sde2D5FExpertObservedPairAlternative(
    val candidateId: String,
    val shownRole: RoleId?,
    val candidateSeats: List<Int>,
    val semanticTruth: SemanticTruth,
    val candidateRegistrations: Set<RegistrationFact>,
    val matchesReconstructedActualWorld: Boolean,
    val exactRegistrationWitnesses: Set<Set<RegistrationFact>>,
)

internal data class Sde2D5FExpertObservedPairDecisionEvidence(
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val observedShownRole: RoleId?,
    val observedCandidateSeats: List<Int>,
    val alternatives: List<Sde2D5FExpertObservedPairAlternative>,
) {
    val observedAlternative: Sde2D5FExpertObservedPairAlternative
        get() = alternatives.single { alternative ->
            alternative.shownRole == observedShownRole &&
                alternative.candidateSeats == observedCandidateSeats
        }
}

/**
 * Evidence-only projection for Washerwoman/Librarian/Investigator-shaped first-night information.
 *
 * Candidate legality is owned by PairInformationLegalDomain. Exact-world evaluation is used only to
 * retain witness/explanation structure for the reconstructed state, including hidden impairment.
 */
internal object Sde2D5FExpertObservedPairEvidenceProjector {
    fun project(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        observedShownRole: RoleId?,
        observedCandidateSeats: List<Int>,
        roleDefinitions: List<RoleDefinition>,
        observationIdPrefix: String,
    ): Sde2D5FExpertObservedPairDecisionEvidence {
        val canonicalObservedSeats = observedCandidateSeats.sorted()
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        ).sortedBy { it.candidateId }
        require(legalCandidates.isNotEmpty()) {
            "Expert-observed pair decision must have at least one production-legal candidate."
        }
        require(
            legalCandidates.any { candidate ->
                candidate.outcome.shownRole == observedShownRole &&
                    candidate.outcome.candidateSeats == canonicalObservedSeats
            },
        ) {
            "Observed pair output is absent from the production pair-information legal domain."
        }

        val world = Sde2D5FExpertObservedWorldProjection.from(game)
        val rolesById = roleDefinitions.associateBy(RoleDefinition::id)
        val alternatives = legalCandidates.mapIndexed { index, candidate ->
            val outcome = candidate.outcome
            val value = if (outcome.shownRole == null) {
                InformationValue.NoCharacters(CharacterType.OUTSIDER)
            } else {
                InformationValue.PlayerPair(
                    shownRole = outcome.shownRole,
                    seats = outcome.candidateSeats,
                )
            }
            val information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = abilityRole,
                value = value,
            )
            val observation = EpistemicObservation(
                observationId = "$observationIdPrefix-candidate-$index",
                snapshotId = "$observationIdPrefix-snapshot",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = index + 1,
                sourceSeat = sourceSeat,
                sourceAbility = abilityRole,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(sourceSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                    game = game,
                    information = information,
                    roleDefinitions = roleDefinitions,
                ),
            )
            val exact = TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = observation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )
            Sde2D5FExpertObservedPairAlternative(
                candidateId = candidate.candidateId,
                shownRole = outcome.shownRole,
                candidateSeats = outcome.candidateSeats,
                semanticTruth = candidate.semanticTruth,
                candidateRegistrations = candidate.registrations.toSet(),
                matchesReconstructedActualWorld = exact.matches,
                exactRegistrationWitnesses = exact.registrationWitnesses,
            )
        }

        require(alternatives.all { it.matchesReconstructedActualWorld }) {
            "Every production-legal pair candidate must have an exact witness or impairment explanation in the reconstructed world."
        }

        return Sde2D5FExpertObservedPairDecisionEvidence(
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            observedShownRole = observedShownRole,
            observedCandidateSeats = canonicalObservedSeats,
            alternatives = alternatives,
        )
    }
}
