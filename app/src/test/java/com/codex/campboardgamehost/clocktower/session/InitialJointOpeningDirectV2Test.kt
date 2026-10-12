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
        val rendered = host.toString()
        assertFalse(rendered.contains("legalCandidates"))
        assertFalse(rendered.contains("conditionalLegalScope"))
        assertFalse(rendered.contains("candidateId"))
        val responses = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            rendered, "gpt-5.6-luna"))
        assertEquals(InitialJointOpeningDirectV2.INSTRUCTIONS,
            responses.getString("instructions"))
        assertTrue("Strategy must look across nights rather than first-night only",
            InitialJointOpeningDirectV2.INSTRUCTIONS.contains("整局、多夜"))
        assertTrue("Strategy must generalize beyond special Drunk roles",
            InitialJointOpeningDirectV2.INSTRUCTIONS.contains("适用于所有角色和角色组合"))
        assertTrue("Future information cannot be precommitted as fabricated fact",
            InitialJointOpeningDirectV2.INSTRUCTIONS.contains("不能虚构将来的目标"))
        assertTrue("Prior intentions must be revisable rather than fixed",
            InitialJointOpeningDirectV2.INSTRUCTIONS.contains("KEEP / REVISE / RETIRE"))
        assertTrue(host.getJSONArray("rules").toString().contains("across nights"))
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
