package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.RecommendationProfile
import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.kind
import java.util.PriorityQueue

internal object RecommendationSearch {
    data class ConstrainedResult(
        val plans: List<RecommendationPlan>,
        val failureCodes: List<String> = emptyList(),
    )

    fun recommend(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
    ): List<RecommendationPlan> = recommendConstrained(game, roleDefinitions).plans

    fun recommendConstrained(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        lockedDecisions: List<StorytellerDecision> = emptyList(),
    ): ConstrainedResult {
        val duplicateKinds = lockedDecisions
            .groupingBy { it.kind() }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        if (duplicateKinds.isNotEmpty()) {
            return ConstrainedResult(emptyList(), listOf("duplicate-locked-decision"))
        }
        if (lockedDecisions.isNotEmpty() && CandidateGenerator.generatePlans(game, roleDefinitions, lockedDecisions).firstOrNull() == null) {
            return ConstrainedResult(emptyList(), listOf("locked-decisions-illegal-or-incompatible"))
        }

        val balancedRanking = rankedPlans(game, roleDefinitions, RecommendationProfiles.balanced, lockedDecisions = lockedDecisions)
        val balanced = balancedRanking.firstOrNull() ?: return ConstrainedResult(emptyList())

        val gentleRanking = rankedPlans(game, roleDefinitions, RecommendationProfiles.gentle, lockedDecisions = lockedDecisions)
        val gentle = CandidatePoolBuilder.selectDiversifiedPlan(
            rankedCandidates = gentleRanking,
            alreadySelected = listOf(balanced),
            profile = RecommendationProfiles.gentle,
        )

        val selectedBeforeAggressive = listOfNotNull(balanced, gentle)
        val aggressiveRanking = rankedPlans(game, roleDefinitions, RecommendationProfiles.aggressive, lockedDecisions = lockedDecisions)
        val aggressive = CandidatePoolBuilder.selectDiversifiedPlan(
            rankedCandidates = aggressiveRanking,
            alreadySelected = selectedBeforeAggressive,
            profile = RecommendationProfiles.aggressive,
        )

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
        val evaluationContext = PlanEvaluator.createContext(game)
        var cachedBaseDecisions: List<StorytellerDecision>? = null
        var cachedBaseEvaluation: RecommendationPlan? = null
        val bluffScoreCache = mutableMapOf<List<com.codex.campboardgamehost.clocktower.domain.RoleId>, ScoreItem>()

        CandidateGenerator
            .generatePlans(game, roleDefinitions, lockedDecisions)
            .map { candidate ->
                val demonBluffs = candidate.decisions.lastOrNull() as? StorytellerDecision.DemonBluffs
                if (demonBluffs == null) {
                    PlanEvaluator.evaluateGenerated(evaluationContext, candidate, profile)
                } else {
                    val baseDecisions = candidate.decisions.dropLast(1)
                    val baseEvaluation = if (cachedBaseDecisions == baseDecisions) {
                        requireNotNull(cachedBaseEvaluation)
                    } else {
                        PlanEvaluator.evaluateGenerated(
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
                when {
                    retained.size < maxResults -> retained += evaluated
                    bestFirst.compare(evaluated, retained.peek()) < 0 -> {
                        retained.poll()
                        retained += evaluated
                    }
                }
            }

        return retained.sortedWith(bestFirst)
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
}
