package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerProductionInteractionOrdererTest {
    @Test
    fun `first night production steps follow projected interaction order`() {
        val projected = listOf(
            interaction("first_night:system:dusk", kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY),
            interaction("first_night:system:minion_info", kind = ClocktowerHostInteractionKind.EVIL_INFORMATION),
            interaction("first_night:system:demon_info", kind = ClocktowerHostInteractionKind.EVIL_INFORMATION),
            interaction("first_night:role:Poisoner", role = "Poisoner"),
            interaction(
                "first_night:fortune_teller:red_herring",
                role = "Fortune Teller",
                kind = ClocktowerHostInteractionKind.STORYTELLER_SETUP,
            ),
            interaction("first_night:role:Fortune Teller", role = "Fortune Teller"),
            interaction("first_night:role:Butler", role = "Butler"),
            interaction("first_night:system:dawn", kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY),
        )
        val steps = listOf(
            Step("butler", ClocktowerProductionNightStepIdentity.role(RoleId("Butler"))),
            Step("fortune", ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller"))),
            Step("demon-info", ClocktowerProductionNightStepIdentity.demonInfo()),
            Step("poisoner", ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner"))),
            Step("red-herring", ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()),
            Step("minion-info", ClocktowerProductionNightStepIdentity.minionInfo()),
        )

        assertEquals(
            listOf("minion-info", "demon-info", "poisoner", "red-herring", "fortune", "butler"),
            ClocktowerProductionInteractionOrderer.order(
                phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
                projectedInteractions = projected,
                productionSteps = steps,
                identityOf = Step::identity,
            ).map(Step::label),
        )
    }

    @Test
    fun `other night event identities stay distinct from their anchor roles`() {
        val projected = listOf(
            interaction("other_night:system:dusk", kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY),
            interaction("other_night:role:Imp", role = "Imp"),
            interaction(
                "other_night:event:imp:demon_successor",
                role = "Imp",
                kind = ClocktowerHostInteractionKind.EVENT_RESOLUTION,
            ),
            interaction(
                "other_night:event:mayor:death_resolution",
                role = "Mayor",
                kind = ClocktowerHostInteractionKind.EVENT_RESOLUTION,
            ),
            interaction("other_night:role:Ravenkeeper", role = "Ravenkeeper"),
            interaction("other_night:role:Undertaker", role = "Undertaker"),
            interaction("other_night:system:dawn", kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY),
        )
        val steps = listOf(
            Step("undertaker", ClocktowerProductionNightStepIdentity.role(RoleId("Undertaker"))),
            Step("successor", ClocktowerProductionNightStepIdentity.demonSuccessor()),
            Step("imp-kill", ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))),
            Step("ravenkeeper", ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper"))),
            Step("mayor", ClocktowerProductionNightStepIdentity.mayorRedirect()),
        )

        assertEquals(
            listOf("imp-kill", "successor", "mayor", "ravenkeeper", "undertaker"),
            ClocktowerProductionInteractionOrderer.order(
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                projectedInteractions = projected,
                productionSteps = steps,
                identityOf = Step::identity,
            ).map(Step::label),
        )
    }

    @Test
    fun `production orderer fails closed when legacy UI has an extra step`() {
        val projected = listOf(interaction("other_night:role:Imp", role = "Imp"))
        val steps = listOf(
            Step("imp", ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))),
            Step("poisoner", ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner"))),
        )

        assertFails {
            ClocktowerProductionInteractionOrderer.order(
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                projectedInteractions = projected,
                productionSteps = steps,
                identityOf = Step::identity,
            )
        }
    }

    @Test
    fun `production orderer fails closed when planner has an unmatched actionable interaction`() {
        val projected = listOf(
            interaction("other_night:role:Imp", role = "Imp"),
            interaction("other_night:role:Empath", role = "Empath"),
        )
        val steps = listOf(Step("imp", ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))))

        assertFails {
            ClocktowerProductionInteractionOrderer.order(
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                projectedInteractions = projected,
                productionSteps = steps,
                identityOf = Step::identity,
            )
        }
    }

    @Test
    fun `production orderer rejects duplicate step identity`() {
        val projected = listOf(interaction("other_night:role:Imp", role = "Imp"))
        val steps = listOf(
            Step("imp-a", ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))),
            Step("imp-b", ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))),
        )

        assertFails {
            ClocktowerProductionInteractionOrderer.order(
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                projectedInteractions = projected,
                productionSteps = steps,
                identityOf = Step::identity,
            )
        }
    }

    @Test
    fun `system boundaries are not required to have production UI steps`() {
        val projected = listOf(
            interaction("other_night:system:dusk", kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY),
            interaction("other_night:role:Imp", role = "Imp"),
            interaction("other_night:system:dawn", kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY),
        )

        assertEquals(
            listOf("imp"),
            ClocktowerProductionInteractionOrderer.order(
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                projectedInteractions = projected,
                productionSteps = listOf(Step("imp", ClocktowerProductionNightStepIdentity.role(RoleId("Imp")))),
                identityOf = Step::identity,
            ).map(Step::label),
        )
    }

    @Test
    fun `step identity validates role and phase semantics`() {
        assertEquals(
            ClocktowerInteractionId("first_night:role:Empath"),
            ClocktowerProductionNightStepIdentity.role(RoleId("Empath"))
                .interactionId(ClocktowerNightFlowPhase.FIRST_NIGHT),
        )
        assertEquals(
            ClocktowerInteractionId("other_night:event:imp:demon_successor"),
            ClocktowerProductionNightStepIdentity.demonSuccessor()
                .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT),
        )
        assertFails {
            ClocktowerProductionNightStepIdentity.minionInfo()
                .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        }
        assertFails {
            ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()
                .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        }
    }

    private data class Step(
        val label: String,
        val identity: ClocktowerProductionNightStepIdentity,
    )

    private fun interaction(
        id: String,
        role: String? = null,
        kind: ClocktowerHostInteractionKind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION,
    ): ClocktowerHostInteraction = ClocktowerHostInteraction(
        id = ClocktowerInteractionId(id),
        phase = if (id.startsWith("first_night:")) {
            ClocktowerNightFlowPhase.FIRST_NIGHT
        } else {
            ClocktowerNightFlowPhase.OTHER_NIGHT
        },
        roleId = role?.let(::RoleId),
        kind = kind,
        completionPolicy = when (kind) {
            ClocktowerHostInteractionKind.SYSTEM_BOUNDARY -> ClocktowerInteractionCompletionPolicy.SYSTEM_TRANSITION
            ClocktowerHostInteractionKind.EVIL_INFORMATION -> ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY
            ClocktowerHostInteractionKind.ROLE_PHASE_ACTION -> ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION
            ClocktowerHostInteractionKind.STORYTELLER_SETUP,
            ClocktowerHostInteractionKind.EVENT_RESOLUTION -> ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION
        },
    )

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected production flow validation to fail closed.", failed)
    }
}
