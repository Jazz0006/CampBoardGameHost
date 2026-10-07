package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DecisionExplanation
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext

internal class ClocktowerRecommendationCoordinator(
    initialArchive: DecisionHistoryArchive = DecisionHistoryArchive(),
    private val nightModule: NightRecommendationModule = NightRecommendationModule(),
    private val historyModule: HistoryReviewModule = HistoryReviewModule(),
) {
    private val eventStore = InMemoryDecisionEventStore(initialArchive)

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

    /**
     * Compatibility shape for the existing first-night precompute cache.
     * Pair-information truth ownership is canonical in [NaturalPairInformationCandidateGenerator];
     * this method only wraps those candidates in the historical SetupClueOutcome transport type.
     */
    fun naturalPairCandidates(context: TroubleBrewingFirstNightPairDecisionContext): List<DecisionCandidate<SetupClueOutcome>> =
        naturalPairCandidates(context.naturalPairGameState)

    fun naturalPairCandidates(game: GameState): List<DecisionCandidate<SetupClueOutcome>> =
        NaturalPairInformationCandidateGenerator.generatePerceivedFirstNightInformationSpace(game).map { candidate ->
            val sourceAbility = candidate.effects
                .filterIsInstance<EffectDraft.PlayerInformation>()
                .single()
                .sourceAbility
            DecisionCandidate(
                candidateId = candidate.candidateId,
                candidateFamilyId = candidate.candidateFamilyId,
                outcome = SetupClueOutcome.PairInformation(sourceAbility, candidate.outcome),
                abilityState = candidate.abilityState,
                truthRelation = candidate.truthRelation,
                registrations = candidate.registrations,
                effects = candidate.effects,
                metadata = candidate.metadata,
            )
        }

    fun appendDecision(event: StorytellerDecisionEvent, revision: DecisionRevision): DecisionAppendResult =
        eventStore.appendAtomically(event, revision)

    fun transitionDecision(
        eventId: String,
        expected: DecisionEventStatus,
        next: DecisionEventStatus,
    ): Boolean = eventStore.transitionStatus(eventId, expected, next)

    fun correctDecision(correction: DecisionCorrectionEvent): Boolean = eventStore.appendCorrection(correction)

    fun archive(): DecisionHistoryArchive = eventStore.archive()

    fun explainDecision(eventId: String): DecisionExplanation? = historyModule.explainEvent(eventStore.archive(), eventId)

    fun postGameReview() = historyModule.postGameReview(eventStore.archive())

}