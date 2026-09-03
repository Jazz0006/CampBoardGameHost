from pathlib import Path

ROOT = Path('.')


def replace_once(path: str, old: str, new: str) -> None:
    p = ROOT / path
    text = p.read_text()
    if text.count(old) != 1:
        raise SystemExit(f'{path}: expected exactly one anchor, found {text.count(old)}')
    p.write_text(text.replace(old, new, 1))


def write_new(path: str, content: str) -> None:
    p = ROOT / path
    if p.exists():
        raise SystemExit(f'{path}: new file already exists')
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content)


write_new(
    'app/src/main/java/com/codex/campboardgamehost/ClocktowerDayNominationGesture.kt',
    '''package com.codex.campboardgamehost

internal data class HostTableDirectionalGesturePolicy(
    val sourceSeatIds: Set<ClocktowerSeatId>,
    val targetSeatIds: Set<ClocktowerSeatId>,
)

internal data class HostTableDirectionalLink(
    val sourceSeatId: ClocktowerSeatId,
    val targetSeatId: ClocktowerSeatId,
) {
    init {
        require(sourceSeatId != targetSeatId) {
            "Directional host-table link requires two different seats"
        }
    }
}

internal fun clocktowerDayNominationGesturePolicy(
    tableState: ClocktowerDayOverviewTableState,
): HostTableDirectionalGesturePolicy {
    val aliveSeatIds = tableState.seats
        .asSequence()
        .filter(HostSeatPresentation::isAlive)
        .map(HostSeatPresentation::seatId)
        .toSet()
    return HostTableDirectionalGesturePolicy(
        sourceSeatIds = aliveSeatIds,
        targetSeatIds = aliveSeatIds,
    )
}

/**
 * Resolves against the same already-computed spatial ring used for rendering.
 *
 * The nearest physical slot wins first. If that nearest slot is self or is not a legal target, the
 * gesture fails closed instead of silently snapping to a farther legal player.
 */
internal fun resolveHostTableDirectionalTargetRingIndex(
    layout: HostTableLayout,
    pointerX: Float,
    pointerY: Float,
    sourceRingIndex: Int,
    eligibleTargetRingIndices: Set<Int>,
): Int? {
    require(sourceRingIndex in layout.slots.indices) {
        "Directional gesture source ring index is outside the rendered Host-table ring"
    }
    require(eligibleTargetRingIndices.all { it in layout.slots.indices }) {
        "Directional gesture target ring indices must belong to the rendered Host-table ring"
    }
    if (eligibleTargetRingIndices.isEmpty()) return null

    val nearestRingIndex = nearestHostTableRingIndex(
        layout = layout,
        pointerX = pointerX,
        pointerY = pointerY,
    )
    return nearestRingIndex.takeIf { candidate ->
        candidate != sourceRingIndex && candidate in eligibleTargetRingIndices
    }
}
''',
)

host_ui = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt'
replace_once(
    host_ui,
    '''    dragEnabled: Boolean = false,\n    neutralSelectionChrome: Boolean = false,\n    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },\n    onSeatDragCommit: (ClocktowerSeatId, Int) -> Unit = { _, _ -> },\n    centerContent: @Composable BoxScope.() -> Unit = {},\n''',
    '''    dragEnabled: Boolean = false,\n    neutralSelectionChrome: Boolean = false,\n    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },\n    onSeatDragCommit: (ClocktowerSeatId, Int) -> Unit = { _, _ -> },\n    directionalGesture: HostTableDirectionalGesturePolicy? = null,\n    directionalLink: HostTableDirectionalLink? = null,\n    onDirectionalGestureCommit: (ClocktowerSeatId, ClocktowerSeatId) -> Unit = { _, _ -> },\n    centerContent: @Composable BoxScope.() -> Unit = {},\n''',
)
replace_once(
    host_ui,
    '''        val renderSeats = frames.map { frame ->\n            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n            )\n        }\n\n        ClocktowerSquareTableSeatSurface(\n''',
    '''        val renderSeats = frames.map { frame ->\n            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n            )\n        }\n        val knownSeatIds = frames.map { frame -> frame.seat.seatId }.toSet()\n        directionalGesture?.let { gesture ->\n            require((gesture.sourceSeatIds + gesture.targetSeatIds).all { it in knownSeatIds }) {\n                "Directional Host-table gesture references an unknown physical seat"\n            }\n        }\n        directionalLink?.let { link ->\n            require(link.sourceSeatId in knownSeatIds && link.targetSeatId in knownSeatIds) {\n                "Directional Host-table link references an unknown physical seat"\n            }\n        }\n\n        ClocktowerSquareTableSeatSurface(\n''',
)
replace_once(
    host_ui,
    '''            onSeatDragCommit = { renderKey, targetRingIndex ->\n                seatIdsByRenderKey[renderKey]?.let { seatId ->\n                    onSeatDragCommit(seatId, targetRingIndex)\n                }\n            },\n            centerContent = centerContent,\n''',
    '''            onSeatDragCommit = { renderKey, targetRingIndex ->\n                seatIdsByRenderKey[renderKey]?.let { seatId ->\n                    onSeatDragCommit(seatId, targetRingIndex)\n                }\n            },\n            directionalGestureSourceSeatIds = directionalGesture\n                ?.sourceSeatIds\n                ?.mapTo(mutableSetOf())(ClocktowerSeatId::renderKey)\n                .orEmpty(),\n            directionalGestureTargetSeatIds = directionalGesture\n                ?.targetSeatIds\n                ?.mapTo(mutableSetOf())(ClocktowerSeatId::renderKey)\n                .orEmpty(),\n            directionalLink = directionalLink?.let { link ->\n                link.sourceSeatId.renderKey() to link.targetSeatId.renderKey()\n            },\n            onDirectionalGestureCommit = { sourceRenderKey, targetRenderKey ->\n                val sourceSeatId = seatIdsByRenderKey[sourceRenderKey]\n                val targetSeatId = seatIdsByRenderKey[targetRenderKey]\n                if (sourceSeatId != null && targetSeatId != null) {\n                    onDirectionalGestureCommit(sourceSeatId, targetSeatId)\n                }\n            },\n            centerContent = centerContent,\n''',
)

square_ui = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt'
replace_once(
    square_ui,
    '''import androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.clickable\n''',
    '''import androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.foundation.clickable\n''',
)
replace_once(
    square_ui,
    '''import kotlin.math.abs\n''',
    '''import kotlin.math.abs\nimport kotlin.math.atan2\nimport kotlin.math.cos\nimport kotlin.math.sin\n''',
)
replace_once(
    square_ui,
    '''    dragEnabled: Boolean = false,\n    onSeatDragCommit: (String, Int) -> Unit = { _, _ -> },\n    centerContent: @Composable BoxScope.() -> Unit = {},\n''',
    '''    dragEnabled: Boolean = false,\n    onSeatDragCommit: (String, Int) -> Unit = { _, _ -> },\n    directionalGestureSourceSeatIds: Set<String> = emptySet(),\n    directionalGestureTargetSeatIds: Set<String> = emptySet(),\n    directionalLink: Pair<String, String>? = null,\n    onDirectionalGestureCommit: (String, String) -> Unit = { _, _ -> },\n    centerContent: @Composable BoxScope.() -> Unit = {},\n''',
)
replace_once(
    square_ui,
    '''        require(abs(resolvedLayout.constraints.availableHeight - availableHeight) < 0.01f) {\n            "Provided square-table layout height must match rendering constraints"\n        }\n\n        var draggedMotionKey by remember { mutableStateOf<String?>(null) }\n''',
    '''        require(abs(resolvedLayout.constraints.availableHeight - availableHeight) < 0.01f) {\n            "Provided square-table layout height must match rendering constraints"\n        }\n        require(!dragEnabled || directionalGestureSourceSeatIds.isEmpty()) {\n            "Reorder drag and directional gesture cannot own the same square-table surface"\n        }\n        val knownSeatIds = seats.map { seat -> seat.seatId }.toSet()\n        require((directionalGestureSourceSeatIds + directionalGestureTargetSeatIds).all { it in knownSeatIds }) {\n            "Directional square-table gesture references an unknown seat"\n        }\n        directionalLink?.let { (sourceSeatId, targetSeatId) ->\n            require(sourceSeatId in knownSeatIds && targetSeatId in knownSeatIds && sourceSeatId != targetSeatId) {\n                "Directional square-table link must reference two distinct known seats"\n            }\n        }\n\n        var draggedMotionKey by remember { mutableStateOf<String?>(null) }\n''',
)
replace_once(
    square_ui,
    '''        var dragPointerPosition by remember { mutableStateOf(Offset.Zero) }\n\n        val dragSourceIndex = draggedMotionKey?.let { motionKey ->\n''',
    '''        var dragPointerPosition by remember { mutableStateOf(Offset.Zero) }\n        var directionalDragSourceSeatId by remember { mutableStateOf<String?>(null) }\n        var directionalDragTargetRingIndex by remember { mutableStateOf<Int?>(null) }\n        var directionalDragPointerPosition by remember { mutableStateOf(Offset.Zero) }\n\n        val dragSourceIndex = draggedMotionKey?.let { motionKey ->\n''',
)
replace_once(
    square_ui,
    '''        val tabletopGeometry = remember(resolvedLayout.constraints) {\n            hostTableTabletopGeometry(resolvedLayout.constraints)\n        }\n\n        Surface(\n''',
    '''        val tabletopGeometry = remember(resolvedLayout.constraints) {\n            hostTableTabletopGeometry(resolvedLayout.constraints)\n        }\n        val directionalTargetRingIndices = directionalGestureTargetSeatIds.mapNotNullTo(mutableSetOf()) { seatId ->\n            seats.indexOfFirst { seat -> seat.seatId == seatId }.takeIf { it >= 0 }\n        }\n        val activeDirectionalSourceRingIndex = directionalDragSourceSeatId?.let { sourceSeatId ->\n            seats.indexOfFirst { seat -> seat.seatId == sourceSeatId }.takeIf { it >= 0 }\n        }\n        val persistentDirectionalSourceRingIndex = directionalLink?.first?.let { sourceSeatId ->\n            seats.indexOfFirst { seat -> seat.seatId == sourceSeatId }.takeIf { it >= 0 }\n        }\n        val persistentDirectionalTargetRingIndex = directionalLink?.second?.let { targetSeatId ->\n            seats.indexOfFirst { seat -> seat.seatId == targetSeatId }.takeIf { it >= 0 }\n        }\n        val arrowStartDp = activeDirectionalSourceRingIndex\n            ?.let { ringIndex -> resolvedLayout.slots[ringIndex] }\n            ?.let { slot -> Offset(slot.centerX, slot.centerY) }\n            ?: persistentDirectionalSourceRingIndex\n                ?.let { ringIndex -> resolvedLayout.slots[ringIndex] }\n                ?.let { slot -> Offset(slot.centerX, slot.centerY) }\n        val arrowEndDp = if (activeDirectionalSourceRingIndex != null) {\n            directionalDragPointerPosition\n        } else {\n            persistentDirectionalTargetRingIndex\n                ?.let { ringIndex -> resolvedLayout.slots[ringIndex] }\n                ?.let { slot -> Offset(slot.centerX, slot.centerY) }\n        }\n        val arrowIsValid = if (activeDirectionalSourceRingIndex != null) {\n            directionalDragTargetRingIndex != null\n        } else {\n            directionalLink != null\n        }\n        val arrowColor = if (arrowIsValid) {\n            MaterialTheme.colorScheme.primary\n        } else {\n            MaterialTheme.colorScheme.outline\n        }\n        val densityScale = density.density\n\n        Surface(\n''',
)
replace_once(
    square_ui,
    '''        ) {\n            Box(modifier = Modifier.fillMaxSize())\n        }\n\n        placements.forEach { placement ->\n''',
    '''        ) {\n            Box(modifier = Modifier.fillMaxSize())\n        }\n\n        if (arrowStartDp != null && arrowEndDp != null) {\n            Canvas(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .zIndex(0.5f),\n            ) {\n                val start = Offset(arrowStartDp.x * densityScale, arrowStartDp.y * densityScale)\n                val end = Offset(arrowEndDp.x * densityScale, arrowEndDp.y * densityScale)\n                val deltaX = end.x - start.x\n                val deltaY = end.y - start.y\n                if (deltaX * deltaX + deltaY * deltaY > 4f) {\n                    val strokeWidth = 4.dp.toPx()\n                    drawLine(\n                        color = arrowColor,\n                        start = start,\n                        end = end,\n                        strokeWidth = strokeWidth,\n                    )\n                    val angle = atan2(deltaY, deltaX)\n                    val headLength = 12.dp.toPx()\n                    val headSpread = 0.55f\n                    val firstHead = Offset(\n                        x = end.x - headLength * cos(angle - headSpread),\n                        y = end.y - headLength * sin(angle - headSpread),\n                    )\n                    val secondHead = Offset(\n                        x = end.x - headLength * cos(angle + headSpread),\n                        y = end.y - headLength * sin(angle + headSpread),\n                    )\n                    drawLine(arrowColor, end, firstHead, strokeWidth)\n                    drawLine(arrowColor, end, secondHead, strokeWidth)\n                }\n            }\n        }\n\n        placements.forEach { placement ->\n''',
)
replace_once(
    square_ui,
    '''                } else {\n                    Modifier\n                }\n\n                ClocktowerSquareTableSeat(\n''',
    '''                } else {\n                    Modifier\n                }\n                val directionalDragModifier = if (\n                    !dragEnabled &&\n                    placement.seat.seatId in directionalGestureSourceSeatIds &&\n                    directionalGestureTargetSeatIds.any { targetSeatId -> targetSeatId != placement.seat.seatId }\n                ) {\n                    Modifier.pointerInput(\n                        placement.seat.seatId,\n                        resolvedLayout,\n                        directionalGestureSourceSeatIds,\n                        directionalGestureTargetSeatIds,\n                        densityScale,\n                    ) {\n                        detectDragGesturesAfterLongPress(\n                            onDragStart = {\n                                val sourceRingIndex = seats.indexOfFirst { seat ->\n                                    seat.seatId == placement.seat.seatId\n                                }\n                                if (sourceRingIndex >= 0) {\n                                    val sourceSlot = resolvedLayout.slots[sourceRingIndex]\n                                    directionalDragSourceSeatId = placement.seat.seatId\n                                    directionalDragTargetRingIndex = null\n                                    directionalDragPointerPosition = Offset(sourceSlot.centerX, sourceSlot.centerY)\n                                }\n                            },\n                            onDrag = { change, dragAmount ->\n                                change.consume()\n                                val sourceSeatId = directionalDragSourceSeatId\n                                val sourceRingIndex = sourceSeatId?.let { id ->\n                                    seats.indexOfFirst { seat -> seat.seatId == id }.takeIf { it >= 0 }\n                                }\n                                if (sourceRingIndex != null) {\n                                    val nextPointer = directionalDragPointerPosition + Offset(\n                                        x = dragAmount.x / densityScale,\n                                        y = dragAmount.y / densityScale,\n                                    )\n                                    directionalDragPointerPosition = nextPointer\n                                    directionalDragTargetRingIndex = resolveHostTableDirectionalTargetRingIndex(\n                                        layout = resolvedLayout,\n                                        pointerX = nextPointer.x,\n                                        pointerY = nextPointer.y,\n                                        sourceRingIndex = sourceRingIndex,\n                                        eligibleTargetRingIndices = directionalTargetRingIndices,\n                                    )\n                                }\n                            },\n                            onDragEnd = {\n                                val sourceSeatId = directionalDragSourceSeatId\n                                val targetSeatId = directionalDragTargetRingIndex\n                                    ?.let { ringIndex -> seats.getOrNull(ringIndex)?.seatId }\n                                directionalDragSourceSeatId = null\n                                directionalDragTargetRingIndex = null\n                                directionalDragPointerPosition = Offset.Zero\n                                if (sourceSeatId != null && targetSeatId != null && sourceSeatId != targetSeatId) {\n                                    onDirectionalGestureCommit(sourceSeatId, targetSeatId)\n                                }\n                            },\n                            onDragCancel = {\n                                directionalDragSourceSeatId = null\n                                directionalDragTargetRingIndex = null\n                                directionalDragPointerPosition = Offset.Zero\n                            },\n                        )\n                    }\n                } else {\n                    Modifier\n                }\n\n                ClocktowerSquareTableSeat(\n''',
)
replace_once(
    square_ui,
    '''                        .zIndex(if (isDragged) 1f else 0f)\n                        .then(dragModifier),\n''',
    '''                        .zIndex(if (isDragged) 2f else 1f)\n                        .then(dragModifier)\n                        .then(directionalDragModifier),\n''',
)
replace_once(
    square_ui,
    '''                .height(resolvedLayout.constraints.centerWorkspaceHeight.dp),\n            shape = RoundedCornerShape(18.dp),\n''',
    '''                .height(resolvedLayout.constraints.centerWorkspaceHeight.dp)\n                .zIndex(0.25f),\n            shape = RoundedCornerShape(18.dp),\n''',
)

day_overview = 'app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayOverviewTableUi.kt'
replace_once(
    day_overview,
    '''import androidx.compose.runtime.Composable\n''',
    '''import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember\n''',
)
replace_once(
    day_overview,
    '''    diagnosticContent: (@Composable () -> Unit)? = null,\n    onStartNomination: () -> Unit,\n    onOpenSlayer: () -> Unit,\n''',
    '''    diagnosticContent: (@Composable () -> Unit)? = null,\n    onNominationGesture: (ClocktowerSeatId, ClocktowerSeatId) -> Unit,\n    onOpenSlayer: () -> Unit,\n''',
)
replace_once(
    day_overview,
    '''    fun text(zh: String, en: String): String = if (language == "en") en else zh\n\n    ClocktowerDarkTheme {\n''',
    '''    fun text(zh: String, en: String): String = if (language == "en") en else zh\n    val nominationGesture = remember(tableState) {\n        clocktowerDayNominationGesturePolicy(tableState)\n    }\n\n    ClocktowerDarkTheme {\n''',
)
replace_once(
    day_overview,
    '''                interaction = tableState.interaction,\n                centerContent = {\n''',
    '''                interaction = tableState.interaction,\n                directionalGesture = nominationGesture.takeIf { actionsEnabled },\n                onDirectionalGestureCommit = onNominationGesture,\n                centerContent = {\n''',
)
replace_once(
    day_overview,
    '''                        diagnosticContent = diagnosticContent,\n                        text = ::text,\n                        onStartNomination = onStartNomination,\n                        onOpenSlayer = onOpenSlayer,\n''',
    '''                        diagnosticContent = diagnosticContent,\n                        text = ::text,\n                        onOpenSlayer = onOpenSlayer,\n''',
)
replace_once(
    day_overview,
    '''    diagnosticContent: (@Composable () -> Unit)?,\n    text: (String, String) -> String,\n    onStartNomination: () -> Unit,\n    onOpenSlayer: () -> Unit,\n''',
    '''    diagnosticContent: (@Composable () -> Unit)?,\n    text: (String, String) -> String,\n    onOpenSlayer: () -> Unit,\n''',
)
replace_once(
    day_overview,
    '''            text = text(\n                "自由讨论 · 有人提名时进入提名流程",\n                "Open discussion · start nominations when someone nominates",\n            ),\n''',
    '''            text = text(\n                "自由讨论 · 长按一名玩家并拖向另一名玩家以发起提名",\n                "Open discussion · long-press a player and drag to another player to nominate",\n            ),\n''',
)
replace_once(
    day_overview,
    '''        Button(\n            onClick = onStartNomination,\n            enabled = actionsEnabled,\n            modifier = Modifier\n                .fillMaxWidth()\n                .heightIn(min = 44.dp),\n            shape = RoundedCornerShape(12.dp),\n        ) {\n            Text(text("开始提名", "Start nomination"))\n        }\n''',
    '',
)

write_new(
    'app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerPendingNominationTableUi.kt',
    '''package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Persistent-table confirmation surface for a nomination already chosen by directional gesture. */
@Composable
internal fun ClocktowerPendingNominationTableScreen(
    round: Int,
    cards: List<PlayerCard>,
    tableState: ClocktowerDayOverviewTableState,
    executionThreshold: Int,
    nominatorName: String?,
    nomineeName: String?,
    specialNotice: String?,
    specialNoticeIsDanger: Boolean,
    continueLabel: String,
    actionsEnabled: Boolean,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    specialContent: @Composable ColumnScope.() -> Unit = {},
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val nominatorSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nominatorName }
    val nomineeSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nomineeName }
    val pendingLink = if (
        nominatorSeat != null && nomineeSeat != null && nominatorSeat.seatId != nomineeSeat.seatId
    ) {
        HostTableDirectionalLink(nominatorSeat.seatId, nomineeSeat.seatId)
    } else {
        null
    }

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            HostTableShell(
                seats = tableState.seats,
                modifier = Modifier.fillMaxSize(),
                interaction = tableState.interaction,
                directionalLink = pendingLink,
                centerContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = text("第 $round 天 · 提名", "Day $round · Nomination"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (pendingLink != null) {
                                "${playerSeatLabel(cards, nominatorName)}  →  ${playerSeatLabel(cards, nomineeName)}"
                            } else {
                                text("提名信息无效，请取消后重试", "Invalid nomination; cancel and try again")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text(
                                "处决门槛：$executionThreshold 票",
                                "Execution threshold: $executionThreshold",
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (specialNotice != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (specialNoticeIsDanger) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text("角色能力检查", "ABILITY CHECK"),
                                        color = if (specialNoticeIsDanger) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                    )
                                    Text(
                                        specialNotice,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    specialContent()
                                }
                            }
                        }
                        Button(
                            onClick = onContinue,
                            enabled = actionsEnabled && pendingLink != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(continueLabel, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text("取消提名", "Cancel nomination"))
                        }
                    }
                },
            )
        }
    }
}
''',
)

host_screen = 'app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt'
replace_once(
    host_screen,
    '''        ClocktowerDayOverviewScreen(\n            round = round,\n            tableState = clocktowerDayOverviewTableState(\n                cards.toClocktowerGameState(\n                    script = script,\n                    seed = gameSeed,\n                    poisonedPlayerName = poisonTarget,\n                ),\n            ),\n''',
    '''        val dayTableState = clocktowerDayOverviewTableState(\n            cards.toClocktowerGameState(\n                script = script,\n                seed = gameSeed,\n                poisonedPlayerName = poisonTarget,\n            ),\n        )\n        ClocktowerDayOverviewScreen(\n            round = round,\n            tableState = dayTableState,\n''',
)
replace_once(
    host_screen,
    '''            diagnosticContent = null,\n            onStartNomination = {\n                nominatorName = null\n                nomineeName = null\n                dayMode = ClocktowerDayMode.Nomination\n            },\n            onOpenSlayer = {\n''',
    '''            diagnosticContent = null,\n            onNominationGesture = { sourceSeatId, targetSeatId ->\n                val sourceName = dayTableState.seats\n                    .firstOrNull { seat -> seat.seatId == sourceSeatId && seat.isAlive }\n                    ?.playerName\n                val targetName = dayTableState.seats\n                    .firstOrNull { seat -> seat.seatId == targetSeatId && seat.isAlive }\n                    ?.playerName\n                if (sourceName != null && targetName != null && sourceName != targetName) {\n                    nominatorName = sourceName\n                    nomineeName = targetName\n                    currentVoteCount = 0\n                    dayMode = ClocktowerDayMode.Nomination\n                }\n            },\n            onOpenSlayer = {\n''',
)
replace_once(
    host_screen,
    '''                else -> text("确认提名，进入投票", "Confirm nomination and vote")\n            },\n            actionsEnabled = gameOutcome == null,\n            onSelectNominator = { nominatorName = if (nominatorName == it) null else it },\n            onSelectNominee = { nomineeName = if (nomineeName == it) null else it },\n            onContinue = {\n''',
    '''                else -> text("开始投票", "Start voting")\n            },\n            actionsEnabled = gameOutcome == null,\n            onContinue = {\n''',
)
replace_once(
    host_screen,
    '''        ClocktowerNominationScreen(\n            round = round,\n            cards = cards,\n            aliveCards = publicAliveCards,\n            executionThreshold = executionThreshold,\n''',
    '''        ClocktowerPendingNominationTableScreen(\n            round = round,\n            cards = cards,\n            tableState = clocktowerDayOverviewTableState(\n                cards.toClocktowerGameState(\n                    script = script,\n                    seed = gameSeed,\n                    poisonedPlayerName = poisonTarget,\n                ),\n            ),\n            executionThreshold = executionThreshold,\n''',
)
