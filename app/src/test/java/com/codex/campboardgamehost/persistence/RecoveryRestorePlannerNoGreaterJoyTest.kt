package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.KlutzDeathTriggerEvidenceV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryRestorePlannerNoGreaterJoyTest {
    @Test
    fun noGreaterJoyRecoveryDoesNotRequireTroubleBrewingRulesetRef() {
        val roles = clocktowerRolesForScript(ClocktowerScript.NoGreaterJoy).take(5)
        require(roles.size == 5)
        val rolesByName = roles.associateBy(ClocktowerRole::enName)
        val snapshot = RecoverySnapshot(
            compatibilityToken = TOKEN,
            savedAtMillis = NOW - 1_000L,
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 1,
                cards = roles.mapIndexed { index, role ->
                    PlayerCard(
                        name = "Player ${index + 1}",
                        role = Role.Civilian,
                        word = "",
                        roleLabel = role.enName,
                        actualRoleLabel = role.enName,
                        clocktowerTeam = role.team,
                        clocktowerRole = role,
                        clocktowerShownRole = role,
                    )
                },
                records = emptyList(),
                outcome = null,
                identity = ClocktowerRecoveryIdentity(
                    script = ClocktowerScript.NoGreaterJoy,
                    gameId = "ngj-recovery",
                    gameSeed = 7L,
                ),
                position = ClocktowerRecoveryPosition(
                    phase = ClocktowerPhase.Day,
                    nightStarted = false,
                    nightStepIndex = 0,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = null,
                    confirmedPoisonTarget = null,
                    confirmedMonkProtectedTarget = null,
                    confirmedMayorRedirectTarget = null,
                    pendingNewDemonName = null,
                    pendingNightNewDemonIdentityName = null,
                    confirmedDemonSuccessorTarget = null,
                    redHerring = null,
                    demonBluffRoleNames = emptyList(),
                    butlerMaster = null,
                    virginUsed = false,
                    slayerUsed = false,
                    slayerClaimedNames = emptyList(),
                    artistUsed = false,
                    artistClaimedNames = emptyList(),
                    lastExecutedName = null,
                    pendingKlutzName = null,
                    klutzReturnToDawn = false,
                    ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
                    highestVoteName = null,
                    highestVoteCount = 0,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = 0L,
                    playerInputRevision = 0L,
                    actionTimeline = ActionFactTimeline(),
                    nextTimelineGlobalSequence = 0L,
                    events = emptyList(),
                    epistemicObservations = emptyList(),
                ),
            ),
        )
        val result = RecoveryRestorePlanner.prepare(
            raw = RecoverySnapshotJsonCodec.encode(snapshot),
            expectedCompatibilityToken = TOKEN,
            nowMillis = NOW,
            roleByName = rolesByName::get,
            clocktowerRulesetResolver = { _, _ -> null },
        )

        assertTrue(result is RecoveryPlanPreparation.Ready)
        val runtime = (result as RecoveryPlanPreparation.Ready).plan.clocktowerRuntime
        assertNotNull(runtime)
        assertEquals(null, runtime?.rulesetRef)
    }

    @Test
    fun `real No Greater Joy pending learned-death Klutz choice survives current Recovery`() {
        val names = listOf("Klutz", "Imp", "Artist", "Baron", "Empath")
        val byName = clocktowerRolesForScript(ClocktowerScript.NoGreaterJoy)
            .associateBy(ClocktowerRole::enName)
        val cards = names.mapIndexed { index, roleName ->
            val role = requireNotNull(byName[roleName])
            PlayerCard(
                name = "Player ${index + 1}",
                role = Role.Civilian,
                word = "",
                roleLabel = role.enName,
                actualRoleLabel = role.enName,
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = role,
                eliminatedRound = if (index == 0) 2 else null,
            )
        }
        val learnedHistory = ActionFactTimeline(listOf(
            TimelineBoundActionFact(
                ActionFact.Nomination("ngj-nomination", 0L, 3, 4, firstVirginNomination = false),
                TimelinePoint(StorytellerPhase.DAY, 1, 1, 0L),
            ),
            TimelineBoundActionFact(
                ActionFact.Vote("ngj-vote", 1L, 3, 4, listOf(2, 3, 4), emptyList()),
                TimelinePoint(StorytellerPhase.DAY, 1, 2, 1L),
            ),
            TimelineBoundActionFact(
                ActionFact.NoExecution("ngj-confirmed-no-execution", 2L),
                TimelinePoint(StorytellerPhase.DAY, 1, 3, 2L),
            ),
            TimelineBoundActionFact(
                ActionFact.Death(
                    "real-death", 3L, 1,
                    KlutzDeathTriggerEvidenceV1(RoleId("Klutz"), true, false, 0L),
                ),
                TimelinePoint(StorytellerPhase.DAWN, 2, 1, 3L),
            ),
            TimelineBoundActionFact(
                ActionFact.KlutzLearnedDeath("real-learn", 4L, 1, "real-death", true),
                TimelinePoint(StorytellerPhase.DAY, 2, 2, 4L),
            ),
        ))
        val saved = RecoverySnapshot(
            compatibilityToken = TOKEN,
            savedAtMillis = NOW - 1_000L,
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = cards,
                records = listOf(EliminationRecord(2, "Player 1")),
                outcome = null,
                identity = ClocktowerRecoveryIdentity(
                    script = ClocktowerScript.NoGreaterJoy,
                    gameId = "ngj-real-klutz-learn", gameSeed = 73L,
                ),
                position = ClocktowerRecoveryPosition(
                    phase = ClocktowerPhase.Day,
                    nightStarted = false,
                    nightStepIndex = 0,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = null,
                    confirmedPoisonTarget = null,
                    confirmedMonkProtectedTarget = null,
                    confirmedMayorRedirectTarget = null,
                    pendingNewDemonName = null,
                    pendingNightNewDemonIdentityName = null,
                    confirmedDemonSuccessorTarget = null,
                    redHerring = null,
                    demonBluffRoleNames = emptyList(),
                    butlerMaster = null,
                    virginUsed = false,
                    slayerUsed = false,
                    slayerClaimedNames = emptyList(),
                    artistUsed = false,
                    artistClaimedNames = emptyList(),
                    lastExecutedName = null,
                    pendingKlutzName = "Player 1",
                    klutzReturnToDawn = true,
                    ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
                    highestVoteName = null,
                    highestVoteCount = 0,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = 5L,
                    playerInputRevision = 0L,
                    actionTimeline = learnedHistory,
                    nextTimelineGlobalSequence = 5L,
                    events = emptyList(),
                    epistemicObservations = emptyList(),
                ),
            ),
        )
        val encoded = RecoverySnapshotJsonCodec.encode(saved)
        val restored = RecoveryRestorePlanner.prepare(
            raw = encoded,
            expectedCompatibilityToken = TOKEN,
            nowMillis = NOW,
            roleByName = byName::get,
            clocktowerRulesetResolver = { _, _ -> null },
        )
        assertTrue(restored is RecoveryPlanPreparation.Ready)
        val recoveredGame = (restored as RecoveryPlanPreparation.Ready).plan.snapshot.game
            as ClocktowerRecovery
        assertEquals(ClocktowerScript.NoGreaterJoy, recoveredGame.identity.script)
        assertEquals("Player 1", recoveredGame.mechanics.pendingKlutzName)
        assertTrue(recoveredGame.mechanics.klutzReturnToDawn)
        assertEquals(2, recoveredGame.cards.first().eliminatedRound)
        assertEquals(learnedHistory, recoveredGame.history.actionTimeline)
        val publicEvents = com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalTimeline.project(
            recipientSeat = 5,
            actionTimeline = recoveredGame.history.actionTimeline,
            observationLog = com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog(),
        )
        assertTrue(publicEvents[0] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicNomination)
        assertTrue(publicEvents[1] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicVote)
        assertTrue(publicEvents[2] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicNoExecution)
        assertTrue(publicEvents[3] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicDeath)
        assertTrue(publicEvents.none {
            it is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicKlutzChoice
        }) // Choice has not been publicly confirmed at the pending recovery point.
        assertEquals(listOf("nomination", "vote"), learnedHistory.entries.take(2).map { entry ->
            when (entry.fact) {
                is ActionFact.Nomination -> "nomination"
                is ActionFact.Vote -> "vote"
                else -> "unexpected"
            }
        })

        val forgedChoice = TimelineBoundActionFact(
            ActionFact.KlutzChoice("forged-choice", 5L, 1, 2, "no-such-learn"),
            TimelinePoint(StorytellerPhase.DAY, 2, 3, 5L),
        )
        val bad = saved.copy(game = (saved.game as ClocktowerRecovery).copy(
            history = saved.game.history.copy(
                actionTimeline = ActionFactTimeline(learnedHistory.entries + forgedChoice),
                nextTimelineGlobalSequence = 6L,
            ),
        ))
        val rejected = RecoveryRestorePlanner.prepare(
            raw = RecoverySnapshotJsonCodec.encode(bad),
            expectedCompatibilityToken = TOKEN,
            nowMillis = NOW,
            roleByName = byName::get,
            clocktowerRulesetResolver = { _, _ -> null },
        )
        assertTrue(rejected is RecoveryPlanPreparation.Rejected)

        // A forged Virgin-first marker on NGJ must not survive strict Recovery.
        val fakeVirgin = saved.copy(game = (saved.game as ClocktowerRecovery).copy(
            history = saved.game.history.copy(
                actionTimeline = ActionFactTimeline(learnedHistory.entries.mapIndexed { index, entry ->
                    if (index == 0) entry.copy(fact =
                        (entry.fact as ActionFact.Nomination).copy(firstVirginNomination = true))
                    else entry
                }),
            ),
        ))
        assertTrue(RecoveryRestorePlanner.prepare(
            raw = RecoverySnapshotJsonCodec.encode(fakeVirgin),
            expectedCompatibilityToken = TOKEN,
            nowMillis = NOW,
            roleByName = byName::get,
            clocktowerRulesetResolver = { _, _ -> null },
        ) is RecoveryPlanPreparation.Rejected)

        // An NGJ vote with no matching confirmed nomination also must fail.
        val unmatchedVote = saved.copy(game = (saved.game as ClocktowerRecovery).copy(
            history = saved.game.history.copy(
                actionTimeline = ActionFactTimeline(learnedHistory.entries.mapIndexed { index, entry ->
                    if (index == 1) entry.copy(fact =
                        (entry.fact as ActionFact.Vote).copy(nomineeSeat = 5))
                    else entry
                }),
            ),
        ))
        assertTrue(RecoveryRestorePlanner.prepare(
            raw = RecoverySnapshotJsonCodec.encode(unmatchedVote),
            expectedCompatibilityToken = TOKEN,
            nowMillis = NOW,
            roleByName = byName::get,
            clocktowerRulesetResolver = { _, _ -> null },
        ) is RecoveryPlanPreparation.Rejected)

        val fakeSlayer = saved.copy(game = (saved.game as ClocktowerRecovery).copy(
            history = saved.game.history.copy(
                actionTimeline = ActionFactTimeline(learnedHistory.entries + TimelineBoundActionFact(
                    ActionFact.SlayerShot("ngj-impossible-slayer", 5L, 3, 2,
                        abilityConsumed = false, hit = false),
                    TimelinePoint(StorytellerPhase.DAY, 2, 3, 5L),
                )),
                nextTimelineGlobalSequence = 6L,
            ),
        ))
        assertTrue(RecoveryRestorePlanner.prepare(
            raw = RecoverySnapshotJsonCodec.encode(fakeSlayer),
            expectedCompatibilityToken = TOKEN,
            nowMillis = NOW,
            roleByName = byName::get,
            clocktowerRulesetResolver = { _, _ -> null },
        ) is RecoveryPlanPreparation.Rejected)
    }

    private companion object {
        const val TOKEN = "test-current-build"
        const val NOW = 20_000_000L
    }
}
