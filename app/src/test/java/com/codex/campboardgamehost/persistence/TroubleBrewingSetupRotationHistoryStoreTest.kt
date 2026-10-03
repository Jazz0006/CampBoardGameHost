package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupRotationHistoryStoreTest {
    @Test
    fun `completed setup survives store recreation as rotation history`() {
        var raw: String? = null
        val record = simpleRecord("tb-8-history-a", 8)

        TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        ).recordCompletedGame("game-1", record)

        val restored = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        ).historyFor(record.datasetId, record.schemaVersion, record.playerCount)

        assertEquals(listOf(record), restored.recentGames)
    }

    @Test
    fun `new rotation history writes v3 without legacy Drunk shown field`() {
        var raw: String? = null
        val record = drunkRecordWithStartingIdentities()
        TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        ).recordCompletedGame("v3-game", record)

        val json = JSONObject(requireNotNull(raw))
        assertEquals(3, json.getInt("version"))
        val entry = json.getJSONArray("entries").getJSONObject(0)
        assertFalse(entry.has("selectedDrunkShownRole"))
    }

    @Test
    fun `legacy v2 rotation history migrates through canonical starting identities`() {
        var raw: String? = null
        val record = drunkRecordWithStartingIdentities()
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        store.recordCompletedGame("legacy-game", record)

        val legacy = JSONObject(requireNotNull(raw))
        legacy.put("version", 2)
        legacy.getJSONArray("entries").getJSONObject(0)
            .put("selectedDrunkShownRole", "investigator")
        raw = legacy.toString()

        val restored = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        ).historyFor(record.datasetId, record.schemaVersion, record.playerCount)

        assertEquals(listOf(record), restored.recentGames)
    }

    @Test
    fun `completed setup starting identities survive store recreation`() {
        var raw: String? = null
        val record = recordWithStartingIdentities()
        TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        ).recordCompletedGame("identity-game", record)

        val restored = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        ).historyFor(record.datasetId, record.schemaVersion, record.playerCount)

        assertEquals(listOf(record), restored.recentGames)
    }

    @Test
    fun `latest starting identities follow immediately previous game across player counts`() {
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
            store.latestPlayerStartingIdentitiesFor("test-dataset", 2),
        )
    }

    @Test
    fun `latest starting identities do not skip intervening completion without identity facts`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        store.recordCompletedGame("older-identity-game", recordWithStartingIdentities())
        store.recordCompletedGame("newer-no-identities-game", simpleRecord("newer-no-identities", 6))

        assertTrue(store.latestPlayerStartingIdentitiesFor("test-dataset", 2).isEmpty())
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
        val first = simpleRecord("tb-8-idempotent-a", 8)

        assertTrue(store.recordCompletedGame("stable-game-id", first))
        assertTrue(store.recordCompletedGame("stable-game-id", first))
        assertEquals(1, writeCount)

        assertThrows(IllegalArgumentException::class.java) {
            store.recordCompletedGame(
                "stable-game-id",
                simpleRecord("tb-8-idempotent-conflict", 8),
            )
        }
        assertEquals(1, writeCount)
    }

    @Test
    fun `history is newest first bounded to five per player count and isolated by player count`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        (1..6).forEach { index ->
            store.recordCompletedGame("eight-$index", simpleRecord("tb-8-$index", 8))
        }
        store.recordCompletedGame("nine-1", simpleRecord("tb-9-only", 9))

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
        store.recordCompletedGame(
            "game-current",
            simpleRecord("current", 8, datasetId = "dataset-current", schemaVersion = 2),
        )
        store.recordCompletedGame(
            "game-other-dataset",
            simpleRecord("other-dataset", 8, datasetId = "dataset-other", schemaVersion = 2),
        )
        store.recordCompletedGame(
            "game-other-schema",
            simpleRecord("other-schema", 8, datasetId = "dataset-current", schemaVersion = 3),
        )

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

        val recovered = simpleRecord("tb-8-recovered", 8)
        assertTrue(store.recordCompletedGame("recovered-game", recovered))
        assertEquals(
            listOf("tb-8-recovered"),
            store.historyFor("test-dataset", 2, 8).recentGames.map { it.presetId },
        )
    }

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
        return TroubleBrewingSetupRotationRecord(
            datasetId = datasetId,
            schemaVersion = schemaVersion,
            presetId = presetId,
            playerCount = playerCount,
            realNonDemonRoleIds = (townsfolk + minion).toSet(),
            minionRoleIds = setOf(minion),
            primaryStyleTag = "style-$presetId",
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
            playerStartingIdentities = listOf(
                startingIdentity("Alice", "chef", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Bob", "empath", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Carol", "washerwoman", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("David", "butler", TroubleBrewingStartingRoleCategory.OUTSIDER),
                startingIdentity("Emma", "poisoner", TroubleBrewingStartingRoleCategory.MINION),
                startingIdentity("Frank", "imp", TroubleBrewingStartingRoleCategory.DEMON),
            ),
        )

    private fun drunkRecordWithStartingIdentities(): TroubleBrewingSetupRotationRecord =
        TroubleBrewingSetupRotationRecord(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = "identity-drunk-five",
            playerCount = 5,
            realNonDemonRoleIds = setOf("chef", "empath", "drunk", "poisoner"),
            minionRoleIds = setOf("poisoner"),
            primaryStyleTag = "identity-test",
            playerStartingIdentities = listOf(
                startingIdentity("Alice", "chef", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity("Bob", "empath", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
                startingIdentity(
                    playerKey = "Carol",
                    actualRoleId = "drunk",
                    category = TroubleBrewingStartingRoleCategory.OUTSIDER,
                    shownRoleId = "investigator",
                ),
                startingIdentity("David", "poisoner", TroubleBrewingStartingRoleCategory.MINION),
                startingIdentity("Emma", "imp", TroubleBrewingStartingRoleCategory.DEMON),
            ),
        )

    private fun startingIdentity(
        playerKey: String,
        actualRoleId: String,
        category: TroubleBrewingStartingRoleCategory,
        shownRoleId: String = actualRoleId,
    ): TroubleBrewingPlayerStartingIdentity = TroubleBrewingPlayerStartingIdentity(
        playerKey = playerKey,
        actualRoleId = actualRoleId,
        shownRoleId = shownRoleId,
        actualRoleCategory = category,
    )
}
