package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerFortuneTellerPhaseAuthorityTest {
    private val imp = RoleId("Imp")
    private val scarletWoman = RoleId("Scarlet Woman")

    private fun fortuneTellerDemonMatch(
        phase: ClocktowerPhase,
        baseRole: RoleId?,
        otherNightRole: () -> RoleId?,
    ): Boolean = clocktowerFortuneTellerRoleAuthority(
        phase = phase,
        baseRole = baseRole,
        otherNightRole = otherNightRole,
    ) == imp

    @Test
    fun `first night matches a base Demon without evaluating other-night projection`() {
        var projectionCalls = 0

        val matched = fortuneTellerDemonMatch(
            phase = ClocktowerPhase.FirstNight,
            baseRole = imp,
            otherNightRole = {
                projectionCalls += 1
                error("First Night Fortune Teller must not evaluate Other Night effective-state projection")
            },
        )

        assertTrue(matched)
        assertEquals(0, projectionCalls)
    }

    @Test
    fun `first night rejects a base non-Demon without evaluating other-night projection`() {
        var projectionCalls = 0

        val matched = fortuneTellerDemonMatch(
            phase = ClocktowerPhase.FirstNight,
            baseRole = scarletWoman,
            otherNightRole = {
                projectionCalls += 1
                error("First Night Fortune Teller must not evaluate Other Night effective-state projection")
            },
        )

        assertFalse(matched)
        assertEquals(0, projectionCalls)
    }

    @Test
    fun `other night matches when same-night projection changes target into Demon`() {
        var projectionCalls = 0

        val matched = fortuneTellerDemonMatch(
            phase = ClocktowerPhase.Night,
            baseRole = scarletWoman,
            otherNightRole = {
                projectionCalls += 1
                imp
            },
        )

        assertTrue(matched)
        assertEquals(1, projectionCalls)
    }

    @Test
    fun `other night rejects when same-night projection changes base Demon away from Demon`() {
        var projectionCalls = 0

        val matched = fortuneTellerDemonMatch(
            phase = ClocktowerPhase.Night,
            baseRole = imp,
            otherNightRole = {
                projectionCalls += 1
                scarletWoman
            },
        )

        assertFalse(matched)
        assertEquals(1, projectionCalls)
    }
}
