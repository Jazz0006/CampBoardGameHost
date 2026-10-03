package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingIntermediateSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset

internal data class DrunkAssignmentQ04V1ProductionSelection(
    val candidate: TroubleBrewingDrunkCandidate,
    val evaluation: DrunkAssignmentQ04V1Evaluation,
)

internal object DrunkAssignmentQ04V1ProductionAdapter {
    fun select(
        gameId: String,
        preset: TroubleBrewingSetupPreset,
        intermediateSetup: TroubleBrewingIntermediateSetup,
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

        require(preset.id == intermediateSetup.presetId) {
            "Q04 production baseline preset must match the seated intermediate setup."
        }
        require(preset.playerCount == intermediateSetup.playerCount) {
            "Q04 production baseline preset player count must match the seated setup."
        }
        require(DRUNK_ROLE_ID in preset.outsiders && intermediateSetup.visibleRoster.hasDrunk) {
            "Q04 production baseline requires a Drunk-bearing preset and seated visible roster."
        }
        val addedVisibleTownsfolk = (
            intermediateSetup.visibleRoster.townsfolkRoleIds.toSet() -
                preset.townsfolk.toSet()
            ).singleOrNull()
        requireNotNull(addedVisibleTownsfolk) {
            "Q04 production baseline requires exactly one post-preset visible Townsfolk."
        }
        require(addedVisibleTownsfolk in preset.drunkAsOptions) {
            "Q04 production baseline Townsfolk must come from the preset Drunk options."
        }
        val compatibilityRef = requireNotNull(
            shadow.decisionContext.legalCandidates.singleOrNull { candidate ->
                candidate.shownRoleId == addedVisibleTownsfolk
            },
        ) {
            "Q04 production baseline must resolve to the snapshot-backed legal domain."
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

    private const val DRUNK_ROLE_ID = "drunk"
}
