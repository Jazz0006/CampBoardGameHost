package com.codex.campboardgamehost.clocktower.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MurmurHash3Test {
    @Test
    fun `x64 128 matches published reference vectors`() {
        assertHash("", "0000000000000000", "0000000000000000")
        assertHash("foo", "e271865701f54561", "7eaf87e42bba7d87")
        assertHash("hello", "cbd8a7b341bd9b02", "5b1e906a48ae1d19")
    }

    @Test
    fun `candidate id is independent of seat and registration input order`() {
        val firstRegistration = registration("interaction-a", 4)
        val secondRegistration = registration("interaction-b", 7)
        val first = StableCandidateIdFactory.create(
            candidateSchemaVersion = "1",
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            abilityRole = RoleId("Chef"),
            candidateSeats = listOf(7, 2, 4),
            numericValue = 1,
            registrations = listOf(secondRegistration, firstRegistration),
        )
        val reordered = StableCandidateIdFactory.create(
            candidateSchemaVersion = "1",
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            abilityRole = RoleId("Chef"),
            candidateSeats = listOf(4, 7, 2),
            numericValue = 1,
            registrations = listOf(firstRegistration, secondRegistration),
        )

        assertEquals(first, reordered)
        assertEquals(16, first.length)
    }

    @Test
    fun `candidate id changes when semantic truth changes`() {
        val truthful = StableCandidateIdFactory.create(
            candidateSchemaVersion = "1",
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            abilityRole = RoleId("Empath"),
            numericValue = 0,
        )
        val falsehood = StableCandidateIdFactory.create(
            candidateSchemaVersion = "1",
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            truthRelation = TruthRelation.FALSE_TO_ACTUAL_STATE,
            abilityRole = RoleId("Empath"),
            numericValue = 0,
        )

        assertNotEquals(truthful, falsehood)
    }

    @Test
    fun `candidate id has a golden vector`() {
        assertEquals(
            "ad7d94e7964ae1e2",
            StableCandidateIdFactory.create(
                candidateSchemaVersion = "1",
                abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                truthRelation = TruthRelation.FALSE_TO_ACTUAL_STATE,
                abilityRole = RoleId("Investigator"),
                shownRole = RoleId("Poisoner"),
                candidateSeats = listOf(2, 6),
            ),
        )
    }

    private fun assertHash(input: String, expectedLow: String, expectedHigh: String) {
        val hash = MurmurHash3.x64_128(input.toByteArray(Charsets.UTF_8))
        assertEquals(expectedLow, unsignedHex(hash.low64))
        assertEquals(expectedHigh, unsignedHex(hash.high64))
    }

    private fun unsignedHex(value: Long): String = java.lang.Long
        .toUnsignedString(value, 16)
        .padStart(16, '0')

    private fun registration(interactionId: String, seat: Int) = RegistrationFact(
        interactionId = interactionId,
        subjectSeat = seat,
        registeredAlignment = Alignment.EVIL,
        registrationQuestion = RegistrationQuestion.ALIGNMENT,
        reason = RegistrationReason.RECLUSE_ABILITY,
    )
}
