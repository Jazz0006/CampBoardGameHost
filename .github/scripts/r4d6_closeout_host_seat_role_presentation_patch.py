from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
POLICY = ROOT / "ClocktowerHostSeatContentPresentation.kt"
CONTRACT = ROOT / "ClocktowerHostTableContract.kt"
PROJECTION = ROOT / "ClocktowerHostTableGameStateProjection.kt"
HOST_UI = ROOT / "ClocktowerHostTableUi.kt"
SQUARE = ROOT / "ClocktowerSquareTableUi.kt"
NIGHT_ACTION = ROOT / "ClocktowerNightActionSquareTableUi.kt"
CHAMBERMAID = ROOT / "ClocktowerChambermaidSquareTableUi.kt"
FORTUNE = ROOT / "ClocktowerFortuneTellerSquareTableUi.kt"
NIGHT_STEP = ROOT / "ClocktowerNightStepUi.kt"


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line ending in {path}")
    return raw.decode("utf-8")


def replacement(path: Path, old: str, new: str):
    return (path, old, new)


texts = {
    path: read_lf(path)
    for path in [CONTRACT, PROJECTION, HOST_UI, SQUARE, NIGHT_ACTION, CHAMBERMAID, FORTUNE, NIGHT_STEP]
}

if POLICY.exists():
    raise SystemExit(f"New policy file already exists: {POLICY}")

changes = [
    replacement(
        CONTRACT,
        """internal data class HostSeatPresentation(\n    val seatId: ClocktowerSeatId,\n    val playerName: String,\n    val isAlive: Boolean,\n    val actualRole: HostRolePresentation? = null,\n    val shownRole: HostRolePresentation? = null,\n)\n""",
        """internal data class HostSeatPresentation(\n    val seatId: ClocktowerSeatId,\n    val playerName: String,\n    val isAlive: Boolean,\n    val actualRole: HostRolePresentation? = null,\n    val shownRole: HostRolePresentation? = null,\n    val contentMode: HostSeatContentMode = HostSeatContentMode.IdentityOnly,\n)\n""",
    ),
    replacement(
        PROJECTION,
        """            shownRole = player.shownRole?.let { shownRole ->\n                HostRolePresentation(\n                    roleId = shownRole.value,\n                    displayName = roleDisplayName(shownRole),\n                )\n            },\n        )\n""",
        """            shownRole = player.shownRole?.let { shownRole ->\n                HostRolePresentation(\n                    roleId = shownRole.value,\n                    displayName = roleDisplayName(shownRole),\n                )\n            },\n            contentMode = HostSeatContentMode.StorytellerRoleDetail,\n        )\n""",
    ),
    replacement(
        HOST_UI,
        """import androidx.compose.ui.Modifier\n""",
        """import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalContext\n""",
    ),
    replacement(
        HOST_UI,
        """    BoxWithConstraints(modifier = modifier) {\n        val availableWidth = maxWidth.value\n        val availableHeight = maxHeight.value\n        val layout = remember(availableWidth, availableHeight, seats.size) {\n            hostTableLayout(\n                playerCount = seats.size,\n                constraints = hostTableSurfaceLayoutConstraints(\n                    availableWidth = availableWidth,\n                    availableHeight = availableHeight,\n                ),\n            )\n        }\n""",
        """    BoxWithConstraints(modifier = modifier) {\n        val availableWidth = maxWidth.value\n        val availableHeight = maxHeight.value\n        val language = LocalContext.current.resources.configuration.locales[0].language\n        val detailedSeatCards = seats.any { seat ->\n            hostSeatContentPresentation(seat, language).detailLabels.isNotEmpty()\n        }\n        val layout = remember(availableWidth, availableHeight, seats.size, detailedSeatCards) {\n            hostTableLayout(\n                playerCount = seats.size,\n                constraints = hostTableSurfaceLayoutConstraints(\n                    availableWidth = availableWidth,\n                    availableHeight = availableHeight,\n                    detailedSeatCards = detailedSeatCards,\n                ),\n            )\n        }\n""",
    ),
    replacement(
        HOST_UI,
        """            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n                interactionMode = interaction.mode,\n                badge = seatBadge(frame.seat),\n            )\n""",
        """            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n                interactionMode = interaction.mode,\n                badge = seatBadge(frame.seat),\n                language = language,\n            )\n""",
    ),
    replacement(
        HOST_UI,
        """private fun HostTableSeatFrame.toSquareTableSeatUiModel(\n    motionKey: String,\n    neutralSelectionChrome: Boolean,\n    interactionMode: HostTableInteractionMode,\n    badge: String?,\n): ClocktowerSquareTableSeatUiModel =\n    ClocktowerSquareTableSeatUiModel(\n        seatId = seat.seatId.renderKey(),\n        seatNumber = seat.seatId.number,\n        label = hostTablePrimarySeatLabel(seat),\n        state = if (neutralSelectionChrome) {\n            ClocktowerSquareTableSeatState.Neutral\n        } else {\n            squareTableSeatState(interactionMode)\n        },\n        isInteractionEnabled = isSelectable && !isLocked,\n        motionKey = motionKey,\n        badge = badge,\n    )\n""",
        """private fun HostTableSeatFrame.toSquareTableSeatUiModel(\n    motionKey: String,\n    neutralSelectionChrome: Boolean,\n    interactionMode: HostTableInteractionMode,\n    badge: String?,\n    language: String,\n): ClocktowerSquareTableSeatUiModel {\n    val content = hostSeatContentPresentation(seat, language)\n    return ClocktowerSquareTableSeatUiModel(\n        seatId = seat.seatId.renderKey(),\n        seatNumber = seat.seatId.number,\n        label = content.primaryLabel,\n        detailLabels = content.detailLabels,\n        state = if (neutralSelectionChrome) {\n            ClocktowerSquareTableSeatState.Neutral\n        } else {\n            squareTableSeatState(interactionMode)\n        },\n        isInteractionEnabled = isSelectable && !isLocked,\n        motionKey = motionKey,\n        badge = badge,\n    )\n}\n""",
    ),
    replacement(
        HOST_UI,
        """/**\n * Foundation density policy. Role detail stays typed on [HostSeatPresentation] and will be surfaced\n * mode-by-mode during migration instead of being re-derived from localized labels.\n */\nprivate fun hostTablePrimarySeatLabel(seat: HostSeatPresentation): String =\n    if (seat.isAlive) seat.playerName else \"${seat.playerName} ☠\"\n""",
        """""",
    ),
    replacement(
        SQUARE,
        """internal data class ClocktowerSquareTableSeatUiModel(\n    val seatId: String,\n    val seatNumber: Int,\n    val label: String,\n""",
        """internal data class ClocktowerSquareTableSeatUiModel(\n    val seatId: String,\n    val seatNumber: Int,\n    val label: String,\n    val detailLabels: List<String> = emptyList(),\n""",
    ),
    replacement(
        SQUARE,
        """private const val HOST_TABLE_SEAT_CARD_WIDTH = 64f\nprivate const val HOST_TABLE_SEAT_CARD_HEIGHT = 50f\nprivate const val HOST_TABLE_MINIMUM_SEPARATION = 4f\n""",
        """private const val HOST_TABLE_SEAT_CARD_WIDTH = 64f\nprivate const val HOST_TABLE_SEAT_CARD_HEIGHT = 50f\nprivate const val HOST_TABLE_DETAILED_SEAT_CARD_HEIGHT = 70f\nprivate const val HOST_TABLE_MINIMUM_SEPARATION = 4f\n""",
    ),
    replacement(
        SQUARE,
        """internal fun hostTableSurfaceLayoutConstraints(\n    availableWidth: Float,\n    availableHeight: Float,\n): HostTableLayoutConstraints {\n    val maximumCenterWidth = (\n""",
        """internal fun hostTableSurfaceLayoutConstraints(\n    availableWidth: Float,\n    availableHeight: Float,\n    detailedSeatCards: Boolean = false,\n): HostTableLayoutConstraints {\n    val seatCardHeight = if (detailedSeatCards) {\n        HOST_TABLE_DETAILED_SEAT_CARD_HEIGHT\n    } else {\n        HOST_TABLE_SEAT_CARD_HEIGHT\n    }\n    val maximumCenterWidth = (\n""",
    ),
    replacement(
        SQUARE,
        """    val maximumCenterHeight = (\n        availableHeight - 2f * (HOST_TABLE_SEAT_CARD_HEIGHT + HOST_TABLE_MINIMUM_SEPARATION)\n        ).coerceAtLeast(0f)\n""",
        """    val maximumCenterHeight = (\n        availableHeight - 2f * (seatCardHeight + HOST_TABLE_MINIMUM_SEPARATION)\n        ).coerceAtLeast(0f)\n""",
    ),
    replacement(
        SQUARE,
        """        seatCardWidth = HOST_TABLE_SEAT_CARD_WIDTH,\n        seatCardHeight = HOST_TABLE_SEAT_CARD_HEIGHT,\n""",
        """        seatCardWidth = HOST_TABLE_SEAT_CARD_WIDTH,\n        seatCardHeight = seatCardHeight,\n""",
    ),
    replacement(
        SQUARE,
        """        val density = LocalDensity.current\n        val resolvedLayout = layout ?: remember(availableWidth, availableHeight, seats.size) {\n            hostTableLayout(\n                playerCount = seats.size,\n                constraints = hostTableSurfaceLayoutConstraints(\n                    availableWidth = availableWidth,\n                    availableHeight = availableHeight,\n                ),\n            )\n        }\n""",
        """        val density = LocalDensity.current\n        val detailedSeatCards = seats.any { seat -> seat.detailLabels.isNotEmpty() }\n        val resolvedLayout = layout ?: remember(\n            availableWidth,\n            availableHeight,\n            seats.size,\n            detailedSeatCards,\n        ) {\n            hostTableLayout(\n                playerCount = seats.size,\n                constraints = hostTableSurfaceLayoutConstraints(\n                    availableWidth = availableWidth,\n                    availableHeight = availableHeight,\n                    detailedSeatCards = detailedSeatCards,\n                ),\n            )\n        }\n""",
    ),
    replacement(
        SQUARE,
        """        modifier = modifier\n            .heightIn(min = 48.dp, max = 62.dp)\n            .then(clickModifier),\n""",
        """        modifier = modifier\n            .heightIn(\n                min = 48.dp,\n                max = if (seat.detailLabels.isEmpty()) 62.dp else HOST_TABLE_DETAILED_SEAT_CARD_HEIGHT.dp,\n            )\n            .then(clickModifier),\n""",
    ),
    replacement(
        SQUARE,
        """            Text(\n                text = seat.label,\n                maxLines = 1,\n                overflow = TextOverflow.Ellipsis,\n                fontSize = 12.sp,\n                lineHeight = 14.sp,\n                fontWeight = if (seat.state in setOf(\n                        ClocktowerSquareTableSeatState.SelectedFirst,\n                        ClocktowerSquareTableSeatState.SelectedSecond,\n                        ClocktowerSquareTableSeatState.Selected,\n                        ClocktowerSquareTableSeatState.SelectedHighlighted,\n                        ClocktowerSquareTableSeatState.HighlightedInformation,\n                    )\n                ) {\n                    FontWeight.Black\n                } else {\n                    FontWeight.SemiBold\n                },\n            )\n        }\n""",
        """            Text(\n                text = seat.label,\n                maxLines = 1,\n                overflow = TextOverflow.Ellipsis,\n                fontSize = 12.sp,\n                lineHeight = 14.sp,\n                fontWeight = if (seat.state in setOf(\n                        ClocktowerSquareTableSeatState.SelectedFirst,\n                        ClocktowerSquareTableSeatState.SelectedSecond,\n                        ClocktowerSquareTableSeatState.Selected,\n                        ClocktowerSquareTableSeatState.SelectedHighlighted,\n                        ClocktowerSquareTableSeatState.HighlightedInformation,\n                    )\n                ) {\n                    FontWeight.Black\n                } else {\n                    FontWeight.SemiBold\n                },\n            )\n            seat.detailLabels.forEach { detail ->\n                Text(\n                    text = detail,\n                    maxLines = 1,\n                    overflow = TextOverflow.Ellipsis,\n                    fontSize = 9.sp,\n                    lineHeight = 10.sp,\n                    fontWeight = FontWeight.Medium,\n                )\n            }\n        }\n""",
    ),
    replacement(
        NIGHT_ACTION,
        """internal data class ClocktowerNightActionSeatUiModel(\n    val seatId: String,\n    val seatNumber: Int,\n    val label: String,\n)\n\n""",
        """""",
    ),
    replacement(
        NIGHT_ACTION,
        """internal fun ClocktowerSingleTargetSquareTableDialog(\n    seats: List<ClocktowerNightActionSeatUiModel>,\n""",
        """internal fun ClocktowerSingleTargetSquareTableDialog(\n    seats: List<HostSeatPresentation>,\n""",
    ),
    replacement(
        NIGHT_ACTION,
        """    ClocktowerNightActionSquareTableDialog(\n        seats = seats,\n        enabled = enabled,\n        seatState = { seatNumber ->\n""",
        """    ClocktowerNightActionSquareTableDialog(\n        seats = seats,\n        enabled = enabled,\n        language = language,\n        seatState = { seatNumber ->\n""",
    ),
    replacement(
        NIGHT_ACTION,
        """internal fun ClocktowerNightActionSquareTableDialog(\n    seats: List<ClocktowerNightActionSeatUiModel>,\n    enabled: Boolean,\n    seatState: (Int) -> ClocktowerSquareTableSeatState,\n""",
        """internal fun ClocktowerNightActionSquareTableDialog(\n    seats: List<HostSeatPresentation>,\n    enabled: Boolean,\n    language: String,\n    seatState: (Int) -> ClocktowerSquareTableSeatState,\n""",
    ),
    replacement(
        NIGHT_ACTION,
        """            ClocktowerSquareTableSeatSurface(\n                seats = seats.map { seat ->\n                    ClocktowerSquareTableSeatUiModel(\n                        seatId = seat.seatId,\n                        seatNumber = seat.seatNumber,\n                        label = seat.label,\n                        state = seatState(seat.seatNumber),\n                    )\n                },\n""",
        """            ClocktowerSquareTableSeatSurface(\n                seats = seats.map { seat ->\n                    val content = hostSeatContentPresentation(seat, language)\n                    ClocktowerSquareTableSeatUiModel(\n                        seatId = seat.seatId.renderKey(),\n                        seatNumber = seat.seatId.number,\n                        label = content.primaryLabel,\n                        detailLabels = content.detailLabels,\n                        state = seatState(seat.seatId.number),\n                    )\n                },\n""",
    ),
    replacement(
        NIGHT_ACTION,
        """                onSeatClick = { seatId ->\n                    seats.firstOrNull { it.seatId == seatId }\n                        ?.seatNumber\n                        ?.let(onSeatSelected)\n                },\n""",
        """                onSeatClick = { renderKey ->\n                    seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }\n                        ?.seatId\n                        ?.number\n                        ?.let(onSeatSelected)\n                },\n""",
    ),
    replacement(
        CHAMBERMAID,
        """internal fun ClocktowerChambermaidSquareTableDialog(\n    seats: List<ClocktowerNightActionSeatUiModel>,\n""",
        """internal fun ClocktowerChambermaidSquareTableDialog(\n    seats: List<HostSeatPresentation>,\n""",
    ),
    replacement(
        CHAMBERMAID,
        """    ClocktowerNightActionSquareTableDialog(\n        seats = seats,\n        enabled = enabled,\n        seatState = { seatNumber ->\n""",
        """    ClocktowerNightActionSquareTableDialog(\n        seats = seats,\n        enabled = enabled,\n        language = language,\n        seatState = { seatNumber ->\n""",
    ),
    replacement(
        FORTUNE,
        """internal data class ClocktowerFortuneTellerSeatUiModel(\n    val seatId: String,\n    val seatNumber: Int,\n    val label: String,\n)\n\n""",
        """""",
    ),
    replacement(
        FORTUNE,
        """internal fun ClocktowerFortuneTellerSquareTableDialog(\n    seats: List<ClocktowerFortuneTellerSeatUiModel>,\n""",
        """internal fun ClocktowerFortuneTellerSquareTableDialog(\n    seats: List<HostSeatPresentation>,\n""",
    ),
    replacement(
        FORTUNE,
        """            ClocktowerSquareTableSeatSurface(\n                seats = seats.map { seat ->\n                    ClocktowerSquareTableSeatUiModel(\n                        seatId = seat.seatId,\n                        seatNumber = seat.seatNumber,\n                        label = seat.label,\n                        state = clocktowerFortuneTellerSeatState(\n                            seatNumber = seat.seatNumber,\n                            selectedSeats = selectedSeats,\n                            selectableSeats = if (enabled) selectableSeats else emptySet(),\n                        ),\n                    )\n                },\n""",
        """            ClocktowerSquareTableSeatSurface(\n                seats = seats.map { seat ->\n                    val content = hostSeatContentPresentation(seat, language)\n                    ClocktowerSquareTableSeatUiModel(\n                        seatId = seat.seatId.renderKey(),\n                        seatNumber = seat.seatId.number,\n                        label = content.primaryLabel,\n                        detailLabels = content.detailLabels,\n                        state = clocktowerFortuneTellerSeatState(\n                            seatNumber = seat.seatId.number,\n                            selectedSeats = selectedSeats,\n                            selectableSeats = if (enabled) selectableSeats else emptySet(),\n                        ),\n                    )\n                },\n""",
    ),
    replacement(
        FORTUNE,
        """                onSeatClick = { seatId ->\n                    seats.firstOrNull { it.seatId == seatId }\n                        ?.seatNumber\n                        ?.let(onSeatSelected)\n                },\n""",
        """                onSeatClick = { renderKey ->\n                    seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }\n                        ?.seatId\n                        ?.number\n                        ?.let(onSeatSelected)\n                },\n""",
    ),
    replacement(
        NIGHT_STEP,
        """        val nightActionSeats = cards.mapIndexed { index, card ->\n            ClocktowerNightActionSeatUiModel(\n                seatId = \"seat-${index + 1}\",\n                seatNumber = index + 1,\n                label = card.name,\n            )\n        }\n""",
        """        val nightActionSeats = cards.mapIndexed { index, card ->\n            card.toStorytellerHostSeatPresentation(\n                seatNumber = index + 1,\n                language = language,\n            )\n        }\n""",
    ),
    replacement(
        NIGHT_STEP,
        """                ClocktowerFortuneTellerSquareTableDialog(\n                    seats = cards.mapIndexed { index, card ->\n                        ClocktowerFortuneTellerSeatUiModel(\n                            seatId = \"seat-${index + 1}\",\n                            seatNumber = index + 1,\n                            label = card.name,\n                        )\n                    },\n                    selectedSeats = fortuneTellerSelectedSeats,\n""",
        """                ClocktowerFortuneTellerSquareTableDialog(\n                    seats = nightActionSeats,\n                    selectedSeats = fortuneTellerSelectedSeats,\n""",
    ),
]

# Validate every exact anchor before mutating any file.
for path, old, _ in changes:
    count = texts[path].count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor in {path}, found {count}: {old[:100]!r}")

for path, old, new in changes:
    texts[path] = texts[path].replace(old, new, 1)

policy_text = """package com.codex.campboardgamehost

internal enum class HostSeatContentMode {
    IdentityOnly,
    StorytellerRoleDetail,
}

internal data class HostSeatContentPresentation(
    val primaryLabel: String,
    val detailLabels: List<String> = emptyList(),
)

/**
 * One fail-closed presentation policy for physical Host seats.
 *
 * Identity-only callers never expose role data merely because richer typed seat data becomes
 * available later. Storyteller surfaces opt in explicitly and preserve actual/shown identity as
 * separate lines when they differ (notably the Drunk).
 */
internal fun hostSeatContentPresentation(
    seat: HostSeatPresentation,
    language: String,
): HostSeatContentPresentation {
    val primaryLabel = if (seat.isAlive) seat.playerName else "${seat.playerName} ☠"
    if (seat.contentMode != HostSeatContentMode.StorytellerRoleDetail) {
        return HostSeatContentPresentation(primaryLabel = primaryLabel)
    }

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
"""

# Stable postconditions before writing.
assert "val contentMode: HostSeatContentMode = HostSeatContentMode.IdentityOnly" in texts[CONTRACT]
assert "contentMode = HostSeatContentMode.StorytellerRoleDetail" in texts[PROJECTION]
assert "detailLabels = content.detailLabels" in texts[HOST_UI]
assert "val detailLabels: List<String> = emptyList()" in texts[SQUARE]
assert "detailedSeatCards = detailedSeatCards" in texts[SQUARE]
assert "seats: List<HostSeatPresentation>" in texts[NIGHT_ACTION]
assert "ClocktowerNightActionSeatUiModel" not in texts[NIGHT_ACTION]
assert "ClocktowerFortuneTellerSeatUiModel" not in texts[FORTUNE]
assert "card.toStorytellerHostSeatPresentation" in texts[NIGHT_STEP]
assert "ClocktowerFortuneTellerSeatUiModel" not in texts[NIGHT_STEP]

for path, text in texts.items():
    path.write_text(text, encoding="utf-8", newline="\n")
POLICY.write_text(policy_text, encoding="utf-8", newline="\n")
