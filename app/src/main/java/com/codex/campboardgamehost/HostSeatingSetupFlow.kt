package com.codex.campboardgamehost

/**
 * Small game-independent state boundary for the seating-first setup sequence.
 *
 * Screen navigation may remain owned by the app router, but it must consume this state so game
 * selection cannot precede seat confirmation and later settings cannot silently rebuild seating.
 */
internal data class HostSeatingSetupFlow(
    val confirmedSeating: ConfirmedHostSeating? = null,
) {
    fun confirmSeats(playerNames: List<String>): HostSeatingSetupFlow = copy(
        confirmedSeating = confirmHostSeating(playerNames),
    )

    fun playerNamesFor(): List<String> = requireNotNull(confirmedSeating) {
        "Production start requires confirmed seating"
    }.playerNames

    fun returnToGameSelection(): HostSeatingSetupFlow {
        requireNotNull(confirmedSeating) { "Game selection requires confirmed seating" }
        return this
    }

    fun reopenSeating(): HostSeatingSetupFlow = HostSeatingSetupFlow()
}
