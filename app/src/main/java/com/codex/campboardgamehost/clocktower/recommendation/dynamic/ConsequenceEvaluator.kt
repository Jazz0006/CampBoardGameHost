package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import kotlin.math.abs

internal data class ConsequenceContext(
    val state: DynamicGameState,
    val style: RecommendationStyle,
    val isOneShotAbility: Boolean = false,
    val playerSelectedTarget: Boolean = false,
    /** Positive values help evil; negative values help good. */
    val alignmentImpact: Int = 0,
) {
    init {
        require(alignmentImpact in -5..5) { "alignmentImpact must be in -5..5." }
    }
}

/** Applies soft consequence risks to a complete, already-legal candidate. */
internal object ConsequenceEvaluator {
    private const val HIGH_IMPACT_THRESHOLD = 4

    fun <T> evaluate(
        evaluation: DecisionEvaluation<T>,
        context: ConsequenceContext,
    ): DecisionEvaluation<T> {
        if (evaluation.qualityTier == QualityTier.REJECTED) return evaluation

        val adjustments = mutableListOf<Pair<String, Int>>()
        val isMisinformation = evaluation.candidate.truthRelation in setOf(
            TruthRelation.FALSE_TO_ACTUAL_STATE,
            TruthRelation.PARTIALLY_TRUE,
        ) || (
            evaluation.candidate.registrations.isNotEmpty() &&
                evaluation.pressureDelta.values.any { it > 0 }
            )
        val maximumImpact = evaluation.pressureDelta.values.maxOfOrNull(::abs) ?: 0

        repeatedTargetPenalty(evaluation, context)?.let {
            adjustments += "pressure.repeated-target-penalty" to it
        }
        if (isMisinformation && context.isOneShotAbility) {
            val penalty = when (context.style) {
                RecommendationStyle.GENTLE -> -18
                RecommendationStyle.BALANCED -> -13
                RecommendationStyle.AGGRESSIVE -> -8
            } - if (context.playerSelectedTarget) 4 else 0
            adjustments += "consequence.one-shot-ability-protection" to penalty
        }
        if (isMisinformation && isHighImpact(maximumImpact, context.state)) {
            val priorRisk = context.state.misinformationLedger.run {
                highImpactFalseCount * 2 + consecutiveFalseCount.coerceAtMost(3) * 2
            }
            val styleRelief = when (context.style) {
                RecommendationStyle.GENTLE -> 0
                RecommendationStyle.BALANCED -> 3
                RecommendationStyle.AGGRESSIVE -> 6
            }
            adjustments += "consequence.high-impact-misinformation-penalty" to
                (-(8 + maximumImpact * 2 + priorRisk) + styleRelief).coerceAtMost(-1)
        }
        if (isMisinformation && context.state.game.players.count { it.alive } <= 3) {
            val targetChoiceRisk = if (context.playerSelectedTarget) 6 else 0
            adjustments += "consequence.final-day-impact-penalty" to when (context.style) {
                RecommendationStyle.GENTLE -> -24 - targetChoiceRisk
                RecommendationStyle.BALANCED -> -18 - targetChoiceRisk
                RecommendationStyle.AGGRESSIVE -> -12 - targetChoiceRisk
            }
        }
        alignmentAdjustment(context)?.let {
            adjustments += "consequence.alignment-advantage-adjustment" to it
        }

        if (adjustments.isEmpty()) return evaluation
        val totalDelta = adjustments.sumOf { it.second }
        val riskCodes = adjustments.filter { it.second < 0 }.map { it.first }
        val highImpactCode = "consequence.high-impact-misinformation-penalty"
        val highImpactRequiresDowngrade = highImpactCode in riskCodes &&
            context.style != RecommendationStyle.AGGRESSIVE &&
            context.state.evilAdvantage * context.alignmentImpact > -25
        val mustDowngrade = riskCodes.any { it != highImpactCode } ||
            highImpactRequiresDowngrade ||
            context.state.misinformationLedger.highImpactFalseCount > 0
        val worsenedTier = when {
            riskCodes.isEmpty() -> evaluation.qualityTier
            totalDelta <= -30 || "consequence.final-day-impact-penalty" in riskCodes &&
                "consequence.one-shot-ability-protection" in riskCodes -> QualityTier.EXPERT_ONLY
            mustDowngrade -> QualityTier.ACCEPTABLE_WITH_WARNING
            else -> evaluation.qualityTier
        }
        return evaluation.copy(
            qualityTier = evaluation.qualityTier.worsenTo(worsenedTier),
            totalScore = evaluation.totalScore + totalDelta,
            withinFamilyWeightFixedPoint = (
                evaluation.withinFamilyWeightFixedPoint + totalDelta.toLong() * 5L
                ).coerceAtLeast(1L),
            warnings = (evaluation.warnings + riskCodes).distinct(),
            explanationCodes = (evaluation.explanationCodes + adjustments.map { it.first }).distinct(),
        )
    }

    private fun <T> repeatedTargetPenalty(
        evaluation: DecisionEvaluation<T>,
        context: ConsequenceContext,
    ): Int? {
        val repeatedPressure = evaluation.pressureDelta.keys.sumOf { seat ->
            val pressure = context.state.playerInformationPressureBySeat[seat] ?: return@sumOf 0
            (
                pressure.directSuspicion + pressure.indirectSuspicion - pressure.confirmation +
                    pressure.recentTargetCount
                ).coerceAtLeast(0)
        }
        if (repeatedPressure < 2) return null
        val strength = when (context.style) {
            RecommendationStyle.GENTLE -> 3
            RecommendationStyle.BALANCED -> 2
            RecommendationStyle.AGGRESSIVE -> 1
        }
        return -(repeatedPressure * strength).coerceAtMost(30)
    }

    private fun isHighImpact(maximumImpact: Int, state: DynamicGameState): Boolean =
        maximumImpact >= HIGH_IMPACT_THRESHOLD ||
            state.misinformationLedger.highImpactFalseCount >= 2 ||
            state.misinformationLedger.consecutiveFalseCount >= 3

    private fun alignmentAdjustment(context: ConsequenceContext): Int? {
        if (context.alignmentImpact == 0 || context.state.evilAdvantage == 0) return null
        val styleStrength = when (context.style) {
            RecommendationStyle.GENTLE -> 2
            RecommendationStyle.BALANCED -> 3
            RecommendationStyle.AGGRESSIVE -> 4
        }
        return (-context.state.evilAdvantage * context.alignmentImpact * styleStrength / 10)
            .coerceIn(-32, 32)
            .takeIf { it != 0 }
    }

    private fun QualityTier.worsenTo(other: QualityTier): QualityTier =
        if (priority() <= other.priority()) this else other

    private fun QualityTier.priority(): Int = when (this) {
        QualityTier.RECOMMENDED -> 3
        QualityTier.ACCEPTABLE_WITH_WARNING -> 2
        QualityTier.EXPERT_ONLY -> 1
        QualityTier.REJECTED -> 0
    }
}
