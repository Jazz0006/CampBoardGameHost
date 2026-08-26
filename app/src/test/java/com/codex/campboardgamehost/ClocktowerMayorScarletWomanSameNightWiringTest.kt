package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerMayorScarletWomanSameNightWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Mayor redirect Demon death forces Scarlet Woman at the death cursor without successor selection`() {
        val successionBlock = hostSource
            .substringAfter("val demonCard =")
            .substringBefore("val sageNightDeath")

        assertTrue(
            "Production must distinguish any resolved Demon death from Imp self-kill.",
            successionBlock.contains("demonActuallyDiedTonight") &&
                successionBlock.contains("resolvedNightDeathCard") &&
                successionBlock.contains("demonCard") &&
                successionBlock.contains("demonActuallyDied = demonActuallyDiedTonight") &&
                successionBlock.contains("demonDeathWasImpSelfKill = impSelfKillActuallyKilledImp"),
        )
        assertTrue(
            "Only an Imp self-kill may request the manual Demon-successor interaction.",
            successionBlock.contains("impSelfKillNeedsSuccessor") &&
                successionBlock.contains("impSelfKillActuallyKilledImp"),
        )
        assertTrue(
            "A non-self Demon death Forced resolution must expose the automatic Scarlet Woman seat separately.",
            successionBlock.contains("forcedNonSelfDemonSuccessorSeat") &&
                successionBlock.contains("DemonSuccessionResolution.Forced"),
        )

        val eventBlock = hostSource
            .substringAfter("val resolvedMechanicalEvents")
            .substringBefore("fun effectiveNightStateAt")

        assertTrue(
            "The automatic Scarlet Woman RoleChanged must use the same resolved death interaction cursor.",
            eventBlock.contains("forcedNonSelfDemonSuccessorSeat") &&
                eventBlock.contains("ResolvedNightMechanicalEvent.RoleChanged") &&
                eventBlock.contains("resolvedNightDeathInteractionId") &&
                eventBlock.contains("ClocktowerInteractionBoundary.AFTER"),
        )
        assertFalse(
            "The automatic non-self Scarlet Woman promotion must not require a confirmed manual successor target.",
            eventBlock.substringAfter("forcedNonSelfDemonSuccessorSeat").substringBefore("confirmedDemonSuccessorSeat")
                .contains("confirmedDemonSuccessorTarget"),
        )
    }
}
