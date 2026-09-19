package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingTopologySetupWitnessEvaluatorTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-setup",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun `respects topology role pins uniqueness and type capacity without enumerating role worlds`() {
        val profile = TroubleBrewingSetupProfiles.standard(7)
        val topology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(2))

        assertFeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(7, RoleId("Imp")),
                InformationProposition.RoleAt(2, RoleId("Poisoner")),
                InformationProposition.CharacterTypeAt(3, CharacterType.TOWNSFOLK),
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(InformationProposition.RoleAt(3, RoleId("Poisoner"))),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(3, RoleId("Empath")),
                InformationProposition.RoleAt(4, RoleId("Empath")),
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(InformationProposition.CharacterTypeAt(3, CharacterType.OUTSIDER)),
        )
    }

    @Test
    fun `recipient shown Townsfolk may be actual role or Drunk only when profile permits it`() {
        val sevenProfile = TroubleBrewingSetupProfiles.standard(7)
        val sevenTopology = StrategicWorldKey(demonSeat = 7, minionSeats = listOf(2))
        assertInfeasible(
            sevenProfile,
            sevenTopology,
            setupKnowledge = listOf(InformationProposition.RoleInPlay(RoleId("Chef"), false)),
        )

        val eightProfile = TroubleBrewingSetupProfiles.standard(8)
        val eightTopology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        assertFeasible(
            eightProfile,
            eightTopology,
            setupKnowledge = listOf(
                InformationProposition.RoleInPlay(RoleId("Chef"), false),
                InformationProposition.RoleAt(1, RoleId("Drunk")),
                InformationProposition.ShownRoleAt(1, RoleId("Chef")),
            ),
        )
        assertInfeasible(
            eightProfile,
            eightTopology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(1, RoleId("Drunk")),
                InformationProposition.RoleInPlay(RoleId("Chef"), true),
            ),
        )
    }

    @Test
    fun `recipient shown role is fixed even on the Drunk branch`() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))

        assertFeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(1, RoleId("Drunk")),
                InformationProposition.ShownRoleAt(1, RoleId("Chef")),
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(1, RoleId("Drunk")),
                InformationProposition.ShownRoleAt(1, RoleId("Empath")),
            ),
        )
    }

    @Test
    fun `non recipient shown Townsfolk can be ordinary identity or hidden Drunk identity`() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))

        assertFeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(3, RoleId("Drunk")),
                InformationProposition.ShownRoleAt(3, RoleId("Empath")),
                InformationProposition.RoleInPlay(RoleId("Empath"), false),
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(3, RoleId("Drunk")),
                InformationProposition.ShownRoleAt(3, RoleId("Empath")),
                InformationProposition.RoleInPlay(RoleId("Empath"), true),
            ),
        )
    }

    @Test
    fun `Baron presence follows the selected legal profile`() {
        val standard = TroubleBrewingSetupProfiles.standard(8)
        val baron = TroubleBrewingSetupProfiles.withBaron(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))

        assertEquals(
            "Standard profile must reject an in-play Baron.",
            TroubleBrewingTopologySetupFeasibility.Infeasible,
            evaluate(
                standard,
                topology,
                listOf(InformationProposition.RoleInPlay(RoleId("Baron"), true)),
            ),
        )
        assertEquals(
            "Baron profile must admit its required Baron.",
            TroubleBrewingTopologySetupFeasibility.Feasible,
            evaluate(
                baron,
                topology,
                listOf(InformationProposition.RoleInPlay(RoleId("Baron"), true)),
            ),
        )
        assertEquals(
            "Baron profile must reject knowledge that Baron is absent.",
            TroubleBrewingTopologySetupFeasibility.Infeasible,
            evaluate(
                baron,
                topology,
                listOf(InformationProposition.RoleInPlay(RoleId("Baron"), false)),
            ),
        )
    }

    @Test
    fun `duplicate identical shown role setup facts are idempotent`() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val shown = InformationProposition.ShownRoleAt(1, RoleId("Chef"))

        assertFeasible(
            profile,
            topology,
            setupKnowledge = listOf(shown, shown),
        )
    }

    @Test
    fun poisoned_state_requires_one_in_play_Poisoner_and_a_non_Drunk_target() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val poisoned = InformationProposition.AbilityStateAt(
            seat = 3,
            abilityRole = RoleId("Chef"),
            abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.MALFUNCTIONING_POISONED,
        )

        assertFeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(7, RoleId("Poisoner")),
                poisoned,
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleInPlay(RoleId("Poisoner"), false),
                poisoned,
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(7, RoleId("Poisoner")),
                InformationProposition.RoleAt(3, RoleId("Drunk")),
                poisoned,
            ),
        )
    }

    @Test
    fun Drunk_and_functioning_states_bind_actual_Drunk_identity() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val drunkState = InformationProposition.AbilityStateAt(
            seat = 3,
            abilityRole = RoleId("Chef"),
            abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.MALFUNCTIONING_DRUNK,
        )
        val functioning = InformationProposition.AbilityStateAt(
            seat = 3,
            abilityRole = RoleId("Chef"),
            abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.FUNCTIONING,
        )

        assertFeasible(profile, topology, setupKnowledge = listOf(drunkState))
        assertFeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(3, RoleId("Drunk")),
                drunkState,
            ),
        )
        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(3, RoleId("Drunk")),
                functioning,
            ),
        )
    }

    @Test
    fun one_Poisoner_target_cannot_make_two_distinct_seats_poisoned() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        fun poisoned(seat: Int) = InformationProposition.AbilityStateAt(
            seat = seat,
            abilityRole = RoleId("Chef"),
            abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.MALFUNCTIONING_POISONED,
        )

        assertInfeasible(
            profile,
            topology,
            setupKnowledge = listOf(
                InformationProposition.RoleAt(7, RoleId("Poisoner")),
                poisoned(2),
                poisoned(3),
            ),
        )
    }

    @Test
    fun mandatory_Poisoner_target_prevents_all_functioning_thirteen_player_standard_world() {
        val profile = TroubleBrewingSetupProfiles.standard(13)
        val topology = StrategicWorldKey(demonSeat = 13, minionSeats = listOf(10, 11, 12))
        fun functioning(seat: Int) = InformationProposition.AbilityStateAt(
            seat = seat,
            abilityRole = RoleId("Chef"),
            abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.FUNCTIONING,
        )

        assertInfeasible(
            profile,
            topology,
            setupKnowledge = (1..13).map(::functioning),
        )
        assertFeasible(
            profile,
            topology,
            setupKnowledge = (1..12).map(::functioning),
        )
    }

    @Test
    fun later_mechanical_propositions_still_defer_instead_of_becoming_false_UNSAT() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val unsupported = InformationProposition.BooleanResult(
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = 1,
            subjectSeats = listOf(2, 3),
            value = true,
        )

        val result = evaluate(profile, topology, listOf(unsupported))

        assertTrue(result is TroubleBrewingTopologySetupFeasibility.Deferred)
        result as TroubleBrewingTopologySetupFeasibility.Deferred
        assertEquals(listOf(unsupported), result.unsupportedPropositions)
    }

    private fun assertFeasible(
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        setupKnowledge: List<InformationProposition>,
    ) {
        assertEquals(
            TroubleBrewingTopologySetupFeasibility.Feasible,
            evaluate(profile, topology, setupKnowledge),
        )
    }

    private fun assertInfeasible(
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        setupKnowledge: List<InformationProposition>,
    ) {
        assertEquals(
            TroubleBrewingTopologySetupFeasibility.Infeasible,
            evaluate(profile, topology, setupKnowledge),
        )
    }

    private fun evaluate(
        profile: InformationProposition.SetupProfile,
        topology: StrategicWorldKey,
        setupKnowledge: List<InformationProposition>,
    ): TroubleBrewingTopologySetupFeasibility {
        val playerCount = profile.townsfolk + profile.outsiders + profile.minions + profile.demons
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "topology-setup-$playerCount",
            formalSnapshotId = "topology-setup-snapshot-$playerCount",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(
                InformationProposition.PlayerCount(playerCount),
                profile,
            ) + setupKnowledge,
        )
        return TroubleBrewingTopologySetupWitnessEvaluator.evaluate(
            rulesetRef = ruleset,
            knowledge = knowledge,
            profile = profile,
            topology = topology,
            roleDefinitions = roles,
        )
    }
}
