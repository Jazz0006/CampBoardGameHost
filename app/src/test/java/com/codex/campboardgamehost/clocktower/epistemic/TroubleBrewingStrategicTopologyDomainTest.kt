package com.codex.campboardgamehost.clocktower.epistemic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingStrategicTopologyDomainTest {
    @Test
    fun `enumerates exact strategic topology counts for every supported player count`() {
        val expected = mapOf(
            5 to 20,
            6 to 30,
            7 to 42,
            8 to 56,
            9 to 72,
            10 to 360,
            11 to 495,
            12 to 660,
            13 to 2_860,
            14 to 4_004,
            15 to 5_460,
        )

        for (playerCount in 5..15) {
            val profile = TroubleBrewingSetupProfiles.standard(playerCount)
            val keys = TroubleBrewingStrategicTopologyDomain.enumerate(playerCount, profile)

            assertEquals(expected.getValue(playerCount), keys.size)
            assertEquals(
                playerCount * choose(playerCount - 1, profile.minions),
                keys.size,
            )
            assertEquals(keys.size, keys.distinct().size)

            keys.forEach { key ->
                assertTrue(key.demonSeat in 1..playerCount)
                assertEquals(profile.minions, key.minionSeats.size)
                assertEquals(key.minionSeats.sorted(), key.minionSeats)
                assertEquals(key.minionSeats.size, key.minionSeats.distinct().size)
                assertFalse(key.demonSeat in key.minionSeats)
                assertTrue(key.minionSeats.all { it in 1..playerCount })
            }
        }
    }

    @Test
    fun `standard and Baron profiles share the same evil seat topology domain`() {
        for (playerCount in 5..15) {
            val standard = TroubleBrewingStrategicTopologyDomain.enumerate(
                playerCount,
                TroubleBrewingSetupProfiles.standard(playerCount),
            )
            val baron = TroubleBrewingStrategicTopologyDomain.enumerate(
                playerCount,
                TroubleBrewingSetupProfiles.withBaron(playerCount),
            )

            assertEquals(standard, baron)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects profile whose total does not match player count`() {
        TroubleBrewingStrategicTopologyDomain.enumerate(
            playerCount = 7,
            profile = InformationProposition.SetupProfile(
                townsfolk = 5,
                outsiders = 1,
                minions = 1,
                demons = 1,
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non Trouble Brewing one Demon profile`() {
        TroubleBrewingStrategicTopologyDomain.enumerate(
            playerCount = 7,
            profile = InformationProposition.SetupProfile(
                townsfolk = 4,
                outsiders = 0,
                minions = 1,
                demons = 2,
            ),
        )
    }

    private fun choose(n: Int, k: Int): Int {
        if (k < 0 || k > n) return 0
        val effective = minOf(k, n - k)
        var result = 1L
        for (i in 1..effective) {
            result = result * (n - effective + i) / i
        }
        return result.toInt()
    }
}
