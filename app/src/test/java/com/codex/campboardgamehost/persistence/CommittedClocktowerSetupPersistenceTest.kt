package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommittedClocktowerSetupPersistenceTest {
    @Test
    fun `template setup round trips exact committed identities without source reconstruction`() {
        val original = CommittedClocktowerSetup(
            script = ScriptId("trouble_brewing"),
            setupSeed = 42L,
            assignments = listOf(
                seat(1, actual = "drunk", shown = "chef"),
                seat(2, actual = "imp", shown = "imp"),
            ),
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.TEMPLATE,
                providerId = "tb-presets-v2",
                candidateId = "tb-5-001",
            ),
        )
        val root = JSONObject().put(
            CommittedClocktowerSetupPersistence.ROOT_KEY,
            CommittedClocktowerSetupPersistence.encode(original),
        )

        val restored = CommittedClocktowerSetupPersistence.decodeOrNull(root)

        assertEquals(original, restored)
    }

    @Test
    fun `generated non TB setup round trips without template dataset dependency`() {
        val original = CommittedClocktowerSetup(
            script = ScriptId("future_script"),
            setupSeed = -17L,
            assignments = listOf(
                seat(1, actual = "alpha", shown = "alpha"),
                seat(2, actual = "beta", shown = "gamma"),
                seat(3, actual = "delta", shown = "delta"),
            ),
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "legal-generator-v1",
            ),
        )
        val root = JSONObject().put(
            CommittedClocktowerSetupPersistence.ROOT_KEY,
            CommittedClocktowerSetupPersistence.encode(original),
        )

        assertEquals(original, CommittedClocktowerSetupPersistence.decodeOrNull(root))
    }

    @Test
    fun `missing exact setup root remains absent instead of reconstructing it`() {
        assertNull(CommittedClocktowerSetupPersistence.decodeOrNull(JSONObject()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unsupported setup schema is rejected`() {
        val payload = CommittedClocktowerSetupPersistence.encode(validSetup())
        payload.put("schemaVersion", CommittedClocktowerSetupPersistence.SCHEMA_VERSION + 1)
        val root = JSONObject().put(CommittedClocktowerSetupPersistence.ROOT_KEY, payload)

        CommittedClocktowerSetupPersistence.decodeOrNull(root)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `malformed exact assignment is rejected`() {
        val payload = CommittedClocktowerSetupPersistence.encode(validSetup())
        payload.put(
            "assignments",
            JSONArray().put(
                JSONObject()
                    .put("seat", 1)
                    .put("actualRole", "chef"),
            ),
        )
        val root = JSONObject().put(CommittedClocktowerSetupPersistence.ROOT_KEY, payload)

        CommittedClocktowerSetupPersistence.decodeOrNull(root)
    }

    private fun validSetup(): CommittedClocktowerSetup = CommittedClocktowerSetup(
        script = ScriptId("trouble_brewing"),
        setupSeed = 7L,
        assignments = listOf(
            seat(1, actual = "chef", shown = "chef"),
            seat(2, actual = "imp", shown = "imp"),
        ),
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.TEMPLATE,
            providerId = "tb-presets-v2",
            candidateId = "tb-5-002",
        ),
    )

    private fun seat(seat: Int, actual: String, shown: String): CommittedSetupSeat = CommittedSetupSeat(
        seat = seat,
        actualRole = RoleId(actual),
        shownRole = RoleId(shown),
    )
}
