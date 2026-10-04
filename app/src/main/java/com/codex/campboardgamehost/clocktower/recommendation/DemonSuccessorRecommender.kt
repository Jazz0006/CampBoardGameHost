package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PlanWarning
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution

internal object DemonSuccessorRecommender {
    private val impRole = RoleId("Imp")

    fun recommend(
        requestId: String,
        context: TroubleBrewingDemonSuccessorDecisionContext,
    ): List<DynamicDecisionRecommendation> = recommend(
        requestId = requestId,
        game = context.recommendationGameState,
        successionResolution = context.successionResolution,
        playerInformationPressureBySeat = context.playerInformationPressureBySeat,
        evilAdvantage = context.evilAdvantage,
    )

    fun recommend(
        requestId: String,
        game: GameState,
        successionResolution: DemonSuccessionResolution,
        playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
        evilAdvantage: Int,
    ): List<DynamicDecisionRecommendation> {
        require(requestId.isNotBlank()) { "Demon successor recommendation request ID cannot be blank." }
        val legalSeats = when (successionResolution) {
            DemonSuccessionResolution.None -> emptySet()
            is DemonSuccessionResolution.Forced -> setOf(successionResolution.targetSeat)
            is DemonSuccessionResolution.Choice -> successionResolution.targetSeats
        }.sorted()
        if (legalSeats.isEmpty()) return emptyList()
        val candidates = legalSeats.map { seat ->
            val target = requireNotNull(game.playerAt(seat)) {
                "Rules-owned Demon succession target seat is absent from recommendation state: " + seat
            }
            DynamicDecisionCandidate(
                choice = DynamicStorytellerChoice.DemonSuccessor(seat),
                outcome = PredictedDecisionOutcome.CharacterChange(
                    subjectSeat = seat,
                    fromRole = target.actualRole,
                    toRole = impRole,
                ),
            )
        }
        if (candidates.size == 1) {
            return listOf(
                evaluate(
                    requestId = requestId,
                    game = game,
                    successionResolution = successionResolution,
                    playerInformationPressureBySeat = playerInformationPressureBySeat,
                    evilAdvantage = evilAdvantage,
                    candidate = candidates.single(),
                    style = RecommendationStyle.BALANCED,
                ),
            )
        }
        return listOf(
            RecommendationStyle.BALANCED,
            RecommendationStyle.GENTLE,
            RecommendationStyle.AGGRESSIVE,
        ).map { style ->
            candidates
                .map { candidate ->
                    evaluate(
                        requestId = requestId,
                        game = game,
                        successionResolution = successionResolution,
                        playerInformationPressureBySeat = playerInformationPressureBySeat,
                        evilAdvantage = evilAdvantage,
                        candidate = candidate,
                        style = style,
                    )
                }
                .sortedWith(
                    compareByDescending<DynamicDecisionRecommendation> { it.totalScore }
                        .thenBy { (it.candidate.choice as DynamicStorytellerChoice.DemonSuccessor).targetSeat },
                )
                .first()
        }
    }

    private fun evaluate(
        requestId: String,
        game: GameState,
        successionResolution: DemonSuccessionResolution,
        playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
        evilAdvantage: Int,
        candidate: DynamicDecisionCandidate,
        style: RecommendationStyle,
    ): DynamicDecisionRecommendation {
        val choice = candidate.choice as DynamicStorytellerChoice.DemonSuccessor
        val target = requireNotNull(game.playerAt(choice.targetSeat))
        val base = when (style) {
            RecommendationStyle.GENTLE -> when (target.actualRole.value) {
                "Baron" -> 10
                "Scarlet Woman" -> 9
                "Spy" -> 5
                "Poisoner" -> 2
                else -> 4
            }
            RecommendationStyle.BALANCED -> when (target.actualRole.value) {
                "Baron" -> 11
                "Scarlet Woman" -> 10
                "Spy" -> 7
                "Poisoner" -> 3
                else -> 5
            }
            RecommendationStyle.AGGRESSIVE -> when (target.actualRole.value) {
                "Spy" -> 12
                "Poisoner" -> 8
                "Scarlet Woman" -> 7
                "Baron" -> 6
                else -> 5
            }
        }
        val pressure = playerInformationPressureBySeat[target.seat]
            ?.let { it.directSuspicion + it.indirectSuspicion - it.confirmation }
            ?.coerceAtLeast(0)
            ?: 0
        val scoreItems = buildList {
            add(
                ScoreItem(
                    ruleId = "successor-role-suitability",
                    category = ScoreCategory.ROLE_SUITABILITY,
                    delta = base,
                    messageKey = "recommendation.successor-role-suitability",
                    affectedSeats = listOf(target.seat),
                ),
            )
            if (pressure > 0) {
                add(
                    ScoreItem(
                        ruleId = "successor-public-pressure",
                        category = ScoreCategory.EXPOSURE,
                        delta = if (style == RecommendationStyle.AGGRESSIVE) pressure else -pressure,
                        messageKey = "recommendation.successor-public-pressure",
                        affectedSeats = listOf(target.seat),
                    ),
                )
            }
            val continuingPower = when (target.actualRole.value) {
                "Poisoner" -> 4
                "Spy" -> 3
                "Scarlet Woman" -> 2
                "Baron" -> 0
                else -> 1
            }
            add(
                ScoreItem(
                    ruleId = "global-balance",
                    category = ScoreCategory.EVIL_PRESSURE,
                    delta = (-evilAdvantage * continuingPower / 15).coerceIn(-24, 24),
                    messageKey = "recommendation.global-balance",
                    affectedSeats = listOf(target.seat),
                ),
            )
        }
        val mandatoryScarletWoman =
            (successionResolution as? DemonSuccessionResolution.Forced)?.targetSeat == target.seat
        val warnings = buildList {
            if (target.actualRole.value == "Poisoner") {
                add(
                    PlanWarning(
                        ruleId = "active-minion-ability-lost",
                        messageKey = "recommendation.warning.active-minion-ability-lost",
                        affectedSeats = listOf(target.seat),
                    ),
                )
            }
            if (mandatoryScarletWoman) {
                add(
                    PlanWarning(
                        ruleId = "scarlet-woman-mandatory",
                        messageKey = "recommendation.warning.scarlet-woman-mandatory",
                        affectedSeats = listOf(target.seat),
                    ),
                )
            }
        }
        return DynamicDecisionRecommendation(
            requestId = requestId,
            candidate = candidate,
            style = style,
            qualityTier = if (warnings.any { it.ruleId == "active-minion-ability-lost" }) {
                QualityTier.ACCEPTABLE_WITH_WARNING
            } else {
                QualityTier.RECOMMENDED
            },
            totalScore = scoreItems.sumOf(ScoreItem::delta),
            scoreItems = scoreItems,
            warnings = warnings,
        )
    }
}
