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

    @Test fun fortuneTellerDetectsDemonAndRedHerringButNotSpyAndKeepsRealRecluseDiscretion() {
        val s = session("Fortune Teller", "Imp", "Spy", "Recluse", "Soldier")
        s.commitRedHerringSeat(5, StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence)
        fun values(a: Int, b: Int) = FortuneTellerInformationResolver.resolve(s.state, 1, listOf(a, b))?.values
        val before = s.state
        assertEquals(listOf(false), values(1, 3)) // Spy cannot register as Demon; self-query is allowed.
        assertEquals(listOf(true), values(2, 4)) // A real positive dominates optional Recluse registration.
        assertEquals(listOf(true), values(3, 5))
        assertEquals(listOf(false, true), values(3, 4))
        assertSame(before, s.state)
        update(s, 2, alive = false, poisoned = true)
        update(s, 5, alive = false, poisoned = true)
        update(s, 4, alive = false)
        assertEquals(listOf(true), values(1, 2))
        assertEquals(listOf(true), values(1, 5))
        assertEquals(listOf(false, true), values(3, 4)) // Recluse retains registration after death.
        update(s, 4, poisoned = true)
        assertEquals(listOf(false), values(3, 4))
        assertNull(FortuneTellerInformationResolver.resolve(s.state, 1, listOf(2, 2)))
        assertNull(FortuneTellerInformationResolver.resolve(s.state, 1, listOf(2, 99)))
        assertNull(FortuneTellerInformationResolver.resolve(s.state, 1, listOf(2)))
    }

    @Test fun fortuneTellerSourceImpairmentAndChangedQueriesRevalidateAdvice() {
        val s = session("Fortune Teller", "Imp", "Spy", "Soldier", "Saint")
        fun resolve(a: Int, b: Int) = requireNotNull(FortuneTellerInformationResolver.resolve(s.state, 1, listOf(a, b)))
        val answer = InformationProposition.BooleanResult(
            com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            1, listOf(2, 3), true)
        assertTrue(resolve(2, 3).accepts(answer))
        assertFalse(resolve(3, 4).accepts(answer))
        assertFalse(resolve(2, 3).accepts(answer.copy(sourceSeat = 4)))
        assertFalse(resolve(2, 3).accepts(answer.copy(value = false)))
        update(s, 1, poisoned = true)
        assertEquals(listOf(false, true), resolve(2, 3).values)
        assertEquals(AbilityFunctioningState.POISONED, resolve(2, 3).sourceAbility)
        update(s, 1, poisoned = false)
        assertFalse(resolve(2, 3).accepts(answer.copy(value = false)))
        val drunk = session("Drunk", "Imp", "Spy", "Soldier", "Saint", shown = mapOf(1 to "Fortune Teller"))
        assertEquals(listOf(false, true), FortuneTellerInformationResolver.resolve(drunk.state, 1, listOf(2, 3))?.values)
        update(s, 1, alive = false)
        assertNull(FortuneTellerInformationResolver.resolve(s.state, 1, listOf(2, 3)))
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

    @Test fun clockmakerUsesShortestPhysicalArcAndOnlyFirstNight() {
        val ngj = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
        val near = session("Clockmaker", "Artist", "Imp", "Sage", "Scarlet Woman",
            script = "no_greater_joy")
        val resolved = requireNotNull(NumericInformationResolver.resolveLaterNight(
            near.state, ngj, NumericMetric.STEPS_TO_NEAREST_MINION, 1,
        ))
        assertEquals(listOf(2), resolved.values)
        assertEquals((1..5).toList(), resolved.subjectSeats)
        assertFalse(resolved.productShortlist)

        val wraparound = session("Scarlet Woman", "Clockmaker", "Artist", "Sage", "Imp",
            script = "no_greater_joy")
        // Imp at seat 5 and minion at seat 1 are one physical step apart.
        assertEquals(listOf(1), NumericInformationResolver.resolveLaterNight(
            wraparound.state, ngj, NumericMetric.STEPS_TO_NEAREST_MINION, 2,
        )?.values)
        assertNull(NumericInformationResolver.resolveLaterNight(
            near.state, rules, NumericMetric.STEPS_TO_NEAREST_MINION, 1,
        ))
        update(near, 1, poisoned = true)
        val poisoned = requireNotNull(NumericInformationResolver.resolveLaterNight(
            near.state, ngj, NumericMetric.STEPS_TO_NEAREST_MINION, 1,
        ))
        assertEquals(AbilityFunctioningState.POISONED, poisoned.sourceAbility)
        assertEquals((0..5).toList(), poisoned.values)
    }

    @Test fun chambermaidCountsOnlyActualAbilityWakersAndRequiresCurrentAlivePair() {
        val ngj = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
        val s = session("Clockmaker", "Investigator", "Chambermaid", "Scarlet Woman", "Imp",
            script = "no_greater_joy")
        val before = s.state
        assertEquals(listOf(2), NumericInformationResolver.resolveLaterNight(
            s.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(1, 2),
        )?.values)
        assertEquals(listOf(0), NumericInformationResolver.resolveLaterNight(
            s.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(4, 5),
        )?.values)
        assertNull(NumericInformationResolver.resolveLaterNight(
            s.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(2, 3),
        ))
        assertNull(NumericInformationResolver.resolveLaterNight(
            s.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(2, 2),
        ))
        assertSame(before, s.state)
        update(s, 4, alive = false)
        assertNull(NumericInformationResolver.resolveLaterNight(
            s.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(4, 5),
        ))
        update(s, 3, poisoned = true)
        assertEquals(listOf(0, 1, 2), NumericInformationResolver.resolveLaterNight(
            s.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(1, 2),
        )?.values)
    }

    @Test fun chambermaidDoesNotCountFakeDrunkWakeAndDrunkClockmakerHasNoFixedAnswer() {
        val ngj = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
        val misleadingWake = session(
            "Clockmaker", "Drunk", "Chambermaid", "Scarlet Woman", "Imp",
            shown = mapOf(2 to "Investigator"), script = "no_greater_joy",
        )
        // Clockmaker woke for their own ability. Seat 2 woke as a fake
        // Investigator but is actually the Drunk, so the true answer is 1.
        assertEquals(listOf(1), NumericInformationResolver.resolveLaterNight(
            misleadingWake.state, ngj, NumericMetric.PLAYERS_WAKING_FOR_ABILITY,
            3, listOf(1, 2),
        )?.values)

        val fakeClockmaker = session(
            "Drunk", "Artist", "Imp", "Sage", "Scarlet Woman",
            shown = mapOf(1 to "Clockmaker"), script = "no_greater_joy",
        )
        val arbitrary = requireNotNull(NumericInformationResolver.resolveLaterNight(
            fakeClockmaker.state, ngj, NumericMetric.STEPS_TO_NEAREST_MINION, 1,
        ))
        assertEquals(AbilityFunctioningState.DRUNK, arbitrary.sourceAbility)
        assertEquals((0..5).toList(), arbitrary.values)
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
