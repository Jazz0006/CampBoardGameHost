package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
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
                eliminatedRound = if (index == 0) 1 else null,
            )
        }
        val saved = RecoverySnapshot(
            compatibilityToken = TOKEN,
            savedAtMillis = NOW - 1_000L,
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = cards,
                records = listOf(EliminationRecord(1, "Player 1")),
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
                    gameStateRevision = 1L,
                    playerInputRevision = 0L,
                    actionTimeline = ActionFactTimeline(),
                    nextTimelineGlobalSequence = 0L,
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
        assertEquals(1, recoveredGame.cards.first().eliminatedRound)
    }

    private companion object {
        const val TOKEN = "test-current-build"
        const val NOW = 20_000_000L
    }
}
