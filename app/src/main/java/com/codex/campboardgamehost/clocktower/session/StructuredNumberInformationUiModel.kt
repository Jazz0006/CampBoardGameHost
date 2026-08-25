package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome

/**
 * Structured Storyteller presentation model for numeric information decisions.
 *
 * The model intentionally has no legality rules of its own. Its choices are a projection of the
 * Foundation context's already-validated legal candidates, and every confirmation is delegated
 * back to that same context. This keeps manual and recommendation-accepted inputs as peers without
 * giving the UI a bypass around semantic validation or stale-revision checks.
 */
internal class StructuredNumberInformationUiModel private constructor(
    private val context: InformationDecisionContext<DynamicInformationOutcome.Number>,
    val choices: List<Choice>,
) {
    internal data class Choice(
        val candidateId: String,
        val value: Int,
        val recommended: Boolean,
    )

    val contextSnapshot: InformationDecisionSnapshot
        get() = context.snapshot

    /** Stable across recomposition when the semantic decision projection has not changed. */
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
            context: InformationDecisionContext<DynamicInformationOutcome.Number>,
        ): StructuredNumberInformationUiModel = StructuredNumberInformationUiModel(
            context = context,
            choices = context.legalCandidates
                .map { candidate ->
                    Choice(
                        candidateId = candidate.candidateId,
                        value = candidate.evaluation.candidate.outcome.value,
                        recommended = candidate.candidateId in context.recommendedCandidateIds,
                    )
                }
                .sortedWith(compareBy(Choice::value, Choice::candidateId)),
        )
    }
}
