package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerNewGameConfigurationTest {
    @Test
    fun `new Trouble Brewing game and same-player restart reuse saved default not previous takeover`() {
        val savedDefault = StorytellerOperationMode.AI_AUTOMATIC
        var runningMode = storytellerModeForNewGame(savedDefault, ClocktowerScript.TroubleBrewing)
        assertEquals(StorytellerOperationMode.AI_AUTOMATIC, runningMode)

        // An AI failure may force the current game to manual. The saved default is untouched.
        runningMode = StorytellerOperationMode.MANUAL
        assertEquals(StorytellerOperationMode.MANUAL, runningMode)
        assertEquals(
            StorytellerOperationMode.AI_AUTOMATIC,
            storytellerModeForNewGame(savedDefault, ClocktowerScript.TroubleBrewing),
        )
    }

    @Test
    fun `unsupported script runs manually without changing saved AI preference`() {
        val savedDefault = StorytellerOperationMode.AI_ASSISTED
        assertEquals(
            StorytellerOperationMode.MANUAL,
            storytellerModeForNewGame(savedDefault, ClocktowerScript.NoGreaterJoy),
        )
        assertEquals(
            StorytellerOperationMode.AI_ASSISTED,
            storytellerModeForNewGame(savedDefault, ClocktowerScript.TroubleBrewing),
        )
    }

    @Test
    fun `manual new game requires no API credentials or async restores`() {
        assertTrue(ready(StorytellerOperationMode.MANUAL))
    }

    @Test
    fun `both AI modes require joint opening direct transport and never select retired Gateway flow`() {
        for (mode in listOf(StorytellerOperationMode.AI_ASSISTED, StorytellerOperationMode.AI_AUTOMATIC)) {
            assertFalse(ready(mode))
            assertFalse(ready(mode, directEnabled = true, directLoaded = false, directKey = "key"))
            assertFalse(ready(mode, directEnabled = true, directLoaded = true, directKey = ""))
            assertFalse(ready(mode, directEnabled = true, directLoaded = true, directKey = "key", directModel = " "))
            assertTrue(ready(mode, directEnabled = true, directLoaded = true, directKey = "key"))
            assertFalse(ready(mode, gatewayLoaded = false, gatewayEndpoint = "https://host.test", gatewayToken = "token"))
            assertFalse(ready(mode, gatewayLoaded = true, gatewayEndpoint = "http://host.test", gatewayToken = "token"))
            assertFalse(ready(mode, gatewayLoaded = true, gatewayEndpoint = "https://host.test"))
            // Even a healthy legacy gateway is NOT a joint-opening provider.
            assertFalse(ready(mode, gatewayLoaded = true, gatewayEndpoint = "https://host.test", gatewayToken = "token"))
            assertTrue(ready(mode, directEnabled = true, directLoaded = true,
                directKey = "key", gatewayLoaded = true,
                gatewayEndpoint = "https://host.test", gatewayToken = "token"))
        }
    }

    private fun ready(
        mode: StorytellerOperationMode,
        directEnabled: Boolean = false,
        directLoaded: Boolean = false,
        directKey: String = "",
        directModel: String = "gpt-6-luna",
        gatewayLoaded: Boolean = false,
        gatewayEndpoint: String = "",
        gatewayToken: String = "",
    ) = storytellerNewGameConfigurationReady(
        mode = mode,
        directEnabled = directEnabled,
        directLoaded = directLoaded,
        directModel = directModel,
        directKey = directKey,
        gatewayLoaded = gatewayLoaded,
        gatewayEndpoint = gatewayEndpoint,
        gatewayToken = gatewayToken,
    )
}
