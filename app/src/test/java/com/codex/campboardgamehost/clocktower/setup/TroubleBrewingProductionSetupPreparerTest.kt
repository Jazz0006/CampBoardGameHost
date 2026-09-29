package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

        val intermediate = prepared.intermediateSetup
        val compatibility = prepared.compatibilityDealPlan

        assertEquals("tb-8-production-a", prepared.preset.id)
        assertEquals(dataset.datasetId, intermediate.datasetId)
        assertEquals(dataset.schemaVersion, intermediate.schemaVersion)
        assertEquals(prepared.preset.id, intermediate.presetId)
        assertEquals(8, intermediate.playerCount)
        assertEquals(6_002L, intermediate.gameSeed)
        assertEquals(players, intermediate.shownSeatAssignments.map { it.playerName })
        assertEquals((1..8).toList(), intermediate.shownSeatAssignments.map { it.seat })
        assertTrue(intermediate.visibleRoster.hasDrunk)
        assertEquals(0, intermediate.visibleRoster.visibleRoleIds.count { it == "drunk" })

        val actualRoleIds = compatibility.assignments.map { it.actualRoleId }.toSet()
        assertEquals(
            (prepared.preset.townsfolk +
                prepared.preset.outsiders +
                prepared.preset.minions +
                prepared.preset.demons).toSet(),
            actualRoleIds,
        )
        val drunk = compatibility.assignments.single { it.actualRoleId == "drunk" }
        assertEquals(compatibility.selectedDrunkShownRole, drunk.shownRoleId)
        assertTrue(drunk.shownRoleId in prepared.preset.drunkAsOptions)
    }

    @Test
    fun `production preparation feeds recent player rotation history into the deal planner`() {
        val dataset = dataset(validPreset())
        val players = (1..8).map { "Player $it" }
        val registry = canonicalRegistry()
        val seed = 6_004L
        val baseline = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = registry,
            orderedPlayerNames = players,
            gameSeed = seed,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )
        val baselineDemonHolder =
            baseline.compatibilityDealPlan.assignments.single { it.actualRoleId == "imp" }.playerName
        val twoGamesAgoDemon = TroubleBrewingPlayerStartingIdentity(
            playerKey = baselineDemonHolder,
            actualRoleId = "pukka",
            shownRoleId = "pukka",
            actualRoleCategory = TroubleBrewingStartingRoleCategory.DEMON,
        )

        val rotated = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = registry,
            orderedPlayerNames = players,
            gameSeed = seed,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
            recentPlayerStartingIdentityHistory = TroubleBrewingPlayerStartingIdentityHistory(
                recentGames = listOf(
                    emptyList(),
                    listOf(twoGamesAgoDemon),
                ),
            ),
        )

        assertNotEquals(
            baselineDemonHolder,
            rotated.compatibilityDealPlan.assignments.single { it.actualRoleId == "imp" }.playerName,
        )
        assertEquals(
            baseline.compatibilityDealPlan.assignments.map { it.actualRoleId }.sorted(),
            rotated.compatibilityDealPlan.assignments.map { it.actualRoleId }.sorted(),
        )
        assertEquals(baseline.preset, rotated.preset)
        assertEquals(baseline.intermediateSetup.visibleRoster, rotated.intermediateSetup.visibleRoster)
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
