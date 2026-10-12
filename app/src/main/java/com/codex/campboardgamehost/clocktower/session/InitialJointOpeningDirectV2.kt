package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject

/**
 * 3C/3D — ONE expert precommit planning request, before any actual Drunk is bound.
 *
 * The provider sees player-visible identities + the rule book, NOT exhaustive Host
 * candidate domains, witness registrations or hypothetical cartesian products.
 * Any returned decision is validated as an entire conditional opening against
 * HostOpeningRulesDomainV2, and a subsequent Host commit remains separate.
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
                "tensions" to array(field("string")),
                "intentions" to array(objectSchema(
                    "trigger" to field("string"),
                    "approach" to field("string"),
                    "risk" to field("string"),
                )),
                "uncertainties" to array(field("string")),
            ),
            "tradeoffSummary" to field("string"),
        )
    }

    const val INSTRUCTIONS = """
你是一位熟悉 Trouble Brewing 的资深血染钟楼说书人。你的目标不是让好人或邪恶获胜，而是创造公平、可推理、
具有多个合理世界，且适合当前玩家水平的游戏。请先在内部分析整局的善良信息生态、邪恶压力、跨角色线索交互、
隐士和间谍的合法登记歧义、酒鬼的全局影响、恶魔掩饰身份、占卜师红鲱鱼与玩家熟练度，然后联合选择唯一完整方案。
不要把每条线索拆开独立优化；不要套用固定座位或特例，也不要优先猜测标准答案。
当前全部玩家展示身份已经确定，但酒鬼（若有）尚未绑定。酒鬼必须与首夜其他信息、红鲱鱼和恶魔掩饰身份
在同一次请求中共同选择；不得要求 Host 先推荐酒鬼后再调用模型。
完整复制 requestIdentity 与 asOfRevision。每条首夜 information / deferredActions 必须使用
decisionInventory 中的 decisionId；不可编造未发生的玩家行动或已公布的线索。
真实且唯一由规则决定的结果使用 HOST_DETERMINED / RULE_DETERMINED / NONE，由 Host 计算。
现在可以选择的合法信息使用 DECISION，填具体可见观察值；显示为酒鬼的能力要明确失效。
图书管理员的「没有外来者」为 ZERO_OUTSIDERS，不能写成 NONE。
尚未发生的投毒与占卜师选定目标是事件依赖：若只表示等待则 DEFERRED / STATUS_DEPENDENT / NONE；
若为未来提交一个具体且未验证的全局协调意图，则 CONDITIONAL_INTENT / STATUS_DEPENDENT，
必须填写 waitingForEvents，Host 确认真实行动后重验。严禁预先假定某个目标。
多个登记见证只需要证明给玩家的可见结果合法，不得把一种解释冒充确定事实。
没有实际占卜师时不得选红鲱鱼。7人或以上需要三种合法且不在场的善良恶魔掩饰身份；否则为 null。
只返回符合 JSON Schema 的结构化数据和简短 private strategyMemory / tradeoffSummary；不写长篇解释。
"""

    fun encode(
        domain: HostOpeningRulesDomainV2,
        registry: ClocktowerCharacterRegistry,
        recentGames: List<String> = emptyList(),
    ): String {
        val scope = domain.scope
        val snapshot = scope.snapshot
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT)
        require(recentGames.size <= 4 && recentGames.all { it.length <= 250 })
        val infoRoles = setOf("washerwoman", "librarian", "investigator",
            "chef", "empath", "fortuneteller")
        val roles = registry.definitions
        val abilityCatalog = JSONObject().also { out ->
            roles.forEach { role ->
                out.put(role.externalId, JSONObject()
                    .put("name", role.name)
                    .put("effect", role.abilityText))
            }
        }
        val groups = JSONObject()
        for (team in ClocktowerCatalogTeam.entries) {
            groups.put(team.name, JSONArray(roles.filter { it.team == team }
                .map { it.externalId }))
        }
        val info = JSONArray()
        snapshot.grimoireSeats.sortedBy { it.seat }.forEach { seat ->
            val shown = (seat.shownRoleId as SnapshotField.Known<String>).value
            if (shown in infoRoles) info.put(JSONObject()
                .put("decisionId", "first-night:seat-" + seat.seat)
                .put("sourceSeat", seat.seat)
                .put("shownRoleId", shown))
        }
        val eventMap = linkedMapOf<String, String>()
        domain.projections.values.forEach { branch ->
            branch.deferredActions.forEach { (id, action) ->
                val previous = eventMap.putIfAbsent(id, action.first)
                require(previous == null || previous == action.first) {
                    "Inconsistent Host action identity across Drunk branches."
                }
            }
        }
        val pendingEvents = eventMap.values.toSet().sorted()
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            .put("schemaVersion", 2)
            .put("scriptId", snapshot.script.value)
            .put("requestIdentity", JSONObject()
                .put("gameId", snapshot.gameId)
                .put("gameStateRevision", scope.gameStateRevision)
                .put("playerInputRevision", scope.playerInputRevision)
                .put("rosterFingerprint", scope.rosterFingerprint))
            .put("roleCatalog", groups)
            .put("roleKnowledge", abilityCatalog)
            .put("rules", JSONArray(listOf(
                "Players learn shown roles; a Drunk actually has no Townsfolk ability.",
                "Healthy Chef/Empath numbers admit Spy/Recluse registration ambiguity.",
                "Poisoner first-night target is player-selected and not known before action.",
                "Fortune Teller selects two targets later; Recluse may register as a Demon.",
                "If a real Fortune Teller is present choose a Good red herring; a Drunk shown Fortune Teller is not real.",
                "Demon bluffs must be three distinct Good offboard roles when 7+ players.",
                "Every early information result must be consistent with the same whole-board opening.",
            )))
            .put("decisionInventory", JSONObject()
                .put("information", info)
                .put("events", JSONArray().also { arr ->
                    eventMap.forEach { (id, event) ->
                        arr.put(JSONObject().put("decisionId", id).put("event", event))
                    }
                }))
            .put("state", JSONObject()
                .put("asOfRevision", scope.gameStateRevision)
                .put("phase", "SETUP_PRECOMMIT")
                .put("hasDrunk", scope.hasDrunk)
                .put("drunkAssignment", if (scope.hasDrunk) "UNCOMMITTED" else "NOT_APPLICABLE")
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
                            .put("experience", scope.playerExperience.getValue(seat.seat).name))
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
        domain: HostOpeningRulesDomainV2,
    ): HostOpeningSemanticAcceptanceV2 {
        val raw = JSONObject(response)
        require(raw.getInt("schemaVersion") == 2)
        val drunk = raw.getJSONObject("openingPlan").let { plan ->
            if (plan.isNull("drunkSeat")) null else plan.getInt("drunkSeat")
        }
        return HostOpeningSemanticGateV2.accept(raw, domain.forDrunkSeat(drunk))
    }

    suspend fun recommend(
        endpoint: String,
        accessToken: String,
        domain: HostOpeningRulesDomainV2,
        registry: ClocktowerCharacterRegistry,
        model: String,
        recentGames: List<String> = emptyList(),
        onStage: (String) -> Unit = {},
    ): HostOpeningSemanticAcceptanceV2 {
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT) {
            "Unified opening requires personal-direct Responses support."
        }
        onStage("BUILDING_JOINT_OPENING_CONTEXT")
        val payload = encode(domain, registry, recentGames)
        val response = ProductionDrunkAiGatewayV1.post(
            endpoint, accessToken, payload, model, onStage,
        )
        onStage("VALIDATING_COMPLETE_HOST_OPENING")
        return decode(response, domain)
    }
}
