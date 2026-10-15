package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fixed Host behavior contracts; not model-strategy golden answers. */
class HostCurrentSingleDecisionJitV2Test {
    private fun session(): ClocktowerGameSession {
        val roles = listOf("Chef", "Empath", "Imp")
        val players = roles.mapIndexed { index, role ->
            PlayerState(
                seat = index + 1, name = "Seat ${index + 1}",
                actualRole = RoleId(role), shownRole = RoleId(role),
                actualAlignment = if (role == "Imp") Alignment.EVIL else Alignment.GOOD,
                actualType = if (role == "Imp") CharacterType.DEMON else CharacterType.TOWNSFOLK,
            )
        }
        return ClocktowerGameSession.createProduction(
            "jit-v2-game", 61L, GameState(ScriptId("trouble_brewing"), players, 61L),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    }

    private fun scalar(id: String, value: String) = StorytellerProviderCandidateV1(
        id, StorytellerProviderCandidatePayloadV1.ScalarResult(value))

    private fun scope(
        state: ClocktowerSessionState,
        candidates: List<StorytellerProviderCandidateV1>,
        memoryVersion: Int = 0,
    ) = HostCurrentSingleDecisionJitV2.Scope(
        state, StorytellerPhase.FIRST_NIGHT, 1,
        StorytellerStrategicContinuationV2.OpenDecision(
            "seat-1-information", 1, "NIGHT_INFORMATION", candidates, "Chef"),
        memoryVersion,
    )

    @Test fun onlyOneVisibleMeaningIsHostDeterminedNotModelOrWitness() {
        val state = session().state
        val same = scope(state, listOf(scalar("witness-a", "0"), scalar("witness-b", "0")))
        assertEquals(
            HostCurrentSingleDecisionJitV2.Route.HostDetermined("scalar:0"),
            HostCurrentSingleDecisionJitV2.route(same, null),
        )
        val pair = StorytellerProviderCandidatePayloadV1.PairInformation(
            "Imp", listOf(2, 3), SemanticTruth.TRUE, emptyList())
        val samePair = scope(state, listOf(
            StorytellerProviderCandidateV1("witness-1", pair),
            StorytellerProviderCandidateV1("witness-2", pair),
        ))
        assertEquals(
            HostCurrentSingleDecisionJitV2.Route.HostDetermined("pair:Imp:2,3"),
            HostCurrentSingleDecisionJitV2.route(samePair, null),
        )
    }

    @Test fun unchangedPrivateCacheSkipsModelButAnyUnprovedChangeInvalidatesIt() {
        val frozen = session().state
        val candidates = listOf(scalar("number-0", "0"), scalar("number-1", "1"))
        val before = scope(frozen, candidates)
        val cached = HostCurrentSingleDecisionJitV2.PrivateCache(before, "number-1")
        assertEquals(
            HostCurrentSingleDecisionJitV2.Route.CacheHit("number-1"),
            HostCurrentSingleDecisionJitV2.route(before, cached),
        )
        assertEquals(HostCurrentSingleDecisionJitV2.Route.ModelRequired,
            HostCurrentSingleDecisionJitV2.route(
                scope(frozen.copy(playerInputRevision = 1), candidates), cached))
        assertEquals(HostCurrentSingleDecisionJitV2.Route.ModelRequired,
            HostCurrentSingleDecisionJitV2.route(scope(frozen, candidates, 1), cached))
        assertEquals(HostCurrentSingleDecisionJitV2.Route.ModelRequired,
            HostCurrentSingleDecisionJitV2.route(
                scope(frozen, listOf(scalar("number-0", "0"), scalar("number-1", "2"))),
                cached))
    }

    @Test fun onlyExactCurrentSingleLegalCandidateCanPassHostGate() {
        val before = scope(session().state,
            listOf(scalar("number-0", "0"), scalar("number-1", "1")))
        val verified = HostCurrentSingleDecisionJitV2.verifyProposal(
            before, before, HostCurrentSingleDecisionJitV2.Proposal(before.source, "number-1"))
        assertEquals("number-1", verified)
        assertTrue(runCatching { HostCurrentSingleDecisionJitV2.verifyProposal(
            before, before, HostCurrentSingleDecisionJitV2.Proposal(before.source, "illegal"))
        }.isFailure)
        assertTrue(runCatching { HostCurrentSingleDecisionJitV2.verifyProposal(
            before, before, HostCurrentSingleDecisionJitV2.Proposal(
                before.source.copy(gameId = "other-game"), "number-1"))
        }.isFailure)
        assertTrue(runCatching { HostCurrentSingleDecisionJitV2.verifyProposal(
            before, scope(before.frozen.copy(gameStateRevision = 1), before.decision.legalCandidates),
            HostCurrentSingleDecisionJitV2.Proposal(before.source, "number-1"))
        }.isFailure)
    }

    @Test fun aPublishedObservationCannotBecomeAnOpenCurrentDecisionAgain() {
        val session = session()
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "received-chef-information",
            phase = StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 0,
            sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 0),
        ))
        assertTrue(runCatching {
            scope(session.state, listOf(scalar("number-0", "0")))
        }.isFailure)
    }
}
