package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Production-facing other-night ordering seam for the staged R5.5 cutover.
 *
 * Rules outcomes are supplied as already-resolved facts; this helper does not derive deaths,
 * poison, protection, execution, succession, or storyteller choices. Production steps must expose
 * the same stable interaction identities emitted by the planner projection.
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
