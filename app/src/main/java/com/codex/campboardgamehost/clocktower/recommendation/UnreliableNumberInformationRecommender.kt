package com.codex.campboardgamehost.clocktower.recommendation

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

internal object UnreliableNumberInformationRecommender {
    fun recommend(context: UnreliableNumberContext): List<UnreliableNumberRecommendation> {
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
        return selected
    }

    private fun evaluate(
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
}
