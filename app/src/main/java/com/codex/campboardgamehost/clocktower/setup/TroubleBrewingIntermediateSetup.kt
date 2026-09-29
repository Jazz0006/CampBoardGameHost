package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingVisibleRoster(
    val hasDrunk: Boolean,
    val townsfolkRoleIds: List<String>,
    val outsiderRoleIds: List<String>,
    val minionRoleIds: List<String>,
    val demonRoleIds: List<String>,
) {
    val visibleRoleIds: List<String> =
        townsfolkRoleIds + outsiderRoleIds + minionRoleIds + demonRoleIds

    init {
        require(visibleRoleIds.isNotEmpty()) {
            "Trouble Brewing visible roster cannot be empty."
        }
        require(visibleRoleIds.none(String::isBlank)) {
            "Trouble Brewing visible roster role IDs cannot be blank."
        }
        require(visibleRoleIds.distinct().size == visibleRoleIds.size) {
            "Trouble Brewing visible roster role IDs must be unique."
        }
        require(!hasDrunk || DRUNK_EXTERNAL_ID !in visibleRoleIds) {
            "A late-bound Trouble Brewing Drunk must not be a visible roster identity."
        }
    }

    private companion object {
        const val DRUNK_EXTERNAL_ID = "drunk"
    }
}

internal data class TroubleBrewingShownSeatAssignment(
    val seat: Int,
    val playerName: String,
    val shownRoleId: String,
) {
    init {
        require(seat > 0) { "Trouble Brewing seat numbers start at 1." }
        require(playerName.isNotBlank()) { "Trouble Brewing player name cannot be blank." }
        require(shownRoleId.isNotBlank()) { "Trouble Brewing shown role ID cannot be blank." }
    }
}

/**
 * Post-visible-roster, post-seating Trouble Brewing setup before a Drunk seat is committed.
 *
 * This contract intentionally contains shown identities only. It must not encode which visible
 * Townsfolk was introduced through drunk_as_options, an actual Drunk role, or a committed Drunk
 * seat. Those become later DLB decisions.
 */
internal data class TroubleBrewingIntermediateSetup(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val gameSeed: Long,
    val visibleRoster: TroubleBrewingVisibleRoster,
    val shownSeatAssignments: List<TroubleBrewingShownSeatAssignment>,
) {
    init {
        require(datasetId.isNotBlank()) {
            "Trouble Brewing intermediate setup dataset ID cannot be blank."
        }
        require(schemaVersion > 0) {
            "Trouble Brewing intermediate setup schema version must be positive."
        }
        require(presetId.isNotBlank()) {
            "Trouble Brewing intermediate setup preset ID cannot be blank."
        }
        require(playerCount > 0) {
            "Trouble Brewing intermediate setup player count must be positive."
        }
        require(visibleRoster.visibleRoleIds.size == playerCount) {
            "Trouble Brewing visible roster size must match player count."
        }
        require(shownSeatAssignments.size == playerCount) {
            "Trouble Brewing shown-seat assignment count must match player count."
        }
        require(shownSeatAssignments.map { it.seat } == (1..playerCount).toList()) {
            "Trouble Brewing shown seats must be ordered canonically from 1 through player count."
        }
        require(shownSeatAssignments.map { it.playerName }.distinct().size == playerCount) {
            "Trouble Brewing shown-seat player identities must be unique."
        }
        require(
            shownSeatAssignments.map { it.shownRoleId }.sorted() ==
                visibleRoster.visibleRoleIds.sorted(),
        ) {
            "Trouble Brewing shown seats must exactly cover the realized visible roster."
        }
    }
}

/**
 * Pure edge adapter from a validated Trouble Brewing preset to its realized visible roster.
 *
 * For a Drunk preset, [addedVisibleTownsfolkRoleId] is a transitional input selected from
 * drunk_as_options. It is deliberately forgotten by the returned roster so every dealt Townsfolk
 * is a peer for later Drunk candidacy.
 */
internal object TroubleBrewingVisibleRosterRealizer {
    fun realize(
        preset: TroubleBrewingSetupPreset,
        addedVisibleTownsfolkRoleId: String?,
    ): TroubleBrewingVisibleRoster {
        val actualRoleIds =
            preset.townsfolk + preset.outsiders + preset.minions + preset.demons
        require(actualRoleIds.size == preset.playerCount) {
            "Trouble Brewing preset role count must match player count."
        }
        require(actualRoleIds.none(String::isBlank)) {
            "Trouble Brewing preset role IDs cannot be blank."
        }
        require(actualRoleIds.distinct().size == actualRoleIds.size) {
            "Trouble Brewing preset actual role IDs must be unique."
        }

        val hasDrunk = DRUNK_EXTERNAL_ID in preset.outsiders
        val visibleTownsfolk = if (hasDrunk) {
            val addedRole = requireNotNull(addedVisibleTownsfolkRoleId) {
                "Trouble Brewing Drunk preset requires one added visible Townsfolk identity."
            }
            require(addedRole.isNotBlank()) {
                "Trouble Brewing added visible Townsfolk identity cannot be blank."
            }
            require(addedRole in preset.drunkAsOptions) {
                "Trouble Brewing added visible Townsfolk identity must come from drunk_as_options."
            }
            require(addedRole !in actualRoleIds) {
                "Trouble Brewing added visible Townsfolk identity must not already be an actual role."
            }
            preset.townsfolk + addedRole
        } else {
            require(addedVisibleTownsfolkRoleId == null) {
                "Trouble Brewing non-Drunk preset cannot add a visible Townsfolk identity."
            }
            preset.townsfolk
        }

        return TroubleBrewingVisibleRoster(
            hasDrunk = hasDrunk,
            townsfolkRoleIds = visibleTownsfolk,
            outsiderRoleIds = preset.outsiders.filterNot { it == DRUNK_EXTERNAL_ID },
            minionRoleIds = preset.minions,
            demonRoleIds = preset.demons,
        ).also { roster ->
            require(roster.visibleRoleIds.size == preset.playerCount) {
                "Trouble Brewing realized visible roster size must match player count."
            }
        }
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
