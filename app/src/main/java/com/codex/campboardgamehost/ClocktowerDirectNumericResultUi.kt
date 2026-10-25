package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * A numeric result is an actual Storyteller choice, not one of the Host's calculated
 * legal answers. Reuse only the semantic metric/source/subjects from the current step;
 * the Host does not decide or certify the chosen value.
 */
internal fun clocktowerDirectNumericResult(
    step: ClocktowerNightStepUi,
    actorSeat: Int,
    value: Int,
    cards: List<PlayerCard>,
    subjectSeats: List<Int>? = null,
): ClocktowerNightStepUi? {
    if (value < 0) return null
    if (cards.indexOfFirst { it.name == step.actor?.name } + 1 != actorSeat || actorSeat <= 0) return null
    val question = step.informationQuestion as? ClocktowerInformationQuestion.Number ?: return null
    val subjects = subjectSeats ?: question.subjectSeats
    if (subjects.distinct().size != subjects.size ||
        subjects.any { it !in 1..cards.size }) return null
    val result = ClocktowerDisplayOption(
        label = value.toString(),
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = step.displayTitle,
        displayPrimary = value.toString(),
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.NumericResult(question.metric, actorSeat, subjects, value),
        presentationSubjectSeats = subjects,
    )
    return resolveClocktowerPlayerDisplay(step, result).copy(
        // The Storyteller confirmed what was shown, not whether Host rules say it is true.
        selectedInformationTruthful = null,
    )
}

/** Restore only a typed number for the current actor and actual query. */
internal fun clocktowerMatchingNumericResult(
    step: ClocktowerNightStepUi,
    actorSeat: Int,
    proposition: InformationProposition?,
    subjectSeats: List<Int>? = null,
): Int? {
    val question = step.informationQuestion as? ClocktowerInformationQuestion.Number ?: return null
    val result = proposition as? InformationProposition.NumericResult ?: return null
    return result.value.takeIf {
        it >= 0 && result.sourceSeat == actorSeat &&
            result.metric == question.metric && result.subjectSeats == (subjectSeats ?: question.subjectSeats)
    }
}

/** Chef/Empath only. Display choices come from the current read-only Session resolution.
 * Uncommitted selection is local Compose state; the existing Host preview/Next is the sole fact writer. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerResolvedNumericResultUi(
    values: List<Int>,
    recommendedValue: Int?,
    lastPreviewValue: Int?,
    automaticOnly: Boolean,
    language: String,
    draftKey: String,
    onConfirm: (Int) -> Unit,
    onDraftChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var manualSelection by remember(draftKey) {
        mutableStateOf<Int?>(null)
    }
    val chosen = when {
        values.size == 1 -> values.single()
        automaticOnly -> recommendedValue?.takeIf { it in values }
        else -> manualSelection?.takeIf { it in values }
            ?: lastPreviewValue?.takeIf { it in values }
            ?: recommendedValue?.takeIf { it in values }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (values.size > 1 && !automaticOnly) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                values.forEach { value ->
                    FilterChip(
                        selected = chosen == value,
                        modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp),
                        onClick = {
                            if (manualSelection != value) {
                                manualSelection = value
                                onDraftChanged()
                            }
                        },
                        label = { Text(value.toString()) },
                    )
                }
            }
            if (recommendedValue in values) Text(
                if (language == "en") "AI suggested: $recommendedValue" else "AI 推荐：$recommendedValue",
            )
        }
        if (automaticOnly && values.size > 1 && chosen == null) {
            Text(if (language == "en") "Waiting for valid current AI advice."
                else "等待当前有效的 AI 推荐，不会自动选择其他结果。")
        }
        Button(
            onClick = { chosen?.let(onConfirm) },
            enabled = chosen != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (chosen != null) {
                if (language == "en") "Show to player: $chosen" else "展示给玩家：$chosen"
            } else if (language == "en") "Show to player" else "展示给玩家")
        }
    }
}
