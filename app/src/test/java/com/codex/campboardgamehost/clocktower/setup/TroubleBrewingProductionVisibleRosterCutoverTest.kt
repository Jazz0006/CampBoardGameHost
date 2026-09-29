package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingProductionVisibleRosterCutoverTest {
    @Test
    fun `drunk production preparation seats only visible identities before compatibility finalization`() {
        val preset = drunkPreset()
        val dataset = dataset(preset)
        val registry = canonicalRegistry()
        val players = (1..preset.playerCount).map { "Player $it" }
        val seed = 7_101L

        val prepared = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = registry,
            orderedPlayerNames = players,
            gameSeed = seed,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )

        val intermediate = prepared.intermediateSetup
        assertEquals(preset, prepared.preset)
        assertTrue(intermediate.visibleRoster.hasDrunk)
        assertFalse("drunk" in intermediate.visibleRoster.visibleRoleIds)
        assertEquals(preset.playerCount, intermediate.visibleRoster.visibleRoleIds.size)
        assertEquals(players, intermediate.shownSeatAssignments.map { it.playerName })
        assertEquals(
            intermediate.visibleRoster.visibleRoleIds.sorted(),
            intermediate.shownSeatAssignments.map { it.shownRoleId }.sorted(),
        )

        val addedVisibleTownsfolk = intermediate.visibleRoster.townsfolkRoleIds
            .single { it !in preset.townsfolk }
        assertTrue(addedVisibleTownsfolk in preset.drunkAsOptions)

        val compatibility = prepared.compatibilityDealPlan
        val compatibilityDrunk = compatibility.assignments.single { it.actualRoleId == "drunk" }
        assertEquals(addedVisibleTownsfolk, compatibilityDrunk.shownRoleId)
        assertEquals(addedVisibleTownsfolk, compatibility.selectedDrunkShownRole)
        assertEquals(
            intermediate.shownSeatAssignments.map { it.seat to it.shownRoleId },
            compatibility.assignments.map { it.seat to it.shownRoleId },
        )
        assertEquals(
            (preset.townsfolk + preset.outsiders + preset.minions + preset.demons).sorted(),
            compatibility.assignments.map { it.actualRoleId }.sorted(),
        )

        assertEquals(
            legacyShownIdentityChoice(
                dataset = dataset,
                preset = preset,
                registry = registry,
                setupSeed = seed,
            ),
            addedVisibleTownsfolk,
        )
    }

    @Test
    fun `non drunk production preparation keeps visible and compatibility identities identical`() {
        val preset = nonDrunkPreset()
        val prepared = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset(preset),
            characterRegistry = canonicalRegistry(),
            orderedPlayerNames = (1..preset.playerCount).map { "Player $it" },
            gameSeed = 7_102L,
            recentSetupRotationHistory = TroubleBrewingSetupRotationHistory.EMPTY,
        )

        assertFalse(prepared.intermediateSetup.visibleRoster.hasDrunk)
        assertEquals(
            prepared.intermediateSetup.shownSeatAssignments.map { it.seat to it.shownRoleId },
            prepared.compatibilityDealPlan.assignments.map { it.seat to it.actualRoleId },
        )
        assertTrue(
            prepared.compatibilityDealPlan.assignments.all {
                it.actualRoleId == it.shownRoleId
            },
        )
    }

    private fun legacyShownIdentityChoice(
        dataset: TroubleBrewingSetupPresetDataset,
        preset: TroubleBrewingSetupPreset,
        registry: ClocktowerCharacterRegistry,
        setupSeed: Long,
    ): String {
        val candidate = SetupCandidate(
            script = ScriptId("trouble_brewing"),
            actualRoles = (preset.townsfolk + preset.outsiders + preset.minions + preset.demons).map { externalId ->
                requireNotNull(registry.findByExternalId(externalId)).id
            },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.TEMPLATE,
                providerId = dataset.datasetId,
                candidateId = preset.id,
            ),
        )
        val policy = requireNotNull(
            TroubleBrewingShownIdentityPolicySource(dataset, registry).find(
                TemplateShownIdentityPolicyKey(
                    providerId = dataset.datasetId,
                    candidateId = preset.id,
                ),
            ),
        )
        val commitment = SetupShownIdentityCommitter().commit(
            candidate = candidate,
            policy = policy,
            setupSeed = setupSeed,
        )
        val drunkRole = requireNotNull(registry.findByExternalId("drunk")).id
        val shownRole = commitment.shownRoleFor(drunkRole)
        return requireNotNull(registry.findByRoleId(shownRole)).externalId
    }

    private fun drunkPreset() = TroubleBrewingSetupPreset(
        id = "tb-dlb1-drunk-eight",
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

    private fun nonDrunkPreset() = TroubleBrewingSetupPreset(
        id = "tb-dlb1-ordinary-eight",
        playerCount = 8,
        townsfolk = listOf("chef", "empath", "fortuneteller", "undertaker", "monk"),
        outsiders = listOf("butler"),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "standard",
        styleTags = listOf("balanced"),
        drunkAsOptions = emptyList(),
    )

    private fun dataset(preset: TroubleBrewingSetupPreset) =
        TroubleBrewingSetupPresetDataset(
            schemaVersion = 2,
            datasetId = "dlb1-test-dataset",
            status = "test",
            declaredPoolSizes = mapOf(preset.playerCount to 1),
            runtimeSelectionPolicy = TroubleBrewingRuntimeSelectionPolicy(
                exactRepeat = "reject",
                similarityScope = "test",
                roleOverlapFormula = "test",
                lastGameMaxOverlap = mapOf(preset.playerCount to 1.0),
                historyWeights = listOf(1.0),
                extraSoftPenalties = emptyList(),
                fallback = "test",
            ),
            pools = mapOf(preset.playerCount to listOf(preset)),
        )

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
}
