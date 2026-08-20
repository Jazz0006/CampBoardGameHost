package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionExplanation
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.PostGameDecisionReview
import com.codex.campboardgamehost.clocktower.domain.ReviewedDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository

internal class HistoryReviewModule {
    fun explainEvent(archive: DecisionHistoryArchive, eventId: String): DecisionExplanation? =
        archive.events.firstOrNull { it.eventId == eventId }?.let(::eventExplanation)

    fun postGameReview(archive: DecisionHistoryArchive): PostGameDecisionReview {
        val projection = DecisionHistoryRepository(archive).project()
        val replaced = archive.corrections.map { it.replacedEventId }.toSet()
        return PostGameDecisionReview(
            decisions = archive.events.map { event ->
                ReviewedDecision(
                    eventId = event.eventId,
                    requestId = event.requestId,
                    selectedCandidateId = event.selectedCandidateId,
                    status = event.status,
                    corrected = event.eventId in replaced,
                    explanation = eventExplanation(event),
                )
            },
            effectiveDecisionCount = projection.effectiveEvents.size,
            failedDecisionCount = archive.events.count { it.status == DecisionEventStatus.FAILED },
            correctionCount = archive.corrections.size,
            misinformationLedger = projection.misinformationLedger,
            registrationLedgerBySeat = projection.registrationLedgerBySeat,
            pressureBySeat = projection.pressureBySeat,
        )
    }

    private fun eventExplanation(event: StorytellerDecisionEvent): DecisionExplanation = DecisionExplanation(
        decisionId = event.eventId,
        status = event.status,
        qualityTier = event.qualityTier,
        totalScore = event.totalScore,
        finalProbabilityFixedPoint = event.finalProbabilityFixedPoint,
        explanationCodes = event.explanationCodes,
        warningCodes = emptyList(),
        affectedSeats = (event.pressureDelta.keys + event.registrations.map { it.subjectSeat }).toSet(),
        alternativeCandidateIds = event.candidateAudit.map { it.candidateId }.filterNot { it == event.selectedCandidateId },
    )
}
