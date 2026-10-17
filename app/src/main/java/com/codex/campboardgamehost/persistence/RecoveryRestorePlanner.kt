package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import org.json.JSONObject

internal enum class RecoveryRejectionReason {
    UnsupportedFormat,
    CompatibilityMismatch,
    InvalidTimestamp,
    Expired,
    MalformedPayload,
    InvalidGameState,
}

internal sealed interface RecoveryPlanPreparation {
    data class Ready(val plan: ValidatedRecoveryPlan) : RecoveryPlanPreparation
    data class Rejected(val reason: RecoveryRejectionReason) : RecoveryPlanPreparation
}

internal enum class ClocktowerRecoveryContinuation {
    Klutz,
}

internal sealed interface RecoverySafeReentry {
    object UndercoverGame : RecoverySafeReentry
    data class PassPhone(val dealIndex: Int) : RecoverySafeReentry
    data class RevealCard(val dealIndex: Int) : RecoverySafeReentry
    data class ClocktowerJudge(
        val phase: ClocktowerPhase,
        val nightStepIndex: Int,
        val continuation: ClocktowerRecoveryContinuation? = null,
    ) : RecoverySafeReentry
}

internal data class ValidatedClocktowerRecoveryRuntime(
    val rulesetRef: RulesetRef?,
    /** The fully verified Session is applied directly; UI never rebuilds it from recovered cards. */
    val session: ClocktowerGameSession,
)

internal data class ValidatedRecoveryPlan(
    val snapshot: RecoverySnapshot,
    val safeReentry: RecoverySafeReentry,
    val presentResults: Boolean,
    val clocktowerRuntime: ValidatedClocktowerRecoveryRuntime? = null,
) {
    init {
        require((snapshot.game is ClocktowerRecovery) == (clocktowerRuntime != null)) {
            "Clocktower recovery plans must carry a fully resolved Clocktower runtime."
        }
    }
}

/** Pure short-horizon eligibility gate. It deliberately does not recreate v3 content compatibility. */
internal object RecoveryValidityPolicy {
    const val MAX_AGE_MILLIS: Long = 4L * 60L * 60L * 1000L

    fun rejectionReason(
        raw: JSONObject,
        expectedCompatibilityToken: String,
        nowMillis: Long,
    ): RecoveryRejectionReason? {
        require(expectedCompatibilityToken.isNotBlank()) { "Expected recovery compatibility token cannot be blank." }

        val formatVersion = raw.integralLongOrMalformed(RecoverySnapshotJsonCodec.FORMAT_VERSION_KEY)
            ?: return RecoveryRejectionReason.MalformedPayload
        if (formatVersion != RecoverySnapshot.CURRENT_FORMAT_VERSION.toLong()) {
            return RecoveryRejectionReason.UnsupportedFormat
        }

        val token = raw.strictStringOrMalformed(RecoverySnapshotJsonCodec.COMPATIBILITY_TOKEN_KEY)
            ?: return RecoveryRejectionReason.MalformedPayload
        if (token != expectedCompatibilityToken) {
            return RecoveryRejectionReason.CompatibilityMismatch
        }

        val savedAtMillis = raw.integralLongOrMalformed("savedAtMillis")
            ?: return RecoveryRejectionReason.MalformedPayload
        if (nowMillis < 0L || savedAtMillis < 0L || savedAtMillis > nowMillis) {
            return RecoveryRejectionReason.InvalidTimestamp
        }
        if (nowMillis - savedAtMillis > MAX_AGE_MILLIS) {
            return RecoveryRejectionReason.Expired
        }
        return null
    }

    private fun JSONObject.strictStringOrMalformed(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return opt(key) as? String
    }

    private fun JSONObject.integralLongOrMalformed(key: String): Long? {
        if (!has(key) || isNull(key)) return null
        val raw = opt(key)
        if (raw !is Byte && raw !is Short && raw !is Int && raw !is Long) return null
        return (raw as Number).toLong()
    }
}

/**
 * Atomic PS3.1 preparation boundary: once [ValidatedRecoveryPlan] exists there is no JSON parsing,
 * compatibility lookup, or fallible Clocktower ruleset reconstruction left for the apply phase.
 */
internal object RecoveryRestorePlanner {
    fun prepare(
        raw: JSONObject,
        expectedCompatibilityToken: String,
        nowMillis: Long,
        roleByName: (String) -> ClocktowerRole?,
        clocktowerRulesetResolver: (ClocktowerScript, ClocktowerRulesetPersistenceBasis) -> RulesetRef?,
    ): RecoveryPlanPreparation {
        RecoveryValidityPolicy.rejectionReason(
            raw = raw,
            expectedCompatibilityToken = expectedCompatibilityToken,
            nowMillis = nowMillis,
        )?.let { reason -> return RecoveryPlanPreparation.Rejected(reason) }

        val snapshot = try {
            RecoverySnapshotJsonCodec.decodeStrict(raw, roleByName)
        } catch (_: RuntimeException) {
            return RecoveryPlanPreparation.Rejected(RecoveryRejectionReason.MalformedPayload)
        }

        return try {
            val plan = validateAndPlan(snapshot, clocktowerRulesetResolver)
            RecoveryPlanPreparation.Ready(plan)
        } catch (_: RuntimeException) {
            RecoveryPlanPreparation.Rejected(RecoveryRejectionReason.InvalidGameState)
        }
    }

    private fun validateAndPlan(
        snapshot: RecoverySnapshot,
        clocktowerRulesetResolver: (ClocktowerScript, ClocktowerRulesetPersistenceBasis) -> RulesetRef?,
    ): ValidatedRecoveryPlan {
        validateCommon(snapshot.game)

        val clocktowerRuntime = when (val game = snapshot.game) {
            is UndercoverRecovery -> {
                validateUndercover(game)
                null
            }
            is ClocktowerRecovery -> validateClocktower(game, clocktowerRulesetResolver)
        }

        return ValidatedRecoveryPlan(
            snapshot = snapshot,
            safeReentry = deriveSafeReentry(snapshot.game),
            presentResults = snapshot.game.outcome != null,
            clocktowerRuntime = clocktowerRuntime,
        )
    }

    private fun validateCommon(game: RecoveryGame) {
        require(game.cards.isNotEmpty()) { "Recovery requires at least one player card." }
        require(game.round > 0) { "Recovery round must be positive." }
        require(game.currentDealIndex >= 0) { "Recovery deal index cannot be negative." }
        if (game.entryPoint == RecoveryEntryPoint.Stable) {
            require(game.currentDealIndex == 0) { "Stable recovery cannot carry a deal continuation index." }
        }

        val names = game.cards.map(PlayerCard::name)
        require(names.all(String::isNotBlank)) { "Recovery player names cannot be blank." }
        require(names.distinct().size == names.size) { "Recovery player names must be unique." }
        val knownNames = names.toSet()

        if (game.entryPoint != RecoveryEntryPoint.Stable) {
            require(game.currentDealIndex in game.cards.indices) {
                "Pass/reveal recovery deal index must reference an existing card."
            }
        }

        game.cards.forEach { card ->
            card.eliminatedRound?.let { eliminatedRound ->
                require(eliminatedRound in 1..game.round) {
                    "Eliminated round must belong to the recovered game."
                }
            }
        }
        game.records.forEach { record ->
            require(record.round in 1..game.round) { "Elimination record round is outside the recovered game." }
            require(record.playerName in knownNames) { "Elimination record references an unknown player." }
        }
    }

    private fun validateUndercover(game: UndercoverRecovery) {
        require(game.cards.all { it.role in UNDERCOVER_ROLES }) { "Undercover recovery contains a foreign role." }
        require(game.cards.any { it.role == Role.Undercover }) {
            "Undercover recovery requires at least one Undercover card."
        }
        require(game.cards.all { it.clocktowerRole == null && it.clocktowerShownRole == null }) {
            "Undercover recovery cannot carry Clocktower role identity."
        }
    }

    private fun validateClocktower(
        game: ClocktowerRecovery,
        clocktowerRulesetResolver: (ClocktowerScript, ClocktowerRulesetPersistenceBasis) -> RulesetRef?,
    ): ValidatedClocktowerRecoveryRuntime {
        require(game.identity.gameId.isNotBlank()) { "Clocktower recovery requires a game ID." }
        require(game.position.nightStepIndex >= 0) { "Clocktower night step cannot be negative." }
        require(game.history.gameStateRevision >= 0L && game.history.playerInputRevision >= 0L) {
            "Clocktower revisions cannot be negative."
        }

        val knownNames = game.cards.mapTo(linkedSetOf(), PlayerCard::name)
        val actualRoles = game.cards.map { card ->
            val role = requireNotNull(card.clocktowerRole) {
                "Clocktower recovery requires an actual role for every player."
            }
            require(card.clocktowerTeam == role.team) {
                "Clocktower card team must agree with its actual role."
            }
            requireNotNull(card.clocktowerShownRole) {
                "Clocktower recovery requires a shown role for every player."
            }
            role
        }

        val mechanics = game.mechanics
        listOf(
            mechanics.confirmedAttackTarget,
            mechanics.confirmedMonkProtectedTarget,
            mechanics.confirmedMayorRedirectTarget,
            mechanics.pendingNewDemonName,
            mechanics.pendingNightNewDemonIdentityName,
            mechanics.confirmedDemonSuccessorTarget,
            mechanics.redHerring,
            mechanics.butlerMaster,
            mechanics.lastExecutedName,
            mechanics.pendingKlutzName,
            mechanics.highestVoteName,
        ).forEach { name -> requireKnownName(name, knownNames) }
        mechanics.slayerClaimedNames.forEach { requireKnownName(it, knownNames) }
        mechanics.artistClaimedNames.forEach { requireKnownName(it, knownNames) }
        require(mechanics.slayerClaimedNames.distinct().size == mechanics.slayerClaimedNames.size) {
            "Slayer claim history cannot contain duplicates."
        }
        require(mechanics.artistClaimedNames.distinct().size == mechanics.artistClaimedNames.size) {
            "Artist claim history cannot contain duplicates."
        }
        require(mechanics.demonBluffRoleNames.distinct().size == mechanics.demonBluffRoleNames.size) {
            "Demon bluffs cannot contain duplicates."
        }
        require(mechanics.highestVoteCount >= 0) { "Highest vote count cannot be negative." }

        mechanics.ghostVoteAuthority.spentSeatIds.forEach { seatId ->
            require(seatId.number in 1..game.cards.size) { "Ghost-vote authority references an unknown seat." }
        }

        if (mechanics.pendingKlutzName == null) {
            require(!mechanics.klutzReturnToDawn) { "Klutz return-to-Dawn flag requires a pending Klutz." }
        } else {
            require(game.entryPoint == RecoveryEntryPoint.Stable) { "Klutz continuation cannot coexist with deal UI." }
            require(game.position.phase == ClocktowerPhase.Day) { "Pending Klutz must re-enter the Day phase." }
            require(game.outcome == null) { "A resolved game cannot retain a pending Klutz continuation." }
        }

        if (mechanics.pendingNewDemonName != null && mechanics.pendingNightNewDemonIdentityName != null) {
            require(mechanics.pendingNewDemonName == mechanics.pendingNightNewDemonIdentityName) {
                "Pending new-Demon identity facts disagree."
            }
        }
        mechanics.confirmedDemonSuccessorTarget?.let { confirmed ->
            mechanics.pendingNewDemonName?.let { pending ->
                require(confirmed == pending) { "Confirmed Demon successor disagrees with pending new Demon." }
            }
        }

        game.history.events.forEach { event ->
            require(event.sequence >= 0) { "Clocktower event sequence cannot be negative." }
            require(event.round in 1..game.round) { "Clocktower event round is outside the recovered game." }
            require(event.playerNames.all { it in knownNames }) { "Clocktower event references an unknown player." }
        }

        validateSemanticHistory(game)
        game.history.causalDecisionJournal?.let { archive ->
            require(game.identity.script == ClocktowerScript.TroubleBrewing || archive.records.isEmpty()) {
                "Only the current Trouble Brewing typed snapshot can own a causal decision journal."
            }
        }
        // Current-format emergency recovery carries one accepted poison fact in Session
        // chronology, not a second saved UI/mechanics target.
        val poisonedPlayerName = game.history.actionTimeline.entries.asReversed()
            .firstOrNull { entry -> entry.fact is ActionFact.Poison }
            ?.let { entry -> (entry.fact as ActionFact.Poison).targetSeat }
            ?.let { seat -> game.cards.getOrNull(seat - 1)?.name
                ?: error("Recovered poison target references an unknown seat.") }
        // Build the single recovered Session before mutating live UI.
        val restoredState = ClocktowerSessionState(
            gameId = game.identity.gameId,
            gameStateRevision = game.history.gameStateRevision,
            playerInputRevision = game.history.playerInputRevision,
            gameSeed = game.identity.gameSeed,
            gameState = game.cards.toClocktowerGameState(
                script = game.identity.script,
                seed = game.identity.gameSeed,
                poisonedPlayerName = poisonedPlayerName,
            ),
            storytellerPlayerContextBySeat = game.history.storytellerPlayerContextBySeat,
            actionTimeline = game.history.actionTimeline,
            epistemicObservationLog = EpistemicObservationLog(game.history.epistemicObservations),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            nextTimelineGlobalSequence = game.history.nextTimelineGlobalSequence,
            redHerringSeat = mechanics.redHerring?.let { name ->
                game.cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                    ?: error("Recovered Red Herring is absent from the canonical seats.")
            },
            demonBluffRoleIds = mechanics.demonBluffRoleNames.map(::RoleId),
        )
        // The read-only Host card projection needs a confirmed death/execute point for
        // every dead Session seat. Fail closed *before* modifying Compose state.
        val recordedDeathSeats = restoredState.actionTimeline.entries.mapNotNull { entry ->
            when (val fact = entry.fact) {
                is ActionFact.Death -> fact.targetSeat
                is ActionFact.Execution -> fact.targetSeat
                else -> null
            }
        }.toSet()
        require(restoredState.gameState.players.all { it.alive || it.seat in recordedDeathSeats }) {
            "Dead recovered Clocktower seats require an accepted death or execution fact."
        }
        val restoredSession = ClocktowerGameSession.restoreProduction(
            state = restoredState,
            causalDecisionArchive = game.history.causalDecisionJournal,
        )

        val basis = ClocktowerRulesetPersistenceBasis(
            actualRoles.mapTo(linkedSetOf()) { role -> RoleId(role.enName) },
        )
        val allowedRoleIds = clocktowerRolesForScript(game.identity.script)
            .mapTo(linkedSetOf()) { role -> RoleId(role.enName) }
        require(basis.roleIds.all { roleId -> roleId in allowedRoleIds }) {
            "Recovered Clocktower roles do not belong to the selected current script."
        }

        val resolvedRuleset = when (game.identity.script) {
            ClocktowerScript.TroubleBrewing -> clocktowerRulesetResolver(game.identity.script, basis)
                ?: throw IllegalArgumentException(
                    "Current Trouble Brewing rules cannot resolve the recovered assigned roles.",
                )
            ClocktowerScript.NoGreaterJoy -> null
        }
        resolvedRuleset?.let { ruleset ->
            require(ruleset.scriptId == game.identity.script.toRecommendationScriptId()) {
                "Resolved Clocktower ruleset belongs to a different script."
            }
        }
        return ValidatedClocktowerRecoveryRuntime(
            rulesetRef = resolvedRuleset,
            session = restoredSession,
        )
    }

    private fun validateSemanticHistory(game: ClocktowerRecovery) {
        val playerCount = game.cards.size
        val history = game.history

        history.actionTimeline.entries.forEach { entry ->
            require(entry.point.round in 1..game.round) {
                "Action timeline point is outside the recovered game round."
            }
            // This is a current snapshot read boundary, not a second historical rule engine.
            // Verify referenced seats, then rely on Host-committed facts and Session integrity.
            when (val fact = entry.fact) {
                is ActionFact.Poison -> fact.targetSeat?.let { requireKnownSeat(it, playerCount) }
                is ActionFact.Protect -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.Attack -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.Execution -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.Death -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.SlayerShot -> {
                    requireKnownSeat(fact.claimantSeat, playerCount)
                    requireKnownSeat(fact.targetSeat, playerCount)
                }
                is ActionFact.Nomination -> {
                    requireKnownSeat(fact.nominatorSeat, playerCount)
                    requireKnownSeat(fact.nomineeSeat, playerCount)
                }
                is ActionFact.Vote -> {
                    requireKnownSeat(fact.nominatorSeat, playerCount)
                    requireKnownSeat(fact.nomineeSeat, playerCount)
                    fact.voterSeats.forEach { requireKnownSeat(it, playerCount) }
                    fact.ghostVoterSeats.forEach { requireKnownSeat(it, playerCount) }
                }
                is ActionFact.KlutzLearnedDeath -> requireKnownSeat(fact.klutzSeat, playerCount)
                is ActionFact.KlutzChoice -> {
                    requireKnownSeat(fact.klutzSeat, playerCount)
                    requireKnownSeat(fact.chosenSeat, playerCount)
                }
                is ActionFact.RoleChange -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.ButlerMasterChoice -> {
                    requireKnownSeat(fact.butlerSeat, playerCount)
                    requireKnownSeat(fact.masterSeat, playerCount)
                }
                is ActionFact.RedHerringChoice ->
                    fact.selectedSeat?.let { requireKnownSeat(it, playerCount) }
                is ActionFact.MayorRedirectChoice -> {
                    requireKnownSeat(fact.mayorSeat, playerCount)
                    requireKnownSeat(fact.targetSeat, playerCount)
                }
                is ActionFact.PhaseAdvance ->
                    require(fact.round in 1..game.round) { "Phase advance exceeds current game round." }
                is ActionFact.NoExecution, is ActionFact.DemonBluffChoice -> Unit
            }
        }


        // Accepted setup decisions must agree with their last typed event. Recovery never
        // substitutes an independently mutable UI choice for an established Session fact.
        history.actionTimeline.entries.map { it.fact }
            .filterIsInstance<ActionFact.RedHerringChoice>()
            .lastOrNull()?.let { fact ->
                val expected = fact.selectedSeat?.let { game.cards[it - 1].name }
                require(game.mechanics.redHerring == expected) {
                    "Recovered Red Herring diverges from the canonical action timeline."
                }
            }
        history.actionTimeline.entries.map { it.fact }
            .filterIsInstance<ActionFact.DemonBluffChoice>()
            .lastOrNull()?.let { fact ->
                require(game.mechanics.demonBluffRoleNames == fact.roles.map { it.value }) {
                    "Recovered Demon bluffs diverge from the canonical action timeline."
                }
            }

        val observationLog = EpistemicObservationLog(history.epistemicObservations)
        ClocktowerSemanticHistoryMode.GLOBAL_V1.requireCompatible(
            actionTimeline = history.actionTimeline,
            observationLog = observationLog,
            nextTimelineGlobalSequence = history.nextTimelineGlobalSequence,
        )
        observationLog.records.forEach { observation ->
            require(observation.round in 1..game.round) {
                "Epistemic observation is outside the recovered game round."
            }
            observation.sourceSeat?.let { requireKnownSeat(it, playerCount) }
            observation.recipientSeats.forEach { requireKnownSeat(it, playerCount) }
            observation.proposition.recoveryReferencedSeats().forEach { requireKnownSeat(it, playerCount) }
            val binding = observation.timelineBinding
            if (binding is ObservationTimelineBinding.Global) {
                require(binding.point.round in 1..game.round) {
                    "Observation timeline point is outside the recovered game round."
                }
            }
        }
    }

    private fun deriveSafeReentry(game: RecoveryGame): RecoverySafeReentry = when (game.entryPoint) {
        RecoveryEntryPoint.PassPhone -> RecoverySafeReentry.PassPhone(game.currentDealIndex)
        RecoveryEntryPoint.RevealCard -> RecoverySafeReentry.RevealCard(game.currentDealIndex)
        RecoveryEntryPoint.Stable -> when (game) {
            is UndercoverRecovery -> RecoverySafeReentry.UndercoverGame
            is ClocktowerRecovery -> {
                val pendingKlutz = game.mechanics.pendingKlutzName != null
                RecoverySafeReentry.ClocktowerJudge(
                    phase = if (pendingKlutz) ClocktowerPhase.Day else game.position.phase,
                    nightStepIndex = game.position.nightStepIndex,
                    continuation = if (pendingKlutz) ClocktowerRecoveryContinuation.Klutz else null,
                )
            }
        }
    }

    private fun requireKnownName(name: String?, knownNames: Set<String>) {
        if (name != null) require(name in knownNames) { "Recovery references an unknown player '$name'." }
    }

    private fun requireKnownSeat(seat: Int, playerCount: Int) {
        require(seat in 1..playerCount) { "Recovery references unknown seat $seat." }
    }

    private fun InformationProposition.recoveryReferencedSeats(): Set<Int> = when (this) {
        is InformationProposition.RoleAt -> setOf(seat)
        is InformationProposition.ShownRoleAt -> setOf(seat)
        is InformationProposition.AlignmentAt -> setOf(seat)
        is InformationProposition.CharacterTypeAt -> setOf(seat)
        is InformationProposition.AliveAt -> setOf(seat)
        is InformationProposition.AbilityStateAt -> setOf(seat)
        is InformationProposition.RoleInPlay -> emptySet()
        is InformationProposition.PlayerCount -> emptySet()
        is InformationProposition.SetupProfile -> emptySet()
        is InformationProposition.AnyOf -> alternatives.flatMapTo(linkedSetOf()) { it.recoveryReferencedSeats() }
        is InformationProposition.AllOf -> propositions.flatMapTo(linkedSetOf()) { it.recoveryReferencedSeats() }
        is InformationProposition.Not -> proposition.recoveryReferencedSeats()
        is InformationProposition.NumericResult -> (listOf(sourceSeat) + subjectSeats).toSet()
        is InformationProposition.BooleanResult -> (listOf(sourceSeat) + subjectSeats).toSet()
        is InformationProposition.GrimoireState -> seats.mapTo(linkedSetOf()) { it.seat }
    }

    private val UNDERCOVER_ROLES = setOf(Role.Civilian, Role.Undercover, Role.Blank)
}
