package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EnumeratedHistoricalExactScriptSupportTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `H4 historical exact baseline explicitly rejects structurally supported non Trouble Brewing script`() {
        val validatedRuleset = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "a3-h4-script-support-test",
            sourceRevision = "official",
        )
        val setupKnowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-a3-h4-ngj",
            formalSnapshotId = "snapshot-a3-h4-ngj",
            recipientSeat = 1,
            perceivedRole = RoleId("Empath"),
            setupKnowledge = listOf(InformationProposition.SetupProfile(3, 0, 1, 1)),
        )

        try {
            EnumeratedHistoricalExactBaseline.build(
                validatedRuleset = validatedRuleset,
                rulesetRef = rulesetRef,
                setupKnowledge = setupKnowledge,
                hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
                roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.NoGreaterJoy),
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = ActionFactTimeline(),
                observationLog = EpistemicObservationLog(),
            )
            fail("Expected historical exact reasoning to fail closed for unsupported No Greater Joy.")
        } catch (expected: IllegalArgumentException) {
            val message = expected.message.orEmpty()
            assertTrue(message.contains("Trouble Brewing", ignoreCase = true))
            assertTrue(message.contains("exact", ignoreCase = true))
            assertTrue(message.contains("support", ignoreCase = true))
        }
    }
}
