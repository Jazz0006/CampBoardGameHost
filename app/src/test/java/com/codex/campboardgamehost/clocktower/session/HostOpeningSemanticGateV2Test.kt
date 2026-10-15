package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
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

/** Synthetic Host domain only: real rules-owned candidate generation is a separate gate. */
class HostOpeningSemanticGateV2Test {
    private val registry by lazy {
        BuiltInClocktowerRulesetCatalog { path ->
            File(File("src/main/assets"), path).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
    private val shown = listOf(
        "washerwoman", "librarian", "chef", "empath",
        "fortuneteller", "recluse", "poisoner", "imp",
    )
    private val known = shown.toSet() + setOf("monk", "soldier", "mayor", "slayer")

    private fun snapshot() = TroubleBrewingGameSnapshotV1(
        gameId = "3b3d-fixture", gameSeed = 71,
        position = TroubleBrewingSnapshotPosition(
            TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
            SnapshotField.NotApplicable, SnapshotField.NotApplicable,
        ),
        grimoireSeats = shown.mapIndexed { index, role ->
            TroubleBrewingSnapshotSeat(
                seat = index + 1, shownRoleId = SnapshotField.Known(role),
                actualRoleId = SnapshotField.Known(role),
                alive = SnapshotField.Known(true), poisoned = SnapshotField.Known(false),
            )
        },
        setupState = TroubleBrewingSnapshotSetupState(
            SnapshotField.Known(false), SnapshotField.NotApplicable),
    )

    private fun scope() = InitialGlobalOpeningScopeV1.fromHost(
        snapshot(), registry,
        hypotheses = listOf(InitialOpeningHypothesisV1(
            drunkSeat = null,
            discretionary = listOf(
                InitialOpeningDecisionV1("first-night:seat-1", "PAIR_INFORMATION", 1,
                    mapOf("ww-3-4-empath" to "Host pair result")),
                InitialOpeningDecisionV1("first-night:seat-3", "NUMBER", 3,
                    mapOf("chef-1" to "Host numeric result")),
                InitialOpeningDecisionV1("setup.red-herring", "RED_HERRING", null,
                    mapOf("rh-1" to "Good seat 1", "rh-6" to "Good seat 6")),
            ),
            determined = listOf(InitialOpeningDeterminedV1(
                "setup.counts", "actual-5-1-1-1", "Host owns setup counts")),
            deferred = listOf(
                InitialOpeningDeferredV1("first-night:seat-4", listOf("poisoner.target")),
                InitialOpeningDeferredV1("first-night:seat-5", listOf("fortune_teller.targets")),
                InitialOpeningDeferredV1("event:poisoner.target", listOf("poisoner.target")),
            ),
        )),
        legalDemonBluffs = listOf("monk", "soldier", "mayor", "slayer"),
        gameStateRevision = 7, playerInputRevision = 12,
    )

    private fun value(
        seat: Int, ability: String, kind: String, number: Int? = null,
        role: String? = null, subjectMode: String = "NONE", seats: List<Int> = emptyList(),
        basis: String = "FUNCTIONING",
    ) = HostOpeningSemanticValueV2(
        sourceSeat = seat, abilityRoleId = ability, effectiveAbilityRoleId = ability,
        basis = basis, subjectMode = subjectMode, subjectSeats = seats,
        observationKind = kind, number = number, roleId = role,
    )

    private fun projection(scope: InitialGlobalOpeningScopeV1) =
        HostOpeningSemanticProjectionV2(
            scope, null, known,
            candidatesByDecision = mapOf(
                "first-night:seat-1" to mapOf(
                    "ww-3-4-empath" to value(1, "washerwoman", "ROLE", role = "empath",
                        subjectMode = "EVIDENCE_PAIR", seats = listOf(3, 4))),
                "first-night:seat-3" to mapOf(
                    "chef-1" to value(3, "chef", "NUMBER", number = 1)),
            ),
            redHerringDecisionId = "setup.red-herring",
            redHerringCandidatesBySeat = mapOf(1 to "rh-1", 6 to "rh-6"),
            deferredActions = mapOf("event:poisoner.target" to
                ("poisoner.target" to 7)),
        )

    private fun info(
        id: String, v: HostOpeningSemanticValueV2,
        resolution: String, waiting: List<String> = emptyList(),
    ): JSONObject = JSONObject()
        .put("decisionId", id)
        .put("sourceSeat", v.sourceSeat)
        .put("abilityRoleId", v.abilityRoleId)
        .put("effectiveAbilityRoleId", v.effectiveAbilityRoleId ?: JSONObject.NULL)
        .put("resolution", resolution)
        .put("basis", v.basis)
        .put("subject", JSONObject()
            .put("mode", v.subjectMode).put("seats", JSONArray(v.subjectSeats)))
        .put("observation", JSONObject()
            .put("kind", v.observationKind)
            .put("number", v.number ?: JSONObject.NULL)
            .put("roleId", v.roleId ?: JSONObject.NULL)
            .put("boolean", v.boolean ?: JSONObject.NULL))
        .put("waitingForEvents", JSONArray(waiting))

    private fun plan(scope: InitialGlobalOpeningScopeV1): JSONObject {
        val info = JSONArray()
            .put(info("first-night:seat-1",
                value(1, "washerwoman", "ROLE", role = "empath",
                    subjectMode = "EVIDENCE_PAIR", seats = listOf(3, 4)), "DECISION"))
            .put(info("first-night:seat-3",
                value(3, "chef", "NUMBER", number = 1), "DECISION"))
            .put(info("first-night:seat-4",
                value(4, "empath", "NUMBER", number = 0, basis = "STATUS_DEPENDENT"),
                "CONDITIONAL_INTENT", listOf("poisoner.target")))
            .put(info("first-night:seat-5",
                value(5, "fortuneteller", "NONE", subjectMode = "PLAYER_QUERY_PENDING",
                    basis = "STATUS_DEPENDENT"), "DEFERRED", listOf("fortune_teller.targets")))
        return JSONObject()
            .put("schemaVersion", 2)
            .put("asOfRevision", scope.gameStateRevision)
            .put("requestIdentity", JSONObject()
                .put("gameId", scope.snapshot.gameId)
                .put("gameStateRevision", scope.gameStateRevision)
                .put("playerInputRevision", scope.playerInputRevision)
                .put("rosterFingerprint", scope.rosterFingerprint))
            .put("openingPlan", JSONObject()
                .put("drunkSeat", JSONObject.NULL)
                .put("redHerringSeat", 6)
                .put("demonBluffRoleIds", JSONArray(listOf("monk", "soldier", "mayor")))
                .put("information", info)
                .put("deferredActions", JSONArray().put(JSONObject()
                    .put("decisionId", "event:poisoner.target")
                    .put("event", "poisoner.target").put("actorSeat", 7))))
            .put("strategyMemory", JSONObject()
                .put("tensions", JSONArray(listOf("synthetic only")))
                .put("intentions", JSONArray()).put("uncertainties", JSONArray()))
            .put("tradeoffSummary", "Synthetic protocol witness, no strategic rating")
    }

    private fun reject(scope: InitialGlobalOpeningScopeV1, edit: (JSONObject) -> Unit) {
        val candidate = plan(scope)
        edit(candidate)
        assertTrue(runCatching {
            HostOpeningSemanticGateV2.accept(candidate, projection(scope))
        }.isFailure)
    }

    @Test fun acceptsMatchingHostDomainButNotPublishedFacts() {
        val scope = scope()
        val accepted = HostOpeningSemanticGateV2.accept(plan(scope), projection(scope))
        assertTrue(accepted.selection.validFor(scope))
        assertEquals(mapOf("setup.red-herring" to "rh-6",
            "first-night:seat-1" to "ww-3-4-empath",
            "first-night:seat-3" to "chef-1"), accepted.selection.choices)
        assertEquals(3, accepted.conditional.size)
        assertEquals(0, accepted.conditional.single {
            it.decisionId == "first-night:seat-4"
        }.proposed?.number)
        assertFalse(accepted.selection.choices.containsKey("first-night:seat-4"))
    }

    @Test fun rejectsStaleGameRevisionAndFingerprint() {
        val scope = scope()
        reject(scope) { it.getJSONObject("requestIdentity").put("gameId", "another") }
        reject(scope) { it.getJSONObject("requestIdentity").put("playerInputRevision", 13) }
        reject(scope) { it.getJSONObject("requestIdentity").put("rosterFingerprint", "stale") }
        reject(scope) { it.put("asOfRevision", 8) }
    }

    @Test fun rejectsMissingDuplicateInventedOrExtraDecision() {
        val scope = scope()
        reject(scope) { it.getJSONObject("openingPlan").getJSONArray("information").remove(1) }
        reject(scope) { it.getJSONObject("openingPlan").getJSONArray("deferredActions").remove(0) }
        reject(scope) {
            val a = it.getJSONObject("openingPlan").getJSONArray("information")
            a.put(JSONObject(a.getJSONObject(0).toString()))
        }
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(0).put("decisionId", "invented")
        }
        reject(scope) { it.getJSONObject("openingPlan").put("unexpected", true) }
        reject(scope) { it.getJSONObject("openingPlan").put("redHerringSeat", 8) }
    }

    @Test fun rejectsIllegalObservationRoleAndSubjectShape() {
        val scope = scope()
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(1).getJSONObject("observation").put("number", 0)
        }
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(0).put("abilityRoleId", "washerwoman (drunk)")
        }
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(0).getJSONObject("subject").put("seats", JSONArray(listOf(3, 4, 5)))
        }
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(1).getJSONObject("subject").put("seats", JSONArray(listOf(2, 4)))
        }
    }

    @Test fun rejectsInventedFutureActionAndPrematureResult() {
        val scope = scope()
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(2).put("waitingForEvents", JSONArray())
        }
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("information")
                .getJSONObject(3).getJSONObject("observation").put("kind", "BOOLEAN")
                .put("boolean", true)
        }
        reject(scope) {
            it.getJSONObject("openingPlan").getJSONArray("deferredActions")
                .getJSONObject(0).put("event", "invented.action")
        }
    }


    @Test fun drunkActualAbilityIsNotConflatedWithShownRoleOrStatus() {
        val shownDrunk = listOf(
            "washerwoman", "librarian", "empath", "fortuneteller",
            "saint", "butler", "baron", "imp")
        val snapshot = TroubleBrewingGameSnapshotV1(
            gameId = "3b3d-drunk", gameSeed = 72,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                SnapshotField.NotApplicable, SnapshotField.NotApplicable),
            grimoireSeats = shownDrunk.mapIndexed { index, role ->
                TroubleBrewingSnapshotSeat(
                    seat = index + 1, shownRoleId = SnapshotField.Known(role),
                    actualRoleId = if (index < 4) SnapshotField.Uncommitted
                        else SnapshotField.Known(role),
                    alive = SnapshotField.Known(true), poisoned = SnapshotField.Known(false))
            },
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Known(true), SnapshotField.Uncommitted),
        )
        val branches = (1..4).map { candidateSeat ->
            InitialOpeningHypothesisV1(
                drunkSeat = candidateSeat,
                discretionary = listOf(
                    InitialOpeningDecisionV1("first-night:seat-2", "PAIR_INFORMATION", 2,
                        mapOf("librarian-fake" to "Host public observation")),
                    InitialOpeningDecisionV1("setup.red-herring", "RED_HERRING", null,
                        mapOf("rh-5" to "Good seat 5", "rh-6" to "Good seat 6")),
                ),
                determined = emptyList(),
                deferred = emptyList(),
            )
        }
        val scope = InitialGlobalOpeningScopeV1.fromHost(
            snapshot, registry, branches,
            legalDemonBluffs = listOf("chef", "monk", "slayer"),
            gameStateRevision = 9, playerInputRevision = 2,
        )
        val drunkInfo = HostOpeningSemanticValueV2(
            sourceSeat = 2, abilityRoleId = "librarian",
            effectiveAbilityRoleId = null,
            basis = "DRUNK_MISINFORMATION",
            subjectMode = "EVIDENCE_PAIR", subjectSeats = listOf(5, 6),
            observationKind = "ROLE", roleId = "recluse")
        val projection = HostOpeningSemanticProjectionV2(
            scope, 2, shownDrunk.toSet() + setOf("chef", "monk", "slayer", "recluse"),
            candidatesByDecision = mapOf("first-night:seat-2" to
                mapOf("librarian-fake" to drunkInfo)),
            redHerringDecisionId = "setup.red-herring",
            redHerringCandidatesBySeat = mapOf(5 to "rh-5", 6 to "rh-6"),
        )
        val output = JSONObject()
            .put("schemaVersion", 2).put("asOfRevision", 9)
            .put("requestIdentity", JSONObject()
                .put("gameId", scope.snapshot.gameId).put("gameStateRevision", 9)
                .put("playerInputRevision", 2)
                .put("rosterFingerprint", scope.rosterFingerprint))
            .put("openingPlan", JSONObject()
                .put("drunkSeat", 2).put("redHerringSeat", 5)
                .put("demonBluffRoleIds", JSONArray(listOf("chef", "monk", "slayer")))
                .put("information", JSONArray().put(info(
                    "first-night:seat-2", drunkInfo, "DECISION")))
                .put("deferredActions", JSONArray()))
            .put("strategyMemory", JSONObject()
                .put("tensions", JSONArray()).put("intentions", JSONArray())
                .put("uncertainties", JSONArray()))
            .put("tradeoffSummary", "Synthetic drunk identity boundary")
        val accepted = HostOpeningSemanticGateV2.accept(output, projection)
        assertEquals(2, accepted.selection.drunkSeat)
        assertEquals("librarian-fake",
            accepted.selection.choices["first-night:seat-2"])
        val malformed = JSONObject(output.toString())
        malformed.getJSONObject("openingPlan").getJSONArray("information")
            .getJSONObject(0).put("abilityRoleId", "librarian (Drunk misinformation)")
        assertTrue(runCatching {
            HostOpeningSemanticGateV2.accept(malformed, projection)
        }.isFailure)
        val malformedBasis = JSONObject(output.toString())
        malformedBasis.getJSONObject("openingPlan").getJSONArray("information")
            .getJSONObject(0).put("basis", "FUNCTIONING")
        assertTrue(runCatching {
            HostOpeningSemanticGateV2.accept(malformedBasis, projection)
        }.isFailure)
    }

    @Test fun malformedHostProjectionCannotMakeAnInvalidModelPass() {
        val scope = scope()
        assertTrue(runCatching {
            HostOpeningSemanticProjectionV2(
                scope, null, known,
                candidatesByDecision = mapOf(
                    "first-night:seat-1" to mapOf("fabricated" to value(
                        1, "washerwoman", "ROLE", role = "empath",
                        subjectMode = "EVIDENCE_PAIR", seats = listOf(3, 4))),
                    "first-night:seat-3" to mapOf(
                        "chef-1" to value(3, "chef", "NUMBER", number = 1)),
                ),
            )
        }.isFailure)
    }
}
