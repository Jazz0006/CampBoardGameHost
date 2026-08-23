package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionKind
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState

@Composable
internal fun RecommendationReasonSummary(
    reasonCodes: List<String>,
    warningCodes: List<String>,
    language: String,
) {
    if (reasonCodes.isEmpty() && warningCodes.isEmpty()) return
    val reasons = reasonCodes.distinct().take(2).joinToString(" · ") { recommendationReasonLabel(it, language) }
    if (reasons.isNotBlank()) {
        Text(
            (if (language == "en") "Why: " else "理由：") + reasons,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (warningCodes.isNotEmpty()) {
        Text(
            (if (language == "en") "Review: " else "注意：") +
                warningCodes.distinct().take(2).joinToString(" · ") { recommendationReasonLabel(it, language) },
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun ClocktowerStorytellerRecommendationScreen(
    title: String,
    subtitle: String,
    description: String,
    buttonLabel: String,
    onStartNight: () -> Unit,
    content: @Composable () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text(
                                "不要向玩家展示推荐、真实角色或说书人裁定。",
                                "Do not show recommendations, actual roles, or Storyteller rulings to players.",
                            ),
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item { content() }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onStartNight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(buttonLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StorytellerRecommendationCard(
    automaticStorytellerInfo: Boolean,
    state: RecommendationUiState,
    selectedStyle: RecommendationStyle,
    appliedStyle: RecommendationStyle?,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    language: String,
    lockedDecisions: List<StorytellerDecision>,
    onSelectStyle: (RecommendationStyle) -> Unit,
    onApply: (RecommendationPlan) -> Unit,
    onReevaluate: (List<StorytellerDecision>) -> Unit,
    onClearLocks: () -> Unit,
) {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    fun styleName(style: RecommendationStyle): String = when (style) {
        RecommendationStyle.GENTLE -> text("稳健", "Gentle")
        RecommendationStyle.BALANCED -> text("平衡", "Balanced")
        RecommendationStyle.AGGRESSIVE -> text("激进", "Aggressive")
    }
    fun roleName(roleId: RoleId): String = clocktowerRolesForScript(script)
        .firstOrNull { it.enName == roleId.value }
        ?.nameFor(language)
        ?: roleId.value
    fun seatLabel(seat: Int): String = cards.getOrNull(seat - 1)?.seatLabel(cards)
        ?: text("${seat}号", "Seat $seat")
    fun scoreReason(ruleId: String): String = when (ruleId) {
        "red-herring-role-suitability" -> text("红鲱鱼身份适合制造可解释的误导", "The red herring creates explainable misinformation")
        "red-herring-sensitive-role" -> text("避免让关键善良角色承受过重压力", "Avoids excessive pressure on a key good role")
        "drunk-shown-role-suitability" -> text("酒鬼展示身份适合持续提供错误信息", "The Drunk's shown role supports ongoing misinformation")
        "drunk-non-information-role" -> text("酒鬼展示为非信息角色，误导空间较少", "A non-information shown role gives the Drunk less useful misinformation")
        "investigator-display-suitability" -> text("调查员假信息具有清晰的讨论价值", "The Investigator misinformation creates a clear discussion hook")
        "drunk-info-avoids-real-evil" -> text("假信息不会直接压中真实邪恶玩家", "The misinformation avoids directly naming real evil")
        "drunk-info-hits-real-evil" -> text("假信息会直接命中真实邪恶玩家", "The misinformation directly names real evil")
        "red-herring-overlaps-drunk-info" -> text("不同误导线索没有堆在同一名玩家身上", "Different misinformation threads do not pile onto one player")
        "one-empath-protected-candidate" -> text("候选人中保留一名可被邻座信息交叉验证的玩家", "One candidate can be cross-checked by neighboring information")
        "both-candidates-empath-protected" -> text("两名候选人都容易被邻座信息快速洗清", "Both candidates may be cleared too quickly by neighboring information")
        "drunk-points-to-self" -> text("避免让酒鬼自己的信息指向自己", "Avoids having the Drunk's information point to themself")
        "candidate-critical-exposure" -> text("控制关键角色被集中怀疑的风险", "Controls the risk of exposing a critical role")
        "candidate-discussion-value" -> text("候选组合能产生有价值的桌面讨论", "The candidate pair should generate useful discussion")
        "candidate-seat-spacing" -> text("候选座位距离符合当前风格", "Candidate spacing fits this recommendation style")
        "demon-bluff-ease" -> text("恶魔伪装较容易解释和维持", "The Demon bluffs are practical to maintain")
        else -> ruleId
    }

    var showOtherPlans by remember { mutableStateOf(false) }
    var showDetails by remember(selectedStyle) { mutableStateOf(false) }
    var editingDecisions by remember { mutableStateOf(false) }
    val plans = (state as? RecommendationUiState.Ready)?.plans.orEmpty()
    val selectedPlan = plans.firstOrNull { it.style == selectedStyle } ?: plans.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text("说书人首夜推荐", "Storyteller first-night recommendation"),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (automaticStorytellerInfo) {
                    text("全自动模式已采用平衡方案，不显示其他候选裁定。", "Automatic mode has applied the balanced plan; alternative rulings are hidden.")
                } else {
                    text("默认选择平衡方案；熟练说书人可比较三种风格。", "Balanced is the default; experienced Storytellers can compare all three styles.")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (!automaticStorytellerInfo && lockedDecisions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text("已锁定 ${lockedDecisions.size} 项裁定", "${lockedDecisions.size} decision(s) locked"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onClearLocks) { Text(text("解除全部", "Clear all")) }
                }
            }

            if (!automaticStorytellerInfo && plans.isNotEmpty() && showOtherPlans) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecommendationStyle.entries.forEach { style ->
                        val enabled = plans.any { it.style == style }
                        if (style == selectedStyle) {
                            Button(
                                onClick = { onSelectStyle(style) },
                                enabled = enabled,
                                shape = RoundedCornerShape(18.dp),
                            ) { Text(styleName(style)) }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectStyle(style) },
                                enabled = enabled,
                                shape = RoundedCornerShape(18.dp),
                            ) { Text(styleName(style)) }
                        }
                    }
                }
            }
            if (!automaticStorytellerInfo && plans.size > 1) {
                TextButton(
                    onClick = {
                        showOtherPlans = !showOtherPlans
                        if (!showOtherPlans) onSelectStyle(RecommendationStyle.BALANCED)
                    },
                ) {
                    Text(if (showOtherPlans) text("只看默认方案", "Show default only") else text("查看其他方案", "View other plans"))
                }
            }

            when (state) {
                RecommendationUiState.Loading -> Text(text("正在计算高质量线索…", "Calculating high-quality information…"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                RecommendationUiState.Empty -> Text(text("当前配置没有找到合法推荐，请使用首夜手动流程。", "No legal recommendation was found; use the manual first-night flow."), color = MaterialTheme.colorScheme.secondary)
                is RecommendationUiState.InvalidLocks -> {
                    Text(text("锁定的裁定不合法或互相冲突，请解除锁定后重试。", "The locked decisions are illegal or incompatible. Clear the locks and try again."), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onClearLocks, modifier = Modifier.fillMaxWidth()) {
                        Text(text("解除锁定并恢复推荐", "Clear locks and restore recommendations"))
                    }
                }
                is RecommendationUiState.Error -> Text(text("推荐暂时不可用：", "Recommendation unavailable: ") + state.message, color = MaterialTheme.colorScheme.error)
                is RecommendationUiState.Ready -> selectedPlan?.let { plan ->
                    val drunkPlayer = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" }
                    val actionLines = plan.decisions.map { decision ->
                        val line = when (decision) {
                            is StorytellerDecision.RedHerring -> text("红鲱鱼：", "Red herring: ") + seatLabel(decision.seat)
                            is StorytellerDecision.DrunkShownRole -> text("酒鬼展示身份：", "Show the Drunk as: ") + roleName(decision.role) + drunkPlayer?.let { " · ${it.seatLabel(cards)}" }.orEmpty()
                            is StorytellerDecision.DrunkInvestigatorInfo -> text("酒鬼调查员信息：", "Drunk Investigator information: ") + roleName(decision.shownMinion) + " · " + decision.candidateSeats.joinToString(" / ") { seatLabel(it) }
                            is StorytellerDecision.DemonBluffs -> text("恶魔伪装：", "Demon bluffs: ") + decision.roles.joinToString(text("、", ", ")) { roleName(it) }
                        }
                        if (lockedDecisions.any { it.kind() == decision.kind() }) "🔒 $line" else line
                    }
                    actionLines.forEach { line -> Text("• $line", style = MaterialTheme.typography.bodyMedium) }

                    if (!automaticStorytellerInfo && editingDecisions) {
                        RecommendationDecisionEditor(
                            plan = plan,
                            lockedDecisions = lockedDecisions,
                            cards = cards,
                            script = script,
                            language = language,
                            onCancel = { editingDecisions = false },
                            onSubmit = { nextLocks ->
                                editingDecisions = false
                                onReevaluate(nextLocks)
                            },
                        )
                    } else if (!automaticStorytellerInfo) {
                        OutlinedButton(
                            onClick = { editingDecisions = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(text("修改裁定", "Edit decisions"))
                        }
                    }

                    val qualityLabel = when (plan.qualityTier) {
                        QualityTier.RECOMMENDED -> text("推荐", "Recommended")
                        QualityTier.ACCEPTABLE_WITH_WARNING -> text("可用，但需留意警告", "Usable with warnings")
                        QualityTier.EXPERT_ONLY -> text("仅建议熟练说书人使用", "Expert only")
                        QualityTier.REJECTED -> text("不可用", "Rejected")
                    }
                    Text(
                        text("质量：", "Quality: ") + qualityLabel,
                        color = if (plan.qualityTier == QualityTier.RECOMMENDED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (showDetails) {
                        Text(text("评分：${plan.totalScore}", "Score: ${plan.totalScore}"), fontWeight = FontWeight.SemiBold)
                        plan.scoreItems
                            .sortedByDescending { kotlin.math.abs(it.delta) }
                            .take(6)
                            .forEach { item ->
                                val sign = if (item.delta >= 0) "+" else ""
                                Text("$sign${item.delta} · ${scoreReason(item.ruleId)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        plan.warnings.forEach { warning ->
                            Text(text("注意：", "Warning: ") + scoreReason(warning.ruleId), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showDetails = !showDetails }, modifier = Modifier.weight(1f)) {
                            Text(if (showDetails) text("收起理由", "Hide reasons") else text("查看推荐理由", "Why this plan"))
                        }
                        if (!automaticStorytellerInfo) {
                            Button(
                                onClick = { onApply(plan) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (appliedStyle == plan.style) text("已采用", "Applied") else text("采用推荐", "Apply plan"))
                            }
                        }
                    }
                    Text(
                        if (automaticStorytellerInfo) {
                            text("以下首夜步骤将直接使用当前自动模式的信息。", "The first-night steps below will use the selected automatic style.")
                        } else {
                            text("采用后仍可在下方首夜步骤中手动修改具体裁定。", "After applying, you can still edit individual decisions in the first-night steps below.")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecommendationDecisionEditor(
    plan: RecommendationPlan,
    lockedDecisions: List<StorytellerDecision>,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    language: String,
    onCancel: () -> Unit,
    onSubmit: (List<StorytellerDecision>) -> Unit,
) {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    fun roleName(role: ClocktowerRole): String = role.nameFor(language)
    val scriptRoles = clocktowerRolesForScript(script)
    val inPlayRoleNames = cards.mapNotNull { it.clocktowerRole?.enName }.toSet()
    val redHerringOptions = cards.filter {
        it.clocktowerTeam == ClocktowerTeam.Townsfolk || it.clocktowerTeam == ClocktowerTeam.Outsider
    }
    val drunkShownRoleOptions = scriptRoles.filter {
        it.team == ClocktowerTeam.Townsfolk && it.enName !in inPlayRoleNames
    }
    val minionRoleOptions = scriptRoles.filter { it.team == ClocktowerTeam.Minion }
    val demonBluffOptions = scriptRoles.filter {
        it.team in setOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider) && it.enName !in inPlayRoleNames
    }
    var draftDecisions by remember(plan.effectSignature) { mutableStateOf(plan.decisions) }
    var modifiedKinds by remember(plan.effectSignature) { mutableStateOf<Set<StorytellerDecisionKind>>(emptySet()) }

    fun replaceDecision(kind: StorytellerDecisionKind, decision: StorytellerDecision?) {
        draftDecisions = draftDecisions.filterNot { it.kind() == kind } + listOfNotNull(decision)
        modifiedKinds = modifiedKinds + kind
    }

    val redHerring = draftDecisions.filterIsInstance<StorytellerDecision.RedHerring>().singleOrNull()
    val drunkShownRole = draftDecisions.filterIsInstance<StorytellerDecision.DrunkShownRole>().singleOrNull()
    val drunkInfo = draftDecisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().singleOrNull()
    val demonBluffs = draftDecisions.filterIsInstance<StorytellerDecision.DemonBluffs>().singleOrNull()
    val isValidDraft = (drunkShownRole?.role != RoleId("Investigator") || drunkInfo?.candidateSeats?.size == 2) &&
        (demonBluffs == null || demonBluffs.roles.size == 3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text("修改后，该项会被锁定；其他项目将重新计算。", "Changed items will be locked; all other items will be recalculated."), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

            redHerring?.let { current ->
                Text(text("红鲱鱼", "Red herring"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    redHerringOptions.forEach { card ->
                        val seat = cards.indexOf(card) + 1
                        val selected = current.seat == seat
                        if (selected) {
                            Button(onClick = { }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("$seat. ${card.name}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { replaceDecision(StorytellerDecisionKind.RED_HERRING, StorytellerDecision.RedHerring(seat)) },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text("${cards.indexOf(card) + 1}. ${card.name}") }
                        }
                    }
                }
                HorizontalDivider()
            }

            drunkShownRole?.let { current ->
                Text(text("酒鬼展示身份", "Drunk shown role"), fontWeight = FontWeight.Bold)
                val committedShownRole = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" }
                    ?.clocktowerShownRole
                Text(
                    committedShownRole?.let { roleName(it) } ?: roleName(
                        drunkShownRoleOptions.first { it.enName == current.role.value },
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text(
                        "该身份已经向玩家展示；后续推荐只能围绕它重新优化。",
                        "This identity has already been shown; later recommendations optimize around it.",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider()
            }

            if (drunkShownRole?.role == RoleId("Investigator") && drunkInfo != null) {
                Text(text("酒鬼调查员展示的爪牙", "Minion shown to the Drunk Investigator"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    minionRoleOptions.forEach { role ->
                        val selected = drunkInfo.shownMinion.value == role.enName
                        if (selected) {
                            Button(onClick = { }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) { Text(roleName(role)) }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    replaceDecision(
                                        StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                        drunkInfo.copy(shownMinion = RoleId(role.enName)),
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        }
                    }
                }
                Text(text("选择两名候选玩家", "Select two candidate players"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cards.forEachIndexed { index, card ->
                        val seat = index + 1
                        val selected = seat in drunkInfo.candidateSeats
                        if (selected) {
                            Button(
                                onClick = {
                                    replaceDecision(
                                        StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                        drunkInfo.copy(candidateSeats = drunkInfo.candidateSeats.filterNot { it == seat }),
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text("$seat. ${card.name}") }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (drunkInfo.candidateSeats.size < 2) {
                                        replaceDecision(
                                            StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                            drunkInfo.copy(candidateSeats = (drunkInfo.candidateSeats + seat).sorted()),
                                        )
                                    }
                                },
                                enabled = drunkInfo.candidateSeats.size < 2,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text("$seat. ${card.name}") }
                        }
                    }
                }
                if (drunkInfo.candidateSeats.size != 2) {
                    Text(text("必须选择正好两名玩家。", "Select exactly two players."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
            }

            demonBluffs?.let { current ->
                Text(text("恶魔伪装（选择三个）", "Demon bluffs (select three)"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    demonBluffOptions.forEach { role ->
                        val roleId = RoleId(role.enName)
                        val selected = roleId in current.roles
                        if (selected) {
                            Button(
                                onClick = {
                                    replaceDecision(
                                        StorytellerDecisionKind.DEMON_BLUFFS,
                                        current.copy(roles = current.roles.filterNot { it == roleId }),
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (current.roles.size < 3) {
                                        replaceDecision(
                                            StorytellerDecisionKind.DEMON_BLUFFS,
                                            current.copy(
                                                roles = (current.roles + roleId).sortedBy { selectedRole ->
                                                    demonBluffOptions.indexOfFirst { it.enName == selectedRole.value }
                                                },
                                            ),
                                        )
                                    }
                                },
                                enabled = current.roles.size < 3,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        }
                    }
                }
                if (current.roles.size != 3) {
                    Text(text("必须选择正好三个伪装角色。", "Select exactly three bluff roles."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(text("取消", "Cancel")) }
                Button(
                    onClick = {
                        val affectedKinds = modifiedKinds
                        val nextLocks = lockedDecisions.filterNot { it.kind() in affectedKinds } +
                            draftDecisions.filter { it.kind() in affectedKinds }
                        onSubmit(nextLocks)
                    },
                    enabled = modifiedKinds.isNotEmpty() && isValidDraft,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text("锁定并重新评价", "Lock and re-evaluate"))
                }
            }
        }
    }
}
