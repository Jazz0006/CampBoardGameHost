package com.codex.campboardgamehost.clocktower.domain

/**
 * RES-2 provider contract.
 *
 * Neutral boundary between Host orchestration and any current/future Storyteller recommendation
 * provider. It deliberately contains no recommendation/SDE implementation, policy version, score,
 * selector, or network/API concept.
 */
internal data class StorytellerProviderRequestV1(
    val schemaId: String = SCHEMA_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val identity: StorytellerProviderDecisionIdentityV1,
    val sourceRevision: StorytellerProviderRevisionV1,
    val state: StorytellerProviderGameStateV1,
    val decisionContext: StorytellerProviderDecisionContextV1,
    val legalCandidates: List<StorytellerProviderCandidateV1>,
    val gameContext: StorytellerProviderGameContextV1 = StorytellerProviderGameContextV1.EMPTY,
    val coordinationHorizon: StorytellerProviderCoordinationHorizonV1 =
        StorytellerProviderCoordinationHorizonV1.CURRENT_DECISION_ONLY,
) {
    init {
        require(schemaId == SCHEMA_ID) { "Unsupported Storyteller provider request schema ID." }
        require(schemaVersion == SCHEMA_VERSION) { "Unsupported Storyteller provider request schema version." }
        require(legalCandidates.isNotEmpty()) { "Provider request requires a non-empty legal candidate domain." }
        require(legalCandidates.map { it.candidateId }.distinct().size == legalCandidates.size) {
            "Provider request legal candidate IDs must be unique."
        }
        require(state.gameId == identity.gameId) {
            "Provider request state must belong to the decision game."
        }
        require(state.scriptId == identity.scriptId) {
            "Provider request state script must match the decision identity."
        }
        require(decisionContext.decisionTypeId == identity.decisionTypeId) {
            "Provider request decision context type must match the decision identity."
        }

        val stateSeats = state.knownSeatNumbers()
        when (val context = decisionContext) {
            StorytellerProviderDecisionContextV1.DrunkAssignment -> {
                require(legalCandidates.all { candidate ->
                    val payload = candidate.payload as? StorytellerProviderCandidatePayloadV1.DrunkAssignment
                    payload != null && payload.seat in stateSeats
                }) {
                    "Drunk provider candidates must use Drunk payloads for current-state seats."
                }
            }

            is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> {
                require(context.sourceSeat in stateSeats) {
                    "Pair-information source seat must belong to the current state."
                }
                require(legalCandidates.all { candidate ->
                    val payload = candidate.payload as? StorytellerProviderCandidatePayloadV1.PairInformation
                    payload != null &&
                        payload.candidateSeats.all { it in stateSeats } &&
                        payload.registrations.all { it.subjectSeat in stateSeats }
                }) {
                    "Pair provider candidates and registration witnesses must reference current-state seats."
                }
            }

            is StorytellerProviderDecisionContextV1.ScalarInformation -> {
                require(context.sourceSeat in stateSeats && context.subjectSeats.all { it in stateSeats }) {
                    "Scalar-information subjects must belong to the current game."
                }
                require(legalCandidates.all { candidate ->
                    val payload = candidate.payload as? StorytellerProviderCandidatePayloadV1.ScalarResult
                    payload != null && when (context.kind) {
                        StorytellerProviderScalarKindV1.NUMBER ->
                            payload.value.toIntOrNull()?.toString() == payload.value
                        StorytellerProviderScalarKindV1.BOOLEAN ->
                            payload.value == "true" || payload.value == "false"
                    }
                }) { "Scalar-information candidates must be typed legal results." }
            }

            is StorytellerProviderDecisionContextV1.DayAbilityRegistration -> {
                require(context.actorSeat in stateSeats && context.subjectSeat in stateSeats)
                require(legalCandidates.all { it.payload is StorytellerProviderCandidatePayloadV1.RegistrationChoice }) {
                    "Day ability-registration candidates must carry typed choices."
                }
            }

            is StorytellerProviderDecisionContextV1.RegistrationResolution -> {
                require(context.sourceSeat in stateSeats && context.subjectSeat in stateSeats)
                require(legalCandidates.all { candidate ->
                    candidate.payload is StorytellerProviderCandidatePayloadV1.RegistrationChoice
                }) {
                    "Registration-resolution candidates must be typed registration choices."
                }
            }

            is StorytellerProviderDecisionContextV1.MayorRedirect -> {
                require(context.mayorSeat in stateSeats) {
                    "Mayor seat must belong to the current state."
                }
                require(legalCandidates.all { candidate ->
                    val payload = candidate.payload as? StorytellerProviderCandidatePayloadV1.SeatTarget
                    payload != null && payload.seat in stateSeats
                }) {
                    "Mayor provider candidates must use current-state SeatTarget payloads."
                }
            }
        }

        require(gameContext.players.map { it.seat }.distinct().size == gameContext.players.size) {
            "Provider game-context player seats must be unique."
        }
        require(
            gameContext.players.isEmpty() ||
                gameContext.players.map { it.seat }.toSet() == stateSeats,
        ) {
            "Populated provider game context must contain every current-state seat exactly once."
        }
        gameContext.historyPrefix?.let { prefix ->
            require(prefix.gameId == identity.gameId) {
                "Provider history prefix must belong to the current decision game."
            }
            require(prefix.sourceRevision == sourceRevision) {
                "Provider history prefix must match the request's frozen source revision."
            }
        }
        require(gameContext.priorDecisions.map { it.eventId }.distinct().size == gameContext.priorDecisions.size) {
            "Provider prior-decision event IDs must be unique."
        }
        require(gameContext.priorDecisions.all { decision ->
            decision.gameStateRevision <= sourceRevision.gameStateRevision &&
                decision.playerInputRevision <= sourceRevision.playerInputRevision
        }) {
            "Provider request cannot contain future decision history."
        }
    }

    val legalCandidateIds: List<String>
        get() = legalCandidates.map { it.candidateId }

    companion object {
        const val SCHEMA_ID = "botc.storyteller-provider-request"
        const val SCHEMA_VERSION = 1
    }
}

internal data class StorytellerProviderDecisionIdentityV1(
    val gameId: String,
    val scriptId: String,
    val decisionTypeId: String,
    val decisionId: String,
) {
    init {
        require(gameId.isNotBlank()) { "Provider decision game ID cannot be blank." }
        require(scriptId.isNotBlank()) { "Provider decision script ID cannot be blank." }
        require(decisionTypeId.isNotBlank()) { "Provider decision type ID cannot be blank." }
        require(decisionId.isNotBlank()) { "Provider decision ID cannot be blank." }
    }
}

internal data class StorytellerProviderRevisionV1(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
) {
    init {
        require(gameStateRevision >= 0) { "Provider game-state revision cannot be negative." }
        require(playerInputRevision >= 0) { "Provider player-input revision cannot be negative." }
    }
}

internal enum class StorytellerProviderCoordinationHorizonV1 {
    CURRENT_DECISION_ONLY,
    BOUNDED_DOWNSTREAM_DECISIONS,
}

/** Stable outer state envelope; each script owns a versioned immutable state payload. */
internal sealed interface StorytellerProviderGameStateV1 {
    val gameId: String
    val scriptId: String
    val stateSchemaId: String
    val stateSchemaVersion: Int

    fun knownSeatNumbers(): Set<Int>

    data class TroubleBrewing(
        val snapshot: TroubleBrewingGameSnapshotV1,
    ) : StorytellerProviderGameStateV1 {
        override val gameId: String get() = snapshot.gameId
        override val scriptId: String get() = snapshot.script.value
        override val stateSchemaId: String get() = TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_ID
        override val stateSchemaVersion: Int get() = TROUBLE_BREWING_GAME_SNAPSHOT_SCHEMA_VERSION

        override fun knownSeatNumbers(): Set<Int> =
            snapshot.grimoireSeats.mapTo(linkedSetOf()) { it.seat }
    }
}

/**
 * Decision-specific semantics without any recommendation score/policy metadata.
 *
 * New scripts/decision families add a subtype; the outer request/response protocol stays stable.
 */
internal sealed interface StorytellerProviderDecisionContextV1 {
    val decisionTypeId: String

    data object DrunkAssignment : StorytellerProviderDecisionContextV1 {
        override val decisionTypeId: String = DRUNK_ASSIGNMENT
    }

    data class FirstNightPairInformation(
        val sourceSeat: Int,
        val abilityRole: RoleId,
        val reliability: ReliabilityState,
    ) : StorytellerProviderDecisionContextV1 {
        override val decisionTypeId: String = FIRST_NIGHT_PAIR_INFORMATION

        init {
            require(sourceSeat > 0) { "Pair-information source seat must be positive." }
        }
    }

    /** One Host-owned numeric/Boolean displayed result; no role-specific AI policy. */
    data class ScalarInformation(
        val sourceSeat: Int,
        val abilityRole: RoleId,
        val reliability: ReliabilityState,
        val kind: StorytellerProviderScalarKindV1,
        val metric: String,
        val subjectSeats: List<Int>,
    ) : StorytellerProviderDecisionContextV1 {
        override val decisionTypeId: String = SCALAR_INFORMATION
        init {
            require(sourceSeat > 0 && metric.isNotBlank())
            require(subjectSeats.distinct().size == subjectSeats.size && subjectSeats.all { it > 0 })
        }
    }

    /**
     * A confirmed, globally recorded player-facing observation is the anchor. One interaction
     * may have multiple independent Spy/Recluse subject decisions; never collapse them.
     */
    data class RegistrationResolution(
        val interactionId: String,
        val observationRecordId: String,
        val sourceSeat: Int,
        val subjectSeat: Int,
        val question: RegistrationQuestion,
    ) : StorytellerProviderDecisionContextV1 {
        override val decisionTypeId: String = REGISTRATION_RESOLUTION

        init {
            require(interactionId.isNotBlank() && observationRecordId.isNotBlank())
            require(sourceSeat > 0 && subjectSeat > 0)
        }
    }

    /**
     * Actual public/day ability interaction, NOT a fabricated private information observation.
     * The Host alone confirms a ruling at the ability's real outcome boundary.
     */
    data class DayAbilityRegistration(
        val interactionId: String,
        val abilityRole: RoleId,
        val actorSeat: Int,
        val subjectSeat: Int,
        val question: RegistrationQuestion,
    ) : StorytellerProviderDecisionContextV1 {
        override val decisionTypeId: String = DAY_ABILITY_REGISTRATION
        init {
            require(interactionId.isNotBlank())
            require(actorSeat > 0 && subjectSeat > 0 && actorSeat != subjectSeat)
        }
    }

    data class MayorRedirect(
        val mayorSeat: Int,
    ) : StorytellerProviderDecisionContextV1 {
        override val decisionTypeId: String = MAYOR_REDIRECT

        init {
            require(mayorSeat > 0) { "Mayor seat must be positive." }
        }
    }

    companion object {
        const val DRUNK_ASSIGNMENT = "drunk-assignment"
        const val FIRST_NIGHT_PAIR_INFORMATION = "first-night-pair-information"
        const val SCALAR_INFORMATION = "scalar-information"
        const val MAYOR_REDIRECT = "mayor-redirect"
        const val REGISTRATION_RESOLUTION = "registration-resolution"
        const val DAY_ABILITY_REGISTRATION = "day-ability-registration"
    }
}

/** Scalar values remain typed by the current engine information context. */
internal enum class StorytellerProviderScalarKindV1 {
    NUMBER,
    BOOLEAN,
}

internal data class StorytellerProviderCandidateV1(
    val candidateId: String,
    val payload: StorytellerProviderCandidatePayloadV1,
) {
    init {
        require(candidateId.isNotBlank()) { "Provider candidate ID cannot be blank." }
    }
}

/** Candidate semantics are rules/domain facts only. */
internal enum class RegistrationResolutionStatusV1 {
    EXPLICIT_SPECIAL,
    EXPLICIT_ACTUAL,
    UNRESOLVED_NOT_REQUIRED,
    NOT_APPLICABLE,
    UNAVAILABLE_OR_UNRECORDED,
}

internal sealed interface StorytellerProviderCandidatePayloadV1 {
    data class DrunkAssignment(
        val seat: Int,
        val shownRoleId: String,
    ) : StorytellerProviderCandidatePayloadV1 {
        init {
            require(seat > 0) { "Drunk candidate seat must be positive." }
            require(shownRoleId.isNotBlank()) { "Drunk candidate shown role cannot be blank." }
        }
    }

    data class PairInformation(
        val shownRoleId: String?,
        val candidateSeats: List<Int>,
        val semanticTruth: SemanticTruth,
        val registrations: List<RegistrationFact>,
    ) : StorytellerProviderCandidatePayloadV1 {
        init {
            require(candidateSeats.distinct().size == candidateSeats.size) {
                "Pair-information candidate seats must be unique."
            }
            require(candidateSeats.all { it > 0 }) {
                "Pair-information candidate seats must be positive."
            }
        }
    }

    data class ScalarResult(val value: String) : StorytellerProviderCandidatePayloadV1 {
        init { require(value.isNotBlank()) }
    }

    data class RegistrationChoice(
        val status: RegistrationResolutionStatusV1,
        /** Only an explicitly selected exact role; never the first legal witness. */
        val selectedRoleId: RoleId? = null,
    ) : StorytellerProviderCandidatePayloadV1 {
        init {
            require(status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL || selectedRoleId == null)
            require(status != RegistrationResolutionStatusV1.UNAVAILABLE_OR_UNRECORDED) {
                "Unrecorded historical registration cannot be a confirmed candidate."
            }
        }
    }

    data class SeatTarget(
        val seat: Int,
    ) : StorytellerProviderCandidatePayloadV1 {
        init {
            require(seat > 0) { "Seat-target candidate must be positive." }
        }
    }
}

internal data class StorytellerProviderPlayerContextV1(
    val seat: Int,
    val experienceLevel: PlayerExperienceLevelV1,
    val claimedRoleIds: List<RoleId>,
    val pressureLevel: StorytellerDeclaredPressureLevelV1?,
) {
    init {
        require(seat > 0) { "Provider player-context seat must be positive." }
        require(claimedRoleIds.distinct().size == claimedRoleIds.size) {
            "Provider claimed roles must be unique per player."
        }
    }
}

internal data class StorytellerProviderPriorDecisionV1(
    val eventId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val selectedCandidateId: String,
    val selectedOutcome: DecisionOutcomeSnapshot,
    val abilityState: AbilityState,
    val truthRelation: TruthRelation,
    val registrations: List<RegistrationFact>,
) {
    init {
        require(eventId.isNotBlank()) { "Provider prior-decision event ID cannot be blank." }
        require(gameStateRevision >= 0) { "Prior game-state revision cannot be negative." }
        require(playerInputRevision >= 0) { "Prior player-input revision cannot be negative." }
        require(selectedCandidateId.isNotBlank()) { "Prior selected candidate ID cannot be blank." }
    }
}

internal data class StorytellerProviderGameContextV1(
    val players: List<StorytellerProviderPlayerContextV1>,
    val priorDecisions: List<StorytellerProviderPriorDecisionV1>,
    /** Null means not provided, NEVER a proven complete empty history. */
    val historyPrefix: StorytellerProviderHistoryPrefixV1? = null,
) {
    companion object {
        val EMPTY = StorytellerProviderGameContextV1(
            players = emptyList(),
            priorDecisions = emptyList(),
        )
    }
}

internal data class StorytellerProviderResponseV1(
    val schemaId: String = SCHEMA_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val decisionId: String,
    val sourceRevision: StorytellerProviderRevisionV1,
    val outcome: StorytellerProviderOutcomeV1,
    val confidence: StorytellerProviderConfidenceV1 = StorytellerProviderConfidenceV1.UNSPECIFIED,
    val uncertainty: List<String> = emptyList(),
    val providerProvenance: String? = null,
) {
    init {
        require(schemaId == SCHEMA_ID) { "Unsupported Storyteller provider response schema ID." }
        require(schemaVersion == SCHEMA_VERSION) { "Unsupported Storyteller provider response schema version." }
        require(decisionId.isNotBlank()) { "Provider response decision ID cannot be blank." }
        require(uncertainty.none(String::isBlank)) { "Provider uncertainty entries cannot be blank." }
        require(providerProvenance == null || providerProvenance.isNotBlank()) {
            "Provider provenance cannot be blank when present."
        }
    }

    companion object {
        const val SCHEMA_ID = "botc.storyteller-provider-response"
        const val SCHEMA_VERSION = 1
    }
}

internal sealed interface StorytellerProviderOutcomeV1 {
    data class Recommendation(
        val primary: StorytellerProviderRecommendationV1,
        val alternatives: List<StorytellerProviderRecommendationV1> = emptyList(),
    ) : StorytellerProviderOutcomeV1 {
        init {
            require(alternatives.map { it.candidateId }.distinct().size == alternatives.size) {
                "Provider alternatives must have unique candidate IDs."
            }
            require(alternatives.none { it.candidateId == primary.candidateId }) {
                "Provider alternatives must differ from the primary candidate."
            }
        }
    }

    data class Deferred(
        val reasons: List<String>,
        val missingContext: List<String> = emptyList(),
    ) : StorytellerProviderOutcomeV1 {
        init {
            require(reasons.isNotEmpty() && reasons.none(String::isBlank)) {
                "Provider deferral requires at least one non-blank reason."
            }
            require(missingContext.none(String::isBlank)) {
                "Provider missing-context entries cannot be blank."
            }
        }
    }
}

internal data class StorytellerProviderRecommendationV1(
    val candidateId: String,
    val rationale: List<String> = emptyList(),
    val tradeoffs: List<String> = emptyList(),
    val risks: List<String> = emptyList(),
) {
    init {
        require(candidateId.isNotBlank()) { "Provider recommendation candidate ID cannot be blank." }
        require(rationale.none(String::isBlank)) { "Provider rationale entries cannot be blank." }
        require(tradeoffs.none(String::isBlank)) { "Provider tradeoff entries cannot be blank." }
        require(risks.none(String::isBlank)) { "Provider risk entries cannot be blank." }
    }
}

internal enum class StorytellerProviderConfidenceV1 {
    LOW,
    MEDIUM,
    HIGH,
    UNSPECIFIED,
}

internal sealed interface StorytellerProviderValidationV1 {
    data class AcceptedRecommendation(
        val primaryCandidateId: String,
        val alternativeCandidateIds: List<String>,
    ) : StorytellerProviderValidationV1

    data class AcceptedDeferral(
        val reasons: List<String>,
        val missingContext: List<String>,
    ) : StorytellerProviderValidationV1

    data class Rejected(
        val reasons: Set<StorytellerProviderValidationFailureV1>,
    ) : StorytellerProviderValidationV1
}

internal enum class StorytellerProviderValidationFailureV1 {
    DECISION_ID_MISMATCH,
    STALE_SOURCE_REVISION,
    UNKNOWN_PRIMARY_CANDIDATE,
    UNKNOWN_ALTERNATIVE_CANDIDATE,
}

internal object StorytellerProviderResponseValidatorV1 {
    fun validate(
        request: StorytellerProviderRequestV1,
        response: StorytellerProviderResponseV1,
    ): StorytellerProviderValidationV1 {
        val failures = linkedSetOf<StorytellerProviderValidationFailureV1>()

        if (response.decisionId != request.identity.decisionId) {
            failures += StorytellerProviderValidationFailureV1.DECISION_ID_MISMATCH
        }
        if (response.sourceRevision != request.sourceRevision) {
            failures += StorytellerProviderValidationFailureV1.STALE_SOURCE_REVISION
        }

        val legalIds = request.legalCandidateIds.toSet()
        val recommendation = response.outcome as? StorytellerProviderOutcomeV1.Recommendation
        if (recommendation != null) {
            if (recommendation.primary.candidateId !in legalIds) {
                failures += StorytellerProviderValidationFailureV1.UNKNOWN_PRIMARY_CANDIDATE
            }
            if (recommendation.alternatives.any { it.candidateId !in legalIds }) {
                failures += StorytellerProviderValidationFailureV1.UNKNOWN_ALTERNATIVE_CANDIDATE
            }
        }

        if (failures.isNotEmpty()) {
            return StorytellerProviderValidationV1.Rejected(failures)
        }

        return when (val outcome = response.outcome) {
            is StorytellerProviderOutcomeV1.Recommendation ->
                StorytellerProviderValidationV1.AcceptedRecommendation(
                    primaryCandidateId = outcome.primary.candidateId,
                    alternativeCandidateIds = outcome.alternatives.map { it.candidateId },
                )

            is StorytellerProviderOutcomeV1.Deferred ->
                StorytellerProviderValidationV1.AcceptedDeferral(
                    reasons = outcome.reasons,
                    missingContext = outcome.missingContext,
                )
        }
    }
}
