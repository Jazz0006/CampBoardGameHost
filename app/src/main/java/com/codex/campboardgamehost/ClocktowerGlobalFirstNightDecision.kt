package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
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
    game: GameState,
    gameId: String,
    revision: StorytellerDecisionRevision,
): PendingPairInformationDecision? {
    if (phase != ClocktowerPhase.FirstNight || round != 1 || !step.isRealAction ||
        game.script.value != "trouble_brewing") return null
    val family = FirstNightInformationFamily.entries.firstOrNull {
        it.role.value == step.roleEnName
    }?.takeIf { it.usesAuthoritativePairDomain() } ?: return null
    val actor = step.actor ?: return null
    require(game.players.sortedBy { it.seat }.map { it.name } == cards.map { it.name }) {
        "UI card order diverged from canonical Host seats."
    }
    val source = game.players.singleOrNull { it.name == actor.name } ?: return null
    val seat = source.seat
    require((source.shownRole ?: source.actualRole) == family.role) {
        "Pair-information source role differs from the canonical Host identity."
    }
    val reliability = when (step.informationReliability) {
        InformationReliability.RELIABLE -> ReliabilityState.RELIABLE
        InformationReliability.DRUNK -> ReliabilityState.DRUNK
        InformationReliability.POISONED -> ReliabilityState.POISONED
    }
    return PairInformationDecisionBoundary.create(
        requestIdentity = StorytellerDecisionRequestIdentity(
            gameId = gameId,
            requestId = "first-night:${phase.name}:$round:${family.name}:$seat",
        ),
        revision = revision,
        game = game,
        roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
        sourceSeat = seat,
        abilityRole = family.role,
        reliability = reliability,
    )
}
