package com.codex.campboardgamehost.clocktower.recommendation.sde

/**
 * Stable identity of one usable healthy-information route.
 *
 * A route identifies provenance only. It carries no score, probability, weight or assumed amount of
 * information. Historical observations remain owned by canonical epistemic history.
 */
internal sealed interface HealthyInformationRouteRef {
    val recipientSeat: Int

    data class HistoricalObservation(
        val recordId: String,
        override val recipientSeat: Int,
    ) : HealthyInformationRouteRef {
        init {
            require(recordId.isNotBlank()) {
                "Historical healthy-information record ID cannot be blank."
            }
            require(recipientSeat > 0) {
                "Historical healthy-information recipient seat must be positive."
            }
        }
    }

    data class CurrentCandidate(
        val candidateId: String,
        override val recipientSeat: Int,
    ) : HealthyInformationRouteRef {
        init {
            require(candidateId.isNotBlank()) {
                "Current healthy-information candidate ID cannot be blank."
            }
            require(recipientSeat > 0) {
                "Current healthy-information recipient seat must be positive."
            }
        }
    }
}

internal data class HistoricalHealthyInformationRouteEvidence(
    val routeRef: HealthyInformationRouteRef.HistoricalObservation,
    val independentlyUsableBefore: Boolean,
    val relationToCurrentCandidate: ConfirmationObservationRelation?,
)

internal data class HealthyInformationCandidateEvidence(
    val candidateId: String,
    val recipientSeat: Int,
    /**
     * Whether the committed exact-history baseline is still feasible before this candidate.
     *
     * A collapsed baseline cannot supply a currently usable route and must not be blamed on the
     * next candidate.
     */
    val historyFeasibleBefore: Boolean,
    /**
     * Whether the exact whole-history bundle remains feasible after applying this candidate.
     *
     * This is not a quality threshold. It is a hard semantic fact used to avoid calling a route
     * usable inside an already-impossible candidate history.
     */
    val candidateHistoryFeasibleAfter: Boolean,
    /** True only when the current output is functioning and legal actual/registered truth. */
    val currentCandidateHealthy: Boolean,
    /** Whether the current healthy output still contributes independent exact constraint. */
    val currentCandidateIndependentlyConstraining: Boolean,
    val historicalRoutes: List<HistoricalHealthyInformationRouteEvidence>,
) {
    init {
        require(candidateId.isNotBlank()) {
            "Healthy-information evidence requires a stable candidate ID."
        }
        require(recipientSeat > 0) {
            "Healthy-information recipient seat must be positive."
        }
        require(
            historicalRoutes.map { it.routeRef }.distinct().size == historicalRoutes.size,
        ) {
            "Healthy-information history may contain each route at most once."
        }
        require(!candidateHistoryFeasibleAfter || historyFeasibleBefore) {
            "A hypothetical candidate cannot restore an already-collapsed exact history."
        }
        require(!currentCandidateIndependentlyConstraining || currentCandidateHealthy) {
            "Only a healthy current candidate may create an independent healthy route."
        }
    }
}

/**
 * Score-free description of healthy-information routes surviving one candidate.
 *
 * "Healthy" means information produced by a functioning ability and legal truth to either actual
 * state or an explicitly allowed registration state. The projector tracks route survival and
 * independence only. It deliberately defines no information
 * budget, percentage floor, player-count band, role-specific value or policy ordering.
 */
internal data class HealthyInformationUtilityFeatures(
    val usableHealthyRouteRefsBefore: Set<HealthyInformationRouteRef.HistoricalObservation>,
    val independentHealthyRouteRefsBefore: Set<HealthyInformationRouteRef.HistoricalObservation>,
    val usableHealthyRouteRefsAfter: Set<HealthyInformationRouteRef>,
    val independentHealthyRouteRefsAfter: Set<HealthyInformationRouteRef>,
    val newlyRedundantHealthyRouteRefs: Set<HealthyInformationRouteRef.HistoricalObservation>,
    val contradictedHealthyRouteRefs: Set<HealthyInformationRouteRef.HistoricalObservation>,
    val currentCandidateHealthyRouteRef: HealthyInformationRouteRef.CurrentCandidate?,
) {
    init {
        require(independentHealthyRouteRefsBefore.all { it in usableHealthyRouteRefsBefore }) {
            "Independent pre-candidate healthy routes must be usable."
        }
        require(independentHealthyRouteRefsAfter.all { it in usableHealthyRouteRefsAfter }) {
            "Independent healthy routes must remain usable."
        }
        require(newlyRedundantHealthyRouteRefs.intersect(contradictedHealthyRouteRefs).isEmpty()) {
            "A historical healthy route cannot be both redundant and contradicted."
        }
        require(contradictedHealthyRouteRefs.none { it in usableHealthyRouteRefsAfter }) {
            "A contradicted healthy route cannot remain usable."
        }
        require(
            currentCandidateHealthyRouteRef == null ||
                currentCandidateHealthyRouteRef in usableHealthyRouteRefsAfter,
        ) {
            "A projected current healthy route must remain usable."
        }
    }

    val hasAnyUsableHealthyRouteAfter: Boolean
        get() = usableHealthyRouteRefsAfter.isNotEmpty()

    val hasAnyIndependentHealthyRouteAfter: Boolean
        get() = independentHealthyRouteRefsAfter.isNotEmpty()

    val lostHealthyRouteRefs: Set<HealthyInformationRouteRef.HistoricalObservation>
        get() = usableHealthyRouteRefsBefore
            .filterNotTo(linkedSetOf()) { it in usableHealthyRouteRefsAfter }

    val removesLastUsableHealthyRoute: Boolean
        get() =
            usableHealthyRouteRefsBefore.isNotEmpty() &&
                usableHealthyRouteRefsAfter.isEmpty()
}

internal object HealthyInformationUtilityFeaturesProjector {
    fun project(
        evidence: HealthyInformationCandidateEvidence,
    ): HealthyInformationUtilityFeatures {
        val usableBefore =
            if (evidence.historyFeasibleBefore) {
                evidence.historicalRoutes.mapTo(linkedSetOf()) { it.routeRef }
            } else {
                linkedSetOf()
            }
        val independentBefore =
            if (evidence.historyFeasibleBefore) {
                evidence.historicalRoutes
                    .filter(HistoricalHealthyInformationRouteEvidence::independentlyUsableBefore)
                    .mapTo(linkedSetOf()) { it.routeRef }
            } else {
                linkedSetOf()
            }

        if (!evidence.historyFeasibleBefore) {
            return HealthyInformationUtilityFeatures(
                usableHealthyRouteRefsBefore = emptySet(),
                independentHealthyRouteRefsBefore = emptySet(),
                usableHealthyRouteRefsAfter = emptySet(),
                independentHealthyRouteRefsAfter = emptySet(),
                newlyRedundantHealthyRouteRefs = emptySet(),
                contradictedHealthyRouteRefs = emptySet(),
                currentCandidateHealthyRouteRef = null,
            )
        }

        if (!evidence.candidateHistoryFeasibleAfter) {
            val contradicted = evidence.historicalRoutes
                .filter {
                    it.relationToCurrentCandidate ==
                        ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION
                }
                .mapTo(linkedSetOf()) { it.routeRef }
            // Whole-history infeasibility only means no route can be called usable after this
            // candidate. It does not prove every healthy route was itself the contradictory edge.
            return HealthyInformationUtilityFeatures(
                usableHealthyRouteRefsBefore = usableBefore,
                independentHealthyRouteRefsBefore = independentBefore,
                usableHealthyRouteRefsAfter = emptySet(),
                independentHealthyRouteRefsAfter = emptySet(),
                newlyRedundantHealthyRouteRefs = emptySet(),
                contradictedHealthyRouteRefs = contradicted,
                currentCandidateHealthyRouteRef = null,
            )
        }

        val contradicted = evidence.historicalRoutes
            .filter {
                it.relationToCurrentCandidate ==
                    ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION
            }
            .mapTo(linkedSetOf()) { it.routeRef }
        val redundant = evidence.historicalRoutes
            .filter {
                it.independentlyUsableBefore &&
                    it.relationToCurrentCandidate ==
                        ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION
            }
            .mapTo(linkedSetOf()) { it.routeRef }

        val usableHistorical = evidence.historicalRoutes
            .map { it.routeRef }
            .filterNotTo(linkedSetOf()) { it in contradicted }
        val independentHistorical = evidence.historicalRoutes
            .filter { route ->
                route.independentlyUsableBefore &&
                    route.routeRef !in contradicted &&
                    route.routeRef !in redundant
            }
            .mapTo(linkedSetOf()) { it.routeRef }

        val current = if (evidence.currentCandidateHealthy) {
            HealthyInformationRouteRef.CurrentCandidate(
                candidateId = evidence.candidateId,
                recipientSeat = evidence.recipientSeat,
            )
        } else {
            null
        }

        val usableAfter = buildSet<HealthyInformationRouteRef> {
            addAll(usableHistorical)
            current?.let(::add)
        }
        val independentAfter = buildSet<HealthyInformationRouteRef> {
            addAll(independentHistorical)
            if (evidence.currentCandidateIndependentlyConstraining) {
                add(requireNotNull(current))
            }
        }

        return HealthyInformationUtilityFeatures(
            usableHealthyRouteRefsBefore = usableBefore,
            independentHealthyRouteRefsBefore = independentBefore,
            usableHealthyRouteRefsAfter = usableAfter,
            independentHealthyRouteRefsAfter = independentAfter,
            newlyRedundantHealthyRouteRefs = redundant,
            contradictedHealthyRouteRefs = contradicted,
            currentCandidateHealthyRouteRef = current,
        )
    }
}
