package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SetupShownIdentityPolicyResolverTest {
    @Test
    fun `generated candidate options are only unused Townsfolk in canonical order`() {
        val ruleset = troubleBrewingLikeRuleset().reordered()
        val candidate = generatedCandidate(
            ruleset = ruleset,
            actualExternalIds = listOf("tf3", "drunk", "poisoner", "tf1", "imp", "tf2"),
        )

        val policy = SetupShownIdentityPolicyResolver().resolve(candidate, ruleset)

        assertTrue(policy.requiresOverride)
        assertEquals(RoleId("Drunk"), policy.overrides.single().actualRole)
        assertEquals(
            listOf(RoleId("Tf4"), RoleId("Tf5"), RoleId("Tf6")),
            policy.overrides.single().legalShownRoles,
        )
        assertTrue(policy.overrides.single().legalShownRoles.none { it in candidate.actualRoles })
        assertTrue(
            policy.overrides.single().legalShownRoles.all { roleId ->
                ruleset.characterRegistry.findByRoleId(roleId)?.team == ClocktowerCatalogTeam.TOWNSFOLK
            },
        )
    }

    @Test
    fun `ruleset character order does not change generated canonical options`() {
        val firstRuleset = troubleBrewingLikeRuleset()
        val secondRuleset = firstRuleset.reordered()
        val firstCandidate = generatedCandidate(
            ruleset = firstRuleset,
            actualExternalIds = listOf("tf1", "tf2", "tf3", "drunk", "poisoner", "imp"),
        )
        val secondCandidate = generatedCandidate(
            ruleset = secondRuleset,
            actualExternalIds = listOf("imp", "poisoner", "drunk", "tf3", "tf2", "tf1"),
        )

        val resolver = SetupShownIdentityPolicyResolver()

        assertEquals(
            resolver.resolve(firstCandidate, firstRuleset),
            resolver.resolve(secondCandidate, secondRuleset),
        )
    }

    @Test
    fun `candidate without Drunk returns explicit no override policy`() {
        val ruleset = troubleBrewingLikeRuleset()
        val candidate = generatedCandidate(
            ruleset = ruleset,
            actualExternalIds = listOf("tf1", "tf2", "tf3", "saint", "poisoner", "imp"),
        )

        val policy = SetupShownIdentityPolicyResolver().resolve(candidate, ruleset)

        assertFalse(policy.requiresOverride)
        assertTrue(policy.overrides.isEmpty())
    }

    @Test
    fun `generated Drunk with no unused Townsfolk fails closed`() {
        val ruleset = ruleset(
            characters = listOf(
                role("Tf1", "tf1", ClocktowerCatalogTeam.TOWNSFOLK),
                role("Tf2", "tf2", ClocktowerCatalogTeam.TOWNSFOLK),
                role("Tf3", "tf3", ClocktowerCatalogTeam.TOWNSFOLK),
                role("Drunk", "drunk", ClocktowerCatalogTeam.OUTSIDER),
                role("Poisoner", "poisoner", ClocktowerCatalogTeam.MINION),
                role("Imp", "imp", ClocktowerCatalogTeam.DEMON),
            ),
        )
        val candidate = generatedCandidate(
            ruleset = ruleset,
            actualExternalIds = listOf("tf1", "tf2", "tf3", "drunk", "poisoner", "imp"),
        )

        expectIllegalArgument {
            SetupShownIdentityPolicyResolver().resolve(candidate, ruleset)
        }
    }

    @Test
    fun `template option that is now an actual in play role fails closed`() {
        val ruleset = troubleBrewingLikeRuleset()
        val policy = SetupShownIdentityPolicy(
            overrides = listOf(
                ShownIdentityOverrideOptions(
                    actualRole = RoleId("Drunk"),
                    legalShownRoles = listOf(RoleId("Tf1"), RoleId("Tf4")),
                ),
            ),
        )
        val source = TemplateShownIdentityPolicySource { key ->
            if (key == TemplateShownIdentityPolicyKey(DATASET_ID, DRUNK_PRESET_ID)) policy else null
        }
        val candidate = SetupCandidate(
            script = SCRIPT_ID,
            actualRoles = listOf(
                RoleId("Tf1"),
                RoleId("Tf2"),
                RoleId("Tf3"),
                RoleId("Drunk"),
                RoleId("Poisoner"),
                RoleId("Imp"),
            ),
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.TEMPLATE,
                providerId = DATASET_ID,
                candidateId = DRUNK_PRESET_ID,
            ),
        )

        expectIllegalArgument {
            SetupShownIdentityPolicyResolver(source).resolve(candidate, ruleset)
        }
    }

    private fun templateCandidate(
        preset: TroubleBrewingSetupPreset,
        ruleset: ValidatedClocktowerRuleset,
        providerId: String = DATASET_ID,
        candidateId: String = preset.id,
    ): SetupCandidate = SetupCandidate(
        script = SCRIPT_ID,
        actualRoles = preset.actualExternalIds().map { externalId ->
            requireNotNull(ruleset.characterRegistry.findByExternalId(externalId)).id
        },
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.TEMPLATE,
            providerId = providerId,
            candidateId = candidateId,
        ),
    )

    private fun generatedCandidate(
        ruleset: ValidatedClocktowerRuleset,
        actualExternalIds: List<String>,
    ): SetupCandidate = SetupCandidate(
        script = SCRIPT_ID,
        actualRoles = actualExternalIds.map { externalId ->
            requireNotNull(ruleset.characterRegistry.findByExternalId(externalId)).id
        },
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.GENERATED,
            providerId = GENERATED_PROVIDER_ID,
        ),
    )

    private fun troubleBrewingLikeRuleset(): ValidatedClocktowerRuleset = ruleset(
        characters = listOf(
            role("Tf1", "tf1", ClocktowerCatalogTeam.TOWNSFOLK),
            role("Tf2", "tf2", ClocktowerCatalogTeam.TOWNSFOLK),
            role("Tf3", "tf3", ClocktowerCatalogTeam.TOWNSFOLK),
            role("Tf4", "tf4", ClocktowerCatalogTeam.TOWNSFOLK),
            role("Tf5", "tf5", ClocktowerCatalogTeam.TOWNSFOLK),
            role("Tf6", "tf6", ClocktowerCatalogTeam.TOWNSFOLK),
            role("Drunk", "drunk", ClocktowerCatalogTeam.OUTSIDER),
            role("Saint", "saint", ClocktowerCatalogTeam.OUTSIDER),
            role("Poisoner", "poisoner", ClocktowerCatalogTeam.MINION),
            role("Imp", "imp", ClocktowerCatalogTeam.DEMON),
        ),
    )

    private fun ValidatedClocktowerRuleset.reordered(): ValidatedClocktowerRuleset =
        ruleset(characters = characters.reversed())

    private fun ruleset(
        characters: List<ClocktowerCharacterDefinition>,
    ): ValidatedClocktowerRuleset = ValidatedClocktowerRuleset(
        script = ClocktowerScriptDefinition(
            id = SCRIPT_ID,
            name = "S6A Test Script",
            author = null,
            characterIds = characters.map(ClocktowerCharacterDefinition::id),
            firstNightOverride = null,
            otherNightOverride = null,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
            contentHash = "6".repeat(32),
        ),
        characters = characters,
        coverage = RuleCoverage.PARTIAL,
    )

    private fun role(
        id: String,
        externalId: String,
        team: ClocktowerCatalogTeam,
    ): ClocktowerCharacterDefinition = ClocktowerCharacterDefinition(
        id = RoleId(id),
        externalId = externalId,
        name = id,
        team = team,
        abilityText = "Test ability.",
        automationCoverage = RuleCoverage.PARTIAL,
    )

    private fun templateDataset(): TroubleBrewingSetupPresetDataset = TroubleBrewingSetupPresetDataset(
        schemaVersion = 1,
        datasetId = DATASET_ID,
        status = "test",
        declaredPoolSizes = mapOf(6 to 2),
        runtimeSelectionPolicy = TroubleBrewingRuntimeSelectionPolicy(
            exactRepeat = "reject",
            similarityScope = "test",
            roleOverlapFormula = "test",
            lastGameMaxOverlap = mapOf(6 to 1.0),
            historyWeights = emptyList(),
            extraSoftPenalties = emptyList(),
            fallback = "test",
        ),
        pools = mapOf(
            6 to listOf(
                TroubleBrewingSetupPreset(
                    id = DRUNK_PRESET_ID,
                    playerCount = 6,
                    townsfolk = listOf("tf1", "tf2", "tf3", "tf4"),
                    outsiders = emptyList(),
                    minions = listOf("poisoner"),
                    demons = listOf("imp"),
                    source = "test",
                    complexity = "test",
                    styleTags = emptyList(),
                    drunk = true,
                ),
                TroubleBrewingSetupPreset(
                    id = NO_DRUNK_PRESET_ID,
                    playerCount = 6,
                    townsfolk = listOf("tf1", "tf2", "tf3", "tf4"),
                    outsiders = listOf("saint"),
                    minions = listOf("poisoner"),
                    demons = listOf("imp"),
                    source = "test",
                    complexity = "test",
                    styleTags = emptyList(),
                    drunk = false,
                ),
            ),
        ),
    )

    private fun TroubleBrewingSetupPreset.actualExternalIds(): List<String> =
        townsfolk + outsiders + minions + demons

    private inline fun expectIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected fail-closed contract.
        }
    }

    private companion object {
        val SCRIPT_ID = ScriptId("trouble_brewing")
        const val DATASET_ID = "tb-presets-test-v1"
        const val GENERATED_PROVIDER_ID = "generated-test-v1"
        const val DRUNK_PRESET_ID = "tb-6-drunk"
        const val NO_DRUNK_PRESET_ID = "tb-6-no-drunk"
    }
}
