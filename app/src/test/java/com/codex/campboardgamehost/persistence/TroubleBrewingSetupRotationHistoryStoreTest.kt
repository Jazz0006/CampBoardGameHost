package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import org.junit.Assert.assertEquals
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

    private fun selection(
        gameSeed: Long,
        presetId: String,
        townsfolk: List<String>,
        outsiders: List<String>,
        minions: List<String>,
        styleTags: List<String>,
        selectedDrunkShownRole: String?,
    ) = TroubleBrewingSetupPresetSelection(
        datasetId = "test-dataset",
        schemaVersion = 2,
        presetId = presetId,
        playerCount = 8,
        gameSeed = gameSeed,
        preset = TroubleBrewingSetupPreset(
            id = presetId,
            playerCount = 8,
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
