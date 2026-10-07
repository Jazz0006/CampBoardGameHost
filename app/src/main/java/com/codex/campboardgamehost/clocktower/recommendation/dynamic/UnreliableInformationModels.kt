package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import kotlin.math.abs

/**
 * Policy-neutral legal/continuity context for numeric Storyteller information.
 *
 * The Host may attach longitudinal warnings to legal candidates, but this model carries no
 * No score, probability or preferred-answer authority is carried here.
 */
internal data class UnreliableNumberContext(
    val trueValue: Int,
    val minimumValue: Int,
    val maximumValue: Int,
    val previousShownValue: Int? = null,
    val truthfulValues: Set<Int> = setOf(trueValue),
) {
    init {
        require(minimumValue <= maximumValue)
        require(trueValue in minimumValue..maximumValue)
        require(truthfulValues.isNotEmpty())
        require(trueValue in truthfulValues)
        require(truthfulValues.all { it in minimumValue..maximumValue })
        require(previousShownValue == null || previousShownValue in minimumValue..maximumValue)
    }

    fun warningCodes(value: Int): List<String> = buildList {
        require(value in minimumValue..maximumValue)
        if (value == maximumValue && truthfulValues.all { it == minimumValue }) {
            add("maximum-false-pressure")
        }
        if (previousShownValue != null && abs(value - previousShownValue) >= 2) {
            add("large-history-jump")
        }
    }
}

/** Policy-neutral semantic candidate for categorical Storyteller information. */
internal data class UnreliableCategoricalCandidate(
    val id: String,
    val isTruthful: Boolean,
) {
    init {
        require(id.isNotBlank())
    }
}
