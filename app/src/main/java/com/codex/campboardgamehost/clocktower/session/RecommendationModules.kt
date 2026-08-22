package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RegistrationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.DemonSuccessorRecommender
import com.codex.campboardgamehost.clocktower.recommendation.MayorRedirectRecommender
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.ImpairedTruthfulException
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.MalfunctionPolicy
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationRecommendation
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.RegistrationPolicy
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalRecommendation
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberRecommendation
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService

internal class SetupRecommendationModule {
    fun recommend(
        game: GameState,
        roles: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision>,
        history: CrossGameHistory,
    ): SetupRecommendationService.ConstrainedResult = SetupRecommendationService.recommendConstrained(
        game = game,
        roleDefinitions = roles,
        lockedDecisions = lockedDecisions,
        history = history,
    )

    fun naturalPairCandidates(game: GameState) = SetupCandidateGenerator.generatePairInformationCandidates(game)
}

internal class NightRecommendationModule {
    fun resolveInformation(request: InformationResolutionRequest): List<DecisionEvaluation<out DynamicInformationOutcome>> =
        when (request) {
            is InformationResolutionRequest.Number -> DynamicCandidateGenerator.generateNumeric(request.context, request.generation)
            is InformationResolutionRequest.Category -> DynamicCandidateGenerator.generateCategorical(request.candidates, request.generation)
            is InformationResolutionRequest.Pair -> DynamicCandidateGenerator.generatePairInformation(request.candidates, request.generation)
        }

    fun resolveRegistration(
        request: DynamicDecisionRequest,
        context: SpecialRegistrationContext,
        style: RecommendationStyle,
    ): List<DecisionEvaluation<RegistrationOutcome>> = RegistrationPolicy.generateCandidates(request, context, style)

    fun recommendRegistration(request: DynamicDecisionRequest, context: SpecialRegistrationContext) =
        RegistrationPolicy.recommendRegistration(request, context)

    fun recommendNumber(context: UnreliableNumberContext): List<UnreliableNumberRecommendation> =
        MalfunctionPolicy.recommendNumber(context)

    fun recommendCategory(candidates: List<UnreliableCategoricalCandidate>): List<UnreliableCategoricalRecommendation> =
        MalfunctionPolicy.recommendCategorical(candidates)

    fun recommendPair(candidates: List<PairInformationCandidate>): List<PairInformationRecommendation> =
        RegistrationPolicy.recommendPair(candidates)

    fun resolveDemonSuccessor(request: DynamicDecisionRequest): List<DynamicDecisionRecommendation> =
        DemonSuccessorRecommender.recommend(request)

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
    ): T? = DynamicCandidateGenerator.select(
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
}

internal class DayRecommendationModule {
    fun resolveMayorDeath(request: DynamicDecisionRequest, mayorSeat: Int): List<DynamicDecisionRecommendation> =
        MayorRedirectRecommender.recommend(request, mayorSeat)
}

internal sealed interface InformationResolutionRequest {
    val generation: DynamicGenerationContext

    data class Number(
        val context: UnreliableNumberContext,
        override val generation: DynamicGenerationContext,
    ) : InformationResolutionRequest

    data class Category(
        val candidates: List<UnreliableCategoricalCandidate>,
        override val generation: DynamicGenerationContext,
    ) : InformationResolutionRequest

    data class Pair(
        val candidates: List<PairInformationCandidate>,
        override val generation: DynamicGenerationContext,
    ) : InformationResolutionRequest
}

internal sealed interface DynamicResolutionRequest {
    data class MayorDeath(val request: DynamicDecisionRequest, val mayorSeat: Int) : DynamicResolutionRequest
    data class DemonSuccessor(val request: DynamicDecisionRequest) : DynamicResolutionRequest
}
