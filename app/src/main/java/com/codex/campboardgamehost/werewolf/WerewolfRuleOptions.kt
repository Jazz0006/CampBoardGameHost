package com.codex.campboardgamehost

/**
 * House-rule identity is deliberately separate from Werewolf board composition.
 * Persistence/versioning of these options remains an R5.5/S4 concern.
 */
internal data class WerewolfRuleOptions(
    val lastWordsMode: LastWordsMode = LastWordsMode.FirstDay,
)
