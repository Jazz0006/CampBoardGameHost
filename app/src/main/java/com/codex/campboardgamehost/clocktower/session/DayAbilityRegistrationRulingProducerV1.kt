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
    val registeredDemonRole: RoleId?, // null is explicit ACTUAL, not an untouched choice
) {
    init {
        require(interactionId.isNotBlank())
        require(slayerSeat > 0 && recluseSeat > 0 && slayerSeat != recluseSeat)
    }
}

/**
 * A first nomination is the player's public action. This is ONLY the Storyteller's
 * deliberate ruling about whether the nominating Spy registers as Townsfolk.
 * Null means no explicit ruling and MUST NOT manufacture a type/witness.
 */
internal data class ConfirmedDayVirginSpyRegistrationV1(
    val interactionId: String,
    val virginSeat: Int,
    val spyNominatorSeat: Int,
    val registersAsTownsfolk: Boolean,
) {
    init {
        require(interactionId.isNotBlank())
        require(virginSeat > 0 && spyNominatorSeat > 0 && virginSeat != spyNominatorSeat)
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
        val candidate = if (input.registeredDemonRole == null) null else {
            require(legal.size == 1) {
                "Selected Demon registration is not an exact rules-owned legal candidate."
            }
            legal.single()
        }
        val candidateId = candidate?.let { "special:${it.registeredRole.value}" } ?: "actual"
        val status = if (candidate == null) RegistrationResolutionStatusV1.EXPLICIT_ACTUAL
            else RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
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
            legalCandidates = listOf(
                StorytellerProviderCandidateV1(
                    "actual",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
                    ),
                ),
            ) + listOfNotNull(candidate?.let {
                StorytellerProviderCandidateV1(
                    "special:${it.registeredRole.value}",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, it.registeredRole,
                    ),
                )
            }),
            gameContext = journal.contextForRequest(snapshot, state),
        )
        val frozen = journal.captureBeforeDecision(request, state)
        val event = StorytellerProviderPriorDecisionV1(
            eventId = "confirmed:$decisionId",
            gameStateRevision = frozen.revision.gameStateRevision,
            playerInputRevision = frozen.revision.playerInputRevision,
            selectedCandidateId = candidateId,
            selectedOutcome = DecisionOutcomeSnapshot(type, sortedMapOf(
                "interactionId" to input.interactionId,
                "abilityRole" to "Slayer",
                "actorSeat" to actor.seat.toString(),
                "subjectSeat" to subject.seat.toString(),
                "question" to RegistrationQuestion.DEMON.name,
                "status" to status.name,
                "registeredRoleId" to (candidate?.registeredRole?.value ?: ""),
            )),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = if (candidate == null) TruthRelation.NOT_APPLICABLE
                else TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = candidate?.let { listOf(requireNotNull(it.registrationFact(
                input.interactionId, RegistrationQuestion.DEMON,
            ))) } ?: emptyList(),
        )
        journal.commit(decisionId, event)
        return event
    }

    /**
     * Public Virgin first-nomination adjudication. Character TYPE only: never invent the
     * Washerwoman (or any Townsfolk) role as a witness.
     * The confirmed execution Boolean is derived by Host and cross-checked here.
     */
    fun confirmVirginSpy(
        session: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
        snapshot: TroubleBrewingGameSnapshotV1,
        legalRoles: List<RoleDefinition>,
        input: ConfirmedDayVirginSpyRegistrationV1,
        confirmedExecution: Boolean,
    ): StorytellerProviderPriorDecisionV1 {
        val state = session.state
        require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(snapshot.gameId == state.gameId && snapshot.gameSeed == state.gameSeed)
        require(snapshot.script == ScriptId("trouble_brewing"))
        require((snapshot.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value == StorytellerPhase.DAY)
        require((snapshot.position.gameStateRevision as? SnapshotField.Known<Long>)?.value == state.gameStateRevision)
        require((snapshot.position.playerInputRevision as? SnapshotField.Known<Long>)?.value == state.playerInputRevision)
        val virgin = state.gameState.players.single { it.seat == input.virginSeat }
        val spy = state.gameState.players.single { it.seat == input.spyNominatorSeat }
        require(virgin.actualRole == RoleId("Virgin") && virgin.alive && !virgin.poisoned) {
            "Only the first nomination of a living, functioning real Virgin may carry this ruling."
        }
        require(spy.actualRole == RoleId("Spy") && spy.alive && !spy.poisoned) {
            "Only a healthy Spy nominator may use special Townsfolk-type registration."
        }
        require(confirmedExecution == input.registersAsTownsfolk) {
            "The public Virgin outcome must agree with the explicit Spy registration ruling."
        }
        val choices = TroubleBrewingRegistrationDomain.resolve(
            TroubleBrewingRegistrationSubject.from(spy),
            legalRoles,
            RegistrationQuestion.CHARACTER_TYPE,
        )
        require(choices.special.any { it.registeredType == CharacterType.TOWNSFOLK }) {
            "Townsfolk-type special registration must be rules-legal at confirmation."
        }
        val status = if (input.registersAsTownsfolk) RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
            else RegistrationResolutionStatusV1.EXPLICIT_ACTUAL
        val candidateId = if (input.registersAsTownsfolk) "special:type:TOWNSFOLK" else "actual"
        val decisionId = "day-virgin-registration:${input.interactionId}:${input.virginSeat}:${spy.seat}"
        require(journal.archive().records.none {
            it is StorytellerCausalJournalRecordV1.Captured &&
                it.frozen.identity.decisionId == decisionId
        }) { "The same Virgin first-nomination ruling cannot be committed twice." }
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
                input.interactionId, RoleId("Virgin"), virgin.seat, spy.seat,
                RegistrationQuestion.CHARACTER_TYPE,
            ),
            legalCandidates = listOf(
                StorytellerProviderCandidateV1(
                    "actual", StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
                    ),
                ),
                StorytellerProviderCandidateV1(
                    "special:type:TOWNSFOLK",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
                    ),
                ),
            ),
            gameContext = journal.contextForRequest(snapshot, state),
        )
        val frozen = journal.captureBeforeDecision(request, state)
        val fact = if (input.registersAsTownsfolk) listOf(RegistrationFact(
            interactionId = input.interactionId,
            subjectSeat = spy.seat,
            registeredRole = null,
            registeredType = CharacterType.TOWNSFOLK,
            registeredAlignment = Alignment.GOOD,
            registrationQuestion = RegistrationQuestion.CHARACTER_TYPE,
            reason = RegistrationReason.SPY_ABILITY,
        )) else emptyList()
        val event = StorytellerProviderPriorDecisionV1(
            eventId = "confirmed:$decisionId",
            gameStateRevision = frozen.revision.gameStateRevision,
            playerInputRevision = frozen.revision.playerInputRevision,
            selectedCandidateId = candidateId,
            selectedOutcome = DecisionOutcomeSnapshot(type, sortedMapOf(
                "interactionId" to input.interactionId,
                "abilityRole" to "Virgin",
                "actorSeat" to virgin.seat.toString(),
                "subjectSeat" to spy.seat.toString(),
                "question" to RegistrationQuestion.CHARACTER_TYPE.name,
                "status" to status.name,
                "registeredType" to (if (input.registersAsTownsfolk) CharacterType.TOWNSFOLK.name else ""),
                "executeNominator" to confirmedExecution.toString(),
            )),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = if (fact.isNotEmpty()) TruthRelation.TRUE_TO_REGISTERED_STATE
                else TruthRelation.NOT_APPLICABLE,
            registrations = fact,
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
        if (f["abilityRole"] == "Virgin") {
            validateVirginCommitted(frozen, event)
            return
        }
        if (f["abilityRole"] == "Klutz") {
            LegacyKlutzSpyRegistrationRecoveryValidatorV1.validateCommitted(frozen, event)
            return
        }
        require(f.keys == setOf(
            "interactionId", "abilityRole", "actorSeat", "subjectSeat", "question", "status", "registeredRoleId",
        ))
        require(f.getValue("interactionId").isNotBlank())
        require(f.getValue("abilityRole") == "Slayer")
        require(f.getValue("question") == RegistrationQuestion.DEMON.name)
        val status = RegistrationResolutionStatusV1.valueOf(f.getValue("status"))
        require(status in setOf(
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
        ))
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
        require(event.abilityState == AbilityState.FUNCTIONING)
        if (status == RegistrationResolutionStatusV1.EXPLICIT_ACTUAL) {
            require(f.getValue("registeredRoleId").isEmpty())
            require(event.selectedCandidateId == "actual")
            require(event.selectedCandidateId in frozen.legalCandidateIds)
            require(event.truthRelation == TruthRelation.NOT_APPLICABLE && event.registrations.isEmpty())
        } else {
            val selectedRole = RoleId(f.getValue("registeredRoleId"))
            require(selectedRole == RoleId("Imp")) {
                "Trouble Brewing Slayer's selected Demon must be the actual legal Demon identity."
            }
            require(event.selectedCandidateId == "special:${selectedRole.value}")
            require(event.selectedCandidateId in frozen.legalCandidateIds)
            require(event.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE)
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

    private fun validateVirginCommitted(
        frozen: FrozenStorytellerDecisionPrefixV1,
        event: StorytellerProviderPriorDecisionV1,
    ) {
        val fields = event.selectedOutcome.canonicalFields
        require(fields.keys == setOf(
            "interactionId", "abilityRole", "actorSeat", "subjectSeat", "question",
            "status", "registeredType", "executeNominator",
        ))
        require(fields.getValue("interactionId").isNotBlank())
        require(fields.getValue("abilityRole") == "Virgin")
        require(fields.getValue("question") == RegistrationQuestion.CHARACTER_TYPE.name)
        require((frozen.snapshotIdentity.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value ==
            StorytellerPhase.DAY)
        val virginSeat = fields.getValue("actorSeat").toInt()
        val spySeat = fields.getValue("subjectSeat").toInt()
        require(virginSeat > 0 && spySeat > 0 && virginSeat != spySeat)
        val virgin = frozen.snapshotIdentity.seats.single { it.seat == virginSeat }
        val spy = frozen.snapshotIdentity.seats.single { it.seat == spySeat }
        require((virgin.actualRoleId as? SnapshotField.Known<String>)?.value == "Virgin")
        require((spy.actualRoleId as? SnapshotField.Known<String>)?.value == "Spy")
        require((virgin.alive as? SnapshotField.Known<Boolean>)?.value == true)
        require((spy.alive as? SnapshotField.Known<Boolean>)?.value == true)
        require((virgin.poisoned as? SnapshotField.Known<Boolean>)?.value == false)
        require((spy.poisoned as? SnapshotField.Known<Boolean>)?.value == false)
        val status = RegistrationResolutionStatusV1.valueOf(fields.getValue("status"))
        require(status in setOf(RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL))
        val special = status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
        require(fields.getValue("executeNominator") == special.toString())
        require(fields.getValue("registeredType") ==
            if (special) CharacterType.TOWNSFOLK.name else "")
        require(event.selectedCandidateId == if (special) "special:type:TOWNSFOLK" else "actual")
        require(event.selectedCandidateId in frozen.legalCandidateIds)
        require(event.abilityState == AbilityState.FUNCTIONING)
        if (special) {
            require(event.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE)
            require(event.registrations.size == 1)
            val fact = event.registrations.single()
            require(fact.interactionId == fields.getValue("interactionId"))
            require(fact.subjectSeat == spySeat &&
                fact.registrationQuestion == RegistrationQuestion.CHARACTER_TYPE)
            require(fact.registeredRole == null)
            require(fact.registeredType == CharacterType.TOWNSFOLK)
            require(fact.registeredAlignment == Alignment.GOOD)
            require(fact.reason == RegistrationReason.SPY_ABILITY)
        } else {
            require(event.truthRelation == TruthRelation.NOT_APPLICABLE)
            require(event.registrations.isEmpty())
        }
    }
}
