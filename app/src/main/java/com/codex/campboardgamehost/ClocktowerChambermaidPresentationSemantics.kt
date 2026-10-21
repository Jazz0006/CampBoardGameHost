package com.codex.campboardgamehost

internal data class ClocktowerChambermaidSelectionPresentation(
    val selectedTargetNames: List<String>,
    val subjectSeats: List<Int>,
) {
    val displaySecondary: String? = subjectSeats
        .joinToString("   ")
        .takeIf { it.isNotBlank() }

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
