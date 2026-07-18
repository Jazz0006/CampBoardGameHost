package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import kotlin.math.abs

internal data class UnreliableCategoricalCandidate(
    val id: String,
    val isTruthful: Boolean,
    val misinformationPressure: Int = 0,
) {
    init {
        require(id.isNotBlank())
        require(misinformationPressure >= 0)
    }
}

internal data class UnreliableCategoricalRecommendation(
    val candidateId: String,
    val style: RecommendationStyle,
    val totalScore: Int,
    val warningIds: List<String>,
)

/** Ranks non-numeric arbitrary information while retaining truthful information as a legal choice. */
internal object UnreliableCategoricalInformationRecommender {
    fun recommend(candidates: List<UnreliableCategoricalCandidate>): List<UnreliableCategoricalRecommendation> {
        val distinctCandidates = candidates.distinctBy(UnreliableCategoricalCandidate::id)
        require(distinctCandidates.isNotEmpty())
        val selectedIds = mutableSetOf<String>()
        return listOf(
            RecommendationStyle.GENTLE,
            RecommendationStyle.BALANCED,
            RecommendationStyle.AGGRESSIVE,
        ).mapNotNull { style ->
            distinctCandidates
                .map { candidate -> evaluate(candidate, style) }
                .sortedWith(
                    compareByDescending<UnreliableCategoricalRecommendation> { it.totalScore }
                        .thenBy(UnreliableCategoricalRecommendation::candidateId),
                )
                .firstOrNull { selectedIds.add(it.candidateId) }
        }
    }

    private fun evaluate(
        candidate: UnreliableCategoricalCandidate,
        style: RecommendationStyle,
    ): UnreliableCategoricalRecommendation {
        val score = when (style) {
            RecommendationStyle.GENTLE ->
                (if (candidate.isTruthful) 12 else 3) - candidate.misinformationPressure * 2

            RecommendationStyle.BALANCED ->
                (if (candidate.isTruthful) 2 else 10) - abs(candidate.misinformationPressure - 2) * 2

            RecommendationStyle.AGGRESSIVE ->
                (if (candidate.isTruthful) -4 else 6) + candidate.misinformationPressure * 3
        }
        return UnreliableCategoricalRecommendation(
            candidateId = candidate.id,
            style = style,
            totalScore = score,
            warningIds = buildList {
                if (!candidate.isTruthful && candidate.misinformationPressure >= 4) add("high-misinformation-pressure")
            },
        )
    }
}
