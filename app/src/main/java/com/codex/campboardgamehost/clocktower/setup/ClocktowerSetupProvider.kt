package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance

/**
 * Persistence-independent pre-seat setup composition.
 *
 * [actualRoles] is a canonical role multiset, not a seat assignment. Shown identities and seating
 * are committed by later setup stages. [provenance] records the candidate source but never replaces
 * the exact candidate facts.
 */
internal class SetupCandidate(
    val script: ScriptId,
    actualRoles: List<RoleId>,
    val provenance: SetupProvenance,
) {
    val actualRoles: List<RoleId> = actualRoles.sortedBy(RoleId::value)

    val playerCount: Int
        get() = actualRoles.size

    init {
        require(this.actualRoles.isNotEmpty()) { "Setup candidate must contain at least one actual role." }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is SetupCandidate &&
            script == other.script &&
            actualRoles == other.actualRoles &&
            provenance == other.provenance

    override fun hashCode(): Int {
        var result = script.hashCode()
        result = 31 * result + actualRoles.hashCode()
        result = 31 * result + provenance.hashCode()
        return result
    }

    override fun toString(): String =
        "SetupCandidate(script=$script, actualRoles=$actualRoles, provenance=$provenance)"
}

/** Inputs candidate sources may need before diversity selection and shown-identity commitment. */
internal data class SetupCandidateRequest(
    val script: ScriptId,
    val playerCount: Int,
    val setupSeed: Long,
) {
    init {
        require(playerCount > 0) { "Setup candidate request playerCount must be positive." }
    }
}

/**
 * Source of legal setup candidates for a request.
 *
 * Template repositories and deterministic generators are later implementations of this boundary;
 * diversity history, persistence and UI state deliberately do not belong here.
 */
internal fun interface SetupCandidateSource {
    fun candidates(request: SetupCandidateRequest): List<SetupCandidate>
}

/**
 * Script-scoped setup provider boundary.
 *
 * The provider owns source attribution and rejects cross-script/cross-provider candidates before
 * later selection stages can consume them.
 */
internal class ClocktowerSetupProvider(
    val script: ScriptId,
    val providerId: String,
    private val candidateSource: SetupCandidateSource,
) {
    init {
        require(providerId.isNotBlank()) { "Clocktower setup providerId cannot be blank." }
    }

    fun candidates(request: SetupCandidateRequest): List<SetupCandidate> {
        require(request.script == script) {
            "Clocktower setup provider '$providerId' cannot serve script '${request.script.value}'."
        }

        val candidates = candidateSource.candidates(request).toList()
        require(candidates.all { it.script == script }) {
            "Clocktower setup source returned a candidate for another script."
        }
        require(candidates.all { it.playerCount == request.playerCount }) {
            "Clocktower setup source returned a candidate with a mismatched player count."
        }
        require(candidates.all { it.provenance.providerId == providerId }) {
            "Clocktower setup source returned a candidate attributed to another provider."
        }
        return candidates
    }
}

/** Script-neutral lookup for the single setup provider registered for each script. */
internal class ClocktowerSetupProviderRegistry(
    providers: Iterable<ClocktowerSetupProvider>,
) {
    val providers: List<ClocktowerSetupProvider> = providers.toList()
    private val byScript: Map<ScriptId, ClocktowerSetupProvider>

    init {
        require(this.providers.map { it.script }.distinct().size == this.providers.size) {
            "Clocktower setup provider registry cannot contain duplicate script registrations."
        }
        byScript = this.providers.associateBy { it.script }
    }

    fun find(script: ScriptId): ClocktowerSetupProvider? = byScript[script]
}
