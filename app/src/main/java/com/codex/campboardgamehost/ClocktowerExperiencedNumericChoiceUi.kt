package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Compact Experienced-only picker: recommended result is always the first one-tap action. */
@Composable
internal fun <T> ClocktowerExperiencedNumericChoiceRows(
    choices: List<T>,
    recommendedChoice: T,
    language: String,
    valueOf: (T) -> Int,
    onConfirm: (T) -> Unit,
) {
    Text(
        text = if (language == "en") "Recommended information" else "推荐信息",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = { onConfirm(recommendedChoice) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            if (language == "en") "Show: ${valueOf(recommendedChoice)}" else "展示：${valueOf(recommendedChoice)}",
            maxLines = 1,
        )
    }

    val alternatives = choices.filterNot { it == recommendedChoice }
    if (alternatives.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (language == "en") "Alternative information" else "备选信息",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        alternatives.chunked(3).forEach { rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rowChoices.forEach { choice ->
                    OutlinedButton(
                        onClick = { onConfirm(choice) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(valueOf(choice).toString(), maxLines = 1)
                    }
                }
                repeat(3 - rowChoices.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
