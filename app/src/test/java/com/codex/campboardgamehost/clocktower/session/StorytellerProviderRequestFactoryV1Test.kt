package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidateAuditSummary
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
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
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerProviderRequestFactoryV1Test {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Drunk setup engine decision materializes neutral request without SDE adapter`() {
        val snapshot = setupSnapshot()
        val revision = StorytellerDecisionRevision(4, 2)
        val decision = DrunkAssignmentDecisionBoundary.create(
            snapshot = snapshot,
            characterRegistry = canonicalRegistry(),
            revision = revision,
        )
        val request = StorytellerProviderRequestFactoryV1.fromDrunkAssignment(
            decision = decision,
            snapshot = snapshot,
        )

        assertEquals(
            StorytellerProviderDecisionContextV1.DRUNK_ASSIGNMENT,
            request.identity.decisionTypeId,
        )
        assertEquals("setup:drunk-seat:provider-direct", request.identity.decisionId)
        assertEquals(
            listOf(
                "setup:drunk-seat:seat-1",
                "setup:drunk-seat:seat-2",
            ),
            request.legalCandidateIds,
        )
        assertEquals(
            listOf(1, 2),
            request.legalCandidates.map {
                (it.payload as StorytellerProviderCandidatePayloadV1.DrunkAssignment).seat
            },
        )
        assertEquals(
            listOf("empath", "monk"),
            request.legalCandidates.map {
                (it.payload as StorytellerProviderCandidatePayloadV1.DrunkAssignment).shownRoleId
            },
        )
    }

    @Test
    fun `pair and Mayor engine decisions materialize neutral requests without recommendation implementation`() {
        val pairRevision = StorytellerDecisionRevision(3, 2)
        val pairDecision = PairInformationDecisionBoundary.create(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = "provider-direct",
                requestId = "first-night:investigator:1",
            ),
            revision = pairRevision,
            game = pairGame(),
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        val pairRequest = StorytellerProviderRequestFactoryV1.fromPairInformation(
            decision = pairDecision,
            snapshot = runtimeSnapshot(
                phase = StorytellerPhase.FIRST_NIGHT,
                gameRevision = 3,
                playerRevision = 2,
            ),
        )

        assertEquals(
            StorytellerProviderDecisionContextV1.FIRST_NIGHT_PAIR_INFORMATION,
            pairRequest.identity.decisionTypeId,
        )
        assertEquals(
            pairDecision.legalCandidates.map { it.candidateId },
            pairRequest.legalCandidateIds,
        )
        assertTrue(
            pairRequest.legalCandidates.all {
                it.payload is StorytellerProviderCandidatePayloadV1.PairInformation
            },
        )

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
    fun `game context is rebuilt from Host inputs and effective committed history prefix`() {
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
            decisionHistory = DecisionHistoryArchive(
                events = listOf(
                    event("past", 7, 4, DecisionEventStatus.APPLIED),
                    event("future", 9, 4, DecisionEventStatus.APPLIED),
                    event("proposal", 6, 3, DecisionEventStatus.PROPOSED),
                ),
            ),
        )

        assertEquals(snapshot.grimoireSeats.map { it.seat }, context.players.map { it.seat })
        assertEquals(PlayerExperienceLevelV1.NORMAL, context.players[0].experienceLevel)
        assertEquals(PlayerExperienceLevelV1.BEGINNER, context.players[1].experienceLevel)
        assertEquals(listOf(RoleId("Saint")), context.players[1].claimedRoleIds)
        assertEquals(StorytellerDeclaredPressureLevelV1.HIGH, context.players[1].pressureLevel)
        assertEquals(listOf("past"), context.priorDecisions.map { it.eventId })
        assertEquals("candidate:past", context.priorDecisions.single().selectedCandidateId)
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
        session.commitGlobalActionFact(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Poison(
                "first-poison", StorytellerPhase.FIRST_NIGHT, 1, 2, 2,
            ),
        )
        val context = StorytellerProviderGameContextBuilderV1.build(
            snapshot = runtimeSnapshot(StorytellerPhase.FIRST_NIGHT, 0L, 0L),
            sourceRevision = StorytellerProviderRevisionV1(0L, 0L),
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

    private fun setupSnapshot() = TroubleBrewingGameSnapshotV1(
        gameId = "provider-direct",
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
                shownRoleId = SnapshotField.Known("empath"),
                actualRoleId = SnapshotField.Uncommitted,
                alive = SnapshotField.Known(true),
                poisoned = SnapshotField.NotApplicable,
            ),
            TroubleBrewingSnapshotSeat(
                seat = 2,
                shownRoleId = SnapshotField.Known("monk"),
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

    private fun canonicalRegistry() =
        BuiltInClocktowerRulesetCatalog { assetPath ->
            File("src/main/assets", assetPath).readText(Charsets.UTF_8)
        }.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry

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

    private fun event(
        id: String,
        gameRevision: Long,
        playerRevision: Long,
        status: DecisionEventStatus,
    ): StorytellerDecisionEvent {
        val candidateId = "candidate:$id"
        return StorytellerDecisionEvent(
            eventId = id,
            requestId = "request:$id",
            idempotencyKey = "idem:$id",
            gameStateRevision = gameRevision,
            playerInputRevision = playerRevision,
            rulesetRef = RulesetRef(
                scriptId = ScriptId("trouble_brewing"),
                scriptContentHash = "00000000000000000000000000000000",
                rulesetVersion = "test",
                sourceRevision = "test",
                coverage = RuleCoverage.VERIFIED,
            ),
            algorithmConfigVersion = "historical",
            selectorVersion = "historical",
            decisionSeed = 1L,
            stateDigest = "state:$id",
            historyDigest = "history:$id",
            selectedCandidateId = candidateId,
            selectedOutcomeSnapshot = DecisionOutcomeSnapshot(
                decisionType = "test-decision",
                canonicalFields = sortedMapOf("seat" to "1"),
            ),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            registrations = emptyList(),
            qualityTier = QualityTier.RECOMMENDED,
            totalScore = 0,
            finalProbabilityFixedPoint = 0L,
            pressureDelta = emptyMap(),
            candidatePoolFingerprint = "pool:$id",
            candidateAudit = listOf(
                CandidateAuditSummary(
                    candidateId = candidateId,
                    candidateFamilyId = "family",
                    qualityTier = QualityTier.RECOMMENDED,
                    totalScore = 0,
                    finalProbabilityFixedPoint = 0L,
                    explanationCodes = emptyList(),
                ),
            ),
            explanationCodes = emptyList(),
            status = status,
        )
    }
}
