package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingRuntimeSelectionPolicy
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetDataset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingSetupProvenancePersistenceTest {
    @Test
    fun `committed Trouble Brewing selection round trips without rerunning selection`() {
        val dataset = dataset()
        val selection = selection(dataset)
        val root = JSONObject().apply {
            put(
                TroubleBrewingSetupProvenancePersistence.ROOT_KEY,
                TroubleBrewingSetupProvenancePersistence.encode(selection),
            )
        }

        val restored = TroubleBrewingSetupProvenancePersistence.decodeOrNull(root, dataset)

        assertEquals(selection, restored)
    }

    @Test
    fun `legacy active game without preset provenance remains legacy instead of selecting a replacement`() {
        assertNull(
            TroubleBrewingSetupProvenancePersistence.decodeOrNull(
                root = JSONObject(),
                dataset = dataset(),
            ),
        )
    }

    @Test
    fun `persisted provenance cannot resolve against a different dataset identity`() {
        val original = dataset()
        val root = JSONObject().apply {
            put(
                TroubleBrewingSetupProvenancePersistence.ROOT_KEY,
                TroubleBrewingSetupProvenancePersistence.encode(selection(original)),
            )
        }
        val replacement = original.copy(datasetId = "replacement-dataset")

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupProvenancePersistence.decodeOrNull(root, replacement)
        }
    }

    private fun dataset(): TroubleBrewingSetupPresetDataset {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-8-restore-a",
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
        return TroubleBrewingSetupPresetDataset(
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
    }

    private fun selection(dataset: TroubleBrewingSetupPresetDataset): TroubleBrewingSetupPresetSelection {
        val preset = dataset.pools.getValue(8).single()
        return TroubleBrewingSetupPresetSelection(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            presetId = preset.id,
            playerCount = preset.playerCount,
            gameSeed = 8_006L,
            preset = preset,
            selectedDrunkShownRole = "investigator",
        )
    }
}
