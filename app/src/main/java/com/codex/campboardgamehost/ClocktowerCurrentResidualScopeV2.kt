package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1

/**
 * Collects current Host-produced legal choices from the *actual* night order.
 * This is an availability projection, NOT a model recommender.
 *
 * A future player action is a hard dependency barrier. No future selected target,
 * poison result, registration or observation is hallucinated to fill the batch.
 */
internal object ClocktowerCurrentResidualScopeV2 {
    internal data class Scope(
        val requests: List<StorytellerProviderRequestV1>,
        val barrierStep: Int?,
        val barrierReason: String?,
    )

    // These actions need new interaction/confirmation by a player or Host.
    // The classification is from the existing night-flow semantics, not AI
    // role-specific policy. A completed action is already behind startIndex.
    internal fun needsLiveInteraction(action: ClocktowerNightAction): Boolean = when (action) {
        ClocktowerNightAction.Poison,
        ClocktowerNightAction.ButlerMaster,
        ClocktowerNightAction.MonkProtect,
        ClocktowerNightAction.Chambermaid,
        ClocktowerNightAction.FortuneTeller,
        ClocktowerNightAction.DemonKill,
        ClocktowerNightAction.MayorRedirect,
        ClocktowerNightAction.DemonSuccessor,
        ClocktowerNightAction.Ravenkeeper,
        ClocktowerNightAction.RedHerring -> true
        ClocktowerNightAction.None,
        ClocktowerNightAction.NewDemonIdentity -> false
    }

    fun collect(
        state: ClocktowerSessionState,
        phase: StorytellerPhase,
        round: Int,
        steps: List<ClocktowerNightStepUi>,
        startIndex: Int,
        cards: List<PlayerCard>,
        snapshot: TroubleBrewingGameSnapshotV1,
        gameContext: StorytellerProviderGameContextV1,
        pairContext: TroubleBrewingFirstNightPairDecisionContext?,
        fortuneTellerSelectedNames: List<String> = emptyList(),
        mayorPending: PendingMayorRedirectDecision? = null,
        redHerringAlreadyChosen: Boolean = false,
    ): Scope {
        require(round >= 1 && startIndex in steps.indices)
        require(state.gameId == snapshot.gameId)
        require(snapshot.gameSeed == state.gameSeed)
        val produced = mutableListOf<StorytellerProviderRequestV1>()
        val revision = StorytellerDecisionRevision(
            state.gameStateRevision, state.playerInputRevision)
        for (i in startIndex until steps.size) {
            val step = steps[i]
            if (!step.isRealAction) continue
            // A player-facing decision already published in this phase/round
            // must not be requested again even after walking backward.
            val seat = step.actor?.let { actor ->
                cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
            }
            val published = seat != null && state.epistemicObservationLog.records.any {
                it.phase == phase && it.round == round && it.sourceSeat == seat
            }
            if (published) continue
            if (step.action == ClocktowerNightAction.RedHerring && redHerringAlreadyChosen) {
                continue // initial Host-fixed setup, not a newly open selection
            }
            // A real redirect ruling exists only after the real attack context.
            if (step.action == ClocktowerNightAction.MayorRedirect &&
                phase == StorytellerPhase.NIGHT && i == startIndex && mayorPending != null
            ) {
                produced += StorytellerProviderRequestFactoryV1.fromMayorRedirect(
                    mayorPending, snapshot, gameContext)
                return Scope(produced, i + 1, "NEXT_INTERACTION_NOT_YET_CONFIRMED")
            }
            if (needsLiveInteraction(step.action)) {
                // If the CURRENT player interaction has already supplied enough
                // exact subjects for the Host to open its legal information
                // domain, include that domain; do not infer any FUTURE action.
                if (i == startIndex && phase in setOf(
                        StorytellerPhase.FIRST_NIGHT, StorytellerPhase.NIGHT)) {
                    val current = pendingGlobalNightScalarDecision(
                        step, if (phase == StorytellerPhase.FIRST_NIGHT)
                            ClocktowerPhase.FirstNight else ClocktowerPhase.Night,
                        round, i, cards,
                        state.gameId, revision, fortuneTellerSelectedNames)
                    if (current != null) {
                        produced += StorytellerProviderRequestFactoryV1.fromScalarInformation(
                            current.requestIdentity, current.revision, current.sourceSeat,
                            current.abilityRole, current.reliability, current.kind,
                            current.metric, current.subjectSeats, current.legalCandidates,
                            snapshot, gameContext)
                        return Scope(produced, i + 1, "NEXT_INTERACTION_NOT_YET_CONFIRMED")
                    }
                }
                return Scope(produced, i, "PLAYER_OR_HOST_ACTION_NOT_YET_CONFIRMED")
            }
            if (phase !in setOf(StorytellerPhase.FIRST_NIGHT, StorytellerPhase.NIGHT)) continue
            val pair = if (phase == StorytellerPhase.FIRST_NIGHT && round == 1)
                pendingGlobalFirstNightPairDecision(
                    step, ClocktowerPhase.FirstNight, round, cards, pairContext)
            else null
            if (pair != null) {
                produced += StorytellerProviderRequestFactoryV1.fromPairInformation(
                    pair, snapshot, gameContext)
                continue
            }
            val scalar = pendingGlobalNightScalarDecision(
                step, if (phase == StorytellerPhase.FIRST_NIGHT)
                    ClocktowerPhase.FirstNight else ClocktowerPhase.Night,
                round, i, cards, state.gameId,
                revision, fortuneTellerSelectedNames)
            if (scalar != null) {
                produced += StorytellerProviderRequestFactoryV1.fromScalarInformation(
                    scalar.requestIdentity, scalar.revision, scalar.sourceSeat,
                    scalar.abilityRole, scalar.reliability, scalar.kind, scalar.metric,
                    scalar.subjectSeats, scalar.legalCandidates, snapshot, gameContext)
            } else if (step.manualInformationCandidates.isNotEmpty()) {
                // An actual Host information domain exists but this producer does
                // not understand its typed contract. Never skip it and pretend
                // the later scope is complete.
                return Scope(produced, i, "UNSUPPORTED_HOST_PENDING_DECISION")
            }
        }
        return Scope(produced, null, null)
    }
}
