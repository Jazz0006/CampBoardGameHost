package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.PlanWarning
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.TruthRelation

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
    val isOneShotAbility: Boolean = false,
    val playerSelectedTarget: Boolean = false,
    val registrationQuestion: RegistrationQuestion = when (detail) {
        RegistrationDetail.ALIGNMENT_ONLY -> RegistrationQuestion.ALIGNMENT
        RegistrationDetail.ROLE -> RegistrationQuestion.ROLE
    },
)

internal object RegistrationPolicy {
    private const val CANDIDATE_SCHEMA_VERSION = "registration-v1"

    fun generateCandidates(
        request: DynamicDecisionRequest,
        context: SpecialRegistrationContext,
        style: RecommendationStyle,
    ): List<DecisionEvaluation<RegistrationOutcome>> {
        require(request.type == StorytellerDecisionType.SPECIAL_REGISTRATION)
        val subject = requireNotNull(request.state.game.playerAt(context.subjectSeat))
        val possible = buildList {
            add(
                RegistrationOutcome(
                    subject.seat,
                    subject.actualAlignment,
                    subject.actualType,
                    subject.actualRole,
                    usesSpecialAbility = false,
                ),
            )
            if (context.canMisregister) {
                val roles = when (context.detail) {
                    RegistrationDetail.ALIGNMENT_ONLY -> context.allowedRoles.distinctBy { it.alignment }
                    RegistrationDetail.ROLE -> context.allowedRoles.distinctBy { it.id }
                }
                roles.forEach { role ->
                    add(RegistrationOutcome(subject.seat, role.alignment, role.type, role.id, usesSpecialAbility = true))
                }
            }
        }
        return possible.map { outcome ->
            val registration = if (outcome.usesSpecialAbility) {
                RegistrationFact(
                    interactionId = "${request.id}:${subject.seat}:${outcome.registeredRole.value}:${context.registrationQuestion.name}",
                    subjectSeat = subject.seat,
                    registeredRole = outcome.registeredRole,
                    registeredType = outcome.registeredType,
                    registeredAlignment = outcome.registeredAlignment,
                    registrationQuestion = context.registrationQuestion,
                    reason = if (subject.actualRole.value == "Spy") RegistrationReason.SPY_ABILITY else RegistrationReason.RECLUSE_ABILITY,
                )
            } else {
                null
            }
            val candidate = DecisionCandidate(
                candidateId = stableRegistrationId(request, outcome, context.registrationQuestion),
                candidateFamilyId = when {
                    !outcome.usesSpecialAbility -> "natural-truth"
                    subject.actualRole.value == "Spy" -> "registration-spy"
                    else -> "registration-recluse"
                },
                outcome = outcome,
                abilityState = AbilityState.FUNCTIONING,
                truthRelation = if (outcome.usesSpecialAbility) {
                    TruthRelation.TRUE_TO_REGISTERED_STATE
                } else {
                    TruthRelation.TRUE_TO_ACTUAL_STATE
                },
                registrations = listOfNotNull(registration),
                effects = listOf(EffectDraft.Reminder(subject.seat, "registration:${outcome.registeredRole.value}")),
                metadata = CandidateMetadata(
                    candidateSchemaVersion = CANDIDATE_SCHEMA_VERSION,
                    decisionType = "special-registration",
                    tags = setOf("registration", context.registrationQuestion.name.lowercase()),
                ),
            )
            val legacyCandidate = DynamicDecisionCandidate(
                choice = DynamicStorytellerChoice.Registration(
                    subjectSeat = outcome.subjectSeat,
                    registeredAlignment = outcome.registeredAlignment,
                    registeredType = outcome.registeredType,
                    registeredRole = outcome.registeredRole,
                    usesSpecialAbility = outcome.usesSpecialAbility,
                ),
                outcome = PredictedDecisionOutcome.Registration(
                    affectedAbility = request.sourceAbility,
                    subjectSeat = outcome.subjectSeat,
                    usesSpecialAbility = outcome.usesSpecialAbility,
                ),
            )
            val evaluated = evaluate(request, context, legacyCandidate, style)
            val baseEvaluation = DecisionEvaluation(
                candidate = candidate,
                qualityTier = evaluated.qualityTier,
                totalScore = evaluated.totalScore,
                withinFamilyWeightFixedPoint = (100L + evaluated.totalScore * 5L).coerceAtLeast(1L),
                finalProbabilityFixedPoint = 0,
                pressureDelta = if (outcome.usesSpecialAbility) {
                    mapOf(subject.seat to context.outcomeMisinformationPressure)
                } else {
                    emptyMap()
                },
                warnings = evaluated.warnings.map { it.ruleId },
                explanationCodes = evaluated.scoreItems.map { it.ruleId }.distinct(),
            )
            ConsequenceEvaluator.evaluate(
                baseEvaluation,
                ConsequenceContext(
                    state = request.state,
                    style = style,
                    isOneShotAbility = context.isOneShotAbility,
                    playerSelectedTarget = context.playerSelectedTarget,
                    alignmentImpact = if (outcome.usesSpecialAbility) {
                        context.specialRegistrationBalanceImpact
                    } else {
                        0
                    },
                ),
            )
        }.sortedBy { it.candidate.candidateId }
    }
    fun recommendRegistration(
        request: DynamicDecisionRequest,
        context: SpecialRegistrationContext,
    ): List<DynamicDecisionRecommendation> {
        val styles = if (context.canMisregister) {
            listOf(RecommendationStyle.BALANCED, RecommendationStyle.GENTLE, RecommendationStyle.AGGRESSIVE)
        } else {
            listOf(RecommendationStyle.BALANCED)
        }
        return styles.map { style ->
            val selected = generateCandidates(request, context, style)
                .sortedWith(
                    compareByDescending<DecisionEvaluation<RegistrationOutcome>> {
                        it.qualityTier == QualityTier.RECOMMENDED
                    }
                        .thenByDescending { it.totalScore }
                        .thenBy { it.candidate.candidateId },
                )
                .first()
            val outcome = selected.candidate.outcome
            evaluate(
                request,
                context,
                DynamicDecisionCandidate(
                    choice = DynamicStorytellerChoice.Registration(
                        subjectSeat = outcome.subjectSeat,
                        registeredAlignment = outcome.registeredAlignment,
                        registeredType = outcome.registeredType,
                        registeredRole = outcome.registeredRole,
                        usesSpecialAbility = outcome.usesSpecialAbility,
                    ),
                    outcome = PredictedDecisionOutcome.Registration(
                        affectedAbility = request.sourceAbility,
                        subjectSeat = outcome.subjectSeat,
                        usesSpecialAbility = outcome.usesSpecialAbility,
                    ),
                ),
                style,
            )
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
        val history = request.state.registrationLedgerBySeat[choice.subjectSeat]?.totalRegistrations ?: 0
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
        val mixed = MurmurHash3.low64Utf8(
            listOf(
                "registration-variation-v1",
                request.state.game.seed.toString(),
                request.id,
                choice.subjectSeat.toString(),
                choice.registeredRole.value,
                style.name,
            ).joinToString("|")
        )
        return Math.floorMod(mixed, (radius * 2 + 1).toLong()).toInt() - radius
    }

    private fun stableRegistrationId(
        request: DynamicDecisionRequest,
        outcome: RegistrationOutcome,
        question: RegistrationQuestion,
    ): String = java.lang.Long.toUnsignedString(
        MurmurHash3.low64Utf8(
            listOf(
                CANDIDATE_SCHEMA_VERSION,
                request.sourceAbility.value,
                outcome.subjectSeat.toString(),
                outcome.registeredAlignment.name,
                outcome.registeredType.name,
                outcome.registeredRole.value,
                outcome.usesSpecialAbility.toString(),
                question.name,
            ).joinToString("|"),
        ),
        16,
    ).padStart(16, '0')

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

    fun recommendPair(candidates: List<PairInformationCandidate>): List<PairInformationRecommendation> {
        val distinctCandidates = candidates.distinctBy(PairInformationCandidate::id)
        if (distinctCandidates.isEmpty()) return emptyList()
        val selectedIds = mutableSetOf<String>()
        val selected = RecommendationStyle.entries.mapNotNull { style ->
            distinctCandidates
                .map { evaluatePair(it, style) }
                .sortedWith(
                    compareByDescending<PairInformationRecommendation> { it.totalScore }
                        .thenBy(PairInformationRecommendation::candidateId),
                )
                .firstOrNull { selectedIds.add(it.candidateId) }
        }
        val truthful = distinctCandidates.firstOrNull { it.isTruthful }
        return if (truthful != null && selected.none { it.candidateId == truthful.id }) {
            selected + evaluatePair(truthful, RecommendationStyle.GENTLE)
        } else {
            selected
        }
    }

    internal fun evaluatePair(
        candidate: PairInformationCandidate,
        style: RecommendationStyle,
    ): PairInformationRecommendation {
        val specialRegistration = candidate.registration != PairInformationRegistration.NONE
        val score = when (style) {
            RecommendationStyle.GENTLE ->
                (if (candidate.isTruthful) 12 else 0) -
                    (if (specialRegistration) 8 else 0) -
                    candidate.targetExposure * 2 - candidate.decoyExposure -
                    candidate.misinformationPressure * 2 - candidate.historyPressure * 3
            RecommendationStyle.BALANCED ->
                (if (candidate.isTruthful) 1 else 7) + candidate.discussionValue * 3 -
                    kotlin.math.abs(candidate.misinformationPressure - 2) * 2 - candidate.targetExposure -
                    (if (specialRegistration) 1 else 0) - candidate.historyPressure * 2
            RecommendationStyle.AGGRESSIVE ->
                (if (candidate.isTruthful) 0 else 8) + candidate.misinformationPressure * 4 +
                    candidate.discussionValue * 2 + (if (specialRegistration) 8 else 0) + candidate.historyPressure
        }
        return PairInformationRecommendation(
            candidateId = candidate.id,
            style = style,
            totalScore = score,
            warningIds = buildList {
                if (specialRegistration) add("special-registration")
                if (candidate.misinformationPressure >= 4) add("high-information-pressure")
                if (candidate.targetExposure + candidate.decoyExposure >= 8) add("critical-role-exposure")
                if (candidate.historyPressure >= 2) add("repeated-target-pressure")
            },
        )
    }
}

internal enum class PairInformationRegistration {
    NONE,
    SPY_AS_GOOD_ROLE,
    RECLUSE_AS_EVIL_ROLE,
}

internal data class PairInformationCandidate(
    val id: String,
    val registration: PairInformationRegistration,
    val isTruthful: Boolean = true,
    val targetExposure: Int,
    val decoyExposure: Int,
    val discussionValue: Int,
    val misinformationPressure: Int,
    val historyPressure: Int = 0,
) {
    init {
        require(id.isNotBlank())
        require(targetExposure >= 0)
        require(decoyExposure >= 0)
        require(discussionValue >= 0)
        require(misinformationPressure >= 0)
        require(historyPressure >= 0)
    }
}

internal data class PairInformationRecommendation(
    val candidateId: String,
    val style: RecommendationStyle,
    val totalScore: Int,
    val warningIds: List<String>,
)
