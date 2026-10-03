package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext

internal data class FunctioningLibrarianV2ProductionSelection(
    val candidateId: String,
    val selectionMode: BeginnerConservativeV2SelectionMode,
    val reasonCodes: Set<PolicyReasonCode> = emptySet(),
) {
    init {
        require(candidateId.isNotBlank()) { "Functioning Librarian production selection requires a candidate ID." }
    }
}

/**
 * Narrow production fast path for C5-E.
 *
 * This selector deliberately does not own legality, manual authority, publication, exact-world
 * evaluation or UI presentation. It is admissible only on the Trouble Brewing first-night,
 * functioning/reliable Librarian surface where PairInformationLegalDomain is truth-only.
 *
 * C5-D exact replay remains the acceptance oracle for the fast path.
 */
internal object FunctioningLibrarianV2ProductionSelector {
    private val troubleBrewing = ScriptId("trouble_brewing")
    private val librarian = RoleId("Librarian")
    private val admittedReason =
        PairInformationFutureFlexibilityReasonCodes
            .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES

    fun select(
        context: TroubleBrewingFirstNightPairDecisionContext,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        decisionId: String,
        selectionSeed: Long,
    ): FunctioningLibrarianV2ProductionSelection? {
        if (context.snapshot.script != troubleBrewing) return null
        if (abilityRole != librarian) return null
        if (reliability != ReliabilityState.RELIABLE) return null
        if (sourceSeat <= 0) return null
        if (decisionId.isBlank()) return null

        val legalCandidates = PairInformationLegalDomain.generate(
            game = context.naturalPairGameState,
            roleDefinitions = context.roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
        if (legalCandidates.isEmpty()) return null
        if (legalCandidates.any { candidate -> candidate.semanticTruth != SemanticTruth.TRUE }) return null

        val futureByCandidateId = PairInformationFutureFlexibilityProjector.project(
            game = context.naturalPairGameState,
            legalCandidates = legalCandidates,
        )
        if (futureByCandidateId.keys != legalCandidates.mapTo(linkedSetOf()) { it.candidateId }) return null

        val preferredIds = mutableListOf<String>()
        for (candidate in legalCandidates) {
            when (val future = futureByCandidateId[candidate.candidateId]) {
                is FeatureProjection.Projected -> {
                    if (admittedReason in future.value.reasonCodes) {
                        preferredIds += candidate.candidateId
                    }
                }

                is FeatureProjection.Unavailable -> return null
                null -> return null
            }
        }

        val legalIds = legalCandidates.map { it.candidateId }
        val strictPreferredSubset =
            preferredIds.isNotEmpty() && preferredIds.size < legalIds.size
        val selectionMode = if (strictPreferredSubset) {
            BeginnerConservativeV2SelectionMode.PREFERRED_BAND
        } else {
            BeginnerConservativeV2SelectionMode.V1_FALLBACK
        }
        val selectionIds = if (strictPreferredSubset) preferredIds else legalIds
        val hashVersion = if (strictPreferredSubset) {
            PolicyVersions.BEGINNER_CONSERVATIVE_V2
        } else {
            PolicyVersions.BEGINNER_CONSERVATIVE_V1
        }

        val selectedId = PolicySeededHashSelector.select(
            candidateIds = selectionIds,
            decisionId = decisionId,
            selectionSeed = selectionSeed,
            hashPolicyVersion = hashVersion,
        )
        return FunctioningLibrarianV2ProductionSelection(
            candidateId = selectedId,
            selectionMode = selectionMode,
            reasonCodes = if (strictPreferredSubset) {
                setOf(
                    BeginnerConservativeV2PolicyReasons
                        .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
                )
            } else {
                emptySet()
            },
        )
    }
}
