package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import kotlin.math.abs

internal data class UnreliableNumberContext(
    val trueValue: Int,
    val minimumValue: Int,
    val maximumValue: Int,
    val previousShownValue: Int? = null,
    val pressureCostPerPoint: Int = 0,
) {
    init {
        require(minimumValue <= maximumValue)
        require(trueValue in minimumValue..maximumValue)
        require(previousShownValue == null || previousShownValue in minimumValue..maximumValue)
        require(pressureCostPerPoint >= 0)
    }
}

internal data class UnreliableNumberScoreItem(
    val ruleId: String,
    val delta: Int,
)

internal data class UnreliableNumberRecommendation(
    val value: Int,
    val style: RecommendationStyle,
    val totalScore: Int,
    val scoreItems: List<UnreliableNumberScoreItem>,
    val warningIds: List<String>,
)

internal object MalfunctionPolicy {
    fun generateCandidates(
        context: UnreliableNumberContext,
        generationContext: DynamicGenerationContext,
    ) = DynamicCandidateGenerator.generateNumeric(context, generationContext)

    fun generateCandidates(
        candidates: List<UnreliableCategoricalCandidate>,
        generationContext: DynamicGenerationContext,
    ) = DynamicCandidateGenerator.generateCategorical(candidates, generationContext)

    fun recommendNumber(context: UnreliableNumberContext): List<UnreliableNumberRecommendation> {
        val selected = mutableListOf<UnreliableNumberRecommendation>()
        listOf(
            RecommendationStyle.GENTLE,
            RecommendationStyle.BALANCED,
            RecommendationStyle.AGGRESSIVE,
        ).forEach { style ->
            val ranked = (context.minimumValue..context.maximumValue)
                .map { value -> evaluate(context, value, style) }
                .sortedWith(
                    compareByDescending<UnreliableNumberRecommendation> { it.totalScore }
                        .thenBy { it.value },
                )
            val diverse = ranked.firstOrNull { candidate -> selected.none { it.value == candidate.value } }
                ?: ranked.first()
            selected += diverse
        }
        if (selected.none { it.value == context.trueValue }) {
            selected += evaluate(context, context.trueValue, RecommendationStyle.GENTLE)
        }
        return selected
    }

    internal fun evaluate(
        context: UnreliableNumberContext,
        value: Int,
        style: RecommendationStyle,
    ): UnreliableNumberRecommendation {
        val items = mutableListOf<UnreliableNumberScoreItem>()
        fun score(ruleId: String, delta: Int) {
            if (delta != 0) items += UnreliableNumberScoreItem(ruleId, delta)
        }

        val distanceFromTruth = abs(value - context.trueValue)
        when (style) {
            RecommendationStyle.GENTLE -> {
                score("truth-distance", when (distanceFromTruth) { 0 -> 8; 1 -> 5; else -> -4 })
                score("extreme-pressure", -value * context.pressureCostPerPoint * 2)
            }
            RecommendationStyle.BALANCED -> {
                score("truth-distance", when (distanceFromTruth) { 0 -> 2; 1 -> 8; else -> 1 })
                score("extreme-pressure", -value * context.pressureCostPerPoint)
            }
            RecommendationStyle.AGGRESSIVE -> {
                score("truth-distance", distanceFromTruth * 6 - if (distanceFromTruth == 0) 2 else 0)
                score("extreme-pressure", -value * context.pressureCostPerPoint / 2)
            }
        }

        context.previousShownValue?.let { previous ->
            val continuityDelta = if (value == previous) {
                when (style) {
                    RecommendationStyle.GENTLE -> 6
                    RecommendationStyle.BALANCED -> 4
                    RecommendationStyle.AGGRESSIVE -> 1
                }
            } else {
                -abs(value - previous) * when (style) {
                    RecommendationStyle.GENTLE -> 3
                    RecommendationStyle.BALANCED -> 2
                    RecommendationStyle.AGGRESSIVE -> 0
                }
            }
            score("history-continuity", continuityDelta)
        }

        val warnings = buildList {
            if (value == context.maximumValue && context.trueValue == context.minimumValue) add("maximum-false-pressure")
            if (context.previousShownValue != null && abs(value - context.previousShownValue) >= 2) add("large-history-jump")
        }
        return UnreliableNumberRecommendation(
            value = value,
            style = style,
            totalScore = items.sumOf { it.delta },
            scoreItems = items,
            warningIds = warnings,
        )
    }

    fun recommendCategorical(
        candidates: List<UnreliableCategoricalCandidate>,
    ): List<UnreliableCategoricalRecommendation> {
        val distinctCandidates = candidates.distinctBy(UnreliableCategoricalCandidate::id)
        require(distinctCandidates.isNotEmpty())
        val selectedIds = mutableSetOf<String>()
        val selected = RecommendationStyle.entries.mapNotNull { style ->
            distinctCandidates
                .map { candidate -> evaluate(candidate, style) }
                .sortedWith(
                    compareByDescending<UnreliableCategoricalRecommendation> { it.totalScore }
                        .thenBy(UnreliableCategoricalRecommendation::candidateId),
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

    internal fun evaluate(
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
