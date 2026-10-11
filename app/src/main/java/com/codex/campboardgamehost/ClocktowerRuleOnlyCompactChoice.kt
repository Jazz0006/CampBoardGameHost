package com.codex.campboardgamehost

/**
 * Legacy compact presentation is permitted to commit only a genuinely unique legal result.
 * An initial highlight, sorting preference, or advisory label never authorizes multi-choice auto selection.
 */
internal fun <T> clocktowerRuleOnlyCompactChoice(legalChoices: List<T>): T? = legalChoices.singleOrNull()
