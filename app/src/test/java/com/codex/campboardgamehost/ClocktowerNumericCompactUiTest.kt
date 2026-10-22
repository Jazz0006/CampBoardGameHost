package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    @Test fun singletonHasOneShowButtonNoTextEntryAndNextIsGated() {
        var shown: Int? = null
        ui.setContent {
            ClocktowerDarkTheme {
                CompositionLocalProvider(LocalClocktowerNightNextEnabled provides false) {
                    ClocktowerResolvedNumericResultUi(
                        values = listOf(1), recommendedValue = null,
                        lastPreviewValue = null, automaticOnly = false,
                        language = "en", draftKey = "small:chef",
                        onHostTools = {}, onNext = {},
                        onConfirm = { shown = it }, onDraftChanged = {},
                    )
                }
            }
        }
        ui.onNode(hasSetTextAction()).assertDoesNotExist()
        ui.onNodeWithText("Show to player: 1").assertExists().performClick()
        ui.onNodeWithText("Next").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(1, shown) }
    }

    @Test fun automaticDiscretionWaitsWithoutValidRecommendation() {
        ui.setContent {
            ClocktowerDarkTheme {
                CompositionLocalProvider(LocalClocktowerNightNextEnabled provides false) {
                    ClocktowerResolvedNumericResultUi(
                        values = listOf(0, 1, 2), recommendedValue = null,
                        lastPreviewValue = null, automaticOnly = true,
                        language = "en", draftKey = "small:empath",
                        onHostTools = {}, onNext = {},
                        onConfirm = {}, onDraftChanged = {},
                    )
                }
            }
        }
        ui.onNodeWithText("Waiting for valid current AI advice.").assertExists()
        ui.onNodeWithText("Show to player: 0").assertDoesNotExist()
        ui.onNodeWithText("Show to player: 1").assertDoesNotExist()
        ui.onNodeWithText("Show to player: 2").assertDoesNotExist()
    }
}
