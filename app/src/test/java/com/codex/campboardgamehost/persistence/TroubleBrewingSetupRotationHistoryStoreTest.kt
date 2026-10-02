package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupRotationHistoryStoreTest {
    @Test
    fun `completed setup survives store recreation as rotation history`() {
        var raw: String? = null
        val writeRaw: (String) -> Boolean = { encoded ->
            raw = encoded
            true
        }
        val record = record(
            presetId = "tb-8-history-a",
            playerCount = 8,
            townsfolk = listOf("washerwoman", "librarian", "chef", "empath", "fortune_teller"),
            outsiders = listOf("drunk"),
            minions = listOf("scarlet_woman"),
            styleTags = listOf("balanced", "information"),
            selectedDrunkShownRole = "investigator",
        )

        TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = writeRaw,
        ).recordCompletedGame(
            gameId = "game-1",
            record = record,
        )

        val restored = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = writeRaw,
        ).historyFor(
            datasetId = record.datasetId,
            schemaVersion = record.schemaVersion,
            playerCount = record.playerCount,
        )

        assertEquals(listOf(record), restored.recentGames)
    }

    @Test
    fun `completed setup starting identities survive store recreation`() {
        var raw: String? = null
        val writeRaw: (String) -> Boolean = { encoded ->
            raw = encoded
            true
        }
        val record = recordWithStartingIdentities()

        TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = writeRaw,
        ).recordCompletedGame(
            gameId = "identity-game",
            record = record,
        )

        val restored = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = writeRaw,
        ).historyFor(
            datasetId = record.datasetId,
            schemaVersion = record.schemaVersion,
            playerCount = record.playerCount,
        )

        assertEquals(listOf(record), restored.recentGames)
    }

    @Test
    fun `latest starting identities follow the immediately previous game across player counts`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        val olderFive = recordWithStartingIdentities().copy(presetId = "older-five")
        val newerSix = sixPlayerRecordWithStartingIdentities()

        store.recordCompletedGame("older-five-game", olderFive)
        store.recordCompletedGame("newer-six-game", newerSix)

        assertEquals(
            newerSix.playerStartingIdentities,
            store.latestPlayerStartingIdentitiesFor(
                datasetId = "test-dataset",
                schemaVersion = 2,
            ),
        )
    }

    @Test
    fun `latest starting identities do not skip an intervening completion without identity history`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        store.recordCompletedGame("older-identity-game", recordWithStartingIdentities())
        store.recordCompletedGame(
            "newer-no-identities-game",
            simpleRecord(
                presetId = "newer-no-identities",
                playerCount = 6,
            ),
        )

        assertTrue(
            store.latestPlayerStartingIdentitiesFor(
                datasetId = "test-dataset",
                schemaVersion = 2,
            ).isEmpty(),
        )
    }

    @Test
    fun `completion retry is idempotent and conflicting reuse of game id is rejected`() {
        var raw: String? = null
        var writeCount = 0
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded ->
                raw = encoded
                writeCount += 1
                true
            },
        )
        val first = simpleRecord(presetId = "tb-8-idempotent-a", playerCount = 8)

        assertTrue(store.recordCompletedGame(gameId = "stable-game-id", record = first))
        assertTrue(store.recordCompletedGame(gameId = "stable-game-id", record = first))
        assertEquals(1, writeCount)
        assertEquals(
            listOf("tb-8-idempotent-a"),
            store.historyFor(first.datasetId, first.schemaVersion, first.playerCount)
                .recentGames
                .map { it.presetId },
        )

        assertThrows(IllegalArgumentException::class.java) {
            store.recordCompletedGame(
                gameId = "stable-game-id",
                record = simpleRecord(
                    presetId = "tb-8-idempotent-conflict",
                    playerCount = 8,
                ),
            )
        }
        assertEquals(1, writeCount)
    }

    @Test
    fun `history is newest first bounded to five per player count and isolated across player counts`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )

        (1..6).forEach { index ->
            store.recordCompletedGame(
                gameId = "eight-$index",
                record = simpleRecord(
                    presetId = "tb-8-$index",
                    playerCount = 8,
                ),
            )
        }
        val ninePlayer = simpleRecord(
            presetId = "tb-9-only",
            playerCount = 9,
        )
        store.recordCompletedGame(gameId = "nine-1", record = ninePlayer)

        assertEquals(
            listOf("tb-8-6", "tb-8-5", "tb-8-4", "tb-8-3", "tb-8-2"),
            store.historyFor("test-dataset", 2, 8).recentGames.map { it.presetId },
        )
        assertEquals(
            listOf("tb-9-only"),
            store.historyFor("test-dataset", 2, 9).recentGames.map { it.presetId },
        )
    }

    @Test
    fun `history projection is isolated by dataset and schema`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        val current = simpleRecord(
            presetId = "current",
            playerCount = 8,
            datasetId = "dataset-current",
            schemaVersion = 2,
        )
        val otherDataset = simpleRecord(
            presetId = "other-dataset",
            playerCount = 8,
            datasetId = "dataset-other",
            schemaVersion = 2,
        )
        val otherSchema = simpleRecord(
            presetId = "other-schema",
            playerCount = 8,
            datasetId = "dataset-current",
            schemaVersion = 3,
        )

        store.recordCompletedGame("game-current", current)
        store.recordCompletedGame("game-other-dataset", otherDataset)
        store.recordCompletedGame("game-other-schema", otherSchema)

        assertEquals(
            listOf("current"),
            store.historyFor("dataset-current", 2, 8).recentGames.map { it.presetId },
        )
    }

    @Test
    fun `malformed or unsupported persisted history fails soft and next completion replaces it`() {
        var raw: String? = "{not-json"
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )

        assertTrue(store.historyFor("test-dataset", 2, 8).recentGames.isEmpty())

        raw = "{\"version\":999,\"entries\":[]}"
        assertTrue(store.historyFor("test-dataset", 2, 8).recentGames.isEmpty())

        val recovered = simpleRecord(
            presetId = "tb-8-recovered",
            playerCount = 8,
        )
        assertTrue(store.recordCompletedGame("recovered-game", recovered))
        assertEquals(
            listOf("tb-8-recovered"),
            store.historyFor("test-dataset", 2, 8).recentGames.map { it.presetId },
        )
    }

    private fun recordWithStartingIdentities(): TroubleBrewingSetupRotationRecord =
        TroubleBrewingSetupRotationRecord(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = "identity-five",
            playerCount = 5,
            realNonDemonRoleIds = setOf("chef", "empath", "butler", "poisoner"),
            minionRoleIds = setOf("poisoner"),
            primaryStyleTag = "identity-test",
            selectedDrunkShownRole = null,
            playerStartingIdentities = listOf(
                startingIdentity("Alice", "chef", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Bob", "empath", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Carol", "butler", TroubleBrewingStartingRoleCategory.OUTSIDER),
                startingIdentity("David", "poisoner", TroubleBrewingStartingRoleCategory.MINION),
                startingIdentity("Emma", "imp", TroubleBrewingStartingRoleCategory.DEMON),
            ),
        )

    private fun sixPlayerRecordWithStartingIdentities(): TroubleBrewingSetupRotationRecord =
        TroubleBrewingSetupRotationRecord(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = "identity-six",
            playerCount = 6,
            realNonDemonRoleIds = setOf("chef", "empath", "washerwoman", "butler", "poisoner"),
            minionRoleIds = setOf("poisoner"),
            primaryStyleTag = "identity-test",
            selectedDrunkShownRole = null,
            playerStartingIdentities = listOf(
                startingIdentity("Alice", "chef", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Bob", "empath", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Carol", "washerwoman", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("David", "butler", TroubleBrewingStartingRoleCategory.OUTSIDER),
                startingIdentity("Emma", "poisoner", TroubleBrewingStartingRoleCategory.MINION),
                startingIdentity("Frank", "imp", TroubleBrewingStartingRoleCategory.DEMON),
            ),
        )

    private fun startingIdentity(
        playerKey: String,
        roleId: String,
        category: TroubleBrewingStartingRoleCategory,
    ): TroubleBrewingPlayerStartingIdentity = TroubleBrewingPlayerStartingIdentity(
        playerKey = playerKey,
        actualRoleId = roleId,
        shownRoleId = roleId,
        actualRoleCategory = category,
    )

    private fun simpleRecord(
        presetId: String,
        playerCount: Int,
        datasetId: String = "test-dataset",
        schemaVersion: Int = 2,
    ): TroubleBrewingSetupRotationRecord {
        val minion = "minion_$presetId".replace('-', '_')
        val townsfolk = (1..playerCount - 2).map { index ->
            "townsfolk_${presetId}_$index".replace('-', '_')
        }
        return record(
            presetId = presetId,
            playerCount = playerCount,
            datasetId = datasetId,
            schemaVersion = schemaVersion,
            townsfolk = townsfolk,
            outsiders = emptyList(),
            minions = listOf(minion),
            styleTags = listOf("style-$presetId"),
            selectedDrunkShownRole = null,
        )
    }

    private fun record(
        presetId: String,
        playerCount: Int,
        townsfolk: List<String>,
        outsiders: List<String>,
        minions: List<String>,
        styleTags: List<String>,
        selectedDrunkShownRole: String?,
        datasetId: String = "test-dataset",
        schemaVersion: Int = 2,
    ): TroubleBrewingSetupRotationRecord =
        TroubleBrewingSetupRotationRecord(
            datasetId = datasetId,
            schemaVersion = schemaVersion,
            presetId = presetId,
            playerCount = playerCount,
            realNonDemonRoleIds = (townsfolk + outsiders + minions).toSet(),
            minionRoleIds = minions.toSet(),
            primaryStyleTag = styleTags.firstOrNull(),
            selectedDrunkShownRole = selectedDrunkShownRole,
        )
}
