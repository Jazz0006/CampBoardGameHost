package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingTopologyObservationDifferentialTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rolesById = roles.associateBy(RoleDefinition::id)
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-observation-differential",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun topology_first_observation_feasibility_matches_bounded_exhaustive_projection() {
        val cases = listOf(
            Case(
                profile = TroubleBrewingSetupProfiles.standard(5),
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Empath")),
                    InformationProposition.RoleAt(3, RoleId("Fortune Teller")),
                ),
                observation = observation(
                    "spy-role",
                    InformationProposition.RoleAt(4, RoleId("Empath")),
                    sourceSeat = null,
                ),
            ),
            Case(
                profile = TroubleBrewingSetupProfiles.standard(5),
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Empath")),
                    InformationProposition.RoleAt(3, RoleId("Fortune Teller")),
                ),
                observation = observation(
                    "chef-zero",
                    InformationProposition.NumericResult(
                        metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                        sourceSeat = 1,
                        subjectSeats = (1..5).toList(),
                        value = 0,
                    ),
                    sourceSeat = null,
                ),
            ),
            Case(
                profile = TroubleBrewingSetupProfiles.standard(5),
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Fortune Teller")),
                    InformationProposition.RoleAt(3, RoleId("Empath")),
                ),
                observation = observation(
                    "ft-red-herring",
                    InformationProposition.BooleanResult(
                        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        sourceSeat = 2,
                        subjectSeats = listOf(1, 3),
                        value = true,
                    ),
                    sourceSeat = null,
                ),
                selectedWitness = emptySet(),
            ),
            Case(
                profile = TroubleBrewingSetupProfiles.withBaron(5),
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Recluse")),
                    InformationProposition.RoleAt(3, RoleId("Butler")),
                ),
                observation = observation(
                    "ft-recluse",
                    InformationProposition.BooleanResult(
                        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        sourceSeat = 1,
                        subjectSeats = listOf(2, 3),
                        value = true,
                    ),
                    sourceSeat = null,
                ),
                selectedWitness = setOf(
                    RegistrationFact(
                        interactionId = "generator-recluse",
                        subjectSeat = 2,
                        registeredType = CharacterType.DEMON,
                        registeredAlignment =
                            com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                        registrationQuestion = RegistrationQuestion.DEMON,
                        reason = RegistrationReason.RECLUSE_ABILITY,
                    ),
                ),
            ),
            Case(
                profile = TroubleBrewingSetupProfiles.withBaron(5),
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Drunk")),
                    InformationProposition.ShownRoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Butler")),
                ),
                observation = observation(
                    "drunk-source",
                    InformationProposition.RoleAt(5, RoleId("Empath")),
                    sourceSeat = 1,
                ),
            ),
        )

        cases.forEachIndexed { index, testCase ->
            val knowledge = knowledge(testCase.profile, testCase.setup, index)
            val worlds = TroubleBrewingWorldEnumerator.enumerate(
                rulesetRef = ruleset,
                knowledge = knowledge,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
            ).enumeratedWorlds()

            val exhaustiveKeys = worlds.filter { world ->
                val result = TroubleBrewingWorldObservationEvaluator.evaluate(
                    world = world,
                    roles = rolesById,
                    observation = testCase.observation,
                    hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                )
                result.matches && (
                    testCase.selectedWitness == null ||
                        result.registrationWitnesses.any { witness ->
                            RegistrationWitnessSemanticMatcher.matches(
                                selected = testCase.selectedWitness,
                                exactWitness = witness,
                                roles = rolesById,
                            )
                        }
                    )
            }.mapTo(linkedSetOf()) { world ->
                StrategicWorldKey.from(world, rolesById)
            }

            val topologyKeys = TroubleBrewingStrategicTopologyDomain
                .enumerate(5, testCase.profile)
                .filterTo(linkedSetOf()) { topology ->
                    TroubleBrewingTopologyObservationWitnessEvaluator.evaluate(
                        rulesetRef = ruleset,
                        knowledge = knowledge,
                        profile = testCase.profile,
                        topology = topology,
                        roleDefinitions = roles,
                        observation = testCase.observation,
                        selectedRegistrationWitness = testCase.selectedWitness,
                        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                    ) is TroubleBrewingTopologyObservationFeasibility.Feasible
                }

            assertEquals(
                "Topology observation differential drifted in case " +
                    index + " (" + testCase.observation.observationId + ").",
                exhaustiveKeys,
                topologyKeys,
            )
        }
    }

    private fun knowledge(
        profile: InformationProposition.SetupProfile,
        extra: List<InformationProposition>,
        index: Int,
    ) = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "d4d-observation-$index",
        formalSnapshotId = "d4d-observation-snapshot-$index",
        recipientSeat = 1,
        perceivedRole = RoleId("Chef"),
        setupKnowledge = listOf(
            InformationProposition.PlayerCount(5),
            profile,
        ) + extra,
    )

    private fun observation(
        id: String,
        proposition: InformationProposition,
        sourceSeat: Int?,
    ) = EpistemicObservation(
        observationId = id,
        snapshotId = "d4d-observation-snapshot",
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

    private data class Case(
        val profile: InformationProposition.SetupProfile,
        val setup: List<InformationProposition>,
        val observation: EpistemicObservation,
        val selectedWitness: Set<RegistrationFact>? = null,
    )
}
