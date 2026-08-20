package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SelectionDistributionTelemetryTest {
    private val dimensions = SelectionAuditDimensions(5, StorytellerPhase.FIRST_NIGHT, RecommendationStyle.BALANCED)

    @Test fun `records opportunity eligibility highest tier and selection by family dimensions`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        val first = record("first")
        val second = record("second")
        recorder.recordPreview(first)
        recorder.recordPreview(second)
        recorder.recordCommittedSelection(commit(first, "truth"))

        val snapshot = recorder.snapshot()
        val truth = snapshot.getValue(key("truth"))
        assertEquals(2, truth.familyOpportunityCount)
        assertEquals(2, truth.familyEligibleCount)
        assertEquals(2, truth.familyHighestTierCount)
        assertEquals(1, truth.familySelectedCount)
        assertEquals(0.5, truth.selectionRateGivenEligibility!!, 0.0)

        val falsehood = snapshot.getValue(key("falsehood"))
        assertEquals(2, falsehood.familyOpportunityCount)
        assertEquals(0, falsehood.familyEligibleCount)
        assertEquals(0, falsehood.familyHighestTierCount)
        assertEquals(0, falsehood.familySelectedCount)
        assertNull(falsehood.selectionRateGivenEligibility)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot record selection from manual only family`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        val preview = SelectionAuditRecord(
            selectionId = "manual-selection",
            dimensions = dimensions,
            candidates = listOf(SelectionAuditCandidate("manual", QualityTier.EXPERT_ONLY)),
        )
        recorder.recordPreview(preview)
        recorder.recordCommittedSelection(commit(preview, "manual"))
    }

    @Test fun `same family in another phase is a separate withholding stratum`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        val firstNight = record("first-night")
        val night = record("night", StorytellerPhase.NIGHT)
        recorder.recordPreview(firstNight)
        recorder.recordPreview(night)
        recorder.recordCommittedSelection(commit(firstNight, "truth"))
        recorder.recordCommittedSelection(commit(night, "truth"))

        assertEquals(4, recorder.snapshot().size)
        assertEquals(1, recorder.snapshot().getValue(key("truth")).familySelectedCount)
        assertEquals(1, recorder.snapshot().getValue(key("truth", StorytellerPhase.NIGHT)).familySelectedCount)
    }

    @Test fun `recomposition cannot double count the same selection`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        val record = record("stable")

        recorder.recordPreview(record)
        recorder.recordPreview(record)
        recorder.recordCommittedSelection(commit(record, "truth"))
        recorder.recordCommittedSelection(commit(record, "truth"))

        assertEquals(1, recorder.snapshot().getValue(key("truth")).familySelectedCount)
        assertEquals(1, recorder.snapshot().getValue(key("truth")).familyOpportunityCount)
    }

    @Test fun `a preview never counts as a committed selection`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        recorder.recordPreview(record("preview-only"))

        assertEquals(0, recorder.snapshot().getValue(key("truth")).familySelectedCount)
    }

    @Test fun `bounded export contains aggregate strata only`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        recorder.recordPreview(record("export"))

        val export = recorder.exportSnapshot(maxStrata = 1)
        assertEquals(1, export.size)
        assertEquals(1, export.values.single().familyOpportunityCount)
    }

    @Test fun `a new exact decision revision is a separate opportunity`() {
        val recorder = SelectionDistributionTelemetryRecorder()
        val revisionOne = record("decision-revision-1")
        val revisionTwo = record("decision-revision-2")

        recorder.recordPreview(revisionOne)
        recorder.recordPreview(revisionTwo)
        recorder.recordCommittedSelection(commit(revisionOne, "truth"))
        recorder.recordCommittedSelection(commit(revisionTwo, "truth"))

        val truth = recorder.snapshot().getValue(key("truth"))
        assertEquals(2, truth.familyOpportunityCount)
        assertEquals(2, truth.familySelectedCount)
    }

    private fun record(
        selectionId: String,
        phase: StorytellerPhase = StorytellerPhase.FIRST_NIGHT,
    ) = SelectionAuditRecord(
        selectionId = "selection:$selectionId",
        dimensions = dimensions.copy(phase = phase),
        candidates = listOf(
            SelectionAuditCandidate("truth", QualityTier.RECOMMENDED),
            SelectionAuditCandidate("truth", QualityTier.EXPERT_ONLY),
            SelectionAuditCandidate("falsehood", QualityTier.REJECTED),
        ),
    )

    private fun commit(record: SelectionAuditRecord, selectedFamilyId: String) = SelectionAuditCommit(
        selectionId = record.selectionId,
        dimensions = record.dimensions,
        selectedFamilyId = selectedFamilyId,
    )

    private fun key(family: String, phase: StorytellerPhase = StorytellerPhase.FIRST_NIGHT) =
        SelectionAuditKey(family, 5, phase, RecommendationStyle.BALANCED)
}
