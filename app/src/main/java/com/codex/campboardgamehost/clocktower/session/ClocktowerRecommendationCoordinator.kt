package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionExplanation
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext

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

    fun resolveInformation(request: InformationResolutionRequest) = nightModule.resolveInformation(request)

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
