package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalConfirmationChainFeatureProjectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-3b2-confirmation-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)

    @Test
    fun `projects canonical historical support without copying history ownership`() {
        val historicalRecord = record(
            id = "history:not-poisoner",
            sequence = 1,
            globalSequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("history-channel"),
            proposition = InformationProposition.Not(
                InformationProposition.RoleAt(4, RoleId("Poisoner")),
            ),
        )
        val exactContext = exactContext(EpistemicObservationLog(listOf(historicalRecord)))
        val exactCandidate = candidate(
            id = "candidate:imp",
            proposition = InformationProposition.RoleAt(4, RoleId("Imp")),
        )
        val sdeCandidate = sdeCandidate(
            candidateId = exactCandidate.candidateId,
            observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
            historyRefs = listOf(
                SdeHistoricalObservationRef(
                    recordId = historicalRecord.recordId,
                    globalSequence = 0,
                ),
            ),
        )
        val context = ExactConsequenceContext(
            validatedRuleset = validatedRuleset,
            exactContext = exactContext,
        )
        val full = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest(
                decisionId = "decision",
                candidates = listOf(exactCandidate),
            ),
            context = context,
        )
        assertTrue(full is ExactConsequenceEvaluation.Ready)

        val projected = HistoricalConfirmationChainFeatureProjector.project(
            fullEvaluation = full as ExactConsequenceEvaluation.Ready,
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sdeCandidate),
            context = context,
        )

        val feature = projected.getValue(exactCandidate.candidateId)
        assertTrue(feature is FeatureProjection.Projected)
        val confirmation = (feature as FeatureProjection.Projected).value
        assertEquals(setOf(historicalRecord.recordId), confirmation.supportingObservationIds)
        assertEquals(historicalRecord.recordId, confirmation.historicalObservationImpacts.single().provenance.observationRef.recordId)
        assertEquals(
            ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION,
            confirmation.historicalObservationImpacts.single().relation,
        )
        assertTrue(confirmation.historicalObservationImpacts.single().authenticatesDistinctSource)
        assertEquals(listOf(historicalRecord), exactContext.observationLog.records)
    }

    @Test
    fun `missing exact capability leaves confirmation feature explicitly unavailable`() {
        val historicalRecord = record(
            id = "history:not-poisoner",
            sequence = 1,
            globalSequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("history-channel"),
            proposition = InformationProposition.Not(
                InformationProposition.RoleAt(4, RoleId("Poisoner")),
            ),
        )
        val historical = exactContext(EpistemicObservationLog(listOf(historicalRecord)))
        val exactCandidate = candidate(
            id = "candidate:imp",
            proposition = InformationProposition.RoleAt(4, RoleId("Imp")),
        )
        val normalContext = ExactConsequenceContext(
            validatedRuleset = validatedRuleset,
            exactContext = historical,
        )
        val full = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest("decision", listOf(exactCandidate)),
            context = normalContext,
        ) as ExactConsequenceEvaluation.Ready
        val unsupportedRuleset = validatedRuleset.copy(
            script = validatedRuleset.script.copy(
                source = ClocktowerScriptSource.IMPORTED_HOMEBREW,
            ),
        )

        val projected = HistoricalConfirmationChainFeatureProjector.project(
            fullEvaluation = full,
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(
                sdeCandidate(
                    candidateId = exactCandidate.candidateId,
                    observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
                    historyRefs = listOf(SdeHistoricalObservationRef(historicalRecord.recordId, 0)),
                ),
            ),
            context = ExactConsequenceContext(
                validatedRuleset = unsupportedRuleset,
                exactContext = historical,
            ),
        )

        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.MISSING_CAPABILITY),
            projected.getValue(exactCandidate.candidateId),
        )
    }

    @Test
    fun `rejects future historical observation before leave one out replay`() {
        val futureRecord = record(
            id = "future:claim",
            sequence = 3,
            globalSequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("future-channel"),
            proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
        )
        val historical = exactContext(EpistemicObservationLog(listOf(futureRecord)))
        val exactCandidate = candidate(
            id = "candidate:imp",
            proposition = InformationProposition.RoleAt(4, RoleId("Imp")),
        )
        val context = ExactConsequenceContext(
            validatedRuleset = validatedRuleset,
            exactContext = historical,
        )
        val full = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest("decision", listOf(exactCandidate)),
            context = context,
        ) as ExactConsequenceEvaluation.Ready

        try {
            HistoricalConfirmationChainFeatureProjector.project(
                fullEvaluation = full,
                exactCandidates = listOf(exactCandidate),
                sdeCandidates = listOf(
                    sdeCandidate(
                        candidateId = exactCandidate.candidateId,
                        observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
                        historyRefs = listOf(SdeHistoricalObservationRef(futureRecord.recordId, 0)),
                    ),
                ),
                context = context,
            )
            throw AssertionError("A future observation must not enter confirmation-chain projection.")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("committed prefix"))
        }
    }

    private fun exactContext(
        observationLog: EpistemicObservationLog,
    ) = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = ActionFactTimeline(),
        perceivedRolesBySeat = perceived,
        observationLog = observationLog,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun candidate(
        id: String,
        proposition: InformationProposition,
    ) = ExactConsequenceCandidate(
        candidateId = id,
        recipientSeat = 1,
        observations = listOf(
            EpistemicObservation(
                observationId = "observation:$id",
                snapshotId = formal.snapshotId,
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 2,
                sourceSeat = 1,
                sourceAbility = RoleId("current-channel"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = proposition,
            ),
        ),
    )

    private fun sdeCandidate(
        candidateId: String,
        observationIds: List<String>,
        historyRefs: List<SdeHistoricalObservationRef>,
    ) = SdeDecisionCandidate(
        decisionId = "decision",
        candidateId = candidateId,
        lifecycleStage = SdeDecisionLifecycleStage.Interaction(
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
        ),
        sourceInteraction = SdeDecisionSourceInteraction(
            interactionId = "decision",
            sourceSeat = 1,
            abilityRole = RoleId("current-channel"),
        ),
        sourceRevision = InformationDecisionRevision(
            gameStateRevision = snapshot.gameStateRevision,
            playerInputRevision = snapshot.playerInputRevision,
        ),
        inputBindings = SdeDecisionInputBindings.NotCaptured,
        historyPrefixRef = SdeHistoricalPrefixRef.Global(
            gameId = snapshot.gameId,
            actionRefs = emptyList(),
            observationRefs = historyRefs,
        ),
        legalOutcomeIdentity = candidateId,
        hypotheticalRef = SdeDecisionHypotheticalRef(
            observationRecordIds = observationIds,
        ),
        legalityProvenance = SdeDecisionLegalityProvenance(
            ownerId = "test-owner",
            candidateSpaceIdentity = "test-space",
        ),
    )

    private fun record(
        id: String,
        sequence: Int,
        globalSequence: Long,
        sourceSeat: Int?,
        sourceAbility: RoleId?,
        proposition: InformationProposition,
    ) = RecordedEpistemicObservation(
        recordId = id,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = sequence,
        sourceSeat = sourceSeat,
        sourceAbility = sourceAbility,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = sequence,
                globalSequence = globalSequence,
            ),
        ),
    )
}
