package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 3C/3D — ONE expert precommit planning request, before any actual Drunk is bound.
 *
 * The provider sees actual/shown roster facts and decision needs, NOT exhaustive Host
 * candidate domains, witness registrations or hypothetical cartesian products.
 * Returned advice is checked for structure and request identity; only later Host
 * confirmation records actual facts. No gameplay legal domain is generated.
 */
internal object InitialJointOpeningDirectV2 {
    const val REQUEST_SCHEMA = "botc.initial-joint-opening-request-v2"

    private fun objectSchema(vararg entries: Pair<String, JSONObject>): JSONObject =
        JSONObject()
            .put("type", "object")
            .put("properties", JSONObject().apply {
                entries.forEach { (key, schema) -> put(key, schema) }
            })
            .put("required", JSONArray(entries.map { it.first }))
            .put("additionalProperties", false)

    private fun field(type: String): JSONObject = JSONObject().put("type", type)
    private fun nullable(type: String): JSONObject = JSONObject()
        .put("type", JSONArray(listOf(type, "null")))
    private fun enum(vararg values: String): JSONObject = field("string")
        .put("enum", JSONArray(values.toList()))
    private fun array(items: JSONObject): JSONObject = field("array").put("items", items)
    private fun memoryText(): JSONObject = field("string")
        .put("pattern", "^[\\s\\S]{1,${HostOpeningStrategyMemoryV2.MAX_TEXT_LENGTH}}$")

    val responseSchema: JSONObject by lazy {
        val subject = objectSchema(
            "mode" to enum("NONE", "EVIDENCE_PAIR", "FIXED_NEIGHBORS",
                "PLAYER_SELECTED_TARGETS", "PLAYER_QUERY_PENDING"),
            "seats" to array(field("integer")),
        )
        val observation = objectSchema(
            "kind" to enum("NONE", "NUMBER", "ROLE", "BOOLEAN", "ZERO_OUTSIDERS"),
            "number" to nullable("integer"),
            "roleId" to nullable("string"),
            "boolean" to nullable("boolean"),
        )
        val info = objectSchema(
            "decisionId" to field("string"),
            "sourceSeat" to field("integer"),
            "abilityRoleId" to field("string"),
            "effectiveAbilityRoleId" to nullable("string"),
            "resolution" to enum("DECISION", "HOST_DETERMINED", "CONDITIONAL_INTENT", "DEFERRED"),
            "basis" to enum("FUNCTIONING", "DRUNK_MISINFORMATION",
                "STATUS_DEPENDENT", "RULE_DETERMINED"),
            "subject" to subject,
            "observation" to observation,
            "waitingForEvents" to array(field("string")),
        )
        val event = objectSchema(
            "decisionId" to field("string"),
            "event" to field("string"),
            "actorSeat" to nullable("integer"),
        )
        objectSchema(
            "schemaVersion" to field("integer"),
            "asOfRevision" to field("integer"),
            "requestIdentity" to objectSchema(
                "gameId" to field("string"),
                "gameStateRevision" to field("integer"),
                "playerInputRevision" to field("integer"),
                "rosterFingerprint" to field("string"),
            ),
            "openingPlan" to objectSchema(
                "drunkSeat" to nullable("integer"),
                "redHerringSeat" to nullable("integer"),
                "demonBluffRoleIds" to JSONObject()
                    .put("type", JSONArray(listOf("array", "null")))
                    .put("items", field("string")),
                "information" to array(info),
                "deferredActions" to array(event),
            ),
            "strategyMemory" to objectSchema(
                "tensions" to array(memoryText()).put("maxItems", HostOpeningStrategyMemoryV2.MAX_ENTRIES),
                "intentions" to array(objectSchema(
                    "trigger" to memoryText(),
                    "approach" to memoryText(),
                    "risk" to memoryText(),
                )).put("maxItems", HostOpeningStrategyMemoryV2.MAX_ENTRIES),
                "uncertainties" to array(memoryText()).put("maxItems", HostOpeningStrategyMemoryV2.MAX_ENTRIES),
            ),
            "tradeoffSummary" to field("string"),
        )
    }

    const val INSTRUCTIONS = """
你是一位资深的 Blood on the Clocktower（Trouble Brewing）真人说书人，将协助主持 Host 提供的这局游戏。
目标是让双方在符合其经验水平的公平、可推理的局面中，通过交流、判断与欺骗影响胜负。
请在内部完成以下整局分析，不必逐步展示：
1. 梳理全部信息位：区分说书人掌握的真实事实、玩家已经知道的事实、目前合法可决定的信息，
   以及取决于未来玩家行动的信息；辨认各信息位可能提供的合法结果和彼此关系。
2. 从善良玩家视角推演：结合每人的经验，估计可能的身份公开、信息交换与交叉验证，
   判断哪些组合会过早锁定邪恶玩家，尤其令新手邪恶玩家失去实际辩护空间。
3. 评估合法的平衡手段：联合考量本局可选择的 Drunk 及其信息、实际存在的 Recluse/Spy
   登记歧义、Red Herring 和其他合法信息选择。登记歧义只是可能性，具体登记由 Host 决定；
   保留善良玩家能够追踪、质疑并最终破解的推理路径。
4. 从邪恶玩家视角推演：结合其经验，设想他们能否实际使用三个 Demon bluffs，
   如何与场上角色和线索配合，怎样建立并维持可信的虚假世界；优先照顾不善编造故事的新手，
   避免仅在理论上精巧、实际难以扮演的方案。
5. 在双方视角下联合选择唯一最合适的首夜整体配置，包括酒鬼人选及相应信息、
   其他首夜线索、Red Herring 和 Demon bluffs；既不让信息过早交叉锁死，
   也不因掩护邪恶而剥夺善良玩家的有效信息。state.hasDrunk 为 true 时选择合法镇民作酒鬼，
   为 false 时 drunkSeat=null；真实阵容保持不变。
6. 预想多夜信息轨迹、身份声明与局势变化，形成可延续的整局战略；
   后续用 Host 新确认的事实与已有 strategyMemory 重新评估尚未执行的决定，
   已公布的信息保留其历史一致性。未来行动和公开声明属于假设，不视为已发生。
只输出一套完整可执行的推荐。strategyMemory 保留最关键的交互张力、条件性跨夜意图及未知因素；
tradeoffSummary 简要交代主要取舍，不复述角色规则或详细分析过程。

输出接口（严格遵守 Host 提供的合同；这不是逐角色规划任务）：
原样复制 requestIdentity、asOfRevision；schemaVersion=2。根据 state.seats 的展示阵容、
有无 Drunk 和 decisionInventory 中本轮实际的首夜信息项/待确认事件，自行联合选择酒鬼与首夜方案。
information 必须完整覆盖 decisionInventory.information，逐项使用相同的 decisionId、
sourceSeat、abilityRoleId。选定的酒鬼 effectiveAbilityRoleId=null，其他角色按展示身份；
resolution、basis、waitingForEvents 必须符合已知状态与尚未确认的真实玩家行动。
当前能够推荐的信息用 DECISION 与具体观察值，即使按规则只有一个答案也应给出具体值；
Host 不会替模型计算或补齐信息。依赖未发生行动的信息用 DEFERRED / STATUS_DEPENDENT、
NONE 观察及真实 waitingForEvents，不猜测未来目标或结果。
deferredActions 必须只对应 decisionInventory.events 中本局已知待确认事件，
decisionId、event、actorSeat 与实际身份一致；不得编造事件。
Host 只检查响应结构、座位/角色引用、请求身份和版本；推荐不会自动成为已确认事实。
需要 Red Herring 时，选择实际善良阵营且真实身份不是 Recluse 的玩家；排除 Recluse 是当前 Host 的开局偏好。
不要用 Spy/Recluse 的登记可能性替代实际阵营，也不要输出或推断未确认的登记事实。
demonBluffRoleIds 根据剧本角色目录选三个不同且未在场的善良身份，
否则为 null。FIXED_NEIGHBORS 的 subject.seats 为空；EVIDENCE_PAIR 只填两个座位，
配合 observation.kind=ROLE 与 roleId 表示该身份在二人之一；不另输出固定规则文案。
NONE / PLAYER_QUERY_PENDING 不填目标；没有用到的 number、roleId、boolean 填 null。
Librarian 观察到没有外来者时使用 ZERO_OUTSIDERS。
strategyMemory 的 tensions、intentions、uncertainties 各最多 ${HostOpeningStrategyMemoryV2.MAX_ENTRIES} 项；各字符串非空且不超过
${HostOpeningStrategyMemoryV2.MAX_TEXT_LENGTH} 字，tradeoffSummary 不超过 1200 字。只返回符合 JSON Schema 的结构化结果。
"""

    fun encode(
        request: InitialOpeningRequestV2,
        registry: ClocktowerCharacterRegistry,
        recentGames: List<String> = emptyList(),
    ): String {
        val snapshot = request.snapshot
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT)
        require(recentGames.size <= 4 && recentGames.all { it.length <= 250 })
        // Script role knowledge is part of the expert's task, not a menu of
        // model-facing legal candidate roles. Only known role references are checked.
        require(snapshot.grimoireSeats.all { seat ->
            registry.findByExternalId(
                (seat.shownRoleId as SnapshotField.Known<String>).value) != null
        }) { "Host opening roster contains an unknown script role." }
        val info = JSONArray()
        request.information.forEach { (id, seat) ->
            info.put(JSONObject().put("decisionId", id).put("sourceSeat", seat)
                .put("shownRoleId", request.shownRoles.getValue(seat)))
        }
        val eventMap = request.events.mapValues { it.value.first }
        val pendingEvents = eventMap.values.toSet().sorted()
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            .put("schemaVersion", 2)
            .put("scriptId", snapshot.script.value)
            .put("requestIdentity", JSONObject()
                .put("gameId", snapshot.gameId)
                .put("gameStateRevision", request.gameStateRevision)
                .put("playerInputRevision", request.playerInputRevision)
                .put("rosterFingerprint", request.rosterFingerprint))
            .put("decisionInventory", JSONObject()
                .put("information", info)
                .put("events", JSONArray().also { arr ->
                    eventMap.forEach { (id, event) ->
                        arr.put(JSONObject().put("decisionId", id).put("event", event))
                    }
                }))
            .put("state", JSONObject()
                .put("asOfRevision", request.gameStateRevision)
                .put("phase", "SETUP_PRECOMMIT")
                .put("hasDrunk", request.hasDrunk)
                .put("drunkAssignment", if (request.hasDrunk) "UNCOMMITTED" else "NOT_APPLICABLE")
                .put("seats", JSONArray().also { arr ->
                    snapshot.grimoireSeats.sortedBy { it.seat }.forEach { seat ->
                        val role = (seat.shownRoleId as SnapshotField.Known<String>).value
                        val actual = when (val field = seat.actualRoleId) {
                            is SnapshotField.Known<*> -> field.value
                            SnapshotField.Uncommitted -> "UNCOMMITTED_IF_DRUNK_SELECTED"
                            else -> error("Unexpected unknown actual role.")
                        }
                        arr.put(JSONObject()
                            .put("seat", seat.seat)
                            .put("shownRoleId", role)
                            .put("actualRoleId", actual)
                            .put("experience", request.playerExperience.getValue(seat.seat).name))
                    }
                })
                .put("confirmedEvents", JSONArray())
                .put("unknownEvents", JSONArray(pendingEvents))
                .put("recentGames", JSONArray(recentGames)))
            .put("unknownFutureActionsAreNotFacts", true)
            .toString()
    }

    fun decode(
        response: String,
        request: InitialOpeningRequestV2,
    ): HostOpeningSemanticAcceptanceV2 {
        return try {
            parseRecommendation(JSONObject(response), request)
        } catch (error: Exception) {
            throw error as? StorytellerResponseValidationException
                ?: StorytellerResponseValidationException(PersonalDirectOpenAiV1.safeFailure(error), error)
        }
    }

    private fun JSONObject.exact(vararg keys: String) {
        require(this.keys().asSequence().toSet() == keys.toSet()) { "Unexpected or missing opening fields." }
    }

    private fun JSONObject.integer(key: String): Long {
        val value = get(key)
        require(value is Int || value is Long) { "Opening $key must be an integer." }
        return (value as Number).toLong()
    }

    private fun JSONObject.seat(key: String): Int? {
        if (isNull(key)) return null
        val value = integer(key)
        require(value in 1..Int.MAX_VALUE.toLong())
        return value.toInt()
    }

    private fun JSONArray.strings(): List<String> = (0 until length()).map {
        require(get(it) is String)
        getString(it)
    }

    private fun parseRecommendation(raw: JSONObject, request: InitialOpeningRequestV2): HostOpeningSemanticAcceptanceV2 {
        raw.exact("schemaVersion", "asOfRevision", "requestIdentity", "openingPlan", "strategyMemory", "tradeoffSummary")
        require(raw.integer("schemaVersion") == 2L)
        val identity = raw.getJSONObject("requestIdentity")
        identity.exact("gameId", "gameStateRevision", "playerInputRevision", "rosterFingerprint")
        require(identity.getString("gameId") == request.snapshot.gameId &&
            identity.integer("gameStateRevision") == request.gameStateRevision &&
            identity.integer("playerInputRevision") == request.playerInputRevision &&
            identity.getString("rosterFingerprint") == request.rosterFingerprint &&
            raw.integer("asOfRevision") == request.gameStateRevision) { "Stale opening response." }
        val plan = raw.getJSONObject("openingPlan")
        plan.exact("drunkSeat", "redHerringSeat", "demonBluffRoleIds", "information", "deferredActions")
        val drunk = plan.seat("drunkSeat")
        val red = plan.seat("redHerringSeat")
        require(request.hasDrunk == (drunk != null))
        require(drunk == null || drunk in request.shownRoles)
        require(red == null || red in request.redHerringEligibleSeats) {
            "redHerringSeat must be an actually good player who is not the Recluse."
        }
        val bluffs = if (plan.isNull("demonBluffRoleIds")) null else plan.getJSONArray("demonBluffRoleIds").strings()
        bluffs?.let {
            require(it.size == 3 && it.distinct().size == 3 && it.all(request.knownRoleIds::contains))
        }
        val information = linkedMapOf<Int, HostOpeningSemanticValueV2>()
        val conditional = mutableListOf<HostOpeningConditionalIntentV2>()
        val ids = mutableSetOf<String>()
        val items = plan.getJSONArray("information")
        require(items.length() <= 64)
        repeat(items.length()) { index ->
            val item = items.getJSONObject(index)
            item.exact("decisionId", "sourceSeat", "abilityRoleId", "effectiveAbilityRoleId", "resolution", "basis",
                "subject", "observation", "waitingForEvents")
            val id = item.getString("decisionId")
            require(ids.add(id)) { "Duplicate opening decision." }
            val source = requireNotNull(item.seat("sourceSeat"))
            require(request.information[id] == source)
            val subject = item.getJSONObject("subject")
            subject.exact("mode", "seats")
            val subjects = subject.getJSONArray("seats")
            val seats = (0 until subjects.length()).map {
                val seat = subjects.get(it)
                require(seat is Int && seat in request.shownRoles)
                seat as Int
            }.sorted()
            val observation = item.getJSONObject("observation")
            observation.exact("kind", "number", "roleId", "boolean")
            val number = if (observation.isNull("number")) null else observation.integer("number").let {
                require(it in 0..Int.MAX_VALUE.toLong())
                it.toInt()
            }
            val value = HostOpeningSemanticValueV2(source, item.getString("abilityRoleId"),
                if (item.isNull("effectiveAbilityRoleId")) null else item.getString("effectiveAbilityRoleId"),
                item.getString("basis"), subject.getString("mode"), seats, observation.getString("kind"), number,
                if (observation.isNull("roleId")) null else observation.getString("roleId"),
                if (observation.isNull("boolean")) null else observation.get("boolean").let {
                    require(it is Boolean)
                    it as Boolean
                })
            require(value.abilityRoleId == request.shownRoles[source]) { "Opening source identity changed." }
            require(value.effectiveAbilityRoleId == null || value.effectiveAbilityRoleId in request.knownRoleIds)
            require(value.roleId == null || value.roleId in request.knownRoleIds)
            val waiting = item.getJSONArray("waitingForEvents").strings()
            require(waiting.distinct().size == waiting.size && waiting.all { event ->
                request.events.values.any { it.first == event }
            }) { "Opening refers to an unknown pending event." }
            when (item.getString("resolution")) {
                "DECISION" -> {
                    require(waiting.isEmpty() && value.observationKind != "NONE")
                    information[source] = value
                }
                "HOST_DETERMINED" -> {
                    // Older responses can abstain; Host never calculates a replacement value.
                    require(waiting.isEmpty() && value.observationKind == "NONE")
                }
                "DEFERRED", "CONDITIONAL_INTENT" -> {
                    require(waiting.isNotEmpty())
                    require((item.getString("resolution") == "DEFERRED") == (value.observationKind == "NONE"))
                    conditional += HostOpeningConditionalIntentV2(id,
                        value.takeIf { it.observationKind != "NONE" }, waiting)
                }
                else -> error("Unknown opening resolution.")
            }
        }
        require(ids == request.information.keys) { "Incomplete opening information inventory." }
        val actions = plan.getJSONArray("deferredActions")
        require(actions.length() == request.events.size)
        repeat(actions.length()) { index ->
            val action = actions.getJSONObject(index)
            action.exact("decisionId", "event", "actorSeat")
            val id = action.getString("decisionId")
            val event = action.getString("event")
            require(ids.add(id) && request.events[id] == (event to action.seat("actorSeat"))) {
                "Unknown or duplicate pending player action."
            }
            conditional += HostOpeningConditionalIntentV2(id, null, listOf(event))
        }
        val strategy = raw.getJSONObject("strategyMemory")
        strategy.exact("tensions", "intentions", "uncertainties")
        val intentions = strategy.getJSONArray("intentions")
        val memory = HostOpeningStrategyMemoryV2(strategy.getJSONArray("tensions").strings(),
            (0 until intentions.length()).map { index ->
                val entry = intentions.getJSONObject(index)
                entry.exact("trigger", "approach", "risk")
                HostOpeningStrategicIntentionV2(entry.getString("trigger"), entry.getString("approach"), entry.getString("risk"))
            }, strategy.getJSONArray("uncertainties").strings())
        require(raw.getString("tradeoffSummary").length <= 1200)
        return HostOpeningSemanticAcceptanceV2(OpeningSelectionV2(request.snapshot.gameId,
            request.gameStateRevision, request.playerInputRevision, request.rosterFingerprint,
            drunk, red, bluffs, information.toMap()), conditional.toList(), memory,
            raw.getString("tradeoffSummary").takeIf(String::isNotBlank))
    }

    /** Provider ID is provisional: only the caller's actual Host adoption may promote it. */
    data class AcceptedResponse(
        val accepted: HostOpeningSemanticAcceptanceV2,
        val responseId: String,
    )

    suspend fun recommendWithReceipt(
        endpoint: String,
        accessToken: String,
        request: InitialOpeningRequestV2,
        registry: ClocktowerCharacterRegistry,
        model: String,
        recentGames: List<String> = emptyList(),
        onStage: (String) -> Unit = {},
    ): AcceptedResponse {
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT) {
            "Unified opening requires personal-direct Responses support."
        }
        onStage("BUILDING_JOINT_OPENING_CONTEXT")
        val payload = encode(request, registry, recentGames)
        val receipt = withContext(Dispatchers.IO) {
            PersonalDirectOpenAiV1.postRetained(accessToken, payload, model,
                onStage = onStage)
        }
        onStage("VALIDATING_COMPLETE_HOST_OPENING")
        return try {
            val accepted = decode(receipt.text, request)
            BotcAiWireTrace.hostValidation(
                "JOINT_OPENING", true, accepted.selection.information.size)
            AcceptedResponse(accepted, receipt.responseId)
        } catch (error: Throwable) {
            BotcAiWireTrace.hostValidation("JOINT_OPENING", false, 0,
                error.message ?: error.javaClass.simpleName)
            throw error
        }
    }
}
