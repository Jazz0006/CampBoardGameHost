package com.codex.campboardgamehost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticAcceptanceV2

/** Private Host-only screen. Player identities were shown with no actual Drunk bound. */
@Composable
internal fun ClocktowerJointOpeningReviewScreen(
    acceptance: HostOpeningSemanticAcceptanceV2?,
    busy: Boolean,
    error: String?,
    onContinue: () -> Unit,
    autoContinue: Boolean,
    onRetry: () -> Unit,
    onManual: () -> Unit,
) {
    LaunchedEffect(autoContinue, acceptance, busy) {
        if (autoContinue && acceptance != null && !busy) onContinue()
    }
    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "说书人私密配置 · 首夜全局方案",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "全部展示身份已发放。酒鬼实际身份尚未绑定；本页仅供说书人查看。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (busy) {
                CircularProgressIndicator()
                Text("AI 正在进行整局联合规划。可随时切换手动主持。",
                    color = MaterialTheme.colorScheme.onSurface)
            }
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (acceptance != null) {
                val selected = acceptance.selection
                Text(
                    "酒鬼：" + (selected.drunkSeat?.let { it.toString() + " 号" }
                        ?: "本局没有酒鬼"),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                selected.demonBluffs?.let { bluffs ->
                    Text("恶魔掩饰身份：" + bluffs.joinToString(" · "),
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Text("联合推荐已经通过 Host 初始规则域验证；包含信息 " +
                    acceptance.matchingCandidateIds.size + " 项，待真实行动 " +
                    acceptance.conditional.size + " 项。",
                    color = MaterialTheme.colorScheme.onSurface)
                Text("以上均为未执行推荐。酒鬼会在下一步确认；掩饰身份和线索只在相应步骤确认、展示。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!autoContinue) {
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                        Text("进入首夜逐步确认")
                    }
                }
            }
            if (!busy) {
                OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text("重新规划整套首夜方案")
                }
            }
            OutlinedButton(onClick = onManual, modifier = Modifier.fillMaxWidth()) {
                Text("改为手动主持")
            }
        }
    }
}
