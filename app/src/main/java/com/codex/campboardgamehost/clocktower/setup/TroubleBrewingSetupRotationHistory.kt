package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingSetupRotationHistory(
    val recentGames: List<TroubleBrewingSetupRotationRecord>,
) {
    companion object {
        val EMPTY = TroubleBrewingSetupRotationHistory(recentGames = emptyList())
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
)
