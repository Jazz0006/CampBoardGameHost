package com.codex.campboardgamehost.clocktower.setup

internal data class TroubleBrewingDrunkSelectionRequest(
    val candidates: List<TroubleBrewingDrunkCandidate>,
    val recommendedCandidate: TroubleBrewingDrunkCandidate?,
) {
    init {
        require(candidates.isNotEmpty()) { "Drunk selection requires legal candidates." }
        require(candidates.distinct().size == candidates.size) { "Drunk candidates must be unique." }
        require(recommendedCandidate == null || recommendedCandidate in candidates) {
            "Drunk recommendation must belong to the current Host legal domain."
        }
    }
}

internal sealed interface TroubleBrewingDrunkSelectionRoute {
    data object NoSelectionNeeded : TroubleBrewingDrunkSelectionRoute
    /** Unique Host-legal outcome, not an AI or experience-mode decision. */
    data class RuleDetermined(val candidate: TroubleBrewingDrunkCandidate) : TroubleBrewingDrunkSelectionRoute
    data class ManualSelection(val request: TroubleBrewingDrunkSelectionRequest) : TroubleBrewingDrunkSelectionRoute
}

/**
 * Host legality owns routing, not the retired experience-level toggle.
 * Multi-choice Drunk assignment always needs an explicitly validated Host confirmation.
 */
internal object TroubleBrewingDrunkSelectionRouter {
    fun route(
        preparedSetup: TroubleBrewingPreparedSetup,
        recommendedCandidate: TroubleBrewingDrunkCandidate?,
    ): TroubleBrewingDrunkSelectionRoute {
        val intermediate = preparedSetup.intermediateSetup
        val legalCandidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)
        if (!intermediate.visibleRoster.hasDrunk) {
            require(legalCandidates.isEmpty()) { "No-Drunk setup cannot expose Drunk candidates." }
            require(recommendedCandidate == null) { "No-Drunk setup cannot carry a Drunk recommendation." }
            return TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded
        }
        require(legalCandidates.isNotEmpty()) { "Drunk setup requires a legal candidate." }
        require(recommendedCandidate == null || recommendedCandidate in legalCandidates) {
            "Drunk recommendation must belong to the current Host legal domain."
        }
        return legalCandidates.singleOrNull()?.let(TroubleBrewingDrunkSelectionRoute::RuleDetermined)
            ?: TroubleBrewingDrunkSelectionRoute.ManualSelection(
                TroubleBrewingDrunkSelectionRequest(legalCandidates, recommendedCandidate),
            )
    }
}
