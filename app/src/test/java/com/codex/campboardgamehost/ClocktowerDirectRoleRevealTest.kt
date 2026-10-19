package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.*
import org.junit.Test

class ClocktowerDirectRoleRevealTest {
    private val scriptRoles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)

    private fun step(role: String, target: Int?) = ClocktowerNightStepUi(
        title = role,
        actor = PlayerCard("P1"),
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = null,
        explanation = "",
        roleEnName = role,
        action = if (role == "Ravenkeeper") ClocktowerNightAction.Ravenkeeper
            else ClocktowerNightAction.None,
        informationQuestion = target?.let(ClocktowerInformationQuestion::Role),
    )
    @Test fun `Undertaker can record an actual role without a Host role answer grid`() {
        val choice = requireNotNull(clocktowerDirectRoleRevealChoice(
            step("Undertaker", 4), 4, 7, "Spy", scriptRoles, { it },
        ))
        assertEquals(4, choice.targetSeat)
        assertEquals(RoleId("Spy"), choice.roleId)
        assertEquals(InformationProposition.RoleAt(4, RoleId("Spy")), choice.displayOption.proposition)
        assertEquals(4, clocktowerUndertakerTypedResult(choice.displayOption.proposition, 7)?.executedSeat)
        assertTrue(clocktowerUndertakerSeatVisual(4, 1, 4, "en").badge == "EX")
    }
    @Test fun `Ravenkeeper target must be player confirmed before role input`() {
        val current = step("Ravenkeeper", 5)
        assertNull(clocktowerDirectRoleRevealChoice(current, null, 7, "Chef", scriptRoles, { it }))
        assertNull(clocktowerDirectRoleRevealChoice(current, 4, 7, "Chef", scriptRoles, { it }))
        assertNull(clocktowerDirectRoleRevealChoice(current, 8, 7, "Chef", scriptRoles, { it }))
        assertNull(clocktowerDirectRoleRevealChoice(current, 5, 7, "NotInScript", scriptRoles, { it }))
        val choice = requireNotNull(clocktowerDirectRoleRevealChoice(
            current, 5, 7, "Chef", scriptRoles, { it },
        ))
        assertEquals(RoleId("Chef"), clocktowerRavenkeeperTypedResult(
            choice.displayOption.proposition, 5, 7,
        )?.roleId)
    }
    @Test fun `typed AI suggested role preselects without a Host candidate list`() {
        val step = step("Ravenkeeper", 5)
        val proposed = requireNotNull(clocktowerDirectRoleRevealChoice(
            step, 5, 7, "Baron", scriptRoles, { it },
        )).displayOption
        assertEquals("Baron", clocktowerDirectRecommendedRoleReveal(
            step, 5, 7, scriptRoles, proposed,
        )?.roleId?.value)
        assertNull(clocktowerDirectRecommendedRoleReveal(step, 4, 7, scriptRoles, proposed))
        assertNull(clocktowerDirectRecommendedRoleReveal(step.copy(informationQuestion = null),
            5, 7, scriptRoles, proposed))
        assertNull(clocktowerDirectRecommendedRoleReveal(
            step, 5, 7, scriptRoles,
            proposed.copy(proposition = InformationProposition.RoleAt(8, RoleId("Baron"))),
        ))
        assertNull(clocktowerDirectRecommendedRoleReveal(
            step, 5, 7, scriptRoles,
            proposed.copy(proposition = InformationProposition.RoleAt(5, RoleId("Missing"))),
        ))
    }
    @Test fun `direct input never asserts or manufactures a character registration`() {
        val chosen = requireNotNull(clocktowerDirectRoleRevealChoice(
            step("Undertaker", 4), 4, 7, "Imp", scriptRoles, { it },
        ))
        assertFalse(chosen.displayOption.isTruthful)
        assertEquals(InformationProposition.RoleAt(4, RoleId("Imp")),
            resolveClocktowerPlayerDisplay(step("Undertaker", 4), chosen.displayOption).displayProposition)
        assertNull(clocktowerUndertakerTypedResult(InformationProposition.RoleInPlay(RoleId("Imp"), true), 7))
        assertNull(clocktowerRavenkeeperTypedResult(InformationProposition.RoleAt(5, RoleId("Chef")), 4, 7))
    }
}
