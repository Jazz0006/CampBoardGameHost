package com.codex.campboardgamehost

/**
 * Small game-independent state boundary for the seating-first setup sequence.
 *
 * Screen navigation stays in the app router. Settings and game startup must use
 * the confirmed seat roster rather than silently rebuilding it.
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

    fun reopenSeating(): HostSeatingSetupFlow = HostSeatingSetupFlow()
}
