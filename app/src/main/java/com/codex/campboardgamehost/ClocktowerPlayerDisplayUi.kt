package com.codex.campboardgamehost

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        listOfNotNull(primary, secondary?.takeIf { it.isNotBlank() }).forEach { section ->
            val lines = section.lines().filter(String::isNotBlank)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B1F25), RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    lines.firstOrNull().orEmpty(),
                    color = Color(0xFFC5A56A),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                lines.drop(1).takeIf { it.isNotEmpty() }?.let { detailLines ->
                    Text(
                        detailLines.joinToString("\n"),
                        color = Color(0xFFF7F1E6),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                }
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
    BackHandler(onBack = onDismiss)
    val displayStep = clocktowerPlayerDisplayStep(step)
    val pairPresentation = clocktowerPairPlayerRevealPresentation(displayStep, cards)

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
            if (displayStep.displayKind != ClocktowerDisplayKind.RoleReveal) Text(
                pairPresentation?.title ?: displayStep.displayTitle,
                color = Color(0xFFF1EADC),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (pairPresentation != null) {
                    ClocktowerPairPlayerRevealContent(pairPresentation)
                } else {
                    ClocktowerPlayerDisplayCenterContent(displayStep)
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
    val language = LocalContext.current.resources.configuration.locales[0].language
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        when (presentation.displayKind) {
            ClocktowerDisplayKind.EitherOne -> {
                ClocktowerPairPlayerRevealPrimary(presentation)
                ClocktowerPairPlayerRevealSeats(presentation, language)
            }

            ClocktowerDisplayKind.Number,
            ClocktowerDisplayKind.YesNo,
            -> {
                ClocktowerPairPlayerRevealSeats(presentation, language)
                ClocktowerPairPlayerRevealPrimary(presentation)
            }

            else -> {
                ClocktowerPairPlayerRevealSeats(presentation, language)
                ClocktowerPairPlayerRevealPrimary(presentation)
            }
        }
    }
}

@Composable
private fun ClocktowerPairPlayerRevealSeats(
    presentation: ClocktowerPairPlayerRevealPresentation,
    language: String,
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
                ClocktowerSeatNumberBadge(
                    seatNumber = seat.seatId.number,
                    languageCode = language,
                    scale = ClocktowerSeatNumberBadgeScale.PlayerReveal,
                    contentColor = Color(0xFFC5A56A),
                    containerColor = Color(0xFFC5A56A).copy(alpha = 0.10f),
                    borderColor = Color(0xFFC5A56A).copy(alpha = 0.65f),
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
}

@Composable
private fun ClocktowerPairPlayerRevealPrimary(
    presentation: ClocktowerPairPlayerRevealPresentation,
) {
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
}

@Composable
private fun ClocktowerPlayerDisplayCenterContent(
    step: ClocktowerNightStepUi,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
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
            }

            ClocktowerDisplayKind.EitherOne -> {
                Text(
                    primary,
                    color = Color(0xFFF7F1E6),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B1F25), RoundedCornerShape(18.dp))
                        .padding(horizontal = 18.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        primary,
                        color = Color(0xFFC5A56A),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ClocktowerDisplayKind.None,
            -> Unit
        }
    }
}


