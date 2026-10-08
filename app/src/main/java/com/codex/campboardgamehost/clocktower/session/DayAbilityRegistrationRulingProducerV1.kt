package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/**
 * A day ability ruling is a separate confirmed action, not a private information observation.
 * C2C-1C first production vertical: an actual functioning Slayer hits a healthy Recluse that
 * the Storyteller EXPLICITLY chooses to register as the Imp. Untouched/false UI selection never
 * creates a special-registration fact or an invented 'actual' ruling.
 */
internal data class ConfirmedDaySlayerRegistrationV1(
    val interactionId: String,
    val slayerSeat: Int,
    val recluseSeat: Int,
    val registeredDemonRole: RoleId,
) {
    init {
        require(interactionId.isNotBlank())
        require(slayerSeat > 0 && recluseSeat > 0 && slayerSeat != recluseSeat)
    }
}

internal object DayAbilityRegistrationRulingProducerV1 {
    fun confirmSlayer(
        session: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
        snapshot: TroubleBrewingGameSnapshotV1,
        legalRoles: List<RoleDefinition>,
        input: ConfirmedDaySlayerRegistrationV1,
    ): StorytellerProviderPriorDecisionV1 {
        val state = session.state
        require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(snapshot.gameId == state.gameId && snapshot.gameSeed == state.gameSeed)
        require(snapshot.script == ScriptId("trouble_brewing"))
        require((snapshot.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value == StorytellerPhase.DAY)
        require((snapshot.position.gameStateRevision as? SnapshotField.Known<Long>)?.value == state.gameStateRevision)
        require((snapshot.position.playerInputRevision as? SnapshotField.Known<Long>)?.value == state.playerInputRevision)
        val actor = state.gameState.players.single { it.seat == input.slayerSeat }
        val subject = state.gameState.players.single { it.seat == input.recluseSeat }
        require(actor.actualRole == RoleId("Slayer") && actor.alive && !actor.poisoned) {
            "Only an actual, living, functioning Slayer can confirm this day ability ruling."
        }
        require(subject.actualRole == RoleId("Recluse") && subject.alive && !subject.poisoned) {
            "The subject must be a living, functioning Recluse before the Slayer outcome."
        }
        val legal = TroubleBrewingRegistrationDomain.resolve(
            TroubleBrewingRegistrationSubject.from(subject),
            legalRoles,
            RegistrationQuestion.DEMON,
        ).special.filter { candidate ->
            candidate.registeredType == CharacterType.DEMON &&
                candidate.registeredRole == input.registeredDemonRole
        }
        require(legal.size == 1) {
            "Selected Demon registration is not an exact rules-owned legal candidate."
        }
        val candidate = legal.single()
        val type = StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION
        val decisionId = "day-slayer-registration:${input.interactionId}:${input.slayerSeat}:${input.recluseSeat}"
        require(journal.archive().records.none {
            it is StorytellerCausalJournalRecordV1.Captured &&
                it.frozen.identity.decisionId == decisionId
        }) { "One irreversible Slayer use cannot be captured twice." }
        val request = StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                state.gameId, snapshot.script.value, type, decisionId,
            ),
            sourceRevision = StorytellerProviderRevisionV1(
                state.gameStateRevision, state.playerInputRevision,
            ),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = StorytellerProviderDecisionContextV1.DayAbilityRegistration(
                interactionId = input.interactionId,
                abilityRole = RoleId("Slayer"),
                actorSeat = actor.seat,
                subjectSeat = subject.seat,
                question = RegistrationQuestion.DEMON,
            ),
            legalCandidates = listOf(StorytellerProviderCandidateV1(
                "special:${candidate.registeredRole.value}",
                StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                    RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, candidate.registeredRole,
                ),
            )),
            gameContext = journal.contextForRequest(snapshot, state),
        )
        val frozen = journal.captureBeforeDecision(request, state)
        val event = StorytellerProviderPriorDecisionV1(
            eventId = "confirmed:$decisionId",
            gameStateRevision = frozen.revision.gameStateRevision,
            playerInputRevision = frozen.revision.playerInputRevision,
            selectedCandidateId = "special:${candidate.registeredRole.value}",
            selectedOutcome = DecisionOutcomeSnapshot(type, sortedMapOf(
                "interactionId" to input.interactionId,
                "abilityRole" to "Slayer",
                "actorSeat" to actor.seat.toString(),
                "subjectSeat" to subject.seat.toString(),
                "question" to RegistrationQuestion.DEMON.name,
                "status" to RegistrationResolutionStatusV1.EXPLICIT_SPECIAL.name,
                "registeredRoleId" to candidate.registeredRole.value,
            )),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = listOf(requireNotNull(candidate.registrationFact(
                input.interactionId, RegistrationQuestion.DEMON,
            ))),
        )
        journal.commit(decisionId, event)
        return event
    }

    /** The same strict check is run by journal.commit on live and restored current-format events. */
    fun validateCommitted(
        frozen: FrozenStorytellerDecisionPrefixV1,
        event: StorytellerProviderPriorDecisionV1,
    ) {
        val type = StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION
        require(frozen.identity.decisionTypeId == type && event.selectedOutcome.decisionType == type)
        val f = event.selectedOutcome.canonicalFields
        require(f.keys == setOf(
            "interactionId", "abilityRole", "actorSeat", "subjectSeat", "question", "status", "registeredRoleId",
        ))
        require(f.getValue("interactionId").isNotBlank())
        require(f.getValue("abilityRole") == "Slayer")
        require(f.getValue("question") == RegistrationQuestion.DEMON.name)
        require(f.getValue("status") == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL.name)
        require((frozen.snapshotIdentity.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value ==
            StorytellerPhase.DAY)
        val actorSeat = f.getValue("actorSeat").toInt()
        val subjectSeat = f.getValue("subjectSeat").toInt()
        require(actorSeat > 0 && subjectSeat > 0 && actorSeat != subjectSeat)
        val actor = frozen.snapshotIdentity.seats.single { it.seat == actorSeat }
        val subject = frozen.snapshotIdentity.seats.single { it.seat == subjectSeat }
        require((actor.actualRoleId as? SnapshotField.Known<String>)?.value == "Slayer")
        require((subject.actualRoleId as? SnapshotField.Known<String>)?.value == "Recluse")
        require((actor.alive as? SnapshotField.Known<Boolean>)?.value == true)
        require((subject.alive as? SnapshotField.Known<Boolean>)?.value == true)
        require((actor.poisoned as? SnapshotField.Known<Boolean>)?.value == false)
        require((subject.poisoned as? SnapshotField.Known<Boolean>)?.value == false)
        val selectedRole = RoleId(f.getValue("registeredRoleId"))
        require(selectedRole == RoleId("Imp")) {
            "Trouble Brewing Slayer's selected Demon must be the actual legal Demon identity."
        }
        require(event.selectedCandidateId == "special:${selectedRole.value}")
        require(event.selectedCandidateId in frozen.legalCandidateIds)
        require(event.abilityState == AbilityState.FUNCTIONING &&
            event.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE)
        require(event.registrations.size == 1)
        val registration = event.registrations.single()
        require(registration.interactionId == f.getValue("interactionId"))
        require(registration.subjectSeat == subjectSeat)
        require(registration.registrationQuestion == RegistrationQuestion.DEMON)
        require(registration.reason == RegistrationReason.RECLUSE_ABILITY)
        require(registration.registeredRole == selectedRole)
        require(registration.registeredType == CharacterType.DEMON)
        require(registration.registeredAlignment == Alignment.EVIL)
    }
}
