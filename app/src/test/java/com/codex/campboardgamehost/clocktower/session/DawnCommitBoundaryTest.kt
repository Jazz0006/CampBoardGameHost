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

    @Test fun `explicit deaths remain independent of the attack protection and role legality`() {
        val s = session()
        s.commitConfirmedGameAction(ActionFactDraft.Attack("actual-attack", StorytellerPhase.NIGHT, 2,
            s.nextInputSequence, 8), s.state.gameState, s.state.gameStateRevision)
        s.commitConfirmedGameAction(ActionFactDraft.Protect("protected-one", StorytellerPhase.NIGHT, 2,
            s.nextInputSequence, 1), s.state.gameState, s.state.gameStateRevision)
        val before = s.state
        s.commitConfirmedNightDeaths(listOf(2, 1), before.gameStateRevision, before.playerInputRevision)
        assertEquals(8, s.state.nightTargets.attackSeat)
        assertFalse(s.state.gameState.playerAt(1)!!.alive)
        assertFalse(s.state.gameState.playerAt(2)!!.alive)
        val deaths = s.state.actionTimeline.entries.filter { it.fact is ActionFact.Death }
        assertEquals(listOf(1, 2), deaths.map { (it.fact as ActionFact.Death).targetSeat })
        assertEquals(2, s.state.epistemicObservationLog.records.size)
        assertEquals(before.gameStateRevision + 2, s.state.gameStateRevision)
        val accepted = s.state
        s.commitConfirmedNightDeaths(listOf(1, 2), accepted.gameStateRevision, accepted.playerInputRevision)
        assertEquals(accepted, s.state)
        val role = ActionFactDraft.RoleChange("confirmed-imp", StorytellerPhase.NIGHT, 2,
            s.nextInputSequence, 1, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON)
        s.commitConfirmedActualRoleChange(role, s.state.gameStateRevision)
        assertEquals(RoleId("Imp"), s.state.gameState.playerAt(1)!!.actualRole)
        assertFalse(s.state.gameState.playerAt(1)!!.alive)
    }

    @Test fun `unknown duplicate and stale result inputs cannot partially publish deaths`() {
        val s = session()
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) {
            s.commitConfirmedNightDeaths(listOf(1, 99), before.gameStateRevision, before.playerInputRevision)
        }
        assertThrows(IllegalArgumentException::class.java) {
            s.commitConfirmedNightDeaths(listOf(1, 1), before.gameStateRevision, before.playerInputRevision)
        }
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedNightDeaths(listOf(1), before.gameStateRevision - 1, before.playerInputRevision)
        }
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedNightDeaths(listOf(1), before.gameStateRevision, before.playerInputRevision + 1)
        }
        assertEquals(before, s.state)
    }

    @Test fun `a later publication conflict leaves every earlier staged death uncommitted`() {
        val s = session()
        s.commitGlobalEpistemicObservation(com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft(
            DawnCommitIdentity.deathObservation(s.state.gameId, 2, 2), StorytellerPhase.NIGHT, 2,
            s.nextInputSequence, null, null,
            com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility.PUBLIC, emptySet(),
            com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability.NOT_ABILITY_INFORMATION,
            com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.AliveAt(2, true),
        ))
        val before = s.state
        assertThrows(IllegalArgumentException::class.java) {
            s.commitConfirmedNightDeaths(listOf(1, 2), before.gameStateRevision, before.playerInputRevision)
        }
        assertEquals(before, s.state)
        assertTrue(s.state.gameState.playerAt(1)!!.alive)
        assertTrue(s.state.gameState.playerAt(2)!!.alive)
    }

    @Test fun `no deaths is an explicit public fact and AI event rather than missing history`() {
        val s = session()
        s.commitConfirmedNightDeaths(emptyList(), s.state.gameStateRevision, s.state.playerInputRevision)
        assertTrue(s.state.actionTimeline.entries.last().fact is ActionFact.NoNightDeaths)
        val accepted = s.state
        s.commitConfirmedNightDeaths(emptyList(), accepted.gameStateRevision, accepted.playerInputRevision)
        assertEquals(accepted, s.state)
        val prefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(s.state,
            StorytellerProviderRevisionV1(s.state.gameStateRevision, s.state.playerInputRevision))
        val delta = StorytellerGlobalEventProjectionV1.encode(prefix, accepted.nextTimelineGlobalSequence - 1)
        assertEquals("no_night_deaths", delta.getJSONObject(0).getString("type"))
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
