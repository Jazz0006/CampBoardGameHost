package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSetupCandidateSourceTest {
    @Test
    fun `same request yields the same legal generated candidate with stable provenance`() {
        val ruleset = flexibleRuleset()
        val provider = provider(ruleset)
        val request = request(playerCount = 5, setupSeed = 20260831L)

        val first = provider.candidates(request).single()
        val second = provider.candidates(request).single()

        assertEquals(first, second)
        assertEquals(SCRIPT_ID, first.script)
        assertEquals(5, first.playerCount)
        assertEquals(SetupSourceKind.GENERATED, first.provenance.sourceKind)
        assertEquals(PROVIDER_ID, first.provenance.providerId)
        assertNull(first.provenance.candidateId)
        assertTrue(first.actualRoles.all { it in ruleset.script.characterIds })
        assertEquals(
            mapOf(
                ClocktowerCatalogTeam.TOWNSFOLK to 3,
                ClocktowerCatalogTeam.MINION to 1,
                ClocktowerCatalogTeam.DEMON to 1,
            ),
            teamCounts(first, ruleset),
        )
    }

    @Test
    fun `different seeds explore more than one legal composition when choices exist`() {
        val ruleset = flexibleRuleset()
        val provider = provider(ruleset)

        val candidates = (0L until 64L).map { setupSeed ->
            provider.candidates(request(playerCount = 5, setupSeed = setupSeed)).single().also { candidate ->
                assertEquals(5, candidate.playerCount)
                assertTrue(candidate.actualRoles.all { it in ruleset.script.characterIds })
                assertEquals(
                    mapOf(
                        ClocktowerCatalogTeam.TOWNSFOLK to 3,
                        ClocktowerCatalogTeam.MINION to 1,
                        ClocktowerCatalogTeam.DEMON to 1,
                    ),
                    teamCounts(candidate, ruleset),
                )
            }
        }

        assertTrue(candidates.map { it.actualRoles }.toSet().size > 1)
    }

    @Test
    fun `Baron applies exactly one plus two outsider composition adjustment`() {
        val ruleset = baronRuleset(outsiderCount = 4)
        val candidate = provider(ruleset)
            .candidates(request(playerCount = 6, setupSeed = 7L))
            .single()

        assertEquals(6, candidate.playerCount)
        assertEquals(
            mapOf(
                ClocktowerCatalogTeam.TOWNSFOLK to 1,
                ClocktowerCatalogTeam.OUTSIDER to 3,
                ClocktowerCatalogTeam.MINION to 1,
                ClocktowerCatalogTeam.DEMON to 1,
            ),
            teamCounts(candidate, ruleset),
        )
    }

    @Test
    fun `Baron caps outsider adjustment to preserve No Greater Joy five and six player legality`() {
        val ruleset = baronRuleset(outsiderCount = 2)

        val fivePlayerCandidate = provider(ruleset)
            .candidates(request(playerCount = 5, setupSeed = 11L))
            .single()
        val sixPlayerCandidate = provider(ruleset)
            .candidates(request(playerCount = 6, setupSeed = 11L))
            .single()

        assertEquals(
            mapOf(
                ClocktowerCatalogTeam.TOWNSFOLK to 1,
                ClocktowerCatalogTeam.OUTSIDER to 2,
                ClocktowerCatalogTeam.MINION to 1,
                ClocktowerCatalogTeam.DEMON to 1,
            ),
            teamCounts(fivePlayerCandidate, ruleset),
        )
        assertEquals(
            mapOf(
                ClocktowerCatalogTeam.TOWNSFOLK to 2,
                ClocktowerCatalogTeam.OUTSIDER to 2,
                ClocktowerCatalogTeam.MINION to 1,
                ClocktowerCatalogTeam.DEMON to 1,
            ),
            teamCounts(sixPlayerCandidate, ruleset),
        )
    }

    private fun provider(ruleset: ValidatedClocktowerRuleset): ClocktowerSetupProvider =
        ClocktowerSetupProvider(
            script = SCRIPT_ID,
            providerId = PROVIDER_ID,
            candidateSource = GeneratedSetupCandidateSource(
                providerId = PROVIDER_ID,
                ruleset = ruleset,
            ),
        )

    private fun request(playerCount: Int, setupSeed: Long): SetupCandidateRequest =
        SetupCandidateRequest(
            script = SCRIPT_ID,
            playerCount = playerCount,
            setupSeed = setupSeed,
        )

    private fun flexibleRuleset(): ValidatedClocktowerRuleset = ruleset(
        characters =
            (1..8).map { index -> role("Tf$index", "tf$index", ClocktowerCatalogTeam.TOWNSFOLK) } +
                (1..3).map { index -> role("Minion$index", "minion$index", ClocktowerCatalogTeam.MINION) } +
                (1..2).map { index -> role("Demon$index", "demon$index", ClocktowerCatalogTeam.DEMON) },
    )

    private fun baronRuleset(outsiderCount: Int): ValidatedClocktowerRuleset = ruleset(
        characters =
            (1..6).map { index -> role("Tf$index", "tf$index", ClocktowerCatalogTeam.TOWNSFOLK) } +
                (1..outsiderCount).map { index ->
                    role("Outsider$index", "outsider$index", ClocktowerCatalogTeam.OUTSIDER)
                } +
                role("Baron", "baron", ClocktowerCatalogTeam.MINION, setup = true) +
                role("Imp", "imp", ClocktowerCatalogTeam.DEMON),
    )

    private fun ruleset(
        characters: List<ClocktowerCharacterDefinition>,
    ): ValidatedClocktowerRuleset = ValidatedClocktowerRuleset(
        script = ClocktowerScriptDefinition(
            id = SCRIPT_ID,
            name = "Test Script",
            author = null,
            characterIds = characters.map(ClocktowerCharacterDefinition::id),
            firstNightOverride = null,
            otherNightOverride = null,
            bootleggerRules = emptyList(),
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
            contentHash = "0".repeat(32),
        ),
        characters = characters,
        coverage = RuleCoverage.PARTIAL,
    )

    private fun role(
        id: String,
        externalId: String,
        team: ClocktowerCatalogTeam,
        setup: Boolean = false,
    ): ClocktowerCharacterDefinition = ClocktowerCharacterDefinition(
        id = RoleId(id),
        externalId = externalId,
        name = id,
        team = team,
        abilityText = "Test ability.",
        setup = setup,
        automationCoverage = RuleCoverage.PARTIAL,
    )

    private fun teamCounts(
        candidate: SetupCandidate,
        ruleset: ValidatedClocktowerRuleset,
    ): Map<ClocktowerCatalogTeam, Int> = candidate.actualRoles
        .groupingBy { roleId ->
            requireNotNull(ruleset.characterRegistry.findByRoleId(roleId)).team
        }
        .eachCount()

    private companion object {
        val SCRIPT_ID = ScriptId("no_greater_joy")
        const val PROVIDER_ID = "generated-seeded-v1"
    }
}
