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
import org.junit.Test
import java.io.File

class DrunkAssignmentQ04V1PolicyTest {
    @Test
    fun `Q04 overrides Empath compatibility candidate with legal Monk when Empath is between Good players`() {
        val shadow = shadow(empathAdjacentToEvil = false, includeMonk = true)
        val empath = shadow.decisionContext.legalCandidates.single { it.shownRoleId == "empath" }

        val evaluation = DrunkAssignmentQ04V1Policy.evaluate(
            DrunkAssignmentQ04V1Request(
                decisionContext = shadow.decisionContext,
                featureEvaluation = shadow.drunkAssignmentFeatureEvaluation,
                compatibilityCandidate = empath,
            ),
        )

        assertEquals(PolicyVersions.DRUNK_ASSIGNMENT_Q04_V1, evaluation.policyVersion)
        assertEquals("monk", evaluation.selectedCandidate.shownRoleId)
        assertEquals(
            DrunkAssignmentQ04V1Disposition.Q04_MONK_OVERRIDE,
            evaluation.disposition,
        )
        assertEquals(
            DrunkAssignmentQ04V1Reason.PRESERVE_HEALTHY_EMPATH_INFORMATION,
            evaluation.reason,
        )
    }

    @Test
    fun `Q04 preserves non Empath compatibility candidate without ranking other legal candidates`() {
        val shadow = shadow(empathAdjacentToEvil = false, includeMonk = true)
        val chef = shadow.decisionContext.legalCandidates.single { it.shownRoleId == "chef" }

        val evaluation = evaluate(shadow, chef.shownRoleId)

        assertEquals(chef, evaluation.selectedCandidate)
        assertEquals(
            DrunkAssignmentQ04V1Disposition.COMPATIBILITY_FALLBACK,
            evaluation.disposition,
        )
        assertEquals(
            DrunkAssignmentQ04V1Reason.COMPATIBILITY_CANDIDATE_NOT_EMPATH,
            evaluation.reason,
        )
    }

    @Test
    fun `Q04 preserves Empath when Monk is not legal`() {
        val shadow = shadow(empathAdjacentToEvil = false, includeMonk = false)

        val evaluation = evaluate(shadow, "empath")

        assertEquals("empath", evaluation.selectedCandidate.shownRoleId)
        assertEquals(DrunkAssignmentQ04V1Reason.MONK_NOT_LEGAL, evaluation.reason)
    }

    @Test
    fun `Q04 preserves Empath when topology condition fails`() {
        val shadow = shadow(empathAdjacentToEvil = true, includeMonk = true)

        val evaluation = evaluate(shadow, "empath")

        assertEquals("empath", evaluation.selectedCandidate.shownRoleId)
        assertEquals(
            DrunkAssignmentQ04V1Reason.EMPATH_TOPOLOGY_CONDITION_NOT_MET,
            evaluation.reason,
        )
    }

    @Test
    fun `Q04 preserves Empath when required topology is unavailable`() {
        val shadow = shadow(empathAdjacentToEvil = false, includeMonk = true)
        val empathId = shadow.decisionContext.legalCandidateIds[
            shadow.decisionContext.legalCandidates.indexOfFirst { it.shownRoleId == "empath" }
        ]
        val unavailableFeatures = DrunkAssignmentFeatureEvaluation(
            candidates = shadow.drunkAssignmentFeatureEvaluation.candidates.map { candidate ->
                if (candidate.candidateId != empathId) {
                    candidate
                } else {
                    candidate.copy(
                        features = candidate.features.copy(
                            topology = FeatureProjection.Unavailable(
                                FeatureUnavailableReason.MISSING_CAPABILITY,
                            ),
                        ),
                    )
                }
            },
        )
        val empath = shadow.decisionContext.legalCandidates.single { it.shownRoleId == "empath" }

        val evaluation = DrunkAssignmentQ04V1Policy.evaluate(
            DrunkAssignmentQ04V1Request(
                decisionContext = shadow.decisionContext,
                featureEvaluation = unavailableFeatures,
                compatibilityCandidate = empath,
            ),
        )

        assertEquals(empath, evaluation.selectedCandidate)
        assertEquals(
            DrunkAssignmentQ04V1Reason.EMPATH_TOPOLOGY_UNAVAILABLE,
            evaluation.reason,
        )
    }

    @Test
    fun `production adapter maps Q04 selection back to the exact current legal candidate`() {
        val intermediate = intermediateSetup(empathAdjacentToEvil = false, includeMonk = true)
        val ruleset = canonicalRuleset()
        val compatibility = TroubleBrewingDrunkCandidateDomain
            .legalCandidates(intermediate)
            .single { it.shownRoleId == "empath" }

        val selection = DrunkAssignmentQ04V1ProductionAdapter.select(
            gameId = "q04-production-adapter-test",
            intermediateSetup = intermediate,
            compatibilityCandidate = compatibility,
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )

        assertEquals("monk", selection.candidate.shownRoleId)
        assertEquals(
            DrunkAssignmentQ04V1Disposition.Q04_MONK_OVERRIDE,
            selection.evaluation.disposition,
        )
        assertEquals(
            selection.evaluation.selectedCandidate.seat,
            selection.candidate.seat,
        )
    }

    private fun evaluate(
        shadow: DrunkSetupShadowEvaluation,
        compatibilityRoleId: String,
    ): DrunkAssignmentQ04V1Evaluation {
        val compatibility = shadow.decisionContext.legalCandidates.single {
            it.shownRoleId == compatibilityRoleId
        }
        return DrunkAssignmentQ04V1Policy.evaluate(
            DrunkAssignmentQ04V1Request(
                decisionContext = shadow.decisionContext,
                featureEvaluation = shadow.drunkAssignmentFeatureEvaluation,
                compatibilityCandidate = compatibility,
            ),
        )
    }

    private fun shadow(
        empathAdjacentToEvil: Boolean,
        includeMonk: Boolean,
    ): DrunkSetupShadowEvaluation {
        val ruleset = canonicalRuleset()
        return DrunkSetupShadowAdapter.evaluate(
            gameId = "q04-policy-test",
            intermediateSetup = intermediateSetup(
                empathAdjacentToEvil = empathAdjacentToEvil,
                includeMonk = includeMonk,
            ),
            characterRegistry = ruleset.characterRegistry,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            sourceRevision = InformationDecisionRevision(0L, 0L),
        )
    }

    private fun intermediateSetup(
        empathAdjacentToEvil: Boolean,
        includeMonk: Boolean,
    ): TroubleBrewingIntermediateSetup {
        val townsfolk = listOf(
            "empath",
            "chef",
            "investigator",
            "undertaker",
            if (includeMonk) "monk" else "mayor",
        )
        val assignments = if (empathAdjacentToEvil) {
            listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "empath"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "poisoner"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "investigator"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "imp"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "chef"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "undertaker"),
                TroubleBrewingShownSeatAssignment(7, "Seat 7", if (includeMonk) "monk" else "mayor"),
            )
        } else {
            listOf(
                TroubleBrewingShownSeatAssignment(1, "Seat 1", "empath"),
                TroubleBrewingShownSeatAssignment(2, "Seat 2", "chef"),
                TroubleBrewingShownSeatAssignment(3, "Seat 3", "investigator"),
                TroubleBrewingShownSeatAssignment(4, "Seat 4", "imp"),
                TroubleBrewingShownSeatAssignment(5, "Seat 5", "poisoner"),
                TroubleBrewingShownSeatAssignment(6, "Seat 6", "undertaker"),
                TroubleBrewingShownSeatAssignment(7, "Seat 7", if (includeMonk) "monk" else "mayor"),
            )
        }
        return TroubleBrewingIntermediateSetup(
            datasetId = "q04-policy-test",
            schemaVersion = 1,
            presetId = "q04-policy-test",
            playerCount = 7,
            gameSeed = 20_261_002L,
            visibleRoster = TroubleBrewingVisibleRoster(
                hasDrunk = true,
                townsfolkRoleIds = townsfolk,
                outsiderRoleIds = emptyList(),
                minionRoleIds = listOf("poisoner"),
                demonRoleIds = listOf("imp"),
            ),
            shownSeatAssignments = assignments,
        )
    }

    private fun canonicalRuleset() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
}
