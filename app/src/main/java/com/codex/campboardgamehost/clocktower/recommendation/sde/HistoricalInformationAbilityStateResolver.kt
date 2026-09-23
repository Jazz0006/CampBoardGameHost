package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

/**
 * Read-only reconstruction of a committed information source's actual ability state at the exact
 * historical observation point.
 *
 * Canonical setup/action history remains authoritative. ObservationReliability is deliberately not
 * consulted: it describes what the recipient was told, not hidden mechanical impairment truth.
 */
internal object HistoricalInformationAbilityStateResolver {
    fun resolve(
        record: RecordedEpistemicObservation,
        context: ExactConsequenceContext,
    ): AbilityState? {
        val sourceSeat = record.sourceSeat ?: return null
        val sourceAbility = record.sourceAbility ?: return null
        val binding = record.timelineBinding as? ObservationTimelineBinding.Global ?: return null
        val historical = context.exactContext
        val actionsBeforeObservation = historical.actionTimeline.entries
            .filter { it.point.globalSequence < binding.point.globalSequence }

        val reduced = DynamicActionReducer.reduce(
            initialSnapshot = historical.initialSnapshot,
            initialPhase = historical.initialPhase,
            initialRound = historical.initialRound,
            facts = actionsBeforeObservation.map { it.fact },
        )
        val player = reduced.snapshot.gameState.playerAt(sourceSeat) ?: return null
        val activePoisonTarget = actionsBeforeObservation
            .asReversed()
            .firstOrNull { it.fact is ActionFact.Poison }
            ?.fact
            ?.let { it as ActionFact.Poison }
            ?.targetSeat

        val state = AbilityFunctioningSemantics.stateForEstablishedInteraction(
            subject = AbilitySubject(
                actualRole = player.actualRole.value,
                shownRole = player.shownRole?.value,
                isPoisoned = activePoisonTarget == sourceSeat,
                isAlive = player.alive,
            ),
            role = sourceAbility.value,
        ) ?: return null

        return when (state) {
            AbilityFunctioningState.FUNCTIONING -> AbilityState.FUNCTIONING
            AbilityFunctioningState.DRUNK -> AbilityState.MALFUNCTIONING_DRUNK
            AbilityFunctioningState.POISONED -> AbilityState.MALFUNCTIONING_POISONED
        }
    }
}
