package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactTruthDangerSourceProjectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-3d2-truth-danger-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)

    @Test
    fun `rule determined Chef truth projects exact descriptive reduction on bounded world set`() {
        val game = snapshot.gameState
        val sourceSeat = 1
        val sourceAbility = RoleId("Chef")
        val value = FirstNightNumericInformationSemantics
            .healthyTruthValues(game, sourceSeat)
            .single()
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = sourceAbility,
                value = InformationValue.Number(value),
            ),
            roleDefinitions = roles,
        )
        val context = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(emptyList()),
            perceivedRolesBySeat = game.players.associate {
                it.seat to (it.shownRole ?: it.actualRole)
            },
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )
        val source = ConfirmationChannelRef.Source(sourceSeat, sourceAbility)

        val evaluation = ExactTruthDangerSourceProjector.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            claims = setOf(
                TruthDangerExactSourceClaim(
                    source = source,
                    proposition = proposition,
                ),
            ),
        )

        assertTrue(evaluation is ExactTruthDangerSourceProjection.Ready)
        val impact = (evaluation as ExactTruthDangerSourceProjection.Ready).impacts.single()
        assertEquals(source, impact.source)
        assertTrue(impact.exactWorldReduction >= BigInteger.ZERO)
        assertTrue(impact.independentlyConstraining)
        assertTrue(
            impact.exactWorldReduction > BigInteger.ZERO ||
                impact.strategicWorldKeysRemoved.isNotEmpty() ||
                impact.demonSeatsRemoved.isNotEmpty(),
        )
    }
}