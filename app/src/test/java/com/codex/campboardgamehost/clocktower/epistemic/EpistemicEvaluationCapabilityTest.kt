package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpistemicEvaluationCapabilityTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `Trouble Brewing is ready for exact historical hypothetical evaluation`() {
        val rulesetRef = catalog.ruleset(ClocktowerScript.TroubleBrewing).toRulesetRef(
            rulesetVersion = "epi-mq-0-5-capability-test",
            sourceRevision = "official",
        )

        val assessment = EpistemicEvaluationCapabilityBoundary.assess(
            rulesetRef = rulesetRef,
            requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
        )

        assertEquals(EpistemicEvaluationAvailability.Ready, assessment)
    }

    @Test
    fun `unsupported script is deferred with explicit missing capabilities`() {
        val rulesetRef = catalog.ruleset(ClocktowerScript.NoGreaterJoy).toRulesetRef(
            rulesetVersion = "epi-mq-0-5-capability-test",
            sourceRevision = "official",
        )

        val assessment = EpistemicEvaluationCapabilityBoundary.assess(
            rulesetRef = rulesetRef,
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
