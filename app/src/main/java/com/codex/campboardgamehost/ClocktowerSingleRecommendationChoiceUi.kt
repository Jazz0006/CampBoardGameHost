package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Shared single-advisory recommendation controls.
 *
 * Only a single recommendation can be shown, never three ranked legacy alternatives.
 * Manual selection remains separate, presenting the full Host-legal domain.
 */
@Composable
internal fun <T> ClocktowerSingleRecommendationButtons(
    recommendations: List<T>,
    language: String,
    labelOf: (T) -> String,
    onConfirm: (T) -> Unit,
    onManualSelection: (() -> Unit)? = null,
) {
    val visibleRecommendations = recommendations.distinct().take(1)
    if (visibleRecommendations.isNotEmpty()) {
        Text(
            text = if (language == "en") "Recommended information" else "推荐信息",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        visibleRecommendations.forEachIndexed { index, recommendation ->
            if (index == 0) {
                Button(
                    onClick = { onConfirm(recommendation) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(labelOf(recommendation), maxLines = 2)
                }
            } else {
                OutlinedButton(
                    onClick = { onConfirm(recommendation) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(labelOf(recommendation), maxLines = 2)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    onManualSelection?.let { openManual ->
        TextButton(onClick = openManual, modifier = Modifier.fillMaxWidth()) {
            Text(if (language == "en") "Manual selection" else "手动选择")
        }
    }
}
