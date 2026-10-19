package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact
import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightCursor
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.ClocktowerOptionalNightSourceChronology
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint

/**
 * Immutable, non-Compose projection of the mechanical state consumed by the Clocktower host UI.
 * Durable mechanics come from Session GameState; cards contain presentation details only.
 */
internal class ClocktowerNightHostProjection internal constructor(
    private val cards: List<PlayerCard>,
    private val gameState: GameState,
    private val phase: ClocktowerPhase,
    private val poisonTarget: String?,
    val canonicalNightDeathResolution: TroubleBrewingDawnDeathResolution,
    val mayorTarget: PlayerCard?,
    val mayorRedirectTargetCards: List<PlayerCard>,
    val ravenkeeperTrigger: PlayerCard?,
    val currentDemonHostContext: CurrentDemonHostContext?,
    val sageNightDeath: PlayerCard?,
    val otherNightInteractions: List<ClocktowerHostInteraction>,
) {
    val mayorCanRedirect: Boolean
        get() = canonicalNightDeathResolution.mayorRedirectEligible

    /** Derived from already confirmed attack/protection choices, not a second writable roster.
     * A Mayor choice must resolve first; do not assume the Mayor is dead while it is pending.
     */
    val resolvedDeathForRemainingWakeups: String?
        get() = canonicalNightDeathResolution.resolvedDeathName.takeUnless {
            canonicalNightDeathResolution.mayorRedirectEligible &&
                canonicalNightDeathResolution.facts.mayorSeat != null &&
                canonicalNightDeathResolution.resolvedDeathSeat == canonicalNightDeathResolution.facts.mayorSeat
        }

    val demonCard: PlayerCard?
        get() = currentDemonHostContext?.actor

    val demonPoisonedForActionExplanation: Boolean
        get() = currentDemonHostContext?.isPoisoned == true

    val otherNightCanonicalInteractionIds: List<ClocktowerInteractionId>
        get() = otherNightInteractions.map { it.id }

    // Menus use confirmed Session state. Attack/successor choices do not change alive or roles.
    val chambermaidTargetCards: List<PlayerCard>
        get() = cards.filterIndexed { index, _ -> gameState.playerAt(index + 1)?.alive == true }

    val ravenkeeperDeathTriggerAbilityState: AbilityFunctioningState? by lazy {
        deathTriggerAbilityState("Ravenkeeper", ravenkeeperTrigger)
    }

    val sageDeathTriggerAbilityState: AbilityFunctioningState? by lazy {
        deathTriggerAbilityState("Sage", sageNightDeath)
    }

    fun effectivePoisonTargetAt(
        interactionId: ClocktowerInteractionId,
        boundary: ClocktowerInteractionBoundary,
    ): String? {
        val source = actualClocktowerRoleCards(cards, "Poisoner").firstOrNull() ?: return null
        val sourceSeat = cards.indexOf(source).plus(1).takeIf { it > 0 } ?: return null
        val cursor = ClocktowerEffectiveNightCursor(interactionId, boundary)
        val sourceInteractionId = ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        if (!ClocktowerOptionalNightSourceChronology.hasActedBy(
                canonicalInteractionIds = otherNightCanonicalInteractionIds,
                cursor = cursor,
                sourceInteractionId = sourceInteractionId,
            )
        ) return null
        val sourceFunctioning =
            gameState.playerAt(sourceSeat)?.actualRole == RoleId("Poisoner") &&
                AbilityFunctioningSemantics.functionsAs(
                    source.abilitySubject(null).copy(
                        isAlive = gameState.playerAt(sourceSeat)?.alive == true,
                    ),
                    "Poisoner",
                )
        return PoisonEffectLifecycle.effectiveTarget(
            poisonTarget,
            true,
            sourceFunctioning,
        )
    }

    fun effectiveAbilitySubjectForRole(enName: String, actor: PlayerCard?): AbilitySubject? {
        if (actor == null) return null
        val seat = cards.indexOf(actor).plus(1).takeIf { it > 0 } ?: return null
        val player = gameState.playerAt(seat) ?: return null
        return actor.abilitySubject(effectivePoisonForRole(enName)).copy(
            actualRole = player.actualRole.value,
            shownRole = player.shownRole?.value,
            isAlive = player.alive,
        )
    }

    fun effectivePoisonForRole(enName: String): String? =
        if (phase != ClocktowerPhase.Night) {
            poisonTarget
        } else {
            effectivePoisonTargetAt(
                ClocktowerProductionNightStepIdentity.role(RoleId(enName))
                    .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT),
                ClocktowerInteractionBoundary.BEFORE,
            )
        }

    // The trigger is still a pending UI preview. It must not synthesize a death/role event
    // or treat a selected successor as a committed role change.
    private fun deathTriggerAbilityState(
        roleEnName: String,
        triggerActor: PlayerCard?,
    ): AbilityFunctioningState? = effectiveAbilitySubjectForRole(roleEnName, triggerActor)
        ?.let { subject -> AbilityFunctioningSemantics.stateFor(subject, roleEnName) }

}

internal object ClocktowerNightHostProjectionFactory {
    fun project(
        cards: List<PlayerCard>,
        gameState: GameState,
        ruleset: ValidatedClocktowerRuleset,
        phase: ClocktowerPhase,
        poisonTarget: String?,
        checkpoint: ClocktowerNightCheckpoint,
        pendingNightNewDemonIdentityName: String?,
        lastExecutedName: String?,
    ): ClocktowerNightHostProjection {
        val canonicalNightDeathResolution = resolveTroubleBrewingDawnDeathResolution(
            cards = cards,
            gameState = gameState,
            checkpoint = checkpoint,
        )
        val mayorCanRedirect = canonicalNightDeathResolution.mayorRedirectEligible
        val mayorTarget = canonicalNightDeathResolution.facts.mayorSeat
            ?.let { targetSeat -> cards.getOrNull(targetSeat - 1) }
        val mayorRedirectTargetCards = cards.filter { card ->
            card.name != mayorTarget?.name
        }
        val resolvedNightDeathCard = canonicalNightDeathResolution.resolvedDeathName
            ?.let { name -> cards.firstOrNull { it.name == name } }
        val ravenkeeperTrigger = resolvedNightDeathCard?.takeIf {
            AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Ravenkeeper")
        }

        val currentDemonHostContext = resolveCurrentDemonHostContext(
            cards = cards,
            poisonedPlayerName = checkpoint.confirmedPoisonTarget,
        )
        val sageNightDeath = resolvedNightDeathCard?.takeIf {
            AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Sage")
        }
        val otherNightWakingRoleIds = clocktowerNightWakingRoleIds(
            gameState = gameState,
            includeDead = false,
        )
        val otherNightResolvedFacts = ClocktowerResolvedFlowFacts(
            buildSet {
                if (pendingNightNewDemonIdentityName != null) {
                    add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)
                }
                if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)
                if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)
                if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)
                if (sageNightDeath != null) add(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON)
            },
        )
        val otherNightInteractions = if (phase == ClocktowerPhase.Night) {
            ClocktowerProductionOtherNightFlow.interactions(
                ruleset = ruleset,
                playerCount = cards.size,
                wakingRoleIds = otherNightWakingRoleIds,
                resolvedFacts = otherNightResolvedFacts,
            )
        } else {
            emptyList()
        }
        return ClocktowerNightHostProjection(
            cards = cards,
            gameState = gameState,
            phase = phase,
            poisonTarget = poisonTarget,
            canonicalNightDeathResolution = canonicalNightDeathResolution,
            mayorTarget = mayorTarget,
            mayorRedirectTargetCards = mayorRedirectTargetCards,
            ravenkeeperTrigger = ravenkeeperTrigger,
            currentDemonHostContext = currentDemonHostContext,
            sageNightDeath = sageNightDeath,
            otherNightInteractions = otherNightInteractions,
        )
    }
}
