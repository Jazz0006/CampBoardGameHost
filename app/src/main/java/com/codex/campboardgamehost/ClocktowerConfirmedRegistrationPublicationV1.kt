package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationResolutionStatusV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.referencedSeats

/**
 * Confirmed player-facing result is separate from all legal registration explanations.
 * This pure plan is only an input to Host rules verification, never a committed ruling.
 */
internal data class ClocktowerConfirmedRegistrationChoiceV1(
    val subjectSeat: Int,
    val question: RegistrationQuestion,
    val status: RegistrationResolutionStatusV1,
    val selectedRole: RoleId? = null,
) {
    init {
        require(subjectSeat > 0)
        require(status in setOf(
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
            RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
        ))
        require(status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL || selectedRole == null)
    }
}

internal data class ClocktowerConfirmedRegistrationPublicationV1(
    val interactionId: String,
    val observationRecordId: String,
    val sourceSeat: Int,
    val shownProposition: InformationProposition,
    val choices: List<ClocktowerConfirmedRegistrationChoiceV1>,
    val legalResultWitnesses: List<ClocktowerRegistrationWitness>,
) {
    init {
        require(interactionId.isNotBlank() && observationRecordId.isNotBlank() && sourceSeat > 0)
        require(choices.isNotEmpty() && choices.map { it.subjectSeat }.distinct().size == choices.size)
        require(legalResultWitnesses.isNotEmpty())
    }
}

internal sealed interface ClocktowerResultRegistrationPlanV1 {
    data object NoVerifiedWitness : ClocktowerResultRegistrationPlanV1
    data object ConflictingManualChoice : ClocktowerResultRegistrationPlanV1
    data class Ready(
        val choices: List<ClocktowerConfirmedRegistrationChoiceV1>,
        val legalResultWitnesses: List<ClocktowerRegistrationWitness>,
    ) : ClocktowerResultRegistrationPlanV1 {
        init { require(choices.isNotEmpty() && legalResultWitnesses.isNotEmpty()) }
    }
}

/**
 * The result must match one uniquely identified *visible option*, preserving its entire legal
 * witness set. A manual choice without such evidence blocks reveal instead of quietly disappearing.
 * Neither a list's first element nor an auto-filled role may become an explicit choice.
 */
internal fun clocktowerPlanConfirmedResultRegistrations(
    shownProposition: InformationProposition?,
    shownKind: ClocktowerDisplayKind,
    shownPrimary: String?,
    legalCandidates: List<ClocktowerDisplayOption>,
    spy: ClocktowerManualRegistrationChoice?,
    spySeat: Int?,
    spyQuestion: RegistrationQuestion,
    recluse: ClocktowerManualRegistrationChoice?,
    recluseSeat: Int?,
    recluseQuestion: RegistrationQuestion,
): ClocktowerResultRegistrationPlanV1 {
    val hasManual = spy != null || recluse != null
    if (shownProposition == null || legalCandidates.isEmpty()) {
        return if (hasManual) ClocktowerResultRegistrationPlanV1.ConflictingManualChoice
            else ClocktowerResultRegistrationPlanV1.NoVerifiedWitness
    }
    val matches = distinctClocktowerFinalInformationResults(legalCandidates).filter {
        it.proposition == shownProposition && it.displayKind == shownKind &&
            it.displayPrimary == shownPrimary
    }
    if (matches.size != 1 || matches.single().legalRegistrationWitnesses.isEmpty()) {
        return if (hasManual) ClocktowerResultRegistrationPlanV1.ConflictingManualChoice
            else ClocktowerResultRegistrationPlanV1.NoVerifiedWitness
    }
    val witnesses = matches.single().legalRegistrationWitnesses
    fun matchesChoice(witness: ClocktowerRegistrationWitness): Boolean =
        (spy == null || witness.spyRegistersGood == spy.usesSpecialRegistration) &&
        (recluse == null || witness.recluseRegistersEvil == recluse.usesSpecialRegistration) &&
        (spy?.selectedRegisteredRoleEnName == null ||
            witness.spyRegisteredRoleEnName == spy.selectedRegisteredRoleEnName) &&
        (recluse?.selectedRegisteredRoleEnName == null ||
            witness.recluseRegisteredRoleEnName == recluse.selectedRegisteredRoleEnName)
    if (hasManual && witnesses.none(::matchesChoice)) {
        return ClocktowerResultRegistrationPlanV1.ConflictingManualChoice
    }
    val resultSeats = shownProposition.referencedSeats()
    val choices = buildList {
        if (spySeat != null && spySeat in resultSeats &&
            witnesses.any { it.spyRegistersGood != null }
        ) {
            val status = when (spy?.usesSpecialRegistration) {
                true -> RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
                false -> RegistrationResolutionStatusV1.EXPLICIT_ACTUAL
                null -> RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED
            }
            add(ClocktowerConfirmedRegistrationChoiceV1(
                spySeat, spyQuestion, status,
                spy?.selectedRegisteredRoleEnName?.takeIf { status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL }
                    ?.let(::RoleId),
            ))
        }
        if (recluseSeat != null && recluseSeat in resultSeats &&
            witnesses.any { it.recluseRegistersEvil != null }
        ) {
            val status = when (recluse?.usesSpecialRegistration) {
                true -> RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
                false -> RegistrationResolutionStatusV1.EXPLICIT_ACTUAL
                null -> RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED
            }
            add(ClocktowerConfirmedRegistrationChoiceV1(
                recluseSeat, recluseQuestion, status,
                recluse?.selectedRegisteredRoleEnName
                    ?.takeIf { status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL }?.let(::RoleId),
            ))
        }
    }
    if (choices.isEmpty()) {
        return if (hasManual) ClocktowerResultRegistrationPlanV1.ConflictingManualChoice
            else ClocktowerResultRegistrationPlanV1.NoVerifiedWitness
    }
    return ClocktowerResultRegistrationPlanV1.Ready(choices, witnesses)
}
