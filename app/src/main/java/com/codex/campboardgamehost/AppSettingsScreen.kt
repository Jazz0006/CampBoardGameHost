package com.codex.campboardgamehost

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
