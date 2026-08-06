package com.codex.campboardgamehost.clocktower.domain

enum class AbilityState {
    FUNCTIONING,
    MALFUNCTIONING_DRUNK,
    MALFUNCTIONING_POISONED,
}

enum class TruthRelation {
    TRUE_TO_ACTUAL_STATE,
    TRUE_TO_REGISTERED_STATE,
    FALSE_TO_ACTUAL_STATE,
    PARTIALLY_TRUE,
    NOT_APPLICABLE,
}

enum class DetectionSemantics {
    ACTUAL_ROLE,
    CHARACTER_TYPE,
    ALIGNMENT,
    SPECIFIC_MINION,
    DEMON_DETECTION,
    NUMERIC_INFORMATION,
    ABILITY_EFFECT,
}

enum class AbilityType {
    SETUP_INFORMATION,
    PAIR_INFORMATION,
    NUMERIC_INFORMATION,
    CATEGORICAL_INFORMATION,
    REGISTRATION,
    DEATH_RESOLUTION,
    CHARACTER_CHANGE,
    OTHER,
}

enum class RegistrationQuestion {
    ALIGNMENT,
    CHARACTER_TYPE,
    ROLE,
    SPECIFIC_MINION,
    DEMON,
    ABILITY_EFFECT,
}

data class RegistrationFact(
    val interactionId: String,
    val subjectSeat: Int,
    val registeredRole: RoleId? = null,
    val registeredType: CharacterType? = null,
    val registeredAlignment: Alignment? = null,
    val registrationQuestion: RegistrationQuestion,
    val reason: RegistrationReason,
) {
    init {
        require(interactionId.isNotBlank()) { "interactionId cannot be blank." }
        require(subjectSeat > 0) { "subjectSeat must be positive." }
        require(
            registeredRole != null || registeredType != null || registeredAlignment != null,
        ) { "A registration fact must contain at least one registered value." }
    }
}
