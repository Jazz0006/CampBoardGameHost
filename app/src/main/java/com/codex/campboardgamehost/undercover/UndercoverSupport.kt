package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Behavior-preserving R2 extraction for Who Is Undercover. */
internal data class WordPair(
    val civilianWord: String,
    val undercoverWord: String,
    val category: String,
)

private val chineseWordPairs = listOf(
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

private val englishWordPairs = listOf(
    WordPair("Tent", "Tarp", "Camping"),
    WordPair("Lantern", "Flashlight", "Camping"),
    WordPair("Sleeping bag", "Sleeping pad", "Camping"),
    WordPair("Barbecue", "Hot pot", "Food"),
    WordPair("Cola", "Lemon-lime soda", "Drink"),
    WordPair("Coffee", "Milk tea", "Drink"),
    WordPair("Apple", "Pear", "Fruit"),
    WordPair("Watermelon", "Cantaloupe", "Fruit"),
    WordPair("Toothbrush", "Towel", "Daily"),
    WordPair("Umbrella", "Raincoat", "Daily"),
    WordPair("High-speed train", "Subway", "Transport"),
    WordPair("Airplane", "Hot air balloon", "Transport"),
    WordPair("Cat", "Dog", "Animal"),
    WordPair("Lion", "Tiger", "Animal"),
    WordPair("Doctor", "Nurse", "Job"),
    WordPair("Teacher", "Coach", "Job"),
)

internal fun wordPairsFor(language: String): List<WordPair> {
    return if (language == "en") englishWordPairs else chineseWordPairs
}

@Composable
internal fun UndercoverSettingsScreen(
    playerCount: Int,
    undercoverCount: Int,
    includeBlank: Boolean,
    onUndercoverCountChange: (Int) -> Unit,
    onIncludeBlankChange: (Boolean) -> Unit,
    onBack: () -> Unit,
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
            GameSettingsHeader(
                title = stringResource(R.string.game_who_is_undercover),
                subtitle = stringResource(R.string.game_settings_subtitle, playerCount),
                onBack = onBack,
            )
        }
        item {
            SettingsPanel(
                playerCount = playerCount,
                undercoverCount = undercoverCount,
                includeBlank = includeBlank,
                maxUndercover = maxUndercover,
                onUndercoverCountChange = onUndercoverCountChange,
                onIncludeBlankChange = onIncludeBlankChange,
            )
        }
        item {
            Button(
                onClick = onStart,
                enabled = playerCount >= MIN_PLAYERS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.start_dealing))
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
            Text(stringResource(R.string.game_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.player_count_summary, playerCount), color = Color(0xFF6F7B74))
            StepperRow(
                label = stringResource(R.string.undercover_count),
                value = undercoverCount,
                range = 1..maxUndercover.coerceAtLeast(1),
                onChange = onUndercoverCountChange,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeBlank, onCheckedChange = onIncludeBlankChange)
                Text(stringResource(R.string.include_blank))
            }
        }
    }
}
