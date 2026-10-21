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
                    "本局自动说书人无法安全继续，请人工接管当前步骤。",
                    "Automatic storytelling cannot safely continue for this game. Take over the current step manually.",
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
                label("人工接管后将从已确认的游戏状态继续。",
                    "Manual mode continues from the confirmed game state."),
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
