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
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability

/**
 * Recommends information for supported pair-information abilities after identity is fixed.
 *
 * Candidate legality/truth/registration comes from [PairInformationLegalDomain]. This object owns
 * ranking/selection only; it must not narrow the Storyteller's legal manual domain.
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
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
        val truthfulOutcomes = legalCandidates
            .filter { it.semanticTruth == SemanticTruth.TRUE }
            .map { it.outcome }
        val selected = DynamicCandidateGenerator.select(
            options = legalCandidates,
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
            stableIdOf = PairInformationLegalCandidate::candidateId,
            isTruthful = { it.semanticTruth == SemanticTruth.TRUE },
            misinformationPressure = { candidate ->
                misinformationPressure(candidate.outcome, truthfulOutcomes)
            },
            styleOf = { style },
        ) ?: return null

        return AbilityObservation(
            sourceSeat = sourceSeat,
            perceivedRole = abilityRole,
            shownRole = selected.outcome.shownRole,
            candidateSeats = selected.outcome.candidateSeats,
            reliability = reliability,
            semanticTruth = selected.semanticTruth,
            registrations = selected.registrations.map { it.toObservationRegistration(abilityRole) },
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

    private fun keyOf(outcome: PairInformationOutcome): PairInformationKey = PairInformationKey(
        shownRole = outcome.shownRole,
        candidateSeats = outcome.candidateSeats,
    )

    private fun ReliabilityState.toDynamicReliability(): InformationReliability = when (this) {
        ReliabilityState.RELIABLE -> InformationReliability.RELIABLE
        ReliabilityState.DRUNK -> InformationReliability.DRUNK
        ReliabilityState.POISONED -> InformationReliability.POISONED
    }

    private data class PairInformationKey(
        val shownRole: RoleId?,
        val candidateSeats: List<Int>,
    )
}
