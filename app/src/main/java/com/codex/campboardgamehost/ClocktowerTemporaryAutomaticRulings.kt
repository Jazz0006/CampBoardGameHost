package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryAutomaticChoice
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryAutomaticSelection
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryAutomaticStorytellerPolicy
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryDemonSuccessorChoice

internal data class ClocktowerAutomaticRegistrationRuling(
    val usesSpecialRegistration: Boolean,
    val registeredRoleEnName: String?,
)

internal data class ClocktowerTemporaryMayorCandidate(
    val seat: Int,
    val team: ClocktowerTeam,
    val alive: Boolean,
) {
    init {
        require(seat > 0) { "Mayor automatic candidate seat must be positive." }
    }
}

internal fun clocktowerTemporaryAutomaticDecisionSeed(decisionKey: String): Long {
    require(decisionKey.isNotBlank()) { "Temporary automatic decision key cannot be blank." }
    return MurmurHash3.low64Utf8("ux-mode-1-temporary-auto-v1|$decisionKey")
}

internal fun clocktowerTemporaryRegistrationSelection(
    legalSpecialRoleEnNames: List<String>,
    decisionKey: String,
): TemporaryAutomaticSelection<ClocktowerAutomaticRegistrationRuling> {
    val specialRoles = legalSpecialRoleEnNames
        .onEach { require(it.isNotBlank()) { "Registration role cannot be blank." } }
        .distinct()
        .sorted()
    return TemporaryAutomaticStorytellerPolicy.selectRegistration(
        actual = TemporaryAutomaticChoice(
            candidateId = "actual-registration",
            payload = ClocktowerAutomaticRegistrationRuling(
                usesSpecialRegistration = false,
                registeredRoleEnName = null,
            ),
        ),
        special = specialRoles.map { roleEnName ->
            TemporaryAutomaticChoice(
                candidateId = "special-registration:$roleEnName",
                payload = ClocktowerAutomaticRegistrationRuling(
                    usesSpecialRegistration = true,
                    registeredRoleEnName = roleEnName,
                ),
            )
        },
        decisionSeed = clocktowerTemporaryAutomaticDecisionSeed(decisionKey),
    )
}

internal fun clocktowerTemporaryMayorEligibleTownsfolkSeats(
    candidates: List<ClocktowerTemporaryMayorCandidate>,
    mayorSeat: Int,
): List<Int> {
    require(mayorSeat > 0) { "Mayor seat must be positive." }
    return candidates
        .asSequence()
        .filter { candidate ->
            candidate.alive &&
                candidate.team == ClocktowerTeam.Townsfolk &&
                candidate.seat != mayorSeat
        }
        .map(ClocktowerTemporaryMayorCandidate::seat)
        .distinct()
        .sorted()
        .toList()
}

internal fun clocktowerTemporaryMayorSelection(
    mayorSeat: Int,
    livingTownsfolkSeats: List<Int>,
    decisionKey: String,
): TemporaryAutomaticSelection<Int> = TemporaryAutomaticStorytellerPolicy.selectMayorRedirect(
    mayorSeat = mayorSeat,
    livingTownsfolkSeats = livingTownsfolkSeats.distinct().sorted(),
    decisionSeed = clocktowerTemporaryAutomaticDecisionSeed(decisionKey),
)

internal fun clocktowerTemporaryDemonSuccessorSelection(
    eligible: List<TemporaryDemonSuccessorChoice>,
    decisionKey: String,
): TemporaryAutomaticSelection<TemporaryDemonSuccessorChoice>? =
    TemporaryAutomaticStorytellerPolicy.selectDemonSuccessor(
        eligible = eligible.sortedBy { it.seat },
        decisionSeed = clocktowerTemporaryAutomaticDecisionSeed(decisionKey),
    )

internal fun temporaryDemonSuccessorChoice(
    seat: Int,
    roleEnName: String,
): TemporaryDemonSuccessorChoice = TemporaryDemonSuccessorChoice(
    seat = seat,
    role = RoleId(roleEnName),
)
