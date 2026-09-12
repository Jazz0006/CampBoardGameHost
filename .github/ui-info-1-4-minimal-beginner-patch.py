from pathlib import Path
import sys

ROOT = Path('.')

def replace_once(path, old, new):
    p = ROOT / path
    text = p.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{path}: expected 1 anchor, found {count}: {old[:80]!r}')
    p.write_text(text.replace(old, new, 1))

TEST = ROOT / 'app/src/test/java/com/codex/campboardgamehost/ClocktowerBeginnerMinimalSurfaceTest.kt'
TEST_TEXT = r'''package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerBeginnerMinimalSurfaceTest {
    @Test
    fun `beginner reveal-only seats never expose information highlights`() {
        val actor = clocktowerBeginnerNeutralSeatPresentation(seatNumber = 2, actorSeat = 2)
        val other = clocktowerBeginnerNeutralSeatPresentation(seatNumber = 5, actorSeat = 2)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertTrue(actor.isCurrentActor)
        assertFalse(other.isCurrentActor)
    }

    @Test
    fun `structured guidance remains the only compact-mode trigger`() {
        assertTrue(clocktowerUsesBeginnerCompactNightGuidance("唤醒 厨师\n2号 Jazz"))
        assertFalse(clocktowerUsesBeginnerCompactNightGuidance("厨师：查看邪恶相邻对"))
    }
}
'''

if '--red' in sys.argv:
    TEST.write_text(TEST_TEXT)
    raise SystemExit(0)

TEST.write_text(TEST_TEXT)

common = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightUi.kt'
p = ROOT / common
text = p.read_text()
append = r'''

internal fun clocktowerBeginnerNeutralSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerBeginnerReadOnlyRevealDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShow: () -> Unit,
    buttonLabel: String? = null,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = false,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerBeginnerNeutralSeatPresentation(seatNumber, actorSeat)
        },
        onSeatSelected = {},
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            androidx.compose.material3.Button(onClick = onShow) {
                androidx.compose.material3.Text(
                    buttonLabel ?: if (language == "en") "Show to player" else "展示给玩家",
                )
            }
        }
    }
}

@Composable
internal fun ClocktowerBeginnerSingleTargetRevealDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShow: (() -> Unit)?,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSingleTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeat = selectedSeat,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
        onSeatSelected = onSeatSelected,
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            onShow?.let { reveal ->
                androidx.compose.material3.Button(onClick = reveal) {
                    androidx.compose.material3.Text(if (language == "en") "Show to player" else "展示给玩家")
                }
            }
        }
    }
}

@Composable
internal fun ClocktowerBeginnerTwoTargetRevealDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShow: (() -> Unit)?,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerTwoTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeats = selectedSeats,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
        onSeatSelected = onSeatSelected,
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            onShow?.let { reveal ->
                androidx.compose.material3.Button(onClick = reveal) {
                    androidx.compose.material3.Text(if (language == "en") "Show to player" else "展示给玩家")
                }
            }
        }
    }
}
'''
if 'internal fun clocktowerBeginnerNeutralSeatPresentation' not in text:
    p.write_text(text + append)

# Reveal-only role surfaces: early-return before any detailed storyteller evidence is rendered.
for path in [
    'app/src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt',
    'app/src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt',
    'app/src/main/java/com/codex/campboardgamehost/ClocktowerClockmakerSquareTableUi.kt',
    'app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt',
    'app/src/main/java/com/codex/campboardgamehost/ClocktowerSageSquareTableUi.kt',
]:
    anchor = '    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.first()\n'
    insert = anchor + '''    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction)) {
        ClocktowerBeginnerReadOnlyRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = { onConfirm(initialChoice) },
        )
        return
    }
'''
    replace_once(path, anchor, insert)

# Generic information fallback must never preview displayTitle/primary/secondary/footer in Beginner.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerPlainInformationSquareTableUi.kt'
anchor = '    val step = presentation.displayStep\n'
insert = anchor + '''    if (clocktowerUsesBeginnerCompactNightGuidance(presentation.wakeInstruction)) {
        ClocktowerBeginnerReadOnlyRevealDialog(
            seats = seats,
            actorSeat = presentation.actorSeat,
            wakeInstruction = presentation.wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = onShowPlayerDisplay,
        )
        return
    }
'''
replace_once(path, anchor, insert)

# Spy: only the immediate action matters in Beginner; explanation and duplicate role title stay Experienced-only.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerSpySquareTableUi.kt'
anchor = ') {\n    ClocktowerNightActionSquareTableDialog(\n'
insert = ''') {
    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction) && presentation.showLegacyRevealAction) {
        ClocktowerBeginnerReadOnlyRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = onShowLegacyReveal,
            buttonLabel = if (language == "en") "Show grimoire" else "展示魔典",
        )
        return
    }
    ClocktowerNightActionSquareTableDialog(
'''
replace_once(path, anchor, insert)

# Ravenkeeper: keep target choice, hide chosen-seat text and role result until explicit reveal.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTableUi.kt'
anchor = '    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice\n\n'
insert = anchor + '''    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction)) {
        ClocktowerBeginnerSingleTargetRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            selectedSeat = selectedSeat,
            selectableSeats = selectableSeats,
            enabled = enabled,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onSeatSelected = onSeatSelected,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = if (selectedChoice != null) ({ onConfirm(selectedChoice) }) else null,
        )
        return
    }

'''
replace_once(path, anchor, insert)

# Fortune Teller: table selection is enough feedback; result value stays hidden until reveal.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt'
anchor = ') {\n    Dialog(\n'
insert = ''') {
    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction)) {
        val actions = clocktowerFortuneTellerResultActions(legalResults, recommendedResult)
        val result = recommendedResult?.takeIf { it in legalResults } ?: actions.firstOrNull()
        val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2
        ClocktowerBeginnerTwoTargetRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            selectedSeats = selectedSeats,
            selectableSeats = selectableSeats,
            enabled = enabled,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onSeatSelected = onSeatSelected,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = if (completePair && result != null) ({
                if (automaticStorytellerInfo) onAutomaticResultSelected(result) else onResultSelected(result)
            }) else null,
        )
        return
    }
    Dialog(
'''
replace_once(path, anchor, insert)

# Chambermaid: keep only the two-player choice and one reveal action.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidSquareTableUi.kt'
anchor = ') {\n    ClocktowerNightActionSquareTableDialog(\n'
insert = ''') {
    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction)) {
        val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2
        val orderedOptions = resultOptions.sortedBy { option -> if (option.isDefaultRecommendation) 0 else 1 }
        val reveal: (() -> Unit)? = when {
            !completePair -> null
            orderedOptions.isEmpty() -> onShowDeterminedResult
            else -> ({ onResultSelected(orderedOptions.first()) })
        }
        ClocktowerBeginnerTwoTargetRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            selectedSeats = selectedSeats,
            selectableSeats = selectableSeats,
            enabled = enabled,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onSeatSelected = onSeatSelected,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = reveal,
        )
        return
    }
    ClocktowerNightActionSquareTableDialog(
'''
replace_once(path, anchor, insert)
