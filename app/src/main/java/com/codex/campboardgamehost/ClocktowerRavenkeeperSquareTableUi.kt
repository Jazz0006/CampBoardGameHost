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

internal enum class ClocktowerRavenkeeperResultSourceKind {
    DisplayOption,
    LegacyUnreliable,
    Direct,
}

internal data class ClocktowerRavenkeeperResultChoice(
    val key: String,
    val targetSeat: Int,
    val roleId: RoleId,
    val sourceKind: ClocktowerRavenkeeperResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val recommended: Boolean = false,
)

/** Reliable direct results must use the exact player-visible RoleAt proposition. */
internal fun clocktowerRavenkeeperTypedResult(
    proposition: InformationProposition?,
    selectedSeat: Int?,
    seatCount: Int,
): ClocktowerRavenkeeperTypedResult? {
    if (selectedSeat == null || selectedSeat !in 1..seatCount) return null
    val roleAt = proposition as? InformationProposition.RoleAt ?: return null
    if (roleAt.seat != selectedSeat || roleAt.seat !in 1..seatCount) return null
    return ClocktowerRavenkeeperTypedResult(
        targetSeat = roleAt.seat,
        roleId = roleAt.role,
    )
}

/** Display options may instead carry non-epistemic Storyteller presentation metadata. */
internal fun clocktowerRavenkeeperTypedResult(
    option: ClocktowerDisplayOption,
    selectedSeat: Int?,
    seatCount: Int,
): ClocktowerRavenkeeperTypedResult? {
    if (selectedSeat == null || selectedSeat !in 1..seatCount) return null
    val resolved = option.resolvedRoleRevealPresentation(seatCount) ?: return null
    if (resolved.targetSeat != selectedSeat) return null
    return ClocktowerRavenkeeperTypedResult(
        targetSeat = resolved.targetSeat,
        roleId = resolved.roleId,
    )
}

/**
 * Thin projection of existing Ravenkeeper result domains. Target legality remains upstream; this
 * adapter only binds a selected target to typed final-role identity supplied by the materializer,
 * registration layer, or Storyteller-only presentation metadata.
 */
internal fun clocktowerRavenkeeperResultChoices(
    step: ClocktowerNightStepUi,
    selectedSeat: Int?,
    seatCount: Int,
    automaticStorytellerInfo: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
    resultFirstRegistrationCandidates: List<ClocktowerDisplayOption>,
): List<ClocktowerRavenkeeperResultChoice> {
    if (step.action != ClocktowerNightAction.Ravenkeeper || step.roleEnName != "Ravenkeeper") {
        return emptyList()
    }
    if (selectedSeat == null || selectedSeat !in 1..seatCount) return emptyList()

    fun choicesFrom(
        options: List<ClocktowerDisplayOption>,
        sourceKind: ClocktowerRavenkeeperResultSourceKind,
    ): List<ClocktowerRavenkeeperResultChoice> {
        if (options.isEmpty()) return emptyList()
        val choices = options.map { option ->
            val typed = clocktowerRavenkeeperTypedResult(
                option = option,
                selectedSeat = selectedSeat,
                seatCount = seatCount,
            ) ?: return emptyList()
            ClocktowerRavenkeeperResultChoice(
                key = clocktowerInformationCandidateId(option),
                targetSeat = typed.targetSeat,
                roleId = typed.roleId,
                sourceKind = sourceKind,
                displayOption = option,
                recommended = option.isDefaultRecommendation,
            )
        }
        return choices.distinctBy { it.key }
    }

    if (automaticStorytellerInfo) {
        automaticDisplayOption?.let { option ->
            return choicesFrom(
                options = listOf(option),
                sourceKind = ClocktowerRavenkeeperResultSourceKind.DisplayOption,
            )
        }
    } else {
        if (resultFirstRegistrationCandidates.isNotEmpty()) {
            return choicesFrom(
                options = resultFirstRegistrationCandidates,
                sourceKind = ClocktowerRavenkeeperResultSourceKind.DisplayOption,
            )
        }
        if (step.displayOptions.isNotEmpty()) {
            return choicesFrom(
                options = step.displayOptions,
                sourceKind = ClocktowerRavenkeeperResultSourceKind.LegacyUnreliable,
            )
        }
    }

    val direct = clocktowerRavenkeeperTypedResult(
        proposition = step.displayProposition,
        selectedSeat = selectedSeat,
        seatCount = seatCount,
    ) ?: return emptyList()
    return listOf(
        ClocktowerRavenkeeperResultChoice(
            key = "direct|${direct.targetSeat}|${direct.roleId.value}",
            targetSeat = direct.targetSeat,
            roleId = direct.roleId,
            sourceKind = ClocktowerRavenkeeperResultSourceKind.Direct,
            recommended = true,
        ),
    )
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
    onNext: () -> Unit,
    onConfirm: (ClocktowerRavenkeeperResultChoice) -> Unit,
) {
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.firstOrNull()
    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice?.key) }
    var roleMenuExpanded by remember(choices.map { it.key }) { mutableStateOf(false) }
    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice

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
                    text = if (language == "en") "Ravenkeeper" else "守鸦人",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (selectedSeat == null) {
                        if (language == "en") {
                            "Select the player chosen by the Ravenkeeper"
                        } else {
                            "选择守鸦人要查验的玩家"
                        }
                    } else {
                        if (language == "en") "Selected target: P$selectedSeat" else "查验目标：P$selectedSeat"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (selectedSeat != null) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))

                if (selectedSeat != null && choices.isEmpty()) {
                    Text(
                        text = if (language == "en") {
                            "No typed result is available for the current target. Re-select the target or go back."
                        } else {
                            "当前目标没有可用的类型化结果。请重新选择目标或返回上一步。"
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                if (choices.size > 1 && selectedChoice != null) {
                    Text(
                        text = if (language == "en") "Choose the character to show" else "选择要展示的角色",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { roleMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(clocktowerRoleLabel(selectedChoice.roleId, language), maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false },
                        ) {
                            choices.forEach { choice ->
                                DropdownMenuItem(
                                    text = { Text(clocktowerRoleLabel(choice.roleId, language)) },
                                    onClick = {
                                        selectedKey = choice.key
                                        roleMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                selectedChoice?.let { choice ->
                    Button(
                        onClick = { onConfirm(choice) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (language == "en") {
                                "Show information: ${clocktowerRoleLabel(choice.roleId, language)}"
                            } else {
                                "展示信息：${clocktowerRoleLabel(choice.roleId, language)}"
                            },
                            maxLines = 1,
                        )
                    }
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
