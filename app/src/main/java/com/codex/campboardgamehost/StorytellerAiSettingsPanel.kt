package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * App-level defaults. Game-specific manual takeover never mutates these settings.
 * All credentials remain in their existing Android Keystore-backed stores.
 */
@Composable
internal fun StorytellerAiSettingsPanel(
    preferredMode: StorytellerOperationMode,
    onPreferredModeChange: (StorytellerOperationMode) -> Unit,
    directEnabled: Boolean,
    onDirectEnabledChange: (Boolean) -> Unit,
    directModel: String,
    onDirectModelChange: (String) -> Unit,
    directKey: String,
    onDirectKeyChange: (String) -> Unit,
    directSaveFailed: Boolean,
    gatewayEndpoint: String,
    onGatewayEndpointChange: (String) -> Unit,
    gatewayToken: String,
    onGatewayTokenChange: (String) -> Unit,
    gatewaySaveFailed: Boolean,
) {
    val english = LocalContext.current.resources.configuration.locales[0].language == "en"
    fun text(zh: String, en: String) = if (english) en else zh
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text("血染钟楼 · AI 说书人", "Clocktower · AI Storyteller"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text("此处设置的模式和连接方式将用于每次新开局，包括原班人马快速再来一局。"
                    + "本局发生人工接管不会改变这里的默认值。其他不支持 AI 的剧本仍以手动模式运行。",
                    "The saved mode and connection apply to each new game, including quick restart with the same players. "
                    + "Manual takeover for one game does not change the default. Unsupported scripts run manually."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StorytellerOperationMode.entries.forEach { mode ->
                Card(
                    onClick = { onPreferredModeChange(mode) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (mode == preferredMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ),
                ) {
                    Text(
                        when (mode) {
                            StorytellerOperationMode.MANUAL ->
                                text("全手动 · 不调用 AI", "Manual · no AI")
                            StorytellerOperationMode.AI_ASSISTED ->
                                text("AI 推荐 · 人工确认", "AI assisted · human confirms")
                            StorytellerOperationMode.AI_AUTOMATIC ->
                                text("AI 自动 · 当前仅部分流程，未覆盖的决策需要人工接管",
                                    "AI automatic · limited coverage, manual takeover on unsupported decisions")
                        },
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        fontWeight = if (mode == preferredMode) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
            Text(text("AI 连接方式", "AI connection"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onDirectEnabledChange(false) }) {
                    Text(if (directEnabled) text("网关", "Gateway") else text("✓ 网关", "✓ Gateway"))
                }
                OutlinedButton(onClick = { onDirectEnabledChange(true) }) {
                    Text(if (directEnabled) text("✓ OpenAI 直连", "✓ OpenAI direct")
                         else text("OpenAI 直连", "OpenAI direct"))
                }
            }
            if (directEnabled) {
                Text(
                    text("仅供个人测试。API Key 用本机 Keystore 加密保存，不打包进 APK；设备受入侵仍有泄露风险。",
                    "Personal testing only. The API key is encrypted with Android Keystore and not bundled in the APK; a compromised device can expose it."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = directModel,
                    onValueChange = onDirectModelChange,
                    label = { Text(text("OpenAI API 模型 ID", "OpenAI API model ID")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = directKey,
                    onValueChange = onDirectKeyChange,
                    label = { Text(text("OpenAI API Key（仅本机）", "OpenAI API Key (device only)")) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = { onDirectKeyChange("") }) {
                    Text(text("删除本机 API Key", "Erase local API Key"))
                }
                if (directSaveFailed) {
                    Text(text("API Key 安全保存失败，重启后可能需要重新输入。",
                        "Could not securely persist API key."), color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(text("网关地址存储于本机，访问令牌由 Android Keystore 加密保存。",
                    "Gateway URL is stored locally; its token is encrypted with Android Keystore."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = gatewayEndpoint,
                    onValueChange = onGatewayEndpointChange,
                    label = { Text(text("Gateway HTTPS 地址", "Gateway HTTPS URL")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = gatewayToken,
                    onValueChange = onGatewayTokenChange,
                    label = { Text(text("Gateway 访问令牌", "Gateway access token")) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (gatewaySaveFailed) {
                    Text(text("连接配置无法安全保存，重启后可能需要重新输入。",
                        "Could not securely persist connection settings."),
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
