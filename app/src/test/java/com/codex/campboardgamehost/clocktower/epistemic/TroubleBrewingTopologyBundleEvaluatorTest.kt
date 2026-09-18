package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingTopologyBundleEvaluatorTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-bundle",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun bundle_rejects_two_natural_role_claims_that_require_one_seat_to_have_two_roles() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val knowledge = knowledge(profile)
        val query = query(
            "role-conflict",
            listOf(
                observation("spy", InformationProposition.RoleAt(6, RoleId("Spy"))),
                observation("poisoner", InformationProposition.RoleAt(6, RoleId("Poisoner"))),
            ),
            bindEmpty = true,
        )

        val result = evaluate(knowledge, query)

        assertTrue(result is ExactStrategicTopologyBundleEvaluation.Ready)
        result as ExactStrategicTopologyBundleEvaluation.Ready
        val diagnostic = result.diagnostics.single()
        assertTrue(diagnostic.beforeFeasible)
        assertFalse(
            StrategicWorldKey(demonSeat = 7, minionSeats = listOf(6)) in
                diagnostic.afterStructure.strategicWorldKeys,
        )
    }

    @Test
    fun bundle_shares_the_single_Poisoner_target_across_observations() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val knowledge = knowledge(profile)
        val query = query(
            "poison-target-conflict",
            listOf(
                observation(
                    "poison-2",
                    InformationProposition.AbilityStateAt(
                        2,
                        RoleId("Chef"),
                        AbilityState.MALFUNCTIONING_POISONED,
                    ),
                ),
                observation(
                    "poison-3",
                    InformationProposition.AbilityStateAt(
                        3,
                        RoleId("Chef"),
                        AbilityState.MALFUNCTIONING_POISONED,
                    ),
                ),
            ),
            bindEmpty = true,
        )

        val result = evaluate(knowledge, query)

        assertTrue(result is ExactStrategicTopologyBundleEvaluation.Ready)
        result as ExactStrategicTopologyBundleEvaluation.Ready
        assertTrue(result.diagnostics.single().beforeFeasible)
        assertFalse(result.diagnostics.single().afterFeasible)
    }

    @Test
    fun bundle_shares_one_Red_Herring_identity_across_Fortune_Teller_observations() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleInPlay(RoleId("Fortune Teller"), true)),
        )
        val subjects = listOf(2, 3)
        val query = query(
            "red-herring-conflict",
            listOf(
                observation(
                    "ft-yes",
                    InformationProposition.BooleanResult(
                        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        sourceSeat = 1,
                        subjectSeats = subjects,
                        value = true,
                    ),
                ),
                observation(
                    "ft-no",
                    InformationProposition.BooleanResult(
                        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        sourceSeat = 1,
                        subjectSeats = subjects,
                        value = false,
                    ),
                ),
            ),
            bindEmpty = true,
        )

        val result = evaluate(knowledge, query)

        assertTrue(result is ExactStrategicTopologyBundleEvaluation.Ready)
        result as ExactStrategicTopologyBundleEvaluation.Ready
        assertTrue(result.diagnostics.single().beforeFeasible)
        assertFalse(result.diagnostics.single().afterFeasible)
    }

    @Test
    fun strategic_bundle_diagnostics_do_not_fake_mechanical_cardinality() {
        val profile = TroubleBrewingSetupProfiles.standard(15)
        val knowledge = knowledge(profile)
        val result = evaluate(
            knowledge,
            query(
                "fifteen-structure",
                listOf(
                    observation(
                        "shown-chef",
                        InformationProposition.ShownRoleAt(1, RoleId("Chef")),
                        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                    ),
                ),
                bindEmpty = true,
            ),
        )

        assertTrue(result is ExactStrategicTopologyBundleEvaluation.Ready)
        result as ExactStrategicTopologyBundleEvaluation.Ready
        val diagnostic = result.diagnostics.single()
        assertTrue(diagnostic.beforeFeasible)
        assertTrue(diagnostic.afterFeasible)
        assertTrue(diagnostic.beforeStructure.distinctStrategicWorldCount <= 5_460)
        assertTrue(diagnostic.afterStructure.distinctStrategicWorldCount <= 5_460)
    }

    private fun evaluate(
        knowledge: PlayerKnowledgeSnapshot,
        query: ExactHypotheticalObservationBundleQuery,
    ) = TroubleBrewingTopologyHypotheticalBundleEvaluator.evaluate(
        rulesetRef = ruleset,
        knowledge = knowledge,
        roleDefinitions = roles,
        queries = listOf(query),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    )

    private fun knowledge(
        profile: InformationProposition.SetupProfile,
        extra: List<InformationProposition> = emptyList(),
    ): PlayerKnowledgeSnapshot {
        val playerCount = profile.townsfolk + profile.outsiders + profile.minions + profile.demons
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "d4c5-$playerCount",
            formalSnapshotId = "d4c5-snapshot-$playerCount",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(
                InformationProposition.PlayerCount(playerCount),
                profile,
            ) + extra,
        )
    }

    private fun query(
        id: String,
        observations: List<EpistemicObservation>,
        bindEmpty: Boolean,
    ) = ExactHypotheticalObservationBundleQuery(
        bundleId = id,
        recipientSeat = 1,
        observations = observations,
        registrationWitnessBindings = if (bindEmpty) {
            observations.map { observation ->
                ExactRegistrationWitnessBinding(
                    observationId = observation.observationId,
                    registrations = emptySet(),
                )
            }
        } else {
            emptyList()
        },
    )

    private fun observation(
        id: String,
        proposition: InformationProposition,
        reliability: ObservationReliability = ObservationReliability.NOT_ABILITY_INFORMATION,
    ) = EpistemicObservation(
        observationId = id,
        snapshotId = "d4c5-observation",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = reliability,
        proposition = proposition,
    )
}
