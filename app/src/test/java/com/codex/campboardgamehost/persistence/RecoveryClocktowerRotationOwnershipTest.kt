package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryClocktowerRotationOwnershipTest {
    @Test
    fun troubleBrewingRotationRecordIsOwnedByTypedClocktowerRecoveryAndRoundTrips() {
        val record = rotationRecord(playerCount = 2)
        val snapshot = clocktowerSnapshot(
            script = ClocktowerScript.TroubleBrewing,
            cards = twoCards(),
            rotationRecord = record,
        )

        val json = RecoverySnapshotJsonCodec.encode(snapshot)
        val decoded = RecoverySnapshotJsonCodec.decodeStrict(json, roleByName = { null })
        val game = decoded.game as ClocktowerRecovery

        assertTrue(json.has(TroubleBrewingSetupCompletionPersistence.ROOT_KEY))
        assertEquals(record, game.troubleBrewingSetupRotationRecord)
    }

    @Test(expected = IllegalArgumentException::class)
    fun noGreaterJoyRecoveryCannotCarryTroubleBrewingRotationRecord() {
        clocktowerSnapshot(
            script = ClocktowerScript.NoGreaterJoy,
            cards = twoCards(),
            rotationRecord = rotationRecord(playerCount = 2),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rotationRecordPlayerCountMustMatchRecoveredCards() {
        clocktowerSnapshot(
            script = ClocktowerScript.TroubleBrewing,
            cards = twoCards(),
            rotationRecord = rotationRecord(playerCount = 3),
        )
    }

    private fun clocktowerSnapshot(
        script: ClocktowerScript,
        cards: List<PlayerCard>,
        rotationRecord: TroubleBrewingSetupRotationRecord?,
    ): RecoverySnapshot {
        return RecoverySnapshot(
            compatibilityToken = "test-current-build",
            savedAtMillis = 1234L,
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = cards,
                records = emptyList(),
                outcome = null,
                identity = ClocktowerRecoveryIdentity(
                    script = script,
                    gameId = "game-rotation-test",
                    gameSeed = 99L,
                ),
                troubleBrewingSetupRotationRecord = rotationRecord,
                position = ClocktowerRecoveryPosition(
                    phase = ClocktowerPhase.Night,
                    nightStarted = true,
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
                    nextTimelineGlobalSequence = 0L,
                    events = emptyList(),
                    epistemicObservations = emptyList(),
                ),
            ),
        )
    }

    private fun twoCards(): List<PlayerCard> = listOf(
        PlayerCard("Alice", Role.Civilian, ""),
        PlayerCard("Bob", Role.Civilian, ""),
    )

    private fun rotationRecord(playerCount: Int): TroubleBrewingSetupRotationRecord =
        TroubleBrewingSetupRotationRecord(
            datasetId = "tb-rotation-test",
            schemaVersion = 1,
            presetId = "preset-$playerCount",
            playerCount = playerCount,
            realNonDemonRoleIds = (1 until playerCount).map { "role-$it" }.toSet(),
            minionRoleIds = emptySet(),
            primaryStyleTag = null,
            selectedDrunkShownRole = null,
        )
}
