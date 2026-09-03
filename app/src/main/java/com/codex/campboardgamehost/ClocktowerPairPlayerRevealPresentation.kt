package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/** Player-visible identity for one subject in a dedicated two-seat reveal. */
internal data class ClocktowerPairPlayerRevealSeat(
    val seatId: ClocktowerSeatId,
    val playerName: String,
)

/**
 * Sanitized presentation model for a Player Reveal that intentionally names exactly two seats.
 *
 * The model is source-agnostic: Manual and recommendation choices reach this projection only after
 * F4 has resolved them to the same player-visible payload. Seat identity comes exclusively from the
 * typed proposition and the canonical roster; localized display text is never parsed for identity.
 */
internal data class ClocktowerPairPlayerRevealPresentation(
    val displayKind: ClocktowerDisplayKind,
    val title: String,
    val primary: String,
    val footer: String?,
    val seats: List<ClocktowerPairPlayerRevealSeat>,
)

internal fun clocktowerPairPlayerRevealPresentation(
    step: ClocktowerNightStepUi,
    cards: List<PlayerCard>,
): ClocktowerPairPlayerRevealPresentation? {
    val seatNumbers = step.typedPairRevealSeatNumbers() ?: return null
    val seats = seatNumbers.map { seatNumber ->
        val card = cards.getOrNull(seatNumber - 1) ?: return null
        ClocktowerPairPlayerRevealSeat(
            seatId = ClocktowerSeatId(seatNumber),
            playerName = card.name,
        )
    }

    return ClocktowerPairPlayerRevealPresentation(
        displayKind = step.displayKind,
        title = step.displayTitle,
        primary = step.displayPrimary ?: step.tellPlayer.orEmpty(),
        footer = step.displayFooter?.takeIf { it.isNotBlank() },
        seats = seats,
    )
}

private fun ClocktowerNightStepUi.typedPairRevealSeatNumbers(): List<Int>? {
    val seats = when (displayKind) {
        ClocktowerDisplayKind.EitherOne -> {
            val anyOf = displayProposition as? InformationProposition.AnyOf ?: return null
            val roleAt = anyOf.alternatives.map { alternative ->
                alternative as? InformationProposition.RoleAt ?: return null
            }
            if (roleAt.size != 2 || roleAt.map { it.role }.distinct().size != 1) return null
            roleAt.map { it.seat }
        }

        ClocktowerDisplayKind.Number ->
            (displayProposition as? InformationProposition.NumericResult)?.subjectSeats ?: return null

        ClocktowerDisplayKind.YesNo ->
            (displayProposition as? InformationProposition.BooleanResult)?.subjectSeats ?: return null

        else -> return null
    }

    return seats
        .distinct()
        .takeIf { it.size == 2 && it.all { seat -> seat > 0 } }
        ?.sorted()
}
