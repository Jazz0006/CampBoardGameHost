package com.codex.campboardgamehost.clocktower.epistemic

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TimelinePointJsonCompatibilityTest {
    @Test fun `legacy schema v2 timeline point requires explicit migration`() {
        val legacySchemaV2 = """
            {
              "detectingAbility": "Fortune Teller",
              "interactionId": "ft-night-1-seat-4",
              "question": "DEMON",
              "schemaVersion": 2,
              "subjectSeat": 4,
              "timelinePoint": {
                "phase": "FIRST_NIGHT",
                "round": 1,
                "sequence": 8
              }
            }
        """.trimIndent()

        try {
            EpistemicSemanticJson.decodeRegistrationQuery(legacySchemaV2)
            fail("legacy schema-v2 TimelinePoint must not infer a global sequence")
        } catch (error: IllegalArgumentException) {
            val message = error.message.orEmpty()
            assertTrue(message.contains("globalSequence"))
            assertTrue(message.contains("explicit migration"))
            assertTrue(message.contains("cannot be inferred"))
        }
    }
}
