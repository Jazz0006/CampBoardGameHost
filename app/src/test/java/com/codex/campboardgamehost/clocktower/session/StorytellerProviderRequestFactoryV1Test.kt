package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerProviderRequestFactoryV1Test {
    @Test
    fun `Mayor target decision materializes neutral request without recommendation implementation`() {
        val mayorRevision = StorytellerDecisionRevision(8, 5)
        val mayorDecision = MayorRedirectDecisionBoundary.create(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = "provider-direct",
                requestId = "night:2:mayor-redirect",
            ),
            revision = mayorRevision,
            game = mayorGame(),
        )
        val mayorRequest = StorytellerProviderRequestFactoryV1.fromMayorRedirect(
            decision = mayorDecision,
            snapshot = runtimeSnapshot(
                phase = StorytellerPhase.NIGHT,
                gameRevision = 8,
                playerRevision = 5,
            ),
        )

        assertEquals(
            StorytellerProviderDecisionContextV1.MAYOR_REDIRECT,
            mayorRequest.identity.decisionTypeId,
        )
        assertEquals(
            mayorDecision.pending.legalCandidates.map { it.candidateId },
            mayorRequest.legalCandidateIds,
        )
        assertEquals(
            mayorDecision.legalTargetSeats,
            mayorRequest.legalCandidates
                .map { (it.payload as StorytellerProviderCandidatePayloadV1.SeatTarget).seat }
                .toSet(),
        )
    }

    @Test
    fun `game context is rebuilt from Host player inputs without uncommitted synthetic archive`() {
        val snapshot = runtimeSnapshot(
            phase = StorytellerPhase.NIGHT,
            gameRevision = 8,
            playerRevision = 5,
        )
        val context = StorytellerProviderGameContextBuilderV1.build(
            snapshot = snapshot,
            sourceRevision = StorytellerProviderRevisionV1(8, 5),
            playerInputsBySeat = mapOf(
                2 to StorytellerPlayerContextInputV1(
                    experienceLevel = PlayerExperienceLevelV1.BEGINNER,
                    claimedRoleIds = listOf(RoleId("Saint")),
                    pressureLevel = StorytellerDeclaredPressureLevelV1.HIGH,
                ),
            ),
        )

        assertEquals(snapshot.grimoireSeats.map { it.seat }, context.players.map { it.seat })
        assertEquals(PlayerExperienceLevelV1.BEGINNER, context.players[0].experienceLevel)
        assertEquals(PlayerExperienceLevelV1.BEGINNER, context.players[1].experienceLevel)
        assertEquals(listOf(RoleId("Saint")), context.players[1].claimedRoleIds)
        assertEquals(StorytellerDeclaredPressureLevelV1.HIGH, context.players[1].pressureLevel)
        assertTrue(context.priorDecisions.isEmpty())
    }

    @Test
    fun `session-owned provider context includes live history and does not elevate unsequenced decisions`() {
        val game = pairGame()
        val session = ClocktowerGameSession.createProduction(
            gameId = "provider-direct",
            gameSeed = game.seed,
            initialState = game,
            semanticHistoryMode = com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitAcceptedTestAction(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Poison(
                "first-poison", StorytellerPhase.FIRST_NIGHT, 1, 2, 2,
            ),
        )
        val context = StorytellerProviderGameContextBuilderV1.build(
            snapshot = runtimeSnapshot(StorytellerPhase.FIRST_NIGHT, 1L, 0L),
            sourceRevision = StorytellerProviderRevisionV1(1L, 0L),
            sessionState = session.state,
        )
        assertEquals("provider-direct", context.historyPrefix?.gameId)
        assertEquals(1L, context.historyPrefix?.exclusiveGlobalSequence)
        assertEquals(listOf("first-poison"), context.historyPrefix?.entries?.map { it.entryId })
        assertTrue(context.priorDecisions.isEmpty())
    }

    private fun pairGame() = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        ),
        seed = 7L,
    )

    private fun mayorGame() = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Mayor", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        ),
        seed = 7L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
        alive = true,
    )

    private fun runtimeSnapshot(
        phase: StorytellerPhase,
        gameRevision: Long,
        playerRevision: Long,
    ) = TroubleBrewingGameSnapshotV1(
        gameId = "provider-direct",
        gameSeed = 7L,
        position = TroubleBrewingSnapshotPosition(
            stage = TroubleBrewingSnapshotStage.RUNTIME,
            phase = SnapshotField.Known(phase),
            round = SnapshotField.Known(if (phase == StorytellerPhase.FIRST_NIGHT) 1 else 2),
            gameStateRevision = SnapshotField.Known(gameRevision),
            playerInputRevision = SnapshotField.Known(playerRevision),
        ),
        grimoireSeats = listOf(
            snapshotSeat(1, if (phase == StorytellerPhase.FIRST_NIGHT) "investigator" else "mayor"),
            snapshotSeat(2, "chef"),
            snapshotSeat(3, "recluse"),
            snapshotSeat(4, "poisoner"),
            snapshotSeat(5, "imp"),
        ),
        setupState = TroubleBrewingSnapshotSetupState(
            hasDrunk = SnapshotField.Known(false),
            drunkAssignmentSeat = SnapshotField.NotApplicable,
        ),
    )

    private fun snapshotSeat(
        seat: Int,
        roleId: String,
    ) = TroubleBrewingSnapshotSeat(
        seat = seat,
        shownRoleId = SnapshotField.Known(roleId),
        actualRoleId = SnapshotField.Known(roleId),
        alive = SnapshotField.Known(true),
        poisoned = SnapshotField.Known(false),
    )

}
