package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerFirstNightPlayerRevealHandoffTest {
    @Test fun `manual pair already committed at lifecycle boundary still opens reveal without duplicate publication`() {
        val handoff = resolveClocktowerPlayerRevealHandoff(
            publicationAllowed = true,
            firstNightPublicationCreated = false,
        )

        assertTrue(handoff.openReveal)
        assertFalse(handoff.recordPublication)
    }

    @Test fun `fresh first night publication records once and opens reveal`() {
        val handoff = resolveClocktowerPlayerRevealHandoff(
            publicationAllowed = true,
            firstNightPublicationCreated = true,
        )

        assertTrue(handoff.openReveal)
        assertTrue(handoff.recordPublication)
    }

    @Test fun `publication guard denial blocks both publication and reveal`() {
        val handoff = resolveClocktowerPlayerRevealHandoff(
            publicationAllowed = false,
            firstNightPublicationCreated = true,
        )

        assertFalse(handoff.openReveal)
        assertFalse(handoff.recordPublication)
    }
    @Test fun `fresh publication executes private observation then history then reveal`() {
        val events = mutableListOf<String>()
        val result = performClocktowerPlayerRevealHandoff(
            authorize = { events += "authorize"; true },
            publishFirstNight = { events += "publish"; true },
            recordPrivateInformation = { events += "private" },
            recordHistory = { events += "history" },
            openReveal = { events += "reveal" },
        )
        assertEquals(listOf("authorize", "publish", "private", "history", "reveal"), events)
        assertEquals(ClocktowerPlayerRevealHandoff(true, true), result)
    }

    @Test fun `reopening a published decision does not repeat observation or history`() {
        val events = mutableListOf<String>()
        var published = false
        repeat(2) {
            performClocktowerPlayerRevealHandoff(
                authorize = { true },
                publishFirstNight = { (!published).also { published = true } },
                recordPrivateInformation = { events += "private" },
                recordHistory = { events += "history" },
                openReveal = { events += "reveal" },
            )
        }
        assertEquals(listOf("private", "history", "reveal", "reveal"), events)
    }

    @Test fun `authorization denial does not invoke any downstream effect`() {
        val result = performClocktowerPlayerRevealHandoff(
            authorize = { false },
            publishFirstNight = { error("must not publish") },
            recordPrivateInformation = { error("must not record private") },
            recordHistory = { error("must not record history") },
            openReveal = { error("must not reveal") },
        )
        assertEquals(ClocktowerPlayerRevealHandoff(false, false), result)
    }

    @Test fun `effect failures propagate without later effects or invented rollback`() {
        val stages = listOf("authorize", "publish", "private", "history", "reveal")
        for (failedStage in stages) {
            val events = mutableListOf<String>()
            val failure = IllegalStateException(failedStage)
            fun effect(stage: String) {
                events += stage
                if (stage == failedStage) throw failure
            }
            try {
                performClocktowerPlayerRevealHandoff(
                    authorize = { effect("authorize"); true },
                    publishFirstNight = { effect("publish"); true },
                    recordPrivateInformation = { effect("private") },
                    recordHistory = { effect("history") },
                    openReveal = { effect("reveal") },
                )
                error("Expected effect failure")
            } catch (actual: IllegalStateException) {
                assertSame(failure, actual)
            }
            assertEquals(stages.take(stages.indexOf(failedStage) + 1), events)
        }
    }

    @Test fun `legacy publication remains allowed without structured confirmation`() {
        assertTrue(clocktowerInformationPublicationAllowed(null, null, InformationDecisionRevision(1, 2)))
    }

    @Test fun `confirmed publication requires exact snapshot and current revisions before effects`() {
        val revision = InformationDecisionRevision(5, 6)
        val model = prepareEmpathNumberInformationUiModel(
            coordinator = ClocktowerRecommendationCoordinator(), gameId = "publication-test",
            phase = ClocktowerPhase.Night, round = 2, sequence = 4, actorSeat = 2,
            subjectSeats = listOf(1, 3), trueValue = 1,
            reliability = InformationReliability.RELIABLE,
            recommendationStyle = RecommendationStyle.BALANCED, revision = revision, recommendedValue = 1,
        )
        val confirmed = requireNotNull(model.acceptRecommendation(model.choices.single().candidateId, revision).confirmed)
        assertTrue(clocktowerInformationPublicationAllowed(confirmed, model.contextSnapshot, revision))
        val rejected = listOf(
            null to revision,
            model.contextSnapshot.copy(semanticIdentity = "other-decision") to revision,
            model.contextSnapshot to revision.copy(gameStateRevision = 7),
            model.contextSnapshot to revision.copy(playerInputRevision = 7),
        )
        for ((snapshot, currentRevision) in rejected) {
            val result = performClocktowerPlayerRevealHandoff(
                authorize = { clocktowerInformationPublicationAllowed(confirmed, snapshot, currentRevision) },
                publishFirstNight = { error("stale publication") },
                recordPrivateInformation = { error("stale observation") },
                recordHistory = { error("stale history") },
                openReveal = { error("stale reveal") },
            )
            assertFalse(result.openReveal)
        }
    }
}
