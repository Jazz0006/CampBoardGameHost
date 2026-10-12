package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
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
priorStrategyMemory 仅为以前的可错战略假说，优先相信已确认 Host 事实。
从跨角色关系、善恶压力、可推理的多个世界、各夜可能的发展与玩家水平看整局；适用于所有角色。
逐一对 priorStrategyMemory.intentions 作 KEEP / REVISE / RETIRE，并简述真实的变化理由。
目标是继续形成多夜合理可解释的线索轨迹，不预言或承诺未来信息重复同一个数字/真假。
事件类型和角色仅决定 Host 合法性；AI 不得创建独立的投毒者、酒鬼、占卜师或其他角色推荐器。
只可引用 openDecisions 中的 ID 标记需要协调重规划的未执行决策；所有其他决定均不可推测或改写。
不能修改既有玩家收到的线索，也不能假造未登记的意图，不能自行断定含糊的间谍/隐士登记。
如果说书人改变了选择，只能根据被记录的决定推断其后果，不得把未明说的人类动机当成事实。
updatedStrategyMemory 是下一次继续使用的短小、可修订战略摘要，不能冒充 Host 事实。
只返回严格结构化 JSON，不输出新线索或候选 ID、不下达游戏执行命令，不写长篇分析。
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
                            .put("experience", context?.experienceLevel?.name ?: "UNKNOWN")
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
                        .put("sourceSeat", decision.sourceSeat ?: JSONObject.NULL))
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
            .put("noUnobservedFutureFactMayBeAssumed", true)
            .toString()
    }

    fun decode(hostPayload: String, modelResponse: String): Result {
        val input = JSONObject(hostPayload)
        require(input.getString("schemaId") == REQUEST_SCHEMA)
        val raw = JSONObject(modelResponse)
        require(raw.keys().asSequence().toSet() == setOf("schemaVersion", "source",
            "revisions", "affectedOpenDecisionIds", "updatedStrategyMemory", "strategicDirection"))
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
            updatedMemory = updated,
            strategicDirection = direction,
        )
    }

    suspend fun recommend(
        endpoint: String,
        accessToken: String,
        model: String,
        session: ClocktowerGameSession,
        prior: HostOpeningSemanticAcceptanceV2,
        openDecisions: List<OpenDecision>,
        cursor: MemoryCursor = MemoryCursor.initial(prior),
        onStage: (String) -> Unit = {},
    ): Result {
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT)
        val frozen = session.state
        onStage("FROZEN_WHOLE_GAME_PREFIX")
        val input = encode(frozen, prior, openDecisions, cursor)
        val raw = ProductionDrunkAiGatewayV1.post(
            endpoint, accessToken, input, model, onStage)
        onStage("VALIDATING_STRATEGIC_CONTINUATION")
        val outcome = decode(input, raw)
        require(session.state === frozen) {
            "Host state changed during whole-game continuation; discard obsolete response."
        }
        return outcome
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
