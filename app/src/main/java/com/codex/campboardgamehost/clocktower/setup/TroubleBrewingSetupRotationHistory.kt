package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.ScriptId

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
    val playerStartingIdentities: List<TroubleBrewingPlayerStartingIdentity> = emptyList(),
)

/**
 * Projects completed games into the shared setup-diversity contract using what players saw.
 * Actual identities remain in each rotation record for player-specific identity rotation.
 */
internal fun TroubleBrewingSetupRotationHistory.toSetupDiversityHistory(
    characterRegistry: ClocktowerCharacterRegistry,
): SetupDiversityHistory {
    return SetupDiversityHistory(
        recentSetups = recentGames.map { record ->
            TroubleBrewingSetupRotationRecordFactory.validate(record)
            require(record.playerStartingIdentities.size == record.playerCount) {
                "Trouble Brewing setup diversity history requires the completed shown identity for every seat."
            }
            val shownRoles = record.playerStartingIdentities.map { identity ->
                requireNotNull(characterRegistry.findByExternalId(identity.shownRoleId)) {
                    "Trouble Brewing setup history references unknown shown role '${identity.shownRoleId}'."
                }.id
            }.distinct()
            require(shownRoles.size == record.playerCount) {
                "Trouble Brewing completed shown roster must contain one distinct role per seat."
            }
            SetupDiversityRecord(
                script = ScriptId("trouble_brewing"),
                actualRoles = shownRoles,
            )
        },
    )
}
