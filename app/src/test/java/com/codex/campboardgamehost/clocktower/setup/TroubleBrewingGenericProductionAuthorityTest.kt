package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TroubleBrewingGenericProductionAuthorityTest {
    @Test
    fun `production preparation follows generic provider and diversity authority`() {
        val registry = canonicalRegistry()
        val dataset = dataset(
            drunkPreset(),
            nonDrunkPreset(),
        )
        val players = (1..8).map { "Player $it" }

        val divergentSeed = (0L until 4_096L).first { seed ->
            val genericPresetId = genericSelectionPresetId(dataset, registry, seed)
            val legacyPresetId = TroubleBrewingSetupPresetSelector.select(
                dataset = dataset,
                playerCount = players.size,
                gameSeed = seed,
                recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
            ).presetId
            genericPresetId != legacyPresetId
        }
        val expectedPresetId = genericSelectionPresetId(dataset, registry, divergentSeed)

        val prepared = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = registry,
            orderedPlayerNames = players,
            gameSeed = divergentSeed,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )

        assertEquals(expectedPresetId, prepared.selection.presetId)
        assertEquals(expectedPresetId, prepared.dealPlan.presetId)
    }

    private fun genericSelectionPresetId(
        dataset: TroubleBrewingSetupPresetDataset,
        registry: ClocktowerCharacterRegistry,
        setupSeed: Long,
    ): String {
        val script = ScriptId("trouble_brewing")
        val candidates = dataset.pools.getValue(8).map { preset ->
            SetupCandidate(
                script = script,
                actualRoles = preset.actualExternalIds().map { externalId ->
                    requireNotNull(registry.findByExternalId(externalId)).id
                },
                provenance = SetupProvenance(
                    sourceKind = SetupSourceKind.TEMPLATE,
                    providerId = dataset.datasetId,
                    candidateId = preset.id,
                ),
            )
        }
        val provider = ClocktowerSetupProvider(
            script = script,
            providerId = dataset.datasetId,
            candidateSource = TemplateRepository(
                buckets = mapOf(TemplateBucketKey(script, 8) to candidates),
            ),
        )
        val selected = SetupDiversitySelector().select(
            candidates = provider.candidates(
                SetupCandidateRequest(
                    script = script,
                    playerCount = 8,
                    setupSeed = setupSeed,
                ),
            ),
            history = SetupDiversityHistory.EMPTY,
            selectionSeed = setupSeed,
        )
        return requireNotNull(selected.provenance.candidateId)
    }

    private fun TroubleBrewingSetupPreset.actualExternalIds(): List<String> =
        townsfolk + outsiders + minions + demons

    private fun drunkPreset(): TroubleBrewingSetupPreset = TroubleBrewingSetupPreset(
        id = "tb-8-generic-a",
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

    private fun nonDrunkPreset(): TroubleBrewingSetupPreset = TroubleBrewingSetupPreset(
        id = "tb-8-generic-b",
        playerCount = 8,
        townsfolk = listOf("washerwoman", "librarian", "investigator", "virgin", "slayer"),
        outsiders = listOf("saint"),
        minions = listOf("scarletwoman"),
        demons = listOf("imp"),
        source = "test",
        complexity = "standard",
        styleTags = listOf("balanced"),
        drunkAsOptions = emptyList(),
    )

    private fun dataset(
        vararg presets: TroubleBrewingSetupPreset,
    ): TroubleBrewingSetupPresetDataset = TroubleBrewingSetupPresetDataset(
        schemaVersion = 2,
        datasetId = "test-generic-authority",
        status = "test",
        declaredPoolSizes = mapOf(8 to presets.size),
        runtimeSelectionPolicy = TroubleBrewingRuntimeSelectionPolicy(
            exactRepeat = "reject",
            similarityScope = "test",
            roleOverlapFormula = "test",
            lastGameMaxOverlap = mapOf(8 to 1.0),
            historyWeights = listOf(1.0),
            extraSoftPenalties = emptyList(),
            fallback = "test",
        ),
        pools = mapOf(8 to presets.toList()),
    )

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
}
