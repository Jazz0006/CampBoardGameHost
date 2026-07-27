package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlanWarning
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType

internal enum class RegistrationDetail {
    ALIGNMENT_ONLY,
    ROLE,
}

internal data class SpecialRegistrationContext(
    val subjectSeat: Int,
    val allowedRoles: List<RoleDefinition>,
    val detail: RegistrationDetail,
    val canMisregister: Boolean,
    val outcomeDiscussionValue: Int = 0,
    val outcomeMisinformationPressure: Int = 0,
    /** +1 when the special registration helps evil, -1 when it helps good. */
    val specialRegistrationBalanceImpact: Int = 0,
)

internal object SpecialRegistrationRecommender {
    fun recommend(
        request: DynamicDecisionRequest,
        context: SpecialRegistrationContext,
    ): List<DynamicDecisionRecommendation> {
        require(request.type == StorytellerDecisionType.SPECIAL_REGISTRATION)
        val subject = requireNotNull(request.state.game.playerAt(context.subjectSeat))
        val actual = DynamicDecisionCandidate(
            choice = DynamicStorytellerChoice.Registration(
                subjectSeat = subject.seat,
                registeredAlignment = subject.actualAlignment,
                registeredType = subject.actualType,
                registeredRole = subject.actualRole,
                usesSpecialAbility = false,
            ),
            outcome = PredictedDecisionOutcome.Registration(
                affectedAbility = request.sourceAbility,
                subjectSeat = subject.seat,
                usesSpecialAbility = false,
            ),
        )
        val special = if (!context.canMisregister) {
            emptyList()
        } else {
            when (context.detail) {
                RegistrationDetail.ALIGNMENT_ONLY -> context.allowedRoles
                    .distinctBy(RoleDefinition::alignment)
                RegistrationDetail.ROLE -> context.allowedRoles.distinctBy(RoleDefinition::id)
            }.map { role ->
                DynamicDecisionCandidate(
                    choice = DynamicStorytellerChoice.Registration(
                        subjectSeat = subject.seat,
                        registeredAlignment = role.alignment,
                        registeredType = role.type,
                        registeredRole = role.id,
                        usesSpecialAbility = true,
                    ),
                    outcome = PredictedDecisionOutcome.Registration(
                        affectedAbility = request.sourceAbility,
                        subjectSeat = subject.seat,
                        usesSpecialAbility = true,
                    ),
                )
            }
        }
        val candidates = listOf(actual) + special
        if (candidates.size == 1) {
            return listOf(evaluate(request, context, actual, RecommendationStyle.BALANCED))
        }
        return listOf(
            RecommendationStyle.BALANCED,
            RecommendationStyle.GENTLE,
            RecommendationStyle.AGGRESSIVE,
        ).map { style ->
            candidates
                .map { evaluate(request, context, it, style) }
                .sortedWith(
                    compareByDescending<DynamicDecisionRecommendation> { it.qualityTier == QualityTier.RECOMMENDED }
                        .thenByDescending { it.totalScore }
                        .thenBy { it.registrationKey() },
                )
                .first()
        }
    }

    private fun evaluate(
        request: DynamicDecisionRequest,
        context: SpecialRegistrationContext,
        candidate: DynamicDecisionCandidate,
        style: RecommendationStyle,
    ): DynamicDecisionRecommendation {
        val choice = candidate.choice as DynamicStorytellerChoice.Registration
        val special = choice.usesSpecialAbility
        val metadata = TroubleBrewingRecommendationMetadata.forRole(choice.registeredRole)
        val history = request.state.specialRegistrationCountBySeat[choice.subjectSeat] ?: 0
        val scoreItems = buildList {
            add(
                score(
                    if (special) "special-registration" else "actual-registration",
                    ScoreCategory.BEGINNER_SAFETY,
                    registrationBaseScore(style, special),
                    choice.subjectSeat,
                ),
            )
            add(
                score(
                    "stable-variation",
                    ScoreCategory.DIVERSITY,
                    stableVariation(request, choice, style),
                    choice.subjectSeat,
                ),
            )
            if (special) {
                add(
                    score(
                        "registration-discussion-value",
                        ScoreCategory.DIVERSITY,
                        context.outcomeDiscussionValue + metadata.discussionValue,
                        choice.subjectSeat,
                    ),
                )
                add(
                    score(
                        "registration-pressure",
                        ScoreCategory.CONTRADICTION,
                        pressureScore(style, context.outcomeMisinformationPressure),
                        choice.subjectSeat,
                    ),
                )
                if (history > 0) {
                    add(
                        score(
                            "registration-history",
                            ScoreCategory.DIVERSITY,
                            historyScore(style, history),
                            choice.subjectSeat,
                        ),
                    )
                }
                if (context.specialRegistrationBalanceImpact != 0) {
                    add(
                        score(
                            "global-balance",
                            ScoreCategory.EVIL_PRESSURE,
                            globalBalanceScore(
                                evilAdvantage = request.state.evilAdvantage,
                                impact = context.specialRegistrationBalanceImpact,
                                style = style,
                            ),
                            choice.subjectSeat,
                        ),
                    )
                }
            }
        }
        val warnings = buildList {
            if (special) add(warning("special-registration", choice.subjectSeat))
            if (special && context.outcomeMisinformationPressure >= 4) {
                add(warning("high-information-pressure", choice.subjectSeat))
            }
            if (special && history >= 2) add(warning("repeated-special-registration", choice.subjectSeat))
        }
        val tier = when {
            warnings.any { it.ruleId == "high-information-pressure" } &&
                style != RecommendationStyle.AGGRESSIVE &&
                request.state.evilAdvantage * context.specialRegistrationBalanceImpact > -25 ->
                QualityTier.ACCEPTABLE_WITH_WARNING
            warnings.any { it.ruleId == "repeated-special-registration" } && style != RecommendationStyle.AGGRESSIVE ->
                QualityTier.ACCEPTABLE_WITH_WARNING
            else -> QualityTier.RECOMMENDED
        }
        return DynamicDecisionRecommendation(
            requestId = request.id,
            candidate = candidate,
            style = style,
            qualityTier = tier,
            totalScore = scoreItems.sumOf(ScoreItem::delta),
            scoreItems = scoreItems,
            warnings = warnings,
        )
    }

    private fun registrationBaseScore(style: RecommendationStyle, special: Boolean): Int = when (style) {
        RecommendationStyle.GENTLE -> if (special) -8 else 12
        RecommendationStyle.BALANCED -> if (special) 2 else 6
        RecommendationStyle.AGGRESSIVE -> if (special) 12 else -2
    }

    private fun pressureScore(style: RecommendationStyle, pressure: Int): Int = when (style) {
        RecommendationStyle.GENTLE -> -pressure * 3
        RecommendationStyle.BALANCED -> -kotlin.math.abs(pressure - 2) * 2
        RecommendationStyle.AGGRESSIVE -> pressure * 3
    }

    private fun historyScore(style: RecommendationStyle, history: Int): Int = when (style) {
        RecommendationStyle.GENTLE -> -history * 4
        RecommendationStyle.BALANCED -> -history * 2
        RecommendationStyle.AGGRESSIVE -> history
    }

    private fun globalBalanceScore(
        evilAdvantage: Int,
        impact: Int,
        style: RecommendationStyle,
    ): Int {
        val strength = when (style) {
            RecommendationStyle.GENTLE -> 2
            RecommendationStyle.BALANCED -> 3
            RecommendationStyle.AGGRESSIVE -> 4
        }
        return (-evilAdvantage * impact * strength / 10).coerceIn(-32, 32)
    }

    private fun stableVariation(
        request: DynamicDecisionRequest,
        choice: DynamicStorytellerChoice.Registration,
        style: RecommendationStyle,
    ): Int {
        val radius = when (style) {
            RecommendationStyle.GENTLE -> 1
            RecommendationStyle.BALANCED -> 4
            RecommendationStyle.AGGRESSIVE -> 7
        }
        val mixed = listOf(
            request.state.game.seed,
            request.id.hashCode().toLong(),
            choice.subjectSeat.toLong(),
            choice.registeredRole.value.hashCode().toLong(),
            style.ordinal.toLong(),
        ).fold(17L) { acc, value -> acc * 31L + value }
        return Math.floorMod(mixed, (radius * 2 + 1).toLong()).toInt() - radius
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

    private fun DynamicDecisionRecommendation.registrationKey(): String {
        val choice = candidate.choice as DynamicStorytellerChoice.Registration
        return "${choice.usesSpecialAbility}:${choice.registeredAlignment}:${choice.registeredType}:${choice.registeredRole.value}"
    }
}
