package com.codex.campboardgamehost.clocktower.setup

/**
 * Builds and validates the compact Trouble Brewing completion fact used by cross-game rotation.
 *
 * This record is intentionally separate from the generic committed setup: style/minion-set metadata
 * belongs to the TB diversity policy, while active-game setup recovery is owned by
 * CommittedClocktowerSetup.
 */
internal object TroubleBrewingSetupRotationRecordFactory {
    fun fromSelection(selection: TroubleBrewingSetupPresetSelection): TroubleBrewingSetupRotationRecord {
        require(selection.datasetId.isNotBlank()) { "Trouble Brewing setup selection dataset ID cannot be blank." }
        require(selection.schemaVersion > 0) { "Trouble Brewing setup selection schema version must be positive." }
        require(selection.presetId == selection.preset.id) {
            "Trouble Brewing setup selection preset provenance is inconsistent."
        }
        require(selection.playerCount == selection.preset.playerCount) {
            "Trouble Brewing setup selection player count is inconsistent."
        }

        val realNonDemonRoleIds = (
            selection.preset.townsfolk + selection.preset.outsiders + selection.preset.minions
            ).toSet()
        require(realNonDemonRoleIds.size == selection.playerCount - 1) {
            "Trouble Brewing completed setup must contain exactly playerCount - 1 unique non-Demon roles."
        }

        val hasDrunk = DRUNK_EXTERNAL_ID in selection.preset.outsiders
        if (hasDrunk) {
            require(!selection.selectedDrunkShownRole.isNullOrBlank()) {
                "Completed Trouble Brewing Drunk setup requires its selector-owned shown role."
            }
            require(selection.selectedDrunkShownRole in selection.preset.drunkAsOptions) {
                "Completed Trouble Brewing Drunk shown role must belong to the selected preset options."
            }
            require(
                selection.selectedDrunkShownRole !in realNonDemonRoleIds &&
                    selection.selectedDrunkShownRole !in selection.preset.demons,
            ) {
                "Completed Trouble Brewing Drunk shown role must not be an actual in-play role."
            }
        } else {
            require(selection.selectedDrunkShownRole == null) {
                "Completed non-Drunk Trouble Brewing setup must not carry a Drunk shown role."
            }
        }

        return TroubleBrewingSetupRotationRecord(
            datasetId = selection.datasetId,
            schemaVersion = selection.schemaVersion,
            presetId = selection.presetId,
            playerCount = selection.playerCount,
            realNonDemonRoleIds = realNonDemonRoleIds,
            minionRoleIds = selection.preset.minions.toSet(),
            primaryStyleTag = selection.preset.styleTags.firstOrNull(),
            selectedDrunkShownRole = selection.selectedDrunkShownRole,
        ).also(::validate)
    }

    fun validate(record: TroubleBrewingSetupRotationRecord) {
        require(record.datasetId.isNotBlank()) { "Trouble Brewing completion dataset ID cannot be blank." }
        require(record.schemaVersion > 0) { "Trouble Brewing completion schema version must be positive." }
        require(record.presetId.isNotBlank()) { "Trouble Brewing completion preset ID cannot be blank." }
        require(record.playerCount > 0) { "Trouble Brewing completion player count must be positive." }
        require(record.realNonDemonRoleIds.size == record.playerCount - 1) {
            "Trouble Brewing completion non-Demon role count is inconsistent."
        }
        require(record.realNonDemonRoleIds.none(String::isBlank)) {
            "Trouble Brewing completion role IDs cannot be blank."
        }
        require(record.minionRoleIds.none(String::isBlank)) {
            "Trouble Brewing completion minion role IDs cannot be blank."
        }
        require(record.minionRoleIds.all { it in record.realNonDemonRoleIds }) {
            "Trouble Brewing completion minion roles must belong to the real non-Demon role set."
        }
        record.primaryStyleTag?.let {
            require(it.isNotBlank()) { "Trouble Brewing completion primary style tag cannot be blank." }
        }

        val hasDrunk = DRUNK_EXTERNAL_ID in record.realNonDemonRoleIds
        if (hasDrunk) {
            val shownRole = requireNotNull(record.selectedDrunkShownRole) {
                "Trouble Brewing completion Drunk setup requires its committed shown role."
            }
            require(shownRole.isNotBlank()) {
                "Trouble Brewing completion Drunk shown role cannot be blank."
            }
            require(shownRole !in record.realNonDemonRoleIds) {
                "Trouble Brewing completion Drunk shown role must not be a real non-Demon role."
            }
        } else {
            require(record.selectedDrunkShownRole == null) {
                "Trouble Brewing completion without Drunk cannot carry a Drunk shown role."
            }
        }
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
