package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class ClocktowerSeatNumberBadgeScale {
    Compact,
    PlayerReveal,
}

/**
 * Shared visual identity for a physical Clocktower seat.
 *
 * The visible badge is language-neutral (Arabic numerals only). The localized textual form remains
 * available to accessibility services through [clocktowerSeatNumberLabel].
 */
@Composable
internal fun ClocktowerSeatNumberBadge(
    seatNumber: Int,
    languageCode: String,
    scale: ClocktowerSeatNumberBadgeScale,
    contentColor: Color,
    containerColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    val localizedSeatDescription = clocktowerSeatNumberLabel(seatNumber, languageCode)
    val isPlayerReveal = scale == ClocktowerSeatNumberBadgeScale.PlayerReveal
    val badgeHeight = if (isPlayerReveal) 72.dp else 22.dp
    val minimumWidth = if (isPlayerReveal) 72.dp else 22.dp
    val horizontalPadding = if (isPlayerReveal) 14.dp else 5.dp
    val cornerRadius = if (isPlayerReveal) 18.dp else 6.dp
    val fontSize = if (isPlayerReveal) 42.sp else 14.sp
    val lineHeight = if (isPlayerReveal) 46.sp else 15.sp
    val borderWidth = if (isPlayerReveal) 1.5.dp else 1.dp

    Surface(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = localizedSeatDescription
        },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Box(
            modifier = Modifier
                .height(badgeHeight)
                .widthIn(min = minimumWidth)
                .padding(horizontal = horizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = seatNumber.toString(),
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
    }
}
