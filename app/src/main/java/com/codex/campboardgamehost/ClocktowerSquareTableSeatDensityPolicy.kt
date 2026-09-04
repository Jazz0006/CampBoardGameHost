package com.codex.campboardgamehost

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
    require(playerCount >= 0) { "Square-table player count cannot be negative" }

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
