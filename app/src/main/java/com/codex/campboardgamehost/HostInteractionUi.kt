package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
internal fun HostProgressCard(
    title: String,
    subtitle: String,
    progress: String,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(progress, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun HostScriptCard(
    title: String,
    script: String,
    action: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val instruction = listOf(script.trim(), action.trim())
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString("\n")
            HostInstructionBlock(
                label = stringResource(R.string.host_instruction_label),
                text = instruction,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                textColor = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
internal fun HostInstructionBlock(
    label: String,
    text: String,
    backgroundColor: Color,
    textColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = textColor.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Text(text, color = textColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun HostActionSection(
    title: String,
    helper: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        helper?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectablePlayerChips(
    cards: List<PlayerCard>,
    selectedName: String?,
    enabled: Boolean,
    allCards: List<PlayerCard> = cards,
    onSelect: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val selected = selectedName == card.name
            if (selected) {
                Button(onClick = { onSelect(card.name) }, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
                    Text(card.seatLabel(allCards))
                }
            } else {
                OutlinedButton(onClick = { onSelect(card.name) }, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
                    Text(card.seatLabel(allCards))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectableTwoPlayerChips(
    cards: List<PlayerCard>,
    firstSelectedName: String?,
    secondSelectedName: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    val selectedNames = setOfNotNull(firstSelectedName, secondSelectedName)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val label = if (card.eliminatedRound != null) {
                stringResource(R.string.clocktower_player_dead_format, card.seatLabel(cards))
            } else {
                card.seatLabel(cards)
            }
            if (card.name in selectedNames) {
                Button(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(label)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(label)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectableSeatNumbers(
    cards: List<PlayerCard>,
    selectedName: String?,
    enabled: Boolean,
    allCards: List<PlayerCard> = cards,
    onSelect: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val selected = selectedName == card.name
            val seatNumber = (allCards.indexOfFirst { it.name == card.name } + 1).takeIf { it > 0 } ?: 0
            val colors = if (selected) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            }
            if (selected) {
                Button(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = colors,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(58.dp),
                ) {
                    Text(seatNumber.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = colors,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(58.dp),
                ) {
                    Text(seatNumber.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
