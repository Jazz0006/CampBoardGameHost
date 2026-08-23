package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EnumeratedHistoricalWorldReplayTest {
    private val script = ScriptId("trouble_brewing")
    private val ruleset = RulesetRef(
        script,
        "0123456789abcdef0123456789abcdef",
        "a3-historical-test",
        "official",
        RuleCoverage.VERIFIED,
    )
    private val formalSnapshotId = "snapshot-a3-historical"
    private val roles = listOf(
        role("Empath", CharacterType.TOWNSFOLK),
        role("Chef", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Drunk", CharacterType.OUTSIDER),
        role("Poisoner", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )

    @Test
    fun `public elimination updates exact worlds before the durability observation is replayed`() {
        val initial = worldSet()
        val executionPoint = TimelinePoint(StorytellerPhase.DAY, 1, 7, 10L)
        val observationPoint = TimelinePoint(StorytellerPhase.DAY, 1, 7, 11L)
        val phasePoint = TimelinePoint(StorytellerPhase.DAY, 1, 8, 12L)
        val publicAliveObservation = RecordedEpistemicObservation(
            recordId = "public-alive-game-7-2",
            phase = StorytellerPhase.DAY,
            round = 1,
            sequence = 7,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, false),
            timelineBinding = ObservationTimelineBinding.Global(observationPoint),
        )

        val result = EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = initial,
            formalSnapshotId = formalSnapshotId,
            initialPhase = StorytellerPhase.DAY,
            initialRound = 1,
            events = listOf(
                PlayerHistoricalEvent.PublicExecution(
                    actionId = "execution-game-1-7-2",
                    targetSeat = 2,
                    point = executionPoint,
                ),
                PlayerHistoricalEvent.Observation(publicAliveObservation, observationPoint),
                PlayerHistoricalEvent.PhaseAdvance(
                    actionId = "phase-night-2",
                    phase = StorytellerPhase.NIGHT,
                    round = 2,
                    point = phasePoint,
                ),
            ),
        )

        assertFalse(result.worldSet.isEmpty())
        assertEquals(setOf(1, 3, 4, 5), result.worldSet.enumeratedWorlds().single().aliveSeats)
        assertEquals(StorytellerPhase.NIGHT, result.phase)
        assertEquals(2, result.round)
        assertEquals(12L, result.lastGlobalSequence)
    }

    @Test
    fun `public Poisoner death clears poison only in worlds where the eliminated seat is Poisoner`() {
        val poisonerDies = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Poisoner"),
                4 to RoleId("Imp"),
                5 to RoleId("Fortune Teller"),
            ),
            abilityStatesBySeat = mapOf(2 to AbilityState.MALFUNCTIONING_POISONED),
        )
        val poisonerSurvives = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Fortune Teller"),
                4 to RoleId("Imp"),
                5 to RoleId("Poisoner"),
            ),
            abilityStatesBySeat = mapOf(2 to AbilityState.MALFUNCTIONING_POISONED),
        )

        val result = EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = worldSet(listOf(poisonerDies, poisonerSurvives)),
            formalSnapshotId = formalSnapshotId,
            initialPhase = StorytellerPhase.DAY,
            initialRound = 1,
            events = listOf(
                PlayerHistoricalEvent.PublicExecution(
                    actionId = "execution-seat-3",
                    targetSeat = 3,
                    point = TimelinePoint(StorytellerPhase.DAY, 1, 7, 10L),
                ),
            ),
        )

        val worlds = result.worldSet.enumeratedWorlds().associateBy { it.rolesBySeat.getValue(3) }
        assertEquals(emptyMap<Int, AbilityState>(), worlds.getValue(RoleId("Poisoner")).abilityStatesBySeat)
        assertEquals(
            mapOf(2 to AbilityState.MALFUNCTIONING_POISONED),
            worlds.getValue(RoleId("Fortune Teller")).abilityStatesBySeat,
        )
        assertEquals(setOf(1, 2, 4, 5), worlds.getValue(RoleId("Poisoner")).aliveSeats)
        assertEquals(setOf(1, 2, 4, 5), worlds.getValue(RoleId("Fortune Teller")).aliveSeats)
    }

    @Test
    fun `Poisoner death preserves intrinsic Drunk malfunction while clearing poison`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Drunk"),
                3 to RoleId("Poisoner"),
                4 to RoleId("Imp"),
                5 to RoleId("Chef"),
            ),
            shownRolesBySeat = mapOf(2 to RoleId("Fortune Teller")),
            abilityStatesBySeat = mapOf(
                2 to AbilityState.MALFUNCTIONING_DRUNK,
                5 to AbilityState.MALFUNCTIONING_POISONED,
            ),
        )

        val result = EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = worldSet(
                worlds = listOf(world),
                setupProfile = InformationProposition.SetupProfile(2, 1, 1, 1),
            ),
            formalSnapshotId = formalSnapshotId,
            initialPhase = StorytellerPhase.DAY,
            initialRound = 1,
            events = listOf(
                PlayerHistoricalEvent.PublicDeath(
                    actionId = "death-poisoner",
                    targetSeat = 3,
                    point = TimelinePoint(StorytellerPhase.DAY, 1, 8, 20L),
                ),
            ),
        )

        assertEquals(
            mapOf(2 to AbilityState.MALFUNCTIONING_DRUNK),
            result.worldSet.enumeratedWorlds().single().abilityStatesBySeat,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `exact historical replay rejects noncanonical event order`() {
        val later = TimelinePoint(StorytellerPhase.DAY, 1, 2, 20L)
        val earlier = TimelinePoint(StorytellerPhase.DAY, 1, 1, 10L)

        EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = worldSet(),
            formalSnapshotId = formalSnapshotId,
            initialPhase = StorytellerPhase.DAY,
            initialRound = 1,
            events = listOf(
                PlayerHistoricalEvent.PhaseAdvance("later", StorytellerPhase.NIGHT, 2, later),
                PlayerHistoricalEvent.PhaseAdvance("earlier", StorytellerPhase.DAY, 2, earlier),
            ),
        )
    }

    private fun worldSet(
        worlds: List<EnumeratedWorld> = listOf(
            EnumeratedWorld(
                rolesBySeat = linkedMapOf(
                    1 to RoleId("Empath"),
                    2 to RoleId("Chef"),
                    3 to RoleId("Poisoner"),
                    4 to RoleId("Imp"),
                    5 to RoleId("Fortune Teller"),
                ),
            ),
        ),
        setupProfile: InformationProposition.SetupProfile = InformationProposition.SetupProfile(3, 0, 1, 1),
    ): EnumeratedWorldSet = EnumeratedWorldSet.fromWorlds(
        rulesetRef = ruleset,
        knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-a3-historical-${setupProfile.outsiders}",
            formalSnapshotId = formalSnapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Empath"),
            setupKnowledge = listOf(setupProfile),
        ),
        hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
        roleDefinitions = roles,
        worlds = worlds,
    )

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK || type == CharacterType.OUTSIDER) {
            Alignment.GOOD
        } else {
            Alignment.EVIL
        },
        type = type,
        scriptIds = setOf(script),
    )
}
