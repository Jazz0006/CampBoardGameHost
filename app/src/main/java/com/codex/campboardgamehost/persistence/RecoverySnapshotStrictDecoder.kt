package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import org.json.JSONArray
import org.json.JSONObject

/**
 * Strict typed reader for the current emergency-recovery schema.
 *
 * Unlike the legacy active-game reader, this boundary never skips malformed collection entries,
 * invents enum defaults, or repairs partially missing durable facts. A payload either becomes one
 * complete typed [RecoverySnapshot] or fails before any application state can be mutated.
 */
internal object RecoverySnapshotStrictDecoder {
    fun decode(
        json: JSONObject,
        roleByName: (String) -> ClocktowerRole?,
    ): RecoverySnapshot {
        val formatVersion = json.requiredInt(RecoverySnapshotJsonCodec.FORMAT_VERSION_KEY)
        val compatibilityToken = json.requiredNonBlankString(RecoverySnapshotJsonCodec.COMPATIBILITY_TOKEN_KEY)
        val savedAtMillis = json.requiredLong("savedAtMillis")
        val gameKind = json.requiredEnum<GameKind>("currentGameKind")
        val entryPoint = json.strictEntryPoint()
        val currentDealIndex = json.requiredInt("currentDealIndex")
        val round = json.requiredInt("round")
        val cards = json.requiredArray("cards").decodeCardsStrict(roleByName)
        val records = json.requiredArray("records").decodeRecordsStrict()
        val outcome = json.decodeOutcomeStrict("gameOutcome")
        val setupRotationRecord = TroubleBrewingSetupCompletionPersistence.decodeOrNull(json)

        val game: RecoveryGame = when (gameKind) {
            GameKind.Undercover -> {
                require(setupRotationRecord == null) {
                    "Undercover recovery cannot carry Clocktower setup metadata."
                }
                UndercoverRecovery(
                    entryPoint = entryPoint,
                    currentDealIndex = currentDealIndex,
                    round = round,
                    cards = cards,
                    records = records,
                    outcome = outcome,
                )
            }

            GameKind.Clocktower -> decodeClocktower(
                json = json,
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
                round = round,
                cards = cards,
                records = records,
                outcome = outcome,
                setupRotationRecord = setupRotationRecord,
            )
        }

        return RecoverySnapshot(
            recoveryFormatVersion = formatVersion,
            compatibilityToken = compatibilityToken,
            savedAtMillis = savedAtMillis,
            game = game,
        )
    }

    private fun decodeClocktower(
        json: JSONObject,
        entryPoint: RecoveryEntryPoint,
        currentDealIndex: Int,
        round: Int,
        cards: List<PlayerCard>,
        records: List<EliminationRecord>,
        outcome: GameOutcome?,
        setupRotationRecord: TroubleBrewingSetupRotationRecord?,
    ): ClocktowerRecovery {
        val phase = json.requiredEnum<ClocktowerPhase>("clocktowerPhase")
        val gameStateRevision = json.requiredLong("clocktowerGameStateRevision")
        val playerInputRevision = json.requiredLong("clocktowerPlayerInputRevision")
        val storytellerPlayerContextBySeat =
            json.requiredArray("clocktowerStorytellerPlayerContext").decodeStorytellerPlayerContextStrict()
        val actionTimeline = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(json)
        val epistemicObservations = json.requiredArray("clocktowerEpistemicObservations")
            .decodeEpistemicObservationsStrict()
        val nextTimelineGlobalSequence = ClocktowerSemanticHistoryPersistence.deriveNextTimelineGlobalSequence(
            actionTimeline = actionTimeline,
            observations = epistemicObservations,
        )

        return ClocktowerRecovery(
            entryPoint = entryPoint,
            currentDealIndex = currentDealIndex,
            round = round,
            cards = cards,
            records = records,
            outcome = outcome,
            identity = ClocktowerRecoveryIdentity(
                script = json.requiredEnum("currentClocktowerScript"),
                gameId = json.requiredNonBlankString("clocktowerGameId"),
                gameSeed = json.requiredLong("clocktowerGameSeed"),
            ),
            troubleBrewingSetupRotationRecord = setupRotationRecord,
            sdeHistoricalReplayInputJson =
                if (!json.has("clocktowerSdeHistoricalReplayInput") ||
                    json.isNull("clocktowerSdeHistoricalReplayInput")
                ) {
                    null
                } else {
                    json.requiredNonBlankString("clocktowerSdeHistoricalReplayInput")
                },
            position = ClocktowerRecoveryPosition(
                phase = phase,
                nightStarted = json.requiredBoolean("clocktowerNightStarted"),
                nightStepIndex = json.requiredInt("clocktowerNightStepIndex"),
            ),
            mechanics = ClocktowerRecoveryMechanics(
                confirmedAttackTarget = json.requiredNullableString("clocktowerPendingNightDeath"),
                confirmedPoisonTarget = json.requiredNullableString("clocktowerConfirmedPoisonTarget"),
                confirmedMonkProtectedTarget = json.requiredNullableString("clocktowerConfirmedMonkProtectedTarget"),
                confirmedMayorRedirectTarget = json.requiredNullableString("clocktowerConfirmedMayorRedirectTarget"),
                pendingNewDemonName = json.requiredNullableString("clocktowerPendingNewDemonName"),
                pendingNightNewDemonIdentityName = json.requiredNullableString("clocktowerPendingNightNewDemonIdentityName"),
                confirmedDemonSuccessorTarget = json.requiredNullableString("clocktowerConfirmedDemonSuccessorTarget"),
                redHerring = json.requiredNullableString("clocktowerRedHerring"),
                demonBluffRoleNames = json.requiredArray("clocktowerRecommendedDemonBluffRoleNames").strictStringList(),
                butlerMaster = json.requiredNullableString("clocktowerButlerMaster"),
                virginUsed = json.requiredBoolean("clocktowerVirginUsed"),
                slayerUsed = json.requiredBoolean("clocktowerSlayerUsed"),
                slayerClaimedNames = json.requiredArray("clocktowerSlayerClaimedNames").strictStringList(),
                artistUsed = json.requiredBoolean("clocktowerArtistUsed"),
                artistClaimedNames = json.requiredArray("clocktowerArtistClaimedNames").strictStringList(),
                lastExecutedName = json.requiredNullableString("clocktowerLastExecutedName"),
                pendingKlutzName = json.requiredNullableString("clocktowerPendingKlutzName"),
                klutzReturnToDawn = json.requiredBoolean("clocktowerKlutzReturnToDawn"),
                ghostVoteAuthority = json.decodeGhostVoteAuthorityStrict(),
                highestVoteName = json.requiredNullableString("clocktowerHighestVoteName"),
                highestVoteCount = json.requiredInt("clocktowerHighestVoteCount"),
            ),
            history = ClocktowerRecoveryHistory(
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                storytellerPlayerContextBySeat = storytellerPlayerContextBySeat,
                actionTimeline = actionTimeline,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                events = json.requiredArray("clocktowerEvents").decodeEventsStrict(),
                epistemicObservations = epistemicObservations,
                causalDecisionJournal = if (json.has(ClocktowerCausalJournalPersistence.ROOT_KEY)) {
                    ClocktowerCausalJournalPersistence.decode(
                        value = json.getJSONObject(ClocktowerCausalJournalPersistence.ROOT_KEY),
                        expectedGameId = json.requiredNonBlankString("clocktowerGameId"),
                        actions = actionTimeline,
                        observations = epistemicObservations,
                        recoveredCursor = nextTimelineGlobalSequence,
                    )
                } else null,
            ),
        )
    }

    private fun JSONObject.strictEntryPoint(): RecoveryEntryPoint {
        if (!has("screen")) return RecoveryEntryPoint.Stable
        require(!isNull("screen")) { "screen cannot be null when present." }
        return when (requiredString("screen")) {
            "PassPhone" -> RecoveryEntryPoint.PassPhone
            "RevealCard" -> RecoveryEntryPoint.RevealCard
            else -> throw IllegalArgumentException("Unsupported recovery screen.")
        }
    }

    private fun JSONArray.decodeCardsStrict(
        roleByName: (String) -> ClocktowerRole?,
    ): List<PlayerCard> = buildList {
        for (index in 0 until length()) {
            val card = opt(index) as? JSONObject
                ?: throw IllegalArgumentException("cards[$index] must be an object.")
            val clocktowerRole = card.requiredNullableString("clocktowerRole")?.let { name ->
                roleByName(name) ?: throw IllegalArgumentException("Unknown Clocktower role '$name'.")
            }
            val shownRole = card.requiredNullableString("clocktowerShownRole")?.let { name ->
                roleByName(name) ?: throw IllegalArgumentException("Unknown shown Clocktower role '$name'.")
            }
            add(
                PlayerCard(
                    name = card.requiredNonBlankString("name"),
                    role = card.requiredEnum("role"),
                    word = card.requiredString("word"),
                    roleLabel = card.requiredNullableString("roleLabel"),
                    actualRoleLabel = card.requiredNullableString("actualRoleLabel"),
                    clocktowerTeam = card.requiredNullableEnum<ClocktowerTeam>("clocktowerTeam") ?: clocktowerRole?.team,
                    clocktowerRole = clocktowerRole,
                    clocktowerShownRole = shownRole,
                    eliminatedRound = card.requiredNullableInt("eliminatedRound"),
                ),
            )
        }
    }

    private fun JSONArray.decodeRecordsStrict(): List<EliminationRecord> = buildList {
        for (index in 0 until length()) {
            val record = opt(index) as? JSONObject
                ?: throw IllegalArgumentException("records[$index] must be an object.")
            add(
                EliminationRecord(
                    round = record.requiredInt("round"),
                    playerName = record.requiredNonBlankString("playerName"),
                    note = record.requiredNullableString("note"),
                ),
            )
        }
    }

    private fun JSONObject.decodeOutcomeStrict(key: String): GameOutcome? {
        require(has(key)) { "$key is required." }
        if (isNull(key)) return null
        val outcome = opt(key) as? JSONObject
            ?: throw IllegalArgumentException("$key must be an object or null.")
        return GameOutcome(
            title = outcome.requiredNonBlankString("title"),
            summary = outcome.requiredString("summary"),
            reason = outcome.requiredString("reason"),
        )
    }

    private fun JSONArray.decodeEventsStrict(): List<ClocktowerEvent> = buildList {
        for (index in 0 until length()) {
            val event = opt(index) as? JSONObject
                ?: throw IllegalArgumentException("clocktowerEvents[$index] must be an object.")
            add(
                ClocktowerEvent(
                    sequence = event.requiredInt("sequence"),
                    type = event.requiredEnum("type"),
                    title = event.requiredNonBlankString("title"),
                    detail = event.requiredString("detail"),
                    playerNames = event.requiredArray("playerNames").strictStringList(),
                    phase = event.requiredEnum("phase"),
                    round = event.requiredInt("round"),
                ),
            )
        }
    }

    private fun JSONArray.decodeStorytellerPlayerContextStrict():
        Map<Int, StorytellerPlayerContextInputV1> = buildMap {
        for (index in 0 until length()) {
            val entry = opt(index) as? JSONObject
                ?: throw IllegalArgumentException(
                    "clocktowerStorytellerPlayerContext[$index] must be an object.",
                )
            val seat = entry.requiredInt("seat")
            require(seat > 0) { "Storyteller player-context seat must be positive." }
            require(seat !in this) { "Storyteller player-context seats must be unique." }
            val claimedRoleIds = entry.requiredArray("claimedRoleIds")
                .strictStringList()
                .map(::RoleId)
            require(claimedRoleIds.distinct().size == claimedRoleIds.size) {
                "Storyteller player-context claimed roles must be unique."
            }
            val value = StorytellerPlayerContextInputV1(
                experienceLevel = entry.requiredEnum<PlayerExperienceLevelV1>("experienceLevel"),
                claimedRoleIds = claimedRoleIds,
                pressureLevel = entry.requiredNullableEnum<StorytellerDeclaredPressureLevelV1>(
                    "pressureLevel",
                ),
            )
            require(!value.isDefault) {
                "Recovery stores only non-default Storyteller player-context overrides."
            }
            put(seat, value)
        }
    }

    private fun JSONArray.decodeEpistemicObservationsStrict() = buildList {
        for (index in 0 until length()) {
            val observation = opt(index) as? JSONObject
                ?: throw IllegalArgumentException("clocktowerEpistemicObservations[$index] must be an object.")
            add(EpistemicSemanticJson.decodeRecordedEpistemicObservation(observation.toString()))
        }
    }

    private fun JSONObject.decodeGhostVoteAuthorityStrict(): ClocktowerGhostVoteAuthority {
        val values = requiredArray(ClocktowerGhostVoteAuthorityPersistence.ROOT_KEY)
        val seatNumbers = buildList {
            for (index in 0 until values.length()) {
                val raw = values.opt(index)
                require(raw is Byte || raw is Short || raw is Int || raw is Long) {
                    "Ghost vote seat $index must be an integer."
                }
                val number = (raw as Number).toLong()
                require(number in 1L..Int.MAX_VALUE.toLong()) { "Ghost vote seats must be positive Int values." }
                add(number.toInt())
            }
        }
        require(seatNumbers.distinct().size == seatNumbers.size) { "Ghost vote seats must be unique." }
        return ClocktowerGhostVoteAuthority(seatNumbers.map(::ClocktowerSeatId).toSet())
    }

    private fun JSONArray.strictStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            val value = opt(index) as? String
                ?: throw IllegalArgumentException("Array entry $index must be a string.")
            require(value.isNotBlank()) { "String array entries cannot be blank." }
            add(value)
        }
    }

    private fun JSONObject.requiredArray(key: String): JSONArray {
        require(has(key) && !isNull(key)) { "$key is required." }
        return opt(key) as? JSONArray ?: throw IllegalArgumentException("$key must be an array.")
    }

    private fun JSONObject.requiredString(key: String): String {
        require(has(key) && !isNull(key)) { "$key is required." }
        return opt(key) as? String ?: throw IllegalArgumentException("$key must be a string.")
    }

    private fun JSONObject.requiredNonBlankString(key: String): String = requiredString(key).also {
        require(it.isNotBlank()) { "$key cannot be blank." }
    }

    private fun JSONObject.requiredNullableString(key: String): String? {
        require(has(key)) { "$key is required." }
        if (isNull(key)) return null
        return requiredString(key)
    }

    private fun JSONObject.requiredBoolean(key: String): Boolean {
        require(has(key) && !isNull(key)) { "$key is required." }
        return opt(key) as? Boolean ?: throw IllegalArgumentException("$key must be a boolean.")
    }

    private fun JSONObject.requiredInt(key: String): Int {
        val value = requiredIntegralNumber(key).toLong()
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) { "$key is outside Int range." }
        return value.toInt()
    }

    private fun JSONObject.requiredLong(key: String): Long = requiredIntegralNumber(key).toLong()

    private fun JSONObject.requiredNullableInt(key: String): Int? {
        require(has(key)) { "$key is required." }
        if (isNull(key)) return null
        return requiredInt(key)
    }

    private fun JSONObject.requiredIntegralNumber(key: String): Number {
        require(has(key) && !isNull(key)) { "$key is required." }
        val raw = opt(key)
        require(raw is Byte || raw is Short || raw is Int || raw is Long) { "$key must be an integer." }
        return raw as Number
    }

    private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(key: String): T {
        val raw = requiredString(key)
        return enumValues<T>().firstOrNull { it.name == raw }
            ?: throw IllegalArgumentException("Unknown $key '$raw'.")
    }

    private inline fun <reified T : Enum<T>> JSONObject.requiredNullableEnum(key: String): T? {
        val raw = requiredNullableString(key) ?: return null
        return enumValues<T>().firstOrNull { it.name == raw }
            ?: throw IllegalArgumentException("Unknown $key '$raw'.")
    }
}
