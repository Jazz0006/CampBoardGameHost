package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
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
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightStateProjector
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.ClocktowerOptionalNightSourceChronology
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegality
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.ResolvedNightMechanicalEvent
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.NightTransactionReconstruction
import com.codex.campboardgamehost.clocktower.session.NightTransactionRestoreComposition

/**
 * Immutable, non-Compose projection of the mechanical state consumed by the Clocktower host UI.
 * Durable authority remains the supplied cards and night checkpoint; this type only composes them.
 */
internal class ClocktowerNightHostProjection internal constructor(
    private val cards: List<PlayerCard>,
    private val phase: ClocktowerPhase,
    private val poisonTarget: String?,
    val canonicalNightDeathResolution: TroubleBrewingDawnDeathResolution,
    val mayorTarget: PlayerCard?,
    val mayorRedirectTargetCards: List<PlayerCard>,
    val resolvedNightDeathCard: PlayerCard?,
    val ravenkeeperTrigger: PlayerCard?,
    val currentDemonHostContext: CurrentDemonHostContext?,
    val demonSuccessorRoleId: RoleId?,
    val demonSuccessionResolution: DemonSuccessionResolution,
    val demonSuccessorTargetSeats: Set<Int>,
    val demonSuccessorTargetCards: List<PlayerCard>,
    val sageNightDeath: PlayerCard?,
    val otherNightWakingRoleIds: Set<RoleId>,
    val otherNightResolvedFacts: ClocktowerResolvedFlowFacts,
    val otherNightInteractions: List<ClocktowerHostInteraction>,
    val baseRoleIdsBySeat: Map<Int, RoleId>,
    val canonicalNightReconstruction: NightTransactionReconstruction?,
    val resolvedMechanicalEvents: List<ResolvedNightMechanicalEvent>,
) {
    val mayorCanRedirect: Boolean
        get() = canonicalNightDeathResolution.mayorRedirectEligible

    val resolvedNightDeathName: String?
        get() = canonicalNightDeathResolution.resolvedDeathName

    val demonCard: PlayerCard?
        get() = currentDemonHostContext?.actor

    val demonPoisonedForActionExplanation: Boolean
        get() = currentDemonHostContext?.isPoisoned == true

    val impSelfKillNeedsSuccessor: Boolean
        get() = demonSuccessorTargetSeats.isNotEmpty()

    val otherNightCanonicalInteractionIds: List<ClocktowerInteractionId>
        get() = otherNightInteractions.map { it.id }

    val chambermaidTargetCards: List<PlayerCard> by lazy {
        val interactionId = ClocktowerProductionNightStepIdentity
            .role(RoleId("Chambermaid"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        if (phase == ClocktowerPhase.Night && interactionId in otherNightCanonicalInteractionIds) {
            val state = effectiveNightStateAt(interactionId, ClocktowerInteractionBoundary.BEFORE)
            cards.filterIndexed { index, _ -> state.isMechanicallyAlive(index + 1) }
        } else {
            cards.filter { it.eliminatedRound == null }
        }
    }

    val ravenkeeperDeathTriggerAbilityState: AbilityFunctioningState? by lazy {
        deathTriggerAbilityState("Ravenkeeper", ravenkeeperTrigger)
    }

    val sageDeathTriggerAbilityState: AbilityFunctioningState? by lazy {
        deathTriggerAbilityState("Sage", sageNightDeath)
    }

    fun effectiveNightStateAt(
        interactionId: ClocktowerInteractionId,
        boundary: ClocktowerInteractionBoundary,
    ): ClocktowerEffectiveNightState = ClocktowerEffectiveNightStateProjector.projectAt(
        baseAliveSeats = cards.mapIndexedNotNull { index, card ->
            (index + 1).takeIf { card.eliminatedRound == null }
        }.toSet(),
        canonicalInteractionIds = otherNightCanonicalInteractionIds,
        confirmedEvents = resolvedMechanicalEvents,
        cursor = ClocktowerEffectiveNightCursor(interactionId, boundary),
        baseRoleIdsBySeat = baseRoleIdsBySeat,
    )

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
        val effectiveState = effectiveNightStateAt(interactionId, boundary)
        val sourceFunctioning =
            effectiveState.currentRoleId(sourceSeat) == RoleId("Poisoner") &&
                AbilityFunctioningSemantics.functionsAs(
                    source.abilitySubject(null).copy(
                        isAlive = effectiveState.isMechanicallyAlive(sourceSeat),
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
        val interactionId = ClocktowerProductionNightStepIdentity.role(RoleId(enName))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        if (phase != ClocktowerPhase.Night || interactionId !in otherNightCanonicalInteractionIds) {
            return actor.abilitySubject(poisonTarget)
        }
        val seat = cards.indexOf(actor).plus(1).takeIf { it > 0 }
            ?: return actor.abilitySubject(poisonTarget)
        val state = effectiveNightStateAt(interactionId, ClocktowerInteractionBoundary.BEFORE)
        return actor.abilitySubject(
            effectivePoisonTargetAt(
                interactionId,
                ClocktowerInteractionBoundary.BEFORE,
            ),
        ).copy(
            actualRole = state.currentRoleId(seat)?.value,
            isAlive = state.isMechanicallyAlive(seat),
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

    fun effectiveRoleForRegistration(enName: String, card: PlayerCard): RoleId? {
        if (phase != ClocktowerPhase.Night) return card.clocktowerRole?.enName?.let(::RoleId)
        val interactionId = ClocktowerProductionNightStepIdentity
            .role(RoleId(enName))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        val seat = cards.indexOf(card).plus(1)
        return if (interactionId !in otherNightCanonicalInteractionIds || seat <= 0) {
            null
        } else {
            effectiveNightStateAt(
                interactionId,
                ClocktowerInteractionBoundary.BEFORE,
            ).currentRoleId(seat)
        }
    }

    private fun deathTriggerAbilityState(
        roleEnName: String,
        triggerActor: PlayerCard?,
    ): AbilityFunctioningState? {
        if (triggerActor == null) return null
        val deathEvent = resolvedMechanicalEvents.singleOrNull()
            as? ResolvedNightMechanicalEvent.MechanicalDeath
            ?: return null
        val deathInteractionId = deathEvent.effectiveAt.interactionId
        val beforeDeathState = effectiveNightStateAt(
            deathInteractionId,
            ClocktowerInteractionBoundary.BEFORE,
        )
        val effectivePoison = effectivePoisonTargetAt(
            deathInteractionId,
            ClocktowerInteractionBoundary.BEFORE,
        )
        val seat = cards.indexOf(triggerActor).plus(1).takeIf { it > 0 } ?: return null
        val subject = triggerActor.abilitySubject(effectivePoison).copy(
            isAlive = beforeDeathState.isMechanicallyAlive(seat),
        )
        return AbilityFunctioningSemantics.stateFor(subject, roleEnName)
    }
}

internal object ClocktowerNightHostProjectionFactory {
    fun project(
        cards: List<PlayerCard>,
        script: ClocktowerScript,
        ruleset: ValidatedClocktowerRuleset,
        gameSeed: Long,
        phase: ClocktowerPhase,
        poisonTarget: String?,
        checkpoint: ClocktowerNightCheckpoint,
        pendingNightNewDemonIdentityName: String?,
        lastExecutedName: String?,
    ): ClocktowerNightHostProjection {
        val canonicalNightDeathResolution = resolveTroubleBrewingDawnDeathResolution(
            cards = cards,
            script = script,
            gameSeed = gameSeed,
            checkpoint = checkpoint,
        )
        val mayorCanRedirect = canonicalNightDeathResolution.mayorRedirectEligible
        val mayorTarget = canonicalNightDeathResolution.facts.mayorSeat
            ?.let { targetSeat -> cards.getOrNull(targetSeat - 1) }
        val mayorRedirectTargetCards = cards.filter { card ->
            card.name != mayorTarget?.name &&
                MayorRedirectLegality.canReceiveRedirect(
                    targetIsDemon = card.clocktowerTeam == ClocktowerTeam.Demon,
                )
        }
        val resolvedNightDeathCard = canonicalNightDeathResolution.resolvedDeathName
            ?.let { name -> cards.firstOrNull { it.name == name } }
        val nightDeathWillOccur = canonicalNightDeathResolution.resolvedDeathSeat != null
        val ravenkeeperTrigger = resolvedNightDeathCard?.takeIf {
            nightDeathWillOccur &&
                AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Ravenkeeper")
        }

        val currentDemonHostContext = resolveCurrentDemonHostContext(
            cards = cards,
            poisonedPlayerName = checkpoint.confirmedPoisonTarget,
        )
        val nightBaseGameState = cards.toClocktowerGameState(script, gameSeed, poisonTarget)
        val demonSuccessorRoleId = resolveNightReconstructionDemonRoleId(
            cards = cards,
            currentDemonHostContext = currentDemonHostContext,
            confirmedDemonAttackerName = checkpoint.confirmedAttackTarget,
        )
        val demonSuccessionResolution = if (phase == ClocktowerPhase.Night) {
            resolveNightDemonSuccessionForHost(
                baseGameState = nightBaseGameState,
                checkpoint = checkpoint,
                currentDemonHostContext = currentDemonHostContext,
                demonRoleId = demonSuccessorRoleId,
            )
        } else {
            DemonSuccessionResolution.None
        }
        val demonSuccessorTargetSeats = when (val resolution = demonSuccessionResolution) {
            DemonSuccessionResolution.None -> emptySet()
            is DemonSuccessionResolution.Forced -> setOf(resolution.targetSeat)
            is DemonSuccessionResolution.Choice -> resolution.targetSeats
        }
        val demonSuccessorTargetCards = cards.filterIndexed { index, _ ->
            index + 1 in demonSuccessorTargetSeats
        }
        val impSelfKillNeedsSuccessor = demonSuccessorTargetSeats.isNotEmpty()
        val sageNightDeath = resolvedNightDeathCard?.takeIf {
            nightDeathWillOccur &&
                AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Sage")
        }
        val otherNightWakingRoleIds = clocktowerOtherNightWakingRoleIds(
            cards = cards,
            pendingSuccessionDemonRoleId = demonSuccessorRoleId.takeIf { impSelfKillNeedsSuccessor },
        )
        val otherNightResolvedFacts = ClocktowerResolvedFlowFacts(
            buildSet {
                if (pendingNightNewDemonIdentityName != null) {
                    add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)
                }
                if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)
                if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)
                if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)
                if (impSelfKillNeedsSuccessor) add(ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED)
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
        val otherNightCanonicalInteractionIds = otherNightInteractions.map { it.id }
        val baseRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
            card.clocktowerRole?.enName?.let { roleName -> index + 1 to RoleId(roleName) }
        }.toMap()
        val demonSuccessorInteractionId = ClocktowerProductionNightStepIdentity.demonSuccessor()
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        val canonicalNightReconstruction = if (phase == ClocktowerPhase.Night) {
            NightTransactionRestoreComposition.compose(
                baseGameState = nightBaseGameState,
                checkpoint = checkpoint,
                canonicalInteractionIds = otherNightCanonicalInteractionIds,
                demonSuccessorInteractionId = demonSuccessorInteractionId,
                demonRoleId = requireNotNull(demonSuccessorRoleId) {
                    "Night transaction reconstruction requires a canonical Demon role."
                },
            )
        } else {
            null
        }
        val resolvedMechanicalEvents = buildList<ResolvedNightMechanicalEvent> {
            if (phase == ClocktowerPhase.Night && nightDeathWillOccur) {
                val targetSeat = cards.indexOf(resolvedNightDeathCard).plus(1)
                require(targetSeat > 0) { "Resolved night death must identify a valid target seat." }
                val effectiveInteractionId = if (mayorCanRedirect) {
                    ClocktowerProductionNightStepIdentity.mayorRedirect()
                        .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
                } else {
                    val demonRoleId = requireNotNull(demonSuccessorRoleId) {
                        "Resolved night death requires a canonical Demon interaction."
                    }
                    ClocktowerProductionNightStepIdentity.role(demonRoleId)
                        .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
                }
                add(
                    ResolvedNightMechanicalEvent.MechanicalDeath(
                        targetSeat = targetSeat,
                        effectiveAt = ClocktowerEffectiveNightCursor(
                            effectiveInteractionId,
                            ClocktowerInteractionBoundary.AFTER,
                        ),
                    ),
                )
            }
            if (phase == ClocktowerPhase.Night && canonicalNightReconstruction != null) {
                addAll(
                    canonicalNightReconstruction.confirmedEvents
                        .filterIsInstance<ResolvedNightMechanicalEvent.RoleChanged>(),
                )
            }
        }

        return ClocktowerNightHostProjection(
            cards = cards,
            phase = phase,
            poisonTarget = poisonTarget,
            canonicalNightDeathResolution = canonicalNightDeathResolution,
            mayorTarget = mayorTarget,
            mayorRedirectTargetCards = mayorRedirectTargetCards,
            resolvedNightDeathCard = resolvedNightDeathCard,
            ravenkeeperTrigger = ravenkeeperTrigger,
            currentDemonHostContext = currentDemonHostContext,
            demonSuccessorRoleId = demonSuccessorRoleId,
            demonSuccessionResolution = demonSuccessionResolution,
            demonSuccessorTargetSeats = demonSuccessorTargetSeats,
            demonSuccessorTargetCards = demonSuccessorTargetCards,
            sageNightDeath = sageNightDeath,
            otherNightWakingRoleIds = otherNightWakingRoleIds,
            otherNightResolvedFacts = otherNightResolvedFacts,
            otherNightInteractions = otherNightInteractions,
            baseRoleIdsBySeat = baseRoleIdsBySeat,
            canonicalNightReconstruction = canonicalNightReconstruction,
            resolvedMechanicalEvents = resolvedMechanicalEvents,
        )
    }
}
