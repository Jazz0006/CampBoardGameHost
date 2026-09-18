package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
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

class TroubleBrewingTopologyObservationWitnessEvaluatorTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-observation",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun Spy_can_satisfy_good_role_observation_only_through_special_registration() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(7, RoleId("Spy"))),
        )
        val observation = observation(
            proposition = InformationProposition.RoleAt(7, RoleId("Empath")),
            sourceSeat = null,
        )

        val result = evaluate(profile, topology, knowledge, observation)

        assertTrue(result is TroubleBrewingTopologyObservationFeasibility.Feasible)
        result as TroubleBrewingTopologyObservationFeasibility.Feasible
        assertEquals(1, result.registrationWitnesses.size)
        val fact = result.registrationWitnesses.single().single()
        assertEquals(7, fact.subjectSeat)
        assertEquals(RoleId("Empath"), fact.registeredRole)
        assertEquals(RegistrationReason.SPY_ABILITY, fact.reason)
    }

    @Test
    fun poisoned_Spy_cannot_use_special_registration() {
        val profile = TroubleBrewingSetupProfiles.standard(10)
        val topology = StrategicWorldKey(demonSeat = 10, minionSeats = listOf(8, 9))
        val knowledge = knowledge(
            profile,
            listOf(
                InformationProposition.RoleAt(8, RoleId("Spy")),
                InformationProposition.RoleAt(9, RoleId("Poisoner")),
                InformationProposition.AbilityStateAt(
                    8,
                    RoleId("Spy"),
                    AbilityState.MALFUNCTIONING_POISONED,
                ),
            ),
        )
        val observation = observation(
            proposition = InformationProposition.RoleAt(8, RoleId("Empath")),
            sourceSeat = null,
        )

        assertEquals(
            TroubleBrewingTopologyObservationFeasibility.Infeasible,
            evaluate(profile, topology, knowledge, observation),
        )
    }

    @Test
    fun Recluse_can_register_as_Demon_type_when_functioning() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(3, RoleId("Recluse"))),
        )
        val observation = observation(
            proposition = InformationProposition.CharacterTypeAt(3, CharacterType.DEMON),
            sourceSeat = null,
        )

        val result = evaluate(profile, topology, knowledge, observation)

        assertTrue(result is TroubleBrewingTopologyObservationFeasibility.Feasible)
        result as TroubleBrewingTopologyObservationFeasibility.Feasible
        val fact = result.registrationWitnesses.single().single()
        assertEquals(RegistrationReason.RECLUSE_ABILITY, fact.reason)
        assertEquals(CharacterType.DEMON, fact.registeredType)
    }

    @Test
    fun selected_type_only_Recluse_witness_matches_without_forcing_a_specific_Demon_role() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(3, RoleId("Recluse"))),
        )
        val observation = observation(
            proposition = InformationProposition.CharacterTypeAt(3, CharacterType.DEMON),
            sourceSeat = null,
        )
        val selected = setOf(
            RegistrationFact(
                interactionId = "generator-type-only",
                subjectSeat = 3,
                registeredType = CharacterType.DEMON,
                registeredAlignment = com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                registrationQuestion = RegistrationQuestion.CHARACTER_TYPE,
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

    @Test
    fun selected_empty_registration_witness_rejects_a_Spy_only_explanation() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(7, RoleId("Spy"))),
        )
        val observation = observation(
            proposition = InformationProposition.RoleAt(7, RoleId("Empath")),
            sourceSeat = null,
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
    fun selected_semantic_Spy_witness_accepts_even_when_interaction_provenance_differs() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleAt(7, RoleId("Spy"))),
        )
        val observation = observation(
            proposition = InformationProposition.RoleAt(7, RoleId("Empath")),
            sourceSeat = null,
        )
        val selected = setOf(
            RegistrationFact(
                interactionId = "generator-local",
                subjectSeat = 7,
                registeredRole = RoleId("Empath"),
                registeredType = CharacterType.TOWNSFOLK,
                registeredAlignment = com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD,
                registrationQuestion = RegistrationQuestion.SPECIFIC_MINION,
                reason = RegistrationReason.SPY_ABILITY,
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

    @Test
    fun mechanically_credible_received_information_keeps_a_Drunk_source_malfunction_branch() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(
                InformationProposition.RoleAt(1, RoleId("Drunk")),
                InformationProposition.ShownRoleAt(1, RoleId("Chef")),
            ),
        )
        val mechanicallyFalse = observation(
            proposition = InformationProposition.RoleAt(8, RoleId("Empath")),
            sourceSeat = 1,
        )

        val credible = evaluate(
            profile,
            topology,
            knowledge,
            mechanicallyFalse,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        val functioningOnly = evaluate(
            profile,
            topology,
            knowledge,
            mechanicallyFalse,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
        )

        assertTrue(credible is TroubleBrewingTopologyObservationFeasibility.Feasible)
        credible as TroubleBrewingTopologyObservationFeasibility.Feasible
        assertEquals(setOf(emptySet<RegistrationFact>()), credible.registrationWitnesses)
        assertEquals(TroubleBrewingTopologyObservationFeasibility.Infeasible, functioningOnly)
    }

    @Test
    fun unsupported_numeric_observation_defers_in_D4C4a() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(profile, emptyList())
        val observation = observation(
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = (1..8).toList(),
                value = 1,
            ),
            sourceSeat = 1,
        )

        val result = evaluate(profile, topology, knowledge, observation)

        assertTrue(result is TroubleBrewingTopologyObservationFeasibility.Deferred)
    }

    private fun evaluate(
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        knowledge: PlayerKnowledgeSnapshot,
        observation: EpistemicObservation,
        selectedWitness: Set<RegistrationFact>? = null,
        hypothesis: EpistemicHypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    ) = TroubleBrewingTopologyObservationWitnessEvaluator.evaluate(
        rulesetRef = ruleset,
        knowledge = knowledge,
        profile = profile,
        topology = topology,
        roleDefinitions = roles,
        observation = observation,
        selectedRegistrationWitness = selectedWitness,
        hypothesis = hypothesis,
    )

    private fun knowledge(
        profile: InformationProposition.SetupProfile,
        extra: List<InformationProposition>,
    ): PlayerKnowledgeSnapshot {
        val playerCount = profile.townsfolk + profile.outsiders + profile.minions + profile.demons
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "d4c4a-$playerCount",
            formalSnapshotId = "d4c4a-snapshot-$playerCount",
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
        sourceSeat: Int?,
    ) = EpistemicObservation(
        observationId = "d4c4a-observation",
        snapshotId = "d4c4a-snapshot-8",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = sourceSeat,
        sourceAbility = sourceSeat?.let { RoleId("Chef") },
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )
}
