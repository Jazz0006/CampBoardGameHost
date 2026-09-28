package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ClocktowerNewDemonIdentityContractTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `new Demon identity has one stable other-night production identity`() {
        assertEquals(
            ClocktowerInteractionId("other_night:event:imp:new_demon_identity"),
            ClocktowerProductionNightStepIdentity.newDemonIdentity()
                .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT),
        )
    }

    @Test
    fun `resolved Scarlet Woman promotion is ordered before current Imp action`() {
        val expected = listOf(
            Step("NewDemonIdentity", ClocktowerProductionNightStepIdentity.newDemonIdentity()),
            Step("DemonKill", ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))),
            Step("Empath", ClocktowerProductionNightStepIdentity.role(RoleId("Empath"))),
        )

        val ordered = ClocktowerProductionOtherNightFlow.order(
            ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing),
            playerCount = 7,
            wakingRoleIds = setOf(RoleId("Imp"), RoleId("Empath")),
            resolvedFacts = ClocktowerResolvedFlowFacts(
                setOf(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON),
            ),
            productionSteps = expected.reversed(),
            identityOf = Step::identity,
        )

        assertEquals(expected.map(Step::label), ordered.map(Step::label))
    }

    private data class Step(
        val label: String,
        val identity: ClocktowerProductionNightStepIdentity,
    )
}
