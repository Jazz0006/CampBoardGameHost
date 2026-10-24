package com.codex.campboardgamehost

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.Alignment
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ClocktowerWakeArrowUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun arrowsFollowCurrentActorsAndIncludeADeadTriggeredActor() {
        checkArrows(7, 520f, setOf(1, 3))
    }

    @Test fun fifteenSeatArrowsAvoidTheActualCompactCentralWorkspace() {
        checkArrows(15, 448f, (1..15).toSet())
    }

    private fun checkArrows(count: Int, height: Float, wakeSeats: Set<Int>) {
        val actors = mutableStateOf(emptySet<Int>())
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerSquareTableSeatSurface(
                    seats = (1..count).map { i -> ClocktowerSquareTableSeatUiModel(
                        seatId = "$i", seatNumber = i, label = "P$i",
                        isAlive = i != 3, actualAlignment = Alignment.GOOD,
                        isCurrentActor = i in actors.value, isInteractionEnabled = true,
                    ) },
                    modifier = Modifier.size(360.dp, height.dp),
                    interactionMode = ClocktowerSquareTableInteractionMode.Selectable,
                    onSeatClick = {},
                ) { Box(Modifier.fillMaxSize().testTag("wake-center")) }
            }
        }
        var before: Bitmap? = null
        ui.runOnIdle { before = screenshot() }
        ui.runOnIdle { actors.value = wakeSeats }
        wakeSeats.forEach { i ->
            ui.onNode(hasClickAction() and hasText("P$i"))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Current wake target"))
        }
        val bounds = (1..count).map { i -> ui.onNode(hasClickAction() and hasText("P$i")).fetchSemanticsNode().boundsInRoot }
        val controls = ui.onNodeWithTag("wake-center").fetchSemanticsNode().boundsInRoot
        ui.runOnIdle {
            val after = screenshot()
            try {
                // Names also become bold. Only count changes outside every physical card.
                val changedOutsideCards = (0 until after.width).sumOf { x ->
                    (0 until after.height).count { y ->
                        bounds.none { it.contains(androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())) } &&
                            after.getPixel(x, y) != before!!.getPixel(x, y)
                    }
                }
                assertTrue("Wake arrows must actually render outside the cards", changedOutsideCards > 30)
                val changedOverControls = (controls.left.toInt() until controls.right.toInt()).sumOf { x ->
                    (controls.top.toInt() until controls.bottom.toInt()).count { y ->
                        after.getPixel(x, y) != before!!.getPixel(x, y)
                    }
                }
                assertTrue("Wake arrows must not obscure central controls", changedOverControls == 0)
            } finally { after.recycle() }
        }
        ui.runOnIdle { actors.value = emptySet() }
        ui.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription)).assertCountEquals(0)
        ui.runOnIdle {
            val cleared = screenshot()
            try { assertTrue("No stale wake arrows may remain", cleared.sameAs(before)) }
            finally { cleared.recycle(); before!!.recycle() }
        }
    }

    private fun screenshot(): Bitmap {
        val view = ui.activity.findViewById<View>(android.R.id.content)
        return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
    }
}
