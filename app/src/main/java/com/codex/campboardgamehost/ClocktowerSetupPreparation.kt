package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.catalog.*
import com.codex.campboardgamehost.clocktower.setup.*
import com.codex.campboardgamehost.clocktower.domain.*

/** Android asset/history input and pre-session card presentation. Gameplay is owned by Session. */
internal object ClocktowerSetupPreparation {
    fun prepareTroubleBrewing(context: Context, catalog: BuiltInClocktowerRulesetCatalog,
        playerNames: List<String>, preparedSeed: Long): TroubleBrewingPreparedSetup {
        val datasetJson = context.assets
            .open("setup/trouble_brewing_setup_presets_v2_final.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val dataset = TroubleBrewingSetupPresetJson.parse(datasetJson)
        val rotationHistoryStore = TroubleBrewingSetupRotationHistoryStore.fromContext(context)
        val rotationHistory = rotationHistoryStore.historyFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            playerCount = playerNames.size,
        )
        val playerRotationHistory = rotationHistoryStore.recentPlayerStartingIdentityHistoryFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
        )
        val characterRegistry = catalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        return TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = characterRegistry,
            orderedPlayerNames = playerNames.toList(),
            gameSeed = preparedSeed,
            recentSetupRotationHistory = rotationHistory,
            recentPlayerStartingIdentityHistory = playerRotationHistory,
        )
    }

    fun shownCards(preparedSetup: TroubleBrewingPreparedSetup, language: String): List<PlayerCard> {
        return preparedSetup.intermediateSetup.shownSeatAssignments.map { assignment ->
                val role = requireNotNull(completeTroubleBrewingRoles.singleOrNull { available ->
                    available.enName.filter(Char::isLetterOrDigit).lowercase() == assignment.shownRoleId
                }) { "Missing shown-only precommit role." }
                PlayerCard(
                    name = assignment.playerName,
                                                            roleLabel = role.nameFor(language),
                    actualRoleLabel = null,
                    clocktowerTeam = role.team,
                    clocktowerRole = null, // No speculative actual Drunk identity.
                    clocktowerShownRole = role,
                )
            }
    }

    fun committedCards(committedSetup: TroubleBrewingCommittedSetupResult,
        characterRegistry: ClocktowerCharacterRegistry, context: Context, language: String): List<PlayerCard> {
        val resolvedAssignments = TroubleBrewingDealRoleResolver.resolveCommitted(
            committedSetup = committedSetup,
            characterRegistry = characterRegistry,
            availableRoles = completeTroubleBrewingRoles,
        )

        return resolvedAssignments.map { assignment ->
            val role = assignment.actualRole
            val shownRole = assignment.shownRole

            PlayerCard(
                name = assignment.playerName.ifBlank {
                    context.getString(R.string.default_player_name_format, assignment.seat)
                },
                                roleLabel = shownRole.nameFor(language),
                actualRoleLabel = role.nameFor(language),
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shownRole,
            )
        }
    }

    fun noGreaterJoyCards(preparedSetup: CommittedClocktowerSetup, script: ClocktowerScript,
        playerNames: List<String>, context: Context, language: String): List<PlayerCard> {
        val availableRolesById = clocktowerRolesForScript(script).associateBy { role -> RoleId(role.enName) }
        return preparedSetup.assignments.map { assignment ->
            val name = playerNames[assignment.seat - 1]
            val role = requireNotNull(availableRolesById[assignment.actualRole]) {
                "Committed No Greater Joy actual role '${assignment.actualRole.value}' is unavailable."
            }
            val shownRole = requireNotNull(availableRolesById[assignment.shownRole]) {
                "Committed No Greater Joy shown role '${assignment.shownRole.value}' is unavailable."
            }
            PlayerCard(
                name = name.ifBlank { context.getString(R.string.default_player_name_format, assignment.seat) },
                                roleLabel = shownRole.nameFor(language),
                actualRoleLabel = role.nameFor(language),
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shownRole,
            )
        }
    }
}
