package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactHypotheticalWorldStructureDiagnosticsTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "fn-bundle-2-structure-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test
    fun `exact bundle diagnostics expose strategic world structure before and after`() {
        // ShownRoleAt is deliberately strict identity state. RoleAt would correctly allow
        // interaction-scoped Spy/Recluse registration and therefore cannot pin this fixture.
        val poisoner = observation(
            id = "shown-poisoner-at-4",
            sequence = 1,
            proposition = InformationProposition.ShownRoleAt(4, RoleId("Poisoner")),
        )
        val imp = observation(
            id = "shown-imp-at-5",
            sequence = 2,
            proposition = InformationProposition.ShownRoleAt(5, RoleId("Imp")),
        )

        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context(),
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "known-evil-team",
                    recipientSeat = 1,
                    observations = listOf(poisoner, imp),
                ),
            ),
        )

        assertTrue(evaluation is ExactHypotheticalObservationBundleEvaluation.Ready)
        val diagnostic = (evaluation as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics.single()

        assertTrue(diagnostic.beforeStructure.possibleDemonSeats.isNotEmpty())
        assertEquals(setOf(5), diagnostic.afterStructure.possibleDemonSeats)
        assertEquals(setOf(setOf(4, 5)), diagnostic.afterStructure.evilTeamSeatConfigurations)
        assertEquals(setOf(4, 5), diagnostic.afterStructure.forcedEvilSeats)
        assertEquals(setOf(1, 2, 3), diagnostic.afterStructure.forcedGoodSeats)
        assertEquals(setOf(4, 5), diagnostic.afterStructure.evilCoverSeats)
        assertEquals(1, diagnostic.afterStructure.demonCoverSize)
        assertEquals(2, diagnostic.afterStructure.evilCoverSize)
        assertEquals(1, diagnostic.afterStructure.distinctEvilTeamConfigurationCount)
    }

    @Test
    fun `unsat bundle reports empty structure rather than vacuous forced seats`() {
        val shownAtFour = InformationProposition.ShownRoleAt(4, RoleId("Poisoner"))
        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context(),
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "contradiction",
                    recipientSeat = 1,
                    observations = listOf(
                        observation("positive", 1, shownAtFour),
                        observation("negative", 2, InformationProposition.Not(shownAtFour)),
                    ),
                ),
            ),
        )

        assertTrue(evaluation is ExactHypotheticalObservationBundleEvaluation.Ready)
        val structure = (evaluation as ExactHypotheticalObservationBundleEvaluation.Ready)
            .diagnostics.single().afterStructure
        assertTrue(structure.possibleDemonSeats.isEmpty())
        assertTrue(structure.evilTeamSeatConfigurations.isEmpty())
        assertTrue(structure.forcedGoodSeats.isEmpty())
        assertTrue(structure.forcedEvilSeats.isEmpty())
        assertTrue(structure.evilCoverSeats.isEmpty())
    }

    private fun context() = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = ActionFactTimeline(emptyList()),
        perceivedRolesBySeat = perceived,
        observationLog = EpistemicObservationLog(),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun observation(
        id: String,
        sequence: Int,
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )
}
