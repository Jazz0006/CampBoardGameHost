package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorld
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingWorldObservationEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightNumericLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer

internal enum class Sde2D5FPrimaryVerificationStatus {
    PRIMARY_VERIFICATION_PENDING,
    PRIMARY_VERIFIED,
}

internal data class Sde2D5FExpertObservedNumericAlternative(
    val candidateId: String,
    val value: Int,
    val semanticTruth: SemanticTruth,
    val matchesReconstructedActualWorld: Boolean,
    val registrationWitnesses: Set<Set<RegistrationFact>>,
)

internal data class Sde2D5FExpertObservedNumericDecisionEvidence(
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val observedValue: Int,
    val alternatives: List<Sde2D5FExpertObservedNumericAlternative>,
) {
    val observedAlternative: Sde2D5FExpertObservedNumericAlternative
        get() = alternatives.single { it.value == observedValue }
}

/**
 * Evidence-only projection for one already-reconstructed first-night numeric decision.
 *
 * The projector does not implement Chef/Empath arithmetic or Spy/Recluse registration legality.
 * The production legal domain supplies player-visible numeric candidates. The existing exact world
 * evaluator supplies the interaction-local registration witnesses that can make each candidate true
 * in the reconstructed actual world.
 *
 * Keeping value and witness separate matters for expert-observed evidence: two legal numeric values
 * may correspond to different Storyteller registration rulings even though the player sees only a
 * number.
 */
internal object Sde2D5FExpertObservedNumericEvidenceProjector {
    fun project(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        observedValue: Int,
        roleDefinitions: List<RoleDefinition>,
        observationIdPrefix: String,
    ): Sde2D5FExpertObservedNumericDecisionEvidence {
        val legalCandidates = FirstNightNumericLegalDomain.generate(
            game = game,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        ).sortedBy { it.value }
        require(legalCandidates.isNotEmpty()) {
            "Expert-observed numeric decision must have at least one production-legal candidate."
        }
        require(legalCandidates.any { it.value == observedValue }) {
            "Observed value $observedValue is absent from the production numeric legal domain."
        }

        val world = EnumeratedWorld(
            rolesBySeat = game.players.associate { player -> player.seat to player.actualRole },
        )
        val rolesById = roleDefinitions.associateBy(RoleDefinition::id)

        val alternatives = legalCandidates.mapIndexed { index, candidate ->
            val information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = abilityRole,
                value = InformationValue.Number(candidate.value),
            )
            val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = game,
                information = information,
                roleDefinitions = roleDefinitions,
            )
            val observation = EpistemicObservation(
                observationId = "$observationIdPrefix-value-${candidate.value}",
                snapshotId = "$observationIdPrefix-snapshot",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = index + 1,
                sourceSeat = sourceSeat,
                sourceAbility = abilityRole,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(sourceSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = proposition,
            )
            val exact = TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = observation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )
            Sde2D5FExpertObservedNumericAlternative(
                candidateId = candidate.candidateId,
                value = candidate.value,
                semanticTruth = candidate.semanticTruth,
                matchesReconstructedActualWorld = exact.matches,
                registrationWitnesses = exact.registrationWitnesses,
            )
        }

        require(alternatives.all { it.matchesReconstructedActualWorld }) {
            "Production-legal healthy numeric alternatives must each have an exact witness in the reconstructed world."
        }

        return Sde2D5FExpertObservedNumericDecisionEvidence(
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            observedValue = observedValue,
            alternatives = alternatives,
        )
    }
}
