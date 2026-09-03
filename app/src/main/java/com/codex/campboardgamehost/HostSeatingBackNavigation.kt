package com.codex.campboardgamehost

/** The two setup-flow surfaces where Back has seating-specific semantics. */
internal enum class HostSeatingBackOrigin {
    GameSelection,
    GameSettings,
}

/** Destinations owned by the game-independent seating setup flow. */
internal enum class HostSeatingSetupDestination {
    Seating,
    GameSelection,
}

/** Pure result consumed by both visible Back/Edit actions and Android system Back. */
internal data class HostSeatingBackTransition(
    val flow: HostSeatingSetupFlow,
    val destination: HostSeatingSetupDestination,
)

internal fun hostSeatingBackTransition(
    flow: HostSeatingSetupFlow,
    origin: HostSeatingBackOrigin,
): HostSeatingBackTransition = when (origin) {
    HostSeatingBackOrigin.GameSelection -> HostSeatingBackTransition(
        flow = flow.reopenSeating(),
        destination = HostSeatingSetupDestination.Seating,
    )

    HostSeatingBackOrigin.GameSettings -> HostSeatingBackTransition(
        flow = flow.returnToGameSelection(),
        destination = HostSeatingSetupDestination.GameSelection,
    )
}
