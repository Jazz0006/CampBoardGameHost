package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingSetupPresetJsonTest {
    @Test
    fun `parser exposes typed preset fields without resolving runtime role ids`() {
        val parsed = TroubleBrewingSetupPresetJson.parse(
            """
            {
              "schema_version": 2,
              "dataset_id": "trouble_brewing_setup_presets_v2_final",
              "status": "final_ready_for_program_integration",
              "pool_sizes": {"5": 1},
              "runtime_selection_policy": {
                "exact_repeat": "reject",
                "similarity_scope": "Compare real in-play non-Demon role IDs.",
                "role_overlap_formula": "intersection / (player_count - 1)",
                "last_game_max_overlap": {"5": 0.6},
                "history_weights": [1.0, 0.65, 0.4, 0.2, 0.1],
                "extra_soft_penalties": ["same minion set"],
                "fallback": "relax by 0.05"
              },
              "pools": {
                "5": [
                  {
                    "id": "TB2_5_001",
                    "player_count": 5,
                    "townsfolk": ["investigator", "ravenkeeper", "virgin"],
                    "outsiders": [],
                    "minions": ["spy"],
                    "demons": ["imp"],
                    "source": "curated_balanced_v2",
                    "complexity": "standard",
                    "style_tags": ["balanced", "registration-chaos"],
                    "drunk_as_options": []
                  }
                ]
              }
            }
            """.trimIndent(),
        )

        assertEquals(2, parsed.schemaVersion)
        assertEquals("trouble_brewing_setup_presets_v2_final", parsed.datasetId)
        assertEquals("final_ready_for_program_integration", parsed.status)
        assertEquals(mapOf(5 to 1), parsed.declaredPoolSizes)
        assertEquals(setOf(5), parsed.pools.keys)
        assertEquals(1, parsed.totalPresetCount)

        val policy = parsed.runtimeSelectionPolicy
        assertEquals("reject", policy.exactRepeat)
        assertEquals(mapOf(5 to 0.6), policy.lastGameMaxOverlap)
        assertEquals(listOf(1.0, 0.65, 0.4, 0.2, 0.1), policy.historyWeights)
        assertEquals(listOf("same minion set"), policy.extraSoftPenalties)
        assertEquals("relax by 0.05", policy.fallback)

        val preset = parsed.pools.getValue(5).single()
        assertEquals("TB2_5_001", preset.id)
        assertEquals(5, preset.playerCount)
        assertEquals(listOf("investigator", "ravenkeeper", "virgin"), preset.townsfolk)
        assertEquals(emptyList<String>(), preset.outsiders)
        assertEquals(listOf("spy"), preset.minions)
        assertEquals(listOf("imp"), preset.demons)
        assertEquals("curated_balanced_v2", preset.source)
        assertEquals("standard", preset.complexity)
        assertEquals(listOf("balanced", "registration-chaos"), preset.styleTags)
        assertEquals(emptyList<String>(), preset.drunkAsOptions)
    }

    @Test
    fun `final asset exposes the frozen dataset identity policy and all curated pool sizes`() {
        val asset = File("src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json")
        assertTrue("Expected final Trouble Brewing preset asset at ${asset.path}", asset.isFile)

        val parsed = TroubleBrewingSetupPresetJson.parse(asset.readText(Charsets.UTF_8))

        assertEquals(2, parsed.schemaVersion)
        assertEquals("trouble_brewing_setup_presets_v2_final", parsed.datasetId)
        assertEquals("final_ready_for_program_integration", parsed.status)
        assertEquals(EXPECTED_POOL_SIZES.keys, parsed.pools.keys)
        assertEquals(EXPECTED_POOL_SIZES, parsed.declaredPoolSizes)
        assertEquals(EXPECTED_POOL_SIZES, parsed.pools.mapValues { (_, presets) -> presets.size })
        assertEquals(EXPECTED_LAST_GAME_MAX_OVERLAP, parsed.runtimeSelectionPolicy.lastGameMaxOverlap)
        assertEquals(listOf(1.0, 0.65, 0.4, 0.2, 0.1), parsed.runtimeSelectionPolicy.historyWeights)
        assertEquals(3, parsed.runtimeSelectionPolicy.extraSoftPenalties.size)
        assertEquals(480, parsed.totalPresetCount)
    }

    companion object {
        private val EXPECTED_POOL_SIZES = linkedMapOf(
            5 to 30,
            6 to 30,
            7 to 50,
            8 to 50,
            9 to 50,
            10 to 50,
            11 to 50,
            12 to 50,
            13 to 40,
            14 to 40,
            15 to 40,
        )
        private val EXPECTED_LAST_GAME_MAX_OVERLAP = linkedMapOf(
            5 to 0.6,
            6 to 0.6,
            7 to 0.7,
            8 to 0.72,
            9 to 0.75,
            10 to 0.78,
            11 to 0.8,
            12 to 0.82,
            13 to 0.83,
            14 to 0.85,
            15 to 0.86,
        )
    }
}
