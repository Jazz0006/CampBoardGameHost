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

/** Keeps simulated character identity separate from canonical ability effects. */
internal object AbilityFunctioningSemantics {
    fun perceivedRole(subject: AbilitySubject): String? =
        if (subject.actualRole == "Drunk") subject.shownRole else subject.actualRole

    fun interactsAs(subject: AbilitySubject, role: String): Boolean =
        subject.isAlive && perceivedRole(subject) == role

    /**
     * Resolves functioning state after another authority has already established that this
     * interaction occurs. This deliberately does not require the subject to be alive: death-trigger
     * information may resolve after a death fact while poison from an earlier action still applies
     * to that interaction.
     */
    fun stateForEstablishedInteraction(
        subject: AbilitySubject,
        role: String,
    ): AbilityFunctioningState? {
        if (perceivedRole(subject) != role) return null
        return when {
            subject.isPoisoned -> AbilityFunctioningState.POISONED
            subject.actualRole == "Drunk" -> AbilityFunctioningState.DRUNK
            else -> AbilityFunctioningState.FUNCTIONING
        }
    }

    fun stateFor(subject: AbilitySubject, role: String): AbilityFunctioningState? {
        if (!subject.isAlive) return null
        return stateForEstablishedInteraction(subject, role)
    }

    fun functionsAs(subject: AbilitySubject, role: String): Boolean =
        stateFor(subject, role) == AbilityFunctioningState.FUNCTIONING

}
