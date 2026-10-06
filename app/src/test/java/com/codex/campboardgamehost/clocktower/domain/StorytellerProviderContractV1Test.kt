package com.codex.campboardgamehost.clocktower.domain

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerProviderContractV1Test {
    @Test
    fun `neutral request carries complete legal domain and validates only current candidate IDs`() {
        val request = request()
        val response = StorytellerProviderResponseV1(
            decisionId = request.identity.decisionId,
            sourceRevision = request.sourceRevision,
            outcome = StorytellerProviderOutcomeV1.Recommendation(
                primary = StorytellerProviderRecommendationV1(
                    candidateId = "drunk:1",
                    rationale = listOf("whole-game interaction"),
                ),
                alternatives = listOf(
                    StorytellerProviderRecommendationV1(candidateId = "drunk:2"),
                ),
            ),
            confidence = StorytellerProviderConfidenceV1.MEDIUM,
        )

        assertEquals(
            StorytellerProviderValidationV1.AcceptedRecommendation(
                primaryCandidateId = "drunk:1",
                alternativeCandidateIds = listOf("drunk:2"),
            ),
            StorytellerProviderResponseValidatorV1.validate(request, response),
        )

        val invented = response.copy(
            outcome = StorytellerProviderOutcomeV1.Recommendation(
                primary = StorytellerProviderRecommendationV1("invented"),
            ),
        )
        val rejected = StorytellerProviderResponseValidatorV1.validate(request, invented)
        assertTrue(rejected is StorytellerProviderValidationV1.Rejected)
        rejected as StorytellerProviderValidationV1.Rejected
        assertTrue(
            StorytellerProviderValidationFailureV1.UNKNOWN_PRIMARY_CANDIDATE in rejected.reasons,
        )
        assertEquals(listOf("drunk:1", "drunk:2"), request.legalCandidateIds)
    }

    @Test
    fun `neutral response can defer without manufacturing a choice`() {
        val request = request()
        val response = StorytellerProviderResponseV1(
            decisionId = request.identity.decisionId,
            sourceRevision = request.sourceRevision,
            outcome = StorytellerProviderOutcomeV1.Deferred(
                reasons = listOf("insufficient public narrative"),
                missingContext = listOf("claims"),
            ),
        )

        assertEquals(
            StorytellerProviderValidationV1.AcceptedDeferral(
                reasons = listOf("insufficient public narrative"),
                missingContext = listOf("claims"),
            ),
            StorytellerProviderResponseValidatorV1.validate(request, response),
        )
    }

    @Test
    fun `request rejects decision payload or player context outside canonical state`() {
        val valid = request()

        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(
                legalCandidates = listOf(
                    StorytellerProviderCandidateV1(
                        candidateId = "bad",
                        payload = StorytellerProviderCandidatePayloadV1.SeatTarget(1),
                    ),
                ),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(
                gameContext = valid.gameContext.copy(
                    players = valid.gameContext.players.dropLast(1),
                ),
            )
        }
    }

    @Test
    fun `neutral contract source does not depend on recommendation implementation`() {
        val source = File(
            "src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderContractV1.kt",
        ).readText(Charsets.UTF_8)

        assertTrue(!source.contains("clocktower.recommendation"))
        assertTrue(!source.contains("Sde"))
        assertTrue(!source.contains("PolicyVersion"))
        assertTrue(!source.contains("totalScore"))
    }

    private fun request(): StorytellerProviderRequestV1 {
        val snapshot = snapshot()
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                gameId = snapshot.gameId,
                scriptId = snapshot.script.value,
                decisionTypeId = StorytellerProviderDecisionContextV1.DRUNK_ASSIGNMENT,
                decisionId = "setup:drunk-assignment",
            ),
            sourceRevision = StorytellerProviderRevisionV1(
                gameStateRevision = 4,
                playerInputRevision = 2,
            ),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = StorytellerProviderDecisionContextV1.DrunkAssignment,
            legalCandidates = listOf(
                StorytellerProviderCandidateV1(
                    candidateId = "drunk:1",
                    payload = StorytellerProviderCandidatePayloadV1.DrunkAssignment(
                        seat = 1,
                        shownRoleId = "Empath",
                    ),
                ),
                StorytellerProviderCandidateV1(
                    candidateId = "drunk:2",
                    payload = StorytellerProviderCandidatePayloadV1.DrunkAssignment(
                        seat = 2,
                        shownRoleId = "Monk",
                    ),
                ),
            ),
            gameContext = StorytellerProviderGameContextV1(
                players = listOf(
                    StorytellerProviderPlayerContextV1(
                        seat = 1,
                        experienceLevel = PlayerExperienceLevelV1.NORMAL,
                        claimedRoleIds = emptyList(),
                        pressureLevel = null,
                    ),
                    StorytellerProviderPlayerContextV1(
                        seat = 2,
                        experienceLevel = PlayerExperienceLevelV1.BEGINNER,
                        claimedRoleIds = listOf(RoleId("Saint")),
                        pressureLevel = StorytellerDeclaredPressureLevelV1.HIGH,
                    ),
                ),
                priorDecisions = emptyList(),
            ),
        )
    }

    private fun snapshot() = TroubleBrewingGameSnapshotV1(
        gameId = "neutral-provider-test",
        gameSeed = 7L,
        position = TroubleBrewingSnapshotPosition(
            stage = TroubleBrewingSnapshotStage.SETUP_PRECOMMIT,
            phase = SnapshotField.NotApplicable,
            round = SnapshotField.NotApplicable,
            gameStateRevision = SnapshotField.Known(4),
            playerInputRevision = SnapshotField.Known(2),
        ),
        grimoireSeats = listOf(
            TroubleBrewingSnapshotSeat(
                seat = 1,
                shownRoleId = SnapshotField.Known("Empath"),
                actualRoleId = SnapshotField.Uncommitted,
                alive = SnapshotField.Known(true),
                poisoned = SnapshotField.NotApplicable,
            ),
            TroubleBrewingSnapshotSeat(
                seat = 2,
                shownRoleId = SnapshotField.Known("Monk"),
                actualRoleId = SnapshotField.Uncommitted,
                alive = SnapshotField.Known(true),
                poisoned = SnapshotField.NotApplicable,
            ),
        ),
        setupState = TroubleBrewingSnapshotSetupState(
            hasDrunk = SnapshotField.Known(true),
            drunkAssignmentSeat = SnapshotField.Uncommitted,
        ),
    )
}
