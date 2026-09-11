package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal data class ClocktowerEvilInfoSquareTablePresentation(
    val actorSeat: Int?,
    val wakeInstruction: String?,
    val title: String,
    val primary: String?,
    val secondary: String?,
    val footer: String?,
    val showPlayerDisplayAction: Boolean,
)

/**
 * First-night evil-team information owns a read-only square-table host surface even though it has
 * no role-specific [ClocktowerNightAction]. The player-facing reveal remains owned by the existing
 * display handoff; this model only routes/presents the Storyteller step.
 */
internal fun clocktowerEvilInfoSquareTablePresentation(
    step: ClocktowerNightStepUi,
    actorSeat: Int?,
    wakeInstruction: String?,
): ClocktowerEvilInfoSquareTablePresentation? {
    if (!step.isRealAction || step.displayKind != ClocktowerDisplayKind.EvilInfo) return null
    return ClocktowerEvilInfoSquareTablePresentation(
        actorSeat = actorSeat,
        wakeInstruction = wakeInstruction,
        title = step.displayTitle,
        primary = step.displayPrimary,
        secondary = step.displaySecondary,
        footer = step.displayFooter,
        showPlayerDisplayAction = step.tellPlayer?.isNotBlank() == true,
    )
}

internal fun clocktowerEvilInfoSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerEvilInfoSquareTableDialog(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerEvilInfoSquareTablePresentation,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShowPlayerDisplay: () -> Unit,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = false,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerEvilInfoSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = presentation.actorSeat,
            )
        },
        onSeatSelected = {},
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
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
                ClocktowerNightActionWakeInstruction(presentation.wakeInstruction)
                Text(
                    text = presentation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                presentation.primary?.takeIf { it.isNotBlank() }?.let { primary ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
                presentation.secondary?.takeIf { it.isNotBlank() }?.let { secondary ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                presentation.footer?.takeIf { it.isNotBlank() }?.let { footer ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = footer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (presentation.showPlayerDisplayAction) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onShowPlayerDisplay,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (language == "en") "Show to player" else "展示给玩家")
                    }
                }
            }
        }
    }
}
