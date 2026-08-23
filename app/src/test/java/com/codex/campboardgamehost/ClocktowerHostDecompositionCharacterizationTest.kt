package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerHostDecompositionCharacterizationTest {
    private fun card(
        team: ClocktowerTeam?,
        actualRole: ClocktowerRole? = null,
        shownRole: ClocktowerRole? = actualRole,
        name: String = "P1",
        eliminatedRound: Int? = null,
    ): PlayerCard = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = team,
        clocktowerRole = actualRole,
        clocktowerShownRole = shownRole,
        eliminatedRound = eliminatedRound,
    )

    private val monk = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = "僧侣",
        enName = "Monk",
        zhDescription = "",
        enDescription = "",
    )

    private val empath = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = "共情者",
        enName = "Empath",
        zhDescription = "",
        enDescription = "",
    )

    private val chef = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = "厨师",
        enName = "Chef",
        zhDescription = "",
        enDescription = "",
    )

    private val demon = ClocktowerRole(
        team = ClocktowerTeam.Demon,
        zhName = "恶魔",
        enName = "Imp",
        zhDescription = "",
        enDescription = "",
    )

    @Test
    fun `actual role cards match actual english role and preserve order`() {
        val drunk = card(ClocktowerTeam.Outsider, monk, empath, name = "Drunk")
        val target = card(ClocktowerTeam.Townsfolk, empath, empath, name = "Target")
        val later = card(ClocktowerTeam.Townsfolk, empath, empath, name = "Later")

        assertEquals(listOf("Target", "Later"), actualClocktowerRoleCards(listOf(drunk, target, later), "Empath").map { it.name })
    }

    @Test
    fun `storyteller pair keeps target first and selects first legal decoy`() {
        val target = card(ClocktowerTeam.Townsfolk, monk, name = "Target")
        val excluded = card(ClocktowerTeam.Townsfolk, empath, name = "Excluded")
        val decoy = card(ClocktowerTeam.Townsfolk, empath, name = "Decoy")
        val nextDecoy = card(ClocktowerTeam.Townsfolk, monk, name = "NextDecoy")

        assertEquals(
            target to decoy,
            storytellerPairHint(target, listOf(target, excluded, decoy), excludeNames = setOf("Excluded")),
        )
        assertEquals(target to nextDecoy, storytellerPairHint(target, listOf(target, excluded, nextDecoy), excludeNames = setOf("Excluded")))
        assertEquals(null, storytellerPairHint(target, listOf(target)))
    }

    @Test
    fun `red herring candidates are stable legal-team filtering`() {
        val townsfolk = card(ClocktowerTeam.Townsfolk, monk, name = "Town")
        val outsider = card(ClocktowerTeam.Outsider, empath, name = "Outsider")
        val minion = card(ClocktowerTeam.Minion, demon, name = "Minion")

        assertEquals(
            listOf("Town", "Outsider"),
            clocktowerRedHerringCandidates(listOf(townsfolk, minion, outsider)).map { it.name },
        )
    }

    @Test
    fun `chef pairs preserve fixed evaluator adjacent and empty results`() {
        val cards = listOf(
            card(ClocktowerTeam.Minion, demon, name = "A"),
            card(ClocktowerTeam.Demon, demon, name = "B"),
            card(ClocktowerTeam.Townsfolk, chef, name = "C"),
        )

        assertEquals(1, chefEvilPairs(cards))
        assertEquals(0, chefEvilPairs(cards) { false })
    }

    @Test
    fun `living neighbors skip dead players and preserve circular seat order`() {
        val cards = listOf(
            card(ClocktowerTeam.Townsfolk, monk, name = "P1"),
            card(ClocktowerTeam.Townsfolk, monk, name = "P2", eliminatedRound = 1),
            card(ClocktowerTeam.Townsfolk, monk, name = "P3"),
            card(ClocktowerTeam.Townsfolk, monk, name = "P4", eliminatedRound = 1),
            card(ClocktowerTeam.Townsfolk, monk, name = "P5"),
        )

        assertEquals(listOf("P5", "P3"), livingNeighbors(cards, "P1").map { it.name })
        assertEquals(emptyList<String>(), livingNeighbors(cards, "P2").map { it.name })
    }

    @Test
    fun `empath count uses injected evil predicate and returns zero for missing source`() {
        val cards = listOf(
            card(ClocktowerTeam.Townsfolk, empath, name = "P1"),
            card(ClocktowerTeam.Townsfolk, monk, name = "P2"),
            card(ClocktowerTeam.Townsfolk, monk, name = "P3", eliminatedRound = 1),
            card(ClocktowerTeam.Townsfolk, monk, name = "P4"),
        )

        assertEquals(1, empathEvilNeighborCount(cards, "P1") { it.name == "P2" })
        assertEquals(0, empathEvilNeighborCount(cards, "Missing") { true })
    }

    @Test
    fun `evil classification remains minion or demon only`() {
        assertFalse(isClocktowerEvil(card(ClocktowerTeam.Townsfolk)))
        assertFalse(isClocktowerEvil(card(ClocktowerTeam.Outsider)))
        assertTrue(isClocktowerEvil(card(ClocktowerTeam.Minion)))
        assertTrue(isClocktowerEvil(card(ClocktowerTeam.Demon)))
        assertFalse(isClocktowerEvil(card(null)))
    }

    @Test
    fun `shown as different role requires both roles and different english names`() {
        assertFalse(card(ClocktowerTeam.Townsfolk, monk, monk).clocktowerShownAsDifferentRole())
        assertTrue(card(ClocktowerTeam.Townsfolk, monk, empath).clocktowerShownAsDifferentRole())
        assertFalse(card(ClocktowerTeam.Townsfolk, monk, null).clocktowerShownAsDifferentRole())
        assertFalse(card(ClocktowerTeam.Townsfolk, null, empath).clocktowerShownAsDifferentRole())
    }

    @Test
    fun `private observation record id remains deterministic and proposition sensitive`() {
        val proposition = InformationProposition.RoleAt(2, RoleId("Monk"))
        val same = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            roleEnName = "Washerwoman",
            actorSeat = 1,
            proposition = proposition,
        )
        val repeated = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            roleEnName = "Washerwoman",
            actorSeat = 1,
            proposition = proposition,
        )
        val different = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            roleEnName = "Washerwoman",
            actorSeat = 1,
            proposition = InformationProposition.RoleAt(3, RoleId("Monk")),
        )

        assertEquals(same, repeated)
        assertNotEquals(same, different)
        assertTrue(same.startsWith("private-game-1-FirstNight-1-Washerwoman-1-"))
        assertEquals(64, same.substringAfterLast('-').length)
    }
}
