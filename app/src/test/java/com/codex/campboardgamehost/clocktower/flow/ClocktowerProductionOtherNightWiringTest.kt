package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Coarse production ownership guards for the non-callable Other Night Host boundary.
 * Canonical ordering, role eligibility and resolved-fact semantics are proved by typed flow tests.
 */
class ClocktowerProductionOtherNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production other night consumes canonical planner and stable materializer boundaries`() {
        assertTrue(source.contains("ClocktowerProductionOtherNightFlow.interactions("))
        assertTrue(source.contains("ClocktowerResolvedFlowFacts("))
        assertTrue(source.contains("ClocktowerNightStepMaterializerRegistry("))
        assertTrue(source.contains("ClocktowerNightFlowPhase.OTHER_NIGHT"))
        assertTrue(source.contains(".materialize("))
    }

    @Test
    fun `production other night does not restore legacy parallel ordering authority`() {
        assertFalse(source.contains("val unfilteredNightSteps ="))
        assertFalse(source.contains("val filteredNightSteps ="))
        assertFalse(source.contains("ClocktowerProductionOtherNightFlow.order("))
        assertFalse(source.contains("fun legacyOtherNightOrder"))
    }
}
