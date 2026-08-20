package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerProductionOtherNightFlowTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `Trouble Brewing production other night orders base roles and resolved events canonically`() {
        val wakingRoles = setOf(
            "Poisoner",
            "Monk",
            "Spy",
            "Imp",
            "Mayor",
            "Ravenkeeper",
            "Undertaker",
            "Empath",
            "Fortune Teller",
            "Butler",
        ).map(::RoleId).toSet()
        val facts = ClocktowerResolvedFlowFacts(
            setOf(
                ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED,
                ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE,
                ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT,
                ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY,
            ),
        )
        val expected = listOf(
            step("Poisoner", role("Poisoner")),
            step("Monk", role("Monk")),
            step("Spy", role("Spy")),
            step("DemonKill", role("Imp")),
            step("DemonSuccessor", ClocktowerProductionNightStepIdentity.demonSuccessor()),
            step("MayorRedirect", ClocktowerProductionNightStepIdentity.mayorRedirect()),
            step("Ravenkeeper", role("Ravenkeeper")),
            step("Undertaker", role("Undertaker")),
            step("Empath", role("Empath")),
            step("Fortune Teller", role("Fortune Teller")),
            step("Butler", role("Butler")),
        )

        val ordered = ClocktowerProductionOtherNightFlow.order(
            ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
            playerCount = 10,
            wakingRoleIds = wakingRoles,
            resolvedFacts = facts,
            productionSteps = expected.reversed(),
            identityOf = Step::identity,
        )

        assertEquals(expected.map(Step::label), ordered.map(Step::label))
    }

    @Test
    fun `No Greater Joy other night uses script override and Sage resolved fact`() {
        val wakingRoles = setOf(
            "Imp",
            "Sage",
            "Empath",
            "Chambermaid",
        ).map(::RoleId).toSet()
        val expected = listOf(
            step("DemonKill", role("Imp")),
            step("Sage", role("Sage")),
            step("Empath", role("Empath")),
            step("Chambermaid", role("Chambermaid")),
        )

        val ordered = ClocktowerProductionOtherNightFlow.order(
            ruleset = catalog.ruleset(ClocktowerScript.NoGreaterJoy),
            playerCount = 6,
            wakingRoleIds = wakingRoles,
            resolvedFacts = ClocktowerResolvedFlowFacts(
                setOf(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON),
            ),
            productionSteps = expected.reversed(),
            identityOf = Step::identity,
        )

        assertEquals(expected.map(Step::label), ordered.map(Step::label))
    }

    @Test
    fun `new Demon identity remains fail closed until its separate production screen is integrated`() {
        val failure = runCatching {
            ClocktowerProductionOtherNightFlow.order(
                ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
                playerCount = 7,
                wakingRoleIds = setOf(RoleId("Imp"), RoleId("Empath")),
                resolvedFacts = ClocktowerResolvedFlowFacts(
                    setOf(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON),
                ),
                productionSteps = listOf(
                    step("DemonKill", role("Imp")),
                    step("Empath", role("Empath")),
                ),
                identityOf = Step::identity,
            )
        }.exceptionOrNull()

        assertTrue("Expected separate new-Demon identity screen to stay fail closed.", failure is IllegalArgumentException)
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
