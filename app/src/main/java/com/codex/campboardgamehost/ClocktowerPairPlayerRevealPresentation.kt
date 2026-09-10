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
 * Seat identity comes exclusively from typed data and the canonical roster. For ordinary typed
 * information this remains the epistemic proposition. Roles such as Sage may instead carry
 * presentation-only subject seats so an unreliable visual pair does not become an epistemic claim.
 * Localized display text is never parsed for identity.
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
    presentationSubjectSeats
        .takeIf { it.isNotEmpty() }
        ?.let { presentationSeats ->
            return presentationSeats
                .distinct()
                .takeIf { it.size == 2 && presentationSeats.size == 2 && it.all { seat -> seat > 0 } }
                ?.sorted()
        }

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
