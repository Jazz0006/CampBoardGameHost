package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NoGreaterJoyProductionSetupPreparerTest {
    @Test
    fun `production preparation is reproducible and commits legal actual and shown identities`() {
        val ruleset = builtInRuleset()
        val drunk = requireNotNull(ruleset.characterRegistry.findByExternalId("drunk")).id
        val seed = (0L until 512L).first { candidateSeed ->
            prepare(ruleset, playerCount = 6, gameSeed = candidateSeed)
                .assignments
                .any { it.actualRole == drunk }
        }

        val first = prepare(ruleset, playerCount = 6, gameSeed = seed)
        val second = prepare(ruleset, playerCount = 6, gameSeed = seed)

        assertEquals(first, second)
        assertEquals(NGJ_SCRIPT, first.script)
        assertEquals(seed, first.setupSeed)
        assertEquals(SetupSourceKind.GENERATED, first.provenance.sourceKind)
        assertEquals(GENERATED_PROVIDER_ID, first.provenance.providerId)
        assertNull(first.provenance.candidateId)
        assertEquals((1..6).toList(), first.assignments.map { it.seat })

        val actualRoles = first.assignments.map { it.actualRole }.toSet()
        first.assignments.forEach { assignment ->
            val shownDefinition = requireNotNull(
                ruleset.characterRegistry.findByRoleId(assignment.shownRole),
            )
            if (assignment.actualRole == drunk) {
                assertNotEquals(assignment.actualRole, assignment.shownRole)
                assertTrue(assignment.shownRole !in actualRoles)
                assertEquals(ClocktowerCatalogTeam.TOWNSFOLK, shownDefinition.team)
            } else {
                assertEquals(assignment.actualRole, assignment.shownRole)
            }
        }
    }

    @Test
    fun `production preparation preserves legal five and six player NGJ distributions`() {
        val ruleset = builtInRuleset()
        val baron = requireNotNull(ruleset.characterRegistry.findByExternalId("baron")).id

        listOf(5, 6).forEach { playerCount ->
            (0L until 128L).forEach { seed ->
                val prepared = prepare(ruleset, playerCount, seed)
                val actualRoles = prepared.assignments.map { it.actualRole }
                val hasBaron = baron in actualRoles
                val expected = when {
                    playerCount == 5 && hasBaron -> counts(townsfolk = 1, outsiders = 2)
                    playerCount == 5 -> counts(townsfolk = 3, outsiders = 0)
                    hasBaron -> counts(townsfolk = 2, outsiders = 2)
                    else -> counts(townsfolk = 3, outsiders = 1)
                }

                assertEquals(playerCount, prepared.playerCount)
                assertEquals(expected, teamCounts(prepared, ruleset))
            }
        }
    }

    private fun prepare(
        ruleset: ValidatedClocktowerRuleset,
        playerCount: Int,
        gameSeed: Long,
    ): CommittedClocktowerSetup = NoGreaterJoyProductionSetupPreparer.prepare(
        ruleset = ruleset,
        playerCount = playerCount,
        gameSeed = gameSeed,
    )

    private fun teamCounts(
        prepared: CommittedClocktowerSetup,
        ruleset: ValidatedClocktowerRuleset,
    ): Map<ClocktowerCatalogTeam, Int> = prepared.assignments
        .groupingBy { assignment ->
            requireNotNull(ruleset.characterRegistry.findByRoleId(assignment.actualRole)).team
        }
        .eachCount()

    private fun counts(townsfolk: Int, outsiders: Int): Map<ClocktowerCatalogTeam, Int> = buildMap {
        put(ClocktowerCatalogTeam.TOWNSFOLK, townsfolk)
        if (outsiders > 0) put(ClocktowerCatalogTeam.OUTSIDER, outsiders)
        put(ClocktowerCatalogTeam.MINION, 1)
        put(ClocktowerCatalogTeam.DEMON, 1)
    }

    private fun builtInRuleset() = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.NoGreaterJoy)

    private companion object {
        val NGJ_SCRIPT = ScriptId("no_greater_joy")
        const val GENERATED_PROVIDER_ID = "generated-seeded-v1"
    }
}
