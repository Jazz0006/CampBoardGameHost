package com.codex.campboardgamehost.clocktower.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class DemonSuccessionSemanticsTest {

    @Test
    fun `no actual Demon death never creates succession`() {
        assertEquals(
            DemonSuccessionResolution.None,
            resolve(
                demonActuallyDied = false,
                demonDeathWasImpSelfKill = true,
                livingMinionSeats = setOf(2, 3),
            ),
        )
    }

    @Test
    fun `successful Imp self kill allows every living Minion`() {
        assertEquals(
            DemonSuccessionResolution.Choice(setOf(2, 3)),
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = true,
                livingMinionSeats = setOf(2, 3),
                aliveCountBeforeDemonDeath = 4,
            ),
        )
    }

    @Test
    fun `successful Imp self kill without a living Minion has no succession`() {
        assertEquals(
            DemonSuccessionResolution.None,
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = true,
                livingMinionSeats = emptySet(),
            ),
        )
    }

    @Test
    fun `functioning Scarlet Woman is forced when five players are alive`() {
        assertEquals(
            DemonSuccessionResolution.Forced(targetSeat = 2),
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = 2,
                livingMinionSeats = setOf(2, 3),
            ),
        )
    }

    @Test
    fun `nonfunctioning Scarlet Woman remains an ordinary self kill Minion candidate`() {
        assertEquals(
            DemonSuccessionResolution.Choice(setOf(2, 3)),
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = null,
                livingMinionSeats = setOf(2, 3),
            ),
        )
    }

    @Test
    fun `Scarlet Woman is not forced below five alive`() {
        assertEquals(
            DemonSuccessionResolution.Choice(setOf(2, 3)),
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = 4,
                functioningScarletWomanSeat = 2,
                livingMinionSeats = setOf(2, 3),
            ),
        )
    }

    @Test
    fun `non self Demon death can force functioning Scarlet Woman`() {
        assertEquals(
            DemonSuccessionResolution.Forced(targetSeat = 2),
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = false,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = 2,
                livingMinionSeats = setOf(2, 3),
            ),
        )
    }

    @Test
    fun `non self Demon death without mandatory Scarlet Woman creates no Minion choice`() {
        assertEquals(
            DemonSuccessionResolution.None,
            resolve(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = false,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = null,
                livingMinionSeats = setOf(2, 3),
            ),
        )
    }

    private fun resolve(
        demonActuallyDied: Boolean,
        demonDeathWasImpSelfKill: Boolean,
        aliveCountBeforeDemonDeath: Int = 5,
        functioningScarletWomanSeat: Int? = null,
        livingMinionSeats: Set<Int>,
    ): DemonSuccessionResolution = DemonSuccessionSemantics.resolve(
        DemonSuccessionContext(
            demonActuallyDied = demonActuallyDied,
            demonDeathWasImpSelfKill = demonDeathWasImpSelfKill,
            aliveCountBeforeDemonDeath = aliveCountBeforeDemonDeath,
            functioningScarletWomanSeat = functioningScarletWomanSeat,
            livingMinionSeats = livingMinionSeats,
        ),
    )
}
