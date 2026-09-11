package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel

/**
 * Empath compatibility wrapper over the role-neutral structured numeric adapter.
 *
 * UI ownership moved to the dedicated Empath square-table surface. This file now keeps only the
 * typed preparation adapter so legal-domain and commit semantics remain unchanged.
 */
internal fun prepareEmpathNumberInformationUiModel(
    coordinator: ClocktowerRecommendationCoordinator,
    gameId: String,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    actorSeat: Int,
    subjectSeats: List<Int>,
    trueValue: Int,
    reliability: InformationReliability,
    recommendationStyle: RecommendationStyle,
    revision: InformationDecisionRevision,
    recommendedValue: Int?,
    previousShownValue: Int? = null,
    pressureCostPerPoint: Int = 1,
): StructuredNumberInformationUiModel = prepareNumericInformationUiModel(
    coordinator = coordinator,
    gameId = gameId,
    phase = phase,
    round = round,
    sequence = sequence,
    actorSeat = actorSeat,
    abilityRole = RoleId("Empath"),
    metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
    subjectSeats = subjectSeats,
    trueValue = trueValue,
    minimumValue = 0,
    maximumValue = 2,
    reliability = reliability,
    recommendationStyle = recommendationStyle,
    revision = revision,
    recommendedValue = recommendedValue,
    previousShownValue = previousShownValue,
    pressureCostPerPoint = pressureCostPerPoint,
)
