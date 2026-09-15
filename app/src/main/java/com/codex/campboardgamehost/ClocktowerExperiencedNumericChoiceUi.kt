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

/** Compatibility entry point for callers that have not migrated to a ranked recommendation set. */
@Composable
internal fun <T> ClocktowerExperiencedNumericChoiceRows(
    choices: List<T>,
    recommendedChoice: T,
    language: String,
    valueOf: (T) -> Int,
    onConfirm: (T) -> Unit,
) = ClocktowerExperiencedNumericChoiceRows(
    choices = choices,
    recommendedChoices = listOf(recommendedChoice),
    language = language,
    valueOf = valueOf,
    onConfirm = onConfirm,
)

/**
 * Experienced picker: show up to three real ranked recommendations first, then the remaining legal
 * domain as manual choices. Recommendations are never padded from the manual domain.
 */
@Composable
internal fun <T> ClocktowerExperiencedNumericChoiceRows(
    choices: List<T>,
    recommendedChoices: List<T>,
    language: String,
    valueOf: (T) -> Int,
    onConfirm: (T) -> Unit,
) {
    val recommendations = recommendedChoices
        .filter { recommendation -> recommendation in choices }
        .distinct()
        .take(3)
    if (recommendations.isNotEmpty()) {
        Text(
            text = if (language == "en") "Recommended information" else "推荐信息",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        recommendations.forEachIndexed { index, recommendation ->
            if (index == 0) {
                Button(
                    onClick = { onConfirm(recommendation) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (language == "en") {
                            "Show: ${valueOf(recommendation)}"
                        } else {
                            "展示：${valueOf(recommendation)}"
                        },
                        maxLines = 1,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onConfirm(recommendation) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (language == "en") {
                            "Recommendation ${index + 1}: ${valueOf(recommendation)}"
                        } else {
                            "推荐 ${index + 1}：${valueOf(recommendation)}"
                        },
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    val manualChoices = choices.filterNot { it in recommendations }
    if (manualChoices.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (language == "en") "Manual selection" else "手动选择",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        manualChoices.chunked(3).forEach { rowChoices ->
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
