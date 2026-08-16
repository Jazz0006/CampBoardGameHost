package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ZddPlayerWorldSetTest {
    private val ruleset = RulesetRef(
        TroubleBrewingFixtures.scriptId,
        "0123456789abcdef0123456789abcdef",
        "a4-test",
        "official",
        RuleCoverage.VERIFIED,
    )

    @Test fun `ZDD prototype matches enumerated baseline before and after observations`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-knowledge",
            formalSnapshotId = "a4-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset,
            knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val zdd = ZddPlayerWorldSet.fromEnumerated(enumerated)

        assertEquivalent(enumerated, zdd, 1..5)
        assertTrue(zdd.nodeCount() < enumerated.cardinality().valueOrLowerBound.toInt() * 12)
        assertSame(zdd, zdd.restore(zdd.checkpoint()))
        println("A4_ZDD worlds=${zdd.cardinality().valueOrLowerBound} nodes=${zdd.nodeCount()}")

        val chefOne = observation(InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = 1,
            subjectSeats = (1..5).toList(),
            value = 1,
        ))
        assertEquivalent(enumerated.require(chefOne), zdd.require(chefOne), 1..5)
        assertEquivalent(enumerated.exclude(chefOne), zdd.exclude(chefOne), 1..5)
    }

    @Test fun `direct streaming construction matches enumerated conversion`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-direct-knowledge",
            formalSnapshotId = "a4-direct-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val roles = TroubleBrewingFixtures.fullRoleDefinitions()
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles,
        )
        val converted = ZddPlayerWorldSet.fromEnumerated(enumerated)
        val direct = ZddPlayerWorldSet.enumerateDirect(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles,
        )

        assertEquivalent(enumerated, direct, 1..5)
        assertEquals(converted.cardinality(), direct.cardinality())
        assertEquals(converted.nodeCount(), direct.nodeCount())
    }

    @Test fun `checkpoint restore and possible values agree across representations`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-values-knowledge",
            formalSnapshotId = "a4-values-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val zdd = ZddPlayerWorldSet.fromEnumerated(enumerated)
        val candidates = (0..5).map { value ->
            observation(InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), value,
            ), "a4-chef-$value", "a4-values-snapshot")
        }

        assertEquals(enumerated.possibleValues(candidates), zdd.possibleValues(candidates))
        val booleanCandidates = listOf(true, false).map { value ->
            EpistemicObservation(
                observationId = "a4-ft-$value",
                snapshotId = "a4-values-snapshot",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 2,
                sourceSeat = 1,
                sourceAbility = RoleId("Fortune Teller"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.BooleanResult(
                    BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 3), value,
                ),
            )
        }
        assertEquals(enumerated.possibleValues(booleanCandidates), zdd.possibleValues(booleanCandidates))
        val selected = candidates.single { candidate ->
            (candidate.proposition as InformationProposition.NumericResult).value == 1
        }
        val enumeratedCheckpoint = enumerated.checkpoint()
        val zddCheckpoint = zdd.checkpoint()
        val filteredEnumerated = enumerated.require(selected)
        val filteredZdd = zdd.require(selected)
        assertEquivalent(filteredEnumerated, filteredZdd, 1..5)
        assertSame(enumerated, filteredEnumerated.restore(enumeratedCheckpoint))
        assertSame(zdd, filteredZdd.restore(zddCheckpoint))

        val otherIdentity = TroubleBrewingWorldEnumerator.enumerate(
            ruleset,
            knowledge.copy(knowledgeSnapshotId = "ignored-by-identity", perceivedRole = RoleId("Empath")),
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        assertThrows(IllegalArgumentException::class.java) { otherIdentity.restore(zddCheckpoint) }
    }

    @Test fun `all A3 executable golden contracts agree through the ZDD representation`() {
        val catalog = A3GoldenContractCatalog.loadDocument()
        val runner = A3GoldenContractRunner(catalog)
        val enumerated = runner.runAll().associateBy(A3ExecutionResult::id)
        val zdd = runner.runAll(useZdd = true).associateBy(A3ExecutionResult::id)

        assertEquals(enumerated.keys, zdd.keys)
        enumerated.forEach { (id, expected) ->
            val actual = zdd.getValue(id)
            assertEquals("$id: ${actual.detail}", expected.passed, actual.passed)
            assertTrue("$id: ${actual.detail}", actual.passed)
        }
    }

    @Test fun `direct state observations use native ZDD restriction`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-native-knowledge",
            formalSnapshotId = "a4-native-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val zdd = ZddPlayerWorldSet.fromEnumerated(enumerated)
        val alive = EpistemicObservation(
            observationId = "a4-seat-two-alive",
            snapshotId = "a4-native-snapshot",
            phase = StorytellerPhase.DAY,
            round = 1,
            sequence = 2,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, true),
        )
        val filtered = zdd.require(alive)
        assertEquivalent(enumerated.require(alive), filtered, 1..5)
        assertEquals(ZddFilterStrategy.NATIVE_RESTRICTION, filtered.lastFilterStrategy)
    }

    @Test fun `role in play uses native ZDD set restriction`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-role-in-play-knowledge",
            formalSnapshotId = "a4-role-in-play-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val zdd = ZddPlayerWorldSet.fromEnumerated(enumerated)
        val inPlay = EpistemicObservation(
            observationId = "a4-ft-in-play",
            snapshotId = "a4-role-in-play-snapshot",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.RoleInPlay(RoleId("Fortune Teller"), true),
        )
        val absent = inPlay.copy(
            observationId = "a4-ft-not-in-play",
            proposition = InformationProposition.RoleInPlay(RoleId("Fortune Teller"), false),
        )

        listOf(inPlay, absent).forEach { observation ->
            val filtered = zdd.require(observation)
            assertEquivalent(enumerated.require(observation), filtered, 1..5)
            assertEquals(ZddFilterStrategy.NATIVE_RESTRICTION, filtered.lastFilterStrategy)
        }
    }

    @Test fun `direct seat predicates use native restriction only after special registration is excluded`() {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "a4-seat-predicate-knowledge",
            formalSnapshotId = "a4-seat-predicate-snapshot",
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val enumerated = TroubleBrewingWorldEnumerator.enumerate(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val zdd = ZddPlayerWorldSet.fromEnumerated(enumerated)
        val roleAt = publicObservation(
            "a4-seat-two-empath", InformationProposition.RoleAt(2, RoleId("Empath")),
        )
        assertEquals(ZddFilterStrategy.DECODE_REBUILD, zdd.require(roleAt).lastFilterStrategy)

        val spyAbsent = publicObservation(
            "a4-spy-absent", InformationProposition.RoleInPlay(RoleId("Spy"), false),
        )
        val recluseAbsent = publicObservation(
            "a4-recluse-absent", InformationProposition.RoleInPlay(RoleId("Recluse"), false),
        )
        val baseline = enumerated.require(spyAbsent).require(recluseAbsent)
        val compact = zdd.require(spyAbsent).require(recluseAbsent)
        listOf(
            roleAt,
            publicObservation("a4-seat-two-townsfolk", InformationProposition.CharacterTypeAt(
                2, com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK,
            )),
            publicObservation("a4-seat-two-good", InformationProposition.AlignmentAt(
                2, com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD,
            )),
        ).forEach { observation ->
            val filtered = compact.require(observation)
            assertEquivalent(baseline.require(observation), filtered, 1..5)
            assertEquals(ZddFilterStrategy.NATIVE_RESTRICTION, filtered.lastFilterStrategy)
        }
    }

    private fun assertEquivalent(expected: PlayerWorldSet, actual: PlayerWorldSet, seats: IntRange) {
        assertEquals(expected.identity, actual.identity)
        assertEquals(expected.cardinality(), actual.cardinality())
        assertEquals(expected.isEmpty(), actual.isEmpty())
        assertEquals(expected.possibleDemonSeats(), actual.possibleDemonSeats())
        assertEquals(expected.possibleMinionSeats(), actual.possibleMinionSeats())
        assertEquals(expected.explanationClusters(), actual.explanationClusters())
        seats.forEach { seat ->
            assertEquals(expected.possibleRoles(seat), actual.possibleRoles(seat))
            expected.possibleRoles(seat).forEach { role ->
                assertEquals(expected.roleWorldCount(seat, role), actual.roleWorldCount(seat, role))
            }
            assertEquals(expected.demonWorldCount(seat), actual.demonWorldCount(seat))
        }
    }

    private fun observation(
        proposition: InformationProposition,
        observationId: String = "a4-chef-one",
        snapshotId: String = "a4-snapshot",
    ) = EpistemicObservation(
        observationId = observationId,
        snapshotId = snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = 1,
        sourceAbility = RoleId("Chef"),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(1),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )

    private fun publicObservation(observationId: String, proposition: InformationProposition) = EpistemicObservation(
        observationId = observationId,
        snapshotId = "a4-seat-predicate-snapshot",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )
}
