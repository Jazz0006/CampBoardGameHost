package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingTopologySetupWitnessDifferentialTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rolesById = roles.associateBy(RoleDefinition::id)
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "sde-2d4-topology-differential",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test
    fun topology_first_setup_feasibility_matches_exhaustive_five_player_exact_projection() {
        val cases = listOf(
            TroubleBrewingSetupProfiles.standard(5) to emptyList(),
            TroubleBrewingSetupProfiles.standard(5) to listOf(
                InformationProposition.RoleAt(2, RoleId("Poisoner")),
            ),
            TroubleBrewingSetupProfiles.withBaron(5) to emptyList(),
            TroubleBrewingSetupProfiles.withBaron(5) to listOf(
                InformationProposition.RoleAt(2, RoleId("Baron")),
            ),
            TroubleBrewingSetupProfiles.withBaron(5) to listOf(
                InformationProposition.RoleInPlay(RoleId("Chef"), false),
            ),
        )

        cases.forEachIndexed { index, (profile, extraKnowledge) ->
            val knowledge = PlayerKnowledgeSnapshot(
                knowledgeSnapshotId = "d4c2-diff-$index",
                formalSnapshotId = "d4c2-diff-snapshot-$index",
                recipientSeat = 1,
                perceivedRole = RoleId("Chef"),
                setupKnowledge = listOf(
                    InformationProposition.PlayerCount(5),
                    profile,
                ) + extraKnowledge,
            )

            val exhaustive = TroubleBrewingWorldEnumerator.enumerate(
                rulesetRef = ruleset,
                knowledge = knowledge,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
            )
            val exhaustiveKeys = exhaustive.enumeratedWorlds()
                .map { world -> StrategicWorldKey.from(world, rolesById) }
                .toSet()

            val topologyFirstKeys = TroubleBrewingStrategicTopologyDomain
                .enumerate(5, profile)
                .filterTo(linkedSetOf()) { topology ->
                    TroubleBrewingTopologySetupWitnessEvaluator.evaluate(
                        rulesetRef = ruleset,
                        knowledge = knowledge,
                        profile = profile,
                        topology = topology,
                        roleDefinitions = roles,
                    ) == TroubleBrewingTopologySetupFeasibility.Feasible
                }

            assertEquals(
                "Topology-first setup feasibility drifted in bounded differential case $index.",
                exhaustiveKeys,
                topologyFirstKeys,
            )
        }
    }
}
