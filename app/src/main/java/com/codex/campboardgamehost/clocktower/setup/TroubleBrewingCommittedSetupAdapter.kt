package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

/** Maps an already-materialized Trouble Brewing deal into the generic committed setup fact. */
internal object TroubleBrewingCommittedSetupAdapter {
    fun fromDealPlan(dealPlan: TroubleBrewingSetupDealPlan): CommittedClocktowerSetup {
        require(dealPlan.datasetId.isNotBlank()) { "Trouble Brewing committed setup dataset ID cannot be blank." }
        require(dealPlan.presetId.isNotBlank()) { "Trouble Brewing committed setup preset ID cannot be blank." }
        require(dealPlan.playerCount == dealPlan.assignments.size) {
            "Trouble Brewing committed setup assignment count must match player count."
        }

        return CommittedClocktowerSetup(
            script = ScriptId(TROUBLE_BREWING_SCRIPT_ID),
            setupSeed = dealPlan.gameSeed,
            assignments = dealPlan.assignments.map { assignment ->
                CommittedSetupSeat(
                    seat = assignment.seat,
                    actualRole = RoleId(assignment.actualRoleId),
                    shownRole = RoleId(assignment.shownRoleId),
                )
            },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.TEMPLATE,
                providerId = dealPlan.datasetId,
                candidateId = dealPlan.presetId,
            ),
        )
    }

    private const val TROUBLE_BREWING_SCRIPT_ID = "trouble_brewing"
}
