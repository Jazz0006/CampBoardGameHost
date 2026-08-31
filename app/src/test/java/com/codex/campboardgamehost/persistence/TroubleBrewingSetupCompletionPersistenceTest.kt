package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingSetupCompletionPersistenceTest {
    @Test
    fun `completion record round trip preserves exact diversity metadata without dataset`() {
        val record = record()
        val root = JSONObject().put(
            TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
            TroubleBrewingSetupCompletionPersistence.encode(record),
        )

        assertEquals(record, TroubleBrewingSetupCompletionPersistence.decodeOrNull(root))
    }

    @Test
    fun `missing completion record is optional for non TB or unsupported legacy active save`() {
        assertNull(TroubleBrewingSetupCompletionPersistence.decodeOrNull(JSONObject()))
    }

    @Test
    fun `unsupported completion schema fails explicitly`() {
        val root = JSONObject().put(
            TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
            TroubleBrewingSetupCompletionPersistence.encode(record())
                .put("schemaVersion", 999),
        )

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupCompletionPersistence.decodeOrNull(root)
        }
    }

    @Test
    fun `corrupt Drunk completion cannot be repaired by restore`() {
        val json = TroubleBrewingSetupCompletionPersistence.encode(record())
            .put("selectedDrunkShownRole", JSONObject.NULL)
        val root = JSONObject().put(TroubleBrewingSetupCompletionPersistence.ROOT_KEY, json)

        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingSetupCompletionPersistence.decodeOrNull(root)
        }
    }

    private fun record(): TroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecord(
        datasetId = "trouble_brewing_setup_presets_v2_final",
        schemaVersion = 2,
        presetId = "tb-8-042",
        playerCount = 8,
        realNonDemonRoleIds = setOf(
            "chef", "empath", "fortune_teller", "monk", "drunk", "butler", "poisoner",
        ),
        minionRoleIds = setOf("poisoner"),
        primaryStyleTag = "balanced",
        selectedDrunkShownRole = "investigator",
    )
}
