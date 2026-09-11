package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingSetupRotationHistory(
    val recentGames: List<TroubleBrewingSetupRotationRecord>,
) {
    companion object {
        val EMPTY = TroubleBrewingSetupRotationHistory(recentGames = emptyList())
    }
}

/**
 * Newest-first immutable snapshots of player starting identities used only for assignment rotation.
 * Empty snapshots are meaningful: a legacy/intervening completed game still occupies its recency slot.
 */
internal data class TroubleBrewingPlayerStartingIdentityHistory(
    val recentGames: List<List<TroubleBrewingPlayerStartingIdentity>>,
) {
    init {
        require(recentGames.size <= MAX_RECENT_GAMES) {
            "Trouble Brewing player rotation history supports at most $MAX_RECENT_GAMES recent games."
        }
    }

    companion object {
        const val MAX_RECENT_GAMES = 3
        val EMPTY = TroubleBrewingPlayerStartingIdentityHistory(recentGames = emptyList())
    }
}

internal data class TroubleBrewingSetupRotationRecord(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val realNonDemonRoleIds: Set<String>,
    val minionRoleIds: Set<String>,
    val primaryStyleTag: String?,
    val selectedDrunkShownRole: String?,
    val playerStartingIdentities: List<TroubleBrewingPlayerStartingIdentity> = emptyList(),
)
