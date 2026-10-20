package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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

internal fun clocktowerHasDirectNumericInput(step: ClocktowerNightStepUi, actorSeat: Int, cards: List<PlayerCard>): Boolean =
    clocktowerDirectNumericResult(step, actorSeat, 0, cards) != null

@Composable
internal fun ClocktowerDirectNumericResultUi(
    title: String,
    initialValue: Int? = null,
    automaticOnly: Boolean = false,
    subjectSeats: List<Int>,
    actorLabel: String,
    wakeInstruction: String?,
    language: String,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var entered by remember(title, actorLabel, initialValue, subjectSeats) { mutableStateOf(initialValue?.toString().orEmpty()) }
    val selected = entered.trim().toIntOrNull()?.takeIf { it >= 0 }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(actorLabel, style = MaterialTheme.typography.titleMedium)
            wakeInstruction?.takeIf(String::isNotBlank)?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            if (initialValue != null) {
                Button(
                    onClick = { onConfirm(initialValue) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (language == "en") "Show recommended: $initialValue" else "展示推荐信息：$initialValue")
                }
            }
            if (automaticOnly && initialValue == null) {
                Text(
                    if (language == "en") "Waiting for a current AI decision. No manual substitution in automatic mode."
                    else "正在等待当前 AI 信息；全自动模式不能用人工结果替代。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (!automaticOnly) {
                OutlinedTextField(
                    value = entered,
                    onValueChange = { value -> if (value.length <= 11) entered = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (language == "en") "Manual information (number)" else "手动选择信息数字") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Button(
                    enabled = selected != null,
                    onClick = { selected?.let(onConfirm) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (language == "en") "Show manually selected result" else "展示手动选择的信息")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onHostTools) {
                    Text(if (language == "en") "Host" else "魔典")
                }
                OutlinedButton(onClick = onNext) {
                    Text(if (language == "en") "Next" else "下一步")
                }
            }
        }
    }
}
