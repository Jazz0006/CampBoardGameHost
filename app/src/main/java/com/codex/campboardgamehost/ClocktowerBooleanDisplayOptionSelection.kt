package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * Finds the existing display option that represents exactly one validated Boolean information result.
 * Localized display strings are deliberately ignored; the typed proposition is the identity boundary.
 */
internal fun findBooleanDisplayOption(
    options: List<ClocktowerDisplayOption>,
    metric: BooleanMetric,
    sourceSeat: Int,
    subjectSeats: List<Int>,
    value: Boolean,
): ClocktowerDisplayOption? = options.firstOrNull { option ->
    val proposition = option.proposition as? InformationProposition.BooleanResult
        ?: return@firstOrNull false
    proposition.metric == metric &&
        proposition.sourceSeat == sourceSeat &&
        proposition.subjectSeats == subjectSeats &&
        proposition.value == value
}
