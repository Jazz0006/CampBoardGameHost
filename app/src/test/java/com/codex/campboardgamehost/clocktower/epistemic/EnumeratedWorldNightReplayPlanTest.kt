package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class EnumeratedWorldNightReplayPlanTest {
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

    private val ruleset by lazy {
        RulesetJsonLoader.parseScript(
            json = File("src/main/assets/scripts/trouble_brewing.json").readText(Charsets.UTF_8),
            requestedScriptId = ScriptId("trouble_brewing"),
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )
    }

    private val world = EnumeratedWorld(
        rolesBySeat = linkedMapOf(
            1 to RoleId("Empath"),
            2 to RoleId("Fortune Teller"),
            3 to RoleId("Monk"),
            4 to RoleId("Poisoner"),
            5 to RoleId("Imp"),
        ),
    )

    @Test
    fun `plan keeps canonical schedule separate from durable observation identities`() {
        val empath = observation(1, "Empath", globalSequence = 900L, localSequence = 77)
        val fortuneTeller = observation(2, "Fortune Teller", globalSequence = 901L, localSequence = 3)

        val plan = requireNotNull(
            EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                observations = listOf(fortuneTeller, empath),
            ),
        )

        assertEquals(
            listOf(
                NightOrderToken.System.DUSK,
                NightOrderToken.Character(RoleId("Poisoner")),
                NightOrderToken.Character(RoleId("Monk")),
                NightOrderToken.Character(RoleId("Imp")),
                NightOrderToken.Character(RoleId("Empath")),
                NightOrderToken.Character(RoleId("Fortune Teller")),
                NightOrderToken.System.DAWN,
            ),
            plan.schedule,
        )
        assertSame(empath, plan.observationsAfterScheduleIndex.getValue(4).single())
        assertSame(fortuneTeller, plan.observationsAfterScheduleIndex.getValue(5).single())
        assertEquals(
            900L,
            (empath.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence,
        )
        assertEquals(77, empath.sequence)
    }

    @Test
    fun `durable ability chronology that reverses canonical night order is incompatible with world`() {
        val fortuneTellerFirst = observation(2, "Fortune Teller", globalSequence = 900L, localSequence = 1)
        val empathSecond = observation(1, "Empath", globalSequence = 901L, localSequence = 99)

        assertNull(
            EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                observations = listOf(empathSecond, fortuneTellerFirst),
            ),
        )
    }

    @Test
    fun `single night plan refuses observations from multiple rounds`() {
        val empathRoundTwo = observation(
            sourceSeat = 1,
            sourceAbility = "Empath",
            globalSequence = 900L,
            localSequence = 1,
            round = 2,
        )
        val fortuneTellerRoundThree = observation(
            sourceSeat = 2,
            sourceAbility = "Fortune Teller",
            globalSequence = 901L,
            localSequence = 1,
            round = 3,
        )

        assertNull(
            EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                observations = listOf(empathRoundTwo, fortuneTellerRoundThree),
            ),
        )
    }

    @Test
    fun `observation whose source identity is incompatible with world makes plan incompatible`() {
        val impossibleEmpath = observation(3, "Empath", globalSequence = 900L, localSequence = 1)

        assertNull(
            EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                observations = listOf(impossibleEmpath),
            ),
        )
    }

    private fun observation(
        sourceSeat: Int,
        sourceAbility: String,
        globalSequence: Long,
        localSequence: Int,
        round: Int = 2,
    ): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = round,
            sequence = localSequence,
            globalSequence = globalSequence,
        )
        return RecordedEpistemicObservation(
            recordId = "plan-$sourceSeat-$sourceAbility-$globalSequence",
            phase = StorytellerPhase.NIGHT,
            round = round,
            sequence = localSequence,
            sourceSeat = sourceSeat,
            sourceAbility = RoleId(sourceAbility),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat = sourceSeat,
                value = 0,
            ),
            timelineBinding = ObservationTimelineBinding.Global(point),
        )
    }
}
