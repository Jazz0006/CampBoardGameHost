package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft

internal class ClocktowerRecommendationCoordinator(
    private val nightModule: NightRecommendationModule = NightRecommendationModule(),
) {
    fun resolveInformation(request: InformationResolutionRequest) = nightModule.resolveInformation(request)

    /** Typed UI-adapter seam; rules candidate generation remains owned by the recommendation/session layer. */
    fun resolveNumberInformation(request: InformationResolutionRequest.Number) =
        nightModule.resolveNumberInformation(request)

    /**
     * Recommendation and future structured-manual callers meet here before information can become
     * an observation draft. This method intentionally does not commit history or expose UI state.
     */
    fun <T : DynamicInformationOutcome> informationDecisionContext(
        evaluations: List<DecisionEvaluation<T>>,
        recommendedCandidateIds: Set<String>,
        revision: InformationDecisionRevision,
        requestIdentity: InformationDecisionRequestIdentity,
        draftOf: (DecisionEvaluation<T>) -> EpistemicObservationDraft,
    ): InformationDecisionContext<T> = InformationDecisionContext.fromEvaluations(
        evaluations = evaluations,
        recommendedCandidateIds = recommendedCandidateIds,
        revision = revision,
        requestIdentity = requestIdentity,
        draftOf = draftOf,
    )

}
