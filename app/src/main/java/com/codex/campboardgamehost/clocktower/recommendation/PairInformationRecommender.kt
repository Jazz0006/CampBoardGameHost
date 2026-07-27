package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import kotlin.math.abs

internal enum class PairInformationRegistration {
    NONE,
    SPY_AS_GOOD_ROLE,
    RECLUSE_AS_EVIL_ROLE,
}

internal data class PairInformationCandidate(
    val id: String,
    val registration: PairInformationRegistration,
    val isTruthful: Boolean = true,
    val targetExposure: Int,
    val decoyExposure: Int,
    val discussionValue: Int,
    val misinformationPressure: Int,
    val historyPressure: Int = 0,
) {
    init {
        require(id.isNotBlank())
        require(targetExposure >= 0)
        require(decoyExposure >= 0)
        require(discussionValue >= 0)
        require(misinformationPressure >= 0)
        require(historyPressure >= 0)
    }
}

internal data class PairInformationRecommendation(
    val candidateId: String,
    val style: RecommendationStyle,
    val totalScore: Int,
    val warningIds: List<String>,
)

/** Scores a complete role + target + decoy + registration information package. */
internal object PairInformationRecommender {
    fun recommend(candidates: List<PairInformationCandidate>): List<PairInformationRecommendation> {
        val distinctCandidates = candidates.distinctBy(PairInformationCandidate::id)
        if (distinctCandidates.isEmpty()) return emptyList()
        val selectedIds = mutableSetOf<String>()
        val selected = listOf(
            RecommendationStyle.GENTLE,
            RecommendationStyle.BALANCED,
            RecommendationStyle.AGGRESSIVE,
        ).mapNotNull { style ->
            distinctCandidates
                .map { evaluate(it, style) }
                .sortedWith(
                    compareByDescending<PairInformationRecommendation> { it.totalScore }
                        .thenBy(PairInformationRecommendation::candidateId),
                )
                .firstOrNull { selectedIds.add(it.candidateId) }
        }
        val truthful = distinctCandidates.firstOrNull { it.isTruthful }
        return if (truthful != null && selected.none { it.candidateId == truthful.id }) {
            selected + evaluate(truthful, RecommendationStyle.GENTLE)
        } else {
            selected
        }
    }

    private fun evaluate(
        candidate: PairInformationCandidate,
        style: RecommendationStyle,
    ): PairInformationRecommendation {
        val specialRegistration = candidate.registration != PairInformationRegistration.NONE
        val score = when (style) {
            RecommendationStyle.GENTLE ->
                (if (candidate.isTruthful) 12 else 0) -
                    (if (specialRegistration) 8 else 0) -
                    candidate.targetExposure * 2 -
                    candidate.decoyExposure -
                    candidate.misinformationPressure * 2 -
                    candidate.historyPressure * 3

            RecommendationStyle.BALANCED ->
                (if (candidate.isTruthful) 1 else 7) +
                candidate.discussionValue * 3 -
                    abs(candidate.misinformationPressure - 2) * 2 -
                    candidate.targetExposure -
                    (if (specialRegistration) 1 else 0) -
                    candidate.historyPressure * 2

            RecommendationStyle.AGGRESSIVE ->
                (if (candidate.isTruthful) 0 else 8) +
                candidate.misinformationPressure * 4 +
                    candidate.discussionValue * 2 +
                    (if (specialRegistration) 8 else 0) +
                    candidate.historyPressure
        }
        return PairInformationRecommendation(
            candidateId = candidate.id,
            style = style,
            totalScore = score,
            warningIds = buildList {
                if (specialRegistration) add("special-registration")
                if (candidate.misinformationPressure >= 4) add("high-information-pressure")
                if (candidate.targetExposure + candidate.decoyExposure >= 8) add("critical-role-exposure")
                if (candidate.historyPressure >= 2) add("repeated-target-pressure")
            },
        )
    }
}
