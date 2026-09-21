package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorld
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingWorldObservationEvaluator

internal data class Sde2D5FExpertObservedBooleanAlternative(
    val value: Boolean,
    val registrationWitnesses: Set<Set<RegistrationFact>>,
)

internal data class Sde2D5FExpertObservedBooleanDecisionEvidence(
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val metric: BooleanMetric,
    val subjectSeats: List<Int>,
    val observedValue: Boolean,
    val alternatives: List<Sde2D5FExpertObservedBooleanAlternative>,
) {
    val observedAlternative: Sde2D5FExpertObservedBooleanAlternative
        get() = alternatives.single { it.value == observedValue }
}

/**
 * Evidence-only projection for an already-validated boolean/target-check interaction.
 *
 * Role modules remain responsible for validating player-controlled targets and any setup commitments
 * such as the Fortune Teller Red Herring. This projector enumerates the complete boolean surface and
 * asks the existing exact semantic evaluator which outputs have a mechanical/registration witness in
 * the reconstructed actual world.
 */
internal object Sde2D5FExpertObservedBooleanEvidenceProjector {
    fun project(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        metric: BooleanMetric,
        subjectSeats: List<Int>,
        observedValue: Boolean,
        redHerringSeat: Int?,
        roleDefinitions: List<RoleDefinition>,
        observationIdPrefix: String,
    ): Sde2D5FExpertObservedBooleanDecisionEvidence {
        require(subjectSeats.isNotEmpty()) {
            "Expert-observed boolean evidence requires a non-empty subject set."
        }
        val world = EnumeratedWorld(
            rolesBySeat = game.players.associate { player -> player.seat to player.actualRole },
            redHerringSeat = redHerringSeat,
            shownRolesBySeat = game.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
        )
        val rolesById = roleDefinitions.associateBy(RoleDefinition::id)

        val alternatives = listOf(false, true).mapIndexedNotNull { index, value ->
            val observation = EpistemicObservation(
                observationId = "$observationIdPrefix-value-$value",
                snapshotId = "$observationIdPrefix-snapshot",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = index + 1,
                sourceSeat = sourceSeat,
                sourceAbility = abilityRole,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(sourceSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.BooleanResult(
                    metric = metric,
                    sourceSeat = sourceSeat,
                    subjectSeats = subjectSeats,
                    value = value,
                ),
            )
            val exact = TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = observation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )
            if (!exact.matches) {
                null
            } else {
                Sde2D5FExpertObservedBooleanAlternative(
                    value = value,
                    registrationWitnesses = exact.registrationWitnesses,
                )
            }
        }

        require(alternatives.any { it.value == observedValue }) {
            "Observed boolean value $observedValue has no exact witness in the reconstructed actual world."
        }

        return Sde2D5FExpertObservedBooleanDecisionEvidence(
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            metric = metric,
            subjectSeats = subjectSeats,
            observedValue = observedValue,
            alternatives = alternatives,
        )
    }
}
