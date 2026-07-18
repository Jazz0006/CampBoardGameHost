package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRecommendationRulesTest {
    @Test
    fun `red herring requires an actually good team`() {
        assertTrue(ClocktowerTeam.Townsfolk.isLegalRedHerringTeam())
        assertTrue(ClocktowerTeam.Outsider.isLegalRedHerringTeam())
        assertFalse(ClocktowerTeam.Minion.isLegalRedHerringTeam())
        assertFalse(ClocktowerTeam.Demon.isLegalRedHerringTeam())
    }

    @Test
    fun `demon bluffs include out of play good roles from the active script`() {
        val chef = role(ClocktowerTeam.Townsfolk, "Chef")
        val saint = role(ClocktowerTeam.Outsider, "Saint")
        val spy = role(ClocktowerTeam.Minion, "Spy")
        val imp = role(ClocktowerTeam.Demon, "Imp")

        val result = legalDemonBluffRoles(
            scriptRoles = listOf(chef, saint, spy, imp),
            inPlayRoleNames = emptySet(),
        )

        assertEquals(listOf("Chef", "Saint"), result.map { it.enName })
    }

    @Test
    fun `demon bluffs exclude roles that are actually in play`() {
        val chef = role(ClocktowerTeam.Townsfolk, "Chef")
        val saint = role(ClocktowerTeam.Outsider, "Saint")

        val result = legalDemonBluffRoles(
            scriptRoles = listOf(chef, saint),
            inPlayRoleNames = setOf("Chef"),
        )

        assertEquals(listOf("Saint"), result.map { it.enName })
    }

    @Test
    fun `demon bluff candidates are distinct and keep script order`() {
        val chef = role(ClocktowerTeam.Townsfolk, "Chef")
        val duplicateChef = role(ClocktowerTeam.Townsfolk, "Chef")
        val saint = role(ClocktowerTeam.Outsider, "Saint")

        val result = legalDemonBluffRoles(
            scriptRoles = listOf(chef, duplicateChef, saint),
            inPlayRoleNames = emptySet(),
        )

        assertEquals(listOf("Chef", "Saint"), result.map { it.enName })
    }

    private fun role(team: ClocktowerTeam, enName: String): ClocktowerRole = ClocktowerRole(
        team = team,
        zhName = enName,
        enName = enName,
        zhDescription = "",
        enDescription = "",
    )
}
