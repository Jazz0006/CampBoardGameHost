package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerDayWorkspacePolicyTest {
    @Test
    fun `slayer claimant eligibility uses only public life state and prior public claims`() {
        val cards = listOf(
            card(name = "Alice", actualRoleLabel = "Slayer"),
            card(name = "Bob", actualRoleLabel = "Imp"),
            card(name = "Casey", actualRoleLabel = "Chef", eliminatedRound = 1),
        )

        val candidates = clocktowerDaySlayerClaimantCandidates(
            cards = cards,
            alreadyClaimedNames = setOf("Bob"),
        )

        assertEquals(listOf("Alice"), candidates.map(PlayerCard::name))
    }

    @Test
    fun `changing hidden actual roles cannot change slayer claimant eligibility`() {
        val firstHiddenWorld = listOf(
            card(name = "Alice", actualRoleLabel = "Slayer"),
            card(name = "Bob", actualRoleLabel = "Imp"),
            card(name = "Casey", actualRoleLabel = "Chef"),
        )
        val secondHiddenWorld = listOf(
            card(name = "Alice", actualRoleLabel = "Imp"),
            card(name = "Bob", actualRoleLabel = "Chef"),
            card(name = "Casey", actualRoleLabel = "Slayer"),
        )

        val firstCandidates = clocktowerDaySlayerClaimantCandidates(
            cards = firstHiddenWorld,
            alreadyClaimedNames = setOf("Casey"),
        )
        val secondCandidates = clocktowerDaySlayerClaimantCandidates(
            cards = secondHiddenWorld,
            alreadyClaimedNames = setOf("Casey"),
        )

        assertEquals(firstCandidates.map(PlayerCard::name), secondCandidates.map(PlayerCard::name))
        assertEquals(listOf("Alice", "Bob"), firstCandidates.map(PlayerCard::name))
    }

    private fun card(
        name: String,
        actualRoleLabel: String,
        eliminatedRound: Int? = null,
    ): PlayerCard = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        actualRoleLabel = actualRoleLabel,
        eliminatedRound = eliminatedRound,
    )
}
