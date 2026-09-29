package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingDrunkCandidateProjectionTest {
    @Test
    fun `drunk setup exposes every and only dealt townsfolk as legal candidates`() {
        val setup = drunkIntermediateSetup()

        val candidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup)

        assertEquals(listOf(1, 3, 5), candidates.map { it.seat })
        assertEquals(listOf("chef", "empath", "washerwoman"), candidates.map { it.shownRoleId })
        assertEquals(listOf("Player 1", "Player 3", "Player 5"), candidates.map { it.playerName })
    }

    @Test
    fun `every legal candidate projects exactly that seat as good outsider drunk while shown identity stays dealt townsfolk`() {
        val setup = drunkIntermediateSetup()
        val registry = canonicalRegistry()
        val candidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup)

        candidates.forEach { candidate ->
            val projection = TroubleBrewingDrunkHypotheticalProjector.project(
                intermediateSetup = setup,
                candidate = candidate,
                characterRegistry = registry,
            )

            assertEquals(candidate, projection.candidate)
            assertEquals(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), projection.gameState.script)
            assertEquals(setup.gameSeed, projection.gameState.seed)

            val projectedCandidate = requireNotNull(projection.gameState.playerAt(candidate.seat))
            assertEquals(RoleId("Drunk"), projectedCandidate.actualRole)
            assertEquals(CharacterType.OUTSIDER, projectedCandidate.actualType)
            assertEquals(Alignment.GOOD, projectedCandidate.actualAlignment)
            assertEquals(
                requireNotNull(registry.findByExternalId(candidate.shownRoleId)).id,
                projectedCandidate.shownRole,
            )
            assertTrue(projectedCandidate.alive)
            assertFalse(projectedCandidate.poisoned)

            projection.gameState.players
                .filterNot { it.seat == candidate.seat }
                .forEach { player ->
                    assertEquals(player.shownRole, player.actualRole)
                }
        }
    }

    @Test
    fun `choosing an original townsfolk leaves transitional visible townsfolk as an actual townsfolk`() {
        val setup = drunkIntermediateSetup()
        val registry = canonicalRegistry()
        val candidate = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup)
            .single { it.shownRoleId == "chef" }

        val projection = TroubleBrewingDrunkHypotheticalProjector.project(
            intermediateSetup = setup,
            candidate = candidate,
            characterRegistry = registry,
        )

        val transitional = projection.gameState.players.single {
            it.shownRole == requireNotNull(registry.findByExternalId("washerwoman")).id
        }
        assertEquals(requireNotNull(registry.findByExternalId("washerwoman")).id, transitional.actualRole)
        assertEquals(CharacterType.TOWNSFOLK, transitional.actualType)
        assertEquals(Alignment.GOOD, transitional.actualAlignment)
    }

    @Test
    fun `non drunk setup has no candidates and non townsfolk candidate is rejected without mutating input`() {
        val noDrunk = nonDrunkIntermediateSetup()
        assertTrue(TroubleBrewingDrunkCandidateDomain.legalCandidates(noDrunk).isEmpty())

        val setup = drunkIntermediateSetup()
        val before = setup.copy(
            visibleRoster = setup.visibleRoster.copy(),
            shownSeatAssignments = setup.shownSeatAssignments.map { it.copy() },
        )
        val illegal = TroubleBrewingDrunkCandidate(
            seat = 2,
            playerName = "Player 2",
            shownRoleId = "saint",
        )

        expectIllegalArgument {
            TroubleBrewingDrunkHypotheticalProjector.project(
                intermediateSetup = setup,
                candidate = illegal,
                characterRegistry = canonicalRegistry(),
            )
        }
        assertEquals(before, setup)

        val legal = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup).first()
        val first = TroubleBrewingDrunkHypotheticalProjector.project(
            intermediateSetup = setup,
            candidate = legal,
            characterRegistry = canonicalRegistry(),
        )
        val second = TroubleBrewingDrunkHypotheticalProjector.project(
            intermediateSetup = setup,
            candidate = legal,
            characterRegistry = canonicalRegistry(),
        )
        assertEquals(first, second)
        assertEquals(before, setup)
    }

    private fun drunkIntermediateSetup(): TroubleBrewingIntermediateSetup {
        val visibleRoster = TroubleBrewingVisibleRoster(
            hasDrunk = true,
            townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
            outsiderRoleIds = listOf("saint"),
            minionRoleIds = listOf("poisoner"),
            demonRoleIds = listOf("imp"),
        )
        return TroubleBrewingIntermediateSetup(
            datasetId = "dlb2-test",
            schemaVersion = 1,
            presetId = "dlb2-drunk-six",
            playerCount = 6,
            gameSeed = 8_201L,
            visibleRoster = visibleRoster,
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Player 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Player 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Player 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Player 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Player 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Player 6", "imp"),
            ),
        )
    }

    private fun nonDrunkIntermediateSetup(): TroubleBrewingIntermediateSetup {
        val visibleRoster = TroubleBrewingVisibleRoster(
            hasDrunk = false,
            townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
            outsiderRoleIds = listOf("saint"),
            minionRoleIds = listOf("poisoner"),
            demonRoleIds = listOf("imp"),
        )
        return TroubleBrewingIntermediateSetup(
            datasetId = "dlb2-test",
            schemaVersion = 1,
            presetId = "dlb2-no-drunk-six",
            playerCount = 6,
            gameSeed = 8_202L,
            visibleRoster = visibleRoster,
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Player 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Player 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Player 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Player 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Player 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Player 6", "imp"),
            ),
        )
    }

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }

    private fun expectIllegalArgument(block: () -> Unit) {
        var thrown = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            thrown = true
        }
        assertTrue("Expected IllegalArgumentException", thrown)
    }
}
