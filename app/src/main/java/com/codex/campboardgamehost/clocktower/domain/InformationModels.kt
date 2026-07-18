package com.codex.campboardgamehost.clocktower.domain

enum class ReliabilityState {
    RELIABLE,
    DRUNK,
    POISONED,
}

enum class SemanticTruth {
    TRUE,
    FALSE,
    PARTIALLY_TRUE,
    NOT_APPLICABLE,
}

enum class YesNoAnswer {
    YES,
    NO,
}

enum class RegistrationReason {
    SPY_ABILITY,
    RECLUSE_ABILITY,
    OTHER,
}

data class RegistrationDecision(
    val playerSeat: Int,
    val affectedAbility: RoleId,
    val registeredAlignment: Alignment? = null,
    val registeredType: CharacterType? = null,
    val registeredRole: RoleId? = null,
    val reason: RegistrationReason,
)

data class AbilityObservation(
    val sourceSeat: Int,
    val perceivedRole: RoleId,
    val shownRole: RoleId? = null,
    val candidateSeats: List<Int> = emptyList(),
    val shownNumber: Int? = null,
    val shownAnswer: YesNoAnswer? = null,
    val reliability: ReliabilityState,
    val semanticTruth: SemanticTruth,
    val registrations: List<RegistrationDecision> = emptyList(),
)
