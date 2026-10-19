package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.*
import org.junit.Test

class ClocktowerRegistrationProjectionTest {
    private fun card(name: String, role: String) = PlayerCard(
        name = name, clocktowerRole = completeTroubleBrewingRoles.single { it.enName == role },
    )
    private val spy = card("S", "Spy")
    private val recluse = card("R", "Recluse")
    private val cards = listOf(spy, recluse)

    @Test fun `day and night queries honor their own effective poison and role`() {
        val choices = ClocktowerRegistrationInteractionState()
        choices.chooseSpy("day", true, "Chef")
        choices.chooseSpy("night", true, "Chef")
        val projection = ClocktowerRegistrationProjection(
            cards, ClocktowerScript.TroubleBrewing, choices,
            effectivePoisonForRole = { if (it == "Virgin") "S" else null },
            effectiveRoleForRegistration = { query, card ->
                if (query == "Ravenkeeper") RoleId("Imp") else card.clocktowerRole?.enName?.let(::RoleId)
            },
        )
        assertFalse(projection.spyCanRegister("Virgin"))
        assertFalse(projection.spyRegistersGood("day", "Virgin"))
        assertTrue(projection.spyRegistersGood("night", "Undertaker"))
        assertFalse(projection.spyCanRegister("Ravenkeeper"))
        assertTrue(projection.recluseCanRegister("Slayer"))
    }

    @Test fun `display interpretation filters manual role against allowed character types`() {
        val choices = ClocktowerRegistrationInteractionState()
        choices.chooseSpy("info", true, "Chef")
        val projection = ClocktowerRegistrationProjection(
            cards, ClocktowerScript.TroubleBrewing, choices,
            { null }, { _, card -> card.clocktowerRole?.enName?.let(::RoleId) },
        )
        assertEquals("Chef", projection.registeredRole("info", listOf(ClocktowerTeam.Townsfolk), "Washerwoman")?.enName)
        assertNull(projection.registeredRole("info", listOf(ClocktowerTeam.Outsider), "Librarian"))
        assertEquals("Spy", projection.registeredRole("untouched", listOf(ClocktowerTeam.Townsfolk), "Washerwoman")?.enName)
        assertNull(choices.explicitChoice(ClocktowerRegistrationSubject.SPY, "untouched"))
        assertTrue(projection.legalRegistrationRoles(null, "Washerwoman", spy, listOf(ClocktowerTeam.Townsfolk)).isEmpty())
    }
}
