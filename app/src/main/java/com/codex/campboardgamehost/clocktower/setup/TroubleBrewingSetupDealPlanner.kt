package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

internal data class TroubleBrewingSetupDealAssignment(
    val seat: Int,
    val playerName: String,
    val actualRoleId: String,
    val shownRoleId: String,
)

internal data class TroubleBrewingSetupDealPlan(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val gameSeed: Long,
    val selectedDrunkShownRole: String?,
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
        val selectedDrunkShownRole = validatedDrunkShownRole(
            selection = selection,
            actualRoleIds = actualRoleIds,
        )
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
            selectedDrunkShownRole = selectedDrunkShownRole,
            assignments = orderedPlayerNames.mapIndexed { index, playerName ->
                val actualRoleId = seatOrderedRoleIds[index]
                TroubleBrewingSetupDealAssignment(
                    seat = index + 1,
                    playerName = playerName,
                    actualRoleId = actualRoleId,
                    shownRoleId = if (actualRoleId == DRUNK_ROLE_ID) {
                        requireNotNull(selectedDrunkShownRole)
                    } else {
                        actualRoleId
                    },
                )
            },
        )
    }

    private fun validatedDrunkShownRole(
        selection: TroubleBrewingSetupPresetSelection,
        actualRoleIds: List<String>,
    ): String? {
        if (DRUNK_ROLE_ID !in actualRoleIds) {
            require(selection.selectedDrunkShownRole == null) {
                "Non-Drunk Trouble Brewing preset must not carry a Drunk shown role."
            }
            return null
        }

        val shownRoleId = requireNotNull(selection.selectedDrunkShownRole) {
            "Drunk Trouble Brewing preset requires the selector-owned shown role."
        }
        require(shownRoleId in selection.preset.drunkAsOptions) {
            "Selected Drunk shown role must come from the selected preset options."
        }
        require(shownRoleId !in actualRoleIds) {
            "Selected Drunk shown role must not already be an actual in-play role."
        }
        return shownRoleId
    }

    private fun seatOrderKey(
        selection: TroubleBrewingSetupPresetSelection,
        roleId: String,
    ): Long = MurmurHash3.low64Utf8(
        "tb-seat-v1|${selection.datasetId}|${selection.playerCount}|${selection.presetId}|" +
            "${selection.gameSeed}|$roleId",
    )

    private const val DRUNK_ROLE_ID = "drunk"
}
