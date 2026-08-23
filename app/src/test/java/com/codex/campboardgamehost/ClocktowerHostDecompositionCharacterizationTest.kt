package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerHostDecompositionCharacterizationTest {
    private fun card(
        team: ClocktowerTeam?,
        actualRole: ClocktowerRole? = null,
        shownRole: ClocktowerRole? = actualRole,
    ): PlayerCard = PlayerCard(
        name = "P1",
        role = Role.Civilian,
        word = "",
        clocktowerTeam = team,
        clocktowerRole = actualRole,
        clocktowerShownRole = shownRole,
    )

    private val monk = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = "僧侣",
        enName = "Monk",
        zhDescription = "",
        enDescription = "",
    )

    private val empath = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = "共情者",
        enName = "Empath",
        zhDescription = "",
        enDescription = "",
    )

    @Test
    fun `evil classification remains minion or demon only`() {
        assertFalse(isClocktowerEvil(card(ClocktowerTeam.Townsfolk)))
        assertFalse(isClocktowerEvil(card(ClocktowerTeam.Outsider)))
        assertTrue(isClocktowerEvil(card(ClocktowerTeam.Minion)))
        assertTrue(isClocktowerEvil(card(ClocktowerTeam.Demon)))
        assertFalse(isClocktowerEvil(card(null)))
    }

    @Test
    fun `shown as different role requires both roles and different english names`() {
        assertFalse(card(ClocktowerTeam.Townsfolk, monk, monk).clocktowerShownAsDifferentRole())
        assertTrue(card(ClocktowerTeam.Townsfolk, monk, empath).clocktowerShownAsDifferentRole())
        assertFalse(card(ClocktowerTeam.Townsfolk, monk, null).clocktowerShownAsDifferentRole())
        assertFalse(card(ClocktowerTeam.Townsfolk, null, empath).clocktowerShownAsDifferentRole())
    }

    @Test
    fun `private observation record id remains deterministic and proposition sensitive`() {
        val proposition = InformationProposition.RoleAt(2, RoleId("Monk"))
        val same = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            roleEnName = "Washerwoman",
            actorSeat = 1,
            proposition = proposition,
        )
        val repeated = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            roleEnName = "Washerwoman",
            actorSeat = 1,
            proposition = proposition,
        )
        val different = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            roleEnName = "Washerwoman",
            actorSeat = 1,
            proposition = InformationProposition.RoleAt(3, RoleId("Monk")),
        )

        assertEquals(same, repeated)
        assertNotEquals(same, different)
        assertTrue(same.startsWith("private-game-1-FirstNight-1-Washerwoman-1-"))
        assertEquals(64, same.substringAfterLast('-').length)
    }
}
