package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupRotationRecordStoreTest {
    @Test
    fun `restored completion record can be committed without original preset selection`() {
        var raw: String? = null
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; true },
        )
        val record = record(presetId = "tb-8-restored")

        assertTrue(store.recordCompletedGame(gameId = "restored-game", record = record))

        assertEquals(
            listOf(record),
            store.historyFor(record.datasetId, record.schemaVersion, record.playerCount).recentGames,
        )
    }

    @Test
    fun `direct completion record keeps retry idempotency and conflict detection`() {
        var raw: String? = null
        var writes = 0
        val store = TroubleBrewingSetupRotationHistoryStore(
            readRaw = { raw },
            writeRaw = { encoded -> raw = encoded; writes += 1; true },
        )
        val first = record(presetId = "tb-8-a")

        assertTrue(store.recordCompletedGame("stable-game", first))
        assertTrue(store.recordCompletedGame("stable-game", first))
        assertEquals(1, writes)

        assertThrows(IllegalArgumentException::class.java) {
            store.recordCompletedGame("stable-game", record(presetId = "tb-8-b"))
        }
        assertEquals(1, writes)
    }

    private fun record(presetId: String): TroubleBrewingSetupRotationRecord =
        TroubleBrewingSetupRotationRecord(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = presetId,
            playerCount = 8,
            realNonDemonRoleIds = setOf(
                "washerwoman", "librarian", "chef", "empath", "fortune_teller", "butler", "poisoner",
            ),
            minionRoleIds = setOf("poisoner"),
            primaryStyleTag = "balanced",
            selectedDrunkShownRole = null,
        )
}
