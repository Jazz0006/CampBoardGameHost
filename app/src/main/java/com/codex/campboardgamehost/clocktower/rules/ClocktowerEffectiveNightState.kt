package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.domain.RoleId

internal data class ClocktowerEffectiveNightState(
    val effectiveAliveSeats: Set<Int>,
    val effectiveRoleIdsBySeat: Map<Int, RoleId> = emptyMap(),
) {
    fun isMechanicallyAlive(seat: Int): Boolean = seat in effectiveAliveSeats

    fun currentRoleId(seat: Int): RoleId? = effectiveRoleIdsBySeat[seat]
}

internal enum class ClocktowerInteractionBoundary {
    BEFORE,
    AFTER,
}

internal data class ClocktowerEffectiveNightCursor(
    val interactionId: ClocktowerInteractionId,
    val boundary: ClocktowerInteractionBoundary,
)

internal object ClocktowerEffectiveNightChronology {
    fun rankOf(
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        position: ClocktowerEffectiveNightCursor,
    ): Int {
        require(canonicalInteractionIds.distinct().size == canonicalInteractionIds.size) {
            "Canonical interaction IDs must be unique."
        }
        val index = canonicalInteractionIds.indexOf(position.interactionId)
        require(index >= 0) { "Unknown effective-state interaction: ${position.interactionId.value}" }
        return index * 2 + position.boundary.ordinal
    }

    fun isAtOrAfter(
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        cursor: ClocktowerEffectiveNightCursor,
        boundary: ClocktowerEffectiveNightCursor,
    ): Boolean {
        return rankOf(canonicalInteractionIds, cursor) >= rankOf(canonicalInteractionIds, boundary)
    }
}
