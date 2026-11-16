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
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Wire/protocol acceptance only. No real model calls or strategy gold answers. */
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
            playerExperience = mapOf(7 to OpeningPlayerExperienceV1.VETERAN),
        )
    }

    // Seats 7 and 8 are adjacent evil; the Recluse at 6 may register as evil or as a Minion.
    private val withDrunk = listOf("chef", "fortuneteller", "ravenkeeper", "butler",
        "investigator", "recluse", "baron", "imp")
    private val noDrunk = listOf("poisoner", "ravenkeeper", "saint", "slayer",
        "empath", "fortuneteller", "mayor", "imp")

    private fun wire(request: InitialOpeningRequestV2) =
        JSONObject(InitialJointOpeningDirectV2.encode(request, ruleset))

    private fun JSONArray.objects() = (0 until length()).map(::getJSONObject)
    private fun JSONArray.values() = (0 until length()).map(::get)
    private fun JSONObject.slot(name: String) =
        getJSONArray("slots").objects().singleOrNull { it.getString("slot") == name }
    private fun JSONObject.info(seat: Int) =
        getJSONArray("firstNightInfo").objects().single { it.getInt("seat") == seat }

    private fun plan(
        drunk: Int?, redHerring: Int?, bluffs: List<String>?, information: List<JSONObject> = emptyList(),
    ) = JSONObject()
        .put("drunkSeat", drunk ?: JSONObject.NULL)
        .put("redHerringSeat", redHerring ?: JSONObject.NULL)
        .put("demonBluffRoleIds", bluffs?.let(::JSONArray) ?: JSONObject.NULL)
        .put("information", JSONArray(information))
        .put("intentions", JSONArray().put(JSONObject()
            .put("trigger", "The Poisoner picks the Chef").put("approach", "Show one more pair")))
        .put("rationale", "Synthetic structural response.")

    private fun answer(seat: Int, roleId: String? = null, seats: List<Int> = emptyList(), number: Int? = null) =
        JSONObject().put("seat", seat).put("roleId", roleId ?: JSONObject.NULL)
            .put("seats", JSONArray(seats)).put("number", number ?: JSONObject.NULL)

    private fun decode(request: InitialOpeningRequestV2, plan: JSONObject) =
        InitialJointOpeningDirectV2.decode(plan.toString(), request, ruleset)

    @Test fun requestOffersOnlyOpenChoicesWithSessionRuleCandidates() {
        val request = build(true, withDrunk)
        val host = wire(request)
        assertEquals(listOf<Any>(1, 2, 3, 5), host.slot("drunk")!!.getJSONArray("seats").values())
        assertEquals(listOf<Any>(1, 2, 3, 4, 5), host.slot("redHerring")!!.getJSONArray("seats").values())
        val bluffs = host.slot("demonBluffs")!!.getJSONArray("roles").values()
        assertTrue(bluffs.containsAll(listOf("washerwoman", "saint")))
        assertTrue(bluffs.none { it in withDrunk || it == "drunk" })
        assertEquals("VETERAN", host.getJSONArray("seats").getJSONObject(6).getString("experience"))
        assertEquals("BEGINNER", host.getJSONArray("seats").getJSONObject(0).getString("experience"))

        val chef = host.info(1)
        assertEquals("ALWAYS", chef.getString("answer"))
        assertEquals(listOf<Any>(1, 2), chef.getJSONArray("truthful").values())
        assertEquals(listOf<Any>(0, 1, 2), chef.getJSONArray("ifDrunk").values())
        assertEquals("AFTER_PLAYER_CHOICE", host.info(2).getString("type"))
        val investigator = host.info(5)
        assertEquals("baron", investigator.getJSONArray("truthful").getJSONObject(0).getString("role"))
        assertEquals(7, investigator.getJSONArray("truthful").getJSONObject(0).getInt("seat"))
        assertEquals(6, investigator.getJSONArray("mayRegister").getJSONObject(0).getInt("seat"))

        // The model never echoes identity; the Host binds the answer to its own request.
        assertFalse(host.has("requestIdentity"))
        val responses = JSONObject(PersonalDirectOpenAiV1.buildRequest(
            host.toString(), PersonalDirectOpenAiV1.DEFAULT_MODEL))
        assertEquals(InitialJointOpeningDirectV2.INSTRUCTIONS, responses.getString("instructions"))
        assertEquals(host.toString(), responses.getString("input"))
        assertTrue(responses.getJSONObject("text").getJSONObject("format").getBoolean("strict"))
    }

    @Test fun rosterWithoutDrunkAsksNothingTheRulesAlreadyDecide() {
        val request = build(false, noDrunk)
        val host = wire(request)
        assertNull(host.slot("drunk"))
        assertEquals(listOf<Any>(2, 3, 4, 5, 6, 7), host.slot("redHerring")!!.getJSONArray("seats").values())
        assertEquals("NEVER", host.info(5).getString("answer"))
        assertEquals(listOf<Any>(0), host.info(5).getJSONArray("truthful").values())

        val accepted = decode(request, plan(null, 4, listOf("washerwoman", "soldier", "monk")))
        assertEquals(4, accepted.selection.redHerringSeat)
        assertEquals(listOf("washerwoman", "soldier", "monk"), accepted.selection.demonBluffs)
        // The Host states the Empath's only legal number itself.
        val empath = accepted.selection.information.getValue(5)
        assertEquals(0, empath.number)
        assertEquals("RULE_DETERMINED", empath.basis)
        assertEquals(setOf(5), accepted.selection.information.keys)
        assertTrue(accepted.selection.validFor(request))
        assertEquals("Show one more pair", accepted.strategyMemory?.intentions?.single()?.approach)
        assertEquals("Synthetic structural response.", accepted.tradeoffSummary)
    }

    @Test fun acceptsLegalJointPlanAndBindsDrunkMisinformation() {
        val request = build(true, withDrunk)
        val accepted = decode(request, plan(1, 3, listOf("washerwoman", "saint", "monk"), listOf(
            answer(1, number = 0), answer(5, "baron", listOf(7, 3)))))
        assertEquals(1, accepted.selection.drunkSeat)
        val chef = accepted.selection.information.getValue(1)
        assertEquals(0, chef.number)
        assertEquals("DRUNK_MISINFORMATION", chef.basis)
        assertNull(chef.effectiveAbilityRoleId)
        val investigator = accepted.selection.information.getValue(5)
        assertEquals("baron", investigator.roleId)
        assertEquals(listOf(3, 7), investigator.subjectSeats)
        assertEquals("FUNCTIONING", investigator.basis)
        // The Recluse may register as any Minion for a functioning Investigator.
        assertEquals("poisoner", decode(request, plan(1, 3, listOf("washerwoman", "saint", "monk"), listOf(
            answer(5, "poisoner", listOf(6, 2))))).selection.information.getValue(5).roleId)
    }

    @Test fun drunkFortuneTellerNeedsNoRedHerring() {
        val request = build(true, withDrunk)
        val accepted = decode(request, plan(2, 3, listOf("washerwoman", "saint", "monk")))
        assertEquals(2, accepted.selection.drunkSeat)
        assertNull(accepted.selection.redHerringSeat)
    }

    @Test fun illegalInformationIsDroppedWithoutLosingTheOpening() {
        val request = build(true, withDrunk)
        val accepted = decode(request, plan(3, 3, listOf("washerwoman", "saint", "monk"), listOf(
            answer(1, number = 0),                     // a functioning Chef cannot see zero pairs here
            answer(5, "saint", listOf(7, 8)),          // not a Minion
            answer(2, number = 1),                     // waits for the player's own choice
        )))
        assertEquals(3, accepted.selection.drunkSeat)
        assertTrue(accepted.selection.information.isEmpty())
        assertTrue(decode(request, plan(3, 3, listOf("washerwoman", "saint", "monk"), listOf(
            answer(5, "baron", listOf(5, 7)),          // the Investigator cannot be in their own pair
        ))).selection.information.isEmpty())
    }

    private fun rejects(request: InitialOpeningRequestV2, plan: JSONObject) =
        assertTrue(runCatching { decode(request, plan) }.isFailure)

    @Test fun rejectsSetupChoicesOutsideTheOfferedCandidates() {
        val request = build(true, withDrunk)
        val bluffs = listOf("washerwoman", "saint", "monk")
        rejects(request, plan(null, 3, bluffs))
        rejects(request, plan(4, 3, bluffs))           // the Butler is not a shown Townsfolk
        rejects(request, plan(99, 3, bluffs))
        // Session Red Herring rule: an actually good player who is not the Recluse.
        rejects(request, plan(1, 6, bluffs))
        rejects(request, plan(1, 8, bluffs))
        rejects(request, plan(1, null, bluffs))
        rejects(request, plan(1, 3, listOf("chef", "saint", "monk")))      // in play
        rejects(request, plan(1, 3, listOf("drunk", "saint", "monk")))     // the Drunk is in play
        rejects(request, plan(1, 3, listOf("saint", "saint", "monk")))
        rejects(request, plan(1, 3, listOf("saint", "monk")))
        rejects(request, plan(1, 3, bluffs).also { it.remove("rationale") })
        rejects(build(false, noDrunk), plan(2, 4, bluffs))
    }
}
