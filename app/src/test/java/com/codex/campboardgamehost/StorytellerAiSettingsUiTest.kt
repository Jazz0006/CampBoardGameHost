package com.codex.campboardgamehost

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real panel events; delayed-load fixtures represent the two independent credential reads. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class StorytellerAiSettingsUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    private fun show(settings: StorytellerAiSettings) {
        ui.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    StorytellerAiSettingsPanel(settings)
                }
            }
        }
    }

    @Test fun directConfigurationCannotBeEditedBeforeItsStoredValuesArrive() {
        val settings = StorytellerAiSettings(StorytellerOperationMode.MANUAL).apply {
            directEnabled = true
            gatewayLoaded = true
        }
        show(settings)
        ui.onNodeWithText("OpenAI API Key (device only)").assertDoesNotExist()
        ui.runOnIdle {
            settings.directModel = "restored-model"
            settings.directKey = "restored-test-key"
            settings.directLoaded = true
        }
        ui.onNodeWithText("OpenAI API model ID").performScrollTo().performTextReplacement("edited-model")
        ui.onNodeWithText("Erase local API Key").performScrollTo().performClick()
        ui.runOnIdle {
            assertEquals("edited-model", settings.model)
            assertEquals("", settings.token)
        }
    }

    @Test fun gatewayConfigurationCannotBeEditedUntilBothConnectionReadsFinish() {
        val settings = StorytellerAiSettings(StorytellerOperationMode.MANUAL).apply { directLoaded = true }
        show(settings)
        ui.onNodeWithText("Gateway HTTPS URL").assertDoesNotExist()
        ui.runOnIdle {
            settings.gatewayEndpoint = "https://restored.test"
            settings.gatewayToken = "restored-test-token"
            settings.gatewayLoaded = true
        }
        ui.onNodeWithText("Gateway HTTPS URL").performScrollTo().performTextReplacement("https://edited.test")
        ui.onNodeWithText("Gateway access token").performScrollTo().performTextReplacement("edited-test-token")
        ui.runOnIdle {
            assertEquals("https://edited.test", settings.endpoint)
            assertEquals("edited-test-token", settings.token)
            assertEquals(null, settings.model)
        }
    }

    @Test fun preferredModeIsPersistedByTheRealSettingsPanel() {
        val settings = StorytellerAiSettings(StorytellerOperationMode.MANUAL)
        show(settings)
        ui.onNodeWithText("AI assisted · human confirms").performScrollTo().performClick()
        ui.runOnIdle {
            assertEquals(StorytellerOperationMode.AI_ASSISTED, settings.preferredMode)
            assertEquals(StorytellerOperationMode.AI_ASSISTED,
                AppPreferencesStore(ui.activity).loadStorytellerOperationMode())
        }
    }
}
