package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.TroubleBrewingGameSnapshotJsonCodec
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.File

class TroubleBrewingGameSnapshotProjectorTest {
    @Test
    fun `G10 precommit projection preserves known shown roles and keeps Townsfolk actual roles uncommitted`() {
        val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
            gameId = "evidence:c1d:g10-game2",
            intermediateSetup = g10Game2IntermediateSetup(),
        )

        assertEquals("botc.tb.game-snapshot", snapshot.schemaId)
        assertEquals(1, snapshot.schemaVersion)
        assertEquals("evidence:c1d:g10-game2", snapshot.gameId)
        assertEquals(ScriptId("trouble_brewing"), snapshot.script)
        assertEquals(20_260_929L, snapshot.gameSeed)

        assertEquals(TroubleBrewingSnapshotStage.SETUP_PRECOMMIT, snapshot.position.stage)
        assertSame(SnapshotField.NotApplicable, snapshot.position.phase)
        assertSame(SnapshotField.NotApplicable, snapshot.position.round)

        assertEquals(SnapshotField.Known(true), snapshot.setupState.hasDrunk)
        assertSame(SnapshotField.Uncommitted, snapshot.setupState.drunkAssignmentSeat)

        assertEquals(
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
            snapshot.grimoireSeats.map { it.seat },
        )
        assertEquals(
            listOf(
                "empath",
                "imp",
                "undertaker",
                "librarian",
                "spy",
                "monk",
                "mayor",
                "virgin",
                "butler",
            ).map { SnapshotField.Known(it) },
            snapshot.grimoireSeats.map { it.shownRoleId },
        )

        val actualBySeat = snapshot.grimoireSeats.associate { it.seat to it.actualRoleId }
        listOf(1, 3, 4, 6, 7, 8).forEach { seat ->
            assertSame(SnapshotField.Uncommitted, actualBySeat.getValue(seat))
        }
        assertEquals(SnapshotField.Known("imp"), actualBySeat.getValue(2))
        assertEquals(SnapshotField.Known("spy"), actualBySeat.getValue(5))
        assertEquals(SnapshotField.Known("butler"), actualBySeat.getValue(9))

        snapshot.grimoireSeats.forEach { seat ->
            assertEquals(SnapshotField.Known(true), seat.alive)
            assertEquals(SnapshotField.Known(false), seat.poisoned)
        }
    }

    @Test
    fun `G10 precommit snapshot matches deterministic V1 interchange fixture and round trips`() {
        val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
            gameId = "evidence:c1d:g10-game2",
            intermediateSetup = g10Game2IntermediateSetup(),
        )
        val encoded = TroubleBrewingGameSnapshotJsonCodec.encode(snapshot)
        val fixture = File(
            "src/test/java/com/codex/campboardgamehost/clocktower/fixtures/" +
                "g10-game2-precommit-tbgs-v1.json",
        ).readText(Charsets.UTF_8).trim()

        assertEquals(fixture, encoded)

        val decoded = TroubleBrewingGameSnapshotJsonCodec.decode(encoded)
        assertEquals(encoded, TroubleBrewingGameSnapshotJsonCodec.encode(decoded))
        assertSame(
            SnapshotField.Uncommitted,
            decoded.grimoireSeats.single { it.seat == 1 }.actualRoleId,
        )
        assertSame(SnapshotField.Uncommitted, decoded.setupState.drunkAssignmentSeat)
    }

    @Test
    fun `G10 committed projection resolves the selected Empath seat as known Drunk`() {
        val registry = canonicalRuleset().characterRegistry
        val snapshot = TroubleBrewingGameSnapshotProjector.fromCommitted(
            gameId = "evidence:c1d:g10-game2",
            committedSetup = g10Game2CommittedSetup(registry),
            characterRegistry = registry,
        )

        assertEquals(TroubleBrewingSnapshotStage.SETUP_COMMITTED, snapshot.position.stage)
        assertEquals(SnapshotField.Known(true), snapshot.setupState.hasDrunk)
        assertEquals(SnapshotField.Known(1), snapshot.setupState.drunkAssignmentSeat)

        val seatOne = snapshot.grimoireSeats.single { it.seat == 1 }
        assertEquals(SnapshotField.Known("empath"), seatOne.shownRoleId)
        assertEquals(SnapshotField.Known("drunk"), seatOne.actualRoleId)

        snapshot.grimoireSeats.drop(1).forEach { seat ->
            assertEquals(seat.shownRoleId, seat.actualRoleId)
        }
    }

    @Test
    fun `runtime projection carries decision position revisions and current mechanical state`() {
        val registry = canonicalRuleset().characterRegistry
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = runtimeGameSnapshot(),
            phase = StorytellerPhase.NIGHT,
            round = 2,
            characterRegistry = registry,
        )

        assertEquals(TroubleBrewingSnapshotStage.RUNTIME, snapshot.position.stage)
        assertEquals(SnapshotField.Known(StorytellerPhase.NIGHT), snapshot.position.phase)
        assertEquals(SnapshotField.Known(2), snapshot.position.round)
        assertEquals(SnapshotField.Known(4L), snapshot.position.gameStateRevision)
        assertEquals(SnapshotField.Known(3L), snapshot.position.playerInputRevision)
        assertEquals(SnapshotField.Known(true), snapshot.setupState.hasDrunk)
        assertEquals(SnapshotField.Known(6), snapshot.setupState.drunkAssignmentSeat)

        val drunkSeat = snapshot.grimoireSeats.single { it.seat == 6 }
        assertEquals(SnapshotField.Known("investigator"), drunkSeat.shownRoleId)
        assertEquals(SnapshotField.Known("drunk"), drunkSeat.actualRoleId)

        assertEquals(
            SnapshotField.Known(false),
            snapshot.grimoireSeats.single { it.seat == 2 }.alive,
        )
        assertEquals(
            SnapshotField.Known(true),
            snapshot.grimoireSeats.single { it.seat == 7 }.poisoned,
        )
    }

    private fun runtimeGameSnapshot(): GameSnapshot {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val runtimeState = base.copy(
            players = base.players.map { player ->
                when (player.seat) {
                    2 -> player.copy(alive = false)
                    7 -> player.copy(poisoned = true)
                    else -> player
                }
            },
        )
        return GameSnapshot(
            gameId = "runtime:tbgs-0",
            gameStateRevision = 4L,
            playerInputRevision = 3L,
            gameSeed = runtimeState.seed,
            rulesetRef = RulesetRef(
                scriptId = ScriptId("trouble_brewing"),
                scriptContentHash = "0123456789abcdef0123456789abcdef",
                rulesetVersion = "trouble-brewing-v1",
                sourceRevision = "tbgs-0-fixture",
                coverage = RuleCoverage.PARTIAL,
            ),
            gameState = runtimeState,
        )
    }

    private fun g10Game2CommittedSetup(
        characterRegistry: ClocktowerCharacterRegistry,
    ): CommittedClocktowerSetup =
        CommittedClocktowerSetup(
            script = ScriptId("trouble_brewing"),
            setupSeed = 20_260_929L,
            assignments = listOf(
                committedSeat(characterRegistry, 1, actual = "drunk", shown = "empath"),
                committedSeat(characterRegistry, 2, actual = "imp", shown = "imp"),
                committedSeat(characterRegistry, 3, actual = "undertaker", shown = "undertaker"),
                committedSeat(characterRegistry, 4, actual = "librarian", shown = "librarian"),
                committedSeat(characterRegistry, 5, actual = "spy", shown = "spy"),
                committedSeat(characterRegistry, 6, actual = "monk", shown = "monk"),
                committedSeat(characterRegistry, 7, actual = "mayor", shown = "mayor"),
                committedSeat(characterRegistry, 8, actual = "virgin", shown = "virgin"),
                committedSeat(characterRegistry, 9, actual = "butler", shown = "butler"),
            ),
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "evidence-replay",
                candidateId = "g10-game2",
            ),
        )

    private fun committedSeat(
        characterRegistry: ClocktowerCharacterRegistry,
        seat: Int,
        actual: String,
        shown: String,
    ): CommittedSetupSeat = CommittedSetupSeat(
        seat = seat,
        actualRole = requireNotNull(characterRegistry.findByExternalId(actual)).id,
        shownRole = requireNotNull(characterRegistry.findByExternalId(shown)).id,
    )

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)

    private fun g10Game2IntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "evidence-c1d-g10",
            schemaVersion = 1,
            presetId = "historical-replay-only:g10-game2",
            playerCount = 9,
            gameSeed = 20_260_929L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf(
                    "empath",
                    "undertaker",
                    "librarian",
                    "monk",
                    "mayor",
                    "virgin",
                ),
                outsiderRoleIds = listOf("butler"),
                minionRoleIds = listOf("spy"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "empath"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "imp"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "undertaker"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "librarian"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "spy"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "monk"),
                TroubleBrewingShownSeatAssignment(7, "Seat 7", "mayor"),
                TroubleBrewingShownSeatAssignment(8, "Seat 8", "virgin"),
                TroubleBrewingShownSeatAssignment(9, "Seat 9", "butler"),
            ),
        )
}
