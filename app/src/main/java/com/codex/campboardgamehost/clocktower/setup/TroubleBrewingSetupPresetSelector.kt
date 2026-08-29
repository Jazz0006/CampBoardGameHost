package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

internal data class TroubleBrewingSetupPresetSelection(
    val datasetId: String,
    val schemaVersion: Int,
    val presetId: String,
    val playerCount: Int,
    val gameSeed: Long,
    val preset: TroubleBrewingSetupPreset,
    val selectedDrunkShownRole: String?,
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
        val selectedDrunkShownRole = selectDrunkShownRole(
            datasetId = dataset.datasetId,
            playerCount = playerCount,
            gameSeed = gameSeed,
            preset = selected,
        )
        return TroubleBrewingSetupPresetSelection(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            presetId = selected.id,
            playerCount = playerCount,
            gameSeed = gameSeed,
            preset = selected,
            selectedDrunkShownRole = selectedDrunkShownRole,
        )
    }

    private fun selectDrunkShownRole(
        datasetId: String,
        playerCount: Int,
        gameSeed: Long,
        preset: TroubleBrewingSetupPreset,
    ): String? {
        if (DRUNK_EXTERNAL_ID !in preset.outsiders) return null

        val canonicalOptions = preset.drunkAsOptions.sorted()
        require(canonicalOptions.isNotEmpty()) {
            "Trouble Brewing Drunk preset '${preset.id}' has no shown-role options."
        }
        val drunkSeed = MurmurHash3.low64Utf8(
            "tb-drunk-v1|$datasetId|$playerCount|${preset.id}|$gameSeed",
        )
        return canonicalOptions[
            java.lang.Long.remainderUnsigned(drunkSeed, canonicalOptions.size.toLong()).toInt()
        ]
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
