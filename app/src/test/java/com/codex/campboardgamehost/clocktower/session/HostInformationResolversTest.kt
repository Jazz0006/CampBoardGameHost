package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HostInformationResolversTest {
    private val catalog = BuiltInClocktowerRulesetCatalog {
        File("src/main/assets/$it").readText(Charsets.UTF_8)
    }
    private val rules = catalog.ruleset(ClocktowerScript.TroubleBrewing)

    private fun session(
        vararg roles: String, shown: Map<Int, String> = emptyMap(),
        script: String = "trouble_brewing",
    ): ClocktowerGameSession {
        val minions = setOf("Spy", "Poisoner", "Baron", "Scarlet Woman")
        val outsiders = setOf("Recluse", "Drunk", "Saint", "Butler")
        val players = roles.mapIndexed { i, role ->
            val type = when (role) {
                "Imp" -> CharacterType.DEMON
                in minions -> CharacterType.MINION
                in outsiders -> CharacterType.OUTSIDER
                else -> CharacterType.TOWNSFOLK
            }
            PlayerState(i + 1, "P${i + 1}", RoleId(role),
                if (type == CharacterType.DEMON || type == CharacterType.MINION) Alignment.EVIL else Alignment.GOOD,
                type, shownRole = shown[i + 1]?.let(::RoleId))
        }
        val game = GameState(ScriptId(script), players, 101L)
        return ClocktowerGameSession.createProduction(
            "host-resolver-test", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    }

    private fun update(s: ClocktowerGameSession, seat: Int, poisoned: Boolean? = null, alive: Boolean? = null) {
        s.updateGameState(s.state.gameState.copy(players = s.state.gameState.players.map {
            if (it.seat == seat) it.copy(
                poisoned = poisoned ?: it.poisoned,
                alive = alive ?: it.alive,
            ) else it
        }))
    }

    @Test fun chefWraparoundCountsSpecialRegistrationsWithoutChangingSession() {
        val s = session("Imp", "Recluse", "Chef", "Empath", "Soldier", "Saint", "Poisoner", "Spy")
        val before = s.state
        val answer = requireNotNull(NumericInformationResolver.resolve(s.state, NumericMetric.ADJACENT_EVIL_PAIRS, 3))
        assertEquals(listOf(0, 1, 2, 3), answer.values)
        assertEquals(setOf(2, 8), answer.relevantSpecialSeats)
        assertEquals(AbilityFunctioningState.FUNCTIONING, answer.sourceAbility)
        assertSame(before, s.state)
        update(s, 8, poisoned = true)
        assertEquals(listOf(2, 3),
            NumericInformationResolver.resolve(s.state, NumericMetric.ADJACENT_EVIL_PAIRS, 3)?.values)
        update(s, 2, poisoned = true)
        assertEquals(listOf(2),
            NumericInformationResolver.resolve(s.state, NumericMetric.ADJACENT_EVIL_PAIRS, 3)?.values)
    }

    @Test fun empathSkipsDeadSeatsAndSourceDrunkPoisonIsIndependentOfTargetRegistration() {
        val s = session("Imp", "Recluse", "Empath", "Soldier", "Spy", "Poisoner", "Chef", "Butler")
        assertEquals(listOf(0, 1),
            NumericInformationResolver.resolve(s.state, NumericMetric.LIVING_EVIL_NEIGHBOURS, 3)?.values)
        update(s, 4, alive = false)
        val working = requireNotNull(NumericInformationResolver.resolve(
            s.state, NumericMetric.LIVING_EVIL_NEIGHBOURS, 3))
        assertEquals(listOf(0, 1, 2), working.values)
        assertEquals(setOf(2, 5), working.relevantSpecialSeats)
        update(s, 2, poisoned = true)
        update(s, 5, poisoned = true)
        assertEquals(listOf(1),
            NumericInformationResolver.resolve(s.state, NumericMetric.LIVING_EVIL_NEIGHBOURS, 3)?.values)
        update(s, 3, poisoned = true)
        val poisoned = requireNotNull(NumericInformationResolver.resolve(
            s.state, NumericMetric.LIVING_EVIL_NEIGHBOURS, 3))
        assertEquals(AbilityFunctioningState.POISONED, poisoned.sourceAbility)
        assertEquals(listOf(0, 1, 2), poisoned.values)
        assertTrue(poisoned.productShortlist)
        assertTrue(poisoned.relevantSpecialSeats.isEmpty())
    }

    @Test fun washerwomanPairUsesSelectedTruthThenOnlyActualInPlayAndCommittedBluffRoles() {
        val s = session("Washerwoman", "Chef", "Spy", "Drunk", "Recluse", "Investigator", "Imp", "Baron",
            shown = mapOf(4 to "Empath"))
        s.commitDemonBluffs(listOf(RoleId("Mayor"), RoleId("Monk")), StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence)
        val one = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Washerwoman"), 1, 2, 7))
        assertEquals(listOf(RoleId("Chef")), one.options.map { it.role })
        assertFalse(one.productShortlist)
        val special = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Washerwoman"), 1, 3, 7))
        assertEquals(setOf(3), special.specialCandidateSeats)
        assertTrue(special.productShortlist)
        assertEquals(listOf(RoleId("Mayor"), RoleId("Monk")),
            special.options.filter { it.source == HostPairRoleSource.DEMON_BLUFF }.map { it.role })
        assertFalse(special.options.any { it.role == RoleId("Empath") })
        update(s, 3, poisoned = true)
        val blocked = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Washerwoman"), 1, 3, 7))
        assertTrue(blocked.options.isEmpty())
        assertTrue(blocked.specialCandidateSeats.isEmpty())
        update(s, 1, poisoned = true)
        val sourcePoisoned = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Washerwoman"), 1, 3, 7))
        assertEquals(AbilityFunctioningState.POISONED, sourcePoisoned.sourceAbility)
        assertTrue(sourcePoisoned.options.isNotEmpty())
    }

    @Test fun librarianZeroAndSpyOutsiderRegistrationAreIndependentOfPoison() {
        val s = session("Librarian", "Spy", "Chef", "Investigator", "Imp")
        val noOutsiders = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Librarian"), 1, null, null))
        assertTrue(noOutsiders.canShowNoOutsiders)
        val withSpy = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Librarian"), 1, 2, 3))
        assertTrue(withSpy.options.any { it.role == RoleId("Recluse") })
        assertEquals(setOf(2), withSpy.specialCandidateSeats)
        update(s, 2, poisoned = true)
        val disabledSpy = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Librarian"), 1, 2, 3))
        assertTrue(disabledSpy.options.isEmpty())
        assertTrue(disabledSpy.canShowNoOutsiders)
        assertTrue(disabledSpy.specialCandidateSeats.isEmpty())
    }

    @Test fun onlyCurrentSessionQueryAndNumericTruthAcceptAIAdvice() {
        val s = session("Chef", "Empath", "Soldier", "Saint", "Imp")
        val metric = NumericMetric.ADJACENT_EVIL_PAIRS
        val current = requireNotNull(NumericInformationResolver.resolve(s.state, metric, 1))
        assertEquals(listOf(1, 2, 3, 4, 5), current.subjectSeats)
        val good = InformationProposition.NumericResult(metric, 1, current.subjectSeats, 0)
        assertTrue(NumericInformationResolver.acceptsCurrentAdvice(s.state, good))
        assertFalse(NumericInformationResolver.acceptsCurrentAdvice(
            s.state, good.copy(value = 2)))
        assertFalse(NumericInformationResolver.acceptsCurrentAdvice(
            s.state, good.copy(subjectSeats = listOf(1, 2))))
        val selected = PairInformationResolver.acceptsCurrentRoleAdvice(
            s.state, rules, RoleId("Chef"), 1, 2, 3, RoleId("Empath"))
        assertFalse(selected)
    }

    @Test fun secondBuiltinUsesOnlyItsOwnRoleCatalog() {
        val s = session("Investigator", "Empath", "Drunk", "Baron", "Imp",
            script = "no_greater_joy", shown = mapOf(3 to "Clockmaker"))
        val ngj = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
        val result = requireNotNull(PairInformationResolver.resolve(
            s.state, ngj, RoleId("Investigator"), 1, 4, 2))
        assertEquals(listOf(RoleId("Baron")), result.options.map { it.role })
        assertNull(PairInformationResolver.resolve(s.state, rules, RoleId("Investigator"), 1, 4, 2))
        assertEquals(listOf(0),
            NumericInformationResolver.resolve(s.state, NumericMetric.LIVING_EVIL_NEIGHBOURS, 2)?.values)
        // Stage 2 deliberately does not invent Clockmaker/Chambermaid answer semantics.
        assertNull(NumericInformationResolver.resolve(s.state, NumericMetric.STEPS_TO_NEAREST_MINION, 3))
    }

    @Test fun poisonedLibrarianMayBeToldZeroEvenWithAnActualOutsider() {
        val s = session("Librarian", "Recluse", "Chef", "Spy", "Imp")
        assertFalse(PairInformationResolver.resolve(
            s.state, rules, RoleId("Librarian"), 1, 2, 3)!!.canShowNoOutsiders)
        update(s, 1, poisoned = true)
        val unreliable = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Librarian"), 1, 2, 3))
        assertTrue(unreliable.canShowNoOutsiders)
        assertEquals(AbilityFunctioningState.POISONED, unreliable.sourceAbility)
    }

    @Test fun investigatorRecluseAndDrunkSourceDoNotInventZeroMinions() {
        val s = session("Investigator", "Recluse", "Chef", "Spy", "Imp")
        val recluse = requireNotNull(PairInformationResolver.resolve(
            s.state, rules, RoleId("Investigator"), 1, 2, 3))
        assertTrue(recluse.options.any { it.role == RoleId("Baron") })
        assertEquals(setOf(2), recluse.specialCandidateSeats)
        assertFalse(recluse.canShowNoOutsiders)
        update(s, 2, poisoned = true)
        assertTrue(PairInformationResolver.resolve(
            s.state, rules, RoleId("Investigator"), 1, 2, 3)!!.options.isEmpty())

        val drunk = session("Drunk", "Recluse", "Chef", "Imp", "Spy",
            shown = mapOf(1 to "Investigator"))
        val answer = requireNotNull(PairInformationResolver.resolve(
            drunk.state, rules, RoleId("Investigator"), 1, 2, 3))
        assertEquals(AbilityFunctioningState.DRUNK, answer.sourceAbility)
        assertTrue(answer.productShortlist)
        assertTrue(answer.options.isNotEmpty())
        assertFalse(answer.canShowNoOutsiders)
        assertNull(PairInformationResolver.resolve(drunk.state, rules, RoleId("Washerwoman"), 1, 2, 3))
    }
}
