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

internal sealed interface ResolvedNightMechanicalEvent {
    data class MechanicalDeath(
        val targetSeat: Int,
        val effectiveAt: ClocktowerEffectiveNightCursor,
    ) : ResolvedNightMechanicalEvent

    data class RoleChanged(
        val targetSeat: Int,
        val roleId: RoleId,
        val effectiveAt: ClocktowerEffectiveNightCursor,
    ) : ResolvedNightMechanicalEvent
}

internal object ClocktowerEffectiveNightStateProjector {
    fun projectAt(
        baseAliveSeats: Set<Int>,
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        confirmedEvents: List<ResolvedNightMechanicalEvent>,
        cursor: ClocktowerEffectiveNightCursor,
        baseRoleIdsBySeat: Map<Int, RoleId> = emptyMap(),
    ): ClocktowerEffectiveNightState {
        val cursorRank = ClocktowerEffectiveNightChronology.rankOf(canonicalInteractionIds, cursor)
        fun eventCursor(event: ResolvedNightMechanicalEvent): ClocktowerEffectiveNightCursor = when (event) {
            is ResolvedNightMechanicalEvent.MechanicalDeath -> event.effectiveAt
            is ResolvedNightMechanicalEvent.RoleChanged -> event.effectiveAt
        }
        val rankedEvents = confirmedEvents.map { event ->
            event to ClocktowerEffectiveNightChronology.rankOf(canonicalInteractionIds, eventCursor(event))
        }
        val effectiveDeaths = rankedEvents
            .filter { (_, eventRank) -> eventRank <= cursorRank }
            .mapNotNull { (event, _) -> (event as? ResolvedNightMechanicalEvent.MechanicalDeath)?.targetSeat }
            .toSet()
        val effectiveRoleIdsBySeat = baseRoleIdsBySeat.toMutableMap()
        rankedEvents
            .mapIndexedNotNull { index, (event, eventRank) ->
                (event as? ResolvedNightMechanicalEvent.RoleChanged)
                    ?.takeIf { eventRank <= cursorRank }
                    ?.let { index to it }
            }
            .sortedWith(compareBy<Pair<Int, ResolvedNightMechanicalEvent.RoleChanged>> { (index, _) ->
                rankedEvents[index].second
            }.thenBy { (index, _) -> index })
            .forEach { (_, event) -> effectiveRoleIdsBySeat[event.targetSeat] = event.roleId }
        return ClocktowerEffectiveNightState(baseAliveSeats - effectiveDeaths, effectiveRoleIdsBySeat)
    }
}
