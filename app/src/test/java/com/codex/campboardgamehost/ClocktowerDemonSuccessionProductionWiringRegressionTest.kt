package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerDemonSuccessionProductionWiringRegressionTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private fun successionBlock(): String = hostSource
        .substringAfter("val demonCard =")
        .substringBefore("val sageNightDeath =")

    @Test
    fun `production self kill succession requires confirmed self target and actual Imp death`() {
        val successionBlock = successionBlock()

        assertTrue(
            "A Mayor redirect or other non-self Demon death must not be classified as an Imp self-kill.",
            successionBlock.contains("pendingNightDeath == livingImp.name") &&
                successionBlock.contains("nightDeathWillOccur") &&
                successionBlock.contains("resolvedNightDeathCard?.name == livingImp.name"),
        )
        assertTrue(
            "SNE-6B2.1 production wiring must feed only the proven self-kill into succession; " +
                "non-self Demon death support remains pure-only until SNE-6C.",
            successionBlock.contains("demonActuallyDied = impSelfKillActuallyKilledImp") &&
                successionBlock.contains("demonDeathWasImpSelfKill = impSelfKillActuallyKilledImp"),
        )
    }

    @Test
    fun `living Minion successor seats preserve original table seat identity`() {
        val successionBlock = successionBlock()

        assertTrue(
            "Living Minion seats must be resolved against the original cards list, not the filtered " +
                "publicAliveCards index, because earlier dead seats must not renumber later players.",
            successionBlock.contains("cards.indexOf(card).plus(1)"),
        )
        assertFalse(
            "Filtering dead players and then using mapIndexed would silently renumber seats.",
            successionBlock.contains("publicAliveCards\n                .mapIndexedNotNull { index, card ->"),
        )
    }
}
