package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

internal data class TroubleBrewingPreparedSetup(
    val preset: TroubleBrewingSetupPreset,
    val intermediateSetup: TroubleBrewingIntermediateSetup,
)

/**
 * Single pure preparation transaction for a new Trouble Brewing production game.
 *
 * Validation is deliberately upstream of selection and materialization. Curated presets are adapted
 * into the generic setup authority before composition selection; invalid data is a hard failure.
 */
internal object TroubleBrewingProductionSetupPreparer {
    fun prepare(
        dataset: TroubleBrewingSetupPresetDataset,
        characterRegistry: ClocktowerCharacterRegistry,
        orderedPlayerNames: List<String>,
        gameSeed: Long,
        recentSetupRotationHistory: TroubleBrewingSetupRotationHistory,
        recentPlayerStartingIdentityHistory: TroubleBrewingPlayerStartingIdentityHistory =
            TroubleBrewingPlayerStartingIdentityHistory.EMPTY,
    ): TroubleBrewingPreparedSetup {
        TroubleBrewingSetupPresetValidator.validate(dataset, characterRegistry)

        val playerCount = orderedPlayerNames.size
        val pool = dataset.pools[playerCount]
            ?: throw IllegalArgumentException("No Trouble Brewing setup preset pool for $playerCount players.")
        val candidates = pool.map { preset ->
            SetupCandidate(
                script = TROUBLE_BREWING_SCRIPT,
                actualRoles = preset.visibleExternalRoleIds().map { externalId ->
                    requireNotNull(characterRegistry.findByExternalId(externalId)) {
                        "Validated Trouble Brewing preset '${preset.id}' references unknown role '$externalId'."
                    }.id
                },
                provenance = SetupProvenance(
                    sourceKind = SetupSourceKind.TEMPLATE,
                    providerId = dataset.datasetId,
                    candidateId = preset.id,
                ),
            )
        }
        val provider = ClocktowerSetupProvider(
            script = TROUBLE_BREWING_SCRIPT,
            providerId = dataset.datasetId,
            candidateSource = TemplateRepository(
                buckets = mapOf(
                    TemplateBucketKey(TROUBLE_BREWING_SCRIPT, playerCount) to candidates,
                ),
            ),
        )
        val request = SetupCandidateRequest(
            script = TROUBLE_BREWING_SCRIPT,
            playerCount = playerCount,
            setupSeed = gameSeed,
        )
        val selectedCandidate = SetupDiversitySelector().select(
            candidates = provider.candidates(request),
            history = recentSetupRotationHistory.toSetupDiversityHistory(characterRegistry),
            selectionSeed = gameSeed,
        )
        val selectedPresetId = requireNotNull(selectedCandidate.provenance.candidateId) {
            "Trouble Brewing template selection requires a durable preset ID."
        }
        val selectedPreset = requireNotNull(pool.singleOrNull { it.id == selectedPresetId }) {
            "Generic Trouble Brewing selection '$selectedPresetId' does not resolve to the validated preset pool."
        }

        val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
            preset = selectedPreset,
        )
        val shownSeatAssignments = TroubleBrewingSetupDealPlanner.planVisibleRoster(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            preset = selectedPreset,
            gameSeed = gameSeed,
            visibleRoster = visibleRoster,
            orderedPlayerNames = orderedPlayerNames,
            recentPlayerStartingIdentityHistory = recentPlayerStartingIdentityHistory,
        )
        val intermediateSetup = TroubleBrewingIntermediateSetup(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            presetId = selectedPreset.id,
            playerCount = playerCount,
            gameSeed = gameSeed,
            visibleRoster = visibleRoster,
            shownSeatAssignments = shownSeatAssignments,
        )
        return TroubleBrewingPreparedSetup(
            preset = selectedPreset,
            intermediateSetup = intermediateSetup,
        )
    }

    private fun TroubleBrewingSetupPreset.visibleExternalRoleIds(): List<String> =
        townsfolk + outsiders.filterNot { it == DRUNK_EXTERNAL_ID } + minions + demons

    private val TROUBLE_BREWING_SCRIPT = ScriptId("trouble_brewing")
    private const val DRUNK_EXTERNAL_ID = "drunk"
}
