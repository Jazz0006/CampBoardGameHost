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
        assertFalse(host.has("legalDemonBluffRoleIds"))
        assertFalse(host.toString().contains("redHerringAllowedSeats"))
        assertFalse(host.toString().contains("subjectModes"))
        assertFalse(host.toString().contains("observationKinds"))
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
     * P15: Kotlin Host precommit scope for the synthetic 15-seat Baron + Drunk.
     * Historical store:false request is retained byte-for-byte as the exact
     * paid P15 fixture; a separate store:true fixture now covers current
     * P1 production chaining. Neither fixture performs network calls.
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

        val retained = PersonalDirectOpenAiV1.buildRequest(hostPayload,
            PersonalDirectOpenAiV1.DEFAULT_MODEL, retainForContinuation = true)
        val liveRequest = JSONObject(retained)
        assertTrue(liveRequest.getBoolean("store"))
        assertFalse(liveRequest.has("previous_response_id"))
        assertEquals(request.getString("input"), liveRequest.getString("input"))
        File("build/p15/opening-app-retained-request.json").writeText(
            retained, Charsets.UTF_8)
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

}
