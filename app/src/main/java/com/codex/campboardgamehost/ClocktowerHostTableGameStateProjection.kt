package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Migration boundary from durable Clocktower domain seat numbers into the typed Storyteller table.
 *
 * The incoming [GameState.players] list is not presentation order authority. Physical order comes
 * only from each player's domain `seat`, which is converted to [ClocktowerSeatId] and validated as
 * one contiguous table before any Compose surface sees it.
 *
 * R4D-2 will move seat confirmation earlier in the session so the domain seat numbers themselves
 * originate from a confirmed seating roster rather than being repeatedly inferred by page UI.
 */
internal fun GameState.toHostSeatPresentations(
    roleDisplayName: (RoleId) -> String = { roleId -> roleId.value },
): List<HostSeatPresentation> {
    val orderedPlayers = players.sortedBy { player -> player.seat }
    val expectedSeats = (1..orderedPlayers.size).toList()
    require(orderedPlayers.map { player -> player.seat } == expectedSeats) {
        "Clocktower host table requires unique contiguous domain seats from seat 1"
    }

    return orderedPlayers.map { player ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(player.seat),
            playerName = player.name,
            isAlive = player.alive,
            actualRole = HostRolePresentation(
                roleId = player.actualRole.value,
                displayName = roleDisplayName(player.actualRole),
            ),
            shownRole = player.shownRole?.let { shownRole ->
                HostRolePresentation(
                    roleId = shownRole.value,
                    displayName = roleDisplayName(shownRole),
                )
            },
        )
    }
}
