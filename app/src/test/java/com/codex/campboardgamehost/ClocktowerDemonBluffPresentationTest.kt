package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDemonBluffPresentationTest {
    @Test
    fun `candidates exclude evil actual identities dead players and Drunk shown identity`() {
        val game = GameState(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), listOf(
            player(1, "Drunk", shown = "Fortune Teller"),
            player(2, "Chef").copy(alive = false),
            player(3, "Mayor"),
            player(4, "Spy").copy(actualAlignment = Alignment.GOOD),
            player(5, "Imp"),
        ), 1L)
        val candidates = clocktowerDemonBluffCandidates(game)
        val names = candidates.map { it.enName }
        assertTrue(candidates.all { it.team in setOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider) })
        assertTrue(names.none { it in setOf("Drunk", "Fortune Teller", "Chef", "Mayor", "Spy", "Imp", "Clockmaker") })
        assertTrue(names.containsAll(listOf("Washerwoman", "Monk", "Butler", "Recluse", "Saint")))
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `candidates stay within the current builtin script`() {
        val game = GameState(ClocktowerScript.NoGreaterJoy.toRecommendationScriptId(), listOf(
            player(1, "Drunk", shown = "Empath"), player(2, "Imp"), player(3, "Clockmaker"),
        ), 2L)
        assertEquals(setOf("Investigator", "Chambermaid", "Artist", "Sage", "Klutz"),
            clocktowerDemonBluffCandidates(game).map { it.enName }.toSet())
    }

    @Test
    fun `occupied or Drunk shown identities cannot form a ready recommendation`() {
        val game = GameState(ClocktowerScript.TroubleBrewing.toRecommendationScriptId(), listOf(
            player(1, "Drunk", shown = "Monk"), player(2, "Chef"), player(3, "Imp"),
        ), 3L)
        val candidates = clocktowerDemonBluffCandidates(game)
        assertTrue(!manualDemonBluffSelectionReady(listOf("Monk", "Mayor", "Saint"), candidates))
        assertTrue(!manualDemonBluffSelectionReady(listOf("Chef", "Mayor", "Saint"), candidates))
        assertTrue(manualDemonBluffSelectionReady(listOf("Washerwoman", "Mayor", "Saint"), candidates))
    }

    @Test
    fun `exact three roles resolve in input order`() {
        val legal = listOf(
            role("Chef"),
            role("Mayor"),
            role("Monk"),
            role("Undertaker"),
        )

        val result = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk", "Undertaker"),
            legalRoles = legal,
        )

        assertEquals(
            listOf("Mayor", "Monk", "Undertaker"),
            (result as DemonBluffPresentationResolution.Ready).roles.map { it.enName },
        )
    }

    @Test
    fun `missing input stays pending and never becomes first three legal roles`() {
        val result = resolveDemonBluffPresentation(
            recommendedRoleNames = null,
            legalRoles = listOf(role("Chef"), role("Empath"), role("Fortune Teller"), role("Mayor")),
        )

        assertTrue(result is DemonBluffPresentationResolution.Pending)
    }

    @Test
    fun `partial or unresolved input is invalid and never silently substituted`() {
        val legal = listOf(role("Chef"), role("Empath"), role("Mayor"), role("Monk"))

        val partial = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk"),
            legalRoles = legal,
        )
        val unresolved = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk", "Undertaker"),
            legalRoles = legal,
        )

        assertTrue(partial is DemonBluffPresentationResolution.Invalid)
        assertEquals(
            listOf("Undertaker"),
            (unresolved as DemonBluffPresentationResolution.Invalid).unresolvedRoleNames,
        )
    }

    @Test
    fun `manual bluff selection is legal only and capped at three`() {
        val legal = setOf("Chef", "Empath", "Mayor", "Monk")

        val one = toggleManualDemonBluffSelection(emptyList(), "Chef", legal)
        val two = toggleManualDemonBluffSelection(one, "Empath", legal)
        val three = toggleManualDemonBluffSelection(two, "Mayor", legal)

        assertEquals(listOf("Chef", "Empath", "Mayor"), three)
        assertEquals(three, toggleManualDemonBluffSelection(three, "Monk", legal))
        assertEquals(three, toggleManualDemonBluffSelection(three, "Spy", legal))
        assertEquals(
            listOf("Chef", "Mayor"),
            toggleManualDemonBluffSelection(three, "Empath", legal),
        )
    }

    @Test
    fun `manual bluff selection is ready only for an exact legal triple`() {
        val legal = listOf(role("Chef"), role("Empath"), role("Mayor"), role("Monk"))

        assertTrue(
            manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath", "Mayor"),
                legalRoles = legal,
            ),
        )
        assertTrue(
            !manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath"),
                legalRoles = legal,
            ),
        )
        assertTrue(
            !manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath", "Spy"),
                legalRoles = legal,
            ),
        )
    }

    private fun role(enName: String) = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = enName,
        enName = enName,
        zhDescription = "",
        enDescription = "",
    )

    private fun player(seat: Int, name: String, shown: String? = null): PlayerState {
        val role = requireNotNull(clocktowerRoleByName(name))
        val type = when (role.team) {
            ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
            ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
            ClocktowerTeam.Minion -> CharacterType.MINION
            ClocktowerTeam.Demon -> CharacterType.DEMON
        }
        return PlayerState(seat, "P$seat", RoleId(name),
            if (type in setOf(CharacterType.MINION, CharacterType.DEMON)) Alignment.EVIL else Alignment.GOOD,
            type, shown?.let(::RoleId))
    }
}
