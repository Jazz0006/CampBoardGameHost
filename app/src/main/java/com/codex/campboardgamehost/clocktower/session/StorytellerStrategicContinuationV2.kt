package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
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
    const val REQUEST_SCHEMA = "botc.whole-game-strategy-continuation-v2"

    internal data class OpenDecision(
        val decisionId: String,
        val sourceSeat: Int?,
        val family: String,
        // Supplied ONLY by a fresh Host pending producer, not inferred from a role list.
        val legalCandidates: List<StorytellerProviderCandidateV1> = emptyList(),
        val abilityRoleId: String? = null,
        val currentDecisionContext: StorytellerProviderDecisionContextV1? = null,
    ) {
        init {
            require(decisionId.isNotBlank() && family.isNotBlank())
            require(sourceSeat == null || sourceSeat > 0)
            require(legalCandidates.map { it.candidateId }.distinct().size == legalCandidates.size)
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
        val remainingChoices: Map<String, String> = emptyMap(),
        val updatedMemory: HostOpeningStrategyMemoryV2,
        val strategicDirection: String,
    )

    private fun str() = JSONObject().put("type", "string")
    private fun integer() = JSONObject().put("type", "integer")
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
            "remainingChoices" to arr(obj("decisionId" to str(), "candidateId" to str())),
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
首先阅读最新完整实际/展示角色、状态、玩家水平与已记录的声明，以及 Host 有来源和先后顺序的事实记录。
历史覆盖是局部的：没有记录的行动、声明、登记或观测必须视为未知。声明不等于事实。
已确认的说书人决策档案单独放在 unsequencedDecisionHistory，不具备与动作和玩家观察日志可证明的统一时间次序；
不要替它猜测事件先后，也不能把档案中的推断或登记歧义升级成唯一真实登记。
priorStrategyMemory 仅为以前的可错战略假说，优先相信已确认 Host 事实。
从跨角色关系、善恶压力、可推理的多个世界、各夜可能的发展与玩家水平看整局；适用于所有角色。
逐一对 priorStrategyMemory.intentions 作 KEEP / REVISE / RETIRE，并简述真实的变化理由。
目标是继续形成多夜合理可解释的线索轨迹，不预言或承诺未来信息重复同一个数字/真假。
事件类型和角色仅决定 Host 合法性；AI 不得创建独立的投毒者、酒鬼、占卜师或其他角色推荐器。
这是一次联合剩余规划：先评估所有 Host 当前开放的决定彼此如何影响，再返回一整包 remainingChoices，不能逐项独立评分。
对 openDecisions 中具有 legalCandidates 的每个决定恰好选择一个候选 ID；不得漏掉、增添、选择非法 ID，也不要凭空选择未来玩家行动。
没有 legalCandidates 的记录仅供战略诊断，不得生成选择。affectedOpenDecisionIds 只是战略影响范围，不能代替 remainingChoices。
Host 决定哪些行动已真正发生；未来尚不可决定的项目不属于本次可采纳包，仍以条件战略记忆表达。
不能修改既有玩家收到的线索，也不能假造未登记的意图，不能自行断定含糊的间谍/隐士登记。
如果说书人改变了选择，只能根据被记录的决定推断其后果，不得把未明说的人类动机当成事实。
updatedStrategyMemory 是下一次继续使用的短小、可修订战略摘要，不能冒充 Host 事实。
只返回严格结构化 JSON；不得输出 Host 合法候选之外的新线索或候选 ID，不下达游戏执行命令，不写长篇分析。
"""

    /** Caller must pass decisions from Host's CURRENT legal decision inventory, not model text. */
    fun encode(
        session: ClocktowerSessionState,
        prior: HostOpeningSemanticAcceptanceV2,
        openDecisions: List<OpenDecision>,
        cursor: MemoryCursor = MemoryCursor.initial(prior),
    ): String {
        require(prior.selection.gameId == session.gameId && cursor.gameId == session.gameId)
        val memory = cursor.memory
        require(session.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(session.gameStateRevision >= cursor.gameStateRevision &&
            session.playerInputRevision >= cursor.playerInputRevision &&
            session.nextTimelineGlobalSequence >= cursor.exclusiveGlobalSequence)
        val seats = session.gameState.players.sortedBy { it.seat }
        require(seats.map { it.seat } == (1..seats.size).toList())
        require(openDecisions.map { it.decisionId }.distinct().size == openDecisions.size)
        require(openDecisions.all { it.sourceSeat == null ||
            it.sourceSeat in 1..seats.size })
        val prefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            session, StorytellerProviderRevisionV1(
                session.gameStateRevision, session.playerInputRevision))
        require(prefix.cutoffSource == StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED)
        val cutoff = requireNotNull(prefix.exclusiveGlobalSequence)
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            .put("schemaVersion", 2)
            .put("source", JSONObject()
                .put("gameId", session.gameId)
                .put("gameStateRevision", session.gameStateRevision)
                .put("playerInputRevision", session.playerInputRevision)
                .put("exclusiveGlobalSequence", cutoff)
                .put("memoryVersion", cursor.version))
            .put("state", JSONObject()
                .put("scriptId", session.gameState.script.value)
                .put("gameSeed", session.gameSeed)
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
            .put("priorOpening", JSONObject()
                .put("drunkSeat", prior.selection.drunkSeat ?: JSONObject.NULL)
                .put("adoptedOpeningCandidateCount", prior.selection.choices.size))
            .put("openDecisions", JSONArray().also { array ->
                openDecisions.forEach { decision ->
                    array.put(JSONObject()
                        .put("decisionId", decision.decisionId)
                        .put("family", decision.family)
                        .put("sourceSeat", decision.sourceSeat ?: JSONObject.NULL)
                        .put("abilityRoleId", decision.abilityRoleId ?: JSONObject.NULL)
                        .put("decisionContext", contextMeaning(decision.currentDecisionContext))
                        .put("legalCandidates", JSONArray().also { options ->
                            decision.legalCandidates.forEach { candidate ->
                                options.put(JSONObject()
                                    .put("candidateId", candidate.candidateId)
                                    .put("meaning", candidateMeaning(candidate.payload)))
                            }
                        }))
                }
            })
            .put("causalHistory", JSONObject()
                .put("coverage", JSONObject().also { coverage ->
                    prefix.coverage.forEach { (dimension, state) ->
                        coverage.put(dimension.name, JSONObject()
                            .put("state", state.state.name).put("reasonCode", state.reasonCode))
                    }
                })
                .put("entries", JSONArray().also { array ->
                    prefix.entries.forEach { entry ->
                        val data = JSONObject()
                            .put("globalSequence", entry.point.globalSequence)
                            .put("phase", entry.point.phase.name)
                            .put("round", entry.point.round)
                            .put("entryId", entry.entryId)
                            .put("kind", entry.kind)
                        when (entry) {
                            is StorytellerProviderHistoryEntryV1.Action -> {
                                data.put("typedFact", entry.fact.toString())
                                    .put("factClass", entry.fact.javaClass.simpleName)
                            }
                            is StorytellerProviderHistoryEntryV1.Observation -> {
                                data.put("sourceSeat", entry.sourceSeat ?: JSONObject.NULL)
                                    .put("sourceAbility", entry.sourceAbility?.value ?: JSONObject.NULL)
                                    .put("visibility", entry.visibility.name)
                                    .put("recipientSeats", JSONArray(entry.recipientSeats.sorted()))
                                    .put("reliability", entry.reliability.name)
                                    .put("typedProposition", entry.proposition.toString())
                                    .put("propositionClass", entry.proposition.javaClass.simpleName)
                            }
                        }
                        array.put(data)
                    }
                }))
            // Legacy decision archive has no global timeline points; keep it
            // explicitly separate from the provably ordered action/observation
            // prefix, never fabricate ordering or treat proposals as committed.
            .put("unsequencedDecisionHistory", JSONObject()
                .put("ordering", "UNKNOWN_RELATIVE_TO_CAUSAL_HISTORY")
                .put("confirmedEvents", JSONArray().also { array ->
                    session.decisionHistory.events.filter { event ->
                        event.status == DecisionEventStatus.CONFIRMED ||
                            event.status == DecisionEventStatus.APPLIED
                    }.forEach { event ->
                        array.put(JSONObject()
                            .put("eventId", event.eventId)
                            .put("status", event.status.name)
                            .put("gameStateRevision", event.gameStateRevision)
                            .put("playerInputRevision", event.playerInputRevision)
                            .put("decisionType", event.selectedOutcomeSnapshot.decisionType)
                            .put("canonicalFields",
                                JSONObject(event.selectedOutcomeSnapshot.canonicalFields))
                            .put("registrationWitnessStatus",
                                "NOT_RECONSTRUCTED_FROM_ARCHIVE"))
                    }
                })
                .put("corrections", JSONArray().also { array ->
                    session.decisionHistory.corrections.forEach { correction ->
                        array.put(JSONObject()
                            .put("eventId", correction.eventId)
                            .put("replacedEventId", correction.replacedEventId)
                            .put("replacementEventId", correction.replacementEventId)
                            .put("reasonCode", correction.reasonCode))
                    }
                }))
            .put("noUnobservedFutureFactMayBeAssumed", true)
            .toString()
    }

    fun decode(hostPayload: String, modelResponse: String): Result {
        val input = JSONObject(hostPayload)
        require(input.getString("schemaId") == REQUEST_SCHEMA)
        val raw = JSONObject(modelResponse)
        require(raw.keys().asSequence().toSet() == setOf("schemaVersion", "source",
            "revisions", "affectedOpenDecisionIds", "remainingChoices",
            "updatedStrategyMemory", "strategicDirection"))
        require(raw.getInt("schemaVersion") == 2)
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
        val known = input.getJSONArray("openDecisions").let { array ->
            (0 until array.length()).map { array.getJSONObject(it).getString("decisionId") }.toSet()
        }
        require(changed.all { it in known }) { "AI cannot change a closed or invented decision." }
        val legal = input.getJSONArray("openDecisions").let { array ->
            (0 until array.length()).associate { index ->
                val decision = array.getJSONObject(index)
                val options = decision.getJSONArray("legalCandidates")
                decision.getString("decisionId") to (0 until options.length())
                    .map { options.getJSONObject(it).getString("candidateId") }.toSet()
            }.filterValues { it.isNotEmpty() }
        }
        val choiceArray = raw.getJSONArray("remainingChoices")
        require(choiceArray.length() == legal.size && choiceArray.length() <= 64) {
            "One coordinated remaining choice is required for each current Host legal domain."
        }
        val choices = linkedMapOf<String, String>()
        for (i in 0 until choiceArray.length()) {
            val item = choiceArray.getJSONObject(i)
            require(item.keys().asSequence().toSet() == setOf("decisionId", "candidateId"))
            val id = item.getString("decisionId")
            val candidate = item.getString("candidateId")
            require(id in legal && candidate in requireNotNull(legal[id])) {
                "Residual selection is not in the current Host legal domain."
            }
            require(choices.put(id, candidate) == null) { "Repeated residual decision" }
        }
        require(choices.keys == legal.keys) { "Incomplete coupled residual selection" }
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

    suspend fun recommend(
        endpoint: String,
        accessToken: String,
        model: String,
        session: ClocktowerGameSession,
        prior: HostOpeningSemanticAcceptanceV2,
        openDecisions: List<OpenDecision>,
        cursor: MemoryCursor = MemoryCursor.initial(prior),
        onStage: (String) -> Unit = {},
    ): Result = recommendWithReceipt(
        endpoint, accessToken, model, session, prior, openDecisions,
        cursor, null, onStage,
    ).proposal

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
        val input = encode(frozen, prior, openDecisions, cursor)
        val receipt = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            PersonalDirectOpenAiV1.postRetained(
                accessToken, input, model, previousResponseId, onStage)
        }
        onStage("VALIDATING_STRATEGIC_CONTINUATION")
        return try {
            val decoded = decode(input, receipt.text)
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
        is StorytellerProviderDecisionContextV1.DayAbilityRegistration -> JSONObject()
            .put("kind", context.decisionTypeId)
            .put("interactionId", context.interactionId)
            .put("abilityRoleId", context.abilityRole.value)
            .put("actorSeat", context.actorSeat)
            .put("subjectSeat", context.subjectSeat)
            .put("question", context.question.toString())
        is StorytellerProviderDecisionContextV1.MayorRedirect -> JSONObject()
            .put("kind", context.decisionTypeId)
            .put("mayorSeat", context.mayorSeat)
        StorytellerProviderDecisionContextV1.DrunkAssignment ->
            error("Committed whole-game continuation cannot re-open Drunk selection")
    }

    private fun candidateMeaning(payload: StorytellerProviderCandidatePayloadV1): JSONObject = when (payload) {
        is StorytellerProviderCandidatePayloadV1.PairInformation -> JSONObject()
            .put("kind", "PAIR_RESULT")
            .put("shownRoleId", payload.shownRoleId ?: JSONObject.NULL)
            .put("candidateSeats", JSONArray(payload.candidateSeats))
            .put("semanticTruth", payload.semanticTruth.name)
            .put("registrationWitness", "AMBIGUOUS_NOT_COMMITTED")
        is StorytellerProviderCandidatePayloadV1.ScalarResult -> JSONObject()
            .put("kind", "SCALAR_RESULT").put("value", payload.value)
        is StorytellerProviderCandidatePayloadV1.SeatTarget -> JSONObject()
            .put("kind", "SEAT_TARGET").put("seat", payload.seat)
        is StorytellerProviderCandidatePayloadV1.RegistrationChoice -> JSONObject()
            .put("kind", "REGISTRATION_CHOICE").put("status", payload.status.name)
            .put("selectedRoleId", payload.selectedRoleId?.value ?: JSONObject.NULL)
        is StorytellerProviderCandidatePayloadV1.DrunkAssignment ->
            error("Initial Drunk assignment cannot be reopened by continuation")
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
