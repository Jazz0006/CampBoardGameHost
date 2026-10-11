package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class InitialGlobalOpeningScopeV1Test {
    private val shown = listOf("chef", "saint", "empath", "poisoner", "washerwoman", "imp")
    private val registry: ClocktowerCharacterRegistry by lazy {
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File(File("src/main/assets"), assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }

    private fun snapshot(hasDrunk: Boolean): TroubleBrewingGameSnapshotV1 =
        TroubleBrewingGameSnapshotV1(
            gameId = "opening-6",
            gameSeed = 12L,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                SnapshotField.NotApplicable,
                SnapshotField.NotApplicable,
            ),
            grimoireSeats = shown.mapIndexed { index, role ->
                TroubleBrewingSnapshotSeat(
                    seat = index + 1,
                    shownRoleId = SnapshotField.Known(role),
                    actualRoleId = if (hasDrunk && role in setOf("chef", "empath", "washerwoman")) {
                        SnapshotField.Uncommitted
                    } else SnapshotField.Known(role),
                    alive = SnapshotField.Known(true),
                    poisoned = SnapshotField.Known(false),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Known(hasDrunk),
                if (hasDrunk) SnapshotField.Uncommitted else SnapshotField.NotApplicable,
            ),
        )

    private fun branches(hasDrunk: Boolean): List<InitialOpeningHypothesisV1> =
        (if (hasDrunk) listOf(1, 3, 5) else listOf(null)).map { drunkSeat ->
            InitialOpeningHypothesisV1(
                drunkSeat = drunkSeat,
                discretionary = listOf(InitialOpeningDecisionV1(
                    "investigator", "PAIR_INFORMATION", 5,
                    mapOf("pair-a" to "A lawful pair result", "pair-b" to "Another lawful pair result"),
                )),
                determined = listOf(InitialOpeningDeterminedV1(
                    "setup.counts", "known-counts", "Host-determined setup count (not a model choice)",
                )),
                deferred = listOf(
                    InitialOpeningDeferredV1("poisoner-target", listOf("POISONER_TARGET_CONFIRMED")),
                    InitialOpeningDeferredV1("first-night:chef", listOf("POISONER_TARGET_CONFIRMED")),
                ),
            )
        }

    private fun scope(hasDrunk: Boolean): InitialGlobalOpeningScopeV1 =
        InitialGlobalOpeningScopeV1.fromHost(
            snapshot(hasDrunk), registry, branches(hasDrunk),
            legalDemonBluffs = null,
            playerExperience = mapOf(4 to OpeningPlayerExperienceV1.BEGINNER),
        )

    private fun valid(scope: InitialGlobalOpeningScopeV1, drunkSeat: Int?): JSONObject =
        JSONObject()
            .put("gameId", scope.snapshot.gameId)
            .put("gameStateRevision", scope.gameStateRevision)
            .put("playerInputRevision", scope.playerInputRevision)
            .put("rosterFingerprint", scope.rosterFingerprint)
            .put("scopeFingerprint", scope.scopeFingerprint)
            .put("drunkSeat", drunkSeat ?: JSONObject.NULL)
            .put("choices", JSONArray().put(JSONObject()
                .put("decisionId", "investigator").put("candidateId", "pair-a")))
            .put("demonBluffRoleIds", JSONObject.NULL)
            .put("deferredDecisionIds", JSONArray(
                listOf("poisoner-target", "first-night:chef"),
            ))

    @Test
    fun `drunk precommit preserves unknown actual roles and all conditional hypotheses`() {
        val scope = scope(true)
        assertEquals(setOf(1, 3, 5), scope.hypotheses.mapNotNull { it.drunkSeat }.toSet())
        val payload = scope.providerPayload()
        assertEquals("SETUP_PRECOMMIT", payload.getString("stage"))
        assertEquals("UNCOMMITTED", payload.getJSONArray("seats")
            .getJSONObject(0).getString("actualRoleId"))
        assertEquals("UNKNOWN", payload.getJSONArray("seats")
            .getJSONObject(0).getString("experience"))
        assertEquals("BEGINNER", payload.getJSONArray("seats")
            .getJSONObject(3).getString("experience"))
        assertEquals(3, payload.getJSONObject("conditionalLegalScope")
            .getJSONArray("hypotheses").length())
        val plan = InitialGlobalOpeningSelectionV1.decode(valid(scope, 1), scope)
        assertTrue(plan.validFor(scope))
        assertFalse(plan.validFor(scope.copyForDifferentRevision()))
    }

    @Test
    fun `no drunk has only no drunk branch and never manufactures a drunk`() {
        val scope = scope(false)
        assertEquals(listOf(null), scope.hypotheses.map { it.drunkSeat })
        assertEquals("chef", scope.providerPayload().getJSONArray("seats")
            .getJSONObject(0).getString("actualRoleId"))
        assertTrue(InitialGlobalOpeningSelectionV1.decode(valid(scope, null), scope).validFor(scope))
        val fabricated = valid(scope, 1)
        assertTrue(runCatching { InitialGlobalOpeningSelectionV1.decode(fabricated, scope) }.isFailure)
        assertTrue(runCatching {
            InitialGlobalOpeningScopeV1.fromHost(snapshot(false), registry, branches(true), null)
        }.isFailure)
    }

    @Test
    fun `a stale illegal partial or invented choice fails closed`() {
        val scope = scope(true)
        val missing = valid(scope, 1).put("choices", JSONArray())
        assertTrue(runCatching { InitialGlobalOpeningSelectionV1.decode(missing, scope) }.isFailure)
        val illegal = valid(scope, 1)
            .put("choices", JSONArray().put(JSONObject()
                .put("decisionId", "investigator").put("candidateId", "invented")))
        assertTrue(runCatching { InitialGlobalOpeningSelectionV1.decode(illegal, scope) }.isFailure)
        val deterministicSelected = valid(scope, 1)
            .put("choices", JSONArray().put(JSONObject()
                .put("decisionId", "investigator").put("candidateId", "pair-a"))
                .put(JSONObject().put("decisionId", "setup.counts").put("candidateId", "known-counts")))
        assertTrue(runCatching {
            InitialGlobalOpeningSelectionV1.decode(deterministicSelected, scope)
        }.isFailure)
        assertTrue(runCatching {
            InitialGlobalOpeningSelectionV1.decode(
                valid(scope, 1).put("rosterFingerprint", "stale"), scope,
            )
        }.isFailure)
    }

    @Test
    fun `unresolved Poisoner dependency cannot be dropped or guessed`() {
        val scope = scope(true)
        assertTrue(runCatching {
            InitialGlobalOpeningSelectionV1.decode(
                valid(scope, 1).put("deferredDecisionIds", JSONArray(listOf("first-night:chef"))),
                scope,
            )
        }.isFailure)
        assertTrue(runCatching {
            InitialGlobalOpeningSelectionV1.decode(
                valid(scope, 1).put("deferredDecisionIds",
                    JSONArray(listOf("poisoner-target", "first-night:chef", "imagined-death"))),
                scope,
            )
        }.isFailure)
        val defer = scope.hypothesis(1).deferred.first()
        assertEquals(listOf("POISONER_TARGET_CONFIRMED"), defer.waitingForEvents)
    }

    @Test
    fun `changed legal domains invalidate an otherwise valid opening selection`() {
        val scope = scope(true)
        val plan = InitialGlobalOpeningSelectionV1.decode(valid(scope, 1), scope)
        val changed = InitialGlobalOpeningScopeV1.fromHost(
            snapshot(true), registry,
            branches(true).map { branch ->
                if (branch.drunkSeat == 1) branch.copy(
                    discretionary = listOf(InitialOpeningDecisionV1(
                        "investigator", "PAIR_INFORMATION", 5,
                        mapOf("pair-b" to "Different legal outcome only"),
                    )),
                ) else branch
            },
            legalDemonBluffs = null,
            playerExperience = mapOf(4 to OpeningPlayerExperienceV1.BEGINNER),
        )
        assertEquals(scope.rosterFingerprint, changed.rosterFingerprint)
        assertFalse(plan.validFor(changed))
    }

    @Test
    fun `missing hypotheses and duplicate decision statuses are rejected`() {
        assertTrue(runCatching {
            InitialGlobalOpeningScopeV1.fromHost(snapshot(true), registry, branches(true).take(2), null)
        }.isFailure)
        assertTrue(runCatching {
            InitialOpeningHypothesisV1(
                drunkSeat = 1,
                discretionary = listOf(InitialOpeningDecisionV1(
                    "chef", "NUMBER", 1, mapOf("candidate" to "result"),
                )),
                determined = listOf(InitialOpeningDeterminedV1("chef", "1", "exact")),
                deferred = emptyList(),
            )
        }.isFailure)
    }

    private fun InitialGlobalOpeningScopeV1.copyForDifferentRevision(): InitialGlobalOpeningScopeV1 =
        InitialGlobalOpeningScopeV1.fromHost(snapshot(true), registry, branches(true),
            legalDemonBluffs = null, gameStateRevision = gameStateRevision + 1)
}
