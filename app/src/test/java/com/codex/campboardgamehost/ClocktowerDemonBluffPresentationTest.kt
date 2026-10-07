package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDemonBluffPresentationTest {
    @Test
    fun `exact three roles resolve in input order`() {
        val legal = listOf(
            role("Chef"),
            role("Mayor"),
            role("Monk"),
            role("Undertaker"),
        )

        val result = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk", "Undertaker"),
            legalRoles = legal,
        )

        assertEquals(
            listOf("Mayor", "Monk", "Undertaker"),
            (result as DemonBluffPresentationResolution.Ready).roles.map { it.enName },
        )
    }

    @Test
    fun `missing input stays pending and never becomes first three legal roles`() {
        val result = resolveDemonBluffPresentation(
            recommendedRoleNames = null,
            legalRoles = listOf(role("Chef"), role("Empath"), role("Fortune Teller"), role("Mayor")),
        )

        assertTrue(result is DemonBluffPresentationResolution.Pending)
    }

    @Test
    fun `partial or unresolved input is invalid and never silently substituted`() {
        val legal = listOf(role("Chef"), role("Empath"), role("Mayor"), role("Monk"))

        val partial = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk"),
            legalRoles = legal,
        )
        val unresolved = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk", "Undertaker"),
            legalRoles = legal,
        )

        assertTrue(partial is DemonBluffPresentationResolution.Invalid)
        assertEquals(
            listOf("Undertaker"),
            (unresolved as DemonBluffPresentationResolution.Invalid).unresolvedRoleNames,
        )
    }

    @Test
    fun `manual bluff selection is legal only and capped at three`() {
        val legal = setOf("Chef", "Empath", "Mayor", "Monk")

        val one = toggleManualDemonBluffSelection(emptyList(), "Chef", legal)
        val two = toggleManualDemonBluffSelection(one, "Empath", legal)
        val three = toggleManualDemonBluffSelection(two, "Mayor", legal)

        assertEquals(listOf("Chef", "Empath", "Mayor"), three)
        assertEquals(three, toggleManualDemonBluffSelection(three, "Monk", legal))
        assertEquals(three, toggleManualDemonBluffSelection(three, "Spy", legal))
        assertEquals(
            listOf("Chef", "Mayor"),
            toggleManualDemonBluffSelection(three, "Empath", legal),
        )
    }

    @Test
    fun `manual bluff selection is ready only for an exact legal triple`() {
        val legal = listOf(role("Chef"), role("Empath"), role("Mayor"), role("Monk"))

        assertTrue(
            manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath", "Mayor"),
                legalRoles = legal,
            ),
        )
        assertTrue(
            !manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath"),
                legalRoles = legal,
            ),
        )
        assertTrue(
            !manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath", "Spy"),
                legalRoles = legal,
            ),
        )
    }

    private fun role(enName: String) = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = enName,
        enName = enName,
        zhDescription = "",
        enDescription = "",
    )
}
