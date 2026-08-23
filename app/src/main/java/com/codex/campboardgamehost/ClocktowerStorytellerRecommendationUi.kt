package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun RecommendationReasonSummary(
    reasonCodes: List<String>,
    warningCodes: List<String>,
    language: String,
) {
    if (reasonCodes.isEmpty() && warningCodes.isEmpty()) return
    val reasons = reasonCodes.distinct().take(2).joinToString(" · ") { recommendationReasonLabel(it, language) }
    if (reasons.isNotBlank()) {
        Text(
            (if (language == "en") "Why: " else "理由：") + reasons,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (warningCodes.isNotEmpty()) {
        Text(
            (if (language == "en") "Review: " else "注意：") +
                warningCodes.distinct().take(2).joinToString(" · ") { recommendationReasonLabel(it, language) },
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun ClocktowerStorytellerRecommendationScreen(
    title: String,
    subtitle: String,
    description: String,
    buttonLabel: String,
    onStartNight: () -> Unit,
    content: @Composable () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text(
                                "不要向玩家展示推荐、真实角色或说书人裁定。",
                                "Do not show recommendations, actual roles, or Storyteller rulings to players.",
                            ),
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item { content() }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onStartNight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(buttonLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
