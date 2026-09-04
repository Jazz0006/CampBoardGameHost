package com.codex.campboardgamehost

/**
 * Shared capacity-aware visual density for every square-table seat card.
 *
 * Identity-only and Storyteller-detail tables intentionally use the same large-card tiers. Detail
 * presence changes seat content, not geometry or typography. The tiers still preserve the proven
 * 360 x 600 table capacity while keeping player names and role/status text readable through 15
 * players.
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

@Suppress("UNUSED_PARAMETER")
internal fun clocktowerSquareTableSeatDensity(
    playerCount: Int,
    detailedSeatCards: Boolean,
): ClocktowerSquareTableSeatDensity {
    require(playerCount >= 0) { "Square-table player count cannot be negative" }

    return when {
        playerCount <= 11 -> ClocktowerSquareTableSeatDensity(
            cardWidth = 80f,
            cardHeight = 90f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 13f,
            primaryLineHeightSp = 14f,
            detailMaxLines = 2,
            detailFontSizeSp = 12f,
            detailLineHeightSp = 13f,
            horizontalPaddingDp = 4f,
            verticalPaddingDp = 3f,
        )
        playerCount <= 14 -> ClocktowerSquareTableSeatDensity(
            cardWidth = 72f,
            cardHeight = 84f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 12f,
            primaryLineHeightSp = 13f,
            detailMaxLines = 2,
            detailFontSizeSp = 11f,
            detailLineHeightSp = 12f,
            horizontalPaddingDp = 3f,
            verticalPaddingDp = 2f,
        )
        else -> ClocktowerSquareTableSeatDensity(
            cardWidth = 64f,
            cardHeight = 70f,
            primaryMaxLines = 2,
            primaryFontSizeSp = 12f,
            primaryLineHeightSp = 13f,
            detailMaxLines = 2,
            detailFontSizeSp = 11f,
            detailLineHeightSp = 12f,
            horizontalPaddingDp = 2f,
            verticalPaddingDp = 1f,
        )
    }
}
