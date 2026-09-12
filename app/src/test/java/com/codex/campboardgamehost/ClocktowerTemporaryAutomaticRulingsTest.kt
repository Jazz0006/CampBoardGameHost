package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerTemporaryAutomaticRulingsTest {
    @Test
    fun `spy automatic registration audit uses complete selector domain when legacy recommendations are empty`() {
        assertRegistrationAuditUsesSelectorDomain(
            legalSpecialRoleEnNames = listOf("Washerwoman", "Chef", "Chef"),
            decisionKey = "spy-registration-audit",
            expectedFamilies = listOf("actual-registration", "special-registration", "special-registration"),
        )
    }

    @Test
    fun `recluse automatic registration audit uses complete selector domain when legacy recommendations are empty`() {
        assertRegistrationAuditUsesSelectorDomain(
            legalSpecialRoleEnNames = listOf("Poisoner", "Imp"),
            decisionKey = "recluse-registration-audit",
            expectedFamilies = listOf("actual-registration", "special-registration", "special-registration"),
        )
    }

    @Test
    fun `mayor automatic redirect candidates keep only other living townsfolk`() {
        val candidates = listOf(
            ClocktowerTemporaryMayorCandidate(seat = 1, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 2, team = ClocktowerTeam.Townsfolk, alive = false),
            ClocktowerTemporaryMayorCandidate(seat = 3, team = ClocktowerTeam.Outsider, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 4, team = ClocktowerTeam.Minion, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 9, team = ClocktowerTeam.Townsfolk, alive = true),
        )

        assertEquals(
            listOf(1),
            clocktowerTemporaryMayorEligibleTownsfolkSeats(
                candidates = candidates,
                mayorSeat = 9,
            ),
        )
    }

    @Test
    fun `mayor automatic redirect candidate projection is order independent`() {
        val candidates = listOf(
            ClocktowerTemporaryMayorCandidate(seat = 7, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 2, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 5, team = ClocktowerTeam.Outsider, alive = true),
        )

        assertEquals(
            listOf(2, 7),
            clocktowerTemporaryMayorEligibleTownsfolkSeats(
                candidates = candidates.reversed(),
                mayorSeat = 4,
            ),
        )
    }

    @Test
    fun `beginner Mayor ruling advances only after automatic target is applied`() {
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = null,
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Bob",
                automaticTargetName = "Alice",
            ),
        )
        assertTrue(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = false,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.DemonSuccessor,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
    }

    @Test
    fun `night ruling decision key excludes mutable revision and separates decision families`() {
        val mayorKey = clocktowerTemporaryNightDecisionKey(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 11,
            family = "mayor-redirect",
        )
        val sameMayorKey = clocktowerTemporaryNightDecisionKey(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 11,
            family = "mayor-redirect",
        )
        val successionKey = clocktowerTemporaryNightDecisionKey(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 11,
            family = "demon-succession",
        )

        assertEquals(mayorKey, sameMayorKey)
        assertNotEquals(mayorKey, successionKey)
        assertEquals(
            clocktowerTemporaryAutomaticDecisionSeed(mayorKey),
            clocktowerTemporaryAutomaticDecisionSeed(sameMayorKey),
        )
        assertNotEquals(
            clocktowerTemporaryAutomaticDecisionSeed(mayorKey),
            clocktowerTemporaryAutomaticDecisionSeed(successionKey),
        )
    }

    private fun assertRegistrationAuditUsesSelectorDomain(
        legalSpecialRoleEnNames: List<String>,
        decisionKey: String,
        expectedFamilies: List<String>,
    ) {
        val legacyRecommendations = emptyList<ClocktowerRegistrationRecommendationOption>()
        assertTrue(legacyRecommendations.isEmpty())

        val selection = clocktowerTemporaryRegistrationSelection(
            legalSpecialRoleEnNames = legalSpecialRoleEnNames,
            decisionKey = decisionKey,
        )
        val auditCandidates = clocktowerTemporaryRegistrationAuditCandidates(legalSpecialRoleEnNames)
        val selectedFamily = clocktowerTemporaryRegistrationAuditFamilyId(selection.selected.payload)

        assertEquals(expectedFamilies, auditCandidates.map { it.familyId })
        assertTrue(auditCandidates.any { it.familyId == selectedFamily })

        val dimensions = SelectionAuditDimensions(
            playerCount = 5,
            phase = StorytellerPhase.FIRST_NIGHT,
            style = RecommendationStyle.BALANCED,
        )
        val record = SelectionAuditRecord(
            selectionId = decisionKey,
            dimensions = dimensions,
            candidates = auditCandidates,
        )
        val recorder = SelectionDistributionTelemetryRecorder()
        recorder.recordPreview(record)
        recorder.recordCommittedSelection(
            SelectionAuditCommit(
                selectionId = decisionKey,
                dimensions = dimensions,
                selectedFamilyId = selectedFamily,
            ),
        )

        assertEquals(1, recorder.snapshot().values.sumOf { it.familySelectedCount })
    }
}
