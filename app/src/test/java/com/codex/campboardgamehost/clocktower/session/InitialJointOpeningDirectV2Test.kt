package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
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
    @Test fun p1StoredSyntheticOpeningMustPassActualHostSemanticGate() {
        val domain = build(true, eight)
        val responseJson = """{"openingPlan":{"redHerringSeat":6,"drunkSeat":1,"deferredActions":[{"actorSeat":2,"decisionId":"event:fortune_teller.targets:seat-2","event":"fortune_teller.targets:seat-2"}],"information":[{"sourceSeat":1,"subject":{"mode":"NONE","seats":[]},"observation":{"number":0,"boolean":null,"kind":"NUMBER","roleId":null},"waitingForEvents":[],"decisionId":"first-night:seat-1","abilityRoleId":"chef","basis":"DRUNK_MISINFORMATION","resolution":"DECISION","effectiveAbilityRoleId":null},{"sourceSeat":2,"subject":{"mode":"NONE","seats":[]},"observation":{"number":null,"boolean":null,"kind":"NONE","roleId":null},"waitingForEvents":["fortune_teller.targets:seat-2"],"decisionId":"first-night:seat-2","abilityRoleId":"fortuneteller","basis":"STATUS_DEPENDENT","resolution":"DEFERRED","effectiveAbilityRoleId":"fortuneteller"},{"sourceSeat":5,"subject":{"mode":"EVIDENCE_PAIR","seats":[6,7]},"observation":{"number":null,"boolean":null,"kind":"ROLE","roleId":"baron"},"waitingForEvents":[],"decisionId":"first-night:seat-5","abilityRoleId":"investigator","basis":"FUNCTIONING","resolution":"DECISION","effectiveAbilityRoleId":"investigator"}],"demonBluffRoleIds":["empath","slayer","soldier"]},"schemaVersion":2,"requestIdentity":{"gameId":"new-game-before-drunk","gameStateRevision":0,"playerInputRevision":0,"rosterFingerprint":"931910b5df1400a33b5ed3fd84e3530dc02c2ffcea9707bab3215ea1d9807c19"},"strategyMemory":{"tensions":["调查员的 Baron 线索指向 6/7；真实 Baron 在 7，但 Recluse 可能造成登记歧义，避免把它当作无条件定罪。","1号 Chef 是酒鬼，其 0 不代表真实邻座邪恶配对数；其他玩家仍应能利用实际可验证线索。","2号 Fortune Teller 的首夜目标尚未提供，红鲱鱼设在 6号 Recluse；不要把未来选择或结果视为已发生。"],"intentions":[{"risk":"避免 Investigator 线索与后续信息过早合并成唯一结论，尤其考虑新手邪恶玩家的辩护空间。","trigger":"2号提交 Fortune Teller 目标后","approach":"按实际目标与红鲱鱼状态处理结果，并保留可由玩家质疑、交叉验证的解释路径。"},{"risk":"新手邪恶玩家可能难以维持复杂伪造信息。","trigger":"邪恶方选择公开身份或形成故事时","approach":"提供 Empath、Slayer、Soldier 作为简明可扮演的 bluffs；后续仍以实际行动和公开信息重新评估。"}],"uncertainties":["玩家未来会否公开 Chef 的 0、Investigator 的二人线索或 Fortune Teller 目标，尚未知。","尚无玩家行动、死亡或后续夜晚信息；不得将任何假设当作已发生事实。","Recluse 的登记可能性不等于已发生的特定登记；具体登记仍由说书人依情境决定。"]},"tradeoffSummary":"将 1号设为酒鬼以保留功能正常的 Investigator 与待选目标的 Fortune Teller，同时让 Chef 的 0 成为可被质疑但不具决定性的噪声。Investigat​​or 得到包含真实 Baron 与 Recluse 的二人线索，既提供可追踪的善方信息，也保留登记歧义带来的合理讨论空间；红鲱鱼设在 Recluse。选择易于新手邪恶玩家扮演的 bluffs，后续依据真实行动再调整。","asOfRevision":0}"""
        val accepted = InitialJointOpeningDirectV2.decode(responseJson, domain)
        assertEquals(1, accepted.selection.drunkSeat)
        assertTrue(accepted.selection.choices.isNotEmpty())
        assertTrue(accepted.strategyMemory?.intentions?.isNotEmpty() == true)
    }
}
