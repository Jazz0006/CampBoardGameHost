package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedHistoricalIncrementalReplayTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val ruleset = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-h6-incremental-replay-test",
        sourceRevision = "official",
    )
    private val script = ruleset.scriptId
    private val roles = listOf(
        role("Empath", CharacterType.TOWNSFOLK),
        role("Chef", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Poisoner", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )
    private val setupKnowledge = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "knowledge-a3-h6-incremental-replay",
        formalSnapshotId = "snapshot-a3-h6-incremental-replay",
        recipientSeat = 1,
        perceivedRole = RoleId("Empath"),
        setupKnowledge = listOf(InformationProposition.SetupProfile(3, 0, 1, 1)),
    )

    @Test
    fun `H6 ordinary night observation is evaluated against state at its global point`() {
        val observationBeforeDeath = build(observationGlobalSequence = 8L)
        val observationAfterDeath = build(observationGlobalSequence = 10L)

        assertFalse(
            "The same ordinary Empath observation is legal while the source is still alive.",
            observationBeforeDeath.worldSet.isEmpty(),
        )
        assertTrue(
            "After the Empath dies at global sequence 9, an ordinary Empath observation at sequence 10 " +
                "must be rejected from the current historical state rather than accepted from the initial-night snapshot.",
            observationAfterDeath.worldSet.isEmpty(),
        )
    }

    private fun build(observationGlobalSequence: Long): EnumeratedHistoricalReplayResult {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.PhaseAdvance("day-1", 1L, StorytellerPhase.DAY, 1),
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    localSequence = 1,
                    globalSequence = 1L,
                ),
                action(
                    ActionFact.PhaseAdvance("night-2", 2L, StorytellerPhase.NIGHT, 2),
                    phase = StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 2L,
                ),
                action(
                    ActionFact.Death("empath-dies", 9L, 1),
                    phase = StorytellerPhase.NIGHT,
                    round = 2,
                    localSequence = 3,
                    globalSequence = 9L,
                ),
            ),
        )
        val observationPoint = TimelinePoint(
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = observationGlobalSequence.toInt(),
            globalSequence = observationGlobalSequence,
        )
        val observation = RecordedEpistemicObservation(
            recordId = "h6-empath-$observationGlobalSequence",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = observationGlobalSequence.toInt(),
            sourceSeat = 1,
            sourceAbility = RoleId("Empath"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.AliveAt(3, true),
            timelineBinding = ObservationTimelineBinding.Global(observationPoint),
        )

        return EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = ruleset,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(listOf(observation)),
        )
    }

    private fun action(
        fact: ActionFact,
        phase: StorytellerPhase,
        round: Int,
        localSequence: Int,
        globalSequence: Long,
    ) = TimelineBoundActionFact(
        fact = fact,
        point = TimelinePoint(
            phase = phase,
            round = round,
            sequence = localSequence,
            globalSequence = globalSequence,
        ),
    )

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
        type = type,
        scriptIds = setOf(script),
    )
}
