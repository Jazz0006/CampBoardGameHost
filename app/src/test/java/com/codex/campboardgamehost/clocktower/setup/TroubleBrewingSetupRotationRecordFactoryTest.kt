package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

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

        val visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
            preset = selection.preset,
            addedVisibleTownsfolkRoleId = selection.selectedDrunkShownRole,
        )
        val record = TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(
            TroubleBrewingPreparedSetup(
                preset = selection.preset,
                intermediateSetup = TroubleBrewingIntermediateSetup(
                    datasetId = selection.datasetId,
                    schemaVersion = selection.schemaVersion,
                    presetId = selection.presetId,
                    playerCount = selection.playerCount,
                    gameSeed = selection.gameSeed,
                    visibleRoster = visibleRoster,
                    shownSeatAssignments = dealPlan.assignments.map { assignment ->
                        TroubleBrewingShownSeatAssignment(
                            seat = assignment.seat,
                            playerName = assignment.playerName,
                            shownRoleId = assignment.shownRoleId,
                        )
                    },
                ),
                compatibilityDealPlan = dealPlan,
            ),
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
    fun `committed final truth drives rotation record when original townsfolk becomes Drunk`() {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-6-dlb4",
            playerCount = 6,
            townsfolk = listOf("chef", "empath"),
            outsiders = listOf("drunk", "saint"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            drunkAsOptions = listOf("washerwoman"),
            styleTags = listOf("balanced"),
        )
        val intermediate = TroubleBrewingIntermediateSetup(
            datasetId = "tb-dataset",
            schemaVersion = 2,
            presetId = preset.id,
            playerCount = 6,
            gameSeed = 88L,
            visibleRoster = TroubleBrewingVisibleRosterRealizer.realize(
                preset = preset,
                addedVisibleTownsfolkRoleId = "washerwoman",
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Alice", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Bob", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Carol", "empath"),
                TroubleBrewingShownSeatAssignment(4, "David", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Emma", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Frank", "imp"),
            ),
        )
        val compatibilityDealPlan = TroubleBrewingCompatibilityDealPlanAdapter.fromIntermediate(
            preset = preset,
            intermediateSetup = intermediate,
            addedVisibleTownsfolkRoleId = "washerwoman",
        )
        val prepared = TroubleBrewingPreparedSetup(
            preset = preset,
            intermediateSetup = intermediate,
            compatibilityDealPlan = compatibilityDealPlan,
        )
        val registry = canonicalRegistry()
        val chefCandidate = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)
            .single { it.shownRoleId == "chef" }
        val committed = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = intermediate,
            confirmedDrunkCandidate = chefCandidate,
            characterRegistry = registry,
        )

        val record = TroubleBrewingSetupRotationRecordFactory.fromCommittedSetup(
            preparedSetup = prepared,
            committedSetup = committed,
            characterRegistry = registry,
        )

        assertEquals(
            setOf("drunk", "saint", "empath", "poisoner", "washerwoman"),
            record.realNonDemonRoleIds,
        )
        assertEquals(setOf("poisoner"), record.minionRoleIds)
        assertEquals("chef", record.selectedDrunkShownRole)
        assertEquals(
            listOf(
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Alice",
                    actualRoleId = "drunk",
                    shownRoleId = "chef",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.OUTSIDER,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Bob",
                    actualRoleId = "saint",
                    shownRoleId = "saint",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.OUTSIDER,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Carol",
                    actualRoleId = "empath",
                    shownRoleId = "empath",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.TOWNSFOLK,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "David",
                    actualRoleId = "poisoner",
                    shownRoleId = "poisoner",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.MINION,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Emma",
                    actualRoleId = "washerwoman",
                    shownRoleId = "washerwoman",
                    actualRoleCategory = TroubleBrewingStartingRoleCategory.TOWNSFOLK,
                ),
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = "Frank",
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

    private fun canonicalRegistry(): ClocktowerCharacterRegistry {
        val assetRoot = File("src/main/assets")
        return BuiltInClocktowerRulesetCatalog { assetPath ->
            File(assetRoot, assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
}
