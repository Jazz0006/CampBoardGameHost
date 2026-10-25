package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Host-only presentation of the recommendation belonging to this request/step. */
internal data class ClocktowerAiRecommendationActions(
    val identity: Any,
    val reason: String?,
    val openingReason: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val onRetry: (() -> Unit)? = null,
)

/** Recommendation presentation belongs to the page, outside shared navigation and seat renderers. */
@Composable
internal fun ClocktowerAiRecommendationPage(
    actions: ClocktowerAiRecommendationActions?,
    content: @Composable () -> Unit,
) {
    ClocktowerDarkTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                actions?.let { ClocktowerAiRecommendationToolbar(it) }
                Box(Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalClocktowerTableCenterHeader provides {
                        actions?.let { ClocktowerAiRecommendationStatus(it) }
                    }) { content() }
                }
            }
        }
    }
}

@Composable
internal fun ClocktowerAiRecommendationToolbar(actions: ClocktowerAiRecommendationActions) {
    val english = LocalContext.current.resources.configuration.locales[0].language == "en"
    fun text(zh: String, en: String) = if (english) en else zh
    // A new request/result must never leave an older reason open.
    var detailsOpen by remember(actions.identity, actions.reason, actions.busy, actions.error) {
        mutableStateOf(false)
    }
    Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { actions.onRetry?.invoke() },
            enabled = !actions.busy && actions.onRetry != null) {
            Text(if (actions.busy) text("请求中…", "Requesting…") else text("重新推荐", "Request again"))
        }
        TextButton(onClick = { detailsOpen = true }, enabled = !actions.busy) {
            Text(if (actions.error != null) text("失败详情", "Failure details")
                else text("推荐理由", "Recommendation details"))
        }
    }
    if (detailsOpen) AlertDialog(
        onDismissRequest = { detailsOpen = false },
        title = { Text(if (actions.error != null) text("本次推荐未采纳", "Recommendation not accepted")
            else if (actions.openingReason) text("首夜整体方案理由", "Opening plan rationale")
            else text("当前推荐理由", "Current recommendation rationale")) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(actions.error ?: actions.reason?.takeIf(String::isNotBlank)
                    ?: text("本次推荐未保留理由。", "No rationale was retained for this recommendation."))
                if (actions.error == null && actions.openingReason && actions.onRetry == null) Text(
                    text("首夜整体方案已进入确认流程；重新规划整套方案仅在首夜配置确认前开放。",
                        "The opening plan is already being confirmed. A full replan is available before confirming the opening setup."),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = { TextButton(onClick = { detailsOpen = false }) { Text(text("关闭", "Close")) } },
    )
}

@Composable
private fun ClocktowerAiRecommendationStatus(actions: ClocktowerAiRecommendationActions) {
    val english = LocalContext.current.resources.configuration.locales[0].language == "en"
    if (actions.busy || actions.error != null) Text(
        text = if (actions.busy) {
            if (english) "Preparing recommendation…" else "正在准备推荐…"
        } else actions.error.orEmpty(),
        style = MaterialTheme.typography.bodySmall,
        color = if (actions.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
