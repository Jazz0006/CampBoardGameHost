package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.RecommendationProfile
import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan

internal object PlanDiversifier {
    fun select(
        rankedCandidates: List<RecommendationPlan>,
        alreadySelected: List<RecommendationPlan>,
        profile: RecommendationProfile,
    ): RecommendationPlan? {
        val bestTier = rankedCandidates.firstOrNull()?.qualityTier ?: return null
        val eligible = rankedCandidates.takeWhile { it.qualityTier == bestTier }
        return eligible.maxWithOrNull(
            compareBy<RecommendationPlan> {
                adjustedScore(it, alreadySelected, profile)
            }.thenBy { it.totalScore },
        )
    }

    fun similarityPercent(
        first: PlanEffectSignature,
        second: PlanEffectSignature,
    ): Int {
        var similarity = 0
        if (first.redHerringSeat != null && first.redHerringSeat == second.redHerringSeat) similarity += 20
        if (first.drunkShownRole != null && first.drunkShownRole == second.drunkShownRole) similarity += 15
        if (
            first.drunkInvestigatorShownMinion != null &&
            first.drunkInvestigatorShownMinion == second.drunkInvestigatorShownMinion
        ) similarity += 15
        similarity += (25 * jaccard(first.suspectedSeats, second.suspectedSeats)).toInt()
        similarity += (25 * jaccard(first.demonBluffs, second.demonBluffs)).toInt()
        return similarity.coerceIn(0, 100)
    }

    private fun adjustedScore(
        candidate: RecommendationPlan,
        selected: List<RecommendationPlan>,
        profile: RecommendationProfile,
    ): Int {
        val maximumSimilarity = selected.maxOfOrNull {
            similarityPercent(candidate.effectSignature, it.effectSignature)
        } ?: 0
        return candidate.totalScore * 100 - maximumSimilarity * profile.diversityPenalty
    }

    private fun <T> jaccard(first: Set<T>, second: Set<T>): Double {
        if (first.isEmpty() && second.isEmpty()) return 0.0
        val unionSize = (first union second).size
        if (unionSize == 0) return 0.0
        return (first intersect second).size.toDouble() / unionSize
    }
}

internal fun QualityTier.rankingPriority(): Int = when (this) {
    QualityTier.RECOMMENDED -> 3
    QualityTier.ACCEPTABLE_WITH_WARNING -> 2
    QualityTier.EXPERT_ONLY -> 1
    QualityTier.REJECTED -> 0
}
