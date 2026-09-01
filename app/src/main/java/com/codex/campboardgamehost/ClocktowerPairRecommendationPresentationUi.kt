package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * UX-R4 presentation for combinatorial pair-information clues.
 *
 * The caller supplies an already-ranked recommendation presentation. This component does not
 * generate candidates, rank them, or own Manual legality; the separate Manual surface remains
 * backed by the complete legal domain.
 */
@Composable
internal fun ClocktowerPairRecommendationPresentationSection(
    presentation: ClocktowerRecommendationPresentation<ClocktowerDisplayOption>,
    language: String,
    onSelect: (ClocktowerDisplayOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (language == "en") "Recommended clue" else "推荐线索",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        val primary = presentation.primary
        if (primary == null) {
            Text(
                if (language == "en") {
                    "No recommendation is currently available. Use the complete Manual clue selection below."
                } else {
                    "当前没有可用推荐，请使用下方完整的手动线索选择。"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Text(
                if (language == "en") {
                    "Use the strongest current recommendation, compare an alternative, or choose manually below."
                } else {
                    "可直接采用当前最强推荐，也可比较备选方案，或在下方手动选择。"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = { onSelect(primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(primary.label)
            }
            RecommendationReasonSummary(primary.reasonCodes, primary.warningCodes, language)

            if (presentation.alternatives.isNotEmpty()) {
                Text(
                    if (language == "en") "Other recommendations" else "其他推荐",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                presentation.alternatives.forEach { option ->
                    OutlinedButton(
                        onClick = { onSelect(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                    RecommendationReasonSummary(option.reasonCodes, option.warningCodes, language)
                }
            }
        }
    }
}
