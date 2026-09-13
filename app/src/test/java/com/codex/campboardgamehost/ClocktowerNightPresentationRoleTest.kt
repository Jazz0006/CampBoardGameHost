package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerNightPresentationRoleTest {
    private fun role(team: ClocktowerTeam, zh: String, en: String) = ClocktowerRole(
        team = team,
        zhName = zh,
        enName = en,
        zhDescription = "",
        enDescription = "",
    )

    private fun player(
        name: String,
        actual: ClocktowerRole,
        shown: ClocktowerRole = actual,
    ) = PlayerCard(
        name = name,
        role = Role.Villager,
        word = "",
        clocktowerTeam = actual.team,
        clocktowerRole = actual,
        clocktowerShownRole = shown,
    )

    @Test
    fun `drunk presentation role uses shown role`() {
        val drunk = role(ClocktowerTeam.Outsider, "酒鬼", "Drunk")
        val empath = role(ClocktowerTeam.Townsfolk, "共情者", "Empath")
        val actor = player("张三", actual = drunk, shown = empath)

        assertEquals(
            "Empath",
            clocktowerNightPresentationRoleEnName(
                stepRoleEnName = "Drunk",
                actor = actor,
            ),
        )
    }

    @Test
    fun `normal presentation role keeps step role`() {
        val chef = role(ClocktowerTeam.Townsfolk, "厨师", "Chef")
        val actor = player("李四", actual = chef)

        assertEquals(
            "Chef",
            clocktowerNightPresentationRoleEnName(
                stepRoleEnName = "Chef",
                actor = actor,
            ),
        )
    }
}
