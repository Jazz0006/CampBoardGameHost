package com.codex.campboardgamehost

/** One player assigned to one confirmed physical seat before a game is chosen. */
internal data class HostSeatAssignment(
    val seatId: ClocktowerSeatId,
    val playerName: String,
)

/**
 * Immutable game-independent seating checkpoint.
 *
 * Player arrangement remains editable before confirmation. Once confirmed, later game selection,
 * game-specific settings, and hosted play should consume this ordered roster instead of rebuilding
 * physical seat identity from whichever list a screen happens to render.
 */
internal data class ConfirmedHostSeating(
    val seats: List<HostSeatAssignment>,
) {
    init {
        val expectedIds = (1..seats.size).map(::ClocktowerSeatId)
        require(seats.map(HostSeatAssignment::seatId) == expectedIds) {
            "Confirmed host seats must be contiguous from seat 1"
        }
        require(seats.all { it.playerName.isNotBlank() }) {
            "Confirmed host seats require non-blank player names"
        }
        require(seats.map { it.playerName }.distinct().size == seats.size) {
            "Confirmed host seating requires unique player identity"
        }
    }

    val playerNames: List<String> = seats.map(HostSeatAssignment::playerName)
}

/** Freeze the current arrangement order into stable typed physical seats. */
internal fun confirmHostSeating(playerNames: List<String>): ConfirmedHostSeating {
    val frozenNames = playerNames.map(String::trim)
    return ConfirmedHostSeating(
        seats = frozenNames.mapIndexed { index, playerName ->
            HostSeatAssignment(
                seatId = ClocktowerSeatId(index + 1),
                playerName = playerName,
            )
        },
    )
}

/** Role-free presentation used while arranging or reviewing confirmed seats before game choice. */
internal fun ConfirmedHostSeating.toHostSeatPresentations(): List<HostSeatPresentation> =
    seats.map { assignment ->
        HostSeatPresentation(
            seatId = assignment.seatId,
            playerName = assignment.playerName,
            isAlive = true,
        )
    }
