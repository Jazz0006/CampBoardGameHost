package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.StorytellerCausalDecisionJournalV1
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInput
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInputJsonCodec
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayMaterialization
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
    val rulesetBasis: ClocktowerRulesetPersistenceBasis,
    val rulesetRef: RulesetRef?,
    val sdeReplayMaterialization: SdeHistoricalReplayMaterialization? = null,
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

        // Apply the *setup* shown-identity contract at Recovery ingress, not only
        // when some later Virgin action happens to reference the Drunk. Both TB
        // and NGJ permit a Drunk to be SHOWN an unused Townsfolk character, never
        // a role already in actual play. Do not manufacture a historical choice.
        val inPlayActualRoles = actualRoles.mapTo(linkedSetOf(), ClocktowerRole::enName)
        val allowedShownRoles = clocktowerRolesForScript(game.identity.script)
            .filter { it.team == ClocktowerTeam.Townsfolk }
            .mapTo(linkedSetOf(), ClocktowerRole::enName)
        game.cards.forEach { card ->
            if (card.clocktowerRole?.enName == "Drunk") {
                val shown = requireNotNull(card.clocktowerShownRole)
                require(shown.enName in allowedShownRoles && shown.enName !in inPlayActualRoles) {
                    "Recovered Drunk must be shown an unused Townsfolk from the actual selected script."
                }
            }
        }

        val mechanics = game.mechanics
        listOf(
            mechanics.confirmedAttackTarget,
            mechanics.confirmedPoisonTarget,
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
        // Validate the entire optional causal journal BEFORE creating a Ready restore plan.
        // Invalid corrections or old-prefix chronology must never fail halfway through UI apply.
        game.history.causalDecisionJournal?.let { archive ->
            require(game.identity.script == ClocktowerScript.TroubleBrewing || archive.records.isEmpty()) {
                "Only the current Trouble Brewing typed snapshot can own a causal decision journal."
            }
            StorytellerCausalDecisionJournalV1.restore(
                archive = archive,
                currentSession = ClocktowerSessionState(
                    gameId = game.identity.gameId,
                    gameStateRevision = game.history.gameStateRevision,
                    playerInputRevision = game.history.playerInputRevision,
                    gameSeed = game.identity.gameSeed,
                    gameState = game.cards.toClocktowerGameState(
                        script = game.identity.script,
                        seed = game.identity.gameSeed,
                        poisonedPlayerName = mechanics.confirmedPoisonTarget,
                    ),
                    storytellerPlayerContextBySeat = game.history.storytellerPlayerContextBySeat,
                    actionTimeline = game.history.actionTimeline,
                    epistemicObservationLog = EpistemicObservationLog(game.history.epistemicObservations),
                    semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                    nextTimelineGlobalSequence = game.history.nextTimelineGlobalSequence,
                ),
            )
        }

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
        val sdeReplayMaterialization = game.sdeHistoricalReplayInputJson?.let { raw ->
            val materialization = SdeHistoricalReplayInputJsonCodec.decodeStrict(raw)
            validateSdeReplayExport(
                game = game,
                input = materialization.input,
                resolvedRuleset = requireNotNull(resolvedRuleset) {
                    "Durable SDE replay requires a resolved current ruleset."
                },
            )
            materialization
        }

        return ValidatedClocktowerRecoveryRuntime(
            rulesetBasis = basis,
            rulesetRef = resolvedRuleset,
            sdeReplayMaterialization = sdeReplayMaterialization,
        )
    }

    private fun validateSdeReplayExport(
        game: ClocktowerRecovery,
        input: SdeHistoricalReplayInput,
        resolvedRuleset: RulesetRef,
    ) {
        require(input.gameId == game.identity.gameId) {
            "Durable SDE replay belongs to a different game."
        }
        require(input.gameStateRevision == game.history.gameStateRevision &&
            input.playerInputRevision == game.history.playerInputRevision
        ) {
            "Durable SDE replay revisions disagree with recovery."
        }
        require(input.rulesetRef == resolvedRuleset) {
            "Durable SDE replay ruleset disagrees with current recovery rules."
        }
        require(input.committedSetup.script == game.identity.script.toRecommendationScriptId() &&
            input.committedSetup.setupSeed == game.identity.gameSeed
        ) {
            "Durable SDE replay setup identity disagrees with recovery."
        }
        require(input.committedSetup.playerCount == game.cards.size) {
            "Durable SDE replay setup player count disagrees with recovery."
        }
        require(input.playerNamesBySeat == game.cards.map(PlayerCard::name)) {
            "Durable SDE replay player identities disagree with recovered cards."
        }
        require(input.actionTimeline == game.history.actionTimeline &&
            input.observationLog == EpistemicObservationLog(game.history.epistemicObservations) &&
            input.nextTimelineGlobalSequence == game.history.nextTimelineGlobalSequence
        ) {
            "Durable SDE replay semantic history disagrees with recovery."
        }
    }

    private fun validateSemanticHistory(game: ClocktowerRecovery) {
        val playerCount = game.cards.size
        val history = game.history

        history.actionTimeline.entries.forEach { entry ->
            require(entry.point.round in 1..game.round) {
                "Action timeline point is outside the recovered game round."
            }
            when (val fact = entry.fact) {
                is ActionFact.Poison -> fact.targetSeat?.let { requireKnownSeat(it, playerCount) }
                is ActionFact.Protect -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.Attack -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.Execution -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.Death -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.NoExecution -> require(
                    entry.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY
                ) { "No-execution can only be a confirmed Day outcome." }
                is ActionFact.SlayerShot -> {
                    requireKnownSeat(fact.claimantSeat, playerCount)
                    requireKnownSeat(fact.targetSeat, playerCount)
                    require(!fact.hit || fact.abilityConsumed)
                    require(entry.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY)
                    require(game.identity.script == ClocktowerScript.TroubleBrewing) {
                        "No Greater Joy has no Slayer ability and cannot carry a canonical SlayerShot."
                    }
                }
                is ActionFact.Nomination -> {
                    requireKnownSeat(fact.nominatorSeat, playerCount)
                    requireKnownSeat(fact.nomineeSeat, playerCount)
                    require(fact.nominatorSeat != fact.nomineeSeat)
                    require(entry.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY)
                    if (fact.firstVirginNomination) {
                        val nominee = game.cards[fact.nomineeSeat - 1]
                        // Match live onConfirmedNomination: a Drunk SHOWN Virgin interacts as
                        // Virgin and consumes the first-nomination opportunity, even though
                        // the actual Drunk ability cannot execute the nominator. Use the
                        // saved actual+shown role, not the player's later alive/poison state.
                        val perceivedAtInteraction = AbilityFunctioningSemantics.perceivedRole(
                            AbilitySubject(
                                actualRole = nominee.clocktowerRole?.enName,
                                shownRole = nominee.clocktowerShownRole?.enName,
                                isPoisoned = false,
                                isAlive = true,
                            ),
                        )
                        require(game.identity.script == ClocktowerScript.TroubleBrewing &&
                            perceivedAtInteraction == "Virgin") {
                            "First Virgin nomination requires actual or Drunk-shown Virgin in TB."
                        }
                    }
                }
                is ActionFact.Vote -> {
                    requireKnownSeat(fact.nominatorSeat, playerCount)
                    requireKnownSeat(fact.nomineeSeat, playerCount)
                    require(fact.nominatorSeat != fact.nomineeSeat)
                    require(fact.ghostVoterSeats.all { it in fact.voterSeats })
                    fact.voterSeats.forEach { requireKnownSeat(it, playerCount) }
                    require(entry.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY)
                }
                is ActionFact.KlutzLearnedDeath -> {
                    requireKnownSeat(fact.klutzSeat, playerCount)
                    require(fact.deathActionId.isNotBlank() && fact.functioningWhenLearned)
                    require(entry.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY)
                    require(game.identity.script == ClocktowerScript.NoGreaterJoy)
                }
                is ActionFact.KlutzChoice -> {
                    requireKnownSeat(fact.klutzSeat, playerCount)
                    requireKnownSeat(fact.chosenSeat, playerCount)
                    require(fact.klutzSeat != fact.chosenSeat && fact.learnedActionId.isNotBlank())
                    require(entry.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY)
                    require(game.identity.script == ClocktowerScript.NoGreaterJoy)
                }
                is ActionFact.RoleChange -> requireKnownSeat(fact.targetSeat, playerCount)
                is ActionFact.PhaseAdvance -> require(fact.round in 1..game.round) {
                    "Phase-advance fact is outside the recovered game round."
                }
            }
        }

        // Newly typed player-action events must retain their actual causal order in Recovery.
        // This does not infer a Storyteller ruling from a shot, nomination or voting result.
        val orderedDayActions = history.actionTimeline.entries.sortedBy { it.point.globalSequence }
        val klutzLearnedSeats = mutableSetOf<Int>()
        val klutzChosenSeats = mutableSetOf<Int>()
        val klutzLearnedActions = mutableMapOf<String, com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact>()
        val noExecutionRounds = mutableSetOf<Int>()
        val consumedSlayerSeats = mutableSetOf<Int>()
        val firstVirginNominees = mutableSetOf<Int>()
        val unvotedNominations = mutableListOf<com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact>()
        orderedDayActions.forEach { entry ->
            when (val fact = entry.fact) {
                is ActionFact.NoExecution -> {
                    require(noExecutionRounds.add(entry.point.round)) {
                        "A day cannot be confirmed as no-execution twice."
                    }
                    require(orderedDayActions.none { other ->
                        other.point.round == entry.point.round &&
                            other.fact is ActionFact.Execution
                    }) { "No-execution contradicts a same-day confirmed execution." }
                    require(orderedDayActions.none { other ->
                        other.point.round == entry.point.round &&
                            other.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY &&
                            other.point.globalSequence > entry.point.globalSequence &&
                            (other.fact is ActionFact.Nomination ||
                                other.fact is ActionFact.Vote ||
                                other.fact is ActionFact.SlayerShot)
                    }) { "No player day actions may follow confirmed end-of-day no-execution." }
                }
                is ActionFact.KlutzLearnedDeath -> {
                    require(klutzLearnedSeats.add(fact.klutzSeat)) {
                        "One Klutz cannot have two learned-death actions."
                    }
                    val predecessor = orderedDayActions.firstOrNull { earlier ->
                        earlier.point.globalSequence < entry.point.globalSequence &&
                            earlier.fact.actionId == fact.deathActionId &&
                            when (val death = earlier.fact) {
                                is ActionFact.Death -> death.targetSeat == fact.klutzSeat &&
                                    death.klutzDeathTrigger?.actualRole?.value == "Klutz"
                                is ActionFact.Execution -> death.targetSeat == fact.klutzSeat &&
                                    death.klutzDeathTrigger?.actualRole?.value == "Klutz"
                                else -> false
                            }
                    }
                    require(predecessor != null) {
                        "Klutz learned-death requires an earlier canonical real Klutz death."
                    }
                    require(game.cards[fact.klutzSeat - 1].clocktowerRole?.enName == "Klutz")
                    klutzLearnedActions[fact.actionId] = entry
                }
                is ActionFact.KlutzChoice -> {
                    require(klutzChosenSeats.add(fact.klutzSeat)) {
                        "Klutz player choice cannot be committed twice."
                    }
                    val learn = requireNotNull(klutzLearnedActions[fact.learnedActionId]) {
                        "Klutz choice requires prior known learned-of-death action."
                    }
                    require(learn.point.globalSequence < entry.point.globalSequence)
                    require((learn.fact as ActionFact.KlutzLearnedDeath).klutzSeat == fact.klutzSeat)
                    require(orderedDayActions.none { prior ->
                        prior.point.globalSequence < entry.point.globalSequence &&
                            ((prior.fact as? ActionFact.Death)?.targetSeat == fact.chosenSeat ||
                                (prior.fact as? ActionFact.Execution)?.targetSeat == fact.chosenSeat)
                    }) { "Klutz must select a living player at choice time." }
                }
                is ActionFact.SlayerShot -> {
                    if (fact.abilityConsumed) {
                        require(consumedSlayerSeats.add(fact.claimantSeat)) {
                            "One actual Slayer ability cannot be spent twice."
                        }
                    }
                    if (fact.hit) {
                        require(orderedDayActions.any { subsequent ->
                            subsequent.point.globalSequence > entry.point.globalSequence &&
                                (subsequent.fact as? ActionFact.Death)?.targetSeat == fact.targetSeat
                        }) { "Confirmed Slayer hit must precede its canonical Death." }
                    }
                }
                is ActionFact.Nomination -> {
                    if (fact.firstVirginNomination) {
                        require(firstVirginNominees.add(fact.nomineeSeat)) {
                            "The same Virgin cannot have two first nominations."
                        }
                    }
                    unvotedNominations.add(entry)
                }
                is ActionFact.Vote -> {
                    val matching = unvotedNominations.indexOfFirst { nomination ->
                        val previous = nomination.fact as ActionFact.Nomination
                        previous.nominatorSeat == fact.nominatorSeat &&
                            previous.nomineeSeat == fact.nomineeSeat &&
                            nomination.point.round == entry.point.round
                    }
                    require(matching >= 0) { "Vote must follow an unconsumed confirmed nomination." }
                    unvotedNominations.removeAt(matching)
                }
                else -> Unit
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
