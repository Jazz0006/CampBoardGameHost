package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerFortuneTellerPhaseAuthorityTest {
    @Test
    fun `first night uses base role without evaluating other-night projection`() {
        val baseRole = RoleId("Imp")
        var projectionCalls = 0

        val actual = clocktowerFortuneTellerRoleAuthority(
            phase = ClocktowerPhase.FirstNight,
            baseRole = baseRole,
            otherNightRole = {
                projectionCalls += 1
                error("First Night must not evaluate Other Night effective-state projection")
            },
        )

        assertEquals(baseRole, actual)
        assertEquals(0, projectionCalls)
    }

    @Test
    fun `other night uses projected role authority exactly once`() {
        val baseRole = RoleId("Scarlet Woman")
        val projectedRole = RoleId("Imp")
        var projectionCalls = 0

        val actual = clocktowerFortuneTellerRoleAuthority(
            phase = ClocktowerPhase.Night,
            baseRole = baseRole,
            otherNightRole = {
                projectionCalls += 1
                projectedRole
            },
        )

        assertEquals(projectedRole, actual)
        assertEquals(1, projectionCalls)
    }
}
