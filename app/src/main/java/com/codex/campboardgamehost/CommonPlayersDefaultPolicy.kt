package com.codex.campboardgamehost

internal val DEFAULT_COMMON_PLAYER_NAMES: List<String> = listOf(
    "Alice",
    "Bob",
    "Carol",
    "David",
    "Emma",
    "Frank",
    "Grace",
    "Henry",
)

internal fun resolveInitialCommonPlayers(
    hasStoredPlayers: Boolean,
    storedPlayers: List<String>,
): List<String> = if (hasStoredPlayers) {
    storedPlayers
} else {
    DEFAULT_COMMON_PLAYER_NAMES
}
