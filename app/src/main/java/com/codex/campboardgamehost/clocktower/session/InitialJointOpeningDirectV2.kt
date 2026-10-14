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
你是一位经验丰富的 Blood on the Clocktower（Trouble Brewing）真人说书人。
请根据本局完整阵容、各玩家的经验水平和 Host 已确认的事实，制定公平、有趣、可推理的整局、多夜策略。
从全局联合规划当前能决定的酒鬼人选及模拟线索、其他首夜信息、占卜师红鲱鱼和恶魔掩饰身份，
而不是把这些决策逐条孤立处理。state.hasDrunk 已由阵容确定：为 true 时须从合法镇民中选择酒鬼，
为 false 时 drunkSeat 为 null；不要改变阵容。
特别考虑信息之间的相互确认与冲突、公开身份后的叙事、Outsider 结构、合法登记歧义、
邪恶玩家受到的压力、Demon bluffs 的可扮演性，以及善良方能否循线推理和最终破解。
对不擅长编造假世界的新手邪恶玩家，应保留他们实际上能够利用的伪装与辩护空间；
但不能为此让善良方丧失公平、清晰的推理机会。避免逻辑精巧却对新手难以理解的过度设计。
提前考虑跨夜信息轨迹、潜在玩家选择与游戏状态变化，让后续策略有连续性又能据实修订；
尚未发生的行动与信息是可能性，不是事实。目标是使双方凭自己的选择影响输赢，而非偏袒一方。
输出唯一最推荐的整体方案；strategyMemory 仅保留关键跨角色张力、条件性跨夜意图与未知因素，
tradeoffSummary 简洁解释为何本方案适合当前玩家，而不是重复角色规则。

输出接口（严格遵守 Host 提供的合同；这不是逐角色规划任务）：
原样复制 requestIdentity、asOfRevision；schemaVersion=2。选择 conditionalDecisionContracts 中
与 openingPlan.drunkSeat 一致的一个分支；information 必须完整覆盖该分支的信息项，
逐项使用相同的 decisionId、sourceSeat、abilityRoleId、effectiveAbilityRoleId、resolution、
basis、waitingForEvents。deferredActions 完整复制该分支的 decisionId、event、actorSeat。
DECISION 填符合 Host 合法语义的当前观察值；HOST_DETERMINED 用 NONE 观察值及
RULE_DETERMINED；DEFERRED 用 NONE 观察值并保留给出的等待事件。
对应分支的 redHerringAllowedSeats 为空则 redHerringSeat=null，否则从中选择。
demonBluffRoleIds 在 Host 提供合法角色清单时选三个不同且未在场的善良身份，
否则为 null。FIXED_NEIGHBORS 的 subject.seats 为空；EVIDENCE_PAIR 填两个目标；
NONE / PLAYER_QUERY_PENDING 不填目标；没有用到的 number、roleId、boolean 填 null。
Librarian 观察到没有外来者时使用 ZERO_OUTSIDERS。
strategyMemory 的 tensions、intentions、uncertainties 各最多三项；各字符串非空且不超过
300 字，tradeoffSummary 不超过 1200 字。只返回符合 JSON Schema 的结构化结果。
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
        // Share only conditional structural requirements, not witness IDs,
        // candidate menus or a Cartesian product of complete opening plans.
        // The Host resolution status depends on the proposed Drunk branch.
        val conditionalContracts = JSONArray().also { branches ->
            domain.projections.forEach { (drunkSeat, projection) ->
                val discretionary = projection.branch.discretionary.associateBy { it.decisionId }
                val determined = projection.branch.determined.associateBy { it.decisionId }
                val deferred = projection.branch.deferred.associateBy { it.decisionId }
                val information = JSONArray()
                for (i in 0 until info.length()) {
                    val source = info.getJSONObject(i)
                    val id = source.getString("decisionId")
                    val sourceSeat = source.getInt("sourceSeat")
                    val shownRole = source.getString("shownRoleId")
                    val choice = discretionary[id]
                    val fixed = determined[id]
                    val waiting = deferred[id]
                    require(listOfNotNull(choice, fixed, waiting).size == 1) {
                        "Host conditional information inventory is not complete."
                    }
                    val resolution = when {
                        choice != null -> "DECISION"
                        fixed != null -> "HOST_DETERMINED"
                        else -> "DEFERRED"
                    }
                    val basis = when (resolution) {
                        "DEFERRED" -> "STATUS_DEPENDENT"
                        "HOST_DETERMINED" -> "RULE_DETERMINED"
                        else -> if (sourceSeat == drunkSeat) "DRUNK_MISINFORMATION"
                                else "FUNCTIONING"
                    }
                    val publicShapes = projection.candidatesByDecision[id]?.values.orEmpty()
                    val modes = if (choice != null) publicShapes.map { it.subjectMode }
                        .distinct().sorted() else listOf("NONE")
                    val kinds = if (choice != null) publicShapes.map { it.observationKind }
                        .distinct().sorted() else listOf("NONE")
                    require(modes.isNotEmpty() && kinds.isNotEmpty())
                    information.put(JSONObject()
                        .put("decisionId", id)
                        .put("sourceSeat", sourceSeat)
                        .put("abilityRoleId", shownRole)
                        .put("effectiveAbilityRoleId",
                            if (sourceSeat == drunkSeat) JSONObject.NULL else shownRole)
                        .put("resolution", resolution)
                        .put("basis", basis)
                        .put("subjectModes", JSONArray(modes))
                        .put("observationKinds", JSONArray(kinds))
                        .put("waitingForEvents", JSONArray(waiting?.waitingForEvents.orEmpty())))
                }
                val actions = JSONArray().also { out ->
                    projection.deferredActions.toSortedMap().forEach { (id, action) ->
                        out.put(JSONObject().put("decisionId", id)
                            .put("event", action.first)
                            .put("actorSeat", action.second ?: JSONObject.NULL))
                    }
                }
                branches.put(JSONObject()
                    .put("drunkSeat", drunkSeat ?: JSONObject.NULL)
                    .put("redHerringAllowedSeats", JSONArray(
                        projection.redHerringCandidatesBySeat.keys.sorted()))
                    .put("information", information)
                    .put("deferredActions", actions))
            }
        }
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
            .put("decisionInventory", JSONObject()
                .put("information", info)
                .put("events", JSONArray().also { arr ->
                    eventMap.forEach { (id, event) ->
                        arr.put(JSONObject().put("decisionId", id).put("event", event))
                    }
                }))
            .put("conditionalDecisionContracts", conditionalContracts)
            .put("legalDemonBluffRoleIds",
                scope.legalDemonBluffs?.let(::JSONArray) ?: JSONObject.NULL)
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
        return try {
            decode(response, domain).also { accepted ->
                BotcAiWireTrace.hostValidation(
                    "JOINT_OPENING", true, accepted.selection.choices.size)
            }
        } catch (error: Throwable) {
            BotcAiWireTrace.hostValidation("JOINT_OPENING", false, 0,
                error.message ?: error.javaClass.simpleName)
            throw error
        }
    }
}
