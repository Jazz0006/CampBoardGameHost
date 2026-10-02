package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktowerRolesForScript
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TroubleBrewingDealRoleResolverTest {
    @Test
    fun `canonical committed Drunk identity resolves exactly to App roles`() {
        val registry = BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        val intermediate = TroubleBrewingIntermediateSetup(
            datasetId = "resolver-test",
            schemaVersion = 2,
            presetId = "resolver-test-preset",
            playerCount = 5,
            gameSeed = 6_003L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
                outsiderRoleIds = emptyList(),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "A", "chef"),
                TroubleBrewingShownSeatAssignment(2, "B", "empath"),
                TroubleBrewingShownSeatAssignment(3, "C", "washerwoman"),
                TroubleBrewingShownSeatAssignment(4, "D", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "E", "imp"),
            ),
        )
        val candidate = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)
            .single { it.shownRoleId == "washerwoman" }
        val committed = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = intermediate,
            confirmedDrunkCandidate = candidate,
            characterRegistry = registry,
        )

        val resolved = TroubleBrewingDealRoleResolver.resolveCommitted(
            committedSetup = committed,
            characterRegistry = registry,
            availableRoles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing),
        )

        assertEquals(listOf(1, 2, 3, 4, 5), resolved.map { it.seat })
        assertEquals(listOf("A", "B", "C", "D", "E"), resolved.map { it.playerName })
        val drunk = resolved.single { it.seat == candidate.seat }
        assertEquals("Drunk", drunk.actualRole.enName)
        assertEquals("Washerwoman", drunk.shownRole.enName)
    }
}
