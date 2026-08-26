package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerRegistrationCurrentRoleWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Spy and Recluse registration follow effective current role without alive gating`() {
        val registrationSetup = hostSource
            .substringAfter("var effectivePoisonForRole")
            .substringBefore("fun registrationKey")

        assertTrue(
            "Registration needs a current-role projection hook in addition to the existing poison hook.",
            registrationSetup.contains("var effectiveRoleForRegistration"),
        )

        val spyBlock = hostSource
            .substringAfter("fun spyCanRegister")
            .substringBefore("fun spyRegistersGood")
        assertTrue(
            "Spy registration requires current-role ownership and the existing poison check.",
            spyBlock.contains("effectiveRoleForRegistration") &&
                spyBlock.contains("RoleId(\"Spy\")") &&
                spyBlock.contains("effectivePoisonForRole"),
        )
        assertFalse(
            "Spy registration is an even-if-dead exception and must not gate on mechanical alive state.",
            spyBlock.contains("isMechanicallyAlive"),
        )

        val recluseBlock = hostSource
            .substringAfter("fun recluseCanRegister")
            .substringBefore("fun recluseRegistersEvil")
        assertTrue(
            "Recluse registration requires current-role ownership and the existing poison check.",
            recluseBlock.contains("effectiveRoleForRegistration") &&
                recluseBlock.contains("RoleId(\"Recluse\")") &&
                recluseBlock.contains("effectivePoisonForRole"),
        )
        assertFalse(
            "Recluse registration is an even-if-dead exception and must not gate on mechanical alive state.",
            recluseBlock.contains("isMechanicallyAlive"),
        )

        val effectiveRegistrationProjection = hostSource
            .substringAfter("effectivePoisonForRole = { enName ->")
            .substringBefore("fun roleActor")
        assertTrue(
            "Night registration current role must come from effective state at the querying role BEFORE cursor.",
            effectiveRegistrationProjection.contains("effectiveRoleForRegistration =") &&
                effectiveRegistrationProjection.contains("effectiveNightStateAt(") &&
                effectiveRegistrationProjection.contains("ClocktowerInteractionBoundary.BEFORE") &&
                effectiveRegistrationProjection.contains("currentRoleId(seat)"),
        )
    }
}
