package com.codex.campboardgamehost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampBoardGameHostApp()
        }
    }
}

private enum class Screen {
    Setup,
    PassPhone,
    RevealCard,
    Game,
}

private enum class Role(val label: String) {
    Civilian("平民"),
    Undercover("卧底"),
    Blank("白板"),
}

private data class WordPair(val civilianWord: String, val undercoverWord: String, val category: String)

private data class PlayerCard(
    val name: String,
    val role: Role,
    val word: String,
    val eliminatedRound: Int? = null,
)

private data class EliminationRecord(
    val round: Int,
    val playerName: String,
)

private data class GameOutcome(
    val title: String,
    val summary: String,
    val reason: String,
)

private val wordPairs = listOf(
    WordPair("帐篷", "天幕", "露营"),
    WordPair("营地灯", "手电筒", "露营"),
    WordPair("睡袋", "防潮垫", "露营"),
    WordPair("烤肉", "火锅", "食物"),
    WordPair("可乐", "雪碧", "饮料"),
    WordPair("咖啡", "奶茶", "饮料"),
    WordPair("苹果", "梨", "水果"),
    WordPair("西瓜", "哈密瓜", "水果"),
    WordPair("牙刷", "毛巾", "生活"),
    WordPair("雨伞", "雨衣", "生活"),
    WordPair("高铁", "地铁", "交通"),
    WordPair("飞机", "热气球", "交通"),
    WordPair("猫", "狗", "动物"),
    WordPair("狮子", "老虎", "动物"),
    WordPair("医生", "护士", "职业"),
    WordPair("老师", "教练", "职业"),
)

@Composable
private fun CampBoardGameHostApp() {
    var screen by remember { mutableStateOf(Screen.Setup) }
    var playerCount by remember { mutableIntStateOf(6) }
    var undercoverCount by remember { mutableIntStateOf(1) }
    var includeBlank by remember { mutableStateOf(false) }
    var currentDealIndex by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(1) }
    var selectedElimination by remember { mutableStateOf<String?>(null) }
    var showResults by remember { mutableStateOf(false) }
    var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }
    val playerNames = remember { mutableStateListOf("玩家 1", "玩家 2", "玩家 3", "玩家 4", "玩家 5", "玩家 6") }
    val cards = remember { mutableStateListOf<PlayerCard>() }
    val records = remember { mutableStateListOf<EliminationRecord>() }

    fun syncPlayerNames(count: Int) {
        while (playerNames.size < count) {
            playerNames.add("玩家 ${playerNames.size + 1}")
        }
        while (playerNames.size > count) {
            playerNames.removeAt(playerNames.lastIndex)
        }
    }

    fun startGame() {
        val pair = wordPairs.random()
        val blankCount = if (includeBlank) 1 else 0
        val roles = buildList {
            repeat(undercoverCount) { add(Role.Undercover) }
            repeat(blankCount) { add(Role.Blank) }
            repeat(playerCount - undercoverCount - blankCount) { add(Role.Civilian) }
        }.shuffled()

        cards.clear()
        cards.addAll(playerNames.take(playerCount).mapIndexed { index, name ->
            val role = roles[index]
            val word = when (role) {
                Role.Civilian -> pair.civilianWord
                Role.Undercover -> pair.undercoverWord
                Role.Blank -> "无词"
            }
            PlayerCard(name = name.ifBlank { "玩家 ${index + 1}" }, role = role, word = word)
        })
        records.clear()
        currentDealIndex = 0
        round = 1
        showResults = false
        gameOutcome = null
        selectedElimination = null
        screen = Screen.PassPhone
    }

    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF2F5D50),
            secondary = Color(0xFFD96C3B),
            background = Color(0xFFF8F6F0),
            surface = Color(0xFFFFFCF6),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1F2925),
            onSurface = Color(0xFF1F2925),
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (screen) {
                Screen.Setup -> SetupScreen(
                    playerCount = playerCount,
                    undercoverCount = undercoverCount,
                    includeBlank = includeBlank,
                    playerNames = playerNames,
                    onPlayerCountChange = { next ->
                        val maxUndercover = if (includeBlank) next - 2 else next - 1
                        playerCount = next
                        syncPlayerNames(next)
                        undercoverCount = undercoverCount.coerceIn(1, maxUndercover.coerceAtLeast(1))
                    },
                    onUndercoverCountChange = { undercoverCount = it },
                    onIncludeBlankChange = { checked ->
                        includeBlank = checked
                        val maxUndercover = if (checked) playerCount - 2 else playerCount - 1
                        undercoverCount = undercoverCount.coerceIn(1, maxUndercover.coerceAtLeast(1))
                    },
                    onNameChange = { index, value -> playerNames[index] = value },
                    onStart = ::startGame,
                )

                Screen.PassPhone -> PassPhoneScreen(
                    playerName = cards[currentDealIndex].name,
                    current = currentDealIndex + 1,
                    total = cards.size,
                    onReveal = { screen = Screen.RevealCard },
                )

                Screen.RevealCard -> RevealCardScreen(
                    card = cards[currentDealIndex],
                    current = currentDealIndex + 1,
                    total = cards.size,
                    onHide = {
                        if (currentDealIndex == cards.lastIndex) {
                            screen = Screen.Game
                        } else {
                            currentDealIndex += 1
                            screen = Screen.PassPhone
                        }
                    },
                )

                Screen.Game -> GameScreen(
                    cards = cards,
                    records = records,
                    round = round,
                    gameOutcome = gameOutcome,
                    selectedElimination = selectedElimination,
                    onSelectElimination = { selectedElimination = it },
                    onConfirmElimination = {
                        val name = selectedElimination
                        if (name != null) {
                            val index = cards.indexOfFirst { it.name == name }
                            if (index >= 0) {
                                cards[index] = cards[index].copy(eliminatedRound = round)
                                records.add(EliminationRecord(round, name))
                                selectedElimination = null
                                gameOutcome = evaluateGameOutcome(cards)
                                if (gameOutcome != null) {
                                    showResults = true
                                }
                                round += 1
                            }
                        }
                    },
                    onShowResults = {
                        gameOutcome = gameOutcome ?: GameOutcome(
                            title = "手动结束",
                            summary = "本局已由主持人手动结束",
                            reason = "当前局面还没有触发自动胜负条件。",
                        )
                        showResults = true
                    },
                    onNewGame = {
                        screen = Screen.Setup
                        cards.clear()
                        records.clear()
                        gameOutcome = null
                    },
                )
            }

            if (showResults) {
                ResultsDialog(
                    cards = cards,
                    outcome = gameOutcome,
                    onDismiss = { showResults = false },
                    onNewGame = {
                        showResults = false
                        gameOutcome = null
                        screen = Screen.Setup
                        cards.clear()
                        records.clear()
                    },
                )
            }
        }
    }
}

private fun evaluateGameOutcome(cards: List<PlayerCard>): GameOutcome? {
    val activeCards = cards.filter { it.eliminatedRound == null }
    val activeCivilians = activeCards.count { it.role == Role.Civilian }
    val activeUndercovers = activeCards.count { it.role == Role.Undercover }
    val activeBlanks = activeCards.count { it.role == Role.Blank }

    return when {
        activeUndercovers == 0 && activeBlanks == 0 -> GameOutcome(
            title = "平民胜利",
            summary = "所有卧底和白板都已出局",
            reason = "场上剩余 $activeCivilians 名平民，隐藏阵营已全部淘汰。",
        )

        activeUndercovers > 0 && activeUndercovers >= activeCivilians -> GameOutcome(
            title = "卧底胜利",
            summary = "卧底人数已经达到或超过平民人数",
            reason = "场上剩余 $activeUndercovers 名卧底、$activeCivilians 名平民。",
        )

        activeCivilians == 0 && activeUndercovers == 0 && activeBlanks > 0 -> GameOutcome(
            title = "白板胜利",
            summary = "白板活到了最后",
            reason = "场上只剩 $activeBlanks 名白板。",
        )

        else -> null
    }
}

@Composable
private fun SetupScreen(
    playerCount: Int,
    undercoverCount: Int,
    includeBlank: Boolean,
    playerNames: List<String>,
    onPlayerCountChange: (Int) -> Unit,
    onUndercoverCountChange: (Int) -> Unit,
    onIncludeBlankChange: (Boolean) -> Unit,
    onNameChange: (Int, String) -> Unit,
    onStart: () -> Unit,
) {
    val maxUndercover = if (includeBlank) playerCount - 2 else playerCount - 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("露营桌游助手", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("谁是卧底 · 离线 MVP", color = Color(0xFF5C6A63))
        }

        item {
            SettingsPanel(
                playerCount = playerCount,
                undercoverCount = undercoverCount,
                includeBlank = includeBlank,
                maxUndercover = maxUndercover,
                onPlayerCountChange = onPlayerCountChange,
                onUndercoverCountChange = onUndercoverCountChange,
                onIncludeBlankChange = onIncludeBlankChange,
            )
        }

        item {
            Text("玩家", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(playerNames.indices.toList()) { index ->
            OutlinedTextField(
                value = playerNames[index],
                onValueChange = { onNameChange(index, it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("玩家 ${index + 1}") },
                singleLine = true,
            )
        }

        item {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("开始发牌")
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    playerCount: Int,
    undercoverCount: Int,
    includeBlank: Boolean,
    maxUndercover: Int,
    onPlayerCountChange: (Int) -> Unit,
    onUndercoverCountChange: (Int) -> Unit,
    onIncludeBlankChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepperRow(
                label = "玩家人数",
                value = playerCount,
                range = 3..12,
                onChange = onPlayerCountChange,
            )
            StepperRow(
                label = "卧底人数",
                value = undercoverCount,
                range = 1..maxUndercover.coerceAtLeast(1),
                onChange = onUndercoverCountChange,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeBlank, onCheckedChange = onIncludeBlankChange)
                Text("加入 1 名白板")
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onChange((value - 1).coerceAtLeast(range.first)) },
                enabled = value > range.first,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("-", fontSize = 22.sp, textAlign = TextAlign.Center)
            }
            Text(
                value.toString(),
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(
                onClick = { onChange((value + 1).coerceAtMost(range.last)) },
                enabled = value < range.last,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("+", fontSize = 22.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PassPhoneScreen(
    playerName: String,
    current: Int,
    total: Int,
    onReveal: () -> Unit,
) {
    FullScreenColumn {
        Text("$current / $total", color = Color(0xFF6F7B74))
        Text("请把手机交给", style = MaterialTheme.typography.titleLarge)
        Text(playerName, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("确认周围没人偷看后再查看身份", color = Color(0xFF5C6A63), textAlign = TextAlign.Center)
        Button(
            onClick = onReveal,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("查看我的牌")
        }
    }
}

@Composable
private fun RevealCardScreen(
    card: PlayerCard,
    current: Int,
    total: Int,
    onHide: () -> Unit,
) {
    FullScreenColumn {
        Text("$current / $total", color = Color(0xFF6F7B74))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(card.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(card.word, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                Text("记住这个词，然后隐藏手机", color = Color(0xFF5C6A63))
            }
        }
        Button(
            onClick = onHide,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (current == total) "全部看完，交给主持人" else "隐藏并交给下一位")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameScreen(
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    round: Int,
    gameOutcome: GameOutcome?,
    selectedElimination: String?,
    onSelectElimination: (String) -> Unit,
    onConfirmElimination: () -> Unit,
    onShowResults: () -> Unit,
    onNewGame: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("主持人面板", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(gameOutcome?.title ?: "第 $round 轮", color = Color(0xFF5C6A63))
                }
                TextButton(onClick = onNewGame) {
                    Text("新游戏")
                }
            }
        }

        if (gameOutcome != null) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(gameOutcome.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(gameOutcome.summary)
                        Text(gameOutcome.reason, color = Color(0xFF5C6A63))
                    }
                }
            }
        }

        item {
            Text("选择本轮淘汰", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cards.filter { it.eliminatedRound == null }.forEach { card ->
                    val selected = selectedElimination == card.name
                    if (selected) {
                        Button(onClick = { onSelectElimination(card.name) }, shape = RoundedCornerShape(8.dp)) {
                            Text(card.name)
                        }
                    } else {
                        OutlinedButton(onClick = { onSelectElimination(card.name) }, shape = RoundedCornerShape(8.dp)) {
                            Text(card.name)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onConfirmElimination,
                enabled = selectedElimination != null && gameOutcome == null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("记录淘汰")
            }
        }

        item {
            HorizontalDivider()
            Text("玩家状态", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(cards) { card ->
            PlayerStatusRow(card)
        }

        item {
            HorizontalDivider()
            Text("淘汰记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text("还没有玩家被淘汰", color = Color(0xFF6F7B74))
            }
        }

        items(records) { record ->
            Text("第 ${record.round} 轮：${record.playerName}", modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            Button(
                onClick = onShowResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(if (gameOutcome == null) "结束并查看答案" else "查看结果")
            }
        }
    }
}

@Composable
private fun PlayerStatusRow(card: PlayerCard) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(card.name, fontWeight = FontWeight.SemiBold)
            val status = card.eliminatedRound?.let { "第 $it 轮淘汰" } ?: "在场"
            Text(status, color = if (card.eliminatedRound == null) Color(0xFF2F5D50) else Color(0xFF9A4B36))
        }
    }
}

@Composable
private fun ResultsDialog(
    cards: List<PlayerCard>,
    outcome: GameOutcome?,
    onDismiss: () -> Unit,
    onNewGame: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(outcome?.title ?: "身份结果") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (outcome != null) {
                    Text(outcome.summary, fontWeight = FontWeight.SemiBold)
                    Text(outcome.reason, color = Color(0xFF5C6A63))
                    HorizontalDivider()
                }
                cards.forEach { card ->
                    Text("${card.name}：${card.role.label} · ${card.word}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onNewGame) {
                Text("再来一局")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("返回")
            }
        },
    )
}

@Composable
private fun FullScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}
