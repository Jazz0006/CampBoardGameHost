package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    @Test fun squareTableKeepsShowVisibleWithMaximumChoicesAndReturnsToThePreviewedValue() {
        val count = 15
        val maximum = 4
        val preview = mutableStateOf<Int?>(null)
        var shown: Int? = null
        ui.setContent {
            ClocktowerDarkTheme {
                Box(Modifier.size(360.dp, 448.dp)) {
                    ClocktowerNumericInformationSquareTableDialog(
                        seats = (1..count).map { HostSeatPresentation(ClocktowerSeatId(it), "P$it", isAlive = true) },
                        actorSeat = 1, wakeInstruction = "Wake P1", values = (0..maximum).toList(),
                        recommendedValue = maximum, lastPreviewValue = preview.value, automaticOnly = false,
                        language = "en", draftKey = "square:$count:${preview.value}",
                        onHostTools = {}, onNext = {}, onConfirm = { shown = it }, onDraftChanged = {},
                    )
                }
            }
        }
        ui.onNodeWithText("Show to player: $maximum").assertIsDisplayed().assertIsEnabled()
        ui.onNode(hasText((maximum - 1).toString(), substring = false) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performScrollTo().performClick()
        ui.onNodeWithText("Show to player: ${maximum - 1}").assertIsEnabled()
        ui.onNode(hasText(maximum.toString(), substring = false) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performScrollTo().performClick()
        ui.onNodeWithText("Show to player: $maximum").assertIsDisplayed().performClick()
        ui.runOnIdle { assertEquals(maximum, shown); preview.value = maximum }
        ui.onNode(hasText(maximum.toString(), substring = false) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).assertIsSelected()
        ui.onNodeWithText("Show to player: $maximum").assertIsDisplayed().performClick()
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

    @Test fun withdrawnAdviceOrPreviewCannotBecomeAnImplicitManualSelection() {
        val advice = mutableStateOf<Int?>(1)
        val preview = mutableStateOf<Int?>(null)
        var shown: Int? = null
        ui.setContent {
            ClocktowerDarkTheme {
                ClocktowerResolvedNumericResultUi(
                    values = listOf(0, 1, 2), recommendedValue = advice.value,
                    lastPreviewValue = preview.value, automaticOnly = false,
                    language = "en", draftKey = "same-interaction",
                    onConfirm = { shown = it }, onDraftChanged = {},
                )
            }
        }
        ui.onNodeWithText("Show to player: 1").assertIsEnabled()
        // Busy/failed retries withdraw the current suggestion without changing the step key.
        ui.runOnIdle { advice.value = null }
        ui.onNodeWithText("Show to player").assertIsNotEnabled()
        ui.runOnIdle { preview.value = 1 }
        ui.onNodeWithText("Show to player: 1").assertIsEnabled()
        ui.runOnIdle { preview.value = null }
        ui.onNodeWithText("Show to player").assertIsNotEnabled()
        ui.runOnIdle { advice.value = 2 }
        ui.onNodeWithText("Show to player: 2").assertIsEnabled()
        ui.onNode(hasText("0", substring = false) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick()
        ui.runOnIdle { advice.value = null }
        ui.onNodeWithText("Show to player: 0").assertIsEnabled().performClick()
        ui.runOnIdle { assertEquals(0, shown) }
    }
}
