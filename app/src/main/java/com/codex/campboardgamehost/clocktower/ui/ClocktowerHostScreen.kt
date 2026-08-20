package com.codex.campboardgamehost

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationLedger
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionKind
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact
import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.recommendation.GameBalanceEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCandidate
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionPoolParityRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolDeviceBenchmark
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolDeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationRegistration
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.RegistrationDetail
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.DynamicResolutionRequest
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmark
import com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationCandidate
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationRequest
import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult
import com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkCase
import com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkHarness
import com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmCoordinator
import com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmRequest
import com.codex.campboardgamehost.clocktower.epistemic.A4MainThreadFrameTelemetry
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildExecutor
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildRequest
import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
import com.codex.campboardgamehost.clocktower.epistemic.A4ShadowWorldSetCache
import com.codex.campboardgamehost.clocktower.epistemic.A4WorldEngineRollout
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.ZddFilterStrategy
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.RegistrationInteractionRules
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import java.util.Locale
import java.util.UUID

internal fun isClocktowerEvil(card: PlayerCard): Boolean =
    card.clocktowerTeam == ClocktowerTeam.Minion || card.clocktowerTeam == ClocktowerTeam.Demon

private fun clocktowerRedHerringCandidates(aliveCards: List<PlayerCard>): List<PlayerCard> =
    aliveCards.filter { card -> card.clocktowerTeam?.isLegalRedHerringTeam() == true }

private fun actualClocktowerRoleCards(cards: List<PlayerCard>, enName: String): List<PlayerCard> =
    cards.filter { it.clocktowerRole?.enName == enName }

private fun chefEvilPairs(cards: List<PlayerCard>, isEvil: (PlayerCard) -> Boolean = ::isClocktowerEvil): Int {
    val evilSeats = cards.mapIndexedNotNull { index, card -> (index + 1).takeIf { isEvil(card) } }.toSet()
    return FixedInformationEvaluator.chefEvilPairs(cards.toClocktowerPlayerStates()) { it.seat in evilSeats }
}

private fun livingNeighbors(cards: List<PlayerCard>, playerName: String): List<PlayerCard> {
    val sourceSeat = cards.indexOfFirst { it.name == playerName } + 1
    if (sourceSeat <= 0) return emptyList()
    val neighborSeats = FixedInformationEvaluator
        .livingNeighbors(cards.toClocktowerPlayerStates(), sourceSeat)
        .map { it.seat }
    return neighborSeats.mapNotNull { seat -> cards.getOrNull(seat - 1) }
}

private fun empathEvilNeighborCount(
    cards: List<PlayerCard>,
    playerName: String,
    isEvil: (PlayerCard) -> Boolean = ::isClocktowerEvil,
): Int {
    val sourceSeat = cards.indexOfFirst { it.name == playerName } + 1
    if (sourceSeat <= 0) return 0
    val evilSeats = cards.mapIndexedNotNull { index, card -> (index + 1).takeIf { isEvil(card) } }.toSet()
    return FixedInformationEvaluator.empathEvilNeighborCount(cards.toClocktowerPlayerStates(), sourceSeat) {
        it.seat in evilSeats
    }
}

private fun storytellerPairHint(
    target: PlayerCard,
    cards: List<PlayerCard>,
    fallbackPool: List<PlayerCard> = cards,
    excludeNames: Set<String> = emptySet(),
): Pair<PlayerCard, PlayerCard>? {
    val decoy = fallbackPool.firstOrNull { it.name != target.name && it.name !in excludeNames }
        ?: fallbackPool.firstOrNull { it.name != target.name }
        ?: return null
    return target to decoy
}

internal fun PlayerCard.clocktowerShownAsDifferentRole(): Boolean =
    clocktowerRole?.enName != null && clocktowerShownRole?.enName != null && clocktowerRole?.enName != clocktowerShownRole?.enName

private data class ClocktowerDisplayOption(
    val label: String,
    val displayKind: ClocktowerDisplayKind,
    val displayTitle: String,
    val displayPrimary: String?,
    val displaySecondary: String?,
    val displayFooter: String?,
    /** Exact player-visible statement; never reconstruct it from localized display strings. */
    val proposition: InformationProposition? = null,
    val spyRegistersGood: Boolean? = null,
    val spyRegisteredRoleEnName: String? = null,
    val recluseRegistersEvil: Boolean? = null,
    val recluseRegisteredRoleEnName: String? = null,
    val recommendationStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    val isTruthful: Boolean = true,
    val misinformationPressure: Int = 0,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

/** Canonical semantic ID shared by legacy, unified-pool and first-night shadow paths. */
private fun clocktowerInformationCandidateId(option: ClocktowerDisplayOption): String = listOf(
    option.displayKind.name,
    option.proposition?.toString().orEmpty(),
    option.displayPrimary.orEmpty(),
    option.displaySecondary.orEmpty(),
    option.displayFooter.orEmpty(),
    option.spyRegistersGood?.toString().orEmpty(),
    option.spyRegisteredRoleEnName.orEmpty(),
    option.recluseRegistersEvil?.toString().orEmpty(),
    option.recluseRegisteredRoleEnName.orEmpty(),
    option.isTruthful.toString(),
).joinToString("|")

private data class ClocktowerDecisionOption(
    val label: String,
    val targetName: String,
    val explanation: String,
    val recommendationStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

private fun unifiedDecisionPool(
    options: List<ClocktowerDecisionOption>,
    familyId: String,
): UnifiedSelectionPool<ClocktowerDecisionOption>? = options
    .takeIf { it.isNotEmpty() }
    ?.let { candidates ->
        UnifiedSelectionPool(candidates.map { option ->
            UnifiedSelectionCandidate(
                candidateId = listOf(option.recommendationStyle.name, option.targetName).joinToString("|"),
                familyId = familyId,
                legality = UnifiedCandidateLegality.LEGAL,
                epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
                qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
                rankFixedPoint = if (option.isDefaultRecommendation) 1_000_000L else 800_000L,
                reasonCodes = option.reasonCodes,
                warningCodes = option.warningCodes,
                payload = option,
            )
        })
    }

private fun unifiedFirstNightInformationPool(
    options: List<ClocktowerDisplayOption>,
    familyId: String,
    automaticStyle: RecommendationStyle,
): UnifiedSelectionPool<ClocktowerDisplayOption> = UnifiedSelectionPool(options.map { option ->
    UnifiedSelectionCandidate(
        candidateId = clocktowerInformationCandidateId(option),
        familyId = familyId,
        legality = UnifiedCandidateLegality.LEGAL,
        epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
        qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
        rankFixedPoint = when {
            option.isDefaultRecommendation -> 1_000_000L
            option.recommendationStyle == automaticStyle -> 900_000L
            else -> 800_000L
        },
        reasonCodes = option.reasonCodes,
        warningCodes = option.warningCodes,
        payload = option,
    )
})

private data class ClocktowerRegistrationRecommendationOption(
    val label: String,
    val usesSpecialRegistration: Boolean,
    val registeredRoleEnName: String?,
    val style: RecommendationStyle,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

/** Shared registration pool: style changes selection, never candidate legality or ordering data. */
private fun unifiedRegistrationPool(
    options: List<ClocktowerRegistrationRecommendationOption>,
): UnifiedSelectionPool<ClocktowerRegistrationRecommendationOption>? = options
    .takeIf { it.isNotEmpty() }
    ?.let { candidates ->
        UnifiedSelectionPool(candidates.map { option ->
            UnifiedSelectionCandidate(
                candidateId = listOf(
                    option.style.name,
                    option.usesSpecialRegistration,
                    option.registeredRoleEnName.orEmpty(),
                ).joinToString("|"),
                familyId = if (option.usesSpecialRegistration) "special-registration" else "actual-registration",
                legality = UnifiedCandidateLegality.LEGAL,
                epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
                qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
                rankFixedPoint = if (option.isDefaultRecommendation) 1_000_000L else 800_000L,
                reasonCodes = option.reasonCodes,
                warningCodes = option.warningCodes,
                payload = option,
            )
        })
    }

private fun recommendationReasonLabel(code: String, language: String): String {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    return when (code) {
        "truth-distance" -> text("结果与真实值的距离符合当前风格", "Distance from truth fits the selected style")
        "extreme-pressure" -> text("限制极端数字造成的压力", "Limits pressure from extreme numbers")
        "history-continuity" -> text("与此前信息保持可解释的连续性", "Maintains explainable continuity with earlier information")
        "dynamic.categorical-score", "dynamic.pair-score" -> text("完整信息结果通过场景评分", "The complete information result passed scenario scoring")
        "special-registration" -> text("本结果使用一次合法的特殊登记", "This result uses a legal special registration")
        "registration-discussion-value" -> text("登记结果保留有意义的讨论空间", "The registration preserves useful discussion")
        "registration-pressure" -> text("登记造成的信息压力符合当前风格", "Registration pressure fits the selected style")
        "registration-history" -> text("已考虑该玩家此前的登记次数", "Prior registrations for this player were considered")
        "global-balance", "consequence.alignment-advantage-adjustment" -> text("根据当前阵营优势修正", "Adjusted for the current alignment advantage")
        "pressure.repeated-target-penalty" -> text("避免持续针对同一名玩家", "Avoids repeatedly targeting the same player")
        "consequence.one-shot-ability-protection" -> text("保护一次性能力的核心体验", "Protects the core experience of a one-shot ability")
        "consequence.high-impact-misinformation-penalty" -> text("近期高冲击误导较多，已降低权重", "Recent high-impact misinformation lowers this option's weight")
        "consequence.final-day-impact-penalty" -> text("终局结果可能直接决定胜负", "This final-day result could decide the game")
        "successor-role-suitability" -> text("继任角色与当前局面相符", "The successor role fits the current state")
        "successor-public-pressure" -> text("已考虑该玩家的公开压力", "Public pressure on this player was considered")
        "scarlet-woman-mandatory" -> text("当前人数下必须由红唇女郎继任", "The Scarlet Woman must succeed at this player count")
        "mayor-survival" -> text("市长存活价值已纳入评估", "The value of keeping the Mayor alive was considered")
        else -> code
    }
}

@Composable
private fun RecommendationReasonSummary(
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

private enum class ClocktowerRegistrationDetail {
    AlignmentOnly,
    Role,
}

private enum class ClocktowerPairInformationAbility {
    Washerwoman,
    Librarian,
    Investigator,
}

private data class ClocktowerNightStepUi(
    val title: String,
    val actor: PlayerCard?,
    val isRealAction: Boolean,
    val reason: String,
    val storytellerAction: String,
    val tellPlayer: String?,
    val explanation: String,
    val action: ClocktowerNightAction = ClocktowerNightAction.None,
    val displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.None,
    val displayTitle: String = title,
    val displayPrimary: String? = null,
    val displaySecondary: String? = null,
    val displayFooter: String? = null,
    val displayProposition: InformationProposition? = null,
    val displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    val recommendedDisplayOptions: List<ClocktowerDisplayOption> = emptyList(),
    /**
     * The complete candidate pool emitted by the legacy helper.  This is kept
     * separate from the UI lists because automatic mode deliberately renders
     * only its selected recommendation.  Batch 4 compares this full pool at
     * the display boundary before allowing the migrated lifecycle to commit.
     */
    val legacyInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    val decisionOptions: List<ClocktowerDecisionOption> = emptyList(),
    val wakeText: String? = null,
    val roleEnName: String? = null,
    val informationReliability: InformationReliability = InformationReliability.RELIABLE,
    val recentMisinformationStreak: Int = 0,
    val selectedInformationTruthful: Boolean? = null,
    val spyRegistrationKey: String? = null,
    val spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    val spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    val spyRegistrationHint: String? = null,
    val recluseRegistrationKey: String? = null,
    val recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerJudgeScreen(
    automaticStorytellerInfo: Boolean,
    automaticStorytellerStyle: RecommendationStyle,
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    events: List<ClocktowerEvent>,
    script: ClocktowerScript,
    gameId: String,
    gameSeed: Long,
    gameStateRevision: Long,
    playerInputRevision: Long,
    rulesetRef: RulesetRef?,
    setupHistory: CrossGameHistory,
    onInitialRecommendationDemand: () -> Unit,
    phase: ClocktowerPhase,
    round: Int,
    pendingNightDeath: String?,
    demonAttackDraftTarget: String?,
    selectedExecution: String?,
    poisonTarget: String?,
    poisonDraftTarget: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    ravenkeeperTarget: String?,
    redHerring: String?,
    recommendedDemonBluffRoleNames: List<String>,
    recommendedDrunkInvestigatorRoleName: String?,
    recommendedDrunkInvestigatorSeats: List<Int>,
    butlerMaster: String?,
    monkProtectedTarget: String?,
    monkProtectedDraftTarget: String?,
    mayorRedirectTarget: String?,
    mayorRedirectDraftTarget: String?,
    pendingNewDemonName: String?,
    pendingNightNewDemonIdentityName: String?,
    demonSuccessorTarget: String?,
    virginUsed: Boolean,
    slayerUsed: Boolean,
    slayerClaimedNames: List<String>,
    artistUsed: Boolean,
    artistClaimedNames: List<String>,
    artistClaimantName: String?,
    artistTruthfulAnswer: Boolean?,
    artistShownAnswer: Boolean?,
    lastExecutedName: String?,
    pendingKlutzName: String?,
    klutzChoiceName: String?,
    nightStartedState: MutableState<Boolean>,
    nightStepIndexState: MutableState<Int>,
    dayModeState: MutableState<ClocktowerDayMode>,
    nominatorNameState: MutableState<String?>,
    nomineeNameState: MutableState<String?>,
    currentVoteCountState: MutableState<Int>,
    highestVoteNameState: MutableState<String?>,
    highestVoteCountState: MutableState<Int>,
    slayerClaimantNameState: MutableState<String?>,
    slayerTargetNameState: MutableState<String?>,
    gameOutcome: GameOutcome?,
    onRecordEvent: (ClocktowerEventType, String, String, List<String>) -> Unit,
    onRecordEpistemicObservation: (RecordedEpistemicObservation) -> Unit,
    onPhaseChange: (ClocktowerPhase) -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onConfirmDemonAttack: () -> Unit,
    onSelectExecution: (String?) -> Unit,
    onSelectPoisonTarget: (String?) -> Unit,
    onConfirmPoisonTarget: () -> Unit,
    onSelectFortuneTellerFirst: (String?) -> Unit,
    onSelectFortuneTellerSecond: (String?) -> Unit,
    onSelectChambermaidFirst: (String?) -> Unit,
    onSelectChambermaidSecond: (String?) -> Unit,
    onSelectRavenkeeperTarget: (String?) -> Unit,
    onSelectRedHerring: (String?) -> Unit,
    onApplyRecommendation: (RecommendationPlan) -> Unit,
    onSelectButlerMaster: (String?) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onConfirmMonkProtectedTarget: () -> Unit,
    onSelectMayorRedirectTarget: (String?) -> Unit,
    onConfirmMayorRedirectTarget: () -> Unit,
    onSelectDemonSuccessor: (String?) -> Unit,
    onConfirmNewDemon: () -> Unit,
    onSelectKlutzChoice: (String?) -> Unit,
    onConfirmKlutzChoice: (Boolean) -> Unit,
    onSelectArtistClaimant: (String?) -> Unit,
    onSelectArtistTruthfulAnswer: (Boolean?) -> Unit,
    onSelectArtistShownAnswer: (Boolean?) -> Unit,
    onConfirmArtistQuestion: () -> Unit,
    onSlayerShot: (String, String, Boolean) -> Unit,
    onVirginNomination: (String, String, Boolean) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: () -> Unit,
    onConfirmNight: () -> Unit,
    onShowResults: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val recommendationCoordinator = remember(gameSeed) { ClocktowerRecommendationCoordinator() }
    // Aggregate-only C8 telemetry lives for this game UI session. The recorder
    // de-duplicates stable decision IDs, so Compose recomposition is not a new selection.
    val selectionDistributionTelemetry = remember(gameId) { SelectionDistributionTelemetryRecorder() }
    // B7.2 shadow telemetry stores only parity totals; candidate IDs and game facts stay local.
    val firstNightPoolParity = remember(gameId) { SelectionPoolParityRecorder() }
    var a4DeviceBenchmarkReport by remember { mutableStateOf<A4DeviceBenchmarkReport?>(null) }
    var a4DeviceBenchmarkRuns by remember { mutableStateOf(0) }
    var a4DeviceBenchmarkError by remember { mutableStateOf<String?>(null) }
    var a4PrewarmCancellationProbeRuns by remember { mutableStateOf(0) }
    var a4PrewarmCancellationProbeResult by remember { mutableStateOf<String?>(null) }
    var a4PrewarmCancellationProbeError by remember { mutableStateOf<String?>(null) }
    var unifiedSetupSelectorBenchmarkRuns by remember { mutableStateOf(0) }
    var unifiedSetupSelectorBenchmarkReport by remember { mutableStateOf<UnifiedSetupSelectorDeviceBenchmarkReport?>(null) }
    var unifiedSetupSelectorBenchmarkError by remember { mutableStateOf<String?>(null) }
    var debugDiagnosticsExpanded by remember { mutableStateOf(false) }
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val aliveCards = cards.filter { it.eliminatedRound == null }
    // The UI still owns rendering, but first-night information now crosses one
    // common lifecycle boundary before it is committed to the event log.
    var firstNightInformationMigration by remember(gameId, round) {
        mutableStateOf(FirstNightInformationMigration())
    }
    var observedFirstNightPoisonTarget by remember(gameId, round) { mutableStateOf<String?>(null) }
    var hasObservedFirstNightPoisonTarget by remember(gameId, round) { mutableStateOf(false) }
    LaunchedEffect(phase, poisonTarget) {
        if (phase != ClocktowerPhase.FirstNight) return@LaunchedEffect
        if (hasObservedFirstNightPoisonTarget && observedFirstNightPoisonTarget != poisonTarget) {
            firstNightInformationMigration = firstNightInformationMigration.invalidateUnshown()
        }
        observedFirstNightPoisonTarget = poisonTarget
        hasObservedFirstNightPoisonTarget = true
    }

    fun firstNightMigrationRequest(displayStep: ClocktowerNightStepUi): FirstNightInformationRequest? {
        if (phase != ClocktowerPhase.FirstNight) return null
        val actor = displayStep.actor ?: return null
        val family = FirstNightInformationFamily.entries.firstOrNull { it.role.value == displayStep.roleEnName } ?: return null
        val sourceSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return null
        val reliability = when (displayStep.informationReliability) {
            InformationReliability.RELIABLE -> ReliabilityState.RELIABLE
            InformationReliability.DRUNK -> ReliabilityState.DRUNK
            InformationReliability.POISONED -> ReliabilityState.POISONED
        }
        fun candidate(option: ClocktowerDisplayOption): FirstNightInformationCandidate {
            val primary = option.displayPrimary
            return FirstNightInformationCandidate(clocktowerInformationCandidateId(option), AbilityObservation(
                sourceSeat = sourceSeat,
                perceivedRole = family.role,
                shownRole = primary?.takeIf { option.displayKind == ClocktowerDisplayKind.EitherOne }
                    ?.let { shown -> clocktowerRolesForScript(script).firstOrNull { it.nameFor(language) == shown }?.enName ?: shown }
                    ?.let(::RoleId),
                candidateSeats = DecisionHistoryRepository.extractSeatNumbers(
                    listOf(option.displaySecondary, option.displayFooter), cards.size,
                ).toList(),
                shownNumber = primary?.toIntOrNull(),
                shownAnswer = primary?.let { answer ->
                    when (answer) {
                        "有", "Yes" -> YesNoAnswer.YES
                        "没有", "No" -> YesNoAnswer.NO
                        else -> null
                    }
                },
                reliability = reliability,
                semanticTruth = if (option.isTruthful) SemanticTruth.TRUE else SemanticTruth.FALSE,
            ),
                qualityTier = if (option.isDefaultRecommendation) {
                    QualityTier.RECOMMENDED
                } else {
                    QualityTier.ACCEPTABLE_WITH_WARNING
                },
                rankFixedPoint = when {
                    option.isDefaultRecommendation -> 1_000_000L
                    option.recommendationStyle == automaticStorytellerStyle -> 900_000L
                    else -> 800_000L
                },
                reasonCodes = option.reasonCodes,
                warningCodes = option.warningCodes,
            )
        }
        val selectedOption = ClocktowerDisplayOption(
            label = "selected",
            displayKind = displayStep.displayKind,
            displayTitle = displayStep.displayTitle,
            displayPrimary = displayStep.displayPrimary ?: displayStep.tellPlayer,
            displaySecondary = displayStep.displaySecondary,
            displayFooter = displayStep.displayFooter,
            proposition = displayStep.displayProposition,
            isTruthful = displayStep.selectedInformationTruthful != false,
            recommendationStyle = automaticStorytellerStyle,
        )
        // The selected statement is included as a defensive fallback for a
        // direct/manual legacy path that has no option list.
        val legacyOptions = (displayStep.legacyInformationCandidates + selectedOption)
            .distinctBy(::clocktowerInformationCandidateId)
        val legacyCandidates = legacyOptions.map(::candidate)
        // The migration adapter intentionally rebuilds typed candidates from
        // the complete legacy pool rather than treating the chosen UI row as
        // the candidate set.  A later generator can replace this side without
        // changing the display/commit boundary.
        val migratedCandidates = legacyOptions.map(::candidate)
        return FirstNightInformationRequest(
            decisionId = "first-night:${phase.name}:$round:${family.name}:$sourceSeat",
            family = family,
            sourceSeat = sourceSeat,
            reliability = reliability,
            selectedCandidateId = clocktowerInformationCandidateId(selectedOption),
            legacyCandidates = legacyCandidates,
            migratedCandidates = migratedCandidates,
        )
    }
    val a4DiagnosticAvailable = BuildConfig.DEBUG && script == ClocktowerScript.TroubleBrewing &&
        cards.size == 5 && rulesetRef != null && cards.all { it.clocktowerRole != null }
    LaunchedEffect(a4DeviceBenchmarkRuns) {
        if (a4DeviceBenchmarkRuns == 0) return@LaunchedEffect
        val activeRuleset = rulesetRef ?: return@LaunchedEffect
        a4DeviceBenchmarkError = null
        a4DeviceBenchmarkReport = null
        runCatching {
            withContext(Dispatchers.Default) {
                val gameState = cards.toClocktowerGameState(script, gameSeed, poisonTarget)
                val snapshot = GameSnapshot(
                    gameId = gameId.ifBlank { "a4-device-diagnostic" },
                    gameStateRevision = gameStateRevision,
                    playerInputRevision = playerInputRevision,
                    gameSeed = gameSeed,
                    rulesetRef = activeRuleset,
                    gameState = gameState,
                )
                val formal = FormalGameState.from(snapshot, when (phase) {
                    ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
                    ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
                    ClocktowerPhase.Day -> StorytellerPhase.DAY
                    ClocktowerPhase.Night -> StorytellerPhase.NIGHT
                }, round)
                val perceivedRoles = cards.mapIndexed { index, card ->
                    index + 1 to RoleId(requireNotNull(card.clocktowerShownRole ?: card.clocktowerRole).enName)
                }.toMap()
                // Multi-night timeline replay belongs to B4. The A4 device harness intentionally
                // measures the current structural fixture plus its synthetic probes only.
                val knowledge = A4PlayerKnowledgeFactory.createAll(
                    formal = formal,
                    perceivedRolesBySeat = perceivedRoles,
                    observationLog = EpistemicObservationLog(),
                ).first()
                fun publicObservation(id: String, proposition: InformationProposition) = EpistemicObservation(
                    id, formal.snapshotId, formal.phase, formal.round, 0, null, null,
                    ObservationVisibility.PUBLIC, emptySet(), ObservationReliability.NOT_ABILITY_INFORMATION, proposition,
                )
                // Always include one synthetic private numeric observation so the device gate can
                // measure decode/rebuild on any legal 5-player draw. It is never displayed,
                // persisted, or fed to recommendation logic; real ability observations retain
                // their own role and recipient semantics elsewhere.
                val fallbackSeat = cards.indexOfFirst { it.eliminatedRound == null }.plus(1)
                check(fallbackSeat > 0) { "A4 diagnostic requires one living recipient." }
                val numericFallbackCase = A4DeviceBenchmarkCase(
                    "numeric-synthetic-fallback",
                    EpistemicObservation(
                        "a4-device-numeric", formal.snapshotId, formal.phase, formal.round, 1,
                        fallbackSeat, RoleId("Chef"),
                        ObservationVisibility.PRIVATE, setOf(fallbackSeat),
                        ObservationReliability.RECEIVED_AS_FUNCTIONING,
                        InformationProposition.NumericResult(
                            NumericMetric.ADJACENT_EVIL_PAIRS,
                            fallbackSeat,
                            (1..cards.size).toList(),
                            1,
                        ),
                    ),
                    ZddFilterStrategy.DECODE_REBUILD,
                )
                A4DeviceBenchmarkHarness.run(
                    deviceLabel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                    formal = formal,
                    knowledge = knowledge,
                    hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                    roleDefinitions = clocktowerRoleDefinitionsForScript(script),
                    cases = listOf(
                        A4DeviceBenchmarkCase("alive-seat-2", publicObservation("a4-device-alive", InformationProposition.AliveAt(2, true)), ZddFilterStrategy.NATIVE_RESTRICTION),
                        A4DeviceBenchmarkCase("spy-absent", publicObservation("a4-device-spy", InformationProposition.RoleInPlay(RoleId("Spy"), false)), ZddFilterStrategy.NATIVE_RESTRICTION),
                        numericFallbackCase,
                    ),
                )
            }
        }.onSuccess { a4DeviceBenchmarkReport = it }
            .onFailure { a4DeviceBenchmarkError = it.message ?: it.javaClass.simpleName }
    }
    LaunchedEffect(a4PrewarmCancellationProbeRuns) {
        if (a4PrewarmCancellationProbeRuns == 0) return@LaunchedEffect
        a4PrewarmCancellationProbeResult = null
        a4PrewarmCancellationProbeError = null
        runCatching {
            check(a4DiagnosticAvailable) { "A4 prewarm diagnostic is unavailable for this game." }
            val activeRuleset = requireNotNull(rulesetRef)
            val snapshot = GameSnapshot(
                gameId = gameId.ifBlank { "a4-prewarm-diagnostic" },
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                gameSeed = gameSeed,
                rulesetRef = activeRuleset,
                gameState = cards.toClocktowerGameState(script, gameSeed, poisonedPlayerName = null),
            )
            val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, round = 1)
            val perceivedRoles = cards.mapIndexed { index, card ->
                index + 1 to RoleId(requireNotNull(card.clocktowerShownRole ?: card.clocktowerRole).enName)
            }.toMap()
            val request = A4IdentityRevealPrewarmRequest(
                formal = formal,
                playerInputRevision = playerInputRevision,
                knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
                    formal = formal,
                    perceivedRolesBySeat = perceivedRoles,
                    observationLog = EpistemicObservationLog(),
                ).associateBy(PlayerKnowledgeSnapshot::recipientSeat),
                revealOrder = cards.indices.map { it + 1 },
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = clocktowerRoleDefinitionsForScript(script),
            )
            // This coordinator and its cache are deliberately isolated from the live prewarmer.
            // The probe reads a snapshot only and must prove its cancelled result is not reusable.
            val coordinator = A4IdentityRevealPrewarmCoordinator()
            val session = coordinator.start(request)
            val frameTelemetry = A4MainThreadFrameTelemetry()
            val frameMonitor = launch {
                while (isActive) withFrameNanos(frameTelemetry::recordFrame)
            }
            try {
                val worker = async(Dispatchers.Default) {
                    coordinator.run(session, prioritizedRecipientSeat = 1)
                }
                // Allow an exact build to enter the worker, then cancel at a main-thread frame
                // boundary so this device run exercises the stale-publication guarantee.
                withFrameNanos { }
                withFrameNanos { }
                val cancellation = coordinator.cancel(session)
                val report = worker.await()
                check(report.entries.any { it.status.name == "STALE" }) {
                    "Cancellation probe did not observe an in-flight stale result."
                }
                check(report.entries.all { coordinator.ready(it.key) == null }) {
                    "Cancellation probe exposed a cancelled shadow result."
                }
                val summary = frameTelemetry.summary()
                val logLine = report.toLogLine(summary) + " " + cancellation.toLogLine() +
                    " verification=stale-not-published"
                Log.i(A4_IDENTITY_PREWARM_LOG_TAG, logLine)
                logLine
            } finally {
                frameMonitor.cancel()
            }
        }.onSuccess { a4PrewarmCancellationProbeResult = it }
            .onFailure { error ->
                a4PrewarmCancellationProbeError = error.message ?: error.javaClass.simpleName
                Log.e(A4_IDENTITY_PREWARM_LOG_TAG, "A4 prewarm cancellation probe failed", error)
            }
    }
    val spyCard = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }
    val recluseCard = cards.firstOrNull { it.clocktowerRole?.enName == "Recluse" }
    val spyRegistrationGood = remember { mutableStateMapOf<String, Boolean>() }
    val spyRegistrationRole = remember { mutableStateMapOf<String, String>() }
    val recordedSpyRegistrations = remember { mutableStateMapOf<String, Boolean>() }
    val recluseRegistrationEvil = remember { mutableStateMapOf<String, Boolean>() }
    val recluseRegistrationRole = remember { mutableStateMapOf<String, String>() }
    val recordedRecluseRegistrations = remember { mutableStateMapOf<String, Boolean>() }
    val recordedNightSteps = remember { mutableStateMapOf<String, Boolean>() }
    fun registrationKey(ability: String, subject: String = "spy") = "${phase.name}:$round:$ability:$subject"
    fun spyCanRegister(): Boolean = spyCard != null && poisonTarget != spyCard.name
    fun spyRegistersGood(key: String?): Boolean = key != null && spyCanRegister() && spyRegistrationGood[key] == true
    fun registeredRole(key: String?, teams: List<ClocktowerTeam>): ClocktowerRole? {
        if (!spyRegistersGood(key)) return spyCard?.clocktowerRole
        val allowed = completeTroubleBrewingRoles.filter { it.team in teams && it.enName != "Spy" }
        return allowed.firstOrNull { it.enName == spyRegistrationRole[key] } ?: allowed.firstOrNull()
    }
    fun recordSpyRegistration(
        key: String?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    ) {
        if (key == null || recordedSpyRegistrations[key] == true || spyCard == null) return
        recordedSpyRegistrations[key] = true
        val registrationDetail = when {
            !spyCanRegister() -> text("中毒，按真实邪恶身份登记", "poisoned; registered as actual evil identity")
            !spyRegistersGood(key) -> text("按真实邪恶身份登记", "registered as actual evil identity")
            detail == ClocktowerRegistrationDetail.AlignmentOnly -> text("登记为善良", "registered as good")
            else -> text(
                "登记为${registeredRole(key, teams)?.nameFor(language).orEmpty()}",
                "registered as ${registeredRole(key, teams)?.nameFor(language).orEmpty()}",
            )
        }
        onRecordEvent(
            ClocktowerEventType.RoleAction,
            text("间谍登记裁定", "Spy registration"),
            "${spyCard.seatLabel(cards)} · $registrationDetail",
            listOf(spyCard.name),
        )
    }
    fun recluseCanRegister(): Boolean = recluseCard != null && poisonTarget != recluseCard.name
    fun recluseRegistersEvil(key: String?): Boolean =
        key != null && recluseCanRegister() && recluseRegistrationEvil[key] == true
    fun recluseRegisteredRole(key: String?, teams: List<ClocktowerTeam>): ClocktowerRole? {
        if (!recluseRegistersEvil(key)) return recluseCard?.clocktowerRole
        val allowed = completeTroubleBrewingRoles.filter { it.team in teams }
        return allowed.firstOrNull { it.enName == recluseRegistrationRole[key] } ?: allowed.firstOrNull()
    }
    fun recordRecluseRegistration(key: String?, teams: List<ClocktowerTeam>) {
        if (key == null || !recluseRegistersEvil(key) || recordedRecluseRegistrations[key] == true || recluseCard == null) return
        recordedRecluseRegistrations[key] = true
        val registeredAs = recluseRegisteredRole(key, teams)
        onRecordEvent(
            ClocktowerEventType.RoleAction,
            text("隐士登记裁定", "Recluse registration"),
            if (registeredAs != null && teams.isNotEmpty()) {
                "${recluseCard.seatLabel(cards)} → ${registeredAs.nameFor(language)}"
            } else {
                text("${recluseCard.seatLabel(cards)} → 邪恶", "${recluseCard.seatLabel(cards)} → evil")
            },
            listOf(recluseCard.name),
        )
    }
    val firstNightWasherwoman = actualClocktowerRoleCards(cards, "Washerwoman").firstOrNull()
    val firstNightLibrarian = actualClocktowerRoleCards(cards, "Librarian").firstOrNull()
    val firstNightInvestigator = actualClocktowerRoleCards(cards, "Investigator").firstOrNull()
    val chefPlayer = actualClocktowerRoleCards(cards, "Chef").firstOrNull()
    val empathPlayers = actualClocktowerRoleCards(cards, "Empath").filter { it.eliminatedRound == null }
    val fortuneTellerPlayers = actualClocktowerRoleCards(cards, "Fortune Teller").filter { it.eliminatedRound == null }
    val poisonerPlayers = actualClocktowerRoleCards(cards, "Poisoner").filter { it.eliminatedRound == null }
    val butlerPlayers = actualClocktowerRoleCards(cards, "Butler").filter { it.eliminatedRound == null }
    val demonPoisonedTonight = poisonTarget?.let { name ->
        cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
    } == true
    val mayorTarget = pendingNightDeath
        ?.let { name -> cards.firstOrNull { it.name == name && it.eliminatedRound == null && it.clocktowerRole?.enName == "Mayor" } }
    val mayorCanRedirect =
        mayorTarget != null &&
            !demonPoisonedTonight &&
            poisonTarget != mayorTarget.name &&
            monkProtectedTarget != mayorTarget.name
    val resolvedNightDeathName = if (mayorCanRedirect && mayorRedirectTarget != null) mayorRedirectTarget else pendingNightDeath
    val resolvedNightDeathCard = resolvedNightDeathName?.let { name -> cards.firstOrNull { it.name == name } }
    val nightDeathWillOccur =
        resolvedNightDeathCard != null &&
            !demonPoisonedTonight &&
            resolvedNightDeathCard.eliminatedRound == null &&
            resolvedNightDeathCard.name != monkProtectedTarget &&
            !(resolvedNightDeathCard.clocktowerRole?.enName == "Soldier" && poisonTarget != resolvedNightDeathName)
    val ravenkeeperTrigger = resolvedNightDeathCard
        ?.takeIf { nightDeathWillOccur && it.clocktowerRole?.enName == "Ravenkeeper" }

    val fortuneTellerRecluseRegistrationKey = recluseCard
        ?.takeIf { it.name == fortuneTellerFirst || it.name == fortuneTellerSecond }
        ?.let { registrationKey("FortuneTellerRecluse", it.name) }
    val fortuneTellerMatched = if (fortuneTellerFirst != null && fortuneTellerSecond != null) {
        val targets = setOf(fortuneTellerFirst, fortuneTellerSecond)
        aliveCards.any {
            it.name in targets && (
                it.clocktowerTeam == ClocktowerTeam.Demon ||
                    it.name == redHerring ||
                    (it.name == recluseCard?.name && recluseRegistersEvil(fortuneTellerRecluseRegistrationKey))
                )
        }
    } else {
        null
    }
    val fortuneTellerResult = fortuneTellerMatched?.let { matched ->
        if (matched) stringResource(R.string.clocktower_yes) else stringResource(R.string.clocktower_no)
    }
    fun clockmakerNumber(): Int {
        val demonIndex = cards.indexOfFirst { it.clocktowerTeam == ClocktowerTeam.Demon }
        val minionIndexes = cards.mapIndexedNotNull { index, card -> index.takeIf { card.clocktowerTeam == ClocktowerTeam.Minion } }
        if (demonIndex < 0 || minionIndexes.isEmpty() || cards.isEmpty()) return 0
        return minionIndexes.minOf { minionIndex ->
            val clockwise = (minionIndex - demonIndex + cards.size) % cards.size
            val counterClockwise = (demonIndex - minionIndex + cards.size) % cards.size
            minOf(clockwise, counterClockwise)
        }
    }
    fun chambermaidWakeRoles(): Set<String> = if (phase == ClocktowerPhase.FirstNight) {
        setOf("Clockmaker", "Investigator", "Empath", "Chambermaid", "Spy")
    } else {
        buildSet {
            add("Chambermaid")
            add("Empath")
            add("Poisoner")
            add("Fortune Teller")
            add("Butler")
            add("Monk")
            add("Imp")
            add("Spy")
            if (lastExecutedName != null) add("Undertaker")
            if (ravenkeeperTrigger != null) add("Ravenkeeper")
        }
    }
    val chambermaidResult = if (chambermaidFirst != null && chambermaidSecond != null) {
        val targets = setOf(chambermaidFirst, chambermaidSecond)
        cards.count { it.name in targets && it.clocktowerRole?.enName in chambermaidWakeRoles() }.toString()
    } else {
        null
    }
    fun recordNightStep(step: ClocktowerNightStepUi) {
        if (!step.isRealAction || step.action == ClocktowerNightAction.DemonKill) return
        // Information shown to a player is recorded by onShowPlayerDisplay with the
        // final displayed result. Unreliable roles (including the Drunk) keep
        // displayKind=None until an option is chosen, so also identify them by
        // their information action/options.
        if (
            step.action == ClocktowerNightAction.None ||
            step.action in setOf(
                ClocktowerNightAction.FortuneTeller,
                ClocktowerNightAction.Chambermaid,
                ClocktowerNightAction.Ravenkeeper,
            ) ||
            step.displayKind != ClocktowerDisplayKind.None ||
            step.displayOptions.isNotEmpty() ||
            step.recommendedDisplayOptions.isNotEmpty()
        ) return
        val recordKey = "${phase.name}:$round:${step.action.name}:${step.actor?.name.orEmpty()}:${step.title}"
        val alreadyRecorded = recordedNightSteps[recordKey] == true || events.any { event ->
            event.type == ClocktowerEventType.RoleAction &&
                event.phase == phase &&
                event.round == round &&
                event.title == step.title &&
                (step.actor == null || step.actor.name in event.playerNames)
        }
        if (alreadyRecorded) {
            recordedNightSteps[recordKey] = true
            return
        }
        val names = when (step.action) {
            ClocktowerNightAction.RedHerring -> listOfNotNull(redHerring)
            ClocktowerNightAction.Poison -> listOfNotNull(step.actor?.name, poisonTarget)
            ClocktowerNightAction.ButlerMaster -> listOfNotNull(step.actor?.name, butlerMaster)
            ClocktowerNightAction.MonkProtect -> listOfNotNull(step.actor?.name, monkProtectedTarget)
            ClocktowerNightAction.FortuneTeller -> listOfNotNull(step.actor?.name, fortuneTellerFirst, fortuneTellerSecond)
            ClocktowerNightAction.Chambermaid -> listOfNotNull(step.actor?.name, chambermaidFirst, chambermaidSecond)
            ClocktowerNightAction.NewDemonIdentity -> listOfNotNull(step.actor?.name)
            ClocktowerNightAction.DemonKill -> listOfNotNull(step.actor?.name, pendingNightDeath)
            ClocktowerNightAction.MayorRedirect -> listOfNotNull(mayorRedirectTarget)
            ClocktowerNightAction.DemonSuccessor -> listOfNotNull(demonSuccessorTarget)
            ClocktowerNightAction.Ravenkeeper -> listOfNotNull(step.actor?.name, ravenkeeperTarget)
            ClocktowerNightAction.None -> listOfNotNull(step.actor?.name)
        }
        val detail = when (step.action) {
            ClocktowerNightAction.RedHerring ->
                text("红鲱鱼：${playerSeatLabel(cards, redHerring)}", "Red herring: ${playerSeatLabel(cards, redHerring)}")
            ClocktowerNightAction.Poison ->
                text("中毒目标：${playerSeatLabel(cards, poisonTarget)}", "Poisoned: ${playerSeatLabel(cards, poisonTarget)}")
            ClocktowerNightAction.ButlerMaster ->
                text("今日主人：${playerSeatLabel(cards, butlerMaster)}", "Master: ${playerSeatLabel(cards, butlerMaster)}")
            ClocktowerNightAction.MonkProtect ->
                text("保护目标：${playerSeatLabel(cards, monkProtectedTarget)}", "Protected: ${playerSeatLabel(cards, monkProtectedTarget)}")
            ClocktowerNightAction.FortuneTeller -> {
                val targets = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).joinToString(" + ") { playerSeatLabel(cards, it) }
                text("查验 $targets：$fortuneTellerResult", "Checked $targets: $fortuneTellerResult")
            }
            ClocktowerNightAction.Chambermaid -> {
                val targets = listOfNotNull(chambermaidFirst, chambermaidSecond).joinToString(" + ") { playerSeatLabel(cards, it) }
                text("查验 $targets：$chambermaidResult 人今晚醒来", "Checked $targets: $chambermaidResult woke tonight")
            }
            ClocktowerNightAction.NewDemonIdentity -> step.tellPlayer ?: step.storytellerAction
            ClocktowerNightAction.DemonKill ->
                text("击杀目标：${playerSeatLabel(cards, pendingNightDeath)}", "Kill target: ${playerSeatLabel(cards, pendingNightDeath)}")
            ClocktowerNightAction.MayorRedirect -> mayorRedirectTarget?.let { target ->
                val mayor = cards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                if (target == mayor?.name)
                    text("市长死亡", "Mayor dies")
                else
                    text("死亡转移给 ${playerSeatLabel(cards, target)}", "Death redirected to ${playerSeatLabel(cards, target)}")
            }.orEmpty()
            ClocktowerNightAction.DemonSuccessor ->
                text("新恶魔：${playerSeatLabel(cards, demonSuccessorTarget)}", "New Demon: ${playerSeatLabel(cards, demonSuccessorTarget)}")
            ClocktowerNightAction.Ravenkeeper ->
                text("查验：${playerSeatLabel(cards, ravenkeeperTarget)}", "Checked: ${playerSeatLabel(cards, ravenkeeperTarget)}")
            ClocktowerNightAction.None -> step.tellPlayer ?: step.storytellerAction
        }
        onRecordEvent(ClocktowerEventType.RoleAction, step.title, detail, names)
        recordedNightSteps[recordKey] = true
    }
    val phaseTitle = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_phase_first_night)
        ClocktowerPhase.Dawn -> text("天亮", "Dawn")
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_phase_day, round)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_phase_night, round)
    }
    val phaseProgress = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_progress_first_night)
        ClocktowerPhase.Dawn -> text("天亮", "Dawn")
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_progress_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_progress_night)
    }
    val phaseScript = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_script_first_night)
        ClocktowerPhase.Dawn -> text("天亮了，所有人睁眼。", "Dawn. Everyone, open your eyes.")
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_script_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_script_night)
    }
    val phaseAction = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_action_first_night)
        ClocktowerPhase.Dawn -> text("宣布昨晚死亡，然后进入白天。", "Announce last night's deaths, then begin the day.")
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_action_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_action_night)
    }

    var nightStarted by nightStartedState
    var nightStepIndex by nightStepIndexState
    var dayMode by dayModeState
    var nominatorName by nominatorNameState
    var nomineeName by nomineeNameState
    var currentVoteCount by currentVoteCountState
    var highestVoteName by highestVoteNameState
    var highestVoteCount by highestVoteCountState
    var slayerClaimantName by slayerClaimantNameState
    var slayerTargetName by slayerTargetNameState
    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    var slayerRecluseRegistersDemon by remember { mutableStateOf(false) }
    val recommendationKey = buildString {
        append(script.name)
        append("|seed:")
        append(gameSeed)
        append("|state:")
        append(gameStateRevision)
        append("|input:")
        append(playerInputRevision)
        append("|phase:")
        append(phase.name)
        append("|round:")
        append(round)
        append("|poison:")
        append(poisonTarget.orEmpty())
        cards.forEachIndexed { index, card ->
            append('|')
            append(index + 1)
            append(':')
            append(card.clocktowerRole?.enName.orEmpty())
        }
    }
    val recommendationCards = cards.toList()
    val committedIdentityDecisions = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" }
        ?.clocktowerShownRole
        ?.let { shownRole ->
            buildList<StorytellerDecision> {
                add(StorytellerDecision.DrunkShownRole(RoleId(shownRole.enName)))
            }
        }
        .orEmpty()
    fun preservingCommittedIdentity(decisions: List<StorytellerDecision>): List<StorytellerDecision> {
        val committedKinds = committedIdentityDecisions.mapTo(hashSetOf(), StorytellerDecision::kind)
        return committedIdentityDecisions + decisions.filterNot { it.kind() in committedKinds }
    }
    var recommendationUiState by remember(recommendationKey) {
        mutableStateOf<RecommendationUiState>(RecommendationUiState.Loading)
    }
    var selectedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf(RecommendationStyle.BALANCED)
    }
    var appliedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf<RecommendationStyle?>(null)
    }
    var lockedRecommendationDecisions by remember(recommendationKey) {
        mutableStateOf(committedIdentityDecisions)
    }
    LaunchedEffect(recommendationKey, lockedRecommendationDecisions) {
        onInitialRecommendationDemand()
        recommendationUiState = RecommendationUiState.Loading
        val result = withContext(Dispatchers.Default) {
            runCatching {
                recommendationCoordinator.recommendSetup(
                    SetupCoordinationRequest(
                        game = recommendationCards.toClocktowerGameState(
                            script = script,
                            seed = gameSeed,
                            poisonedPlayerName = poisonTarget,
                        ),
                        roles = clocktowerRoleDefinitionsForScript(script),
                        lockedDecisions = lockedRecommendationDecisions,
                        history = setupHistory,
                    ),
                )
            }
        }
        // A changed revision/key cancels this effect. Never publish a completed
        // old generation into the state created for the new first-night input.
        if (!isActive) return@LaunchedEffect
        recommendationUiState = result.fold(
            onSuccess = { constrained ->
                when {
                    constrained.failureCodes.isNotEmpty() -> RecommendationUiState.InvalidLocks(constrained.failureCodes)
                    constrained.plans.isEmpty() -> RecommendationUiState.Empty
                    else -> RecommendationUiState.Ready(constrained.plans)
                }
            },
            onFailure = { error ->
                RecommendationUiState.Error(error.message ?: text("推荐计算失败", "Recommendation failed"))
            },
        )
    }
    LaunchedEffect(automaticStorytellerInfo, automaticStorytellerStyle, recommendationUiState) {
        if (automaticStorytellerInfo && appliedRecommendationStyle != automaticStorytellerStyle) {
            val setupPlans = (recommendationUiState as? RecommendationUiState.Ready)?.plans.orEmpty()
            val automaticPlan = recommendationCoordinator.selectSetupPlan(setupPlans, automaticStorytellerStyle)
            if (automaticPlan != null) {
                fun setupFamily(plan: RecommendationPlan): String = plan.decisions
                    .filterIsInstance<StorytellerDecision.DrunkShownRole>()
                    .singleOrNull()
                    ?.role
                    ?.value
                    ?.let { "drunk-shown-role:$it" }
                    ?: "setup-plan"
                val setupAuditId = "$recommendationKey|setup"
                val setupDimensions = SelectionAuditDimensions(
                    playerCount = cards.size,
                    phase = StorytellerPhase.FIRST_NIGHT,
                    style = automaticStorytellerStyle,
                )
                selectionDistributionTelemetry.recordPreview(
                    SelectionAuditRecord(
                        selectionId = setupAuditId,
                        dimensions = setupDimensions,
                        candidates = setupPlans.map { plan ->
                            SelectionAuditCandidate(
                                familyId = setupFamily(plan),
                                qualityTier = plan.qualityTier,
                            )
                        },
                    ),
                )
                onApplyRecommendation(automaticPlan)
                selectionDistributionTelemetry.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = setupAuditId,
                        dimensions = setupDimensions,
                    selectedFamilyId = setupFamily(automaticPlan),
                    ),
                )
                selectedRecommendationStyle = automaticPlan.style
                appliedRecommendationStyle = automaticPlan.style
            }
        }
    }
    LaunchedEffect(unifiedSetupSelectorBenchmarkRuns) {
        if (unifiedSetupSelectorBenchmarkRuns == 0) return@LaunchedEffect
        unifiedSetupSelectorBenchmarkReport = null
        unifiedSetupSelectorBenchmarkError = null
        runCatching {
            val plans = (recommendationUiState as? RecommendationUiState.Ready)?.plans.orEmpty()
            check(plans.isNotEmpty()) { "Setup selector diagnostic requires a ready setup recommendation." }
            withContext(Dispatchers.Default) {
                UnifiedSetupSelectorDeviceBenchmark.run(
                    coordinator = recommendationCoordinator,
                    plans = plans,
                    playerCount = cards.size,
                    style = automaticStorytellerStyle,
                )
            }
        }.onSuccess { report ->
            unifiedSetupSelectorBenchmarkReport = report
            Log.i(UNIFIED_SETUP_SELECTOR_BENCHMARK_LOG_TAG, report.toLogLine())
        }.onFailure { error ->
            unifiedSetupSelectorBenchmarkError = error.message ?: error.javaClass.simpleName
            Log.e(UNIFIED_SETUP_SELECTOR_BENCHMARK_LOG_TAG, "Unified setup selector diagnostic failed", error)
        }
    }
    val executionThreshold = (aliveCards.size + 1) / 2
    fun recordCurrentVote(): String? {
        if (currentVoteCount >= executionThreshold) {
            when {
                currentVoteCount > highestVoteCount -> {
                    highestVoteName = nomineeName
                    highestVoteCount = currentVoteCount
                }
                currentVoteCount == highestVoteCount -> {
                    highestVoteName = null
                }
            }
        }
        return highestVoteName?.takeIf { highestVoteCount >= executionThreshold }
    }
    val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
    val scriptHasSlayer = "Slayer" in scriptRoleNames
    val scriptHasArtist = "Artist" in scriptRoleNames
    val slayerClaimantCandidates = aliveCards.filter { card ->
        card.name !in slayerClaimedNames && !(slayerUsed && card.clocktowerRole?.enName == "Slayer")
    }
    val artistClaimantCandidates = aliveCards.filter { card ->
        card.name !in artistClaimedNames && !(artistUsed && card.clocktowerRole?.enName == "Artist")
    }

    fun roleActor(enName: String): PlayerCard? =
        cards.firstOrNull {
            it.eliminatedRound == null &&
                (it.clocktowerRole?.enName == enName || (it.clocktowerRole?.enName == "Drunk" && it.clocktowerShownRole?.enName == enName))
        }

    fun roleMissingReason(enName: String): String {
        val roleCard = actualClocktowerRoleCards(cards, enName).firstOrNull()
        val drunkShownAsRole = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" && it.clocktowerShownRole?.enName == enName }
        return when {
            roleCard == null && drunkShownAsRole != null -> ""
            roleCard == null -> text("本局没有这个角色。", "This character is not in play.")
            roleCard.eliminatedRound != null -> text(
                "${roleCard.seatLabel(cards)} 已经死亡，死亡后不再执行这个能力。",
                "${roleCard.seatLabel(cards)} is dead and no longer uses this ability.",
            )
            else -> ""
        }
    }

    fun stableIndex(key: String, size: Int): Int = if (size <= 0) 0 else Math.floorMod(key.hashCode(), size)
    fun actorIsPoisoned(actor: PlayerCard?): Boolean = actor != null && actor.eliminatedRound == null && poisonTarget == actor.name
    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        actor != null && ((actor.clocktowerRole?.enName == "Drunk" && actor.clocktowerShownRole?.enName == enName) || actorIsPoisoned(actor))
    fun orderedPair(first: PlayerCard?, second: PlayerCard?, key: String): Pair<PlayerCard, PlayerCard>? =
        if (first == null || second == null) null else if (stableIndex(key, 2) == 0) first to second else second to first
    fun seatNumberFor(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    fun seatNumbersText(pair: Pair<PlayerCard, PlayerCard>?): String? =
        pair?.let { "${seatNumberFor(it.first)}   ${seatNumberFor(it.second)}" }
    fun displayOption(
        label: String,
        kind: ClocktowerDisplayKind,
        title: String,
        primary: String?,
        secondary: String? = null,
        footer: String? = null,
        proposition: InformationProposition? = null,
        recommendationStyle: RecommendationStyle = RecommendationStyle.BALANCED,
        isTruthful: Boolean = true,
        misinformationPressure: Int = 0,
        isDefaultRecommendation: Boolean = false,
        reasonCodes: List<String> = emptyList(),
        warningCodes: List<String> = emptyList(),
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = kind,
        displayTitle = title,
        displayPrimary = primary,
        displaySecondary = secondary,
        displayFooter = footer,
        proposition = proposition,
        recommendationStyle = recommendationStyle,
        isTruthful = isTruthful,
        misinformationPressure = misinformationPressure,
        isDefaultRecommendation = isDefaultRecommendation,
        reasonCodes = reasonCodes,
        warningCodes = warningCodes,
    )
    fun recommendedDrunkInvestigatorOption(actor: PlayerCard): ClocktowerDisplayOption? {
        if (actor.clocktowerRole?.enName != "Drunk" || actor.clocktowerShownRole?.enName != "Investigator") return null
        val minionRole = recommendedDrunkInvestigatorRoleName
            ?.let { roleName -> clocktowerRolesForScript(script).firstOrNull { it.enName == roleName } }
            ?: return null
        val candidateCards = recommendedDrunkInvestigatorSeats
            .mapNotNull { seat -> cards.getOrNull(seat - 1) }
        if (candidateCards.size != 2) return null
        return displayOption(
            label = text("采用的推荐信息", "Applied recommendation"),
            kind = ClocktowerDisplayKind.EitherOne,
            title = text("调查员信息", "Investigator information"),
            primary = minionRole.nameFor(language),
            secondary = recommendedDrunkInvestigatorSeats.joinToString("   "),
            footer = text("在下面两位玩家之中", "One of these two players"),
            isDefaultRecommendation = true,
        )
    }
    fun previousUnreliableNumber(title: String, actor: PlayerCard): Int? = events
        .asReversed()
        .firstOrNull { event ->
            event.type == ClocktowerEventType.UnreliableInformation &&
                actor.name in event.playerNames &&
                event.title.startsWith(title)
        }
        ?.detail
        ?.let { detail ->
            val payload = when {
                "：" in detail -> detail.substringAfter("：")
                ": " in detail -> detail.substringAfter(": ")
                else -> detail
            }
            Regex("\\d+").find(payload)?.value?.toIntOrNull()
        }

    fun recommendationStyleLabel(style: RecommendationStyle): String = when (style) {
        RecommendationStyle.GENTLE -> text("推荐·稳健", "Recommended · gentle")
        RecommendationStyle.BALANCED -> text("推荐·平衡", "Recommended · balanced")
        RecommendationStyle.AGGRESSIVE -> text("专家·激进", "Expert · aggressive")
    }

    fun recommendedNumberOptions(
        title: String,
        actor: PlayerCard,
        trueValue: Int,
        maxValue: Int,
        footer: String,
        pressureCostPerPoint: Int = 0,
        secondary: String? = null,
        propositionForValue: ((Int) -> InformationProposition)? = null,
    ): List<ClocktowerDisplayOption> {
        return recommendationCoordinator.recommendNumber(
            UnreliableNumberContext(
                trueValue = trueValue,
                minimumValue = 0,
                maximumValue = maxOf(trueValue, maxValue),
                previousShownValue = previousUnreliableNumber(title, actor)
                    ?.takeIf { it in 0..maxOf(trueValue, maxValue) },
                pressureCostPerPoint = pressureCostPerPoint,
            ),
        ).map { recommendation ->
            val styleLabel = recommendationStyleLabel(recommendation.style)
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "$styleLabel：${recommendation.value}$warning",
                kind = ClocktowerDisplayKind.Number,
                title = title,
                primary = recommendation.value.toString(),
                secondary = secondary,
                footer = footer,
                proposition = propositionForValue?.invoke(recommendation.value),
                recommendationStyle = recommendation.style,
                isTruthful = recommendation.value == trueValue,
                misinformationPressure = kotlin.math.abs(recommendation.value - trueValue)
                    .coerceIn(0, 5),
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = recommendation.scoreItems.map { it.ruleId },
                warningCodes = recommendation.warningIds,
            )
        }
    }

    fun recommendedYesNoOptions(
        title: String,
        truthfulYes: Boolean,
        secondary: String?,
        footer: String,
        propositionForValue: ((Boolean) -> InformationProposition)? = null,
    ): List<ClocktowerDisplayOption> {
        val yesText = text("有", "Yes")
        val noText = text("没有", "No")
        val candidates = listOf(
            UnreliableCategoricalCandidate("yes", isTruthful = truthfulYes, misinformationPressure = if (truthfulYes) 0 else 3),
            UnreliableCategoricalCandidate("no", isTruthful = !truthfulYes, misinformationPressure = if (truthfulYes) 3 else 0),
        )
        return recommendationCoordinator.recommendCategory(candidates).map { recommendation ->
            val candidate = candidates.first { it.id == recommendation.candidateId }
            val value = if (recommendation.candidateId == "yes") yesText else noText
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$value$warning",
                kind = ClocktowerDisplayKind.YesNo,
                title = title,
                primary = value,
                secondary = secondary,
                footer = footer,
                proposition = propositionForValue?.invoke(recommendation.candidateId == "yes"),
                recommendationStyle = recommendation.style,
                isTruthful = candidate.isTruthful,
                misinformationPressure = candidate.misinformationPressure,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = listOf("dynamic.categorical-score"),
                warningCodes = recommendation.warningIds,
            )
        }
    }

    fun recommendedRoleRevealOptions(
        title: String,
        truthfulRole: ClocktowerRole?,
        footer: String,
    ): List<ClocktowerDisplayOption> {
        if (truthfulRole == null) return emptyList()
        val roles = (listOf(truthfulRole) + clocktowerRolesForScript(script)).distinctBy(ClocktowerRole::enName)
        val candidates = roles.map { role ->
            val metadata = TroubleBrewingRecommendationMetadata.forRole(RoleId(role.enName))
            UnreliableCategoricalCandidate(
                id = role.enName,
                isTruthful = role.enName == truthfulRole.enName,
                misinformationPressure = if (role.enName == truthfulRole.enName) {
                    0
                } else {
                    ((metadata.exposureSensitivity + metadata.discussionValue) / 2).coerceIn(1, 5)
                },
            )
        }
        return recommendationCoordinator.recommendCategory(candidates).mapNotNull { recommendation ->
            val candidate = candidates.first { it.id == recommendation.candidateId }
            val role = roles.firstOrNull { it.enName == recommendation.candidateId } ?: return@mapNotNull null
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：${role.nameFor(language)}$warning",
                kind = ClocktowerDisplayKind.RoleReveal,
                title = title,
                primary = role.nameFor(language),
                footer = footer,
                recommendationStyle = recommendation.style,
                isTruthful = candidate.isTruthful,
                misinformationPressure = candidate.misinformationPressure,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = listOf("dynamic.categorical-score"),
                warningCodes = recommendation.warningIds,
            )
        }
    }

    fun recommendedSageOptions(
        actor: PlayerCard,
        demon: PlayerCard,
        truthfulOnly: Boolean = false,
    ): List<ClocktowerDisplayOption> {
        val pool = cards.filter { it.name != actor.name }
        val pairs = buildList {
            for (firstIndex in 0 until pool.lastIndex) {
                for (secondIndex in firstIndex + 1 until pool.size) {
                    add(pool[firstIndex] to pool[secondIndex])
                }
            }
        }
        if (pairs.isEmpty()) return emptyList()
        fun pairId(pair: Pair<PlayerCard, PlayerCard>): String = listOf(cards.indexOf(pair.first), cards.indexOf(pair.second)).sorted().joinToString(":")
        val byId = pairs.associateBy(::pairId)
        val candidates = pairs.map { pair ->
            val isTruthful = pair.first.name == demon.name || pair.second.name == demon.name
            val evilCount = listOf(pair.first, pair.second).count(::isClocktowerEvil)
            UnreliableCategoricalCandidate(
                id = pairId(pair),
                isTruthful = isTruthful,
                misinformationPressure = if (isTruthful) 0 else when (evilCount) {
                    1 -> 2
                    2 -> 3
                    else -> 4
                },
            )
        }.filter { !truthfulOnly || it.isTruthful }
        return recommendationCoordinator.recommendCategory(candidates).mapNotNull { recommendation ->
            val candidate = candidates.first { it.id == recommendation.candidateId }
            val pair = byId[recommendation.candidateId] ?: return@mapNotNull null
            val seats = "${seatNumberFor(pair.first)}   ${seatNumberFor(pair.second)}"
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$seats$warning",
                kind = ClocktowerDisplayKind.EitherOne,
                title = text("贤者信息", "Sage information"),
                primary = text("恶魔", "Demon"),
                secondary = seats,
                footer = text("在下面两位玩家之中", "One of these two players"),
                recommendationStyle = recommendation.style,
                isTruthful = candidate.isTruthful,
                misinformationPressure = candidate.misinformationPressure,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = listOf("dynamic.pair-score"),
                warningCodes = recommendation.warningIds,
            )
        }
    }

    data class PairInformationEffect(
        val id: String,
        val shownRole: ClocktowerRole?,
        val target: PlayerCard?,
        val decoy: PlayerCard?,
        val registration: PairInformationRegistration,
    )

    fun informationHistoryPressure(card: PlayerCard?): Int {
        if (card == null) return 0
        return events.count { event ->
            event.type in setOf(ClocktowerEventType.Information, ClocktowerEventType.UnreliableInformation) &&
                card.name in event.playerNames.drop(1)
        }
    }

    fun recentMisinformationStreak(card: PlayerCard?): Int {
        if (card == null) return 0
        return events.asReversed()
            .filter { event ->
                event.type in setOf(ClocktowerEventType.Information, ClocktowerEventType.UnreliableInformation) &&
                    event.playerNames.firstOrNull() == card.name
            }
            .takeWhile { event ->
                event.title.contains("misleading", ignoreCase = true) || event.title.contains("误导")
            }
            .count()
    }

    fun dynamicStorytellerState(): DynamicGameState {
        val spentRoleNames = buildSet {
            if (virginUsed) add("Virgin")
            if (slayerUsed) add("Slayer")
            if (artistUsed) add("Artist")
        }
        val gameState = cards.toClocktowerGameState(
            script = script,
            seed = gameSeed,
            poisonedPlayerName = poisonTarget,
        )
        val spentAbilitySeats = cards.mapIndexedNotNull { index, card ->
            (index + 1).takeIf { card.clocktowerRole?.enName in spentRoleNames }
        }.toSet()
        val playerInformationPressureBySeat = cards.mapIndexed { index, card ->
            val seat = index + 1
            val pressure = informationHistoryPressure(card)
            seat to PlayerInformationPressure(
                seat = seat,
                directSuspicion = pressure,
                recentTargetCount = pressure,
            )
        }.toMap()
        val registrationLedgerBySeat = cards.mapIndexedNotNull { index, card ->
            val count = events.count { event ->
                card.name in event.playerNames &&
                    (event.title.contains("registration", ignoreCase = true) || event.title.contains("登记"))
            }
            (index + 1 to RegistrationLedger(evilRegistrationCount = count)).takeIf { count > 0 }
        }.toMap()
        val balance = GameBalanceEvaluator.evaluate(
            game = gameState,
            round = round,
            spentAbilitySeats = spentAbilitySeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
        )
        return DynamicGameState(
            game = gameState,
            phase = when (phase) {
                ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
                ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
                ClocktowerPhase.Day -> StorytellerPhase.DAY
                ClocktowerPhase.Night -> StorytellerPhase.NIGHT
            },
            round = round,
            protectedSeats = setOfNotNull(
                monkProtectedTarget
                    ?.let { name -> cards.indexOfFirst { it.name == name } + 1 }
                    ?.takeIf { it > 0 },
            ),
            spentAbilitySeats = spentAbilitySeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
            registrationLedgerBySeat = registrationLedgerBySeat,
            publicBalanceHint = balance.hint,
            evilAdvantage = balance.evilAdvantage,
        )
    }
    val currentDynamicStorytellerState = dynamicStorytellerState()
    val automaticInformationStyle = GameBalanceEvaluator.adjustInformationStyle(
        configured = automaticStorytellerStyle,
        evilAdvantage = currentDynamicStorytellerState.evilAdvantage,
    )

    fun registrationRecommendationOptions(
        key: String?,
        roleEnName: String?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail,
        subject: PlayerCard?,
        isSpy: Boolean,
        suppressForJointRecommendation: Boolean = false,
        outcomeMisinformationPressure: Int = 0,
        specialRegistrationBalanceImpact: Int = 0,
    ): List<ClocktowerRegistrationRecommendationOption> {
        if (subject == null || suppressForJointRecommendation) return emptyList()
        if (key == null || teams.isEmpty()) return emptyList()
        val allowedRoleNames = completeTroubleBrewingRoles
            .filter { it.team in teams && (!isSpy || it.enName != "Spy") }
            .map { it.enName }
            .toSet()
        val allowedRoles = clocktowerRoleDefinitionsForScript(script).filter { it.id.value in allowedRoleNames }
        if (allowedRoles.isEmpty()) return emptyList()
        val subjectSeat = cards.indexOfFirst { it.name == subject.name } + 1
        if (subjectSeat <= 0) return emptyList()
        val request = DynamicDecisionRequest(
            id = key,
            type = StorytellerDecisionType.SPECIAL_REGISTRATION,
            sourceAbility = RoleId(roleEnName ?: return emptyList()),
            state = dynamicStorytellerState(),
        )
        return recommendationCoordinator.recommendRegistration(
            request = request,
            context = SpecialRegistrationContext(
                subjectSeat = subjectSeat,
                allowedRoles = allowedRoles,
                detail = if (isSpy && detail == ClocktowerRegistrationDetail.AlignmentOnly) {
                    RegistrationDetail.ALIGNMENT_ONLY
                } else {
                    RegistrationDetail.ROLE
                },
                canMisregister = subject.name != poisonTarget,
                outcomeMisinformationPressure = outcomeMisinformationPressure,
                specialRegistrationBalanceImpact = specialRegistrationBalanceImpact,
            ),
        ).map { recommendation ->
            val explanation = recommendationCoordinator.explainDecision(recommendation)
            val choice = recommendation.candidate.choice as DynamicStorytellerChoice.Registration
            val role = completeTroubleBrewingRoles.firstOrNull { it.enName == choice.registeredRole.value }
            val decisionText = when {
                !choice.usesSpecialAbility -> text("按真实身份登记", "Register actual identity")
                isSpy && detail == ClocktowerRegistrationDetail.AlignmentOnly ->
                    text("登记为善良", "Register as good")
                !isSpy && teams.isEmpty() -> text("登记为邪恶", "Register as evil")
                else -> text(
                    "登记为 ${role?.nameFor(language) ?: choice.registeredRole.value}",
                    "Register as ${role?.nameFor(language) ?: choice.registeredRole.value}",
                )
            }
            val warning = if (recommendation.warnings.size > 1) text(" · 高影响", " · high impact") else ""
            ClocktowerRegistrationRecommendationOption(
                label = "${recommendationStyleLabel(recommendation.style)} · $decisionText$warning",
                usesSpecialRegistration = choice.usesSpecialAbility,
                registeredRoleEnName = choice.registeredRole.value.takeIf { choice.usesSpecialAbility },
                style = recommendation.style,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = explanation.explanationCodes,
                warningCodes = explanation.warningCodes,
            )
        }
    }

    fun registrationRecommendationOptions(
        step: ClocktowerNightStepUi,
        subject: PlayerCard?,
        isSpy: Boolean,
    ): List<ClocktowerRegistrationRecommendationOption> = registrationRecommendationOptions(
        key = if (isSpy) step.spyRegistrationKey else step.recluseRegistrationKey,
        roleEnName = step.roleEnName,
        teams = if (isSpy) step.spyRegistrationTeams else step.recluseRegistrationTeams,
        detail = if (isSpy) step.spyRegistrationDetail else ClocktowerRegistrationDetail.Role,
        subject = subject,
        isSpy = isSpy,
        suppressForJointRecommendation = step.recommendedDisplayOptions.isNotEmpty(),
        specialRegistrationBalanceImpact = 1,
    )

    fun mayorDecisionOptions(mayor: PlayerCard): List<ClocktowerDecisionOption> {
        val mayorSeat = cards.indexOfFirst { it.name == mayor.name } + 1
        if (mayorSeat <= 0) return emptyList()
        val request = DynamicDecisionRequest(
            id = registrationKey("MayorRedirect", mayor.name),
            type = StorytellerDecisionType.MAYOR_DEATH_RESOLUTION,
            sourceAbility = RoleId("Mayor"),
            state = dynamicStorytellerState(),
        )
        return recommendationCoordinator.resolveDynamicDecision(
            DynamicResolutionRequest.MayorDeath(request, mayorSeat),
        ).mapNotNull { recommendation ->
            val explanation = recommendationCoordinator.explainDecision(recommendation)
            val choice = recommendation.candidate.choice as DynamicStorytellerChoice.MayorDeathResolution
            val target = cards.getOrNull(choice.targetSeat - 1) ?: return@mapNotNull null
            val outcome = recommendation.candidate.outcome as PredictedDecisionOutcome.NightDeath
            val result = when {
                outcome.actualDeathSeat == null -> text("今夜无人死亡", "No death tonight")
                outcome.actualDeathSeat == mayorSeat -> text("市长死亡", "Mayor dies")
                else -> text("${target.seatLabel(cards)} 死亡", "${target.seatLabel(cards)} dies")
            }
            val warning = if (recommendation.warnings.isNotEmpty()) text(" · 注意风险", " · review risk") else ""
            ClocktowerDecisionOption(
                label = "${recommendationStyleLabel(recommendation.style)} · $result$warning",
                targetName = target.name,
                explanation = result,
                recommendationStyle = recommendation.style,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = explanation.explanationCodes,
                warningCodes = explanation.warningCodes,
            )
        }
    }

    fun demonSuccessorDecisionOptions(): List<ClocktowerDecisionOption> {
        val request = DynamicDecisionRequest(
            id = registrationKey("DemonSuccessor"),
            type = StorytellerDecisionType.DEMON_SUCCESSION,
            sourceAbility = RoleId("Imp"),
            state = dynamicStorytellerState(),
        )
        return recommendationCoordinator.resolveDynamicDecision(
            DynamicResolutionRequest.DemonSuccessor(request),
        ).mapNotNull { recommendation ->
            val explanation = recommendationCoordinator.explainDecision(recommendation)
            val choice = recommendation.candidate.choice as DynamicStorytellerChoice.DemonSuccessor
            val target = cards.getOrNull(choice.targetSeat - 1) ?: return@mapNotNull null
            val warning = when {
                recommendation.warnings.any { it.ruleId == "scarlet-woman-mandatory" } ->
                    text(" · 规则要求", " · required")
                recommendation.warnings.isNotEmpty() -> text(" · 注意风险", " · review risk")
                else -> ""
            }
            ClocktowerDecisionOption(
                label = "${recommendationStyleLabel(recommendation.style)} · ${target.seatLabel(cards)}$warning",
                targetName = target.name,
                explanation = text(
                    "${target.seatLabel(cards)} 成为新的小恶魔",
                    "${target.seatLabel(cards)} becomes the new Imp",
                ),
                recommendationStyle = recommendation.style,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = explanation.explanationCodes,
                warningCodes = explanation.warningCodes,
            )
        }
    }

    fun recommendedPairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
        val scriptRoles = clocktowerRolesForScript(script)
        val targetEffects = buildList<PairInformationEffect> {
            fun addTargets(
                targets: List<PlayerCard>,
                roleForTarget: (PlayerCard) -> ClocktowerRole?,
                registration: PairInformationRegistration = PairInformationRegistration.NONE,
            ) {
                targets.forEach { target ->
                    val shownRole = roleForTarget(target) ?: return@forEach
                    cards.filter { decoy -> decoy.name != actor.name && decoy.name != target.name }
                        .forEach { decoy ->
                            val targetSeat = cards.indexOf(target) + 1
                            val decoySeat = cards.indexOf(decoy) + 1
                            add(
                                PairInformationEffect(
                                    id = "${ability.name}:${shownRole.enName}:$targetSeat:$decoySeat:${registration.name}",
                                    shownRole = shownRole,
                                    target = target,
                                    decoy = decoy,
                                    registration = registration,
                                ),
                            )
                        }
                }
            }

            fun addNaturalCandidates(abilityRole: RoleId) {
                val sourceSeat = cards.indexOfFirst { it.name == actor.name } + 1
                if (sourceSeat <= 0) return
                val gameState = cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                )
                recommendationCoordinator
                    .naturalPairCandidates(gameState)
                    .filter { candidate ->
                        val outcome = candidate.outcome as SetupClueOutcome.PairInformation
                        outcome.abilityRole == abilityRole && candidate.effects.any { effect ->
                            effect is com.codex.campboardgamehost.clocktower.domain.EffectDraft.PlayerInformation &&
                                effect.recipientSeat == sourceSeat
                        }
                    }
                    .forEach { candidate ->
                        val outcome = (candidate.outcome as SetupClueOutcome.PairInformation).information
                        add(
                            PairInformationEffect(
                                id = candidate.candidateId,
                                shownRole = outcome.shownRole?.value?.let { roleName ->
                                    scriptRoles.firstOrNull { it.enName == roleName }
                                },
                                target = outcome.targetSeat?.let { cards.getOrNull(it - 1) },
                                decoy = outcome.decoySeat?.let { cards.getOrNull(it - 1) },
                                registration = PairInformationRegistration.NONE,
                            ),
                        )
                    }
            }

            when (ability) {
                ClocktowerPairInformationAbility.Washerwoman -> {
                    addTargets(
                        targets = cards.filter { it.name != actor.name && it.clocktowerTeam == ClocktowerTeam.Townsfolk },
                        roleForTarget = { it.clocktowerRole },
                    )
                    if (spyCanRegister() && spyCard != null) {
                        scriptRoles.filter { it.team == ClocktowerTeam.Townsfolk }.forEach { role ->
                            addTargets(
                                targets = listOf(spyCard),
                                roleForTarget = { role },
                                registration = PairInformationRegistration.SPY_AS_GOOD_ROLE,
                            )
                        }
                    }
                }

                ClocktowerPairInformationAbility.Librarian -> {
                    addNaturalCandidates(RoleId("Librarian"))
                    if (spyCanRegister() && spyCard != null) {
                        scriptRoles.filter { it.team == ClocktowerTeam.Outsider }.forEach { role ->
                            addTargets(
                                targets = listOf(spyCard),
                                roleForTarget = { role },
                                registration = PairInformationRegistration.SPY_AS_GOOD_ROLE,
                            )
                        }
                    }
                }

                ClocktowerPairInformationAbility.Investigator -> {
                    addNaturalCandidates(RoleId("Investigator"))
                    if (recluseCanRegister() && recluseCard != null) {
                        scriptRoles.filter { it.team == ClocktowerTeam.Minion }.forEach { role ->
                            addTargets(
                                targets = listOf(recluseCard),
                                roleForTarget = { role },
                                registration = PairInformationRegistration.RECLUSE_AS_EVIL_ROLE,
                            )
                        }
                    }
                }
            }
        }.distinctBy(PairInformationEffect::id)

        val candidates = targetEffects.map { effect ->
            val targetMetadata = effect.target?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val decoyMetadata = effect.decoy?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val specialRegistration = effect.registration != PairInformationRegistration.NONE
            val evilPlayersNamed = listOfNotNull(effect.target, effect.decoy).count(::isClocktowerEvil)
            PairInformationCandidate(
                id = effect.id,
                registration = effect.registration,
                targetExposure = targetMetadata?.exposureSensitivity ?: 0,
                decoyExposure = decoyMetadata?.exposureSensitivity ?: 0,
                discussionValue = (targetMetadata?.discussionValue ?: 0) + (decoyMetadata?.discussionValue ?: 0),
                misinformationPressure = (
                    (if (specialRegistration) 3 else 0) +
                        evilPlayersNamed +
                        if ((targetMetadata?.exposureSensitivity ?: 0) >= 4) 1 else 0
                    ).coerceIn(0, 5),
                historyPressure = informationHistoryPressure(effect.target) + informationHistoryPressure(effect.decoy),
            )
        }
        val effectsById = targetEffects.associateBy(PairInformationEffect::id)
        return recommendationCoordinator.recommendPair(candidates).mapNotNull { recommendation ->
            val candidate = candidates.first { it.id == recommendation.candidateId }
            val effect = effectsById[recommendation.candidateId] ?: return@mapNotNull null
            val roleText = effect.shownRole?.nameFor(language) ?: text("没有外来者", "No Outsiders")
            val seats = if (effect.target != null && effect.decoy != null) {
                "${seatNumberFor(effect.target)}   ${seatNumberFor(effect.decoy)}"
            } else {
                null
            }
            val registrationText = when (effect.registration) {
                PairInformationRegistration.NONE -> ""
                PairInformationRegistration.SPY_AS_GOOD_ROLE -> text(" · 间谍登记", " · Spy registration")
                PairInformationRegistration.RECLUSE_AS_EVIL_ROLE -> text(" · 隐士登记", " · Recluse registration")
            }
            val warning = if (recommendation.warningIds.any { it != "special-registration" }) {
                text(" ⚠ 高压", " ⚠ high pressure")
            } else {
                ""
            }
            ClocktowerDisplayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$roleText${seats?.let { " · $it" }.orEmpty()}$registrationText$warning",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayTitle = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman -> text("洗衣妇信息", "Washerwoman information")
                    ClocktowerPairInformationAbility.Librarian -> text("图书管理员信息", "Librarian information")
                    ClocktowerPairInformationAbility.Investigator -> text("调查员信息", "Investigator information")
                },
                displayPrimary = roleText,
                displaySecondary = seats,
                displayFooter = if (seats == null) "" else text("在下面两位玩家之中", "One of these two players"),
                proposition = if (effect.shownRole != null && effect.target != null && effect.decoy != null) {
                    InformationProposition.AnyOf(listOf(
                        InformationProposition.RoleAt(cards.indexOf(effect.target) + 1, RoleId(effect.shownRole.enName)),
                        InformationProposition.RoleAt(cards.indexOf(effect.decoy) + 1, RoleId(effect.shownRole.enName)),
                    ))
                } else {
                    InformationProposition.AllOf(clocktowerRolesForScript(script)
                        .filter { it.team == when (ability) {
                            ClocktowerPairInformationAbility.Washerwoman -> ClocktowerTeam.Townsfolk
                            ClocktowerPairInformationAbility.Librarian -> ClocktowerTeam.Outsider
                            ClocktowerPairInformationAbility.Investigator -> ClocktowerTeam.Minion
                        } }
                        .map { InformationProposition.RoleInPlay(RoleId(it.enName), false) })
                },
                spyRegistersGood = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman,
                    ClocktowerPairInformationAbility.Librarian,
                    ClocktowerPairInformationAbility.Investigator
                    -> effect.registration == PairInformationRegistration.SPY_AS_GOOD_ROLE
                },
                spyRegisteredRoleEnName = effect.shownRole?.enName
                    ?.takeIf { effect.registration == PairInformationRegistration.SPY_AS_GOOD_ROLE },
                recluseRegistersEvil = if (ability == ClocktowerPairInformationAbility.Investigator) {
                    effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE
                } else {
                    null
                },
                recluseRegisteredRoleEnName = effect.shownRole?.enName
                    ?.takeIf { effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE },
                recommendationStyle = recommendation.style,
                isTruthful = candidate.isTruthful,
                misinformationPressure = candidate.misinformationPressure,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = listOf("dynamic.pair-score"),
                warningCodes = recommendation.warningIds,
            )
        }
    }

    fun recommendedUnreliablePairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
        val roleTeam = when (ability) {
            ClocktowerPairInformationAbility.Washerwoman -> ClocktowerTeam.Townsfolk
            ClocktowerPairInformationAbility.Librarian -> ClocktowerTeam.Outsider
            ClocktowerPairInformationAbility.Investigator -> ClocktowerTeam.Minion
        }
        val roles = clocktowerRolesForScript(script).filter { it.team == roleTeam }
        val pool = cards.filter { it.name != actor.name }
        val effects = buildList<PairInformationEffect> {
            roles.forEach { role ->
                for (firstIndex in 0 until pool.lastIndex) {
                    for (secondIndex in firstIndex + 1 until pool.size) {
                        val first = pool[firstIndex]
                        val second = pool[secondIndex]
                        add(
                            PairInformationEffect(
                                id = "unreliable:${ability.name}:${role.enName}:${cards.indexOf(first) + 1}:${cards.indexOf(second) + 1}",
                                shownRole = role,
                                target = first,
                                decoy = second,
                                registration = PairInformationRegistration.NONE,
                            ),
                        )
                    }
                }
            }
            if (ability != ClocktowerPairInformationAbility.Washerwoman) {
                add(
                    PairInformationEffect(
                        id = "unreliable:${ability.name}:none",
                        shownRole = null,
                        target = null,
                        decoy = null,
                        registration = PairInformationRegistration.NONE,
                    ),
                )
            }
        }
        val candidates = effects.map { effect ->
            val namedPlayers = listOfNotNull(effect.target, effect.decoy)
            val truthful = if (effect.shownRole == null) {
                cards.none { it.clocktowerTeam == roleTeam }
            } else {
                namedPlayers.any { it.clocktowerRole?.enName == effect.shownRole.enName }
            }
            val targetMetadata = effect.target?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val decoyMetadata = effect.decoy?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val goodPlayersNamed = namedPlayers.count { !isClocktowerEvil(it) }
            val shownMetadata = effect.shownRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            PairInformationCandidate(
                id = effect.id,
                registration = PairInformationRegistration.NONE,
                isTruthful = truthful,
                targetExposure = targetMetadata?.exposureSensitivity ?: 0,
                decoyExposure = decoyMetadata?.exposureSensitivity ?: 0,
                discussionValue = (targetMetadata?.discussionValue ?: 0) +
                    (decoyMetadata?.discussionValue ?: 0) +
                    (shownMetadata?.discussionValue ?: 0),
                misinformationPressure = if (truthful) {
                    0
                } else {
                    (2 +
                        (if (goodPlayersNamed == 2) 1 else 0) +
                        (if ((shownMetadata?.exposureSensitivity ?: 0) >= 4) 1 else 0))
                        .coerceIn(0, 5)
                },
                historyPressure = informationHistoryPressure(effect.target) + informationHistoryPressure(effect.decoy),
            )
        }
        val effectsById = effects.associateBy(PairInformationEffect::id)
        return recommendationCoordinator.recommendPair(candidates).mapNotNull { recommendation ->
            val candidate = candidates.first { it.id == recommendation.candidateId }
            val effect = effectsById[recommendation.candidateId] ?: return@mapNotNull null
            val noRoleText = when (ability) {
                ClocktowerPairInformationAbility.Librarian -> text("没有外来者", "No Outsiders")
                ClocktowerPairInformationAbility.Investigator -> text("没有爪牙", "No Minions")
                ClocktowerPairInformationAbility.Washerwoman -> text("没有镇民", "No Townsfolk")
            }
            val roleText = effect.shownRole?.nameFor(language) ?: noRoleText
            val seats = if (effect.target != null && effect.decoy != null) {
                "${seatNumberFor(effect.target)}   ${seatNumberFor(effect.decoy)}"
            } else {
                null
            }
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$roleText${seats?.let { " · $it" }.orEmpty()}$warning",
                kind = ClocktowerDisplayKind.EitherOne,
                title = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman -> text("洗衣妇信息", "Washerwoman information")
                    ClocktowerPairInformationAbility.Librarian -> text("图书管理员信息", "Librarian information")
                    ClocktowerPairInformationAbility.Investigator -> text("调查员信息", "Investigator information")
                },
                primary = roleText,
                secondary = seats,
                footer = if (seats == null) "" else text("在下面两位玩家之中", "One of these two players"),
                proposition = if (effect.shownRole != null && effect.target != null && effect.decoy != null) {
                    InformationProposition.AnyOf(listOf(
                        InformationProposition.RoleAt(cards.indexOf(effect.target) + 1, RoleId(effect.shownRole.enName)),
                        InformationProposition.RoleAt(cards.indexOf(effect.decoy) + 1, RoleId(effect.shownRole.enName)),
                    ))
                } else {
                    InformationProposition.AllOf(roles.map { InformationProposition.RoleInPlay(RoleId(it.enName), false) })
                },
                recommendationStyle = recommendation.style,
                isTruthful = candidate.isTruthful,
                misinformationPressure = candidate.misinformationPressure,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
                reasonCodes = listOf("dynamic.pair-score"),
                warningCodes = recommendation.warningIds,
            )
        }
    }

    fun infoStep(
        roleName: String,
        enName: String,
        tellPlayer: String?,
        explanation: String,
        action: ClocktowerNightAction = ClocktowerNightAction.None,
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.Plain,
        displayPrimary: String? = tellPlayer,
        displaySecondary: String? = null,
        displayFooter: String? = explanation,
        displayTitle: String? = null,
        displayProposition: InformationProposition? = null,
        hostInstruction: String? = null,
        displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        reliableDisplayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        spyRegistrationKey: String? = null,
        spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
        spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
        spyRegistrationHint: String? = null,
        recluseRegistrationKey: String? = null,
        recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    ): ClocktowerNightStepUi {
        val actor = roleActor(enName)
        val localizedRoleName = if (language == "en") enName else roleName
        val localizedDisplayTitle = displayTitle ?: text("$roleName 信息", "$enName information")
        val actorIsDrunkShownRole = actor?.clocktowerRole?.enName == "Drunk" && actor.clocktowerShownRole?.enName == enName
        val informationReliability = when {
            actorIsPoisoned(actor) -> InformationReliability.POISONED
            actorIsDrunkShownRole -> InformationReliability.DRUNK
            else -> InformationReliability.RELIABLE
        }
        val hostUnreliableNote = if (actorIsDrunkShownRole) {
            text(
                "注意：这名玩家真实身份是酒鬼，显示为$roleName。请照常唤醒并给信息，但信息可以不可靠或完全错误。",
                "This player is actually the Drunk but appears as $enName. Wake them normally and give information that may be unreliable or entirely false.",
            )
        } else if (actorIsPoisoned(actor)) {
            text(
                "注意：这名玩家今晚中毒，能力信息可以不可靠或错误。",
                "This player is poisoned tonight, so their ability information may be unreliable or false.",
            )
        } else {
            null
        }
        val actorAbilityUnreliable = actor != null && actorIsUnreliable(enName, actor)
        val unreliableOptions = actor?.takeIf { actorAbilityUnreliable }?.let(displayOptions).orEmpty()
        val reliableRecommendations = actor?.takeUnless { actorAbilityUnreliable }?.let(reliableDisplayOptions).orEmpty()
        val automaticRecommendations = when {
            !automaticStorytellerInfo -> reliableRecommendations
            actorAbilityUnreliable -> unreliableOptions
            else -> reliableRecommendations
        }
        val resolvedDisplayKind = when (enName) {
            "Chef", "Empath", "Clockmaker", "Chambermaid" -> ClocktowerDisplayKind.Number
            "Fortune Teller" -> ClocktowerDisplayKind.YesNo
            "Ravenkeeper", "Undertaker" -> ClocktowerDisplayKind.RoleReveal
            "Washerwoman", "Librarian", "Investigator" -> ClocktowerDisplayKind.EitherOne
            else -> displayKind
        }
        val directLegacyCandidate = actor
            ?.takeIf { unreliableOptions.isEmpty() && !tellPlayer.isNullOrBlank() }
            ?.let {
                ClocktowerDisplayOption(
                    label = "direct-legacy",
                    displayKind = resolvedDisplayKind,
                    displayTitle = localizedDisplayTitle,
                    displayPrimary = displayPrimary ?: tellPlayer,
                    displaySecondary = displaySecondary,
                    displayFooter = displayFooter ?: explanation,
                    proposition = displayProposition,
                )
            }
        val completeLegacyCandidates = (unreliableOptions + reliableRecommendations + listOfNotNull(directLegacyCandidate))
            .distinctBy { option ->
                listOf(
                    option.displayKind.name,
                    option.proposition?.toString().orEmpty(),
                    option.displayPrimary.orEmpty(),
                    option.displaySecondary.orEmpty(),
                    option.displayFooter.orEmpty(),
                    option.spyRegistersGood?.toString().orEmpty(),
                    option.spyRegisteredRoleEnName.orEmpty(),
                    option.recluseRegistersEvil?.toString().orEmpty(),
                    option.recluseRegisteredRoleEnName.orEmpty(),
                    option.isTruthful.toString(),
                ).joinToString("|")
            }
        return ClocktowerNightStepUi(
            title = localizedRoleName,
            actor = actor,
            isRealAction = actor != null,
            reason = if (actor != null) "" else roleMissingReason(enName),
            storytellerAction = if (actor != null) {
                listOfNotNull(
                    hostInstruction ?: text(
                        "轻拍 ${actor.seatLabel(cards)}，示意睁眼。把本步骤信息只给他看；看完后收回手机，示意闭眼。",
                        "Tap ${actor.seatLabel(cards)} to wake them. Show this information only to that player, then take the phone back and signal them to close their eyes.",
                    ),
                    hostUnreliableNote,
                ).joinToString("\n")
            } else {
                text(
                    "不要唤醒任何玩家。为了避免玩家通过流程判断角色是否在场，请停顿 2-3 秒，然后点击下一步。",
                    "Do not wake anyone. Pause for 2–3 seconds so players cannot infer whether this character is in play, then continue.",
                )
            },
            tellPlayer = if (actor != null && unreliableOptions.isEmpty()) tellPlayer else null,
            explanation = listOfNotNull(explanation, hostUnreliableNote).joinToString("\n"),
            action = action,
            displayKind = if (actor != null && unreliableOptions.isEmpty() && !tellPlayer.isNullOrBlank()) resolvedDisplayKind else ClocktowerDisplayKind.None,
            displayTitle = localizedDisplayTitle,
            displayProposition = displayProposition,
            displayPrimary = if (actor != null && unreliableOptions.isEmpty()) displayPrimary ?: tellPlayer else null,
            displaySecondary = if (actor != null && unreliableOptions.isEmpty()) displaySecondary else null,
            displayFooter = if (actor != null && unreliableOptions.isEmpty()) displayFooter ?: explanation else null,
            displayOptions = if (automaticStorytellerInfo) emptyList() else unreliableOptions,
            recommendedDisplayOptions = automaticRecommendations,
            legacyInformationCandidates = completeLegacyCandidates,
            roleEnName = enName,
            informationReliability = informationReliability,
            recentMisinformationStreak = recentMisinformationStreak(actor),
            spyRegistrationKey = RegistrationInteractionRules.effectiveRegistrationKey(
                spyRegistrationKey,
                informationAbilityReliable = !actorAbilityUnreliable,
            ),
            spyRegistrationTeams = spyRegistrationTeams,
            spyRegistrationDetail = spyRegistrationDetail,
            spyRegistrationHint = spyRegistrationHint,
            recluseRegistrationKey = RegistrationInteractionRules.effectiveRegistrationKey(
                recluseRegistrationKey,
                informationAbilityReliable = !actorAbilityUnreliable,
            ),
            recluseRegistrationTeams = recluseRegistrationTeams,
        )
    }

    val washerwomanActor = roleActor("Washerwoman")
    val washerwomanRegistrationKey = washerwomanActor?.let { registrationKey("Washerwoman") }
    val washerwomanTarget = spyCard?.takeIf { spyRegistersGood(washerwomanRegistrationKey) }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Townsfolk && it.clocktowerRole?.enName != "Washerwoman" }
    val washerwomanPair = washerwomanTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(washerwomanActor?.name)) }
    val washerwomanRevealedRole = if (washerwomanTarget?.name == spyCard?.name) {
        registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk))
    } else washerwomanTarget?.clocktowerRole
    val washerwomanOrderedPair = orderedPair(washerwomanPair?.first, washerwomanPair?.second, "Washerwoman-${washerwomanPair?.first?.name}-${washerwomanPair?.second?.name}")
    val librarianActor = roleActor("Librarian")
    val librarianRegistrationKey = librarianActor?.let { registrationKey("Librarian") }
    val librarianTarget = spyCard?.takeIf { spyRegistersGood(librarianRegistrationKey) }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Outsider }
    val librarianPair = librarianTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(librarianActor?.name)) }
    val librarianRevealedRole = if (librarianTarget?.name == spyCard?.name) {
        registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider))
    } else librarianTarget?.clocktowerRole
    val librarianOrderedPair = orderedPair(librarianPair?.first, librarianPair?.second, "Librarian-${librarianPair?.first?.name}-${librarianPair?.second?.name}")
    val investigatorActor = roleActor("Investigator")
    val investigatorRegistrationKey = investigatorActor?.let { registrationKey("Investigator") }
    val investigatorRecluseRegistrationKey = investigatorActor?.let {
        recluseCard?.let { recluse -> registrationKey("InvestigatorRecluse", recluse.name) }
    }
    val investigatorTarget = recluseCard?.takeIf { recluseRegistersEvil(investigatorRecluseRegistrationKey) }
        ?: spyCard?.takeIf { !spyRegistersGood(investigatorRegistrationKey) }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion && it.clocktowerRole?.enName != "Spy" }
    val investigatorRevealedRole = if (investigatorTarget?.name == recluseCard?.name) {
        recluseRegisteredRole(investigatorRecluseRegistrationKey, listOf(ClocktowerTeam.Minion))
    } else {
        investigatorTarget?.clocktowerRole
    }
    val investigatorPair = investigatorTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(investigatorActor?.name)) }
    val investigatorOrderedPair = orderedPair(investigatorPair?.first, investigatorPair?.second, "Investigator-${investigatorPair?.first?.name}-${investigatorPair?.second?.name}")
    val clockmakerValue = clockmakerNumber()
    val clockmakerNumber = clockmakerValue.toString()
    val empathActor = roleActor("Empath")
    val empathNeighbors = empathActor?.let { livingNeighbors(cards, it.name) }.orEmpty()
    val empathAbilityUnreliable = empathActor?.let { actorIsUnreliable("Empath", it) } == true
    val empathRegistrationKey = empathActor?.takeIf { actor -> empathNeighbors.any { it.name == spyCard?.name } }?.let { registrationKey("Empath", it.name) }
    val empathRecluseRegistrationKey = empathActor
        ?.takeIf { empathNeighbors.any { neighbor -> neighbor.name == recluseCard?.name } }
        ?.let { registrationKey("EmpathRecluse", it.name) }
    fun registeredIsEvil(card: PlayerCard, spyKey: String?, recluseKey: String?): Boolean = when {
        card.name == spyCard?.name && spyRegistersGood(spyKey) -> false
        card.name == recluseCard?.name && recluseRegistersEvil(recluseKey) -> true
        else -> isClocktowerEvil(card)
    }
    val chefActor = roleActor("Chef")
    val chefAbilityUnreliable = chefActor?.let { actorIsUnreliable("Chef", it) } == true
    val chefRegistrationKey = chefActor?.let { registrationKey("Chef") }
    val chefRecluseRegistrationKey = chefActor?.let {
        recluseCard?.let { recluse -> registrationKey("ChefRecluse", recluse.name) }
    }
    val chefValue = chefEvilPairs(cards) { card -> registeredIsEvil(card, chefRegistrationKey, chefRecluseRegistrationKey) }
    val chefActualIdentityValue = chefEvilPairs(cards)
    val chefSpyGoodValue = chefEvilPairs(cards) { card ->
        if (card.name == spyCard?.name) false else isClocktowerEvil(card)
    }
    val chefReferenceValue = if (chefAbilityUnreliable) chefActualIdentityValue else chefValue
    val chefRegistrationHint = when {
        chefAbilityUnreliable && (spyCard != null || recluseCard != null) -> text(
            "厨师能力不可靠：直接选择最终展示数字，不需要先裁定间谍或隐士如何登记。",
            "The Chef is unreliable: choose the final shown number directly; no Spy or Recluse registration ruling is required.",
        )
        chefRegistrationKey != null && spyCard != null -> text(
            "结果预览：间谍按真实邪恶登记时为 $chefActualIdentityValue；登记善良时为 $chefSpyGoodValue。",
            "Result preview: $chefActualIdentityValue if the Spy registers as actual evil; $chefSpyGoodValue if the Spy registers as good.",
        )
        else -> null
    }
    val chefMaximumValue = maxOf(
        chefValue,
        cards.count(::isClocktowerEvil) + if (recluseCard != null) 1 else 0,
    )
    val chefNumber = chefValue.toString()
    val empathValue = empathActor?.let { actor ->
        empathEvilNeighborCount(cards, actor.name) {
            registeredIsEvil(it, empathRegistrationKey, empathRecluseRegistrationKey)
        }
    } ?: 0
    val empathActualIdentityValue = empathNeighbors.count(::isClocktowerEvil)
    val empathSpyActualValue = empathNeighbors.count { neighbor ->
        if (neighbor.name == spyCard?.name) true else registeredIsEvil(neighbor, null, empathRecluseRegistrationKey)
    }
    val empathSpyGoodValue = empathNeighbors.count { neighbor ->
        if (neighbor.name == spyCard?.name) false else registeredIsEvil(neighbor, null, empathRecluseRegistrationKey)
    }
    val empathReferenceValue = if (empathAbilityUnreliable) empathActualIdentityValue else empathValue
    val empathRegistrationHint = when {
        empathAbilityUnreliable && (empathRegistrationKey != null || empathRecluseRegistrationKey != null) -> text(
            "共情者能力不可靠：直接选择最终展示数字，不需要先裁定间谍或隐士如何登记。",
            "The Empath is unreliable: choose the final shown number directly; no Spy or Recluse registration ruling is required.",
        )
        empathRegistrationKey != null -> text(
            "结果预览：保持其他裁定不变，间谍按真实邪恶登记时为 $empathSpyActualValue；登记善良时为 $empathSpyGoodValue。",
            "Result preview with other rulings unchanged: $empathSpyActualValue if the Spy registers as actual evil; $empathSpyGoodValue if the Spy registers as good.",
        )
        else -> null
    }
    val empathNumber = empathValue.toString()
    fun recordReliablePrivateInformation(displayStep: ClocktowerNightStepUi) {
        val actor = displayStep.actor ?: return
        val actorSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return
        if (displayStep.displayProposition == null &&
            (actor.clocktowerRole?.enName == "Drunk" || actor.name == poisonTarget)) return
        val proposition = displayStep.displayProposition ?: when (displayStep.roleEnName) {
            "Chef" -> InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, actorSeat, cards.indices.map { it + 1 }, chefReferenceValue,
            )
            "Empath" -> InformationProposition.NumericResult(
                NumericMetric.LIVING_EVIL_NEIGHBOURS, actorSeat,
                empathNeighbors.map { cards.indexOf(it) + 1 }, empathReferenceValue,
            )
            "Fortune Teller" -> InformationProposition.BooleanResult(
                BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, actorSeat,
                listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
                    cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                }, fortuneTellerMatched ?: return,
            )
            "Investigator" -> {
                val pair = investigatorPair
                val revealedRole = investigatorRevealedRole
                if (pair != null && revealedRole != null) {
                    val firstSeat = cards.indexOf(pair.first).takeIf { it >= 0 }?.plus(1) ?: return
                    val secondSeat = cards.indexOf(pair.second).takeIf { it >= 0 }?.plus(1) ?: return
                    InformationProposition.AnyOf(listOf(
                        InformationProposition.RoleAt(firstSeat, RoleId(revealedRole.enName)),
                        InformationProposition.RoleAt(secondSeat, RoleId(revealedRole.enName)),
                    ))
                } else {
                    InformationProposition.AllOf(completeTroubleBrewingRoles
                        .filter { it.team == ClocktowerTeam.Minion }
                        .map { InformationProposition.RoleInPlay(RoleId(it.enName), false) })
                }
            }
            "Washerwoman", "Librarian" -> {
                val pair = (if (displayStep.roleEnName == "Washerwoman") washerwomanPair else librarianPair) ?: return
                val role = if (displayStep.roleEnName == "Washerwoman") washerwomanRevealedRole else librarianRevealedRole
                val firstSeat = cards.indexOf(pair.first).takeIf { index -> index >= 0 }?.plus(1) ?: return
                val secondSeat = pair.second.let { cards.indexOf(it).takeIf { index -> index >= 0 }?.plus(1) } ?: return
                InformationProposition.AnyOf(listOf(
                    InformationProposition.RoleAt(firstSeat, RoleId(role?.enName ?: return)),
                    InformationProposition.RoleAt(secondSeat, RoleId(role.enName)),
                ))
            }
            else -> return
        }
        onRecordEpistemicObservation(RecordedEpistemicObservation(
            recordId = "private-${gameId}-${phase.name}-${round}-${displayStep.roleEnName}-$actorSeat",
            phase = when (phase) {
                ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
                ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
                ClocktowerPhase.Day -> StorytellerPhase.DAY
                ClocktowerPhase.Night -> StorytellerPhase.NIGHT
            },
            round = round, sequence = nightStepIndex, sourceSeat = actorSeat,
            sourceAbility = RoleId(requireNotNull(displayStep.roleEnName)), visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(actorSeat), reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        ))
    }
    val undertakerTarget = lastExecutedName?.let { name -> cards.firstOrNull { it.name == name } }
    val undertakerRegistrationKey = undertakerTarget?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Undertaker", it.name) }
    val undertakerRecluseRegistrationKey = undertakerTarget?.takeIf { it.name == recluseCard?.name }?.let { registrationKey("UndertakerRecluse", it.name) }
    val ravenkeeperTargetCard = ravenkeeperTarget?.let { name -> cards.firstOrNull { it.name == name } }
    val ravenkeeperRegistrationKey = ravenkeeperTargetCard?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Ravenkeeper", it.name) }
    val ravenkeeperRecluseRegistrationKey = ravenkeeperTargetCard?.takeIf { it.name == recluseCard?.name }?.let { registrationKey("RavenkeeperRecluse", it.name) }
    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val demonCard = cards.firstOrNull { it.clocktowerRole?.team == ClocktowerTeam.Demon }
    val livingImp = demonCard?.takeIf {
        it.eliminatedRound == null && it.clocktowerRole?.enName == "Imp"
    }
    val impSelfKillNeedsSuccessor =
        livingImp != null &&
            pendingNightDeath == livingImp.name &&
            !demonPoisonedTonight &&
            aliveCards.any { it.clocktowerTeam == ClocktowerTeam.Minion }
    val sageNightDeath = resolvedNightDeathCard
        ?.takeIf { nightDeathWillOccur && it.clocktowerRole?.enName == "Sage" }
    val sagePair = demonCard?.let { storytellerPairHint(it, cards) }
    val spyDelta: String? = run {
        if (spyCard == null || phase == ClocktowerPhase.FirstNight) return@run null
        val prevRound = round - 1
        val excludedTitles = setOf(
            text("间谍登记裁定", "Spy registration"),
            text("今日主人", "Master"),
            text("红鲱鱼", "Red herring"),
            text("魔典", "Grimoire"),
            text("爪牙信息", "Minion info"),
            text("恶魔信息", "Demon info"),
            text("杀手行动", "Slayer claim"),
            text("杀手命中", "Slayer hit"),
        )
        val deltaEvents = events.filter { e ->
            e.type in setOf(
                ClocktowerEventType.RoleAction,
                ClocktowerEventType.Information,
                ClocktowerEventType.UnreliableInformation,
            ) &&
                e.title !in excludedTitles &&
                ((e.phase == ClocktowerPhase.Night && e.round == prevRound) ||
                    (e.phase == ClocktowerPhase.FirstNight && e.round == prevRound) ||
                    (e.phase == ClocktowerPhase.Day && e.round == prevRound))
        }
        if (deltaEvents.isEmpty()) null
        else deltaEvents.joinToString("\n") { "• ${it.title}：${it.detail}" }
    }
    val minionCards = cards.filter { it.clocktowerRole?.team == ClocktowerTeam.Minion }
    fun seatNumberText(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    fun twoSeatNumbers(first: PlayerCard?, second: PlayerCard?): String? =
        if (first != null && second != null) "${seatNumberText(first)}   ${seatNumberText(second)}" else null
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
    val legalDemonBluffs = legalDemonBluffRoles(
        scriptRoles = clocktowerRolesForScript(script),
        inPlayRoleNames = cards.mapNotNull { it.clocktowerRole?.enName }.toSet(),
    )
    val appliedDemonBluffs = recommendedDemonBluffRoleNames
        .mapNotNull { roleName -> legalDemonBluffs.firstOrNull { it.enName == roleName } }
        .distinctBy(ClocktowerRole::enName)
    val demonBluffs = if (appliedDemonBluffs.size == 3) appliedDemonBluffs else legalDemonBluffs.take(3)
    val minionInfoText = demonCard?.let { stringResource(R.string.clocktower_first_night_minion_info_format, it.seatLabel(cards)) }
    val demonInfoText = buildList {
        add(
            if (minionCards.isEmpty()) {
                stringResource(R.string.clocktower_first_night_demon_no_minions)
            } else {
                stringResource(
                    R.string.clocktower_first_night_demon_minions_format,
                    minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                )
            },
        )
        add(
            stringResource(
                R.string.clocktower_first_night_demon_bluffs_format,
                demonBluffs.joinToString(stringResource(R.string.name_separator)) { it.nameFor(language) },
            ),
        )
    }.joinToString("\n")
    val minionInfoTitle = stringResource(R.string.clocktower_first_night_minion_title)
    val demonInfoTitle = stringResource(R.string.clocktower_first_night_demon_title)
    val unfilteredNightSteps = if (phase == ClocktowerPhase.FirstNight) {
        listOf(
            ClocktowerNightStepUi(
                title = stringResource(R.string.clocktower_first_night_minion_title),
                actor = minionCards.firstOrNull().takeIf { shouldGiveFirstNightEvilInfo },
                isRealAction = minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo,
                reason = when {
                    !shouldGiveFirstNightEvilInfo -> stringResource(R.string.clocktower_first_night_small_game_no_evil_info_reason)
                    minionCards.isEmpty() -> stringResource(R.string.clocktower_first_night_no_minions_reason)
                    else -> ""
                },
                storytellerAction = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) {
                    stringResource(
                        R.string.clocktower_first_night_minion_action_format,
                        minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                    )
                } else {
                    stringResource(R.string.clocktower_first_night_placeholder_action)
                },
                tellPlayer = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) minionInfoText else null,
                explanation = if (shouldGiveFirstNightEvilInfo) {
                    stringResource(R.string.clocktower_first_night_minion_explain)
                } else {
                    stringResource(R.string.clocktower_first_night_small_game_no_evil_info_explain)
                },
                displayKind = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo && minionInfoText != null) ClocktowerDisplayKind.EvilInfo else ClocktowerDisplayKind.None,
                displayTitle = stringResource(R.string.clocktower_first_night_minion_title),
                displayPrimary = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) "${stringResource(R.string.clocktower_evil_display_demon)}\n${demonCard?.seatLabel(cards).orEmpty()}" else null,
                displayFooter = null,
                wakeText = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) {
                    stringResource(
                        R.string.clocktower_first_night_minion_wake_format,
                        minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                    )
                } else {
                    null
                },
            ),
            ClocktowerNightStepUi(
                title = stringResource(R.string.clocktower_first_night_demon_title),
                actor = demonCard.takeIf { shouldGiveFirstNightEvilInfo },
                isRealAction = demonCard != null && shouldGiveFirstNightEvilInfo,
                reason = when {
                    !shouldGiveFirstNightEvilInfo -> stringResource(R.string.clocktower_first_night_small_game_no_evil_info_reason)
                    demonCard == null -> stringResource(R.string.clocktower_first_night_no_demon_reason)
                    else -> ""
                },
                storytellerAction = if (demonCard != null && shouldGiveFirstNightEvilInfo) {
                    stringResource(R.string.clocktower_first_night_demon_action_format, demonCard.seatLabel(cards))
                } else {
                    stringResource(R.string.clocktower_first_night_placeholder_action)
                },
                tellPlayer = if (demonCard != null && shouldGiveFirstNightEvilInfo) demonInfoText else null,
                explanation = if (shouldGiveFirstNightEvilInfo) {
                    stringResource(R.string.clocktower_first_night_demon_explain)
                } else {
                    stringResource(R.string.clocktower_first_night_small_game_no_evil_info_explain)
                },
                displayKind = if (demonCard != null && shouldGiveFirstNightEvilInfo) ClocktowerDisplayKind.EvilInfo else ClocktowerDisplayKind.None,
                displayTitle = stringResource(R.string.clocktower_first_night_demon_title),
                displayPrimary = if (demonCard != null && shouldGiveFirstNightEvilInfo) "${stringResource(R.string.clocktower_evil_display_minions)}\n${if (minionCards.isEmpty()) stringResource(R.string.clocktower_first_night_demon_no_minions) else minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) }}" else null,
                displaySecondary = if (demonCard != null && shouldGiveFirstNightEvilInfo) "${stringResource(R.string.clocktower_evil_display_bluffs)}\n${demonBluffs.joinToString(stringResource(R.string.name_separator)) { it.nameFor(language) }}" else null,
                displayFooter = null,
            ),
            infoStep(
                roleName = "投毒者",
                enName = "Poisoner",
                tellPlayer = poisonTarget?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                explanation = text("投毒者选择一名玩家，使其能力暂时失效。", "The Poisoner chooses a player whose ability stops working temporarily."),
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。", "Tap the Poisoner to wake them. Have them point to one player and record that player as tonight's poisoned target."),
            ),
            ClocktowerNightStepUi(
                title = text("占卜师红鲱鱼", "Fortune Teller red herring"),
                actor = null,
                isRealAction = actualClocktowerRoleCards(cards, "Fortune Teller").isNotEmpty(),
                reason = if (actualClocktowerRoleCards(cards, "Fortune Teller").isEmpty()) text("本局没有占卜师，此步骤只用于首夜配置。", "No Fortune Teller is in play; this is only a first-night setup step.") else "",
                storytellerAction = text("不要公开说明这个选择。请选择一名善良玩家作为红鲱鱼；可以选择占卜师本人。", "Keep this choice private. Choose a good player as the red herring; the Fortune Teller may be chosen."),
                tellPlayer = redHerring?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                explanation = text("选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。", "Choose a good player as the red herring. The Fortune Teller detects that player as a Demon."),
                action = ClocktowerNightAction.RedHerring,
                roleEnName = "Fortune Teller",
            ),
            infoStep(
                roleName = "钟表匠",
                enName = "Clockmaker",
                tellPlayer = clockmakerNumber,
                explanation = text("这个数字表示恶魔到最近爪牙相隔几步。", "This number is the distance from the Demon to the nearest Minion."),
                displayFooter = text("恶魔到最近爪牙的距离", "Distance from Demon to nearest Minion"),
                hostInstruction = text("轻拍钟表匠，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。", "Tap the Clockmaker to wake them. Show the number only to that player, then take the phone back and signal them to close their eyes."),
                displayOptions = { actor -> recommendedNumberOptions(text("钟表匠信息", "Clockmaker information"), actor, clockmakerValue, cards.size / 2, text("恶魔到最近爪牙的距离", "Distance from Demon to nearest Minion")) },
            ),
            infoStep(
                roleName = "洗衣妇",
                enName = "Washerwoman",
                tellPlayer = washerwomanTarget?.let { text("${if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk))?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${washerwomanOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${washerwomanOrderedPair?.second?.seatLabel(cards).orEmpty()}", "${if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk))?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} is one of these two players: ${washerwomanOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${washerwomanOrderedPair?.second?.seatLabel(cards).orEmpty()}") },
                explanation = text("洗衣妇会得知某个镇民在两名玩家之一中。", "The Washerwoman learns that a particular Townsfolk is one of two players."),
                displayPrimary = washerwomanTarget?.let { if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk))?.nameFor(language) else it.clocktowerRole?.nameFor(language) },
                displaySecondary = seatNumbersText(washerwomanOrderedPair),
                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("轻拍洗衣妇，示意睁眼。点击“全屏展示给玩家”，只给她看；看完后收回手机，示意闭眼。", "Tap the Washerwoman to wake them. Show the full-screen information only to that player, then take the phone back and signal them to close their eyes."),
                displayOptions = { actor ->
                    recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Washerwoman, actor)
                },
                reliableDisplayOptions = { actor ->
                    recommendedPairInformationOptions(ClocktowerPairInformationAbility.Washerwoman, actor)
                },
                spyRegistrationKey = washerwomanRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk),
            ),
            infoStep(
                roleName = "图书管理员",
                enName = "Librarian",
                tellPlayer = librarianTarget?.let { text("${if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider))?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${librarianOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${librarianOrderedPair?.second?.seatLabel(cards).orEmpty()}", "${if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider))?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} is one of these two players: ${librarianOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${librarianOrderedPair?.second?.seatLabel(cards).orEmpty()}") } ?: text("本局没有外来者。", "There are no Outsiders in play."),
                explanation = text("图书管理员会得知某个外来者在两名玩家之一中，或得知没有外来者。", "The Librarian learns that an Outsider is one of two players, or that no Outsiders are in play."),
                displayPrimary = librarianTarget?.let { if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider))?.nameFor(language) else it.clocktowerRole?.nameFor(language) } ?: text("没有外来者", "No Outsiders"),
                displaySecondary = seatNumbersText(librarianOrderedPair),
                displayFooter = if (librarianTarget == null) "" else text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("轻拍图书管理员，示意睁眼。把结果只给他看；如果显示“没有外来者”，也只告诉他本人。", "Tap the Librarian to wake them. Show the result only to that player, including a No Outsiders result."),
                displayOptions = { actor ->
                    recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Librarian, actor)
                },
                reliableDisplayOptions = { actor ->
                    recommendedPairInformationOptions(ClocktowerPairInformationAbility.Librarian, actor)
                },
                spyRegistrationKey = librarianRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Outsider),
            ),
            infoStep(
                roleName = "调查员",
                enName = "Investigator",
                tellPlayer = investigatorTarget?.let { text("${investigatorRevealedRole?.nameFor(language).orEmpty()} 在这两人之中：${investigatorOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${investigatorOrderedPair?.second?.seatLabel(cards).orEmpty()}", "${investigatorRevealedRole?.nameFor(language).orEmpty()} is one of these two players: ${investigatorOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${investigatorOrderedPair?.second?.seatLabel(cards).orEmpty()}") } ?: text("本局没有爪牙。", "There are no Minions in play."),
                explanation = text("调查员会得知某个爪牙在两名玩家之一中，或得知没有爪牙。", "The Investigator learns that a Minion is one of two players, or that no Minions are in play."),
                displayPrimary = investigatorRevealedRole?.nameFor(language) ?: text("没有爪牙", "No Minions"),
                displaySecondary = seatNumbersText(investigatorOrderedPair),
                displayFooter = if (investigatorTarget == null) "" else text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("轻拍调查员，示意睁眼。把结果只给他看；不要让其他玩家看到被点名的两人。", "Tap the Investigator to wake them. Show the result only to that player; do not let anyone else see the two named players."),
                displayOptions = { actor ->
                    listOfNotNull(recommendedDrunkInvestigatorOption(actor)) +
                        recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                },
                reliableDisplayOptions = { actor ->
                    recommendedPairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                },
                spyRegistrationKey = investigatorRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                recluseRegistrationKey = investigatorRecluseRegistrationKey,
                recluseRegistrationTeams = listOf(ClocktowerTeam.Minion),
            ),
            infoStep(
                roleName = "厨师",
                enName = "Chef",
                tellPlayer = chefNumber,
                explanation = listOfNotNull(text("这个数字表示有几对邪恶玩家相邻而坐。", "This number is the number of adjacent evil pairs."), chefRegistrationHint).joinToString("\n"),
                hostInstruction = text("轻拍厨师，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。", "Tap the Chef to wake them. Show the number only to that player, then take the phone back and signal them to close their eyes."),
                displayOptions = { actor -> recommendedNumberOptions(text("厨师信息", "Chef information"), actor, chefReferenceValue, chefMaximumValue, text("邪恶玩家相邻对数", "Adjacent evil pairs"), pressureCostPerPoint = 1, propositionForValue = { value -> InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, cards.indexOf(actor) + 1, cards.indices.map { it + 1 }, value) }) },
                spyRegistrationKey = chefRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                spyRegistrationHint = chefRegistrationHint,
                recluseRegistrationKey = chefRecluseRegistrationKey,
            ),
            infoStep(
                roleName = "共情者",
                enName = "Empath",
                tellPlayer = empathNumber,
                explanation = listOfNotNull(text("这个数字表示共情者两个存活邻居中有几个邪恶玩家。", "This number is how many of the Empath's living neighbors are evil."), empathRegistrationHint).joinToString("\n"),
                hostInstruction = text("轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。", "Tap the Empath to wake them. Show only the number; do not identify either neighbor."),
                displayOptions = { actor -> recommendedNumberOptions(text("共情者信息", "Empath information"), actor, empathReferenceValue, 2, text("邪恶存活邻居数量", "Evil living neighbors"), pressureCostPerPoint = 1, propositionForValue = { value -> InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, cards.indexOf(actor) + 1, livingNeighbors(cards, actor.name).map { cards.indexOf(it) + 1 }, value) }) },
                spyRegistrationKey = empathRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                spyRegistrationHint = empathRegistrationHint,
                recluseRegistrationKey = empathRecluseRegistrationKey,
            ),
            infoStep(
                roleName = "侍女",
                enName = "Chambermaid",
                tellPlayer = chambermaidResult,
                explanation = text("侍女选择两名玩家，得知其中有几人今晚因自己的能力醒来。", "The Chambermaid chooses two players and learns how many woke tonight because of their own ability."),
                action = ClocktowerNightAction.Chambermaid,
                displaySecondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = text("查询这两名玩家", "Checking these two players"),
                hostInstruction = text("轻拍侍女，示意睁眼。让她依次指两名玩家，不能选自己；点查询后只展示数字。", "Tap the Chambermaid to wake them. Have them point to two players other than themself, then show only the number."),
                displayOptions = { actor ->
                    chambermaidResult?.toIntOrNull()?.let { trueValue ->
                        recommendedNumberOptions(
                            title = text("侍女信息", "Chambermaid information"),
                            actor = actor,
                            trueValue = trueValue,
                            maxValue = 2,
                            footer = text("查询这两名玩家", "Checking these two players"),
                            pressureCostPerPoint = 1,
                            secondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                        )
                    }.orEmpty()
                },
            ),
            infoStep(
                roleName = "占卜师",
                enName = "Fortune Teller",
                tellPlayer = fortuneTellerResult,
                explanation = text("如果两名玩家中包含恶魔或红鲱鱼，向占卜师展示“有”；否则展示“没有”。", "Show Yes if either selected player is the Demon or red herring; otherwise show No."),
                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = text("查询这两名玩家", "Checking these two players"),
                hostInstruction = text("轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后展示“有”或“没有”。", "Tap the Fortune Teller to wake them. Have them point to two players, record both, then show Yes or No."),
                displayOptions = { actor ->
                    fortuneTellerMatched?.let { matched ->
                        recommendedYesNoOptions(
                            title = text("占卜师信息", "Fortune Teller information"),
                            truthfulYes = matched,
                            secondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                            footer = text("查询这两名玩家", "Checking these two players"),
                            propositionForValue = { value -> InformationProposition.BooleanResult(
                                BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                                cards.indexOf(actor) + 1,
                                listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
                                    cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                                },
                                value,
                            ) },
                        )
                    }.orEmpty()
                },
                recluseRegistrationKey = fortuneTellerRecluseRegistrationKey,
                recluseRegistrationTeams = listOf(ClocktowerTeam.Demon),
            ),
            infoStep(
                roleName = "管家",
                enName = "Butler",
                tellPlayer = butlerMaster?.let { text("今天的主人：${playerSeatLabel(cards, it)}", "Today's master: ${playerSeatLabel(cards, it)}") },
                explanation = text("管家每天选择一名主人，白天只能在主人投票时投票。", "The Butler chooses a master each day and may vote only when that master votes."),
                action = ClocktowerNightAction.ButlerMaster,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍管家，示意睁眼。让他指一名玩家作为今天的主人；记在心里，白天投票时提醒自己核对。", "Tap the Butler to wake them. Have them point to today's master and keep the choice available for checking during voting."),
            ),
            infoStep(
                roleName = "间谍",
                enName = "Spy",
                tellPlayer = if (poisonTarget == spyCard?.name) null else {
                    val grimoire = cards.joinToString("\n") { "${it.seatLabel(cards)}${text("：", ": ")}${it.hostRoleLabel(context, GameKind.Clocktower)}" }
                    listOfNotNull(spyDelta, grimoire).joinToString("\n\n")
                },
                explanation = if (poisonTarget == spyCard?.name) text("间谍已中毒：仍照常唤醒，但不要展示真实魔典，也不能改变登记身份。", "The Spy is poisoned: wake them normally, but do not show the real grimoire or alter registration.") else text("存活间谍每晚可以查看所有玩家的真实身份。", "A living Spy may view every player's true identity each night."),
                displayKind = ClocktowerDisplayKind.Grimoire,
                displayTitle = text("魔典", "Grimoire"),
                displayFooter = text("这些是所有玩家的真实身份。只给间谍短暂查看。", "These are every player's true identities. Show this only briefly to the Spy."),
                displayProposition = if (poisonTarget == spyCard?.name) null else InformationProposition.GrimoireState(
                    cards.mapIndexed { index, card -> GrimoireSeatView(index + 1, RoleId(requireNotNull(card.clocktowerRole).enName), card.eliminatedRound == null) },
                ),
                hostInstruction = if (poisonTarget == spyCard?.name) text("照常轻拍间谍示意睁眼，但不要展示真实魔典；停顿后示意闭眼。", "Wake the Spy normally, but do not show the real grimoire. Pause, then signal them to close their eyes.") else text("轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。", "Tap the Spy to wake them. Briefly show the Storyteller overview, then take the phone back and signal them to close their eyes."),
            ),
        )
    } else {
        buildList {
            add(
            infoStep(
                roleName = "投毒者",
                enName = "Poisoner",
                tellPlayer = poisonTarget?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                explanation = text("投毒者选择一名玩家，使其能力暂时失效。", "The Poisoner chooses a player whose ability stops working temporarily."),
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。", "Tap the Poisoner to wake them. Have them point to one player and record that player as tonight's poisoned target."),
            ),
            )
            add(
            infoStep(
                roleName = "管家",
                enName = "Butler",
                tellPlayer = butlerMaster?.let { text("今天的主人：${playerSeatLabel(cards, it)}", "Today's master: ${playerSeatLabel(cards, it)}") },
                explanation = text("管家每天选择一名主人。", "The Butler chooses a master each day."),
                action = ClocktowerNightAction.ButlerMaster,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍管家，示意睁眼。让他指今天的主人；白天投票时用这个记录提醒自己。", "Tap the Butler to wake them and have them point to today's master. Keep the choice available for checking during voting."),
            ),
            )
            add(
            infoStep(
                roleName = "共情者",
                enName = "Empath",
                tellPlayer = empathNumber,
                explanation = listOfNotNull(text("这个数字表示共情者两个存活邻居中有几个邪恶玩家。", "This number is how many of the Empath's living neighbors are evil."), empathRegistrationHint).joinToString("\n"),
                hostInstruction = text("轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。", "Tap the Empath to wake them. Show only the number; do not identify either neighbor."),
                displayOptions = { actor -> recommendedNumberOptions(text("共情者信息", "Empath information"), actor, empathReferenceValue, 2, text("邪恶存活邻居数量", "Evil living neighbors"), pressureCostPerPoint = 1) },
                spyRegistrationKey = empathRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                spyRegistrationHint = empathRegistrationHint,
                recluseRegistrationKey = empathRecluseRegistrationKey,
            ),
            )
            add(
            infoStep(
                roleName = "侍女",
                enName = "Chambermaid",
                tellPlayer = chambermaidResult,
                explanation = text("侍女选择两名玩家，得知其中有几人今晚因自己的能力醒来。", "The Chambermaid chooses two players and learns how many woke tonight because of their own ability."),
                action = ClocktowerNightAction.Chambermaid,
                displaySecondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = text("查询这两名玩家", "Checking these two players"),
                hostInstruction = text("轻拍侍女，示意睁眼。让她依次指两名玩家，不能选自己；点查询后只展示数字。", "Tap the Chambermaid to wake them. Have them point to two players other than themself, then show only the number."),
                displayOptions = { actor ->
                    chambermaidResult?.toIntOrNull()?.let { trueValue ->
                        recommendedNumberOptions(
                            title = text("侍女信息", "Chambermaid information"),
                            actor = actor,
                            trueValue = trueValue,
                            maxValue = 2,
                            footer = text("查询这两名玩家", "Checking these two players"),
                            pressureCostPerPoint = 1,
                            secondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                        )
                    }.orEmpty()
                },
            ),
            )
            add(
            infoStep(
                roleName = "占卜师",
                enName = "Fortune Teller",
                tellPlayer = fortuneTellerResult,
                explanation = text("如果两名玩家中包含恶魔或红鲱鱼，向占卜师展示“有”；否则展示“没有”。", "Show Yes if either selected player is the Demon or red herring; otherwise show No."),
                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = text("查询这两名玩家", "Checking these two players"),
                hostInstruction = text("轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后展示“有”或“没有”。", "Tap the Fortune Teller to wake them. Have them point to two players, record both, then show Yes or No."),
                displayOptions = { actor ->
                    fortuneTellerMatched?.let { matched ->
                        recommendedYesNoOptions(
                            title = text("占卜师信息", "Fortune Teller information"),
                            truthfulYes = matched,
                            secondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                            footer = text("查询这两名玩家", "Checking these two players"),
                            propositionForValue = { value -> InformationProposition.BooleanResult(
                                BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                                cards.indexOf(actor) + 1,
                                listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
                                    cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                                }, value,
                            ) },
                        )
                    }.orEmpty()
                },
                recluseRegistrationKey = fortuneTellerRecluseRegistrationKey,
                recluseRegistrationTeams = listOf(ClocktowerTeam.Demon),
            ),
            )
            if (lastExecutedName != null) {
                add(
                    infoStep(
                        roleName = "送葬者",
                        enName = "Undertaker",
                        tellPlayer = text("${playerSeatLabel(cards, lastExecutedName)} 的角色是 ${when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language).orEmpty()
                            else -> undertakerTarget?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}", "${playerSeatLabel(cards, lastExecutedName)} was ${when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language).orEmpty()
                            else -> undertakerTarget?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}"),
                        explanation = text("送葬者每晚得知今天被处决玩家的真实身份。", "Each night, the Undertaker learns the character of the player executed today."),
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("送葬者信息", "Undertaker information"),
                        displayPrimary = when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language)
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language)
                            else -> undertakerTarget?.clocktowerRole?.nameFor(language)
                        },
                        displayProposition = undertakerTarget?.let { target ->
                            val shownRole = when (target.name) {
                                spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))
                                recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))
                                else -> target.clocktowerRole
                            } ?: return@let null
                            InformationProposition.RoleAt(cards.indexOf(target) + 1, RoleId(shownRole.enName))
                        },
                        displayFooter = text("今天被处决：${playerSeatLabel(cards, lastExecutedName)}", "Executed today: ${playerSeatLabel(cards, lastExecutedName)}"),
                        hostInstruction = text("轻拍送葬者，示意睁眼。把今天被处决玩家的真实身份只给他看；看完后收回手机，示意闭眼。", "Tap the Undertaker to wake them. Show the executed player's identity only to that player, then take the phone back and signal them to close their eyes."),
                        displayOptions = {
                            recommendedRoleRevealOptions(
                                title = text("送葬者信息", "Undertaker information"),
                                truthfulRole = undertakerTarget?.clocktowerRole,
                                footer = text("今天被处决：${playerSeatLabel(cards, lastExecutedName)}", "Executed today: ${playerSeatLabel(cards, lastExecutedName)}"),
                            )
                        },
                        spyRegistrationKey = undertakerRegistrationKey,
                        spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        recluseRegistrationKey = undertakerRecluseRegistrationKey,
                        recluseRegistrationTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                    ),
                )
            }
            add(
            infoStep(
                roleName = "僧侣",
                enName = "Monk",
                tellPlayer = monkProtectedTarget?.let { text("已选择保护：${playerSeatLabel(cards, it)}。如果恶魔今晚选择该玩家，他不会死亡。", "Protected: ${playerSeatLabel(cards, it)}. If the Demon chooses this player tonight, they will not die.") },
                explanation = text("僧侣每晚选择除自己以外的一名玩家。若恶魔今晚攻击被保护的玩家，天亮时宣布无人死亡；不要透露是僧侣保护导致。", "Each night, the Monk protects one other player from the Demon. If that player is attacked, announce no death at dawn without revealing the protection."),
                action = ClocktowerNightAction.MonkProtect,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍僧侣，示意睁眼。让他指一名除自己以外的玩家，在下面记录为今晚保护目标。", "Tap the Monk to wake them. Have them point to another player and record that player as tonight's protected target."),
            ),
            )
            if (pendingNightNewDemonIdentityName != null) {
                val newDemon = requireNotNull(
                    aliveCards.firstOrNull {
                        it.name == pendingNightNewDemonIdentityName &&
                            it.clocktowerRole?.enName == "Imp"
                    },
                ) {
                    "Pending next-night new-Demon identity must reference the current living Imp."
                }
                add(
                    ClocktowerNightStepUi(
                        title = text("新恶魔身份", "New Demon identity"),
                        actor = newDemon,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text(
                            "轻拍 ${newDemon.seatLabel(cards)}，示意睁眼。把手机交给他，确认他现在是小恶魔；看完后收回手机并示意闭眼。",
                            "Tap ${newDemon.seatLabel(cards)} to wake them. Hand them the phone to confirm they are now the Imp, then take it back and signal them to close their eyes.",
                        ),
                        tellPlayer = text("你现在是小恶魔。", "You are now the Imp."),
                        explanation = text(
                            "猩红女巫在白天因恶魔死亡而继任。必须在新的小恶魔本夜行动前私下确认身份。",
                            "The Scarlet Woman became the Demon during the day. Confirm the new identity privately before the Imp acts tonight.",
                        ),
                        action = ClocktowerNightAction.NewDemonIdentity,
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("新身份", "New role"),
                        displayPrimary = text("小恶魔", "Imp"),
                        displayFooter = "",
                        roleEnName = "Imp",
                    ),
                )
            }
            add(
            ClocktowerNightStepUi(
                title = text("恶魔行动", "Demon action"),
                actor = aliveCards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon },
                isRealAction = aliveCards.any { it.clocktowerTeam == ClocktowerTeam.Demon },
                reason = if (aliveCards.none { it.clocktowerTeam == ClocktowerTeam.Demon }) text("当前没有存活恶魔。", "There is no living Demon.") else "",
                storytellerAction = aliveCards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon }?.let {
                    text("轻拍 ${it.seatLabel(cards)}，示意睁眼。让他指今晚要杀死的玩家，在下面记录；记录后示意闭眼。", "Tap ${it.seatLabel(cards)} to wake them. Have them point to tonight's kill target, record it, then signal them to close their eyes.")
                } ?: text("不要唤醒任何玩家，停顿 2-3 秒后继续。", "Do not wake anyone. Pause for 2–3 seconds, then continue."),
                tellPlayer = if (aliveCards.any { it.clocktowerTeam == ClocktowerTeam.Demon }) {
                    if (demonPoisonedTonight) {
                        text("恶魔已中毒，今晚杀人会失效。", "The Demon is poisoned, so tonight's kill will fail.")
                    } else {
                        pendingNightDeath?.let { text("已记录：今晚恶魔选择杀死 ${playerSeatLabel(cards, it)}。现在不要宣布死亡，等天亮统一宣布。", "Recorded: the Demon chose ${playerSeatLabel(cards, it)}. Do not announce the death until dawn.") }
                    }
                } else {
                    null
                },
                explanation = if (demonPoisonedTonight) text("可以记录恶魔选择，但天亮不会因此死亡。", "Record the Demon's choice, but it will not cause a death at dawn.") else text("恶魔选择的死亡目标会在天亮时统一公布。", "The Demon's chosen target is announced at dawn."),
                action = ClocktowerNightAction.DemonKill,
            ),
            )

            if (impSelfKillNeedsSuccessor) {
                add(
                    ClocktowerNightStepUi(
                        title = text("选择新小恶魔", "Choose the new Imp"),
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text(
                            "小恶魔选择自杀。请选择一名存活爪牙成为新的小恶魔。",
                            "The Imp chose themself. Choose a living Minion to become the new Imp.",
                        ),
                        tellPlayer = demonSuccessorTarget?.let { playerSeatLabel(cards, it) },
                        explanation = text(
                            "五名或更多玩家存活且猩红女巫能力正常时，必须由猩红女巫继承。",
                            "With five or more alive, a healthy Scarlet Woman must inherit.",
                        ),
                        action = ClocktowerNightAction.DemonSuccessor,
                        roleEnName = "Imp",
                        decisionOptions = demonSuccessorDecisionOptions(),
                    ),
                )
            }
            if (mayorCanRedirect) {
                val targetedMayor = requireNotNull(mayorTarget)
                add(
                    ClocktowerNightStepUi(
                        title = text("市长死亡裁定", "Mayor death ruling"),
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text("市长被恶魔击杀。选择让市长死亡，或将死亡转移给另一名玩家。", "The Demon attacked the Mayor. Let the Mayor die or redirect the death to another player."),
                        tellPlayer = mayorRedirectTarget?.let { target ->
                            if (target == targetedMayor.name) {
                                text("市长死亡", "Mayor dies")
                            } else {
                                text("死亡转移给 ${playerSeatLabel(cards, target)}", "Death redirected to ${playerSeatLabel(cards, target)}")
                            }
                        },
                        explanation = text("市长保持存活时，可以让另一名玩家代替死亡。选择死亡或受保护的玩家，可能导致今夜无人死亡。", "To keep the Mayor alive, another player may die instead. Choosing a dead or protected player can result in no death tonight."),
                        action = ClocktowerNightAction.MayorRedirect,
                        displayKind = ClocktowerDisplayKind.None,
                        roleEnName = "Mayor",
                        decisionOptions = mayorDecisionOptions(targetedMayor),
                    ),
                )
            }
            if (sageNightDeath != null && demonCard != null && sagePair != null) {
                add(
                    infoStep(
                        roleName = "贤者",
                        enName = "Sage",
                        tellPlayer = "${demonCard.seatLabel(cards)} / ${sagePair.second.seatLabel(cards)}",
                        explanation = text("贤者被恶魔杀死时，得知恶魔是两名玩家之一。", "When killed by the Demon, the Sage learns that the Demon is one of two players."),
                        displayKind = ClocktowerDisplayKind.EitherOne,
                        displayTitle = text("贤者信息", "Sage information"),
                        displayPrimary = text("恶魔", "Demon"),
                        displaySecondary = twoSeatNumbers(demonCard, sagePair.second),
                        displayFooter = text("在下面两位玩家之中", "One of these two players"),
                        hostInstruction = text("如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。", "If the Demon killed the Sage tonight, wake the Sage and show only them two players, one of whom is the Demon."),
                        displayOptions = { actor -> recommendedSageOptions(actor, demonCard) },
                        reliableDisplayOptions = { actor ->
                            recommendedSageOptions(actor, demonCard, truthfulOnly = true)
                        },
                    ),
                )
            }
            if (ravenkeeperTrigger != null) {
                add(
                    infoStep(
                        roleName = "守鸦人",
                        enName = "Ravenkeeper",
                        tellPlayer = ravenkeeperTarget?.let { text("${playerSeatLabel(cards, it)} 的角色是 ${when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language).orEmpty()
                            else -> ravenkeeperTargetCard?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}", "${playerSeatLabel(cards, it)} is ${when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language).orEmpty()
                            else -> ravenkeeperTargetCard?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}") },
                        explanation = text("守鸦人只有在夜晚死亡时才会当晚醒来，选择一名玩家并得知其真实身份。", "The Ravenkeeper wakes only when they die at night, then chooses a player and learns that player's character."),
                        action = ClocktowerNightAction.Ravenkeeper,
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("守鸦人信息", "Ravenkeeper information"),
                        displayPrimary = when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language)
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language)
                            else -> ravenkeeperTargetCard?.clocktowerRole?.nameFor(language)
                        },
                        displayProposition = ravenkeeperTargetCard?.let { target ->
                            val shownRole = when (target.name) {
                                spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))
                                recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))
                                else -> target.clocktowerRole
                            } ?: return@let null
                            InformationProposition.RoleAt(cards.indexOf(target) + 1, RoleId(shownRole.enName))
                        },
                        displayFooter = ravenkeeperTarget?.let { text("查询目标：${playerSeatLabel(cards, it)}", "Checked player: ${playerSeatLabel(cards, it)}") },
                        hostInstruction = text("轻拍 ${ravenkeeperTrigger.seatLabel(cards)}，示意睁眼。让他指一名玩家，在下面记录后把该玩家角色只给他看。", "Tap ${ravenkeeperTrigger.seatLabel(cards)} to wake them. Have them point to a player, record the target, and show that character only to the Ravenkeeper."),
                        displayOptions = {
                            recommendedRoleRevealOptions(
                                title = text("守鸦人信息", "Ravenkeeper information"),
                                truthfulRole = ravenkeeperTargetCard?.clocktowerRole,
                                footer = ravenkeeperTarget?.let { text("查询目标：${playerSeatLabel(cards, it)}", "Checked player: ${playerSeatLabel(cards, it)}") }.orEmpty(),
                            )
                        },
                        spyRegistrationKey = ravenkeeperRegistrationKey,
                        spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        recluseRegistrationKey = ravenkeeperRecluseRegistrationKey,
                        recluseRegistrationTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                    ),
                )
            }
            add(
                infoStep(
                    roleName = "间谍",
                    enName = "Spy",
                    tellPlayer = if (poisonTarget == spyCard?.name) null else {
                        val grimoire = cards.joinToString("\n") { "${it.seatLabel(cards)}${text("：", ": ")}${it.hostRoleLabel(context, GameKind.Clocktower)}" }
                        listOfNotNull(spyDelta, grimoire).joinToString("\n\n")
                    },
                    explanation = if (poisonTarget == spyCard?.name) text("间谍已中毒：仍照常唤醒，但不要展示真实魔典，也不能改变登记身份。", "The Spy is poisoned: wake them normally, but do not show the real grimoire or alter registration.") else text("存活间谍每晚查看真实魔典。", "A living Spy views the true grimoire each night."),
                    displayKind = ClocktowerDisplayKind.Grimoire,
                    displayTitle = text("魔典", "Grimoire"),
                    displayFooter = text("这些是所有玩家的真实身份。只给间谍短暂查看。", "These are every player's true identities. Show this only briefly to the Spy."),
                    displayProposition = if (poisonTarget == spyCard?.name) null else InformationProposition.GrimoireState(
                        cards.mapIndexed { index, card -> GrimoireSeatView(index + 1, RoleId(requireNotNull(card.clocktowerRole).enName), card.eliminatedRound == null) },
                    ),
                    hostInstruction = if (poisonTarget == spyCard?.name) text("照常唤醒间谍，但不要展示真实魔典。", "Wake the Spy normally, but do not show the real grimoire.") else text("轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。", "Tap the Spy to wake them. Briefly show the Storyteller overview, then take the phone back and signal them to close their eyes."),
                ),
            )
        }
    }

    val filteredNightSteps = unfilteredNightSteps.filter { step ->
        step.isRealAction &&
            (step.roleEnName == null || step.roleEnName in scriptRoleNames) &&
            !(script == ClocktowerScript.NoGreaterJoy && step.title in setOf(minionInfoTitle, demonInfoTitle))
    }
    val firstNightWakingRoleIds = buildSet {
        cards.forEach { card ->
            card.clocktowerRole?.enName?.let { add(RoleId(it)) }
            if (card.clocktowerRole?.enName == "Drunk") {
                card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }
            }
        }
    }
    val otherNightWakingRoleIds = buildSet {
        aliveCards.forEach { card ->
            card.clocktowerRole?.enName?.let { add(RoleId(it)) }
            if (card.clocktowerRole?.enName == "Drunk") {
                card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }
            }
        }
    }
    val otherNightResolvedFacts = ClocktowerResolvedFlowFacts(
        buildSet {
            if (pendingNightNewDemonIdentityName != null) add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)
            if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)
            if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)
            if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)
            if (impSelfKillNeedsSuccessor) add(ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED)
            if (sageNightDeath != null) add(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON)
        },
    )
    val nightSteps = if (phase == ClocktowerPhase.FirstNight) {
        ClocktowerProductionFirstNightFlow.order(
            ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
            playerCount = cards.size,
            inPlayRoleIds = firstNightWakingRoleIds,
            productionSteps = filteredNightSteps,
            identityOf = { step ->
                when {
                    step.title == minionInfoTitle -> ClocktowerProductionNightStepIdentity.minionInfo()
                    step.title == demonInfoTitle -> ClocktowerProductionNightStepIdentity.demonInfo()
                    step.action == ClocktowerNightAction.RedHerring ->
                        ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()
                    else -> ClocktowerProductionNightStepIdentity.role(
                        RoleId(requireNotNull(step.roleEnName) {
                            "First-night production step is missing a stable role identity."
                        }),
                    )
                }
            },
        )
    } else {
        ClocktowerProductionOtherNightFlow.order(
            ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
            playerCount = cards.size,
            wakingRoleIds = otherNightWakingRoleIds,
            resolvedFacts = otherNightResolvedFacts,
            productionSteps = filteredNightSteps,
            identityOf = { step ->
                when {
                    step.action == ClocktowerNightAction.NewDemonIdentity ->
                        ClocktowerProductionNightStepIdentity.newDemonIdentity()
                    step.action == ClocktowerNightAction.DemonSuccessor ->
                        ClocktowerProductionNightStepIdentity.demonSuccessor()
                    step.action == ClocktowerNightAction.MayorRedirect ->
                        ClocktowerProductionNightStepIdentity.mayorRedirect()
                    step.action == ClocktowerNightAction.DemonKill ->
                        ClocktowerProductionNightStepIdentity.role(RoleId("Imp"))
                    else -> ClocktowerProductionNightStepIdentity.role(
                        RoleId(requireNotNull(step.roleEnName) {
                            "Other-night production step is missing a stable role identity."
                        }),
                    )
                }
            },
        )
    }

    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            onDismiss = { playerDisplayStep = null },
        )
        return
    }

    pendingNewDemonName?.let { newDemonName ->
        val newDemon = cards.firstOrNull { it.name == newDemonName }
        val newDemonStep = ClocktowerNightStepUi(
            title = text("新恶魔", "New Demon"),
            actor = newDemon,
            isRealAction = newDemon != null,
            reason = "",
            storytellerAction = text("唤醒新的小恶魔。", "Wake the new Imp."),
            tellPlayer = text("你现在是小恶魔", "You are now the Imp"),
            explanation = "",
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = text("新身份", "New role"),
            displayPrimary = text("你现在是小恶魔", "You are now the Imp"),
            displayFooter = "",
        )
        ClocktowerNewDemonConfirmationScreen(
            newDemonLabel = newDemon?.seatLabel(cards).orEmpty(),
            hasNewDemon = newDemon != null,
            onShowPlayerDisplay = { playerDisplayStep = newDemonStep },
            onConfirm = onConfirmNewDemon,
        )
        return
    }

    if (phase == ClocktowerPhase.Dawn) {
        ClocktowerDawnSummaryScreen(
            round = round,
            cards = cards,
            events = events,
            pendingNightDeath = pendingNightDeath,
            onEnterDay = onAdvanceFromFirstNight,
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Overview) {
        val highestVoteText = when {
            highestVoteName != null -> text(
                "最高票 · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                "Highest · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
            )
            highestVoteCount >= executionThreshold -> text(
                "最高票 · 平票 $highestVoteCount 票",
                "Highest · tie at $highestVoteCount",
            )
            else -> text("最高票 · 无", "Highest · none")
        }
        ClocktowerDayOverviewScreen(
            round = round,
            cards = cards,
            aliveCount = aliveCards.size,
            executionThreshold = executionThreshold,
            highestVoteText = highestVoteText,
            showSlayerAction = scriptHasSlayer,
            slayerActionEnabled = slayerClaimantCandidates.isNotEmpty(),
            showArtistAction = scriptHasArtist,
            artistActionEnabled = artistClaimantCandidates.isNotEmpty(),
            actionsEnabled = gameOutcome == null,
            diagnosticContent = if (BuildConfig.DEBUG) {
                {
                    OutlinedButton(
                        onClick = { debugDiagnosticsExpanded = !debugDiagnosticsExpanded },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (debugDiagnosticsExpanded) "Hide developer diagnostics" else "Show developer diagnostics") }
                    if (debugDiagnosticsExpanded) Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("A4 device diagnostic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Debug only. Uses the live structural snapshot and synthetic native/fallback probes; it does not replay the full information history.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            OutlinedButton(
                                onClick = { a4DeviceBenchmarkRuns += 1 },
                                enabled = a4DiagnosticAvailable,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Run A4 benchmark (11 samples)") }
                            OutlinedButton(
                                onClick = { a4PrewarmCancellationProbeRuns += 1 },
                                enabled = a4DiagnosticAvailable,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Run A4 prewarm cancellation probe") }
                            if (!a4DiagnosticAvailable) {
                                Text(
                                    "Available only for a 5-player Trouble Brewing game with complete role data. Larger exact enumeration can exhaust device memory before ZDD compression.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            a4DeviceBenchmarkReport?.let { report ->
                                Text(report.toLogLine(), style = MaterialTheme.typography.bodySmall)
                            }
                            a4DeviceBenchmarkError?.let { error ->
                                Text("Benchmark failed: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            a4PrewarmCancellationProbeResult?.let { result ->
                                Text(result, style = MaterialTheme.typography.bodySmall)
                            }
                            a4PrewarmCancellationProbeError?.let { error ->
                                Text("Prewarm cancellation probe failed: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            } else null,
            onStartNomination = {
                nominatorName = null
                nomineeName = null
                dayMode = ClocktowerDayMode.Nomination
            },
            onOpenSlayer = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
                dayMode = ClocktowerDayMode.Slayer
            },
            onOpenArtist = {
                onSelectArtistClaimant(null)
                dayMode = ClocktowerDayMode.Artist
            },
            onEndDay = {
                onSelectExecution(highestVoteName?.takeIf { highestVoteCount >= executionThreshold })
                dayMode = ClocktowerDayMode.EndConfirm
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Nomination) {
        val nominatorCard = cards.firstOrNull { it.name == nominatorName }
        val nomineeCard = cards.firstOrNull { it.name == nomineeName }
        val virginFirstNomination = nomineeCard?.clocktowerRole?.enName == "Virgin" && !virginUsed
        val virginAbilityWorks = virginFirstNomination && poisonTarget != nomineeCard?.name
        val virginRegistrationKey = nominatorCard
            ?.takeIf { it.name == spyCard?.name && virginFirstNomination }
            ?.let { registrationKey("Virgin", it.name) }
        val virginExecutes = virginAbilityWorks &&
            (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || spyRegistersGood(virginRegistrationKey))
        val specialNotice = when {
            virginExecutes -> text(
                "${playerSeatLabel(cards, nomineeName)} 首次被真实镇民提名：不进行投票，提名者将立即被处决。",
                "${playerSeatLabel(cards, nomineeName)} was first nominated by a Townsfolk: skip voting and execute the nominator.",
            )
            virginFirstNomination -> text(
                "这是圣女第一次被提名，但能力不会处决提名者；记录能力已用过后继续投票。",
                "This is the Virgin's first nomination, but the ability does not execute the nominator. Mark it spent and continue.",
            )
            else -> null
        }
        ClocktowerNominationScreen(
            round = round,
            cards = cards,
            aliveCards = aliveCards,
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            specialNotice = specialNotice,
            specialNoticeIsDanger = virginExecutes,
            continueLabel = when {
                virginExecutes -> text("确认并处决提名者", "Confirm and execute nominator")
                virginFirstNomination -> text("记录能力，进入投票", "Record ability and continue")
                else -> text("确认提名，进入投票", "Confirm nomination and vote")
            },
            actionsEnabled = gameOutcome == null,
            onSelectNominator = { nominatorName = if (nominatorName == it) null else it },
            onSelectNominee = { nomineeName = if (nomineeName == it) null else it },
            onContinue = {
                val chosenNominator = nominatorName
                val chosenNominee = nomineeName
                if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {
                    recordSpyRegistration(virginRegistrationKey, listOf(ClocktowerTeam.Townsfolk))
                    onVirginNomination(chosenNominator, chosenNominee, virginExecutes)
                }
                if (chosenNominator != null && chosenNominee != null && virginExecutes) {
                    onRecordEvent(
                        ClocktowerEventType.Nomination,
                        text("提名", "Nomination"),
                        "${playerSeatLabel(cards, chosenNominator)} → ${playerSeatLabel(cards, chosenNominee)}",
                        listOf(chosenNominator, chosenNominee),
                    )
                }
                if (!virginExecutes) {
                    currentVoteCount = 0
                    dayMode = ClocktowerDayMode.Vote
                }
            },
            onCancel = {
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
            specialContent = {
                if (virginRegistrationKey != null && spyCard != null) {
                    SpyRegistrationPanel(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        automaticStorytellerStyle = automaticStorytellerStyle,
                        cards = cards,
                        spy = spyCard,
                        teams = listOf(ClocktowerTeam.Townsfolk),
                        registersGood = spyRegistersGood(virginRegistrationKey),
                        registeredRoleEnName = spyRegistrationRole[virginRegistrationKey],
                        recommendations = registrationRecommendationOptions(
                            key = virginRegistrationKey,
                            roleEnName = "Virgin",
                            teams = listOf(ClocktowerTeam.Townsfolk),
                            detail = ClocktowerRegistrationDetail.Role,
                            subject = spyCard,
                            isSpy = true,
                            outcomeMisinformationPressure = 5,
                        ),
                        enabled = spyCanRegister(),
                        onRegistersGoodChange = { good ->
                            spyRegistrationGood[virginRegistrationKey] = good
                            if (good && spyRegistrationRole[virginRegistrationKey] == null) {
                                spyRegistrationRole[virginRegistrationKey] = "Washerwoman"
                            }
                        },
                        onRoleChange = { spyRegistrationRole[virginRegistrationKey] = it },
                    )
                }
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Vote) {
        val highestVoteText = when {
            highestVoteName != null -> text(
                "当前最高：${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                "Current highest: ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
            )
            highestVoteCount >= executionThreshold -> text(
                "当前最高为平票：$highestVoteCount 票；暂时无人被处决。",
                "Current high vote is tied at $highestVoteCount; nobody is set for execution.",
            )
            else -> text("当前还没有达到门槛的最高票。", "No qualifying high vote has been recorded yet.")
        }
        val recordVoteEvent = {
            onRecordEvent(
                ClocktowerEventType.Vote,
                text("提名与投票", "Nomination and vote"),
                "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                listOfNotNull(nominatorName, nomineeName),
            )
        }
        ClocktowerVoteScreen(
            round = round,
            cards = cards,
            aliveCount = aliveCards.size,
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            voteCount = currentVoteCount,
            highestVoteText = highestVoteText,
            actionsEnabled = gameOutcome == null,
            onVoteCountChange = { currentVoteCount = it },
            onRecordAndContinue = {
                recordVoteEvent()
                recordCurrentVote()
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
            onRecordAndEndDay = {
                recordVoteEvent()
                onSelectExecution(recordCurrentVote())
                dayMode = ClocktowerDayMode.EndConfirm
            },
            onBack = {
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Nomination
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.EndConfirm) {
        ClocktowerExecutionConfirmScreen(
            round = round,
            cards = cards,
            executionThreshold = executionThreshold,
            selectedExecution = selectedExecution,
            highestVoteCount = highestVoteCount,
            actionsEnabled = gameOutcome == null,
            onConfirm = onConfirmDay,
            onBack = { dayMode = ClocktowerDayMode.Overview },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Slayer) {
        val slayerTargetCard = cards.firstOrNull { it.name == slayerTargetName }
        val slayerRecluseRecommendations = slayerTargetCard
            ?.takeIf { it.clocktowerRole?.enName == "Recluse" }
            ?.let { recluse ->
                registrationRecommendationOptions(
                    key = registrationKey("SlayerRecluse", recluse.name),
                    roleEnName = "Slayer",
                    teams = listOf(ClocktowerTeam.Demon),
                    detail = ClocktowerRegistrationDetail.Role,
                    subject = recluse,
                    isSpy = false,
                    outcomeMisinformationPressure = 4,
                    specialRegistrationBalanceImpact = 1,
                )
            }
            .orEmpty()
        val automaticSlayerRecluseRegistration = WeightedStableSelector.selectStyle(
            slayerRecluseRecommendations,
            automaticStorytellerStyle,
            ClocktowerRegistrationRecommendationOption::style,
        )
        ClocktowerSpecialDayActionScreen(
            round = round,
            title = text("杀手行动", "Slayer action"),
            primaryLabel = text("结算杀手行动", "Resolve Slayer action"),
            primaryEnabled = slayerClaimantName != null && slayerTargetName != null && gameOutcome == null,
            onPrimary = {
                val claimantName = slayerClaimantName
                val targetName = slayerTargetName
                if (claimantName != null && targetName != null) {
                    val targetIsHealthyRecluse =
                        slayerTargetCard?.clocktowerRole?.enName == "Recluse" &&
                            poisonTarget != targetName
                    val recluseRegistersDemon = if (
                        automaticStorytellerInfo &&
                        targetIsHealthyRecluse &&
                        automaticSlayerRecluseRegistration != null
                    ) {
                        automaticSlayerRecluseRegistration.usesSpecialRegistration
                    } else {
                        slayerRecluseRegistersDemon
                    }
                    onSlayerShot(claimantName, targetName, recluseRegistersDemon)
                    slayerClaimantName = null
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = false
                    dayMode = ClocktowerDayMode.Overview
                }
            },
            onBack = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
                dayMode = ClocktowerDayMode.Overview
            },
        ) {
            if (slayerClaimantCandidates.isEmpty()) {
                Text(
                    text("没有可用的杀手声称者", "No eligible Slayer claimant"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                HostActionSection(title = text("谁声称发动杀手能力？", "Who claims the Slayer ability?")) {
                    SelectablePlayerChips(
                        cards = slayerClaimantCandidates,
                        selectedName = slayerClaimantName,
                        enabled = gameOutcome == null,
                        allCards = cards,
                        onSelect = {
                            slayerClaimantName = if (slayerClaimantName == it) null else it
                            if (slayerTargetName == it) slayerTargetName = null
                        },
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                HostActionSection(title = text("选择目标", "Choose target")) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != slayerClaimantName },
                        selectedName = slayerTargetName,
                        enabled = gameOutcome == null,
                        allCards = cards,
                        onSelect = {
                            slayerTargetName = if (slayerTargetName == it) null else it
                            slayerRecluseRegistersDemon = false
                        },
                    )
                }
                if (slayerClaimantName != null && slayerTargetName != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            "${playerSeatLabel(cards, slayerClaimantName)} → ${playerSeatLabel(cards, slayerTargetName)}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (slayerTargetCard?.clocktowerRole?.enName == "Recluse") {
                    val slayerRecluse = slayerTargetCard
                    RecluseRegistrationPanel(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        automaticStorytellerStyle = automaticStorytellerStyle,
                        cards = cards,
                        recluse = slayerRecluse,
                        teams = listOf(ClocktowerTeam.Demon),
                        registersEvil = slayerRecluseRegistersDemon,
                        registeredRoleEnName = if (slayerRecluseRegistersDemon) "Imp" else null,
                        recommendations = slayerRecluseRecommendations,
                        enabled = poisonTarget != slayerTargetName,
                        onRegistersEvilChange = { slayerRecluseRegistersDemon = it },
                        onRoleChange = {},
                    )
                }
            }
        }
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Artist) {
        val artistClaimant = cards.firstOrNull { it.name == artistClaimantName }
        val artistReliable = artistClaimant?.let {
            it.clocktowerRole?.enName == "Artist" && it.name != poisonTarget
        } == true
        val answerRecommendations = if (artistClaimant != null && artistTruthfulAnswer != null) {
            if (artistReliable) {
                listOf(Triple(RecommendationStyle.BALANCED, artistTruthfulAnswer, false))
            } else {
                recommendationCoordinator.recommendCategory(
                    listOf(
                        UnreliableCategoricalCandidate(
                            id = "yes",
                            isTruthful = artistTruthfulAnswer,
                            misinformationPressure = if (artistTruthfulAnswer) 0 else 3,
                        ),
                        UnreliableCategoricalCandidate(
                            id = "no",
                            isTruthful = !artistTruthfulAnswer,
                            misinformationPressure = if (artistTruthfulAnswer) 3 else 0,
                        ),
                    ),
                ).map { recommendation ->
                    Triple(
                        recommendation.style,
                        recommendation.candidateId == "yes",
                        recommendation.warningIds.isNotEmpty(),
                    )
                }
            }
        } else {
            emptyList()
        }
        val artistInformationReliability = when {
            artistClaimant?.name == poisonTarget -> InformationReliability.POISONED
            artistClaimant?.clocktowerRole?.enName == "Drunk" &&
                artistClaimant.clocktowerShownRole?.enName == "Artist" ->
                InformationReliability.DRUNK
            else -> InformationReliability.RELIABLE
        }
        val automaticArtistRecommendation = if (artistInformationReliability != InformationReliability.RELIABLE) {
            recommendationCoordinator.selectInformation(
                options = answerRecommendations,
                reliability = artistInformationReliability,
                style = automaticStorytellerStyle,
                evilAdvantage = currentDynamicStorytellerState.evilAdvantage,
                stableKey = "$recommendationKey:artist:$round:${artistClaimant?.name}",
                recentMisinformationStreak = recentMisinformationStreak(artistClaimant),
                stableIdOf = { "${it.first.name}:${it.second}" },
                isTruthful = { it.second == artistTruthfulAnswer },
                misinformationPressure = { if (it.second == artistTruthfulAnswer) 0 else 3 },
                styleOf = { it.first },
            )
        } else {
            WeightedStableSelector.selectStyle(
                answerRecommendations,
                automaticInformationStyle,
            ) { it.first }
        }
        val automaticArtistAnswer = automaticArtistRecommendation?.second
        LaunchedEffect(automaticStorytellerInfo, artistClaimantName, artistTruthfulAnswer, automaticArtistAnswer) {
            if (automaticStorytellerInfo && automaticArtistAnswer != null && artistShownAnswer != automaticArtistAnswer) {
                onSelectArtistShownAnswer(automaticArtistAnswer)
            }
        }
        ClocktowerSpecialDayActionScreen(
            round = round,
            title = text("艺术家提问", "Artist question"),
            primaryLabel = text("记录艺术家提问", "Record Artist question"),
            primaryEnabled = artistClaimantName != null &&
                artistTruthfulAnswer != null &&
                artistShownAnswer != null &&
                gameOutcome == null,
            onPrimary = onConfirmArtistQuestion,
            onBack = {
                onSelectArtistClaimant(null)
                dayMode = ClocktowerDayMode.Overview
            },
        ) {
            HostActionSection(title = text("选择提问者", "Choose claimant")) {
                SelectablePlayerChips(
                    cards = artistClaimantCandidates,
                    selectedName = artistClaimantName,
                    enabled = gameOutcome == null,
                    allCards = cards,
                    onSelect = { onSelectArtistClaimant(if (artistClaimantName == it) null else it) },
                )
            }
            if (artistClaimant != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                HostActionSection(title = text("问题的真实答案", "Truthful answer")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(true, false).forEach { answer ->
                            val label = if (answer) text("是", "Yes") else text("否", "No")
                            if (artistTruthfulAnswer == answer) {
                                Button(
                                    onClick = { onSelectArtistTruthfulAnswer(answer) },
                                    modifier = Modifier.weight(1f),
                                ) { Text(label) }
                            } else {
                                OutlinedButton(
                                    onClick = { onSelectArtistTruthfulAnswer(answer) },
                                    modifier = Modifier.weight(1f),
                                ) { Text(label) }
                            }
                        }
                    }
                }
            }
            if (artistClaimant != null && artistTruthfulAnswer != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                HostActionSection(title = text("告诉玩家的答案", "Answer to show")) {
                    answerRecommendations
                        .filter { !automaticStorytellerInfo || it == automaticArtistRecommendation }
                        .forEach { (style, answer, warning) ->
                            val answerLabel = if (answer) text("是", "Yes") else text("否", "No")
                            val label = if (artistReliable) {
                                answerLabel
                            } else {
                                "${recommendationStyleLabel(style)} · $answerLabel${if (warning) text(" · 高影响", " · high impact") else ""}"
                            }
                            if (automaticStorytellerInfo) {
                                Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            } else if (artistShownAnswer == answer) {
                                Button(
                                    onClick = { onSelectArtistShownAnswer(answer) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(label) }
                            } else {
                                OutlinedButton(
                                    onClick = { onSelectArtistShownAnswer(answer) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(label) }
                            }
                        }
                }
            }
        }
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Klutz) {
        val klutzChoiceCard = cards.firstOrNull { it.name == klutzChoiceName }
        val klutzRegistrationKey = klutzChoiceCard
            ?.takeIf { it.name == spyCard?.name }
            ?.let { registrationKey("Klutz", it.name) }
        val klutzSpyRecommendations = if (klutzRegistrationKey != null && spyCard != null) {
            registrationRecommendationOptions(
                key = klutzRegistrationKey,
                roleEnName = "Klutz",
                teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                detail = ClocktowerRegistrationDetail.Role,
                subject = spyCard,
                isSpy = true,
                outcomeMisinformationPressure = 5,
                specialRegistrationBalanceImpact = -1,
            )
        } else {
            emptyList()
        }
        val automaticKlutzSpyRegistration = WeightedStableSelector.selectStyle(
            klutzSpyRecommendations,
            automaticStorytellerStyle,
            ClocktowerRegistrationRecommendationOption::style,
        )
        ClocktowerSpecialDayActionScreen(
            round = round,
            title = text("呆瓜选择", "Klutz choice"),
            primaryLabel = text("确认呆瓜选择", "Confirm Klutz choice"),
            primaryEnabled = klutzChoiceName != null && gameOutcome == null,
            onPrimary = {
                if (
                    automaticStorytellerInfo &&
                    spyCanRegister() &&
                    klutzRegistrationKey != null &&
                    automaticKlutzSpyRegistration != null
                ) {
                    spyRegistrationGood[klutzRegistrationKey] =
                        automaticKlutzSpyRegistration.usesSpecialRegistration
                    if (automaticKlutzSpyRegistration.usesSpecialRegistration) {
                        automaticKlutzSpyRegistration.registeredRoleEnName?.let {
                            spyRegistrationRole[klutzRegistrationKey] = it
                        }
                    }
                }
                recordSpyRegistration(klutzRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))
                onConfirmKlutzChoice(spyRegistersGood(klutzRegistrationKey))
            },
        ) {
            pendingKlutzName?.let { klutzName ->
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        playerSeatLabel(cards, klutzName),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HostActionSection(title = text("选择一名存活玩家", "Choose a living player")) {
                SelectablePlayerChips(
                    cards = aliveCards.filter { it.name != pendingKlutzName },
                    selectedName = klutzChoiceName,
                    enabled = gameOutcome == null,
                    allCards = cards,
                    onSelect = { onSelectKlutzChoice(if (klutzChoiceName == it) null else it) },
                )
            }
            if (klutzRegistrationKey != null && spyCard != null) {
                SpyRegistrationPanel(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    automaticStorytellerStyle = automaticStorytellerStyle,
                    cards = cards,
                    spy = spyCard,
                    teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                    registersGood = spyRegistersGood(klutzRegistrationKey),
                    registeredRoleEnName = spyRegistrationRole[klutzRegistrationKey],
                    recommendations = klutzSpyRecommendations,
                    enabled = spyCanRegister(),
                    onRegistersGoodChange = { good ->
                        spyRegistrationGood[klutzRegistrationKey] = good
                        if (good && spyRegistrationRole[klutzRegistrationKey] == null) {
                            spyRegistrationRole[klutzRegistrationKey] = "Washerwoman"
                        }
                    },
                    onRoleChange = { spyRegistrationRole[klutzRegistrationKey] = it },
                )
            }
        }
        return
    }

    if (phase == ClocktowerPhase.FirstNight && !nightStarted) {
        ClocktowerStorytellerRecommendationScreen(
            title = text("说书人开局准备", "STORYTELLER SETUP"),
            subtitle = text("首夜裁定推荐", "First-night recommendations"),
            description = text(
                "这是说书人私密页面。确认推荐与裁定后，直接进入首夜流程。",
                "This is a private Storyteller screen. Review the plan, then begin the first night.",
            ),
            buttonLabel = text("确认裁定，开始首夜", "Confirm plan and begin first night"),
            onStartNight = { nightStarted = true },
        ) {
            StorytellerRecommendationCard(
                automaticStorytellerInfo = automaticStorytellerInfo,
                state = recommendationUiState,
                selectedStyle = selectedRecommendationStyle,
                appliedStyle = appliedRecommendationStyle,
                cards = cards,
                script = script,
                language = language,
                lockedDecisions = lockedRecommendationDecisions,
                onSelectStyle = { selectedRecommendationStyle = it },
                onApply = { plan ->
                    onApplyRecommendation(plan)
                    appliedRecommendationStyle = plan.style
                },
                onReevaluate = { nextLockedDecisions ->
                    lockedRecommendationDecisions = preservingCommittedIdentity(nextLockedDecisions)
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                    appliedRecommendationStyle = null
                },
                onClearLocks = {
                    lockedRecommendationDecisions = committedIdentityDecisions
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                },
            )
        }
        return
    }

    if (phase == ClocktowerPhase.Night && !nightStarted) {
        ClocktowerStorytellerRecommendationScreen(
            title = text("说书人", "STORYTELLER"),
            subtitle = text("第 $round 夜即将开始", "Night $round is about to begin"),
            description = text(
                "这是说书人私密页面。准备好后开始夜晚流程。",
                "This is a private Storyteller screen. Begin the night flow when ready.",
            ),
            buttonLabel = text("开始第 $round 夜流程", "Begin night $round"),
            onStartNight = { nightStarted = true },
        ) {
            ClocktowerNightReadyCard()
        }
        return
    }

    if ((phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) && nightStarted && nightSteps.isNotEmpty()) {
        val currentStepIndex = nightStepIndex.coerceIn(0, nightSteps.lastIndex)
        val currentStep = nightSteps[currentStepIndex]
        val selectedNightName = when (currentStep.action) {
            ClocktowerNightAction.RedHerring -> redHerring
            ClocktowerNightAction.Poison -> poisonDraftTarget
            ClocktowerNightAction.ButlerMaster -> butlerMaster
            ClocktowerNightAction.MonkProtect -> monkProtectedDraftTarget
            ClocktowerNightAction.DemonKill -> demonAttackDraftTarget
            ClocktowerNightAction.MayorRedirect -> mayorRedirectDraftTarget
            ClocktowerNightAction.DemonSuccessor -> demonSuccessorTarget
            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
            else -> null
        }
        val advanceNightStep = {
            if (currentStep.action == ClocktowerNightAction.Poison) {
                onConfirmPoisonTarget()
            }
            if (currentStep.action == ClocktowerNightAction.MonkProtect) {
                onConfirmMonkProtectedTarget()
            }
            if (currentStep.action == ClocktowerNightAction.DemonKill) {
                onConfirmDemonAttack()
            }
            if (currentStep.action == ClocktowerNightAction.MayorRedirect) {
                if (automaticStorytellerInfo) {
                    val autoOptions = unifiedDecisionPool(currentStep.decisionOptions, "mayor-redirect")
                        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
                        ?.map { it.payload }
                        .orEmpty()
                    val selected = WeightedStableSelector.selectStyle(
                        autoOptions,
                        automaticStorytellerStyle,
                        ClocktowerDecisionOption::recommendationStyle,
                    )
                    if (selected != null && mayorRedirectDraftTarget == selected.targetName) {
                        val auditId = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}|mayor-redirect"
                        val dimensions = SelectionAuditDimensions(
                            playerCount = cards.size,
                            phase = StorytellerPhase.NIGHT,
                            style = automaticStorytellerStyle,
                        )
                        selectionDistributionTelemetry.recordPreview(
                            SelectionAuditRecord(
                                selectionId = auditId,
                                dimensions = dimensions,
                                candidates = currentStep.decisionOptions.map { option ->
                                    SelectionAuditCandidate("mayor-redirect", if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING)
                                },
                            ),
                        )
                        selectionDistributionTelemetry.recordCommittedSelection(
                            SelectionAuditCommit(auditId, dimensions, "mayor-redirect"),
                        )
                    }
                }
                onConfirmMayorRedirectTarget()
            }
            if (currentStep.action == ClocktowerNightAction.DemonSuccessor && automaticStorytellerInfo) {
                val autoOptions = unifiedDecisionPool(currentStep.decisionOptions, "demon-succession")
                    ?.candidatesFor(SelectionExecutionPolicy.AUTO)
                    ?.map { it.payload }
                    .orEmpty()
                val selected = WeightedStableSelector.selectStyle(
                    autoOptions,
                    automaticStorytellerStyle,
                    ClocktowerDecisionOption::recommendationStyle,
                )
                if (selected != null && demonSuccessorTarget == selected.targetName) {
                    val auditId = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}|demon-succession"
                    val dimensions = SelectionAuditDimensions(cards.size, StorytellerPhase.NIGHT, automaticStorytellerStyle)
                    selectionDistributionTelemetry.recordPreview(
                        SelectionAuditRecord(
                            selectionId = auditId,
                            dimensions = dimensions,
                            candidates = currentStep.decisionOptions.map { option ->
                                SelectionAuditCandidate("demon-succession", if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING)
                            },
                        ),
                    )
                    selectionDistributionTelemetry.recordCommittedSelection(
                        SelectionAuditCommit(auditId, dimensions, "demon-succession"),
                    )
                }
            }
            recordSpyRegistration(
                currentStep.spyRegistrationKey,
                currentStep.spyRegistrationTeams,
                currentStep.spyRegistrationDetail,
            )
            recordRecluseRegistration(currentStep.recluseRegistrationKey, currentStep.recluseRegistrationTeams)
            recordNightStep(currentStep)
            if (currentStepIndex < nightSteps.lastIndex) {
                nightStepIndex = currentStepIndex + 1
            } else {
                onConfirmNight()
            }
        }

        LaunchedEffect(
            automaticStorytellerInfo,
            currentStepIndex,
            currentStep.action,
            currentStep.isRealAction,
            redHerring,
        ) {
            if (
                shouldAutoAdvanceRedHerring(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    isRedHerringStep = currentStep.action == ClocktowerNightAction.RedHerring,
                    isRealAction = currentStep.isRealAction,
                    hasSelectedRedHerring = redHerring != null,
                )
            ) {
                advanceNightStep()
            }
        }

        ClocktowerNightActiveScreen(
            title = if (phase == ClocktowerPhase.FirstNight) {
                text("第 1 夜", "Night 1")
            } else {
                text("第 $round 夜", "Night $round")
            },
            subtitle = text("当前阶段：${currentStep.title}", "Current: ${currentStep.title}"),
            progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
            canGoPrevious = currentStepIndex > 0,
            nextEnabled = currentStep.action !in setOf(
                ClocktowerNightAction.MayorRedirect,
                ClocktowerNightAction.DemonSuccessor,
            ) || selectedNightName != null,
            onPrevious = {
                if (currentStepIndex > 0) {
                    nightStepIndex = currentStepIndex - 1
                }
            },
            onNext = advanceNightStep,
        ) {
            if (BuildConfig.DEBUG) {
                OutlinedButton(
                    onClick = { debugDiagnosticsExpanded = !debugDiagnosticsExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (debugDiagnosticsExpanded) "Hide developer diagnostics" else "Show developer diagnostics") }
            }
            if (BuildConfig.DEBUG && debugDiagnosticsExpanded) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("A4 device diagnostic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Debug only. Starts an isolated shadow prewarm from the current snapshot and cancels it without changing this night step.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(
                            onClick = { a4PrewarmCancellationProbeRuns += 1 },
                            enabled = a4DiagnosticAvailable,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Run A4 prewarm cancellation probe") }
                        OutlinedButton(
                            onClick = { unifiedSetupSelectorBenchmarkRuns += 1 },
                            enabled = a4DiagnosticAvailable,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Run unified setup selector benchmark (11 samples)") }
                        a4PrewarmCancellationProbeResult?.let { result ->
                            Text(result, style = MaterialTheme.typography.bodySmall)
                        }
                        a4PrewarmCancellationProbeError?.let { error ->
                            Text("Prewarm cancellation probe failed: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        unifiedSetupSelectorBenchmarkReport?.let { report ->
                            Text(report.toLogLine(), style = MaterialTheme.typography.bodySmall)
                        }
                        unifiedSetupSelectorBenchmarkError?.let { error ->
                            Text("Unified setup selector diagnostic failed: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            ClocktowerNightStepCardLocalized(
                recommendationCoordinator = recommendationCoordinator,
                automaticStorytellerInfo = automaticStorytellerInfo,
                automaticStorytellerStyle = automaticStorytellerStyle,
                phase = phase,
                debugDiagnosticsExpanded = debugDiagnosticsExpanded,
                selectionDistributionTelemetry = selectionDistributionTelemetry,
                evilAdvantage = currentDynamicStorytellerState.evilAdvantage,
                informationDecisionKey = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}",
                cards = cards,
                aliveCards = aliveCards,
                step = currentStep,
                spyCard = spyCard,
                spyRegistrationGood = spyRegistersGood(currentStep.spyRegistrationKey),
                spyRegisteredRoleEnName = currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] },
                spyRegistrationRecommendations = registrationRecommendationOptions(currentStep, spyCard, isSpy = true),
                spyCanRegister = spyCanRegister(),
                onSpyRegistrationGoodChange = { good ->
                    currentStep.spyRegistrationKey?.let { key ->
                        spyRegistrationGood[key] = good
                        if (good && currentStep.spyRegistrationDetail == ClocktowerRegistrationDetail.Role && spyRegistrationRole[key] == null) {
                            spyRegistrationRole[key] = completeTroubleBrewingRoles
                                .firstOrNull { it.team in currentStep.spyRegistrationTeams && it.enName != "Spy" }
                                ?.enName
                                .orEmpty()
                        }
                        if (!good && redHerring == spyCard?.name && currentStep.action == ClocktowerNightAction.RedHerring) {
                            onSelectRedHerring(null)
                        }
                    }
                },
                onSpyRegistrationRoleChange = { roleName ->
                    currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] = roleName }
                },
                recluseCard = recluseCard,
                recluseRegistrationEvil = recluseRegistersEvil(currentStep.recluseRegistrationKey),
                recluseRegisteredRoleEnName = currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] },
                recluseRegistrationRecommendations = registrationRecommendationOptions(currentStep, recluseCard, isSpy = false),
                recluseCanRegister = recluseCanRegister(),
                onRecluseRegistrationEvilChange = { evil ->
                    currentStep.recluseRegistrationKey?.let { key ->
                        recluseRegistrationEvil[key] = evil
                        if (evil && currentStep.recluseRegistrationTeams.isNotEmpty() && recluseRegistrationRole[key] == null) {
                            recluseRegistrationRole[key] = completeTroubleBrewingRoles
                                .firstOrNull { it.team in currentStep.recluseRegistrationTeams }
                                ?.enName
                                .orEmpty()
                        }
                    }
                },
                onRecluseRegistrationRoleChange = { roleName ->
                    currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] = roleName }
                },
                selectedName = selectedNightName,
                fortuneTellerFirst = fortuneTellerFirst,
                fortuneTellerSecond = fortuneTellerSecond,
                chambermaidFirst = chambermaidFirst,
                chambermaidSecond = chambermaidSecond,
                onSelectName = { name ->
                    when (currentStep.action) {
                        ClocktowerNightAction.RedHerring -> onSelectRedHerring(if (redHerring == name) null else name)
                        ClocktowerNightAction.Poison -> onSelectPoisonTarget(if (poisonTarget == name) null else name)
                        ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(if (butlerMaster == name) null else name)
                        ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(if (monkProtectedTarget == name) null else name)
                        ClocktowerNightAction.DemonKill -> onSelectNightDeath(if (demonAttackDraftTarget == name) null else name)
                        ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(if (mayorRedirectDraftTarget == name) null else name)
                        ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(if (demonSuccessorTarget == name) null else name)
                        ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(if (ravenkeeperTarget == name) null else name)
                        else -> Unit
                    }
                },
                onSelectFortuneTellerFirst = {
                    onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it)
                },
                onSelectFortuneTellerSecond = {
                    onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it)
                },
                onSelectChambermaidFirst = {
                    onSelectChambermaidFirst(if (chambermaidFirst == it) null else it)
                },
                onSelectChambermaidSecond = {
                    onSelectChambermaidSecond(if (chambermaidSecond == it) null else it)
                },
                onApplyRecommendedDisplayOption = { option ->
                    currentStep.spyRegistrationKey?.let { key ->
                        option.spyRegistersGood?.let { good ->
                            spyRegistrationGood[key] = good
                            if (good) {
                                option.spyRegisteredRoleEnName?.let { spyRegistrationRole[key] = it }
                            } else {
                                spyRegistrationRole.remove(key)
                            }
                        }
                    }
                    currentStep.recluseRegistrationKey?.let { key ->
                        option.recluseRegistersEvil?.let { evil ->
                            recluseRegistrationEvil[key] = evil
                            if (evil) {
                                option.recluseRegisteredRoleEnName?.let { recluseRegistrationRole[key] = it }
                            } else {
                                recluseRegistrationRole.remove(key)
                            }
                        }
                    }
                    recordSpyRegistration(
                        currentStep.spyRegistrationKey,
                        currentStep.spyRegistrationTeams,
                        currentStep.spyRegistrationDetail,
                    )
                    recordRecluseRegistration(
                        currentStep.recluseRegistrationKey,
                        currentStep.recluseRegistrationTeams,
                    )
                },
                onShowPlayerDisplay = showPlayerDisplay@{ displayStep ->
                    firstNightMigrationRequest(displayStep)?.let { request ->
                        val shadow = firstNightInformationMigration.shadow(request)
                        firstNightPoolParity.recordResult(
                            familyId = request.family.name.lowercase(),
                            matches = shadow is FirstNightShadowResult.Ready,
                        )
                        val prepared = firstNightInformationMigration.publishIfShadowMatches(request)
                        // Re-entering a completed night step must not create a second information
                        // event or replace the statement that the player already received.
                        if (prepared.isDisplayed(request.decisionId)) return@showPlayerDisplay
                        // A parity failure must retain the legacy UI/event path. It is never
                        // allowed to call display() on an unpublished migrated draft.
                        if (shadow is FirstNightShadowResult.Ready) {
                            firstNightInformationMigration = prepared.display(request.decisionId, request.selectedCandidateId)
                        }
                    }
                    recordReliablePrivateInformation(displayStep)
                    val actor = displayStep.actor
                    val unreliable = actor?.clocktowerRole?.enName == "Drunk" || actor?.name == poisonTarget
                    val primary = displayStep.displayPrimary ?: displayStep.tellPlayer
                    val secondary = displayStep.displaySecondary
                    val recordDetail = when (displayStep.displayKind) {
                        ClocktowerDisplayKind.EitherOne ->
                            if (primary != null && secondary != null)
                                text("$primary 在 ${secondary.trim().replace("   ", " / ")} 号之中", "$primary: seats ${secondary.trim().replace("   ", " / ")}")
                            else primary.orEmpty()
                        ClocktowerDisplayKind.Number ->
                            if (primary != null)
                                text("${displayStep.displayFooter.orEmpty()}：$primary", "${displayStep.displayFooter.orEmpty()}: $primary")
                            else primary.orEmpty()
                        ClocktowerDisplayKind.YesNo ->
                            if (secondary != null && primary != null)
                                text("查验 ${secondary.trim().replace("   ", " + ")} 号：$primary", "Checked seats ${secondary.trim().replace("   ", " + ")}: $primary")
                            else primary.orEmpty()
                        ClocktowerDisplayKind.RoleReveal ->
                            primary.orEmpty()
                        ClocktowerDisplayKind.Grimoire ->
                            text("间谍查看了魔典", "Spy viewed the grimoire")
                        else ->
                            primary.orEmpty()
                    }
                    val referencedPlayerNames = DecisionHistoryRepository.extractSeatNumbers(
                        values = listOf(displayStep.displaySecondary, displayStep.displayFooter),
                        maximumSeat = cards.size,
                    ).mapNotNull { seat -> cards.getOrNull(seat - 1)?.name }
                    onRecordEvent(
                        if (unreliable) ClocktowerEventType.UnreliableInformation else ClocktowerEventType.Information,
                        if (unreliable) {
                            if (displayStep.selectedInformationTruthful == false) {
                                text("${displayStep.displayTitle}（误导）", "${displayStep.displayTitle} (misleading)")
                            } else {
                                text("${displayStep.displayTitle}（不可靠）", "${displayStep.displayTitle} (unreliable)")
                            }
                        } else {
                            displayStep.displayTitle
                        },
                        recordDetail,
                        (listOfNotNull(actor?.name) + referencedPlayerNames).distinct(),
                    )
                    playerDisplayStep = displayStep
                },
                canGoPrevious = currentStepIndex > 0,
                onPrevious = {
                    if (currentStepIndex > 0) {
                        nightStepIndex = currentStepIndex - 1
                    }
                },
                onNext = advanceNightStep,
                showNavigationActions = false,
            )
        }
        return
    }

    ClocktowerDarkTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.clocktower_judge_assistant), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(gameOutcome?.title ?: phaseTitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) {
            if (nightStarted) {
                val currentStepIndex = nightStepIndex.coerceIn(0, nightSteps.lastIndex)
                val currentStep = nightSteps[currentStepIndex]
                item {
                    HostProgressCard(
                        title = if (phase == ClocktowerPhase.FirstNight) text("第 1 夜", "Night 1") else text("第 $round 夜", "Night $round"),
                        subtitle = text("当前阶段：${currentStep.title}", "Current: ${currentStep.title}"),
                        progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
                    )
                }
                item {
                    ClocktowerNightStepCardLocalized(
                        recommendationCoordinator = recommendationCoordinator,
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        automaticStorytellerStyle = automaticStorytellerStyle,
                        phase = phase,
                        debugDiagnosticsExpanded = debugDiagnosticsExpanded,
                        selectionDistributionTelemetry = selectionDistributionTelemetry,
                        evilAdvantage = currentDynamicStorytellerState.evilAdvantage,
                        informationDecisionKey = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}",
                        cards = cards,
                        aliveCards = aliveCards,
                        step = currentStep,
                        spyCard = spyCard,
                        spyRegistrationGood = spyRegistersGood(currentStep.spyRegistrationKey),
                        spyRegisteredRoleEnName = currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] },
                        spyRegistrationRecommendations = registrationRecommendationOptions(currentStep, spyCard, isSpy = true),
                        spyCanRegister = spyCanRegister(),
                        onSpyRegistrationGoodChange = { good ->
                            currentStep.spyRegistrationKey?.let { key ->
                                spyRegistrationGood[key] = good
                                if (good && currentStep.spyRegistrationDetail == ClocktowerRegistrationDetail.Role && spyRegistrationRole[key] == null) {
                                    spyRegistrationRole[key] = completeTroubleBrewingRoles.firstOrNull { it.team in currentStep.spyRegistrationTeams && it.enName != "Spy" }?.enName.orEmpty()
                                }
                                if (!good && redHerring == spyCard?.name && currentStep.action == ClocktowerNightAction.RedHerring) onSelectRedHerring(null)
                            }
                        },
                        onSpyRegistrationRoleChange = { roleName -> currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] = roleName } },
                        recluseCard = recluseCard,
                        recluseRegistrationEvil = recluseRegistersEvil(currentStep.recluseRegistrationKey),
                        recluseRegisteredRoleEnName = currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] },
                        recluseRegistrationRecommendations = registrationRecommendationOptions(currentStep, recluseCard, isSpy = false),
                        recluseCanRegister = recluseCanRegister(),
                        onRecluseRegistrationEvilChange = { evil ->
                            currentStep.recluseRegistrationKey?.let { key ->
                                recluseRegistrationEvil[key] = evil
                                if (evil && currentStep.recluseRegistrationTeams.isNotEmpty() && recluseRegistrationRole[key] == null) {
                                    recluseRegistrationRole[key] = completeTroubleBrewingRoles
                                        .firstOrNull { it.team in currentStep.recluseRegistrationTeams }
                                        ?.enName
                                        .orEmpty()
                                }
                            }
                        },
                        onRecluseRegistrationRoleChange = { roleName ->
                            currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] = roleName }
                        },
                        selectedName = when (currentStep.action) {
                            ClocktowerNightAction.RedHerring -> redHerring
                            ClocktowerNightAction.Poison -> poisonTarget
                            ClocktowerNightAction.ButlerMaster -> butlerMaster
                            ClocktowerNightAction.MonkProtect -> monkProtectedTarget
                            ClocktowerNightAction.DemonKill -> demonAttackDraftTarget
                            ClocktowerNightAction.MayorRedirect -> mayorRedirectDraftTarget
                            ClocktowerNightAction.DemonSuccessor -> demonSuccessorTarget
                            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
                            else -> null
                        },
                        fortuneTellerFirst = fortuneTellerFirst,
                        fortuneTellerSecond = fortuneTellerSecond,
                        chambermaidFirst = chambermaidFirst,
                        chambermaidSecond = chambermaidSecond,
                        onSelectName = { name ->
                            when (currentStep.action) {
                                ClocktowerNightAction.RedHerring -> onSelectRedHerring(if (redHerring == name) null else name)
                                ClocktowerNightAction.Poison -> onSelectPoisonTarget(if (poisonTarget == name) null else name)
                                ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(if (butlerMaster == name) null else name)
                                ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(if (monkProtectedTarget == name) null else name)
                                ClocktowerNightAction.DemonKill -> onSelectNightDeath(if (demonAttackDraftTarget == name) null else name)
                                ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(if (mayorRedirectDraftTarget == name) null else name)
                                ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(if (demonSuccessorTarget == name) null else name)
                                ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(if (ravenkeeperTarget == name) null else name)
                                else -> Unit
                            }
                        },
                        onSelectFortuneTellerFirst = { onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it) },
                        onSelectFortuneTellerSecond = { onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it) },
                        onSelectChambermaidFirst = { onSelectChambermaidFirst(if (chambermaidFirst == it) null else it) },
                        onSelectChambermaidSecond = { onSelectChambermaidSecond(if (chambermaidSecond == it) null else it) },
                        onApplyRecommendedDisplayOption = { option ->
                            currentStep.spyRegistrationKey?.let { key ->
                                option.spyRegistersGood?.let { good ->
                                    spyRegistrationGood[key] = good
                                    if (good) {
                                        option.spyRegisteredRoleEnName?.let { spyRegistrationRole[key] = it }
                                    } else {
                                        spyRegistrationRole.remove(key)
                                    }
                                }
                            }
                            currentStep.recluseRegistrationKey?.let { key ->
                                option.recluseRegistersEvil?.let { evil ->
                                    recluseRegistrationEvil[key] = evil
                                    if (evil) {
                                        option.recluseRegisteredRoleEnName?.let { recluseRegistrationRole[key] = it }
                                    } else {
                                        recluseRegistrationRole.remove(key)
                                    }
                                }
                            }
                            recordSpyRegistration(
                                currentStep.spyRegistrationKey,
                                currentStep.spyRegistrationTeams,
                                currentStep.spyRegistrationDetail,
                            )
                            recordRecluseRegistration(
                                currentStep.recluseRegistrationKey,
                                currentStep.recluseRegistrationTeams,
                            )
                        },
                        onShowPlayerDisplay = { displayStep ->
                            recordReliablePrivateInformation(displayStep)
                            val actor = displayStep.actor
                            val unreliable = actor?.clocktowerRole?.enName == "Drunk" || actor?.name == poisonTarget
                            val shownInformation = listOfNotNull(
                                displayStep.displayPrimary ?: displayStep.tellPlayer,
                                displayStep.displaySecondary,
                                displayStep.displayFooter,
                            ).filter { it.isNotBlank() }.joinToString(" · ")
                            val referencedPlayerNames = DecisionHistoryRepository.extractSeatNumbers(
                                values = listOf(
                                displayStep.displaySecondary,
                                displayStep.displayFooter,
                                ),
                                maximumSeat = cards.size,
                            ).mapNotNull { seat -> cards.getOrNull(seat - 1)?.name }
                            onRecordEvent(
                                if (unreliable) ClocktowerEventType.UnreliableInformation else ClocktowerEventType.Information,
                                if (unreliable) {
                                    text("${displayStep.displayTitle}（不可靠）", "${displayStep.displayTitle} (unreliable)")
                                } else {
                                    displayStep.displayTitle
                                },
                                "${actor?.seatLabel(cards).orEmpty()}：$shownInformation",
                                (listOfNotNull(actor?.name) + referencedPlayerNames).distinct(),
                            )
                            playerDisplayStep = displayStep
                        },
                        canGoPrevious = currentStepIndex > 0,
                        onPrevious = {
                            if (currentStepIndex > 0) {
                                nightStepIndex = currentStepIndex - 1
                            }
                        },
                        onNext = {
                            recordSpyRegistration(
                                currentStep.spyRegistrationKey,
                                currentStep.spyRegistrationTeams,
                                currentStep.spyRegistrationDetail,
                            )
                            recordRecluseRegistration(currentStep.recluseRegistrationKey, currentStep.recluseRegistrationTeams)
                            recordNightStep(currentStep)
                            if (currentStepIndex < nightSteps.lastIndex) {
                                nightStepIndex = currentStepIndex + 1
                            } else {
                                onConfirmNight()
                            }
                        },
                    )
                }
            }
        } else if (phase == ClocktowerPhase.Dawn) {
            val deathText = pendingNightDeath?.let { playerSeatLabel(cards, it) } ?: text("无", "None")
            item {
                HostScriptCard(
                    title = text("天亮了", "Dawn"),
                    script = text("天亮了，所有人睁眼。", "Dawn. Everyone, open your eyes."),
                    action = if (pendingNightDeath == null) {
                        text("请宣布：昨晚没有人死亡。", "Announce: Nobody died last night.")
                    } else {
                        text("请宣布：昨晚，$deathText 死亡。", "Announce: $deathText died last night.")
                    },
                ) {
                    HostInstructionBlock(
                        label = text("昨晚死亡", "Last night's death"),
                        text = deathText,
                        backgroundColor = Color(0xFFFFFCF6),
                        textColor = Color(0xFF1F2925),
                    )
                    Button(
                        onClick = {
                            onSelectNightDeath(null)
                            onAdvanceFromFirstNight()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(text("进入白天", "Enter day"))
                    }
                }
            }
        } else {
            item {
                HostProgressCard(
                    title = text("第 $round 天 白天", "Day $round"),
                    subtitle = text("存活玩家：${aliveCards.size}，处决所需票数：$executionThreshold", "Alive: ${aliveCards.size}; votes required to execute: $executionThreshold"),
                    progress = when {
                        highestVoteName != null -> text("最高票：${playerSeatLabel(cards, highestVoteName)}，$highestVoteCount 票", "Highest vote: ${playerSeatLabel(cards, highestVoteName)}, $highestVoteCount")
                        highestVoteCount >= executionThreshold -> text("最高票：平票，$highestVoteCount 票（无人被处决）", "Highest vote: tied at $highestVoteCount; nobody is executed")
                        else -> text("最高票：无", "Highest vote: none")
                    },
                )
            }
            when (dayMode) {
                ClocktowerDayMode.Overview -> {
                    item {
                        HostScriptCard(
                            title = text("白天管理", "Day management"),
                            script = text("现在自由讨论。有人提名时，点击开始提名。", "Players may discuss freely. Start a nomination when someone nominates."),
                            action = text("管理提名、投票、处决。今天结束前会确认是否有人被处决。", "Manage nominations, votes, and execution. Confirm the day's outcome before ending the day."),
                        ) {
                            Button(
                                onClick = {
                                    nominatorName = null
                                    nomineeName = null
                                    dayMode = ClocktowerDayMode.Nomination
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("开始提名", "Start nomination"))
                            }
                            if (scriptHasSlayer) {
                                OutlinedButton(
                                    onClick = {
                                        slayerClaimantName = null
                                        slayerTargetName = null
                                        slayerRecluseRegistersDemon = false
                                        dayMode = ClocktowerDayMode.Slayer
                                    },
                                    enabled = gameOutcome == null && slayerClaimantCandidates.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(text("杀手行动", "Slayer action"))
                                }
                            }
                            if (scriptHasArtist) {
                                OutlinedButton(
                                    onClick = {
                                        onSelectArtistClaimant(null)
                                        dayMode = ClocktowerDayMode.Artist
                                    },
                                    enabled = gameOutcome == null && artistClaimantCandidates.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(text("艺术家提问", "Artist question"))
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    onSelectExecution(highestVoteName?.takeIf { highestVoteCount >= executionThreshold })
                                    dayMode = ClocktowerDayMode.EndConfirm
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("结束白天", "End day"))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Slayer -> {
                    item {
                        HostScriptCard(
                            title = text("杀手行动", "Slayer action"),
                            script = text("选择公开声称自己是杀手的玩家，再选择目标。", "Choose the player publicly claiming to be the Slayer, then choose a target."),
                            action = text("真实杀手首次使用时，真实恶魔会死亡；隐士也可由说书人裁定登记为恶魔并死亡。", "A real Slayer's first use kills the real Demon. The Storyteller may also register the Recluse as the Demon."),
                        ) {
                            if (slayerClaimantCandidates.isEmpty()) {
                                HostInstructionBlock(
                                    label = text("杀手", "Slayer"),
                                    text = text("所有存活玩家都已经声称过杀手行动，本局不再提供声称者。", "Every living player has already claimed a Slayer action."),
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                HostActionSection(
                                    title = text("选择声称者", "Choose claimant"),
                                    helper = text("已经声称过杀手行动的玩家不会再出现。", "Players who already claimed a Slayer action are excluded."),
                                ) {
                                    SelectablePlayerChips(
                                        cards = slayerClaimantCandidates,
                                        selectedName = slayerClaimantName,
                                        enabled = gameOutcome == null,
                                        allCards = cards,
                                        onSelect = {
                                            slayerClaimantName = if (slayerClaimantName == it) null else it
                                            if (slayerTargetName == it) slayerTargetName = null
                                        },
                                    )
                                }
                                HostActionSection(title = text("选择目标", "Choose target")) {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name != slayerClaimantName },
                                        selectedName = slayerTargetName,
                                        enabled = gameOutcome == null,
                                        allCards = cards,
                                        onSelect = {
                                            slayerTargetName = if (slayerTargetName == it) null else it
                                            slayerRecluseRegistersDemon = false
                                        },
                                    )
                                }
                                if (cards.firstOrNull { it.name == slayerTargetName }?.clocktowerRole?.enName == "Recluse") {
                                    val slayerRecluse = cards.first { it.name == slayerTargetName }
                                    RecluseRegistrationPanel(
                                        automaticStorytellerInfo = automaticStorytellerInfo,
                                        automaticStorytellerStyle = automaticStorytellerStyle,
                                        cards = cards,
                                        recluse = slayerRecluse,
                                        teams = listOf(ClocktowerTeam.Demon),
                                        registersEvil = slayerRecluseRegistersDemon,
                                        registeredRoleEnName = if (slayerRecluseRegistersDemon) "Imp" else null,
                                        recommendations = registrationRecommendationOptions(
                                            key = registrationKey("SlayerRecluse", slayerRecluse.name),
                                            roleEnName = "Slayer",
                                            teams = listOf(ClocktowerTeam.Demon),
                                            detail = ClocktowerRegistrationDetail.Role,
                                            subject = slayerRecluse,
                                            isSpy = false,
                                            outcomeMisinformationPressure = 4,
                                        ),
                                        enabled = poisonTarget != slayerTargetName,
                                        onRegistersEvilChange = { slayerRecluseRegistersDemon = it },
                                        onRoleChange = {},
                                    )
                                }
                                Button(
                                    onClick = {
                                        val claimantName = slayerClaimantName
                                        val targetName = slayerTargetName
                                        if (claimantName != null && targetName != null) {
                                            onSlayerShot(claimantName, targetName, slayerRecluseRegistersDemon)
                                            slayerClaimantName = null
                                            slayerTargetName = null
                                            slayerRecluseRegistersDemon = false
                                            dayMode = ClocktowerDayMode.Overview
                                        }
                                    },
                                    enabled = slayerClaimantName != null && slayerTargetName != null && gameOutcome == null,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(text("结算杀手行动", "Resolve Slayer action"))
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    slayerClaimantName = null
                                    slayerTargetName = null
                                    slayerRecluseRegistersDemon = false
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("返回白天", "Return to day"))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Artist -> {
                    item {
                        HostScriptCard(
                            title = text("艺术家提问", "Artist question"),
                            script = text("选择公开声称自己是艺术家的玩家。艺术家每局一次，可以私下问说书人一个是/否问题。", "Choose the player publicly claiming to be the Artist. Once per game, the Artist may privately ask the Storyteller a yes/no question."),
                            action = text("如果是真艺术家首次提问，请根据魔典回答是/否；如果是酒鬼或假声称，可以给不可靠回答。", "For a real Artist's first question, answer from the grimoire. Drunk or false claimants may receive unreliable information."),
                        ) {
                            HostActionSection(
                                title = text("选择提问者", "Choose claimant"),
                                helper = text("已经提问过或声称提问过的玩家不会再出现。", "Players who already asked or claimed a question are excluded."),
                            ) {
                                SelectablePlayerChips(
                                    cards = artistClaimantCandidates,
                                    selectedName = artistClaimantName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { onSelectArtistClaimant(if (artistClaimantName == it) null else it) },
                                )
                            }
                            val artistClaimant = cards.firstOrNull { it.name == artistClaimantName }
                            if (artistClaimant != null) {
                                HostActionSection(
                                    title = text("输入问题的真实答案", "Enter the truthful answer"),
                                    helper = text(
                                        "系统不知道玩家提出的问题语义，请说书人先根据魔典判断真实答案。",
                                        "The app cannot interpret the player's question. First judge its truthful answer from the Grimoire.",
                                    ),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(true, false).forEach { answer ->
                                            val label = if (answer) text("是", "Yes") else text("否", "No")
                                            if (artistTruthfulAnswer == answer) {
                                                Button(
                                                    onClick = { onSelectArtistTruthfulAnswer(answer) },
                                                    modifier = Modifier.weight(1f),
                                                ) { Text(label) }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { onSelectArtistTruthfulAnswer(answer) },
                                                    modifier = Modifier.weight(1f),
                                                ) { Text(label) }
                                            }
                                        }
                                    }
                                }
                            }
                            if (artistClaimant != null && artistTruthfulAnswer != null) {
                                val artistReliable =
                                    artistClaimant.clocktowerRole?.enName == "Artist" &&
                                        artistClaimant.name != poisonTarget
                                val answerRecommendations = if (artistReliable) {
                                    listOf(Triple(RecommendationStyle.BALANCED, artistTruthfulAnswer, false))
                                } else {
                                    recommendationCoordinator.recommendCategory(
                                        listOf(
                                            UnreliableCategoricalCandidate(
                                                id = "yes",
                                                isTruthful = artistTruthfulAnswer,
                                                misinformationPressure = if (artistTruthfulAnswer) 0 else 3,
                                            ),
                                            UnreliableCategoricalCandidate(
                                                id = "no",
                                                isTruthful = !artistTruthfulAnswer,
                                                misinformationPressure = if (artistTruthfulAnswer) 3 else 0,
                                            ),
                                        ),
                                    ).map { recommendation ->
                                        Triple(
                                            recommendation.style,
                                            recommendation.candidateId == "yes",
                                            recommendation.warningIds.isNotEmpty(),
                                        )
                                    }
                                }
                                val automaticArtistRecommendation = WeightedStableSelector.selectPreferred(answerRecommendations) {
                                    it.first == RecommendationStyle.BALANCED
                                }
                                val automaticArtistAnswer = automaticArtistRecommendation?.second
                                LaunchedEffect(automaticStorytellerInfo, artistClaimantName, artistTruthfulAnswer, automaticArtistAnswer) {
                                    if (automaticStorytellerInfo && automaticArtistAnswer != null && artistShownAnswer != automaticArtistAnswer) {
                                        onSelectArtistShownAnswer(automaticArtistAnswer)
                                    }
                                }
                                HostActionSection(
                                    title = text("推荐回答", "Recommended answer"),
                                    helper = if (automaticStorytellerInfo) {
                                        text("已自动采用平衡回答。", "The balanced answer has been applied automatically.")
                                    } else if (artistReliable) {
                                        text("能力可靠，必须回答真实结果。", "The ability is reliable; give the truthful result.")
                                    } else {
                                        text("能力不可靠，可以给出真实或错误答案。", "The ability is unreliable; either answer is legal.")
                                    },
                                ) {
                                    answerRecommendations
                                        .filter { !automaticStorytellerInfo || it == automaticArtistRecommendation }
                                        .forEach { (style, answer, warning) ->
                                        val answerLabel = if (answer) text("是", "Yes") else text("否", "No")
                                        val label = if (artistReliable) {
                                            text("规则结果 · $answerLabel", "Rules result · $answerLabel")
                                        } else {
                                            "${recommendationStyleLabel(style)} · $answerLabel${if (warning) text(" · 高影响", " · high impact") else ""}"
                                        }
                                        if (automaticStorytellerInfo) {
                                            Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        } else if (style == RecommendationStyle.BALANCED) {
                                            Button(
                                                onClick = { onSelectArtistShownAnswer(answer) },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) { Text(label) }
                                        } else {
                                            OutlinedButton(
                                                onClick = { onSelectArtistShownAnswer(answer) },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) { Text(label) }
                                        }
                                    }
                                    artistShownAnswer?.let { shown ->
                                        Text(
                                            text(
                                                "当前准备回答：${if (shown) "是" else "否"}",
                                                "Prepared answer: ${if (shown) "Yes" else "No"}",
                                            ),
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = onConfirmArtistQuestion,
                                enabled = artistClaimantName != null &&
                                    artistTruthfulAnswer != null &&
                                    artistShownAnswer != null &&
                                    gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("记录艺术家提问", "Record Artist question"))
                            }
                            OutlinedButton(
                                onClick = {
                                    onSelectArtistClaimant(null)
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("返回白天", "Return to day"))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Klutz -> {
                    item {
                        val klutzChoiceCard = cards.firstOrNull { it.name == klutzChoiceName }
                        val klutzRegistrationKey = klutzChoiceCard?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Klutz", it.name) }
                        HostScriptCard(
                            title = text("呆瓜选择", "Klutz choice"),
                            script = text("${playerSeatLabel(cards, pendingKlutzName)} 是呆瓜，得知自己死亡后必须公开选择一名存活玩家。", "${playerSeatLabel(cards, pendingKlutzName)} is the Klutz and must publicly choose a living player after learning of their death."),
                            action = text("如果他选择邪恶玩家，善良阵营失败；选择善良玩家则游戏继续。", "If the Klutz chooses an evil player, the good team loses; otherwise the game continues."),
                        ) {
                            HostActionSection(title = text("选择呆瓜公开指定的玩家", "Choose the player named by the Klutz")) {
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != pendingKlutzName },
                                    selectedName = klutzChoiceName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { onSelectKlutzChoice(if (klutzChoiceName == it) null else it) },
                                )
                            }
                            if (klutzRegistrationKey != null && spyCard != null) {
                                SpyRegistrationPanel(
                                    automaticStorytellerInfo = automaticStorytellerInfo,
                                    automaticStorytellerStyle = automaticStorytellerStyle,
                                    cards = cards,
                                    spy = spyCard,
                                    teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                    registersGood = spyRegistersGood(klutzRegistrationKey),
                                    registeredRoleEnName = spyRegistrationRole[klutzRegistrationKey],
                                    recommendations = registrationRecommendationOptions(
                                        key = klutzRegistrationKey,
                                        roleEnName = "Klutz",
                                        teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                        detail = ClocktowerRegistrationDetail.Role,
                                        subject = spyCard,
                                        isSpy = true,
                                        outcomeMisinformationPressure = 5,
                                    ),
                                    enabled = spyCanRegister(),
                                    onRegistersGoodChange = { good ->
                                        spyRegistrationGood[klutzRegistrationKey] = good
                                        if (good && spyRegistrationRole[klutzRegistrationKey] == null) spyRegistrationRole[klutzRegistrationKey] = "Washerwoman"
                                    },
                                    onRoleChange = { spyRegistrationRole[klutzRegistrationKey] = it },
                                )
                            }
                            Button(
                                onClick = {
                                    recordSpyRegistration(klutzRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))
                                    onConfirmKlutzChoice(spyRegistersGood(klutzRegistrationKey))
                                },
                                enabled = klutzChoiceName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("确认呆瓜选择", "Confirm Klutz choice"))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Nomination -> {
                    item {
                        val nominatorCard = cards.firstOrNull { it.name == nominatorName }
                        val nomineeCard = cards.firstOrNull { it.name == nomineeName }
                        val virginFirstNomination = nomineeCard?.clocktowerRole?.enName == "Virgin" && !virginUsed
                        val virginAbilityWorks = virginFirstNomination && poisonTarget != nomineeCard?.name
                        val virginRegistrationKey = nominatorCard?.takeIf { it.name == spyCard?.name && virginFirstNomination }?.let { registrationKey("Virgin", it.name) }
                        val virginExecutes = virginAbilityWorks && (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || spyRegistersGood(virginRegistrationKey))
                        HostScriptCard(
                            title = text("提名", "Nomination"),
                            script = if (nominatorName != null && nomineeName != null) {
                                text("请宣布：${playerSeatLabel(cards, nominatorName)} 提名 ${playerSeatLabel(cards, nomineeName)}。然后请提名人说明理由，再请被提名人辩护。", "Announce: ${playerSeatLabel(cards, nominatorName)} nominates ${playerSeatLabel(cards, nomineeName)}. Ask the nominator for their case, then let the nominee defend themself.")
                            } else {
                                text("选择提名人和被提名人。", "Choose the nominator and nominee.")
                            },
                            action = when {
                                virginExecutes -> text("这是圣女第一次被镇民提名。不要投票，直接处决提名者。", "This is the Virgin's first nomination by a Townsfolk. Skip voting and execute the nominator.")
                                virginFirstNomination -> text("这是圣女第一次被提名，但提名者不是真实镇民。圣女能力用过，继续正常投票。", "This is the Virgin's first nomination, but the nominator is not a real Townsfolk. Mark the ability spent and continue to voting.")
                                else -> text("两名玩家都选好后，进入投票。", "Choose both players to continue to voting.")
                            },
                        ) {
                            HostActionSection(title = text("选择提名人", "Choose nominator")) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = nominatorName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { nominatorName = if (nominatorName == it) null else it },
                                )
                            }
                            HostActionSection(title = text("选择被提名人", "Choose nominee")) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = nomineeName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { nomineeName = if (nomineeName == it) null else it },
                                )
                            }
                            if (virginRegistrationKey != null && spyCard != null) {
                                SpyRegistrationPanel(
                                    automaticStorytellerInfo = automaticStorytellerInfo,
                                    automaticStorytellerStyle = automaticStorytellerStyle,
                                    cards = cards,
                                    spy = spyCard,
                                    teams = listOf(ClocktowerTeam.Townsfolk),
                                    registersGood = spyRegistersGood(virginRegistrationKey),
                                    registeredRoleEnName = spyRegistrationRole[virginRegistrationKey],
                                    recommendations = registrationRecommendationOptions(
                                        key = virginRegistrationKey,
                                        roleEnName = "Virgin",
                                        teams = listOf(ClocktowerTeam.Townsfolk),
                                        detail = ClocktowerRegistrationDetail.Role,
                                        subject = spyCard,
                                        isSpy = true,
                                        outcomeMisinformationPressure = 5,
                                    ),
                                    enabled = spyCanRegister(),
                                    onRegistersGoodChange = { good ->
                                        spyRegistrationGood[virginRegistrationKey] = good
                                        if (good && spyRegistrationRole[virginRegistrationKey] == null) spyRegistrationRole[virginRegistrationKey] = "Washerwoman"
                                    },
                                    onRoleChange = { spyRegistrationRole[virginRegistrationKey] = it },
                                )
                            }
                            if (virginFirstNomination) {
                                HostInstructionBlock(
                                    label = text("圣女能力", "Virgin ability"),
                                    text = if (virginExecutes) {
                                        text("${playerSeatLabel(cards, nomineeName)} 第一次被真实镇民提名。${playerSeatLabel(cards, nominatorName)} 立即被处决，本次提名不进入投票，白天结束。", "${playerSeatLabel(cards, nomineeName)} was first nominated by a real Townsfolk. ${playerSeatLabel(cards, nominatorName)} is executed immediately; skip voting and end the day.")
                                    } else {
                                        text("${playerSeatLabel(cards, nomineeName)} 第一次被提名，但 ${playerSeatLabel(cards, nominatorName)} 不是真实镇民。不要处决提名者；记录圣女能力已用过，然后继续投票。", "${playerSeatLabel(cards, nomineeName)} was nominated for the first time, but ${playerSeatLabel(cards, nominatorName)} is not a real Townsfolk. Do not execute the nominator; mark the ability spent and continue to voting.")
                                    },
                                    backgroundColor = if (virginExecutes) Color(0xFFFFF4DC) else Color(0xFFFFFCF6),
                                    textColor = if (virginExecutes) Color(0xFF9A4B36) else Color(0xFF5C6A63),
                                )
                            }
                            Button(
                                onClick = {
                                    val chosenNominator = nominatorName
                                    val chosenNominee = nomineeName
                                    if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {
                                        recordSpyRegistration(virginRegistrationKey, listOf(ClocktowerTeam.Townsfolk))
                                        onVirginNomination(chosenNominator, chosenNominee, virginExecutes)
                                    }
                                    if (chosenNominator != null && chosenNominee != null) {
                                        if (virginExecutes) {
                                            onRecordEvent(
                                                ClocktowerEventType.Nomination,
                                                text("提名", "Nomination"),
                                                "${playerSeatLabel(cards, chosenNominator)} → ${playerSeatLabel(cards, chosenNominee)}",
                                                listOf(chosenNominator, chosenNominee),
                                            )
                                        }
                                    }
                                    if (!virginExecutes) {
                                        currentVoteCount = executionThreshold
                                        dayMode = ClocktowerDayMode.Vote
                                    }
                                },
                                enabled = nominatorName != null && nomineeName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    when {
                                        virginExecutes -> text("处决提名者", "Execute nominator")
                                        virginFirstNomination -> text("记录圣女已用过，开始投票", "Mark Virgin spent and begin vote")
                                        else -> text("开始投票", "Begin vote")
                                    },
                                )
                            }
                        }
                    }
                }

                ClocktowerDayMode.Vote -> {
                    item {
                        HostScriptCard(
                            title = text("投票", "Vote"),
                            script = text("正在投票：是否处决 ${playerSeatLabel(cards, nomineeName)}。", "Vote on whether to execute ${playerSeatLabel(cards, nomineeName)}."),
                            action = text("输入票数。达到 $executionThreshold 票才可能成为今天处决目标。", "Enter the vote count. At least $executionThreshold votes are required to become today's execution target."),
                        ) {
                            StepperRow(
                                label = text("票数", "Votes"),
                                value = currentVoteCount,
                                range = 0..aliveCards.size,
                                onChange = { currentVoteCount = it },
                            )
                            val reached = currentVoteCount >= executionThreshold
                            HostInstructionBlock(
                                label = text("结果", "Result"),
                                text = if (reached) {
                                    text("${playerSeatLabel(cards, nomineeName)} 获得 $currentVoteCount 票，达到处决门槛。", "${playerSeatLabel(cards, nomineeName)} received $currentVoteCount votes and reached the execution threshold.")
                                } else {
                                    text("${playerSeatLabel(cards, nomineeName)} 获得 $currentVoteCount 票，未达到处决门槛。", "${playerSeatLabel(cards, nomineeName)} received $currentVoteCount votes and did not reach the execution threshold.")
                                },
                                backgroundColor = if (reached) Color(0xFFEAF2EA) else Color(0xFFFFFCF6),
                                textColor = if (reached) Color(0xFF2F5D50) else Color(0xFF6F7B74),
                            )
                            Button(
                                onClick = {
                                    onRecordEvent(
                                        ClocktowerEventType.Vote,
                                        text("提名与投票", "Nomination and vote"),
                                        "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                                        listOfNotNull(nominatorName, nomineeName),
                                    )
                                    recordCurrentVote()
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                enabled = nomineeName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("继续提名", "Continue nominations"))
                            }
                            OutlinedButton(
                                onClick = {
                                    onRecordEvent(
                                        ClocktowerEventType.Vote,
                                        text("提名与投票", "Nomination and vote"),
                                        "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                                        listOfNotNull(nominatorName, nomineeName),
                                    )
                                    onSelectExecution(recordCurrentVote())
                                    dayMode = ClocktowerDayMode.EndConfirm
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("结束白天", "End day"))
                            }
                        }
                    }
                }

                ClocktowerDayMode.EndConfirm -> {
                    item {
                        val target = selectedExecution
                        HostScriptCard(
                            title = text("准备结束白天", "End-day confirmation"),
                            script = target?.let { text("当前将被处决：${playerSeatLabel(cards, it)}，票数：$highestVoteCount。", "Current execution target: ${playerSeatLabel(cards, it)} with $highestVoteCount votes.") } ?: text("今天没有玩家被处决。", "Nobody will be executed today."),
                            action = target?.let { text("确认处决 ${playerSeatLabel(cards, it)} 吗？", "Execute ${playerSeatLabel(cards, it)}?") } ?: text("确认进入夜晚吗？", "Continue to night?"),
                        ) {
                            Button(
                                onClick = onConfirmDay,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(target?.let { text("确认处决", "Confirm execution") } ?: text("进入夜晚", "Enter night"))
                            }
                            OutlinedButton(
                                onClick = { dayMode = ClocktowerDayMode.Overview },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text("返回白天", "Return to day"))
                            }
                        }
                    }
                }

                ClocktowerDayMode.ExecutionResult -> Unit
            }
        }

        item {
            ClocktowerGameRecordPanel(
                cards = cards,
                events = events,
                language = language,
            )
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
                Text(if (gameOutcome == null) stringResource(R.string.end_and_reveal) else stringResource(R.string.view_results))
            }
        }
    }
    }

    return

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.clocktower_judge_assistant), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(gameOutcome?.title ?: phaseTitle, color = Color(0xFF5C6A63))
                }
            }
        }

        item {
            HostProgressCard(
                title = stringResource(R.string.host_current_stage),
                subtitle = phaseTitle,
                progress = phaseProgress,
            )
        }

        item {
            HostScriptCard(
                title = phaseTitle,
                script = phaseScript,
                action = phaseAction,
            ) {
                Text(stringResource(R.string.clocktower_storyteller_hint_body), color = Color(0xFF6F7B74))
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

        when (phase) {
            ClocktowerPhase.FirstNight -> {
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_red_herring_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.clocktower_red_herring_hint), color = Color(0xFF6F7B74))
                            SelectablePlayerChips(
                                cards = clocktowerRedHerringCandidates(aliveCards),
                                selectedName = redHerring,
                                onSelect = { onSelectRedHerring(if (redHerring == it) null else it) },
                                enabled = gameOutcome == null,
                            )
                        }
                    }
                }
                firstNightWasherwoman?.let { washerwoman ->
                    val target = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Townsfolk && it.name != washerwoman.name }
                    val pair = target?.let { storytellerPairHint(it, cards) }
                    if (pair != null && target.clocktowerRole != null) {
                        item {
                            ClocktowerInfoCard(
                                title = stringResource(R.string.clocktower_role_info_format, washerwoman.name, washerwoman.hostRoleLabel(context, GameKind.Clocktower)),
                                body = stringResource(
                                    R.string.clocktower_washerwoman_info,
                                    target.clocktowerRole!!.nameFor(language),
                                    pair.first.name,
                                    pair.second.name,
                                ),
                            )
                        }
                    }
                }
                firstNightLibrarian?.let { librarian ->
                    val outsider = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Outsider }
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, librarian.name, librarian.hostRoleLabel(context, GameKind.Clocktower)),
                            body = if (outsider?.clocktowerRole != null) {
                                val pair = storytellerPairHint(outsider, cards)
                                if (pair != null) {
                                    stringResource(
                                        R.string.clocktower_librarian_info,
                                        outsider.clocktowerRole!!.nameFor(language),
                                        pair.first.name,
                                        pair.second.name,
                                    )
                                } else {
                                    stringResource(R.string.clocktower_no_info)
                                }
                            } else {
                                stringResource(R.string.clocktower_librarian_none)
                            },
                        )
                    }
                }
                firstNightInvestigator?.let { investigator ->
                    val minion = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion }
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, investigator.name, investigator.hostRoleLabel(context, GameKind.Clocktower)),
                            body = if (minion?.clocktowerRole != null) {
                                val pair = storytellerPairHint(minion, cards)
                                if (pair != null) {
                                    stringResource(
                                        R.string.clocktower_investigator_info,
                                        minion.clocktowerRole!!.nameFor(language),
                                        pair.first.name,
                                        pair.second.name,
                                    )
                                } else {
                                    stringResource(R.string.clocktower_no_info)
                                }
                            } else {
                                stringResource(R.string.clocktower_investigator_none)
                            },
                        )
                    }
                }
                chefPlayer?.let { chef ->
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, chef.name, chef.hostRoleLabel(context, GameKind.Clocktower)),
                            body = stringResource(R.string.clocktower_chef_info, chefEvilPairs(cards)),
                        )
                    }
                }
                if (actualClocktowerRoleCards(cards, "Spy").isNotEmpty() || actualClocktowerRoleCards(cards, "Baron").isNotEmpty()) {
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_special_reminders_title),
                            body = buildList {
                                if (actualClocktowerRoleCards(cards, "Spy").isNotEmpty()) {
                                    add(stringResource(R.string.clocktower_spy_hint))
                                }
                                if (actualClocktowerRoleCards(cards, "Baron").isNotEmpty()) {
                                    add(stringResource(R.string.clocktower_baron_hint))
                                }
                            }.joinToString("\n"),
                        )
                    }
                }
                item {
                    Button(
                        onClick = onAdvanceFromFirstNight,
                        enabled = gameOutcome == null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_start_day_one))
                    }
                }
            }

            ClocktowerPhase.Dawn -> Unit

            ClocktowerPhase.Day -> {
                if (butlerPlayers.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_butler_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.clocktower_butler_hint), color = Color(0xFF6F7B74))
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name !in butlerPlayers.map(PlayerCard::name) },
                                    selectedName = butlerMaster,
                                    onSelect = { onSelectButlerMaster(if (butlerMaster == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_execution_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.clocktower_execution_hint), color = Color(0xFF6F7B74))
                            SelectablePlayerChips(
                                cards = aliveCards,
                                selectedName = selectedExecution,
                                onSelect = { onSelectExecution(if (selectedExecution == it) null else it) },
                                enabled = gameOutcome == null,
                            )
                            Button(
                                onClick = onConfirmDay,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    if (selectedExecution == null) {
                                        stringResource(R.string.clocktower_no_execution)
                                    } else {
                                        stringResource(R.string.clocktower_confirm_execution)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            ClocktowerPhase.Night -> {
                if (poisonerPlayers.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_poisoner_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.clocktower_poisoner_hint), color = Color(0xFF6F7B74))
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = poisonTarget,
                                    onSelect = { onSelectPoisonTarget(if (poisonTarget == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                        }
                    }
                }
                empathPlayers.forEach { empath ->
                    val neighbors = livingNeighbors(cards, empath.name)
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, empath.name, empath.hostRoleLabel(context, GameKind.Clocktower)),
                            body = stringResource(
                                R.string.clocktower_empath_info,
                                neighbors.joinToString(stringResource(R.string.name_separator)) { it.name },
                                empathEvilNeighborCount(cards, empath.name),
                            ),
                        )
                    }
                }
                fortuneTellerPlayers.forEach { fortuneTeller ->
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    stringResource(R.string.clocktower_role_info_format, fortuneTeller.name, fortuneTeller.hostRoleLabel(context, GameKind.Clocktower)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(stringResource(R.string.clocktower_fortune_teller_hint), color = Color(0xFF6F7B74))
                                Text(stringResource(R.string.clocktower_choose_first_target), fontWeight = FontWeight.SemiBold)
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = fortuneTellerFirst,
                                    onSelect = { onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                                Text(stringResource(R.string.clocktower_choose_second_target), fontWeight = FontWeight.SemiBold)
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != fortuneTellerFirst },
                                    selectedName = fortuneTellerSecond,
                                    onSelect = { onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                                fortuneTellerResult?.let { result ->
                                    Text(
                                        stringResource(
                                            R.string.clocktower_fortune_teller_result,
                                            fortuneTellerFirst ?: "",
                                            fortuneTellerSecond ?: "",
                                            result,
                                        ),
                                        color = Color(0xFF2F5D50),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_demon_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.clocktower_demon_hint), color = Color(0xFF6F7B74))
                            SelectablePlayerChips(
                                cards = aliveCards,
                                selectedName = pendingNightDeath,
                                onSelect = { onSelectNightDeath(if (pendingNightDeath == it) null else it) },
                                enabled = gameOutcome == null,
                            )
                            ravenkeeperTrigger?.let {
                                Text(stringResource(R.string.clocktower_ravenkeeper_hint), fontWeight = FontWeight.SemiBold)
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { card -> card.name != ravenkeeperTrigger.name },
                                    selectedName = ravenkeeperTarget,
                                    onSelect = { onSelectRavenkeeperTarget(if (ravenkeeperTarget == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                                ravenkeeperTarget?.let { targetName ->
                                    val target = cards.firstOrNull { it.name == targetName }
                                    if (target != null) {
                                        Text(
                                            stringResource(
                                                R.string.clocktower_ravenkeeper_result,
                                                target.name,
                                                target.hostRoleLabel(context, GameKind.Clocktower),
                                            ),
                                            color = Color(0xFF2F5D50),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = onConfirmNight,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_confirm_night))
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        when (phase) {
                            ClocktowerPhase.FirstNight -> Unit
                            ClocktowerPhase.Dawn -> onPhaseChange(ClocktowerPhase.Night)
                            ClocktowerPhase.Day -> onPhaseChange(ClocktowerPhase.FirstNight)
                            ClocktowerPhase.Night -> onPhaseChange(ClocktowerPhase.Day)
                        }
                    },
                    enabled = phase != ClocktowerPhase.FirstNight,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.previous_step))
                }
                Button(
                    onClick = {
                        when (phase) {
                            ClocktowerPhase.FirstNight -> onAdvanceFromFirstNight()
                            ClocktowerPhase.Dawn -> onAdvanceFromFirstNight()
                            ClocktowerPhase.Day -> onPhaseChange(ClocktowerPhase.Night)
                            ClocktowerPhase.Night -> onPhaseChange(ClocktowerPhase.Day)
                        }
                    },
                    enabled = gameOutcome == null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.next_step))
                }
            }
        }

        if (BuildConfig.DEBUG) {
            item {
                OutlinedButton(
                    onClick = { debugDiagnosticsExpanded = !debugDiagnosticsExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (debugDiagnosticsExpanded) "Hide developer diagnostics" else "Show developer diagnostics") }
            }
        }
        if (BuildConfig.DEBUG && debugDiagnosticsExpanded) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("A4 device diagnostic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Debug only. Uses the live structural snapshot and synthetic native/fallback probes; it does not replay the full information history.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(
                            onClick = { a4DeviceBenchmarkRuns += 1 },
                            enabled = a4DiagnosticAvailable,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Run A4 benchmark (11 samples)") }
                        OutlinedButton(
                            onClick = { a4PrewarmCancellationProbeRuns += 1 },
                            enabled = a4DiagnosticAvailable,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Run A4 prewarm cancellation probe") }
                        if (!a4DiagnosticAvailable) {
                            Text(
                                "Available only for a 5-player Trouble Brewing game with complete role data. Larger exact enumeration can exhaust device memory before ZDD compression.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        a4DeviceBenchmarkReport?.let { report ->
                            Text(report.toLogLine(), style = MaterialTheme.typography.bodySmall)
                        }
                        a4DeviceBenchmarkError?.let { error ->
                            Text("Benchmark failed: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        a4PrewarmCancellationProbeResult?.let { result ->
                            Text(result, style = MaterialTheme.typography.bodySmall)
                        }
                        a4PrewarmCancellationProbeError?.let { error ->
                            Text("Prewarm cancellation probe failed: $error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.player_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(cards) { card ->
            ClocktowerPlayerStatusRow(card)
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.elimination_records), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text(stringResource(R.string.no_eliminations), color = Color(0xFF6F7B74))
            }
        }

        items(records) { record ->
            Text(record.displayText(), modifier = Modifier.padding(vertical = 4.dp))
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
                Text(if (gameOutcome == null) stringResource(R.string.end_and_reveal) else stringResource(R.string.view_results))
            }
        }
    }
}

@Composable
private fun EvilInfoDisplay(
    primary: String,
    secondary: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        listOfNotNull(primary, secondary?.takeIf { it.isNotBlank() }).forEach { section ->
            val lines = section.lines()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B3833), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    lines.firstOrNull().orEmpty(),
                    color = Color(0xFFAFC7BC),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    lines.drop(1).joinToString("\n"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerStorytellerRecommendationScreen(
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
private fun StorytellerRecommendationCard(
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
private fun RecommendationDecisionEditor(
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

@Composable
private fun ClocktowerPlayerDisplayCardLocalized(
    step: ClocktowerNightStepUi,
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0D10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                step.displayTitle,
                color = Color(0xFFF1EADC),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                when (step.displayKind) {
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                        Text(
                            primary,
                            color = Color(0xFFC5A56A),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        secondary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                color = Color(0xFFF7F1E6),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            footer,
                            color = Color(0xFFF1EADC),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EitherOne -> {
                        Text(
                            primary,
                            color = Color(0xFFF7F1E6),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFC5A56A),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

                    ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                        Text(
                            primary,
                            color = Color(0xFFF7F1E6),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.Grimoire -> {
                        val lines = primary.lines().filter { it.isNotBlank() }
                        val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            lines.forEach { line ->
                                val parts = line.split(Regex("[：:]"), limit = 2)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1B1F25), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        parts.firstOrNull().orEmpty(),
                                        color = Color(0xFFF1EADC),
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.35f),
                                    )
                                    Text(
                                        parts.getOrNull(1).orEmpty(),
                                        color = Color(0xFFC5A56A),
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.85f),
                                    )
                                }
                            }
                        }
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.None -> Unit
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC5A56A)),
            ) {
                Text(stringResource(R.string.clocktower_display_close))
            }
        }
    }
}

@Composable
private fun ClocktowerPlayerDisplayCard(
    step: ClocktowerNightStepUi,
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1F2925),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                step.displayTitle,
                color = Color(0xFFEAF2EA),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                when (step.displayKind) {
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                        Text(
                            primary,
                            color = Color(0xFFFFF4DC),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            footer,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EitherOne -> {
                        Text(
                            primary,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFFFF4DC),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            footer,
                            color = Color(0xFFEAF2EA),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

                    ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                        Text(
                            primary,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFEAF2EA),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.Grimoire -> {
                        val lines = primary.lines().filter { it.isNotBlank() }
                        val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            lines.forEach { line ->
                                val parts = line.split("：", limit = 2)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2B3833), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        parts.firstOrNull().orEmpty(),
                                        color = Color.White,
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.35f),
                                    )
                                    Text(
                                        parts.getOrNull(1).orEmpty(),
                                        color = Color(0xFFFFF4DC),
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.85f),
                                    )
                                }
                            }
                        }
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFEAF2EA),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.None -> Unit
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.clocktower_display_close))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpyRegistrationPanel(
    cards: List<PlayerCard>,
    spy: PlayerCard,
    teams: List<ClocktowerTeam>,
    registersGood: Boolean,
    registeredRoleEnName: String?,
    detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    hint: String? = null,
    recommendations: List<ClocktowerRegistrationRecommendationOption> = emptyList(),
    automaticStorytellerInfo: Boolean = false,
    automaticStorytellerStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    selectionAudit: SelectionAuditContext? = null,
    enabled: Boolean,
    onRegistersGoodChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val roles = completeTroubleBrewingRoles.filter { it.team in teams && it.enName != "Spy" }
    val registrationPool = unifiedRegistrationPool(recommendations)
    val assistedRecommendations = registrationPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: recommendations
    val automaticRecommendation = WeightedStableSelector.selectStyle(
        registrationPool?.candidatesFor(SelectionExecutionPolicy.AUTO)?.map { it.payload }.orEmpty(),
        automaticStorytellerStyle,
        ClocktowerRegistrationRecommendationOption::style,
    )
    val automaticStyleLabel = when (automaticStorytellerStyle) {
        RecommendationStyle.GENTLE -> if (language == "en") "gentle" else "稳健"
        RecommendationStyle.BALANCED -> if (language == "en") "balanced" else "均衡"
        RecommendationStyle.AGGRESSIVE -> if (language == "en") "aggressive" else "激进"
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRecommendation, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRecommendation != null) {
            selectionAudit?.let { audit ->
                audit.recorder.recordPreview(
                    SelectionAuditRecord(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        candidates = recommendations.map { option ->
                            SelectionAuditCandidate(
                                familyId = if (option.usesSpecialRegistration) "special-registration" else "actual-registration",
                                qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
                            )
                        },
                    ),
                )
                audit.recorder.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        selectedFamilyId = if (automaticRecommendation.usesSpecialRegistration) "special-registration" else "actual-registration",
                    ),
                )
            }
            onRegistersGoodChange(automaticRecommendation.usesSpecialRegistration)
            if (automaticRecommendation.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                automaticRecommendation.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                },
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (language == "en") "Private Storyteller ruling" else "说书人私密裁定", fontWeight = FontWeight.Black)
        Text(
            "${spy.seatLabel(cards)} · ${if (language == "en") "this interaction only" else "仅影响本次交互"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        hint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        if (!enabled) {
            Text(
                if (language == "en") "The Spy is poisoned; registration cannot change." else "间谍已中毒，本次不能改变登记身份。",
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            if (automaticStorytellerInfo && automaticRecommendation != null) {
                Text(
                    if (language == "en") "Automatic $automaticStyleLabel ruling" else "已自动采用${automaticStyleLabel}裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(automaticRecommendation.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RecommendationReasonSummary(
                    automaticRecommendation.reasonCodes,
                    automaticRecommendation.warningCodes,
                    language,
                )
            } else if (assistedRecommendations.isNotEmpty()) {
                Text(
                    if (language == "en") "Recommended ruling" else "推荐裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                assistedRecommendations.forEach { recommendation ->
                    val apply = {
                        onRegistersGoodChange(recommendation.usesSpecialRegistration)
                        if (recommendation.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                            recommendation.registeredRoleEnName?.let(onRoleChange)
                        }
                    }
                    if (recommendation.isDefaultRecommendation) {
                        Button(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    } else {
                        OutlinedButton(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    }
                    RecommendationReasonSummary(recommendation.reasonCodes, recommendation.warningCodes, language)
                }
                Text(
                    if (language == "en") "Or choose manually" else "或手动裁定",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!automaticStorytellerInfo) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (registersGood) {
                    OutlinedButton(onClick = { onRegistersGoodChange(false) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Actual" else "真实身份")
                    }
                    Button(onClick = { onRegistersGoodChange(true) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Register good" else "登记善良")
                    }
                } else {
                    Button(onClick = { onRegistersGoodChange(false) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Actual" else "真实身份")
                    }
                    OutlinedButton(onClick = { onRegistersGoodChange(true) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Register good" else "登记善良")
                    }
                }
            }
            if (registersGood && detail == ClocktowerRegistrationDetail.Role && roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { role ->
                        if (registeredRoleEnName == role.enName) {
                            Button(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        } else {
                            OutlinedButton(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        }
                    }
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecluseRegistrationPanel(
    cards: List<PlayerCard>,
    recluse: PlayerCard,
    teams: List<ClocktowerTeam>,
    registersEvil: Boolean,
    registeredRoleEnName: String?,
    recommendations: List<ClocktowerRegistrationRecommendationOption> = emptyList(),
    automaticStorytellerInfo: Boolean = false,
    automaticStorytellerStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    selectionAudit: SelectionAuditContext? = null,
    enabled: Boolean,
    onRegistersEvilChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val roles = completeTroubleBrewingRoles.filter { it.team in teams }
    val registrationPool = unifiedRegistrationPool(recommendations)
    val assistedRecommendations = registrationPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: recommendations
    val automaticRecommendation = WeightedStableSelector.selectStyle(
        registrationPool?.candidatesFor(SelectionExecutionPolicy.AUTO)?.map { it.payload }.orEmpty(),
        automaticStorytellerStyle,
        ClocktowerRegistrationRecommendationOption::style,
    )
    val automaticStyleLabel = when (automaticStorytellerStyle) {
        RecommendationStyle.GENTLE -> if (language == "en") "gentle" else "稳健"
        RecommendationStyle.BALANCED -> if (language == "en") "balanced" else "均衡"
        RecommendationStyle.AGGRESSIVE -> if (language == "en") "aggressive" else "激进"
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRecommendation, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRecommendation != null) {
            selectionAudit?.let { audit ->
                audit.recorder.recordPreview(
                    SelectionAuditRecord(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        candidates = recommendations.map { option ->
                            SelectionAuditCandidate(
                                familyId = if (option.usesSpecialRegistration) "special-registration" else "actual-registration",
                                qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
                            )
                        },
                    ),
                )
                audit.recorder.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = audit.selectionId,
                        dimensions = audit.dimensions,
                        selectedFamilyId = if (automaticRecommendation.usesSpecialRegistration) "special-registration" else "actual-registration",
                    ),
                )
            }
            onRegistersEvilChange(automaticRecommendation.usesSpecialRegistration)
            if (automaticRecommendation.usesSpecialRegistration) {
                automaticRecommendation.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                },
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (language == "en") "Recluse registration" else "隐士登记裁定", fontWeight = FontWeight.Black)
        Text(
            "${recluse.seatLabel(cards)} · ${if (language == "en") "this interaction only" else "仅影响本次交互"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!enabled) {
            Text(
                if (language == "en") "The Recluse is poisoned and must register normally." else "隐士已中毒，本次只能按真实身份登记。",
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            if (automaticStorytellerInfo && automaticRecommendation != null) {
                Text(
                    if (language == "en") "Automatic $automaticStyleLabel ruling" else "已自动采用${automaticStyleLabel}裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(automaticRecommendation.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RecommendationReasonSummary(
                    automaticRecommendation.reasonCodes,
                    automaticRecommendation.warningCodes,
                    language,
                )
            } else if (assistedRecommendations.isNotEmpty()) {
                Text(
                    if (language == "en") "Recommended ruling" else "推荐裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                assistedRecommendations.forEach { recommendation ->
                    val apply = {
                        onRegistersEvilChange(recommendation.usesSpecialRegistration)
                        if (recommendation.usesSpecialRegistration) {
                            recommendation.registeredRoleEnName?.let(onRoleChange)
                        }
                    }
                    if (recommendation.isDefaultRecommendation) {
                        Button(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    } else {
                        OutlinedButton(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    }
                    RecommendationReasonSummary(recommendation.reasonCodes, recommendation.warningCodes, language)
                }
                Text(
                    if (language == "en") "Or choose manually" else "或手动裁定",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!automaticStorytellerInfo) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!registersEvil) {
                    Button(onClick = { onRegistersEvilChange(false) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Actual" else "真实身份") }
                } else {
                    OutlinedButton(onClick = { onRegistersEvilChange(false) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Actual" else "真实身份") }
                }
                if (registersEvil) {
                    Button(onClick = { onRegistersEvilChange(true) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Register evil" else "登记邪恶") }
                } else {
                    OutlinedButton(onClick = { onRegistersEvilChange(true) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Register evil" else "登记邪恶") }
                }
            }
            if (registersEvil && roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { role ->
                        if (registeredRoleEnName == role.enName) {
                            Button(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        } else {
                            OutlinedButton(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun ClocktowerNightStepCardLocalized(
    recommendationCoordinator: ClocktowerRecommendationCoordinator,
    automaticStorytellerInfo: Boolean,
    automaticStorytellerStyle: RecommendationStyle,
    phase: ClocktowerPhase,
    debugDiagnosticsExpanded: Boolean,
    selectionDistributionTelemetry: SelectionDistributionTelemetryRecorder,
    evilAdvantage: Int,
    informationDecisionKey: String,
    cards: List<PlayerCard>,
    aliveCards: List<PlayerCard>,
    step: ClocktowerNightStepUi,
    spyCard: PlayerCard?,
    spyRegistrationGood: Boolean,
    spyRegisteredRoleEnName: String?,
    spyRegistrationRecommendations: List<ClocktowerRegistrationRecommendationOption>,
    spyCanRegister: Boolean,
    onSpyRegistrationGoodChange: (Boolean) -> Unit,
    onSpyRegistrationRoleChange: (String) -> Unit,
    recluseCard: PlayerCard?,
    recluseRegistrationEvil: Boolean,
    recluseRegisteredRoleEnName: String?,
    recluseRegistrationRecommendations: List<ClocktowerRegistrationRecommendationOption>,
    recluseCanRegister: Boolean,
    onRecluseRegistrationEvilChange: (Boolean) -> Unit,
    onRecluseRegistrationRoleChange: (String) -> Unit,
    selectedName: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    onSelectName: (String) -> Unit,
    onSelectFortuneTellerFirst: (String) -> Unit,
    onSelectFortuneTellerSecond: (String) -> Unit,
    onSelectChambermaidFirst: (String) -> Unit,
    onSelectChambermaidSecond: (String) -> Unit,
    onApplyRecommendedDisplayOption: (ClocktowerDisplayOption) -> Unit,
    onShowPlayerDisplay: (ClocktowerNightStepUi) -> Unit,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    showNavigationActions: Boolean = true,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun optionId(option: ClocktowerDisplayOption): String = clocktowerInformationCandidateId(option)
    // B7.3's first production slice: a single complete first-night pool is
    // projected differently by execution policy. Later-night families retain
    // their legacy lists until individually migrated.
    val firstNightPool = step.legacyInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: "first-night-information",
            automaticStyle = automaticStorytellerStyle,
        ) }
    var firstNightPoolBenchmarkRuns by remember(
        phase,
        step.roleEnName,
        step.legacyInformationCandidates,
        automaticStorytellerStyle,
    ) { mutableStateOf(0) }
    var firstNightPoolBenchmarkReport by remember { mutableStateOf<UnifiedSelectionPoolDeviceBenchmarkReport?>(null) }
    var firstNightPoolBenchmarkError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(firstNightPoolBenchmarkRuns) {
        if (firstNightPoolBenchmarkRuns == 0) return@LaunchedEffect
        firstNightPoolBenchmarkReport = null
        firstNightPoolBenchmarkError = null
        runCatching {
            val options = requireNotNull(step.legacyInformationCandidates.takeIf {
                phase == ClocktowerPhase.FirstNight && it.isNotEmpty()
            })
            val family = step.roleEnName ?: "first-night-information"
            withContext(Dispatchers.Default) {
                UnifiedSelectionPoolDeviceBenchmark.run(
                    poolFactory = {
                        unifiedFirstNightInformationPool(options, family, automaticStorytellerStyle)
                    },
                    playerCount = cards.size,
                    phase = StorytellerPhase.FIRST_NIGHT,
                    style = automaticStorytellerStyle,
                    styleOf = ClocktowerDisplayOption::recommendationStyle,
                )
            }
        }.onSuccess { report ->
            firstNightPoolBenchmarkReport = report
            Log.i(
                UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG,
                report.toLogLine(step.roleEnName ?: "first-night-information"),
            )
        }.onFailure { error ->
            firstNightPoolBenchmarkError = error.message ?: error.javaClass.simpleName
            Log.e(UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG, "Unified first-night pool diagnostic failed", error)
        }
    }
    val automaticInformationOptions = firstNightPool
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?: step.recommendedDisplayOptions
    val assistedInformationOptions = firstNightPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: step.recommendedDisplayOptions
    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions
    val dynamicDecisionFamily = when (step.action) {
        ClocktowerNightAction.MayorRedirect -> "mayor-redirect"
        ClocktowerNightAction.DemonSuccessor -> "demon-succession"
        else -> null
    }
    val dynamicDecisionPool = dynamicDecisionFamily
        ?.let { family -> unifiedDecisionPool(step.decisionOptions, family) }
    val automaticDecisionOptions = dynamicDecisionPool
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?: step.decisionOptions
    val assistedDecisionOptions = dynamicDecisionPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: step.decisionOptions
    val automaticDecision = WeightedStableSelector.selectStyle(
        automaticDecisionOptions,
        automaticStorytellerStyle,
        ClocktowerDecisionOption::recommendationStyle,
    )
    val selectionAudit = if (automaticStorytellerInfo) {
        SelectionAuditContext(
            selectionId = informationDecisionKey,
            dimensions = SelectionAuditDimensions(
                playerCount = cards.size,
                phase = if (phase == ClocktowerPhase.FirstNight) StorytellerPhase.FIRST_NIGHT else StorytellerPhase.NIGHT,
                style = automaticStorytellerStyle,
            ),
            recorder = selectionDistributionTelemetry,
        )
    } else {
        null
    }
    val automaticDisplayOption = recommendationCoordinator.selectInformation(
        options = automaticInformationOptions,
        reliability = step.informationReliability,
        style = automaticStorytellerStyle,
        evilAdvantage = evilAdvantage,
        stableKey = informationDecisionKey,
        recentMisinformationStreak = step.recentMisinformationStreak,
        stableIdOf = ::optionId,
        isTruthful = ClocktowerDisplayOption::isTruthful,
        misinformationPressure = ClocktowerDisplayOption::misinformationPressure,
        styleOf = ClocktowerDisplayOption::recommendationStyle,
        selectionAudit = selectionAudit,
    )
    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {
        onApplyRecommendedDisplayOption(option)
        selectionAudit?.let { audit ->
            audit.recorder.recordCommittedSelection(
                SelectionAuditCommit(
                    selectionId = audit.selectionId,
                    dimensions = audit.dimensions,
                    selectedFamilyId = DynamicCandidateGenerator.selectionAuditFamilyId(
                        reliability = step.informationReliability,
                        truthful = option.isTruthful,
                    ),
                ),
            )
        }
        onShowPlayerDisplay(
            step.copy(
                tellPlayer = option.displayPrimary,
                displayKind = option.displayKind,
                displayTitle = option.displayTitle,
                displayPrimary = option.displayPrimary,
                displaySecondary = option.displaySecondary,
                displayFooter = option.displayFooter,
                displayProposition = option.proposition,
                selectedInformationTruthful = option.isTruthful,
                displayOptions = emptyList(),
                recommendedDisplayOptions = emptyList(),
            ),
        )
    }
    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecision?.targetName) {
        if (automaticStorytellerInfo && automaticDecision != null && selectedName != automaticDecision.targetName) {
            onSelectName(automaticDecision.targetName)
        }
    }
    var fortuneTellerLimitExceeded by remember(step.actor?.name, step.title) { mutableStateOf(false) }
    var showFortuneTellerDisplayOptions by remember(step.actor?.name, step.title) { mutableStateOf(false) }
    val command = when {
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
            if (language == "en") {
                "Ask ${step.actor.seatLabel(cards)} to choose two players to check"
            } else {
                "让 ${step.actor.seatLabel(cards)} 选择两名想要查验的玩家"
            }
        }
        step.wakeText != null -> step.wakeText
        step.actor != null -> if (language == "en") "Wake ${step.actor.seatLabel(cards)}" else "唤醒 ${step.actor.seatLabel(cards)}"
        else -> step.title
    }
    val helper = when {
        step.action == ClocktowerNightAction.Chambermaid -> if (language == "en") "Have them choose two players, then show only the number." else "让她选择两名玩家，点查询后直接展示数字。"
        step.action == ClocktowerNightAction.FortuneTeller -> if (language == "en") "Have them choose two players, then show Yes or No." else "让他选择两名玩家，点查询后展示“有”或“没有”。"
        step.action == ClocktowerNightAction.RedHerring -> if (language == "en") "Choose a good player as the red herring. The Fortune Teller detects that player as a Demon." else "选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。"
        step.action == ClocktowerNightAction.Poison -> if (language == "en") "Record the poisoned player." else "记录中毒的玩家。"
        step.action == ClocktowerNightAction.ButlerMaster -> if (language == "en") "Have the Butler choose a master. During voting, the Butler may vote only if their master votes." else "让他选择主人。白天计票时，只有主人投票时，管家才能投票，需要管家自律。"
        step.action == ClocktowerNightAction.MonkProtect -> if (language == "en") "Record the protected player." else "记录被保护的玩家。"
        step.action == ClocktowerNightAction.DemonKill -> if (language == "en") "Record the selected kill target." else "记录被击杀的玩家。"
        step.action == ClocktowerNightAction.MayorRedirect -> if (language == "en") "Let the Mayor die or redirect the death to another player." else "选择让市长死亡，或将死亡转移给另一名玩家。"
        step.action == ClocktowerNightAction.DemonSuccessor -> if (language == "en") "Choose an eligible living Minion to become the new Imp." else "选择一名合法的存活爪牙成为新的小恶魔。"
        step.action == ClocktowerNightAction.Ravenkeeper -> if (language == "en") "After choosing a target, show that character only to the Ravenkeeper." else "选目标后，把该玩家角色只给他看。"
        step.displayKind != ClocktowerDisplayKind.None -> if (language == "en") "Show the information to the player." else "展示信息给玩家。"
        else -> step.explanation
    }
    Card(
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

            if (BuildConfig.DEBUG && debugDiagnosticsExpanded && firstNightPool != null) {
                OutlinedButton(
                    onClick = { firstNightPoolBenchmarkRuns += 1 },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Run first-night unified pool benchmark (11 samples)") }
                firstNightPoolBenchmarkReport?.let { report ->
                    Text(
                        report.toLogLine(step.roleEnName ?: "first-night-information"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                firstNightPoolBenchmarkError?.let { error ->
                    Text(
                        "First-night pool diagnostic failed: $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (step.spyRegistrationKey != null && spyCard != null) {
                SpyRegistrationPanel(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    cards = cards,
                    spy = spyCard,
                    teams = step.spyRegistrationTeams,
                    registersGood = spyRegistrationGood,
                    registeredRoleEnName = spyRegisteredRoleEnName,
                    detail = step.spyRegistrationDetail,
                    hint = step.spyRegistrationHint,
                    recommendations = spyRegistrationRecommendations,
                    enabled = spyCanRegister,
                    selectionAudit = selectionAudit?.copy(selectionId = "$informationDecisionKey|spy-registration"),
                    onRegistersGoodChange = onSpyRegistrationGoodChange,
                    onRoleChange = onSpyRegistrationRoleChange,
                )
            }
            if (step.recluseRegistrationKey != null && recluseCard != null) {
                RecluseRegistrationPanel(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    automaticStorytellerStyle = automaticStorytellerStyle,
                    cards = cards,
                    recluse = recluseCard,
                    teams = step.recluseRegistrationTeams,
                    registersEvil = recluseRegistrationEvil,
                    registeredRoleEnName = recluseRegisteredRoleEnName,
                    recommendations = recluseRegistrationRecommendations,
                    enabled = recluseCanRegister,
                    selectionAudit = selectionAudit?.copy(selectionId = "$informationDecisionKey|recluse-registration"),
                    onRegistersEvilChange = onRecluseRegistrationEvilChange,
                    onRoleChange = onRecluseRegistrationRoleChange,
                )
            }
            if (step.decisionOptions.isNotEmpty()) {
                HostActionSection(
                    title = if (language == "en") "Recommended ruling" else "推荐裁定",
                    helper = if (automaticStorytellerInfo) {
                        if (language == "en") "The selected automatic ruling has been applied." else "已采用当前自动模式的裁定。"
                    } else if (language == "en") {
                        "The balanced option is the beginner default. You can still choose manually below."
                    } else {
                        if (language == "en") "The balanced option is the beginner default; you can still choose manually below." else "平衡方案是新手默认建议；仍可在下方手动裁定。"
                    },
                ) {
                    step.decisionOptions
                        .filter { !automaticStorytellerInfo || it == automaticDecision }
                        .forEach { option ->
                        if (automaticStorytellerInfo) {
                            Text(option.label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else
                        if (automaticStorytellerInfo || option.isDefaultRecommendation) {
                            Button(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        }
                        RecommendationReasonSummary(option.reasonCodes, option.warningCodes, language)
                    }
                }
            }

        when (step.action) {
            ClocktowerNightAction.RedHerring -> {
                if (step.isRealAction) {
                    val candidates = clocktowerRedHerringCandidates(aliveCards)
                    HostActionSection(
                        title = stringResource(R.string.clocktower_host_choose_red_herring),
                        helper = stringResource(R.string.clocktower_host_choose_red_herring_hint),
                    ) {
                        if (candidates.isEmpty()) {
                            Text(
                                stringResource(R.string.clocktower_host_no_red_herring_candidates),
                                color = Color(0xFF6F7B74),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            SelectablePlayerChips(
                                cards = candidates,
                                selectedName = selectedName,
                                enabled = true,
                                allCards = cards,
                                onSelect = onSelectName,
                            )
                        }
                    }
                }
            }

            ClocktowerNightAction.Poison -> {
                HostActionSection(title = stringResource(R.string.clocktower_host_choose_poison_target)) {
                    SelectablePlayerChips(
                        cards = aliveCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.ButlerMaster -> {
                HostActionSection(
                    title = if (LocalContext.current.resources.configuration.locales[0].language == "en") "Choose the Butler's master" else "选择管家的主人",
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.MonkProtect -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.FortuneTeller -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SelectableTwoPlayerChips(
                        cards = cards,
                        firstSelectedName = fortuneTellerFirst,
                        secondSelectedName = fortuneTellerSecond,
                        enabled = step.isRealAction,
                        onSelect = { name ->
                            showFortuneTellerDisplayOptions = false
                            when (twoPlayerSelectionAction(fortuneTellerFirst, fortuneTellerSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> {
                                    fortuneTellerLimitExceeded = false
                                    onSelectFortuneTellerFirst(name)
                                }
                                TwoPlayerSelectionAction.ToggleSecond -> {
                                    fortuneTellerLimitExceeded = false
                                    onSelectFortuneTellerSecond(name)
                                }
                                TwoPlayerSelectionAction.RejectLimit -> {
                                    fortuneTellerLimitExceeded = true
                                }
                            }
                        },
                    )
                    if (fortuneTellerLimitExceeded) {
                        Text(
                            stringResource(R.string.clocktower_fortune_cannot_select_more_than_two),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else if (fortuneTellerFirst == null || fortuneTellerSecond == null) {
                        Text(
                            stringResource(R.string.clocktower_fortune_must_select_two),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Button(
                        onClick = {
                            if (automaticStorytellerInfo && automaticDisplayOption != null) {
                                showRecommendedDisplayOption(automaticDisplayOption)
                            } else if (step.displayOptions.isNotEmpty() || displayedInformationOptions.isNotEmpty()) {
                                showFortuneTellerDisplayOptions = true
                            } else {
                                onShowPlayerDisplay(step)
                            }
                        },
                        enabled = step.isRealAction && fortuneTellerFirst != null && fortuneTellerSecond != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_fortune_show_result))
                    }
                    if (showFortuneTellerDisplayOptions) {
                        Text(
                            stringResource(R.string.clocktower_fortune_unreliable_choose_result),
                            color = Color(0xFF8C4B20),
                        )
                    }
                }
            }

            ClocktowerNightAction.Chambermaid -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val candidates = aliveCards.filter { it.name != step.actor?.name }
                    SelectableSeatNumbers(
                        cards = candidates,
                        selectedName = chambermaidFirst,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectChambermaidFirst,
                    )
                    SelectableSeatNumbers(
                        cards = candidates.filter { it.name != chambermaidFirst },
                        selectedName = chambermaidSecond,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectChambermaidSecond,
                    )
                    if (step.displayOptions.isEmpty()) {
                        Button(
                            onClick = { onShowPlayerDisplay(step) },
                            enabled = step.isRealAction && chambermaidFirst != null && chambermaidSecond != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(if (language == "en") "Check and show" else "查询并展示")
                        }
                    } else {
                        Text(if (language == "en") "This ability is unreliable. Choose the final result below." else "能力不可靠：请在下方选择最终展示结果。", color = Color(0xFF8C4B20))
                    }
                }
            }

            ClocktowerNightAction.DemonKill -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_night_death),
                    helper = stringResource(R.string.clocktower_host_choose_night_death_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.MayorRedirect -> {
                val mayor = aliveCards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == "en") "The Demon attacked the Mayor" else "市长被恶魔击杀",
                    helper = if (language == "en") "Choosing a dead or protected player as the redirect target can result in no death tonight." else "选择死亡或受保护的玩家作为转移目标，可能导致今夜无人死亡。",
                ) {
                    if (mayor != null) {
                        if (selectedName == mayor.name) {
                            Button(
                                onClick = { onSelectName(mayor.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (language == "en") "Mayor dies" else "市长死亡")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(mayor.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (language == "en") "Mayor dies" else "市长死亡")
                            }
                        }
                        Text(if (language == "en") "Or redirect the death to:" else "或将死亡转移给：", fontWeight = FontWeight.SemiBold)
                        SelectablePlayerChips(
                            cards = cards.filter { it.name != mayor.name && it.name in assistedDecisionOptions.map(ClocktowerDecisionOption::targetName) },
                            selectedName = selectedName,
                            enabled = step.isRealAction,
                            allCards = cards,
                            onSelect = onSelectName,
                        )
                    }
                }
                }
            }

            ClocktowerNightAction.DemonSuccessor -> {
                val legalNames = assistedDecisionOptions.map { it.targetName }.toSet()
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == "en") "Choose the new Imp" else "选择新小恶魔",
                    helper = step.explanation,
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name in legalNames },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
                }
            }

            ClocktowerNightAction.Ravenkeeper -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            else -> Unit
        }

            step.tellPlayer
                ?.takeIf { step.isRealAction && it.isNotBlank() && step.displayKind == ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid }
                ?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

            if (
                displayedInformationOptions.isNotEmpty() &&
                (step.action != ClocktowerNightAction.FortuneTeller || showFortuneTellerDisplayOptions)
            ) {
                Text(if (language == "en") "Recommended information" else "推荐给说书人的完整信息", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    if (automaticStorytellerInfo) {
                        if (language == "en") "The automatic mode selected this information. Use the button below to show it." else "已按当前自动模式选定信息；点击下方按钮即可向玩家展示。"
                    } else {
                        if (language == "en") "The balanced option is the default; other options apply different pressure. Choosing one also updates this interaction's Spy or Recluse registration." else "平衡方案适合直接采用；其他方案提供不同压力。选择后会同步本次间谍或隐士登记。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                displayedInformationOptions
                    .filter { !automaticStorytellerInfo || it == automaticDisplayOption }
                    .sortedBy { if (it.isDefaultRecommendation) 0 else 1 }
                    .forEach { option ->
                        val onClick = {
                            showRecommendedDisplayOption(option)
                        }
                        if (option.isDefaultRecommendation) {
                            Button(
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("${if (automaticStorytellerInfo) { if (language == "en") "Auto" else "自动" } else { if (language == "en") "Default" else "默认" }} · ${option.label}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        }
                        RecommendationReasonSummary(option.reasonCodes, option.warningCodes, language)
                    }
                if (!automaticStorytellerInfo && firstNightPool == null) {
                    OutlinedButton(
                        onClick = { onShowPlayerDisplay(step.copy(recommendedDisplayOptions = emptyList())) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(if (language == "en") "Use the manual ruling above" else "使用上方手动裁定")
                    }
                }
            }

            if (
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
                (step.action != ClocktowerNightAction.FortuneTeller || showFortuneTellerDisplayOptions)
            ) {
                Text(if (language == "en") "This ability is unreliable. Choose a result to show." else "能力不可靠：请选择一个结果展示。", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                step.displayOptions.forEach { option ->
                    OutlinedButton(
                        onClick = {
                            onShowPlayerDisplay(
                                step.copy(
                                    tellPlayer = option.displayPrimary,
                                    displayKind = option.displayKind,
                                    displayTitle = option.displayTitle,
                                    displayPrimary = option.displayPrimary,
                                    displaySecondary = option.displaySecondary,
                                    displayFooter = option.displayFooter,
                                    displayProposition = option.proposition,
                                    selectedInformationTruthful = option.isTruthful,
                                    displayOptions = emptyList(),
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                    RecommendationReasonSummary(option.reasonCodes, option.warningCodes, language)
                }
            } else if (step.recommendedDisplayOptions.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid) {
                OutlinedButton(
                    onClick = { onShowPlayerDisplay(step) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.clocktower_host_show_to_player))
                }
            }

            if (showNavigationActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.previous_step))
                    }
                    Button(
                        onClick = onNext,
                        enabled = step.action !in setOf(
                            ClocktowerNightAction.MayorRedirect,
                            ClocktowerNightAction.DemonSuccessor,
                        ) || selectedName != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_finish_next))
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        if (language == "en") "STEP NOTE" else "步骤提示",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        helper,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClocktowerInfoCard(
    title: String,
    body: String,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, color = Color(0xFF5C6A63))
        }
    }
}
