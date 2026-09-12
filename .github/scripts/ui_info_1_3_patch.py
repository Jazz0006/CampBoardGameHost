from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "app/src/test/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightPresentationTest.kt"
HELPER = ROOT / "app/src/main/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightUi.kt"
SINGLE = ROOT / "app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionUi.kt"
PAIR = ROOT / "app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt"

TEST_TEXT = r'''package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerBeginnerCompactNightPresentationTest {
    @Test
    fun `structured beginner wake guidance uses compact surface`() {
        assertTrue(clocktowerUsesBeginnerCompactNightGuidance("唤醒 投毒者\n3号 张三\n让他选择一名玩家作为中毒目标"))
        assertFalse(clocktowerUsesBeginnerCompactNightGuidance("Wake P3 张三 and choose a target"))
    }

    @Test
    fun `beginner pair surface hides clue target accents but keeps actor cue`() {
        val actor = clocktowerBeginnerPairSeatPresentation(seatNumber = 3, actorSeat = 3)
        val other = clocktowerBeginnerPairSeatPresentation(seatNumber = 5, actorSeat = 3)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertTrue(actor.isCurrentActor)
        assertFalse(other.isCurrentActor)
    }
}
'''

HELPER_TEXT = r'''package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal fun clocktowerUsesBeginnerCompactNightGuidance(instruction: String?): Boolean {
    val lines = instruction
        ?.lines()
        ?.map(String::trim)
        ?.filter(String::isNotBlank)
        .orEmpty()
    return lines.size in 2..3 &&
        (lines.firstOrNull()?.startsWith("唤醒 ") == true || lines.firstOrNull()?.startsWith("Wake ") == true)
}

internal fun clocktowerBeginnerPairSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerBeginnerSingleTargetAbilityDialog(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerSingleTargetAbilityPresentation,
    language: String,
    canGoPrevious: Boolean,
    onHostTools: () -> Unit,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = presentation.selection.enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSingleTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = presentation.actorSeat,
                selectedSeat = presentation.selection.selectedSeat,
                selectableSeats = if (presentation.selection.enabled) presentation.selection.selectableSeats else emptySet(),
            )
        },
        onSeatSelected = { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) },
        canGoPrevious = canGoPrevious,
        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onHostTools = onHostTools,
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(presentation.wakeInstruction)
        }
    }
}
'''

def replace_once(path: Path, old: str, new: str, label: str) -> None:
    text = path.read_text()
    if text.count(old) != 1:
        raise SystemExit(f"{label} anchor mismatch: {text.count(old)}")
    path.write_text(text.replace(old, new, 1))


def write_test() -> None:
    TEST.write_text(TEST_TEXT)


def apply_patch() -> None:
    HELPER.write_text(HELPER_TEXT)

    replace_once(
        SINGLE,
        """    val action = presentation.action\n    val title = when (action) {""",
        """    val action = presentation.action\n    if (clocktowerUsesBeginnerCompactNightGuidance(presentation.wakeInstruction)) {\n        ClocktowerBeginnerSingleTargetAbilityDialog(\n            seats = seats,\n            presentation = presentation,\n            language = language,\n            canGoPrevious = canGoPrevious,\n            onHostTools = onHostTools,\n            onEvent = onEvent,\n        )\n        return\n    }\n    val title = when (action) {""",
        "single-target compact",
    )

    replace_once(
        PAIR,
        """    val canonicalRecommendedOption = remember(interactionKey, presentation, recommendedOption) {""",
        """    val beginnerMode = !allowManualEditing\n    val canonicalRecommendedOption = remember(interactionKey, presentation, recommendedOption) {""",
        "pair beginner flag",
    )
    replace_once(
        PAIR,
        """                        val seatPresentation = clocktowerPairInformationSeatPresentation(\n                            selection = selection,\n                            seatNumber = seat.seatId.number,\n                            editing = editing,\n                            actorSeat = actorSeat,\n                        )""",
        """                        val seatPresentation = if (beginnerMode) {\n                            clocktowerBeginnerPairSeatPresentation(\n                                seatNumber = seat.seatId.number,\n                                actorSeat = actorSeat,\n                            )\n                        } else {\n                            clocktowerPairInformationSeatPresentation(\n                                selection = selection,\n                                seatNumber = seat.seatId.number,\n                                editing = editing,\n                                actorSeat = actorSeat,\n                            )\n                        }""",
        "pair seat presentation",
    )
    replace_once(
        PAIR,
        """                        wakeInstruction = wakeInstruction,\n                        abilityLabel = abilityLabel,""",
        """                        wakeInstruction = wakeInstruction,\n                        beginnerMode = beginnerMode,\n                        abilityLabel = abilityLabel,""",
        "pair center call",
    )
    replace_once(
        PAIR,
        """private fun ClocktowerPairInformationCenterControls(\n    wakeInstruction: String?,\n    abilityLabel: String,""",
        """private fun ClocktowerPairInformationCenterControls(\n    wakeInstruction: String?,\n    beginnerMode: Boolean,\n    abilityLabel: String,""",
        "pair center signature",
    )
    replace_once(
        PAIR,
        """    val hasRecommendation = recommendedSelection.resolvedOption != null\n\n    Column(""",
        """    val hasRecommendation = recommendedSelection.resolvedOption != null\n\n    if (beginnerMode) {\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .padding(6.dp),\n            horizontalAlignment = Alignment.CenterHorizontally,\n            verticalArrangement = Arrangement.Center,\n        ) {\n            ClocktowerNightActionWakeInstruction(wakeInstruction)\n            Spacer(Modifier.height(8.dp))\n            recommendedSelection.resolvedOption?.let { resolved ->\n                Button(\n                    onClick = { onConfirm(resolved) },\n                    modifier = Modifier.fillMaxWidth(),\n                ) {\n                    Text(if (language == \"en\") \"Show to player\" else \"展示给玩家\")\n                }\n            }\n        }\n        return\n    }\n\n    Column(""",
        "pair compact center",
    )


mode = sys.argv[1] if len(sys.argv) > 1 else ""
if mode == "test":
    write_test()
elif mode == "patch":
    apply_patch()
else:
    raise SystemExit("usage: ui_info_1_3_patch.py test|patch")
