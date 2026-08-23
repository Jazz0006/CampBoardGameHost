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
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        listOfNotNull(primary, secondary?.takeIf { it.isNotBlank() }).forEach { section ->
            val lines = section.lines()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B3833), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    lines.firstOrNull().orEmpty(),
                    color = Color(0xFFAFC7BC),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    lines.drop(1).joinToString("\n"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
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
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                when (step.displayKind) {
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                        Text(
                            primary,
                            color = Color(0xFFC5A56A),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        secondary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                color = Color(0xFFF7F1E6),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            footer,
                            color = Color(0xFFF1EADC),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EitherOne -> {
                        Text(
                            primary,
                            color = Color(0xFFF7F1E6),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFC5A56A),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

                    ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                        Text(
                            primary,
                            color = Color(0xFFF7F1E6),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.Grimoire -> {
                        val lines = primary.lines().filter { it.isNotBlank() }
                        val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp
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
                    }

                    ClocktowerDisplayKind.None -> Unit
                }
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

@Composable
private fun ClocktowerPlayerDisplayCard(
    step: ClocktowerNightStepUi,
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1F2925),
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
                color = Color(0xFFEAF2EA),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                when (step.displayKind) {
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                        Text(
                            primary,
                            color = Color(0xFFFFF4DC),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            footer,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EitherOne -> {
                        Text(
                            primary,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFFFF4DC),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            footer,
                            color = Color(0xFFEAF2EA),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

                    ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                        Text(
                            primary,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFEAF2EA),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.Grimoire -> {
                        val lines = primary.lines().filter { it.isNotBlank() }
                        val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            lines.forEach { line ->
                                val parts = line.split("：", limit = 2)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2B3833), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        parts.firstOrNull().orEmpty(),
                                        color = Color.White,
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.35f),
                                    )
                                    Text(
                                        parts.getOrNull(1).orEmpty(),
                                        color = Color(0xFFFFF4DC),
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
                                color = Color(0xFFEAF2EA),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.None -> Unit
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.clocktower_display_close))
            }
        }
    }
}
