package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingVisibleRosterSeatPlannerTest {
    @Test
    fun `visible seating is deterministic canonicalized and seed dependent`() {
        val preset = preset()
        val players = (1..5).map { "Player $it" }
        val roster = visibleRoster()
        val reordered = roster.copy(
            townsfolkRoleIds = roster.townsfolkRoleIds.reversed(),
            outsiderRoleIds = roster.outsiderRoleIds.reversed(),
            minionRoleIds = roster.minionRoleIds.reversed(),
            demonRoleIds = roster.demonRoleIds.reversed(),
        )

        fun assignments(currentRoster: TroubleBrewingVisibleRoster, seed: Long) =
            TroubleBrewingSetupDealPlanner.planVisibleRoster(
                datasetId = "test-dataset",
                schemaVersion = 2,
                preset = preset,
                gameSeed = seed,
                visibleRoster = currentRoster,
                orderedPlayerNames = players,
            )

        val first = assignments(roster, 3_101L)
        val replay = assignments(roster, 3_101L)
        val reorderedInput = assignments(reordered, 3_101L)
        val seededAssignments = (3_101L..3_132L).map { seed ->
            assignments(roster, seed)
        }.toSet()

        assertEquals(first, replay)
        assertEquals(first, reorderedInput)
        assertTrue(seededAssignments.size > 1)
        assertEquals(roster.visibleRoleIds.sorted(), first.map { it.shownRoleId }.sorted())
    }

    @Test
    fun `recent exact shown identity is avoided without changing visible role set`() {
        val preset = preset()
        val players = (1..5).map { "Player $it" }
        val baseline = TroubleBrewingSetupDealPlanner.planVisibleRoster(
            datasetId = "test-dataset",
            schemaVersion = 2,
            preset = preset,
            gameSeed = 3_301L,
            visibleRoster = visibleRoster(),
            orderedPlayerNames = players,
        )
        val previousPlayerOne = baseline.first()

        val rotated = TroubleBrewingSetupDealPlanner.planVisibleRoster(
            datasetId = "test-dataset",
            schemaVersion = 2,
            preset = preset,
            gameSeed = 3_301L,
            visibleRoster = visibleRoster(),
            orderedPlayerNames = players,
            recentPlayerStartingIdentityHistory = TroubleBrewingPlayerStartingIdentityHistory(
                recentGames = listOf(
                    listOf(
                        TroubleBrewingPlayerStartingIdentity(
                            playerKey = previousPlayerOne.playerName,
                            actualRoleId = previousPlayerOne.shownRoleId,
                            shownRoleId = previousPlayerOne.shownRoleId,
                            actualRoleCategory = categoryOf(previousPlayerOne.shownRoleId),
                        ),
                    ),
                ),
            ),
        )

        assertNotEquals(previousPlayerOne.shownRoleId, rotated.first().shownRoleId)
        assertEquals(
            baseline.map { it.shownRoleId }.sorted(),
            rotated.map { it.shownRoleId }.sorted(),
        )
    }

    @Test
    fun `last game stays dominant while older exact histories use two to one recency weight`() {
        val preset = preset()
        val players = (1..5).map { "Player $it" }
        val roles = listOf("chef", "empath", "washerwoman", "poisoner", "imp")
        val lastGame = snapshot(players, roles)
        val twoGamesAgo = snapshot(
            players,
            listOf("chef", "empath", "poisoner", "imp", "washerwoman"),
        )
        val threeGamesAgo = snapshot(
            players,
            listOf("chef", "empath", "imp", "washerwoman", "poisoner"),
        )

        val plan = TroubleBrewingSetupDealPlanner.planVisibleRoster(
            datasetId = "test-dataset",
            schemaVersion = 2,
            preset = preset,
            gameSeed = 4_101L,
            visibleRoster = visibleRoster(),
            orderedPlayerNames = players,
            recentPlayerStartingIdentityHistory = TroubleBrewingPlayerStartingIdentityHistory(
                recentGames = listOf(lastGame, twoGamesAgo, threeGamesAgo),
            ),
        )

        assertEquals(0, exactRepeats(plan, lastGame))
        assertEquals(0, exactRepeats(plan, twoGamesAgo))
        assertEquals(1, exactRepeats(plan, threeGamesAgo))
        assertEquals(roles.sorted(), plan.map { it.shownRoleId }.sorted())
    }

    @Test
    fun `older demon category is avoided even when immediate previous game has no identity facts`() {
        val players = (1..5).map { "Player $it" }
        val playerOneOldDemon = TroubleBrewingPlayerStartingIdentity(
            playerKey = "Player 1",
            actualRoleId = "pukka",
            shownRoleId = "pukka",
            actualRoleCategory = TroubleBrewingStartingRoleCategory.DEMON,
        )

        val plan = TroubleBrewingSetupDealPlanner.planVisibleRoster(
            datasetId = "test-dataset",
            schemaVersion = 2,
            preset = preset(),
            gameSeed = 4_102L,
            visibleRoster = visibleRoster(),
            orderedPlayerNames = players,
            recentPlayerStartingIdentityHistory = TroubleBrewingPlayerStartingIdentityHistory(
                recentGames = listOf(
                    emptyList(),
                    listOf(playerOneOldDemon),
                ),
            ),
        )

        assertNotEquals(
            "Player 1",
            plan.single { it.shownRoleId == "imp" }.playerName,
        )
    }

    private fun exactRepeats(
        plan: List<TroubleBrewingShownSeatAssignment>,
        history: List<TroubleBrewingPlayerStartingIdentity>,
    ): Int {
        val previousByPlayer = history.associateBy { it.playerKey }
        return plan.count { assignment ->
            previousByPlayer[assignment.playerName]?.shownRoleId == assignment.shownRoleId
        }
    }

    private fun snapshot(
        players: List<String>,
        assignedRoles: List<String>,
    ): List<TroubleBrewingPlayerStartingIdentity> = players.zip(assignedRoles).map { (player, roleId) ->
        TroubleBrewingPlayerStartingIdentity(
            playerKey = player,
            actualRoleId = roleId,
            shownRoleId = roleId,
            actualRoleCategory = categoryOf(roleId),
        )
    }

    private fun categoryOf(roleId: String): TroubleBrewingStartingRoleCategory = when (roleId) {
        "chef", "empath", "washerwoman" -> TroubleBrewingStartingRoleCategory.TOWNSFOLK
        "poisoner" -> TroubleBrewingStartingRoleCategory.MINION
        "imp", "pukka" -> TroubleBrewingStartingRoleCategory.DEMON
        else -> error("Unknown role $roleId")
    }

    private fun visibleRoster(): TroubleBrewingVisibleRoster = TroubleBrewingVisibleRoster(
        hasDrunk = false,
        townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
        outsiderRoleIds = emptyList(),
        minionRoleIds = listOf("poisoner"),
        demonRoleIds = listOf("imp"),
    )

    private fun preset(): TroubleBrewingSetupPreset = TroubleBrewingSetupPreset(
        id = "tb-visible-seat-five",
        playerCount = 5,
        townsfolk = listOf("chef", "empath", "washerwoman"),
        outsiders = emptyList(),
        minions = listOf("poisoner"),
        demons = listOf("imp"),
        source = "test",
        complexity = "test",
        styleTags = emptyList(),
        drunkAsOptions = emptyList(),
    )
}
