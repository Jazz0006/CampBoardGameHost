package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerSageStepMaterializerTest {
    @Test fun `Sage return restores typed preview pair and rejects incomplete or unrelated shapes`() {
        val preview = demonClaim(1, 5)
        assertEquals(listOf(1, 5), clocktowerSagePreviewSeats(preview, 5))
        assertEquals(listOf(2, 4), clocktowerSagePreviewSeats(demonClaim(2, 4), 5))
        assertNull(clocktowerSagePreviewSeats(null, 5))
        assertNull(clocktowerSagePreviewSeats(demonClaim(1, 1), 5))
        assertNull(clocktowerSagePreviewSeats(demonClaim(1, 6), 5))
        assertNull(clocktowerSagePreviewSeats(InformationProposition.RoleAt(1, RoleId("Imp")), 5))
        assertNull(clocktowerSagePreviewSeats(InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(1, RoleId("Imp")),
            InformationProposition.RoleAt(5, RoleId("Chef")),
        )), 5))
    }

    @Test
    fun `Sage manually confirmed pair is recorded as typed information without rules filtering`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat", seat == 2) }
        val builder = ClocktowerInformationStepBuilder(
            cards = cards, language = "en", text = { _, en -> en },
            roleActor = { null }, roleMissingReason = { "missing" },
            abilityStateFor = { _, _ -> null },
        )
        val step = clocktowerSageStepMaterializer(
            builder, cards[3], null, sageContent(),
        ).build()
        val shown = requireNotNull(clocktowerSageManualPlayerDisplay(step, listOf(5, 1), cards.size))
        assertEquals(listOf(1, 5), shown.presentationSubjectSeats)
        assertEquals("1   5", shown.displaySecondary)
        assertEquals(demonClaim(1, 5), shown.displayProposition)
        assertEquals(null, shown.selectedInformationTruthful)
        assertTrue(shown.displayOptions.isEmpty())
        assertEquals(null, clocktowerSageManualPlayerDisplay(step, listOf(1, 1), cards.size))
        assertEquals(null, clocktowerSageManualPlayerDisplay(step, listOf(0, 5), cards.size))
    }

    @Test
    fun `Sage materializer builds direct reliable step from prepared Host facts`() {
        val cards = (1..5).map { seat -> sageMaterializerCard("P$seat", seat == 2) }
        val actor = cards[3]
        val builder = ClocktowerInformationStepBuilder(
            cards = cards,
            language = "en",
            text = { _, en -> en },
            roleActor = { null },
            roleMissingReason = { "missing" },
            abilityStateFor = { _, _ -> null },
        )

        val entry = clocktowerSageStepMaterializer(
            builder = builder,
            triggerActor = actor,
            abilityState = null,
            content = sageContent(),
        )

        val step = entry.build()

        assertEquals("Sage", step.roleEnName)
        assertEquals(actor, step.actor)
        assertTrue(step.presentationSubjectSeats.isEmpty())
        assertEquals(null, step.displaySecondary)
        assertEquals(null, step.displayProposition)
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
    )

    private fun sageMaterializerCard(name: String, demon: Boolean = false) = PlayerCard(
        name = name,
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
