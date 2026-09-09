package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric

internal data class ClocktowerChambermaidSelectionPresentation(
    val selectedTargetNames: List<String>,
    val subjectSeats: List<Int>,
) {
    val displaySecondary: String? = subjectSeats
        .joinToString("   ")
        .takeIf { it.isNotBlank() }

    fun proposition(
        sourceSeat: Int,
        value: Int,
    ): InformationProposition.NumericResult {
        require(sourceSeat > 0) { "Chambermaid actor must belong to the current seat list" }
        require(selectedTargetNames.size == 2 && subjectSeats.size == 2 && subjectSeats.distinct().size == 2) {
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
}

internal fun clocktowerChambermaidSelectionPresentation(
    cards: List<PlayerCard>,
    selection: RevalidatedTwoPlayerSelection,
): ClocktowerChambermaidSelectionPresentation {
    val selectedTargetNames = listOfNotNull(selection.first, selection.second)
    return ClocktowerChambermaidSelectionPresentation(
        selectedTargetNames = selectedTargetNames,
        subjectSeats = selectedTargetNames.mapNotNull { targetName ->
            cards.indexOfFirst { card -> card.name == targetName }
                .takeIf { it >= 0 }
                ?.plus(1)
        },
    )
}

internal fun clocktowerChambermaidDisplayProposition(
    cards: List<PlayerCard>,
    actor: PlayerCard,
    firstTargetName: String,
    secondTargetName: String,
    value: Int,
): InformationProposition.NumericResult {
    val sourceSeat = cards.indexOf(actor).plus(1)
    val presentation = clocktowerChambermaidSelectionPresentation(
        cards = cards,
        selection = RevalidatedTwoPlayerSelection(firstTargetName, secondTargetName),
    )
    return presentation.proposition(sourceSeat, value)
}
