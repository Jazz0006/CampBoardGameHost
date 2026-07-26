package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlanWarning
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.PublicBalanceHint
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType

internal object MayorRedirectRecommender {
    private val mayorRole = RoleId("Mayor")
    private val soldierRole = RoleId("Soldier")

    fun recommend(
        request: DynamicDecisionRequest,
        mayorSeat: Int,
        abilityReliable: Boolean = true,
    ): List<DynamicDecisionRecommendation> {
        require(request.type == StorytellerDecisionType.MAYOR_DEATH_RESOLUTION)
        val mayor = requireNotNull(request.state.game.playerAt(mayorSeat))
        require(mayor.actualRole == mayorRole && mayor.alive)
        if (!abilityReliable) return emptyList()

        val candidates = request.state.game.players.map { target ->
            val outcome = resolveOutcome(request, mayorSeat, target.seat)
            DynamicDecisionCandidate(
                choice = DynamicStorytellerChoice.MayorDeathResolution(target.seat),
                outcome = outcome,
            )
        }

        val selectedKeys = mutableSetOf<Int>()
        return listOf(
            RecommendationStyle.BALANCED,
            RecommendationStyle.GENTLE,
            RecommendationStyle.AGGRESSIVE,
        ).mapNotNull { style ->
            candidates
                .map { evaluate(request, mayorSeat, it, style) }
                .sortedWith(
                    compareByDescending<DynamicDecisionRecommendation> { it.qualityTier.priority() }
                        .thenByDescending { it.totalScore }
                        .thenBy { (it.candidate.choice as DynamicStorytellerChoice.MayorDeathResolution).targetSeat },
                )
                .firstOrNull {
                    selectedKeys.add(
                        (it.candidate.choice as DynamicStorytellerChoice.MayorDeathResolution).targetSeat,
                    )
                }
        }
    }

    internal fun resolveOutcome(
        request: DynamicDecisionRequest,
        mayorSeat: Int,
        targetSeat: Int,
    ): PredictedDecisionOutcome.NightDeath {
        val target = requireNotNull(request.state.game.playerAt(targetSeat))
        val actualDeathSeat = when {
            targetSeat == mayorSeat -> mayorSeat
            !target.alive -> null
            targetSeat in request.state.protectedSeats -> null
            target.actualRole == soldierRole -> null
            else -> targetSeat
        }
        return PredictedDecisionOutcome.NightDeath(
            attackedSeat = mayorSeat,
            actualDeathSeat = actualDeathSeat,
            mayorSurvives = targetSeat != mayorSeat,
        )
    }

    private fun evaluate(
        request: DynamicDecisionRequest,
        mayorSeat: Int,
        candidate: DynamicDecisionCandidate,
        style: RecommendationStyle,
    ): DynamicDecisionRecommendation {
        val state = request.state
        val choice = candidate.choice as DynamicStorytellerChoice.MayorDeathResolution
        val outcome = candidate.outcome as PredictedDecisionOutcome.NightDeath
        val target = requireNotNull(state.game.playerAt(choice.targetSeat))
        val mayorDies = choice.targetSeat == mayorSeat
        val noDeath = outcome.actualDeathSeat == null
        val livingDeathTarget = outcome.actualDeathSeat?.takeIf { it != mayorSeat }
            ?.let(state.game::playerAt)
        val targetMetadata = TroubleBrewingRecommendationMetadata.forRole(target.actualRole)
        val aliveAfter = state.game.players.count { it.alive } - if (outcome.actualDeathSeat != null) 1 else 0
        val createsMayorFinalThree = !mayorDies && aliveAfter == 3
        val scoreItems = buildList {
            if (mayorDies) {
                add(score("mayor-direct-resolution", ScoreCategory.BEGINNER_SAFETY, directResolutionScore(style), mayorSeat))
            } else {
                add(score("mayor-survival", ScoreCategory.ROLE_SUITABILITY, mayorSurvivalScore(style), mayorSeat))
            }
            if (noDeath) {
                add(score("no-night-death", ScoreCategory.CONTRADICTION, noDeathScore(style), choice.targetSeat))
            }
            if (livingDeathTarget != null) {
                val spent = livingDeathTarget.seat in state.spentAbilitySeats
                if (spent) {
                    add(score("spent-ability-target", ScoreCategory.BEGINNER_SAFETY, 7, livingDeathTarget.seat))
                } else {
                    add(
                        score(
                            "remaining-player-agency",
                            ScoreCategory.BEGINNER_SAFETY,
                            -targetMetadata.exposureSensitivity,
                            livingDeathTarget.seat,
                        ),
                    )
                }
                if (livingDeathTarget.actualAlignment == Alignment.EVIL) {
                    add(score("evil-player-redirect", ScoreCategory.EVIL_PRESSURE, evilDeathScore(style), livingDeathTarget.seat))
                }
                val pressure = state.informationPressureBySeat[livingDeathTarget.seat] ?: 0
                if (pressure > 0) {
                    add(score("information-pressure-continuity", ScoreCategory.CONTRADICTION, pressureScore(style, pressure), livingDeathTarget.seat))
                }
            }
            if (createsMayorFinalThree) {
                add(score("mayor-final-three-leverage", ScoreCategory.CONFIRMATION, finalThreeScore(state.publicBalanceHint), mayorSeat))
            }
            add(
                score(
                    "public-balance-hint",
                    ScoreCategory.EVIL_PRESSURE,
                    balanceHintScore(state.publicBalanceHint, mayorDies),
                    mayorSeat,
                ),
            )
        }
        val warnings = buildList {
            if (noDeath) add(warning("no-night-death", choice.targetSeat))
            if (createsMayorFinalThree) add(warning("mayor-final-three-leverage", mayorSeat))
            if (livingDeathTarget?.actualAlignment == Alignment.EVIL) {
                add(warning("redirect-kills-evil-player", livingDeathTarget.seat))
            }
            if (
                livingDeathTarget != null &&
                livingDeathTarget.seat !in state.spentAbilitySeats &&
                targetMetadata.exposureSensitivity >= 4
            ) {
                add(warning("unused-high-impact-role-dies", livingDeathTarget.seat))
            }
        }
        val quality = when {
            style != RecommendationStyle.AGGRESSIVE && noDeath && createsMayorFinalThree -> QualityTier.EXPERT_ONLY
            warnings.any { it.ruleId == "unused-high-impact-role-dies" } -> QualityTier.ACCEPTABLE_WITH_WARNING
            warnings.size >= 2 -> QualityTier.ACCEPTABLE_WITH_WARNING
            else -> QualityTier.RECOMMENDED
        }
        return DynamicDecisionRecommendation(
            requestId = request.id,
            candidate = candidate,
            style = style,
            qualityTier = quality,
            totalScore = scoreItems.sumOf(ScoreItem::delta),
            scoreItems = scoreItems,
            warnings = warnings,
        )
    }

    private fun directResolutionScore(style: RecommendationStyle): Int = when (style) {
        RecommendationStyle.GENTLE -> 14
        RecommendationStyle.BALANCED -> 5
        RecommendationStyle.AGGRESSIVE -> -2
    }

    private fun mayorSurvivalScore(style: RecommendationStyle): Int = when (style) {
        RecommendationStyle.GENTLE -> 0
        RecommendationStyle.BALANCED -> 7
        RecommendationStyle.AGGRESSIVE -> 12
    }

    private fun noDeathScore(style: RecommendationStyle): Int = when (style) {
        RecommendationStyle.GENTLE -> -18
        RecommendationStyle.BALANCED -> -10
        RecommendationStyle.AGGRESSIVE -> 8
    }

    private fun evilDeathScore(style: RecommendationStyle): Int = when (style) {
        RecommendationStyle.GENTLE -> -12
        RecommendationStyle.BALANCED -> -9
        RecommendationStyle.AGGRESSIVE -> -3
    }

    private fun pressureScore(style: RecommendationStyle, pressure: Int): Int = when (style) {
        RecommendationStyle.GENTLE -> -pressure * 3
        RecommendationStyle.BALANCED -> -pressure
        RecommendationStyle.AGGRESSIVE -> pressure
    }

    private fun finalThreeScore(hint: PublicBalanceHint): Int = when (hint) {
        PublicBalanceHint.GOOD_AHEAD -> -14
        PublicBalanceHint.EVIL_AHEAD -> 8
        PublicBalanceHint.BALANCED, PublicBalanceHint.UNKNOWN -> -6
    }

    private fun balanceHintScore(hint: PublicBalanceHint, mayorDies: Boolean): Int = when (hint) {
        PublicBalanceHint.GOOD_AHEAD -> if (mayorDies) 7 else -7
        PublicBalanceHint.EVIL_AHEAD -> if (mayorDies) -7 else 7
        PublicBalanceHint.BALANCED, PublicBalanceHint.UNKNOWN -> 0
    }

    private fun score(ruleId: String, category: ScoreCategory, delta: Int, seat: Int) = ScoreItem(
        ruleId = ruleId,
        category = category,
        delta = delta,
        messageKey = "recommendation.$ruleId",
        affectedSeats = listOf(seat),
    )

    private fun warning(ruleId: String, seat: Int) = PlanWarning(
        ruleId = ruleId,
        messageKey = "recommendation.warning.$ruleId",
        affectedSeats = listOf(seat),
    )

    private fun QualityTier.priority(): Int = when (this) {
        QualityTier.RECOMMENDED -> 3
        QualityTier.ACCEPTABLE_WITH_WARNING -> 2
        QualityTier.EXPERT_ONLY -> 1
        QualityTier.REJECTED -> 0
    }
}
