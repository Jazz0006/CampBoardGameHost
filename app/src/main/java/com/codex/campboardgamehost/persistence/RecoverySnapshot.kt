package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord

/**
 * Short-horizon process-death recovery state.
 *
 * This models game facts and mandatory continuation only. It intentionally does not model arbitrary
 * Compose/navigation state. [RecoveryEntryPoint] is the narrow exception required while identities
 * are still being handed to players.
 */
internal data class RecoverySnapshot(
    val recoveryFormatVersion: Int = CURRENT_FORMAT_VERSION,
    val compatibilityToken: String,
    val savedAtMillis: Long,
    val game: RecoveryGame,
) {
    init {
        require(recoveryFormatVersion == CURRENT_FORMAT_VERSION) {
            "Unsupported in-memory recovery format version $recoveryFormatVersion."
        }
        require(compatibilityToken.isNotBlank()) { "Recovery compatibility token cannot be blank." }
        require(savedAtMillis >= 0L) { "Recovery timestamp cannot be negative." }
    }

    companion object {
        const val CURRENT_FORMAT_VERSION: Int = 2
    }
}

internal enum class RecoveryEntryPoint {
    Stable,
    PassPhone,
    RevealCard,
}

internal sealed interface RecoveryGame {
    val gameKind: GameKind
    val entryPoint: RecoveryEntryPoint
    val currentDealIndex: Int
    val round: Int
    val cards: List<PlayerCard>
    val records: List<EliminationRecord>
    val outcome: GameOutcome?
}

internal data class UndercoverRecovery(
    override val entryPoint: RecoveryEntryPoint,
    override val currentDealIndex: Int,
    override val round: Int,
    override val cards: List<PlayerCard>,
    override val records: List<EliminationRecord>,
    override val outcome: GameOutcome?,
    val undercoverCount: Int,
    val includeBlank: Boolean,
    val lastWordsMode: LastWordsMode,
) : RecoveryGame {
    override val gameKind: GameKind = GameKind.Undercover
}

internal data class WerewolfRecovery(
    override val entryPoint: RecoveryEntryPoint,
    override val currentDealIndex: Int,
    override val round: Int,
    override val cards: List<PlayerCard>,
    override val records: List<EliminationRecord>,
    override val outcome: GameOutcome?,
    val werewolfCount: Int,
    val includeSeer: Boolean,
    val includeWitch: Boolean,
    val includeHunter: Boolean,
    val lastWordsMode: LastWordsMode,
    /** Night-flow cursor; already performed role interactions must not be replayed after process death. */
    val judgeStepIndex: Int,
    val pendingNightDeath: String?,
    val seerCheckTarget: String?,
    val witchSaveUsed: Boolean,
    val witchPoisonUsed: Boolean,
    val witchSavedTonight: Boolean,
    val witchPoisonTarget: String?,
    val hunterShotTarget: String?,
) : RecoveryGame {
    override val gameKind: GameKind = GameKind.Werewolf
}

internal data class ClocktowerRecovery(
    override val entryPoint: RecoveryEntryPoint,
    override val currentDealIndex: Int,
    override val round: Int,
    override val cards: List<PlayerCard>,
    override val records: List<EliminationRecord>,
    override val outcome: GameOutcome?,
    val identity: ClocktowerRecoveryIdentity,
    val troubleBrewingSetupRotationRecord: TroubleBrewingSetupRotationRecord? = null,
    val position: ClocktowerRecoveryPosition,
    val mechanics: ClocktowerRecoveryMechanics,
    val history: ClocktowerRecoveryHistory,
) : RecoveryGame {
    override val gameKind: GameKind = GameKind.Clocktower

    init {
        troubleBrewingSetupRotationRecord?.let { record ->
            require(identity.script == ClocktowerScript.TroubleBrewing) {
                "Only Trouble Brewing recovery can carry Trouble Brewing setup rotation bookkeeping."
            }
            require(record.playerCount == cards.size) {
                "Trouble Brewing setup rotation player count must match recovered cards."
            }
        }
    }
}

internal data class ClocktowerRecoveryIdentity(
    val script: ClocktowerScript,
    val gameId: String,
    val gameSeed: Long,
)

/** Safe resumable position, not raw UI navigation. */
internal data class ClocktowerRecoveryPosition(
    val phase: ClocktowerPhase,
    val nightStarted: Boolean,
    val nightStepIndex: Int,
)

/** Durable consequences plus rule-mandated unfinished continuations. */
internal data class ClocktowerRecoveryMechanics(
    val confirmedAttackTarget: String?,
    val confirmedPoisonTarget: String?,
    val confirmedMonkProtectedTarget: String?,
    val confirmedMayorRedirectTarget: String?,
    val pendingNewDemonName: String?,
    val pendingNightNewDemonIdentityName: String?,
    val confirmedDemonSuccessorTarget: String?,
    val redHerring: String?,
    val demonBluffRoleNames: List<String>,
    val butlerMaster: String?,
    val virginUsed: Boolean,
    val slayerUsed: Boolean,
    val slayerClaimedNames: List<String>,
    val artistUsed: Boolean,
    val artistClaimedNames: List<String>,
    val lastExecutedName: String?,
    val pendingKlutzName: String?,
    val klutzChoiceName: String?,
    val klutzReturnToDawn: Boolean,
    val ghostVoteAuthority: ClocktowerGhostVoteAuthority,
    val highestVoteName: String?,
    val highestVoteCount: Int,
)

/** Published/history authority used by recommendation and epistemic replay after recovery. */
internal data class ClocktowerRecoveryHistory(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val semanticHistoryMode: ClocktowerSemanticHistoryMode,
    val actionTimeline: ActionFactTimeline,
    val nextTimelineGlobalSequence: Long,
    val events: List<ClocktowerEvent>,
    val epistemicObservations: List<RecordedEpistemicObservation>,
)
