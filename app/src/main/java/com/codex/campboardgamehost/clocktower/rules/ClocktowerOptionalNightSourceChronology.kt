package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId

/**
 * Chronology policy for an optional night source whose interaction may legitimately be absent
 * from the canonical plan, for example a Poisoner who died before the night began.
 *
 * The strict [ClocktowerEffectiveNightChronology] contract remains unchanged: unknown interactions
 * are still programmer errors when ranked directly. Optional-source callers must prove both the
 * queried cursor and the source interaction belong to this night's canonical plan before ranking.
 */
internal object ClocktowerOptionalNightSourceChronology {
    fun hasActedBy(
        canonicalInteractionIds: List<ClocktowerInteractionId>,
        cursor: ClocktowerEffectiveNightCursor,
        sourceInteractionId: ClocktowerInteractionId,
    ): Boolean {
        if (cursor.interactionId !in canonicalInteractionIds) return false
        if (sourceInteractionId !in canonicalInteractionIds) return false

        return ClocktowerEffectiveNightChronology.isAtOrAfter(
            canonicalInteractionIds = canonicalInteractionIds,
            cursor = cursor,
            boundary = ClocktowerEffectiveNightCursor(
                interactionId = sourceInteractionId,
                boundary = ClocktowerInteractionBoundary.AFTER,
            ),
        )
    }
}
