package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry

internal data class TroubleBrewingPreparedSetup(
    val selection: TroubleBrewingSetupPresetSelection,
    val dealPlan: TroubleBrewingSetupDealPlan,
)

/**
 * Single pure preparation transaction for a new Trouble Brewing production game.
 *
 * Validation is deliberately upstream of selection and materialization. Invalid curated data is a
 * hard failure; this owner has no legacy/random fallback path.
 */
internal object TroubleBrewingProductionSetupPreparer {
    fun prepare(
        dataset: TroubleBrewingSetupPresetDataset,
        characterRegistry: ClocktowerCharacterRegistry,
        orderedPlayerNames: List<String>,
        gameSeed: Long,
        recentSetupRotationHistory: TroubleBrewingSetupRotationHistory,
    ): TroubleBrewingPreparedSetup {
        TroubleBrewingSetupPresetValidator.validate(dataset, characterRegistry)

        val selection = TroubleBrewingSetupPresetSelector.select(
            dataset = dataset,
            playerCount = orderedPlayerNames.size,
            gameSeed = gameSeed,
            recentSetupRotationHistory = recentSetupRotationHistory,
        )
        val dealPlan = TroubleBrewingSetupDealPlanner.plan(
            selection = selection,
            orderedPlayerNames = orderedPlayerNames,
        )

        return TroubleBrewingPreparedSetup(
            selection = selection,
            dealPlan = dealPlan,
        )
    }
}
