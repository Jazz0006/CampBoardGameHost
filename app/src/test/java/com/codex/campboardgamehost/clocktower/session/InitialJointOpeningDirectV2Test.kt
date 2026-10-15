package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Wire/protocol acceptance only. No real Luna calls or strategy gold answers. */
class InitialJointOpeningDirectV2Test {
    private val ruleset by lazy {
        BuiltInClocktowerRulesetCatalog { path ->
            File(File("src/main/assets"), path).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing)
    }

    private fun build(hasDrunk: Boolean, shown: List<String>): HostOpeningRulesDomainV2 {
        val registry = ruleset.characterRegistry
        val snapshot = TroubleBrewingGameSnapshotV1(
            gameId = "new-game-before-drunk",
            gameSeed = 425,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                SnapshotField.NotApplicable, SnapshotField.NotApplicable),
            grimoireSeats = shown.mapIndexed { index, role ->
                val team = requireNotNull(registry.findByExternalId(role)).team
                TroubleBrewingSnapshotSeat(
                    seat = index + 1,
                    shownRoleId = SnapshotField.Known(role),
                    actualRoleId = if (hasDrunk &&
                        team == ClocktowerCatalogTeam.TOWNSFOLK)
                        SnapshotField.Uncommitted else SnapshotField.Known(role),
                    alive = SnapshotField.Known(true), poisoned = SnapshotField.Known(false),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Known(hasDrunk),
                if (hasDrunk) SnapshotField.Uncommitted else SnapshotField.NotApplicable,
            ),
        )
        return HostOpeningRulesDomainV2.fromHostSnapshot(
            snapshot, registry, clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            playerExperience = mapOf(7 to OpeningPlayerExperienceV1.BEGINNER,
                8 to OpeningPlayerExperienceV1.BEGINNER),
            gameStateRevision = 0, playerInputRevision = 0,
        )
    }

    private val eight = listOf("chef", "fortuneteller", "ravenkeeper", "butler",
        "investigator", "recluse", "baron", "imp")

    @Test fun initialRequestReallyStartsBeforeDrunkAndDoesNotSendCandidateMenus() {
        val domain = build(true, eight)
        val host = JSONObject(InitialJointOpeningDirectV2.encode(
            domain, ruleset.characterRegistry))
        assertEquals("SETUP_PRECOMMIT", host.getJSONObject("state").getString("phase"))
        assertTrue(host.getJSONObject("state").getBoolean("hasDrunk"))
        assertEquals("UNCOMMITTED", host.getJSONObject("state").getString("drunkAssignment"))
        assertEquals(0, host.getJSONObject("requestIdentity").getInt("gameStateRevision"))
        assertEquals(domain.scope.rosterFingerprint,
            host.getJSONObject("requestIdentity").getString("rosterFingerprint"))
        val seats = host.getJSONObject("state").getJSONArray("seats")
        assertEquals(8, seats.length())
        assertEquals("UNCOMMITTED_IF_DRUNK_SELECTED", seats.getJSONObject(0)
            .getString("actualRoleId"))
        assertEquals("BEGINNER", seats.getJSONObject(6).getString("experience"))
        assertEquals(3, host.getJSONObject("decisionInventory")
            .getJSONArray("information").length())
        // The model sees the Host's branch-dependent protocol obligations,
        // without exhaustive legal candidate IDs or registration witnesses.
        val contracts = host.getJSONArray("conditionalDecisionContracts")
        assertEquals(domain.projections.size, contracts.length())
        for (index in 0 until contracts.length()) {
            val contract = contracts.getJSONObject(index)
            val drunkSeat = if (contract.isNull("drunkSeat")) null
                else contract.getInt("drunkSeat")
            val branch = domain.forDrunkSeat(drunkSeat)
            val expectedStatus = buildMap<String, String> {
                branch.branch.discretionary.filter { it.decisionId != "setup.red-herring" }
                    .forEach { put(it.decisionId, "DECISION") }
                branch.branch.determined.filter { it.decisionId in branch.determinedInformationIds }
                    .forEach { put(it.decisionId, "HOST_DETERMINED") }
                branch.branch.deferred.filter { it.decisionId.startsWith("first-night:") }
                    .forEach { put(it.decisionId, "DEFERRED") }
            }
            val information = contract.getJSONArray("information")
            assertEquals(expectedStatus.size, information.length())
            for (i in 0 until information.length()) {
                val entry = information.getJSONObject(i)
                assertEquals(expectedStatus[entry.getString("decisionId")],
                    entry.getString("resolution"))
                assertEquals(entry.getInt("sourceSeat") == drunkSeat,
                    entry.isNull("effectiveAbilityRoleId"))
                val deferred = branch.branch.deferred.singleOrNull {
                    it.decisionId == entry.getString("decisionId")
                }
                assertEquals(deferred?.waitingForEvents.orEmpty(),
                    (0 until entry.getJSONArray("waitingForEvents").length()).map { j ->
                        entry.getJSONArray("waitingForEvents").getString(j)
                    })
            }
            val actions = contract.getJSONArray("deferredActions")
            assertEquals(branch.deferredActions.size, actions.length())
        }
        assertEquals(domain.scope.legalDemonBluffs,
            (0 until host.getJSONArray("legalDemonBluffRoleIds").length())
                .map { host.getJSONArray("legalDemonBluffRoleIds").getString(it) })
        val rendered = host.toString()
        assertFalse(rendered.contains("legalCandidates"))
        assertFalse(rendered.contains("conditionalLegalScope"))
        assertFalse(rendered.contains("candidateId"))
        val responses = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            rendered, PersonalDirectOpenAiV1.DEFAULT_MODEL))
        assertEquals(InitialJointOpeningDirectV2.INSTRUCTIONS,
            responses.getString("instructions"))
        assertEquals("gpt-6-luna", responses.getString("model"))
        assertEquals(rendered, responses.getString("input"))
        assertFalse(host.has("roleKnowledge"))
        assertFalse(host.has("rules"))
        assertFalse(responses.getBoolean("store"))
        assertTrue(responses.getJSONObject("text").getJSONObject("format")
            .getBoolean("strict"))
    }

    @Test fun bothDrunkAndNoDrunkCasesMustBeBoundToTheirOriginalHostDomain() {
        val drunk = build(true, eight)
        val noDrunk = build(false, eight)
        val inputDrunk = JSONObject(InitialJointOpeningDirectV2.encode(
            drunk, ruleset.characterRegistry))
        val inputClean = JSONObject(InitialJointOpeningDirectV2.encode(
            noDrunk, ruleset.characterRegistry))
        assertTrue(inputDrunk.getJSONObject("state").getBoolean("hasDrunk"))
        assertFalse(inputClean.getJSONObject("state").getBoolean("hasDrunk"))
        assertEquals("NOT_APPLICABLE", inputClean.getJSONObject("state")
            .getString("drunkAssignment"))
        assertEquals(1, noDrunk.projections.size)
        assertEquals(4, drunk.projections.size)
        assertFalse(inputClean.getJSONObject("decisionInventory")
            .toString().contains("legalCandidates"))
    }

    @Test fun hostMustRejectIncompleteMalformedOrInventedV2PlanAsWhole() {
        val domain = build(true, eight)
        val identity = JSONObject(InitialJointOpeningDirectV2.encode(
            domain, ruleset.characterRegistry)).getJSONObject("requestIdentity")
        fun output(drunk: Int?) = JSONObject()
            .put("schemaVersion", 2)
            .put("asOfRevision", 0)
            .put("requestIdentity", JSONObject(identity.toString()))
            .put("openingPlan", JSONObject()
                .put("drunkSeat", drunk ?: JSONObject.NULL)
                .put("redHerringSeat", JSONObject.NULL)
                .put("demonBluffRoleIds", JSONArray())
                .put("information", JSONArray())
                .put("deferredActions", JSONArray()))
            .put("strategyMemory", JSONObject()
                .put("tensions", JSONArray()).put("intentions", JSONArray())
                .put("uncertainties", JSONArray()))
            .put("tradeoffSummary", "Synthetic only.")
        assertTrue(runCatching {
            InitialJointOpeningDirectV2.decode(output(1).toString(), domain)
        }.isFailure)
        assertTrue(runCatching {
            InitialJointOpeningDirectV2.decode(output(9).toString(), domain)
        }.isFailure)
        assertTrue(runCatching {
            InitialJointOpeningDirectV2.decode(output(null).toString(), domain)
        }.isFailure)
        assertTrue(runCatching {
            val stale = output(1)
            stale.getJSONObject("requestIdentity").put("gameId", "other-game")
            InitialJointOpeningDirectV2.decode(stale.toString(), domain)
        }.isFailure)
    }
    /**
     * P15: exactly the normal Kotlin Host precommit scope and Android V2
     * buildRequest for the synthetic 15-seat Baron + Drunk scenario.
     * No mock model response, API key, paid call or production change.
     */
    @Test fun exportFifteenSeatOpeningWireWhenOptedIn() {
        if (System.getenv("BOTC_15_WIRE_EXPORT") != "1") return
        val shown = listOf(
            "chef", "fortuneteller", "recluse", "investigator", "poisoner",
            "monk", "washerwoman", "baron", "butler", "undertaker",
            "imp", "librarian", "saint", "empath", "scarletwoman",
        )
        val domain = build(true, shown)
        val hostPayload = InitialJointOpeningDirectV2.encode(
            domain, ruleset.characterRegistry,
        )
        val host = JSONObject(hostPayload)
        assertEquals(15, host.getJSONObject("state").getJSONArray("seats").length())
        assertEquals(8, host.getJSONArray("conditionalDecisionContracts").length())
        assertEquals("SETUP_PRECOMMIT",
            host.getJSONObject("state").getString("phase"))
        assertEquals(6, host.getJSONObject("decisionInventory")
            .getJSONArray("information").length())
        val wire = PersonalDirectOpenAiV1.buildRequest(
            hostPayload, PersonalDirectOpenAiV1.DEFAULT_MODEL,
        )
        val request = JSONObject(wire)
        assertEquals("gpt-6-luna", request.getString("model"))
        assertFalse(request.getBoolean("store"))
        assertFalse(request.has("service_tier"))
        val output = File("build/p15/opening-app-request.json")
        output.parentFile.mkdirs()
        output.writeText(wire, Charsets.UTF_8)
    }

    /**
     * One actual authorized 2026-10-15 Standard Luna synthetic 15-seat output.
     * No raw provider response, encrypted reasoning, PII, or API key.
     * This tests the same decode/Host legal gate used by the production V2 path.
     */
    @Test fun p15StoredRealLunaModelOutputPassesKotlinHostGate() {
        val shown = listOf(
            "chef", "fortuneteller", "recluse", "investigator", "poisoner",
            "monk", "washerwoman", "baron", "butler", "undertaker",
            "imp", "librarian", "saint", "empath", "scarletwoman",
        )
        val domain = build(true, shown)
        val modelOutput = """{
  "openingPlan": {
    "redHerringSeat": 10,
    "drunkSeat": 4,
    "deferredActions": [
      {
        "actorSeat": 2,
        "decisionId": "event:fortune_teller.targets:seat-2",
        "event": "fortune_teller.targets:seat-2"
      },
      {
        "actorSeat": 5,
        "decisionId": "event:poisoner.target",
        "event": "poisoner.target"
      }
    ],
    "information": [
      {
        "sourceSeat": 1,
        "subject": {
          "mode": "NONE",
          "seats": []
        },
        "observation": {
          "number": null,
          "boolean": null,
          "kind": "NONE",
          "roleId": null
        },
        "waitingForEvents": [
          "poisoner.target"
        ],
        "decisionId": "first-night:seat-1",
        "abilityRoleId": "chef",
        "basis": "STATUS_DEPENDENT",
        "resolution": "DEFERRED",
        "effectiveAbilityRoleId": "chef"
      },
      {
        "sourceSeat": 2,
        "subject": {
          "mode": "NONE",
          "seats": []
        },
        "observation": {
          "number": null,
          "boolean": null,
          "kind": "NONE",
          "roleId": null
        },
        "waitingForEvents": [
          "poisoner.target",
          "fortune_teller.targets:seat-2"
        ],
        "decisionId": "first-night:seat-2",
        "abilityRoleId": "fortuneteller",
        "basis": "STATUS_DEPENDENT",
        "resolution": "DEFERRED",
        "effectiveAbilityRoleId": "fortuneteller"
      },
      {
        "sourceSeat": 4,
        "subject": {
          "mode": "NONE",
          "seats": []
        },
        "observation": {
          "number": null,
          "boolean": null,
          "kind": "NONE",
          "roleId": null
        },
        "waitingForEvents": [
          "poisoner.target"
        ],
        "decisionId": "first-night:seat-4",
        "abilityRoleId": "investigator",
        "basis": "STATUS_DEPENDENT",
        "resolution": "DEFERRED",
        "effectiveAbilityRoleId": null
      },
      {
        "sourceSeat": 7,
        "subject": {
          "mode": "NONE",
          "seats": []
        },
        "observation": {
          "number": null,
          "boolean": null,
          "kind": "NONE",
          "roleId": null
        },
        "waitingForEvents": [
          "poisoner.target"
        ],
        "decisionId": "first-night:seat-7",
        "abilityRoleId": "washerwoman",
        "basis": "STATUS_DEPENDENT",
        "resolution": "DEFERRED",
        "effectiveAbilityRoleId": "washerwoman"
      },
      {
        "sourceSeat": 12,
        "subject": {
          "mode": "NONE",
          "seats": []
        },
        "observation": {
          "number": null,
          "boolean": null,
          "kind": "NONE",
          "roleId": null
        },
        "waitingForEvents": [
          "poisoner.target"
        ],
        "decisionId": "first-night:seat-12",
        "abilityRoleId": "librarian",
        "basis": "STATUS_DEPENDENT",
        "resolution": "DEFERRED",
        "effectiveAbilityRoleId": "librarian"
      },
      {
        "sourceSeat": 14,
        "subject": {
          "mode": "NONE",
          "seats": []
        },
        "observation": {
          "number": null,
          "boolean": null,
          "kind": "NONE",
          "roleId": null
        },
        "waitingForEvents": [
          "poisoner.target"
        ],
        "decisionId": "first-night:seat-14",
        "abilityRoleId": "empath",
        "basis": "STATUS_DEPENDENT",
        "resolution": "DEFERRED",
        "effectiveAbilityRoleId": "empath"
      }
    ],
    "demonBluffRoleIds": [
      "soldier",
      "slayer",
      "mayor"
    ]
  },
  "schemaVersion": 2,
  "requestIdentity": {
    "gameId": "new-game-before-drunk",
    "gameStateRevision": 0,
    "playerInputRevision": 0,
    "rosterFingerprint": "76f97603eb59ada11465766b60c6dad7450747b963054597f4878ea9a1ef4309"
  },
  "strategyMemory": {
    "tensions": [
      "Drunk Investigator的线索可给新手邪恶玩家留下辩护空间；若后续与其他信息吻合，再评估其可信度，不预先视为已发生。",
      "Recluse的登记歧义可能影响信息核验，但其具体登记尚未决定。",
      "首夜多个信息角色可能形成交叉验证；Poisoner目标未确定，需结合实际状态判断信息可靠性。"
    ],
    "intentions": [
      {
        "risk": "避免把可疑线索过早当作锁定邪恶的证据。",
        "trigger": "首夜信息与公开声明开始交叉验证时",
        "approach": "区分酒鬼误导、投毒影响及Recluse登记可能性，并保留可追踪的验证路径。"
      },
      {
        "risk": "初学者邪恶玩家可能难以维持复杂伪装。",
        "trigger": "邪恶方选择公开身份或解释信息时",
        "approach": "优先检验简单、连贯且能随夜晚延续的说法，不把假设行动当作既成事实。"
      }
    ],
    "uncertainties": [
      "Poisoner的目标尚未决定。",
      "Fortune Teller的首夜目标尚未决定。",
      "未来玩家会如何公开身份、交换信息及投票尚未知。"
    ]
  },
  "tradeoffSummary": "将座位4设为Drunk，削弱最可能直接指向邪恶阵营的调查线索，同时保留其余首夜信息供善良阵营推理；座位10作为Red Herring提供可追踪但非决定性的后续信号。邪恶伪装选用较易扮演的善良身份。首夜观察均待Poisoner及相关目标行动确认后再决定。"
,
  "asOfRevision": 0
}"""
        val accepted = InitialJointOpeningDirectV2.decode(modelOutput, domain)
        assertEquals(4, accepted.selection.drunkSeat)
        assertTrue(accepted.strategyMemory?.tensions?.isNotEmpty() == true)
    }

    /**
     * P1 nonsecret, opt-in fixture. This is an ACTUAL Kotlin V2 encoder +
     * PersonalDirectOpenAiV1.buildRequest output, not a hand-built wire request.
     * The fixture is synthetic and cannot itself certify model Host acceptance.
     */
    @Test fun exportP1OpeningWireWhenOptedIn() {
        if (System.getenv("BOTC_P1_WIRE_EXPORT") != "1") return
        val domain = build(true, eight)
        val hostPayload = InitialJointOpeningDirectV2.encode(
            domain, ruleset.characterRegistry,
        )
        val wire = PersonalDirectOpenAiV1.buildRequest(
            hostPayload, PersonalDirectOpenAiV1.DEFAULT_MODEL,
        )
        val output = File("build/p1/opening-app-request.json")
        output.parentFile.mkdirs()
        output.writeText(wire, Charsets.UTF_8)
        assertEquals("gpt-6-luna", JSONObject(wire).getString("model"))
        assertFalse(JSONObject(wire).getBoolean("store"))
    }
    /** Actual Luna P1 response from a synthetic eight-seat game: no provider metadata or API credentials. */
    /** Synthetic P1 model-generated plan, NO API key, raw provider body or personal records. */
    private val p1StoredSyntheticModelPlan = """{"openingPlan":{"redHerringSeat":6,"drunkSeat":1,"deferredActions":[{"actorSeat":2,"decisionId":"event:fortune_teller.targets:seat-2","event":"fortune_teller.targets:seat-2"}],"information":[{"sourceSeat":1,"subject":{"mode":"NONE","seats":[]},"observation":{"number":0,"boolean":null,"kind":"NUMBER","roleId":null},"waitingForEvents":[],"decisionId":"first-night:seat-1","abilityRoleId":"chef","basis":"DRUNK_MISINFORMATION","resolution":"DECISION","effectiveAbilityRoleId":null},{"sourceSeat":2,"subject":{"mode":"NONE","seats":[]},"observation":{"number":null,"boolean":null,"kind":"NONE","roleId":null},"waitingForEvents":["fortune_teller.targets:seat-2"],"decisionId":"first-night:seat-2","abilityRoleId":"fortuneteller","basis":"STATUS_DEPENDENT","resolution":"DEFERRED","effectiveAbilityRoleId":"fortuneteller"},{"sourceSeat":5,"subject":{"mode":"EVIDENCE_PAIR","seats":[6,7]},"observation":{"number":null,"boolean":null,"kind":"ROLE","roleId":"baron"},"waitingForEvents":[],"decisionId":"first-night:seat-5","abilityRoleId":"investigator","basis":"FUNCTIONING","resolution":"DECISION","effectiveAbilityRoleId":"investigator"}],"demonBluffRoleIds":["empath","slayer","soldier"]},"schemaVersion":2,"requestIdentity":{"gameId":"new-game-before-drunk","gameStateRevision":0,"playerInputRevision":0,"rosterFingerprint":"931910b5df1400a33b5ed3fd84e3530dc02c2ffcea9707bab3215ea1d9807c19"},"strategyMemory":{"tensions":["调查员的 Baron 线索指向 6/7；真实 Baron 在 7，但 Recluse 可能造成登记歧义，避免把它当作无条件定罪。","1号 Chef 是酒鬼，其 0 不代表真实邻座邪恶配对数；其他玩家仍应能利用实际可验证线索。","2号 Fortune Teller 的首夜目标尚未提供，红鲱鱼设在 6号 Recluse；不要把未来选择或结果视为已发生。"],"intentions":[{"risk":"避免 Investigator 线索与后续信息过早合并成唯一结论，尤其考虑新手邪恶玩家的辩护空间。","trigger":"2号提交 Fortune Teller 目标后","approach":"按实际目标与红鲱鱼状态处理结果，并保留可由玩家质疑、交叉验证的解释路径。"},{"risk":"新手邪恶玩家可能难以维持复杂伪造信息。","trigger":"邪恶方选择公开身份或形成故事时","approach":"提供 Empath、Slayer、Soldier 作为简明可扮演的 bluffs；后续仍以实际行动和公开信息重新评估。"}],"uncertainties":["玩家未来会否公开 Chef 的 0、Investigator 的二人线索或 Fortune Teller 目标，尚未知。","尚无玩家行动、死亡或后续夜晚信息；不得将任何假设当作已发生事实。","Recluse 的登记可能性不等于已发生的特定登记；具体登记仍由说书人依情境决定。"]},"tradeoffSummary":"将 1号设为酒鬼以保留功能正常的 Investigator 与待选目标的 Fortune Teller，同时让 Chef 的 0 成为可被质疑但不具决定性的噪声。Investigat​​or 得到包含真实 Baron 与 Recluse 的二人线索，既提供可追踪的善方信息，也保留登记歧义带来的合理讨论空间；红鲱鱼设在 Recluse。选择易于新手邪恶玩家扮演的 bluffs，后续依据真实行动再调整。","asOfRevision":0}"""

    @Test fun p1StoredSyntheticOpeningMustPassActualHostSemanticGate() {
        val domain = build(true, eight)
        val responseJson = p1StoredSyntheticModelPlan
        val accepted = InitialJointOpeningDirectV2.decode(responseJson, domain)
        assertEquals(1, accepted.selection.drunkSeat)
        assertTrue(accepted.selection.choices.isNotEmpty())
        assertTrue(accepted.strategyMemory?.intentions?.isNotEmpty() == true)
    }

    /** Exact Kotlin continuation wire for the same synthetic committed Host roster.
     *  No fabricated open legal decisions: this P1 checkpoint tests strategic
     *  memory continuity only, not later-night legal candidate quality.
     */
    @Test fun exportP1KotlinContinuationFromAcceptedSyntheticOpening() {
        if (System.getenv("BOTC_P1_WIRE_EXPORT") != "1") return
        val accepted = InitialJointOpeningDirectV2.decode(
            p1StoredSyntheticModelPlan, build(true, eight),
        )
        val players = eight.mapIndexed { index, shown ->
            val seat = index + 1
            val actual = if (seat == accepted.selection.drunkSeat) "drunk" else shown
            val type = when (actual) {
                "drunk", "butler", "recluse" -> CharacterType.OUTSIDER
                "baron" -> CharacterType.MINION
                "imp" -> CharacterType.DEMON
                else -> CharacterType.TOWNSFOLK
            }
            PlayerState(
                seat = seat,
                name = "Seat $seat",
                actualRole = RoleId(actual),
                shownRole = RoleId(shown),
                actualAlignment = if (type == CharacterType.MINION ||
                    type == CharacterType.DEMON) Alignment.EVIL else Alignment.GOOD,
                actualType = type,
            )
        }
        val session = ClocktowerGameSession.createProduction(
            "new-game-before-drunk", 425,
            GameState(ScriptId("trouble_brewing"), players, 425),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val continuationHostPayload = StorytellerStrategicContinuationV2.encode(
            session.state, accepted, emptyList(),
        )
        // Actual second-call Luna output against this exact Kotlin V2 payload,
        // extracted from the private synthetic P1 receipt (no provider metadata).
        val continuedPlan = """{"schemaVersion":2,"updatedStrategyMemory":{"tensions":["1号实际是 Drunk；Chef 的 0 是误导信息，不能当作真实邻座邪恶配对数。","5号 Investigator 的 6/7线索包含真实 Baron，但 Recluse 的登记歧义仍不应被忽略。","没有新的行动、声明或观察记录；2号 Fortune Teller 的目标和结果仍未知。"],"intentions":[{"risk":"不要把 Investigator 线索与后续信息过早合并成唯一结论。","trigger":"2号提交 Fortune Teller 目标后","approach":"根据实际目标与红鲱鱼状态处理信息，并保留可交叉验证的解释路径。"},{"risk":"新手邪恶玩家可能难以维持复杂伪造信息。","trigger":"邪恶方选择公开身份或形成故事时","approach":"保留简明 bluffs 作为可用战略假说；根据实际行动和公开信息再评估。"}],"uncertainties":["当前没有因果历史条目或公开声明；未记录的行动、登记和观察仍未知。","Recluse 可能登记为邪恶角色，但没有已确认的具体登记裁定。","尚无 Fortune Teller 目标、后续夜晚或玩家反应记录。"]},"strategicDirection":"保持既有开局战略，不把尚未发生的行动或信息当作事实；等待实际目标、声明和行动记录后再联合评估多种局势。","revisions":[{"reason":"没有新的目标提交或结果记录，原有条件策略仍适用。","priorIndex":0,"verdict":"KEEP"},{"reason":"没有公开声明或邪恶方行动记录，尚无依据改变针对新手玩家的简明伪装策略。","priorIndex":1,"verdict":"KEEP"}],"source":{"gameId":"new-game-before-drunk","gameStateRevision":0,"playerInputRevision":0,"memoryVersion":0,"exclusiveGlobalSequence":0},"affectedOpenDecisionIds":[],"remainingChoices":[]}"""
        val checked = StorytellerStrategicContinuationV2.decode(
            continuationHostPayload, continuedPlan,
        )
        assertEquals("new-game-before-drunk", checked.gameId)
        assertEquals(listOf("KEEP", "KEEP"), checked.revisions.map { it.verdict })
        assertTrue(checked.remainingChoices.isEmpty())
        val outputWire = PersonalDirectOpenAiV1.buildRequest(
            continuationHostPayload, "gpt-6-luna",
        )
        val decoded = JSONObject(outputWire)
        assertEquals("gpt-6-luna", decoded.getString("model"))
        assertFalse(decoded.getBoolean("store"))
        assertEquals("botc.whole-game-strategy-continuation-v2",
            JSONObject(decoded.getString("input")).getString("schemaId"))
        assertEquals("new-game-before-drunk",
            JSONObject(decoded.getString("input")).getJSONObject("source").getString("gameId"))
        assertEquals(0, JSONObject(decoded.getString("input"))
            .getJSONArray("openDecisions").length())
        val output = File("build/p1/continuation-app-request.json")
        output.parentFile.mkdirs()
        output.writeText(outputWire, Charsets.UTF_8)
    }
}
