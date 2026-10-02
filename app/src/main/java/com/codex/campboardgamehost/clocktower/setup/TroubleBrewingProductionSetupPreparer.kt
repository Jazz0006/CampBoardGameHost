package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

internal data class TroubleBrewingPreparedSetup(
    val preset: TroubleBrewingSetupPreset,
    val intermediateSetup: TroubleBrewingIntermediateSetup,
    val compatibilityConfirmedDrunkCandidate: TroubleBrewingDrunkCandidate? = null,
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
                actualRoles = preset.actualExternalRoleIds().map { externalId ->
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
            history = recentSetupRotationHistory.toGenericHistory(characterRegistry),
            selectionSeed = gameSeed,
        )
        val selectedPresetId = requireNotNull(selectedCandidate.provenance.candidateId) {
            "Trouble Brewing template selection requires a durable preset ID."
        }
        val selectedPreset = requireNotNull(pool.singleOrNull { it.id == selectedPresetId }) {
            "Generic Trouble Brewing selection '$selectedPresetId' does not resolve to the validated preset pool."
        }

        val addedVisibleTownsfolkRoleId =
            TroubleBrewingVisibleRosterOptionSelector.selectAddedTownsfolk(
                candidate = selectedCandidate,
                preset = selectedPreset,
                characterRegistry = characterRegistry,
                setupSeed = gameSeed,
            )
        val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
            preset = selectedPreset,
            addedVisibleTownsfolkRoleId = addedVisibleTownsfolkRoleId,
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
        val compatibilityConfirmedDrunkCandidate =
            resolveCompatibilityConfirmedDrunkCandidate(
                preset = selectedPreset,
                intermediateSetup = intermediateSetup,
                addedVisibleTownsfolkRoleId = addedVisibleTownsfolkRoleId,
            )

        return TroubleBrewingPreparedSetup(
            preset = selectedPreset,
            intermediateSetup = intermediateSetup,
            compatibilityConfirmedDrunkCandidate = compatibilityConfirmedDrunkCandidate,
        )
    }

    private fun resolveCompatibilityConfirmedDrunkCandidate(
        preset: TroubleBrewingSetupPreset,
        intermediateSetup: TroubleBrewingIntermediateSetup,
        addedVisibleTownsfolkRoleId: String?,
    ): TroubleBrewingDrunkCandidate? {
        if (!intermediateSetup.visibleRoster.hasDrunk) {
            require(addedVisibleTownsfolkRoleId == null) {
                "Trouble Brewing setup without Drunk cannot carry a transitional fallback Townsfolk."
            }
            return null
        }

        require(DRUNK_EXTERNAL_ID in preset.outsiders) {
            "Trouble Brewing Drunk visible roster must come from a Drunk preset."
        }
        val fallbackShownRoleId = requireNotNull(addedVisibleTownsfolkRoleId) {
            "Trouble Brewing Drunk setup requires the transitional fallback Townsfolk."
        }
        require(fallbackShownRoleId in preset.drunkAsOptions) {
            "Trouble Brewing transitional fallback Townsfolk must come from drunk_as_options."
        }

        return requireNotNull(
            TroubleBrewingDrunkCandidateDomain
                .legalCandidates(intermediateSetup)
                .singleOrNull { candidate ->
                    candidate.shownRoleId == fallbackShownRoleId
                },
        ) {
            "Trouble Brewing transitional fallback must resolve to one current legal Drunk candidate."
        }
    }

    private fun TroubleBrewingSetupPreset.actualExternalRoleIds(): List<String> =
        townsfolk + outsiders + minions + demons

    private fun TroubleBrewingSetupRotationHistory.toGenericHistory(
        characterRegistry: ClocktowerCharacterRegistry,
    ): SetupDiversityHistory {
        val impRole = requireNotNull(characterRegistry.findByExternalId(IMP_EXTERNAL_ID)) {
            "Trouble Brewing generic setup history requires the canonical Imp role."
        }.id
        return SetupDiversityHistory(
            recentSetups = recentGames.map { record ->
                TroubleBrewingSetupRotationRecordFactory.validate(record)
                SetupDiversityRecord(
                    script = TROUBLE_BREWING_SCRIPT,
                    actualRoles = record.realNonDemonRoleIds.map { externalId ->
                        requireNotNull(characterRegistry.findByExternalId(externalId)) {
                            "Trouble Brewing setup history references unknown role '$externalId'."
                        }.id
                    } + impRole,
                )
            },
        )
    }

    private val TROUBLE_BREWING_SCRIPT = ScriptId("trouble_brewing")
    private const val DRUNK_EXTERNAL_ID = "drunk"
    private const val IMP_EXTERNAL_ID = "imp"
}
