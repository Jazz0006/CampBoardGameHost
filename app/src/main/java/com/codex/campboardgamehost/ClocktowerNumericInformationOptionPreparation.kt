package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberRecommendation
import kotlin.math.abs

internal fun previousClocktowerUnreliableNumber(
    events: List<ClocktowerEvent>,
    title: String,
    actorName: String,
): Int? = events
    .asReversed()
    .firstOrNull { event ->
        event.type == ClocktowerEventType.UnreliableInformation &&
            actorName in event.playerNames &&
            event.title.startsWith(title)
    }
    ?.detail
    ?.let { detail ->
        val payload = when {
            "：" in detail -> detail.substringAfter("：")
            ": " in detail -> detail.substringAfter(": ")
            else -> detail
        }
        Regex("\\d+").find(payload)?.value?.toIntOrNull()
    }

internal fun clocktowerUnreliableNumberDisplayOptions(
    recommendations: List<UnreliableNumberRecommendation>,
    title: String,
    trueValue: Int,
    secondary: String?,
    footer: String,
    styleLabel: (RecommendationStyle) -> String,
    highPressureSuffix: String,
    propositionForValue: ((Int) -> InformationProposition)? = null,
): List<ClocktowerDisplayOption> = recommendations.map { recommendation ->
    val warning = highPressureSuffix.takeIf { recommendation.warningIds.isNotEmpty() }.orEmpty()
    ClocktowerDisplayOption(
        label = "${styleLabel(recommendation.style)}：${recommendation.value}$warning",
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = title,
        displayPrimary = recommendation.value.toString(),
        displaySecondary = secondary,
        displayFooter = footer,
        proposition = propositionForValue?.invoke(recommendation.value),
        recommendationStyle = recommendation.style,
        isTruthful = recommendation.value == trueValue,
        misinformationPressure = abs(recommendation.value - trueValue).coerceIn(0, 5),
        isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
        reasonCodes = recommendation.scoreItems.map { it.ruleId },
        warningCodes = recommendation.warningIds,
    )
}
