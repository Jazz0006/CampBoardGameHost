package com.codex.campboardgamehost

/** Presentation-only seat number label. Typed [ClocktowerSeatId] remains the semantic authority. */
internal fun clocktowerSeatNumberLabel(
    seatNumber: Int,
    languageCode: String,
): String {
    require(seatNumber > 0) { "Seat number must be positive" }
    return if (languageCode.lowercase() == "zh") "${seatNumber}号" else "#$seatNumber"
}
