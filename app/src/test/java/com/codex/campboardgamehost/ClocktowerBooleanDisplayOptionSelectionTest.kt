package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerBooleanDisplayOptionSelectionTest {
    @Test
    fun `selection matches exact typed boolean proposition instead of localized label`() {
        val expected = option(
            label = "expected",
            sourceSeat = 4,
            subjectSeats = listOf(2, 7),
            value = true,
            displayPrimary = "有",
        )
        val wrongPairSameLabel = option(
            label = "wrong-pair",
            sourceSeat = 4,
            subjectSeats = listOf(2, 8),
            value = true,
            displayPrimary = "有",
        )
        val wrongValueSameLabel = option(
            label = "wrong-value",
            sourceSeat = 4,
            subjectSeats = listOf(2, 7),
            value = false,
            displayPrimary = "有",
        )

        val selected = findBooleanDisplayOption(
            options = listOf(wrongPairSameLabel, wrongValueSameLabel, expected),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = 4,
            subjectSeats = listOf(2, 7),
            value = true,
        )

        assertEquals(expected, selected)
    }

    @Test
    fun `selection fails closed when no exact typed proposition exists`() {
        val selected = findBooleanDisplayOption(
            options = listOf(
                option(
                    label = "different-pair",
                    sourceSeat = 4,
                    subjectSeats = listOf(1, 7),
                    value = false,
                    displayPrimary = "No",
                ),
            ),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = 4,
            subjectSeats = listOf(2, 7),
            value = false,
        )

        assertNull(selected)
    }

    private fun option(
        label: String,
        sourceSeat: Int,
        subjectSeats: List<Int>,
        value: Boolean,
        displayPrimary: String,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.YesNo,
        displayTitle = "Fortune Teller information",
        displayPrimary = displayPrimary,
        displaySecondary = null,
        displayFooter = "Checking these two players",
        proposition = InformationProposition.BooleanResult(
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = sourceSeat,
            subjectSeats = subjectSeats,
            value = value,
        ),
    )
}
