package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorKind
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleDeferredComplexity
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightBundleCandidateSpaceAuditor
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingShownSeatAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingVisibleRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DrunkAssignmentFeatureSurfaceTest {
    @Test
    fun `G10 Empath projects evidence-qualified topology and first-night opportunity while longitudinal remains unavailable`() {
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

        val features = DrunkAssignmentFeaturesProjector.project(envelope)

        val topology = (features.topology as FeatureProjection.Projected<DrunkAssignmentTopologyFeatures>).value
        assertEquals(9, topology.previousSeat)
        assertEquals(2, topology.nextSeat)
        assertEquals(setOf(2), topology.adjacentEvilSeats)
        assertEquals(setOf(2), topology.adjacentDemonSeats)
        assertTrue(topology.adjacentMinionSeats.isEmpty())

        val firstNight =
            (features.firstNightInformationOpportunity as FeatureProjection.Projected<DrunkAssignmentFirstNightInformationOpportunityFeatures>).value
        val factor = firstNight.factors.single()
        assertEquals("numeric.empath.seat-1", factor.factorId)
        assertEquals(
            FirstNightBundleCandidateFactorKind.FIXED_NUMERIC_INFORMATION,
            factor.kind,
        )
        assertTrue(factor.hasMultipleLegalOutputs)
        assertTrue(firstNight.hasStorytellerControlledRoute)
        assertTrue(firstNight.hasMultiOutputStorytellerControlledRoute)

        val longitudinal =
            features.longitudinalNarrativeOpportunity as FeatureProjection.Unavailable
        assertEquals(FeatureUnavailableReason.MISSING_CAPABILITY, longitudinal.reason)

        assertTrue(
            FirstNightBundleDeferredComplexity.SPY_RECLUSE_REGISTRATION in
                features.limitations.deferredComplexities,
        )
        assertTrue(features.limitations.excludedPlayerControlledElements.isEmpty())
    }

    @Test
    fun `known absence of candidate first-night route is projected empty rather than unavailable`() {
        val ruleset = canonicalRuleset()
        val setup = g10Game2IntermediateSetup()
        val candidate = TroubleBrewingDrunkCandidateDomain.legalCandidates(setup)
            .single { it.shownRoleId == "monk" }
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

        val features = DrunkAssignmentFeaturesProjector.project(envelope)

        val firstNight =
            (features.firstNightInformationOpportunity as FeatureProjection.Projected<DrunkAssignmentFirstNightInformationOpportunityFeatures>).value
        assertTrue(firstNight.factors.isEmpty())
        assertTrue(!firstNight.hasStorytellerControlledRoute)
        assertTrue(!firstNight.hasMultiOutputStorytellerControlledRoute)

        val longitudinal =
            features.longitudinalNarrativeOpportunity as FeatureProjection.Unavailable
        assertEquals(FeatureUnavailableReason.MISSING_CAPABILITY, longitudinal.reason)
    }

    @Test
    fun `shadow exposes dedicated feature evaluation in legal-candidate order`() {
        val ruleset = canonicalRuleset()
        val shadow = DrunkSetupShadowAdapter.evaluate(
            gameId = "dlb3b2-feature-surface",
            intermediateSetup = g10Game2IntermediateSetup(),
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = roleDefinitions(),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )

        val dedicated = shadow.drunkAssignmentFeatureEvaluation
        assertEquals(
            shadow.sdeCandidates.map(SdeDecisionCandidate::candidateId),
            dedicated.candidateIds,
        )

        val empath = dedicated.candidates.single {
            it.candidateId == "setup:drunk-seat:seat-1"
        }
        val empathTopology =
            (empath.features.topology as FeatureProjection.Projected<DrunkAssignmentTopologyFeatures>).value
        assertEquals(setOf(2), empathTopology.adjacentDemonSeats)

        val monk = dedicated.candidates.single {
            it.candidateId == "setup:drunk-seat:seat-6"
        }
        val monkFirstNight =
            (monk.features.firstNightInformationOpportunity as FeatureProjection.Projected<DrunkAssignmentFirstNightInformationOpportunityFeatures>).value
        assertTrue(monkFirstNight.factors.isEmpty())

        val ordinary = shadow.featureEvaluation as DecisionFeatureEvaluation.Ready
        ordinary.candidates.forEach { candidate ->
            assertEquals(
                FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                candidate.features.strategic,
            )
        }

    }

    private fun g10Game2IntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "dlb3b2-g10",
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

    private fun roleDefinitions() =
        clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing)

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
}
