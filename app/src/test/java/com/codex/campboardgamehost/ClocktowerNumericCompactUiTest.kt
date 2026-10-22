package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Small-screen accessibility regression for the production Chef/Empath choice component. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
class ClocktowerNumericCompactUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun singletonHasOneShowButtonNoDuplicatedNavigation() {
        var shown: Int? = null
        ui.setContent {
            ClocktowerDarkTheme {
                    ClocktowerResolvedNumericResultUi(
                        values = listOf(1), recommendedValue = null,
                        lastPreviewValue = null, automaticOnly = false,
                        language = "en", draftKey = "small:chef",
                        onConfirm = { shown = it }, onDraftChanged = {},
                    )
            }
        }
        ui.onNode(hasSetTextAction()).assertDoesNotExist()
        ui.onNodeWithText("Show to player: 1").assertExists().performClick()
        // Next/Host navigation belongs to the enclosing shared square-table scaffold.
        ui.onNodeWithText("Next").assertDoesNotExist()
        ui.runOnIdle { assertEquals(1, shown) }
    }

    @Test fun compactSquareTableOwnsNavigationAndReadOnlySeatContext() {
        val seats = (1..5).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = true,
            )
        }
        ui.setContent {
            ClocktowerDarkTheme {
                CompositionLocalProvider(LocalClocktowerNightNextEnabled provides false) {
                    ClocktowerNumericInformationSquareTableDialog(
                        seats = seats,
                        actorSeat = 1,
                        wakeInstruction = "Wake Player 1",
                        values = listOf(0),
                        recommendedValue = null,
                        lastPreviewValue = null,
                        automaticOnly = false,
                        language = "en",
                        draftKey = "square:chef",
                        onHostTools = {},
                        onNext = {},
                        onConfirm = {},
                        onDraftChanged = {},
                    )
                }
            }
        }
        ui.onNodeWithText("Show to player: 0").assertExists()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
    }

    @Test fun fifteenSeatDiscretionCanScrollToLastValueAndShow() {
        var shown: Int? = null
        ui.setContent {
            ClocktowerDarkTheme {
                    ClocktowerResolvedNumericResultUi(
                        values = (0..15).toList(), recommendedValue = null,
                        lastPreviewValue = null, automaticOnly = false,
                        language = "en", draftKey = "small:15",
                        onConfirm = { shown = it }, onDraftChanged = {},
                    )
            }
        }
        ui.onNodeWithText("15").performScrollTo().performClick()
        ui.onNodeWithText("Show to player: 15").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(15, shown) }
    }

    @Test fun automaticDiscretionWaitsWithoutValidRecommendation() {
        ui.setContent {
            ClocktowerDarkTheme {
                    ClocktowerResolvedNumericResultUi(
                        values = listOf(0, 1, 2), recommendedValue = null,
                        lastPreviewValue = null, automaticOnly = true,
                        language = "en", draftKey = "small:empath",
                        onConfirm = {}, onDraftChanged = {},
                    )
            }
        }
        ui.onNodeWithText("Waiting for valid current AI advice.").assertExists()
        ui.onNodeWithText("Show to player: 0").assertDoesNotExist()
        ui.onNodeWithText("Show to player: 1").assertDoesNotExist()
        ui.onNodeWithText("Show to player: 2").assertDoesNotExist()
    }
}
