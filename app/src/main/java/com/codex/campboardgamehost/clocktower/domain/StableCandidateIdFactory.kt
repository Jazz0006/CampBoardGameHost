package com.codex.campboardgamehost.clocktower.domain

internal object StableCandidateIdFactory {
    fun create(
        candidateSchemaVersion: String,
        abilityState: AbilityState,
        truthRelation: TruthRelation,
        abilityRole: RoleId,
        shownRole: RoleId? = null,
        candidateSeats: Collection<Int> = emptyList(),
        numericValue: Int? = null,
        registrations: Collection<RegistrationFact> = emptyList(),
    ): String {
        require(candidateSchemaVersion.isNotBlank()) { "candidateSchemaVersion cannot be blank." }
        require(candidateSeats.all { it > 0 }) { "Candidate seats must be positive." }
        val canonical = listOf(
            candidateSchemaVersion,
            abilityState.name,
            truthRelation.name,
            abilityRole.value,
            shownRole?.value.orEmpty(),
            candidateSeats.sorted().joinToString(","),
            numericValue?.toString().orEmpty(),
            canonicalRegistrations(registrations),
        ).joinToString("|")
        return java.lang.Long
            .toUnsignedString(MurmurHash3.low64Utf8(canonical), 16)
            .padStart(16, '0')
    }

    private fun canonicalRegistrations(registrations: Collection<RegistrationFact>): String = registrations
        .sortedBy { it.interactionId }
        .joinToString(";") { fact ->
            listOf(
                fact.interactionId,
                fact.subjectSeat.toString(),
                fact.registeredRole?.value.orEmpty(),
                fact.registeredType?.name.orEmpty(),
                fact.registeredAlignment?.name.orEmpty(),
                fact.registrationQuestion.name,
                fact.reason.name,
            ).joinToString("~")
        }
}
