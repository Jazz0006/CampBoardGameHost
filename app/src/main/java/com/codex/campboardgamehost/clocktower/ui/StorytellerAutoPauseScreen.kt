package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Bounded automation: no downstream TB arbitration may silently inherit
 * pre-existing beginner policy when it is not implemented by the AI controller.
 */
@Composable
internal fun StorytellerAutoPauseScreen(
    hasGlobalPlan: Boolean,
    language: String,
    onTakeOverManually: () -> Unit,
) {
    fun label(zh: String, en: String) = if (language == "en") en else zh
    ClocktowerDarkTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                label("自动说书人已暂停", "AUTOMATIC STORYTELLER PAUSED"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                label(
                    "当前版本的自动裁量仅覆盖酒鬼选择。首夜其他角色信息、合法登记以及后续操作尚未全部接入统一策略执行器。",
                    "This build only automates Drunk assignment. Other first-night decisions and downstream choices are not yet connected to the shared strategic executor.",
                ),
            )
            if (!hasGlobalPlan) {
                Text(
                    label("本局没有经过验证的 AI 全局策略，不能继续无人值守。",
                        "No verified global strategy was obtained for this setup."),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                label("请人工接管，原有规则引擎仍然独立运行。不会自动编造后续决定。",
                    "Take over manually; the Host rules engine remains usable. No unsupported action will be invented."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onTakeOverManually,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(label("切换全手动并继续", "Take over in manual mode"))
            }
        }
    }
}
