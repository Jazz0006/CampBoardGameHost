package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class ClocktowerSquareTableSeatState {
    Neutral,
    Selectable,
    SelectedFirst,
    SelectedSecond,
    HighlightedInformation,
    Disabled,
}

internal enum class ClocktowerSquareTableEdge {
    Top,
    Right,
    Bottom,
    Left,
}

internal enum class ClocktowerSquareTableInteractionMode {
    ReadOnly,
    Selectable,
}

internal data class ClocktowerSquareTableSeatUiModel(
    val seatId: String,
    val seatNumber: Int,
    val label: String,
    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,
)

internal data class ClocktowerSquareTableSeatPlacement(
    val seat: ClocktowerSquareTableSeatUiModel,
    val edge: ClocktowerSquareTableEdge,
    val indexOnEdge: Int,
)

internal fun clocktowerSquareTablePlacements(
    seats: List<ClocktowerSquareTableSeatUiModel>,
): List<ClocktowerSquareTableSeatPlacement> {
    require(seats.map { it.seatId }.distinct().size == seats.size) {
        "Square-table seat identity must be unique"
    }
    if (seats.isEmpty()) return emptyList()

    val edges = ClocktowerSquareTableEdge.values()
    val baseCount = seats.size / edges.size
    val remainder = seats.size % edges.size
    val edgeCounts = edges.indices.map { edgeIndex ->
        baseCount + if (edgeIndex < remainder) 1 else 0
    }

    var seatIndex = 0
    return buildList(seats.size) {
        edges.forEachIndexed { edgeIndex, edge ->
            repeat(edgeCounts[edgeIndex]) { indexOnEdge ->
                add(
                    ClocktowerSquareTableSeatPlacement(
                        seat = seats[seatIndex],
                        edge = edge,
                        indexOnEdge = indexOnEdge,
                    ),
                )
                seatIndex += 1
            }
        }
    }
}

@Composable
internal fun ClocktowerSquareTableSeatSurface(
    seats: List<ClocktowerSquareTableSeatUiModel>,
    modifier: Modifier = Modifier,
    interactionMode: ClocktowerSquareTableInteractionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
    onSeatClick: (String) -> Unit = {},
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    val placements = remember(seats) { clocktowerSquareTablePlacements(seats) }
    val top = placements.filter { it.edge == ClocktowerSquareTableEdge.Top }
    val right = placements.filter { it.edge == ClocktowerSquareTableEdge.Right }
    val bottom = placements.filter { it.edge == ClocktowerSquareTableEdge.Bottom }.reversed()
    val left = placements.filter { it.edge == ClocktowerSquareTableEdge.Left }.reversed()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        ClocktowerSquareTableHorizontalEdge(
            placements = top,
            interactionMode = interactionMode,
            onSeatClick = onSeatClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 46.dp),
        )
        ClocktowerSquareTableVerticalEdge(
            placements = right,
            interactionMode = interactionMode,
            onSeatClick = onSeatClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 54.dp),
        )
        ClocktowerSquareTableHorizontalEdge(
            placements = bottom,
            interactionMode = interactionMode,
            onSeatClick = onSeatClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 46.dp),
        )
        ClocktowerSquareTableVerticalEdge(
            placements = left,
            interactionMode = interactionMode,
            onSeatClick = onSeatClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .padding(vertical = 54.dp),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.56f)
                .fillMaxHeight(0.52f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
                content = centerContent,
            )
        }
    }
}

@Composable
private fun ClocktowerSquareTableHorizontalEdge(
    placements: List<ClocktowerSquareTableSeatPlacement>,
    interactionMode: ClocktowerSquareTableInteractionMode,
    onSeatClick: (String) -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        placements.forEach { placement ->
            ClocktowerSquareTableSeat(
                seat = placement.seat,
                interactionMode = interactionMode,
                onSeatClick = onSeatClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
            )
        }
    }
}

@Composable
private fun ClocktowerSquareTableVerticalEdge(
    placements: List<ClocktowerSquareTableSeatPlacement>,
    interactionMode: ClocktowerSquareTableInteractionMode,
    onSeatClick: (String) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        placements.forEach { placement ->
            ClocktowerSquareTableSeat(
                seat = placement.seat,
                interactionMode = interactionMode,
                onSeatClick = onSeatClick,
                modifier = Modifier
                    .width(70.dp)
                    .padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ClocktowerSquareTableSeat(
    seat: ClocktowerSquareTableSeatUiModel,
    interactionMode: ClocktowerSquareTableInteractionMode,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSelect = interactionMode == ClocktowerSquareTableInteractionMode.Selectable &&
        seat.state in setOf(
            ClocktowerSquareTableSeatState.Selectable,
            ClocktowerSquareTableSeatState.SelectedFirst,
            ClocktowerSquareTableSeatState.SelectedSecond,
        )
    val palette = clocktowerSquareTableSeatPalette(seat.state)
    val clickModifier = if (canSelect) {
        Modifier.clickable { onSeatClick(seat.seatId) }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp, max = 62.dp)
            .then(clickModifier),
        shape = RoundedCornerShape(12.dp),
        color = palette.container,
        contentColor = palette.content,
        border = BorderStroke(palette.borderWidth, palette.border),
        tonalElevation = if (canSelect) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                clocktowerSquareTableStateMarker(seat.state)?.let { marker ->
                    Text(
                        text = marker,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "#${seat.seatNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = seat.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = if (seat.state in setOf(
                        ClocktowerSquareTableSeatState.SelectedFirst,
                        ClocktowerSquareTableSeatState.SelectedSecond,
                        ClocktowerSquareTableSeatState.HighlightedInformation,
                    )
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            )
        }
    }
}

private data class ClocktowerSquareTableSeatPalette(
    val container: Color,
    val content: Color,
    val border: Color,
    val borderWidth: androidx.compose.ui.unit.Dp,
)

@Composable
private fun clocktowerSquareTableSeatPalette(
    state: ClocktowerSquareTableSeatState,
): ClocktowerSquareTableSeatPalette {
    val colors = MaterialTheme.colorScheme
    return when (state) {
        ClocktowerSquareTableSeatState.Neutral -> ClocktowerSquareTableSeatPalette(
            container = colors.surfaceVariant,
            content = colors.onSurfaceVariant,
            border = colors.outlineVariant,
            borderWidth = 1.dp,
        )
        ClocktowerSquareTableSeatState.Selectable -> ClocktowerSquareTableSeatPalette(
            container = colors.surface,
            content = colors.onSurface,
            border = colors.primary,
            borderWidth = 1.5.dp,
        )
        ClocktowerSquareTableSeatState.SelectedFirst -> ClocktowerSquareTableSeatPalette(
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
            border = colors.primary,
            borderWidth = 2.5.dp,
        )
        ClocktowerSquareTableSeatState.SelectedSecond -> ClocktowerSquareTableSeatPalette(
            container = colors.secondaryContainer,
            content = colors.onSecondaryContainer,
            border = colors.secondary,
            borderWidth = 2.5.dp,
        )
        ClocktowerSquareTableSeatState.HighlightedInformation -> ClocktowerSquareTableSeatPalette(
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
            border = colors.tertiary,
            borderWidth = 2.5.dp,
        )
        ClocktowerSquareTableSeatState.Disabled -> ClocktowerSquareTableSeatPalette(
            container = colors.surfaceVariant.copy(alpha = 0.45f),
            content = colors.onSurfaceVariant.copy(alpha = 0.55f),
            border = colors.outlineVariant.copy(alpha = 0.55f),
            borderWidth = 1.dp,
        )
    }
}

private fun clocktowerSquareTableStateMarker(
    state: ClocktowerSquareTableSeatState,
): String? = when (state) {
    ClocktowerSquareTableSeatState.Neutral -> null
    ClocktowerSquareTableSeatState.Selectable -> "○"
    ClocktowerSquareTableSeatState.SelectedFirst -> "①"
    ClocktowerSquareTableSeatState.SelectedSecond -> "②"
    ClocktowerSquareTableSeatState.HighlightedInformation -> "★"
    ClocktowerSquareTableSeatState.Disabled -> "×"
}
