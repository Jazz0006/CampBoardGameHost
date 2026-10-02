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
        val prepared = TroubleBrewingPreparedSetup(
            preset = preset,
            intermediateSetup = intermediate,
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
