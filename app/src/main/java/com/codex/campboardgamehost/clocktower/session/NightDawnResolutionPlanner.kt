package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegality
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle

/**
 * SNE-7 typed planner seam.
 *
 * The planner remains pure: it returns checkpoint/continuation/commit intent and does not mutate
 * GameState or own durable timeline state.
 */
internal enum class NightResolutionContinuation {
    AWAIT_DEMON_SUCCESSOR,
    AWAIT_NEW_DEMON_IDENTITY,
    DAWN,
}

internal data class DawnRoleChangeIntent(
    val targetSeat: Int,
    val roleId: RoleId,
)

internal data class DawnDeathIntent(
    val targetSeat: Int,
)

internal data class DawnPoisonCarryIntent(
    val targetSeat: Int,
)

internal data class DawnCommitIntent(
    val roleChanges: List<DawnRoleChangeIntent> = emptyList(),
    val death: DawnDeathIntent? = null,
    val poisonCarry: DawnPoisonCarryIntent? = null,
)

internal data class NightDawnResolutionTransition(
    val checkpoint: ClocktowerNightCheckpoint,
    val continuation: NightResolutionContinuation,
    val dawnCommitIntent: DawnCommitIntent?,
    val outcomeEvaluationAllowed: Boolean,
)

internal data class NightDawnDeathResolutionInput(
    val originalDeathSeat: Int?,
    val mayorSeat: Int?,
    val mayorRedirectMayApply: Boolean,
    val attackOutcome: DemonNightAttackOutcome? = null,
    val demonSafeSeats: Set<Int> = emptySet(),
    val effectiveNightState: ClocktowerEffectiveNightState,
    val demonRoleIds: Set<RoleId>,
)

internal data class NightDawnPoisonResolutionInput(
    val poisonerSeat: Int,
    val poisonerRoleId: RoleId,
    val effectiveNightState: ClocktowerEffectiveNightState,
)

internal object NightDawnResolutionPlanner {
    @Suppress("UNUSED_PARAMETER")
    fun planDemonSuccession(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        successionResolution: DemonSuccessionResolution,
        demonRoleId: RoleId,
    ): NightDawnResolutionTransition {
        val confirmedName = checkpoint.confirmedDemonSuccessorTarget
        val confirmedSeat = confirmedName
            ?.let { name -> baseGameState.players.firstOrNull { it.name == name }?.seat }
        val confirmedChoiceIsLegal =
            successionResolution is DemonSuccessionResolution.Choice &&
                confirmedSeat != null &&
                confirmedSeat in successionResolution.targetSeats

        return if (confirmedChoiceIsLegal) {
            NightDawnResolutionTransition(
                checkpoint = checkpoint.copy(pendingNewDemonName = confirmedName),
                continuation = NightResolutionContinuation.AWAIT_NEW_DEMON_IDENTITY,
                dawnCommitIntent = null,
                outcomeEvaluationAllowed = false,
            )
        } else {
            NightDawnResolutionTransition(
                checkpoint = checkpoint,
                continuation = NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR,
                dawnCommitIntent = null,
                outcomeEvaluationAllowed = false,
            )
        }
    }

    fun confirmNewDemonIdentity(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        demonRoleId: RoleId,
        poisonResolutionInput: NightDawnPoisonResolutionInput? = null,
    ): NightDawnResolutionTransition {
        val pendingName = checkpoint.pendingNewDemonName
        val pendingSeat = pendingName
            ?.let { name -> baseGameState.players.firstOrNull { it.name == name }?.seat }

        if (pendingSeat == null) {
            return NightDawnResolutionTransition(
                checkpoint = checkpoint,
                continuation = NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR,
                dawnCommitIntent = null,
                outcomeEvaluationAllowed = false,
            )
        }

        val roleChange = DawnRoleChangeIntent(
            targetSeat = pendingSeat,
            roleId = demonRoleId,
        )
        val poisonCarry = poisonResolutionInput?.let { input ->
            val transactionRemovesPoisonerAbility =
                roleChange.targetSeat == input.poisonerSeat &&
                    roleChange.roleId != input.poisonerRoleId
            if (transactionRemovesPoisonerAbility) {
                null
            } else {
                planPoisonCarry(
                    baseGameState = baseGameState,
                    checkpoint = checkpoint,
                    input = input,
                )
            }
        }

        return NightDawnResolutionTransition(
            checkpoint = checkpoint.copy(
                pendingNewDemonName = null,
                pendingNightNewDemonIdentityName = null,
                demonSuccessorDraftTarget = null,
                confirmedDemonSuccessorTarget = null,
            ),
            continuation = NightResolutionContinuation.DAWN,
            dawnCommitIntent = DawnCommitIntent(
                roleChanges = listOf(roleChange),
                poisonCarry = poisonCarry,
            ),
            outcomeEvaluationAllowed = true,
        )
    }

    fun planValidatedNightDeath(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        input: NightDawnDeathResolutionInput,
    ): NightDawnResolutionTransition {
        val canonicalOriginalDeathSeat = when (input.attackOutcome) {
            DemonNightAttackOutcome.NO_DEATH -> null
            DemonNightAttackOutcome.TARGET_DIES,
            DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED,
            DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
            null,
            -> input.originalDeathSeat
        }
        val canonicalMayorRedirectMayApply = when (input.attackOutcome) {
            DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED -> true
            DemonNightAttackOutcome.NO_DEATH,
            DemonNightAttackOutcome.TARGET_DIES,
            DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
            -> false
            null -> input.mayorRedirectMayApply
        }
        val confirmedRedirectSeat = checkpoint.confirmedMayorRedirectTarget
            ?.let { name -> baseGameState.players.firstOrNull { it.name == name }?.seat }
        val confirmedRedirectIsLegal = confirmedRedirectSeat?.let { targetSeat ->
            MayorRedirectLegality.canReceiveRedirect(
                targetIsDemon = input.effectiveNightState.currentRoleId(targetSeat) in input.demonRoleIds,
            )
        } == true
        val redirectApplies =
            canonicalMayorRedirectMayApply &&
                canonicalOriginalDeathSeat != null &&
                canonicalOriginalDeathSeat == input.mayorSeat &&
                confirmedRedirectIsLegal
        val resolvedDeathSeat = if (redirectApplies) {
            confirmedRedirectSeat
                ?.takeIf(input.effectiveNightState::isMechanicallyAlive)
                ?.takeUnless { it in input.demonSafeSeats }
        } else {
            canonicalOriginalDeathSeat
        }

        return NightDawnResolutionTransition(
            checkpoint = checkpoint,
            continuation = NightResolutionContinuation.DAWN,
            dawnCommitIntent = DawnCommitIntent(
                death = resolvedDeathSeat?.let(::DawnDeathIntent),
            ),
            outcomeEvaluationAllowed = true,
        )
    }

    fun planPoisonCarry(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        input: NightDawnPoisonResolutionInput,
    ): DawnPoisonCarryIntent? {
        val sourceStillOwnsPoisonerAbility =
            input.effectiveNightState.isMechanicallyAlive(input.poisonerSeat) &&
                input.effectiveNightState.currentRoleId(input.poisonerSeat) == input.poisonerRoleId
        val carriedTargetName = PoisonEffectLifecycle.afterNight(
            target = checkpoint.confirmedPoisonTarget,
            poisonerAlive = sourceStillOwnsPoisonerAbility,
        )
        val targetSeat = carriedTargetName
            ?.let { name -> baseGameState.players.firstOrNull { it.name == name }?.seat }
        return targetSeat?.let(::DawnPoisonCarryIntent)
    }
}
