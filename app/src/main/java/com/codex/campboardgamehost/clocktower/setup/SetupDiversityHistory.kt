package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId

/**
 * Persistence-independent record of one previously committed actual-role composition.
 *
 * Shown identities, recommendation outputs and script-specific presentation metadata deliberately
 * do not belong in generic setup-diversity history.
 */
internal class SetupDiversityRecord(
    val script: ScriptId,
    actualRoles: List<RoleId>,
) {
    val actualRoles: List<RoleId> = actualRoles.sortedBy(RoleId::value)

    val playerCount: Int
        get() = actualRoles.size

    init {
        require(this.actualRoles.isNotEmpty()) {
            "Setup diversity record must contain at least one actual role."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is SetupDiversityRecord &&
            script == other.script &&
            actualRoles == other.actualRoles

    override fun hashCode(): Int = 31 * script.hashCode() + actualRoles.hashCode()

    override fun toString(): String =
        "SetupDiversityRecord(script=$script, actualRoles=$actualRoles)"
}

/** Newest-first actual-composition history consumed only by setup diversity selection. */
internal class SetupDiversityHistory(
    recentSetups: List<SetupDiversityRecord>,
) {
    val recentSetups: List<SetupDiversityRecord> = recentSetups.toList()

    companion object {
        val EMPTY = SetupDiversityHistory(emptyList())
    }
}
