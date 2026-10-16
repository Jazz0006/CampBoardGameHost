package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSageStepMaterializerTest {
    @Test
    fun `Sage option retains actually selected demon candidate seats as a typed proposition`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat", seat == 2) }
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
        assertEquals(demonClaim(1, 2), option.proposition)
        assertTrue(option.isTruthful)
        assertTrue(options.any { !it.isTruthful })
    }

    @Test
    fun `truthful-only Sage projection exposes every legal truthful pair without ranking`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat", seat == 2) }

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
        assertTrue(options.all { option ->
            option.proposition == demonClaim(
                option.presentationSubjectSeats[0], option.presentationSubjectSeats[1],
            )
        })
    }

    @Test
    fun `Sage materializer builds direct reliable step from prepared Host facts`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat", seat == 2) }
        val actor = cards[3]
        val demon = cards[1]
        val builder = ClocktowerInformationStepBuilder(
            cards = cards,
            language = "en",
            text = { _, en -> en },
            roleActor = { null },
            roleMissingReason = { "missing" },
            abilityStateFor = { _, _ -> null },
            actorIsUnreliable = { _, _ -> false },
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
        assertEquals(demonClaim(2, 5), step.displayProposition)
    }

    private fun demonClaim(first: Int, second: Int) = InformationProposition.AnyOf(listOf(
        InformationProposition.RoleAt(first, RoleId("Imp")),
        InformationProposition.RoleAt(second, RoleId("Imp")),
    ))

    private fun sageContent() = ClocktowerSageStepContent(
        explanation = "When killed by the Demon, the Sage learns that the Demon is one of two players.",
        displayTitle = "Sage information",
        displayPrimary = "Demon",
        displayFooter = "One of these two players",
        hostInstruction = "Wake the Sage and show two players.",
        highPressureSuffix = " high pressure",
    )

    private fun sageMaterializerCard(name: String, demon: Boolean = false) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = if (demon) ClocktowerTeam.Demon else ClocktowerTeam.Townsfolk,
        clocktowerRole = ClocktowerRole(
            team = if (demon) ClocktowerTeam.Demon else ClocktowerTeam.Townsfolk,
            zhName = if (demon) "小恶魔" else "贤者",
            enName = if (demon) "Imp" else "Sage",
            zhDescription = "",
            enDescription = "",
        ),
    )
}
