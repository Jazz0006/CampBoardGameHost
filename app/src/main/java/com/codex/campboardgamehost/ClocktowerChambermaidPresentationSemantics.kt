package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric

internal fun clocktowerChambermaidDisplayProposition(
    cards: List<PlayerCard>,
    actor: PlayerCard,
    firstTargetName: String,
    secondTargetName: String,
    value: Int,
): InformationProposition.NumericResult {
    val sourceSeat = cards.indexOf(actor).plus(1)
    val subjectSeats = listOf(firstTargetName, secondTargetName).map { targetName ->
        cards.indexOfFirst { card -> card.name == targetName }.plus(1)
    }
    require(sourceSeat > 0) { "Chambermaid actor must belong to the current seat list" }
    require(subjectSeats.all { it > 0 } && subjectSeats.distinct().size == 2) {
        "Chambermaid display requires two distinct current-player targets"
    }
    require(value in 0..2) { "Chambermaid displayed value must be 0, 1, or 2" }

    return InformationProposition.NumericResult(
        metric = NumericMetric.PLAYERS_WAKING_FOR_ABILITY,
        sourceSeat = sourceSeat,
        subjectSeats = subjectSeats,
        value = value,
    )
}
