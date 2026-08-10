package com.codex.campboardgamehost.clocktower.domain

sealed interface SetupClueOutcome {
    data class DrunkShownRole(
        val shownRole: RoleId,
        val investigatorInformation: StorytellerDecision.DrunkInvestigatorInfo? = null,
    ) : SetupClueOutcome

    data class PairInformation(
        val abilityRole: RoleId,
        val information: PairInformationOutcome,
    ) : SetupClueOutcome

    data class RedHerring(val seat: Int) : SetupClueOutcome

    data class DemonBluffs(val roles: List<RoleId>) : SetupClueOutcome

    data class FullPlan(val decisions: List<StorytellerDecision>) : SetupClueOutcome
}
