package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
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
import org.junit.Test

class EnumeratedWorldNightObservationAnchorTest {
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
    fun `night observation anchors to canonical role slot without borrowing durable timeline identity`() {
        val world = world("Empath", "Chef", "Monk", "Poisoner", "Imp")
        val record = nightObservation(
            sourceSeat = 1,
            sourceAbility = "Empath",
            localSequence = 77,
            globalSequence = 900L,
        )

        assertEquals(
            EnumeratedWorldNightObservationAnchor(
                roleId = RoleId("Empath"),
                scheduleIndex = 4,
            ),
            EnumeratedWorldNightObservationAnchoring.anchorOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                record = record,
            ),
        )
    }

    @Test
    fun `Drunk shown ability anchors to shown waking slot while actual role remains Drunk`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Drunk"),
                2 to RoleId("Chef"),
                3 to RoleId("Empath"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            shownRolesBySeat = mapOf(1 to RoleId("Fortune Teller")),
        )
        val record = nightObservation(
            sourceSeat = 1,
            sourceAbility = "Fortune Teller",
            localSequence = 3,
            globalSequence = 901L,
            round = 1,
        )

        val anchor = EnumeratedWorldNightObservationAnchoring.anchorOrNull(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            world = world,
            record = record,
        )

        assertEquals(RoleId("Fortune Teller"), anchor?.roleId)
        assertEquals(RoleId("Drunk"), world.rolesBySeat.getValue(1))
    }

    @Test
    fun `role existing elsewhere does not anchor observation from an incompatible source seat`() {
        val world = world("Chef", "Empath", "Monk", "Poisoner", "Imp")
        val record = nightObservation(
            sourceSeat = 1,
            sourceAbility = "Empath",
            localSequence = 2,
            globalSequence = 902L,
        )

        assertNull(
            EnumeratedWorldNightObservationAnchoring.anchorOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                record = record,
            ),
        )
    }

    @Test
    fun `public or non-ability observation has no canonical role-slot anchor`() {
        val world = world("Empath", "Chef", "Monk", "Poisoner", "Imp")
        val point = TimelinePoint(StorytellerPhase.NIGHT, 2, 8, 903L)
        val record = RecordedEpistemicObservation(
            recordId = "public-night-fact",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 8,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, false),
            timelineBinding = ObservationTimelineBinding.Global(point),
        )

        assertNull(
            EnumeratedWorldNightObservationAnchoring.anchorOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                record = record,
            ),
        )
    }

    private fun nightObservation(
        sourceSeat: Int,
        sourceAbility: String,
        localSequence: Int,
        globalSequence: Long,
        round: Int = 2,
    ): RecordedEpistemicObservation {
        val point = TimelinePoint(StorytellerPhase.NIGHT, round, localSequence, globalSequence)
        return RecordedEpistemicObservation(
            recordId = "night-observation-$sourceSeat-$sourceAbility-$globalSequence",
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

    private fun world(vararg roles: String): EnumeratedWorld = EnumeratedWorld(
        rolesBySeat = roles.mapIndexed { index, role -> index + 1 to RoleId(role) }.toMap(),
    )
}
