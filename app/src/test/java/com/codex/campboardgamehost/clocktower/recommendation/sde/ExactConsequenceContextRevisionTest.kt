package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class ExactConsequenceContextRevisionTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-1e-current-revision-test",
        sourceRevision = "official",
    )

    @Test
    fun `current decision revision is independent of historical baseline revision`() {
        val baseline = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val sourceRevision = InformationDecisionRevision(
            gameStateRevision = baseline.gameStateRevision + 4,
            playerInputRevision = baseline.playerInputRevision + 2,
        )
        val perceivedRoles = baseline.gameState.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val context = ExactConsequenceContext(
            validatedRuleset = validatedRuleset,
            exactContext = ExactHistoricalHypotheticalContext(
                initialSnapshot = baseline,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = ActionFactTimeline(emptyList()),
                perceivedRolesBySeat = perceivedRoles,
                observationLog = EpistemicObservationLog(),
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            ),
            sourceRevision = sourceRevision,
        )

        assertEquals(sourceRevision.gameStateRevision, context.gameStateRevision)
        assertEquals(sourceRevision.playerInputRevision, context.playerInputRevision)
        assertEquals(baseline.gameStateRevision, context.exactContext.initialSnapshot.gameStateRevision)
        assertEquals(baseline.playerInputRevision, context.exactContext.initialSnapshot.playerInputRevision)
    }
}
