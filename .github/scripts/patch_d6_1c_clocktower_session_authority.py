import re
from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label} anchor count was {count}, expected exactly 1")
    text = text.replace(old, new, 1)


replace_once(
    """import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
""",
    """import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
""",
    "session imports",
)

replace_once(
    """    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var currentClocktowerScript by remember { mutableStateOf(ClocktowerScript.TroubleBrewing) }
    var clocktowerGameId by remember { mutableStateOf(\"\") }
    var clocktowerGameSeed by remember { mutableStateOf(0L) }
    var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
    var committedTroubleBrewingSetupRotationRecord by remember {
        mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
    }
    var clocktowerGameStateRevision by remember { mutableStateOf(0L) }
    var clocktowerPlayerInputRevision by remember { mutableStateOf(0L) }
    var clocktowerSemanticHistoryMode by remember { mutableStateOf(ClocktowerSemanticHistoryMode.LEGACY_LOCAL) }
    var clocktowerNextTimelineGlobalSequence by remember { mutableStateOf(0L) }
    var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
    var clocktowerRulesetRoleIds by remember { mutableStateOf<Set<RoleId>>(emptySet()) }
""",
    """    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var clocktowerGameSession by remember { mutableStateOf<ClocktowerGameSession?>(null) }
    var clocktowerSessionView by remember { mutableStateOf<ClocktowerSessionView?>(null) }
    val currentClocktowerScript = clocktowerSessionView?.scriptId
        ?.let { scriptId ->
            ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
        }
        ?: ClocktowerScript.TroubleBrewing
    val clocktowerGameId = clocktowerSessionView?.gameId.orEmpty()
    val clocktowerGameSeed = clocktowerSessionView?.gameSeed ?: 0L
    val clocktowerGameStateRevision = clocktowerSessionView?.gameStateRevision ?: 0L
    val clocktowerPlayerInputRevision = clocktowerSessionView?.playerInputRevision ?: 0L
    val clocktowerSemanticHistoryMode =
        clocktowerSessionView?.semanticHistoryMode ?: ClocktowerSemanticHistoryMode.LEGACY_LOCAL
    val clocktowerNextTimelineGlobalSequence =
        clocktowerSessionView?.nextTimelineGlobalSequence ?: 0L
    val clocktowerActionTimeline = clocktowerSessionView?.actionTimeline ?: ActionFactTimeline()
    val clocktowerEpistemicObservations =
        clocktowerSessionView?.epistemicObservationLog?.records.orEmpty()
    var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
    var committedTroubleBrewingSetupRotationRecord by remember {
        mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
    }
    var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
    var clocktowerRulesetRoleIds by remember { mutableStateOf<Set<RoleId>>(emptySet()) }
""",
    "session authority declarations",
)

replace_once(
    """    val clocktowerEvents = remember { mutableStateListOf<ClocktowerEvent>() }
    val clocktowerEpistemicObservations = remember { mutableStateListOf<RecordedEpistemicObservation>() }
    var clocktowerActionTimeline by remember { mutableStateOf(ActionFactTimeline()) }
    var clocktowerEventCounter by remember { mutableStateOf(0) }
""",
    """    val clocktowerEvents = remember { mutableStateListOf<ClocktowerEvent>() }
    var clocktowerEventCounter by remember { mutableStateOf(0) }
""",
    "remove duplicate semantic history storage",
)

replace_once(
    """    fun invalidateA4SessionBoundary() {
        a4ShadowLifecycleInvalidator.sessionBoundary(clocktowerGameId)
    }

    fun advanceClocktowerGameStateRevision() {
        clocktowerGameStateRevision = clocktowerGameStateRevision + 1
        invalidateA4RevisionScope()
    }

    fun advanceClocktowerPlayerInputRevision() {
        clocktowerPlayerInputRevision = clocktowerPlayerInputRevision + 1
        invalidateA4RevisionScope()
    }
    val playerCount = playerNames.size
""",
    """    fun invalidateA4SessionBoundary() {
        a4ShadowLifecycleInvalidator.sessionBoundary(clocktowerGameId)
    }

    fun publishClocktowerSessionView() {
        clocktowerSessionView = clocktowerGameSession?.view
    }

    fun requireClocktowerGameSession(): ClocktowerGameSession =
        requireNotNull(clocktowerGameSession) {
            \"Clocktower session authority is unavailable.\"
        }

    fun advanceClocktowerGameStateRevision() {
        requireClocktowerGameSession().advanceGameStateRevision()
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
    }

    fun advanceClocktowerPlayerInputRevision() {
        requireClocktowerGameSession().recordPlayerInput()
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
    }
    val playerCount = playerNames.size
""",
    "session mutation adapters",
)

replace_once(
    """    fun recordClocktowerAction(draft: ActionFactDraft) {
        if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
        val committed = ClocktowerGameSession.commitGlobalActionFact(
            semanticHistoryMode = clocktowerSemanticHistoryMode,
            actionTimeline = clocktowerActionTimeline,
            observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
            nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
            draft = draft,
        )
        clocktowerActionTimeline = committed.actionTimeline
        clocktowerNextTimelineGlobalSequence = committed.nextTimelineGlobalSequence
    }
""",
    """    fun recordClocktowerAction(draft: ActionFactDraft) {
        if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
        requireClocktowerGameSession().commitGlobalActionFact(draft)
        publishClocktowerSessionView()
    }
""",
    "global action authority",
)

replace_once(
    """    fun recordEpistemicObservation(draft: EpistemicObservationDraft) {
        when (clocktowerSemanticHistoryMode) {
            ClocktowerSemanticHistoryMode.LEGACY_LOCAL -> {
                if (clocktowerEpistemicObservations.any { it.recordId == draft.recordId }) return
                clocktowerEpistemicObservations += draft.bindLegacyLocal()
                advanceClocktowerPlayerInputRevision()
                a4ObservationDurabilityGate.markPending(draft.recordId)
            }
            ClocktowerSemanticHistoryMode.GLOBAL_V1 -> {
                val committed = ClocktowerGameSession.commitGlobalEpistemicObservation(
                    semanticHistoryMode = clocktowerSemanticHistoryMode,
                    observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
                    nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
                    playerInputRevision = clocktowerPlayerInputRevision,
                    draft = draft,
                    actionTimeline = clocktowerActionTimeline,
                )
                if (committed.playerInputRevision == clocktowerPlayerInputRevision) return
                clocktowerEpistemicObservations.clear()
                clocktowerEpistemicObservations.addAll(committed.observationLog.records)
                clocktowerPlayerInputRevision = committed.playerInputRevision
                clocktowerNextTimelineGlobalSequence = committed.nextTimelineGlobalSequence
                invalidateA4RevisionScope()
                a4ObservationDurabilityGate.markPending(committed.record.recordId)
            }
        }
    }
""",
    """    fun recordEpistemicObservation(draft: EpistemicObservationDraft) {
        val session = requireClocktowerGameSession()
        when (clocktowerSemanticHistoryMode) {
            ClocktowerSemanticHistoryMode.LEGACY_LOCAL -> {
                if (clocktowerEpistemicObservations.any { it.recordId == draft.recordId }) return
                session.recordEpistemicObservation(draft.bindLegacyLocal())
                publishClocktowerSessionView()
                invalidateA4RevisionScope()
                a4ObservationDurabilityGate.markPending(draft.recordId)
            }
            ClocktowerSemanticHistoryMode.GLOBAL_V1 -> {
                val beforeRevision = clocktowerPlayerInputRevision
                val committed = session.commitGlobalEpistemicObservation(draft)
                if (session.view.playerInputRevision == beforeRevision) return
                publishClocktowerSessionView()
                invalidateA4RevisionScope()
                a4ObservationDurabilityGate.markPending(committed.recordId)
            }
        }
    }
""",
    "global observation authority",
)

replace_once(
    """        val committed = ClocktowerGameSession.commitGlobalEpistemicObservation(
            semanticHistoryMode = clocktowerSemanticHistoryMode,
            observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
            nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
            playerInputRevision = clocktowerPlayerInputRevision,
            draft = EpistemicObservationDraft(
                recordId = recordId ?: \"public-alive-${clocktowerGameId}-${eventSequence}-$seat\",
                phase = epistemicPhase,
                round = eventRound,
                sequence = eventSequence,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(seat, false),
            ),
            actionTimeline = clocktowerActionTimeline,
        )
""",
    """        val committed = requireClocktowerGameSession().preflightGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = recordId ?: \"public-alive-${clocktowerGameId}-${eventSequence}-$seat\",
                phase = epistemicPhase,
                round = eventRound,
                sequence = eventSequence,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(seat, false),
            ),
        )
""",
    "non-mutating observation preflight",
)

replace_once(
    """        selectedClocktowerScript = null
        currentClocktowerScript = ClocktowerScript.TroubleBrewing
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        clocktowerGameId = \"\"
        clocktowerGameSeed = 0L
        clocktowerGameStateRevision = 0L
        clocktowerPlayerInputRevision = 0L
        clocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL
        clocktowerNextTimelineGlobalSequence = 0L
        clocktowerRulesetRoleIds = emptySet()
        clocktowerRulesetRef = null
        clocktowerPhase = ClocktowerPhase.FirstNight
""",
    """        selectedClocktowerScript = null
        clocktowerGameSession = null
        publishClocktowerSessionView()
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        clocktowerRulesetRoleIds = emptySet()
        clocktowerRulesetRef = null
        clocktowerPhase = ClocktowerPhase.FirstNight
""",
    "recovery generic session reset",
)

replace_once(
    """        clocktowerNightStartedState.value = false
        clocktowerNightStepIndexState.value = 0

        clocktowerEvents.clear()
        clocktowerEpistemicObservations.clear()
        clocktowerActionTimeline = ActionFactTimeline()
        clocktowerEventCounter = 0

        clocktowerPendingNightDeath = null
""",
    """        clocktowerNightStartedState.value = false
        clocktowerNightStepIndexState.value = 0

        clocktowerEvents.clear()
        clocktowerEventCounter = 0

        clocktowerPendingNightDeath = null
""",
    "recovery semantic history reset",
)

replace_once(
    """                currentClocktowerScript = game.identity.script
                committedTroubleBrewingSetupRotationRecord =
                    game.troubleBrewingSetupRotationRecord
                clocktowerGameId = game.identity.gameId
                clocktowerGameSeed = game.identity.gameSeed
                clocktowerGameStateRevision = history.gameStateRevision
                clocktowerPlayerInputRevision = history.playerInputRevision
                clocktowerSemanticHistoryMode = history.semanticHistoryMode
                clocktowerNextTimelineGlobalSequence = history.nextTimelineGlobalSequence
                clocktowerRulesetRoleIds = if (game.identity.script == ClocktowerScript.TroubleBrewing) {
""",
    """                committedTroubleBrewingSetupRotationRecord =
                    game.troubleBrewingSetupRotationRecord
                val recoveredGameState = restoredCards.toClocktowerGameState(
                    script = game.identity.script,
                    seed = game.identity.gameSeed,
                    poisonedPlayerName = mechanics.confirmedPoisonTarget,
                )
                clocktowerGameSession = ClocktowerGameSession.restoreProduction(
                    ClocktowerSessionState(
                        gameId = game.identity.gameId,
                        gameStateRevision = history.gameStateRevision,
                        playerInputRevision = history.playerInputRevision,
                        gameSeed = game.identity.gameSeed,
                        gameState = recoveredGameState,
                        actionTimeline = history.actionTimeline,
                        epistemicObservationLog = EpistemicObservationLog(history.epistemicObservations),
                        semanticHistoryMode = history.semanticHistoryMode,
                        nextTimelineGlobalSequence = history.nextTimelineGlobalSequence,
                    ),
                )
                publishClocktowerSessionView()
                clocktowerRulesetRoleIds = if (game.identity.script == ClocktowerScript.TroubleBrewing) {
""",
    "recovery session restoration",
)

replace_once(
    """                clocktowerEvents.addAll(history.events)
                clocktowerEpistemicObservations.addAll(history.epistemicObservations)
                clocktowerActionTimeline = history.actionTimeline
                clocktowerEventCounter = history.events.maxOfOrNull(ClocktowerEvent::sequence) ?: 0
""",
    """                clocktowerEvents.addAll(history.events)
                clocktowerEventCounter = history.events.maxOfOrNull(ClocktowerEvent::sequence) ?: 0
""",
    "recovery history projection",
)

replace_once(
    """        records.clear()
        clocktowerEvents.clear()
        clocktowerEpistemicObservations.clear()
        clocktowerActionTimeline = ActionFactTimeline()
        clocktowerEventCounter = 0
        clocktowerSemanticHistoryMode = if (nextGameKind == GameKind.Clocktower) {
            ClocktowerSemanticHistoryMode.GLOBAL_V1
        } else {
            ClocktowerSemanticHistoryMode.LEGACY_LOCAL
        }
        clocktowerNextTimelineGlobalSequence = 0L
        currentDealIndex = 0
""",
    """        records.clear()
        clocktowerEvents.clear()
        clocktowerGameSession = null
        publishClocktowerSessionView()
        clocktowerEventCounter = 0
        currentDealIndex = 0
""",
    "new-game session boundary",
)

replace_once(
    """        clocktowerPhase = ClocktowerPhase.FirstNight
        currentClocktowerScript = clocktowerScript
        if (nextGameKind == GameKind.Clocktower) {
            clocktowerGameId = UUID.randomUUID().toString()
            clocktowerGameSeed = preparedClocktowerSeed ?: newClocktowerSeed()
            clocktowerGameStateRevision = 0L
            clocktowerPlayerInputRevision = 0L
            if (clocktowerScript == ClocktowerScript.TroubleBrewing) {
""",
    """        clocktowerPhase = ClocktowerPhase.FirstNight
        if (nextGameKind == GameKind.Clocktower) {
            val gameId = UUID.randomUUID().toString()
            val gameSeed = preparedClocktowerSeed ?: newClocktowerSeed()
            clocktowerGameSession = ClocktowerGameSession.createProduction(
                gameId = gameId,
                gameSeed = gameSeed,
                initialState = cards.toClocktowerGameState(
                    script = clocktowerScript,
                    seed = gameSeed,
                    poisonedPlayerName = null,
                ),
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            )
            publishClocktowerSessionView()
            if (clocktowerScript == ClocktowerScript.TroubleBrewing) {
""",
    "new Clocktower session creation",
)

replace_once(
    """        } else {
            clocktowerGameId = \"\"
            clocktowerGameSeed = 0L
            clocktowerGameStateRevision = 0L
            clocktowerPlayerInputRevision = 0L
            clocktowerRulesetRoleIds = emptySet()
            clocktowerRulesetRef = null
        }
""",
    """        } else {
            clocktowerRulesetRoleIds = emptySet()
            clocktowerRulesetRef = null
        }
""",
    "non-Clocktower session reset",
)

replace_once(
    """        clearSavedGameState()
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
""",
    """        clearSavedGameState()
        clocktowerGameSession = null
        publishClocktowerSessionView()
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
""",
    "archive session boundary",
)

replace_once(
    """        cards.clear()
        records.clear()
        clocktowerEvents.clear()
        clocktowerEpistemicObservations.clear()
        clocktowerEventCounter = 0
""",
    """        cards.clear()
        records.clear()
        clocktowerEvents.clear()
        clocktowerEventCounter = 0
""",
    "archive derived observation cleanup",
)

for forbidden in (
    "var currentClocktowerScript by remember",
    "var clocktowerGameId by remember",
    "var clocktowerGameSeed by remember",
    "var clocktowerGameStateRevision by remember",
    "var clocktowerPlayerInputRevision by remember",
    "var clocktowerSemanticHistoryMode by remember",
    "var clocktowerNextTimelineGlobalSequence by remember",
    "val clocktowerEpistemicObservations = remember { mutableStateListOf",
    "var clocktowerActionTimeline by remember",
    "ClocktowerGameSession.commitGlobalActionFact(",
    "ClocktowerGameSession.commitGlobalEpistemicObservation(",
    "clocktowerEpistemicObservations +=",
    "clocktowerEpistemicObservations.clear()",
    "clocktowerEpistemicObservations.addAll(",
):
    if forbidden in text:
        raise SystemExit(f"Forbidden duplicate authority remains: {forbidden}")

for writable_name in (
    "currentClocktowerScript",
    "clocktowerGameId",
    "clocktowerGameSeed",
    "clocktowerGameStateRevision",
    "clocktowerPlayerInputRevision",
    "clocktowerSemanticHistoryMode",
    "clocktowerNextTimelineGlobalSequence",
    "clocktowerActionTimeline",
):
    if re.search(rf"(?m)^\s*{re.escape(writable_name)}\s*=", text):
        raise SystemExit(f"Forbidden bare assignment remains for {writable_name}")

required_counts = {
    "ClocktowerGameSession.createProduction(": 1,
    "ClocktowerGameSession.restoreProduction(": 1,
    "ClocktowerSessionState(": 1,
    "var clocktowerGameSession by remember": 1,
    "var clocktowerSessionView by remember": 1,
    "fun publishClocktowerSessionView()": 1,
    "fun requireClocktowerGameSession()": 1,
    ".preflightGlobalEpistemicObservation(": 1,
}
for needle, expected in required_counts.items():
    count = text.count(needle)
    if count != expected:
        raise SystemExit(f"Required '{needle}' count was {count}, expected {expected}")

TARGET.write_text(text, encoding="utf-8", newline="\n")
