package com.codex.campboardgamehost.clocktower.domain

enum class SetupSourceKind {
    TEMPLATE,
    GENERATED,
}

data class SetupProvenance(
    val sourceKind: SetupSourceKind,
    val providerId: String,
    val candidateId: String? = null,
) {
    init {
        require(providerId.isNotBlank()) { "Setup provenance providerId cannot be blank." }
        require(candidateId == null || candidateId.isNotBlank()) {
            "Setup provenance candidateId cannot be blank when present."
        }
    }
}

data class CommittedSetupSeat(
    val seat: Int,
    val actualRole: RoleId,
    val shownRole: RoleId,
) {
    init {
        require(seat > 0) { "Setup seat numbers start at 1." }
    }
}

/**
 * Exact immutable initial Clocktower setup after all setup-time identity decisions are committed.
 *
 * This is a domain fact, not a persistence payload. Persistence schema/versioning belongs to the
 * outer persistence layer. [provenance] records where the setup came from, but the exact
 * [assignments] are the authority for actual/shown identities and must never be reconstructed from
 * provenance.
 */
class CommittedClocktowerSetup(
    val script: ScriptId,
    val setupSeed: Long,
    assignments: List<CommittedSetupSeat>,
    val provenance: SetupProvenance,
) {
    val assignments: List<CommittedSetupSeat> = assignments.toList()

    val playerCount: Int
        get() = assignments.size

    init {
        require(this.assignments.isNotEmpty()) { "Committed setup must contain at least one seat." }
        require(this.assignments.map(CommittedSetupSeat::seat) == (1..this.assignments.size).toList()) {
            "Committed setup seats must be ordered canonically from 1 through player count."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is CommittedClocktowerSetup &&
            script == other.script &&
            setupSeed == other.setupSeed &&
            assignments == other.assignments &&
            provenance == other.provenance

    override fun hashCode(): Int {
        var result = script.hashCode()
        result = 31 * result + setupSeed.hashCode()
        result = 31 * result + assignments.hashCode()
        result = 31 * result + provenance.hashCode()
        return result
    }

    override fun toString(): String =
        "CommittedClocktowerSetup(script=$script, setupSeed=$setupSeed, assignments=$assignments, provenance=$provenance)"
}
