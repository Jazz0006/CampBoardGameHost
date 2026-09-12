package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerBeginnerNightGuidanceTest {
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
    fun `normal actor shows role seat name and concise action`() {
        val poisoner = role(ClocktowerTeam.Minion, "投毒者", "Poisoner")
        val cards = listOf(
            player("张三", role(ClocktowerTeam.Townsfolk, "洗衣妇", "Washerwoman")),
            player("李四", poisoner),
        )

        val guidance = clocktowerBeginnerNightGuidance(
            action = ClocktowerNightAction.Poison,
            actor = cards[1],
            cards = cards,
            language = "zh",
        )

        assertEquals("唤醒 投毒者", guidance?.wakeLine)
        assertEquals("2号 李四", guidance?.actorLine)
        assertEquals("让他选择一名玩家作为中毒目标", guidance?.instruction)
    }

    @Test
    fun `drunk shows actual role arrow shown role`() {
        val drunk = role(ClocktowerTeam.Outsider, "酒鬼", "Drunk")
        val empath = role(ClocktowerTeam.Townsfolk, "共情者", "Empath")
        val actor = player("张三", actual = drunk, shown = empath)

        val guidance = clocktowerBeginnerNightGuidance(
            action = ClocktowerNightAction.None,
            actor = actor,
            cards = listOf(actor),
            language = "zh",
        )

        assertEquals("唤醒 酒鬼 → 共情者", guidance?.wakeLine)
        assertEquals("1号 张三", guidance?.actorLine)
        assertNull(guidance?.instruction)
        assertEquals("唤醒 酒鬼 → 共情者\n1号 张三", guidance?.asWakeInstruction())
    }

    @Test
    fun `actorless group step stays on legacy presentation`() {
        assertNull(
            clocktowerBeginnerNightGuidance(
                action = ClocktowerNightAction.None,
                actor = null,
                cards = emptyList(),
                language = "zh",
            ),
        )
    }
}
