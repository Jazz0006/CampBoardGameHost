package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerRoleActorCurrentRoleWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `night role actor lookup uses effective current role and preserves Drunk shown role semantics`() {
        val effectiveSubject = hostSource
            .substringAfter("fun effectiveAbilitySubjectForRole(")
            .substringBefore("effectivePoisonForRole =")

        assertTrue(
            "The effective ability subject must replace the public actual role with the role at the querying cursor.",
            effectiveSubject.contains("actualRole = state.currentRoleId(seat)?.value"),
        )
        assertTrue(
            "Current-role projection must keep using the existing PlayerCard abilitySubject so shownRole remains available for Drunk semantics.",
            effectiveSubject.contains("actor.abilitySubject(") &&
                effectiveSubject.contains("effectivePoisonTargetAt("),
        )

        val roleActor = hostSource
            .substringAfter("fun roleActor(enName: String): PlayerCard? {")
            .substringBefore("fun roleMissingReason(enName: String)")

        assertFalse(
            "Night actor lookup must not commit to the first public-role candidate before effective current-role evaluation.",
            roleActor.contains("val candidate = cards.firstOrNull"),
        )
        assertTrue(
            "Night actor lookup must evaluate cards through effectiveAbilitySubjectForRole at the role interaction cursor.",
            roleActor.contains("cards.firstOrNull { candidate ->") &&
                roleActor.contains("effectiveAbilitySubjectForRole(enName, candidate)") &&
                roleActor.contains("AbilityFunctioningSemantics.interactsAs(") &&
                roleActor.contains("effectiveSubject") &&
                roleActor.contains("enName,"),
        )
        assertTrue(
            "Outside the night effective-state path, existing public-role/Drunk shown-role lookup must remain available.",
            roleActor.contains("if (phase != ClocktowerPhase.Night)") &&
                roleActor.contains("AbilityFunctioningSemantics.interactsAs(") &&
                roleActor.contains("it.abilitySubject(null)"),
        )
    }
}
