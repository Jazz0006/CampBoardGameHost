package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingShownSeatAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingVisibleRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DrunkSetupShadowAdapterTest {
    @Test
    fun `setup shadow preserves legal domain and records candidate-specific ecology without selecting`() {
        val ruleset = canonicalRuleset()
        val revision = InformationDecisionRevision(
            gameStateRevision = 0L,
            playerInputRevision = 0L,
        )
        val intermediate = drunkIntermediateSetup()

        val shadow = DrunkSetupShadowAdapter.evaluate(
            gameId = "game-dlb3-a",
            intermediateSetup = intermediate,
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            sourceRevision = revision,
        )

        assertEquals(
            listOf(
                "setup:drunk-seat:seat-1",
                "setup:drunk-seat:seat-3",
                "setup:drunk-seat:seat-5",
            ),
            shadow.sdeCandidates.map(SdeDecisionCandidate::candidateId),
        )
        assertEquals(
            shadow.sdeCandidates.map(SdeDecisionCandidate::candidateId),
            shadow.decisionContext.legalCandidateIds,
        )
        assertEquals("botc.tb.game-snapshot", shadow.decisionContext.snapshot.schemaId)
        assertEquals("game-dlb3-a", shadow.decisionContext.snapshot.gameId)
        assertEquals(
            listOf(1, 3, 5),
            shadow.candidates.map { it.candidate.seat },
        )

        shadow.candidates.forEach { candidate ->
            assertEquals(
                SdeDecisionLifecycleStage.SetupPrecommit,
                candidate.sdeCandidate.lifecycleStage,
            )
            assertEquals(revision, candidate.sdeCandidate.sourceRevision)
            assertEquals(
                SdeCommittedDecisionInputKind.DRUNK_SEAT,
                candidate.proposedCommitRef.kind,
            )
            assertEquals(
                "TroubleBrewingDrunkCandidateDomain",
                candidate.sdeCandidate.legalityProvenance.ownerId,
            )
            assertEquals(
                SdeHistoricalPrefixRef.Global(
                    gameId = "game-dlb3-a",
                    actionRefs = emptyList(),
                    observationRefs = emptyList(),
                ),
                candidate.sdeCandidate.historyPrefixRef,
            )

            val bindings = candidate.sdeCandidate.inputBindings as SdeDecisionInputBindings.Captured
            assertTrue(candidate.proposedCommitRef !in bindings.committedInputRefs)
            assertEquals(
                candidate.candidate.seat,
                candidate.hypotheticalSetup.candidate.seat,
            )
            assertTrue(candidate.ecologyAudit.factors.isNotEmpty())
        }

        val chefDrunk = shadow.candidates.single { it.candidate.shownRoleId == "chef" }
        val washerwomanDrunk = shadow.candidates.single { it.candidate.shownRoleId == "washerwoman" }
        val chefFactorWhenChefDrunk = chefDrunk.ecologyAudit.factors
            .single { it.factorId == "numeric.chef.seat-1" }
        val chefFactorWhenWasherwomanDrunk = washerwomanDrunk.ecologyAudit.factors
            .single { it.factorId == "numeric.chef.seat-1" }
        assertEquals(FirstNightBundleEntryControl.STORYTELLER_CONTROLLED, chefFactorWhenChefDrunk.control)
        assertEquals(FirstNightBundleEntryControl.RULE_DETERMINED, chefFactorWhenWasherwomanDrunk.control)

        val featureEvaluation = shadow.featureEvaluation as DecisionFeatureEvaluation.Ready
        featureEvaluation.candidates.forEach { candidate ->
            assertEquals(
                FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                candidate.features.strategic,
            )
        }

    }

    @Test
    fun `shadow evaluation is pure and non drunk setup has no SDE decision surface`() {
        val ruleset = canonicalRuleset()
        val nonDrunk = nonDrunkIntermediateSetup()
        val before = nonDrunk.copy(
            visibleRoster = nonDrunk.visibleRoster.copy(),
            shownSeatAssignments = nonDrunk.shownSeatAssignments.map { it.copy() },
        )

        val result = DrunkSetupShadowAdapter.evaluateIfNeeded(
            gameId = "game-no-drunk",
            intermediateSetup = nonDrunk,
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )

        assertNull(result)
        assertEquals(before, nonDrunk)
    }

    private fun drunkIntermediateSetup(): TroubleBrewingIntermediateSetup {
        val visibleRoster = TroubleBrewingVisibleRoster(
            hasDrunk = true,
            townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
            outsiderRoleIds = listOf("saint"),
            minionRoleIds = listOf("poisoner"),
            demonRoleIds = listOf("imp"),
        )
        return TroubleBrewingIntermediateSetup(
            datasetId = "dlb3-test",
            schemaVersion = 1,
            presetId = "dlb3-drunk-six",
            playerCount = 6,
            gameSeed = 9_301L,
            visibleRoster = visibleRoster,
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Player 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Player 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Player 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Player 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Player 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Player 6", "imp"),
            ),
        )
    }

    private fun nonDrunkIntermediateSetup(): TroubleBrewingIntermediateSetup {
        val visibleRoster = TroubleBrewingVisibleRoster(
            hasDrunk = false,
            townsfolkRoleIds = listOf("chef", "empath", "washerwoman"),
            outsiderRoleIds = listOf("saint"),
            minionRoleIds = listOf("poisoner"),
            demonRoleIds = listOf("imp"),
        )
        return TroubleBrewingIntermediateSetup(
            datasetId = "dlb3-test",
            schemaVersion = 1,
            presetId = "dlb3-no-drunk-six",
            playerCount = 6,
            gameSeed = 9_302L,
            visibleRoster = visibleRoster,
            shownSeatAssignments = listOf(
                TroubleBrewingShownSeatAssignment(1, "Player 1", "chef"),
                TroubleBrewingShownSeatAssignment(2, "Player 2", "saint"),
                TroubleBrewingShownSeatAssignment(3, "Player 3", "empath"),
                TroubleBrewingShownSeatAssignment(4, "Player 4", "poisoner"),
                TroubleBrewingShownSeatAssignment(5, "Player 5", "washerwoman"),
                TroubleBrewingShownSeatAssignment(6, "Player 6", "imp"),
            ),
        )
    }

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
}
