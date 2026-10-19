package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class ClocktowerProductionFirstNightFlowTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `Trouble Brewing first-night steps follow script order and include real Fortune Teller setup`() {
        val inPlay = setOf(
            "Poisoner", "Spy", "Chef", "Empath", "Fortune Teller", "Butler", "Imp",
        ).map(::RoleId).toSet()
        assertEquals(
            listOf(
                "first_night:system:minion_info",
                "first_night:system:demon_info",
                "first_night:role:Poisoner",
                "first_night:role:Spy",
                "first_night:role:Chef",
                "first_night:role:Empath",
                "first_night:fortune_teller:red_herring",
                "first_night:role:Fortune Teller",
                "first_night:role:Butler",
            ),
            ClocktowerProductionFirstNightFlow.interactions(
                ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
                playerCount = 7,
                inPlayRoleIds = inPlay,
                actualRoleIds = inPlay,
            ).actionIds(),
        )
    }

    @Test
    fun `No Greater Joy first-night steps follow its script order`() {
        val inPlay = setOf(
            "Investigator", "Empath", "Clockmaker", "Chambermaid", "Scarlet Woman", "Imp",
        ).map(::RoleId).toSet()
        assertEquals(
            listOf(
                "first_night:role:Investigator",
                "first_night:role:Empath",
                "first_night:role:Clockmaker",
                "first_night:role:Chambermaid",
            ),
            ClocktowerProductionFirstNightFlow.interactions(
                ruleset = catalog.ruleset(ClocktowerScript.NoGreaterJoy),
                playerCount = 6,
                inPlayRoleIds = inPlay,
            ).actionIds(),
        )
    }

    @Test
    fun `Drunk shown Fortune Teller wakes without real red-herring setup`() {
        val waking = setOf("Fortune Teller", "Drunk", "Imp").map(::RoleId).toSet()
        val actual = setOf("Drunk", "Imp").map(::RoleId).toSet()
        val steps = ClocktowerProductionFirstNightFlow.interactions(
            ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
            playerCount = 6,
            inPlayRoleIds = waking,
            actualRoleIds = actual,
        )
        assertEquals(listOf("first_night:role:Fortune Teller"), steps.actionIds())
        assertFalse(steps.any { it.id.value == "first_night:fortune_teller:red_herring" })
    }

    private fun List<ClocktowerHostInteraction>.actionIds(): List<String> =
        filterNot { it.kind == ClocktowerHostInteractionKind.SYSTEM_BOUNDARY }
            .map { it.id.value }
}

