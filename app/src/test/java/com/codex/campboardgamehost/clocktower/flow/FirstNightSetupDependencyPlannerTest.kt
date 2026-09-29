package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightSetupDependencyPlannerTest {
    @Test
    fun `plans each uncommitted fact at its earliest semantic dependency`() {
        val minionInfo = id("first_night:system:minion_info")
        val demonInfo = id("first_night:system:demon_info")
        val poisoner = id("first_night:role:Poisoner")
        val fortuneTeller = id("first_night:role:Fortune Teller")

        val barriers = FirstNightSetupDependencyPlanner.plan(
            orderedInteractionIds = listOf(minionInfo, demonInfo, poisoner, fortuneTeller),
            dependencies = listOf(
                FirstNightSetupFactDependency(
                    interactionId = demonInfo,
                    fact = DeferredFirstNightSetupFact.DEMON_BLUFFS,
                    kind = FirstNightSetupFactDependencyKind.PRESENTATION_REQUIRED,
                ),
                FirstNightSetupFactDependency(
                    interactionId = fortuneTeller,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                ),
            ),
        )

        assertEquals(
            listOf(
                FirstNightSetupCommitmentBarrier(
                    fact = DeferredFirstNightSetupFact.DEMON_BLUFFS,
                    beforeInteractionId = demonInfo,
                    dependencyKinds = setOf(
                        FirstNightSetupFactDependencyKind.PRESENTATION_REQUIRED,
                    ),
                ),
                FirstNightSetupCommitmentBarrier(
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    beforeInteractionId = fortuneTeller,
                    dependencyKinds = setOf(
                        FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                    ),
                ),
            ),
            barriers,
        )
    }

    @Test
    fun `earlier grimoire observation moves red herring barrier before fortune teller`() {
        val poisoner = id("first_night:role:Poisoner")
        val grimoireObserver = id("first_night:role:SomeObserver")
        val fortuneTeller = id("first_night:role:Fortune Teller")

        val barriers = FirstNightSetupDependencyPlanner.plan(
            orderedInteractionIds = listOf(poisoner, grimoireObserver, fortuneTeller),
            dependencies = listOf(
                FirstNightSetupFactDependency(
                    interactionId = fortuneTeller,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                ),
                FirstNightSetupFactDependency(
                    interactionId = grimoireObserver,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.OBSERVATION_REQUIRED,
                ),
            ),
        )

        assertEquals(1, barriers.size)
        assertEquals(grimoireObserver, barriers.single().beforeInteractionId)
        assertEquals(
            setOf(FirstNightSetupFactDependencyKind.OBSERVATION_REQUIRED),
            barriers.single().dependencyKinds,
        )
    }

    @Test
    fun `non observing interaction does not move red herring barrier earlier`() {
        val poisoner = id("first_night:role:Poisoner")
        val malfunctioningObserver = id("first_night:role:SomeObserver")
        val fortuneTeller = id("first_night:role:Fortune Teller")

        val barriers = FirstNightSetupDependencyPlanner.plan(
            orderedInteractionIds = listOf(poisoner, malfunctioningObserver, fortuneTeller),
            dependencies = listOf(
                FirstNightSetupFactDependency(
                    interactionId = fortuneTeller,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                ),
            ),
        )

        assertEquals(fortuneTeller, barriers.single().beforeInteractionId)
    }

    @Test
    fun `committed fact is omitted from pending barriers`() {
        val demonInfo = id("first_night:system:demon_info")
        val fortuneTeller = id("first_night:role:Fortune Teller")

        val barriers = FirstNightSetupDependencyPlanner.plan(
            orderedInteractionIds = listOf(demonInfo, fortuneTeller),
            dependencies = listOf(
                FirstNightSetupFactDependency(
                    interactionId = demonInfo,
                    fact = DeferredFirstNightSetupFact.DEMON_BLUFFS,
                    kind = FirstNightSetupFactDependencyKind.PRESENTATION_REQUIRED,
                ),
                FirstNightSetupFactDependency(
                    interactionId = fortuneTeller,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                ),
            ),
            committedFacts = setOf(DeferredFirstNightSetupFact.DEMON_BLUFFS),
        )

        assertEquals(
            listOf(DeferredFirstNightSetupFact.RED_HERRING),
            barriers.map(FirstNightSetupCommitmentBarrier::fact),
        )
    }

    @Test
    fun `multiple dependency kinds at same earliest interaction are retained`() {
        val observer = id("first_night:role:Observer")

        val barriers = FirstNightSetupDependencyPlanner.plan(
            orderedInteractionIds = listOf(observer),
            dependencies = listOf(
                FirstNightSetupFactDependency(
                    interactionId = observer,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.OBSERVATION_REQUIRED,
                ),
                FirstNightSetupFactDependency(
                    interactionId = observer,
                    fact = DeferredFirstNightSetupFact.RED_HERRING,
                    kind = FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
                ),
            ),
        )

        assertEquals(
            setOf(
                FirstNightSetupFactDependencyKind.OBSERVATION_REQUIRED,
                FirstNightSetupFactDependencyKind.RESULT_SEMANTICS_REQUIRED,
            ),
            barriers.single().dependencyKinds,
        )
    }

    @Test
    fun `dependency on unknown interaction fails closed`() {
        val known = id("first_night:role:Known")
        val unknown = id("first_night:role:Unknown")

        assertThrows(IllegalArgumentException::class.java) {
            FirstNightSetupDependencyPlanner.plan(
                orderedInteractionIds = listOf(known),
                dependencies = listOf(
                    FirstNightSetupFactDependency(
                        interactionId = unknown,
                        fact = DeferredFirstNightSetupFact.RED_HERRING,
                        kind = FirstNightSetupFactDependencyKind.OBSERVATION_REQUIRED,
                    ),
                ),
            )
        }
    }

    @Test
    fun `duplicate ordered interaction identities fail closed`() {
        val duplicated = id("first_night:role:Duplicate")

        val error = assertThrows(IllegalArgumentException::class.java) {
            FirstNightSetupDependencyPlanner.plan(
                orderedInteractionIds = listOf(duplicated, duplicated),
                dependencies = emptyList(),
            )
        }

        assertTrue(error.message.orEmpty().contains("unique", ignoreCase = true))
    }

    private fun id(value: String): ClocktowerInteractionId = ClocktowerInteractionId(value)
}
