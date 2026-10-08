package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import org.json.JSONArray
import org.json.JSONObject

/** Typed current-format emergency-recovery encoder. */
internal object RecoverySnapshotJsonCodec {
    const val FORMAT_VERSION_KEY = "recoveryFormatVersion"
    const val COMPATIBILITY_TOKEN_KEY = "recoveryCompatibilityToken"

    fun encode(snapshot: RecoverySnapshot): JSONObject = JSONObject().apply {
        put(FORMAT_VERSION_KEY, snapshot.recoveryFormatVersion)
        put(COMPATIBILITY_TOKEN_KEY, snapshot.compatibilityToken)
        put("savedAtMillis", snapshot.savedAtMillis)
        put("currentGameKind", snapshot.game.gameKind.name)
        encodeEntryPoint(snapshot.game.entryPoint)
        put(
            "currentDealIndex",
            if (snapshot.game.entryPoint == RecoveryEntryPoint.Stable) 0
            else snapshot.game.currentDealIndex.coerceAtLeast(0),
        )
        put("round", snapshot.game.round.coerceAtLeast(1))
        put("cards", AppGameStateJsonCodec.encodeCards(snapshot.game.cards))
        put("records", AppGameStateJsonCodec.encodeRecords(snapshot.game.records))
        put(
            "gameOutcome",
            snapshot.game.outcome?.let(AppGameStateJsonCodec::encodeOutcome) ?: JSONObject.NULL,
        )

        when (val game = snapshot.game) {
            is UndercoverRecovery -> Unit
            is ClocktowerRecovery -> encodeClocktower(game)
        }
    }

    /** Strict PS3 read boundary. Malformed nested state is never repaired or partially decoded. */
    fun decodeStrict(
        json: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): RecoverySnapshot = RecoverySnapshotStrictDecoder.decode(json, roleByName)

    private fun JSONObject.encodeEntryPoint(entryPoint: RecoveryEntryPoint) {
        when (entryPoint) {
            RecoveryEntryPoint.Stable -> Unit
            RecoveryEntryPoint.PassPhone -> put("screen", "PassPhone")
            RecoveryEntryPoint.RevealCard -> put("screen", "RevealCard")
        }
    }

    private fun JSONObject.encodeClocktower(game: ClocktowerRecovery) {
        put("clocktowerPhase", game.position.phase.name)
        put("currentClocktowerScript", game.identity.script.name)
        put("clocktowerGameId", game.identity.gameId)
        put("clocktowerGameSeed", game.identity.gameSeed)
        game.troubleBrewingSetupRotationRecord?.let { record ->
            put(
                TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
                TroubleBrewingSetupCompletionPersistence.encode(record),
            )
        }
        putNullableString("clocktowerSdeHistoricalReplayInput", game.sdeHistoricalReplayInputJson)
        put("clocktowerGameStateRevision", game.history.gameStateRevision.coerceAtLeast(0L))
        put("clocktowerPlayerInputRevision", game.history.playerInputRevision.coerceAtLeast(0L))
        put(
            "clocktowerStorytellerPlayerContext",
            encodeStorytellerPlayerContext(game.history.storytellerPlayerContextBySeat),
        )
        put(
            ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
            ClocktowerSemanticHistoryPersistence.encodeActionTimeline(game.history.actionTimeline),
        )

        put("clocktowerNightStarted", game.position.nightStarted)
        put("clocktowerNightStepIndex", game.position.nightStepIndex.coerceAtLeast(0))
        putNullableString("clocktowerPendingNightDeath", game.mechanics.confirmedAttackTarget)
        putNullableString("clocktowerConfirmedPoisonTarget", game.mechanics.confirmedPoisonTarget)
        putNullableString("clocktowerConfirmedMonkProtectedTarget", game.mechanics.confirmedMonkProtectedTarget)
        putNullableString("clocktowerConfirmedMayorRedirectTarget", game.mechanics.confirmedMayorRedirectTarget)
        putNullableString("clocktowerPendingNewDemonName", game.mechanics.pendingNewDemonName)
        putNullableString(
            "clocktowerPendingNightNewDemonIdentityName",
            game.mechanics.pendingNightNewDemonIdentityName,
        )
        putNullableString(
            "clocktowerConfirmedDemonSuccessorTarget",
            game.mechanics.confirmedDemonSuccessorTarget,
        )
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
        put("clocktowerKlutzReturnToDawn", game.mechanics.klutzReturnToDawn)
        put(
            ClocktowerGhostVoteAuthorityPersistence.ROOT_KEY,
            ClocktowerGhostVoteAuthorityPersistence.encode(game.mechanics.ghostVoteAuthority),
        )
        putNullableString("clocktowerHighestVoteName", game.mechanics.highestVoteName)
        put("clocktowerHighestVoteCount", game.mechanics.highestVoteCount.coerceAtLeast(0))
        put("clocktowerEvents", AppGameStateJsonCodec.encodeEvents(game.history.events))
        put("clocktowerEpistemicObservations", encodeObservations(game.history.epistemicObservations))
        game.history.causalDecisionJournal?.let { archive ->
            put(ClocktowerCausalJournalPersistence.ROOT_KEY, ClocktowerCausalJournalPersistence.encode(archive))
        }
    }

    private fun encodeStorytellerPlayerContext(
        values: Map<Int, StorytellerPlayerContextInputV1>,
    ): JSONArray = JSONArray().apply {
        values.toSortedMap().forEach { (seat, input) ->
            put(JSONObject().apply {
                put("seat", seat)
                put("experienceLevel", input.experienceLevel.name)
                put(
                    "claimedRoleIds",
                    stringsToJsonArray(input.claimedRoleIds.map { it.value }),
                )
                putNullableString("pressureLevel", input.pressureLevel?.name)
            })
        }
    }

    private fun encodeObservations(
        observations: List<com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation>,
    ): JSONArray = JSONArray().apply {
        observations.forEach { observation ->
            put(JSONObject(EpistemicSemanticJson.encode(observation)))
        }
    }
}
