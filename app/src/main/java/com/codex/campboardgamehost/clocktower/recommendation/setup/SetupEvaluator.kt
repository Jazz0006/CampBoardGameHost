package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.config.RecommendationProfile
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.PlanWarning
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScoreCategory
import com.codex.campboardgamehost.clocktower.domain.ScoreItem
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.rules.PlanLegalityValidator

internal object SetupEvaluator {
    private val drunk = RoleId("Drunk")
    private val empath = RoleId("Empath")
    private val investigator = RoleId("Investigator")

    fun evaluateClue(
        game: GameState,
        candidate: DecisionCandidate<SetupClueOutcome>,
        profile: RecommendationProfile,
    ): DecisionEvaluation<SetupClueOutcome> {
        val recommendation = when (val outcome = candidate.outcome) {
            is SetupClueOutcome.DrunkShownRole -> evaluateGenerated(
                createContext(game),
                CandidatePlan(
                    listOfNotNull(
                        StorytellerDecision.DrunkShownRole(outcome.shownRole),
                        outcome.investigatorInformation,
                    ),
                ),
                profile,
            )
            is SetupClueOutcome.RedHerring -> evaluateGenerated(
                createContext(game),
                CandidatePlan(listOf(StorytellerDecision.RedHerring(outcome.seat))),
                profile,
            )
            is SetupClueOutcome.DemonBluffs -> evaluateGenerated(
                createContext(game),
                CandidatePlan(listOf(StorytellerDecision.DemonBluffs(outcome.roles))),
                profile,
            )
            is SetupClueOutcome.FullPlan -> evaluateGenerated(
                createContext(game),
                CandidatePlan(outcome.decisions),
                profile,
            )
            is SetupClueOutcome.PairInformation -> pairInformationRecommendation(game, outcome, profile)
        }
        return recommendation.toDecisionEvaluation(candidate)
    }

    fun evaluatePlanCandidate(
        context: PlanEvaluationContext,
        candidate: DecisionCandidate<SetupClueOutcome.FullPlan>,
        profile: RecommendationProfile,
    ): DecisionEvaluation<SetupClueOutcome.FullPlan> =
        evaluateGenerated(context, CandidatePlan(candidate.outcome.decisions), profile)
            .toDecisionEvaluation(candidate)

    fun evaluate(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        candidate: CandidatePlan,
        profile: RecommendationProfile,
    ): RecommendationPlan = evaluateInternal(
        context = createContext(game),
        candidate = candidate,
        profile = profile,
        legalityFailureCodes = PlanLegalityValidator
            .validate(game, roleDefinitions, candidate)
            .map { it.code },
    )

    fun createContext(game: GameState): PlanEvaluationContext = PlanEvaluationContext(
        game = game,
        drunkPlayer = game.players.firstOrNull { it.actualRole == drunk },
        zeroEmpathProtectedSeats = zeroEmpathProtectedSeats(game),
    )

    fun evaluateGenerated(
        context: PlanEvaluationContext,
        candidate: CandidatePlan,
        profile: RecommendationProfile,
    ): RecommendationPlan = evaluateInternal(
        context = context,
        candidate = candidate,
        profile = profile,
        legalityFailureCodes = emptyList(),
    )

    private fun evaluateInternal(
        context: PlanEvaluationContext,
        candidate: CandidatePlan,
        profile: RecommendationProfile,
        legalityFailureCodes: List<String>,
    ): RecommendationPlan {
        val game = context.game
        if (legalityFailureCodes.isNotEmpty()) {
            return RecommendationPlan(
                decisions = candidate.decisions,
                observations = emptyList(),
                qualityTier = QualityTier.REJECTED,
                style = profile.style,
                totalScore = Int.MIN_VALUE,
                scoreItems = emptyList(),
                warnings = legalityFailureCodes.map { failureCode ->
                    PlanWarning(ruleId = failureCode, messageKey = "legality.$failureCode")
                },
                effectSignature = candidate.effectSignature(),
            )
        }

        val scoreItems = mutableListOf<ScoreItem>()
        val warnings = mutableListOf<PlanWarning>()
        var qualityTier = QualityTier.RECOMMENDED
        val redHerring = candidate.decision<StorytellerDecision.RedHerring>()
        val drunkShownRole = candidate.decision<StorytellerDecision.DrunkShownRole>()
        val drunkInfo = candidate.decision<StorytellerDecision.DrunkInvestigatorInfo>()
        val demonBluffs = candidate.decision<StorytellerDecision.DemonBluffs>()
        val drunkPlayer = context.drunkPlayer

        redHerring?.let { decision ->
            val target = game.playerAt(decision.seat) ?: return@let
            val metadata = TroubleBrewingRecommendationMetadata.forRole(target.actualRole)
            addScore(
                scoreItems,
                ruleId = "red-herring-role-suitability",
                category = ScoreCategory.ROLE_SUITABILITY,
                delta = metadata.redHerringSuitability * 2,
                seats = listOf(target.seat),
            )
            addScore(
                scoreItems,
                ruleId = "red-herring-sensitive-role",
                category = ScoreCategory.EXPOSURE,
                delta = -metadata.exposureSensitivity * profile.criticalExposurePenalty,
                seats = listOf(target.seat),
            )
        }

        drunkShownRole?.let { decision ->
            val metadata = TroubleBrewingRecommendationMetadata.forRole(decision.role)
            addScore(
                scoreItems,
                ruleId = "drunk-shown-role-suitability",
                category = ScoreCategory.ROLE_SUITABILITY,
                delta = metadata.drunkSuitability * 3,
                seats = listOfNotNull(drunkPlayer?.seat),
            )
        }

        drunkInfo?.let { info ->
            val candidatePlayers = info.candidateSeats.mapNotNull(game::playerAt)
            val candidateSeatSet = info.candidateSeats.toSet()
            val evilCandidates = candidatePlayers.count { it.actualAlignment == Alignment.EVIL }
            val displayMetadata = TroubleBrewingRecommendationMetadata.forRole(info.shownMinion)
            val redHerringOverlap = redHerring?.seat?.let { it in candidateSeatSet } == true
            val protectedSeats = context.zeroEmpathProtectedSeats
            val protectedCandidateCount = candidateSeatSet.count { it in protectedSeats }

            addScore(
                scoreItems,
                ruleId = "investigator-display-suitability",
                category = ScoreCategory.ROLE_SUITABILITY,
                delta = displayMetadata.investigatorDisplaySuitability * 2,
                seats = info.candidateSeats,
            )
            if (evilCandidates == 0) {
                addScore(
                    scoreItems,
                    ruleId = "drunk-info-avoids-real-evil",
                    category = ScoreCategory.BEGINNER_SAFETY,
                    delta = 5,
                    seats = info.candidateSeats,
                )
            } else {
                addScore(
                    scoreItems,
                    ruleId = "drunk-info-hits-real-evil",
                    category = ScoreCategory.EVIL_PRESSURE,
                    delta = -evilCandidates * profile.evilCandidatePenalty,
                    seats = candidatePlayers.filter { it.actualAlignment == Alignment.EVIL }.map { it.seat },
                )
                qualityTier = qualityTier.worsenTo(
                    if (evilCandidates >= 2) QualityTier.EXPERT_ONLY else QualityTier.ACCEPTABLE_WITH_WARNING,
                )
                warnings += PlanWarning(
                    ruleId = "drunk-info-hits-real-evil",
                    messageKey = "warning.drunk-info-hits-real-evil",
                    affectedSeats = candidatePlayers.filter { it.actualAlignment == Alignment.EVIL }.map { it.seat },
                )
            }

            if (redHerringOverlap) {
                addScore(
                    scoreItems,
                    ruleId = "red-herring-overlaps-drunk-info",
                    category = ScoreCategory.CONFIRMATION,
                    delta = -profile.redHerringOverlapPenalty,
                    seats = listOfNotNull(redHerring?.seat),
                )
            }

            when (protectedCandidateCount) {
                1 -> addScore(
                    scoreItems,
                    ruleId = "one-empath-protected-candidate",
                    category = ScoreCategory.CONTRADICTION,
                    delta = profile.protectedCrossCheckReward,
                    seats = info.candidateSeats,
                )
                2 -> addScore(
                    scoreItems,
                    ruleId = "both-candidates-empath-protected",
                    category = ScoreCategory.CONFIRMATION,
                    delta = -profile.bothProtectedPenalty,
                    seats = info.candidateSeats,
                )
            }

            if (drunkPlayer?.seat in candidateSeatSet) {
                addScore(
                    scoreItems,
                    ruleId = "drunk-points-to-self",
                    category = ScoreCategory.BEGINNER_SAFETY,
                    delta = -profile.drunkSelfCandidatePenalty,
                    seats = listOfNotNull(drunkPlayer?.seat),
                )
                warnings += PlanWarning(
                    ruleId = "drunk-points-to-self",
                    messageKey = "warning.drunk-points-to-self",
                    affectedSeats = listOfNotNull(drunkPlayer?.seat),
                )
                qualityTier = qualityTier.worsenTo(QualityTier.ACCEPTABLE_WITH_WARNING)
            }

            val exposure = candidatePlayers.sumOf {
                TroubleBrewingRecommendationMetadata.forRole(it.actualRole).exposureSensitivity
            }
            val discussion = candidatePlayers.sumOf {
                TroubleBrewingRecommendationMetadata.forRole(it.actualRole).discussionValue
            }
            addScore(
                scoreItems,
                ruleId = "candidate-critical-exposure",
                category = ScoreCategory.EXPOSURE,
                delta = -exposure * profile.criticalExposurePenalty,
                seats = info.candidateSeats,
            )
            addScore(
                scoreItems,
                ruleId = "candidate-discussion-value",
                category = ScoreCategory.CONTRADICTION,
                delta = discussion * profile.discussionValueWeight,
                seats = info.candidateSeats,
            )

            val distance = circularDistance(info.candidateSeats, game.players.size)
            val maximumDistance = game.players.size / 2
            addScore(
                scoreItems,
                ruleId = "candidate-seat-spacing",
                category = ScoreCategory.DIVERSITY,
                delta = (distance - 1).coerceAtLeast(0) * profile.separatedPairWeight +
                    (maximumDistance - distance).coerceAtLeast(0) * profile.clusteredPairWeight,
                seats = info.candidateSeats,
            )
        }

        demonBluffs?.let { decision ->
            val ease = decision.roles.sumOf { role ->
                5 - TroubleBrewingRecommendationMetadata.forRole(role).bluffDifficulty
            }
            addScore(
                scoreItems,
                ruleId = "demon-bluff-ease",
                category = ScoreCategory.BLUFF_SUPPORT,
                delta = ease * profile.bluffEaseWeight,
            )
        }

        return RecommendationPlan(
            decisions = candidate.decisions,
            observations = buildObservations(game, drunkInfo, drunkPlayer?.seat),
            qualityTier = qualityTier,
            style = profile.style,
            totalScore = scoreItems.sumOf(ScoreItem::delta),
            scoreItems = scoreItems,
            warnings = warnings,
            effectSignature = candidate.effectSignature(),
        )
    }

    private fun pairInformationRecommendation(
        game: GameState,
        outcome: SetupClueOutcome.PairInformation,
        profile: RecommendationProfile,
    ): RecommendationPlan {
        val seats = outcome.information.candidateSeats
        val scoreItems = seats.mapNotNull(game::playerAt).flatMap { player ->
            val roleMetadata = TroubleBrewingRecommendationMetadata.forRole(player.actualRole)
            listOf(
                ScoreItem(
                    ruleId = "setup-pair-discussion-value",
                    category = ScoreCategory.CONTRADICTION,
                    delta = roleMetadata.discussionValue * profile.discussionValueWeight,
                    messageKey = "score.setup-pair-discussion-value",
                    affectedSeats = listOf(player.seat),
                ),
                ScoreItem(
                    ruleId = "setup-pair-exposure",
                    category = ScoreCategory.EXPOSURE,
                    delta = -roleMetadata.exposureSensitivity * profile.criticalExposurePenalty,
                    messageKey = "score.setup-pair-exposure",
                    affectedSeats = listOf(player.seat),
                ),
            )
        }
        return RecommendationPlan(
            decisions = emptyList(),
            observations = emptyList(),
            qualityTier = QualityTier.RECOMMENDED,
            style = profile.style,
            totalScore = scoreItems.sumOf { it.delta },
            scoreItems = scoreItems,
            warnings = emptyList(),
            effectSignature = PlanEffectSignature(suspectedSeats = seats.toSet()),
        )
    }

    private fun <T> RecommendationPlan.toDecisionEvaluation(
        candidate: DecisionCandidate<T>,
    ): DecisionEvaluation<T> = DecisionEvaluation(
        candidate = candidate,
        qualityTier = qualityTier,
        totalScore = totalScore,
        withinFamilyWeightFixedPoint = (1_000_000L + totalScore.toLong() * 25_000L).coerceAtLeast(1L),
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = warnings.map { it.ruleId },
        explanationCodes = scoreItems.map { it.ruleId }.distinct(),
    )

    private fun zeroEmpathProtectedSeats(game: GameState): Set<Int> = game.players
        .asSequence()
        .filter { it.actualRole == empath && it.alive && !it.poisoned }
        .filter { empathPlayer ->
            FixedInformationEvaluator.empathEvilNeighborCount(game.players, empathPlayer.seat) == 0
        }
        .flatMap { empathPlayer ->
            FixedInformationEvaluator.livingNeighbors(game.players, empathPlayer.seat).asSequence()
        }
        .map { it.seat }
        .toSet()

    private fun circularDistance(seats: List<Int>, playerCount: Int): Int {
        if (seats.size != 2 || playerCount <= 1) return 0
        val direct = kotlin.math.abs(seats[0] - seats[1])
        return minOf(direct, playerCount - direct)
    }

    private fun buildObservations(
        game: GameState,
        info: StorytellerDecision.DrunkInvestigatorInfo?,
        sourceSeat: Int?,
    ): List<AbilityObservation> {
        if (info == null || sourceSeat == null) return emptyList()
        val statementIsTrue = info.candidateSeats
            .mapNotNull(game::playerAt)
            .any { it.actualRole == info.shownMinion }
        return listOf(
            AbilityObservation(
                sourceSeat = sourceSeat,
                perceivedRole = investigator,
                shownRole = info.shownMinion,
                candidateSeats = info.candidateSeats,
                reliability = ReliabilityState.DRUNK,
                semanticTruth = if (statementIsTrue) SemanticTruth.TRUE else SemanticTruth.FALSE,
            ),
        )
    }

    private fun CandidatePlan.effectSignature(): PlanEffectSignature {
        val redHerring = decision<StorytellerDecision.RedHerring>()
        val shownRole = decision<StorytellerDecision.DrunkShownRole>()
        val investigatorInfo = decision<StorytellerDecision.DrunkInvestigatorInfo>()
        val bluffs = decision<StorytellerDecision.DemonBluffs>()
        return PlanEffectSignature(
            redHerringSeat = redHerring?.seat,
            drunkShownRole = shownRole?.role,
            drunkInvestigatorShownMinion = investigatorInfo?.shownMinion,
            suspectedSeats = investigatorInfo?.candidateSeats?.toSet().orEmpty(),
            demonBluffs = bluffs?.roles?.toSet().orEmpty(),
        )
    }

    private fun addScore(
        items: MutableList<ScoreItem>,
        ruleId: String,
        category: ScoreCategory,
        delta: Int,
        seats: List<Int> = emptyList(),
    ) {
        if (delta == 0) return
        items += ScoreItem(
            ruleId = ruleId,
            category = category,
            delta = delta,
            messageKey = "score.$ruleId",
            affectedSeats = seats,
        )
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

internal data class PlanEvaluationContext(
    val game: GameState,
    val drunkPlayer: PlayerState?,
    val zeroEmpathProtectedSeats: Set<Int>,
)
