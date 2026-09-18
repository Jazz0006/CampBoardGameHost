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

        assertInfeasible(
            standard,
            topology,
            setupKnowledge = listOf(InformationProposition.RoleInPlay(RoleId("Baron"), true)),
        )
        assertFeasible(
            baron,
            topology,
            setupKnowledge = listOf(InformationProposition.RoleInPlay(RoleId("Baron"), true)),
        )
        assertInfeasible(
            baron,
            topology,
            setupKnowledge = listOf(InformationProposition.RoleInPlay(RoleId("Baron"), false)),
        )
    }

    @Test
    fun `mechanical setup propositions defer instead of being reported as infeasible`() {
        val profile = TroubleBrewingSetupProfiles.standard(8)
        val topology = StrategicWorldKey(demonSeat = 8, minionSeats = listOf(7))
        val unsupported = InformationProposition.AbilityStateAt(
            seat = 2,
            abilityRole = RoleId("Chef"),
            abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.MALFUNCTIONING_POISONED,
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
