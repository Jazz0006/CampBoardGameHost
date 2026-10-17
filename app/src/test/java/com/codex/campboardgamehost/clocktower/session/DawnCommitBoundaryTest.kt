package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

/** Real Session transitions replace the retired materialization-plan retry simulation. */
class DawnCommitBoundaryTest {
    private fun session(): ClocktowerGameSession {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val s = ClocktowerGameSession.createProduction("dawn-boundary", base.seed, base,
            ClocktowerSemanticHistoryMode.GLOBAL_V1)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT,
            1, s.nextInputSequence, StorytellerPhase.DAY, 1), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night", StorytellerPhase.DAY,
            1, s.nextInputSequence, StorytellerPhase.NIGHT, 2), s.state.gameStateRevision)
        return s
    }

    private fun death(s: ClocktowerGameSession): ActionFactDraft.Death {
        val id = DawnCommitIdentity.death(s.state.gameId, 2, 8)
        val prior = s.state.actionTimeline.entries.firstOrNull { it.fact.actionId == id }
        return ActionFactDraft.Death(id, StorytellerPhase.NIGHT, 2,
            prior?.point?.sequence ?: s.nextInputSequence, 8)
    }

    @Test fun `death publication succession and dawn are ordered and retries add nothing`() {
        val s = session()
        val observationId = DawnCommitIdentity.deathObservation(s.state.gameId, 2, 8)
        val committedDeath = s.commitConfirmedPlayerDeath(death(s), s.state.gameStateRevision, observationId)
        val roleId = DawnCommitIdentity.roleChange(s.state.gameId, 2, 7, RoleId("Imp"))
        val role = ActionFactDraft.RoleChange(roleId, StorytellerPhase.NIGHT, 2, s.nextInputSequence,
            7, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON)
        val committedRole = s.commitConfirmedActualRoleChange(role, s.state.gameStateRevision)
        val beforeRetry = s.state
        // The callback is reconstructed after other accepted actions moved the input cursor.
        assertEquals(committedDeath, s.commitConfirmedPlayerDeath(death(s), s.state.gameStateRevision, observationId))
        assertEquals(committedRole, s.commitConfirmedActualRoleChange(role, s.state.gameStateRevision))
        assertEquals(beforeRetry, s.state)
        val phase = ActionFactDraft.PhaseAdvance("dawn", StorytellerPhase.NIGHT, 2,
            s.nextInputSequence, StorytellerPhase.DAWN, 2)
        val dawn = s.commitConfirmedPhaseAdvance(phase, s.state.gameStateRevision)
        val afterDawn = s.state
        s.commitConfirmedPhaseAdvance(phase, s.state.gameStateRevision)
        assertEquals(afterDawn, s.state)
        val publication = s.state.epistemicObservationLog.records.single()
        val point = (publication.timelineBinding as ObservationTimelineBinding.Global).point
        assertEquals(committedDeath.point.globalSequence + 1, point.globalSequence)
        assertTrue(point.globalSequence < committedRole.point.globalSequence)
        assertTrue(committedRole.point.globalSequence < dawn.point.globalSequence)
        assertFalse(s.state.gameState.playerAt(8)!!.alive)
        assertEquals(RoleId("Imp"), s.state.gameState.playerAt(7)!!.actualRole)
        assertEquals(StorytellerPhase.DAWN, s.state.phase)
    }

    @Test fun `existing death without publication cannot be repaired by a retry`() {
        val s = session()
        s.commitConfirmedPlayerDeath(death(s), s.state.gameStateRevision)
        val before = s.state
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedPlayerDeath(death(s), s.state.gameStateRevision,
                DawnCommitIdentity.deathObservation(s.state.gameId, 2, 8))
        }
        assertEquals(before, s.state)
    }

    @Test fun `stale new dawn death leaves both mechanics and publication unchanged`() {
        val s = session()
        val before = s.state
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedPlayerDeath(death(s), before.gameStateRevision - 1,
                DawnCommitIdentity.deathObservation(s.state.gameId, 2, 8))
        }
        assertEquals(before, s.state)
    }
}
