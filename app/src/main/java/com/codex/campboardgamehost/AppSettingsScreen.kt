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
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsContent(
    preferences: AppPreferencesState,
    aiSettingsSection: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isEnglish = context.resources.configuration.locales[0].language == "en"
    val chineseSelected = preferences.languageMode == LanguageMode.Chinese ||
        (preferences.languageMode == LanguageMode.System && !isEnglish)
    val englishSelected = preferences.languageMode == LanguageMode.English ||
        (preferences.languageMode == LanguageMode.System && isEnglish)

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
                    TextButton(onClick = { preferences.setLanguage(LanguageMode.Chinese) }) {
                        Text(
                            "中",
                            color = if (chineseSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (chineseSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    TextButton(onClick = { preferences.setLanguage(LanguageMode.English) }) {
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.common_players_management), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isEnglish) "Experience belongs to each player. Unset means Beginner. Updates apply to future games, not the current game."
                        else "玩家水平按人长期保存；未设置均为新手。修改对下一局生效，不改变正在进行的游戏。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = preferences.newCommonPlayerName,
                            onValueChange = { preferences.newCommonPlayerName = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.player_name_input_label)) },
                            singleLine = true,
                        )
                        Button(
                            onClick = preferences::addCommonPlayer,
                            enabled = preferences.newCommonPlayerName.trim().isNotEmpty() && preferences.newCommonPlayerName.trim() !in preferences.commonPlayers,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }

                    if (preferences.commonPlayers.isEmpty()) {
                        EmptyStateCard(text = stringResource(R.string.no_common_players_settings))
                    } else {
                        preferences.commonPlayers.forEach { name ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                PlayerExperiencePicker(
                                    playerName = name,
                                    value = preferences.playerExperienceByName[name] ?: PlayerExperienceLevelV1.BEGINNER,
                                    onChange = { preferences.setExperience(name, it) },
                                )
                                TextButton(onClick = { preferences.removeCommonPlayer(name) }) {
                                    Text(stringResource(R.string.remove))
                                }
                            }
                        }
                    }
                }
            }
        }

        item { aiSettingsSection() }

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
