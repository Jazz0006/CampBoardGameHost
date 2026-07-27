package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle

internal object AutomaticStorytellerSelector {
    fun <T> select(
        options: List<T>,
        isBalanced: (T) -> Boolean,
    ): T? = options.firstOrNull(isBalanced) ?: options.firstOrNull()

    fun <T> selectStyle(
        options: List<T>,
        style: RecommendationStyle,
        styleOf: (T) -> RecommendationStyle,
    ): T? = options.firstOrNull { styleOf(it) == style }
        ?: options.firstOrNull { styleOf(it) == RecommendationStyle.BALANCED }
        ?: options.firstOrNull()
}
