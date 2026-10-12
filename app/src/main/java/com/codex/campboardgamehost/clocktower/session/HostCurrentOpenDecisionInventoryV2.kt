package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1

/**
 * Host-derived, current and ONLY currently open inventory.
 * No future abilities/decisions are fabricated; no model-provided ID is trusted.
 * Use the *live* decision producers at the current phase instead of an all-role
 * speculation table. If the Host cannot produce a pending decision, return none.
 */
internal object HostCurrentOpenDecisionInventoryV2 {
    fun fromPendingRequests(
        frozen: ClocktowerSessionState,
        phase: StorytellerPhase,
        round: Int,
        requests: List<StorytellerProviderRequestV1>,
    ): List<StorytellerStrategicContinuationV2.OpenDecision> {
        require(round >= 1)
        require(requests.map { it.identity.decisionId }.distinct().size == requests.size)
        return requests.map { request ->
            val identity = request.identity
            require(identity.gameId == frozen.gameId &&
                identity.scriptId == frozen.gameState.script.value &&
                request.sourceRevision.gameStateRevision == frozen.gameStateRevision &&
                request.sourceRevision.playerInputRevision == frozen.playerInputRevision) {
                "Pending decision must be regenerated from the same frozen live Host revision."
            }
            val context = request.decisionContext
            val seat = when (context) {
                is StorytellerProviderDecisionContextV1.FirstNightPairInformation ->
                    context.sourceSeat
                is StorytellerProviderDecisionContextV1.ScalarInformation ->
                    context.sourceSeat
                is StorytellerProviderDecisionContextV1.RegistrationResolution ->
                    context.sourceSeat
                is StorytellerProviderDecisionContextV1.DayAbilityRegistration ->
                    context.actorSeat
                is StorytellerProviderDecisionContextV1.MayorRedirect ->
                    context.mayorSeat
                StorytellerProviderDecisionContextV1.DrunkAssignment ->
                    error("A committed-game continuation cannot re-open initial Drunk selection.")
            }
            val information = context is StorytellerProviderDecisionContextV1.FirstNightPairInformation ||
                context is StorytellerProviderDecisionContextV1.ScalarInformation
            if (information) {
                require(phase == StorytellerPhase.FIRST_NIGHT && round == 1) {
                    "First-night information pending domain cannot be reused in a later phase."
                }
                require(frozen.epistemicObservationLog.records.none {
                    it.phase == phase && it.round == round && it.sourceSeat == seat
                }) { "The current player has already received this first-night information." }
            }
            require(frozen.gameState.playerAt(seat) != null)
            StorytellerStrategicContinuationV2.OpenDecision(
                decisionId = identity.decisionId,
                sourceSeat = seat,
                family = if (information) "NIGHT_INFORMATION" else "STORYTELLER_DISCRETION",
            )
        }
    }
}
