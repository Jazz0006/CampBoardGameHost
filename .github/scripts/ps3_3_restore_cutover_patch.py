from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line endings; refusing App normalization")
text = raw.decode("utf-8")

start = "    fun restoreSavedGame() {\n"
end = "    val latestPersistActiveGameState by rememberUpdatedState { persistAndReleaseA4ObservationRebuildIfDurable() }\n"
if text.count(start) != 1:
    raise SystemExit(f"Expected one restore start marker, found {text.count(start)}")
if text.count(end) != 1:
    raise SystemExit(f"Expected one restore end marker, found {text.count(end)}")
start_index = text.index(start)
end_index = text.index(end)
if start_index >= end_index:
    raise SystemExit("Restore markers are out of order")

replacement = '''    fun applyValidatedRecoveryPlan(plan: ValidatedRecoveryPlan) {
        val game = plan.snapshot.game
        val restoredCards = game.cards.map(::localizedRestoredCard)

        playerNames.clear()
        playerNames.addAll(restoredCards.map(PlayerCard::name))
        cards.clear()
        cards.addAll(restoredCards)
        records.clear()
        records.addAll(game.records)

        currentGameKind = game.gameKind
        currentDealIndex = game.currentDealIndex
        round = game.round
        gameOutcome = game.outcome
        showResults = plan.presentResults
        savedGamePreview = null
        showHostTools = false
        showNewGameConfirmation = false

        undercoverCount = 1
        includeBlank = false
        lastWordsMode = LastWordsMode.FirstDay
        lastWordsPromptNames = emptyList()
        selectedElimination = null

        werewolfCount = 1
        includeSeer = true
        includeWitch = false
        includeHunter = false
        werewolfJudgeStepIndex = 0
        pendingNightDeath = null
        seerCheckTarget = null
        witchSaveUsed = false
        witchPoisonUsed = false
        witchSavedTonight = false
        witchPoisonTarget = null
        hunterShotTarget = null
        selectedDayExile = null

        selectedClocktowerScript = null
        currentClocktowerScript = ClocktowerScript.TroubleBrewing
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        clocktowerGameId = ""
        clocktowerGameSeed = 0L
        clocktowerGameStateRevision = 0L
        clocktowerPlayerInputRevision = 0L
        clocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL
        clocktowerNextTimelineGlobalSequence = 0L
        clocktowerRulesetRoleIds = emptySet()
        clocktowerRulesetRef = null
        clocktowerPhase = ClocktowerPhase.FirstNight
        clocktowerNightStartedState.value = false
        clocktowerNightStepIndexState.value = 0

        clocktowerEvents.clear()
        clocktowerEpistemicObservations.clear()
        clocktowerActionTimeline = ActionFactTimeline()
        clocktowerEventCounter = 0

        clocktowerPendingNightDeath = null
        clocktowerDemonAttackDraftTarget = null
        clocktowerSelectedExecution = null
        clocktowerPoisonTarget = null
        clocktowerConfirmedPoisonTarget = null
        clocktowerFortuneTellerFirst = null
        clocktowerFortuneTellerSecond = null
        clocktowerChambermaidFirst = null
        clocktowerChambermaidSecond = null
        clocktowerRavenkeeperTarget = null
        clocktowerRedHerring = null
        clocktowerRecommendedDemonBluffRoleNames = emptyList()
        clocktowerRecommendedDrunkInvestigatorRoleName = null
        clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerConfirmedMonkProtectedTarget = null
        clocktowerMayorRedirectTarget = null
        clocktowerConfirmedMayorRedirectTarget = null
        clocktowerPendingNewDemonName = null
        clocktowerPendingNightNewDemonIdentityName = null
        clocktowerDemonSuccessorTarget = null
        clocktowerConfirmedDemonSuccessorTarget = null
        clocktowerVirginUsed = false
        clocktowerSlayerUsed = false
        clocktowerSlayerClaimedNames = emptyList()
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerArtistClaimantName = null
        clocktowerArtistTruthfulAnswer = null
        clocktowerArtistShownAnswer = null
        clocktowerLastExecutedName = null
        clocktowerPendingKlutzName = null
        clocktowerKlutzChoiceName = null
        clocktowerKlutzReturnToDawn = false

        clocktowerDayModeState.value = ClocktowerDayMode.Overview
        clocktowerNominatorNameState.value = null
        clocktowerNomineeNameState.value = null
        clocktowerCurrentVoteCountState.value = 0
        clocktowerGhostVoteAuthorityState.value = ClocktowerGhostVoteAuthority()
        clocktowerHighestVoteNameState.value = null
        clocktowerHighestVoteCountState.value = 0
        clocktowerSlayerClaimantNameState.value = null
        clocktowerSlayerTargetNameState.value = null

        when (game) {
            is UndercoverRecovery -> {
                undercoverCount = game.undercoverCount
                includeBlank = game.includeBlank
                lastWordsMode = game.lastWordsMode
            }
            is ClocktowerRecovery -> {
                val runtime = plan.clocktowerRuntime
                val safeClocktower = plan.safeReentry as? RecoverySafeReentry.ClocktowerJudge
                val mechanics = game.mechanics
                val history = game.history

                currentClocktowerScript = game.identity.script
                committedTroubleBrewingSetupRotationRecord =
                    plan.snapshot.legacyRestoreCompatibility.troubleBrewingSetupRotationRecord
                clocktowerGameId = game.identity.gameId
                clocktowerGameSeed = game.identity.gameSeed
                clocktowerGameStateRevision = history.gameStateRevision
                clocktowerPlayerInputRevision = history.playerInputRevision
                clocktowerSemanticHistoryMode = history.semanticHistoryMode
                clocktowerNextTimelineGlobalSequence = history.nextTimelineGlobalSequence
                clocktowerRulesetRoleIds = if (game.identity.script == ClocktowerScript.TroubleBrewing) {
                    runtime?.rulesetBasis?.roleIds.orEmpty()
                } else {
                    emptySet()
                }
                clocktowerRulesetRef = runtime?.rulesetRef
                clocktowerPhase = safeClocktower?.phase ?: game.position.phase
                clocktowerNightStartedState.value = game.position.nightStarted
                clocktowerNightStepIndexState.value =
                    safeClocktower?.nightStepIndex ?: game.position.nightStepIndex

                clocktowerEvents.addAll(history.events)
                clocktowerEpistemicObservations.addAll(history.epistemicObservations)
                clocktowerActionTimeline = history.actionTimeline
                clocktowerEventCounter = history.events.maxOfOrNull(ClocktowerEvent::sequence) ?: 0

                clocktowerPendingNightDeath = mechanics.confirmedAttackTarget
                clocktowerConfirmedPoisonTarget = mechanics.confirmedPoisonTarget
                clocktowerConfirmedMonkProtectedTarget = mechanics.confirmedMonkProtectedTarget
                clocktowerConfirmedMayorRedirectTarget = mechanics.confirmedMayorRedirectTarget
                clocktowerPendingNewDemonName = mechanics.pendingNewDemonName
                clocktowerPendingNightNewDemonIdentityName = mechanics.pendingNightNewDemonIdentityName
                clocktowerConfirmedDemonSuccessorTarget = mechanics.confirmedDemonSuccessorTarget
                clocktowerRedHerring = mechanics.redHerring
                clocktowerRecommendedDemonBluffRoleNames = mechanics.demonBluffRoleNames
                clocktowerButlerMaster = mechanics.butlerMaster
                clocktowerVirginUsed = mechanics.virginUsed
                clocktowerSlayerUsed = mechanics.slayerUsed
                clocktowerSlayerClaimedNames = mechanics.slayerClaimedNames
                clocktowerArtistUsed = mechanics.artistUsed
                clocktowerArtistClaimedNames = mechanics.artistClaimedNames
                clocktowerLastExecutedName = mechanics.lastExecutedName
                clocktowerPendingKlutzName = mechanics.pendingKlutzName
                clocktowerKlutzReturnToDawn = mechanics.klutzReturnToDawn
                clocktowerGhostVoteAuthorityState.value = mechanics.ghostVoteAuthority
                clocktowerHighestVoteNameState.value = mechanics.highestVoteName
                clocktowerHighestVoteCountState.value = mechanics.highestVoteCount
                clocktowerDayModeState.value =
                    if (safeClocktower?.continuation == ClocktowerRecoveryContinuation.Klutz) {
                        ClocktowerDayMode.Klutz
                    } else {
                        ClocktowerDayMode.Overview
                    }
            }
            is WerewolfRecovery -> Unit
        }

        screen = when (plan.safeReentry) {
            RecoverySafeReentry.UndercoverGame -> Screen.Game
            is RecoverySafeReentry.PassPhone -> Screen.PassPhone
            is RecoverySafeReentry.RevealCard -> Screen.RevealCard
            is RecoverySafeReentry.ClocktowerJudge -> Screen.ClocktowerJudge
        }
    }

    fun restoreSavedGame() {
        RecoveryApplicationCoordinator.apply(
            raw = baseContext.loadActiveGameStateJson(),
            prepare = { raw -> baseContext.prepareCurrentRecoveryPlan(raw) },
            clearRejected = ::clearSavedGameState,
            crossSessionBoundary = ::invalidateA4SessionBoundary,
            applyValidated = ::applyValidatedRecoveryPlan,
        )
    }

'''

new_text = text[:start_index] + replacement + text[end_index:]

if "ClocktowerNightCheckpoint.fromPersistedValues" in new_text:
    raise SystemExit("Legacy NightCheckpoint restore fallback remains in App restore path")
if "activeGamePersistenceCoordinator.resolveForRestore" in new_text:
    raise SystemExit("Legacy active-game restore coordinator remains in App restore path")
if new_text.count("RecoveryApplicationCoordinator.apply(") != 1:
    raise SystemExit("Typed recovery application coordinator is not wired exactly once")
if new_text.count("fun applyValidatedRecoveryPlan(plan: ValidatedRecoveryPlan)") != 1:
    raise SystemExit("Validated recovery apply function missing or duplicated")
if new_text.count("fun restoreSavedGame()") != 1:
    raise SystemExit("restoreSavedGame missing or duplicated")

path.write_text(new_text, encoding="utf-8", newline="\n")
