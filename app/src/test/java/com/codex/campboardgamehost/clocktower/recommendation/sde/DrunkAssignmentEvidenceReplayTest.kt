package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateDomain
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingShownSeatAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingVisibleRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * C1D cross-project replay regression derived from ClocktowerEvidenceLab C1C-G10 Game 2.
 *
 * The historical evidence supplies only the pre-assignment shown-role layout, observed Storyteller
 * choice, rationale and provenance. Host rules remain the sole owner of legal Drunk candidacy and
 * this test deliberately does not convert the observed expert choice into a policy verdict.
 */
class DrunkAssignmentEvidenceReplayTest {
    @Test
    fun `G10 historical Empath choice maps into Host legal domain while frozen V1 stays deferred`() {
        val ruleset = canonicalRuleset()
        val intermediate = g10Game2IntermediateSetup()

        val legalCandidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)

        assertEquals(
            listOf(1, 3, 4, 6, 7, 8),
            legalCandidates.map { it.seat },
        )
        assertEquals(
            listOf("empath", "undertaker", "librarian", "monk", "mayor", "virgin"),
            legalCandidates.map { it.shownRoleId },
        )

        val observedHistoricalChoice = legalCandidates.single { candidate ->
            candidate.seat == 1 && candidate.shownRoleId == "empath"
        }

        val shadow = DrunkSetupShadowAdapter.evaluate(
            gameId = "evidence:c1d:g10-game2",
            intermediateSetup = intermediate,
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            sourceRevision = InformationDecisionRevision(
                gameStateRevision = 0L,
                playerInputRevision = 0L,
            ),
        )

        val observedCandidateId = "setup:drunk-seat:seat-${observedHistoricalChoice.seat}"
        assertTrue(observedCandidateId in shadow.decisionTrace.legalCandidateIds)
        assertEquals(
            legalCandidates.map { "setup:drunk-seat:seat-${it.seat}" },
            shadow.decisionTrace.legalCandidateIds,
        )

        val observedProjection = shadow.candidates.single {
            it.candidate == observedHistoricalChoice
        }.hypotheticalSetup.gameState
        val drunk = requireNotNull(ruleset.characterRegistry.findByExternalId("drunk"))
        val empath = requireNotNull(ruleset.characterRegistry.findByExternalId("empath"))
        val imp = requireNotNull(ruleset.characterRegistry.findByExternalId("imp"))

        assertEquals(drunk.id, requireNotNull(observedProjection.playerAt(1)).actualRole)
        assertEquals(empath.id, requireNotNull(observedProjection.playerAt(1)).shownRole)
        assertEquals(
            imp.id,
            requireNotNull(observedProjection.playerAt(2)).actualRole,
        )

        assertEquals(DecisionTraceActualChoice.Pending, shadow.decisionTrace.actualChoice)
        assertTrue(shadow.policyEvaluation is BeginnerConservativePolicyEvaluation.Deferred)
        assertNull(shadow.policySelection)
    }

    private fun g10Game2IntermediateSetup(): TroubleBrewingIntermediateSetup =
        TroubleBrewingIntermediateSetup(
            datasetId = "evidence-c1d-g10",
            schemaVersion = 1,
            presetId = "historical-replay-only:g10-game2",
            playerCount = 9,
            // Fixture-only deterministic seed. The historical source does not evidence a game seed,
            // and DLB-3A must remain deferred so this value cannot create a policy selection.
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

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
}
