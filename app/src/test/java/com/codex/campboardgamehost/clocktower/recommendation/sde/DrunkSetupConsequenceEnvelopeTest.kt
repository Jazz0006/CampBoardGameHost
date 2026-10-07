package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleDeferredComplexity
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightBundleCandidateSpaceAuditor
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingShownSeatAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingVisibleRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DrunkSetupConsequenceEnvelopeTest {
    @Test
    fun `G10 Empath envelope preserves topology and first-night impaired-information opportunity without ranking`() {
        val ruleset = canonicalRuleset()
        val setup = g10Game2IntermediateSetup()
        val candidate = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup)
            .single { it.seat == 1 && it.shownRoleId == "empath" }
        val hypothetical = TroubleBrewingDrunkHypotheticalProjector.project(
            intermediateSetup = setup,
            candidate = candidate,
            characterRegistry = ruleset.characterRegistry,
        )
        val ecology = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(
            game = hypothetical.gameState,
            roleDefinitions = roleDefinitions(),
        )

        val envelope = DrunkSetupConsequenceProjector.project(
            candidate = candidate,
            hypotheticalSetup = hypothetical,
            ecologyAudit = ecology,
        )

        assertEquals(1, envelope.candidateSeat)
        assertEquals("empath", envelope.shownRoleId)
        assertEquals(9, envelope.topology.previousSeat)
        assertEquals(2, envelope.topology.nextSeat)
        assertEquals(setOf(2), envelope.topology.adjacentEvilSeats)
        assertEquals(setOf(2), envelope.topology.adjacentDemonSeats)
        assertTrue(envelope.topology.adjacentMinionSeats.isEmpty())

        val factor = envelope.firstNightInformation.factors.single()
        assertEquals("numeric.empath.seat-1", factor.factorId)
        assertEquals(FirstNightBundleEntryControl.STORYTELLER_CONTROLLED, factor.control)
        assertTrue(factor.hasMultipleLegalOutputs)
        assertTrue(factor.optionCount > 1)

        assertTrue(
            FirstNightBundleDeferredComplexity.SPY_RECLUSE_REGISTRATION in
                envelope.deferredComplexities,
        )
        assertTrue(envelope.excludedPlayerControlledElements.isEmpty())
    }

    @Test
    fun `Chef envelope exposes misinformation route while non-information Townsfolk remains valid with no own Night-1 factor`() {
        val ruleset = canonicalRuleset()
        val chefSetup = chefIntermediateSetup()
        val chef = TroubleBrewingDrunkCandidateDomain.legalCandidates(chefSetup)
            .single { it.shownRoleId == "chef" }
        val chefHypothetical = TroubleBrewingDrunkHypotheticalProjector.project(
            intermediateSetup = chefSetup,
            candidate = chef,
            characterRegistry = ruleset.characterRegistry,
        )
        val chefEcology = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(
            game = chefHypothetical.gameState,
            roleDefinitions = roleDefinitions(),
        )

        val chefEnvelope = DrunkSetupConsequenceProjector.project(
            candidate = chef,
            hypotheticalSetup = chefHypothetical,
            ecologyAudit = chefEcology,
        )

        val chefFactor = chefEnvelope.firstNightInformation.factors.single()
        assertEquals("numeric.chef.seat-1", chefFactor.factorId)
        assertEquals(FirstNightBundleEntryControl.STORYTELLER_CONTROLLED, chefFactor.control)
        assertTrue(chefFactor.hasMultipleLegalOutputs)
        assertTrue(
            FirstNightBundleDeferredComplexity.POISONER_TARGET in
                chefEnvelope.deferredComplexities,
        )

        val g10 = g10Game2IntermediateSetup()
        val monk = TroubleBrewingDrunkCandidateDomain.legalCandidates(g10)
            .single { it.shownRoleId == "monk" }
        val monkHypothetical = TroubleBrewingDrunkHypotheticalProjector.project(
            intermediateSetup = g10,
            candidate = monk,
            characterRegistry = ruleset.characterRegistry,
        )
        val monkEcology = TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(
            game = monkHypothetical.gameState,
            roleDefinitions = roleDefinitions(),
        )

        val monkEnvelope = DrunkSetupConsequenceProjector.project(
            candidate = monk,
            hypotheticalSetup = monkHypothetical,
            ecologyAudit = monkEcology,
        )

        assertTrue(monkEnvelope.firstNightInformation.factors.isEmpty())
        assertFalse(monkEnvelope.firstNightInformation.hasStorytellerControlledRoute)
    }

    @Test
    fun `shadow attaches envelopes and neutral features in legal-candidate order`() {
        val ruleset = canonicalRuleset()
        val shadow = DrunkSetupShadowAdapter.evaluate(
            gameId = "dlb3b-envelope",
            intermediateSetup = g10Game2IntermediateSetup(),
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = roleDefinitions(),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )

        assertEquals(
            shadow.candidates.map { it.candidate.seat },
            shadow.candidates.map { it.consequenceEnvelope.candidateSeat },
        )

        val empath = shadow.candidates.single { it.candidate.shownRoleId == "empath" }
        assertEquals(setOf(2), empath.consequenceEnvelope.topology.adjacentDemonSeats)

        val featureEvaluation = shadow.featureEvaluation as DecisionFeatureEvaluation.Ready
        featureEvaluation.candidates.forEach { candidate ->
            assertEquals(
                FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                candidate.features.strategic,
            )
        }
    }

    private fun g10Game2IntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "dlb3b-g10",
            schemaVersion = 1,
            presetId = "historical-replay-only:g10-game2",
            playerCount = 9,
            gameSeed = 20_260_929L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf(
                    "empath",
                    "undertaker",
                    "librarian",
                    "monk",
                    "mayor",
                    "virgin",
                ),
                outsiderRoleIds = listOf("butler"),
                minionRoleIds = listOf("spy"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "empath"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "imp"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "undertaker"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "librarian"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "spy"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "monk"),
                TroubleBrewingShownSeatAssignment(7, "Seat 7", "mayor"),
                TroubleBrewingShownSeatAssignment(8, "Seat 8", "virgin"),
                TroubleBrewingShownSeatAssignment(9, "Seat 9", "butler"),
            ),
        )

    private fun chefIntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "dlb3b-chef",
            schemaVersion = 1,
            presetId = "dlb3b-chef-route",
            playerCount = 6,
            gameSeed = 20_260_930L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
                outsiderRoleIds = listOf("saint"),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "imp"),
            ),
        )

    private fun roleDefinitions() =
        clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing)

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
}
