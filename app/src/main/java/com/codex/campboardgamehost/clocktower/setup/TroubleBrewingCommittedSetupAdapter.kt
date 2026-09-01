package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

/** Maps an already-materialized Trouble Brewing deal into the generic committed setup fact. */
internal object TroubleBrewingCommittedSetupAdapter {
    fun fromDealPlan(
        dealPlan: TroubleBrewingSetupDealPlan,
        resolvedAssignments: List<TroubleBrewingResolvedDealAssignment>,
    ): CommittedClocktowerSetup {
        require(dealPlan.datasetId.isNotBlank()) { "Trouble Brewing committed setup dataset ID cannot be blank." }
        require(dealPlan.presetId.isNotBlank()) { "Trouble Brewing committed setup preset ID cannot be blank." }
        require(dealPlan.playerCount == dealPlan.assignments.size) {
            "Trouble Brewing committed setup assignment count must match player count."
        }
        require(resolvedAssignments.size == dealPlan.assignments.size) {
            "Resolved Trouble Brewing assignments must match the committed deal size."
        }
        require(resolvedAssignments.map { it.seat }.distinct().size == resolvedAssignments.size) {
            "Resolved Trouble Brewing committed seats must be unique."
        }

        val resolvedBySeat = resolvedAssignments.associateBy { it.seat }
        return CommittedClocktowerSetup(
            script = ScriptId(TROUBLE_BREWING_SCRIPT_ID),
            setupSeed = dealPlan.gameSeed,
            assignments = dealPlan.assignments.map { assignment ->
                val resolved = requireNotNull(resolvedBySeat[assignment.seat]) {
                    "Resolved Trouble Brewing assignment is missing seat ${assignment.seat}."
                }
                require(resolved.playerName == assignment.playerName) {
                    "Resolved Trouble Brewing player identity must match the committed deal."
                }
                CommittedSetupSeat(
                    seat = assignment.seat,
                    actualRole = RoleId(resolved.actualRole.enName),
                    shownRole = RoleId(resolved.shownRole.enName),
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
