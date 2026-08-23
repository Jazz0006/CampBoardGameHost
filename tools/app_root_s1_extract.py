from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
TARGET = Path("app/src/main/java/com/codex/campboardgamehost/AppPlayerSetupScreens.kt")

source = ROOT.read_text(encoding="utf-8")


def extract_exact(text: str, start: str, end: str) -> tuple[str, str]:
    if text.count(start) != 1:
        raise SystemExit(f"expected exactly one start anchor: {start!r}")
    if text.count(end) != 1:
        raise SystemExit(f"expected exactly one end anchor: {end!r}")
    start_index = text.index(start)
    end_index = text.index(end, start_index)
    return text[start_index:end_index], text[:start_index] + text[end_index:]


drag_block, source = extract_exact(
    source,
    "private sealed class DraggedPlayer {",
    "private fun Context.playerName(number: Int): String",
)

setup_block, source = extract_exact(
    source,
    "@OptIn(ExperimentalLayoutApi::class)\n@Composable\nprivate fun SetupScreen(",
    "@OptIn(ExperimentalLayoutApi::class)\n@Composable\nprivate fun SettingsScreen(",
)
setup_block = setup_block.replace(
    "@OptIn(ExperimentalLayoutApi::class)\n@Composable\nprivate fun SetupScreen(",
    "@OptIn(ExperimentalLayoutApi::class)\n@Composable\ninternal fun SetupScreen(",
    1,
)

replacements = {
    "private data class SavedGamePreview(": "internal data class SavedGamePreview(",
    "private const val MAX_PLAYERS = 15": "internal const val MAX_PLAYERS = 15",
    "private fun EmptyStateCard(text: String)": "internal fun EmptyStateCard(text: String)",
}
for old, new in replacements.items():
    if source.count(old) != 1:
        raise SystemExit(f"expected exactly one visibility anchor: {old!r}")
    source = source.replace(old, new, 1)

new_file = '''package com.codex.campboardgamehost

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

'''
new_file += drag_block.strip() + "\n\n" + setup_block.strip() + "\n"

# S1 is deliberately mechanical: the root remains the state/navigation owner and
# the new file receives only callback-driven presentation plus local drag state.
ROOT.write_text(source, encoding="utf-8")
TARGET.write_text(new_file, encoding="utf-8")

print(f"extracted {len(new_file)} bytes to {TARGET}")
print(f"root is now {len(source)} bytes")
