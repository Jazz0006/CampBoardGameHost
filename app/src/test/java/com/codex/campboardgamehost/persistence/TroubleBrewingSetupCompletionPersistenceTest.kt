package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory
import org.json.JSONArray
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
    fun `completion record round trip preserves frozen player starting identities`() {
        val record = record(playerStartingIdentities = startingIdentities())
        val root = JSONObject().put(
            TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
            TroubleBrewingSetupCompletionPersistence.encode(record),
        )

        assertEquals(record, TroubleBrewingSetupCompletionPersistence.decodeOrNull(root))
    }

    @Test
    fun `schema v1 completion remains readable without player starting identities`() {
        val legacy = JSONObject().apply {
            put("schemaVersion", 1)
            put("datasetId", "trouble_brewing_setup_presets_v2_final")
            put("datasetSchemaVersion", 2)
            put("presetId", "tb-8-042")
            put("playerCount", 8)
            put(
                "realNonDemonRoleIds",
                JSONArray(
                    listOf(
                        "chef",
                        "empath",
                        "fortune_teller",
                        "monk",
                        "drunk",
                        "butler",
                        "poisoner",
                    ),
                ),
            )
            put("minionRoleIds", JSONArray(listOf("poisoner")))
            put("primaryStyleTag", "balanced")
            put("selectedDrunkShownRole", "investigator")
        }
        val root = JSONObject().put(TroubleBrewingSetupCompletionPersistence.ROOT_KEY, legacy)

        assertEquals(record(), TroubleBrewingSetupCompletionPersistence.decodeOrNull(root))
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

    private fun record(
        playerStartingIdentities: List<TroubleBrewingPlayerStartingIdentity> = emptyList(),
    ): TroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecord(
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
        playerStartingIdentities = playerStartingIdentities,
    )

    private fun startingIdentities(): List<TroubleBrewingPlayerStartingIdentity> = listOf(
        startingIdentity("Alice", "chef", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
        startingIdentity("Bob", "empath", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
        startingIdentity("Carol", "fortune_teller", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
        startingIdentity("David", "monk", TroubleBrewingStartingRoleCategory.TOWNSFOLK),
        startingIdentity(
            playerKey = "Emma",
            actualRoleId = "drunk",
            category = TroubleBrewingStartingRoleCategory.OUTSIDER,
            shownRoleId = "investigator",
        ),
        startingIdentity("Frank", "butler", TroubleBrewingStartingRoleCategory.OUTSIDER),
        startingIdentity("Grace", "poisoner", TroubleBrewingStartingRoleCategory.MINION),
        startingIdentity("Henry", "imp", TroubleBrewingStartingRoleCategory.DEMON),
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
