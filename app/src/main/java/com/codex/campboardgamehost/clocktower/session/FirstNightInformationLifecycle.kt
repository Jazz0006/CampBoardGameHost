package com.codex.campboardgamehost.clocktower.session

/** Keeps player-visible information immutable while invalidating only drafts. */
internal data class FirstNightInformationLifecycle(
    val generation: Long = 0,
    val readyDecisionIds: Set<String> = emptySet(),
    val displayedDecisionIds: Set<String> = emptySet(),
) {
    fun publish(id: String): FirstNightInformationLifecycle =
        if (id in displayedDecisionIds) this else copy(readyDecisionIds = readyDecisionIds + id)

    fun display(id: String): FirstNightInformationLifecycle {
        require(id in readyDecisionIds) { "Only a ready decision can be displayed." }
        return copy(readyDecisionIds = readyDecisionIds - id, displayedDecisionIds = displayedDecisionIds + id)
    }

    fun invalidateUnshown(): FirstNightInformationLifecycle = copy(
        generation = generation + 1,
        readyDecisionIds = emptySet(),
    )
}
