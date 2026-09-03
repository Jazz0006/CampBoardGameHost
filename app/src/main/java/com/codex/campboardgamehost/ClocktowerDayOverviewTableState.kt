package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Day Overview presentation boundary for the persistent Storyteller table.
 *
 * Physical identity is projected from the durable domain seat authority. Day Overview is read-only;
 * later R4D slices may add bounded seat interactions without changing this physical topology.
 */
internal data class ClocktowerDayOverviewTableState(
    val seats: List<HostSeatPresentation>,
    val interaction: HostTableInteractionState,
)

internal fun clocktowerDayOverviewTableState(
    gameState: GameState,
    roleDisplayName: (RoleId) -> String = { roleId -> roleId.value },
): ClocktowerDayOverviewTableState = ClocktowerDayOverviewTableState(
    seats = gameState.toHostSeatPresentations(roleDisplayName),
    interaction = HostTableInteractionState(mode = HostTableInteractionMode.ReadOnly),
)
