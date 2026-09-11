package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TroubleBrewingSetupDealPlannerRecentHistoryTest {
    @Test
    fun `last game stays dominant while two older exact histories use two to one recency weight`() {
        val preset = preset()
        val selection = selection(preset, gameSeed = 4_101L)
        val players = (1..5).map { "Player $it" }
        val roles = listOf("chef", "empath", "washerwoman", "poisoner", "imp")
        val lastGame = snapshot(players, roles, preset)
        val twoGamesAgo = snapshot(
            players,
            listOf("chef", "empath", "poisoner", "imp", "washerwoman"),
            preset,
        )
        val threeGamesAgo = snapshot(
            players,
            listOf("chef", "empath", "imp", "washerwoman", "poisoner"),
            preset,
        )

        val plan = TroubleBrewingSetupDealPlanner.plan(
            selection = selection,
            orderedPlayerNames = players,
            recentPlayerStartingIdentityHistory = TroubleBrewingPlayerStartingIdentityHistory(
                recentGames = listOf(lastGame, twoGamesAgo, threeGamesAgo),
            ),
        )

        assertEquals(0, exactRepeats(plan, lastGame))
        assertEquals(0, exactRepeats(plan, twoGamesAgo))
        assertEquals(1, exactRepeats(plan, threeGamesAgo))
        assertEquals(roles.sorted(), plan.assignments.map { it.actualRoleId }.sorted())
    }

    @Test
    fun `demon from two games ago is weakly avoided even when immediate previous game has no identity facts`() {
        val preset = preset()
        val selection = selection(preset, gameSeed = 4_102L)
        val players = (1..5).map { "Player $it" }
        val playerOneOldDemon = TroubleBrewingPlayerStartingIdentity(
            playerKey = "Player 1",
            actualRoleId = "pukka",
            shownRoleId = "pukka",
            actualRoleCategory = TroubleBrewingStartingRoleCategory.DEMON,
        )

        val plan = TroubleBrewingSetupDealPlanner.plan(
            selection = selection,
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
            plan.assignments.single { it.actualRoleId == "imp" }.playerName,
        )
    }

    private fun exactRepeats(
        plan: TroubleBrewingSetupDealPlan,
        history: List<TroubleBrewingPlayerStartingIdentity>,
    ): Int {
        val previousByPlayer = history.associateBy { it.playerKey }
        return plan.assignments.count { assignment ->
            previousByPlayer[assignment.playerName]?.shownRoleId == assignment.shownRoleId
        }
    }

    private fun snapshot(
        players: List<String>,
        assignedRoles: List<String>,
        preset: TroubleBrewingSetupPreset,
    ): List<TroubleBrewingPlayerStartingIdentity> = players.zip(assignedRoles).map { (player, roleId) ->
        TroubleBrewingPlayerStartingIdentity(
            playerKey = player,
            actualRoleId = roleId,
            shownRoleId = roleId,
            actualRoleCategory = categoryOf(preset, roleId),
        )
    }

    private fun categoryOf(
        preset: TroubleBrewingSetupPreset,
        roleId: String,
    ): TroubleBrewingStartingRoleCategory = when (roleId) {
        in preset.townsfolk -> TroubleBrewingStartingRoleCategory.TOWNSFOLK
        in preset.outsiders -> TroubleBrewingStartingRoleCategory.OUTSIDER
        in preset.minions -> TroubleBrewingStartingRoleCategory.MINION
        in preset.demons -> TroubleBrewingStartingRoleCategory.DEMON
        else -> error("Unknown role $roleId")
    }

    private fun preset(): TroubleBrewingSetupPreset = TroubleBrewingSetupPreset(
        id = "tbsp-recent-rotation-five",
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

    private fun selection(
        preset: TroubleBrewingSetupPreset,
        gameSeed: Long,
    ): TroubleBrewingSetupPresetSelection = TroubleBrewingSetupPresetSelection(
        datasetId = "test-dataset",
        schemaVersion = 2,
        presetId = preset.id,
        playerCount = preset.playerCount,
        gameSeed = gameSeed,
        preset = preset,
        selectedDrunkShownRole = null,
    )
}
