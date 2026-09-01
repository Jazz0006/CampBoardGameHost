package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDisplayedInformationReliabilityTest {
    @Test
    fun `evil-team introduction without a role ability is safe to display`() {
        val step = ClocktowerNightStepUi(
            title = "Minion info",
            actor = playerCardWithRole("Spy"),
            isRealAction = true,
            reason = "",
            storytellerAction = "Wake the Minions",
            tellPlayer = "The Demon is seat 7",
            explanation = "Show the Demon",
            displayKind = ClocktowerDisplayKind.EvilInfo,
            roleEnName = null,
        )
        var abilityReliabilityChecked = false

        val unreliable = clocktowerDisplayedInformationIsUnreliable(step) { role, _ ->
            abilityReliabilityChecked = true
            RoleId(role)
            false
        }

        assertFalse(unreliable)
        assertFalse(abilityReliabilityChecked)
    }

    @Test
    fun `role information still uses actor reliability`() {
        val step = ClocktowerNightStepUi(
            title = "Empath info",
            actor = playerCardWithRole("Empath"),
            isRealAction = true,
            reason = "",
            storytellerAction = "Show the number",
            tellPlayer = "1",
            explanation = "Living evil neighbours",
            displayKind = ClocktowerDisplayKind.Number,
            roleEnName = "Empath",
        )

        assertTrue(clocktowerDisplayedInformationIsUnreliable(step) { role, actor ->
            role == "Empath" && actor == step.actor
        })
    }

    private fun playerCardWithRole(roleName: String): PlayerCard = PlayerCard(
        name = "Player",
        role = Role.Civilian,
        word = "",
        clocktowerRole = ClocktowerRole(
            team = ClocktowerTeam.Minion,
            zhName = roleName,
            enName = roleName,
            zhDescription = "",
            enDescription = "",
        ),
    )
}
