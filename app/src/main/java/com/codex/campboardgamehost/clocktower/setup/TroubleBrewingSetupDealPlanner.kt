package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

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
        val seatOrderedRoleIds = actualRoleIds.sortedWith(
            Comparator { leftRoleId, rightRoleId ->
                val leftKey = seatOrderKey(selection, leftRoleId)
                val rightKey = seatOrderKey(selection, rightRoleId)
                val keyComparison = java.lang.Long.compareUnsigned(leftKey, rightKey)
                if (keyComparison != 0) keyComparison else leftRoleId.compareTo(rightRoleId)
            },
        )

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
                    actualRoleId = seatOrderedRoleIds[index],
                )
            },
        )
    }

    private fun seatOrderKey(
        selection: TroubleBrewingSetupPresetSelection,
        roleId: String,
    ): Long = MurmurHash3.low64Utf8(
        "tb-seat-v1|${selection.datasetId}|${selection.playerCount}|${selection.presetId}|" +
            "${selection.gameSeed}|$roleId",
    )
}
