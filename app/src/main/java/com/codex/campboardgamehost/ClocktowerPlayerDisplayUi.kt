package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal fun clocktowerPlayerDisplayHighlightedSeats(
    step: ClocktowerNightStepUi,
): Set<Int> {
    return when (step.displayKind) {
    ClocktowerDisplayKind.EitherOne -> {
        val anyOf = step.displayProposition as? InformationProposition.AnyOf ?: return emptySet()
        val roleAt = anyOf.alternatives.map { it as? InformationProposition.RoleAt ?: return emptySet() }
        if (roleAt.size != 2 || roleAt.map { it.role }.distinct().size != 1) return emptySet()
        roleAt.map { it.seat }.distinct().takeIf { it.size == 2 }?.toSet().orEmpty()
    }

    ClocktowerDisplayKind.RoleReveal ->
        (step.displayProposition as? InformationProposition.RoleAt)?.let { setOf(it.seat) }.orEmpty()

    ClocktowerDisplayKind.Number ->
        (step.displayProposition as? InformationProposition.NumericResult)
            ?.subjectSeats
            ?.distinct()
            ?.takeIf { it.size in 1..2 }
            ?.toSet()
            .orEmpty()

    ClocktowerDisplayKind.YesNo ->
        (step.displayProposition as? InformationProposition.BooleanResult)
            ?.subjectSeats
            ?.distinct()
            ?.takeIf { it.size in 1..2 }
            ?.toSet()
            .orEmpty()

    else -> emptySet()
    }
}

@Composable
private fun EvilInfoDisplay(
    primary: String,
    secondary: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOfNotNull(primary, secondary?.takeIf { it.isNotBlank() }).forEach { section ->
            val lines = section.lines()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B3833), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    lines.firstOrNull().orEmpty(),
                    color = Color(0xFFAFC7BC),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    lines.drop(1).joinToString("\n"),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun ClocktowerPlayerDisplayCardLocalized(
    step: ClocktowerNightStepUi,
    cards: List<PlayerCard>,
    onDismiss: () -> Unit,
) {
    if (step.displayKind == ClocktowerDisplayKind.Grimoire) {
        ClocktowerPlayerGrimoireDisplay(step = step, onDismiss = onDismiss)
        return
    }

    val highlightedSeats = clocktowerPlayerDisplayHighlightedSeats(step)
    val seats = cards.mapIndexed { index, card ->
        val seatNumber = index + 1
        ClocktowerSquareTableSeatUiModel(
            seatId = "seat-$seatNumber",
            seatNumber = seatNumber,
            label = card.name,
            state = if (seatNumber in highlightedSeats) {
                ClocktowerSquareTableSeatState.HighlightedInformation
            } else {
                ClocktowerSquareTableSeatState.Neutral
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0D10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                step.displayTitle,
                color = Color(0xFFF1EADC),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
            ClocktowerSquareTableSeatSurface(
                seats = seats,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
            ) {
                ClocktowerPlayerDisplayCenterContent(
                    step = step,
                    hasHighlightedSeats = highlightedSeats.isNotEmpty(),
                )
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC5A56A)),
            ) {
                Text(stringResource(R.string.clocktower_display_close))
            }
        }
    }
}

@Composable
private fun ClocktowerPlayerDisplayCenterContent(
    step: ClocktowerNightStepUi,
    hasHighlightedSeats: Boolean,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (step.displayKind) {
            ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                Text(
                    primary,
                    color = Color(0xFFC5A56A),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                secondary?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        color = Color(0xFFF7F1E6),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
                if (footer.isNotBlank()) {
                    Text(
                        footer,
                        color = Color(0xFFF1EADC),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ClocktowerDisplayKind.EitherOne -> {
                Text(
                    primary,
                    color = Color(0xFFF7F1E6),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                if (footer.isNotBlank()) {
                    Text(
                        footer,
                        color = Color(0xFFAAA397),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                if (!hasHighlightedSeats) {
                    secondary?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            color = Color(0xFFC5A56A),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

            ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                Text(
                    primary,
                    color = Color(0xFFF7F1E6),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                if (footer.isNotBlank()) {
                    Text(
                        footer,
                        color = Color(0xFFAAA397),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ClocktowerDisplayKind.Grimoire,
            ClocktowerDisplayKind.None,
            -> Unit
        }
    }
}

@Composable
private fun ClocktowerPlayerGrimoireDisplay(
    step: ClocktowerNightStepUi,
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val footer = step.displayFooter ?: step.explanation
    val lines = primary.lines().filter { it.isNotBlank() }
    val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0D10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                step.displayTitle,
                color = Color(0xFFF1EADC),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                lines.forEach { line ->
                    val parts = line.split(Regex("[：:]"), limit = 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B1F25), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            parts.firstOrNull().orEmpty(),
                            color = Color(0xFFF1EADC),
                            fontSize = rowFontSize,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1.35f),
                        )
                        Text(
                            parts.getOrNull(1).orEmpty(),
                            color = Color(0xFFC5A56A),
                            fontSize = rowFontSize,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.85f),
                        )
                    }
                }
            }
            if (footer.isNotBlank()) {
                Text(
                    footer,
                    color = Color(0xFFAAA397),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC5A56A)),
            ) {
                Text(stringResource(R.string.clocktower_display_close))
            }
        }
    }
}
