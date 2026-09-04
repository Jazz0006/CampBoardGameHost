package com.codex.campboardgamehost

/**
 * Small game-independent state boundary for the seating-first setup sequence.
 *
 * Screen navigation may remain owned by the app router, but it must consume this state so game
 * selection cannot precede seat confirmation and later settings cannot silently rebuild seating.
 */
internal data class HostSeatingSetupFlow(
    val confirmedSeating: ConfirmedHostSeating? = null,
    val selectedGame: GameKind? = null,
) {
    fun confirmSeats(playerNames: List<String>): HostSeatingSetupFlow = copy(
        confirmedSeating = confirmHostSeating(playerNames),
        selectedGame = null,
    )

    fun chooseGame(game: GameKind): HostSeatingSetupFlow {
        requireNotNull(confirmedSeating) { "Seats must be confirmed before choosing a game" }
        return copy(selectedGame = game)
    }

    fun playerNamesFor(game: GameKind): List<String> {
        require(selectedGame == game) {
            "Production start requires the currently selected game"
        }
        return requireNotNull(confirmedSeating) {
            "Production start requires confirmed seating"
        }.playerNames
    }

    fun returnToGameSelection(): HostSeatingSetupFlow {
        requireNotNull(confirmedSeating) { "Game selection requires confirmed seating" }
        return copy(selectedGame = null)
    }

    fun reopenSeating(): HostSeatingSetupFlow = HostSeatingSetupFlow()
}
