package com.codex.campboardgamehost.clocktower.review

import org.junit.Assert.assertEquals
import org.junit.Test

class Sde2D5CrossRegimeCalibrationEvidenceTest {
    @Test
    fun `player count regimes are explicit at every supported boundary`() {
        val expected = mapOf(
            5 to Sde2D5PlayerCountRegime.FIVE_TO_SIX,
            6 to Sde2D5PlayerCountRegime.FIVE_TO_SIX,
            7 to Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
            9 to Sde2D5PlayerCountRegime.SEVEN_TO_NINE,
            10 to Sde2D5PlayerCountRegime.TEN_TO_TWELVE,
            12 to Sde2D5PlayerCountRegime.TEN_TO_TWELVE,
            13 to Sde2D5PlayerCountRegime.THIRTEEN_TO_FIFTEEN,
            15 to Sde2D5PlayerCountRegime.THIRTEEN_TO_FIFTEEN,
        )

        expected.forEach { (playerCount, regime) ->
            assertEquals(regime, Sde2D5PlayerCountRegime.from(playerCount))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `player count regime rejects unsupported counts`() {
        Sde2D5PlayerCountRegime.from(16)
    }
}
