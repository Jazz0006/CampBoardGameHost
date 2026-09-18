package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingTopologyObservationCompositionTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-composition",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun Librarian_zero_AllOf_Not_RoleInPlay_is_supported_exactly() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val topology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(6))
        val proposition = InformationProposition.AllOf(
            listOf("Butler", "Drunk", "Recluse", "Saint").map { role ->
                InformationProposition.Not(
                    InformationProposition.RoleInPlay(RoleId(role), true),
                )
            },
        )

        assertTrue(
            evaluate(profile, topology, proposition) is
                TroubleBrewingTopologyObservationFeasibility.Feasible,
        )
    }

    @Test
    fun pair_AnyOf_preserves_alternative_role_assignments() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val topology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(6))
        val proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.RoleAt(2, RoleId("Empath")),
                InformationProposition.RoleAt(3, RoleId("Empath")),
            ),
        )

        assertTrue(
            evaluate(profile, topology, proposition) is
                TroubleBrewingTopologyObservationFeasibility.Feasible,
        )
    }

    @Test
    fun public_claim_shape_can_survive_through_the_evil_speaker_branch() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.AlignmentAt(7, Alignment.EVIL),
                InformationProposition.AllOf(
                    listOf(
                        InformationProposition.ShownRoleAt(7, RoleId("Empath")),
                        InformationProposition.AbilityStateAt(
                            7,
                            RoleId("Empath"),
                            AbilityState.FUNCTIONING,
                        ),
                    ),
                ),
            ),
        )

        assertTrue(
            evaluate(
                profile,
                topology,
                proposition,
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            ) is TroubleBrewingTopologyObservationFeasibility.Feasible,
        )
    }

    @Test
    fun one_observation_cannot_require_Red_Herring_both_inside_and_outside_the_same_pair() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val knowledge = knowledge(
            profile,
            listOf(InformationProposition.RoleInPlay(RoleId("Fortune Teller"), true)),
        )
        val subjects = listOf(2, 3)
        val proposition = InformationProposition.AllOf(
            listOf(
                InformationProposition.BooleanResult(
                    metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                    sourceSeat = 1,
                    subjectSeats = subjects,
                    value = true,
                ),
                InformationProposition.BooleanResult(
                    metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                    sourceSeat = 1,
                    subjectSeats = subjects,
                    value = false,
                ),
            ),
        )

        assertEquals(
            TroubleBrewingTopologyObservationFeasibility.Infeasible,
            evaluate(
                profile,
                topology,
                proposition,
                knowledge = knowledge,
            ),
        )
    }

    @Test
    fun unsupported_general_Not_remains_explicitly_deferred() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val topology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(6))
        val proposition = InformationProposition.Not(
            InformationProposition.AlignmentAt(2, Alignment.GOOD),
        )

        assertTrue(
            evaluate(profile, topology, proposition) is
                TroubleBrewingTopologyObservationFeasibility.Deferred,
        )
    }

    private fun evaluate(
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        proposition: InformationProposition,
        knowledge: PlayerKnowledgeSnapshot = knowledge(profile, emptyList()),
        reliability: ObservationReliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
    ) = TroubleBrewingTopologyObservationWitnessEvaluator.evaluate(
        rulesetRef = ruleset,
        knowledge = knowledge,
        profile = profile,
        topology = topology,
        roleDefinitions = roles,
        observation = EpistemicObservation(
            observationId = "d4c4c-composition",
            snapshotId = "d4c4c-snapshot",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = reliability,
            proposition = proposition,
        ),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    )

    private fun knowledge(
        profile: InformationProposition.SetupProfile,
        extra: List<InformationProposition>,
    ): PlayerKnowledgeSnapshot {
        val playerCount = profile.townsfolk + profile.outsiders + profile.minions + profile.demons
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "d4c4c-$playerCount",
            formalSnapshotId = "d4c4c-snapshot-$playerCount",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(
                InformationProposition.PlayerCount(playerCount),
                profile,
            ) + extra,
        )
    }
}
