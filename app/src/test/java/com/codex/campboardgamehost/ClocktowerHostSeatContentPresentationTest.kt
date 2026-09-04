package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ClocktowerHostSeatContentPresentationTest {
    @Test
    fun `identity only mode hides host role detail even when typed role data exists`() {
        val seat = HostSeatPresentation(
            seatId = ClocktowerSeatId(1),
            playerName = "Alice",
            isAlive = true,
            actualRole = HostRolePresentation(roleId = "Drunk", displayName = "酒鬼"),
            shownRole = HostRolePresentation(roleId = "Empath", displayName = "共情者"),
        )

        val presentation = hostSeatContentPresentation(seat, language = "zh")

        assertEquals("Alice", presentation.primaryLabel)
        assertEquals(emptyList<String>(), presentation.detailLabels)
    }

    @Test
    fun `storyteller mode shows one role line when actual and shown identity agree`() {
        val seat = HostSeatPresentation(
            seatId = ClocktowerSeatId(2),
            playerName = "Bob",
            isAlive = true,
            actualRole = HostRolePresentation(roleId = "Monk", displayName = "Monk"),
            shownRole = HostRolePresentation(roleId = "Monk", displayName = "Monk"),
            contentMode = HostSeatContentMode.StorytellerRoleDetail,
        )

        val presentation = hostSeatContentPresentation(seat, language = "en")

        assertEquals("Bob", presentation.primaryLabel)
        assertEquals(listOf("Monk"), presentation.detailLabels)
    }

    @Test
    fun `storyteller mode distinguishes actual and shown role for drunk in both languages`() {
        val englishSeat = HostSeatPresentation(
            seatId = ClocktowerSeatId(3),
            playerName = "Casey",
            isAlive = false,
            actualRole = HostRolePresentation(roleId = "Drunk", displayName = "Drunk"),
            shownRole = HostRolePresentation(roleId = "Empath", displayName = "Empath"),
            contentMode = HostSeatContentMode.StorytellerRoleDetail,
        )
        val chineseSeat = englishSeat.copy(
            actualRole = HostRolePresentation(roleId = "Drunk", displayName = "酒鬼"),
            shownRole = HostRolePresentation(roleId = "Empath", displayName = "共情者"),
        )

        assertEquals(
            listOf("Actual: Drunk", "Shown: Empath"),
            hostSeatContentPresentation(englishSeat, language = "en").detailLabels,
        )
        val chinese = hostSeatContentPresentation(chineseSeat, language = "zh")
        assertEquals("Casey ☠", chinese.primaryLabel)
        assertEquals(listOf("实际：酒鬼", "认为：共情者"), chinese.detailLabels)
    }

    @Test
    fun `night card projection preserves typed actual shown role and life state`() {
        val drunk = ClocktowerRole(
            team = ClocktowerTeam.Outsider,
            zhName = "酒鬼",
            enName = "Drunk",
            zhDescription = "",
            enDescription = "",
        )
        val empath = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "共情者",
            enName = "Empath",
            zhDescription = "",
            enDescription = "",
        )
        val card = PlayerCard(
            name = "Dana",
            role = Role.Civilian,
            word = "",
            clocktowerRole = drunk,
            clocktowerShownRole = empath,
            eliminatedRound = 2,
        )

        val seat = card.toStorytellerHostSeatPresentation(seatNumber = 4, language = "zh")

        assertEquals(ClocktowerSeatId(4), seat.seatId)
        assertEquals(HostSeatContentMode.StorytellerRoleDetail, seat.contentMode)
        assertEquals("Drunk", seat.actualRole?.roleId)
        assertEquals("酒鬼", seat.actualRole?.displayName)
        assertEquals("Empath", seat.shownRole?.roleId)
        assertEquals("共情者", seat.shownRole?.displayName)
        assertFalse(seat.isAlive)
    }
}
