package com.codex.campboardgamehost

import android.content.Context
import androidx.compose.runtime.*
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** App connection preferences only; no game facts, per-game mode or AI request lifecycle. */
internal class StorytellerAiSettings(preferredMode: StorytellerOperationMode) {
    var preferredMode by mutableStateOf(preferredMode)
    var directEnabled by mutableStateOf(false)
    var directModel by mutableStateOf(PersonalDirectOpenAiV1.DEFAULT_MODEL)
    var directKey by mutableStateOf("")
    var directLoaded by mutableStateOf(false)
    var directSaveFailed by mutableStateOf(false)
    var gatewayEndpoint by mutableStateOf("")
    var gatewayToken by mutableStateOf("")
    var gatewayLoaded by mutableStateOf(false)
    var gatewaySaveFailed by mutableStateOf(false)

    val endpoint get() = if (directEnabled) PersonalDirectOpenAiV1.ENDPOINT else gatewayEndpoint
    val token get() = if (directEnabled) directKey else gatewayToken
    val model get() = if (directEnabled) directModel.trim() else null
}

/** Lives at the app root so leaving Settings cannot cancel loading or debounced secure saves. */
@Composable
internal fun rememberStorytellerAiSettings(context: Context): StorytellerAiSettings {
    val preferences = remember(context) { AppPreferencesStore(context) }
    val direct = remember(context) { PersonalOpenAiSettingsStore(context) }
    val gateway = remember(context) { StorytellerGatewayConnectionStore(context) }
    val settings = remember(context) { StorytellerAiSettings(preferences.loadStorytellerOperationMode()) }

    LaunchedEffect(direct) {
        val restored = withContext(Dispatchers.IO) {
            Triple(direct.loadEnabled(), direct.loadModel(), direct.loadKey())
        }
        settings.directEnabled = restored.first
        settings.directModel = restored.second
        settings.directKey = restored.third
        settings.directLoaded = true
    }
    LaunchedEffect(settings.directLoaded, settings.directEnabled, settings.directModel, settings.directKey) {
        if (!settings.directLoaded) return@LaunchedEffect
        delay(700)
        val enabled = settings.directEnabled
        val model = settings.directModel
        val key = settings.directKey
        val saved = withContext(Dispatchers.IO) {
            runCatching {
                direct.saveEnabled(enabled)
                direct.saveModel(model)
                direct.saveKey(key)
            }.getOrDefault(false)
        }
        settings.directSaveFailed = !saved
    }
    LaunchedEffect(gateway) {
        val restored = withContext(Dispatchers.IO) { gateway.loadEndpoint() to gateway.loadToken() }
        settings.gatewayEndpoint = restored.first
        settings.gatewayToken = restored.second
        settings.gatewayLoaded = true
    }
    LaunchedEffect(settings.gatewayLoaded, settings.gatewayEndpoint, settings.gatewayToken) {
        if (!settings.gatewayLoaded) return@LaunchedEffect
        delay(700)
        val endpoint = settings.gatewayEndpoint
        val token = settings.gatewayToken
        val saved = withContext(Dispatchers.IO) {
            runCatching {
                gateway.saveEndpoint(endpoint)
                gateway.saveToken(token)
            }.getOrDefault(false)
        }
        settings.gatewaySaveFailed = !saved
    }
    return settings
}

