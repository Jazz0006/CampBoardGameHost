package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ONE joint precommit planning request, before any actual Drunk is bound.
 *
 * The request lists only the choices that are open now, each with the candidates the Session
 * rules allow ([InitialOpeningDomainV3]); results with a single legal value are filled by the
 * Host and never asked. The response is checked against the same candidates. Accepted advice
 * is still only advice: later Host confirmation records the actual facts.
 */
internal object InitialJointOpeningDirectV2 {
    const val REQUEST_SCHEMA = "botc.initial-joint-opening-request-v3"
    private const val MAX_RATIONALE_LENGTH = 600

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
    private fun array(items: JSONObject): JSONObject = field("array").put("items", items)

    /** One fixed schema for every roster; the open choices vary in the request, not here. */
    val responseSchema: JSONObject by lazy {
        objectSchema(
            "drunkSeat" to nullable("integer"),
            "redHerringSeat" to nullable("integer"),
            "demonBluffRoleIds" to JSONObject()
                .put("type", JSONArray(listOf("array", "null")))
                .put("items", field("string")),
            "information" to array(objectSchema(
                "seat" to field("integer"),
                "roleId" to nullable("string"),
                "seats" to array(field("integer")),
                "number" to nullable("integer"),
            )),
            "intentions" to array(objectSchema(
                "trigger" to field("string"),
                "approach" to field("string"),
            )).put("maxItems", HostOpeningStrategyMemoryV2.MAX_ENTRIES),
            "rationale" to field("string"),
        )
    }

    const val INSTRUCTIONS = """
你是资深的 Blood on the Clocktower（Trouble Brewing）说书人，为 Host 提供开局推荐。
Host 负责规则与合法性：输入中的候选都已合法，你只需在候选内选出对这局最好的组合。

目标：让双方在符合各自经验的局面中靠交流、推理和欺骗决定胜负。
- 善良方应拿到能用来推理的信息，不要为了保护邪恶而让信息失去价值。
- 邪恶方需要讲得出来的故事：掩护身份要适合该玩家的经验，且不会被在场角色当场证伪。
- 首夜信息交叉后不应直接锁定邪恶玩家，也不应让善良方无从下手。
- 酒鬼人选与给酒鬼的假信息一起考虑：假信息应当可信，并最终可以被破解。
- 玩家经验越低，方案越要简单直接。

输入：
- seats：座位按顺时针排列，首尾相邻；role 是玩家看到的身份。
- slots：本次要决定的开局项及其候选。drunk 是酒鬼座位，redHerring 是占卜师的红鲱鱼，
  demonBluffs 是给恶魔的三个掩护身份。
- firstNightInfo：首夜信息位。truthful 是该角色正常运作且未中毒时的合法结果。
  answer 说明是否需要你回答：ALWAYS 必须回答，IF_DRUNK 仅当你把该座位选为酒鬼时回答，NEVER 不回答。
  NUMBER 是一个数字；PAIR 是“某身份在两名玩家之一”；AFTER_PLAYER_CHOICE 要等玩家选人。
- 中毒由 Host 在夜间另行询问，本次按未中毒规划。

回答：
- slots 中出现的项都要回答，没有出现的项填 null。若你把占卜师选为酒鬼，redHerringSeat 填 null。
- information 只包含按 answer 需要回答的信息位，seat 是信息位本人的座位；用不到的字段填 null 或 []。
- NUMBER：number 从 truthful 取值。
- PAIR：roleId 取 truthful 中的一个身份，seats 填两个座位：该身份所在的座位，加上除信息位本人外的任意一个座位。
  mayRegister 列出的座位可以被当作其所列的任一身份。
  noneTruthful 为 true 时，“没有外来者”是合法结果，用 roleId=null、seats=[] 表示。
- 酒鬼实际不是镇民：洗衣妇的 truthful 中，位于你所选酒鬼座位的那一项不能使用。
  truthful 里座位为 DRUNK_SEAT 的一项指你选定的酒鬼座位。
- 被你选为酒鬼的信息位不受 truthful 限制：NUMBER 从 ifDrunk 取值；
  洗衣妇可用任一在场镇民身份，或你选的掩护身份中的镇民身份；
  图书管理员、调查员可用剧本中任一外来者、爪牙身份；seats 为除本人外的任意两个座位。
- intentions：最多 ${HostOpeningStrategyMemoryV2.MAX_ENTRIES} 条跨夜意向，供夜间后续决定参考，例如某角色中毒时打算给什么；
  trigger 与 approach 各不超过 100 字。
- rationale：用一两句话说明主要取舍，不超过 200 字，不复述规则。
"""

    fun encode(request: InitialOpeningRequestV2, ruleset: ValidatedClocktowerRuleset): String {
        require(request.snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT)
        val domain = InitialOpeningDomainV3(request, ruleset)
        val slots = JSONArray()
        if (domain.drunkSeats.isNotEmpty()) slots.put(JSONObject()
            .put("slot", "drunk").put("seats", JSONArray(domain.drunkSeats)))
        if (domain.redHerringSeats.isNotEmpty()) slots.put(JSONObject()
            .put("slot", "redHerring").put("seats", JSONArray(domain.redHerringSeats)))
        if (domain.bluffRoleIds.isNotEmpty()) slots.put(JSONObject()
            .put("slot", "demonBluffs").put("roles", JSONArray(domain.bluffRoleIds)))
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            .put("script", request.snapshot.script.value)
            .put("seats", JSONArray().also { seats ->
                request.shownRoles.toSortedMap().forEach { (seat, role) ->
                    seats.put(JSONObject().put("seat", seat).put("role", role)
                        .put("experience", request.playerExperience.getValue(seat).name))
                }
            })
            .put("slots", slots)
            .put("firstNightInfo", JSONArray().also { info ->
                domain.information.forEach { info.put(it.toJson()) }
            })
            .toString()
    }

    private fun InitialOpeningDomainV3.Info.toJson(): JSONObject {
        val json = JSONObject().put("seat", seat).put("role", roleId)
        when (this) {
            is InitialOpeningDomainV3.Number -> {
                json.put("type", "NUMBER").put("answer", answer.name).put("truthful", JSONArray(truthful))
                if (ifDrunk.isNotEmpty()) json.put("ifDrunk", JSONArray(ifDrunk))
            }
            is InitialOpeningDomainV3.Pair -> {
                json.put("type", "PAIR").put("answer", answer.name)
                    .put("truthful", JSONArray().also { items ->
                        truthful.forEach { items.put(JSONObject().put("role", it.roleId)
                            .put("seat", it.seat ?: "DRUNK_SEAT")) }
                    })
                if (mayRegister.isNotEmpty()) json.put("mayRegister", JSONArray().also { items ->
                    mayRegister.forEach { items.put(JSONObject().put("seat", it.seat)
                        .put("roles", JSONArray(it.roleIds))) }
                })
                if (noneTruthful) json.put("noneTruthful", true)
            }
            is InitialOpeningDomainV3.AfterPlayerChoice ->
                json.put("type", "AFTER_PLAYER_CHOICE").put("answer", answer.name)
        }
        return json
    }

    fun decode(
        response: String,
        request: InitialOpeningRequestV2,
        ruleset: ValidatedClocktowerRuleset,
    ): HostOpeningSemanticAcceptanceV2 {
        val domain = InitialOpeningDomainV3(request, ruleset)
        return try {
            parseRecommendation(JSONObject(response), request, domain)
        } catch (error: Exception) {
            throw error as? StorytellerResponseValidationException
                ?: StorytellerResponseValidationException(PersonalDirectOpenAiV1.safeFailure(error), error)
        }
    }

    private fun JSONObject.exact(vararg keys: String) {
        require(this.keys().asSequence().toSet() == keys.toSet()) { "Unexpected or missing opening fields." }
    }

    private fun JSONObject.integer(key: String): Int {
        val value = get(key)
        require(value is Int) { "Opening $key must be an integer." }
        return value
    }

    private fun JSONObject.nullableInteger(key: String): Int? = if (isNull(key)) null else integer(key)

    private fun JSONArray.strings(): List<String> = (0 until length()).map {
        require(get(it) is String)
        getString(it)
    }

    private fun parseRecommendation(
        raw: JSONObject,
        request: InitialOpeningRequestV2,
        domain: InitialOpeningDomainV3,
    ): HostOpeningSemanticAcceptanceV2 {
        raw.exact("drunkSeat", "redHerringSeat", "demonBluffRoleIds", "information", "intentions", "rationale")
        // Every other choice depends on the Drunk, so a wrong Drunk rejects the whole plan.
        val drunk = raw.nullableInteger("drunkSeat")
        require(if (request.hasDrunk) drunk in domain.drunkSeats else drunk == null) {
            "drunkSeat must be one of the offered seats."
        }
        val bluffs = if (domain.bluffRoleIds.isEmpty()) null
            else raw.getJSONArray("demonBluffRoleIds").strings().also {
                require(it.size == 3 && it.distinct().size == 3 && it.all(domain.bluffRoleIds::contains)) {
                    "demonBluffRoleIds must be three different offered roles."
                }
            }
        val redHerringSeats = domain.redHerringSeatsFor(drunk)
        val red = if (redHerringSeats.isEmpty()) null else raw.nullableInteger("redHerringSeat").also {
            require(it in redHerringSeats) { "redHerringSeat must be one of the offered seats." }
        }
        // An information item the rules do not allow is dropped on its own; that seat's
        // current night step then asks again instead of losing the whole opening.
        val information = linkedMapOf<Int, HostOpeningSemanticValueV2>()
        val items = raw.getJSONArray("information")
        require(items.length() <= 64)
        repeat(items.length()) { index ->
            val item = items.getJSONObject(index)
            item.exact("seat", "roleId", "seats", "number")
            val seat = item.integer("seat")
            val subjects = item.getJSONArray("seats")
            val seats = (0 until subjects.length()).map { subjects.get(it) as? Int ?: 0 }
            if (seat !in information) domain.accepted(
                seat, if (item.isNull("roleId")) null else item.getString("roleId"), seats,
                item.nullableInteger("number"), drunk, bluffs.orEmpty(),
            )?.let { information[seat] = it }
        }
        domain.ruleDetermined(drunk).forEach { information.putIfAbsent(it.sourceSeat, it) }
        val entries = raw.getJSONArray("intentions")
        val intentions = (0 until entries.length()).mapNotNull { index ->
            val entry = entries.getJSONObject(index)
            entry.exact("trigger", "approach")
            runCatching {
                HostOpeningStrategicIntentionV2(entry.getString("trigger"), entry.getString("approach"))
            }.getOrNull()
        }.take(HostOpeningStrategyMemoryV2.MAX_ENTRIES)
        return HostOpeningSemanticAcceptanceV2(OpeningSelectionV2(request.snapshot.gameId,
            request.gameStateRevision, request.playerInputRevision, request.rosterFingerprint,
            drunk, red, bluffs, information.toSortedMap()),
            HostOpeningStrategyMemoryV2(emptyList(), intentions, emptyList()),
            raw.getString("rationale").take(MAX_RATIONALE_LENGTH).takeIf(String::isNotBlank))
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
        ruleset: ValidatedClocktowerRuleset,
        model: String,
        onStage: (String) -> Unit = {},
    ): AcceptedResponse {
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT) {
            "Unified opening requires personal-direct Responses support."
        }
        onStage("BUILDING_JOINT_OPENING_CONTEXT")
        val payload = encode(request, ruleset)
        val receipt = withContext(Dispatchers.IO) {
            PersonalDirectOpenAiV1.postRetained(accessToken, payload, model,
                onStage = onStage)
        }
        onStage("VALIDATING_COMPLETE_HOST_OPENING")
        return try {
            val accepted = decode(receipt.text, request, ruleset)
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
