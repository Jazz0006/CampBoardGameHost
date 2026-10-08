package com.codex.campboardgamehost

/**
 * An explanation that COULD legally produce an observed result. It is not a Storyteller ruling.
 * Scope is the current ability interaction, never a permanent property of Spy or Recluse.
 */
internal data class ClocktowerRegistrationWitness(
    val spyRegistersGood: Boolean? = null,
    val spyRegisteredRoleEnName: String? = null,
    val recluseRegistersEvil: Boolean? = null,
    val recluseRegisteredRoleEnName: String? = null,
)

/**
 * Result-first means selecting a result does NOT select an arbitrary legal witness.
 * Keep ALL possible explanations explicitly on the projected option, and erase the old witness
 * fields so no consumer can accidentally commit/display the first enumeration as canonical.
 * The same grouping applies to numeric, boolean and role-reveal propositions.
 */
internal fun distinctClocktowerFinalInformationResults(
    candidates: List<ClocktowerDisplayOption>,
): List<ClocktowerDisplayOption> = candidates
    .groupBy(::clocktowerFinalInformationResultId)
    .values.map { alternatives ->
        val witnesses = alternatives.flatMap { candidate ->
            candidate.legalRegistrationWitnesses.ifEmpty {
                if (
                    candidate.spyRegistersGood != null || candidate.recluseRegistersEvil != null ||
                    candidate.spyRegisteredRoleEnName != null || candidate.recluseRegisteredRoleEnName != null
                ) {
                    listOf(ClocktowerRegistrationWitness(
                        spyRegistersGood = candidate.spyRegistersGood,
                        spyRegisteredRoleEnName = candidate.spyRegisteredRoleEnName,
                        recluseRegistersEvil = candidate.recluseRegistersEvil,
                        recluseRegisteredRoleEnName = candidate.recluseRegisteredRoleEnName,
                    ))
                } else emptyList()
            }
        }.distinct()
        alternatives.first().copy(
            spyRegistersGood = null,
            spyRegisteredRoleEnName = null,
            recluseRegistersEvil = null,
            recluseRegisteredRoleEnName = null,
            legalRegistrationWitnesses = witnesses,
        )
    }

internal fun clocktowerFinalInformationResultId(option: ClocktowerDisplayOption): String = listOf(
    option.displayKind.name,
    option.proposition?.toString().orEmpty(),
    option.displayPrimary.orEmpty(),
    option.displaySecondary.orEmpty(),
    option.displayFooter.orEmpty(),
).joinToString("|")

internal data class ClocktowerAlignmentRegistrationWitness(
    val spyRegistersGood: Boolean?,
    val recluseRegistersEvil: Boolean?,
)

/** Enumeration may prefer the current UI state, but order never grants a candidate adjudication authority. */
internal fun clocktowerAlignmentRegistrationWitnesses(
    currentSpyRegistersGood: Boolean,
    spySelectable: Boolean,
    currentRecluseRegistersEvil: Boolean,
    recluseSelectable: Boolean,
): List<ClocktowerAlignmentRegistrationWitness> {
    val spyValues: List<Boolean?> = if (spySelectable) {
        listOf(currentSpyRegistersGood, !currentSpyRegistersGood)
    } else {
        listOf(null)
    }
    val recluseValues: List<Boolean?> = if (recluseSelectable) {
        listOf(currentRecluseRegistersEvil, !currentRecluseRegistersEvil)
    } else {
        listOf(null)
    }
    return spyValues.flatMap { spyGood ->
        recluseValues.map { recluseEvil ->
            ClocktowerAlignmentRegistrationWitness(
                spyRegistersGood = spyGood,
                recluseRegistersEvil = recluseEvil,
            )
        }
    }
}

internal fun ClocktowerNightStepUi.usesResultFirstRegistrationDomain(): Boolean =
    manualInformationCandidates.isNotEmpty() &&
        (spyRegistrationKey != null || recluseRegistrationKey != null)
