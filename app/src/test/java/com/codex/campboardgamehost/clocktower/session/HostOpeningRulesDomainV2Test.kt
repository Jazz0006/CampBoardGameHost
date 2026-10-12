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


    private fun firstNightGame(
        shown: List<String>,
        drunkSeat: Int? = null,
        poisonedSeat: Int? = null,
    ): com.codex.campboardgamehost.clocktower.domain.GameState {
        val players = shown.mapIndexed { index, external ->
            val seat = index + 1
            val displayed = requireNotNull(registry.findByExternalId(external))
            val actual = if (seat == drunkSeat) requireNotNull(
                registry.findByExternalId("drunk")) else displayed
            val type = when (actual.team) {
                com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam.TOWNSFOLK ->
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK
                com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam.OUTSIDER ->
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.OUTSIDER
                com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam.MINION ->
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.MINION
                com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam.DEMON ->
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON
                else -> error("Unsupported test role.")
            }
            com.codex.campboardgamehost.clocktower.domain.PlayerState(
                seat = seat, name = "seat-" + seat, actualRole = actual.id,
                actualAlignment = if (type in setOf(
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK,
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.OUTSIDER))
                    com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD
                else com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                actualType = type, shownRole = displayed.id,
                poisoned = seat == poisonedSeat,
            )
        }
        return com.codex.campboardgamehost.clocktower.domain.GameState(
            com.codex.campboardgamehost.clocktower.domain.ScriptId("trouble_brewing"),
            players, 42,
        )
    }

    private fun firstNightFacts(
        projection: HostOpeningSemanticProjectionV2,
        shown: List<String>,
        poisoned: Int? = null,
        ftTargets: Map<Int, List<Int>> = emptyMap(),
        published: List<HostOpeningPublishedInformationV2> = emptyList(),
    ): HostOpeningConfirmedFirstNightV2 =
        HostOpeningConfirmedFirstNightV2(
            gameId = projection.scope.snapshot.gameId,
            gameStateRevision = 11,
            playerInputRevision = 5,
            phase = com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.FIRST_NIGHT,
            round = 1,
            game = firstNightGame(shown, projection.drunkSeat, poisoned),
            drunkSeat = projection.drunkSeat,
            redHerringSeat = projection.redHerringCandidatesBySeat.keys.firstOrNull(),
            confirmedPoisonerTargetSeat = poisoned,
            confirmedFortuneTellerTargets = ftTargets,
            published = published,
        )

    @Test fun confirmedPoisonerActionRechecksAllUnpublishedAndCannotAssumeMissingPlan() {
        val shown = listOf("chef", "empath", "washerwoman", "fortuneteller",
            "poisoner", "recluse", "librarian", "imp")
        val domain = make(shown, false)
        val branch = domain.forDrunkSeat(null)
        val original = HostOpeningSemanticGateV2.accept(plan(branch), branch)
        val before = firstNightFacts(branch, shown).copy(
            gameStateRevision = 10, playerInputRevision = 4)
        val waiting = HostOpeningResidualEventGateV2.review(
            original, branch, before, registry, definitions)
        assertEquals(HostOpeningResidualOutcomeV2.WAIT_FOR_HOST_ACTION, waiting.outcome)
        assertTrue(waiting.decisions.any {
            it.disposition == HostOpeningResidualDispositionV2.WAITING_EVENT })

        // Poisoner confirms target 2. The earlier plan had only deferred
        // statements and MUST NOT silently manufacture a result for them.
        val after = firstNightFacts(branch, shown, poisoned = 2)
        val reviewed = HostOpeningResidualEventGateV2.review(
            original, branch, after, registry, definitions)
        assertEquals(HostOpeningResidualOutcomeV2.REPLAN_WHOLE_UNPUBLISHED_RESIDUAL,
            reviewed.outcome)
        assertTrue("Empath remains an unplanned Host decision",
            "first-night:seat-2" in reviewed.replanDecisionIds)
        assertTrue("FT still waits for real query, not a fictitious result",
            reviewed.decisions.any {
                it.decisionId == "first-night:seat-4" &&
                    it.disposition == HostOpeningResidualDispositionV2.WAITING_EVENT })
        val forged = after.copy(confirmedPoisonerTargetSeat = 3)
        assertTrue(runCatching {
            HostOpeningResidualEventGateV2.review(original, branch, forged, registry, definitions)
        }.isFailure)
    }

    @Test fun conditionalIntentIsAdvisoryAndMustMatchResolvedHostDomain() {
        val shown = listOf("chef", "empath", "washerwoman", "fortuneteller",
            "poisoner", "recluse", "librarian", "imp")
        val branch = make(shown, false).forDrunkSeat(null)
        fun proposed(number: Int): HostOpeningSemanticAcceptanceV2 {
            val raw = plan(branch)
            val info = raw.getJSONObject("openingPlan").getJSONArray("information")
            val empath = (0 until info.length()).map { info.getJSONObject(it) }
                .single { it.getString("decisionId") == "first-night:seat-2" }
            empath.put("resolution", "CONDITIONAL_INTENT")
            empath.getJSONObject("observation").put("kind", "NUMBER").put("number", number)
            return HostOpeningSemanticGateV2.accept(raw, branch)
        }
        // If Poisoner chose himself, the two good neighbours of the Empath
        // are clear, and 0 is Host-legal. This is a candidate, not a publication.
        val confirmed = firstNightFacts(branch, shown, poisoned = 5)
        val retained = HostOpeningResidualEventGateV2.review(
            proposed(0), branch, confirmed, registry, definitions)
        assertEquals(HostOpeningResidualDispositionV2.STILL_LEGAL_UNPUBLISHED,
            retained.decisions.single { it.decisionId == "first-night:seat-2" }.disposition)

        // The original gate accepts conditional speculation *before* the
        // target is known; the event-time gate must reject an impossible 99.
        val invalid = HostOpeningResidualEventGateV2.review(
            proposed(99), branch, confirmed, registry, definitions)
        assertEquals(HostOpeningResidualDispositionV2.REPLAN_REQUIRED,
            invalid.decisions.single { it.decisionId == "first-night:seat-2" }.disposition)
        assertEquals(HostOpeningResidualOutcomeV2.REPLAN_WHOLE_UNPUBLISHED_RESIDUAL,
            invalid.outcome)
    }

    @Test fun fortuneTellerTargetsUseHostRuleDomainAndRecluseMayLegallyRegisterDemon() {
        val shown = listOf("chef", "empath", "washerwoman", "fortuneteller",
            "recluse", "butler", "spy", "imp")
        val branch = make(shown, false).forDrunkSeat(null)
        val raw = plan(branch)
        val info = raw.getJSONObject("openingPlan").getJSONArray("information")
        val ft = (0 until info.length()).map { info.getJSONObject(it) }
            .single { it.getString("decisionId") == "first-night:seat-4" }
        ft.put("resolution", "CONDITIONAL_INTENT")
        ft.getJSONObject("observation").put("kind", "BOOLEAN").put("boolean", true)
        val original = HostOpeningSemanticGateV2.accept(raw, branch)
        val stillWaiting = HostOpeningResidualEventGateV2.review(
            original, branch, firstNightFacts(branch, shown), registry, definitions)
        assertEquals(HostOpeningResidualDispositionV2.WAITING_EVENT,
            stillWaiting.decisions.single { it.decisionId == "first-night:seat-4" }.disposition)
        // Neither the Demon (8) nor the Red Herring (1) was inspected,
        // but Recluse (5) may register Demon. Keep both possible worlds.
        val actual = firstNightFacts(branch, shown, ftTargets = mapOf(4 to listOf(5, 6)))
        val reviewed = HostOpeningResidualEventGateV2.review(
            original, branch, actual, registry, definitions)
        assertEquals(HostOpeningResidualDispositionV2.STILL_LEGAL_UNPUBLISHED,
            reviewed.decisions.single { it.decisionId == "first-night:seat-4" }.disposition)
        val badQuery = actual.copy(confirmedFortuneTellerTargets = mapOf(4 to listOf(5, 5)))
        assertTrue(runCatching {
            HostOpeningResidualEventGateV2.review(
                original, branch, badQuery, registry, definitions)
        }.isFailure)
    }

    @Test fun drunkShownFortuneTellerQueryNeedsNoRedHerringAndStillChecksLegalTargets() {
        val shown = listOf("chef", "fortuneteller", "ravenkeeper", "butler",
            "investigator", "recluse", "baron", "imp")
        val branch = make(shown, true).forDrunkSeat(2)
        assertTrue(branch.redHerringCandidatesBySeat.isEmpty())
        val raw = plan(branch)
        val info = raw.getJSONObject("openingPlan").getJSONArray("information")
        val ft = (0 until info.length()).map { info.getJSONObject(it) }
            .single { it.getString("decisionId") == "first-night:seat-2" }
        assertTrue(ft.isNull("effectiveAbilityRoleId"))
        ft.put("resolution", "CONDITIONAL_INTENT")
        ft.getJSONObject("observation").put("kind", "BOOLEAN").put("boolean", true)
        val original = HostOpeningSemanticGateV2.accept(raw, branch)
        val resolved = firstNightFacts(branch, shown, ftTargets = mapOf(2 to listOf(6, 7)))
        val review = HostOpeningResidualEventGateV2.review(
            original, branch, resolved, registry, definitions)
        assertEquals(HostOpeningResidualDispositionV2.STILL_LEGAL_UNPUBLISHED,
            review.decisions.single { it.decisionId == "first-night:seat-2" }.disposition)
        assertTrue(runCatching {
            HostOpeningResidualEventGateV2.review(
                original, branch, resolved.copy(redHerringSeat = 4),
                registry, definitions)
        }.isFailure)
    }

    @Test fun publishedHostObservationStaysFrozenAndIsNeverRewritten() {
        val shown = listOf("chef", "empath", "washerwoman", "fortuneteller",
            "recluse", "butler", "spy", "imp")
        val branch = make(shown, false).forDrunkSeat(null)
        val accepted = HostOpeningSemanticGateV2.accept(plan(branch), branch)
        val locked = branch.candidatesByDecision["first-night:seat-3"]
            .orEmpty().values.first()
        val originalRecord = HostOpeningPublishedInformationV2(
            "first-night:seat-3", "host-observation-17", locked)
        val facts = firstNightFacts(branch, shown, published = listOf(originalRecord))
        val reviewed = HostOpeningResidualEventGateV2.review(
            accepted, branch, facts, registry, definitions)
        assertEquals(setOf("host-observation-17"),
            reviewed.preservedPublishedRecordIds)
        assertEquals(HostOpeningResidualDispositionV2.PUBLISHED_LOCKED,
            reviewed.decisions.single { it.decisionId == originalRecord.decisionId }.disposition)
        // A changed observation record is not overwritten with an AI choice;
        // the only output is the original record ID.
        assertTrue(reviewed.decisions.none {
            it.disposition == HostOpeningResidualDispositionV2.PUBLISHED_LOCKED &&
                it.legalMatchingCandidateIds.isNotEmpty() })
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
