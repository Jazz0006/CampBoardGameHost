package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionCompletionPolicy
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerNightStepMaterializerRegistryTest {
    private val phase = ClocktowerNightFlowPhase.FIRST_NIGHT

    @Test
    fun `materializes only projected actionable interactions in planner order`() {
        var unprojectedBuildCount = 0
        val empathIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Empath"))
        val fortuneTellerIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller"))
        val butlerIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Butler"))
        val registry = ClocktowerNightStepMaterializerRegistry(
            phase = phase,
            entries = listOf(
                ClocktowerNightStepMaterializerRegistry.Entry(
                    identity = fortuneTellerIdentity,
                    build = { step("Fortune Teller") },
                ),
                ClocktowerNightStepMaterializerRegistry.Entry(
                    identity = empathIdentity,
                    build = { step("Empath") },
                ),
                ClocktowerNightStepMaterializerRegistry.Entry(
                    identity = butlerIdentity,
                    build = {
                        unprojectedBuildCount += 1
                        step("Butler")
                    },
                ),
            ),
        )

        val materialized = registry.materialize(
            listOf(
                systemBoundary("first_night:system:dusk"),
                roleInteraction(empathIdentity, "Empath"),
                roleInteraction(fortuneTellerIdentity, "Fortune Teller"),
                systemBoundary("first_night:system:dawn"),
            ),
        )

        assertEquals(listOf("Empath", "Fortune Teller"), materialized.map { it.title })
        assertEquals(0, unprojectedBuildCount)
    }

    @Test
    fun `other night omits normal role step without effective actor but keeps death trigger actor`() {
        val otherNightPhase = ClocktowerNightFlowPhase.OTHER_NIGHT
        val fortuneTellerIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller"))
        val ravenkeeperIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper"))
        val ravenkeeperActor = PlayerCard(
            name = "Ravenkeeper",
            role = Role.Civilian,
            word = "",
        )
        val registry = ClocktowerNightStepMaterializerRegistry(
            phase = otherNightPhase,
            entries = listOf(
                ClocktowerNightStepMaterializerRegistry.Entry(
                    identity = fortuneTellerIdentity,
                    build = { step("Fortune Teller") },
                ),
                ClocktowerNightStepMaterializerRegistry.Entry(
                    identity = ravenkeeperIdentity,
                    build = { step("Ravenkeeper", actor = ravenkeeperActor) },
                ),
            ),
        )

        val materialized = registry.materialize(
            listOf(
                roleInteraction(fortuneTellerIdentity, "Fortune Teller", otherNightPhase),
                roleInteraction(ravenkeeperIdentity, "Ravenkeeper", otherNightPhase),
            ),
        )

        assertEquals(listOf("Ravenkeeper"), materialized.map { it.title })
    }

    @Test
    fun `missing projected actionable interaction fails closed`() {
        val empathIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Empath"))
        val chefIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Chef"))
        val registry = ClocktowerNightStepMaterializerRegistry(
            phase = phase,
            entries = listOf(
                ClocktowerNightStepMaterializerRegistry.Entry(
                    identity = empathIdentity,
                    build = { step("Empath") },
                ),
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            registry.materialize(listOf(roleInteraction(chefIdentity, "Chef")))
        }
    }

    @Test
    fun `duplicate registered interaction identity fails closed`() {
        val empathIdentity = ClocktowerProductionNightStepIdentity.role(RoleId("Empath"))

        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerNightStepMaterializerRegistry(
                phase = phase,
                entries = listOf(
                    ClocktowerNightStepMaterializerRegistry.Entry(
                        identity = empathIdentity,
                        build = { step("Empath A") },
                    ),
                    ClocktowerNightStepMaterializerRegistry.Entry(
                        identity = empathIdentity,
                        build = { step("Empath B") },
                    ),
                ),
            )
        }
    }

    @Test
    fun `production night flow helpers expose canonical projected interactions`() {
        assertTrue(
            "First-night production flow must expose canonical projected interactions before UI materialization",
            ClocktowerProductionFirstNightFlow::class.java.declaredMethods.any { it.name == "interactions" },
        )
        assertTrue(
            "Other-night production flow must expose canonical projected interactions before UI materialization",
            ClocktowerProductionOtherNightFlow::class.java.declaredMethods.any { it.name == "interactions" },
        )
    }

    private fun roleInteraction(
        identity: ClocktowerProductionNightStepIdentity,
        roleName: String,
        interactionPhase: ClocktowerNightFlowPhase = phase,
    ): ClocktowerHostInteraction = ClocktowerHostInteraction(
        id = identity.interactionId(interactionPhase),
        phase = interactionPhase,
        roleId = RoleId(roleName),
        kind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION,
        completionPolicy = ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION,
    )

    private fun systemBoundary(id: String): ClocktowerHostInteraction = ClocktowerHostInteraction(
        id = ClocktowerInteractionId(id),
        phase = phase,
        roleId = null,
        kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY,
        completionPolicy = ClocktowerInteractionCompletionPolicy.SYSTEM_TRANSITION,
    )

    private fun step(
        title: String,
        actor: PlayerCard? = null,
    ): ClocktowerNightStepUi = ClocktowerNightStepUi(
        title = title,
        actor = actor,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = null,
        explanation = "",
    )
}
