package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector

internal object HistoryCooldown {
    private const val MINIMUM_MULTIPLIER = 200_000L
    private const val MEDIUM_PENALTY = 300_000L
    private const val LIGHT_PENALTY = 120_000L

    fun <T> apply(
        pool: List<DecisionEvaluation<T>>,
        history: CrossGameHistory,
        signatureOf: (DecisionEvaluation<T>) -> HistoricalClueSignature,
    ): List<DecisionEvaluation<T>> = pool.map { evaluation ->
        val multiplier = multiplierFixedPoint(signatureOf(evaluation), history)
        if (multiplier == WeightedStableSelector.FIXED_POINT_SCALE) {
            evaluation
        } else {
            evaluation.copy(
                withinFamilyWeightFixedPoint = Math.multiplyExact(
                    evaluation.withinFamilyWeightFixedPoint,
                    multiplier,
                ) / WeightedStableSelector.FIXED_POINT_SCALE,
                explanationCodes = (evaluation.explanationCodes + "selection.cross-game-cooldown").distinct(),
            ).let { adjusted ->
                adjusted.copy(withinFamilyWeightFixedPoint = adjusted.withinFamilyWeightFixedPoint.coerceAtLeast(1L))
            }
        }
    }

    fun multiplierFixedPoint(
        candidate: HistoricalClueSignature,
        history: CrossGameHistory,
    ): Long {
        var penalty = 0L
        history.recentSignatures.forEachIndexed { age, previous ->
            if (candidate.decisionType != previous.decisionType) return@forEachIndexed
            val decayTenths = (10 - age).coerceAtLeast(1)
            fun decayed(value: Long): Long = value * decayTenths / 10L

            if (age < 3 && candidate.shownCharacter != null && candidate.shownCharacter == previous.shownCharacter) {
                penalty += decayed(MEDIUM_PENALTY)
            }
            if (candidate.candidateAlignmentPattern != null &&
                candidate.candidateAlignmentPattern == previous.candidateAlignmentPattern
            ) {
                penalty += decayed(LIGHT_PENALTY)
            }
            if (candidate.candidateSeatDistance != null &&
                candidate.candidateSeatDistance == previous.candidateSeatDistance
            ) {
                penalty += decayed(LIGHT_PENALTY)
            }
            if (candidate.redHerringRole != null && candidate.redHerringRole == previous.redHerringRole) {
                penalty += decayed(MEDIUM_PENALTY)
            }
            if (candidate.demonBluffs.isNotEmpty() && candidate.demonBluffs == previous.demonBluffs) {
                penalty += decayed(MEDIUM_PENALTY)
            }
        }
        return (WeightedStableSelector.FIXED_POINT_SCALE - penalty)
            .coerceIn(MINIMUM_MULTIPLIER, WeightedStableSelector.FIXED_POINT_SCALE)
    }
}
