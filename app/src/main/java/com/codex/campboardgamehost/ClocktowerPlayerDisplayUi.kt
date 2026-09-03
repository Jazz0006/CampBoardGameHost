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

    val pairPresentation = clocktowerPairPlayerRevealPresentation(step, cards)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0D10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                pairPresentation?.title ?: step.displayTitle,
                color = Color(0xFFF1EADC),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (pairPresentation != null) {
                    ClocktowerPairPlayerRevealContent(pairPresentation)
                } else {
                    ClocktowerPlayerDisplayCenterContent(step)
                }
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
private fun ClocktowerPairPlayerRevealContent(
    presentation: ClocktowerPairPlayerRevealPresentation,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            presentation.seats.forEach { seat ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1B1F25), RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "#${seat.seatId.number}",
                        color = Color(0xFFC5A56A),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                    Text(
                        text = seat.playerName,
                        color = Color(0xFFF7F1E6),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }
        }

        when (presentation.displayKind) {
            ClocktowerDisplayKind.Number,
            ClocktowerDisplayKind.YesNo,
            -> Text(
                text = presentation.primary,
                color = Color(0xFFC5A56A),
                fontSize = 60.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )

            ClocktowerDisplayKind.EitherOne -> Text(
                text = presentation.primary,
                color = Color(0xFFF7F1E6),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )

            else -> Unit
        }

        presentation.footer?.let { footer ->
            Text(
                text = footer,
                color = Color(0xFFAAA397),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ClocktowerPlayerDisplayCenterContent(
    step: ClocktowerNightStepUi,
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
