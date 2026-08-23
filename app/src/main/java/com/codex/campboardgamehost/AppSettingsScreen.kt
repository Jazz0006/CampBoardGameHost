package com.codex.campboardgamehost

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsScreen(
    languageMode: LanguageMode,
    storytellerAutomationMode: StorytellerAutomationMode,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onStorytellerAutomationModeChange: (StorytellerAutomationMode) -> Unit,
    onNewCommonPlayerNameChange: (String) -> Unit,
    onAddCommonPlayer: () -> Unit,
    onRemoveCommonPlayer: (String) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_subtitle), color = Color(0xFF5C6A63))
                }
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.back))
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (language == "en") "Storyteller decisions" else "说书人判定方式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (language == "en") {
                                "Choose manual control or an automatic style. Automatic rulings also consider the global game balance."
                            } else {
                                "选择手动控制或全自动风格；自动裁定还会结合全局局势。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    val automationModes = listOf(
                        StorytellerAutomationMode.MANUAL to (
                            if (language == "en") "Manual" to "Show legal recommendations and let the Storyteller decide."
                            else "手动" to "显示合法建议，由说书人自行决定。"
                        ),
                        StorytellerAutomationMode.AUTO_BALANCED to (
                            if (language == "en") "Automatic · Balanced" to "Moderate information, risk, and assistance to the trailing team."
                            else "全自动－均衡" to "适度控制信息、风险，并帮助当前落后的一方。"
                        ),
                        StorytellerAutomationMode.AUTO_AGGRESSIVE to (
                            if (language == "en") "Automatic · Aggressive" to "Allows more deception and high-impact rulings while preserving balance."
                            else "全自动－激进" to "允许更多误导和高影响裁定，同时保持局势平衡。"
                        ),
                        StorytellerAutomationMode.AUTO_GENTLE to (
                            if (language == "en") "Automatic · Gentle" to "Prefers clear, low-risk, and less disruptive rulings."
                            else "全自动－稳健" to "优先清晰、低风险、较少改变局势的裁定。"
                        ),
                    )
                    automationModes.forEach { (mode, copy) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStorytellerAutomationModeChange(mode) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = storytellerAutomationMode == mode,
                                onClick = { onStorytellerAutomationModeChange(mode) },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(copy.first, fontWeight = FontWeight.SemiBold)
                                Text(
                                    copy.second,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.language_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LanguageMode.entries.forEach { mode ->
                        val selected = mode == languageMode
                        if (selected) {
                            Button(
                                onClick = { onLanguageModeChange(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(mode.labelResId()))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onLanguageModeChange(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(mode.labelResId()))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.common_players_management), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = newCommonPlayerName,
                            onValueChange = onNewCommonPlayerNameChange,
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.player_name_input_label)) },
                            singleLine = true,
                        )
                        Button(
                            onClick = onAddCommonPlayer,
                            enabled = newCommonPlayerName.trim().isNotEmpty() && newCommonPlayerName.trim() !in commonPlayers,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }

                    if (commonPlayers.isEmpty()) {
                        EmptyStateCard(text = stringResource(R.string.no_common_players_settings))
                    } else {
                        commonPlayers.forEach { name ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { onRemoveCommonPlayer(name) }) {
                                    Text(stringResource(R.string.remove))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
