package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

/**
 * Actual previously shown misinformation comes from the single Session observation history.
 * Presentation prose is never a second source of truth.
 * Callers supply the confirmed actor seat and the ability metric being displayed.
 */
internal fun previousClocktowerUnreliableNumber(
    observations: List<RecordedEpistemicObservation>,
    actorSeat: Int,
    roleId: RoleId,
    metric: NumericMetric,
): Int? = observations.asReversed()
    .firstNotNullOfOrNull { observation ->
        val value = observation.proposition as? InformationProposition.NumericResult
        value?.value?.takeIf {
            observation.sourceSeat == actorSeat &&
                observation.sourceAbility == roleId &&
                observation.reliability == ObservationReliability.KNOWN_MALFUNCTIONING &&
                value.sourceSeat == actorSeat &&
                value.metric == metric
        }
    }
