package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerRavenkeeperTypedResult(
    val targetSeat: Int,
    val roleId: RoleId,
)

internal data class ClocktowerRavenkeeperResultChoice(
    val key: String,
    val targetSeat: Int,
    val roleId: RoleId?,
    val displayLabel: String,
    val displayOption: ClocktowerDisplayOption,
    val recommended: Boolean = false,
)

internal fun clocktowerRavenkeeperTypedResult(
    proposition: InformationProposition?,
    selectedSeat: Int?,
    seatCount: Int,
): ClocktowerRavenkeeperTypedResult? = clocktowerRoleRevealAnchor(
    proposition = proposition,
    expectedSeat = selectedSeat,
    seatCount = seatCount,
)?.let { anchor ->
    ClocktowerRavenkeeperTypedResult(
        targetSeat = anchor.targetSeat,
        roleId = anchor.roleId,
    )
}

/**
 * Thin projection of existing Ravenkeeper result domains. The step-level RoleAt owns target-seat
 * identity. Individual options remain opaque display choices when unreliable; typed option
 * propositions, when present, must agree with that target and are never reconstructed from text.
 */
internal fun clocktowerRavenkeeperResultChoices(
    step: ClocktowerNightStepUi,
    selectedSeat: Int?,
    seatCount: Int,
    allowRuleDeterminedAutoUi: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
    recommendedOptionIds: Set<String> = emptySet(),
): List<ClocktowerRavenkeeperResultChoice> {
    if (step.roleEnName != "Ravenkeeper" || step.action != ClocktowerNightAction.Ravenkeeper) return emptyList()
    val targetSeat = (step.informationQuestion as? ClocktowerInformationQuestion.Role)?.subjectSeat
        ?.takeIf { it in 1..seatCount } ?: return emptyList()
    if (selectedSeat != targetSeat) return emptyList()
    val options = if (allowRuleDeterminedAutoUi && automaticDisplayOption != null) listOf(automaticDisplayOption)
        else listOfNotNull(automaticDisplayOption) + step.manualInformationCandidates
    return options.distinctBy(::clocktowerInformationCandidateId).mapNotNull { option ->
        val result = option.proposition as? InformationProposition.RoleAt ?: return@mapNotNull null
        if (result.seat != targetSeat) return@mapNotNull null
        ClocktowerRavenkeeperResultChoice(
            key = clocktowerInformationCandidateId(option), targetSeat = targetSeat,
            roleId = result.role, displayLabel = option.displayPrimary.orEmpty(),
            displayOption = option,
            recommended = clocktowerInformationCandidateId(option) in recommendedOptionIds,
        )
    }
}

/**
 * Ravenkeeper keeps target selection and result choice on one square-table surface. The dead actor
 * is still projected independently through isCurrentActor by the shared single-target seat model.
 */
@Composable
internal fun ClocktowerRavenkeeperSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    wakeInstruction: String?,
    choices: List<ClocktowerRavenkeeperResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerRavenkeeperResultChoice) -> Unit,
) {
    val confirmationEnabled = clocktowerSingleTargetConfirmationEnabled(
        ClocktowerSingleTargetSelection(
            selectedSeat = selectedSeat,
            selectableSeats = selectableSeats,
            enabled = enabled,
        ),
    )
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.firstOrNull()
    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice?.key) }
    var roleMenuExpanded by remember(choices.map { it.key }) { mutableStateOf(false) }
    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice

    val recommendations = choices.filter { it.recommended }.take(1)
    val manualChoices = choices.filterNot { it in recommendations }
    var manualSelection by remember(choices.map { it.key }, recommendations.map { it.key }) {
        mutableStateOf(recommendations.isEmpty() && choices.isNotEmpty())
    }

    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSingleTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeat = selectedSeat,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
        onSeatSelected = onSeatSelected,
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        nextEnabled = confirmationEnabled,
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

                if (selectedSeat != null && choices.isEmpty()) {
                    Text(
                        text = if (language == "en") {
                            "No result is available for the current target. Re-select the target or go back."
                        } else {
                            "当前目标没有可用结果。请重新选择目标或返回上一步。"
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                if (!manualSelection && recommendations.isNotEmpty()) {
                    ClocktowerSingleRecommendationButtons(
                        recommendations = recommendations,
                        language = language,
                        labelOf = { choice ->
                            if (language == "en") "Show: ${choice.displayLabel}" else "展示：${choice.displayLabel}"
                        },
                        onConfirm = onConfirm,
                        onManualSelection = if (manualChoices.isNotEmpty()) {
                            { manualSelection = true }
                        } else {
                            null
                        },
                    )
                } else if (selectedChoice != null) {
                    Text(
                        text = if (language == "en") "Manual selection" else "手动选择",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { roleMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(selectedChoice.displayLabel, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false },
                        ) {
                            choices.forEach { choice ->
                                DropdownMenuItem(
                                    text = { Text(choice.displayLabel) },
                                    onClick = {
                                        selectedKey = choice.key
                                        roleMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = { onConfirm(selectedChoice) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (language == "en") "Show to player" else "展示给玩家", maxLines = 1)
                    }
                    if (recommendations.isNotEmpty()) {
                        TextButton(onClick = { manualSelection = false }) {
                            Text(if (language == "en") "Use recommendations" else "返回推荐")
                        }
                    }
                }
            }
        }
    }
}
