package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TroubleBrewingSetupCompletionPersistenceTest {
    @Test
    fun `completion record round trip preserves exact diversity metadata without dataset`() {
        val record = record()
        val encoded = TroubleBrewingSetupCompletionPersistence.encode(record)
        val root = JSONObject().put(
            TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
            encoded,
        )

        assertEquals(3, encoded.getInt("schemaVersion"))
        assertFalse(encoded.has("selectedDrunkShownRole"))
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
    fun `missing completion record is optional for non TB recovery`() {
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
    fun `legacy v2 completion migrates when old Drunk field agrees with canonical identity`() {
        val expected = record(playerStartingIdentities = startingIdentities())
        val json = TroubleBrewingSetupCompletionPersistence.encode(expected)
            .put("schemaVersion", 2)
            .put("selectedDrunkShownRole", "investigator")
        val root = JSONObject().put(TroubleBrewingSetupCompletionPersistence.ROOT_KEY, json)

        assertEquals(expected, TroubleBrewingSetupCompletionPersistence.decodeOrNull(root))
    }

    @Test
    fun `legacy v2 completion rejects old Drunk field that conflicts with canonical identity`() {
        val json = TroubleBrewingSetupCompletionPersistence.encode(
            record(playerStartingIdentities = startingIdentities()),
        )
            .put("schemaVersion", 2)
            .put("selectedDrunkShownRole", "monk")
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
