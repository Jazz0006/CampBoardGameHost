package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry

/**
 * Trouble Brewing edge adapter from validated preset metadata to the generic S6A policy source.
 *
 * The generic policy layer never sees TroubleBrewingSetupPreset. Exact TB cardinality/team/in-play
 * validation remains owned by TroubleBrewingSetupPresetValidator before metadata is normalized.
 */
internal class TroubleBrewingShownIdentityPolicySource(
    dataset: TroubleBrewingSetupPresetDataset,
    private val characterRegistry: ClocktowerCharacterRegistry,
) : TemplateShownIdentityPolicySource {
    private val providerId: String = dataset.datasetId
    private val policiesByCandidateId: Map<String, SetupShownIdentityPolicy>

    init {
        require(providerId.isNotBlank()) { "Trouble Brewing shown-identity provider ID cannot be blank." }
        TroubleBrewingSetupPresetValidator.validate(dataset, characterRegistry)
        policiesByCandidateId = dataset.pools.values
            .flatten()
            .associate { preset -> preset.id to policyFor(preset) }
    }

    override fun find(key: TemplateShownIdentityPolicyKey): SetupShownIdentityPolicy? {
        if (key.providerId != providerId) return null
        return policiesByCandidateId[key.candidateId]
    }

    private fun policyFor(preset: TroubleBrewingSetupPreset): SetupShownIdentityPolicy {
        if (DRUNK_EXTERNAL_ID !in preset.outsiders) return SetupShownIdentityPolicy.NO_OVERRIDE

        val drunkRole = requireNotNull(characterRegistry.findByExternalId(DRUNK_EXTERNAL_ID)) {
            "Validated Trouble Brewing metadata requires the canonical Drunk role."
        }.id
        val legalShownRoles = preset.drunkAsOptions.map { externalId ->
            requireNotNull(characterRegistry.findByExternalId(externalId)) {
                "Validated Trouble Brewing Drunk option '$externalId' is missing from the registry."
            }.id
        }

        return SetupShownIdentityPolicy(
            overrides = listOf(
                ShownIdentityOverrideOptions(
                    actualRole = drunkRole,
                    legalShownRoles = legalShownRoles,
                ),
            ),
        )
    }

    private companion object {
        const val DRUNK_EXTERNAL_ID = "drunk"
    }
}
