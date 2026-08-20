package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Production-facing other-night ordering seam for the staged R5.5 cutover.
 *
 * Rules outcomes are supplied as already-resolved facts; this helper does not derive deaths,
 * poison, protection, execution, succession, or storyteller choices. The Scarlet-Woman-to-Demon
 * identity notification is still rendered by a separate legacy production screen, so that fact is
 * rejected until the screen itself is integrated into the exact-match interaction list.
 */
internal object ClocktowerProductionOtherNightFlow {
    private val planner = ClocktowerFlowPlanner()
    private val projector = ClocktowerHostInteractionProjector()

    fun <T> order(
        ruleset: ValidatedClocktowerRuleset,
        playerCount: Int,
        wakingRoleIds: Set<RoleId>,
        resolvedFacts: ClocktowerResolvedFlowFacts,
        productionSteps: List<T>,
        identityOf: (T) -> ClocktowerProductionNightStepIdentity,
    ): List<T> {
        require(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON !in resolvedFacts) {
            "New-Demon identity remains a separate production screen and is not yet planner-ordered."
        }

        val phase = ClocktowerNightFlowPhase.OTHER_NIGHT
        val basePlan = planner.planNight(
            ruleset = ruleset,
            phase = phase,
            context = ClocktowerFlowContext(
                playerCount = playerCount,
                inPlayRoleIds = wakingRoleIds,
            ),
        )
        val interactions = projector.projectNight(
            phase = phase,
            basePlan = basePlan,
            resolvedFacts = resolvedFacts,
        )
        return ClocktowerProductionInteractionOrderer.order(
            phase = phase,
            projectedInteractions = interactions,
            productionSteps = productionSteps,
            identityOf = identityOf,
        )
    }
}
