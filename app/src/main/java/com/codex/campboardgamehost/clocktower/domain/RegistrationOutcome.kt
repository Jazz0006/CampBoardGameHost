package com.codex.campboardgamehost.clocktower.domain

data class RegistrationOutcome(
    val subjectSeat: Int,
    val registeredAlignment: Alignment,
    val registeredType: CharacterType,
    val registeredRole: RoleId,
    val usesSpecialAbility: Boolean,
) {
    init {
        require(subjectSeat > 0) { "subjectSeat must be positive." }
    }
}
