package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerUndertakerTypedResult(
    val executedSeat: Int,
    val roleId: RoleId,
)

internal enum class ClocktowerUndertakerResultSourceKind {
    DisplayOption,
    LegacyUnreliable,
    Direct,
}

internal data class ClocktowerUndertakerResultChoice(
    val key: String,
    val executedSeat: Int,
    val roleId: RoleId,
    val sourceKind: ClocktowerUndertakerResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val recommended: Boolean = false,
)

internal data class ClocktowerUndertakerSeatVisual(
    val state: ClocktowerSquareTableSeatState,
    val badge: String?,
    val isCurrentActor: Boolean,
)

internal fun clocktowerUndertakerTypedResult(
    proposition: InformationProposition?,
    seatCount: Int,
): ClocktowerUndertakerTypedResult? {
    val roleAt = proposition as? InformationProposition.RoleAt ?: return null
    if (roleAt.seat !in 1..seatCount) return null
    return ClocktowerUndertakerTypedResult(
        executedSeat = roleAt.seat,
        roleId = roleAt.role,
    )
}

internal fun clocktowerUndertakerResultChoices(
    step: ClocktowerNightStepUi,
    seatCount: Int,
    automaticStorytellerInfo: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
    resultFirstRegistrationCandidates: List<ClocktowerDisplayOption>,
): List<ClocktowerUndertakerResultChoice> {
    if (step.roleEnName != "Undertaker") return emptyList()

    fun choicesFrom(
        options: List<ClocktowerDisplayOption>,
        sourceKind: ClocktowerUndertakerResultSourceKind,
    ): List<ClocktowerUndertakerResultChoice> {
        if (options.isEmpty()) return emptyList()
        val choices = options.map { option ->
            val typed = clocktowerUndertakerTypedResult(option.proposition, seatCount)
                ?: return emptyList()
            ClocktowerUndertakerResultChoice(
                key = clocktowerInformationCandidateId(option),
                executedSeat = typed.executedSeat,
                roleId = typed.roleId,
                sourceKind = sourceKind,
                displayOption = option,
                recommended = option.isDefaultRecommendation,
            )
        }
        if (choices.map { it.executedSeat }.distinct().size != 1) return emptyList()
        return choices.distinctBy { it.key }
    }

    if (automaticStorytellerInfo) {
        automaticDisplayOption?.let { option ->
            return choicesFrom(
                options = listOf(option),
                sourceKind = ClocktowerUndertakerResultSourceKind.DisplayOption,
            )
        }
    } else {
        if (resultFirstRegistrationCandidates.isNotEmpty()) {
            return choicesFrom(
                options = resultFirstRegistrationCandidates,
                sourceKind = ClocktowerUndertakerResultSourceKind.DisplayOption,
            )
        }
        if (step.displayOptions.isNotEmpty()) {
            return choicesFrom(
                options = step.displayOptions,
                sourceKind = ClocktowerUndertakerResultSourceKind.LegacyUnreliable,
            )
        }
    }

    val direct = clocktowerUndertakerTypedResult(step.displayProposition, seatCount) ?: return emptyList()
    return listOf(
        ClocktowerUndertakerResultChoice(
            key = "direct|${direct.executedSeat}|${direct.roleId.value}",
            executedSeat = direct.executedSeat,
            roleId = direct.roleId,
            sourceKind = ClocktowerUndertakerResultSourceKind.Direct,
            recommended = true,
        ),
    )
}

internal fun clocktowerUndertakerSeatVisual(
    seatNumber: Int,
    actorSeat: Int?,
    executedSeat: Int,
    language: String,
): ClocktowerUndertakerSeatVisual = ClocktowerUndertakerSeatVisual(
    state = if (seatNumber == executedSeat) {
        ClocktowerSquareTableSeatState.HighlightedInformation
    } else {
        ClocktowerSquareTableSeatState.Neutral
    },
    badge = if (seatNumber == executedSeat) {
        if (language == "en") "EX" else "处"
    } else {
        null
    },
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerUndertakerSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    choices: List<ClocktowerUndertakerResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerUndertakerResultChoice) -> Unit,
) {
    if (choices.isEmpty()) return
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.first()
    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice.key) }
    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice

    Dialog(
        onDismissRequest = { if (canGoPrevious) onPrevious() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    val visual = clocktowerUndertakerSeatVisual(
                        seatNumber = seat.seatId.number,
                        actorSeat = actorSeat,
                        executedSeat = selectedChoice.executedSeat,
                        language = language,
                    )
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = visual.state,
                        isCurrentActor = visual.isCurrentActor,
                        badge = visual.badge,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        ClocktowerNightActionWakeInstruction(wakeInstruction)
                        Text(
                            text = if (language == "en") "Undertaker" else "送葬者",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (language == "en") {
                                "EX marks today's executed player · read-only context"
                            } else {
                                "处 标记今天被处决的玩家 · 仅作信息上下文"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))

                        if (choices.size > 1) {
                            Text(
                                text = if (language == "en") "Choose the character to show" else "选择要展示的角色",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            choices.chunked(2).forEach { rowChoices ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    rowChoices.forEach { choice ->
                                        val label = clocktowerRoleLabel(choice.roleId, language)
                                        if (choice.key == selectedChoice.key) {
                                            Button(
                                                onClick = { selectedKey = choice.key },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(label, maxLines = 1)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { selectedKey = choice.key },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(label, maxLines = 1)
                                            }
                                        }
                                    }
                                    repeat(2 - rowChoices.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        Button(
                            onClick = { onConfirm(selectedChoice) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (language == "en") {
                                    "Show information: ${clocktowerRoleLabel(selectedChoice.roleId, language)}"
                                } else {
                                    "展示信息：${clocktowerRoleLabel(selectedChoice.roleId, language)}"
                                },
                                maxLines = 1,
                            )
                        }
                    }

                    ClocktowerSquareTableStepNavigation(
                        language = language,
                        canGoPrevious = canGoPrevious,
                        onPrevious = onPrevious,
                        onNext = onNext,
                    )
                }
            }
        }
    }
}
