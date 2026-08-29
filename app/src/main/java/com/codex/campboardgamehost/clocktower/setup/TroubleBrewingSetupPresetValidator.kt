package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktowerDistribution

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
    INVALID_DRUNK_OPTIONS,
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

        val actualRoleIds = preset.actualRoleIds()
        presetValidate(
            actualRoleIds.size == preset.playerCount,
            code = TroubleBrewingSetupPresetValidationCode.ROLE_COUNT_MISMATCH,
            preset = preset,
        ) {
            "Preset '${preset.id}' contains ${actualRoleIds.size} actual roles for ${preset.playerCount} players."
        }

        presetValidate(
            preset.demons == listOf(IMP_EXTERNAL_ID),
            code = TroubleBrewingSetupPresetValidationCode.INVALID_DEMON,
            preset = preset,
        ) {
            "Preset '${preset.id}' must contain exactly one Demon and that Demon must be Imp."
        }

        presetValidate(
            actualRoleIds.distinct().size == actualRoleIds.size,
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

        validateComposition(preset)
        validateDrunkOptions(preset, actualRoleIds.toSet(), characterRegistry)
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
        if (includesBaron) {
            presetValidate(
                preset.playerCount !in 5..6,
                code = TroubleBrewingSetupPresetValidationCode.BARON_NOT_ALLOWED_IN_CURATED_SMALL_GAME,
                preset = preset,
            ) {
                "Curated ${preset.playerCount}-player Trouble Brewing presets must not contain Baron."
            }
        }

        val standard = clocktowerDistribution(preset.playerCount)
        val baronShift = if (includesBaron) 2 else 0
        val expectedTownsfolk = standard.getValue(ClocktowerTeam.Townsfolk) - baronShift
        val expectedOutsiders = standard.getValue(ClocktowerTeam.Outsider) + baronShift
        val expectedMinions = standard.getValue(ClocktowerTeam.Minion)
        val expectedDemons = standard.getValue(ClocktowerTeam.Demon)

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

    private fun validateDrunkOptions(
        preset: TroubleBrewingSetupPreset,
        actualRoleIds: Set<String>,
        characterRegistry: ClocktowerCharacterRegistry,
    ) {
        val hasDrunk = DRUNK_EXTERNAL_ID in preset.outsiders
        if (!hasDrunk) {
            presetValidate(
                preset.drunkAsOptions.isEmpty(),
                code = TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_OPTIONS,
                preset = preset,
            ) {
                "Preset '${preset.id}' has no Drunk but declares Drunk shown-role options."
            }
            return
        }

        val options = preset.drunkAsOptions
        presetValidate(
            options.size == 3 && options.distinct().size == 3,
            code = TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_OPTIONS,
            preset = preset,
        ) {
            "Preset '${preset.id}' must declare exactly three unique Drunk shown-role options."
        }

        options.forEach { externalId ->
            val definition = characterRegistry.findByExternalId(externalId)
            presetValidate(
                definition?.team == ClocktowerCatalogTeam.TOWNSFOLK,
                code = TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_OPTIONS,
                preset = preset,
            ) {
                "Preset '${preset.id}' Drunk option '$externalId' must resolve to a Trouble Brewing Townsfolk."
            }
            presetValidate(
                externalId !in actualRoleIds,
                code = TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_OPTIONS,
                preset = preset,
            ) {
                "Preset '${preset.id}' Drunk option '$externalId' is already an actual in-play role."
            }
        }
    }

    private fun TroubleBrewingSetupPreset.actualRoleIds(): List<String> =
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
