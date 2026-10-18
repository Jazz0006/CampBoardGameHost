package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.PairInformationDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.PendingPairInformationDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.usesAuthoritativePairDomain

/**
 * Shared Host rules boundary for the existing playable first-night pair interactions.
 * It delegates the complete legal domain to the engine; no AI selector or role policy.
 */
internal fun pendingGlobalFirstNightPairDecision(
    step: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    cards: List<PlayerCard>,
    context: TroubleBrewingFirstNightPairDecisionContext?,
): PendingPairInformationDecision? {
    if (phase != ClocktowerPhase.FirstNight || context == null) return null
    val family = FirstNightInformationFamily.entries.firstOrNull {
        it.role.value == step.roleEnName
    }?.takeIf { it.usesAuthoritativePairDomain() } ?: return null
    val actor = step.actor ?: return null
    val seat = cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
        ?: return null
    val position = context.snapshot.position
    val gameRevision = (position.gameStateRevision as? SnapshotField.Known<Long>)?.value
        ?: return null
    val inputRevision = (position.playerInputRevision as? SnapshotField.Known<Long>)?.value
        ?: return null
    val reliability = when (step.informationReliability) {
        InformationReliability.RELIABLE -> ReliabilityState.RELIABLE
        InformationReliability.DRUNK -> ReliabilityState.DRUNK
        InformationReliability.POISONED -> ReliabilityState.POISONED
    }
    return PairInformationDecisionBoundary.create(
        requestIdentity = StorytellerDecisionRequestIdentity(
            gameId = context.snapshot.gameId,
            requestId = "first-night:${phase.name}:$round:${family.name}:$seat",
        ),
        revision = StorytellerDecisionRevision(gameRevision, inputRevision),
        game = context.naturalPairGameState,
        roleDefinitions = context.roleDefinitions,
        sourceSeat = seat,
        abilityRole = family.role,
        reliability = reliability,
    )
}
