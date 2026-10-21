package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktowerRolesForScript
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import org.json.JSONArray
import org.json.JSONObject

/**
 * Generic whole-game continuation, NOT a Poisoner/Drunk/FT recommender.
 *
 * Uses one frozen Host session prefix and the prior *advisory* multi-night memory.
 * The model may revise strategy and suggest which Host-open decisions to revisit.
 * It cannot submit an observation, register a witness or commit a game action.
 */
internal object StorytellerStrategicContinuationV2 {
    const val REQUEST_SCHEMA = "botc.whole-game-strategy-continuation-v3"

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

    internal data class Revision(
        val priorIndex: Int,
        val verdict: String,
        val reason: String,
    )

    internal data class Result(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val memoryVersion: Int,
        val revisions: List<Revision>,
        val affectedOpenDecisionIds: Set<String>,
        /** Atomic *unpublished* suggestions; never a Host action or observation. */
        val remainingChoices: Map<String, SemanticResult> = emptyMap(),
        val updatedMemory: HostOpeningStrategyMemoryV2,
        val strategicDirection: String,
    )

    private fun str() = JSONObject().put("type", "string")
    private fun integer() = JSONObject().put("type", "integer")
    private fun nullable(type: String) = JSONObject().put("type", JSONArray(listOf(type, "null")))
    private fun arr(item: JSONObject) = JSONObject().put("type", "array").put("items", item)
    private fun obj(vararg members: Pair<String, JSONObject>): JSONObject = JSONObject()
        .put("type", "object")
        .put("properties", JSONObject().apply { members.forEach { (k, v) -> put(k, v) } })
        .put("required", JSONArray(members.map { it.first }))
        .put("additionalProperties", false)

    val responseSchema: JSONObject by lazy {
        val intention = obj("trigger" to str(), "approach" to str(), "risk" to str())
        obj(
            "schemaVersion" to integer(),
            "source" to obj(
                "gameId" to str(),
                "gameStateRevision" to integer(),
                "playerInputRevision" to integer(),
                "exclusiveGlobalSequence" to integer(),
                "memoryVersion" to integer(),
            ),
            "revisions" to arr(obj(
                "priorIndex" to integer(),
                "verdict" to str().put("enum", JSONArray(listOf("KEEP", "REVISE", "RETIRE"))),
                "reason" to str(),
            )),
            "affectedOpenDecisionIds" to arr(str()),
            "currentResult" to obj(
                "decisionId" to str(),
                "semantic" to obj(
                    "kind" to str().put("enum", JSONArray(listOf(
                        "SCALAR_RESULT", "PAIR_RESULT", "SEAT_TARGET", "REGISTRATION_CHOICE"))),
                    "scalarValue" to nullable("string"),
                    "pairRoleId" to nullable("string"),
                    "pairSeats" to arr(integer()),
                    "seat" to nullable("integer"),
                    "registrationStatus" to nullable("string"),
                    "registrationRoleId" to nullable("string"),
                ),
            ),
            "updatedStrategyMemory" to obj(
                "tensions" to arr(str()),
                "intentions" to arr(intention),
                "uncertainties" to arr(str()),
            ),
            "strategicDirection" to str(),
        )
    }

    const val INSTRUCTIONS = """
你是同一局血染钟楼的资深 AI 说书人，现在继续先前整局、多夜的战略，而不是重新从零挑一条线索。
首先阅读最新实际/展示角色、状态、玩家水平、已记录声明及 gameLogDelta 精简游戏记录。连续会话中它是自上次接受回复后的新增事实；当没有保留的上次会话时，它包含本局全部精简记录以避免遗漏。
历史覆盖是局部的：没有记录的行动、声明、登记或观测必须视为未知。声明不等于事实。
gameLogDelta 按夜/天标记，仅包含有意义的正式动作及实际玩家获得的信息；酒鬼选定来自 Host 最终确认的角色而不是 AI 推荐。玩家公开声明仅是玩家发言，不代表真实身份。没有出现的内部阶段、清毒延续或重复死亡记录不是新的线索。不要从缺失记录推断未发生的行动，也不能把登记歧义升级成唯一真实登记。
priorStrategyMemory 仅为以前的可错战略假说，优先相信已确认 Host 事实。
从跨角色关系、善恶压力、可推理的多个世界、各夜可能的发展与玩家水平看整局；适用于所有角色。
逐一对 priorStrategyMemory.intentions 作 KEEP / REVISE / RETIRE，并简述真实的变化理由。
目标是继续形成多夜合理可解释的线索轨迹，不预言或承诺未来信息重复同一个数字/真假。
事件类型和角色描述当前问题；AI 不得创建独立的投毒者、酒鬼、占卜师或其他角色推荐器。
从整局战略评估当前唯一待裁量的决定，直接返回一条语义结果，不生成未来决定或整包线索。
输入 currentDecision 仅描述当前问题及其实际状态。不要索要、猜测或输出 Host 的候选 ID；也不要虚构未发生的玩家行动。
currentResult.decisionId 必须等于 currentDecision.decisionId；semantic 直接填写当前一条数字、真假、配对、座位或登记语义。Host 收到后验证结构、身份与版本。
Host 决定哪些行动已真正发生；未来尚不可决定的项目不属于本次可采纳包，仍以条件战略记忆表达。
不能修改既有玩家收到的线索，也不能假造未登记的意图，不能自行断定含糊的间谍/隐士登记。
如果说书人改变了选择，只能根据被记录的决定推断其后果，不得把未明说的人类动机当成事实。
updatedStrategyMemory 是下一次继续使用的短小、可修订战略摘要，不能冒充 Host 事实。
semantic.kind 为 SCALAR_RESULT / PAIR_RESULT / SEAT_TARGET / REGISTRATION_CHOICE 之一。对应填写 scalarValue（数字或真假用字符串）、pairRoleId 与两个 pairSeats（无该类角色时 pairRoleId 为 null、pairSeats 为 []）、seat、或 registrationStatus 与 registrationRoleId；其他可空字段填 null、无关 pairSeats 为 []。只返回严格 JSON，不输出候选 ID、未来线索或执行命令。
"""

    /** Caller must pass decisions from Host's current actual decision inventory, not model text. */
    fun encode(
        session: ClocktowerSessionState,
        prior: HostOpeningSemanticAcceptanceV2,
        openDecisions: List<OpenDecision>,
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
            .put("schemaVersion", 3)
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
                .put("gameSeed", session.gameSeed)
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
                .put("decisionContext", contextMeaning(currentDecision.currentDecisionContext)))
            .put("gameLogDelta", StorytellerGlobalEventProjectionV1.encode(
                prefix, historyFromGlobalSequence, session.gameState))
            .put("noUnobservedFutureFactMayBeAssumed", true)
            .toString()
    }

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
                        com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1.NUMBER ->
                            scalarValue?.toIntOrNull()?.let { it >= 0 && it.toString() == scalarValue } == true
                        com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1.BOOLEAN ->
                            scalarValue in setOf("true", "false")
                        com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1.ROLE ->
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
            fun fromModel(raw: JSONObject): SemanticResult {
                require(raw.keys().asSequence().toSet() == setOf(
                    "kind", "scalarValue", "pairRoleId", "pairSeats", "seat",
                    "registrationStatus", "registrationRoleId"))
                fun optionalString(key: String) = if (raw.isNull(key)) null else raw.getString(key)
                fun exactInt(value: Any): Int {
                    require(value is Number)
                    return requireNotNull(value.toString().toIntOrNull()) { "Seat must be an exact integer." }
                }
                val pair = raw.getJSONArray("pairSeats").let { arr ->
                    (0 until arr.length()).map { exactInt(arr.get(it)) }
                }
                require(pair.distinct().size == pair.size && pair.all { it > 0 })
                return SemanticResult(
                    raw.getString("kind"), optionalString("scalarValue"),
                    optionalString("pairRoleId"), pair.sorted(),
                    if (raw.isNull("seat")) null else exactInt(raw.get("seat")),
                    optionalString("registrationStatus"), optionalString("registrationRoleId"),
                )
            }


        }
    }

    fun decode(
        hostPayload: String,
        modelResponse: String,
        openDecisions: List<OpenDecision>,
    ): Result {
        val input = JSONObject(hostPayload)
        require(input.getString("schemaId") == REQUEST_SCHEMA)
        val raw = JSONObject(modelResponse)
        require(raw.keys().asSequence().toSet() == setOf("schemaVersion", "source",
            "revisions", "affectedOpenDecisionIds", "currentResult",
            "updatedStrategyMemory", "strategicDirection"))
        require(raw.getInt("schemaVersion") == 3)
        val expected = input.getJSONObject("source")
        val observed = raw.getJSONObject("source")
        require(observed.keys().asSequence().toSet() ==
            expected.keys().asSequence().toSet())
        for (field in listOf("gameId", "gameStateRevision",
                "playerInputRevision", "exclusiveGlobalSequence", "memoryVersion")) {
            require(observed.get(field) == expected.get(field)) {
                "Stale/modified Host causal-prefix identity."
            }
        }
        val oldIntentions = input.getJSONObject("priorStrategyMemory")
            .getJSONArray("intentions").length()
        val reports = raw.getJSONArray("revisions")
        require(reports.length() == oldIntentions)
        val revisions = (0 until reports.length()).map { i ->
            val obj = reports.getJSONObject(i)
            require(obj.keys().asSequence().toSet() ==
                setOf("priorIndex", "verdict", "reason"))
            val index = obj.getInt("priorIndex")
            val verdict = obj.getString("verdict")
            val reason = obj.getString("reason")
            require(index in 0 until oldIntentions)
            require(verdict in setOf("KEEP", "REVISE", "RETIRE"))
            require(reason.isNotBlank() && reason.length <= 300)
            Revision(index, verdict, reason)
        }
        require(revisions.map { it.priorIndex }.toSet() == (0 until oldIntentions).toSet())
        val affected = raw.getJSONArray("affectedOpenDecisionIds")
        val changed = (0 until affected.length()).map { affected.getString(it) }
        require(changed.distinct().size == changed.size)
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
        require(changed.all { it == pending.decisionId }) {
            "AI cannot change a closed or invented decision."
        }
        val selected = raw.getJSONObject("currentResult")
        require(selected.keys().asSequence().toSet() == setOf("decisionId", "semantic"))
        require(selected.getString("decisionId") == pending.decisionId)
        val semantic = SemanticResult.fromModel(selected.getJSONObject("semantic"))
        require(current.get("decisionContext").toString() == contextMeaning(pending.currentDecisionContext).toString()) {
            "Current question or observed subjects changed."
        }
        semantic.validate(pending.currentDecisionContext, input.getJSONObject("state").getJSONArray("seats").length())
        val roleIds = input.getJSONObject("state").getJSONArray("roleIds").let { roles ->
            (0 until roles.length()).map { roles.getString(it).filter(Char::isLetterOrDigit).lowercase() }.toSet()
        }
        val roleReference = semantic.pairRoleId ?: semantic.scalarValue?.takeIf {
            (pending.currentDecisionContext as? StorytellerProviderDecisionContextV1.ScalarInformation)?.kind ==
                com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1.ROLE
        }
        require(roleReference == null || roleReference.filter(Char::isLetterOrDigit).lowercase() in roleIds) {
            "Unknown role reference."
        }
        val choices = mapOf(pending.decisionId to semantic)
        val memory = raw.getJSONObject("updatedStrategyMemory")
        require(memory.keys().asSequence().toSet() == setOf("tensions", "intentions", "uncertainties"))
        fun strings(key: String): List<String> {
            val arr = memory.getJSONArray(key)
            return (0 until arr.length()).map { arr.getString(it) }
        }
        val entries = memory.getJSONArray("intentions")
        val updated = HostOpeningStrategyMemoryV2(
            tensions = strings("tensions"),
            intentions = (0 until entries.length()).map {
                val item = entries.getJSONObject(it)
                require(item.keys().asSequence().toSet() == setOf("trigger", "approach", "risk"))
                HostOpeningStrategicIntentionV2(
                    item.getString("trigger"), item.getString("approach"), item.getString("risk"))
            },
            uncertainties = strings("uncertainties"),
        )
        val direction = raw.getString("strategicDirection")
        require(direction.isNotBlank() && direction.length <= 600)
        return Result(
            gameId = expected.getString("gameId"),
            gameStateRevision = expected.getLong("gameStateRevision"),
            playerInputRevision = expected.getLong("playerInputRevision"),
            exclusiveGlobalSequence = expected.getLong("exclusiveGlobalSequence"),
            memoryVersion = expected.getInt("memoryVersion"),
            revisions = revisions,
            affectedOpenDecisionIds = changed.toSet(),
            remainingChoices = choices,
            updatedMemory = updated,
            strategicDirection = direction,
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
        cursor: MemoryCursor = MemoryCursor.initial(prior),
        previousResponseId: String? = null,
        onStage: (String) -> Unit = {},
    ): ValidatedResponse {
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT)
        val frozen = session.state
        onStage("FROZEN_WHOLE_GAME_PREFIX")
        // A missing/invalidated provider anchor has no earlier conversation to remember.
        // Resend the compact complete game log then; never silently lose accepted facts.
        val input = encode(frozen, prior, openDecisions, cursor,
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
}
