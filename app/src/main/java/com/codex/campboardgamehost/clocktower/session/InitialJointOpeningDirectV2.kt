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
原样复制 requestIdentity、asOfRevision；schemaVersion=2。选择 conditionalDecisionContracts 中
与 openingPlan.drunkSeat 一致的一个分支；information 必须完整覆盖该分支的信息项，
逐项使用相同的 decisionId、sourceSeat、abilityRoleId、effectiveAbilityRoleId、resolution、
basis、waitingForEvents。deferredActions 完整复制该分支的 decisionId、event、actorSeat。
DECISION 填符合 Host 合法语义的当前观察值；HOST_DETERMINED 用 NONE 观察值及
RULE_DETERMINED；DEFERRED 用 NONE 观察值并保留给出的等待事件。
对应分支的 redHerringAllowedSeats 为空则 redHerringSeat=null，否则从中选择。
demonBluffRoleIds 在 Host 提供合法角色清单时选三个不同且未在场的善良身份，
否则为 null。FIXED_NEIGHBORS 的 subject.seats 为空；EVIDENCE_PAIR 只填两个座位，
配合 observation.kind=ROLE 与 roleId 表示该身份在二人之一；不另输出固定规则文案。
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
