package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostCausalEventDeltaV2Test {
    private fun session(): ClocktowerGameSession {
        val roles = listOf("Chef", "Empath", "Imp")
        val players = roles.mapIndexed { index, role ->
            PlayerState(index + 1, "Seat ${index + 1}", RoleId(role),
                if (role == "Imp") Alignment.EVIL else Alignment.GOOD,
                if (role == "Imp") CharacterType.DEMON else CharacterType.TOWNSFOLK,
                shownRole = RoleId(role))
        }
        return ClocktowerGameSession.createProduction(
            "delta-v2-game", 27L, GameState(ScriptId("trouble_brewing"), players, 27L),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    }

    @Test fun unchangedAnchorsProduceNoDuplicateEntriesOrMissingFacts() {
        val game = session()
        val confirmed = game.state
        val unchanged = HostCausalEventDeltaV2.since(confirmed, confirmed)
        assertTrue(unchanged.noDetectedSnapshotFallback)
        assertFalse(unchanged.allGameEventsProvenComplete)
        assertTrue(unchanged.newlyLoggedFacts.isEmpty())
        assertEquals(0L, unchanged.fromExclusiveGlobalSequence)

        game.commitGlobalActionFact(ActionFactDraft.Poison(
            "confirmed-poison", StorytellerPhase.FIRST_NIGHT, 1, 0, 2))
        game.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-observation",
            phase = StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 1,
            sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 0),
        ))
        val suffix = HostCausalEventDeltaV2.since(confirmed, game.state)
        assertTrue(suffix.noDetectedSnapshotFallback)
        assertFalse(suffix.allGameEventsProvenComplete)
        assertEquals(listOf(0L, 1L),
            suffix.newlyLoggedFacts.map { it.point.globalSequence })
        assertEquals(2L, suffix.toExclusiveGlobalSequence)

        val lastConfirmed = game.state
        val next = HostCausalEventDeltaV2.since(lastConfirmed, game.state)
        assertTrue(next.newlyLoggedFacts.isEmpty())
        assertEquals(2L, next.fromExclusiveGlobalSequence)
    }

    @Test fun aClaimChangeWithNoSequencedEventCannotBePassedAsCompleteDelta() {
        val game = session()
        val lastConfirmed = game.state
        game.updateStorytellerPlayerContext(2, StorytellerPlayerContextInputV1(
            claimedRoleIds = listOf(RoleId("Mayor"))))
        val result = HostCausalEventDeltaV2.since(lastConfirmed, game.state)
        assertFalse(result.noDetectedSnapshotFallback)
        assertTrue(HostCausalEventDeltaV2.SnapshotReason.PLAYER_CONTEXT_CHANGED in
            result.snapshotReasons)
    }

    @Test fun unloggedMechanicalChangesRequireFullHostSnapshot() {
        val frozen = session().state
        val modified = frozen.copy(gameState = frozen.gameState.copy(
            players = frozen.gameState.players.map {
                if (it.seat == 1) it.copy(alive = false) else it
            }))
        val result = HostCausalEventDeltaV2.since(frozen, modified)
        assertFalse(result.noDetectedSnapshotFallback)
        assertTrue(HostCausalEventDeltaV2.SnapshotReason.MECHANICAL_STATE_CHANGED in
            result.snapshotReasons)
        assertTrue(result.newlyLoggedFacts.isEmpty())
    }

    @Test fun differentGamesOrRegressedRevisionsFailClosed() {
        val frozen = session().state
        assertTrue(runCatching {
            HostCausalEventDeltaV2.since(frozen,
                frozen.copy(gameId = "another-game"))
        }.isFailure)
        assertTrue(runCatching {
            HostCausalEventDeltaV2.since(frozen.copy(playerInputRevision = 2), frozen)
        }.isFailure)
    }
}
