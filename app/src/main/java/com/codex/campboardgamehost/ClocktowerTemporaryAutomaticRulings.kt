package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCandidate
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryAutomaticChoice
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryAutomaticSelection
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryAutomaticStorytellerPolicy
import com.codex.campboardgamehost.clocktower.recommendation.TemporaryDemonSuccessorChoice
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationResolution

private const val ACTUAL_REGISTRATION_FAMILY = "actual-registration"
private const val SPECIAL_REGISTRATION_FAMILY = "special-registration"

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

internal fun clocktowerTemporaryNightDecisionKey(
    gameId: String,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    family: String,
): String {
    require(gameId.isNotBlank()) { "Temporary automatic game ID cannot be blank." }
    require(round > 0) { "Temporary automatic round must be positive." }
    require(sequence >= 0) { "Temporary automatic sequence cannot be negative." }
    require(family.isNotBlank()) { "Temporary automatic decision family cannot be blank." }
    return listOf(gameId, phase.name, round.toString(), sequence.toString(), family).joinToString("|")
}

internal fun clocktowerTemporaryRegistrationDecisionKey(
    gameId: String,
    phase: ClocktowerPhase,
    round: Int,
    registrationKey: String,
): String {
    require(registrationKey.isNotBlank()) { "Temporary automatic registration key cannot be blank." }
    return clocktowerTemporaryNightDecisionKey(
        gameId = gameId,
        phase = phase,
        round = round,
        sequence = 0,
        family = "registration:$registrationKey",
    )
}

internal fun clocktowerTemporaryAutomaticDecisionSeed(decisionKey: String): Long {
    require(decisionKey.isNotBlank()) { "Temporary automatic decision key cannot be blank." }
    return MurmurHash3.low64Utf8("ux-mode-1-temporary-auto-v1|$decisionKey")
}

private fun clocktowerTemporaryRegistrationChoices(
    registration: TroubleBrewingRegistrationResolution,
): List<TemporaryAutomaticChoice<ClocktowerAutomaticRegistrationRuling>> {
    val specialRoles = registration.special
        .map { it.registeredRole.value }
        .distinct()
        .sorted()
    return buildList {
        add(
            TemporaryAutomaticChoice(
                candidateId = ACTUAL_REGISTRATION_FAMILY,
                payload = ClocktowerAutomaticRegistrationRuling(
                    usesSpecialRegistration = false,
                    registeredRoleEnName = null,
                ),
            ),
        )
        specialRoles.forEach { roleEnName ->
            add(
                TemporaryAutomaticChoice(
                    candidateId = "$SPECIAL_REGISTRATION_FAMILY:$roleEnName",
                    payload = ClocktowerAutomaticRegistrationRuling(
                        usesSpecialRegistration = true,
                        registeredRoleEnName = roleEnName,
                    ),
                ),
            )
        }
    }
}

internal fun clocktowerTemporaryRegistrationAuditCandidates(
    registration: TroubleBrewingRegistrationResolution,
): List<SelectionAuditCandidate> = clocktowerTemporaryRegistrationChoices(registration).map { choice ->
    SelectionAuditCandidate(
        familyId = clocktowerTemporaryRegistrationAuditFamilyId(choice.payload),
        qualityTier = QualityTier.RECOMMENDED,
    )
}

internal fun clocktowerTemporaryRegistrationAuditFamilyId(
    ruling: ClocktowerAutomaticRegistrationRuling,
): String = if (ruling.usesSpecialRegistration) SPECIAL_REGISTRATION_FAMILY else ACTUAL_REGISTRATION_FAMILY

internal fun clocktowerTemporaryRegistrationSelection(
    registration: TroubleBrewingRegistrationResolution,
    decisionKey: String,
): TemporaryAutomaticSelection<ClocktowerAutomaticRegistrationRuling> {
    val choices = clocktowerTemporaryRegistrationChoices(registration)
    return TemporaryAutomaticStorytellerPolicy.selectRegistration(
        actual = choices.first(),
        special = choices.drop(1),
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

internal fun clocktowerAutomaticMayorRulingShouldAdvance(
    automaticStorytellerInfo: Boolean,
    action: ClocktowerNightAction,
    selectedName: String?,
    automaticTargetName: String?,
): Boolean =
    automaticStorytellerInfo &&
        action in setOf(ClocktowerNightAction.MayorRedirect, ClocktowerNightAction.DemonSuccessor) &&
        automaticTargetName != null &&
        selectedName == automaticTargetName

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
