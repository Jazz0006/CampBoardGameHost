package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode
import com.codex.campboardgamehost.debug.DebugFlightRecorder

@Composable
internal fun SettingsScreen(
    languageMode: LanguageMode,
    storytellerExperienceMode: StorytellerExperienceMode,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onStorytellerExperienceModeChange: (StorytellerExperienceMode) -> Unit,
    onNewCommonPlayerNameChange: (String) -> Unit,
    onAddCommonPlayer: () -> Unit,
    onRemoveCommonPlayer: (String) -> Unit,
    onBack: () -> Unit,
) {
    SettingsContent(
        languageMode = languageMode,
        storytellerExperienceMode = storytellerExperienceMode,
        commonPlayers = commonPlayers,
        newCommonPlayerName = newCommonPlayerName,
        onLanguageModeChange = onLanguageModeChange,
        onStorytellerExperienceModeChange = onStorytellerExperienceModeChange,
        onNewCommonPlayerNameChange = onNewCommonPlayerNameChange,
        onAddCommonPlayer = onAddCommonPlayer,
        onRemoveCommonPlayer = onRemoveCommonPlayer,
        onBack = onBack,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsContent(
    languageMode: LanguageMode,
    storytellerExperienceMode: StorytellerExperienceMode,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onStorytellerExperienceModeChange: (StorytellerExperienceMode) -> Unit,
    onNewCommonPlayerNameChange: (String) -> Unit,
    onAddCommonPlayer: () -> Unit,
    onRemoveCommonPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isEnglish = context.resources.configuration.locales[0].language == "en"
    val chineseSelected = languageMode == LanguageMode.Chinese ||
        (languageMode == LanguageMode.System && !isEnglish)
    val englishSelected = languageMode == LanguageMode.English ||
        (languageMode == LanguageMode.System && isEnglish)

    LazyColumn(
        modifier = modifier
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onLanguageModeChange(LanguageMode.Chinese) }) {
                        Text(
                            "中",
                            color = if (chineseSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (chineseSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    TextButton(onClick = { onLanguageModeChange(LanguageMode.English) }) {
                        Text(
                            "EN",
                            color = if (englishSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (englishSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    onBack?.let { back ->
                        TextButton(onClick = back) {
                            Text(stringResource(R.string.back))
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (isEnglish) "Experienced mode" else "熟练模式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (isEnglish) {
                                "Allow the Storyteller to manually adjust system-recommended information."
                            } else {
                                "允许说书人手动调整系统推荐的线索"
                            },
                            color = Color(0xFF5C6A63),
                        )
                    }
                    Switch(
                        checked = storytellerExperienceMode == StorytellerExperienceMode.EXPERIENCED,
                        onCheckedChange = { enabled ->
                            onStorytellerExperienceModeChange(
                                if (enabled) StorytellerExperienceMode.EXPERIENCED else StorytellerExperienceMode.BEGINNER,
                            )
                        },
                    )
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
                    Text(
                        stringResource(R.string.debug_diagnostics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.debug_diagnostics_description),
                        color = Color(0xFF5C6A63),
                    )
                    OutlinedButton(
                        onClick = { DebugFlightRecorder.shareDebugBundle(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.export_debug_bundle))
                    }
                }
            }
        }
    }
}
