package com.codex.campboardgamehost.clocktower.domain

sealed interface StorytellerDecision {
    data class RedHerring(val seat: Int) : StorytellerDecision

    data class DrunkInvestigatorInfo(
        val shownMinion: RoleId,
        val candidateSeats: List<Int>,
    ) : StorytellerDecision

    data class DemonBluffs(val roles: List<RoleId>) : StorytellerDecision
}

enum class StorytellerDecisionKind {
    RED_HERRING,
    DRUNK_INVESTIGATOR_INFO,
    DEMON_BLUFFS,
}

fun StorytellerDecision.kind(): StorytellerDecisionKind = when (this) {
    is StorytellerDecision.RedHerring -> StorytellerDecisionKind.RED_HERRING
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
