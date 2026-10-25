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

    private fun build(hasDrunk: Boolean, shown: List<String>): InitialOpeningRequestV2 {
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
        return InitialOpeningRequestV2.fromHostSnapshot(
            snapshot, registry,
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
        assertEquals(domain.rosterFingerprint,
            host.getJSONObject("requestIdentity").getString("rosterFingerprint"))
        val seats = host.getJSONObject("state").getJSONArray("seats")
        assertEquals(8, seats.length())
        assertEquals("UNCOMMITTED_IF_DRUNK_SELECTED", seats.getJSONObject(0)
            .getString("actualRoleId"))
        assertEquals("BEGINNER", seats.getJSONObject(6).getString("experience"))
        assertEquals(3, host.getJSONObject("decisionInventory")
            .getJSONArray("information").length())
        // No legal Drunk options or branch menus are exported to the model.
        assertFalse(host.has("conditionalDecisionContracts"))
        assertFalse(host.has("roleCatalog"))
        assertFalse(host.toString().contains("drunkSeat"))
        assertTrue(host.getJSONObject("state").getJSONArray("unknownEvents").length() > 0)
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

    @Test fun bothDrunkAndNoDrunkRequestsCarryOriginalHostFacts() {
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
        assertFalse(inputClean.getJSONObject("decisionInventory")
            .toString().contains("legalCandidates"))
    }

    @Test fun hostRejectsIncompleteAndStaleOpeningResponse() {
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
     * One actual authorized 2026-10-15 Standard Luna synthetic 15-seat output.
     * No raw provider response, encrypted reasoning, PII, or API key.
     * This tests the production response parser without making a new API call.
     */
    @Test fun p15StoredRealLunaModelOutputRetainsStrategyAndDeferredActions() {
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
        assertEquals(JSONObject(modelOutput).getString("tradeoffSummary"), accepted.tradeoffSummary)
        assertTrue(accepted.strategyMemory?.tensions?.isNotEmpty() == true)
    }

    /** Actual Luna P1 response from a synthetic eight-seat game: no provider metadata or API credentials. */
    /** Synthetic P1 model-generated plan, NO API key, raw provider body or personal records. */


    private fun directPlan(request: InitialOpeningRequestV2): JSONObject {
        val wire = JSONObject(InitialJointOpeningDirectV2.encode(request, ruleset.characterRegistry))
        val information = JSONArray()
        request.information.forEach { (id, seat) ->
            val role = request.shownRoles.getValue(seat)
            val waiting = if (role == "fortuneteller") listOf("fortune_teller.targets:seat-$seat") else emptyList()
            information.put(JSONObject().put("decisionId", id).put("sourceSeat", seat)
                .put("abilityRoleId", role).put("effectiveAbilityRoleId", role)
                .put("resolution", if (waiting.isEmpty()) "DECISION" else "DEFERRED")
                .put("basis", if (waiting.isEmpty()) "FUNCTIONING" else "STATUS_DEPENDENT")
                .put("subject", JSONObject().put("mode", if (role == "investigator") "EVIDENCE_PAIR" else "NONE")
                    .put("seats", JSONArray(if (role == "investigator") listOf(8, 7) else emptyList<Int>())))
                .put("observation", JSONObject().put("kind", when (role) {
                    "chef" -> "NUMBER"
                    "investigator" -> "ROLE"
                    else -> "NONE"
                }).put("number", if (role == "chef") 999 else JSONObject.NULL)
                    .put("roleId", if (role == "investigator") "saint" else JSONObject.NULL)
                    .put("boolean", JSONObject.NULL))
                .put("waitingForEvents", JSONArray(waiting)))
        }
        return JSONObject().put("schemaVersion", 2).put("asOfRevision", request.gameStateRevision)
            .put("requestIdentity", wire.getJSONObject("requestIdentity"))
            .put("openingPlan", JSONObject().put("drunkSeat", if (request.hasDrunk) 1 else JSONObject.NULL)
                .put("redHerringSeat", 8).put("demonBluffRoleIds", JSONArray(listOf("imp", "baron", "chef")))
                .put("information", information).put("deferredActions", JSONArray().also { actions ->
                    request.events.forEach { (id, action) -> actions.put(JSONObject().put("decisionId", id)
                        .put("event", action.first).put("actorSeat", action.second)) }
                }))
            .put("strategyMemory", JSONObject().put("tensions", JSONArray(listOf("Keep advice private")))
                .put("intentions", JSONArray()).put("uncertainties", JSONArray()))
            .put("tradeoffSummary", "Synthetic structural response; gameplay legality is not a Host gate.")
    }

    @Test fun directAdviceRetainsValuesOutsideRetiredLegalDomainsWithoutWitnessInference() {
        val request = build(true, eight)
        val accepted = InitialJointOpeningDirectV2.decode(directPlan(request).toString(), request)
        assertEquals(999, accepted.selection.information.getValue(1).number)
        assertEquals("saint", accepted.selection.information.getValue(5).roleId)
        assertEquals(listOf(7, 8), accepted.selection.information.getValue(5).subjectSeats)
        assertEquals(8, accepted.selection.redHerringSeat)
        assertEquals(listOf("imp", "baron", "chef"), accepted.selection.demonBluffs)
        assertTrue(accepted.selection.validFor(request))
        assertFalse(accepted.selection.validFor(build(false, eight)))
        assertEquals(2, accepted.conditional.size)
        assertFalse(accepted.selection.information.containsKey(2))
        assertEquals(SnapshotField.Uncommitted, request.snapshot.setupState.drunkAssignmentSeat)
        assertEquals(listOf("Keep advice private"), accepted.strategyMemory?.tensions)
    }

    private fun rejects(edit: (JSONObject) -> Unit) {
        val request = build(true, eight)
        val raw = directPlan(request).also(edit)
        assertTrue(runCatching { InitialJointOpeningDirectV2.decode(raw.toString(), request) }.isFailure)
    }

    @Test fun rejectsChangedGameRevisionAndRosterIdentity() {
        rejects { it.getJSONObject("requestIdentity").put("gameId", "another-game") }
        rejects { it.getJSONObject("requestIdentity").put("gameStateRevision", 1) }
        rejects { it.getJSONObject("requestIdentity").put("playerInputRevision", 1) }
        rejects { it.getJSONObject("requestIdentity").put("rosterFingerprint", "changed") }
        rejects { it.put("asOfRevision", 1) }
    }

    @Test fun rejectsMissingDuplicateAndInventedInformationOrActions() {
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").remove(0) }
        rejects { val a = it.getJSONObject("openingPlan").getJSONArray("information"); a.put(a.getJSONObject(0)) }
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(0).put("sourceSeat", 5) }
        rejects { it.getJSONObject("openingPlan").getJSONArray("deferredActions").remove(0) }
        rejects { it.getJSONObject("openingPlan").getJSONArray("deferredActions").getJSONObject(0).put("actorSeat", 8) }
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(1)
            .put("waitingForEvents", JSONArray(listOf("invented.event"))) }
    }

    @Test fun rejectsUnknownReferencesMalformedTypedValuesAndUnboundedStrategy() {
        rejects { it.getJSONObject("openingPlan").put("drunkSeat", 99) }
        rejects { it.getJSONObject("openingPlan").put("redHerringSeat", 99) }
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(2)
            .getJSONObject("subject").put("seats", JSONArray(listOf(8, 8))) }
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(2)
            .getJSONObject("observation").put("roleId", "unknown-role") }
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(0)
            .getJSONObject("observation").put("number", "999") }
        rejects { it.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(0)
            .getJSONObject("observation").put("number", 1.5) }
        rejects { it.getJSONObject("strategyMemory").put("uncertainties", JSONArray(List(4) { "Too many" })) }
    }

}
