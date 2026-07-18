package com.codex.campboardgamehost.clocktower.config

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle

internal data class RecommendationProfile(
    val style: RecommendationStyle,
    val redHerringOverlapPenalty: Int,
    val criticalExposurePenalty: Int,
    val protectedCrossCheckReward: Int,
    val bothProtectedPenalty: Int,
    val evilCandidatePenalty: Int,
    val drunkSelfCandidatePenalty: Int,
    val discussionValueWeight: Int,
    val separatedPairWeight: Int,
    val clusteredPairWeight: Int,
    val bluffEaseWeight: Int,
    val diversityPenalty: Int,
)

internal object RecommendationProfiles {
    val gentle = RecommendationProfile(
        style = RecommendationStyle.GENTLE,
        redHerringOverlapPenalty = 12,
        criticalExposurePenalty = 3,
        protectedCrossCheckReward = 3,
        bothProtectedPenalty = 10,
        evilCandidatePenalty = 18,
        drunkSelfCandidatePenalty = 10,
        discussionValueWeight = -1,
        separatedPairWeight = 2,
        clusteredPairWeight = 0,
        bluffEaseWeight = 3,
        diversityPenalty = 30,
    )

    val balanced = RecommendationProfile(
        style = RecommendationStyle.BALANCED,
        redHerringOverlapPenalty = 8,
        criticalExposurePenalty = 2,
        protectedCrossCheckReward = 7,
        bothProtectedPenalty = 8,
        evilCandidatePenalty = 14,
        drunkSelfCandidatePenalty = 8,
        discussionValueWeight = 0,
        separatedPairWeight = 2,
        clusteredPairWeight = 0,
        bluffEaseWeight = 2,
        diversityPenalty = 24,
    )

    val aggressive = RecommendationProfile(
        style = RecommendationStyle.AGGRESSIVE,
        redHerringOverlapPenalty = 3,
        criticalExposurePenalty = 1,
        protectedCrossCheckReward = 8,
        bothProtectedPenalty = 4,
        evilCandidatePenalty = 5,
        drunkSelfCandidatePenalty = 4,
        discussionValueWeight = 2,
        separatedPairWeight = 0,
        clusteredPairWeight = 2,
        bluffEaseWeight = 1,
        diversityPenalty = 20,
    )

    val all: List<RecommendationProfile> = listOf(gentle, balanced, aggressive)
}
