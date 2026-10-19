package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ClocktowerProductionOtherNightFlowTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `Trouble Brewing later-night steps follow script order and confirmed event flags`() {
        val roles = setOf(
            "Poisoner", "Monk", "Spy", "Imp", "Mayor", "Ravenkeeper",
            "Undertaker", "Empath", "Fortune Teller", "Butler",
        ).map(::RoleId).toSet()
        val facts = ClocktowerResolvedFlowFacts(
            setOf(
                ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED,
                ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE,
                ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT,
                ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY,
            ),
        )
        assertEquals(
            listOf(
                "other_night:role:Poisoner",
                "other_night:role:Monk",
                "other_night:role:Spy",
                "other_night:role:Imp",
                "other_night:event:imp:demon_successor",
                "other_night:event:mayor:death_resolution",
                "other_night:role:Ravenkeeper",
                "other_night:role:Undertaker",
                "other_night:role:Empath",
                "other_night:role:Fortune Teller",
                "other_night:role:Butler",
            ),
            ClocktowerProductionOtherNightFlow.interactions(
                ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
                playerCount = 10,
                wakingRoleIds = roles,
                resolvedFacts = facts,
            ).actionIds(),
        )
    }

    @Test
    fun `No Greater Joy later-night steps follow script order and Sage death event`() {
        val roles = setOf("Imp", "Sage", "Empath", "Chambermaid").map(::RoleId).toSet()
        assertEquals(
            listOf(
                "other_night:role:Imp",
                "other_night:role:Sage",
                "other_night:role:Empath",
                "other_night:role:Chambermaid",
            ),
            ClocktowerProductionOtherNightFlow.interactions(
                ruleset = catalog.ruleset(ClocktowerScript.NoGreaterJoy),
                playerCount = 6,
                wakingRoleIds = roles,
                resolvedFacts = ClocktowerResolvedFlowFacts(
                    setOf(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON),
                ),
            ).actionIds(),
        )
    }

    @Test
    fun `confirmed new Demon identity appears before current Imp action`() {
        assertEquals(
            listOf(
                "other_night:event:imp:new_demon_identity",
                "other_night:role:Imp",
                "other_night:role:Empath",
            ),
            ClocktowerProductionOtherNightFlow.interactions(
                ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
                playerCount = 7,
                wakingRoleIds = setOf(RoleId("Imp"), RoleId("Empath")),
                resolvedFacts = ClocktowerResolvedFlowFacts(
                    setOf(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON),
                ),
            ).actionIds(),
        )
    }

    private fun List<ClocktowerHostInteraction>.actionIds(): List<String> =
        filterNot { it.kind == ClocktowerHostInteractionKind.SYSTEM_BOUNDARY }
            .map { it.id.value }
}

