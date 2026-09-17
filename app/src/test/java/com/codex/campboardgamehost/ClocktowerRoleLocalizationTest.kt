package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerRoleLocalizationTest {
    @Test
    fun `canonical role identity localizes to Chinese and English labels`() {
        val roleId = RoleId("Chef")

        assertEquals("厨师", clocktowerRoleLabel(roleId, "zh"))
        assertEquals("Chef", clocktowerRoleLabel(roleId, "en"))
    }

    @Test
    fun `unknown role id falls back to canonical value`() {
        assertEquals("FutureRole", clocktowerRoleLabel(RoleId("FutureRole"), "zh"))
    }
}
