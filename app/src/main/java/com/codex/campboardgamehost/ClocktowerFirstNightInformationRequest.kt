package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationCandidate
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationRequest

/**
 * A confirmed first-night display is a Host observation, not a Rules candidate.
 * Never reconstruct Spy/Recluse registrations or reject a manually shown value by rule truth.
 */
internal fun clocktowerFirstNightInformationRequest(
    displayStep: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    cards: List<PlayerCard>,
): FirstNightInformationRequest? {
    if (phase != ClocktowerPhase.FirstNight) return null
    val actor = displayStep.actor ?: return null
    val family = FirstNightInformationFamily.entries.firstOrNull {
        it.role.value == displayStep.roleEnName
    } ?: return null
    val sourceSeat = cards.indexOfFirst { it.name == actor.name }
        .takeIf { it >= 0 }?.plus(1) ?: return null
    val rawValue = (displayStep.displayPrimary ?: displayStep.tellPlayer)?.trim()
    val proposition = displayStep.displayProposition
    val shownPair = (proposition as? InformationProposition.AnyOf)
        ?.alternatives?.map { it as? InformationProposition.RoleAt }
        ?.takeIf { entries ->
            entries.size == 2 && entries.all { it != null } &&
                entries.mapNotNull { it?.role }.distinct().size == 1 &&
                entries.mapNotNull { it?.seat }.distinct().size == 2
        }?.filterNotNull()
    val pairSeats = shownPair?.map { it.seat }?.sorted().orEmpty()
    val shownRole = shownPair?.firstOrNull()?.role
    val isPair = family.role.value in setOf("Washerwoman", "Librarian", "Investigator")
    if (isPair && (proposition !is InformationProposition.AllOf && shownPair == null)) return null
    if (pairSeats.any { it !in 1..cards.size }) return null
    val reliability = when (displayStep.informationReliability) {
        ReliabilityState.RELIABLE -> ReliabilityState.RELIABLE
        ReliabilityState.DRUNK -> ReliabilityState.DRUNK
        ReliabilityState.POISONED -> ReliabilityState.POISONED
    }
    val observation = AbilityObservation(
        sourceSeat = sourceSeat,
        perceivedRole = family.role,
        shownRole = shownRole,
        candidateSeats = pairSeats,
        shownNumber = (proposition as? InformationProposition.NumericResult)?.value
            ?: rawValue?.toIntOrNull(),
        shownAnswer = ((proposition as? InformationProposition.BooleanResult)?.value
            ?: when (rawValue?.lowercase()) {
                "有", "是", "yes" -> true
                "没有", "否", "no" -> false
                else -> null
            })?.let { if (it) YesNoAnswer.YES else YesNoAnswer.NO },
        reliability = reliability,
        semanticTruth = SemanticTruth.NOT_APPLICABLE,
    )
    val decisionId = "first-night:${phase.name}:$round:${family.name}:$sourceSeat"
    val candidateId = "host-observed:${decisionId}"
    return FirstNightInformationRequest(
        decisionId = decisionId,
        family = family,
        sourceSeat = sourceSeat,
        reliability = reliability,
        selectedCandidateId = candidateId,
        candidates = listOf(FirstNightInformationCandidate(candidateId, observation)),
    )
}
