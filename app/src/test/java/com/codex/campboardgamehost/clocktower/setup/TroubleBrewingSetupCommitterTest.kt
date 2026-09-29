package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingSetupCommitterTest {
    @Test
    fun `legal original townsfolk candidate commits exactly one Drunk while preserving shown identities`() {
        val setup = drunkIntermediateSetup()
        val before = setup.copy(
            visibleRoster = setup.visibleRoster.copy(),
            shownSeatAssignments = setup.shownSeatAssignments.map { it.copy() },
        )
        val candidate = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup)
            .single { it.shownRoleId == "chef" }

        val committed = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = setup,
            confirmedDrunkCandidate = candidate,
            characterRegistry = canonicalRegistry(),
        )

        assertEquals(candidate, committed.confirmedDrunkCandidate)
        assertEquals(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), committed.gameState.script)
        assertEquals(setup.gameSeed, committed.gameState.seed)
        assertEquals(setup.gameSeed, committed.committedSetup.setupSeed)
        assertEquals(setup.playerCount, committed.committedSetup.playerCount)

        val drunkPlayer = committed.gameState.players.single { it.actualRole == RoleId("Drunk") }
        assertEquals(candidate.seat, drunkPlayer.seat)
        assertEquals(RoleId("Chef"), drunkPlayer.shownRole)
        assertEquals(1, committed.gameState.players.count { it.actualRole == RoleId("Drunk") })

        val transitional = committed.gameState.players.single { it.shownRole == RoleId("Washerwoman") }
        assertEquals(RoleId("Washerwoman"), transitional.actualRole)

        committed.gameState.players
            .filterNot { it.seat == candidate.seat }
            .forEach { player -> assertEquals(player.shownRole, player.actualRole) }

        assertEquals(
            committed.gameState.players.map { player ->
                Triple(player.seat, player.actualRole, player.shownRole)
            },
            committed.committedSetup.assignments.map { assignment ->
                Triple(assignment.seat, assignment.actualRole, assignment.shownRole)
            },
        )
        assertEquals(before, setup)
    }

    @Test
    fun `stale candidate fails closed against the current intermediate setup legal domain`() {
        val setup = drunkIntermediateSetup()
        val legal = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup).first()
        val stale = legal.copy(playerName = "Stale Player")

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupCommitter.commit(
                intermediateSetup = setup,
                confirmedDrunkCandidate = stale,
                characterRegistry = canonicalRegistry(),
            )
        }
    }

    @Test
    fun `drunk setup requires a confirmed candidate`() {
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupCommitter.commit(
                intermediateSetup = drunkIntermediateSetup(),
                confirmedDrunkCandidate = null,
                characterRegistry = canonicalRegistry(),
            )
        }
    }

    @Test
    fun `non drunk setup forbids a candidate and commits actual equal shown for every seat`() {
        val setup = nonDrunkIntermediateSetup()
        val registry = canonicalRegistry()
        val foreignCandidate = TroubleBrewingDrunkCandidate(
            seat = 1,
            playerName = "Player 1",
            shownRoleId = "chef",
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupCommitter.commit(
                intermediateSetup = setup,
                confirmedDrunkCandidate = foreignCandidate,
                characterRegistry = registry,
            )
        }

        val committed = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = setup,
            confirmedDrunkCandidate = null,
            characterRegistry = registry,
        )

        assertEquals(null, committed.confirmedDrunkCandidate)
        assertTrue(committed.gameState.players.none { it.actualRole == RoleId("Drunk") })
        committed.gameState.players.forEach { player ->
            assertEquals(player.shownRole, player.actualRole)
        }
        assertEquals(
            committed.gameState.players.map { player ->
                Triple(player.seat, player.actualRole, player.shownRole)
            },
            committed.committedSetup.assignments.map { assignment ->
                Triple(assignment.seat, assignment.actualRole, assignment.shownRole)
            },
        )
    }

    private fun drunkIntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "dlb4-test",
            schemaVersion = 1,
            presetId = "dlb4-drunk-six",
            playerCount = 6,
            gameSeed = 9_401L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
                outsiderRoleIds = listOf("saint"),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Player 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Player 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Player 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Player 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Player 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Player 6", "imp"),
            ),
        )

    private fun nonDrunkIntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "dlb4-test",
            schemaVersion = 1,
            presetId = "dlb4-no-drunk-six",
            playerCount = 6,
            gameSeed = 9_402L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = false,
                townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
                outsiderRoleIds = listOf("saint"),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Player 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Player 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Player 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Player 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Player 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Player 6", "imp"),
            ),
        )

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
}
