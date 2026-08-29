package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class TroubleBrewingSetupPresetValidatorTest {
    @Test
    fun `final asset satisfies the semantic preset contract`() {
        val dataset = finalDataset()

        TroubleBrewingSetupPresetValidator.validate(dataset, canonicalRegistry())

        val drunkPresets = dataset.pools.values
            .flatten()
            .filter { "drunk" in it.outsiders }
        assertEquals(208, drunkPresets.size)
        assertEquals(624, drunkPresets.sumOf { it.drunkAsOptions.size })
        assertEquals(
            208,
            drunkPresets.map { it.drunkAsOptions.sorted() }.toSet().size,
        )
    }

    @Test
    fun `preset ids must be unique across the whole dataset`() {
        val first = standardSevenPlayerPreset(id = "duplicate-id")
        val second = standardSevenPlayerPreset(id = "duplicate-id").copy(
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "fortuneteller"),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.DUPLICATE_PRESET_ID) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, first, second), canonicalRegistry())
        }
    }

    @Test
    fun `preset player count must match its owning pool`() {
        val preset = standardSevenPlayerPreset().copy(playerCount = 8)

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.PLAYER_COUNT_POOL_MISMATCH) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `actual role count must equal player count`() {
        val preset = standardSevenPlayerPreset().copy(
            townsfolk = standardSevenPlayerPreset().townsfolk.dropLast(1),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.ROLE_COUNT_MISMATCH) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `demon list must contain exactly one Imp`() {
        val wrongDemon = standardSevenPlayerPreset().copy(demons = listOf("scarletwoman"))
        val twoDemons = standardSevenPlayerPreset().copy(
            townsfolk = standardSevenPlayerPreset().townsfolk.dropLast(1),
            demons = listOf("imp", "imp"),
        )

        listOf(wrongDemon, twoDemons).forEach { preset ->
            assertValidationCode(TroubleBrewingSetupPresetValidationCode.INVALID_DEMON) {
                TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
            }
        }
    }

    @Test
    fun `actual roles must be unique`() {
        val preset = standardSevenPlayerPreset().copy(
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "chef"),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.DUPLICATE_ACTUAL_ROLE) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `every role id must resolve through the canonical Trouble Brewing registry`() {
        val preset = standardSevenPlayerPreset().copy(
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "notarole"),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.UNKNOWN_ROLE_ID) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `role ids must appear in the category owned by the canonical registry`() {
        val preset = standardSevenPlayerPreset().copy(
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "poisoner"),
            minions = listOf("spy"),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.ROLE_TEAM_MISMATCH) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `non Baron presets must use the standard player count distribution`() {
        val preset = standardSevenPlayerPreset().copy(
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef"),
            outsiders = listOf("recluse"),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.INVALID_COMPOSITION) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `Baron presets apply exactly plus two Outsiders and minus two Townsfolk`() {
        val validBaronPreset = TroubleBrewingSetupPreset(
            id = "baron-seven",
            playerCount = 7,
            townsfolk = listOf("washerwoman", "librarian", "investigator"),
            outsiders = listOf("recluse", "saint"),
            minions = listOf("baron"),
            demons = listOf("imp"),
            source = "test",
            complexity = "standard",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )
        TroubleBrewingSetupPresetValidator.validate(datasetOf(7, validBaronPreset), canonicalRegistry())

        val wrongBaronPreset = validBaronPreset.copy(
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef"),
            outsiders = listOf("recluse"),
        )
        assertValidationCode(TroubleBrewingSetupPresetValidationCode.INVALID_COMPOSITION) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, wrongBaronPreset), canonicalRegistry())
        }
    }

    @Test
    fun `curated five and six player pools must not contain Baron`() {
        val fivePlayerBaronPreset = TroubleBrewingSetupPreset(
            id = "baron-five",
            playerCount = 5,
            townsfolk = listOf("washerwoman"),
            outsiders = listOf("recluse", "saint"),
            minions = listOf("baron"),
            demons = listOf("imp"),
            source = "test",
            complexity = "standard",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.BARON_NOT_ALLOWED_IN_CURATED_SMALL_GAME) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(5, fivePlayerBaronPreset), canonicalRegistry())
        }
    }

    @Test
    fun `non Drunk presets must not declare Drunk shown role options`() {
        val preset = standardSevenPlayerPreset().copy(
            drunkAsOptions = listOf("fortuneteller", "monk", "undertaker"),
        )

        assertValidationCode(TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_OPTIONS) {
            TroubleBrewingSetupPresetValidator.validate(datasetOf(7, preset), canonicalRegistry())
        }
    }

    @Test
    fun `Drunk options must be exactly three unique absent Townsfolk`() {
        val valid = drunkEightPlayerPreset()
        TroubleBrewingSetupPresetValidator.validate(datasetOf(8, valid), canonicalRegistry())

        val invalidOptions = listOf(
            valid.copy(drunkAsOptions = listOf("washerwoman", "librarian")),
            valid.copy(drunkAsOptions = listOf("washerwoman", "washerwoman", "librarian")),
            valid.copy(drunkAsOptions = listOf("washerwoman", "librarian", "recluse")),
            valid.copy(drunkAsOptions = listOf("washerwoman", "librarian", "chef")),
        )

        invalidOptions.forEach { preset ->
            assertValidationCode(TroubleBrewingSetupPresetValidationCode.INVALID_DRUNK_OPTIONS) {
                TroubleBrewingSetupPresetValidator.validate(datasetOf(8, preset), canonicalRegistry())
            }
        }
    }

    private fun finalDataset(): TroubleBrewingSetupPresetDataset {
        val asset = File("src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json")
        return TroubleBrewingSetupPresetJson.parse(asset.readText(Charsets.UTF_8))
    }

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }

    private fun datasetOf(
        playerCount: Int,
        vararg presets: TroubleBrewingSetupPreset,
    ): TroubleBrewingSetupPresetDataset = TroubleBrewingSetupPresetDataset(
        schemaVersion = 2,
        datasetId = "test-dataset",
        status = "test",
        declaredPoolSizes = mapOf(playerCount to presets.size),
        pools = mapOf(playerCount to presets.toList()),
    )

    private fun standardSevenPlayerPreset(id: String = "standard-seven"): TroubleBrewingSetupPreset =
        TroubleBrewingSetupPreset(
            id = id,
            playerCount = 7,
            townsfolk = listOf("washerwoman", "librarian", "investigator", "chef", "empath"),
            outsiders = emptyList(),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "standard",
            styleTags = emptyList(),
            drunkAsOptions = emptyList(),
        )

    private fun drunkEightPlayerPreset(): TroubleBrewingSetupPreset = TroubleBrewingSetupPreset(
        id = "drunk-eight",
        playerCount = 8,
        townsfolk = listOf("chef", "empath", "fortuneteller", "undertaker", "monk"),
        outsiders = listOf("drunk"),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "standard",
        styleTags = emptyList(),
        drunkAsOptions = listOf("washerwoman", "librarian", "investigator"),
    )

    private fun assertValidationCode(
        expected: TroubleBrewingSetupPresetValidationCode,
        block: () -> Unit,
    ) {
        val error = assertThrows(
            TroubleBrewingSetupPresetValidationException::class.java,
            block,
        )
        assertEquals(expected, error.code)
    }
}
