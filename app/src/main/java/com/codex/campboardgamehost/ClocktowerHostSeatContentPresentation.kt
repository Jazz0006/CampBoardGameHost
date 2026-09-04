package com.codex.campboardgamehost

internal enum class HostSeatContentMode {
    IdentityOnly,
    StorytellerRoleDetail,
}

internal data class HostSeatContentPresentation(
    val primaryLabel: String,
    val detailLabels: List<String> = emptyList(),
)

/**
 * One Storyteller-private presentation policy for physical Host seats.
 *
 * Before roles are dealt there is no [HostSeatPresentation.actualRole], so the table is identity
 * only. Once an actual role exists, every Host table shows it automatically; phase-specific UI may
 * not accidentally hide dealt identity. Actual/shown identity remain separate lines when they
 * differ (notably the Drunk).
 */
internal fun hostSeatContentPresentation(
    seat: HostSeatPresentation,
    language: String,
): HostSeatContentPresentation {
    val primaryLabel = if (seat.isAlive) seat.playerName else "${seat.playerName} ☠"
    val actualRole = seat.actualRole
        ?: return HostSeatContentPresentation(primaryLabel = primaryLabel)
    val shownRole = seat.shownRole
    val detailLabels = when {
        shownRole == null || shownRole.roleId == actualRole.roleId -> listOf(actualRole.displayName)
        language == "en" -> listOf(
            "Actual: ${actualRole.displayName}",
            "Shown: ${shownRole.displayName}",
        )
        else -> listOf(
            "实际：${actualRole.displayName}",
            "认为：${shownRole.displayName}",
        )
    }
    return HostSeatContentPresentation(
        primaryLabel = primaryLabel,
        detailLabels = detailLabels,
    )
}

/** Converts the runtime night card into the same typed Storyteller seat used by Day. */
internal fun PlayerCard.toStorytellerHostSeatPresentation(
    seatNumber: Int,
    language: String,
): HostSeatPresentation = HostSeatPresentation(
    seatId = ClocktowerSeatId(seatNumber),
    playerName = name,
    isAlive = eliminatedRound == null,
    actualRole = clocktowerRole?.toHostRolePresentation(language),
    shownRole = clocktowerShownRole?.toHostRolePresentation(language),
    contentMode = HostSeatContentMode.StorytellerRoleDetail,
)

private fun ClocktowerRole.toHostRolePresentation(language: String): HostRolePresentation =
    HostRolePresentation(
        roleId = enName,
        displayName = if (language == "en") enName else zhName,
    )
