package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry

internal enum class TroubleBrewingSetupPresetValidationCode {
    DUPLICATE_PRESET_ID,
    PLAYER_COUNT_POOL_MISMATCH,
    ROLE_COUNT_MISMATCH,
    INVALID_DEMON,
    DUPLICATE_ACTUAL_ROLE,
    UNKNOWN_ROLE_ID,
    ROLE_TEAM_MISMATCH,
    INVALID_COMPOSITION,
    BARON_NOT_ALLOWED_IN_CURATED_SMALL_GAME,
    INVALID_DRUNK_ROSTER,
}

internal class TroubleBrewingSetupPresetValidationException(
    val code: TroubleBrewingSetupPresetValidationCode,
    val presetId: String? = null,
    message: String,
) : IllegalArgumentException(message)

internal object TroubleBrewingSetupPresetValidator {
    fun validate(
        dataset: TroubleBrewingSetupPresetDataset,
        characterRegistry: ClocktowerCharacterRegistry,
    ) {
        val seenPresetIds = mutableSetOf<String>()
        dataset.pools.toSortedMap().forEach { (poolPlayerCount, presets) ->
            presets.forEach { preset ->
                validatePresetId(preset, seenPresetIds)
                validatePreset(poolPlayerCount, preset, characterRegistry)
            }
        }
    }

    private fun validatePresetId(
        preset: TroubleBrewingSetupPreset,
        seenPresetIds: MutableSet<String>,
    ) {
        presetValidate(
            seenPresetIds.add(preset.id),
            code = TroubleBrewingSetupPresetValidationCode.DUPLICATE_PRESET_ID,
            preset = preset,
        ) { "Duplicate Trouble Brewing setup preset id '${preset.id}'." }
    }

    private fun validatePreset(
        poolPlayerCount: Int,
        preset: TroubleBrewingSetupPreset,
        characterRegistry: ClocktowerCharacterRegistry,
    ) {
        presetValidate(
            preset.playerCount == poolPlayerCount,
            code = TroubleBrewingSetupPresetValidationCode.PLAYER_COUNT_POOL_MISMATCH,
            preset = preset,
        ) {
            "Preset '${preset.id}' declares ${preset.playerCount} players but belongs to pool $poolPlayerCount."
        }

        val presetRoleIds = preset.presetRoleIds()
        presetValidate(
            preset.townsfolk.size + preset.outsiders.size +
                preset.minions.size + preset.demons.size == preset.playerCount,
            code = TroubleBrewingSetupPresetValidationCode.ROLE_COUNT_MISMATCH,
            preset = preset,
        ) {
            "Preset '${preset.id}' visible roster does not contain ${preset.playerCount} roles."
        }

        presetValidate(
            preset.demons == listOf(IMP_EXTERNAL_ID),
            code = TroubleBrewingSetupPresetValidationCode.INVALID_DEMON,
            preset = preset,
        ) {
            "Preset '${preset.id}' must contain exactly one Demon and that Demon must be Imp."
        }

        presetValidate(
            presetRoleIds.distinct().size == presetRoleIds.size,
            code = TroubleBrewingSetupPresetValidationCode.DUPLICATE_ACTUAL_ROLE,
            preset = preset,
        ) {
            "Preset '${preset.id}' contains duplicate actual roles."
        }

        validateRoleCategory(
            preset = preset,
            roleIds = preset.townsfolk,
            expectedTeam = ClocktowerCatalogTeam.TOWNSFOLK,
            characterRegistry = characterRegistry,
        )
        validateRoleCategory(
            preset = preset,
            roleIds = preset.outsiders,
            expectedTeam = ClocktowerCatalogTeam.OUTSIDER,
            characterRegistry = characterRegistry,
        )
        validateRoleCategory(
            preset = preset,
            roleIds = preset.minions,
            expectedTeam = ClocktowerCatalogTeam.MINION,
            characterRegistry = characterRegistry,
        )
        validateRoleCategory(
            preset = preset,
            roleIds = preset.demons,
            expectedTeam = ClocktowerCatalogTeam.DEMON,
            characterRegistry = characterRegistry,
        )

        validateDrunkRoster(preset, characterRegistry)
        validateComposition(preset)
    }

    private fun validateRoleCategory(
        preset: TroubleBrewingSetupPreset,
        roleIds: List<String>,
        expectedTeam: ClocktowerCatalogTeam,
        characterRegistry: ClocktowerCharacterRegistry,
    ) {
        roleIds.forEach { externalId ->
            val definition = characterRegistry.findByExternalId(externalId)
                ?: presetFailure(
                    code = TroubleBrewingSetupPresetValidationCode.UNKNOWN_ROLE_ID,
                    preset = preset,
                    message = "Preset '${preset.id}' references unknown Trouble Brewing role '$externalId'.",
                )
            presetValidate(
                definition.team == expectedTeam,
                code = TroubleBrewingSetupPresetValidationCode.ROLE_TEAM_MISMATCH,
                preset = preset,
            ) {
                "Preset '${preset.id}' places '$externalId' in $expectedTeam but the canonical registry owns ${definition.team}."
            }
        }
    }

    private fun validateComposition(preset: TroubleBrewingSetupPreset) {
        val includesBaron = BARON_EXTERNAL_ID in preset.minions
        val includesDrunk = preset.drunk
        if (includesBaron) {
            presetValidate(
                preset.playerCount !in 5..6,
                code = TroubleBrewingSetupPresetValidationCode.BARON_NOT_ALLOWED_IN_CURATED_SMALL_GAME,
                preset = preset,
            ) {
                "Curated ${preset.playerCount}-player Trouble Brewing presets must not contain Baron."
            }
        }

        val standard = clocktowerSetupDistribution(preset.playerCount)
        val baronShift = if (includesBaron) 2 else 0
        val drunkShift = if (includesDrunk) 1 else 0
        val expectedTownsfolk = standard.townsfolk - baronShift + drunkShift
        val expectedOutsiders = standard.outsiders + baronShift - drunkShift
        val expectedMinions = standard.minions
        val expectedDemons = standard.demons

        val compositionMatches =
            preset.townsfolk.size == expectedTownsfolk &&
                preset.outsiders.size == expectedOutsiders &&
                preset.minions.size == expectedMinions &&
                preset.demons.size == expectedDemons
        presetValidate(
            compositionMatches,
            code = TroubleBrewingSetupPresetValidationCode.INVALID_COMPOSITION,
            preset = preset,
        ) {
            "Preset '${preset.id}' has composition " +
                "${preset.townsfolk.size}/${preset.outsiders.size}/${preset.minions.size}/${preset.demons.size}; " +
                "expected $expectedTownsfolk/$expectedOutsiders/$expectedMinions/$expectedDemons."
        }
    }

    private fun validateDrunkRoster(
        preset: TroubleBrewingSetupPreset,
        characterRegistry: ClocktowerCharacterRegistry,
    ) {
        presetValidate(
            DRUNK_EXTERNAL_ID !in preset.outsiders,
            code = TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_ROSTER,
            preset = preset,
        ) {
            "Preset '${preset.id}' must not list Drunk among displayed Outsiders."
        }
        preset.townsfolk.forEach { externalId ->
            val definition = characterRegistry.findByExternalId(externalId)
            presetValidate(
                definition?.team == ClocktowerCatalogTeam.TOWNSFOLK,
                code = TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_ROSTER,
                preset = preset,
            ) {
                "Preset '${preset.id}' townsfolk '$externalId' must resolve to a Trouble Brewing Townsfolk."
            }
        }
    }

    private fun TroubleBrewingSetupPreset.presetRoleIds(): List<String> =
        townsfolk + outsiders + minions + demons

    private const val IMP_EXTERNAL_ID = "imp"
    private const val BARON_EXTERNAL_ID = "baron"
    private const val DRUNK_EXTERNAL_ID = "drunk"
}

private fun presetFailure(
    code: TroubleBrewingSetupPresetValidationCode,
    preset: TroubleBrewingSetupPreset,
    message: String,
): Nothing = throw TroubleBrewingSetupPresetValidationException(
    code = code,
    presetId = preset.id,
    message = message,
)

private inline fun presetValidate(
    condition: Boolean,
    code: TroubleBrewingSetupPresetValidationCode,
    preset: TroubleBrewingSetupPreset,
    message: () -> String,
) {
    if (!condition) presetFailure(code, preset, message())
}
