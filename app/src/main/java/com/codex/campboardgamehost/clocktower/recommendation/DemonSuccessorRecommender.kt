package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlanWarning
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType

internal object DemonSuccessorRecommender {
    private val impRole = RoleId("Imp")
    private val scarletWomanRole = RoleId("Scarlet Woman")

    fun recommend(request: DynamicDecisionRequest): List<DynamicDecisionRecommendation> {
        require(request.type == StorytellerDecisionType.DEMON_SUCCESSION)
        val alivePlayers = request.state.game.players.filter { it.alive }
        val minions = alivePlayers.filter { it.actualType == CharacterType.MINION }
        if (minions.isEmpty()) return emptyList()
        val eligible = if (alivePlayers.size >= 5) {
            minions.filter { it.actualRole == scarletWomanRole && !it.poisoned }.ifEmpty { minions }
        } else {
            minions
        }
        val candidates = eligible.map { minion ->
            DynamicDecisionCandidate(
                choice = DynamicStorytellerChoice.DemonSuccessor(minion.seat),
                outcome = PredictedDecisionOutcome.CharacterChange(
                    subjectSeat = minion.seat,
                    fromRole = minion.actualRole,
                    toRole = impRole,
                ),
            )
        }
        if (candidates.size == 1) {
            return listOf(evaluate(request, candidates.single(), RecommendationStyle.BALANCED, alivePlayers.size))
        }
        return listOf(
            RecommendationStyle.BALANCED,
            RecommendationStyle.GENTLE,
            RecommendationStyle.AGGRESSIVE,
        ).map { style ->
            candidates
                .map { evaluate(request, it, style, alivePlayers.size) }
                .sortedWith(
                    compareByDescending<DynamicDecisionRecommendation> { it.totalScore }
                        .thenBy { (it.candidate.choice as DynamicStorytellerChoice.DemonSuccessor).targetSeat },
                )
                .first()
        }
    }

    private fun evaluate(
        request: DynamicDecisionRequest,
        candidate: DynamicDecisionCandidate,
        style: RecommendationStyle,
        aliveCount: Int,
    ): DynamicDecisionRecommendation {
        val choice = candidate.choice as DynamicStorytellerChoice.DemonSuccessor
        val target = requireNotNull(request.state.game.playerAt(choice.targetSeat))
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
        val pressure = request.state.playerInformationPressureBySeat[target.seat]
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
                    delta = (-request.state.evilAdvantage * continuingPower / 15).coerceIn(-24, 24),
                    messageKey = "recommendation.global-balance",
                    affectedSeats = listOf(target.seat),
                ),
            )
        }
        val mandatoryScarletWoman = aliveCount >= 5 && target.actualRole == scarletWomanRole && !target.poisoned
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
            requestId = request.id,
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
