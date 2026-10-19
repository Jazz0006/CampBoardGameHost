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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerRavenkeeperTypedResult(
    val targetSeat: Int,
    val roleId: RoleId,
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
    ClocktowerRavenkeeperTypedResult(anchor.targetSeat, anchor.roleId)
}

/** The query target is actual player input; the shown role is chosen independently. */
@Composable
internal fun ClocktowerRavenkeeperSquareTableDialog(
    seats: List<HostSeatPresentation>,
    step: ClocktowerNightStepUi,
    actorSeat: Int?,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    wakeInstruction: String?,
    scriptRoles: List<ClocktowerRole>,
    recommendedOption: ClocktowerDisplayOption?,
    roleLabel: (String) -> String,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDirectRoleRevealChoice) -> Unit,
) {
    val confirmationEnabled = clocktowerSingleTargetConfirmationEnabled(
        ClocktowerSingleTargetSelection(
            selectedSeat = selectedSeat, selectableSeats = selectableSeats, enabled = enabled,
        ),
    )
    val advised = clocktowerDirectRecommendedRoleReveal(
        step, selectedSeat, seats.size, scriptRoles, recommendedOption,
    )
    var editing by remember(selectedSeat, advised) { mutableStateOf(advised == null) }
    var selectedRole by remember(selectedSeat, advised) { mutableStateOf(advised?.roleId?.value) }
    var roleMenuExpanded by remember(selectedSeat) { mutableStateOf(false) }

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
        Column(Modifier.fillMaxSize().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                if (selectedSeat == null || !confirmationEnabled) {
                    Text(
                        if (language == "en") "Choose a player to learn their character"
                        else "请选择要查看身份的玩家",
                        textAlign = TextAlign.Center,
                    )
                } else if (!editing && advised != null) {
                    ClocktowerSingleRecommendationButtons(
                        recommendations = listOf(advised),
                        language = language,
                        labelOf = { choice ->
                            if (language == "en") "Show: ${choice.displayLabel}" else "展示：${choice.displayLabel}"
                        },
                        onConfirm = onConfirm,
                        onManualSelection = { editing = true },
                    )
                } else {
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { roleMenuExpanded = true }, modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(selectedRole?.let(roleLabel) ?:
                                if (language == "en") "Choose character" else "选择角色", maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false },
                        ) {
                            scriptRoles.distinctBy { it.enName }.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(roleLabel(role.enName)) },
                                    onClick = { selectedRole = role.enName; roleMenuExpanded = false },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    val choice = selectedRole?.let {
                        clocktowerDirectRoleRevealChoice(
                            step, selectedSeat, seats.size, it, scriptRoles, roleLabel,
                        )
                    }
                    Button(
                        onClick = { choice?.let(onConfirm) }, enabled = choice != null,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (language == "en") "Show to player" else "展示给玩家") }
                    if (advised != null) {
                        TextButton(onClick = {
                            selectedRole = advised.roleId.value
                            editing = false
                        }) { Text(if (language == "en") "Use recommendation" else "恢复推荐") }
                    }
                }
            }
        }
    }
}

