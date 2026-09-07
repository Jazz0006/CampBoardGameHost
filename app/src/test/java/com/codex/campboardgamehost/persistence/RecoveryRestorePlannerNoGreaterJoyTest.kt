package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import org.json.JSONObject
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
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = PersistedActiveGameIdentityEnvelope.clocktower(
                    PersistedGameContentIdentity(
                        kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
                        variantId = "no_greater_joy",
                        contentHash = "0123456789abcdef0123456789abcdef",
                        semanticVersion = "1",
                    ),
                ),
            ),
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
                    klutzChoiceName = null,
                    klutzReturnToDawn = false,
                    ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
                    highestVoteName = null,
                    highestVoteCount = 0,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = 0L,
                    playerInputRevision = 0L,
                    semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                    actionTimeline = ActionFactTimeline(),
                    nextTimelineGlobalSequence = 1L,
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

    private companion object {
        const val TOKEN = "test-current-build"
        const val NOW = 20_000_000L
    }
}
