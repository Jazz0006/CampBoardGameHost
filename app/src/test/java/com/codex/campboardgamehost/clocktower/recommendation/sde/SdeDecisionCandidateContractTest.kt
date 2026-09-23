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
            committedInputRefs = setOf(CommittedDecisionInputRef("setup:red-herring:seat-4")),
            playerControlledInputRefs = setOf(PlayerControlledDecisionInputRef("ft-targets:2,7")),
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

    @Test(expected = IllegalArgumentException::class)
    fun `same input cannot be owned as both committed and player controlled`() {
        SdeDecisionInputBindings.Captured(
            committedInputRefs = setOf(CommittedDecisionInputRef("same-input")),
            playerControlledInputRefs = setOf(PlayerControlledDecisionInputRef("same-input")),
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
