package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationWitnessAlternativesTest {
    private val script = ScriptId("trouble_brewing")
    private val roles = listOf(
        role("Chef", CharacterType.TOWNSFOLK),
        role("Empath", CharacterType.TOWNSFOLK),
        role("Spy", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
        role("Recluse", CharacterType.OUTSIDER),
    ).associateBy(RoleDefinition::id)

    @Test
    fun `AnyOf preserves natural and Spy registration as separate successful witness paths`() {
        val observation = observation(
            id = "washerwoman-either",
            sourceAbility = "Washerwoman",
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Empath")),
                    InformationProposition.RoleAt(4, RoleId("Empath")),
                ),
            ),
        )
        val world = world("Chef", "Spy", "Imp", "Empath", "Recluse")

        val result = TroubleBrewingWorldObservationEvaluator.evaluate(
            world = world,
            roles = roles,
            observation = observation,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

        assertTrue(result.matches)
        assertTrue(emptySet<com.codex.campboardgamehost.clocktower.domain.RegistrationFact>() in result.registrationWitnesses)
        val spyWitnesses = result.registrationWitnesses.filter { witness ->
            witness.any { it.subjectSeat == 2 && it.reason == RegistrationReason.SPY_ABILITY }
        }
        assertEquals(1, spyWitnesses.size)
        assertEquals(RoleId("Empath"), spyWitnesses.single().single().registeredRole)
        assertEquals(
            result.registrationWitnesses.flatten().toSet(),
            result.registrationFacts,
        )
    }

    @Test
    fun `numeric result preserves complete registration witness sets instead of only flattened facts`() {
        val observation = observation(
            id = "chef-zero",
            sourceAbility = "Chef",
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = listOf(1, 2, 3, 4, 5),
                value = 0,
            ),
        )
        val world = world("Chef", "Spy", "Imp", "Empath", "Recluse")

        val result = TroubleBrewingWorldObservationEvaluator.evaluate(
            world = world,
            roles = roles,
            observation = observation,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )

        assertTrue(result.matches)
        val specialWitnesses = result.registrationWitnesses.filter { it.isNotEmpty() }
        assertTrue(specialWitnesses.any { witness ->
            witness.size == 1 &&
                witness.single().subjectSeat == 2 &&
                witness.single().reason == RegistrationReason.SPY_ABILITY
        })
        assertTrue(specialWitnesses.any { witness ->
            witness.size == 2 &&
                witness.any { it.subjectSeat == 2 && it.reason == RegistrationReason.SPY_ABILITY } &&
                witness.any { it.subjectSeat == 5 && it.reason == RegistrationReason.RECLUSE_ABILITY }
        })
        assertEquals(
            specialWitnesses.flatten().toSet(),
            result.registrationFacts,
        )
    }

    private fun observation(
        id: String,
        sourceAbility: String,
        proposition: InformationProposition,
    ): EpistemicObservation = EpistemicObservation(
        observationId = id,
        snapshotId = "sde-2b-registration-witness",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = 1,
        sourceAbility = RoleId(sourceAbility),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )

    private fun world(vararg roleNames: String): EnumeratedWorld = EnumeratedWorld(
        rolesBySeat = roleNames.mapIndexed { index, role -> index + 1 to RoleId(role) }.toMap(),
    )

    private fun role(name: String, type: CharacterType): RoleDefinition = RoleDefinition(
        id = RoleId(name),
        alignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        type = type,
        scriptIds = setOf(script),
    )
}
