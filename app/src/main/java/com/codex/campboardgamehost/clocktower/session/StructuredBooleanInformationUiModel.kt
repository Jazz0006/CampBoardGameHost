package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome

/**
 * Structured Storyteller presentation model for a two-value Boolean information domain.
 *
 * The Foundation context owns legality and confirmation. This model only projects validated
 * categorical outcomes into typed Boolean values so UI code never parses localized Yes/No labels.
 */
internal class StructuredBooleanInformationUiModel private constructor(
    private val context: InformationDecisionContext<DynamicInformationOutcome.Category>,
    val choices: List<Choice>,
) {
    internal data class Choice(
        val candidateId: String,
        val value: Boolean,
        val recommended: Boolean,
    )

    val contextSnapshot: InformationDecisionSnapshot
        get() = context.snapshot

    val semanticStateKey: String = buildString {
        append(context.semanticIdentity)
        append('|')
        append(context.revision.gameStateRevision)
        append(':')
        append(context.revision.playerInputRevision)
        append('|')
        append(
            choices.joinToString(",") { choice ->
                "${choice.candidateId}:${choice.value}:${choice.recommended}"
            },
        )
    }

    fun acceptRecommendation(
        candidateId: String,
        currentRevision: InformationDecisionRevision,
    ): InformationDecisionConfirmation = context.confirm(
        candidateId = candidateId,
        source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
        currentRevision = currentRevision,
    )

    fun chooseManually(
        candidateId: String,
        currentRevision: InformationDecisionRevision,
    ): InformationDecisionConfirmation = context.confirm(
        candidateId = candidateId,
        source = InformationDecisionSource.MANUAL,
        currentRevision = currentRevision,
    )

    companion object {
        fun from(
            context: InformationDecisionContext<DynamicInformationOutcome.Category>,
            trueOutcomeId: String,
            falseOutcomeId: String,
        ): StructuredBooleanInformationUiModel {
            require(trueOutcomeId.isNotBlank() && falseOutcomeId.isNotBlank() && trueOutcomeId != falseOutcomeId) {
                "Boolean outcome IDs must be distinct and non-blank."
            }
            return StructuredBooleanInformationUiModel(
                context = context,
                choices = context.legalCandidates
                    .map { candidate ->
                        val outcomeId = candidate.evaluation.candidate.outcome.id
                        val value = when (outcomeId) {
                            trueOutcomeId -> true
                            falseOutcomeId -> false
                            else -> error("Unexpected Boolean information outcome '$outcomeId'.")
                        }
                        Choice(
                            candidateId = candidate.candidateId,
                            value = value,
                            recommended = candidate.candidateId in context.recommendedCandidateIds,
                        )
                    }
                    .sortedWith(compareBy(Choice::value, Choice::candidateId)),
            )
        }
    }
}
