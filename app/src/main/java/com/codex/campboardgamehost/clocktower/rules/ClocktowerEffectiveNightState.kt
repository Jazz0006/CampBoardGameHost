package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId

internal data class ClocktowerEffectiveNightState(
    val effectiveAliveSeats: Set<Int>,
) {
    fun isMechanicallyAlive(seat: Int): Boolean = seat in effectiveAliveSeats
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

internal sealed interface ResolvedNightMechanicalEvent {
    data class MechanicalDeath(
        val targetSeat: Int,
        val effectiveAt: ClocktowerEffectiveNightCursor,
    ) : ResolvedNightMechanicalEvent
}

internal object ClocktowerEffectiveNightStateProjector {
    fun projectAt(
        baseAliveSeats: Set<Int>,
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        confirmedEvents: List<ResolvedNightMechanicalEvent>,
        cursor: ClocktowerEffectiveNightCursor,
    ): ClocktowerEffectiveNightState {
        val cursorRank = ClocktowerEffectiveNightChronology.rankOf(canonicalInteractionIds, cursor)
        val effectiveDeaths = confirmedEvents
            .map { event ->
                when (event) {
                    is ResolvedNightMechanicalEvent.MechanicalDeath -> {
                        ClocktowerEffectiveNightChronology.rankOf(canonicalInteractionIds, event.effectiveAt)
                        event
                    }
                }
            }
        .filter { event -> ClocktowerEffectiveNightChronology.rankOf(canonicalInteractionIds, event.effectiveAt) <= cursorRank }
            .map { event -> event.targetSeat }
            .toSet()
        return ClocktowerEffectiveNightState(baseAliveSeats - effectiveDeaths)
    }
}
