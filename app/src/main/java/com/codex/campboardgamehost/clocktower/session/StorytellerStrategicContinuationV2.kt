package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktowerRolesForScript
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import org.json.JSONArray
import org.json.JSONObject

/**
 * Generic whole-game continuation, NOT a Poisoner/Drunk/FT recommender.
 *
 * Uses one frozen Host session prefix and the prior *advisory* multi-night memory.
 * The request carries the one current Host decision with the results the Session rules
 * allow; the model picks one and may revise its cross-night intentions.
 * It cannot submit an observation, register a witness or commit a game action.
 */
internal object StorytellerStrategicContinuationV2 {
    const val REQUEST_SCHEMA = "botc.whole-game-strategy-continuation-v4"
    private const val MAX_REASON_LENGTH = 600

    internal data class OpenDecision(
        val decisionId: String,
        val sourceSeat: Int?,
        val family: String,
        val abilityRoleId: String? = null,
        val currentDecisionContext: StorytellerProviderDecisionContextV1? = null,
    ) {
        init {
            require(decisionId.isNotBlank() && family.isNotBlank())
            require(sourceSeat == null || sourceSeat > 0)
        }
    }

    /**
     * Game-scoped client-owned advisory policy cursor. Each accepted continuation
     * replaces the previous memory; later requests do not restart from night one.
     * This is NOT Host canonical state and grants no decision/publication authority.
     */
    internal data class MemoryCursor(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val version: Int,
        val memory: HostOpeningStrategyMemoryV2,
    ) {
        init {
            require(gameId.isNotBlank() && version >= 0)
            require(gameStateRevision >= 0 && playerInputRevision >= 0 &&
                exclusiveGlobalSequence >= 0)
        }

        fun advance(accepted: Result): MemoryCursor {
            require(accepted.gameId == gameId && accepted.memoryVersion == version)
            require(accepted.gameStateRevision >= gameStateRevision &&
                accepted.playerInputRevision >= playerInputRevision &&
                accepted.exclusiveGlobalSequence >= exclusiveGlobalSequence)
            require(accepted.gameStateRevision > gameStateRevision ||
                accepted.playerInputRevision > playerInputRevision ||
                accepted.exclusiveGlobalSequence > exclusiveGlobalSequence) {
                "Do not advance strategy without any new Host change."
            }
            return MemoryCursor(
                gameId, accepted.gameStateRevision, accepted.playerInputRevision,
                accepted.exclusiveGlobalSequence, version + 1, accepted.updatedMemory)
        }

        /**
         * Promote a provisional reply ONLY after a genuinely new confirmed player
         * observation. Retain the pre-publication history cursor so the actual
         * observed fact is sent to the next model call rather than assumed known.
         * The updated memory is advisory; it never overwrites Session truth.
         */
        fun afterConfirmedPublication(
            proposal: Result,
            before: ClocktowerSessionState,
            after: ClocktowerSessionState,
        ): MemoryCursor {
            require(proposal.gameId == gameId && before.gameId == gameId &&
                after.gameId == gameId && proposal.memoryVersion == version)
            require(proposal.gameStateRevision == before.gameStateRevision &&
                proposal.playerInputRevision == before.playerInputRevision &&
                proposal.exclusiveGlobalSequence == before.nextTimelineGlobalSequence)
            require(before.gameStateRevision >= gameStateRevision &&
                before.playerInputRevision >= playerInputRevision &&
                before.nextTimelineGlobalSequence >= exclusiveGlobalSequence)
            require(after.gameStateRevision >= before.gameStateRevision &&
                after.playerInputRevision > before.playerInputRevision &&
                after.nextTimelineGlobalSequence > before.nextTimelineGlobalSequence)
            require(after.epistemicObservationLog.records.size >
                before.epistemicObservationLog.records.size &&
                after.epistemicObservationLog.records.take(
                    before.epistemicObservationLog.records.size) ==
                    before.epistemicObservationLog.records)
            // This is the request's frozen history prefix, not the later published
            // fact; the next continuation will read the actual Session delta.
            return MemoryCursor(
                gameId, before.gameStateRevision, before.playerInputRevision,
                before.nextTimelineGlobalSequence, version + 1, proposal.updatedMemory)
        }

        companion object {
            fun initial(accepted: HostOpeningSemanticAcceptanceV2): MemoryCursor =
                MemoryCursor(
                    accepted.selection.gameId, accepted.selection.gameStateRevision,
                    accepted.selection.playerInputRevision, 0, 0,
                    requireNotNull(accepted.strategyMemory))
        }
    }

    internal data class Result(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val memoryVersion: Int,
        /** Atomic *unpublished* suggestions; never a Host action or observation. */
        val remainingChoices: Map<String, SemanticResult> = emptyMap(),
        val updatedMemory: HostOpeningStrategyMemoryV2,
        val strategicDirection: String,
    )

    private fun str() = JSONObject().put("type", "string")
    private fun nullable(type: String) = JSONObject().put("type", JSONArray(listOf(type, "null")))
    private fun arr(item: JSONObject) = JSONObject().put("type", "array").put("items", item)
    private fun obj(vararg members: Pair<String, JSONObject>): JSONObject = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject().apply { members.forEach { (k, v) -> put(k, v) } })
        .put("required", JSONArray(members.map { it.first }))
        .put("additionalProperties", false)

    val responseSchema: JSONObject by lazy {
        obj(
            "answer" to obj(
                "value" to nullable("string"),
                "roleId" to nullable("string"),
                "seats" to arr(JSONObject().put("type", "integer")),
                "seat" to nullable("integer"),
            ),
            "reason" to str(),
            "intentions" to JSONObject()
                .put("type", JSONArray(listOf("array", "null")))
                .put("items", obj("trigger" to str(), "approach" to str()))
                .put("maxItems", HostOpeningStrategyMemoryV2.MAX_ENTRIES),
        )
    }

    const val INSTRUCTIONS = """
你是同一局 Blood on the Clocktower（Trouble Brewing）的资深说书人，延续开局时的整体思路，为当前这一个待定事项给出推荐。
Host 负责规则与合法性：currentDecision.candidates 列出的都是合法结果，你只需从中选出对整局最好的一个。

目标与开局相同：让双方在符合各自经验的局面中靠交流、推理和欺骗决定胜负。
善良方的信息应当可用，邪恶方应保有撑得住的故事，各夜线索连起来可以解释。

输入：
- state：座位、实际与展示身份、存活、中毒、经验和已记录的公开声明，以及红鲱鱼与恶魔掩护身份。
- gameLogDelta：按夜/天标记的已确认记录。连续会话时只含上次之后的新增，否则是本局全部记录。
  没有记录的事视为未知；玩家声明不等于事实。
- priorStrategyMemory：此前的跨夜意向，只是可修订的设想，以已确认事实为准。
- currentDecision：当前唯一要决定的事项。reliability 表示该信息位是否正常运作，candidates 是合法结果。

回答：
- answer 只填与本事项对应的字段，其余填 null 或 []。
  数字或真假：value 取 candidates.values 中的一个，写成字符串，如 "1"、"true"。
  查看某玩家的身份：value 取 candidates.roles 中的一个身份。
  “某身份在两名玩家之一”：填 roleId 与 seats（两个座位）。candidates.truthful 的每一项给出身份和它所在的座位，
  seats 为该座位加上除信息位本人外的任意一个座位；candidates.mayRegister 的座位可以被当作所列任一身份；
  candidates.anyPair 为 true 时，可用 candidates.roles 中任一身份和除本人外的任意两个座位；
  candidates.none 为 true 时，可用 roleId=null、seats=[] 表示没有这类角色。
  选择座位：seat 取 candidates.seats 中的一个。
- reason：一两句话说明为什么这样选，区分事实与假设，不超过 150 字。
- intentions：跨夜意向需要调整时，给出调整后的完整列表（最多 ${HostOpeningStrategyMemoryV2.MAX_ENTRIES} 条，trigger 与 approach 各不超过 100 字）；
  不需要调整时填 null。
"""

    /** Caller must pass decisions from Host's current actual decision inventory, not model text. */
    fun encode(
        session: ClocktowerSessionState,
        prior: HostOpeningSemanticAcceptanceV2,
        openDecisions: List<OpenDecision>,
        ruleset: ValidatedClocktowerRuleset,
        cursor: MemoryCursor = MemoryCursor.initial(prior),
        historyFromGlobalSequence: Long = cursor.exclusiveGlobalSequence,
    ): String {
        require(prior.selection.gameId == session.gameId && cursor.gameId == session.gameId)
        val memory = cursor.memory
        require(session.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(session.gameStateRevision >= cursor.gameStateRevision &&
            session.playerInputRevision >= cursor.playerInputRevision &&
            session.nextTimelineGlobalSequence >= cursor.exclusiveGlobalSequence)
        val seats = session.gameState.players.sortedBy { it.seat }
        require(seats.map { it.seat } == (1..seats.size).toList())
        require(openDecisions.size == 1) {
            "Only the current actual Host decision may be submitted for continuation."
        }
        val currentDecision = openDecisions.single()
        require(currentDecision.sourceSeat == null ||
            currentDecision.sourceSeat in 1..seats.size)
        val prefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            session, StorytellerProviderRevisionV1(
                session.gameStateRevision, session.playerInputRevision))
        require(prefix.cutoffSource == StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED)
        val cutoff = requireNotNull(prefix.exclusiveGlobalSequence)
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            // Host bookkeeping for binding the answer to this request; the model does not echo it.
            .put("source", JSONObject()
                .put("gameId", session.gameId)
                .put("gameStateRevision", session.gameStateRevision)
                .put("playerInputRevision", session.playerInputRevision)
                .put("exclusiveGlobalSequence", cutoff)
                .put("memoryVersion", cursor.version))
            .put("state", JSONObject()
                .put("scriptId", session.gameState.script.value)
                .put("roleIds", JSONArray(clocktowerRolesForScript(ClocktowerScript.entries.single {
                    it.toRecommendationScriptId() == session.gameState.script
                }).map { it.enName }))
                .put("redHerringSeat", session.redHerringSeat ?: JSONObject.NULL)
                .put("demonBluffRoleIds", JSONArray(session.demonBluffRoleIds.map(RoleId::value)))
                .put("seats", JSONArray().also { array ->
                    seats.forEach { player ->
                        val context = session.storytellerPlayerContextBySeat[player.seat]
                        array.put(JSONObject()
                            .put("seat", player.seat)
                            .put("actualRoleId", player.actualRole.value)
                            .put("shownRoleId", (player.shownRole ?: player.actualRole).value)
                            .put("alive", player.alive).put("poisoned", player.poisoned)
                            .put("experience", (context ?: StorytellerPlayerContextInputV1.DEFAULT).experienceLevel.name)
                            .put("claimedRoleIds", JSONArray(context?.claimedRoleIds?.map { it.value }
                                ?: emptyList<String>())))
                    }
                }))
            .put("priorStrategyMemory", memory.toJson())
            .put("currentDecision", JSONObject()
                .put("decisionId", currentDecision.decisionId)
                .put("family", currentDecision.family)
                .put("sourceSeat", currentDecision.sourceSeat ?: JSONObject.NULL)
                .put("abilityRoleId", currentDecision.abilityRoleId ?: JSONObject.NULL)
                .put("decisionContext", contextMeaning(currentDecision.currentDecisionContext))
                .put("candidates", candidates(session, ruleset, currentDecision.currentDecisionContext)))
            .put("gameLogDelta", StorytellerGlobalEventProjectionV1.encode(
                prefix, historyFromGlobalSequence, session.gameState))
            .toString()
    }

    /**
     * The results the Session rules allow for the current question, read from the same
     * resolvers the night step uses. Null when no resolver covers the question.
     */
    private fun candidates(
        session: ClocktowerSessionState,
        ruleset: ValidatedClocktowerRuleset,
        context: StorytellerProviderDecisionContextV1?,
    ): Any = when (context) {
        is StorytellerProviderDecisionContextV1.ScalarInformation -> when (context.kind) {
            StorytellerProviderScalarKindV1.NUMBER ->
                runCatching { NumericMetric.valueOf(context.metric) }.getOrNull()?.let { metric ->
                    NumericInformationResolver.resolve(session, metric, context.sourceSeat)
                        ?: NumericInformationResolver.resolveLaterNight(
                            session, ruleset, metric, context.sourceSeat, context.subjectSeats)
                }?.let { JSONObject().put("values", JSONArray(it.values.map(Int::toString))) }
            StorytellerProviderScalarKindV1.BOOLEAN -> FortuneTellerInformationResolver
                .resolve(session, context.sourceSeat, context.subjectSeats)
                ?.let { JSONObject().put("values", JSONArray(it.values.map(Boolean::toString))) }
            StorytellerProviderScalarKindV1.ROLE -> RoleInformationResolver.resolve(
                session, ruleset, context.abilityRole.value, context.sourceSeat,
                context.subjectSeats.singleOrNull(),
            )?.let { JSONObject().put("roles", JSONArray(it.roles.map(RoleId::value))) }
        }
        is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> PairInformationResolver
            .choices(session, ruleset, context.abilityRole, context.sourceSeat)?.let { choices ->
                val json = JSONObject()
                choices.anyPairRoles?.let {
                    json.put("anyPair", true).put("roles", JSONArray(it.map(RoleId::value)))
                }
                if (choices.truthful.isNotEmpty()) json.put("truthful", JSONArray().also { items ->
                    choices.truthful.forEach { (role, seat) ->
                        items.put(JSONObject().put("role", role.value).put("seat", seat))
                    }
                })
                if (choices.registrations.isNotEmpty()) json.put("mayRegister", JSONArray().also { items ->
                    choices.registrations.forEach { (seat, roles) ->
                        items.put(JSONObject().put("seat", seat)
                            .put("roles", JSONArray(roles.map(RoleId::value))))
                    }
                })
                if (choices.canShowNoOutsiders) json.put("none", true)
                json
            }
        is StorytellerProviderDecisionContextV1.MayorRedirect -> JSONObject().put("seats", JSONArray(
            (session.mayorRedirectEligibleSeats() + context.mayorSeat)
                .filter { it in session.allSeats() }.sorted()))
        else -> null
    } ?: JSONObject.NULL

    /** A direct recommendation. It asserts no Host truth verdict or registration witness. */
    internal data class SemanticResult(
        val kind: String,
        val scalarValue: String? = null,
        val pairRoleId: String? = null,
        val pairSeats: List<Int> = emptyList(),
        val seat: Int? = null,
        val registrationStatus: String? = null,
        val registrationRoleId: String? = null,
    ) {
        fun validate(context: StorytellerProviderDecisionContextV1?, seatCount: Int) {
            val seats = 1..seatCount
            require(pairSeats.distinct().size == pairSeats.size && pairSeats.all { it in seats })
            when (context) {
                is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> {
                    require(kind == "PAIR_RESULT" && scalarValue == null && seat == null &&
                        registrationStatus == null && registrationRoleId == null)
                    require(if (pairRoleId == null) pairSeats.isEmpty()
                        else pairRoleId.isNotBlank() && pairSeats.size == 2)
                }
                is StorytellerProviderDecisionContextV1.ScalarInformation -> {
                    require(kind == "SCALAR_RESULT" && pairRoleId == null && pairSeats.isEmpty() &&
                        seat == null && registrationStatus == null && registrationRoleId == null)
                    require(when (context.kind) {
                        StorytellerProviderScalarKindV1.NUMBER ->
                            scalarValue?.toIntOrNull()?.let { it >= 0 && it.toString() == scalarValue } == true
                        StorytellerProviderScalarKindV1.BOOLEAN ->
                            scalarValue in setOf("true", "false")
                        StorytellerProviderScalarKindV1.ROLE ->
                            context.subjectSeats.size == 1 && !scalarValue.isNullOrBlank()
                    })
                }
                is StorytellerProviderDecisionContextV1.MayorRedirect ->
                    require(kind == "SEAT_TARGET" && seat in seats && scalarValue == null &&
                        pairRoleId == null && pairSeats.isEmpty() && registrationStatus == null &&
                        registrationRoleId == null)
                else -> error("Unsupported current information response context")
            }
        }

        companion object {
            /** The current Host question decides which answer fields count; the rest are ignored. */
            fun fromModel(raw: JSONObject, context: StorytellerProviderDecisionContextV1?): SemanticResult {
                require(raw.keys().asSequence().toSet() == setOf("value", "roleId", "seats", "seat"))
                fun optionalString(key: String) = if (raw.isNull(key)) null else raw.getString(key)
                fun exactInt(value: Any): Int {
                    require(value is Number)
                    return requireNotNull(value.toString().toIntOrNull()) { "Seat must be an exact integer." }
                }
                return when (context) {
                    is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> {
                        val pair = raw.getJSONArray("seats").let { arr ->
                            (0 until arr.length()).map { exactInt(arr.get(it)) }
                        }
                        require(pair.distinct().size == pair.size && pair.all { it > 0 })
                        SemanticResult("PAIR_RESULT", pairRoleId = optionalString("roleId"), pairSeats = pair.sorted())
                    }
                    is StorytellerProviderDecisionContextV1.ScalarInformation ->
                        SemanticResult("SCALAR_RESULT", scalarValue = optionalString("value"))
                    is StorytellerProviderDecisionContextV1.MayorRedirect ->
                        SemanticResult("SEAT_TARGET", seat = if (raw.isNull("seat")) null else exactInt(raw.get("seat")))
                    else -> error("Unsupported current information response context")
                }
            }
        }
    }

    fun decode(
        hostPayload: String,
        modelResponse: String,
        openDecisions: List<OpenDecision>,
    ): Result {
        // Check the Host's own payload outside the wrapper: its failures are Host defects,
        // not "AI response rejected".
        val input = JSONObject(hostPayload)
        require(input.getString("schemaId") == REQUEST_SCHEMA)
        listOf("source", "priorStrategyMemory", "currentDecision", "state").forEach { input.getJSONObject(it) }
        require(openDecisions.size == 1)
        val pending = openDecisions.single()
        val current = input.getJSONObject("currentDecision")
        require(current.getString("decisionId") == pending.decisionId &&
            current.getString("family") == pending.family &&
            (if (pending.sourceSeat == null) current.isNull("sourceSeat")
                else current.getInt("sourceSeat") == pending.sourceSeat) &&
            (if (pending.abilityRoleId == null) current.isNull("abilityRoleId")
                else current.getString("abilityRoleId") == pending.abilityRoleId)) {
            "Host pending identity changed."
        }
        require(current.get("decisionContext").toString() == contextMeaning(pending.currentDecisionContext).toString()) {
            "Current question or observed subjects changed."
        }
        return try {
            decodeResponse(input, modelResponse, pending)
        } catch (error: Exception) {
            throw error as? StorytellerResponseValidationException
                ?: StorytellerResponseValidationException(PersonalDirectOpenAiV1.safeFailure(error), error)
        }
    }

    private fun decodeResponse(
        input: JSONObject,
        modelResponse: String,
        pending: OpenDecision,
    ): Result {
        val raw = JSONObject(modelResponse)
        require(raw.keys().asSequence().toSet() == setOf("answer", "reason", "intentions"))
        val semantic = SemanticResult.fromModel(raw.getJSONObject("answer"), pending.currentDecisionContext)
        semantic.validate(pending.currentDecisionContext, input.getJSONObject("state").getJSONArray("seats").length())
        val roleIds = input.getJSONObject("state").getJSONArray("roleIds").let { roles ->
            (0 until roles.length()).map { roles.getString(it).filter(Char::isLetterOrDigit).lowercase() }.toSet()
        }
        val roleReference = semantic.pairRoleId ?: semantic.scalarValue?.takeIf {
            (pending.currentDecisionContext as? StorytellerProviderDecisionContextV1.ScalarInformation)?.kind ==
                StorytellerProviderScalarKindV1.ROLE
        }
        require(roleReference == null || roleReference.filter(Char::isLetterOrDigit).lowercase() in roleIds) {
            "Unknown role reference."
        }
        val prior = memoryFromJson(input.getJSONObject("priorStrategyMemory"))
        val updated = if (raw.isNull("intentions")) prior else {
            val entries = raw.getJSONArray("intentions")
            prior.copy(intentions = (0 until entries.length()).map {
                val item = entries.getJSONObject(it)
                require(item.keys().asSequence().toSet() == setOf("trigger", "approach"))
                HostOpeningStrategicIntentionV2(item.getString("trigger"), item.getString("approach"))
            })
        }
        val reason = raw.getString("reason")
        require(reason.isNotBlank())
        val source = input.getJSONObject("source")
        return Result(
            gameId = source.getString("gameId"),
            gameStateRevision = source.getLong("gameStateRevision"),
            playerInputRevision = source.getLong("playerInputRevision"),
            exclusiveGlobalSequence = source.getLong("exclusiveGlobalSequence"),
            memoryVersion = source.getInt("memoryVersion"),
            remainingChoices = mapOf(pending.decisionId to semantic),
            updatedMemory = updated,
            strategicDirection = reason.take(MAX_REASON_LENGTH),
        )
    }

    /** Receipt is provisional until the UI confirms this entire Host package. */
    data class ValidatedResponse(val proposal: Result, val responseId: String)

    suspend fun recommendWithReceipt(
        endpoint: String,
        accessToken: String,
        model: String,
        session: ClocktowerGameSession,
        prior: HostOpeningSemanticAcceptanceV2,
        openDecisions: List<OpenDecision>,
        ruleset: ValidatedClocktowerRuleset,
        cursor: MemoryCursor = MemoryCursor.initial(prior),
        previousResponseId: String? = null,
        onStage: (String) -> Unit = {},
    ): ValidatedResponse {
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT)
        val frozen = session.state
        onStage("FROZEN_WHOLE_GAME_PREFIX")
        // A missing/invalidated provider anchor has no earlier conversation to remember.
        // Resend the compact complete game log then; never silently lose accepted facts.
        val input = encode(frozen, prior, openDecisions, ruleset, cursor,
            historyFromGlobalSequence = if (previousResponseId == null) 0L
                else cursor.exclusiveGlobalSequence)
        val receipt = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            PersonalDirectOpenAiV1.postRetained(
                accessToken, input, model, previousResponseId, onStage)
        }
        onStage("VALIDATING_STRATEGIC_CONTINUATION")
        return try {
            val decoded = decode(input, receipt.text, openDecisions)
            require(session.state === frozen) {
                "Host state changed during whole-game continuation; discard obsolete response."
            }
            BotcAiWireTrace.hostValidation(
                "STRATEGIC_CONTINUATION", true, decoded.remainingChoices.size)
            ValidatedResponse(decoded, receipt.responseId)
        } catch (error: Throwable) {
            BotcAiWireTrace.hostValidation("STRATEGIC_CONTINUATION", false, 0,
                error.message ?: error.javaClass.simpleName)
            throw error
        }
    }

    /** Semantics of the CURRENT confirmed pending question, not an inferred player action. */
    private fun contextMeaning(context: StorytellerProviderDecisionContextV1?): Any = when (context) {
        null -> JSONObject.NULL
        is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> JSONObject()
            .put("kind", context.decisionTypeId)
            .put("reliability", context.reliability.name)
            .put("abilityRoleId", context.abilityRole.value)
            .put("sourceSeat", context.sourceSeat)
        is StorytellerProviderDecisionContextV1.ScalarInformation -> JSONObject()
            .put("kind", context.decisionTypeId)
            .put("reliability", context.reliability.name)
            .put("abilityRoleId", context.abilityRole.value)
            .put("sourceSeat", context.sourceSeat)
            .put("resultKind", context.kind.name)
            .put("metric", context.metric)
            .put("subjectSeats", JSONArray(context.subjectSeats))
        is StorytellerProviderDecisionContextV1.RegistrationResolution -> JSONObject()
            .put("kind", context.decisionTypeId)
            .put("interactionId", context.interactionId)
            .put("observationRecordId", context.observationRecordId)
            .put("sourceSeat", context.sourceSeat)
            .put("subjectSeat", context.subjectSeat)
            .put("question", context.question.toString())
        is StorytellerProviderDecisionContextV1.MayorRedirect -> JSONObject()
            .put("kind", context.decisionTypeId)
            .put("mayorSeat", context.mayorSeat)
        StorytellerProviderDecisionContextV1.DrunkAssignment ->
            error("Committed whole-game continuation cannot re-open Drunk selection")
    }

    private fun HostOpeningStrategyMemoryV2.toJson() = JSONObject()
        .put("tensions", JSONArray(tensions))
        .put("intentions", JSONArray().also { array ->
            intentions.forEach { item ->
                array.put(JSONObject().put("trigger", item.trigger)
                    .put("approach", item.approach).put("risk", item.risk))
            }
        })
        .put("uncertainties", JSONArray(uncertainties))

    private fun memoryFromJson(json: JSONObject): HostOpeningStrategyMemoryV2 {
        fun strings(key: String) = json.getJSONArray(key).let { arr -> (0 until arr.length()).map(arr::getString) }
        val intentions = json.getJSONArray("intentions")
        return HostOpeningStrategyMemoryV2(strings("tensions"), (0 until intentions.length()).map {
            val item = intentions.getJSONObject(it)
            HostOpeningStrategicIntentionV2(
                item.getString("trigger"), item.getString("approach"), item.optString("risk"))
        }, strings("uncertainties"))
    }
}
