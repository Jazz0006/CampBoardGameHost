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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedHistoricalAbilityEligibilityTest {
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

    @Test
    fun `H2 dead standard nightly information role keeps canonical slot but cannot produce ordinary later-night observation`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Monk"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            aliveSeats = setOf(2, 3, 4, 5),
        )
        val schedule = EnumeratedWorldNightSchedule.plan(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            world = world,
        )

        assertTrue(NightOrderToken.Character(RoleId("Empath")) in schedule)
        assertNull(
            EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                observations = listOf(observation(1, "Empath", 900L)),
            ),
        )
    }

    @Test
    fun `H2 triggered death ability is not rejected merely because source seat is dead`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Ravenkeeper"),
                2 to RoleId("Chef"),
                3 to RoleId("Monk"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            aliveSeats = setOf(2, 3, 4, 5),
        )
        val schedule = EnumeratedWorldNightSchedule.plan(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            world = world,
        )

        assertTrue(NightOrderToken.Character(RoleId("Ravenkeeper")) in schedule)
        assertNotNull(
            EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                observations = listOf(observation(1, "Ravenkeeper", 901L)),
            ),
        )
    }

    private fun observation(
        sourceSeat: Int,
        sourceAbility: String,
        globalSequence: Long,
    ): RecordedEpistemicObservation {
        val point = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            globalSequence = globalSequence,
        )
        return RecordedEpistemicObservation(
            recordId = "h2-$sourceSeat-$sourceAbility-$globalSequence",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            sourceSeat = sourceSeat,
            sourceAbility = RoleId(sourceAbility),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.RoleAt(2, RoleId("Chef")),
            timelineBinding = ObservationTimelineBinding.Global(point),
        )
    }
}
