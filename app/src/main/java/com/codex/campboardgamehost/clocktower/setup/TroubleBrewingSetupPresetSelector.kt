package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import kotlin.math.roundToLong

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
        recentSetupRotationHistory: TroubleBrewingSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
    ): TroubleBrewingSetupPresetSelection {
        val pool = dataset.pools[playerCount]
            ?: throw IllegalArgumentException("No Trouble Brewing setup preset pool for $playerCount players.")
        require(pool.isNotEmpty()) { "Trouble Brewing setup preset pool for $playerCount players is empty." }
        require(pool.all { it.playerCount == playerCount }) {
            "Trouble Brewing setup preset pool $playerCount contains a mismatched preset player count."
        }

        val previousGame = recentSetupRotationHistory.recentGames.firstOrNull()
        val previousComposition = previousGame?.realNonDemonRoleIds
        val exactRepeatEligible = if (
            dataset.runtimeSelectionPolicy.exactRepeat == EXACT_REPEAT_REJECT && previousComposition != null
        ) {
            pool.filterNot { candidate -> candidate.nonDemonRoleIds() == previousComposition }
        } else {
            pool
        }
        require(exactRepeatEligible.isNotEmpty()) {
            "No Trouble Brewing setup preset remains after exact-repeat filtering for $playerCount players."
        }

        val overlapEligible = if (previousComposition != null) {
            val maxOverlap = dataset.runtimeSelectionPolicy.lastGameMaxOverlap[playerCount]
                ?: throw IllegalArgumentException(
                    "No Trouble Brewing last-game overlap threshold for $playerCount players.",
                )
            exactRepeatEligible.filter { candidate ->
                candidate.overlapWith(previousComposition) <= maxOverlap
            }
        } else {
            exactRepeatEligible
        }
        require(overlapEligible.isNotEmpty()) {
            "No Trouble Brewing setup preset remains after last-game overlap filtering for $playerCount players."
        }

        val canonicalPool = overlapEligible.sortedBy { it.id }
        val selectionSeed = MurmurHash3.low64Utf8(
            "tb-preset-v1|${dataset.datasetId}|$playerCount|$gameSeed",
        )
        val weightedPool = canonicalPool.map { candidate ->
            val score = TroubleBrewingSetupPresetRotationScorer.scoreBaseNovelty(
                candidate = candidate,
                recentSetupRotationHistory = recentSetupRotationHistory,
                historyWeights = dataset.runtimeSelectionPolicy.historyWeights,
            )
            WeightedCandidate(
                preset = candidate,
                weight = (score.baseNoveltyWeight * WEIGHT_SCALE).roundToLong().coerceAtLeast(1L),
            )
        }
        val selected = selectWeighted(weightedPool, selectionSeed)
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

    private fun selectWeighted(
        candidates: List<WeightedCandidate>,
        selectionSeed: Long,
    ): TroubleBrewingSetupPreset {
        require(candidates.isNotEmpty())
        val firstWeight = candidates.first().weight
        if (candidates.all { it.weight == firstWeight }) {
            return candidates[
                java.lang.Long.remainderUnsigned(selectionSeed, candidates.size.toLong()).toInt()
            ].preset
        }

        val totalWeight = candidates.sumOf { it.weight }
        require(totalWeight > 0L) { "Trouble Brewing candidate weights must have positive total weight." }
        val draw = java.lang.Long.remainderUnsigned(selectionSeed, totalWeight)
        var cumulative = 0L
        candidates.forEach { candidate ->
            cumulative += candidate.weight
            if (draw < cumulative) return candidate.preset
        }
        error("Trouble Brewing weighted preset selection did not resolve a candidate.")
    }

    private fun TroubleBrewingSetupPreset.nonDemonRoleIds(): Set<String> =
        (townsfolk + outsiders + minions).toSet()

    private fun TroubleBrewingSetupPreset.overlapWith(previousRoleIds: Set<String>): Double =
        nonDemonRoleIds().intersect(previousRoleIds).size.toDouble() / (playerCount - 1).toDouble()

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

    private data class WeightedCandidate(
        val preset: TroubleBrewingSetupPreset,
        val weight: Long,
    )

    private const val EXACT_REPEAT_REJECT = "reject"
    private const val DRUNK_EXTERNAL_ID = "drunk"
    private const val WEIGHT_SCALE = 1_000_000.0
}
