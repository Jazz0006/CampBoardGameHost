package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingSetupDealAssignment(
    val seat: Int,
    val playerName: String,
    val actualRoleId: String,
)

internal data class TroubleBrewingSetupDealPlan(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val gameSeed: Long,
    val assignments: List<TroubleBrewingSetupDealAssignment>,
)

internal object TroubleBrewingSetupDealPlanner {
    fun plan(
        selection: TroubleBrewingSetupPresetSelection,
        orderedPlayerNames: List<String>,
    ): TroubleBrewingSetupDealPlan {
        require(selection.playerCount == selection.preset.playerCount) {
            "Selected Trouble Brewing preset player count does not match selection provenance."
        }
        require(orderedPlayerNames.size == selection.playerCount) {
            "Ordered Trouble Brewing player identities must match selected preset player count."
        }

        val actualRoleIds = (
            selection.preset.townsfolk +
                selection.preset.outsiders +
                selection.preset.minions +
                selection.preset.demons
            ).sorted()
        require(actualRoleIds.size == selection.playerCount) {
            "Selected Trouble Brewing preset role count does not match player count."
        }

        return TroubleBrewingSetupDealPlan(
            datasetId = selection.datasetId,
            schemaVersion = selection.schemaVersion,
            presetId = selection.presetId,
            playerCount = selection.playerCount,
            gameSeed = selection.gameSeed,
            assignments = orderedPlayerNames.mapIndexed { index, playerName ->
                TroubleBrewingSetupDealAssignment(
                    seat = index + 1,
                    playerName = playerName,
                    actualRoleId = actualRoleIds[index],
                )
            },
        )
    }
}
