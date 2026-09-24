package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
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
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.math.BigInteger
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalImpairedNarrativeFeatureProjectorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-3b3-impaired-test",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `persistent setup bound impairment carries its source narrative across nights`() {
        // Evidence Lab R04 establishes this lifecycle shape: one Drunk information stream persists
        // across nights. R04's shown role is still unknown, so Empath here is generic test data,
        // not a claim about R04's missing grimoire field.
        val snapshot = A4RuntimeFixtures.snapshot().copy(
            rulesetRef = rulesetRef,
            gameState = A4RuntimeFixtures.snapshot().gameState.copy(
                players = A4RuntimeFixtures.snapshot().gameState.players.map { player ->
                    if (player.seat == 2) {
                        player.copy(
                            actualRole = RoleId("Drunk"),
                            actualAlignment = Alignment.GOOD,
                            actualType = CharacterType.OUTSIDER,
                            shownRole = RoleId("Empath"),
                        )
                    } else {
                        player
                    }
                },
            ),
        )
        val first = record(
            id = "drunk-night-one",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
            globalSequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
        )
        val second = record(
            id = "drunk-night-two",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 2,
            globalSequence = 1,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
        )
        val observationLog = EpistemicObservationLog(listOf(first, second))
        val exactContext = exactContext(snapshot, ActionFactTimeline(), observationLog)
        val exactCandidate = candidate(snapshot, "current", StorytellerPhase.NIGHT, 3, 2, 2, RoleId("Empath"))
        val sdeCandidate = sdeCandidate(
            snapshot = snapshot,
            candidateId = exactCandidate.candidateId,
            observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
            abilityState = AbilityState.MALFUNCTIONING_DRUNK,
            phase = StorytellerPhase.NIGHT,
            round = 3,
            sequence = 2,
            actionRefs = emptyList(),
            observationRefs = listOf(first, second),
        )

        val projected = HistoricalImpairedNarrativeFeatureProjector.project(
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(
                    confirmation(first, second),
                ),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sdeCandidate),
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        val feature = (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        assertEquals(ImpairmentLifetime.PERSISTENT_SETUP_BOUND, feature.impairmentLifetime)
        assertEquals(setOf(first.recordId, second.recordId), feature.priorImpairedObservationIds)
        assertEquals(
            ImpairedNarrativeRelation.COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE,
            feature.relation,
        )
    }

    @Test
    fun `temporary action bound impairment excludes observations from an earlier poison episode`() {
        val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val firstPoison = poison("poison-one", 0, StorytellerPhase.NIGHT, 2, 0, 2)
        val oldObservation = record(
            id = "old-poison-episode",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            globalSequence = 1,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
        )
        val clear = poison("poison-clear", 2, StorytellerPhase.DAY, 3, 0, null)
        val secondPoison = poison("poison-two", 3, StorytellerPhase.NIGHT, 3, 0, 2)
        val currentObservation = record(
            id = "current-poison-episode",
            phase = StorytellerPhase.NIGHT,
            round = 3,
            sequence = 1,
            globalSequence = 4,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
        )
        val timeline = ActionFactTimeline(listOf(firstPoison, clear, secondPoison))
        val observationLog = EpistemicObservationLog(listOf(oldObservation, currentObservation))
        val exactContext = exactContext(snapshot, timeline, observationLog)
        val exactCandidate = candidate(snapshot, "current", StorytellerPhase.NIGHT, 3, 2, 2, RoleId("Empath"))
        val sdeCandidate = sdeCandidate(
            snapshot = snapshot,
            candidateId = exactCandidate.candidateId,
            observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            phase = StorytellerPhase.NIGHT,
            round = 3,
            sequence = 2,
            actionRefs = timeline.entries,
            observationRefs = listOf(oldObservation, currentObservation),
        )

        val projected = HistoricalImpairedNarrativeFeatureProjector.project(
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(
                    confirmation(oldObservation, currentObservation),
                ),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sdeCandidate),
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        val feature = (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        assertEquals(ImpairmentLifetime.TEMPORARY_ACTION_BOUND, feature.impairmentLifetime)
        assertEquals(setOf(currentObservation.recordId), feature.priorImpairedObservationIds)
        assertTrue(oldObservation.recordId !in feature.priorImpairedObservationIds)
    }

    @Test
    fun `R06 derived poison death trigger remains a temporary active impairment episode`() {
        // Evidence Lab R06 directly observes Poisoner -> Ravenkeeper death -> poisoned false
        // Ravenkeeper information. This is a bounded lifecycle regression, not a full game replay.
        val base = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val snapshot = base.copy(
            gameState = base.gameState.copy(
                players = base.gameState.players.map { player ->
                    if (player.seat == 2) {
                        player.copy(
                            actualRole = RoleId("Ravenkeeper"),
                            shownRole = RoleId("Ravenkeeper"),
                        )
                    } else {
                        player
                    }
                },
            ),
        )
        val poison = poison("r06-poison", 0, StorytellerPhase.NIGHT, 4, 0, 2)
        val death = TimelineBoundActionFact(
            ActionFact.Death("r06-death", 1, 2),
            TimelinePoint(StorytellerPhase.NIGHT, 4, 1, 1),
        )
        val timeline = ActionFactTimeline(listOf(poison, death))
        val exactContext = exactContext(snapshot, timeline, EpistemicObservationLog())
        val exactCandidate = candidate(
            snapshot = snapshot,
            id = "r06-ravenkeeper-result",
            phase = StorytellerPhase.NIGHT,
            round = 4,
            sequence = 2,
            sourceSeat = 2,
            sourceAbility = RoleId("Ravenkeeper"),
        )
        val sdeCandidate = sdeCandidate(
            snapshot = snapshot,
            candidateId = exactCandidate.candidateId,
            observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            phase = StorytellerPhase.NIGHT,
            round = 4,
            sequence = 2,
            actionRefs = timeline.entries,
            observationRefs = emptyList(),
            sourceAbility = RoleId("Ravenkeeper"),
        )

        val projected = HistoricalImpairedNarrativeFeatureProjector.project(
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(confirmation()),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sdeCandidate),
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        val feature =
            (projected.getValue(exactCandidate.candidateId) as FeatureProjection.Projected).value
        assertEquals(ImpairmentLifetime.TEMPORARY_ACTION_BOUND, feature.impairmentLifetime)
        assertEquals(ImpairedNarrativeRelation.NO_PRIOR_IMPAIRED_NARRATIVE, feature.relation)
        assertTrue(feature.priorImpairedObservationIds.isEmpty())
    }

    @Test
    fun `temporary impairment without a canonical active episode is explicitly unavailable`() {
        val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val exactContext = exactContext(snapshot, ActionFactTimeline(), EpistemicObservationLog())
        val exactCandidate = candidate(snapshot, "current", StorytellerPhase.NIGHT, 2, 2, 2, RoleId("Empath"))
        val sdeCandidate = sdeCandidate(
            snapshot = snapshot,
            candidateId = exactCandidate.candidateId,
            observationIds = exactCandidate.observations.map(EpistemicObservation::observationId),
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 2,
            actionRefs = emptyList(),
            observationRefs = emptyList(),
        )

        val projected = HistoricalImpairedNarrativeFeatureProjector.project(
            confirmationByCandidateId = mapOf(
                exactCandidate.candidateId to FeatureProjection.Projected(confirmation()),
            ),
            exactCandidates = listOf(exactCandidate),
            sdeCandidates = listOf(sdeCandidate),
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.HISTORICAL_INPUT_NOT_CAPTURED),
            projected.getValue(exactCandidate.candidateId),
        )
    }

    private fun exactContext(
        snapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot,
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ) = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = timeline,
        perceivedRolesBySeat = snapshot.gameState.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        },
        observationLog = observationLog,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun candidate(
        snapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot,
        id: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        sourceSeat: Int,
        sourceAbility: RoleId,
    ) = ExactConsequenceCandidate(
        candidateId = id,
        recipientSeat = sourceSeat,
        observations = listOf(
            EpistemicObservation(
                observationId = "observation:$id",
                snapshotId = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1).snapshotId,
                phase = phase,
                round = round,
                sequence = sequence,
                sourceSeat = sourceSeat,
                sourceAbility = sourceAbility,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(sourceSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
            ),
        ),
    )

    private fun sdeCandidate(
        snapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot,
        candidateId: String,
        observationIds: List<String>,
        abilityState: AbilityState,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        actionRefs: List<TimelineBoundActionFact>,
        observationRefs: List<RecordedEpistemicObservation>,
        sourceAbility: RoleId = RoleId("Empath"),
    ) = SdeDecisionCandidate(
        decisionId = "decision",
        candidateId = candidateId,
        lifecycleStage = SdeDecisionLifecycleStage.Interaction(phase, round, sequence),
        sourceInteraction = SdeDecisionSourceInteraction(
            interactionId = "decision",
            sourceSeat = 2,
            abilityRole = sourceAbility,
            abilityState = abilityState,
        ),
        sourceRevision = InformationDecisionRevision(
            gameStateRevision = snapshot.gameStateRevision,
            playerInputRevision = snapshot.playerInputRevision,
        ),
        inputBindings = SdeDecisionInputBindings.Captured(),
        historyPrefixRef = SdeHistoricalPrefixRef.Global(
            gameId = snapshot.gameId,
            actionRefs = actionRefs.map {
                SdeHistoricalActionRef(it.fact.actionId, it.point.globalSequence)
            },
            observationRefs = observationRefs.map {
                SdeHistoricalObservationRef(
                    it.recordId,
                    (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence,
                )
            },
        ),
        legalOutcomeIdentity = candidateId,
        hypotheticalRef = SdeDecisionHypotheticalRef(observationRecordIds = observationIds),
        legalityProvenance = SdeDecisionLegalityProvenance("test-owner", "test-space"),
    )

    private fun record(
        id: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        globalSequence: Long,
        sourceSeat: Int,
        sourceAbility: RoleId,
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
        proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(phase, round, sequence, globalSequence),
        ),
    )

    private fun poison(
        id: String,
        globalSequence: Long,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        targetSeat: Int?,
    ) = TimelineBoundActionFact(
        ActionFact.Poison(id, globalSequence, targetSeat),
        TimelinePoint(phase, round, sequence, globalSequence),
    )

    private fun confirmation(
        vararg records: RecordedEpistemicObservation,
    ) = ConfirmationChainFeatures(
        candidateChannel = ConfirmationChannelRef.Interaction("decision"),
        historicalObservationImpacts = records.map { record ->
            HistoricalObservationConfirmationImpact(
                provenance = ConfirmationObservationProvenance(
                    observationRef = SdeHistoricalObservationRef(
                        record.recordId,
                        (record.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence,
                    ),
                    sourceSeat = record.sourceSeat,
                    sourceAbility = record.sourceAbility,
                    channel = ConfirmationChannelRef.Observation(record.recordId),
                ),
                relation = ConfirmationObservationRelation.NO_CONTRIBUTION,
                restoration = ConfirmationAmbiguityRestoration(
                    BigInteger.ZERO,
                    emptySet(),
                    emptySet(),
                ),
                authenticatesDistinctSource = false,
            )
        },
    )
}
