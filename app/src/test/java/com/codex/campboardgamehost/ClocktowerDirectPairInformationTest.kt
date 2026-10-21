package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDirectPairInformationTest {
    private val roles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)

    @Test fun `direct role and two seats records exact typed pair`() {
        val selection = ClocktowerDirectPairSelection().chooseRole("Baron")
            .chooseSeat(4, 8).chooseSeat(2, 8)
        val option = requireNotNull(selection.toDisplayOption(
            "Investigator", 8, roles, { it }, "Investigator", "en",
        ))
        val pair = option.proposition as InformationProposition.AnyOf
        assertEquals(listOf(2, 4), pair.alternatives
            .map { (it as InformationProposition.RoleAt).seat })
        assertEquals(RoleId("Baron"),
            (pair.alternatives.first() as InformationProposition.RoleAt).role)
        assertEquals(listOf(2, 4), option.presentationSubjectSeats)
        assertEquals(false, option.isTruthful)
    }

    @Test fun `incomplete unknown or same seats cannot publish`() {
        val selection = ClocktowerDirectPairSelection().chooseRole("Baron").chooseSeat(2, 8)
        assertNull(selection.toDisplayOption("Investigator", 8, roles, { it }, "info", "en"))
        assertEquals(selection, selection.chooseSeat(9, 8))
        assertNull(selection.chooseSeat(2, 8).firstSeat)
        assertNull(ClocktowerDirectPairSelection("Missing", 1, 2)
            .toDisplayOption("Investigator", 8, roles, { it }, "info", "en"))
    }

    @Test fun `none is a typed absence not a derived legal result`() {
        val option = requireNotNull(ClocktowerDirectPairSelection().chooseNone()
            .toDisplayOption("Librarian", 8, roles, { it }, "Librarian", "en"))
        assertEquals("No Outsiders", option.displayPrimary)
        val all = option.proposition as InformationProposition.AllOf
        assertTrue(all.propositions.isNotEmpty())
        assertTrue(all.propositions.all {
            it is InformationProposition.RoleInPlay && !it.inPlay
        })
        assertEquals(emptyList<Int>(), option.presentationSubjectSeats)
    }

    @Test fun `AI pair and absence directly prefill without enumerated Host domain`() {
        val recommended = ClocktowerDirectPairSelection("Baron", 2, 5).toDisplayOption(
            "Investigator", 8, roles, { it }, "Info", "en",
        )
        assertEquals(ClocktowerDirectPairSelection("Baron", 2, 5),
            ClocktowerDirectPairSelection.fromRecommendation(
                recommended, "Investigator", 8, roles))
        assertNull(ClocktowerDirectPairSelection.fromRecommendation(
            recommended, "Investigator", 4, roles))
        val none = ClocktowerDirectPairSelection().chooseNone().toDisplayOption(
            "Librarian", 8, roles, { it }, "Info", "en",
        )
        assertEquals(ClocktowerDirectPairSelection(noneSelected = true),
            ClocktowerDirectPairSelection.fromRecommendation(
                none, "Librarian", 8, roles))
    }

    @Test fun `returning from fullscreen reconstructs the last previewed pair rather than AI default`() {
        val ai = requireNotNull(ClocktowerDirectPairSelection("Baron", 2, 5).toDisplayOption(
            "Investigator", 8, roles, { it }, "Info", "en",
        ))
        val manualPreview = requireNotNull(ClocktowerDirectPairSelection("Scarlet Woman", 3, 7).toDisplayOption(
            "Investigator", 8, roles, { it }, "Info", "en",
        ))
        val restored = ClocktowerDirectPairSelection.fromProposition(
            manualPreview.proposition, "Investigator", 8, roles,
        )
        assertEquals(ClocktowerDirectPairSelection("Scarlet Woman", 3, 7), restored)
        assertTrue(restored != ClocktowerDirectPairSelection.fromRecommendation(
            ai, "Investigator", 8, roles,
        ))
        assertNull(ClocktowerDirectPairSelection.fromProposition(
            manualPreview.proposition, "Investigator", 6, roles,
        ))
    }

    @Test fun `true-role UI marker never invents Spy Recluse registration`() {
        assertEquals("✓", clocktowerPairInformationTruthMarker(true, "Chef", "Chef"))
        assertNull(clocktowerPairInformationTruthMarker(true, "Chef", "Empath"))
        assertNull(clocktowerPairInformationTruthMarker(false, "Chef", "Chef"))
        assertNull(clocktowerPairInformationTruthMarker(true, "Poisoner", "Recluse"))
        assertNull(clocktowerPairInformationTruthMarker(true, "Washerwoman", "Spy"))
    }
}
