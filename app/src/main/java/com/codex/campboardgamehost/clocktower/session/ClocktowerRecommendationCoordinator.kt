package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DecisionExplanation
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.ImpairedTruthfulException
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator

internal class ClocktowerRecommendationCoordinator(
    initialArchive: DecisionHistoryArchive = DecisionHistoryArchive(),
    private val setupModule: SetupRecommendationModule = SetupRecommendationModule(),
    private val nightModule: NightRecommendationModule = NightRecommendationModule(),
    private val dayModule: DayRecommendationModule = DayRecommendationModule(),
    private val historyModule: HistoryReviewModule = HistoryReviewModule(),
) {
    private val eventStore = InMemoryDecisionEventStore(initialArchive)

    fun recommendSetup(request: SetupCoordinationRequest) = setupModule.recommend(
        request.game,
        request.roles,
        request.lockedDecisions,
        request.history,
    )

    fun selectSetupPlan(
        request: SetupCoordinationRequest,
        style: RecommendationStyle,
    ): RecommendationPlan? {
        val result = recommendSetup(request)
        if (result.failureCodes.isNotEmpty()) return null
        return WeightedStableSelector.selectStyle(result.plans, style, RecommendationPlan::style)
    }

    /** B7.3 setup projection: one pool supplies both AUTO and ASSISTED. */
    fun unifiedSetupPool(plans: List<RecommendationPlan>): UnifiedSelectionPool<RecommendationPlan>? =
        plans.takeIf { it.isNotEmpty() }?.let { source ->
            UnifiedSelectionPool(source.mapIndexed { index, plan ->
                val canonicalPlan = SetupCandidateGenerator.canonicalPlan(plan.decisions)
                UnifiedSelectionCandidate(
                    // A legal no-op setup plan has an empty canonical decision list, and the
                    // same outcome can legitimately appear under several recommendation styles.
                    // Keep each stable source variant distinct for AUTO style selection.
                    candidateId = java.lang.Long.toUnsignedString(
                        MurmurHash3.low64Utf8(
                            "unified-setup-plan-v2|$canonicalPlan|${plan.style.name}|" +
                                "${plan.qualityTier.name}|${plan.totalScore}|$index",
                        ),
                        16,
                    ).padStart(16, '0'),
                    familyId = SetupCandidateGenerator.drunkShownRoleFamily(plan.decisions) ?: "setup-plan",
                    legality = UnifiedCandidateLegality.LEGAL,
                    epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
                    qualityTier = plan.qualityTier,
                    rankFixedPoint = plan.totalScore.toLong() * 1_000L,
                    reasonCodes = plan.scoreItems.map { it.ruleId },
                    warningCodes = plan.warnings.map { it.ruleId },
                    payload = plan,
                )
            })
        }

    fun selectSetupPlan(
        plans: List<RecommendationPlan>,
        style: RecommendationStyle,
    ): RecommendationPlan? = unifiedSetupPool(plans)
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?.let { WeightedStableSelector.selectStyle(it, style, RecommendationPlan::style) }

    fun resolveInformation(request: InformationResolutionRequest) = nightModule.resolveInformation(request)

    /** Typed UI-adapter seam; rules candidate generation remains owned by the recommendation/session layer. */
    fun resolveNumberInformation(request: InformationResolutionRequest.Number) =
        nightModule.resolveNumberInformation(request)

    /**
     * Recommendation and future structured-manual callers meet here before information can become
     * an observation draft. This method intentionally does not commit history or expose UI state.
     */
    fun <T : DynamicInformationOutcome> informationDecisionContext(
        evaluations: List<DecisionEvaluation<T>>,
        recommendedCandidateIds: Set<String>,
        revision: InformationDecisionRevision,
        semanticIdentity: String,
        draftOf: (DecisionEvaluation<T>) -> EpistemicObservationDraft,
    ): InformationDecisionContext<T> = InformationDecisionContext.fromEvaluations(
        evaluations = evaluations,
        recommendedCandidateIds = recommendedCandidateIds,
        revision = revision,
        semanticIdentity = semanticIdentity,
        draftOf = draftOf,
    )

    fun resolveRegistration(request: RegistrationResolutionRequest) = nightModule.resolveRegistration(
        request.request,
        request.context,
        request.style,
    )

    fun recommendRegistration(request: com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest, context: SpecialRegistrationContext) =
        nightModule.recommendRegistration(request, context)

    fun recommendNumber(context: UnreliableNumberContext) = nightModule.recommendNumber(context)

    fun recommendCategory(candidates: List<UnreliableCategoricalCandidate>) = nightModule.recommendCategory(candidates)

    fun recommendPair(candidates: List<PairInformationCandidate>) = nightModule.recommendPair(candidates)

    fun naturalPairCandidates(game: GameState) = setupModule.naturalPairCandidates(game)

    /**
     * Selects a recommendation suggestion only. Durable information must still pass through
     * [informationDecisionContext] and explicit confirmation before a draft is available.
     */
    fun <T> selectInformation(
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
        truthfulException: ImpairedTruthfulException? = null,
    ): T? = nightModule.selectInformation(
        options,
        reliability,
        style,
        evilAdvantage,
        stableKey,
        recentMisinformationStreak,
        stableIdOf,
        isTruthful,
        misinformationPressure,
        styleOf,
        history,
        historicalSignatureOf,
        selectionAudit,
        truthfulException,
    )

    fun resolveDynamicDecision(request: DynamicResolutionRequest): List<DynamicDecisionRecommendation> = when (request) {
        is DynamicResolutionRequest.MayorDeath -> dayModule.resolveMayorDeath(request.request, request.mayorSeat)
        is DynamicResolutionRequest.DemonSuccessor -> nightModule.resolveDemonSuccessor(request.request)
    }

    fun appendDecision(event: StorytellerDecisionEvent, revision: DecisionRevision): DecisionAppendResult =
        eventStore.appendAtomically(event, revision)

    fun transitionDecision(
        eventId: String,
        expected: DecisionEventStatus,
        next: DecisionEventStatus,
    ): Boolean = eventStore.transitionStatus(eventId, expected, next)

    fun correctDecision(correction: DecisionCorrectionEvent): Boolean = eventStore.appendCorrection(correction)

    fun archive(): DecisionHistoryArchive = eventStore.archive()

    fun explainDecision(plan: RecommendationPlan): DecisionExplanation = DecisionExplanation(
        decisionId = planExplanationId(plan),
        qualityTier = plan.qualityTier,
        totalScore = plan.totalScore,
        explanationCodes = plan.scoreItems.map { it.ruleId }.distinct(),
        warningCodes = plan.warnings.map { it.ruleId }.distinct(),
        affectedSeats = (plan.scoreItems.flatMap { it.affectedSeats } + plan.warnings.flatMap { it.affectedSeats }).toSet(),
    )

    fun explainDecision(recommendation: DynamicDecisionRecommendation): DecisionExplanation = DecisionExplanation(
        decisionId = recommendation.requestId,
        qualityTier = recommendation.qualityTier,
        totalScore = recommendation.totalScore,
        explanationCodes = recommendation.scoreItems.map { it.ruleId }.distinct(),
        warningCodes = recommendation.warnings.map { it.ruleId }.distinct(),
        affectedSeats = (recommendation.scoreItems.flatMap { it.affectedSeats } +
            recommendation.warnings.flatMap { it.affectedSeats }).toSet(),
    )

    fun explainDecision(eventId: String): DecisionExplanation? = historyModule.explainEvent(eventStore.archive(), eventId)

    fun postGameReview() = historyModule.postGameReview(eventStore.archive())

    private fun planExplanationId(plan: RecommendationPlan): String {
        val canonical = plan.decisions.joinToString("|") { decision ->
            when (decision) {
                is StorytellerDecision.RedHerring -> "red-herring:${decision.seat}"
                is StorytellerDecision.DrunkShownRole -> "drunk-role:${decision.role.value}"
                is StorytellerDecision.DrunkInvestigatorInfo ->
                    "drunk-investigator:${decision.shownMinion.value}:${decision.candidateSeats.sorted().joinToString(",")}"
                is StorytellerDecision.DemonBluffs ->
                    "demon-bluffs:${decision.roles.map { it.value }.sorted().joinToString(",")}"
            }
        }
        return java.lang.Long.toUnsignedString(MurmurHash3.low64Utf8("decision-explanation-v1|$canonical"), 16)
            .padStart(16, '0')
    }
}
