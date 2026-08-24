package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedHistoricalExactBaselineTest {
    private val script = ScriptId("trouble_brewing")
    private val legacyKnowledge by lazy {
        RulesetJsonLoader.parse(
            File("src/main/assets/rules/trouble_brewing.json").readText(Charsets.UTF_8),
        )
    }
    private val registry by lazy {
        LegacyRulesetCatalogAdapter.characterRegistry(
            knowledge = legacyKnowledge,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            coverage = RuleCoverage.PARTIAL,
        )
    }
    private val validatedRuleset by lazy {
        RulesetJsonLoader.parseScript(
            json = File("src/main/assets/scripts/trouble_brewing.json").readText(Charsets.UTF_8),
            requestedScriptId = script,
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )
    }
    private val ruleset by lazy {
        validatedRuleset.toRulesetRef(
            rulesetVersion = "a3-historical-constructor-test",
            sourceRevision = "official",
        )
    }
    private val roles = listOf(
        role("Empath", CharacterType.TOWNSFOLK),
        role("Chef", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Poisoner", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )
    private val setupKnowledge = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-a3-historical-constructor",
        formalSnapshotId = "snapshot-a3-historical-constructor",
        recipientSeat = 1,
        perceivedRole = RoleId("Empath"),
        setupKnowledge = listOf(InformationProposition.SetupProfile(3, 0, 1, 1)),
    )

    @Test
    fun `exact baseline constructs setup worlds then replays knowledge safe global history`() {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.Poison("hidden-poison", 10L, 4),
                    StorytellerPhase.NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 10L,
                ),
                action(
                    ActionFact.PhaseAdvance("day-1", 15L, StorytellerPhase.DAY, 1),
                    StorytellerPhase.NIGHT,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 15L,
                ),
                action(
                    ActionFact.Execution("execution-seat-2", 20L, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 3,
                    globalSequence = 20L,
                ),
                action(
                    ActionFact.PhaseAdvance("night-2", 30L, StorytellerPhase.NIGHT, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 4,
                    globalSequence = 30L,
                ),
            ),
        )

        val result = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(),
        )

        assertFalse(result.worldSet.isEmpty())
        assertTrue(result.worldSet.enumeratedWorlds().all { 2 !in it.aliveSeats })
        assertEquals(StorytellerPhase.NIGHT, result.phase)
        assertEquals(2, result.round)
        assertEquals(30L, result.lastGlobalSequence)
        assertEquals(
            setOf(1, 2, 3, 4, 5),
            result.worldSet.enumeratedWorlds().flatMap { world ->
                world.abilityStatesBySeat.filterValues { it == AbilityState.MALFUNCTIONING_POISONED }.keys
            }.toSet(),
        )
    }

    @Test
    fun `exact baseline uses validated night order to reject reversed ability chronology`() {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.PhaseAdvance("day-1", 1L, StorytellerPhase.DAY, 1),
                    StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 1L,
                ),
                action(
                    ActionFact.PhaseAdvance("night-2", 2L, StorytellerPhase.NIGHT, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 2L,
                ),
            ),
        )
        val fortuneTellerFirst = nightAbilityObservation(
            recordId = "fortune-teller-first",
            sourceSeat = 2,
            sourceAbility = RoleId("Fortune Teller"),
            globalSequence = 10L,
        )
        val empathSecond = nightAbilityObservation(
            recordId = "empath-second",
            sourceSeat = 1,
            sourceAbility = RoleId("Empath"),
            globalSequence = 11L,
        )

        val result = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(listOf(fortuneTellerFirst, empathSecond)),
        )

        assertTrue(result.worldSet.isEmpty())
    }

    @Test
    fun `persisted hidden attack and protect payloads do not constrain exact player worlds`() {
        val rolesBySeat = linkedMapOf(
            1 to RoleId("Empath"),
            2 to RoleId("Chef"),
            3 to RoleId("Monk"),
            4 to RoleId("Poisoner"),
            5 to RoleId("Imp"),
        )
        val roleDefinitions = listOf(
            role("Empath", CharacterType.TOWNSFOLK),
            role("Chef", CharacterType.TOWNSFOLK),
            role("Monk", CharacterType.TOWNSFOLK),
            role("Poisoner", CharacterType.MINION),
            role("Imp", CharacterType.DEMON),
        )
        val controlActions = ActionFactTimeline(
            listOf(
                phaseAdvance("day-1", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1, 1L),
                phaseAdvance("night-2", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2, 2L),
                phaseAdvance("day-2", StorytellerPhase.NIGHT, 2, StorytellerPhase.DAY, 2, 7L),
            ),
        )
        val hiddenPayloadA = ActionFactTimeline(
            listOf(
                phaseAdvance("day-1", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1, 1L),
                phaseAdvance("night-2", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2, 2L),
                action(ActionFact.Protect("protect-a", 3L, 1), StorytellerPhase.NIGHT, 2, 3, 3L),
                action(ActionFact.Attack("attack-a", 4L, 1), StorytellerPhase.NIGHT, 2, 4, 4L),
                phaseAdvance("day-2", StorytellerPhase.NIGHT, 2, StorytellerPhase.DAY, 2, 7L),
            ),
        )
        val hiddenPayloadB = ActionFactTimeline(
            listOf(
                phaseAdvance("day-1", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1, 1L),
                phaseAdvance("night-2", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2, 2L),
                action(ActionFact.Protect("protect-b", 3L, 5), StorytellerPhase.NIGHT, 2, 3, 3L),
                action(ActionFact.Attack("attack-b", 4L, 5), StorytellerPhase.NIGHT, 2, 4, 4L),
                phaseAdvance("day-2", StorytellerPhase.NIGHT, 2, StorytellerPhase.DAY, 2, 7L),
            ),
        )

        val control = buildFixed(
            rolesBySeat = rolesBySeat,
            profile = InformationProposition.SetupProfile(3, 0, 1, 1),
            roleDefinitions = roleDefinitions,
            actions = controlActions,
            id = "hidden-attack-protect-control",
        )
        val payloadA = buildFixed(
            rolesBySeat = rolesBySeat,
            profile = InformationProposition.SetupProfile(3, 0, 1, 1),
            roleDefinitions = roleDefinitions,
            actions = hiddenPayloadA,
            id = "hidden-attack-protect-a",
        )
        val payloadB = buildFixed(
            rolesBySeat = rolesBySeat,
            profile = InformationProposition.SetupProfile(3, 0, 1, 1),
            roleDefinitions = roleDefinitions,
            actions = hiddenPayloadB,
            id = "hidden-attack-protect-b",
        )

        assertFalse(control.worldSet.isEmpty())
        assertReplayMechanicallyEquivalent(control, payloadA)
        assertReplayMechanicallyEquivalent(control, payloadB)
        assertEquals(7L, payloadA.lastGlobalSequence)
        assertEquals(7L, payloadB.lastGlobalSequence)
    }

    @Test
    fun `persisted hidden role change target does not select a rule derived Imp successor`() {
        val rolesBySeat = linkedMapOf(
            1 to RoleId("Washerwoman"),
            2 to RoleId("Librarian"),
            3 to RoleId("Investigator"),
            4 to RoleId("Chef"),
            5 to RoleId("Empath"),
            6 to RoleId("Monk"),
            7 to RoleId("Ravenkeeper"),
            8 to RoleId("Poisoner"),
            9 to RoleId("Spy"),
            10 to RoleId("Imp"),
        )
        val roleDefinitions = listOf(
            role("Washerwoman", CharacterType.TOWNSFOLK),
            role("Librarian", CharacterType.TOWNSFOLK),
            role("Investigator", CharacterType.TOWNSFOLK),
            role("Chef", CharacterType.TOWNSFOLK),
            role("Empath", CharacterType.TOWNSFOLK),
            role("Monk", CharacterType.TOWNSFOLK),
            role("Ravenkeeper", CharacterType.TOWNSFOLK),
            role("Poisoner", CharacterType.MINION),
            role("Spy", CharacterType.MINION),
            role("Imp", CharacterType.DEMON),
        )
        val controlActions = ActionFactTimeline(
            listOf(
                phaseAdvance("day-1", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1, 1L),
                phaseAdvance("night-2", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2, 2L),
                action(ActionFact.Death("imp-public-death", 4L, 10), StorytellerPhase.NIGHT, 2, 4, 4L),
                phaseAdvance("day-2", StorytellerPhase.NIGHT, 2, StorytellerPhase.DAY, 2, 7L),
            ),
        )
        fun actionsWithPersistedSuccessor(targetSeat: Int, suffix: String) = ActionFactTimeline(
            listOf(
                phaseAdvance("day-1", StorytellerPhase.FIRST_NIGHT, 1, StorytellerPhase.DAY, 1, 1L),
                phaseAdvance("night-2", StorytellerPhase.DAY, 1, StorytellerPhase.NIGHT, 2, 2L),
                action(ActionFact.Death("imp-public-death", 4L, 10), StorytellerPhase.NIGHT, 2, 4, 4L),
                action(
                    ActionFact.RoleChange(
                        actionId = "persisted-starpass-$suffix",
                        sequence = 5L,
                        targetSeat = targetSeat,
                        role = RoleId("Imp"),
                        alignment = Alignment.EVIL,
                        type = CharacterType.DEMON,
                    ),
                    StorytellerPhase.NIGHT,
                    2,
                    5,
                    5L,
                ),
                phaseAdvance("day-2", StorytellerPhase.NIGHT, 2, StorytellerPhase.DAY, 2, 7L),
            ),
        )

        val control = buildFixed(
            rolesBySeat = rolesBySeat,
            profile = InformationProposition.SetupProfile(7, 0, 2, 1),
            roleDefinitions = roleDefinitions,
            actions = controlActions,
            id = "hidden-role-change-control",
        )
        val poisonerPayload = buildFixed(
            rolesBySeat = rolesBySeat,
            profile = InformationProposition.SetupProfile(7, 0, 2, 1),
            roleDefinitions = roleDefinitions,
            actions = actionsWithPersistedSuccessor(8, "poisoner"),
            id = "hidden-role-change-poisoner",
        )
        val spyPayload = buildFixed(
            rolesBySeat = rolesBySeat,
            profile = InformationProposition.SetupProfile(7, 0, 2, 1),
            roleDefinitions = roleDefinitions,
            actions = actionsWithPersistedSuccessor(9, "spy"),
            id = "hidden-role-change-spy",
        )

        assertFalse(control.worldSet.isEmpty())
        assertReplayMechanicallyEquivalent(control, poisonerPayload)
        assertReplayMechanicallyEquivalent(control, spyPayload)
        assertEquals(
            setOf(8, 9),
            control.worldSet.enumeratedWorlds().map { world ->
                world.currentRolesBySeat.entries.single { (seat, role) ->
                    seat in world.aliveSeats && role.value.equals("Imp", ignoreCase = true)
                }.key
            }.toSet(),
        )
        assertEquals(7L, poisonerPayload.lastGlobalSequence)
        assertEquals(7L, spyPayload.lastGlobalSequence)
    }

    private fun buildFixed(
        rolesBySeat: LinkedHashMap<Int, RoleId>,
        profile: InformationProposition.SetupProfile,
        roleDefinitions: List<RoleDefinition>,
        actions: ActionFactTimeline,
        id: String,
    ): EnumeratedHistoricalReplayResult {
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-$id",
            formalSnapshotId = "snapshot-$id",
            recipientSeat = 1,
            perceivedRole = rolesBySeat.getValue(1),
            setupKnowledge = listOf(profile) + rolesBySeat.map { (seat, role) ->
                InformationProposition.RoleAt(seat, role)
            },
        )
        return EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = knowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roleDefinitions,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(),
        )
    }

    private fun assertReplayMechanicallyEquivalent(
        expected: EnumeratedHistoricalReplayResult,
        actual: EnumeratedHistoricalReplayResult,
    ) {
        assertEquals(expected.phase, actual.phase)
        assertEquals(expected.round, actual.round)
        assertEquals(expected.lastGlobalSequence, actual.lastGlobalSequence)
        assertEquals(mechanicalStates(expected), mechanicalStates(actual))
    }

    private fun mechanicalStates(result: EnumeratedHistoricalReplayResult): Set<MechanicalStateKey> =
        result.worldSet.enumeratedWorlds().map { world ->
            MechanicalStateKey(
                rolesBySeat = world.rolesBySeat,
                currentRolesBySeat = world.currentRolesBySeat,
                redHerringSeat = world.redHerringSeat,
                shownRolesBySeat = world.shownRolesBySeat,
                aliveSeats = world.aliveSeats,
                abilityStatesBySeat = world.abilityStatesBySeat,
            )
        }.toSet()

    private data class MechanicalStateKey(
        val rolesBySeat: Map<Int, RoleId>,
        val currentRolesBySeat: Map<Int, RoleId>,
        val redHerringSeat: Int?,
        val shownRolesBySeat: Map<Int, RoleId>,
        val aliveSeats: Set<Int>,
        val abilityStatesBySeat: Map<Int, AbilityState>,
    )

    private fun nightAbilityObservation(
        recordId: String,
        sourceSeat: Int,
        sourceAbility: RoleId,
        globalSequence: Long,
    ): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            globalSequence = globalSequence,
        )
        return RecordedEpistemicObservation(
            recordId = recordId,
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = globalSequence.toInt(),
            sourceSeat = sourceSeat,
            sourceAbility = sourceAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(setupKnowledge.recipientSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.AliveAt(3, true),
            timelineBinding = ObservationTimelineBinding.Global(point),
        )
    }

    private fun phaseAdvance(
        actionId: String,
        pointPhase: StorytellerPhase,
        pointRound: Int,
        nextPhase: StorytellerPhase,
        nextRound: Int,
        globalSequence: Long,
    ): TimelineBoundActionFact = action(
        ActionFact.PhaseAdvance(actionId, globalSequence, nextPhase, nextRound),
        pointPhase,
        pointRound,
        globalSequence.toInt(),
        globalSequence,
    )

    private fun action(
        fact: ActionFact,
        phase: StorytellerPhase,
        round: Int,
        localSequence: Int,
        globalSequence: Long,
    ) = TimelineBoundActionFact(
        fact = fact,
        point = TimelinePoint(
            phase = phase,
            round = round,
            sequence = localSequence,
            globalSequence = globalSequence,
        ),
    )

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
        type = type,
        scriptIds = setOf(script),
    )
}
