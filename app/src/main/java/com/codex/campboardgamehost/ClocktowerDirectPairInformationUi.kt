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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * Manual pair information contains exactly the seats and character the Storyteller chose.
 * It never proves whether the result is true, nor infers any Spy/Recluse registration.
 */
internal fun clocktowerDirectPairResult(
    step: ClocktowerNightStepUi,
    actorSeat: Int,
    firstSeat: Int,
    secondSeat: Int,
    roleId: String,
    allowedRoleIds: Set<String>,
    playerCount: Int,
): ClocktowerNightStepUi? {
    if (step.roleEnName !in setOf("Washerwoman", "Librarian", "Investigator") ||
        actorSeat !in 1..playerCount ||
        firstSeat !in 1..playerCount || secondSeat !in 1..playerCount ||
        firstSeat == secondSeat || roleId !in allowedRoleIds
    ) return null
    val seats = listOf(firstSeat, secondSeat).sorted()
    val option = ClocktowerDisplayOption(
        label = roleId,
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = step.displayTitle,
        displayPrimary = roleId,
        displaySecondary = seats.joinToString(" / "),
        displayFooter = null,
        proposition = InformationProposition.AnyOf(seats.map { seat ->
            InformationProposition.RoleAt(seat, RoleId(roleId))
        }),
        presentationSubjectSeats = seats,
        presentationRoleId = roleId,
    )
    return resolveClocktowerPlayerDisplay(step, option).copy(
        selectedInformationTruthful = null,
        manualUnrestrictedPairResult = true,
    )
}

/** "None of this information family's characters is in play" is also a manual statement. */
internal fun clocktowerDirectPairNoneResult(
    step: ClocktowerNightStepUi,
    actorSeat: Int,
    eligibleRoleIds: List<String>,
): ClocktowerNightStepUi? {
    if (actorSeat <= 0 || eligibleRoleIds.isEmpty() ||
        step.roleEnName !in setOf("Washerwoman", "Librarian", "Investigator")
    ) return null
    val option = ClocktowerDisplayOption(
        label = "None",
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = step.displayTitle,
        displayPrimary = "None",
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.AllOf(eligibleRoleIds.distinct().map { role ->
            InformationProposition.RoleInPlay(RoleId(role), false)
        }),
    )
    return resolveClocktowerPlayerDisplay(step, option).copy(
        selectedInformationTruthful = null,
        manualUnrestrictedPairResult = true,
    )
}

@Composable
internal fun ClocktowerDirectPairInformationDialog(
    interactionKey: String,
    seats: List<HostSeatPresentation>,
    actorSeat: Int,
    roleIds: List<String>,
    roleLabel: (String) -> String,
    abilityLabel: String,
    language: String,
    wakeInstruction: String?,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit,
    onConfirmNone: () -> Unit,
) {
    var selectedSeats by remember(interactionKey) { mutableStateOf<List<Int>>(emptyList()) }
    var selectedRole by remember(interactionKey) { mutableStateOf<String?>(null) }
    var menuExpanded by remember(interactionKey) { mutableStateOf(false) }

    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = ClocktowerSquareTableInteractionMode.Selectable,
        onSeatSelected = { seat ->
            selectedSeats = if (seat in selectedSeats) {
                selectedSeats - seat
            } else if (selectedSeats.size < 2) {
                selectedSeats + seat
            } else {
                listOf(seat)
            }
        },
        seatUiModel = { seat ->
            clocktowerPairManualSquareTableSeat(
                seat = seat,
                language = language,
                state = if (seat.seatId.number in selectedSeats) {
                    ClocktowerSquareTableSeatState.SelectedHighlighted
                } else {
                    ClocktowerSquareTableSeatState.Selectable
                },
            ).copy(isCurrentActor = seat.seatId.number == actorSeat)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            Text(abilityLabel, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                if (language == "en") "Select any two seats; then choose any character."
                else "任选两个座位，再任选要展示的角色。",
                textAlign = TextAlign.Center,
            )
            Text(
                selectedSeats.joinToString(" / ") { seat ->
                    if (language == "en") "P$seat" else "${seat}号"
                }.ifBlank { if (language == "en") "No seats selected" else "尚未选择座位" },
            )
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { menuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedRole?.let(roleLabel)
                        ?: if (language == "en") "Choose shown character" else "选择展示角色")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    roleIds.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(roleLabel(role)) },
                            onClick = {
                                selectedRole = role
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
            Button(
                onClick = {
                    if (selectedSeats.size == 2) {
                        selectedRole?.let { role ->
                            onConfirm(selectedSeats[0], selectedSeats[1], role)
                        }
                    }
                },
                enabled = selectedSeats.size == 2 && selectedRole != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Confirm and show" else "确认并展示")
            }
            OutlinedButton(onClick = onConfirmNone, modifier = Modifier.fillMaxWidth()) {
                Text(if (language == "en") "No character of this type in play" else "本局没有对应类型角色")
            }
        }
    }
}
