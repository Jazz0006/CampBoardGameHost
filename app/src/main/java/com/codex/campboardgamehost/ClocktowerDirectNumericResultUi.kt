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
): ClocktowerNightStepUi? {
    if (value < 0) return null
    if (cards.indexOfFirst { it.name == step.actor?.name } + 1 != actorSeat || actorSeat <= 0) return null
    val question = step.informationQuestion as? ClocktowerInformationQuestion.Number ?: return null
    if (question.subjectSeats.distinct().size != question.subjectSeats.size ||
        question.subjectSeats.any { it !in 1..cards.size }) return null
    val result = ClocktowerDisplayOption(
        label = value.toString(),
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = step.displayTitle,
        displayPrimary = value.toString(),
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.NumericResult(question.metric, actorSeat, question.subjectSeats, value),
        presentationSubjectSeats = question.subjectSeats,
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
    subjectSeats: List<Int>,
    actorLabel: String,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
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
            OutlinedTextField(
                value = entered,
                onValueChange = { value -> if (value.length <= 11) entered = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (language == "en") "Information shown (number)" else "向玩家展示的数字") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            Text(
                if (language == "en") "Enter the actual chosen result. The Host does not calculate or verify its game-rule truth."
                else "直接填写说书人决定展示的数字；Host 不计算或校验规则上的真假。",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                enabled = selected != null,
                onClick = { selected?.let(onConfirm) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Confirm and show to player" else "确认并向玩家展示")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPrevious, enabled = canGoPrevious) {
                    Text(if (language == "en") "Previous" else "上一步")
                }
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
