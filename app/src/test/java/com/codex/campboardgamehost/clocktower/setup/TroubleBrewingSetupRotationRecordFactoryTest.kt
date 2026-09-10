package com.codex.campboardgamehost.clocktower.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingSetupRotationRecordFactoryTest {
    @Test
    fun `selection becomes compact completion record without template lookup`() {
        val selection = TroubleBrewingSetupPresetSelection(
            datasetId = "tb-dataset",
            schemaVersion = 2,
            presetId = "tb-5-001",
            playerCount = 5,
            gameSeed = 77L,
            preset = TroubleBrewingSetupPreset(
                id = "tb-5-001",
                playerCount = 5,
                townsfolk = listOf("chef", "empath"),
                outsiders = listOf("drunk"),
                minions = listOf("poisoner"),
                demons = listOf("imp"),
                source = "test",
                complexity = "test",
                drunkAsOptions = listOf("investigator"),
                styleTags = listOf("balanced", "information"),
            ),
            selectedDrunkShownRole = "investigator",
        )

        val record = TroubleBrewingSetupRotationRecordFactory.fromSelection(selection)

        assertEquals("tb-dataset", record.datasetId)
        assertEquals(2, record.schemaVersion)
        assertEquals("tb-5-001", record.presetId)
        assertEquals(5, record.playerCount)
        assertEquals(setOf("chef", "empath", "drunk", "poisoner"), record.realNonDemonRoleIds)
        assertEquals(setOf("poisoner"), record.minionRoleIds)
        assertEquals("balanced", record.primaryStyleTag)
        assertEquals("investigator", record.selectedDrunkShownRole)
    }

    @Test
    fun `prepared setup freezes player starting shown identities including Drunk disguise`() {
        val selection = TroubleBrewingSetupPresetSelection(
            datasetId = "tb-dataset",
            schemaVersion = 2,
            presetId = "tb-5-001",
            playerCount = 5,
            gameSeed = 77L,
            preset = TroubleBrewingSetupPreset(
                id = "tb-5-001",
                playerCount = 5,
                townsfolk = listOf("chef", "empath"),
                outsiders = listOf("drunk"),
                minions = listOf("poisoner"),
                demons = listOf("imp"),
                source = "test",
                complexity = "test",
                drunkAsOptions = listOf("investigator"),
                styleTags = listOf("balanced", "information"),
            ),
            selectedDrunkShownRole = "investigator",
        )
        val dealPlan = TroubleBrewingSetupDealPlan(
            datasetId = selection.datasetId,
            schemaVersion = selection.schemaVersion,
            presetId = selection.presetId,
            playerCount = selection.playerCount,
            gameSeed = selection.gameSeed,
            selectedDrunkShownRole = selection.selectedDrunkShownRole,
            assignments = listOf(
                TroubleBrewingSetupDealAssignment(1, "Alice", "chef", "chef"),
                TroubleBrewingSetupDealAssignment(2, "Bob", "empath", "empath"),
                TroubleBrewingSetupDealAssignment(3, "Carol", "drunk", "investigator"),
                TroubleBrewingSetupDealAssignment(4, "David", "poisoner", "poisoner"),
                TroubleBrewingSetupDealAssignment(5, "Emma", "imp", "imp"),
            ),
        )

        val record = TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(
            TroubleBrewingPreparedSetup(selection = selection, dealPlan = dealPlan),
        )

        assertEquals(
            listOf(
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Alice",
                    actualRoleId = "chef",
                    shownRoleId = "chef",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.TOWNSFOLK,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Bob",
                    actualRoleId = "empath",
                    shownRoleId = "empath",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.TOWNSFOLK,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Carol",
                    actualRoleId = "drunk",
                    shownRoleId = "investigator",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.OUTSIDER,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "David",
                    actualRoleId = "poisoner",
                    shownRoleId = "poisoner",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.MINION,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Emma",
                    actualRoleId = "imp",
                    shownRoleId = "imp",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.DEMON,
                ),
            ),
            record.playerStartingIdentities,
        )
    }

    @Test
    fun `persisted completion record rejects missing Drunk shown identity`() {
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupRotationRecordFactory.validate(
                TroubleBrewingSetupRotationRecord(
                    datasetId = "tb-dataset",
                    schemaVersion = 2,
                    presetId = "tb-5-001",
                    playerCount = 5,
                    realNonDemonRoleIds = setOf("chef", "empath", "drunk", "poisoner"),
                    minionRoleIds = setOf("poisoner"),
                    primaryStyleTag = "balanced",
                    selectedDrunkShownRole = null,
                ),
            )
        }
    }

    @Test
    fun `persisted completion record rejects minion outside real role set`() {
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupRotationRecordFactory.validate(
                TroubleBrewingSetupRotationRecord(
                    datasetId = "tb-dataset",
                    schemaVersion = 2,
                    presetId = "tb-5-001",
                    playerCount = 5,
                    realNonDemonRoleIds = setOf("chef", "empath", "butler", "poisoner"),
                    minionRoleIds = setOf("spy"),
                    primaryStyleTag = null,
                    selectedDrunkShownRole = null,
                ),
            )
        }
    }
}
