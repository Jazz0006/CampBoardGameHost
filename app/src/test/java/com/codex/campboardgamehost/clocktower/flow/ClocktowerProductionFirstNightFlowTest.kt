package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ClocktowerProductionFirstNightFlowTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `Trouble Brewing production first night is ordered exclusively by canonical planner interactions`() {
        val inPlay = setOf(
            "Poisoner", "Spy", "Chef", "Empath", "Fortune Teller", "Butler", "Imp",
        ).map(::RoleId).toSet()
        val expected = listOf(
            step("minion-info", ClocktowerProductionNightStepIdentity.minionInfo()),
            step("demon-info", ClocktowerProductionNightStepIdentity.demonInfo()),
            step("Poisoner", role("Poisoner")),
            step("Spy", role("Spy")),
            step("Chef", role("Chef")),
            step("Empath", role("Empath")),
            step("red-herring", ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()),
            step("Fortune Teller", role("Fortune Teller")),
            step("Butler", role("Butler")),
        )

        val ordered = ClocktowerProductionFirstNightFlow.order(
            ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
            playerCount = 7,
            inPlayRoleIds = inPlay,
            productionSteps = expected.reversed(),
            identityOf = Step::identity,
        )

        assertEquals(expected.map(Step::label), ordered.map(Step::label))
    }

    @Test
    fun `No Greater Joy production first night follows canonical script override not legacy host ordering`() {
        val inPlay = setOf(
            "Investigator", "Empath", "Clockmaker", "Chambermaid", "Scarlet Woman", "Imp",
        ).map(::RoleId).toSet()
        val productionSteps = listOf(
            step("Clockmaker", role("Clockmaker")),
            step("Chambermaid", role("Chambermaid")),
            step("Empath", role("Empath")),
            step("Investigator", role("Investigator")),
        )

        val ordered = ClocktowerProductionFirstNightFlow.order(
            ruleset = catalog.ruleset(ClocktowerScript.NoGreaterJoy),
            playerCount = 6,
            inPlayRoleIds = inPlay,
            productionSteps = productionSteps,
            identityOf = Step::identity,
        )

        assertEquals(
            listOf("Investigator", "Empath", "Clockmaker", "Chambermaid"),
            ordered.map(Step::label),
        )
    }

    private fun role(name: String): ClocktowerProductionNightStepIdentity =
        ClocktowerProductionNightStepIdentity.role(RoleId(name))

    private fun step(
        label: String,
        identity: ClocktowerProductionNightStepIdentity,
    ): Step = Step(label, identity)

    private data class Step(
        val label: String,
        val identity: ClocktowerProductionNightStepIdentity,
    )
}
