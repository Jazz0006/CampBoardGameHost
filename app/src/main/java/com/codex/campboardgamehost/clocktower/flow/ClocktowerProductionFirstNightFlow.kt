package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Production-facing first-night ordering seam for the R5.5 planner cutover.
 *
 * This helper deliberately owns no UI behavior and no event resolution. It only feeds the current
 * first-night table composition into the canonical planner/projector and exact-matches the existing
 * production UI steps through stable interaction identities.
 */
internal object ClocktowerProductionFirstNightFlow {
    private val planner = ClocktowerFlowPlanner()
    private val projector = ClocktowerHostInteractionProjector()

    fun interactions(
        ruleset: ValidatedClocktowerRuleset,
        playerCount: Int,
        inPlayRoleIds: Set<RoleId>,
        actualRoleIds: Set<RoleId> = inPlayRoleIds,
    ): List<ClocktowerHostInteraction> {
        val phase = ClocktowerNightFlowPhase.FIRST_NIGHT
        val basePlan = planner.planNight(
            ruleset = ruleset,
            phase = phase,
            context = ClocktowerFlowContext(
                playerCount = playerCount,
                inPlayRoleIds = inPlayRoleIds,
            ),
        )
        return projector.projectNight(
            phase = phase,
            basePlan = basePlan,
            actualRoleIds = actualRoleIds,
        )
    }

    fun <T> order(
        ruleset: ValidatedClocktowerRuleset,
        playerCount: Int,
        inPlayRoleIds: Set<RoleId>,
        actualRoleIds: Set<RoleId> = inPlayRoleIds,
        productionSteps: List<T>,
        identityOf: (T) -> ClocktowerProductionNightStepIdentity,
    ): List<T> {
        val phase = ClocktowerNightFlowPhase.FIRST_NIGHT
        val projectedInteractions = interactions(
            ruleset = ruleset,
            playerCount = playerCount,
            inPlayRoleIds = inPlayRoleIds,
            actualRoleIds = actualRoleIds,
        )
        return ClocktowerProductionInteractionOrderer.order(
            phase = phase,
            projectedInteractions = projectedInteractions,
            productionSteps = productionSteps,
            identityOf = identityOf,
        )
    }
}
