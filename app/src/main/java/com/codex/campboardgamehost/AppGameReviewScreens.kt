package com.codex.campboardgamehost

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1

internal enum class HostToolTab {
    Roles,
    Records,
    History,
    Settings,
}
@Composable
internal fun ResultsDialog(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    outcome: GameOutcome?,
    onDismiss: () -> Unit,
    onReview: () -> Unit,
    onNewGame: () -> Unit,
) {
    val defaultTitle = if (gameKind == GameKind.Clocktower) {
        stringResource(R.string.clocktower_role_results)
    } else {
        stringResource(R.string.identity_results)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(outcome?.title ?: defaultTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (outcome != null) {
                    Text(outcome.summary, fontWeight = FontWeight.SemiBold)
                    Text(outcome.reason, color = Color(0xFF5C6A63))
                    HorizontalDivider()
                }
                cards.forEach { card ->
                    if (gameKind == GameKind.Clocktower) {
                        val roleText = if (gameKind == GameKind.Clocktower && card.clocktowerShownAsDifferentRole() && card.clocktowerShownRole != null) {
                            stringResource(
                                R.string.clocktower_result_role_format,
                                card.hostRoleLabel(LocalContext.current, GameKind.Clocktower),
                                card.clocktowerShownRole.nameFor(LocalContext.current.resources.configuration.locales[0].language),
                            )
                        } else {
                            card.hostRoleLabel(LocalContext.current, gameKind)
                        }
                        Text(stringResource(R.string.result_role_format, card.name, roleText))
                    } else {
                        Text(stringResource(R.string.result_card_format, card.name, stringResource(card.role.labelResId()), card.word))
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onReview) {
                    Text(if (LocalContext.current.resources.configuration.locales[0].language == "en") "Review log" else "复盘记录")
                }
                TextButton(onClick = onNewGame) {
                    Text(if (LocalContext.current.resources.configuration.locales[0].language == "en") "Prepare next game" else "准备下一局")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        },
    )
}

@Composable
internal fun NewGameConfirmationDialog(
    gameKind: GameKind,
    onDismiss: () -> Unit,
    onManagePlayers: () -> Unit,
    onQuickRestart: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val gameName = when (gameKind) {
        GameKind.Undercover -> stringResource(R.string.game_who_is_undercover)
        GameKind.Clocktower -> stringResource(R.string.game_clocktower)
    }
    ClocktowerDarkTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text("结束当前游戏？", "End this game?")) },
            text = {
                Text(
                    text(
                        "“$gameName”的角色身份和操作记录会保存到历史复盘。建议先返回玩家管理，确认本局参与者后再发牌。",
                        "Roles and game records for $gameName will be saved to history. Review the player list before dealing the next game.",
                    ),
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onManagePlayers,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("保存并调整玩家", "Save & manage players"), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onQuickRestart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("原班人马快速再来一局", "Quick restart with same players"))
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("继续当前游戏", "Continue game"))
                    }
                }
            },
            dismissButton = {},
        )
    }
}

@Composable
internal fun HostGameToolsScreen(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    events: List<ClocktowerEvent>,
    history: List<ArchivedGameReview>,
    initialTab: HostToolTab,
    clocktowerPlayerContextBySeat: Map<Int, StorytellerPlayerContextInputV1> = emptyMap(),
    clocktowerClaimableRoles: List<ClocktowerRole> = emptyList(),
    onClocktowerPlayerContextChange: (Int, StorytellerPlayerContextInputV1) -> Unit = { _, _ -> },
    settingsContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onNewGame: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var selectedHistoryId by remember { mutableStateOf<Long?>(null) }
    val selectedHistory = history.firstOrNull { it.id == selectedHistoryId }

    BackHandler(onBack = onDismiss)

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text("私密 · 仅主持人", "PRIVATE · HOST ONLY"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text("主持工具", "Host tools"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text("关闭", "Close"))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HostToolTab.entries.forEach { tab ->
                        val label = when (tab) {
                            HostToolTab.Roles -> text("角色身份", "Roles")
                            HostToolTab.Records -> text("操作记录", "Game log")
                            HostToolTab.History -> text("历史复盘 ${history.size}", "History ${history.size}")
                            HostToolTab.Settings -> text("设置", "Settings")
                        }
                        if (selectedTab == tab) {
                            Button(
                                onClick = {
                                    selectedTab = tab
                                    selectedHistoryId = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            ) {
                                Text(label, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    selectedTab = tab
                                    selectedHistoryId = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            ) {
                                Text(label, maxLines = 1)
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

                when (selectedTab) {
                    HostToolTab.Roles -> HostRolesList(
                        gameKind = gameKind,
                        cards = cards,
                        clocktowerPlayerContextBySeat = clocktowerPlayerContextBySeat,
                        clocktowerClaimableRoles = clocktowerClaimableRoles,
                        onClocktowerPlayerContextChange = onClocktowerPlayerContextChange,
                        modifier = Modifier.weight(1f),
                    )
                    HostToolTab.Records -> HostRecordsList(
                        gameKind = gameKind,
                        records = records,
                        events = events,
                        modifier = Modifier.weight(1f),
                    )
                    HostToolTab.History -> {
                        if (selectedHistory == null) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (history.isEmpty()) {
                                    item {
                                        Text(
                                            text("结束一局后，角色和记录会保存在这里。", "Finished games will be saved here."),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                items(history, key = { it.id }) { review ->
                                    ArchivedGameCard(
                                        review = review,
                                        onClick = { selectedHistoryId = review.id },
                                    )
                                }
                            }
                        } else {
                            ArchivedGameReviewContent(
                                review = selectedHistory,
                                onBack = { selectedHistoryId = null },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    HostToolTab.Settings -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            settingsContent()
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
                ) {
                    TextButton(
                        onClick = onNewGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text("结束当前游戏并开始新一局", "End current game and start a new one"),
                            color = Color(0xFFC9574A),
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostRolesList(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    clocktowerPlayerContextBySeat: Map<Int, StorytellerPlayerContextInputV1>,
    clocktowerClaimableRoles: List<ClocktowerRole>,
    onClocktowerPlayerContextChange: (Int, StorytellerPlayerContextInputV1) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val isEnglish = language == "en"
    fun text(zh: String, en: String): String = if (isEnglish) en else zh
    var editingSeat by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text("请确认屏幕仅对主持人可见。", "Make sure only the host can see this screen."),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        itemsIndexed(cards) { index, card ->
            val seat = index + 1
            val role = card.hostRoleLabel(context, gameKind)
            val shown = if (
                gameKind == GameKind.Clocktower &&
                card.clocktowerShownAsDifferentRole() &&
                card.clocktowerShownRole != null
            ) {
                text(
                    " · 展示为 ${card.clocktowerShownRole.nameFor(language)}",
                    " · shown as ${card.clocktowerShownRole.nameFor(language)}",
                )
            } else {
                ""
            }
            val playerContext = clocktowerPlayerContextBySeat[seat]
                ?: StorytellerPlayerContextInputV1.DEFAULT
            val experience = when (playerContext.experienceLevel) {
                PlayerExperienceLevelV1.BEGINNER -> text("新手", "Beginner")
                PlayerExperienceLevelV1.NORMAL -> text("普通", "Normal")
                PlayerExperienceLevelV1.EXPERT -> text("高手", "Expert")
            }
            val pressure = when (playerContext.pressureLevel) {
                StorytellerDeclaredPressureLevelV1.LOW -> text("低", "Low")
                StorytellerDeclaredPressureLevelV1.MEDIUM -> text("中", "Medium")
                StorytellerDeclaredPressureLevelV1.HIGH -> text("高", "High")
                null -> text("未设", "Unset")
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                seat.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(card.name, fontWeight = FontWeight.Bold)
                        Text(role + shown, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (gameKind == GameKind.Clocktower) {
                            Text(
                                text(
                                    "推荐上下文：$experience · 声称 ${playerContext.claimedRoleIds.size} · 承压 $pressure",
                                    "Recommendation context: $experience · claims ${playerContext.claimedRoleIds.size} · pressure $pressure",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            card.eliminatedRound?.let { text("死亡", "Dead") } ?: text("存活", "Alive"),
                            color = if (card.eliminatedRound == null) {
                                Color(0xFF2F7A57)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (gameKind == GameKind.Clocktower) {
                            TextButton(onClick = { editingSeat = seat }) {
                                Text(text("编辑上下文", "Edit context"))
                            }
                        }
                    }
                }
            }
        }
    }

    val seat = editingSeat
    if (seat != null && gameKind == GameKind.Clocktower) {
        val card = cards.getOrNull(seat - 1)
        if (card != null) {
            StorytellerPlayerContextEditDialog(
                seat = seat,
                playerName = card.name,
                current = clocktowerPlayerContextBySeat[seat]
                    ?: StorytellerPlayerContextInputV1.DEFAULT,
                claimableRoles = clocktowerClaimableRoles,
                language = language,
                onDismiss = { editingSeat = null },
                onSave = { edited ->
                    onClocktowerPlayerContextChange(seat, edited)
                    editingSeat = null
                },
            )
        }
    }
}

@Composable
private fun StorytellerPlayerContextEditDialog(
    seat: Int,
    playerName: String,
    current: StorytellerPlayerContextInputV1,
    claimableRoles: List<ClocktowerRole>,
    language: String,
    onDismiss: () -> Unit,
    onSave: (StorytellerPlayerContextInputV1) -> Unit,
) {
    val isEnglish = language == "en"
    fun text(zh: String, en: String): String = if (isEnglish) en else zh
    var editState by remember(seat, current) {
        mutableStateOf(ClocktowerPlayerContextEditState.from(current))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text(
                    "#$seat $playerName · 推荐上下文",
                    "#$seat $playerName · Recommendation context",
                ),
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text(
                        text("玩家熟练度（跨局保存）", "Player experience (saved across games)"),
                        fontWeight = FontWeight.Bold,
                    )
                    PlayerExperienceSelector(
                        selected = editState.experienceLevel,
                        isEnglish = isEnglish,
                        onSelect = { level ->
                            editState = editState.copy(experienceLevel = level)
                        },
                    )
                }
                item {
                    Text(
                        text("当前承压程度（仅本局）", "Current pressure (this game only)"),
                        fontWeight = FontWeight.Bold,
                    )
                    StorytellerPressureSelector(
                        selected = editState.pressureLevel,
                        isEnglish = isEnglish,
                        onSelect = { pressure ->
                            editState = editState.copy(pressureLevel = pressure)
                        },
                    )
                }
                item {
                    Text(
                        text("声称角色（可多选，仅本局）", "Claimed roles (multi-select, this game only)"),
                        fontWeight = FontWeight.Bold,
                    )
                    if (claimableRoles.isEmpty()) {
                        Text(
                            text("当前剧本没有可选角色。", "No roles are available for this script."),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(claimableRoles, key = { it.enName }) { role ->
                    val roleId = RoleId(role.enName)
                    val selected = roleId in editState.claimedRoleIds
                    if (selected) {
                        Button(
                            onClick = { editState = editState.toggleClaim(roleId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(role.nameFor(language))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { editState = editState.toggleClaim(roleId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(role.nameFor(language))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(editState.toInput()) }) {
                Text(text("保存", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text("取消", "Cancel"))
            }
        },
    )
}

@Composable
private fun StorytellerPressureSelector(
    selected: StorytellerDeclaredPressureLevelV1?,
    isEnglish: Boolean,
    onSelect: (StorytellerDeclaredPressureLevelV1?) -> Unit,
) {
    val options = listOf(
        null to if (isEnglish) "Unset" else "未设",
        StorytellerDeclaredPressureLevelV1.LOW to if (isEnglish) "Low" else "低",
        StorytellerDeclaredPressureLevelV1.MEDIUM to if (isEnglish) "Medium" else "中",
        StorytellerDeclaredPressureLevelV1.HIGH to if (isEnglish) "High" else "高",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            if (value == selected) {
                Button(
                    onClick = { onSelect(value) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                ) {
                    Text(label)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(value) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun HostRecordsList(
    gameKind: GameKind,
    records: List<EliminationRecord>,
    events: List<ClocktowerEvent>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val visibleEvents = events
        .filterNot { it.type == ClocktowerEventType.System }
        .filter { it.phase != ClocktowerPhase.Dawn }
        .sortedByDescending { it.sequence }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (gameKind == GameKind.Clocktower) {
            if (visibleEvents.isEmpty()) {
                item { Text(text("还没有操作记录。", "No game records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                val grouped = visibleEvents
                    .groupBy { clocktowerEventPhaseLabel(it, language) }
                    .entries
                    .sortedByDescending { (_, g) -> g.first().sequence }
                grouped.forEach { (label, group) ->
                    item {
                        Text(
                            label,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                        )
                    }
                    items(group, key = { it.sequence }) { event ->
                        GameRecordRow(
                            title = event.title,
                            detail = event.detail,
                        )
                    }
                }
            }
        } else {
            if (records.isEmpty()) {
                item { Text(text("还没有操作记录。", "No game records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(records.asReversed()) { record ->
                GameRecordRow(
                    title = record.playerName,
                    detail = record.note.orEmpty(),
                    phase = text("第 ${record.round} 轮", "Round ${record.round}"),
                )
            }
        }
    }
}

@Composable
private fun GameRecordRow(title: String, detail: String, phase: String? = null) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (phase != null) {
                Text(phase, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Text(title, fontWeight = FontWeight.Bold)
            if (detail.isNotBlank()) Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ArchivedGameCard(review: ArchivedGameReview, onClick: () -> Unit) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val gameName = when (review.gameKind) {
        GameKind.Undercover -> stringResource(R.string.game_who_is_undercover)
        GameKind.Clocktower -> stringResource(R.string.game_clocktower)
    }
    val pattern = if (language == "en") "MMM d, HH:mm" else "M月d日 HH:mm"
    val date = java.text.SimpleDateFormat(pattern, context.resources.configuration.locales[0])
        .format(java.util.Date(review.archivedAtMillis))
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(gameName, fontWeight = FontWeight.Black)
                Text(
                    "$date · ${if (language == "en") "Round ${review.round}" else "第 ${review.round} 轮"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun ArchivedGameReviewContent(
    review: ArchivedGameReview,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("← ${text("全部历史对局", "All past games")}")
            }
        }
        review.outcome?.let { outcome ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(outcome.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        Text(outcome.summary, color = MaterialTheme.colorScheme.onSurface)
                        Text(outcome.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Text(text("角色身份", "Roles"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
        items(review.cards) { card ->
            val role = card.hostRoleLabel(context, review.gameKind)
            GameRecordRow(
                title = "#${review.cards.indexOf(card) + 1} ${card.name}",
                detail = role,
                phase = card.eliminatedRound?.let { text("死亡", "Dead") } ?: text("存活", "Alive"),
            )
        }
        item { Text(text("操作记录", "Game log"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
        if (review.gameKind == GameKind.Clocktower) {
            items(review.events.filterNot { it.type == ClocktowerEventType.System }) { event ->
                GameRecordRow(event.title, event.detail, text("第 ${event.round} 轮", "Round ${event.round}"))
            }
        } else {
            items(review.records) { record ->
                GameRecordRow(record.playerName, record.note.orEmpty(), text("第 ${record.round} 轮", "Round ${record.round}"))
            }
        }
        if (review.events.isEmpty() && review.records.isEmpty()) {
            item { Text(text("本局没有操作记录。", "This game has no records."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
