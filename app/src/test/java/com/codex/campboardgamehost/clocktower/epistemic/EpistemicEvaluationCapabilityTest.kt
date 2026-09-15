package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpistemicEvaluationCapabilityTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `built-in Trouble Brewing is ready for exact historical hypothetical evaluation`() {
        val ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

        val assessment = EpistemicEvaluationCapabilityBoundary.assess(
            validatedRuleset = ruleset,
            requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
        )

        assertEquals(EpistemicEvaluationAvailability.Ready, assessment)
    }

    @Test
    fun `unsupported script is deferred with explicit missing capabilities`() {
        val ruleset = catalog.ruleset(ClocktowerScript.NoGreaterJoy)

        val assessment = EpistemicEvaluationCapabilityBoundary.assess(
            validatedRuleset = ruleset,
            requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
        )

        assertTrue(assessment is EpistemicEvaluationAvailability.Deferred)
        val deferred = assessment as EpistemicEvaluationAvailability.Deferred
        assertEquals(
            EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            deferred.missingCapabilities,
        )
    }

    @Test
    fun `homebrew content reusing Trouble Brewing id is deferred`() {
        val builtIn = catalog.ruleset(ClocktowerScript.TroubleBrewing)
        val reusedIdHomebrew = builtIn.copy(
            script = builtIn.script.copy(source = ClocktowerScriptSource.IMPORTED_HOMEBREW),
        )

        val assessment = EpistemicEvaluationCapabilityBoundary.assess(
            validatedRuleset = reusedIdHomebrew,
            requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
        )

        assertTrue(assessment is EpistemicEvaluationAvailability.Deferred)
        val deferred = assessment as EpistemicEvaluationAvailability.Deferred
        assertEquals(
            EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            deferred.missingCapabilities,
        )
    }
}
