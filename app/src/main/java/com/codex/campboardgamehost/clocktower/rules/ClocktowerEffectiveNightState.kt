package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId

internal data class ClocktowerEffectiveNightState(
    val effectiveAliveSeats: Set<Int>,
) {
    fun isMechanicallyAlive(seat: Int): Boolean = seat in effectiveAliveSeats
}

internal sealed interface ResolvedNightMechanicalEvent {
    data class MechanicalDeath(
        val targetSeat: Int,
        val effectiveAt: ClocktowerInteractionId,
    ) : ResolvedNightMechanicalEvent
}

internal object ClocktowerEffectiveNightStateProjector {
    fun projectAt(
        baseAliveSeats: Set<Int>,
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        confirmedEvents: List<ResolvedNightMechanicalEvent>,
        cursor: ClocktowerInteractionId,
    ): ClocktowerEffectiveNightState {
        val cursorIndex = canonicalInteractionIds.indexOf(cursor)
        require(cursorIndex >= 0) { "Unknown effective-state cursor: ${cursor.value}" }
        require(canonicalInteractionIds.distinct().size == canonicalInteractionIds.size) {
            "Canonical interaction IDs must be unique."
        }
        val rank = canonicalInteractionIds.withIndex().associate { it.value to it.index }
        val effectiveDeaths = confirmedEvents
            .map { event ->
                when (event) {
                    is ResolvedNightMechanicalEvent.MechanicalDeath -> {
                        require(event.effectiveAt in rank) {
                            "Unknown mechanical-event interaction: ${event.effectiveAt.value}"
                        }
                        event
                    }
                }
            }
            .filter { event -> rank.getValue(event.effectiveAt) <= cursorIndex }
            .map { event -> event.targetSeat }
            .toSet()
        return ClocktowerEffectiveNightState(baseAliveSeats - effectiveDeaths)
    }
}
