package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalRecommendation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSageStepMaterializerTest {
    @Test
    fun `Sage option projection carries typed pair seats without epistemic proposition`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat") }
        val options = clocktowerSageDisplayOptions(
            cards = cards,
            actor = cards[3],
            demon = cards[1],
            truthfulOnly = false,
            content = sageContent(),
            recommendCategory = { candidates ->
                val candidate = candidates.first { it.id == "0:1" }
                listOf(
                    UnreliableCategoricalRecommendation(
                        candidateId = candidate.id,
                        style = RecommendationStyle.BALANCED,
                        totalScore = 0,
                        warningIds = emptyList(),
                    ),
                )
            },
            isEvil = { it.name == cards[1].name },
            recommendationStyleLabel = { it.name },
        )

        val option = options.single()
        assertEquals(listOf(1, 2), option.presentationSubjectSeats)
        assertEquals("1   2", option.displaySecondary)
        assertNull(option.proposition)
        assertTrue(option.isTruthful)
    }

    @Test
    fun `truthful-only Sage projection filters false pairs before recommendation`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat") }
        var sawOnlyTruthfulCandidates = false

        val options = clocktowerSageDisplayOptions(
            cards = cards,
            actor = cards[3],
            demon = cards[1],
            truthfulOnly = true,
            content = sageContent(),
            recommendCategory = { candidates ->
                sawOnlyTruthfulCandidates = candidates.isNotEmpty() && candidates.all { it.isTruthful }
                candidates.take(1).map { candidate ->
                    UnreliableCategoricalRecommendation(
                        candidateId = candidate.id,
                        style = RecommendationStyle.GENTLE,
                        totalScore = 0,
                        warningIds = emptyList(),
                    )
                }
            },
            isEvil = { it.name == cards[1].name },
            recommendationStyleLabel = { it.name },
        )

        assertTrue(sawOnlyTruthfulCandidates)
        assertEquals(1, options.size)
        assertTrue(cards[1].let { demon -> options.single().presentationSubjectSeats.contains(cards.indexOf(demon) + 1) })
        assertNull(options.single().proposition)
    }

    @Test
    fun `Sage materializer builds direct reliable step from prepared Host facts`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat") }
        val actor = cards[3]
        val demon = cards[1]
        val builder = ClocktowerInformationStepBuilder(
            cards = cards,
            language = "en",
            automaticStorytellerInfo = false,
            text = { _, en -> en },
            roleActor = { null },
            roleMissingReason = { "missing" },
            abilityStateFor = { _, _ -> null },
            actorIsUnreliable = { _, _ -> false },
            recentMisinformationStreak = { 0 },
        )

        val entry = clocktowerSageStepMaterializer(
            builder = builder,
            cards = cards,
            triggerActor = actor,
            demon = demon,
            directPair = demon to cards[4],
            abilityState = null,
            content = sageContent(),
            recommendCategory = { emptyList() },
            isEvil = { it.name == demon.name },
            recommendationStyleLabel = { it.name },
        )

        val step = entry.build()

        assertEquals("Sage", step.roleEnName)
        assertEquals(actor, step.actor)
        assertEquals(listOf(2, 5), step.presentationSubjectSeats)
        assertEquals("2   5", step.displaySecondary)
        assertNull(step.displayProposition)
    }

    private fun sageContent() = ClocktowerSageStepContent(
        explanation = "When killed by the Demon, the Sage learns that the Demon is one of two players.",
        displayTitle = "Sage information",
        displayPrimary = "Demon",
        displayFooter = "One of these two players",
        hostInstruction = "Wake the Sage and show two players.",
        highPressureSuffix = " high pressure",
    )

    private fun sageMaterializerCard(name: String) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = ClocktowerTeam.Townsfolk,
        clocktowerRole = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "贤者",
            enName = "Sage",
            zhDescription = "",
            enDescription = "",
        ),
    )
}
