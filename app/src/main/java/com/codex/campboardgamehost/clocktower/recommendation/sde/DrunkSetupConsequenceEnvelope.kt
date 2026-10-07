package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorKind
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateSpaceAudit
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleDeferredComplexity
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleProfileExposure
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkHypotheticalSetup

/**
 * Factual circular-seat context around one hypothetical Drunk candidate.
 *
 * These fields carry no preference semantics. They describe only the canonical setup topology of
 * the candidate-specific hypothetical state.
 */
internal data class DrunkSetupTopologyConsequence(
    val previousSeat: Int,
    val nextSeat: Int,
    val adjacentEvilSeats: Set<Int>,
    val adjacentDemonSeats: Set<Int>,
    val adjacentMinionSeats: Set<Int>,
) {
    init {
        require(previousSeat > 0 && nextSeat > 0) {
            "Drunk setup topology neighbour seats must be positive."
        }
        require(adjacentEvilSeats.all { it == previousSeat || it == nextSeat }) {
            "Adjacent Evil seats must be immediate neighbours of the Drunk candidate."
        }
        require(adjacentDemonSeats.all { it in adjacentEvilSeats }) {
            "Adjacent Demon seats must also be adjacent Evil seats."
        }
        require(adjacentMinionSeats.all { it in adjacentEvilSeats }) {
            "Adjacent Minion seats must also be adjacent Evil seats."
        }
        require(adjacentDemonSeats.intersect(adjacentMinionSeats).isEmpty()) {
            "One adjacent seat cannot be both Demon and Minion."
        }
    }
}

/**
 * Existing first-night candidate-space factor as seen from the selected Drunk seat.
 *
 * The option domain remains owned by FirstNightBundleCandidateSpaceAudit. This projection retains
 * only descriptive shape and never chooses an output or turns option count into a score.
 */
internal data class DrunkSetupFirstNightInformationFactor(
    val factorId: String,
    val kind: FirstNightBundleCandidateFactorKind,
    val control: FirstNightBundleEntryControl,
    val optionCount: Int,
) {
    init {
        require(factorId.isNotBlank()) {
            "Drunk first-night information factor ID cannot be blank."
        }
        require(optionCount >= 0) {
            "Drunk first-night information option count cannot be negative."
        }
    }

    val hasMultipleLegalOutputs: Boolean
        get() = optionCount > 1
}

internal data class DrunkSetupFirstNightInformationConsequence(
    val factors: List<DrunkSetupFirstNightInformationFactor>,
) {
    init {
        require(factors.map(DrunkSetupFirstNightInformationFactor::factorId).distinct().size == factors.size) {
            "Drunk setup consequence may contain each first-night factor at most once."
        }
    }

    val hasStorytellerControlledRoute: Boolean
        get() = factors.any {
            it.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
        }
}

/**
 * Score-free, candidate-local setup consequence surface for DLB-3B.
 *
 * This envelope is diagnostic evidence beside the Drunk setup candidate. It is descriptive only
 * and carries no recommendation-policy or selection authority.
 */
internal data class DrunkSetupConsequenceEnvelope(
    val candidateSeat: Int,
    val shownRoleId: String,
    val topology: DrunkSetupTopologyConsequence,
    val firstNightInformation: DrunkSetupFirstNightInformationConsequence,
    val excludedPlayerControlledElements: Set<String>,
    val deferredComplexities: Set<FirstNightBundleDeferredComplexity>,
) {
    init {
        require(candidateSeat > 0) { "Drunk consequence candidate seat must be positive." }
        require(shownRoleId.isNotBlank()) { "Drunk consequence shown role cannot be blank." }
        require(excludedPlayerControlledElements.none(String::isBlank)) {
            "Drunk consequence excluded player-controlled element IDs cannot be blank."
        }
    }
}

/**
 * Pure projection over authorities that already exist in DLB-2 and the first-night ecology census.
 *
 * This object never regenerates Drunk legality or information-output legality.
 */
internal object DrunkSetupConsequenceProjector {
    fun project(
        candidate: TroubleBrewingDrunkCandidate,
        hypotheticalSetup: TroubleBrewingDrunkHypotheticalSetup,
        ecologyAudit: FirstNightBundleCandidateSpaceAudit,
    ): DrunkSetupConsequenceEnvelope {
        require(candidate == hypotheticalSetup.candidate) {
            "Drunk setup consequence requires the matching DLB-2 hypothetical candidate."
        }

        val players = hypotheticalSetup.gameState.players.sortedBy { it.seat }
        require(players.size >= 3) {
            "Trouble Brewing Drunk setup consequence requires at least three seats."
        }
        require(players.map { it.seat } == (1..players.size).toList()) {
            "Trouble Brewing Drunk setup consequence requires canonical contiguous seat order."
        }

        val candidateIndex = players.indexOfFirst { it.seat == candidate.seat }
        require(candidateIndex >= 0) {
            "Drunk setup consequence candidate seat must exist in the hypothetical game."
        }
        val previous = players[(candidateIndex - 1 + players.size) % players.size]
        val next = players[(candidateIndex + 1) % players.size]
        val adjacent = listOf(previous, next)

        val ownPublicInformationFactors = ecologyAudit.factors
            .asSequence()
            .filter { factor ->
                factor.sourceSeat == candidate.seat &&
                    factor.profileExposure == FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO
            }
            .map { factor ->
                DrunkSetupFirstNightInformationFactor(
                    factorId = factor.factorId,
                    kind = factor.kind,
                    control = factor.control,
                    optionCount = factor.optionCount,
                )
            }
            .toList()

        return DrunkSetupConsequenceEnvelope(
            candidateSeat = candidate.seat,
            shownRoleId = candidate.shownRoleId,
            topology = DrunkSetupTopologyConsequence(
                previousSeat = previous.seat,
                nextSeat = next.seat,
                adjacentEvilSeats = adjacent
                    .filter { it.actualAlignment == Alignment.EVIL }
                    .mapTo(linkedSetOf()) { it.seat },
                adjacentDemonSeats = adjacent
                    .filter { it.actualType == CharacterType.DEMON }
                    .mapTo(linkedSetOf()) { it.seat },
                adjacentMinionSeats = adjacent
                    .filter { it.actualType == CharacterType.MINION }
                    .mapTo(linkedSetOf()) { it.seat },
            ),
            firstNightInformation = DrunkSetupFirstNightInformationConsequence(
                factors = ownPublicInformationFactors,
            ),
            excludedPlayerControlledElements = ecologyAudit.excludedPlayerControlledElements,
            deferredComplexities = ecologyAudit.deferredComplexities,
        )
    }
}
