from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
paths = {
    "day": ROOT / "clocktower/ui/ClocktowerDayTableScaffoldUi.kt",
    "night_screen": ROOT / "clocktower/ui/ClocktowerNightScreen.kt",
    "night_action": ROOT / "ClocktowerNightActionSquareTableUi.kt",
    "host": ROOT / "clocktower/ui/ClocktowerHostScreen.kt",
    "night_step": ROOT / "ClocktowerNightStepUi.kt",
    "shared": ROOT / "ClocktowerHostFullScreenScaffold.kt",
    "ownership": ROOT / "ClocktowerNightFullScreenOwnership.kt",
}

for key in ("day", "night_screen", "night_action", "host", "night_step"):
    raw = paths[key].read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {paths[key]}")
for key in ("shared", "ownership"):
    if paths[key].exists():
        raise SystemExit(f"Expected new file to be absent: {paths[key]}")


def replace_exact(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor for {label}, found {count}")
    return text.replace(old, new, 1)


shared = '''package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Single Activity-window owner for persistent Clocktower host workspaces.
 *
 * The body owns game content only. This scaffold exclusively owns the persistent Previous / Host
 * Tools / Next row and its navigation-bar inset policy, so Day and Night cannot drift into parallel
 * window or bottom-navigation implementations.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerHostFullScreenScaffold(
    previousLabel: String,
    hostToolsLabel: String,
    nextLabel: String,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    body: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                body()
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp,
            ) {
                HostBottomActionBar(
                    previousLabel = previousLabel,
                    hostToolsLabel = hostToolsLabel,
                    nextLabel = nextLabel,
                    previousEnabled = previousEnabled,
                    nextEnabled = nextEnabled,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                    modifier = Modifier
                        .clocktowerHostBottomNavigationBarPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
'''

ownership = '''package com.codex.campboardgamehost

private val clocktowerNightSquareTableActions = setOf(
    ClocktowerNightAction.RedHerring,
    ClocktowerNightAction.Poison,
    ClocktowerNightAction.ButlerMaster,
    ClocktowerNightAction.MonkProtect,
    ClocktowerNightAction.DemonKill,
    ClocktowerNightAction.Ravenkeeper,
    ClocktowerNightAction.FortuneTeller,
    ClocktowerNightAction.Chambermaid,
    ClocktowerNightAction.MayorRedirect,
    ClocktowerNightAction.DemonSuccessor,
)

internal fun clocktowerNightActionOwnsSquareTable(action: ClocktowerNightAction): Boolean =
    action in clocktowerNightSquareTableActions

/** True when the current night step owns the Activity-root host workspace instead of a legacy card. */
internal fun clocktowerNightUsesFullScreenHostSurface(
    isRealAction: Boolean,
    action: ClocktowerNightAction,
    displayKind: ClocktowerDisplayKind,
): Boolean =
    isRealAction &&
        (clocktowerNightActionOwnsSquareTable(action) || displayKind != ClocktowerDisplayKind.None)
'''

# Day delegates all persistent window/navigation ownership to the shared scaffold.
day = '''package com.codex.campboardgamehost

import androidx.compose.runtime.Composable

/** Shared Day table adapter onto the single Activity-root Clocktower host scaffold. */
@Composable
internal fun ClocktowerDayTableScaffold(
    previousLabel: String,
    hostToolsLabel: String,
    nextLabel: String,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    tableContent: @Composable () -> Unit,
) {
    ClocktowerDarkTheme {
        ClocktowerHostFullScreenScaffold(
            previousLabel = previousLabel,
            hostToolsLabel = hostToolsLabel,
            nextLabel = nextLabel,
            previousEnabled = previousEnabled,
            nextEnabled = nextEnabled,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            body = tableContent,
        )
    }
}
'''
paths["day"].write_text(day, encoding="utf-8", newline="\n")

# Night regular-card shell uses the same shared host scaffold, while true full-screen content owns
# the Activity-root workspace directly and must not be wrapped in a second bottom-navigation shell.
night_screen = paths["night_screen"].read_text(encoding="utf-8")
old_night_active = '''/** Compact active-night shell: no independent top chrome; progress belongs inside the table. */
@Composable
internal fun ClocktowerNightActiveScreen(
    title: String,
    subtitle: String,
    progress: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    content: @Composable () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    ClocktowerDarkTheme {
        CompositionLocalProvider(LocalClocktowerNightProgress provides progress) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { content() }
                }

                ClocktowerNightBottomActionBar(
                    language = language,
                    canGoPrevious = canGoPrevious,
                    nextEnabled = nextEnabled,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                )
            }
        }
    }
}
'''
new_night_active = '''/** Compact active-night shell: full-screen steps own the Activity-root workspace directly. */
@Composable
internal fun ClocktowerNightActiveScreen(
    title: String,
    subtitle: String,
    progress: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    contentOwnsFullScreen: Boolean = false,
    content: @Composable () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    ClocktowerDarkTheme {
        CompositionLocalProvider(LocalClocktowerNightProgress provides progress) {
            if (contentOwnsFullScreen) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            } else {
                ClocktowerHostFullScreenScaffold(
                    previousLabel = if (language == "en") "← Previous" else "← 上一步",
                    hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
                    nextLabel = if (language == "en") "Next →" else "下一步 →",
                    previousEnabled = canGoPrevious,
                    nextEnabled = nextEnabled,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item { content() }
                    }
                }
            }
        }
    }
}
'''
night_screen = replace_exact(night_screen, old_night_active, new_night_active, "NightActive shared root")
paths["night_screen"].write_text(night_screen, encoding="utf-8", newline="\n")

# Host decides ownership once from structured step data; UI layers consume that decision.
host = paths["host"].read_text(encoding="utf-8")
host_old = '''            onPrevious = onMovePreviousNightStep,
            onHostTools = onHostTools,
            onNext = advanceNightStep,
        ) {
'''
host_new = '''            onPrevious = onMovePreviousNightStep,
            onHostTools = onHostTools,
            onNext = advanceNightStep,
            contentOwnsFullScreen = clocktowerNightUsesFullScreenHostSurface(
                isRealAction = currentStep.isRealAction,
                action = currentStep.action,
                displayKind = currentStep.displayKind,
            ),
        ) {
'''
host = replace_exact(host, host_old, host_new, "HostScreen night full-screen ownership")
paths["host"].write_text(host, encoding="utf-8", newline="\n")

# The common night square-table root becomes inline Activity-window content and owns exactly one
# shared bottom navigation through ClocktowerHostFullScreenScaffold.
night_action = paths["night_action"].read_text(encoding="utf-8")
for import_line in (
    "import androidx.compose.ui.window.Dialog\n",
    "import androidx.compose.ui.window.DialogProperties\n",
):
    if night_action.count(import_line) != 1:
        raise SystemExit(f"Expected one Dialog import: {import_line.strip()}")
    night_action = night_action.replace(import_line, "", 1)

bottom_start = night_action.find("/**\n * Shared screen-bottom navigation for square-table night actions.")
bottom_end_marker = "@Composable\ninternal fun ClocktowerSingleTargetSquareTableDialog("
bottom_end = night_action.find(bottom_end_marker)
if bottom_start < 0 or bottom_end < 0 or bottom_end <= bottom_start:
    raise SystemExit("Could not locate legacy ClocktowerNightBottomActionBar block")
night_action = night_action[:bottom_start] + night_action[bottom_end:]

core_marker = "@Composable\ninternal fun ClocktowerNightActionSquareTableDialog("
core_start = night_action.find(core_marker)
if core_start < 0 or night_action.find(core_marker, core_start + 1) >= 0:
    raise SystemExit("Expected exactly one night square-table root")
old_core = night_action[core_start:]
if "Dialog(" not in old_core or "ClocktowerNightBottomActionBar(" not in old_core:
    raise SystemExit("Legacy night square-table root shape drifted")
new_core = '''@Composable
internal fun ClocktowerNightActionSquareTableDialog(
    seats: List<HostSeatPresentation>,
    enabled: Boolean,
    language: String,
    seatPresentation: (Int) -> ClocktowerNightActionSeatPresentation,
    onSeatSelected: (Int) -> Unit,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    centerContent: @Composable () -> Unit,
) {
    ClocktowerHostFullScreenScaffold(
        previousLabel = if (language == "en") "← Previous" else "← 上一步",
        hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
        nextLabel = if (language == "en") "Next →" else "下一步 →",
        previousEnabled = canGoPrevious,
        nextEnabled = nextEnabled,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        ClocktowerSquareTableSeatSurface(
            seats = seats.map { seat ->
                val content = hostSeatContentPresentation(seat, language)
                val presentation = seatPresentation(seat.seatId.number)
                ClocktowerSquareTableSeatUiModel(
                    seatId = seat.seatId.renderKey(),
                    seatNumber = seat.seatId.number,
                    label = content.primaryLabel,
                    detailLabels = content.detailLabels,
                    state = presentation.targetState,
                    isCurrentActor = presentation.isCurrentActor,
                )
            },
            modifier = Modifier.fillMaxSize(),
            interactionMode = if (enabled) {
                ClocktowerSquareTableInteractionMode.Selectable
            } else {
                ClocktowerSquareTableInteractionMode.ReadOnly
            },
            onSeatClick = { renderKey ->
                seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }
                    ?.seatId
                    ?.number
                    ?.let(onSeatSelected)
            },
        ) {
            centerContent()
        }
    }
}
'''
night_action = night_action[:core_start] + new_core
if "ClocktowerNightBottomActionBar(" in night_action:
    raise SystemExit("Legacy night bottom bar survived")
if "Dialog(" in night_action or "DialogProperties(" in night_action:
    raise SystemExit("Night square-table source still owns a Dialog window")
paths["night_action"].write_text(night_action, encoding="utf-8", newline="\n")

# NightStep loses duplicate action ownership and legacy card chrome whenever the structured step owns
# the full-screen workspace.
night_step = paths["night_step"].read_text(encoding="utf-8")
helper_anchor = '''        step.displayKind != ClocktowerDisplayKind.None -> if (language == "en") "Show the information to the player." else "展示信息给玩家。"
        else -> step.explanation
    }
    Card(
'''
helper_replacement = '''        step.displayKind != ClocktowerDisplayKind.None -> if (language == "en") "Show the information to the player." else "展示信息给玩家。"
        else -> step.explanation
    }
    val ownsFullScreenSurface = clocktowerNightUsesFullScreenHostSurface(
        isRealAction = step.isRealAction,
        action = step.action,
        displayKind = step.displayKind,
    )
    Card(
'''
night_step = replace_exact(night_step, helper_anchor, helper_replacement, "NightStep full-screen policy")

card_old = '''    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (step.actor != null) {
                    if (language == "en") "CURRENT PLAYER" else "当前玩家"
                } else {
                    if (language == "en") "CURRENT STEP" else "当前步骤"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                command.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Black,
            )

        val nightActionSeats = cards.mapIndexed { index, card ->
'''
card_new = '''    Card(
        modifier = if (ownsFullScreenSurface) Modifier.fillMaxSize() else Modifier,
        shape = RoundedCornerShape(if (ownsFullScreenSurface) 0.dp else 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = if (ownsFullScreenSurface) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            },
            verticalArrangement = if (ownsFullScreenSurface) Arrangement.Top else Arrangement.spacedBy(14.dp),
        ) {
            if (!ownsFullScreenSurface) {
                Text(
                    if (step.actor != null) {
                        if (language == "en") "CURRENT PLAYER" else "当前玩家"
                    } else {
                        if (language == "en") "CURRENT STEP" else "当前步骤"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    command.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Black,
                )
            }

        val nightActionSeats = cards.mapIndexed { index, card ->
'''
night_step = replace_exact(night_step, card_old, card_new, "NightStep card chrome")

action_owner_old = '''        val actionOwnsSquareTable = step.action in setOf(
            ClocktowerNightAction.RedHerring,
            ClocktowerNightAction.Poison,
            ClocktowerNightAction.ButlerMaster,
            ClocktowerNightAction.MonkProtect,
            ClocktowerNightAction.DemonKill,
            ClocktowerNightAction.Ravenkeeper,
            ClocktowerNightAction.FortuneTeller,
            ClocktowerNightAction.Chambermaid,
            ClocktowerNightAction.MayorRedirect,
            ClocktowerNightAction.DemonSuccessor,
        )
'''
action_owner_new = '''        val actionOwnsSquareTable = clocktowerNightActionOwnsSquareTable(step.action)
'''
night_step = replace_exact(night_step, action_owner_old, action_owner_new, "duplicate night action ownership")

footer_marker = '''            if (showNavigationActions) {
                HostBottomActionBar(
'''
footer_start = night_step.find(footer_marker)
if footer_start < 0 or night_step.find(footer_marker, footer_start + 1) >= 0:
    raise SystemExit("Expected exactly one legacy NightStep footer")
legacy_footer = night_step[footer_start:]
if not legacy_footer.endswith("        }\n    }\n}"):
    raise SystemExit("NightStep footer/end shape drifted")
footer_body = legacy_footer[:-len("        }\n    }\n}")]
footer_body = footer_body.replace(
    "            if (showNavigationActions) {\n",
    "            if (!ownsFullScreenSurface) {\n                if (showNavigationActions) {\n",
    1,
)
footer_body = footer_body.replace(
    "            }\n\n            Surface(\n",
    "                }\n\n                Surface(\n",
    1,
)
# Indent the legacy step-note block one level so it remains inside the non-full-screen guard.
note_start = footer_body.find("                Surface(\n")
if note_start < 0:
    raise SystemExit("Could not locate NightStep note after footer wrapping")
prefix = footer_body[:note_start]
note = footer_body[note_start:]
note_lines = note.splitlines(True)
note = "".join(("    " + line if line.strip() else line) for line in note_lines)
new_footer = prefix + note + "            }\n        }\n    }\n}\n"
night_step = night_step[:footer_start] + new_footer

if night_step.count("clocktowerNightActionOwnsSquareTable(step.action)") != 1:
    raise SystemExit("NightStep did not consolidate action ownership")
if night_step.count("ownsFullScreenSurface") < 4:
    raise SystemExit("NightStep full-screen ownership was not wired through chrome/footer")
paths["night_step"].write_text(night_step, encoding="utf-8", newline="\n")

paths["shared"].write_text(shared, encoding="utf-8", newline="\n")
paths["ownership"].write_text(ownership, encoding="utf-8", newline="\n")

# Final structural assertions before Gradle sees the patch.
assert "ClocktowerHostFullScreenScaffold(" in paths["day"].read_text(encoding="utf-8")
assert "ClocktowerHostFullScreenScaffold(" in paths["night_screen"].read_text(encoding="utf-8")
assert "ClocktowerHostFullScreenScaffold(" in paths["night_action"].read_text(encoding="utf-8")
assert "Dialog(" not in paths["night_action"].read_text(encoding="utf-8")
assert "ClocktowerNightBottomActionBar(" not in paths["night_action"].read_text(encoding="utf-8")
