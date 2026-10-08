package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/** Synthetic, UNREACHABLE TB Klutz/Spy fixture retained solely to prove archived
 * 1C3A/1C3B causal records remain decodable after removing their production writer.
 * NEVER interpret tests using this fixture as a production role/script combination.
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

}
