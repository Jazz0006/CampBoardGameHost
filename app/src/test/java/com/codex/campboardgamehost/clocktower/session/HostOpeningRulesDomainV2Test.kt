package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
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

/** Real rules-owned generated domains; never gold expert strategy answers. */
class HostOpeningRulesDomainV2Test {
    private val registry by lazy {
        BuiltInClocktowerRulesetCatalog { path ->
            File(File("src/main/assets"), path).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
    }
    private val definitions by lazy {
        clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing)
    }

    private fun snapshot(shown: List<String>, drunk: Boolean) =
        TroubleBrewingGameSnapshotV1(
            gameId = if (drunk) "rules-drunk" else "rules-no-drunk",
            gameSeed = 42,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
                SnapshotField.NotApplicable, SnapshotField.NotApplicable),
            grimoireSeats = shown.mapIndexed { index, role ->
                val team = requireNotNull(registry.findByExternalId(role)).team
                TroubleBrewingSnapshotSeat(
                    seat = index + 1, shownRoleId = SnapshotField.Known(role),
                    actualRoleId = if (drunk &&
                        team == com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam.TOWNSFOLK)
                        SnapshotField.Uncommitted else SnapshotField.Known(role),
                    alive = SnapshotField.Known(true), poisoned = SnapshotField.Known(false),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Known(drunk),
                if (drunk) SnapshotField.Uncommitted else SnapshotField.NotApplicable),
        )

    private fun make(shown: List<String>, drunk: Boolean) =
        HostOpeningRulesDomainV2.fromHostSnapshot(
            snapshot(shown, drunk), registry, definitions,
            gameStateRevision = 10, playerInputRevision = 4,
        )

    private fun information(
        id: String, v: HostOpeningSemanticValueV2, status: String,
        waiting: List<String> = emptyList(),
    ) = JSONObject()
        .put("decisionId", id)
        .put("sourceSeat", v.sourceSeat)
        .put("abilityRoleId", v.abilityRoleId)
        .put("effectiveAbilityRoleId", v.effectiveAbilityRoleId ?: JSONObject.NULL)
        .put("resolution", status).put("basis", v.basis)
        .put("subject", JSONObject()
            .put("mode", v.subjectMode).put("seats", JSONArray(v.subjectSeats)))
        .put("observation", JSONObject()
            .put("kind", v.observationKind)
            .put("number", v.number ?: JSONObject.NULL)
            .put("roleId", v.roleId ?: JSONObject.NULL)
            .put("boolean", v.boolean ?: JSONObject.NULL))
        .put("waitingForEvents", JSONArray(waiting))

    private fun plan(
        projection: HostOpeningSemanticProjectionV2,
        override: Map<String, HostOpeningSemanticValueV2> = emptyMap(),
    ): JSONObject {
        val scope = projection.scope
        val candidateBluffs = scope.legalDemonBluffs?.take(3)
        val infoArray = JSONArray()
        val choices = projection.candidatesByDecision
        choices.forEach { (id, values) ->
            infoArray.put(information(id,
                override[id] ?: values.values.first(), "DECISION"))
        }
        projection.determinedInformationIds.forEach { id ->
            val seat = id.substringAfterLast('-').toInt()
            val ability = (scope.snapshot.grimoireSeats[seat - 1]
                .shownRoleId as SnapshotField.Known<String>).value
            val value = HostOpeningSemanticValueV2(
                sourceSeat = seat, abilityRoleId = ability,
                effectiveAbilityRoleId = ability,
                basis = "RULE_DETERMINED",
                subjectMode = if (ability == "empath") "FIXED_NEIGHBORS" else "NONE",
                subjectSeats = emptyList(), observationKind = "NONE")
            infoArray.put(information(id, value, "HOST_DETERMINED"))
        }
        projection.branch.deferred.filter { it.decisionId.startsWith("first-night:seat-") }
            .forEach { d ->
                val seat = d.decisionId.substringAfterLast('-').toInt()
                val role = (scope.snapshot.grimoireSeats[seat - 1]
                    .shownRoleId as SnapshotField.Known<String>).value
                val value = HostOpeningSemanticValueV2(
                    sourceSeat = seat, abilityRoleId = role,
                    effectiveAbilityRoleId = if (seat == projection.drunkSeat) null else role,
                    basis = "STATUS_DEPENDENT",
                    subjectMode = when (role) {
                        "fortuneteller" -> "PLAYER_QUERY_PENDING"
                        "empath" -> "FIXED_NEIGHBORS"
                        else -> "NONE"
                    },
                    subjectSeats = emptyList(), observationKind = "NONE")
                infoArray.put(information(d.decisionId, value, "DEFERRED", d.waitingForEvents))
            }
        val actions = JSONArray()
        projection.deferredActions.forEach { (id, action) ->
            actions.put(JSONObject().put("decisionId", id)
                .put("event", action.first).put("actorSeat", action.second ?: JSONObject.NULL))
        }
        return JSONObject()
            .put("schemaVersion", 2).put("asOfRevision", scope.gameStateRevision)
            .put("requestIdentity", JSONObject()
                .put("gameId", scope.snapshot.gameId)
                .put("gameStateRevision", scope.gameStateRevision)
                .put("playerInputRevision", scope.playerInputRevision)
                .put("rosterFingerprint", scope.rosterFingerprint))
            .put("openingPlan", JSONObject()
                .put("drunkSeat", projection.drunkSeat ?: JSONObject.NULL)
                .put("redHerringSeat", projection.redHerringCandidatesBySeat.keys.firstOrNull()
                    ?: JSONObject.NULL)
                .put("demonBluffRoleIds", candidateBluffs?.let(::JSONArray) ?: JSONObject.NULL)
                .put("information", infoArray).put("deferredActions", actions))
            .put("strategyMemory", JSONObject()
                .put("tensions", JSONArray()).put("intentions", JSONArray())
                .put("uncertainties", JSONArray()))
            .put("tradeoffSummary", "Synthetic model-shaped response checked against real TB rules.")
    }

    @Test fun allPrecommitDrunkBranchesUseRealDomainsAndLegalBluffs() {
        val actual = make(listOf("chef", "fortuneteller", "ravenkeeper", "butler",
            "investigator", "recluse", "baron", "imp"), true)
        assertEquals(setOf(1, 2, 3, 5), actual.projections.keys.filterNotNull().toSet())
        val branch = actual.forDrunkSeat(1)
        assertTrue(branch.candidatesByDecision["first-night:seat-1"]
            .orEmpty().values.any { it.number == 0 && it.basis == "DRUNK_MISINFORMATION" })
        assertTrue(branch.candidatesByDecision["first-night:seat-5"]
            .orEmpty().values.any { it.roleId == "baron" &&
                6 in it.subjectSeats && 7 in it.subjectSeats })
        assertEquals(setOf(1, 2, 3, 4, 5, 6), branch.redHerringCandidatesBySeat.keys)
        assertTrue(HostOpeningSemanticGateV2.accept(plan(branch), branch)
            .selection.validFor(actual.scope))
        val drunkFortuneTeller = actual.forDrunkSeat(2)
        assertTrue(drunkFortuneTeller.redHerringCandidatesBySeat.isEmpty())
        assertTrue(HostOpeningSemanticGateV2.accept(plan(drunkFortuneTeller),
            drunkFortuneTeller).selection.validFor(actual.scope))
    }

    @Test fun realSpyRegistrationAndNumericDomainsDoNotInventUniqueWitness() {
        val actual = make(listOf("chef", "empath", "washerwoman", "fortuneteller",
            "spy", "recluse", "librarian", "imp"), false)
        val branch = actual.forDrunkSeat(null)
        val spyRole = branch.candidatesByDecision["first-night:seat-3"]
            .orEmpty().values.firstOrNull {
                it.roleId == "mayor" && 5 in it.subjectSeats
            }
        assertTrue("Spy-as-Townsfolk needs actual rules-owned registration", spyRole != null)
        val checked = HostOpeningSemanticGateV2.accept(
            plan(branch, mapOf("first-night:seat-3" to requireNotNull(spyRole))), branch)
        assertTrue(checked.selection.validFor(actual.scope))
        assertTrue(branch.candidatesByDecision["first-night:seat-1"]
            .orEmpty().values.mapNotNull { it.number }.distinct().size >= 2)
        val wrong = plan(branch)
        wrong.getJSONObject("openingPlan").getJSONArray("information").getJSONObject(0)
            .getJSONObject("observation").put("number", 999)
        assertTrue(runCatching { HostOpeningSemanticGateV2.accept(wrong, branch) }.isFailure)
    }

    @Test fun poisonerAndFortuneTellerRemainRealUnobservedEvents() {
        val actual = make(listOf("chef", "empath", "washerwoman", "fortuneteller",
            "poisoner", "recluse", "librarian", "imp"), false)
        val branch = actual.forDrunkSeat(null)
        assertTrue(branch.candidatesByDecision.isEmpty())
        assertEquals(2, branch.deferredActions.size)
        assertTrue(branch.branch.deferred.any {
            it.decisionId == "first-night:seat-2" &&
                "poisoner.target" in it.waitingForEvents
        })
        assertTrue(HostOpeningSemanticGateV2.accept(plan(branch), branch)
            .selection.validFor(actual.scope))
        val forged = plan(branch)
        forged.getJSONObject("openingPlan").getJSONArray("deferredActions")
            .getJSONObject(0).put("actorSeat", 1)
        assertTrue(runCatching {
            HostOpeningSemanticGateV2.accept(forged, branch)
        }.isFailure)
    }

    @Test fun librarianZeroOutsidersIsHostDeterminedNotMissingInformation() {
        val actual = make(listOf("washerwoman", "librarian",
            "investigator", "chef", "scarletwoman", "imp"), false)
        val branch = actual.forDrunkSeat(null)
        assertTrue("Zero-Outsider outcome should be computed by Host",
            "first-night:seat-2" in branch.determinedInformationIds)
        assertFalse(branch.candidatesByDecision.containsKey("first-night:seat-2"))
        assertTrue(HostOpeningSemanticGateV2.accept(plan(branch), branch)
            .selection.validFor(actual.scope))
    }
}
