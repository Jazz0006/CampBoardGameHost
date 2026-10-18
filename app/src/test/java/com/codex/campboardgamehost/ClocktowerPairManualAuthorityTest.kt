package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerPairManualAuthorityTest {
    private fun option(role: String, first: Int, second: Int) = ClocktowerDisplayOption(
        label = "$role $first/$second",
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = "Information",
        displayPrimary = role,
        displaySecondary = "$first / $second",
        displayFooter = null,
        proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(first, RoleId(role)),
            InformationProposition.RoleAt(second, RoleId(role)),
        )),
    )

    @Test fun `manual choice resolves selected structured role and seats without a Rules domain`() {
        val a = option("Chef", 2, 5)
        val b = option("Chef", 3, 5)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(listOf(a, b))
        val selected = ClocktowerPairManualSelectionModel.from(presentation)
            .selectRole("Chef").selectSeat(2).selectSeat(5)
        assertEquals(a, selected.resolvedOption)
    }

    @Test fun `recommendation projection matches only an existing structurally identical manual option`() {
        val a = option("Chef", 2, 5)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(listOf(a))
        assertEquals(a, ClocktowerPairManualAuthority.canonicalManualOption(
            presentation, a.copy(label = "localized recommendation"),
        ))
        assertNull(ClocktowerPairManualAuthority.canonicalManualOption(
            presentation, option("Chef", 1, 4),
        ))
    }
}

