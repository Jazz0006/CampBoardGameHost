package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.PlanEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationSearch
import com.codex.campboardgamehost.clocktower.rules.PlanLegalityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

private data class ReviewedPlan(
    val legal: Boolean,
    val recommended: Boolean,
    val score: Int,
    val warningCount: Int,
    val realEvilCandidateHits: Int,
    val redHerringOverlap: Boolean,
    val drunkPointsToSelf: Boolean,
    val exposurePenalty: Int,
    val bluffSupport: Int,
)

private data class ScenarioReview(
    val id: String,
    val playerCount: Int,
    val legacy: ReviewedPlan,
    val balanced: ReviewedPlan,
    val recommendationCount: Int,
    val distinctRecommendationCount: Int,
    val allRecommendationsLegal: Boolean,
    val elapsedMilliseconds: Double,
)

private object LegacyFirstLegalBaseline {
    private val fortuneTeller = RoleId("Fortune Teller")
    private val drunk = RoleId("Drunk")
    private val investigator = RoleId("Investigator")

    fun create(game: GameState, roleDefinitions: List<RoleDefinition>): CandidatePlan {
        val scriptRoles = roleDefinitions.filter { game.script in it.scriptIds }.distinctBy { it.id }
        val inPlay = game.players.map { it.actualRole }.toSet()
        val decisions = buildList {
            if (game.players.any { it.actualRole == fortuneTeller }) {
                game.players.firstOrNull { it.actualAlignment == Alignment.GOOD }?.let {
                    add(StorytellerDecision.RedHerring(it.seat))
                }
            }

            val drunkPlayer = game.players.firstOrNull { it.actualRole == drunk }
            if (drunkPlayer != null) {
                val legalShownRoles = scriptRoles.filter {
                    it.type == CharacterType.TOWNSFOLK && it.id !in inPlay
                }
                val shownRole = drunkPlayer.shownRole
                    ?.takeIf { shown -> legalShownRoles.any { it.id == shown } }
                    ?: legalShownRoles.first().id
                add(StorytellerDecision.DrunkShownRole(shownRole))
                if (shownRole == investigator) {
                    val shownMinion = scriptRoles.first { it.type == CharacterType.MINION }.id
                    val pair = game.players.filterNot { it.seat == drunkPlayer.seat }.take(2).map { it.seat }.sorted()
                    add(StorytellerDecision.DrunkInvestigatorInfo(shownMinion, pair))
                }
            }

            if (game.players.size >= 7 && game.players.any { it.actualType == CharacterType.DEMON }) {
                val bluffs = scriptRoles
                    .filter { it.alignment == Alignment.GOOD && it.id !in inPlay }
                    .map { it.id }
                    .distinct()
                    .take(3)
                add(StorytellerDecision.DemonBluffs(bluffs))
            }
        }
        return CandidatePlan(decisions)
    }
}

class ExpertRecommendationReviewTest {
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `twenty four expert scenarios beat deterministic first legal baseline`() {
        // Exclude class loading and JVM compilation from the scenario timing samples.
        RecommendationSearch.recommend(ExpertReviewFixtures.scenarios.first().game, roleDefinitions)
        val reviews = ExpertReviewFixtures.scenarios.map(::review)

        assertEquals(24, reviews.size)
        assertTrue(reviews.all { it.allRecommendationsLegal })
        assertTrue(reviews.all { it.recommendationCount >= 1 })
        assertTrue(reviews.count { it.balanced.recommended } >= reviews.count { it.legacy.recommended })
        assertTrue(reviews.map { it.balanced.score }.average() > reviews.map { it.legacy.score }.average())
        assertTrue(reviews.sumOf { it.balanced.realEvilCandidateHits } <= reviews.sumOf { it.legacy.realEvilCandidateHits })
        assertTrue(reviews.map { it.distinctRecommendationCount }.average() >= 2.0)

        println(toMarkdown(reviews))
    }

    @Test
    fun `review fixtures are deterministic and independent of player names`() {
        ExpertReviewFixtures.scenarios.take(4).forEach { scenario ->
            val first = RecommendationSearch.recommend(scenario.game, roleDefinitions)
            val renamedGame = scenario.game.copy(
                players = scenario.game.players.map { it.copy(name = "Renamed ${it.seat}") },
            )
            val second = RecommendationSearch.recommend(renamedGame, roleDefinitions)

            assertEquals(first.map { it.decisions }, second.map { it.decisions })
        }
    }

    private fun review(scenario: ExpertReviewScenario): ScenarioReview {
        val baselineCandidate = LegacyFirstLegalBaseline.create(scenario.game, roleDefinitions)
        val baseline = PlanEvaluator.evaluate(
            game = scenario.game,
            roleDefinitions = roleDefinitions,
            candidate = baselineCandidate,
            profile = RecommendationProfiles.balanced,
        )
        lateinit var recommendations: List<RecommendationPlan>
        val elapsedNanos = measureNanoTime {
            recommendations = RecommendationSearch.recommend(scenario.game, roleDefinitions)
        }
        val balanced = recommendations.first { it.style == RecommendationStyle.BALANCED }
        val allLegal = recommendations.all { plan ->
            PlanLegalityValidator.validate(
                scenario.game,
                roleDefinitions,
                CandidatePlan(plan.decisions),
            ).isEmpty()
        }
        return ScenarioReview(
            id = scenario.id,
            playerCount = scenario.game.players.size,
            legacy = metrics(scenario.game, baseline),
            balanced = metrics(scenario.game, balanced),
            recommendationCount = recommendations.size,
            distinctRecommendationCount = recommendations.map { it.effectSignature }.distinct().size,
            allRecommendationsLegal = allLegal,
            elapsedMilliseconds = elapsedNanos / 1_000_000.0,
        )
    }

    private fun metrics(game: GameState, plan: RecommendationPlan): ReviewedPlan {
        val info = plan.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().singleOrNull()
        val redHerring = plan.decisions.filterIsInstance<StorytellerDecision.RedHerring>().singleOrNull()
        val drunkSeat = game.players.firstOrNull { it.actualRole == RoleId("Drunk") }?.seat
        val evilHits = info?.candidateSeats.orEmpty().count { seat ->
            game.playerAt(seat)?.actualAlignment == Alignment.EVIL
        }
        return ReviewedPlan(
            legal = plan.qualityTier != QualityTier.REJECTED,
            recommended = plan.qualityTier == QualityTier.RECOMMENDED,
            score = plan.totalScore,
            warningCount = plan.warnings.size,
            realEvilCandidateHits = evilHits,
            redHerringOverlap = redHerring?.seat?.let { it in info?.candidateSeats.orEmpty() } == true,
            drunkPointsToSelf = drunkSeat?.let { it in info?.candidateSeats.orEmpty() } == true,
            exposurePenalty = plan.scoreItems.filter { it.ruleId == "candidate-critical-exposure" }.sumOf { -it.delta },
            bluffSupport = plan.scoreItems.filter { it.ruleId == "demon-bluff-ease" }.sumOf { it.delta },
        )
    }

    private fun toMarkdown(reviews: List<ScenarioReview>): String {
        fun rate(count: Int): String = "%.1f%%".format(count * 100.0 / reviews.size)
        fun average(selector: (ScenarioReview) -> Int): String = "%.2f".format(reviews.map(selector).average())
        val durationGroups = reviews.groupBy { it.playerCount }.toSortedMap()
        return buildString {
            appendLine("EXPERT_REVIEW_REPORT_START")
            appendLine("| Metric | Legacy baseline | Balanced recommendation |")
            appendLine("|---|---:|---:|")
            appendLine("| Legal scenarios | ${rate(reviews.count { it.legacy.legal })} | ${rate(reviews.count { it.balanced.legal })} |")
            appendLine("| Recommended tier | ${rate(reviews.count { it.legacy.recommended })} | ${rate(reviews.count { it.balanced.recommended })} |")
            appendLine("| Average score | ${average { it.legacy.score }} | ${average { it.balanced.score }} |")
            appendLine("| Average warnings | ${average { it.legacy.warningCount }} | ${average { it.balanced.warningCount }} |")
            appendLine("| Real evil candidate hits | ${reviews.sumOf { it.legacy.realEvilCandidateHits }} | ${reviews.sumOf { it.balanced.realEvilCandidateHits }} |")
            appendLine("| Red herring overlaps | ${reviews.count { it.legacy.redHerringOverlap }} | ${reviews.count { it.balanced.redHerringOverlap }} |")
            appendLine("| Drunk points to self | ${reviews.count { it.legacy.drunkPointsToSelf }} | ${reviews.count { it.balanced.drunkPointsToSelf }} |")
            appendLine("| Average exposure penalty | ${average { it.legacy.exposurePenalty }} | ${average { it.balanced.exposurePenalty }} |")
            appendLine("| Average bluff support | ${average { it.legacy.bluffSupport }} | ${average { it.balanced.bluffSupport }} |")
            appendLine()
            appendLine("Average distinct recommendations: %.2f".format(reviews.map { it.distinctRecommendationCount }.average()))
            durationGroups.forEach { (players, group) ->
                appendLine("${players}-player runtime: average %.1f ms, max %.1f ms".format(group.map { it.elapsedMilliseconds }.average(), group.maxOf { it.elapsedMilliseconds }))
            }
            appendLine("EXPERT_REVIEW_REPORT_END")
        }
    }
}
