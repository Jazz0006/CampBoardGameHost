package com.codex.campboardgamehost.clocktower.rules

/** Separates a malfunctioning information ability from special-character registration rulings. */
internal object RegistrationInteractionRules {
    fun effectiveRegistrationKey(key: String?, informationAbilityReliable: Boolean): String? =
        key.takeIf { informationAbilityReliable }
}
