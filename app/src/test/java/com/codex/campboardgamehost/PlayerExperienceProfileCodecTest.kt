package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlayerExperienceProfileCodecTest {
    @Test
    fun roundTripKeepsOnlyNonDefaultCrossGameExperience() {
        val encoded = PlayerExperienceProfileCodec.encode(
            mapOf(
                "Alice" to PlayerExperienceLevelV1.BEGINNER,
                "Bob" to PlayerExperienceLevelV1.NORMAL,
                "Carol" to PlayerExperienceLevelV1.EXPERT,
            ),
        )

        assertFalse(encoded.contains("Bob"))
        assertEquals(
            mapOf(
                "Alice" to PlayerExperienceLevelV1.BEGINNER,
                "Carol" to PlayerExperienceLevelV1.EXPERT,
            ),
            PlayerExperienceProfileCodec.decode(encoded),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeRejectsPersistedNormalDefault() {
        PlayerExperienceProfileCodec.decode(
            """[{"playerName":"Alice","experienceLevel":"NORMAL"}]""",
        )
    }
}
