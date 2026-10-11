package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingProductionVisibleRosterCutoverTest {
    @Test
    fun `preset full visible townsfolk roster is dealt before later Drunk commitment`() {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-visible-roster",
            playerCount = 8,
            townsfolk = listOf("chef", "empath", "fortuneteller", "undertaker", "monk", "ravenkeeper"),
            outsiders = listOf("drunk"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test", complexity = "standard", styleTags = listOf("balanced"), drunk = true,
        )
        val prepared = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = TroubleBrewingSetupPresetDataset(
                schemaVersion = 3, datasetId = "tb-test", status = "test",
                declaredPoolSizes = mapOf(8 to 1),
                runtimeSelectionPolicy = TroubleBrewingRuntimeSelectionPolicy(
                    exactRepeat = "reject", similarityScope = "shown roles", roleOverlapFormula = "test",
                    lastGameMaxOverlap = mapOf(8 to 1.0), historyWeights = listOf(1.0),
                    extraSoftPenalties = emptyList(), fallback = "test",
                ), pools = mapOf(8 to listOf(preset)),
            ),
            characterRegistry = canonicalRegistry(), orderedPlayerNames = (1..8).map { "Player $it" },
            gameSeed = 7101L, recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )
        assertTrue(prepared.intermediateSetup.visibleRoster.hasDrunk)
        assertEquals(preset.townsfolk.toSet(), prepared.intermediateSetup.visibleRoster.townsfolkRoleIds.toSet())
        assertEquals(8, prepared.intermediateSetup.shownSeatAssignments.size)
        assertTrue(prepared.intermediateSetup.shownSeatAssignments.all { it.shownRoleId in preset.townsfolk + preset.minions + preset.demons })
    }

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
}
