package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSemanticHistoryPersistenceTest {
    @Test
    fun `missing semantic history mode restores as legacy local`() {
        assertEquals(
            ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
            ClocktowerSemanticHistoryPersistence.decodeMode(JSONObject()),
        )
    }

    @Test
    fun `explicit legacy and global modes round trip through the persisted value`() {
        ClocktowerSemanticHistoryMode.values().forEach { mode ->
            val json = JSONObject().put(
                ClocktowerSemanticHistoryPersistence.MODE_KEY,
                ClocktowerSemanticHistoryPersistence.encode(mode),
            )
            assertEquals(mode, ClocktowerSemanticHistoryPersistence.decodeMode(json))
        }
    }

    @Test
    fun `explicit null or unknown semantic history mode fails closed`() {
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject().put(ClocktowerSemanticHistoryPersistence.MODE_KEY, JSONObject.NULL),
            )
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject().put(ClocktowerSemanticHistoryPersistence.MODE_KEY, "GLOBAL_V2"),
            )
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeMode(
                JSONObject().put(ClocktowerSemanticHistoryPersistence.MODE_KEY, 1),
            )
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
