package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupRotationHistoryStoreTest {
    @Test
    fun `completed setup survives store recreation as selector rotation history`() {
        var raw: String? = null
        val writeRaw: (String) -> Boolean = { encoded ->
            raw = encoded
            true
        }
        val selection = selection(
            gameSeed = 7_001L,
            presetId = "tb-8-history-a",
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
            selection = selection,
        )

        val restored = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = writeRaw,
        ).historyFor(
            datasetId = selection.datasetId,
            schemaVersion = selection.schemaVersion,
            playerCount = selection.playerCount,
        )

        assertEquals(
            listOf(
                TroubleBrewingSetupRotationRecord(
                    datasetId = "test-dataset",
                    schemaVersion = 2,
                    presetId = "tb-8-history-a",
                    playerCount = 8,
                    realNonDemonRoleIds = setOf(
                        "washerwoman",
                        "librarian",
                        "chef",
                        "empath",
                        "fortune_teller",
                        "drunk",
                        "scarlet_woman",
                    ),
                    minionRoleIds = setOf("scarlet_woman"),
                    primaryStyleTag = "balanced",
                    selectedDrunkShownRole = "investigator",
                ),
            ),
            restored.recentGames,
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
        val first = simpleSelection(gameSeed = 8_001L, presetId = "tb-8-idempotent-a", playerCount = 8)

        assertTrue(store.recordCompletedGame(gameId = "stable-game-id", selection = first))
        assertTrue(store.recordCompletedGame(gameId = "stable-game-id", selection = first))
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
                selection = simpleSelection(
                    gameSeed = 8_002L,
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
                selection = simpleSelection(
                    gameSeed = 9_000L + index,
                    presetId = "tb-8-$index",
                    playerCount = 8,
                ),
            )
        }
        val ninePlayer = simpleSelection(
            gameSeed = 9_100L,
            presetId = "tb-9-only",
            playerCount = 9,
        )
        store.recordCompletedGame(gameId = "nine-1", selection = ninePlayer)

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
        val current = simpleSelection(
            gameSeed = 10_001L,
            presetId = "current",
            playerCount = 8,
            datasetId = "dataset-current",
            schemaVersion = 2,
        )
        val otherDataset = simpleSelection(
            gameSeed = 10_002L,
            presetId = "other-dataset",
            playerCount = 8,
            datasetId = "dataset-other",
            schemaVersion = 2,
        )
        val otherSchema = simpleSelection(
            gameSeed = 10_003L,
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

        val recovered = simpleSelection(
            gameSeed = 11_001L,
            presetId = "tb-8-recovered",
            playerCount = 8,
        )
        assertTrue(store.recordCompletedGame("recovered-game", recovered))
        assertEquals(
            listOf("tb-8-recovered"),
            store.historyFor("test-dataset", 2, 8).recentGames.map { it.presetId },
        )
    }

    private fun simpleSelection(
        gameSeed: Long,
        presetId: String,
        playerCount: Int,
        datasetId: String = "test-dataset",
        schemaVersion: Int = 2,
    ): TroubleBrewingSetupPresetSelection {
        val minion = "minion_$presetId".replace('-', '_')
        val townsfolk = (1..playerCount - 2).map { index -> "townsfolk_${presetId}_$index".replace('-', '_') }
        return selection(
            gameSeed = gameSeed,
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

    private fun selection(
        gameSeed: Long,
        presetId: String,
        townsfolk: List<String>,
        outsiders: List<String>,
        minions: List<String>,
        styleTags: List<String>,
        selectedDrunkShownRole: String?,
        playerCount: Int = 8,
        datasetId: String = "test-dataset",
        schemaVersion: Int = 2,
    ) = TroubleBrewingSetupPresetSelection(
        datasetId = datasetId,
        schemaVersion = schemaVersion,
        presetId = presetId,
        playerCount = playerCount,
        gameSeed = gameSeed,
        preset = TroubleBrewingSetupPreset(
            id = presetId,
            playerCount = playerCount,
            townsfolk = townsfolk,
            outsiders = outsiders,
            minions = minions,
            demons = listOf("imp"),
            source = "test",
            complexity = "test",
            styleTags = styleTags,
            drunkAsOptions = if (selectedDrunkShownRole == null) emptyList() else listOf(
                selectedDrunkShownRole,
                "monk",
                "soldier",
            ),
        ),
        selectedDrunkShownRole = selectedDrunkShownRole,
    )
}
