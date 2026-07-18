package com.codex.campboardgamehost.clocktower.domain

sealed interface StorytellerDecision {
    data class RedHerring(val seat: Int) : StorytellerDecision

    data class DrunkShownRole(val role: RoleId) : StorytellerDecision

    data class DrunkInvestigatorInfo(
        val shownMinion: RoleId,
        val candidateSeats: List<Int>,
    ) : StorytellerDecision

    data class DemonBluffs(val roles: List<RoleId>) : StorytellerDecision
}

enum class StorytellerDecisionKind {
    RED_HERRING,
    DRUNK_SHOWN_ROLE,
    DRUNK_INVESTIGATOR_INFO,
    DEMON_BLUFFS,
}

fun StorytellerDecision.kind(): StorytellerDecisionKind = when (this) {
    is StorytellerDecision.RedHerring -> StorytellerDecisionKind.RED_HERRING
    is StorytellerDecision.DrunkShownRole -> StorytellerDecisionKind.DRUNK_SHOWN_ROLE
    is StorytellerDecision.DrunkInvestigatorInfo -> StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO
    is StorytellerDecision.DemonBluffs -> StorytellerDecisionKind.DEMON_BLUFFS
}

data class CandidatePlan(
    val decisions: List<StorytellerDecision>,
) {
    inline fun <reified T : StorytellerDecision> decision(): T? = decisions.filterIsInstance<T>().singleOrNull()
}

enum class QualityTier {
    RECOMMENDED,
    ACCEPTABLE_WITH_WARNING,
    EXPERT_ONLY,
    REJECTED,
}

enum class RecommendationStyle {
    GENTLE,
    BALANCED,
    AGGRESSIVE,
}

enum class ScoreCategory {
    EXPOSURE,
    CONFIRMATION,
    CONTRADICTION,
    BLUFF_SUPPORT,
    BEGINNER_SAFETY,
    EVIL_PRESSURE,
    ROLE_SUITABILITY,
    DIVERSITY,
}

data class ScoreItem(
    val ruleId: String,
    val category: ScoreCategory,
    val delta: Int,
    val messageKey: String,
    val affectedSeats: List<Int> = emptyList(),
)

data class PlanWarning(
    val ruleId: String,
    val messageKey: String,
    val affectedSeats: List<Int> = emptyList(),
)

data class PlanEffectSignature(
    val redHerringSeat: Int? = null,
    val drunkShownRole: RoleId? = null,
    val drunkInvestigatorShownMinion: RoleId? = null,
    val suspectedSeats: Set<Int> = emptySet(),
    val demonBluffs: Set<RoleId> = emptySet(),
)

data class RecommendationPlan(
    val decisions: List<StorytellerDecision>,
    val observations: List<AbilityObservation>,
    val qualityTier: QualityTier,
    val style: RecommendationStyle,
    val totalScore: Int,
    val scoreItems: List<ScoreItem>,
    val warnings: List<PlanWarning>,
    val effectSignature: PlanEffectSignature,
)
