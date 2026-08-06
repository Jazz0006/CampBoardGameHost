package com.codex.campboardgamehost.clocktower.simulation

import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.AutomaticInformationPolicy
import com.codex.campboardgamehost.clocktower.recommendation.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationSearch
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableNumberInformationRecommender
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableNumberRecommendation
import com.codex.campboardgamehost.clocktower.rules.PlanLegalityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val BASELINE_SAMPLE_SIZE = 1_000
private const val BASELINE_SEED = 4_000_000L

private data class SetupBaseline(
    val shownRoleCounts: Map<String, Int>,
    val templateCounts: Map<String, Int>,
    val repeatedTemplateTransitions: Int,
    val longestTemplateRun: Int,
    val illegalPlanCount: Int,
)

private data class DynamicNumberBaseline(
    val shownValueCounts: Map<Int, Int>,
    val truthfulCount: Int,
)

private data class StorytellerV4BaselineReport(
    val sampleSize: Int,
    val setup: SetupBaseline,
    val dynamicNumber: DynamicNumberBaseline,
) {
    fun toMarkdown(): String = buildString {
        appendLine("STORYTELLER_V4_BASELINE_START")
        appendLine("Sample size: $sampleSize")
        appendLine()
        appendLine("Drunk shown-role distribution:")
        setup.shownRoleCounts.toSortedMap().forEach { (role, count) ->
            appendLine("- $role: $count (${percentage(count, sampleSize)})")
        }
        appendLine()
        appendLine("Dynamic poisoned numeric distribution (truth=0, range=0..2, balanced):")
        dynamicNumber.shownValueCounts.toSortedMap().forEach { (value, count) ->
            appendLine("- $value: $count (${percentage(count, sampleSize)})")
        }
        appendLine("- truthful: ${dynamicNumber.truthfulCount} (${percentage(dynamicNumber.truthfulCount, sampleSize)})")
        appendLine()
        appendLine("Initial-template repetition:")
        appendLine("- distinct templates: ${setup.templateCounts.size}")
        appendLine("- repeated adjacent transitions: ${setup.repeatedTemplateTransitions}/${sampleSize - 1} " +
            "(${percentage(setup.repeatedTemplateTransitions, sampleSize - 1)})")
        appendLine("- longest identical run: ${setup.longestTemplateRun}")
        appendLine("- illegal selected plans: ${setup.illegalPlanCount}")
        appendLine("STORYTELLER_V4_BASELINE_END")
    }

    private fun percentage(count: Int, total: Int): String = "%.1f%%".format(count * 100.0 / total)
}

private object StorytellerV4BaselineSimulator {
    private val allRoleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
    private val setupRoleIds = setOf(
        "Chef",
        "Empath",
        "Fortune Teller",
        "Undertaker",
        "Virgin",
        "Drunk",
        "Scarlet Woman",
        "Imp",
        "Investigator",
        "Monk",
        "Soldier",
    )
    private val setupRoleDefinitions = allRoleDefinitions.filter { it.id.value in setupRoleIds }

    fun run(sampleSize: Int = BASELINE_SAMPLE_SIZE): StorytellerV4BaselineReport {
        require(sampleSize > 1)
        return StorytellerV4BaselineReport(
            sampleSize = sampleSize,
            setup = setupBaseline(sampleSize),
            dynamicNumber = dynamicNumberBaseline(sampleSize),
        )
    }

    private fun setupBaseline(sampleSize: Int): SetupBaseline {
        val shownRoles = mutableMapOf<String, Int>()
        val templates = mutableMapOf<String, Int>()
        var previousTemplate: String? = null
        var repeatedTransitions = 0
        var currentRun = 0
        var longestRun = 0
        var illegalPlans = 0

        repeat(sampleSize) { index ->
            val game = compactSetupGame(BASELINE_SEED + index)
            val selected = RecommendationSearch
                .recommend(game, setupRoleDefinitions)
                .first { it.style == RecommendationStyle.BALANCED }
            if (PlanLegalityValidator.validate(game, setupRoleDefinitions, CandidatePlan(selected.decisions)).isNotEmpty()) {
                illegalPlans += 1
            }

            val shownRole = selected.decisions
                .filterIsInstance<StorytellerDecision.DrunkShownRole>()
                .single()
                .role
                .value
            shownRoles[shownRole] = shownRoles.getOrDefault(shownRole, 0) + 1

            val template = selected.decisions.joinToString("|") { it.toString() }
            templates[template] = templates.getOrDefault(template, 0) + 1
            if (template == previousTemplate) {
                repeatedTransitions += 1
                currentRun += 1
            } else {
                currentRun = 1
                previousTemplate = template
            }
            longestRun = maxOf(longestRun, currentRun)
        }

        return SetupBaseline(
            shownRoleCounts = shownRoles.toMap(),
            templateCounts = templates.toMap(),
            repeatedTemplateTransitions = repeatedTransitions,
            longestTemplateRun = longestRun,
            illegalPlanCount = illegalPlans,
        )
    }

    private fun dynamicNumberBaseline(sampleSize: Int): DynamicNumberBaseline {
        val context = UnreliableNumberContext(
            trueValue = 0,
            minimumValue = 0,
            maximumValue = 2,
            pressureCostPerPoint = 2,
        )
        val options = UnreliableNumberInformationRecommender.recommend(context)
        val counts = mutableMapOf<Int, Int>()

        repeat(sampleSize) { index ->
            val selected = requireNotNull(
                AutomaticInformationPolicy.select(
                    options = options,
                    reliability = InformationReliability.POISONED,
                    style = RecommendationStyle.BALANCED,
                    evilAdvantage = 0,
                    stableKey = "v4-baseline:poisoned-number:$index",
                    recentMisinformationStreak = 0,
                    isTruthful = { it.value == context.trueValue },
                    misinformationPressure = UnreliableNumberRecommendation::value,
                    styleOf = UnreliableNumberRecommendation::style,
                ),
            )
            counts[selected.value] = counts.getOrDefault(selected.value, 0) + 1
        }

        return DynamicNumberBaseline(
            shownValueCounts = counts.toMap(),
            truthfulCount = counts.getOrDefault(context.trueValue, 0),
        )
    }

    private fun compactSetupGame(seed: Long): GameState {
        val definitionsById = allRoleDefinitions.associateBy { it.id.value }
        fun player(seat: Int, role: String, shownRole: String = role): PlayerState {
            val definition = requireNotNull(definitionsById[role])
            return PlayerState(
                seat = seat,
                name = "Player $seat",
                actualRole = definition.id,
                actualAlignment = definition.alignment,
                actualType = definition.type,
                shownRole = RoleId(shownRole),
            )
        }
        return GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = seed,
            players = listOf(
                player(1, "Chef"),
                player(2, "Empath"),
                player(3, "Fortune Teller"),
                player(4, "Undertaker"),
                player(5, "Virgin"),
                player(6, "Drunk", shownRole = "Investigator"),
                player(7, "Scarlet Woman"),
                player(8, "Imp"),
            ),
        )
    }
}

class StorytellerV4BaselineSimulationTest {
    @Test
    fun `fixed seed baseline simulation is reproducible`() {
        assertEquals(
            StorytellerV4BaselineSimulator.run(sampleSize = 25),
            StorytellerV4BaselineSimulator.run(sampleSize = 25),
        )
    }

    @Test
    fun `one thousand game baseline records current behavior without blessing known defects`() {
        val report = StorytellerV4BaselineSimulator.run()

        assertEquals(BASELINE_SAMPLE_SIZE, report.setup.shownRoleCounts.values.sum())
        assertEquals(BASELINE_SAMPLE_SIZE, report.setup.templateCounts.values.sum())
        assertEquals(BASELINE_SAMPLE_SIZE, report.dynamicNumber.shownValueCounts.values.sum())
        assertEquals(0, report.setup.illegalPlanCount)
        assertTrue(report.setup.templateCounts.isNotEmpty())
        println(report.toMarkdown())
    }
}
