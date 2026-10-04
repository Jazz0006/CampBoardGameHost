package com.codex.campboardgamehost

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
        )

        assertEquals(6, options.size)
        val option = options.single { it.presentationSubjectSeats == listOf(1, 2) }
        assertEquals("1   2", option.displaySecondary)
        assertNull(option.proposition)
        assertTrue(option.isTruthful)
        assertTrue(options.any { !it.isTruthful })
    }

    @Test
    fun `truthful-only Sage projection exposes every legal truthful pair without ranking`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat") }

        val options = clocktowerSageDisplayOptions(
            cards = cards,
            actor = cards[3],
            demon = cards[1],
            truthfulOnly = true,
            content = sageContent(),
        )

        assertEquals(3, options.size)
        assertTrue(options.all { option -> 2 in option.presentationSubjectSeats })
        assertTrue(options.all { it.isTruthful })
        assertTrue(options.all { it.proposition == null })
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
