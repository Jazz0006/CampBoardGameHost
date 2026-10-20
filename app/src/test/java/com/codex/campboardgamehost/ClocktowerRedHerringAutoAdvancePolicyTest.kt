package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvancePolicyTest {
    @Test
    fun `accepted joint red herring may be reused only by automatic Host`() {
        assertTrue(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = true,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = true,
            acceptedJointOpening = true,
        ))
        assertFalse(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = true,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = false,
            acceptedJointOpening = true,
        ))
        assertFalse(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = false,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = true,
            acceptedJointOpening = true,
        ))
    }

    @Test
    fun `red herring auto advance skips only non-actions and never silently confirms real selection`() {
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = false,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `live target query may be shown once and then only reopened unchanged`() {
        val shown = InformationProposition.RoleAt(5, RoleId("Spy"))
        val record = RecordedEpistemicObservation(
            recordId = "ravenkeeper-n2-5", phase = StorytellerPhase.NIGHT,
            round = 2, sequence = 6, sourceSeat = 3,
            sourceAbility = RoleId("Ravenkeeper"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(3),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = shown,
        )
        fun verdict(
            info: InformationProposition? = shown,
            records: List<RecordedEpistemicObservation> = listOf(record),
            action: ClocktowerNightAction = ClocktowerNightAction.Ravenkeeper,
        ) = clocktowerLiveQueryRevealDisposition(
            action = action, phase = ClocktowerPhase.Night, round = 2,
            actorSeat = 3, roleEnName = "Ravenkeeper", shown = info,
            confirmedObservations = records,
        )
        assertEquals(ClocktowerLiveQueryRevealDisposition.NEW,
            verdict(records = emptyList()))
        assertEquals(ClocktowerLiveQueryRevealDisposition.REOPEN, verdict())
        assertEquals(ClocktowerLiveQueryRevealDisposition.CONFLICT,
            verdict(info = InformationProposition.RoleAt(4, RoleId("Spy"))))
        assertEquals(ClocktowerLiveQueryRevealDisposition.CONFLICT,
            verdict(info = InformationProposition.RoleAt(5, RoleId("Washerwoman"))))
        assertEquals(ClocktowerLiveQueryRevealDisposition.CONFLICT,
            verdict(info = null))
        assertEquals(ClocktowerLiveQueryRevealDisposition.CONFLICT,
            verdict(records = listOf(record, record.copy(proposition =
                InformationProposition.RoleAt(4, RoleId("Spy"))))))
        assertEquals(ClocktowerLiveQueryRevealDisposition.NEW,
            verdict(records = listOf(record.copy(round = 1))))
        assertEquals(ClocktowerLiveQueryRevealDisposition.NEW,
            verdict(records = listOf(record.copy(sourceSeat = 4))))
        assertEquals(ClocktowerLiveQueryRevealDisposition.NEW,
            verdict(action = ClocktowerNightAction.MonkProtect))
        // The same lock protects the other two player-selected query roles.
        val boolean = InformationProposition.BooleanResult(
            com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            3, listOf(4, 5), true,
        )
        val fortuneRecord = record.copy(
            sourceAbility = RoleId("Fortune Teller"), proposition = boolean)
        assertEquals(ClocktowerLiveQueryRevealDisposition.REOPEN,
            clocktowerLiveQueryRevealDisposition(
                action = ClocktowerNightAction.FortuneTeller,
                phase = ClocktowerPhase.Night, round = 2, actorSeat = 3,
                roleEnName = "Fortune Teller", shown = boolean,
                confirmedObservations = listOf(fortuneRecord),
            ))
        val chambermaidRecord = record.copy(
            sourceAbility = RoleId("Chambermaid"),
            proposition = InformationProposition.NumericResult(
                NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(4, 5), 1),
        )
        assertEquals(ClocktowerLiveQueryRevealDisposition.CONFLICT,
            clocktowerLiveQueryRevealDisposition(
                action = ClocktowerNightAction.Chambermaid,
                phase = ClocktowerPhase.Night, round = 2, actorSeat = 3,
                roleEnName = "Chambermaid",
                shown = InformationProposition.NumericResult(
                    NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 3, listOf(4, 6), 1),
                confirmedObservations = listOf(chambermaidRecord),
            ))
    }

    @Test
    fun `stale prior step AI error or busy never blocks a current Host-determined result`() {
        assertFalse(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = "round1-chef",
            busy = true, error = "old error"))
        assertFalse(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = null,
            busy = true, error = null))
        assertFalse(clocktowerCurrentAiRequestBlocked(
            currentKey = null, adviceKey = null,
            busy = true, error = "old error"))
        assertTrue(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = "round2-ft-1-3",
            busy = true, error = null))
        assertTrue(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = "round2-ft-1-3",
            busy = false, error = "current error"))
    }
}
