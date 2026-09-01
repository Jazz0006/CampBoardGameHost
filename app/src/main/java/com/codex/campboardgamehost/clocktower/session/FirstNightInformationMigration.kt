package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool

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
    val qualityTier: QualityTier = QualityTier.RECOMMENDED,
    val rankFixedPoint: Long = 0L,
    val legality: UnifiedCandidateLegality = UnifiedCandidateLegality.LEGAL,
    val epistemicStatus: UnifiedEpistemicStatus = UnifiedEpistemicStatus.VERIFIED,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
) {
    init {
        require(id.isNotBlank())
    }
}

private fun FirstNightInformationCandidate.toUnified(family: FirstNightInformationFamily) = UnifiedSelectionCandidate(
    candidateId = id,
    familyId = family.name.lowercase(),
    legality = legality,
    epistemicStatus = epistemicStatus,
    qualityTier = qualityTier,
    rankFixedPoint = rankFixedPoint,
    reasonCodes = reasonCodes,
    warningCodes = warningCodes,
    payload = observation,
)

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
        val legacyPool = UnifiedSelectionPool(request.legacyCandidates.map { it.toUnified(request.family) })
        val migratedPool = UnifiedSelectionPool(request.migratedCandidates.map { it.toUnified(request.family) })
        val legacyParity = legacyPool.paritySignature()
        val migratedParity = migratedPool.paritySignature()
        val legacy = legacyParity.mapTo(sortedSetOf()) { it.candidateId }
        val migrated = migratedParity.mapTo(sortedSetOf()) { it.candidateId }
        return if (legacyParity == migratedParity) FirstNightShadowResult.Ready(migrated)
        else FirstNightShadowResult.Mismatch(legacy - migrated, migrated - legacy)
    }

    /** A mismatch deliberately leaves the decision unavailable; callers must retain the legacy path. */
    fun publishIfShadowMatches(request: FirstNightInformationRequest): FirstNightInformationMigration = when (shadow(request)) {
        is FirstNightShadowResult.Mismatch -> this
        is FirstNightShadowResult.Ready -> publish(request)
    }

    /**
     * UX-R2B cutover for pair-information families whose shared legal domain is now authoritative.
     *
     * Legacy candidates remain available to [shadow] for telemetry, but parity is no longer a
     * publication gate because the complete legal Manual domain is intentionally broader than the
     * historical curated presentation pool. Non-pair families must keep using
     * [publishIfShadowMatches].
     */
    fun publishAuthoritativePairDomain(request: FirstNightInformationRequest): FirstNightInformationMigration {
        require(request.family.usesAuthoritativePairDomain()) {
            "Authoritative pair-domain publication is limited to Washerwoman/Librarian/Investigator."
        }
        return publish(request)
    }

    private fun publish(request: FirstNightInformationRequest): FirstNightInformationMigration = copy(
        lifecycle = lifecycle.publish(request.decisionId),
        candidatesByDecisionId = candidatesByDecisionId + (
            request.decisionId to request.migratedCandidates.associateBy(FirstNightInformationCandidate::id)
        ),
    )

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
