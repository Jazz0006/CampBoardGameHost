package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class TroubleBrewingSetupRotationRecordFactoryTest {
    @Test
    fun `setup diversity projects five completed shown rosters regardless of actual Drunk seat`() {
        val first = completedRecord(
            shownRoles = listOf("chef", "empath", "poisoner", "butler", "slayer", "imp"),
            drunkShownRole = "chef",
        )
        val sameShownRosterDifferentDrunkSeat = completedRecord(
            shownRoles = listOf("imp", "slayer", "butler", "poisoner", "empath", "chef"),
            drunkShownRole = "slayer",
        )
        val recent = listOf(
            sameShownRosterDifferentDrunkSeat,
            first,
            first,
            first,
            first,
        )

        val projected = TroubleBrewingSetupRotationHistory(recent)
            .toSetupDiversityHistory(canonicalRegistry())

        assertEquals(5, projected.recentSetups.size)
        assertEquals(
            listOf("chef", "empath", "poisoner", "butler", "slayer", "imp").toSet(),
            projected.recentSetups[0].actualRoles.map { role ->
                requireNotNull(canonicalRegistry().findByRoleId(role)).externalId
            }.toSet(),
        )
        assertEquals(projected.recentSetups[0].actualRoles, projected.recentSetups[1].actualRoles)
        assertNotEquals(first.realNonDemonRoleIds, sameShownRosterDifferentDrunkSeat.realNonDemonRoleIds)
        assertEquals(listOf(100L, 65L, 40L, 20L, 10L), SetupDiversityPolicy().historyWeights)

        val script = ScriptId("trouble_brewing")
        val original = projected.recentSetups[0].actualRoles
        val chef = requireNotNull(canonicalRegistry().findByExternalId("chef")).id
        val washerwoman = requireNotNull(canonicalRegistry().findByExternalId("washerwoman")).id
        val sameShownRoster = SetupCandidate(
            script = script,
            actualRoles = original.reversed(),
            provenance = SetupProvenance(SetupSourceKind.TEMPLATE, "tb-test", "same"),
        )
        val differentShownRoster = SetupCandidate(
            script = script,
            actualRoles = original.filterNot { it == chef } + washerwoman,
            provenance = SetupProvenance(SetupSourceKind.TEMPLATE, "tb-test", "different"),
        )
        val selected = SetupDiversitySelector().select(
            candidates = listOf(sameShownRoster, differentShownRoster),
            history = projected,
            selectionSeed = 17L,
        )
        assertEquals("different", selected.provenance.candidateId)
    }

    private fun completedRecord(
        shownRoles: List<String>,
        drunkShownRole: String,
    ): TroubleBrewingSetupRotationRecord {
        val nonDemonShown = shownRoles.filterNot { it == "imp" }
        val actualNonDemon = nonDemonShown.filterNot { it == drunkShownRole }.toSet() + "drunk"
        val identities = shownRoles.mapIndexed { index, shown ->
            val isDrunk = shown == drunkShownRole
            TroubleBrewingPlayerStartingIdentity(
                playerKey = "P${index + 1}",
                actualRoleId = if (isDrunk) "drunk" else shown,
                shownRoleId = shown,
                actualRoleCategory = when {
                    isDrunk -> TroubleBrewingStartingRoleCategory.OUTSIDER
                    shown == "imp" -> TroubleBrewingStartingRoleCategory.DEMON
                    shown == "poisoner" -> TroubleBrewingStartingRoleCategory.MINION
                    shown == "butler" -> TroubleBrewingStartingRoleCategory.OUTSIDER
                    else -> TroubleBrewingStartingRoleCategory.TOWNSFOLK
                },
            )
        }
        return TroubleBrewingSetupRotationRecord(
            datasetId = "tb-test",
            schemaVersion = 3,
            presetId = "tb-test-$drunkShownRole",
            playerCount = shownRoles.size,
            realNonDemonRoleIds = actualNonDemon,
            minionRoleIds = setOf("poisoner"),
            primaryStyleTag = "balanced",
            playerStartingIdentities = identities,
        )
    }

    @Test
    fun `committed final truth drives rotation record when original townsfolk becomes Drunk`() {
        val preset = TroubleBrewingSetupPreset(
            id = "tb-6-dlb4",
            playerCount = 6,
            townsfolk = listOf("chef", "empath", "washerwoman"),
            outsiders = listOf("saint"),
            minions = listOf("poisoner"),
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            drunk = true,
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
