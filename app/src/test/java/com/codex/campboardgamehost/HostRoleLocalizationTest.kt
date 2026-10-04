package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class HostRoleLocalizationTest {
    private val washerwoman = completeTroubleBrewingRoles.single { it.enName == "Washerwoman" }

    @Test
    fun `clocktower host role uses current chinese locale instead of cached english label`() {
        val card = PlayerCard(
            name = "Alice",
            role = Role.Civilian,
            word = "",
            roleLabel = "Washerwoman",
            actualRoleLabel = "Washerwoman",
            clocktowerTeam = ClocktowerTeam.Townsfolk,
            clocktowerRole = washerwoman,
            clocktowerShownRole = washerwoman,
        )

        assertEquals("洗衣妇", card.clocktowerHostRoleLabel("zh"))
    }

    @Test
    fun `clocktower host role uses current english locale instead of cached chinese label`() {
        val card = PlayerCard(
            name = "Alice",
            role = Role.Civilian,
            word = "",
            roleLabel = "洗衣妇",
            actualRoleLabel = "洗衣妇",
            clocktowerTeam = ClocktowerTeam.Townsfolk,
            clocktowerRole = washerwoman,
            clocktowerShownRole = washerwoman,
        )

        assertEquals("Washerwoman", card.clocktowerHostRoleLabel("en"))
    }

    @Test
    fun `legacy clocktower card without typed role keeps cached fallback label`() {
        val card = PlayerCard(
            name = "Alice",
            role = Role.Civilian,
            word = "",
            roleLabel = "shown",
            actualRoleLabel = "actual",
        )

        assertEquals("actual", card.clocktowerHostRoleLabel("zh"))
    }
}
