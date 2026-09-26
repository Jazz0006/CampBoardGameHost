package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft

/** The only provenance values owned by the first Storyteller information-decision slice. */
internal enum class InformationDecisionSource {
    MANUAL,
    RECOMMENDATION_ACCEPTED,
}

/**
 * Freshness boundary for a Storyteller-visible information decision.
 *
 * A decision prepared against either an older game state or older player input must not be
 * confirmed after that input changes.
 */
internal data class InformationDecisionRevision(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
) {
    init {
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
    }
}

/**
 * Typed identity boundary for one information-decision request.
 *
 * [gameId] binds the request to the canonical game/session. [requestId] is the existing stable
 * semantic decision identity used for UI/trace correlation; callers must never parse it to recover
 * the game identity.
 */
internal data class InformationDecisionRequestIdentity(
    val gameId: String,
    val requestId: String,
) {
    init {
        require(gameId.isNotBlank()) { "Information decision game ID cannot be blank." }
        require(requestId.isNotBlank()) { "Information decision request ID cannot be blank." }
    }
}

internal enum class InformationDecisionHardBlockReason {
    STALE_CONTEXT,
    ILLEGAL_CANDIDATE,
    NOT_RECOMMENDED,
}

internal data class InformationDecisionWarning(
    val code: String,
) {
    init {
        require(code.isNotBlank()) { "Information decision warning code cannot be blank." }
    }
}

/** Immutable identity of the validated candidate space that produced a confirmation. */
internal data class InformationDecisionSnapshot(
    val requestIdentity: InformationDecisionRequestIdentity,
    val revision: InformationDecisionRevision,
    val legalCandidateIds: List<String>,
    val recommendedCandidateIds: Set<String>,
) {
    val semanticIdentity: String get() = requestIdentity.requestId
    val gameId: String get() = requestIdentity.gameId

    init {
        require(legalCandidateIds.isNotEmpty()) { "Information decision snapshot requires legal candidates." }
        require(recommendedCandidateIds.all(legalCandidateIds::contains)) {
            "Snapshot recommendations must belong to the snapshot legal candidates."
        }
    }

    fun isCurrentFor(currentRevision: InformationDecisionRevision): Boolean = revision == currentRevision
}

internal sealed interface InformationDecisionValidationResult {
    data class Allowed(
        val warnings: List<InformationDecisionWarning> = emptyList(),
    ) : InformationDecisionValidationResult

    data class Blocked(
        val reason: InformationDecisionHardBlockReason,
    ) : InformationDecisionValidationResult
}

internal data class ConfirmedInformationDecision(
    val candidateId: String,
    val source: InformationDecisionSource,
    val warnings: List<InformationDecisionWarning>,
    val draft: EpistemicObservationDraft,
    val contextSnapshot: InformationDecisionSnapshot,
) {
    /** Narrow durable-publication authority: exact immutable snapshot plus current revision. */
    fun authorizes(
        expectedCurrentSnapshot: InformationDecisionSnapshot,
        currentRevision: InformationDecisionRevision,
    ): Boolean = contextSnapshot == expectedCurrentSnapshot && contextSnapshot.isCurrentFor(currentRevision)
}

internal data class InformationDecisionConfirmation(
    val validation: InformationDecisionValidationResult,
    val confirmed: ConfirmedInformationDecision? = null,
) {
    init {
        require(
            (validation is InformationDecisionValidationResult.Allowed) == (confirmed != null),
        ) { "Allowed information decisions must have a confirmation; blocked decisions must not." }
    }
}

/**
 * Pure semantic authority seam shared by recommendation acceptance and structured manual choice.
 *
 * [evaluations] are the role-specific candidate space produced by the existing rules pipeline.
 * This class deliberately does not accept a free-form value from manual callers. Confirmation is
 * by stable candidate ID only, so both sources consume the same impairment, registration, and
 * role-shape evidence before an unbound [EpistemicObservationDraft] can be obtained.
 */
internal class InformationDecisionContext<T : DynamicInformationOutcome> private constructor(
    val requestIdentity: InformationDecisionRequestIdentity,
    val revision: InformationDecisionRevision,
    val legalCandidates: List<InformationDecisionCandidate<T>>,
    val recommendedCandidateIds: Set<String>,
) {
    val semanticIdentity: String get() = requestIdentity.requestId
    val gameId: String get() = requestIdentity.gameId
    private val candidatesById = legalCandidates.associateBy { it.candidateId }

    init {
        require(legalCandidates.isNotEmpty()) { "Information decision requires at least one legal candidate." }
        require(candidatesById.size == legalCandidates.size) { "Information candidate IDs must be unique." }
        require(recommendedCandidateIds.all(candidatesById::containsKey)) {
            "Recommended information candidates must belong to the validated legal candidate set."
        }
    }

    val snapshot: InformationDecisionSnapshot = InformationDecisionSnapshot(
        requestIdentity = requestIdentity,
        revision = revision,
        legalCandidateIds = legalCandidates.map(InformationDecisionCandidate<T>::candidateId),
        recommendedCandidateIds = recommendedCandidateIds,
    )

    fun validate(
        candidateId: String,
        source: InformationDecisionSource,
        currentRevision: InformationDecisionRevision,
    ): InformationDecisionValidationResult {
        if (currentRevision != revision) {
            return InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.STALE_CONTEXT)
        }

        val selected = candidatesById[candidateId]
            ?: return InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.ILLEGAL_CANDIDATE)

        if (source == InformationDecisionSource.RECOMMENDATION_ACCEPTED && candidateId !in recommendedCandidateIds) {
            return InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.NOT_RECOMMENDED)
        }

        val warnings = buildList {
            addAll(selected.warningCodes.map(::InformationDecisionWarning))
            if (source == InformationDecisionSource.MANUAL && candidateId !in recommendedCandidateIds) {
                add(InformationDecisionWarning("information.manual.differs-from-recommendation"))
            }
            if (
                selected.isImpairedTruthful &&
                legalCandidates.any(InformationDecisionCandidate<T>::isImpairedFalse)
            ) {
                add(InformationDecisionWarning("information.impaired.truthful-with-false-alternative"))
            }
        }.distinctBy(InformationDecisionWarning::code)

        return InformationDecisionValidationResult.Allowed(warnings)
    }

    fun confirm(
        candidateId: String,
        source: InformationDecisionSource,
        currentRevision: InformationDecisionRevision,
    ): InformationDecisionConfirmation {
        val validation = validate(candidateId, source, currentRevision)
        if (validation is InformationDecisionValidationResult.Blocked) {
            return InformationDecisionConfirmation(validation = validation)
        }

        validation as InformationDecisionValidationResult.Allowed
        val selected = requireNotNull(candidatesById[candidateId])
        return InformationDecisionConfirmation(
            validation = validation,
            confirmed = ConfirmedInformationDecision(
                candidateId = candidateId,
                source = source,
                warnings = validation.warnings,
                draft = selected.draft,
                contextSnapshot = snapshot,
            ),
        )
    }

    companion object {
        fun <T : DynamicInformationOutcome> fromEvaluations(
            evaluations: List<DecisionEvaluation<T>>,
            recommendedCandidateIds: Set<String>,
            revision: InformationDecisionRevision,
            requestIdentity: InformationDecisionRequestIdentity,
            draftOf: (DecisionEvaluation<T>) -> EpistemicObservationDraft,
        ): InformationDecisionContext<T> {
            require(evaluations.isNotEmpty()) { "Information decision evaluations cannot be empty." }
            require(evaluations.map { it.candidate.candidateId }.distinct().size == evaluations.size) {
                "Information decision evaluation candidate IDs must be unique."
            }

            val legal = evaluations
                .filter(::isValidatedLegalInformationCandidate)
                .map { evaluation ->
                    InformationDecisionCandidate(
                        evaluation = evaluation,
                        draft = draftOf(evaluation),
                    )
                }

            return InformationDecisionContext(
                requestIdentity = requestIdentity,
                revision = revision,
                legalCandidates = legal,
                recommendedCandidateIds = recommendedCandidateIds,
            )
        }

        private fun isValidatedLegalInformationCandidate(
            evaluation: DecisionEvaluation<out DynamicInformationOutcome>,
        ): Boolean {
            val candidate = evaluation.candidate
            val informationEffect = candidate.effects.filterIsInstance<EffectDraft.PlayerInformation>().singleOrNull()
                ?: return false
            if (!outcomeMatchesInformationValue(candidate.outcome, informationEffect.value)) return false

            return when (candidate.abilityState) {
                AbilityState.FUNCTIONING -> candidate.truthRelation in setOf(
                    TruthRelation.TRUE_TO_ACTUAL_STATE,
                    TruthRelation.TRUE_TO_REGISTERED_STATE,
                )
                AbilityState.MALFUNCTIONING_DRUNK,
                AbilityState.MALFUNCTIONING_POISONED,
                -> candidate.truthRelation != TruthRelation.NOT_APPLICABLE
            }
        }

        private fun outcomeMatchesInformationValue(
            outcome: DynamicInformationOutcome,
            value: InformationValue,
        ): Boolean = when (outcome) {
            is DynamicInformationOutcome.Number -> value == InformationValue.Number(outcome.value)
            is DynamicInformationOutcome.Category -> value == InformationValue.Category(outcome.id)
        }
    }
}

internal data class InformationDecisionCandidate<T : DynamicInformationOutcome>(
    val evaluation: DecisionEvaluation<T>,
    val draft: EpistemicObservationDraft,
) {
    val candidateId: String = evaluation.candidate.candidateId
    val warningCodes: List<String> = evaluation.warnings.distinct()

    val isImpairedTruthful: Boolean
        get() = evaluation.candidate.abilityState != AbilityState.FUNCTIONING &&
            evaluation.candidate.truthRelation in setOf(
                TruthRelation.TRUE_TO_ACTUAL_STATE,
                TruthRelation.TRUE_TO_REGISTERED_STATE,
            )

    val isImpairedFalse: Boolean
        get() = evaluation.candidate.abilityState != AbilityState.FUNCTIONING &&
            evaluation.candidate.truthRelation == TruthRelation.FALSE_TO_ACTUAL_STATE
}
