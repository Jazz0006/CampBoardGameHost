package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRecurringPlayerInformationRouteDomain

internal object PairInformationFutureFlexibilityReasonCodes {
    const val PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES =
        "preserves-competing-recurring-information-routes"
}

/**
 * Score-free future-flexibility projection for already-legal pair-information candidates.
 *
 * This projector only describes whether the two visible candidate seats preserve multiple
 * recurring player-information routes. It does not own pair legality, truth, registration,
 * recommendation ordering, weights, thresholds or selection.
 *
 * Route IDs are seat-scoped rather than role-named so downstream policy can consume the generic
 * feature without encoding Undertaker/Empath/Fortune-Teller branches.
 */
internal object PairInformationFutureFlexibilityProjector {
    private val troubleBrewing = ScriptId("trouble_brewing")

    fun project(
        game: GameState,
        legalCandidates: List<PairInformationLegalCandidate>,
    ): Map<String, FeatureProjection<FutureFlexibilityFeatures>> {
        require(legalCandidates.map(PairInformationLegalCandidate::candidateId).distinct().size == legalCandidates.size) {
            "Pair future-flexibility candidate IDs must be unique."
        }

        return legalCandidates.associate { candidate ->
            candidate.candidateId to projectCandidate(
                game = game,
                candidate = candidate,
            )
        }
    }

    private fun projectCandidate(
        game: GameState,
        candidate: PairInformationLegalCandidate,
    ): FeatureProjection<FutureFlexibilityFeatures> {
        if (game.script != troubleBrewing || candidate.semanticTruth != SemanticTruth.TRUE) {
            return FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE)
        }
        if (candidate.outcome.candidateSeats.isEmpty()) {
            return FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE)
        }

        val candidatePlayers = candidate.outcome.candidateSeats.map { seat ->
            game.playerAt(seat)
                ?: return FeatureProjection.Unavailable(FeatureUnavailableReason.MISSING_CAPABILITY)
        }
        if (candidatePlayers.any { player -> player.shownRole == null }) {
            return FeatureProjection.Unavailable(FeatureUnavailableReason.MISSING_CAPABILITY)
        }

        val retainedRouteIds = candidatePlayers
            .asSequence()
            .filter { player ->
                TroubleBrewingRecurringPlayerInformationRouteDomain.capabilityFor(player.shownRole) != null
            }
            .map { player -> recurringRouteId(player.seat) }
            .toCollection(linkedSetOf())

        val reasonCodes = if (retainedRouteIds.size >= 2) {
            setOf(
                PairInformationFutureFlexibilityReasonCodes
                    .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
            )
        } else {
            emptySet()
        }

        return FeatureProjection.Projected(
            FutureFlexibilityFeatures(
                retainedRouteIds = retainedRouteIds,
                lostRouteIds = emptySet(),
                reasonCodes = reasonCodes,
            ),
        )
    }

    private fun recurringRouteId(seat: Int): String =
        "recurring-player-information:seat-$seat"
}
