package com.codex.campboardgamehost.clocktower.rules

internal enum class AbilityFunctioningState {
    FUNCTIONING,
    DRUNK,
    POISONED,
}

internal data class AbilitySubject(
    val actualRole: String?,
    val shownRole: String?,
    val isPoisoned: Boolean,
    val isAlive: Boolean,
)

internal data class OneShotAbilityDecision(
    val state: AbilityFunctioningState?,
    val mayAttempt: Boolean,
    val consumesUse: Boolean,
    val effectApplies: Boolean,
)

/** Keeps simulated character identity separate from canonical ability effects. */
internal object AbilityFunctioningSemantics {
    fun perceivedRole(subject: AbilitySubject): String? =
        if (subject.actualRole == "Drunk") subject.shownRole else subject.actualRole

    fun interactsAs(subject: AbilitySubject, role: String): Boolean =
        subject.isAlive && perceivedRole(subject) == role

    fun stateFor(subject: AbilitySubject, role: String): AbilityFunctioningState? {
        if (!interactsAs(subject, role)) return null
        return when {
            subject.isPoisoned -> AbilityFunctioningState.POISONED
            subject.actualRole == "Drunk" -> AbilityFunctioningState.DRUNK
            else -> AbilityFunctioningState.FUNCTIONING
        }
    }

    fun functionsAs(subject: AbilitySubject, role: String): Boolean =
        stateFor(subject, role) == AbilityFunctioningState.FUNCTIONING

    fun selectedMechanicalEffectApplies(
        subject: AbilitySubject?,
        role: String,
        selectionMatches: Boolean,
    ): Boolean = selectionMatches && subject != null && functionsAs(subject, role)

    fun oneShotDecision(
        subject: AbilitySubject?,
        role: String,
        alreadyUsed: Boolean,
    ): OneShotAbilityDecision {
        val state = subject?.let { stateFor(it, role) }
        val mayAttempt = state != null && !alreadyUsed
        return OneShotAbilityDecision(
            state = state,
            mayAttempt = mayAttempt,
            consumesUse = mayAttempt,
            effectApplies = mayAttempt && state == AbilityFunctioningState.FUNCTIONING,
        )
    }
}
