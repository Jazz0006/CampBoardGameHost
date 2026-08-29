package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerOtherNightWakingRoleAuthorityTest {
    @Test
    fun `pending succession retains historical Imp ordering anchor after old Imp is dead`() {
        val cards = listOf(
            card(
                name = "Imp0",
                roleName = "Imp",
                team = ClocktowerTeam.Demon,
                eliminatedRound = 3,
            ),
            card(
                name = "Poisoner",
                roleName = "Poisoner",
                team = ClocktowerTeam.Minion,
            ),
            card(
                name = "Empath",
                roleName = "Empath",
                team = ClocktowerTeam.Townsfolk,
            ),
        )

        assertEquals(
            setOf(RoleId("Poisoner"), RoleId("Empath"), RoleId("Imp")),
            clocktowerOtherNightWakingRoleIds(
                cards = cards,
                pendingSuccessionDemonRoleId = RoleId("Imp"),
            ),
        )
    }

    @Test
    fun `ordinary other night keeps only living current and shown Drunk roles`() {
        val drunk = card(
            name = "Drunk",
            roleName = "Drunk",
            team = ClocktowerTeam.Outsider,
            shownRoleName = "Fortune Teller",
        )
        val cards = listOf(
            card("DeadImp", "Imp", ClocktowerTeam.Demon, eliminatedRound = 2),
            card("Monk", "Monk", ClocktowerTeam.Townsfolk),
            drunk,
        )

        assertEquals(
            setOf(RoleId("Monk"), RoleId("Drunk"), RoleId("Fortune Teller")),
            clocktowerOtherNightWakingRoleIds(
                cards = cards,
                pendingSuccessionDemonRoleId = null,
            ),
        )
    }

    private fun card(
        name: String,
        roleName: String,
        team: ClocktowerTeam,
        eliminatedRound: Int? = null,
        shownRoleName: String? = roleName,
    ) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = team,
        clocktowerRole = ClocktowerRole(
            team = team,
            zhName = roleName,
            enName = roleName,
            zhDescription = "",
            enDescription = "",
        ),
        clocktowerShownRole = shownRoleName?.let { shownRole ->
            ClocktowerRole(
                team = team,
                zhName = shownRole,
                enName = shownRole,
                zhDescription = "",
                enDescription = "",
            )
        },
        eliminatedRound = eliminatedRound,
    )
}
