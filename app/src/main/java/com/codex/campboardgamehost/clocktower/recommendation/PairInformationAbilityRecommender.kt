package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RegistrationDecision
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.rules.PairInformationDisplaySemantics

/**
 * Recommends information for supported pair-information abilities after identity is fixed.
 *
 * Ability-specific code owns only the healthy truthful space. The shared pair display semantics
 * owns legal player-visible shape, and the dynamic information selector owns RELIABLE / DRUNK /
 * POISONED family selection and misinformation severity within the false family.
 */
internal object PairInformationAbilityRecommender {
    private const val stableVersion = "pair-information-ability-v1"

    fun recommend(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        style: RecommendationStyle,
    ): AbilityObservation? {
        val healthyCandidates = NaturalPairInformationCandidateGenerator
            .generateHealthyInformationSpace(game, sourceSeat, abilityRole, roleDefinitions)
        val healthyOutcomes = healthyCandidates.map { it.outcome }
        val healthyByKey = healthyCandidates.groupBy { keyOf(it.outcome) }
        val options = PairInformationDisplaySemantics
            .legalOutcomes(game, roleDefinitions, sourceSeat, abilityRole)
            .map { outcome ->
                PairInformationOption(
                    id = canonicalId(abilityRole, outcome),
                    outcome = outcome,
                    truthful = keyOf(outcome) in healthyByKey,
                    misinformationPressure = misinformationPressure(outcome, healthyOutcomes),
                )
            }
        val selected = DynamicCandidateGenerator.select(
            options = options,
            reliability = reliability.toDynamicReliability(),
            style = style,
            evilAdvantage = 0,
            stableKey = listOf(
                stableVersion,
                game.seed.toString(),
                sourceSeat.toString(),
                abilityRole.value,
                reliability.name,
                style.name,
            ).joinToString("|"),
            recentMisinformationStreak = 0,
            stableIdOf = PairInformationOption::id,
            isTruthful = PairInformationOption::truthful,
            misinformationPressure = PairInformationOption::misinformationPressure,
            styleOf = { style },
        ) ?: return null

        val selectedHealthyCandidates = if (selected.truthful) {
            healthyByKey[keyOf(selected.outcome)].orEmpty()
        } else {
            emptyList()
        }
        // If the same displayed clue is already true to actual state, no special registration
        // is needed. Otherwise preserve the deterministic registered-state fact that makes the
        // clue truthful so replay/history can distinguish it from ordinary actual-state truth.
        val semanticCandidate = selectedHealthyCandidates
            .filter { it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE }
            .minByOrNull { it.candidateId }
            ?: selectedHealthyCandidates.minByOrNull { it.candidateId }
        val registrations = semanticCandidate
            ?.registrations
            .orEmpty()
            .map { it.toObservationRegistration(abilityRole) }

        return AbilityObservation(
            sourceSeat = sourceSeat,
            perceivedRole = abilityRole,
            shownRole = selected.outcome.shownRole,
            candidateSeats = selected.outcome.candidateSeats,
            reliability = reliability,
            semanticTruth = if (selected.truthful) SemanticTruth.TRUE else SemanticTruth.FALSE,
            registrations = registrations,
        )
    }

    private fun misinformationPressure(
        outcome: PairInformationOutcome,
        healthyOutcomes: List<PairInformationOutcome>,
    ): Int {
        if (healthyOutcomes.any { keyOf(it) == keyOf(outcome) }) return 0
        if (healthyOutcomes.isEmpty()) return 2
        return healthyOutcomes.minOf { healthy ->
            if (outcome.shownRole == null || healthy.shownRole == null) {
                4
            } else {
                val rolePenalty = if (outcome.shownRole == healthy.shownRole) 0 else 2
                val seatOverlap = outcome.candidateSeats.intersect(healthy.candidateSeats.toSet()).size
                (rolePenalty + (2 - seatOverlap)).coerceIn(1, 4)
            }
        }
    }

    private fun RegistrationFact.toObservationRegistration(affectedAbility: RoleId): RegistrationDecision =
        RegistrationDecision(
            playerSeat = subjectSeat,
            affectedAbility = affectedAbility,
            registeredAlignment = registeredAlignment,
            registeredType = registeredType,
            registeredRole = registeredRole,
            reason = reason,
        )

    private fun canonicalId(abilityRole: RoleId, outcome: PairInformationOutcome): String = listOf(
        stableVersion,
        abilityRole.value,
        outcome.shownRole?.value ?: "none",
        outcome.candidateSeats.joinToString(","),
    ).joinToString("|")

    private fun keyOf(outcome: PairInformationOutcome): PairInformationKey = PairInformationKey(
        shownRole = outcome.shownRole,
        candidateSeats = outcome.candidateSeats,
    )

    private fun ReliabilityState.toDynamicReliability(): InformationReliability = when (this) {
        ReliabilityState.RELIABLE -> InformationReliability.RELIABLE
        ReliabilityState.DRUNK -> InformationReliability.DRUNK
        ReliabilityState.POISONED -> InformationReliability.POISONED
    }

    private data class PairInformationOption(
        val id: String,
        val outcome: PairInformationOutcome,
        val truthful: Boolean,
        val misinformationPressure: Int,
    )

    private data class PairInformationKey(
        val shownRole: RoleId?,
        val candidateSeats: List<Int>,
    )
}
