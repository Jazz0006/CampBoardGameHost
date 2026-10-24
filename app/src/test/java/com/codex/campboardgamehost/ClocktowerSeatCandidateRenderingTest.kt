package com.codex.campboardgamehost

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.Alignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Render the production seats: parameter tests cannot detect a Surface hiding its child stroke. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ClocktowerSeatCandidateRenderingTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun ordinarySpecialAndSelectedOutlinesAreVisibleOnTheActualTable() {
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerSquareTableSeatSurface(
                    seats = (1..7).map { i ->
                        ClocktowerSquareTableSeatUiModel(
                            seatId = "$i", seatNumber = i, label = "P$i",
                            actualAlignment = Alignment.GOOD,
                            isCandidate = i == 2,
                            isSpecialCandidate = i == 3 || i == 4,
                            isSelected = i == 4,
                            isInteractionEnabled = true,
                        )
                    },
                    modifier = Modifier.size(360.dp, 520.dp),
                    interactionMode = ClocktowerSquareTableInteractionMode.Selectable,
                    onSeatClick = {},
                )
            }
        }
        val bounds = (1..4).associateWith { i ->
            ui.onNode(hasClickAction() and hasText("P$i")).fetchSemanticsNode().boundsInRoot
        }
        ui.runOnIdle {
            val view = ui.activity.findViewById<View>(android.R.id.content)
            val image = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            try {
                view.draw(Canvas(image))
                fun colorPixels(seat: Int, color: Int): Int {
                    val b = bounds.getValue(seat)
                    return (b.left.toInt() until b.right.toInt()).sumOf { x ->
                        (b.top.toInt() until b.bottom.toInt()).count { y -> image.getPixel(x, y) == color }
                    }
                }
                assertEquals(0, colorPixels(1, 0xFFFFD56B.toInt()))
                assertTrue("Ordinary candidate dashes must be visible", colorPixels(2, 0xFFFFD56B.toInt()) > 20)
                assertTrue("Special candidate dashes must be visible", colorPixels(3, 0xFF53D9EC.toInt()) > 20)
                assertTrue("Selected outline must remain visible", colorPixels(4, 0xFFFFD56B.toInt()) > 20)
                assertEquals("Selection replaces special candidate dashes", 0, colorPixels(4, 0xFF53D9EC.toInt()))
            } finally {
                image.recycle()
            }
        }
    }
}
