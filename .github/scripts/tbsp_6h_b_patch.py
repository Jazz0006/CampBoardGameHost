from pathlib import Path

APP = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")


def replace_once(path: Path, old: str, new: str, label: str) -> None:
    text = path.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    path.write_text(text.replace(old, new, 1))


replace_once(
    APP,
    "import com.codex.campboardgamehost.clocktower.domain.GameSnapshot\nimport com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment\n",
    "import com.codex.campboardgamehost.clocktower.domain.GameSnapshot\nimport com.codex.campboardgamehost.clocktower.domain.DecisionCandidate\nimport com.codex.campboardgamehost.clocktower.domain.GameState\nimport com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment\n",
    "App typed First Night imports",
)

replace_once(
    APP,
    "import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationRevealCoordinator\nimport com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmark\n",
    "import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationRevealCoordinator\nimport com.codex.campboardgamehost.clocktower.session.TroubleBrewingFirstNightPrecomputeCoordinator\nimport com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmark\n",
    "App 6H coordinator import",
)

replace_once(
    APP,
    '''    val troubleBrewingSetupRecommendationRevealCoordinator =
        remember(troubleBrewingSetupRecommendationPrewarmer) {
            TroubleBrewingSetupRecommendationRevealCoordinator(
                prewarmer = troubleBrewingSetupRecommendationPrewarmer,
            )
        }
    val a4ShadowWorldSetCache = remember { A4ShadowWorldSetCache() }
''',
    '''    val troubleBrewingSetupRecommendationRevealCoordinator =
        remember(troubleBrewingSetupRecommendationPrewarmer) {
            TroubleBrewingSetupRecommendationRevealCoordinator(
                prewarmer = troubleBrewingSetupRecommendationPrewarmer,
            )
        }
    val troubleBrewingFirstNightPrecomputeScope = rememberCoroutineScope()
    val troubleBrewingFirstNightPrecomputeCoordinator = remember {
        val recommendationCoordinator = ClocktowerRecommendationCoordinator()
        TroubleBrewingFirstNightPrecomputeCoordinator<GameState, List<DecisionCandidate<SetupClueOutcome>>> { request ->
            recommendationCoordinator.naturalPairCandidates(request)
        }
    }
    val a4ShadowWorldSetCache = remember { A4ShadowWorldSetCache() }
''',
    "App remembered First Night lifecycle owner",
)

replace_once(
    APP,
    '''        val initialSetupRecommendationRequest = SetupCoordinationRequest(
            game = committedCards.toClocktowerGameState(
                script = ClocktowerScript.TroubleBrewing,
                seed = preparedSeed,
                poisonedPlayerName = null,
            ),
            roles = setupRecommendationRoleDefinitions,
            lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
                dealPlan = preparedSetup.dealPlan,
                roleDefinitions = setupRecommendationRoleDefinitions,
            ),
            history = gameHistory.toClocktowerSetupHistory(),
        )

        cards.clear()
''',
    '''        val initialSetupRecommendationRequest = SetupCoordinationRequest(
            game = committedCards.toClocktowerGameState(
                script = ClocktowerScript.TroubleBrewing,
                seed = preparedSeed,
                poisonedPlayerName = null,
            ),
            roles = setupRecommendationRoleDefinitions,
            lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
                dealPlan = preparedSetup.dealPlan,
                roleDefinitions = setupRecommendationRoleDefinitions,
            ),
            history = gameHistory.toClocktowerSetupHistory(),
        )
        val initialFirstNightPrecomputeRequest = committedCards.toClocktowerGameState(
            script = ClocktowerScript.TroubleBrewing,
            seed = preparedSeed,
            poisonedPlayerName = null,
        )

        cards.clear()
''',
    "App committed First Night request",
)

replace_once(
    APP,
    '''                committedTroubleBrewingSetupSelection = preparedSetup.selection
            },
            launchBackground = { work ->
''',
    '''                committedTroubleBrewingSetupSelection = preparedSetup.selection
                troubleBrewingFirstNightPrecomputeCoordinator.prewarm(
                    request = initialFirstNightPrecomputeRequest,
                    launchBackground = { work ->
                        troubleBrewingFirstNightPrecomputeScope.launch(Dispatchers.Default) {
                            work()
                        }
                    },
                )
            },
            launchBackground = { work ->
''',
    "App Reveal to First Night prewarm launch",
)

replace_once(
    APP,
    '''                        setupRecommendationResultProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingSetupRecommendationRevealCoordinator::resultFor
                            } else {
                                null
                            },
                        onInitialRecommendationDemand = recordA4InitialRecommendationDemand,
''',
    '''                        setupRecommendationResultProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingSetupRecommendationRevealCoordinator::resultFor
                            } else {
                                null
                            },
                        firstNightNaturalPairReadyProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingFirstNightPrecomputeCoordinator::readyFor
                            } else {
                                null
                            },
                        firstNightNaturalPairResultProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingFirstNightPrecomputeCoordinator::resultFor
                            } else {
                                null
                            },
                        onInitialRecommendationDemand = recordA4InitialRecommendationDemand,
''',
    "App Judge First Night providers",
)

replace_once(
    HOST,
    "import com.codex.campboardgamehost.clocktower.domain.GameSnapshot\nimport com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice\n",
    "import com.codex.campboardgamehost.clocktower.domain.GameSnapshot\nimport com.codex.campboardgamehost.clocktower.domain.DecisionCandidate\nimport com.codex.campboardgamehost.clocktower.domain.GameState\nimport com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice\n",
    "Host typed First Night imports",
)

replace_once(
    HOST,
    '''    setupHistory: CrossGameHistory,
    setupRecommendationResultProvider: ((SetupCoordinationRequest) -> SetupRecommendationService.ConstrainedResult)? = null,
    onInitialRecommendationDemand: () -> Unit,
''',
    '''    setupHistory: CrossGameHistory,
    setupRecommendationResultProvider: ((SetupCoordinationRequest) -> SetupRecommendationService.ConstrainedResult)? = null,
    firstNightNaturalPairReadyProvider: ((GameState) -> List<DecisionCandidate<SetupClueOutcome>>?)? = null,
    firstNightNaturalPairResultProvider: (suspend (GameState) -> List<DecisionCandidate<SetupClueOutcome>>)? = null,
    onInitialRecommendationDemand: () -> Unit,
''',
    "Host First Night provider signature",
)

replace_once(
    HOST,
    '''    var slayerClaimantName by slayerClaimantNameState
    var slayerTargetName by slayerTargetNameState
    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    var slayerRecluseRegistersDemon by remember { mutableStateOf(false) }
    val recommendationKey = buildString {
''',
    '''    var slayerClaimantName by slayerClaimantNameState
    var slayerTargetName by slayerTargetNameState
    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    var slayerRecluseRegistersDemon by remember { mutableStateOf(false) }
    val firstNightNaturalPairPrecomputeRequest = if (
        script == ClocktowerScript.TroubleBrewing &&
        phase == ClocktowerPhase.FirstNight &&
        firstNightNaturalPairResultProvider != null
    ) {
        cards.toClocktowerGameState(
            script = script,
            seed = gameSeed,
            poisonedPlayerName = null,
        )
    } else {
        null
    }
    var firstNightNaturalPairCandidates by remember(gameId, gameSeed) {
        mutableStateOf<List<DecisionCandidate<SetupClueOutcome>>?>(null)
    }
    var firstNightNaturalPairStartRequested by remember(gameId, gameSeed) { mutableStateOf(false) }
    var firstNightNaturalPairLoadFailed by remember(gameId, gameSeed) { mutableStateOf(false) }
    var firstNightNaturalPairRetryGeneration by remember(gameId, gameSeed) { mutableStateOf(0) }
    LaunchedEffect(firstNightNaturalPairPrecomputeRequest, firstNightNaturalPairRetryGeneration) {
        val request = firstNightNaturalPairPrecomputeRequest
        val resultProvider = firstNightNaturalPairResultProvider
        if (request == null || resultProvider == null) {
            firstNightNaturalPairCandidates = null
            firstNightNaturalPairLoadFailed = false
            return@LaunchedEffect
        }
        firstNightNaturalPairCandidates = firstNightNaturalPairReadyProvider?.invoke(request)
        if (firstNightNaturalPairCandidates != null) {
            firstNightNaturalPairLoadFailed = false
            return@LaunchedEffect
        }
        val result = runCatching {
            withContext(Dispatchers.Default) {
                resultProvider(request)
            }
        }
        if (!isActive) return@LaunchedEffect
        result.fold(
            onSuccess = { candidates ->
                firstNightNaturalPairCandidates = candidates
                firstNightNaturalPairLoadFailed = false
            },
            onFailure = {
                firstNightNaturalPairCandidates = null
                firstNightNaturalPairLoadFailed = true
            },
        )
    }
    val firstNightNaturalPairPrecomputeReady =
        firstNightNaturalPairPrecomputeRequest == null || firstNightNaturalPairCandidates != null
    LaunchedEffect(firstNightNaturalPairStartRequested, firstNightNaturalPairPrecomputeReady) {
        if (firstNightNaturalPairStartRequested && firstNightNaturalPairPrecomputeReady) {
            firstNightNaturalPairStartRequested = false
            nightStarted = true
        }
    }
    val recommendationKey = buildString {
''',
    "Host First Night ready/busy/miss lifecycle",
)

replace_once(
    HOST,
    '''            fun addNaturalCandidates(abilityRole: RoleId) {
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
''',
    '''            fun addNaturalCandidates(abilityRole: RoleId) {
                val sourceSeat = cards.indexOfFirst { it.name == actor.name } + 1
                if (sourceSeat <= 0) return
                val naturalCandidates = if (
                    script == ClocktowerScript.TroubleBrewing &&
                    phase == ClocktowerPhase.FirstNight &&
                    firstNightNaturalPairResultProvider != null
                ) {
                    firstNightNaturalPairCandidates.orEmpty()
                } else {
                    val gameState = cards.toClocktowerGameState(
                        script = script,
                        seed = gameSeed,
                        poisonedPlayerName = poisonTarget,
                    )
                    recommendationCoordinator.naturalPairCandidates(gameState)
                }
                naturalCandidates
                    .filter { candidate ->
''',
    "Host consume precomputed natural pair baseline",
)

replace_once(
    HOST,
    '''            buttonLabel = text("确认裁定，开始首夜", "Confirm plan and begin first night"),
            onStartNight = { nightStarted = true },
''',
    '''            buttonLabel = text("确认裁定，开始首夜", "Confirm plan and begin first night"),
            onStartNight = {
                if (firstNightNaturalPairPrecomputeReady) {
                    nightStarted = true
                } else {
                    firstNightNaturalPairStartRequested = true
                    if (firstNightNaturalPairLoadFailed) {
                        firstNightNaturalPairRetryGeneration += 1
                    }
                }
            },
''',
    "Host First Night BUSY await gate",
)
