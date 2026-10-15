package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Supported first-night information publication families, not recommendation policies. */
internal enum class FirstNightInformationFamily(val role: RoleId) {
    WASHERWOMAN(RoleId("Washerwoman")),
    LIBRARIAN(RoleId("Librarian")),
    INVESTIGATOR(RoleId("Investigator")),
    CHEF(RoleId("Chef")),
    EMPATH(RoleId("Empath")),
    FORTUNE_TELLER(RoleId("Fortune Teller")),
}

private val authoritativePairInformationFamilies = setOf(
    FirstNightInformationFamily.WASHERWOMAN,
    FirstNightInformationFamily.LIBRARIAN,
    FirstNightInformationFamily.INVESTIGATOR,
)

internal fun FirstNightInformationFamily.usesAuthoritativePairDomain(): Boolean =
    this in authoritativePairInformationFamilies

internal data class FirstNightInformationCandidate(
    val id: String,
    val observation: AbilityObservation,
) {
    init { require(id.isNotBlank()) }
}

/**
 * One selected public result and its Host-verified publication domain.
 * No parallel legacy/Shadow candidate pool, quality score or recommendation preference.
 */
internal data class FirstNightInformationRequest(
    val decisionId: String,
    val family: FirstNightInformationFamily,
    val sourceSeat: Int,
    val reliability: ReliabilityState,
    val selectedCandidateId: String,
    val candidates: List<FirstNightInformationCandidate>,
) {
    init {
        require(decisionId.isNotBlank() && sourceSeat > 0 && selectedCandidateId.isNotBlank())
        require(candidates.map(FirstNightInformationCandidate::id).distinct().size == candidates.size)
        require(candidates.any { it.id == selectedCandidateId }) {
            "Selected first-night information must belong to the current Host publication domain."
        }
        require(candidates.all {
            it.observation.sourceSeat == sourceSeat &&
                it.observation.perceivedRole == family.role &&
                it.observation.reliability == reliability
        })
    }
}

/**
 * Ephemeral UI reveal gate only; it retains decision identifiers, never a second copy
 * of actual observations, roles, or choices. Session remains the factual history owner.
 */
internal data class FirstNightInformationMigration(
    private val lifecycle: FirstNightInformationLifecycle = FirstNightInformationLifecycle(),
) {
    fun resolvePublication(request: FirstNightInformationRequest): FirstNightPublicationResolution {
        if (isDisplayed(request.decisionId)) return FirstNightPublicationResolution.AlreadyDisplayed
        val nextLifecycle = lifecycle.publish(request.decisionId).display(request.decisionId)
        return FirstNightPublicationResolution.Published(copy(lifecycle = nextLifecycle))
    }

    /** Invalidates only transient unshown decisions and never rewrites revealed decisions. */
    fun invalidateUnshown(): FirstNightInformationMigration =
        copy(lifecycle = lifecycle.invalidateUnshown())

    fun isDisplayed(decisionId: String): Boolean = decisionId in lifecycle.displayedDecisionIds
    fun isReady(decisionId: String): Boolean = decisionId in lifecycle.readyDecisionIds
    fun generation(): Long = lifecycle.generation
}

internal sealed interface FirstNightPublicationResolution {
    data class Published(val migration: FirstNightInformationMigration) : FirstNightPublicationResolution
    data object AlreadyDisplayed : FirstNightPublicationResolution
}
