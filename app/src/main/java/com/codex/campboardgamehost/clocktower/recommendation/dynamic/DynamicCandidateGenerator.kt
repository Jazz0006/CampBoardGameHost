package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.recommendation.FamilyProbabilityBudget
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCandidate
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.history.HistoryCooldown
import kotlin.math.abs

internal enum class InformationReliability {
    RELIABLE,
    DRUNK,
    POISONED,
}

internal data class DynamicGenerationContext(
    val abilityRole: RoleId,
    val recipientSeat: Int,
    val reliability: InformationReliability,
    val style: RecommendationStyle,
    val state: DynamicGameState? = null,
    val targetSeats: Set<Int> = emptySet(),
    val isOneShotAbility: Boolean = false,
    val playerSelectedTarget: Boolean = false,
    val alignmentImpact: Int = 0,
) {
    init {
        require(recipientSeat > 0) { "recipientSeat must be positive." }
        require(targetSeats.all { it > 0 }) { "target seats must be positive." }
        require(alignmentImpact in -5..5) { "alignmentImpact must be in -5..5." }
    }
}

/** Optional C8 observation attached at the one stable-selection boundary. */
internal data class SelectionAuditContext(
    val selectionId: String,
    val dimensions: SelectionAuditDimensions,
    val recorder: SelectionDistributionTelemetryRecorder,
)

internal object DynamicCandidateGenerator {
    private const val CANDIDATE_SCHEMA_VERSION = "dynamic-v1"
    private const val SELECTOR_VERSION = "dynamic-weighted-v1"

    /** Family names are retained for the commit-time C8 audit without exposing a candidate. */
    fun selectionAuditFamilyId(
        reliability: InformationReliability,
        truthful: Boolean,
    ): String = when {
        truthful && reliability == InformationReliability.RELIABLE -> "natural-truth"
        truthful -> "malfunction-truth"
        else -> "malfunction-falsehood-role"
    }

    fun generateNumeric(
        numberContext: UnreliableNumberContext,
        context: DynamicGenerationContext,
    ): List<DecisionEvaluation<DynamicInformationOutcome.Number>> =
        (numberContext.minimumValue..numberContext.maximumValue).map { value ->
            val recommendation = MalfunctionPolicy.evaluate(numberContext, value, context.style)
            val truthful = value == numberContext.trueValue
            val candidate = candidate(
                stableOptionId = value.toString(),
                outcome = DynamicInformationOutcome.Number(value),
                truthful = truthful,
                context = context,
                informationValue = InformationValue.Number(value),
                decisionType = "numeric-information",
            )
            evaluation(
                candidate = candidate,
                context = context,
                totalScore = recommendation.totalScore,
                pressure = abs(value - numberContext.trueValue),
                warnings = recommendation.warningIds,
                explanations = recommendation.scoreItems.map { it.ruleId },
            )
        }.sortedBy { it.candidate.candidateId }

    fun generateCategorical(
        candidates: List<UnreliableCategoricalCandidate>,
        context: DynamicGenerationContext,
    ): List<DecisionEvaluation<DynamicInformationOutcome.Category>> = candidates
        .distinctBy { it.id }
        .map { input ->
            val recommendation = MalfunctionPolicy.evaluate(input, context.style)
            evaluation(
                candidate = candidate(
                    stableOptionId = input.id,
                    outcome = DynamicInformationOutcome.Category(input.id),
                    truthful = input.isTruthful,
                    context = context,
                    informationValue = InformationValue.Category(input.id),
                    decisionType = "categorical-information",
                ),
                context = context,
                totalScore = recommendation.totalScore,
                pressure = input.misinformationPressure,
                warnings = recommendation.warningIds,
                explanations = listOf("dynamic.categorical-score"),
            )
        }.sortedBy { it.candidate.candidateId }

    fun generatePairInformation(
        candidates: List<PairInformationCandidate>,
        context: DynamicGenerationContext,
    ): List<DecisionEvaluation<DynamicInformationOutcome.Category>> = candidates
        .distinctBy { it.id }
        .map { input ->
            val recommendation = RegistrationPolicy.evaluatePair(input, context.style)
            evaluation(
                candidate = candidate(
                    stableOptionId = input.id,
                    outcome = DynamicInformationOutcome.Category(input.id),
                    truthful = input.isTruthful,
                    context = context,
                    informationValue = InformationValue.Category(input.id),
                    decisionType = "pair-information",
                ),
                context = context,
                totalScore = recommendation.totalScore,
                pressure = input.misinformationPressure,
                warnings = recommendation.warningIds,
                explanations = listOf("dynamic.pair-score"),
            )
        }.sortedBy { it.candidate.candidateId }

    fun misinformationMassFixedPoint(
        reliability: InformationReliability,
        style: RecommendationStyle,
        evilAdvantage: Int,
        recentMisinformationStreak: Int = 0,
        minimumMisinformationPressure: Int = 0,
    ): Long {
        if (reliability == InformationReliability.RELIABLE) return 0
        val base = when (reliability) {
            InformationReliability.DRUNK -> when (style) {
                RecommendationStyle.GENTLE -> 550_000L
                RecommendationStyle.BALANCED -> 650_000L
                RecommendationStyle.AGGRESSIVE -> 750_000L
            }
            InformationReliability.POISONED -> when (style) {
                RecommendationStyle.GENTLE -> 700_000L
                RecommendationStyle.BALANCED -> 820_000L
                RecommendationStyle.AGGRESSIVE -> 920_000L
            }
            InformationReliability.RELIABLE -> 0L
        }
        val balanceAdjustment = (-evilAdvantage.toLong() * 1_500L).coerceIn(-150_000L, 100_000L)
        val streakAdjustment = if (recentMisinformationStreak >= 2) {
            -(recentMisinformationStreak - 1).coerceAtMost(2) * 50_000L
        } else {
            0L
        }
        val impactAdjustment = -(minimumMisinformationPressure - 3).coerceAtLeast(0) * 25_000L
        val range = when (reliability) {
            InformationReliability.DRUNK -> 520_000L..850_000L
            InformationReliability.POISONED -> 600_000L..950_000L
            InformationReliability.RELIABLE -> 0L..0L
        }
        return (base + balanceAdjustment + streakAdjustment + impactAdjustment)
            .coerceIn(range.first, range.last)
    }

    fun <T> select(
        options: List<T>,
        reliability: InformationReliability,
        style: RecommendationStyle,
        evilAdvantage: Int,
        stableKey: String,
        recentMisinformationStreak: Int,
        stableIdOf: (T) -> String,
        isTruthful: (T) -> Boolean,
        misinformationPressure: (T) -> Int,
        styleOf: (T) -> RecommendationStyle,
        history: CrossGameHistory = CrossGameHistory(),
        historicalSignatureOf: ((T) -> HistoricalClueSignature)? = null,
        selectionAudit: SelectionAuditContext? = null,
    ): T? {
        if (options.isEmpty()) return null
        require(stableKey.isNotBlank()) { "stableKey cannot be blank." }
        val ids = options.map(stableIdOf)
        require(ids.all { it.isNotBlank() } && ids.distinct().size == ids.size) {
            "Dynamic option stable IDs must be non-blank and unique."
        }
        val evaluations = options.map { option ->
            val truthful = isTruthful(option)
            val pressure = misinformationPressure(option)
            require(pressure >= 0) { "misinformationPressure cannot be negative." }
            val stableOptionId = stableIdOf(option)
            val preferredPressure = when (style) {
                RecommendationStyle.GENTLE -> 1
                RecommendationStyle.BALANCED -> 2
                RecommendationStyle.AGGRESSIVE -> 4
            }
            val weight = (
                100L - abs(styleOf(option).ordinal - style.ordinal) * 20L -
                    abs(pressure - preferredPressure) * 10L
                ).coerceAtLeast(1L)
            val candidate = candidate(
                stableOptionId = stableOptionId,
                outcome = DynamicInformationOutcome.Category(stableOptionId),
                truthful = truthful,
                context = DynamicGenerationContext(
                    abilityRole = RoleId("Dynamic Information"),
                    recipientSeat = 1,
                    reliability = reliability,
                    style = style,
                ),
                informationValue = InformationValue.Category(stableOptionId),
                decisionType = "generic-dynamic-information",
            )
            DecisionEvaluation(
                candidate = candidate,
                qualityTier = QualityTier.RECOMMENDED,
                totalScore = 0,
                withinFamilyWeightFixedPoint = weight,
                finalProbabilityFixedPoint = 0,
                pressureDelta = emptyMap(),
                warnings = emptyList(),
                explanationCodes = listOf("selection.weighted-stable-random"),
            )
        }
        val misleadingMass = misinformationMassFixedPoint(
            reliability,
            style,
            evilAdvantage,
            recentMisinformationStreak,
            options.filterNot(isTruthful).minOfOrNull(misinformationPressure) ?: 0,
        )
        val truthfulFamily = evaluations.firstOrNull {
            it.candidate.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE
        }?.candidate?.candidateFamilyId
        val misleadingFamily = evaluations.firstOrNull {
            it.candidate.truthRelation == TruthRelation.FALSE_TO_ACTUAL_STATE
        }?.candidate?.candidateFamilyId
        val massByFamily = when {
            truthfulFamily == null -> mapOf(requireNotNull(misleadingFamily) to 1_000_000L)
            misleadingFamily == null || reliability == InformationReliability.RELIABLE -> mapOf(truthfulFamily to 1_000_000L)
            else -> mapOf(
                truthfulFamily to 1_000_000L - misleadingMass,
                misleadingFamily to misleadingMass,
            ).filterValues { it > 0 }
        }
        val activePool = evaluations.filter { (massByFamily[it.candidate.candidateFamilyId] ?: 0L) > 0L }
        val cooledPool = if (historicalSignatureOf == null || history.recentSignatures.isEmpty()) {
            activePool
        } else {
            val optionById = options.associateBy(stableIdOf)
            HistoryCooldown.apply(activePool, history) { evaluation ->
                historicalSignatureOf(requireNotNull(optionById[evaluation.candidate.outcome.id]))
            }
        }
        val historyKey = history.digest().takeIf { it.isNotBlank() }?.let { "|history:$it" }.orEmpty()
        val selection = WeightedStableSelector.select(
            pool = cooledPool,
            familyBudget = FamilyProbabilityBudget(massByFamily),
            decisionSeed = MurmurHash3.low64Utf8("$SELECTOR_VERSION|$stableKey$historyKey"),
        ) ?: return null
        selectionAudit?.let { audit ->
            audit.recorder.recordPreview(
                SelectionAuditRecord(
                    selectionId = audit.selectionId,
                    dimensions = audit.dimensions,
                    // Audit the entire generated pool, rather than the probability- or
                    // cooldown-filtered selector pool, so withholding denominators retain
                    // every family that was available at this decision point.
                    candidates = evaluations.map { evaluation ->
                        SelectionAuditCandidate(
                            familyId = evaluation.candidate.candidateFamilyId,
                            qualityTier = evaluation.qualityTier,
                        )
                    },
                ),
            )
        }
        val selectedId = selection.selected.candidate.outcome.id
        return options.first { stableIdOf(it) == selectedId }
    }

    private fun <T : DynamicInformationOutcome> candidate(
        stableOptionId: String,
        outcome: T,
        truthful: Boolean,
        context: DynamicGenerationContext,
        informationValue: InformationValue,
        decisionType: String,
    ): DecisionCandidate<T> = DecisionCandidate(
        candidateId = stableCandidateId(decisionType, stableOptionId, context),
        candidateFamilyId = when (outcome) {
            is DynamicInformationOutcome.Number -> when {
                truthful && context.reliability == InformationReliability.RELIABLE -> "natural-truth"
                truthful -> "malfunction-truth"
                else -> "malfunction-falsehood-numeric"
            }
            else -> selectionAuditFamilyId(context.reliability, truthful)
        },
        outcome = outcome,
        abilityState = when (context.reliability) {
            InformationReliability.RELIABLE -> AbilityState.FUNCTIONING
            InformationReliability.DRUNK -> AbilityState.MALFUNCTIONING_DRUNK
            InformationReliability.POISONED -> AbilityState.MALFUNCTIONING_POISONED
        },
        truthRelation = if (truthful) TruthRelation.TRUE_TO_ACTUAL_STATE else TruthRelation.FALSE_TO_ACTUAL_STATE,
        effects = listOf(EffectDraft.PlayerInformation(context.recipientSeat, context.abilityRole, informationValue)),
        metadata = CandidateMetadata(CANDIDATE_SCHEMA_VERSION, decisionType, setOf("dynamic-information")),
    )

    private fun <T : DynamicInformationOutcome> evaluation(
        candidate: DecisionCandidate<T>,
        context: DynamicGenerationContext,
        totalScore: Int,
        pressure: Int,
        warnings: List<String>,
        explanations: List<String>,
    ): DecisionEvaluation<T> {
        val pressureSeats = context.targetSeats.ifEmpty {
            setOf(candidate.effects.filterIsInstance<EffectDraft.PlayerInformation>().single().recipientSeat)
        }
        val base = DecisionEvaluation(
            candidate = candidate,
            qualityTier = if (warnings.any { it.contains("high") || it.contains("maximum") }) {
                QualityTier.ACCEPTABLE_WITH_WARNING
            } else {
                QualityTier.RECOMMENDED
            },
            totalScore = totalScore,
            withinFamilyWeightFixedPoint = (100L + totalScore.toLong() * 5L - pressure * 5L).coerceAtLeast(1L),
            finalProbabilityFixedPoint = 0,
            pressureDelta = if (pressure == 0) emptyMap() else pressureSeats.associateWith { pressure },
            warnings = warnings,
            explanationCodes = explanations.distinct(),
        )
        val state = context.state ?: return base
        return ConsequenceEvaluator.evaluate(
            base,
            ConsequenceContext(
                state = state,
                style = context.style,
                isOneShotAbility = context.isOneShotAbility,
                playerSelectedTarget = context.playerSelectedTarget,
                alignmentImpact = context.alignmentImpact,
            ),
        )
    }

    private fun stableCandidateId(
        decisionType: String,
        stableOptionId: String,
        context: DynamicGenerationContext,
    ): String = java.lang.Long.toUnsignedString(
        MurmurHash3.low64Utf8(
            "$CANDIDATE_SCHEMA_VERSION|$decisionType|${context.abilityRole.value}|${context.recipientSeat}|${context.reliability.name}|$stableOptionId",
        ),
        16,
    ).padStart(16, '0')
}
