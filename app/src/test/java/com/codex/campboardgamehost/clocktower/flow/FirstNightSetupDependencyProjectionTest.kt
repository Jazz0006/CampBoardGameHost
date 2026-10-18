package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightSetupDependencyProjectionTest {
    @Test
    fun `built in projection preserves demon presentation and fortune teller red herring prerequisites`() {
        val interactions = listOf(
            system("first_night:system:demon_info", ClocktowerHostInteractionKind.EVIL_INFORMATION),
            role("Poisoner"),
            role("Spy"),
            redHerringSetup(),
            role("Fortune Teller"),
        )

        val projection = FirstNightSetupDependencyProjector.project(
            interactions = interactions,
            functioningRoleIds = setOf(RoleId("Poisoner"), RoleId("Spy"), RoleId("Fortune Teller")),
        )

        assertEquals(
            setOf(
                DeferredFirstNightSetupFact.DEMON_BLUFFS,
                DeferredFirstNightSetupFact.RED_HERRING,
            ),
            projection.activeFacts,
        )
        assertTrue(
            projection.dependencies.any {
                it.interactionId == id("first_night:system:demon_info") &&
                    it.fact == DeferredFirstNightSetupFact.DEMON_BLUFFS &&
                    it.kind == FirstNightSetupFactDependencyKind.PRESENTATION_REQUIRED
            },
        )
        assertTrue(
            projection.dependencies.any {
                it.interactionId == id("first_night:role:Fortune Teller") &&
                    it.fact == DeferredFirstNightSetupFact.RED_HERRING &&
                    it.kind == FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED
            },
        )
    }

    @Test
    fun `fortune teller role interaction without real red herring setup does not activate red herring fact`() {
        val projection = FirstNightSetupDependencyProjector.project(
            interactions = listOf(role("Fortune Teller")),
            functioningRoleIds = emptySet(),
        )

        assertFalse(DeferredFirstNightSetupFact.RED_HERRING in projection.activeFacts)
        assertTrue(projection.dependencies.none { it.fact == DeferredFirstNightSetupFact.RED_HERRING })
    }

    @Test
    fun `red herring setup moves before fortune teller and leaves spy stage uninterrupted`() {
        val interactions = listOf(
            system("first_night:system:minion_info", ClocktowerHostInteractionKind.EVIL_INFORMATION),
            system("first_night:system:demon_info", ClocktowerHostInteractionKind.EVIL_INFORMATION),
            role("Poisoner"),
            role("Spy"),
            role("Empath"),
            redHerringSetup(),
            role("Fortune Teller"),
            role("Butler"),
        )

        val ordered = FirstNightSetupCommitInteractionOrderer.order(
            interactions = interactions,
            functioningRoleIds = setOf(
                RoleId("Poisoner"),
                RoleId("Spy"),
                RoleId("Empath"),
                RoleId("Fortune Teller"),
                RoleId("Butler"),
            ),
        )

        assertEquals(
            listOf(
                "first_night:system:minion_info",
                "first_night:system:demon_info",
                "first_night:role:Poisoner",
                "first_night:role:Spy",
                "first_night:role:Empath",
                "first_night:fortune_teller:red_herring",
                "first_night:role:Fortune Teller",
                "first_night:role:Butler",
            ),
            ordered.map { it.id.value },
        )
    }

    @Test
    fun `fortune teller red herring barrier does not depend on spy functioning`() {
        val interactions = listOf(
            role("Poisoner"),
            role("Spy"),
            role("Empath"),
            redHerringSetup(),
            role("Fortune Teller"),
        )

        val ordered = FirstNightSetupCommitInteractionOrderer.order(
            interactions = interactions,
            functioningRoleIds = setOf(
                RoleId("Poisoner"),
                RoleId("Empath"),
                RoleId("Fortune Teller"),
            ),
        )

        assertEquals(
            listOf(
                "first_night:role:Poisoner",
                "first_night:role:Spy",
                "first_night:role:Empath",
                "first_night:fortune_teller:red_herring",
                "first_night:role:Fortune Teller",
            ),
            ordered.map { it.id.value },
        )
    }

    private fun redHerringSetup() = ClocktowerHostInteraction(
        id = id("first_night:fortune_teller:red_herring"),
        phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
        roleId = RoleId("Fortune Teller"),
        kind = ClocktowerHostInteractionKind.STORYTELLER_SETUP,
        completionPolicy = ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION,
    )

    private fun role(role: String) = ClocktowerHostInteraction(
        id = id("first_night:role:$role"),
        phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
        roleId = RoleId(role),
        kind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION,
        completionPolicy = ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION,
    )

    private fun system(
        value: String,
        kind: ClocktowerHostInteractionKind,
    ) = ClocktowerHostInteraction(
        id = id(value),
        phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
        roleId = null,
        kind = kind,
        completionPolicy = ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY,
    )

    private fun id(value: String) = ClocktowerInteractionId(value)
}
