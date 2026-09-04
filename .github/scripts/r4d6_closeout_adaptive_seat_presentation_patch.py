from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
POLICY = ROOT / "ClocktowerSquareTableSeatDensityPolicy.kt"
SQUARE = ROOT / "ClocktowerSquareTableUi.kt"
HOST = ROOT / "ClocktowerHostTableUi.kt"
PAIR = ROOT / "ClocktowerPairManualSelectionUi.kt"
NIGHT = ROOT / "ClocktowerNightStepUi.kt"


def replace_once(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor in {path}: found {count}\nANCHOR:\n{old}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")


if POLICY.exists():
    raise SystemExit(f"Policy file already exists: {POLICY}")
POLICY.write_text(
    '''package com.codex.campboardgamehost

/**
 * Capacity-aware visual density for one square-table seat card.
 *
 * Storyteller detail seats expand while the physical ring has room, then compact deliberately as
 * player count grows. The 15-player tier preserves the proven 360 x 600 table capacity instead of
 * silently allowing enlarged cards to overlap.
 */
internal data class ClocktowerSquareTableSeatDensity(
    val cardWidth: Float,
    val cardHeight: Float,
    val primaryMaxLines: Int,
    val primaryFontSizeSp: Float,
    val primaryLineHeightSp: Float,
    val detailMaxLines: Int,
    val detailFontSizeSp: Float,
    val detailLineHeightSp: Float,
    val horizontalPaddingDp: Float,
    val verticalPaddingDp: Float,
)

internal fun clocktowerSquareTableSeatDensity(
    playerCount: Int,
    detailedSeatCards: Boolean,
): ClocktowerSquareTableSeatDensity {
    require(playerCount > 0) { "Square-table seat density requires at least one player" }

    if (!detailedSeatCards) {
        return ClocktowerSquareTableSeatDensity(
            cardWidth = 64f,
            cardHeight = 50f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 11f,
            primaryLineHeightSp = 12f,
            detailMaxLines = 1,
            detailFontSizeSp = 9f,
            detailLineHeightSp = 10f,
            horizontalPaddingDp = 4f,
            verticalPaddingDp = 3f,
        )
    }

    return when {
        playerCount <= 11 -> ClocktowerSquareTableSeatDensity(
            cardWidth = 80f,
            cardHeight = 90f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 11f,
            primaryLineHeightSp = 12f,
            detailMaxLines = 2,
            detailFontSizeSp = 10f,
            detailLineHeightSp = 10.5f,
            horizontalPaddingDp = 4f,
            verticalPaddingDp = 3f,
        )
        playerCount <= 14 -> ClocktowerSquareTableSeatDensity(
            cardWidth = 72f,
            cardHeight = 84f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 10f,
            primaryLineHeightSp = 11f,
            detailMaxLines = 2,
            detailFontSizeSp = 9f,
            detailLineHeightSp = 9.5f,
            horizontalPaddingDp = 3f,
            verticalPaddingDp = 2f,
        )
        else -> ClocktowerSquareTableSeatDensity(
            cardWidth = 64f,
            cardHeight = 70f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 9f,
            primaryLineHeightSp = 9.5f,
            detailMaxLines = 2,
            detailFontSizeSp = 7.5f,
            detailLineHeightSp = 8f,
            horizontalPaddingDp = 2f,
            verticalPaddingDp = 1f,
        )
    }
}
''',
    encoding="utf-8",
    newline="\n",
)

replace_once(
    SQUARE,
    '''private const val HOST_TABLE_SEAT_CARD_WIDTH = 64f
private const val HOST_TABLE_SEAT_CARD_HEIGHT = 50f
private const val HOST_TABLE_DETAILED_SEAT_CARD_HEIGHT = 70f
private const val HOST_TABLE_MINIMUM_SEPARATION = 4f
''',
    '''private const val HOST_TABLE_MINIMUM_SEPARATION = 4f
''',
)

replace_once(
    SQUARE,
    '''internal fun hostTableSurfaceLayoutConstraints(
    availableWidth: Float,
    availableHeight: Float,
    detailedSeatCards: Boolean = false,
): HostTableLayoutConstraints {
    val seatCardHeight = if (detailedSeatCards) {
        HOST_TABLE_DETAILED_SEAT_CARD_HEIGHT
    } else {
        HOST_TABLE_SEAT_CARD_HEIGHT
    }
    val maximumCenterWidth = (
        availableWidth - 2f * (HOST_TABLE_SEAT_CARD_WIDTH + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)
    val maximumCenterHeight = (
        availableHeight - 2f * (seatCardHeight + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)

    return HostTableLayoutConstraints(
        availableWidth = availableWidth,
        availableHeight = availableHeight,
        seatCardWidth = HOST_TABLE_SEAT_CARD_WIDTH,
        seatCardHeight = seatCardHeight,
''',
    '''internal fun hostTableSurfaceLayoutConstraints(
    availableWidth: Float,
    availableHeight: Float,
    detailedSeatCards: Boolean = false,
    playerCount: Int = 15,
): HostTableLayoutConstraints {
    val seatDensity = clocktowerSquareTableSeatDensity(
        playerCount = playerCount,
        detailedSeatCards = detailedSeatCards,
    )
    val maximumCenterWidth = (
        availableWidth - 2f * (seatDensity.cardWidth + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)
    val maximumCenterHeight = (
        availableHeight - 2f * (seatDensity.cardHeight + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)

    return HostTableLayoutConstraints(
        availableWidth = availableWidth,
        availableHeight = availableHeight,
        seatCardWidth = seatDensity.cardWidth,
        seatCardHeight = seatDensity.cardHeight,
''',
)

replace_once(
    SQUARE,
    '''                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                    detailedSeatCards = detailedSeatCards,
''',
    '''                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                    detailedSeatCards = detailedSeatCards,
                    playerCount = seats.size,
''',
)

replace_once(
    SQUARE,
    '''        val seatCardWidth = resolvedLayout.constraints.seatCardWidth
        val seatCardHeight = resolvedLayout.constraints.seatCardHeight
''',
    '''        val seatCardWidth = resolvedLayout.constraints.seatCardWidth
        val seatCardHeight = resolvedLayout.constraints.seatCardHeight
        val seatDensity = clocktowerSquareTableSeatDensity(
            playerCount = seats.size,
            detailedSeatCards = detailedSeatCards,
        )
''',
)

replace_once(
    SQUARE,
    '''                ClocktowerSquareTableSeat(
                    seat = placement.seat,
                    interactionMode = interactionMode,
                    onSeatClick = onSeatClick,
''',
    '''                ClocktowerSquareTableSeat(
                    seat = placement.seat,
                    interactionMode = interactionMode,
                    onSeatClick = onSeatClick,
                    density = seatDensity,
''',
)

replace_once(
    SQUARE,
    '''private fun ClocktowerSquareTableSeat(
    seat: ClocktowerSquareTableSeatUiModel,
    interactionMode: ClocktowerSquareTableInteractionMode,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
''',
    '''private fun ClocktowerSquareTableSeat(
    seat: ClocktowerSquareTableSeatUiModel,
    interactionMode: ClocktowerSquareTableInteractionMode,
    onSeatClick: (String) -> Unit,
    density: ClocktowerSquareTableSeatDensity,
    modifier: Modifier = Modifier,
) {
''',
)

replace_once(
    SQUARE,
    '''    Surface(
        modifier = modifier
            .heightIn(
                min = 48.dp,
                max = if (seat.detailLabels.isEmpty()) 62.dp else HOST_TABLE_DETAILED_SEAT_CARD_HEIGHT.dp,
            )
            .then(clickModifier),
''',
    '''    Surface(
        modifier = modifier
            .heightIn(min = 48.dp, max = density.cardHeight.dp)
            .then(clickModifier),
''',
)

replace_once(
    SQUARE,
    '''            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
''',
    '''            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = density.horizontalPaddingDp.dp,
                    vertical = density.verticalPaddingDp.dp,
                ),
''',
)

replace_once(
    SQUARE,
    '''            Text(
                text = seat.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 14.sp,
''',
    '''            Text(
                text = seat.label,
                maxLines = density.primaryMaxLines,
                overflow = TextOverflow.Ellipsis,
                fontSize = density.primaryFontSizeSp.sp,
                lineHeight = density.primaryLineHeightSp.sp,
''',
)

replace_once(
    SQUARE,
    '''                Text(
                    text = detail,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
''',
    '''                Text(
                    text = detail,
                    maxLines = density.detailMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = density.detailFontSizeSp.sp,
                    lineHeight = density.detailLineHeightSp.sp,
                    fontWeight = FontWeight.Medium,
                )
''',
)

replace_once(
    HOST,
    '''                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                    detailedSeatCards = detailedSeatCards,
''',
    '''                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                    detailedSeatCards = detailedSeatCards,
                    playerCount = seats.size,
''',
)

replace_once(
    PAIR,
    '''import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
''',
    '''import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
''',
)

replace_once(
    PAIR,
    '''internal data class ClocktowerPairManualSeatUiModel(
    val seatId: String,
    val seatNumber: Int,
    val label: String,
)

''',
    '',
)

replace_once(
    PAIR,
    '''internal fun clocktowerPairManualSelectionModel(
    candidates: List<ClocktowerDisplayOption>,
): ClocktowerPairManualSelectionModel = ClocktowerPairManualSelectionModel.from(candidates)

@Composable
''',
    '''internal fun clocktowerPairManualSelectionModel(
    candidates: List<ClocktowerDisplayOption>,
): ClocktowerPairManualSelectionModel = ClocktowerPairManualSelectionModel.from(candidates)

internal fun clocktowerPairManualSquareTableSeat(
    seat: HostSeatPresentation,
    language: String,
    state: ClocktowerSquareTableSeatState,
): ClocktowerSquareTableSeatUiModel {
    val content = hostSeatContentPresentation(seat, language)
    return ClocktowerSquareTableSeatUiModel(
        seatId = seat.seatId.renderKey(),
        seatNumber = seat.seatId.number,
        label = content.primaryLabel,
        detailLabels = content.detailLabels,
        state = state,
    )
}

@Composable
''',
)

replace_once(
    PAIR,
    '''    candidates: List<ClocktowerDisplayOption>,
    seats: List<ClocktowerPairManualSeatUiModel>,
    roleLabel: (String) -> String,
''',
    '''    candidates: List<ClocktowerDisplayOption>,
    seats: List<HostSeatPresentation>,
    roleLabel: (String) -> String,
''',
)

replace_once(
    PAIR,
    '''    var selection by remember(interactionKey, candidates) {
        mutableStateOf(clocktowerPairManualSelectionModel(candidates))
    }

    Dialog(
''',
    '''    var selection by remember(interactionKey, candidates) {
        mutableStateOf(clocktowerPairManualSelectionModel(candidates))
    }
    val language = LocalContext.current.resources.configuration.locales[0].language

    Dialog(
''',
)

replace_once(
    PAIR,
    '''                seats = seats.map { seat ->
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId,
                        seatNumber = seat.seatNumber,
                        label = seat.label,
                        state = clocktowerPairManualSeatState(selection, seat.seatNumber),
                    )
                },
''',
    '''                seats = seats.map { seat ->
                    clocktowerPairManualSquareTableSeat(
                        seat = seat,
                        language = language,
                        state = clocktowerPairManualSeatState(selection, seat.seatId.number),
                    )
                },
''',
)

replace_once(
    PAIR,
    '''                onSeatClick = { seatId ->
                    val seatNumber = seats.firstOrNull { it.seatId == seatId }?.seatNumber
                    if (seatNumber != null) {
                        selection = selection.selectSeat(seatNumber)
                    }
                },
''',
    '''                onSeatClick = { seatKey ->
                    val seatNumber = seats
                        .firstOrNull { seat -> seat.seatId.renderKey() == seatKey }
                        ?.seatId
                        ?.number
                    if (seatNumber != null) {
                        selection = selection.selectSeat(seatNumber)
                    }
                },
''',
)

replace_once(
    NIGHT,
    '''                        seats = cards.mapIndexed { index, card ->
                            ClocktowerPairManualSeatUiModel(
                                seatId = "seat-${index + 1}",
                                seatNumber = index + 1,
                                label = card.name,
                            )
                        },
''',
    '''                        seats = cards.mapIndexed { index, card ->
                            card.toStorytellerHostSeatPresentation(
                                seatNumber = index + 1,
                                language = language,
                            )
                        },
''',
)
