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
        assertTrue(InitialJointOpeningDirectV2.INSTRUCTIONS.contains("整局、多夜"))
        assertTrue(InitialJointOpeningDirectV2.INSTRUCTIONS.contains("新手邪恶玩家"))
        assertTrue(InitialJointOpeningDirectV2.INSTRUCTIONS.contains("善良方"))
        assertTrue(InitialJointOpeningDirectV2.INSTRUCTIONS.contains("尚未发生的行动"))
        assertTrue(InitialJointOpeningDirectV2.INSTRUCTIONS.contains("conditionalDecisionContracts"))
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
}
