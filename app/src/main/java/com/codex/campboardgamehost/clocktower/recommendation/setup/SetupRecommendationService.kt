package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.config.RecommendationProfile
import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.config.DecisionScoreTolerances
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.recommendation.CandidatePoolBuilder
import com.codex.campboardgamehost.clocktower.recommendation.FamilyProbabilityBudget
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.recommendation.rankingPriority
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.history.HistoryCooldown
import java.util.PriorityQueue

internal object SetupRecommendationService {
    // Composite setup plans accumulate several independent score items; simulation calibration
    // showed that the document's provisional value of 8 still excluded every non-Investigator role.
    internal const val SETUP_PLAN_SCORE_TOLERANCE = DecisionScoreTolerances.SETUP_PLAN
    private const val SCORE_TEMPERATURE = 11L
    private const val SELECTOR_VERSION = "setup-weighted-v1"
    data class ConstrainedResult(
        val plans: List<RecommendationPlan>,
        val failureCodes: List<String> = emptyList(),
    )

    fun recommend(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        history: CrossGameHistory = CrossGameHistory(),
    ): List<RecommendationPlan> = recommendConstrained(game, roleDefinitions, history = history).plans

    fun recommendConstrained(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision> = emptyList(),
        history: CrossGameHistory = CrossGameHistory(),
    ): ConstrainedResult {
        val duplicateKinds = lockedDecisions
            .groupingBy { it.kind() }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        if (duplicateKinds.isNotEmpty()) {
            return ConstrainedResult(emptyList(), listOf("duplicate-locked-decision"))
        }
        if (lockedDecisions.any { it is StorytellerDecision.DrunkShownRole }) {
            return ConstrainedResult(emptyList(), listOf("shown-identity-is-committed-setup-fact"))
        }
        if (lockedDecisions.isNotEmpty() && SetupCandidateGenerator.generatePlans(game, roleDefinitions, lockedDecisions).firstOrNull() == null) {
            return ConstrainedResult(emptyList(), listOf("locked-decisions-illegal-or-incompatible"))
        }

        val balancedRanking = rankedPlans(game, roleDefinitions, RecommendationProfiles.balanced, lockedDecisions = lockedDecisions)
        val balanced = selectStablePlan(balancedRanking, game, RecommendationProfiles.balanced, history)
            ?: return ConstrainedResult(emptyList())
        val gentleRanking = rankedPlans(game, roleDefinitions, RecommendationProfiles.gentle, lockedDecisions = lockedDecisions)
        val gentle = selectStablePlan(gentleRanking, game, RecommendationProfiles.gentle, history)
            ?.let { diversifyIfNeeded(it, gentleRanking, setOf(balanced.effectSignature)) }
        val aggressiveRanking = rankedPlans(game, roleDefinitions, RecommendationProfiles.aggressive, lockedDecisions = lockedDecisions)
        val aggressive = selectStablePlan(aggressiveRanking, game, RecommendationProfiles.aggressive, history)
            ?.let { diversifyIfNeeded(it, aggressiveRanking, setOfNotNull(balanced.effectSignature, gentle?.effectSignature)) }

        return ConstrainedResult(listOfNotNull(gentle, balanced, aggressive))
    }

    fun rankedPlans(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        profile: RecommendationProfile,
        maxResults: Int = 256,
        lockedDecisions: List<StorytellerDecision> = emptyList(),
    ): List<RecommendationPlan> {
        require(maxResults > 0) { "maxResults must be positive." }
        val bestFirst = compareByDescending<RecommendationPlan> { it.qualityTier.rankingPriority() }
            .thenByDescending { it.totalScore }
            .thenByDescending { stableTieBreak(it, game.seed) }
        val retained = PriorityQueue(maxResults, bestFirst.reversed())
        val retainedByFamily = mutableMapOf<String, PriorityQueue<RecommendationPlan>>()
        val evaluationContext = SetupEvaluator.createContext(game, roleDefinitions)
        var cachedBaseDecisions: List<StorytellerDecision>? = null
        var cachedBaseEvaluation: RecommendationPlan? = null
        val bluffScoreCache = mutableMapOf<List<com.codex.campboardgamehost.clocktower.domain.RoleId>, ScoreItem>()

        SetupCandidateGenerator
            .generatePlans(game, roleDefinitions, lockedDecisions)
            .map { candidate ->
                val demonBluffs = candidate.decisions.lastOrNull() as? StorytellerDecision.DemonBluffs
                if (demonBluffs == null) {
                    SetupEvaluator.evaluateGenerated(evaluationContext, candidate, profile)
                } else {
                    val baseDecisions = candidate.decisions.dropLast(1)
                    val baseEvaluation = if (cachedBaseDecisions == baseDecisions) {
                        requireNotNull(cachedBaseEvaluation)
                    } else {
                        SetupEvaluator.evaluateGenerated(
                            evaluationContext,
                            CandidatePlan(baseDecisions),
                            profile,
                        ).also {
                            cachedBaseDecisions = baseDecisions
                            cachedBaseEvaluation = it
                        }
                    }
                    val bluffScore = bluffScoreCache.getOrPut(demonBluffs.roles) {
                        val ease = demonBluffs.roles.sumOf { role ->
                            5 - TroubleBrewingRecommendationMetadata.forRole(role).bluffDifficulty
                        }
                        ScoreItem(
                            ruleId = "demon-bluff-ease",
                            category = ScoreCategory.BLUFF_SUPPORT,
                            delta = ease * profile.bluffEaseWeight,
                            messageKey = "score.demon-bluff-ease",
                        )
                    }
                    baseEvaluation.copy(
                        decisions = candidate.decisions,
                        totalScore = baseEvaluation.totalScore + bluffScore.delta,
                        scoreItems = baseEvaluation.scoreItems + bluffScore,
                        effectSignature = baseEvaluation.effectSignature.copy(
                            demonBluffs = demonBluffs.roles.toSet(),
                        ),
                    )
                }
            }
            .filter { it.qualityTier != QualityTier.REJECTED }
            .forEach { evaluated ->
                retainBest(retained, evaluated, maxResults, bestFirst)
                val family = setupFamily(evaluated)
                val familyQueue = retainedByFamily.getOrPut(family) {
                    PriorityQueue(maxResults, bestFirst.reversed())
                }
                retainBest(familyQueue, evaluated, maxResults, bestFirst)
            }

        val familyQuota = maxOf(1, maxResults / maxOf(1, retainedByFamily.size * 2))
        val reserved = retainedByFamily.values
            .flatMap { it.sortedWith(bestFirst).take(familyQuota) }
        val selectedById = linkedMapOf<String, RecommendationPlan>()
        reserved.forEach { selectedById.putIfAbsent(SetupCandidateGenerator.canonicalPlan(it.decisions), it) }
        retained.sortedWith(bestFirst).forEach { plan ->
            if (selectedById.size < maxResults) {
                selectedById.putIfAbsent(SetupCandidateGenerator.canonicalPlan(plan.decisions), plan)
            }
        }
        return selectedById.values.sortedWith(bestFirst)
    }

    private fun stableTieBreak(plan: RecommendationPlan, seed: Long): Long {
        val canonical = plan.decisions.joinToString("|") { decision ->
            when (decision) {
                is StorytellerDecision.RedHerring -> "red-herring:${decision.seat}"
                is StorytellerDecision.DrunkShownRole -> "drunk-shown-role:${decision.role.value}"
                is StorytellerDecision.DrunkInvestigatorInfo -> listOf(
                    "drunk-investigator",
                    decision.shownMinion.value,
                    decision.candidateSeats.sorted().joinToString(","),
                ).joinToString(":")
                is StorytellerDecision.DemonBluffs -> {
                    "demon-bluffs:${decision.roles.map { it.value }.sorted().joinToString(",")}"
                }
            }
        }
        return MurmurHash3.low64Utf8("recommendation-plan-v1|$seed|$canonical")
    }

    private fun selectStablePlan(
        rankedPlans: List<RecommendationPlan>,
        game: GameState,
        profile: RecommendationProfile,
        history: CrossGameHistory,
    ): RecommendationPlan? {
        if (rankedPlans.isEmpty()) return null
        val planByCandidateId = linkedMapOf<String, RecommendationPlan>()
        val rawEvaluations = rankedPlans.map { plan ->
            val candidate = SetupCandidateGenerator.planCandidate(CandidatePlan(plan.decisions))
            planByCandidateId[candidate.candidateId] = plan
            DecisionEvaluation(
                candidate = candidate,
                qualityTier = plan.qualityTier,
                totalScore = plan.totalScore,
                withinFamilyWeightFixedPoint = 1,
                finalProbabilityFixedPoint = 0,
                pressureDelta = emptyMap(),
                warnings = plan.warnings.map { it.ruleId },
                explanationCodes = plan.scoreItems.map { it.ruleId }.distinct(),
            )
        }
        val pool = CandidatePoolBuilder.build(rawEvaluations, SETUP_PLAN_SCORE_TOLERANCE)
        if (pool.isEmpty()) return null
        val bestScore = pool.maxOf { it.totalScore }
        val weightedPool = pool.map { evaluation ->
            val delta = evaluation.totalScore.toLong() - bestScore.toLong()
            evaluation.copy(withinFamilyWeightFixedPoint = (SCORE_TEMPERATURE + delta).coerceAtLeast(1L))
        }
        val cooledPool = HistoryCooldown.apply(weightedPool, history) { evaluation ->
            HistoricalClueSignature.fromSetupPlan(
                game,
                planByCandidateId.getValue(evaluation.candidate.candidateId),
            )
        }
        val activeFamilies = cooledPool.map { it.candidate.candidateFamilyId }.distinct().sorted()
        val budget = FamilyProbabilityBudget(
            activeFamilies.associateWith { WeightedStableSelector.FIXED_POINT_SCALE },
        )
        val seed = MurmurHash3.low64Utf8(
            "$SELECTOR_VERSION|${game.seed}|${profile.style.name}|${history.digest()}|${cooledPool.map { it.candidate.candidateId }.sorted().joinToString(",")}",
        )
        val selected = WeightedStableSelector.select(cooledPool, budget, seed) ?: return null
        return planByCandidateId.getValue(selected.selected.candidate.candidateId)
    }

    private fun diversifyIfNeeded(
        selected: RecommendationPlan,
        rankedPlans: List<RecommendationPlan>,
        usedSignatures: Set<com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature>,
    ): RecommendationPlan {
        if (selected.effectSignature !in usedSignatures) return selected
        val bestTier = rankedPlans.firstOrNull()?.qualityTier ?: return selected
        val bestScore = rankedPlans.filter { it.qualityTier == bestTier }.maxOfOrNull { it.totalScore } ?: return selected
        return rankedPlans.firstOrNull {
            it.qualityTier == bestTier &&
                it.totalScore >= bestScore - SETUP_PLAN_SCORE_TOLERANCE &&
                it.effectSignature !in usedSignatures
        } ?: selected
    }

    private fun setupFamily(plan: RecommendationPlan): String =
        SetupCandidateGenerator.drunkInformationFamily(plan.decisions) ?: "setup-plan"

    private fun retainBest(
        queue: PriorityQueue<RecommendationPlan>,
        candidate: RecommendationPlan,
        limit: Int,
        bestFirst: Comparator<RecommendationPlan>,
    ) {
        when {
            queue.size < limit -> queue += candidate
            bestFirst.compare(candidate, queue.peek()) < 0 -> {
                queue.poll()
                queue += candidate
            }
        }
    }
}
