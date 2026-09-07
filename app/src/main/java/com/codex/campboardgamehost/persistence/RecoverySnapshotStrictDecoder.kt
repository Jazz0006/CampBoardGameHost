package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import org.json.JSONArray
import org.json.JSONObject

/**
 * Strict PS3 reader for the reduced emergency-recovery schema.
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
        val activeGameStateVersion = json.requiredInt("version")
        val savedAtMillis = json.requiredLong("savedAtMillis")
        val gameKind = json.requiredEnum<GameKind>("currentGameKind")
        val entryPoint = json.strictEntryPoint()
        val currentDealIndex = json.requiredInt("currentDealIndex")
        val round = json.requiredInt("round")
        val cards = json.requiredArray("cards").decodeCardsStrict(roleByName)
        val records = json.requiredArray("records").decodeRecordsStrict()
        val outcome = json.decodeOutcomeStrict("gameOutcome")
        val identity = PersistedActiveGameIdentityJsonCodec.decode(
            json.requiredObject(PersistedActiveGameIdentityJsonCodec.ROOT_KEY),
        )
        require(identity.gameKind == gameKind) {
            "Recovery game kind does not match persisted content identity."
        }

        val committedSetup = CommittedClocktowerSetupPersistence.decodeOrNull(json)
        val setupRotationRecord = TroubleBrewingSetupCompletionPersistence.decodeOrNull(json)
        val clocktowerRulesetRoleIds = json.decodeRulesetRoleIdsStrict()
        val clocktowerRulesetRef = json.decodeRulesetRefStrict()

        val legacy = LegacyRestoreCompatibility(
            activeGameStateVersion = activeGameStateVersion,
            identity = identity,
            committedClocktowerSetup = committedSetup,
            troubleBrewingSetupRotationRecord = setupRotationRecord,
            clocktowerRulesetRoleIds = clocktowerRulesetRoleIds,
            clocktowerRulesetRef = clocktowerRulesetRef,
        )

        val game: RecoveryGame = when (gameKind) {
            GameKind.Undercover -> {
                require(committedSetup == null && setupRotationRecord == null) {
                    "Undercover recovery cannot carry Clocktower setup metadata."
                }
                require(clocktowerRulesetRoleIds.isEmpty() && clocktowerRulesetRef == null) {
                    "Undercover recovery cannot carry Clocktower ruleset metadata."
                }
                UndercoverRecovery(
                    entryPoint = entryPoint,
                    currentDealIndex = currentDealIndex,
                    round = round,
                    cards = cards,
                    records = records,
                    outcome = outcome,
                    undercoverCount = json.requiredInt("undercoverCount"),
                    includeBlank = json.requiredBoolean("includeBlank"),
                    lastWordsMode = json.requiredEnum("lastWordsMode"),
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
            )

            GameKind.Werewolf -> throw IllegalArgumentException(
                "Werewolf is outside the PS3 typed recovery support surface.",
            )
        }

        return RecoverySnapshot(
            recoveryFormatVersion = formatVersion,
            compatibilityToken = compatibilityToken,
            savedAtMillis = savedAtMillis,
            legacyRestoreCompatibility = legacy,
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
    ): ClocktowerRecovery {
        val phase = json.requiredEnum<ClocktowerPhase>("clocktowerPhase")
        val gameStateRevision = json.requiredLong("clocktowerGameStateRevision")
        val playerInputRevision = json.requiredLong("clocktowerPlayerInputRevision")
        val semanticHistoryMode = ClocktowerSemanticHistoryPersistence.decodeMode(json)
        val actionTimeline = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(json)
        val nextTimelineGlobalSequence = json.requiredLong(ClocktowerSemanticHistoryPersistence.CURSOR_KEY)

        json.requireNull("clocktowerDemonAttackDraftTarget")
        json.requireNull("clocktowerPoisonTarget")
        json.requireNull("clocktowerMonkProtectedTarget")
        json.requireNull("clocktowerMayorRedirectTarget")
        json.requireNull("clocktowerDemonSuccessorTarget")

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
                klutzChoiceName = json.requiredNullableString("clocktowerKlutzChoiceName"),
                klutzReturnToDawn = json.requiredBoolean("clocktowerKlutzReturnToDawn"),
                ghostVoteAuthority = json.decodeGhostVoteAuthorityStrict(),
                highestVoteName = json.requiredNullableString("clocktowerHighestVoteName"),
                highestVoteCount = json.requiredInt("clocktowerHighestVoteCount"),
            ),
            history = ClocktowerRecoveryHistory(
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                semanticHistoryMode = semanticHistoryMode,
                actionTimeline = actionTimeline,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                events = json.requiredArray("clocktowerEvents").decodeEventsStrict(),
                epistemicObservations = json.requiredArray("clocktowerEpistemicObservations")
                    .decodeEpistemicObservationsStrict(),
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

    private fun JSONObject.decodeRulesetRoleIdsStrict(): Set<RoleId> {
        if (!has("clocktowerRulesetRoleIds")) return emptySet()
        if (isNull("clocktowerRulesetRoleIds")) return emptySet()
        val raw = opt("clocktowerRulesetRoleIds")
        require(raw is JSONArray) { "clocktowerRulesetRoleIds must be an array or null." }
        return ClocktowerRulesetPersistenceBasisJsonCodec.decode(raw).roleIds
    }

    private fun JSONObject.decodeRulesetRefStrict(): RulesetRef? {
        if (!has("clocktowerRulesetRef")) return null
        if (isNull("clocktowerRulesetRef")) return null
        val raw = opt("clocktowerRulesetRef")
        require(raw is JSONObject) { "clocktowerRulesetRef must be an object or null." }
        return RulesetRef(
            scriptId = ScriptId(raw.requiredNonBlankString("scriptId")),
            scriptContentHash = raw.requiredNonBlankString("scriptContentHash"),
            rulesetVersion = raw.requiredNonBlankString("rulesetVersion"),
            sourceRevision = raw.requiredNonBlankString("sourceRevision"),
            coverage = raw.requiredEnum<RuleCoverage>("coverage"),
        )
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
                    clocktowerTeam = card.requiredNullableEnum("clocktowerTeam") ?: clocktowerRole?.team,
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

    private fun JSONObject.requireNull(key: String) {
        require(has(key)) { "$key is required." }
        require(isNull(key)) { "$key must be null in typed recovery; draft interaction state is not durable." }
    }

    private fun JSONObject.requiredObject(key: String): JSONObject {
        require(has(key) && !isNull(key)) { "$key is required." }
        return opt(key) as? JSONObject ?: throw IllegalArgumentException("$key must be an object.")
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
