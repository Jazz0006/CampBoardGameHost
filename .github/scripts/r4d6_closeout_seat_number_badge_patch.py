from pathlib import Path

SQUARE_TABLE_PATH = Path(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt"
)
PLAYER_DISPLAY_PATH = Path(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt"
)
BADGE_PATH = Path(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSeatNumberBadge.kt"
)


def read_lf_utf8(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}; refusing implicit normalization")
    return raw.decode("utf-8")


square_text = read_lf_utf8(SQUARE_TABLE_PATH)
player_display_text = read_lf_utf8(PLAYER_DISPLAY_PATH)

if BADGE_PATH.exists():
    raise SystemExit(f"{BADGE_PATH} already exists; refusing to overwrite")

old_square = """                Text(
                    text = clocktowerSeatNumberLabel(seat.seatNumber, language),
                    fontSize = 15.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Black,
                )
"""
new_square = """                ClocktowerSeatNumberBadge(
                    seatNumber = seat.seatNumber,
                    languageCode = language,
                    scale = ClocktowerSeatNumberBadgeScale.Compact,
                    contentColor = palette.content,
                    containerColor = palette.content.copy(alpha = 0.08f),
                    borderColor = palette.content.copy(alpha = 0.45f),
                )
"""

old_player_display = """                Text(
                    text = clocktowerSeatNumberLabel(seat.seatId.number, language),
                    color = Color(0xFFC5A56A),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
"""
new_player_display = """                ClocktowerSeatNumberBadge(
                    seatNumber = seat.seatId.number,
                    languageCode = language,
                    scale = ClocktowerSeatNumberBadgeScale.PlayerReveal,
                    contentColor = Color(0xFFC5A56A),
                    containerColor = Color(0xFFC5A56A).copy(alpha = 0.10f),
                    borderColor = Color(0xFFC5A56A).copy(alpha = 0.65f),
                )
"""

square_count = square_text.count(old_square)
player_display_count = player_display_text.count(old_player_display)
if square_count != 1:
    raise SystemExit(f"Expected exactly one square-table seat-number anchor, found {square_count}")
if player_display_count != 1:
    raise SystemExit(
        f"Expected exactly one Player Reveal seat-number anchor, found {player_display_count}"
    )

badge_text = """package com.codex.campboardgamehost

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
"""

square_text = square_text.replace(old_square, new_square, 1)
player_display_text = player_display_text.replace(old_player_display, new_player_display, 1)

if old_square in square_text or old_player_display in player_display_text:
    raise SystemExit("Original seat-number render anchor remains after replacement")
if "ClocktowerSeatNumberBadgeScale.Compact" not in square_text:
    raise SystemExit("Compact square-table badge postcondition missing")
if "ClocktowerSeatNumberBadgeScale.PlayerReveal" not in player_display_text:
    raise SystemExit("Player Reveal badge postcondition missing")
if 'text = seatNumber.toString()' not in badge_text:
    raise SystemExit("Language-neutral visible seat-number postcondition missing")
if "clocktowerSeatNumberLabel(seatNumber, languageCode)" not in badge_text:
    raise SystemExit("Localized accessibility fallback postcondition missing")

SQUARE_TABLE_PATH.write_text(square_text, encoding="utf-8", newline="\n")
PLAYER_DISPLAY_PATH.write_text(player_display_text, encoding="utf-8", newline="\n")
BADGE_PATH.write_text(badge_text, encoding="utf-8", newline="\n")
