package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1

/**
 * Player-profile editor, NOT a per-night Storyteller policy setting.
 * Changes are applied to the next game's frozen player/seat snapshot.
 */
@Composable
internal fun PlayerExperiencePicker(
    playerName: String,
    value: PlayerExperienceLevelV1,
    onChange: (PlayerExperienceLevelV1) -> Unit,
) {
    val english = LocalContext.current.resources.configuration.locales[0].language == "en"
    fun label(level: PlayerExperienceLevelV1) = when (level) {
        PlayerExperienceLevelV1.BEGINNER -> if (english) "Beginner" else "新手"
        PlayerExperienceLevelV1.NORMAL -> if (english) "Regular" else "普通"
        PlayerExperienceLevelV1.EXPERT -> if (english) "Expert" else "高手"
    }
    var expanded by remember(playerName) { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label(value) + " ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PlayerExperienceLevelV1.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(label(level)) },
                    onClick = {
                        onChange(level)
                        expanded = false
                    },
                )
            }
        }
    }
}
