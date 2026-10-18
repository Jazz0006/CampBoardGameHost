package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerOtherNightWakingRoleAuthorityTest {
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
