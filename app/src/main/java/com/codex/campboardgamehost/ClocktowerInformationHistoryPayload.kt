package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository

/** The history fields for one already-authorized player information reveal. */
internal data class ClocktowerInformationHistoryPayload(
    val type: ClocktowerEventType,
    val title: String,
    val detail: String,
    val playerNames: List<String>,
)

internal fun clocktowerInformationHistoryPayload(
    displayStep: ClocktowerNightStepUi,
    orderedPlayerNames: List<String>,
    unreliable: Boolean,
    text: (String, String) -> String,
): ClocktowerInformationHistoryPayload {
    val primary = displayStep.displayPrimary ?: displayStep.tellPlayer
    val secondary = displayStep.displaySecondary
    val detail = when (displayStep.displayKind) {
        ClocktowerDisplayKind.EitherOne ->
            if (primary != null && secondary != null)
                text("$primary 在 ${secondary.trim().replace("   ", " / ")} 号之中", "$primary: seats ${secondary.trim().replace("   ", " / ")}")
            else primary.orEmpty()
        ClocktowerDisplayKind.Number ->
            if (primary != null)
                text("${displayStep.displayFooter.orEmpty()}：$primary", "${displayStep.displayFooter.orEmpty()}: $primary")
            else primary.orEmpty()
        ClocktowerDisplayKind.YesNo ->
            if (secondary != null && primary != null)
                text("查验 ${secondary.trim().replace("   ", " + ")} 号：$primary", "Checked seats ${secondary.trim().replace("   ", " + ")}: $primary")
            else primary.orEmpty()
        ClocktowerDisplayKind.RoleReveal -> primary.orEmpty()
        ClocktowerDisplayKind.Grimoire -> text("间谍查看了魔典", "Spy viewed the grimoire")
        else -> primary.orEmpty()
    }
    val referencedPlayerNames = DecisionHistoryRepository.extractSeatNumbers(
        values = listOf(displayStep.displaySecondary, displayStep.displayFooter),
        maximumSeat = orderedPlayerNames.size,
    ).mapNotNull { seat -> orderedPlayerNames.getOrNull(seat - 1) }
    val title = if (unreliable) {
        if (displayStep.selectedInformationTruthful == false) {
            text("${displayStep.displayTitle}（误导）", "${displayStep.displayTitle} (misleading)")
        } else {
            text("${displayStep.displayTitle}（不可靠）", "${displayStep.displayTitle} (unreliable)")
        }
    } else {
        displayStep.displayTitle
    }
    return ClocktowerInformationHistoryPayload(
        type = if (unreliable) ClocktowerEventType.UnreliableInformation else ClocktowerEventType.Information,
        title = title,
        detail = detail,
        playerNames = (listOfNotNull(displayStep.actor?.name) + referencedPlayerNames).distinct(),
    )
}
