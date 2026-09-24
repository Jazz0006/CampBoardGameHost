package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SdeDecisionCandidateContractTest {
    private val revision = InformationDecisionRevision(gameStateRevision = 4, playerInputRevision = 9)

    @Test
    fun `candidate keeps player-controlled inputs separate from committed storyteller inputs`() {
        val bindings = SdeDecisionInputBindings.Captured(
            committedInputRefs = setOf(
                CommittedDecisionInputRef(
                    inputId = "setup:red-herring:seat-4",
                    ownerId = "clocktower-setup-commitments",
                    kind = SdeCommittedDecisionInputKind.RED_HERRING,
                ),
            ),
            playerControlledInputRefs = setOf(
                PlayerControlledDecisionInputRef(
                    inputId = "ft-targets:2,7",
                    ownerId = "fortune-teller-target-selection",
                    kind = SdePlayerControlledDecisionInputKind.TARGET_SELECTION,
                ),
            ),
        )

        val candidate = candidate(bindings)

        assertEquals(setOf("setup:red-herring:seat-4"), bindings.committedInputRefs.map { it.inputId }.toSet())
        assertEquals(setOf("ft-targets:2,7"), bindings.playerControlledInputRefs.map { it.inputId }.toSet())
        assertEquals(revision, candidate.sourceRevision)
        assertEquals("candidate-yes", candidate.legalOutcomeIdentity)
    }

    @Test
    fun `not captured input state is explicit rather than silently treated as empty`() {
        val candidate = candidate(SdeDecisionInputBindings.NotCaptured)

        assertSame(SdeDecisionInputBindings.NotCaptured, candidate.inputBindings)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ability state cannot exist without a source ability role`() {
        SdeDecisionSourceInteraction(
            interactionId = "night:information:seat-3",
            sourceSeat = 3,
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
        )
    }

    @Test
    fun `global history prefix keeps canonical action and observation identities without copying state`() {
        val prefix = SdeHistoricalPrefixRef.Global(
            gameId = "game-1",
            actionRefs = listOf(
                SdeHistoricalActionRef("poison-night-2", 11L),
                SdeHistoricalActionRef("death-night-2", 13L),
            ),
            observationRefs = listOf(
                SdeHistoricalObservationRef("empath-night-1", 4L),
                SdeHistoricalObservationRef("undertaker-night-2", 12L),
            ),
        )

        assertEquals(listOf(11L, 13L), prefix.actionRefs.map(SdeHistoricalActionRef::globalSequence))
        assertEquals(listOf(4L, 12L), prefix.observationRefs.map(SdeHistoricalObservationRef::globalSequence))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `history prefix rejects cross-type global-sequence collisions`() {
        SdeHistoricalPrefixRef.Global(
            gameId = "game-1",
            actionRefs = listOf(SdeHistoricalActionRef("poison", 7L)),
            observationRefs = listOf(SdeHistoricalObservationRef("shown-info", 7L)),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `same input cannot be owned as both committed and player controlled`() {
        SdeDecisionInputBindings.Captured(
            committedInputRefs = setOf(
                CommittedDecisionInputRef(
                    inputId = "same-input",
                    ownerId = "setup-owner",
                    kind = SdeCommittedDecisionInputKind.OTHER,
                ),
            ),
            playerControlledInputRefs = setOf(
                PlayerControlledDecisionInputRef(
                    inputId = "same-input",
                    ownerId = "player-owner",
                    kind = SdePlayerControlledDecisionInputKind.OTHER,
                ),
            ),
        )
    }

    private fun candidate(inputBindings: SdeDecisionInputBindings) = SdeDecisionCandidate(
        decisionId = "first-night:fortune-teller:seat-3",
        candidateId = "candidate-yes",
        lifecycleStage = SdeDecisionLifecycleStage.Interaction(
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 8,
        ),
        sourceInteraction = SdeDecisionSourceInteraction(
            interactionId = "first-night:fortune-teller:seat-3",
            sourceSeat = 3,
            abilityRole = RoleId("Fortune Teller"),
        ),
        sourceRevision = revision,
        inputBindings = inputBindings,
        historyPrefixRef = SdeHistoricalPrefixRef.Global(
            gameId = "game-1",
            actionRefs = emptyList(),
            observationRefs = emptyList(),
        ),
        legalOutcomeIdentity = "candidate-yes",
        hypotheticalRef = SdeDecisionHypotheticalRef(
            observationRecordIds = listOf("ft-result-candidate-yes"),
        ),
        legalityProvenance = SdeDecisionLegalityProvenance(
            ownerId = "information-decision-context-v1",
            candidateSpaceIdentity = "first-night:fortune-teller:seat-3",
            candidateSchemaVersion = "structured-v1",
        ),
    )
}
