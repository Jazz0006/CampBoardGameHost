from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

raw = TARGET.read_bytes()
if b"\r" in raw:
    raise SystemExit("Refusing to patch CampBoardGameHostApp.kt with CR/CRLF line endings")
text = raw.decode("utf-8")

old_persist = '''    fun persistActiveGameStateIfNeeded(): Boolean {
        if (!screen.isActiveGameScreen() || cards.isEmpty()) return false
        return baseContext.saveActiveGameState(activeGameSnapshotJson())
    }
'''

new_persist = '''    fun activeGameRecoverySnapshot(): RecoverySnapshot {
        val gameContentIdentity = activeGamePersistenceCoordinator.identityForSave(
            ActiveGamePersistenceInputs(
                gameKind = currentGameKind,
                clocktowerScript = currentClocktowerScript,
                assignedClocktowerRoleIds = if (currentGameKind == GameKind.Clocktower) {
                    cards.map { card ->
                        RoleId(requireNotNull(card.clocktowerRole) {
                            "Clocktower recovery save is missing an assigned role."
                        }.enName)
                    }
                } else {
                    emptyList()
                },
                assignedWerewolfRoles = if (currentGameKind == GameKind.Werewolf) {
                    cards.map { it.role }
                } else {
                    emptyList()
                },
                werewolfCount = werewolfCount,
                includeSeer = includeSeer,
                includeWitch = includeWitch,
                includeHunter = includeHunter,
                lastWordsMode = lastWordsMode,
            ),
        )
        val entryPoint = when (screen) {
            Screen.PassPhone -> RecoveryEntryPoint.PassPhone
            Screen.RevealCard -> RecoveryEntryPoint.RevealCard
            else -> RecoveryEntryPoint.Stable
        }
        val commonCards = cards.toList()
        val commonRecords = records.toList()
        val legacyRestoreCompatibility = LegacyRestoreCompatibility(
            activeGameStateVersion = ACTIVE_GAME_STATE_VERSION,
            identity = gameContentIdentity,
            committedClocktowerSetup = committedClocktowerSetup,
            troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,
            clocktowerRulesetRoleIds = clocktowerRulesetRoleIds.toSet(),
            clocktowerRulesetRef = clocktowerRulesetRef,
        )
        val recoveryGame: RecoveryGame = when (currentGameKind) {
            GameKind.Undercover -> UndercoverRecovery(
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
                undercoverCount = undercoverCount,
                includeBlank = includeBlank,
                lastWordsMode = lastWordsMode,
            )
            GameKind.Werewolf -> WerewolfRecovery(
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
                werewolfCount = werewolfCount,
                includeSeer = includeSeer,
                includeWitch = includeWitch,
                includeHunter = includeHunter,
                lastWordsMode = lastWordsMode,
                judgeStepIndex = werewolfJudgeStepIndex,
                pendingNightDeath = pendingNightDeath,
                seerCheckTarget = seerCheckTarget,
                witchSaveUsed = witchSaveUsed,
                witchPoisonUsed = witchPoisonUsed,
                witchSavedTonight = witchSavedTonight,
                witchPoisonTarget = witchPoisonTarget,
                hunterShotTarget = hunterShotTarget,
            )
            GameKind.Clocktower -> ClocktowerRecovery(
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
                identity = ClocktowerRecoveryIdentity(
                    script = currentClocktowerScript,
                    gameId = clocktowerGameId,
                    gameSeed = clocktowerGameSeed,
                ),
                position = ClocktowerRecoveryPosition(
                    phase = clocktowerPhase,
                    nightStarted = clocktowerNightStartedState.value,
                    nightStepIndex = clocktowerNightStepIndexState.value,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = clocktowerPendingNightDeath,
                    confirmedPoisonTarget = clocktowerConfirmedPoisonTarget,
                    confirmedMonkProtectedTarget = clocktowerConfirmedMonkProtectedTarget,
                    confirmedMayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
                    pendingNewDemonName = clocktowerPendingNewDemonName,
                    pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
                    confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget,
                    redHerring = clocktowerRedHerring,
                    demonBluffRoleNames = clocktowerRecommendedDemonBluffRoleNames.toList(),
                    butlerMaster = clocktowerButlerMaster,
                    virginUsed = clocktowerVirginUsed,
                    slayerUsed = clocktowerSlayerUsed,
                    slayerClaimedNames = clocktowerSlayerClaimedNames.toList(),
                    artistUsed = clocktowerArtistUsed,
                    artistClaimedNames = clocktowerArtistClaimedNames.toList(),
                    lastExecutedName = clocktowerLastExecutedName,
                    pendingKlutzName = clocktowerPendingKlutzName,
                    klutzChoiceName = clocktowerKlutzChoiceName,
                    klutzReturnToDawn = clocktowerKlutzReturnToDawn,
                    ghostVoteAuthority = clocktowerGhostVoteAuthorityState.value,
                    highestVoteName = clocktowerHighestVoteNameState.value,
                    highestVoteCount = clocktowerHighestVoteCountState.value,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = clocktowerGameStateRevision,
                    playerInputRevision = clocktowerPlayerInputRevision,
                    semanticHistoryMode = clocktowerSemanticHistoryMode,
                    actionTimeline = clocktowerActionTimeline,
                    nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
                    events = clocktowerEvents.toList(),
                    epistemicObservations = clocktowerEpistemicObservations.toList(),
                ),
            )
        }
        return RecoverySnapshot(
            compatibilityToken = "active-v${ACTIVE_GAME_STATE_VERSION}:${currentGameKind.name}",
            savedAtMillis = System.currentTimeMillis(),
            legacyRestoreCompatibility = legacyRestoreCompatibility,
            game = recoveryGame,
        )
    }

    fun persistActiveGameStateIfNeeded(): Boolean {
        if (!screen.isActiveGameScreen() || cards.isEmpty()) return false
        return baseContext.saveActiveGameState(
            RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot()),
        )
    }
'''

if text.count(old_persist) != 1:
    raise SystemExit(f"Expected exactly one active recovery save anchor, found {text.count(old_persist)}")

lifecycle_trigger = '''            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                latestPersistActiveGameState()
            }
'''
if text.count(lifecycle_trigger) != 1:
    raise SystemExit("Lifecycle recovery trigger anchor drifted before PS2 patch")
if text.count("    fun activeGameSnapshotJson(): JSONObject = JSONObject().apply {") != 1:
    raise SystemExit("Legacy active snapshot anchor drifted before PS2 patch")

text = text.replace(old_persist, new_persist, 1)

if text.count("RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())") != 1:
    raise SystemExit("Typed recovery write postcondition failed")
if text.count(lifecycle_trigger) != 1:
    raise SystemExit("Lifecycle recovery trigger changed unexpectedly")
if text.count("    fun activeGameSnapshotJson(): JSONObject = JSONObject().apply {") != 1:
    raise SystemExit("PS2 unexpectedly removed legacy snapshot infrastructure")

persist_start = text.index("    fun persistActiveGameStateIfNeeded(): Boolean {")
persist_end = text.index("\n    fun persistAndReleaseA4ObservationRebuildIfDurable()", persist_start)
persist_block = text[persist_start:persist_end]
if "activeGameSnapshotJson()" in persist_block:
    raise SystemExit("Production active recovery write still consumes the legacy snapshot")

TARGET.write_text(text, encoding="utf-8", newline="\n")
