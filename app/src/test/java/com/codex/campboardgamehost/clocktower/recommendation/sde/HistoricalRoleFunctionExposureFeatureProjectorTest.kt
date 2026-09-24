package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalRoleFunctionExposureFeatureProjectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val snapshot = A4RuntimeFixtures.snapshot()
    private val target = RoleFunctionExposureTargetRef(
        seat = 4,
        role = snapshot.gameState.playerAt(4)!!.actualRole,
        recipientSeat = 1,
        capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
    )

    @Test
    fun `visible canonical exposure becomes already exposed and distinct source support amplifies it`() {
        val history = record(
            id = "history:exposure",
            sourceSeat = 2,
            sourceAbility = RoleId("historical-channel"),
            proposition = pairClaim(target),
        )
        val context = context(EpistemicObservationLog(listOf(history)))
        val projected = HistoricalRoleFunctionExposureFeatureProjector.project(
            baseEvidence = listOf(baseEvidence()),
            confirmationByCandidateId = mapOf(
                "candidate" to confirmation(history, authenticatesDistinctSource = true),
            ),
            sdeCandidates = listOf(sdeCandidate(context, listOf(history))),
            context = context,
        )

        val feature = projected.getValue("candidate")
        assertTrue(feature is FeatureProjection.Projected)
        val exposure = (feature as FeatureProjection.Projected).value
        assertEquals(setOf(target), exposure.alreadyExposedTargets)
        assertTrue(exposure.newlyExposedTargets.isEmpty())
        assertEquals(setOf(target), exposure.confirmationAmplifiedTargets)
    }

    @Test
    fun `private exposure hidden from the candidate recipient does not enter exposure history`() {
        val history = record(
            id = "history:private-other-seat",
            sourceSeat = 2,
            sourceAbility = RoleId("historical-channel"),
            proposition = pairClaim(target),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(2),
        )
        val context = context(EpistemicObservationLog(listOf(history)))
        val projected = HistoricalRoleFunctionExposureFeatureProjector.project(
            baseEvidence = listOf(baseEvidence()),
            confirmationByCandidateId = mapOf(
                "candidate" to confirmation(history, authenticatesDistinctSource = true),
            ),
            sdeCandidates = listOf(sdeCandidate(context, listOf(history))),
            context = context,
        )

        val exposure =
            (projected.getValue("candidate") as FeatureProjection.Projected).value
        assertTrue(exposure.alreadyExposedTargets.isEmpty())
        assertEquals(setOf(target), exposure.newlyExposedTargets)
        assertTrue(exposure.confirmationAmplifiedTargets.isEmpty())
    }

    @Test
    fun `visible prior exposure remains separate from confirmation amplification`() {
        val history = record(
            id = "history:exposure-no-confirmation",
            sourceSeat = 2,
            sourceAbility = RoleId("historical-channel"),
            proposition = pairClaim(target),
        )
        val context = context(EpistemicObservationLog(listOf(history)))
        val projected = HistoricalRoleFunctionExposureFeatureProjector.project(
            baseEvidence = listOf(baseEvidence()),
            confirmationByCandidateId = mapOf(
                "candidate" to confirmation(history, authenticatesDistinctSource = false),
            ),
            sdeCandidates = listOf(sdeCandidate(context, listOf(history))),
            context = context,
        )

        val exposure =
            (projected.getValue("candidate") as FeatureProjection.Projected).value
        assertEquals(setOf(target), exposure.alreadyExposedTargets)
        assertTrue(exposure.newlyExposedTargets.isEmpty())
        assertTrue(exposure.confirmationAmplifiedTargets.isEmpty())
    }

    @Test
    fun `uncaptured canonical history keeps exposure feature explicitly unavailable`() {
        val context = context(EpistemicObservationLog())
        val candidate = sdeCandidate(context, emptyList()).copy(
            historyPrefixRef = SdeHistoricalPrefixRef.NotCaptured,
        )

        val projected = HistoricalRoleFunctionExposureFeatureProjector.project(
            baseEvidence = listOf(baseEvidence()),
            confirmationByCandidateId = mapOf(
                "candidate" to FeatureProjection.Projected(
                    ConfirmationChainFeatures(
                        candidateChannel = ConfirmationChannelRef.Source(
                            sourceSeat = 1,
                            sourceAbility = RoleId("current-channel"),
                        ),
                    ),
                ),
            ),
            sdeCandidates = listOf(candidate),
            context = context,
        )

        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED),
            projected.getValue("candidate"),
        )
    }

    private fun baseEvidence() = RoleFunctionExposureCandidateEvidence(
        candidateId = "candidate",
        directlyExposedTargets = setOf(target),
    )

    private fun pairClaim(
        target: RoleFunctionExposureTargetRef,
    ) = InformationProposition.AnyOf(
        listOf(
            InformationProposition.RoleAt(target.seat, target.role),
            InformationProposition.RoleAt(5, target.role),
        ),
    )

    private fun confirmation(
        history: RecordedEpistemicObservation,
        authenticatesDistinctSource: Boolean,
    ): FeatureProjection<ConfirmationChainFeatures> = FeatureProjection.Projected(
        ConfirmationChainFeatures(
            candidateChannel = ConfirmationChannelRef.Source(
                sourceSeat = 1,
                sourceAbility = RoleId("current-channel"),
            ),
            historicalObservationImpacts = listOf(
                HistoricalObservationConfirmationImpact(
                    provenance = ConfirmationObservationProvenance(
                        observationRef = SdeHistoricalObservationRef(
                            recordId = history.recordId,
                            globalSequence = 0,
                        ),
                        sourceSeat = history.sourceSeat,
                        sourceAbility = history.sourceAbility,
                        channel = ConfirmationChannelRef.Source(
                            sourceSeat = history.sourceSeat,
                            sourceAbility = history.sourceAbility,
                        ),
                    ),
                    relation = if (authenticatesDistinctSource) {
                        ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION
                    } else {
                        ConfirmationObservationRelation.NO_CONTRIBUTION
                    },
                    restoration = ConfirmationAmbiguityRestoration(
                        restoredExactWorldCount = BigInteger.ZERO,
                        restoredStrategicWorldKeys = emptySet(),
                        restoredDemonSeats = emptySet(),
                    ),
                    authenticatesDistinctSource = authenticatesDistinctSource,
                    wasIndependentlyConstrainingBefore = true,
                ),
            ),
        ),
    )

    private fun context(
        observationLog: EpistemicObservationLog,
    ) = ExactConsequenceContext(
        validatedRuleset = validatedRuleset,
        exactContext = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(),
            perceivedRolesBySeat = snapshot.gameState.players.associate { player ->
                player.seat to (player.shownRole ?: player.actualRole)
            },
            observationLog = observationLog,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        ),
        sourceRevision = InformationDecisionRevision(
            gameStateRevision = snapshot.gameStateRevision,
            playerInputRevision = snapshot.playerInputRevision,
        ),
    )

    private fun sdeCandidate(
        context: ExactConsequenceContext,
        history: List<RecordedEpistemicObservation>,
    ) = SdeDecisionCandidate(
        decisionId = "decision",
        candidateId = "candidate",
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
        sourceRevision = context.sourceRevision,
        inputBindings = SdeDecisionInputBindings.NotCaptured,
        historyPrefixRef = SdeHistoricalPrefixRef.Global(
            gameId = snapshot.gameId,
            actionRefs = emptyList(),
            observationRefs = history.map { record ->
                SdeHistoricalObservationRef(
                    recordId = record.recordId,
                    globalSequence =
                        (record.timelineBinding as ObservationTimelineBinding.Global)
                            .point.globalSequence,
                )
            },
        ),
        legalOutcomeIdentity = "candidate",
        hypotheticalRef = SdeDecisionHypotheticalRef(
            observationRecordIds = listOf("candidate:observation"),
        ),
        legalityProvenance = SdeDecisionLegalityProvenance(
            ownerId = "pair-information",
            candidateSpaceIdentity = "pair-information:test",
        ),
    )

    private fun record(
        id: String,
        sourceSeat: Int?,
        sourceAbility: RoleId?,
        proposition: InformationProposition,
        visibility: ObservationVisibility = ObservationVisibility.PUBLIC,
        recipientSeats: Set<Int> = emptySet(),
    ) = RecordedEpistemicObservation(
        recordId = id,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = sourceSeat,
        sourceAbility = sourceAbility,
        visibility = visibility,
        recipientSeats = recipientSeats,
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
                globalSequence = 0,
            ),
        ),
    )
}
