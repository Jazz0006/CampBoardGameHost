package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ClocktowerStorytellerRecommendationScreen(
    title: String,
    subtitle: String,
    description: String,
    buttonLabel: String,
    onHostTools: () -> Unit,
    onPrevious: (() -> Unit)? = null,
    startEnabled: Boolean = true,
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
                HostBottomActionBar(
                    previousLabel = text("上一步", "Previous"),
                    hostToolsLabel = text("主持工具", "Host Tools"),
                    nextLabel = buttonLabel,
                    onPrevious = onPrevious ?: {},
                    onHostTools = onHostTools,
                    onNext = onStartNight,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    previousEnabled = onPrevious != null,
                    nextEnabled = startEnabled,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerDemonBluffManualPicker(
    legalRoles: List<ClocktowerRole>,
    selectedRoleNames: List<String>,
    language: String,
    onSelectionChange: (List<String>) -> Unit,
) {
    val legalRoleNames = legalRoles.mapTo(linkedSetOf()) { it.enName }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (language == "en") "Demon bluffs · Manual required" else "恶魔伪装身份 · 需要人工选择",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (language == "en") {
                    "No accepted automatic policy exists for this decision. Choose exactly 3 legal out-of-play good roles."
                } else {
                    "此裁定尚无已验收的自动策略。请从合法、未在场的善良角色中手动选择恰好 3 个。"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                if (language == "en") "Selected ${selectedRoleNames.size}/3"
                else "已选择 ${selectedRoleNames.size}/3",
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                legalRoles.forEach { role ->
                    val selected = role.enName in selectedRoleNames
                    val onClick = {
                        onSelectionChange(
                            toggleManualDemonBluffSelection(
                                selectedRoleNames = selectedRoleNames,
                                roleName = role.enName,
                                legalRoleNames = legalRoleNames,
                            ),
                        )
                    }
                    if (selected) {
                        Button(onClick = onClick) { Text(role.nameFor(language)) }
                    } else {
                        OutlinedButton(onClick = onClick) { Text(role.nameFor(language)) }
                    }
                }
            }
        }
    }
}
