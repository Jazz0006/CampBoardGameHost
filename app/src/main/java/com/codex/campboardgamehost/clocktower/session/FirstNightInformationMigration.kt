package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Batch 4's common boundary for first-night information.
 *
 * The legacy UI can continue to build candidates role-by-role while this
 * coordinator verifies that the new candidate set is identical before a
 * decision becomes available.  It intentionally stores only the selected
 * observation after display: generated candidates are never game facts.
 */
internal enum class FirstNightInformationFamily(val role: RoleId) {
    WASHERWOMAN(RoleId("Washerwoman")),
    LIBRARIAN(RoleId("Librarian")),
    INVESTIGATOR(RoleId("Investigator")),
    CHEF(RoleId("Chef")),
    EMPATH(RoleId("Empath")),
    FORTUNE_TELLER(RoleId("Fortune Teller")),
}

internal data class FirstNightInformationCandidate(
    val id: String,
    val observation: AbilityObservation,
) {
    init {
        require(id.isNotBlank())
    }
}

internal data class FirstNightInformationRequest(
    val decisionId: String,
    val family: FirstNightInformationFamily,
    val sourceSeat: Int,
    val reliability: ReliabilityState,
    val selectedCandidateId: String,
    val legacyCandidates: List<FirstNightInformationCandidate>,
    val migratedCandidates: List<FirstNightInformationCandidate>,
) {
    init {
        require(decisionId.isNotBlank() && sourceSeat > 0 && selectedCandidateId.isNotBlank())
        require(legacyCandidates.map(FirstNightInformationCandidate::id).distinct().size == legacyCandidates.size)
        require(migratedCandidates.map(FirstNightInformationCandidate::id).distinct().size == migratedCandidates.size)
        require((legacyCandidates + migratedCandidates).all {
            it.observation.sourceSeat == sourceSeat && it.observation.perceivedRole == family.role &&
                it.observation.reliability == reliability
        })
        require(selectedCandidateId in migratedCandidates.map(FirstNightInformationCandidate::id))
    }
}

internal sealed interface FirstNightShadowResult {
    data class Ready(val candidateIds: Set<String>) : FirstNightShadowResult
    data class Mismatch(
        val legacyOnly: Set<String>,
        val migratedOnly: Set<String>,
    ) : FirstNightShadowResult
}

/** One coordinator is shared by all first-night information families. */
internal data class FirstNightInformationMigration(
    private val lifecycle: FirstNightInformationLifecycle = FirstNightInformationLifecycle(),
    private val candidatesByDecisionId: Map<String, Map<String, FirstNightInformationCandidate>> = emptyMap(),
    private val displayedObservations: Map<String, AbilityObservation> = emptyMap(),
) {
    fun shadow(request: FirstNightInformationRequest): FirstNightShadowResult {
        val legacy = request.legacyCandidates.mapTo(sortedSetOf(), FirstNightInformationCandidate::id)
        val migrated = request.migratedCandidates.mapTo(sortedSetOf(), FirstNightInformationCandidate::id)
        return if (legacy == migrated) FirstNightShadowResult.Ready(migrated)
        else FirstNightShadowResult.Mismatch(legacy - migrated, migrated - legacy)
    }

    /** A mismatch deliberately leaves the decision unavailable; callers must retain the legacy path. */
    fun publishIfShadowMatches(request: FirstNightInformationRequest): FirstNightInformationMigration = when (shadow(request)) {
        is FirstNightShadowResult.Mismatch -> this
        is FirstNightShadowResult.Ready -> copy(
            lifecycle = lifecycle.publish(request.decisionId),
            candidatesByDecisionId = candidatesByDecisionId + (
                request.decisionId to request.migratedCandidates.associateBy(FirstNightInformationCandidate::id)
            ),
        )
    }

    /** Display is the sole commit boundary: it creates exactly one typed observation. */
    fun display(decisionId: String, candidateId: String): FirstNightInformationMigration {
        if (decisionId in displayedObservations) return this
        val candidate = candidatesByDecisionId[decisionId]?.get(candidateId)
            ?: throw IllegalArgumentException("Candidate $candidateId is not ready for $decisionId.")
        return copy(
            lifecycle = lifecycle.display(decisionId),
            displayedObservations = displayedObservations + (decisionId to candidate.observation),
        )
    }

    /** Poison target changes (draft or confirmed) retain displayed facts and discard all drafts. */
    fun invalidateUnshown(): FirstNightInformationMigration = copy(
        lifecycle = lifecycle.invalidateUnshown(),
        candidatesByDecisionId = candidatesByDecisionId.filterKeys { it in lifecycle.displayedDecisionIds },
    )

    fun displayedObservation(decisionId: String): AbilityObservation? = displayedObservations[decisionId]
    fun isDisplayed(decisionId: String): Boolean = decisionId in displayedObservations
    fun isReady(decisionId: String): Boolean = decisionId in lifecycle.readyDecisionIds
    fun generation(): Long = lifecycle.generation
}
