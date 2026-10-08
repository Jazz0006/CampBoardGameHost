package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/**
 * A Klutz choice happens after the Klutz died. The current player state cannot prove
 * whether the Klutz was poisoned at the trigger moment, because death clears poison.
 * Only an explicitly GLOBAL death/Execution followed back to a known poison state
 * can support a durable, historical registration ruling.
 */
internal data class ConfirmedDayKlutzSpyRegistrationV1(
    val interactionId: String,
    val klutzSeat: Int,
    val chosenSpySeat: Int,
    val spyRegistersGood: Boolean,
) {
    init {
        require(interactionId.isNotBlank())
        require(klutzSeat > 0 && chosenSpySeat > 0 && klutzSeat != chosenSpySeat)
    }
}

internal data class KlutzDeathTriggerProvenanceV1(
    val deathActionId: String,
    val deathSequence: Long,
    val lastPoisonActionId: String? = null,
    val lastPoisonSequence: Long? = null,
    val deathTriggerRevision: Long? = null,
) {
    val isVerifiedDeathSnapshot: Boolean get() = deathTriggerRevision != null
}

/** Pure proof shared by live writer and strict historical Recovery validation. */
internal object KlutzDeathTriggerProvenanceResolverV1 {
    fun verified(
        actions: List<ActionFact>,
        klutzSeat: Int,
    ): KlutzDeathTriggerProvenanceV1? {
        val ordered = actions.sortedBy { it.sequence }
        val death = ordered.lastOrNull {
            when (it) {
                is ActionFact.Death -> it.targetSeat == klutzSeat
                is ActionFact.Execution -> it.targetSeat == klutzSeat
                else -> false
            }
        } ?: return null
        // New canonical death facts have their actual predeath condition stamped by
        // ClocktowerGameSession. Never override a known poisoned-at-death value using
        // an older poison target that may be stale or incomplete.
        val predeath = when (death) {
            is ActionFact.Death -> death.klutzDeathTrigger
            is ActionFact.Execution -> death.klutzDeathTrigger
            else -> null
        }
        if (predeath != null) {
            if (!predeath.functioningAtDeath || !predeath.wasAlive ||
                predeath.actualRole != RoleId("Klutz")) return null
            return KlutzDeathTriggerProvenanceV1(
                deathActionId = death.actionId,
                deathSequence = death.sequence,
                deathTriggerRevision = predeath.sourceGameStateRevision,
            )
        }
        // Historical 1C3A format with no predeath snapshot: retain its narrower
        // positively proven prior-poison contract; absence remains UNKNOWN.
        val poison = ordered.lastOrNull {
            it is ActionFact.Poison && it.sequence < death.sequence
        } as? ActionFact.Poison ?: return null
        // An absent/corrupted poison prefix is UNKNOWN, not a proof of sobriety.
        if (poison.targetSeat == klutzSeat) return null
        return KlutzDeathTriggerProvenanceV1(
            death.actionId, death.sequence, poison.actionId, poison.sequence,
        )
    }
}

internal object KlutzSpyDayRegistrationProducerV1 {
    fun confirm(
        session: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
        snapshot: TroubleBrewingGameSnapshotV1,
        legalRoles: List<RoleDefinition>,
        input: ConfirmedDayKlutzSpyRegistrationV1,
    ): StorytellerProviderPriorDecisionV1 {
        val state = session.state
        require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(snapshot.gameId == state.gameId && snapshot.gameSeed == state.gameSeed)
        require(snapshot.script == ScriptId("trouble_brewing"))
        require((snapshot.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value == StorytellerPhase.DAY)
        require((snapshot.position.gameStateRevision as? SnapshotField.Known<Long>)?.value == state.gameStateRevision)
        require((snapshot.position.playerInputRevision as? SnapshotField.Known<Long>)?.value == state.playerInputRevision)
        val klutz = state.gameState.players.single { it.seat == input.klutzSeat }
        val spy = state.gameState.players.single { it.seat == input.chosenSpySeat }
        require(klutz.actualRole == RoleId("Klutz") && !klutz.alive) {
            "A Klutz registration ruling requires the actual already dead Klutz."
        }
        require(spy.actualRole == RoleId("Spy") && spy.alive && !spy.poisoned) {
            "The chosen Spy must be living and functioning at choice time."
        }
        val death = requireNotNull(KlutzDeathTriggerProvenanceResolverV1.verified(
            state.actionTimeline.reducerFacts(), klutz.seat,
        )) {
            "Klutz death-time ability state must be captured at death or positively proven by legacy poison chronology."
        }
        require(TroubleBrewingRegistrationDomain.resolve(
            TroubleBrewingRegistrationSubject.from(spy), legalRoles,
            RegistrationQuestion.ALIGNMENT,
        ).special.any { it.registeredAlignment == Alignment.GOOD }) {
            "A Spy-as-good alignment ruling requires a rule-owned legal candidate."
        }
        val status = if (input.spyRegistersGood) RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
            else RegistrationResolutionStatusV1.EXPLICIT_ACTUAL
        val candidateId = if (input.spyRegistersGood) "special:alignment:GOOD" else "actual"
        val decisionId = "day-klutz-registration:${input.interactionId}:${klutz.seat}:${spy.seat}"
        require(journal.archive().records.none {
            it is StorytellerCausalJournalRecordV1.Captured &&
                it.frozen.identity.decisionId == decisionId
        }) { "A resolved dying-player choice must not produce duplicate history." }
        val type = StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION
        val request = StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                state.gameId, snapshot.script.value, type, decisionId,
            ),
            sourceRevision = StorytellerProviderRevisionV1(
                state.gameStateRevision, state.playerInputRevision,
            ),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = StorytellerProviderDecisionContextV1.DayAbilityRegistration(
                input.interactionId, RoleId("Klutz"), klutz.seat, spy.seat,
                RegistrationQuestion.ALIGNMENT,
            ),
            legalCandidates = listOf(
                StorytellerProviderCandidateV1(
                    "actual", StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
                    ),
                ),
                StorytellerProviderCandidateV1(
                    "special:alignment:GOOD",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
                    ),
                ),
            ),
            gameContext = journal.contextForRequest(snapshot, state),
        )
        val frozen = journal.captureBeforeDecision(request, state)
        val registration = if (input.spyRegistersGood) listOf(RegistrationFact(
            interactionId = input.interactionId,
            subjectSeat = spy.seat,
            registeredRole = null,
            registeredType = null,
            registeredAlignment = Alignment.GOOD,
            registrationQuestion = RegistrationQuestion.ALIGNMENT,
            reason = RegistrationReason.SPY_ABILITY,
        )) else emptyList()
        val event = StorytellerProviderPriorDecisionV1(
            eventId = "confirmed:$decisionId",
            gameStateRevision = frozen.revision.gameStateRevision,
            playerInputRevision = frozen.revision.playerInputRevision,
            selectedCandidateId = candidateId,
            selectedOutcome = DecisionOutcomeSnapshot(type, sortedMapOf(
                "interactionId" to input.interactionId,
                "abilityRole" to "Klutz",
                "actorSeat" to klutz.seat.toString(),
                "subjectSeat" to spy.seat.toString(),
                "question" to RegistrationQuestion.ALIGNMENT.name,
                "status" to status.name,
                "registeredAlignment" to (if (input.spyRegistersGood) "GOOD" else ""),
                "evilWins" to (!input.spyRegistersGood).toString(),
                "deathActionId" to death.deathActionId,
                "deathSequence" to death.deathSequence.toString(),
                "lastPoisonActionId" to (death.lastPoisonActionId ?: ""),
                "lastPoisonSequence" to (death.lastPoisonSequence?.toString() ?: ""),
                "deathEvidenceKind" to (if (death.isVerifiedDeathSnapshot) "SESSION_TRIGGER" else "LEGACY_POISON"),
                "deathTriggerRevision" to (death.deathTriggerRevision?.toString() ?: ""),
            )),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = if (registration.isEmpty()) TruthRelation.NOT_APPLICABLE
                else TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = registration,
        )
        journal.commit(decisionId, event)
        return event
    }

    fun validateCommitted(
        frozen: FrozenStorytellerDecisionPrefixV1,
        event: StorytellerProviderPriorDecisionV1,
    ) {
        val type = StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION
        require(frozen.identity.decisionTypeId == type && event.selectedOutcome.decisionType == type)
        val f = event.selectedOutcome.canonicalFields
        val priorKeys = setOf(
            "interactionId", "abilityRole", "actorSeat", "subjectSeat", "question",
            "status", "registeredAlignment", "evilWins",
            "deathActionId", "deathSequence", "lastPoisonActionId", "lastPoisonSequence",
        )
        require(f.keys == priorKeys || f.keys == priorKeys +
            setOf("deathEvidenceKind", "deathTriggerRevision"))
        require(f.getValue("interactionId").isNotBlank())
        require(f.getValue("abilityRole") == "Klutz")
        require(f.getValue("question") == RegistrationQuestion.ALIGNMENT.name)
        require((frozen.snapshotIdentity.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value ==
            StorytellerPhase.DAY)
        val klutzSeat = f.getValue("actorSeat").toInt()
        val spySeat = f.getValue("subjectSeat").toInt()
        require(klutzSeat > 0 && spySeat > 0 && klutzSeat != spySeat)
        val klutz = frozen.snapshotIdentity.seats.single { it.seat == klutzSeat }
        val spy = frozen.snapshotIdentity.seats.single { it.seat == spySeat }
        require((klutz.actualRoleId as? SnapshotField.Known<String>)?.value == "Klutz")
        require((klutz.alive as? SnapshotField.Known<Boolean>)?.value == false)
        require((spy.actualRoleId as? SnapshotField.Known<String>)?.value == "Spy")
        require((spy.alive as? SnapshotField.Known<Boolean>)?.value == true)
        require((spy.poisoned as? SnapshotField.Known<Boolean>)?.value == false)
        val actions = frozen.historyPrefix.entries
            .filterIsInstance<StorytellerProviderHistoryEntryV1.Action>()
            .map { it.fact }
        val death = requireNotNull(KlutzDeathTriggerProvenanceResolverV1.verified(actions, klutzSeat)) {
            "Recovered Klutz ruling has no actual death-time sobriety evidence."
        }
        require(f.getValue("deathActionId") == death.deathActionId)
        require(f.getValue("deathSequence").toLong() == death.deathSequence)
        if ("deathEvidenceKind" in f) {
            require(f.getValue("deathEvidenceKind") ==
                if (death.isVerifiedDeathSnapshot) "SESSION_TRIGGER" else "LEGACY_POISON")
            require(f.getValue("deathTriggerRevision") == (death.deathTriggerRevision?.toString() ?: ""))
            require(f.getValue("lastPoisonActionId") == (death.lastPoisonActionId ?: ""))
            require(f.getValue("lastPoisonSequence") == (death.lastPoisonSequence?.toString() ?: ""))
        } else {
            // Validated 1C3A decision archives retain their original exact schema.
            require(!death.isVerifiedDeathSnapshot)
            require(f.getValue("lastPoisonActionId") == death.lastPoisonActionId)
            require(f.getValue("lastPoisonSequence").toLong() == death.lastPoisonSequence)
        }
        val status = RegistrationResolutionStatusV1.valueOf(f.getValue("status"))
        require(status == RegistrationResolutionStatusV1.EXPLICIT_ACTUAL ||
            status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL)
        val special = status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
        require(f.getValue("registeredAlignment") == if (special) "GOOD" else "")
        require(f.getValue("evilWins") == (!special).toString())
        require(event.selectedCandidateId == if (special) "special:alignment:GOOD" else "actual")
        require(event.selectedCandidateId in frozen.legalCandidateIds)
        require(event.abilityState == AbilityState.FUNCTIONING)
        if (special) {
            require(event.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE)
            val fact = event.registrations.single()
            require(fact.interactionId == f.getValue("interactionId"))
            require(fact.subjectSeat == spySeat && fact.reason == RegistrationReason.SPY_ABILITY)
            require(fact.registrationQuestion == RegistrationQuestion.ALIGNMENT)
            require(fact.registeredAlignment == Alignment.GOOD)
            require(fact.registeredType == null && fact.registeredRole == null)
        } else {
            require(event.truthRelation == TruthRelation.NOT_APPLICABLE)
            require(event.registrations.isEmpty())
        }
    }
}
