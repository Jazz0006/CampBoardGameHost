package com.codex.campboardgamehost.clocktower.presentation

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate

/**
 * UI-facing structured view of the already-authoritative pair-information legal domain.
 *
 * This layer does not decide legality, truth, registration, recommendation quality, or automation
 * policy. It only makes the supplied legal candidates convenient for a structured manual picker
 * and resolves a Storyteller choice back to the exact original semantic candidate.
 */
internal object PairInformationManualSelection {
    fun fromLegalCandidates(
        candidates: List<PairInformationLegalCandidate>,
    ): PairInformationManualSelectionModel {
        require(candidates.map { it.candidateId }.distinct().size == candidates.size) {
            "Manual pair selection requires unique legal candidate IDs."
        }

        val zeroCandidates = candidates.filter { candidate ->
            candidate.outcome.shownRole == null && candidate.outcome.candidateSeats.isEmpty()
        }
        require(zeroCandidates.size <= 1) {
            "Pair-information legal domain may contain at most one zero-result candidate."
        }

        val roleChoices = candidates
            .asSequence()
            .filter { it.outcome.shownRole != null }
            .groupBy { requireNotNull(it.outcome.shownRole) }
            .toSortedMap(compareBy(RoleId::value))
            .map { (shownRole, roleCandidates) ->
                PairInformationManualRoleChoice(
                    shownRole = shownRole,
                    candidates = roleCandidates.sortedWith(
                        compareBy<PairInformationLegalCandidate>(
                            { it.outcome.candidateSeats.getOrNull(0) ?: Int.MAX_VALUE },
                            { it.outcome.candidateSeats.getOrNull(1) ?: Int.MAX_VALUE },
                            { it.candidateId },
                        ),
                    ),
                )
            }

        return PairInformationManualSelectionModel(
            roleChoices = roleChoices,
            zeroResultCandidate = zeroCandidates.singleOrNull(),
        )
    }
}

internal data class PairInformationManualRoleChoice(
    val shownRole: RoleId,
    val candidates: List<PairInformationLegalCandidate>,
) {
    init {
        require(candidates.isNotEmpty())
        require(candidates.all { it.outcome.shownRole == shownRole }) {
            "Every manual role choice candidate must match its shown role."
        }
    }

    val seatPairs: List<Pair<Int, Int>> = candidates.map { candidate ->
        val seats = candidate.outcome.candidateSeats
        require(seats.size == 2) { "A role-based pair-information candidate must contain two seats." }
        seats[0] to seats[1]
    }
}

internal data class PairInformationManualSelectionModel(
    val roleChoices: List<PairInformationManualRoleChoice>,
    val zeroResultCandidate: PairInformationLegalCandidate?,
) {
    val allCandidates: List<PairInformationLegalCandidate> =
        roleChoices.flatMap { it.candidates } + listOfNotNull(zeroResultCandidate)

    fun resolve(
        shownRole: RoleId,
        firstSeat: Int,
        secondSeat: Int,
    ): PairInformationLegalCandidate? {
        if (firstSeat == secondSeat) return null
        val selectedSeats = listOf(firstSeat, secondSeat).sorted()
        return roleChoices
            .firstOrNull { it.shownRole == shownRole }
            ?.candidates
            ?.firstOrNull { candidate -> candidate.outcome.candidateSeats == selectedSeats }
    }
}
