package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId

/**
 * Builds and validates the compact Trouble Brewing completion fact used by cross-game rotation.
 *
 * This record is intentionally separate from the generic committed setup: style/minion-set metadata
 * belongs to the TB diversity policy, while active-game setup recovery is owned by
 * CommittedClocktowerSetup.
 */
internal object TroubleBrewingSetupRotationRecordFactory {
    /**
     * Builds the completion/rotation fact from the already-canonical DLB setup truth.
     *
     * The final committed GameState is authoritative for every starting identity; preset data
     * contributes provenance/style metadata only.
     */
    fun fromCommittedSetup(
        preparedSetup: TroubleBrewingPreparedSetup,
        committedSetup: TroubleBrewingCommittedSetupResult,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingSetupRotationRecord {
        val intermediate = preparedSetup.intermediateSetup
        val gameState = committedSetup.gameState
        val canonicalSetup = committedSetup.committedSetup

        require(preparedSetup.preset.id == intermediate.presetId) {
            "Trouble Brewing committed completion preset provenance is inconsistent."
        }
        require(preparedSetup.preset.playerCount == intermediate.playerCount) {
            "Trouble Brewing committed completion player count is inconsistent."
        }
        require(gameState.script == ClocktowerScript.TroubleBrewing.toRecommendationScriptId()) {
            "Trouble Brewing committed completion requires the Trouble Brewing script."
        }
        require(gameState.seed == intermediate.gameSeed && canonicalSetup.setupSeed == intermediate.gameSeed) {
            "Trouble Brewing committed completion seed provenance is inconsistent."
        }
        require(canonicalSetup.script == gameState.script) {
            "Trouble Brewing committed setup and GameState script must agree."
        }
        require(canonicalSetup.provenance.providerId == intermediate.datasetId) {
            "Trouble Brewing committed completion dataset provenance is inconsistent."
        }
        require(canonicalSetup.provenance.candidateId == intermediate.presetId) {
            "Trouble Brewing committed completion preset candidate provenance is inconsistent."
        }

        val players = gameState.players.sortedBy { player -> player.seat }
        require(players.size == intermediate.playerCount) {
            "Trouble Brewing committed completion must cover every player."
        }
        require(players.map { player -> player.seat } == (1..intermediate.playerCount).toList()) {
            "Trouble Brewing committed completion seats must remain in canonical order."
        }
        require(players.map { player -> player.name }.distinct().size == intermediate.playerCount) {
            "Trouble Brewing committed completion player identities must be unique."
        }
        require(players.none { player -> player.name.isBlank() }) {
            "Trouble Brewing committed completion player identities cannot be blank."
        }

        require(
            canonicalSetup.assignments.map { assignment ->
                Triple(assignment.seat, assignment.actualRole, assignment.shownRole)
            } == players.map { player ->
                Triple(
                    player.seat,
                    player.actualRole,
                    requireNotNull(player.shownRole) {
                        "Trouble Brewing committed completion requires every player to have a shown role."
                    },
                )
            },
        ) {
            "Trouble Brewing committed setup assignments must equal canonical GameState identities."
        }

        fun externalRoleId(roleId: com.codex.campboardgamehost.clocktower.domain.RoleId): String =
            requireNotNull(characterRegistry.findByRoleId(roleId)) {
                "Trouble Brewing committed role '${roleId.value}' is missing from the character registry."
            }.externalId

        val identities = players.map { player ->
            val shownRole = requireNotNull(player.shownRole) {
                "Trouble Brewing committed completion requires every player to have a shown role."
            }
            TroubleBrewingPlayerStartingIdentity(
                playerKey = player.name,
                actualRoleId = externalRoleId(player.actualRole),
                shownRoleId = externalRoleId(shownRole),
                actualRoleCategory = player.actualType.toStartingRoleCategory(),
            )
        }

        require(
            intermediate.shownSeatAssignments.map { assignment ->
                Triple(assignment.seat, assignment.playerName, assignment.shownRoleId)
            } == identities.mapIndexed { index, identity ->
                Triple(players[index].seat, identity.playerKey, identity.shownRoleId)
            },
        ) {
            "Trouble Brewing committed completion must preserve the intermediate shown-seat identities."
        }

        val drunkIdentities = identities.filter { identity ->
            identity.actualRoleId == DRUNK_EXTERNAL_ID
        }
        if (intermediate.visibleRoster.hasDrunk) {
            require(drunkIdentities.size == 1) {
                "Trouble Brewing Drunk completion requires exactly one committed Drunk."
            }
            val confirmed = requireNotNull(committedSetup.confirmedDrunkCandidate) {
                "Trouble Brewing Drunk completion requires the confirmed canonical candidate."
            }
            val drunkPlayerIndex = identities.indexOf(drunkIdentities.single())
            require(players[drunkPlayerIndex].seat == confirmed.seat) {
                "Trouble Brewing completion Drunk seat must match the confirmed candidate."
            }
            require(drunkIdentities.single().playerKey == confirmed.playerName) {
                "Trouble Brewing completion Drunk player must match the confirmed candidate."
            }
            require(drunkIdentities.single().shownRoleId == confirmed.shownRoleId) {
                "Trouble Brewing completion Drunk shown role must match the confirmed candidate."
            }
        } else {
            require(committedSetup.confirmedDrunkCandidate == null && drunkIdentities.isEmpty()) {
                "Trouble Brewing completion without Drunk cannot carry a Drunk candidate or role."
            }
        }

        val realNonDemonRoleIds = identities
            .filterNot { identity -> identity.actualRoleCategory == TroubleBrewingStartingRoleCategory.DEMON }
            .map { identity -> identity.actualRoleId }
            .toSet()
        val minionRoleIds = identities
            .filter { identity -> identity.actualRoleCategory == TroubleBrewingStartingRoleCategory.MINION }
            .map { identity -> identity.actualRoleId }
            .toSet()

        return TroubleBrewingSetupRotationRecord(
            datasetId = intermediate.datasetId,
            schemaVersion = intermediate.schemaVersion,
            presetId = intermediate.presetId,
            playerCount = intermediate.playerCount,
            realNonDemonRoleIds = realNonDemonRoleIds,
            minionRoleIds = minionRoleIds,
            primaryStyleTag = preparedSetup.preset.styleTags.firstOrNull(),
            playerStartingIdentities = identities,
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

        if (record.playerStartingIdentities.isNotEmpty()) {
            require(record.playerStartingIdentities.size == record.playerCount) {
                "Trouble Brewing completion starting identities must cover every player."
            }
            require(
                record.playerStartingIdentities.map { it.playerKey }.distinct().size == record.playerCount,
            ) {
                "Trouble Brewing completion starting identities must contain unique player keys."
            }
            val drunkStartingIdentities = record.playerStartingIdentities.filter { identity ->
                identity.actualRoleId == DRUNK_EXTERNAL_ID
            }
            require(drunkStartingIdentities.size == if (hasDrunk) 1 else 0) {
                "Trouble Brewing completion starting identities must match Drunk presence."
            }
            record.playerStartingIdentities.forEach { identity ->
                require(identity.playerKey.isNotBlank()) {
                    "Trouble Brewing completion player key cannot be blank."
                }
                require(identity.actualRoleId.isNotBlank() && identity.shownRoleId.isNotBlank()) {
                    "Trouble Brewing completion starting identity roles cannot be blank."
                }
                if (identity.actualRoleId == DRUNK_EXTERNAL_ID) {
                    require(identity.shownRoleId != identity.actualRoleId) {
                        "Trouble Brewing completion Drunk starting identity must preserve a distinct shown role."
                    }
                    require(identity.shownRoleId !in record.realNonDemonRoleIds) {
                        "Trouble Brewing completion Drunk shown role must not be a real non-Demon role."
                    }
                } else {
                    require(identity.shownRoleId == identity.actualRoleId) {
                        "Trouble Brewing completion non-Drunk starting identities must show their actual role."
                    }
                }
            }
            require(
                record.playerStartingIdentities.map { it.actualRoleId }.toSet() ==
                    record.realNonDemonRoleIds + IMP_EXTERNAL_ID,
            ) {
                "Trouble Brewing completion starting identities must preserve the completed role set."
            }
        }
    }

    private fun CharacterType.toStartingRoleCategory(): TroubleBrewingStartingRoleCategory = when (this) {
        CharacterType.TOWNSFOLK -> TroubleBrewingStartingRoleCategory.TOWNSFOLK
        CharacterType.OUTSIDER -> TroubleBrewingStartingRoleCategory.OUTSIDER
        CharacterType.MINION -> TroubleBrewingStartingRoleCategory.MINION
        CharacterType.DEMON -> TroubleBrewingStartingRoleCategory.DEMON
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
    private const val IMP_EXTERNAL_ID = "imp"
}
