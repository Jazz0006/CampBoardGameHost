package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
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
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bounded whole-bundle differential oracle for the D4C5 topology-first evaluator.
 *
 * Unlike single-observation parity, the exhaustive side filters every observation against the same
 * EnumeratedWorld. Drunk identity, Poisoner target, Red Herring identity, role identities and
 * registration witnesses are therefore shared mechanical resources across the complete bundle.
 */
class TroubleBrewingTopologyBundleDifferentialTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rolesById = roles.associateBy(RoleDefinition::id)
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-bundle-differential",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun topology_first_whole_bundle_keys_match_bounded_exhaustive_shared_witness_projection() {
        val standard = TroubleBrewingSetupProfiles.standard(5)
        val baron = TroubleBrewingSetupProfiles.withBaron(5)
        val standardGood = listOf(
            InformationProposition.RoleAt(1, RoleId("Chef")),
            InformationProposition.RoleAt(2, RoleId("Empath")),
            InformationProposition.RoleAt(3, RoleId("Fortune Teller")),
        )
        val spyAsEmpath = setOf(
            RegistrationFact(
                interactionId = "selected-spy",
                subjectSeat = 4,
                registeredRole = RoleId("Empath"),
                registeredType = CharacterType.TOWNSFOLK,
                registeredAlignment = Alignment.GOOD,
                registrationQuestion = RegistrationQuestion.SPECIFIC_MINION,
                reason = RegistrationReason.SPY_ABILITY,
            ),
        )
        val recluseAsDemon = setOf(
            RegistrationFact(
                interactionId = "selected-recluse",
                subjectSeat = 2,
                registeredType = CharacterType.DEMON,
                registeredAlignment = Alignment.EVIL,
                registrationQuestion = RegistrationQuestion.CHARACTER_TYPE,
                reason = RegistrationReason.RECLUSE_ABILITY,
            ),
        )

        val cases = listOf(
            Case(
                id = "role-identity-conflict",
                profile = standard,
                setup = standardGood,
                observations = listOf(
                    observation("spy-at-4", InformationProposition.RoleAt(4, RoleId("Spy"))),
                    observation("poisoner-at-4", InformationProposition.RoleAt(4, RoleId("Poisoner"))),
                ),
            ),
            Case(
                id = "one-poisoner-target",
                profile = standard,
                setup = standardGood,
                observations = listOf(
                    observation(
                        "poison-empath",
                        InformationProposition.AbilityStateAt(
                            seat = 2,
                            role = RoleId("Empath"),
                            state = AbilityState.MALFUNCTIONING_POISONED,
                        ),
                    ),
                    observation(
                        "poison-fortune-teller",
                        InformationProposition.AbilityStateAt(
                            seat = 3,
                            role = RoleId("Fortune Teller"),
                            state = AbilityState.MALFUNCTIONING_POISONED,
                        ),
                    ),
                ),
            ),
            Case(
                id = "one-drunk-identity",
                profile = baron,
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(4, RoleId("Baron")),
                    InformationProposition.RoleAt(5, RoleId("Imp")),
                ),
                observations = listOf(
                    observation(
                        "drunk-chef-at-2",
                        InformationProposition.AbilityStateAt(
                            seat = 2,
                            role = RoleId("Chef"),
                            state = AbilityState.MALFUNCTIONING_DRUNK,
                        ),
                    ),
                    observation(
                        "drunk-empath-at-3",
                        InformationProposition.AbilityStateAt(
                            seat = 3,
                            role = RoleId("Empath"),
                            state = AbilityState.MALFUNCTIONING_DRUNK,
                        ),
                    ),
                ),
            ),
            Case(
                id = "spy-registration-witness",
                profile = standard,
                setup = standardGood,
                observations = listOf(
                    observation(
                        "spy-registers-empath",
                        InformationProposition.RoleAt(4, RoleId("Empath")),
                    ),
                ),
                bindings = listOf(
                    ExactRegistrationWitnessBinding(
                        observationId = "spy-registers-empath",
                        registrations = spyAsEmpath,
                    ),
                ),
            ),
            Case(
                id = "fortune-teller-shared-red-herring",
                profile = standard,
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Fortune Teller")),
                    InformationProposition.RoleAt(3, RoleId("Empath")),
                    InformationProposition.RoleAt(4, RoleId("Poisoner")),
                    InformationProposition.RoleAt(5, RoleId("Imp")),
                ),
                observations = listOf(
                    observation(
                        "ft-yes",
                        InformationProposition.BooleanResult(
                            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                            sourceSeat = 2,
                            subjectSeats = listOf(1, 3),
                            value = true,
                        ),
                    ),
                    observation(
                        "ft-no",
                        InformationProposition.BooleanResult(
                            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                            sourceSeat = 2,
                            subjectSeats = listOf(1, 4),
                            value = false,
                        ),
                    ),
                ),
            ),
            Case(
                id = "recluse-selected-registration",
                profile = baron,
                setup = listOf(
                    InformationProposition.RoleAt(1, RoleId("Chef")),
                    InformationProposition.RoleAt(2, RoleId("Recluse")),
                    InformationProposition.RoleAt(3, RoleId("Butler")),
                    InformationProposition.RoleAt(4, RoleId("Baron")),
                    InformationProposition.RoleAt(5, RoleId("Imp")),
                ),
                observations = listOf(
                    observation(
                        "recluse-registers-demon",
                        InformationProposition.CharacterTypeAt(2, CharacterType.DEMON),
                    ),
                ),
                bindings = listOf(
                    ExactRegistrationWitnessBinding(
                        observationId = "recluse-registers-demon",
                        registrations = recluseAsDemon,
                    ),
                ),
            ),
            Case(
                id = "selected-natural-registration-rejects-spy-only-explanation",
                profile = standard,
                setup = standardGood,
                observations = listOf(
                    observation(
                        "spy-special-rejected",
                        InformationProposition.RoleAt(4, RoleId("Empath")),
                    ),
                ),
                bindings = listOf(
                    ExactRegistrationWitnessBinding(
                        observationId = "spy-special-rejected",
                        registrations = emptySet(),
                    ),
                ),
            ),
            Case(
                id = "contradictory-role-in-play-bundle",
                profile = standard,
                setup = standardGood,
                observations = listOf(
                    observation(
                        "spy-in-play",
                        InformationProposition.RoleInPlay(RoleId("Spy"), true),
                    ),
                    observation(
                        "spy-not-in-play",
                        InformationProposition.RoleInPlay(RoleId("Spy"), false),
                    ),
                ),
            ),
        )

        cases.forEach { testCase ->
            val knowledge = knowledge(testCase.profile, testCase.setup, testCase.id)
            val query = ExactHypotheticalObservationBundleQuery(
                bundleId = testCase.id,
                recipientSeat = 1,
                observations = testCase.observations,
                registrationWitnessBindings = testCase.bindings,
            )

            val worlds = TroubleBrewingWorldEnumerator.enumerate(
                rulesetRef = ruleset,
                knowledge = knowledge,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
            ).enumeratedWorlds()
            val exhaustiveBefore = worlds.mapTo(linkedSetOf()) { world ->
                StrategicWorldKey.from(world, rolesById)
            }
            val exhaustiveAfter = worlds
                .asSequence()
                .filter { world -> queryMatchesWorld(query, world) }
                .mapTo(linkedSetOf()) { world -> StrategicWorldKey.from(world, rolesById) }

            val topology = TroubleBrewingTopologyHypotheticalBundleEvaluator.evaluate(
                rulesetRef = ruleset,
                knowledge = knowledge,
                roleDefinitions = roles,
                queries = listOf(query),
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )

            assertTrue(
                "Topology-first bundle unexpectedly deferred in case ${testCase.id}.",
                topology is ExactStrategicTopologyBundleEvaluation.Ready,
            )
            topology as ExactStrategicTopologyBundleEvaluation.Ready
            val diagnostic = topology.diagnostics.single()

            assertEquals(
                "BEFORE strategic-key projection drifted in case ${testCase.id}.",
                exhaustiveBefore,
                diagnostic.beforeStructure.strategicWorldKeys,
            )
            assertEquals(
                "Whole-bundle shared-witness projection drifted in case ${testCase.id}.",
                exhaustiveAfter,
                diagnostic.afterStructure.strategicWorldKeys,
            )
        }
    }

    private fun queryMatchesWorld(
        query: ExactHypotheticalObservationBundleQuery,
        world: EnumeratedWorld,
    ): Boolean = query.observations.all { observation ->
        val result = TroubleBrewingWorldObservationEvaluator.evaluate(
            world = world,
            roles = rolesById,
            observation = observation,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        if (!result.matches) {
            false
        } else {
            val selected = query.registrationWitnessBindingFor(observation)
            selected == null || result.registrationWitnesses.any { exactWitness ->
                RegistrationWitnessSemanticMatcher.matches(
                    selected = selected.registrations,
                    exactWitness = exactWitness,
                    roles = rolesById,
                )
            }
        }
    }

    private fun knowledge(
        profile: InformationProposition.SetupProfile,
        extra: List<InformationProposition>,
        id: String,
    ): PlayerKnowledgeSnapshot {
        val playerCount = profile.townsfolk + profile.outsiders + profile.minions + profile.demons
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "d4d-bundle-$id",
            formalSnapshotId = "d4d-bundle-snapshot-$id",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(
                InformationProposition.PlayerCount(playerCount),
                profile,
            ) + extra,
        )
    }

    private fun observation(
        id: String,
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = id,
        snapshotId = "d4d-bundle-observation",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )

    private data class Case(
        val id: String,
        val profile: InformationProposition.SetupProfile,
        val setup: List<InformationProposition>,
        val observations: List<EpistemicObservation>,
        val bindings: List<ExactRegistrationWitnessBinding> = emptyList(),
    )
}
