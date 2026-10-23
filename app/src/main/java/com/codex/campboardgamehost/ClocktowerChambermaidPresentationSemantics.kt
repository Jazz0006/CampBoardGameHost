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
    selection: RevalidatedTwoPlayerSelection<Int>,
): ClocktowerChambermaidSelectionPresentation {
    val seats = listOfNotNull(selection.first, selection.second)
    return ClocktowerChambermaidSelectionPresentation(
        selectedTargetNames = seats.mapNotNull { seat -> cards.getOrNull(seat - 1)?.name },
        subjectSeats = seats,
    )
}
