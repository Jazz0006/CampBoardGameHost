package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedWorldSetTest {
    private val script = ScriptId("trouble_brewing")
    private val ruleset = RulesetRef(
        script, "0123456789abcdef0123456789abcdef", "a3-test", "official", RuleCoverage.VERIFIED,
    )
    private val snapshotId = "snapshot-a3"
    private val roles = listOf(
        role("Chef", CharacterType.TOWNSFOLK),
        role("Empath", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Drunk", CharacterType.OUTSIDER),
        role("Recluse", CharacterType.OUTSIDER),
        role("Poisoner", CharacterType.MINION),
        role("Spy", CharacterType.MINION),
        role("Baron", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )

    @Test fun `setup profiles encode official standard and Baron distributions`() {
        assertEquals(InformationProposition.SetupProfile(5, 1, 1, 1), TroubleBrewingSetupProfiles.standard(8))
        assertEquals(InformationProposition.SetupProfile(3, 3, 1, 1), TroubleBrewingSetupProfiles.withBaron(8))
        assertEquals(InformationProposition.SetupProfile(9, 2, 3, 1), TroubleBrewingSetupProfiles.standard(15))
    }

    @Test fun `enumerator keeps standard and hidden Baron profiles without leaking setup truth`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-a3-player-count",
            formalSnapshotId = snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val result = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.FUNCTIONING_ONLY, roles,
        )

        // Seat 1 is fixed as Chef. Empath/Fortune Teller occupy two of four remaining seats,
        // one of Poisoner/Spy is the Minion, and Imp is the Demon. FT worlds include one
        // distinct hidden red-herring choice for each of the four good seats.
        assertEquals(WorldCardinality.Exact(BigInteger("552")), result.cardinality())
        assertEquals(setOf(RoleId("Chef"), RoleId("Drunk")), result.possibleRoles(1))
        assertEquals(setOf(2, 3, 4, 5), result.possibleDemonSeats())
        assertEquals(WorldCardinality.Exact(BigInteger("138")), result.demonWorldCount(2))
        assertTrue(RoleId("Baron") in (2..5).flatMapTo(linkedSetOf(), result::possibleRoles))
        assertEquals(
            WorldCardinality.Exact(BigInteger("120")),
            result.explanationClusters().worldCountByCluster[WorldExplanationClusterId("baron-setup")],
        )
    }

    @Test fun `pair information keeps actual and interaction-local registration explanations`() {
        val worldWithSpy = world("Chef", "Spy", "Imp", "Empath", "Recluse")
        val worldWithoutSpy = world("Chef", "Empath", "Imp", "Poisoner", "Recluse")
        val set = fromWorlds(listOf(worldWithSpy, worldWithoutSpy))
        val observation = observation(
            InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Empath")),
                    InformationProposition.RoleAt(4, RoleId("Empath")),
                ),
            ),
            sourceAbility = "Chef",
        )

        val filtered = set.require(observation)
        assertEquals(WorldCardinality.Exact(BigInteger.TWO), filtered.cardinality())
        assertEquals(
            WorldCardinality.Exact(BigInteger.ONE),
            filtered.explanationClusters().worldCountByCluster[WorldExplanationClusterId("spy-registration")],
        )
        assertEquals(WorldCardinality.Exact(BigInteger.TWO), filtered.roleWorldCount(3, RoleId("Imp")))
    }

    @Test fun `numeric information evaluates Spy and Recluse registration locally`() {
        val set = fromWorlds(listOf(world("Chef", "Spy", "Imp", "Empath", "Recluse")))
        val chefZero = observation(
            InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = listOf(1, 2, 3, 4, 5),
                value = 0,
            ),
        )

        assertFalse(set.require(chefZero).isEmpty())
        assertTrue(set.exclude(chefZero).isEmpty())
    }

    @Test fun `Fortune Teller result includes hidden red herring and local Demon registration`() {
        val assignment = world("Fortune Teller", "Empath", "Imp", "Spy", "Recluse")
        val redHerringWorld = assignment.copy(redHerringSeat = 2)
        val set = fromWorlds(listOf(assignment, redHerringWorld), perceivedRole = "Fortune Teller")
        val yes = observation(
            InformationProposition.BooleanResult(
                BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = 1,
                subjectSeats = listOf(1, 2),
                value = true,
            ),
            sourceAbility = "Fortune Teller",
        )

        assertEquals(WorldCardinality.Exact(BigInteger.ONE), set.require(yes).cardinality())
        assertEquals(2, set.require(yes).enumeratedWorlds().single().redHerringSeat)
    }

    @Test fun `registration alternatives are bound to the exact observation interaction`() {
        val set = fromWorlds(listOf(world("Chef", "Spy", "Imp", "Empath", "Recluse")))
        val observation = observation(
            InformationProposition.RoleAt(2, RoleId("Empath")),
            sourceAbility = "Washerwoman",
        )

        val facts = set.boundRegistrationFacts(observation)
        assertEquals(1, facts.size)
        assertEquals(observation.observationId, facts.single().interactionId)
        assertEquals(2, facts.single().subjectSeat)
        assertEquals(RoleId("Empath"), facts.single().registeredRole)
        assertEquals(RegistrationReason.SPY_ABILITY, facts.single().reason)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Spy cannot be a Fortune Teller red herring even though Spy can register good`() {
        fromWorlds(listOf(world("Chef", "Empath", "Imp", "Spy", "Recluse").copy(redHerringSeat = 4)))
    }

    @Test fun `malfunction hypotheses do not share identity or functioning-only results`() {
        val poisonedWorld = world("Chef", "Empath", "Imp", "Poisoner", "Recluse").copy(
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_POISONED),
        )
        val impossible = observation(InformationProposition.RoleAt(2, RoleId("Imp")))
        val knowledge = knowledge("Chef", InformationProposition.SetupProfile(2, 1, 1, 1))
        val functioning = EnumeratedWorldSet.fromWorlds(
            ruleset, knowledge, EpistemicHypothesis.FUNCTIONING_ONLY, roles, listOf(poisonedWorld),
        )
        val credible = EnumeratedWorldSet.fromWorlds(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles, listOf(poisonedWorld),
        )

        assertTrue(functioning.require(impossible).isEmpty())
        assertFalse(credible.require(impossible).isEmpty())
        assertNotEquals(functioning.identity, credible.identity)
        assertEquals(
            WorldCardinality.Exact(BigInteger.ONE),
            credible.require(impossible).explanationClusters()
                .worldCountByCluster[WorldExplanationClusterId("poisoned-explanation")],
        )
    }

    @Test fun `functioning-only excludes a poisoned world even when its displayed information happens to be true`() {
        val poisonedWorld = world("Chef", "Empath", "Imp", "Poisoner", "Recluse").copy(
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_POISONED),
        )
        val trueByCoincidence = observation(InformationProposition.RoleAt(2, RoleId("Empath")))
        val knowledge = knowledge("Chef", InformationProposition.SetupProfile(2, 1, 1, 1))
        val functioning = EnumeratedWorldSet.fromWorlds(
            ruleset, knowledge, EpistemicHypothesis.FUNCTIONING_ONLY, roles, listOf(poisonedWorld),
        )
        val credible = EnumeratedWorldSet.fromWorlds(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles, listOf(poisonedWorld),
        )

        assertTrue(functioning.require(trueByCoincidence).isEmpty())
        assertFalse(credible.require(trueByCoincidence).isEmpty())
        assertFalse(
            WorldExplanationClusterId("true-info") in credible.require(trueByCoincidence)
                .explanationClusters().worldCountByCluster,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a private observation cannot be applied to another recipient world set`() {
        fromWorlds(listOf(world("Chef", "Empath", "Imp", "Poisoner", "Recluse"))).require(
            observation(InformationProposition.RoleAt(2, RoleId("Empath"))).copy(recipientSeats = setOf(2)),
        )
    }

    private fun fromWorlds(
        worlds: List<EnumeratedWorld>,
        perceivedRole: String = "Chef",
    ): EnumeratedWorldSet = EnumeratedWorldSet.fromWorlds(
        ruleset,
        knowledge(perceivedRole, InformationProposition.SetupProfile(2, 1, 1, 1)),
        EpistemicHypothesis.FUNCTIONING_ONLY,
        roles,
        worlds,
    )

    private fun world(vararg roleNames: String): EnumeratedWorld = EnumeratedWorld(
        roleNames.mapIndexed { index, role -> index + 1 to RoleId(role) }.toMap(linkedMapOf()),
    )

    private fun knowledge(
        perceivedRole: String,
        setup: InformationProposition.SetupProfile,
    ) = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-a3-$perceivedRole-${setup.outsiders}",
        formalSnapshotId = snapshotId,
        recipientSeat = 1,
        perceivedRole = RoleId(perceivedRole),
        setupKnowledge = listOf(setup),
    )

    private fun observation(
        proposition: InformationProposition,
        sourceAbility: String = "Chef",
    ) = EpistemicObservation(
        observationId = "observation-a3-${proposition.hashCode()}",
        snapshotId = snapshotId,
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

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        RoleId(name),
        if (type == CharacterType.TOWNSFOLK || type == CharacterType.OUTSIDER) Alignment.GOOD else Alignment.EVIL,
        type,
        setOf(script),
    )
}
