package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.flow.ClocktowerFlowContext
import com.codex.campboardgamehost.clocktower.flow.ClocktowerFlowPlanner
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase

/**
 * Knowledge-neutral canonical night schedule for one exact possible world.
 *
 * Actual roles remain the mechanical identity. Shown roles are added only to the waking-role set so
 * a possible Drunk recipient still reaches the shown character's information slot. Triggered/event
 * interactions and Storyteller-resolved facts are intentionally not projected here.
 */
internal object EnumeratedWorldNightSchedule {
    private val planner = ClocktowerFlowPlanner()

    fun plan(
        ruleset: ValidatedClocktowerRuleset,
        phase: ClocktowerNightFlowPhase,
        world: EnumeratedWorld,
    ): List<NightOrderToken> {
        val wakingRoleIds = (world.rolesBySeat.values + world.shownRolesBySeat.values).toSet()
        return planner.planNight(
            ruleset = ruleset,
            phase = phase,
            context = ClocktowerFlowContext(
                playerCount = world.rolesBySeat.size,
                inPlayRoleIds = wakingRoleIds,
            ),
        )
    }
}
