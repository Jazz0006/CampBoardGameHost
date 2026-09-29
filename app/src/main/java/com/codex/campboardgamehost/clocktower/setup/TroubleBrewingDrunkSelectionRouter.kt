package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode

internal data class TroubleBrewingDrunkSelectionRequest(
    val candidates: List<TroubleBrewingDrunkCandidate>,
    val recommendedCandidate: TroubleBrewingDrunkCandidate?,
) {
    init {
        require(candidates.isNotEmpty()) {
            "Trouble Brewing manual Drunk selection requires at least one legal candidate."
        }
        require(candidates.distinct().size == candidates.size) {
            "Trouble Brewing manual Drunk candidates must be unique."
        }
        require(recommendedCandidate == null || recommendedCandidate in candidates) {
            "Trouble Brewing manual Drunk recommendation must belong to the current legal candidate domain."
        }
    }
}

internal sealed interface TroubleBrewingDrunkSelectionRoute {
    data object NoSelectionNeeded : TroubleBrewingDrunkSelectionRoute

    data class ManualSelection(
        val request: TroubleBrewingDrunkSelectionRequest,
    ) : TroubleBrewingDrunkSelectionRoute

    data class CompatibilityImmediate(
        val candidate: TroubleBrewingDrunkCandidate,
    ) : TroubleBrewingDrunkSelectionRoute
}

/**
 * DLB-4A UX router.
 *
 * Rules/setup owns legality, the App owns only the transient interaction lifetime, and the canonical
 * commit owner still revalidates the final candidate. No SDE policy is invoked here.
 */
internal object TroubleBrewingDrunkSelectionRouter {
    fun route(
        preparedSetup: TroubleBrewingPreparedSetup,
        experienceMode: StorytellerExperienceMode,
        recommendedCandidate: TroubleBrewingDrunkCandidate?,
    ): TroubleBrewingDrunkSelectionRoute {
        val intermediate = preparedSetup.intermediateSetup
        val legalCandidates = TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)

        if (!intermediate.visibleRoster.hasDrunk) {
            require(legalCandidates.isEmpty()) {
                "Trouble Brewing setup without Drunk cannot expose Drunk candidates."
            }
            require(recommendedCandidate == null) {
                "Trouble Brewing setup without Drunk cannot carry a Drunk recommendation."
            }
            require(preparedSetup.compatibilityConfirmedDrunkCandidate == null) {
                "Trouble Brewing setup without Drunk cannot carry a compatibility Drunk confirmation."
            }
            return TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded
        }

        require(legalCandidates.isNotEmpty()) {
            "Trouble Brewing Drunk setup requires a non-empty legal candidate domain."
        }

        return when (experienceMode) {
            StorytellerExperienceMode.EXPERIENCED -> {
                TroubleBrewingDrunkSelectionRoute.ManualSelection(
                    TroubleBrewingDrunkSelectionRequest(
                        candidates = legalCandidates,
                        recommendedCandidate = recommendedCandidate,
                    ),
                )
            }

            StorytellerExperienceMode.BEGINNER -> {
                require(recommendedCandidate == null) {
                    "Beginner Drunk selection cannot consume a recommendation before the production cutover gate."
                }
                val compatibilityCandidate =
                    requireNotNull(preparedSetup.compatibilityConfirmedDrunkCandidate) {
                        "Beginner compatibility Drunk selection requires the transitional confirmed candidate."
                    }
                val currentLegalCandidate =
                    legalCandidates.singleOrNull { candidate -> candidate == compatibilityCandidate }
                requireNotNull(currentLegalCandidate) {
                    "Beginner compatibility Drunk selection must resolve to the current legal candidate domain."
                }
                TroubleBrewingDrunkSelectionRoute.CompatibilityImmediate(currentLegalCandidate)
            }
        }
    }
}
