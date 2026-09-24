package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalHealthyInformationUtilityFeatureProjectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-3b4-healthy-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `whole table keeps another recipient healthy route when same recipient route becomes redundant`() {
        val chef = numericRecord(
            id = "chef-one",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            globalSequence = 0,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = snapshot.gameState.players.map { it.seat },
            value = 1,
        )
        val empath = numericRecord(
            id = "empath-zero",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            globalSequence = 1,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            value = 0,
        )
        val observationLog = EpistemicObservationLog(listOf(chef, empath))
        val exactContext = exactContext(ActionFactTimeline(), observationLog)
        val exactCandidate = currentCandidate(
            id = "current-empath-zero",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            value = 0,
        )
        val sde = sdeCandidate(
            candidate = exactCandidate,
            abilityState = AbilityState.FUNCTIONING,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
            timeline = ActionFactTimeline(),
            observationLog = observationLog,
        )
        val confirmation = ConfirmationChainFeatures(
            candidateRemovedExactWorldCount = BigInteger.ZERO,
            candidateChannel = ConfirmationChannelRef.Source(2, RoleId("Empath")),
            historicalObservationImpacts = listOf(
                impact(
                    record = empath,
                    relation = ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION,
                    independentlyConstrainingBefore = true,
                ),
            ),
        )

        val projected = HistoricalHealthyInformationUtilityFeatureProjector.project(
            fullEvaluation = readyConsequence(exactCandidate, before = 12, after = 12),
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(confirmation),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sde),
            truthRelationByCandidateId = mapOf(exactCandidate.candidateId to TruthRelation.TRUE_TO_ACTUAL_STATE),
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        val feature = (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        val chefRoute = HealthyInformationRouteRef.HistoricalObservation(chef.recordId, 1)
        val empathRoute = HealthyInformationRouteRef.HistoricalObservation(empath.recordId, 2)
        val currentRoute = HealthyInformationRouteRef.CurrentCandidate(exactCandidate.candidateId, 2)
        assertTrue(chefRoute in feature.usableHealthyRouteRefsAfter)
        assertTrue(chefRoute in feature.independentHealthyRouteRefsAfter)
        assertTrue(empathRoute in feature.usableHealthyRouteRefsAfter)
        assertFalse(empathRoute in feature.independentHealthyRouteRefsAfter)
        assertEquals(setOf(empathRoute), feature.newlyRedundantHealthyRouteRefs)
        assertEquals(currentRoute, feature.currentCandidateHealthyRouteRef)
        assertTrue(currentRoute in feature.usableHealthyRouteRefsAfter)
        assertFalse(currentRoute in feature.independentHealthyRouteRefsAfter)
    }

    @Test
    fun `functioning registered truth creates a healthy current route`() {
        val exactCandidate = currentCandidate(
            id = "registered-truth-current",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = snapshot.gameState.players.map { it.seat },
            value = 1,
        )
        val sde = sdeCandidate(
            candidate = exactCandidate,
            abilityState = AbilityState.FUNCTIONING,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            timeline = ActionFactTimeline(),
            observationLog = EpistemicObservationLog(),
        )

        val projected = HistoricalHealthyInformationUtilityFeatureProjector.project(
            fullEvaluation = readyConsequence(exactCandidate, before = 10, after = 5),
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(
                    ConfirmationChainFeatures(
                        candidateRemovedExactWorldCount = BigInteger.valueOf(5),
                        candidateChannel = ConfirmationChannelRef.Source(1, RoleId("Chef")),
                    ),
                ),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sde),
            truthRelationByCandidateId = mapOf(
                exactCandidate.candidateId to TruthRelation.TRUE_TO_REGISTERED_STATE,
            ),
            context = ExactConsequenceContext(
                validatedRuleset,
                exactContext(ActionFactTimeline(), EpistemicObservationLog()),
            ),
        )

        val feature =
            (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        val current = HealthyInformationRouteRef.CurrentCandidate(
            exactCandidate.candidateId,
            exactCandidate.recipientSeat,
        )
        assertEquals(current, feature.currentCandidateHealthyRouteRef)
        assertTrue(current in feature.usableHealthyRouteRefsAfter)
        assertTrue(current in feature.independentHealthyRouteRefsAfter)
    }

    @Test
    fun `R01 bounded shape keeps functioning route separate from temporary poison channel`() {
        val poison = TimelineBoundActionFact(
            fact = ActionFact.Poison("poison-empath", 0, 2),
            point = TimelinePoint(StorytellerPhase.FIRST_NIGHT, 1, 0, 0),
        )
        val poisonedEmpath = numericRecord(
            id = "poisoned-empath-zero",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            globalSequence = 1,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            value = 0,
        )
        val timeline = ActionFactTimeline(listOf(poison))
        val observationLog = EpistemicObservationLog(listOf(poisonedEmpath))
        val exactCandidate = currentCandidate(
            id = "current-chef-one",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = snapshot.gameState.players.map { it.seat },
            value = 1,
        )
        val sde = sdeCandidate(
            candidate = exactCandidate,
            abilityState = AbilityState.FUNCTIONING,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
            timeline = timeline,
            observationLog = observationLog,
        )

        val projected = HistoricalHealthyInformationUtilityFeatureProjector.project(
            fullEvaluation = readyConsequence(exactCandidate, before = 10, after = 5),
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(
                    ConfirmationChainFeatures(
                        candidateRemovedExactWorldCount = BigInteger.valueOf(5),
                        candidateChannel = ConfirmationChannelRef.Source(1, RoleId("Chef")),
                    ),
                ),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sde),
            truthRelationByCandidateId = mapOf(exactCandidate.candidateId to TruthRelation.TRUE_TO_ACTUAL_STATE),
            context = ExactConsequenceContext(
                validatedRuleset,
                exactContext(timeline, observationLog),
            ),
        )

        val feature = (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        assertFalse(
            HealthyInformationRouteRef.HistoricalObservation(poisonedEmpath.recordId, 2) in
                feature.usableHealthyRouteRefsAfter,
        )
        assertEquals(
            HealthyInformationRouteRef.CurrentCandidate(exactCandidate.candidateId, 1),
            feature.currentCandidateHealthyRouteRef,
        )
    }

    @Test
    fun `R04 bounded shape excludes persistent Drunk channel from healthy routes`() {
        // R04 verifies only the persistent Drunk + parallel healthy-channel shape. Its exact shown
        // role is still unknown, so this generic fixture must not be read as an R04 replay.
        val drunkSnapshot = snapshot.copy(
            gameState = snapshot.gameState.copy(
                players = snapshot.gameState.players.map { player ->
                    if (player.seat == 2) {
                        player.copy(
                            actualRole = RoleId("Drunk"),
                            actualType = CharacterType.OUTSIDER,
                            shownRole = RoleId("Empath"),
                        )
                    } else {
                        player
                    }
                },
            ),
        )
        val drunkRecord = numericRecord(
            id = "drunk-channel-zero",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            globalSequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            value = 0,
        )
        val observationLog = EpistemicObservationLog(listOf(drunkRecord))
        val exactCandidate = currentCandidate(
            id = "current-chef-one",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = drunkSnapshot.gameState.players.map { it.seat },
            value = 1,
            baseSnapshot = drunkSnapshot,
        )
        val sde = sdeCandidate(
            candidate = exactCandidate,
            abilityState = AbilityState.FUNCTIONING,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            timeline = ActionFactTimeline(),
            observationLog = observationLog,
            baseSnapshot = drunkSnapshot,
        )

        val projected = HistoricalHealthyInformationUtilityFeatureProjector.project(
            fullEvaluation = readyConsequence(exactCandidate, before = 10, after = 5),
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(
                    ConfirmationChainFeatures(
                        candidateRemovedExactWorldCount = BigInteger.valueOf(5),
                        candidateChannel = ConfirmationChannelRef.Source(1, RoleId("Chef")),
                    ),
                ),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sde),
            truthRelationByCandidateId = mapOf(exactCandidate.candidateId to TruthRelation.TRUE_TO_ACTUAL_STATE),
            context = ExactConsequenceContext(
                validatedRuleset,
                exactContext(
                    timeline = ActionFactTimeline(),
                    observationLog = observationLog,
                    baseSnapshot = drunkSnapshot,
                ),
            ),
        )

        val feature = (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        assertFalse(
            HealthyInformationRouteRef.HistoricalObservation(drunkRecord.recordId, 2) in
                feature.usableHealthyRouteRefsAfter,
        )
        assertEquals(
            HealthyInformationRouteRef.CurrentCandidate(exactCandidate.candidateId, 1),
            feature.currentCandidateHealthyRouteRef,
        )
    }

    @Test
    fun `unresolvable historical ability state is explicit unavailable rather than silently ignored`() {
        val unknown = numericRecord(
            id = "unknown-source",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            globalSequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("Not Empath"),
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = listOf(1, 3),
            value = 0,
        )
        val observationLog = EpistemicObservationLog(listOf(unknown))
        val exactCandidate = currentCandidate(
            id = "current-chef",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = snapshot.gameState.players.map { it.seat },
            value = 1,
        )
        val sde = sdeCandidate(
            candidate = exactCandidate,
            abilityState = AbilityState.FUNCTIONING,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            timeline = ActionFactTimeline(),
            observationLog = observationLog,
        )

        val projected = HistoricalHealthyInformationUtilityFeatureProjector.project(
            fullEvaluation = readyConsequence(exactCandidate, before = 10, after = 5),
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(
                    ConfirmationChainFeatures(
                        candidateRemovedExactWorldCount = BigInteger.valueOf(5),
                        candidateChannel = ConfirmationChannelRef.Source(1, RoleId("Chef")),
                    ),
                ),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sde),
            truthRelationByCandidateId = mapOf(exactCandidate.candidateId to TruthRelation.TRUE_TO_ACTUAL_STATE),
            context = ExactConsequenceContext(
                validatedRuleset,
                exactContext(ActionFactTimeline(), observationLog),
            ),
        )

        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED),
            projected.getValue(exactCandidate.candidateId),
        )
    }

    private fun numericRecord(
        id: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        globalSequence: Long,
        sourceSeat: Int,
        sourceAbility: RoleId,
        metric: NumericMetric,
        subjectSeats: List<Int>,
        value: Int,
    ) = RecordedEpistemicObservation(
        recordId = id,
        phase = phase,
        round = round,
        sequence = sequence,
        sourceSeat = sourceSeat,
        sourceAbility = sourceAbility,
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(sourceSeat),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = InformationProposition.NumericResult(
            metric = metric,
            sourceSeat = sourceSeat,
            subjectSeats = subjectSeats,
            value = value,
        ),
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(phase, round, sequence, globalSequence),
        ),
    )

    private fun currentCandidate(
        id: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        sourceSeat: Int,
        sourceAbility: RoleId,
        metric: NumericMetric,
        subjectSeats: List<Int>,
        value: Int,
        baseSnapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot = snapshot,
    ) = ExactConsequenceCandidate(
        candidateId = id,
        recipientSeat = sourceSeat,
        observations = listOf(
            EpistemicObservation(
                observationId = "observation:$id",
                snapshotId = FormalGameState.from(
                    baseSnapshot,
                    StorytellerPhase.FIRST_NIGHT,
                    1,
                ).snapshotId,
                phase = phase,
                round = round,
                sequence = sequence,
                sourceSeat = sourceSeat,
                sourceAbility = sourceAbility,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(sourceSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.NumericResult(
                    metric = metric,
                    sourceSeat = sourceSeat,
                    subjectSeats = subjectSeats,
                    value = value,
                ),
            ),
        ),
    )

    private fun sdeCandidate(
        candidate: ExactConsequenceCandidate,
        abilityState: AbilityState,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
        baseSnapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot = snapshot,
    ) = SdeDecisionCandidate(
        decisionId = "healthy-decision",
        candidateId = candidate.candidateId,
        lifecycleStage = SdeDecisionLifecycleStage.Interaction(phase, round, sequence),
        sourceInteraction = SdeDecisionSourceInteraction(
            interactionId = "healthy-decision",
            sourceSeat = candidate.recipientSeat,
            abilityRole = candidate.observations.single().sourceAbility,
            abilityState = abilityState,
        ),
        sourceRevision = InformationDecisionRevision(
            gameStateRevision = baseSnapshot.gameStateRevision,
            playerInputRevision = baseSnapshot.playerInputRevision,
        ),
        inputBindings = SdeDecisionInputBindings.Captured(),
        historyPrefixRef = SdeHistoricalPrefixRef.Global(
            gameId = baseSnapshot.gameId,
            actionRefs = timeline.entries.map {
                SdeHistoricalActionRef(it.fact.actionId, it.point.globalSequence)
            },
            observationRefs = observationLog.records.map {
                SdeHistoricalObservationRef(
                    it.recordId,
                    (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence,
                )
            },
        ),
        legalOutcomeIdentity = candidate.candidateId,
        hypotheticalRef = SdeDecisionHypotheticalRef(
            observationRecordIds = candidate.observations.map(EpistemicObservation::observationId),
        ),
        legalityProvenance = SdeDecisionLegalityProvenance("test-owner", "test-space"),
    )

    private fun exactContext(
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
        baseSnapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot = snapshot,
    ) = ExactHistoricalHypotheticalContext(
        initialSnapshot = baseSnapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = timeline,
        perceivedRolesBySeat = baseSnapshot.gameState.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        },
        observationLog = observationLog,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun impact(
        record: RecordedEpistemicObservation,
        relation: ConfirmationObservationRelation,
        independentlyConstrainingBefore: Boolean,
    ) = HistoricalObservationConfirmationImpact(
        provenance = ConfirmationObservationProvenance(
            observationRef = SdeHistoricalObservationRef(
                record.recordId,
                (record.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence,
            ),
            sourceSeat = record.sourceSeat,
            sourceAbility = record.sourceAbility,
            channel = ConfirmationChannelRef.Source(record.sourceSeat, record.sourceAbility),
        ),
        relation = relation,
        restoration = ConfirmationAmbiguityRestoration(
            BigInteger.ZERO,
            emptySet(),
            emptySet(),
        ),
        authenticatesDistinctSource = false,
        wasIndependentlyConstrainingBefore = independentlyConstrainingBefore,
    )

    private fun readyConsequence(
        candidate: ExactConsequenceCandidate,
        before: Long,
        after: Long,
    ) = ExactConsequenceEvaluation.Ready(
        consequences = listOf(
            CandidateConsequence(
                candidateId = candidate.candidateId,
                diagnostics = ExactHypotheticalObservationBundleDiagnostics(
                    bundleId = candidate.candidateId,
                    recipientSeat = candidate.recipientSeat,
                    before = WorldCardinality.Exact(BigInteger.valueOf(before)),
                    after = WorldCardinality.Exact(BigInteger.valueOf(after)),
                    beforeStructure = ExactWorldStructureDiagnostics.EMPTY,
                    afterStructure = ExactWorldStructureDiagnostics.EMPTY,
                ),
            ),
        ),
    )
}
