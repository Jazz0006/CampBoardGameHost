package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingSetupPresetSelectorTest {
    @Test
    fun `selection is isolated to the requested player count and returns provenance`() {
        val dataset = TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "test-dataset",
            status = "test",
            declaredPoolSizes = mapOf(7 to 1, 8 to 1),
            runtimeSelectionPolicy = testPolicy(),
            pools = mapOf(
                7 to listOf(preset(id = "seven", playerCount = 7)),
                8 to listOf(preset(id = "eight", playerCount = 8)),
            ),
        )

        val selected = TroubleBrewingSetupPresetSelector.select(
            dataset = dataset,
            playerCount = 8,
            gameSeed = 1234L,
        )

        assertEquals("test-dataset", selected.datasetId)
        assertEquals(2, selected.schemaVersion)
        assertEquals("eight", selected.presetId)
        assertEquals(8, selected.playerCount)
        assertEquals(1234L, selected.gameSeed)
        assertEquals("eight", selected.preset.id)
        assertEquals(8, selected.preset.playerCount)
    }

    @Test
    fun `selection rejects unsupported player counts instead of falling across pools`() {
        val dataset = TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "test-dataset",
            status = "test",
            declaredPoolSizes = mapOf(7 to 1),
            runtimeSelectionPolicy = testPolicy(),
            pools = mapOf(7 to listOf(preset(id = "seven", playerCount = 7))),
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupPresetSelector.select(
                dataset = dataset,
                playerCount = 8,
                gameSeed = 55L,
            )
        }
    }

    private fun testPolicy() = TroubleBrewingRuntimeSelectionPolicy(
        exactRepeat = "reject",
        similarityScope = "test",
        roleOverlapFormula = "test",
        lastGameMaxOverlap = mapOf(7 to 0.7, 8 to 0.72),
        historyWeights = listOf(1.0, 0.65, 0.4, 0.2, 0.1),
        extraSoftPenalties = emptyList(),
        fallback = "test",
    )

    private fun preset(
        id: String,
        playerCount: Int,
    ) = TroubleBrewingSetupPreset(
        id = id,
        playerCount = playerCount,
        townsfolk = List(playerCount - 2) { index -> "town-$id-$index" },
        outsiders = emptyList(),
        minions = listOf("minion-$id"),
        demons = listOf("imp"),
        source = "test",
        complexity = "test",
        styleTags = emptyList(),
        drunkAsOptions = emptyList(),
    )
}
