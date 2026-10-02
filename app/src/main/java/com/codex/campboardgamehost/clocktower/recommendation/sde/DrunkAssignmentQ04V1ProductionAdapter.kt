package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup

internal data class DrunkAssignmentQ04V1ProductionSelection(
    val candidate: TroubleBrewingDrunkCandidate,
    val evaluation: DrunkAssignmentQ04V1Evaluation,
)

internal object DrunkAssignmentQ04V1ProductionAdapter {
    fun select(
        gameId: String,
        intermediateSetup: TroubleBrewingIntermediateSetup,
        compatibilityCandidate: TroubleBrewingDrunkCandidate,
        characterRegistry: ClocktowerCharacterRegistry,
        roleDefinitions: List<RoleDefinition>,
        sourceRevision: InformationDecisionRevision,
    ): DrunkAssignmentQ04V1ProductionSelection {
        val shadow = DrunkSetupShadowAdapter.evaluate(
            gameId = gameId,
            intermediateSetup = intermediateSetup,
            characterRegistry = characterRegistry,
            roleDefinitions = roleDefinitions,
            sourceRevision = sourceRevision,
        )

        val compatibilityRef = requireNotNull(
            shadow.decisionContext.legalCandidates.singleOrNull { candidate ->
                candidate.seat == compatibilityCandidate.seat &&
                    candidate.shownRoleId == compatibilityCandidate.shownRoleId
            },
        ) {
            "Q04 production compatibility candidate must resolve to the snapshot-backed legal domain."
        }

        val evaluation = DrunkAssignmentQ04V1Policy.evaluate(
            DrunkAssignmentQ04V1Request(
                decisionContext = shadow.decisionContext,
                featureEvaluation = shadow.drunkAssignmentFeatureEvaluation,
                compatibilityCandidate = compatibilityRef,
            ),
        )

        val selected = requireNotNull(
            shadow.candidates.singleOrNull { candidate ->
                candidate.candidate.seat == evaluation.selectedCandidate.seat &&
                    candidate.candidate.shownRoleId == evaluation.selectedCandidate.shownRoleId
            },
        ) {
            "Q04 production selection must resolve to one current rules-legal Drunk candidate."
        }.candidate

        return DrunkAssignmentQ04V1ProductionSelection(
            candidate = selected,
            evaluation = evaluation,
        )
    }
}
