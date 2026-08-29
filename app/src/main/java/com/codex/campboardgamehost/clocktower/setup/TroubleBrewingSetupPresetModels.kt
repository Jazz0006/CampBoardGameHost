package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingSetupPresetDataset(
    val schemaVersion: Int,
    val datasetId: String,
    val status: String,
    val declaredPoolSizes: Map<Int, Int>,
    val pools: Map<Int, List<TroubleBrewingSetupPreset>>,
) {
    val totalPresetCount: Int
        get() = pools.values.sumOf { it.size }
}

internal data class TroubleBrewingSetupPreset(
    val id: String,
    val playerCount: Int,
    val townsfolk: List<String>,
    val outsiders: List<String>,
    val minions: List<String>,
    val demons: List<String>,
    val source: String,
    val complexity: String,
    val styleTags: List<String>,
    val drunkAsOptions: List<String>,
)
