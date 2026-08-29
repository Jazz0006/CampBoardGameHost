package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

internal data class TroubleBrewingSetupPresetSelection(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val gameSeed: Long,
    val preset: TroubleBrewingSetupPreset,
)

internal object TroubleBrewingSetupPresetSelector {
    fun select(
        dataset: TroubleBrewingSetupPresetDataset,
        playerCount: Int,
        gameSeed: Long,
    ): TroubleBrewingSetupPresetSelection {
        val pool = dataset.pools[playerCount]
            ?: throw IllegalArgumentException("No Trouble Brewing setup preset pool for $playerCount players.")
        require(pool.isNotEmpty()) { "Trouble Brewing setup preset pool for $playerCount players is empty." }
        require(pool.all { it.playerCount == playerCount }) {
            "Trouble Brewing setup preset pool $playerCount contains a mismatched preset player count."
        }

        val canonicalPool = pool.sortedBy { it.id }
        val selectionSeed = MurmurHash3.low64Utf8(
            "tb-preset-v1|${dataset.datasetId}|$playerCount|$gameSeed",
        )
        val selected = canonicalPool[
            java.lang.Long.remainderUnsigned(selectionSeed, canonicalPool.size.toLong()).toInt()
        ]
        return TroubleBrewingSetupPresetSelection(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            presetId = selected.id,
            playerCount = playerCount,
            gameSeed = gameSeed,
            preset = selected,
        )
    }
}
