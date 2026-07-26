package com.codex.campboardgamehost.clocktower.recommendation

internal object AutomaticStorytellerSelector {
    fun <T> select(
        options: List<T>,
        isBalanced: (T) -> Boolean,
    ): T? = options.firstOrNull(isBalanced) ?: options.firstOrNull()
}
