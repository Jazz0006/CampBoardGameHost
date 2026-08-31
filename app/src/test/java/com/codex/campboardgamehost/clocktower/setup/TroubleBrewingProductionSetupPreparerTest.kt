package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingProductionSetupPreparerTest {
    @Test
    fun `production preparation validates selects and materializes one committed setup transaction`() {
        val dataset = dataset(validPreset())
        val players = (1..8).map { "Player $it" }

        val prepared = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = canonicalRegistry(),
            orderedPlayerNames = players,
            gameSeed = 6_002L,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )

        assertEquals(dataset.datasetId, prepared.selection.datasetId)
        assertEquals(dataset.schemaVersion, prepared.selection.schemaVersion)
        assertEquals("tb-8-production-a", prepared.selection.presetId)
        assertEquals(8, prepared.selection.playerCount)
        assertEquals(6_002L, prepared.selection.gameSeed)
        assertEquals(prepared.selection.datasetId, prepared.dealPlan.datasetId)
        assertEquals(prepared.selection.schemaVersion, prepared.dealPlan.schemaVersion)
        assertEquals(prepared.selection.presetId, prepared.dealPlan.presetId)
        assertEquals(prepared.selection.playerCount, prepared.dealPlan.playerCount)
        assertEquals(prepared.selection.gameSeed, prepared.dealPlan.gameSeed)
        assertEquals(prepared.selection.selectedDrunkShownRole, prepared.dealPlan.selectedDrunkShownRole)
        assertEquals(players, prepared.dealPlan.assignments.map { it.playerName })
        assertEquals((1..8).toList(), prepared.dealPlan.assignments.map { it.seat })

        val actualRoleIds = prepared.dealPlan.assignments.map { it.actualRoleId }.toSet()
        assertEquals(
            (prepared.selection.preset.townsfolk +
                prepared.selection.preset.outsiders +
                prepared.selection.preset.minions +
                prepared.selection.preset.demons).toSet(),
            actualRoleIds,
        )
        val drunk = prepared.dealPlan.assignments.single { it.actualRoleId == "drunk" }
        assertEquals(prepared.selection.selectedDrunkShownRole, drunk.shownRoleId)
        assertTrue(drunk.shownRoleId in prepared.selection.preset.drunkAsOptions)
    }

    @Test
    fun `invalid preset data fails instead of producing a fallback deal`() {
        val invalid = validPreset().copy(demons = listOf("poisoner"))

        val error = assertThrows(TroubleBrewingSetupPresetValidationException::class.java) {
            TroubleBrewingProductionSetupPreparer.prepare(
                dataset = dataset(invalid),
                characterRegistry = canonicalRegistry(),
                orderedPlayerNames = (1..8).map { "Player $it" },
                gameSeed = 6_003L,
                recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
            )
        }

        assertEquals(TroubleBrewingSetupPresetValidationCode.INVALID_DEMON, error.code)
    }

    private fun validPreset(): TroubleBrewingSetupPreset = TroubleBrewingSetupPreset(
        id = "tb-8-production-a",
        playerCount = 8,
        townsfolk = listOf("chef", "empath", "fortuneteller", "undertaker", "monk"),
        outsiders = listOf("drunk"),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "standard",
        styleTags = listOf("balanced"),
        drunkAsOptions = listOf("washerwoman", "librarian", "investigator"),
    )

    private fun dataset(preset: TroubleBrewingSetupPreset): TroubleBrewingSetupPresetDataset =
        TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "test-dataset",
            status = "test",
            declaredPoolSizes = mapOf(8 to 1),
            runtimeSelectionPolicy = TroubleBrewingRuntimeSelectionPolicy(
                exactRepeat = "reject",
                similarityScope = "test",
                roleOverlapFormula = "test",
                lastGameMaxOverlap = mapOf(8 to 1.0),
                historyWeights = listOf(1.0),
                extraSoftPenalties = emptyList(),
                fallback = "test",
            ),
            pools = mapOf(8 to listOf(preset)),
        )

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
}
