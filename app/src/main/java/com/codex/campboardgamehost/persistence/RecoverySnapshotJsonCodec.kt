package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import org.json.JSONArray
import org.json.JSONObject

/**
 * PS2 encoder for the typed emergency-recovery model.
 *
 * The output intentionally keeps the legacy flat keys that the PS2 restore reader still consumes.
 * PS3 replaces that reader; until then this codec is the only owner of the reduced write schema.
 */
internal object RecoverySnapshotJsonCodec {
    const val FORMAT_VERSION_KEY = "recoveryFormatVersion"
    const val COMPATIBILITY_TOKEN_KEY = "recoveryCompatibilityToken"

    fun encode(snapshot: RecoverySnapshot): JSONObject = JSONObject().apply {
        put(FORMAT_VERSION_KEY, snapshot.recoveryFormatVersion)
        put(COMPATIBILITY_TOKEN_KEY, snapshot.compatibilityToken)
        put("version", snapshot.legacyRestoreCompatibility.activeGameStateVersion)
        put("savedAtMillis", snapshot.savedAtMillis)
        put("currentGameKind", snapshot.game.gameKind.name)
        encodeEntryPoint(snapshot.game.entryPoint)
        put("currentDealIndex", snapshot.game.currentDealIndex.coerceAtLeast(0))
        put("round", snapshot.game.round.coerceAtLeast(1))
        put("cards", AppGameStateJsonCodec.encodeCards(snapshot.game.cards))
        put("records", AppGameStateJsonCodec.encodeRecords(snapshot.game.records))
        put(
            "gameOutcome",
            snapshot.game.outcome?.let(AppGameStateJsonCodec::encodeOutcome) ?: JSONObject.NULL,
        )

        val legacy = snapshot.legacyRestoreCompatibility
        put(
            PersistedActiveGameIdentityJsonCodec.ROOT_KEY,
            PersistedActiveGameIdentityJsonCodec.encode(legacy.identity),
        )
        legacy.committedClocktowerSetup?.let { setup ->
            put(
                CommittedClocktowerSetupPersistence.ROOT_KEY,
                CommittedClocktowerSetupPersistence.encode(setup),
            )
        }
        legacy.troubleBrewingSetupRotationRecord?.let { record ->
            put(
                TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
                TroubleBrewingSetupCompletionPersistence.encode(record),
            )
        }

        when (val game = snapshot.game) {
            is UndercoverRecovery -> encodeUndercover(game)
            is WerewolfRecovery -> encodeWerewolf(game)
            is ClocktowerRecovery -> encodeClocktower(game, legacy)
        }
    }

    private fun JSONObject.encodeEntryPoint(entryPoint: RecoveryEntryPoint) {
        when (entryPoint) {
            RecoveryEntryPoint.Stable -> Unit
            RecoveryEntryPoint.PassPhone -> put("screen", "PassPhone")
            RecoveryEntryPoint.RevealCard -> put("screen", "RevealCard")
        }
    }

    private fun JSONObject.encodeUndercover(game: UndercoverRecovery) {
        put("undercoverCount", game.undercoverCount.coerceAtLeast(1))
        put("includeBlank", game.includeBlank)
        put("lastWordsMode", game.lastWordsMode.name)
    }

    private fun JSONObject.encodeWerewolf(game: WerewolfRecovery) {
        put("werewolfCount", game.werewolfCount.coerceAtLeast(1))
        put("includeSeer", game.includeSeer)
        put("includeWitch", game.includeWitch)
        put("includeHunter", game.includeHunter)
        put("lastWordsMode", game.lastWordsMode.name)
        put("werewolfJudgeStepIndex", game.judgeStepIndex.coerceAtLeast(0))
        putNullableString("pendingNightDeath", game.pendingNightDeath)
        putNullableString("seerCheckTarget", game.seerCheckTarget)
        put("witchSaveUsed", game.witchSaveUsed)
        put("witchPoisonUsed", game.witchPoisonUsed)
        put("witchSavedTonight", game.witchSavedTonight)
        putNullableString("witchPoisonTarget", game.witchPoisonTarget)
        putNullableString("hunterShotTarget", game.hunterShotTarget)
    }

    private fun JSONObject.encodeClocktower(
        game: ClocktowerRecovery,
        legacy: LegacyRestoreCompatibility,
    ) {
        put("clocktowerPhase", game.position.phase.name)
        put("currentClocktowerScript", game.identity.script.name)
        put("clocktowerGameId", game.identity.gameId)
        put("clocktowerGameSeed", game.identity.gameSeed)
        put("clocktowerGameStateRevision", game.history.gameStateRevision.coerceAtLeast(0L))
        put("clocktowerPlayerInputRevision", game.history.playerInputRevision.coerceAtLeast(0L))
        put(
            ClocktowerSemanticHistoryPersistence.MODE_KEY,
            ClocktowerSemanticHistoryPersistence.encode(game.history.semanticHistoryMode),
        )
        put(
            ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
            ClocktowerSemanticHistoryPersistence.encodeActionTimeline(game.history.actionTimeline),
        )

        if (legacy.clocktowerRulesetRoleIds.isEmpty()) {
            put("clocktowerRulesetRoleIds", JSONObject.NULL)
        } else {
            put(
                "clocktowerRulesetRoleIds",
                ClocktowerRulesetPersistenceBasisJsonCodec.encode(
                    ClocktowerRulesetPersistenceBasis(legacy.clocktowerRulesetRoleIds),
                ),
            )
        }
        val rulesetRef = legacy.clocktowerRulesetRef
        if (rulesetRef == null) {
            put("clocktowerRulesetRef", JSONObject.NULL)
        } else {
            put("clocktowerRulesetRef", JSONObject().apply {
                put("scriptId", rulesetRef.scriptId.value)
                put("scriptContentHash", rulesetRef.scriptContentHash)
                put("rulesetVersion", rulesetRef.rulesetVersion)
                put("sourceRevision", rulesetRef.sourceRevision)
                put("coverage", rulesetRef.coverage.name)
            })
        }

        ClocktowerNightCheckpoint(
            phaseName = game.position.phase.name,
            round = game.round,
            gameStateRevision = game.history.gameStateRevision,
            playerInputRevision = game.history.playerInputRevision,
            nightStarted = game.position.nightStarted,
            nightStepIndex = game.position.nightStepIndex,
            confirmedAttackTarget = game.mechanics.confirmedAttackTarget,
            attackDraftTarget = null,
            confirmedPoisonTarget = game.mechanics.confirmedPoisonTarget,
            poisonDraftTarget = null,
            confirmedMonkTarget = game.mechanics.confirmedMonkProtectedTarget,
            monkDraftTarget = null,
            confirmedMayorRedirectTarget = game.mechanics.confirmedMayorRedirectTarget,
            mayorRedirectDraftTarget = null,
            pendingNewDemonName = game.mechanics.pendingNewDemonName,
            pendingNightNewDemonIdentityName = game.mechanics.pendingNightNewDemonIdentityName,
            demonSuccessorDraftTarget = null,
            confirmedDemonSuccessorTarget = game.mechanics.confirmedDemonSuccessorTarget,
            nextTimelineGlobalSequence = game.history.nextTimelineGlobalSequence,
        ).persistedValues().forEach { (key, value) -> put(key, value ?: JSONObject.NULL) }

        putNullableString("clocktowerRedHerring", game.mechanics.redHerring)
        put("clocktowerRecommendedDemonBluffRoleNames", stringsToJsonArray(game.mechanics.demonBluffRoleNames))
        putNullableString("clocktowerButlerMaster", game.mechanics.butlerMaster)
        put("clocktowerVirginUsed", game.mechanics.virginUsed)
        put("clocktowerSlayerUsed", game.mechanics.slayerUsed)
        put("clocktowerSlayerClaimedNames", stringsToJsonArray(game.mechanics.slayerClaimedNames))
        put("clocktowerArtistUsed", game.mechanics.artistUsed)
        put("clocktowerArtistClaimedNames", stringsToJsonArray(game.mechanics.artistClaimedNames))
        putNullableString("clocktowerLastExecutedName", game.mechanics.lastExecutedName)
        putNullableString("clocktowerPendingKlutzName", game.mechanics.pendingKlutzName)
        putNullableString("clocktowerKlutzChoiceName", game.mechanics.klutzChoiceName)
        put("clocktowerKlutzReturnToDawn", game.mechanics.klutzReturnToDawn)
        put(
            ClocktowerGhostVoteAuthorityPersistence.ROOT_KEY,
            ClocktowerGhostVoteAuthorityPersistence.encode(game.mechanics.ghostVoteAuthority),
        )
        putNullableString("clocktowerHighestVoteName", game.mechanics.highestVoteName)
        put("clocktowerHighestVoteCount", game.mechanics.highestVoteCount.coerceAtLeast(0))
        put("clocktowerEvents", AppGameStateJsonCodec.encodeEvents(game.history.events))
        put("clocktowerEpistemicObservations", encodeObservations(game.history.epistemicObservations))
    }

    private fun encodeObservations(
        observations: List<com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation>,
    ): JSONArray = JSONArray().apply {
        observations.forEach { observation ->
            put(JSONObject(EpistemicSemanticJson.encode(observation)))
        }
    }
}
