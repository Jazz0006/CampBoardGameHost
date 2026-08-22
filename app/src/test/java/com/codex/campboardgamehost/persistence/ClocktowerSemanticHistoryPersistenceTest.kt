package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSemanticHistoryPersistenceTest {
    @Test
    fun `missing semantic history mode fails closed`() {
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject().put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, 0L),
            )
        }
    }

    @Test
    fun `missing invalid or negative cursor fails closed`() {
        val explicitLegacy = JSONObject().put(
            ClocktowerSemanticHistoryPersistence.MODE_KEY,
            ClocktowerSemanticHistoryPersistence.encode(ClocktowerSemanticHistoryMode.LEGACY_LOCAL),
        )
        assertFails { ClocktowerSemanticHistoryPersistence.decodeMode(JSONObject(explicitLegacy.toString())) }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject(explicitLegacy.toString())
                    .put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, JSONObject.NULL),
            )
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject(explicitLegacy.toString())
                    .put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, "0"),
            )
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject(explicitLegacy.toString())
                    .put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, -1L),
            )
        }
    }

    @Test
    fun `persisted semantic history mode names and cursor key are stable`() {
        assertEquals(
            "LEGACY_LOCAL",
            ClocktowerSemanticHistoryPersistence.encode(ClocktowerSemanticHistoryMode.LEGACY_LOCAL),
        )
        assertEquals(
            "GLOBAL_V1",
            ClocktowerSemanticHistoryPersistence.encode(ClocktowerSemanticHistoryMode.GLOBAL_V1),
        )
        assertEquals(
            "clocktowerNextTimelineGlobalSequence",
            ClocktowerSemanticHistoryPersistence.CURSOR_KEY,
        )
    }

    @Test
    fun `explicit legacy and global modes round trip through the persisted value`() {
        ClocktowerSemanticHistoryMode.values().forEach { mode ->
            val json = JSONObject()
                .put(
                    ClocktowerSemanticHistoryPersistence.MODE_KEY,
                    ClocktowerSemanticHistoryPersistence.encode(mode),
                )
                .put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, 0L)
            assertEquals(mode, ClocktowerSemanticHistoryPersistence.decodeMode(json))
        }
    }

    @Test
    fun `explicit null unknown or non-string semantic history mode fails closed`() {
        fun jsonWith(value: Any?): JSONObject = JSONObject()
            .put(ClocktowerSemanticHistoryPersistence.MODE_KEY, value)
            .put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, 0L)

        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(jsonWith(JSONObject.NULL))
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(jsonWith("GLOBAL_V2"))
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(jsonWith(1))
        }
    }

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected semantic-history persistence to fail closed.", failed)
    }
}
