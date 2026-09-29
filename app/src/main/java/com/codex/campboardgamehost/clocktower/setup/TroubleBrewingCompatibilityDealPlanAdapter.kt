package com.codex.campboardgamehost.clocktower.setup

/**
 * Temporary DLB compatibility projection for the current committed runtime.
 *
 * It is deliberately downstream of the DLB intermediate setup and must not be treated as the
 * canonical Drunk-selection owner. DLB-4 replaces this bridge with the real commit boundary.
 */
internal object TroubleBrewingCompatibilityDealPlanAdapter {
    fun fromIntermediate(
        preset: TroubleBrewingSetupPreset,
        intermediateSetup: TroubleBrewingIntermediateSetup,
        addedVisibleTownsfolkRoleId: String?,
    ): TroubleBrewingSetupDealPlan {
        require(intermediateSetup.presetId == preset.id) {
            "Trouble Brewing compatibility projection preset provenance is inconsistent."
        }
        require(intermediateSetup.playerCount == preset.playerCount) {
            "Trouble Brewing compatibility projection player count is inconsistent."
        }

        val hasDrunk = intermediateSetup.visibleRoster.hasDrunk
        if (hasDrunk) {
            require(DRUNK_EXTERNAL_ID in preset.outsiders) {
                "Trouble Brewing visible roster cannot claim Drunk without a Drunk preset."
            }
            val addedRole = requireNotNull(addedVisibleTownsfolkRoleId) {
                "Trouble Brewing Drunk compatibility projection requires the transitional added Townsfolk."
            }
            require(addedRole in preset.drunkAsOptions) {
                "Trouble Brewing compatibility Drunk identity must come from drunk_as_options."
            }
            require(addedRole in intermediateSetup.visibleRoster.townsfolkRoleIds) {
                "Trouble Brewing compatibility Drunk identity must be a visible Townsfolk."
            }
        } else {
            require(DRUNK_EXTERNAL_ID !in preset.outsiders) {
                "Trouble Brewing Drunk preset must realize a Drunk-bearing visible roster."
            }
            require(addedVisibleTownsfolkRoleId == null) {
                "Non-Drunk Trouble Brewing compatibility projection cannot carry an added Townsfolk."
            }
        }

        val assignments = intermediateSetup.shownSeatAssignments.map { shown ->
            TroubleBrewingSetupDealAssignment(
                seat = shown.seat,
                playerName = shown.playerName,
                actualRoleId = if (
                    hasDrunk && shown.shownRoleId == addedVisibleTownsfolkRoleId
                ) {
                    DRUNK_EXTERNAL_ID
                } else {
                    shown.shownRoleId
                },
                shownRoleId = shown.shownRoleId,
            )
        }

        require(assignments.count { it.actualRoleId == DRUNK_EXTERNAL_ID } == if (hasDrunk) 1 else 0) {
            "Trouble Brewing compatibility projection must produce exactly one Drunk when required."
        }
        val expectedActualRoles =
            (preset.townsfolk + preset.outsiders + preset.minions + preset.demons).sorted()
        require(assignments.map { it.actualRoleId }.sorted() == expectedActualRoles) {
            "Trouble Brewing compatibility projection must preserve the preset actual-role multiset."
        }

        return TroubleBrewingSetupDealPlan(
            datasetId = intermediateSetup.datasetId,
            schemaVersion = intermediateSetup.schemaVersion,
            presetId = intermediateSetup.presetId,
            playerCount = intermediateSetup.playerCount,
            gameSeed = intermediateSetup.gameSeed,
            selectedDrunkShownRole = addedVisibleTownsfolkRoleId,
            assignments = assignments,
        )
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
