package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.RoleId

/** One committed setup-time shown-identity replacement before seats are materialized. */
internal data class ShownIdentityCommitment(
    val actualRole: RoleId,
    val shownRole: RoleId,
) {
    init {
        require(actualRole != shownRole) {
            "Shown-identity commitment must differ from the actual role."
        }
    }
}

/**
 * Canonical pre-seat shown-identity commitment for one selected setup composition.
 *
 * Only roles whose shown identity differs from their actual identity are stored. All other roles
 * resolve to themselves through [shownRoleFor]. Seating and PlayerState materialization remain later
 * concerns.
 */
internal class SetupShownIdentityCommitment(
    overrides: List<ShownIdentityCommitment>,
) {
    val overrides: List<ShownIdentityCommitment> = overrides.sortedBy { it.actualRole.value }
    private val shownRoleByActualRole: Map<RoleId, RoleId>

    init {
        require(this.overrides.map { it.actualRole }.distinct().size == this.overrides.size) {
            "Shown-identity commitment cannot contain duplicate actual-role overrides."
        }
        shownRoleByActualRole = this.overrides.associate { it.actualRole to it.shownRole }
    }

    fun shownRoleFor(actualRole: RoleId): RoleId = shownRoleByActualRole[actualRole] ?: actualRole

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is SetupShownIdentityCommitment && overrides == other.overrides

    override fun hashCode(): Int = overrides.hashCode()

    override fun toString(): String = "SetupShownIdentityCommitment(overrides=$overrides)"

    companion object {
        val NO_OVERRIDE = SetupShownIdentityCommitment(emptyList())
    }
}

/**
 * Pure deterministic S6B selector from S6A legal options to committed shown identities.
 *
 * This stage never changes candidate composition or option legality. Each override receives an
 * independent namespaced draw derived from the selected candidate, canonical legal options, actual
 * role and setup seed. No history, seating, recommendation state or unseeded randomness participates.
 */
internal class SetupShownIdentityCommitter {
    fun commit(
        candidate: SetupCandidate,
        policy: SetupShownIdentityPolicy,
        setupSeed: Long,
    ): SetupShownIdentityCommitment {
        if (policy.overrides.isEmpty()) return SetupShownIdentityCommitment.NO_OVERRIDE

        val actualRoles = candidate.actualRoles.toSet()
        val committed = policy.overrides.map { override ->
            require(override.actualRole in actualRoles) {
                "Shown-identity policy references actual role '${override.actualRole.value}' that is absent from the selected candidate."
            }
            require(override.actualRole !in override.legalShownRoles) {
                "Shown-identity policy cannot show '${override.actualRole.value}' as itself."
            }
            require(override.legalShownRoles.none { it in actualRoles }) {
                "Shown-identity policy contains a shown role that is already an actual in-play role."
            }

            val options = override.legalShownRoles
            val drawSeed = MurmurHash3.low64Utf8(
                seedMaterial(
                    candidate = candidate,
                    override = override,
                    setupSeed = setupSeed,
                ),
            )
            val selectedIndex = java.lang.Long.remainderUnsigned(drawSeed, options.size.toLong()).toInt()
            ShownIdentityCommitment(
                actualRole = override.actualRole,
                shownRole = options[selectedIndex],
            )
        }

        return SetupShownIdentityCommitment(committed)
    }

    private fun seedMaterial(
        candidate: SetupCandidate,
        override: ShownIdentityOverrideOptions,
        setupSeed: Long,
    ): String = buildString {
        append(NAMESPACE)
        appendField(candidate.script.value)
        appendField(candidate.provenance.sourceKind.name)
        appendField(candidate.provenance.providerId)
        appendField(candidate.provenance.candidateId.orEmpty())
        candidate.actualRoles.forEach { role -> appendField(role.value) }
        appendField(override.actualRole.value)
        override.legalShownRoles.forEach { role -> appendField(role.value) }
        appendField(setupSeed.toString())
    }

    private fun StringBuilder.appendField(value: String) {
        append('|')
        append(value.length)
        append(':')
        append(value)
    }

    private companion object {
        const val NAMESPACE = "setup-shown-identity-v1"
    }
}
