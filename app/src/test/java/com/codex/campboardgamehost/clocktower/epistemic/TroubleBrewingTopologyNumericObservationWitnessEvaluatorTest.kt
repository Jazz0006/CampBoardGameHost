package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingTopologyNumericObservationWitnessEvaluatorTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-numeric",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun Chef_zero_can_require_a_Spy_to_register_good() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val topology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(6))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(6, RoleId("Spy"))),
        )
        val observation = observation(
            InformationProposition.NumericResult(
                metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = (1..7).toList(),
                value = 0,
            ),
        )

        val result = evaluate(profile, topology, knowledge, observation)

        assertTrue(result is TroubleBrewingTopologyObservationFeasibility.Feasible)
        result as TroubleBrewingTopologyObservationFeasibility.Feasible
        val witness = result.registrationWitnesses.single()
        val fact = witness.single()
        assertEquals(6, fact.subjectSeat)
        assertEquals(Alignment.GOOD, fact.registeredAlignment)
        assertEquals(RegistrationReason.SPY_ABILITY, fact.reason)
    }

    @Test
    fun Empath_two_can_require_a_Recluse_to_register_evil() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(2, RoleId("Recluse"))),
        )
        val observation = observation(
            InformationProposition.NumericResult(
                metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat = 1,
                subjectSeats = listOf(7, 2),
                value = 2,
            ),
        )

        val result = evaluate(profile, topology, knowledge, observation)

        assertTrue(result is TroubleBrewingTopologyObservationFeasibility.Feasible)
        result as TroubleBrewingTopologyObservationFeasibility.Feasible
        val fact = result.registrationWitnesses.single().single()
        assertEquals(2, fact.subjectSeat)
        assertEquals(Alignment.EVIL, fact.registeredAlignment)
        assertEquals(RegistrationReason.RECLUSE_ABILITY, fact.reason)
    }

    @Test
    fun selected_empty_witness_rejects_a_numeric_result_that_requires_special_registration() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val topology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(6))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(6, RoleId("Spy"))),
        )
        val observation = observation(
            InformationProposition.NumericResult(
                metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = (1..7).toList(),
                value = 0,
            ),
        )

        assertEquals(
            TroubleBrewingTopologyObservationFeasibility.Infeasible,
            evaluate(
                profile,
                topology,
                knowledge,
                observation,
                selectedWitness = emptySet(),
            ),
        )
    }

    @Test
    fun Fortune_Teller_yes_can_be_explained_by_a_Red_Herring_without_registration() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleInPlay(RoleId("Fortune Teller"), true)),
        )
        val observation = observation(
            InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 1,
                subjectSeats = listOf(2, 3),
                value = true,
            ),
        )

        val result = evaluate(
            profile,
            topology,
            knowledge,
            observation,
            selectedWitness = emptySet(),
        )

        assertTrue(result is TroubleBrewingTopologyObservationFeasibility.Feasible)
        result as TroubleBrewingTopologyObservationFeasibility.Feasible
        assertEquals(setOf(emptySet<RegistrationFact>()), result.registrationWitnesses)
    }

    @Test
    fun Fortune_Teller_no_is_impossible_when_the_actual_Demon_is_selected() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val observation = observation(
            InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 1,
                subjectSeats = listOf(8, 2),
                value = false,
            ),
        )

        assertEquals(
            TroubleBrewingTopologyObservationFeasibility.Infeasible,
            evaluate(
                profile,
                topology,
                knowledge(profile, emptyList()),
                observation,
                selectedWitness = emptySet(),
            ),
        )
    }

    @Test
    fun Fortune_Teller_yes_can_use_Recluse_Demon_registration_when_no_Demon_or_Red_Herring_is_selected() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(
                InformationProposition.RoleAt(2, RoleId("Recluse")),
                InformationProposition.RoleInPlay(RoleId("Fortune Teller"), false),
            ),
        )
        val observation = observation(
            InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 1,
                subjectSeats = listOf(2, 3),
                value = true,
            ),
        )
        val selected = setOf(
            RegistrationFact(
                interactionId = "generator-ft",
                subjectSeat = 2,
                registeredType = CharacterType.DEMON,
                registeredAlignment = Alignment.EVIL,
                registrationQuestion = RegistrationQuestion.DEMON,
                reason = RegistrationReason.RECLUSE_ABILITY,
            ),
        )

        assertTrue(
            evaluate(
                profile,
                topology,
                knowledge,
                observation,
                selectedWitness = selected,
            ) is TroubleBrewingTopologyObservationFeasibility.Feasible,
        )
    }

    private fun evaluate(
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        knowledge: PlayerKnowledgeSnapshot,
        observation: EpistemicObservation,
        selectedWitness: Set<RegistrationFact>? = null,
    ) = TroubleBrewingTopologyObservationWitnessEvaluator.evaluate(
        rulesetRef = ruleset,
        knowledge = knowledge,
        profile = profile,
        topology = topology,
        roleDefinitions = roles,
        observation = observation,
        selectedRegistrationWitness = selectedWitness,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    )

    private fun knowledge(
        profile: InformationProposition.SetupProfile,
        extra: List<InformationProposition>,
    ): PlayerKnowledgeSnapshot {
        val playerCount = profile.townsfolk + profile.outsiders + profile.minions + profile.demons
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "d4c4b-$playerCount",
            formalSnapshotId = "d4c4b-snapshot-$playerCount",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(
                InformationProposition.PlayerCount(playerCount),
                profile,
            ) + extra,
        )
    }

    private fun observation(
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = "d4c4b-observation",
        snapshotId = "d4c4b-snapshot",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = null,
        sourceAbility = RoleId("Chef"),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )
}
