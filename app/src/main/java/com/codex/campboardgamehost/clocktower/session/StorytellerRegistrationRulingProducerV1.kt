package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.referencedSeats
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/**
 * Only an explicitly confirmed special ruling is a RegistrationFact.
 * UNRESOLVED_NOT_REQUIRED is a positive statement attached to a confirmed observation.
 * Missing older provenance is UNAVAILABLE_OR_UNRECORDED, never an invented selection.
 */
internal enum class RegistrationResolutionStatusV1 {
    EXPLICIT_SPECIAL,
    EXPLICIT_ACTUAL,
    UNRESOLVED_NOT_REQUIRED,
    NOT_APPLICABLE,
    UNAVAILABLE_OR_UNRECORDED,
}

internal data class ConfirmedRegistrationResolutionInputV1(
    val interactionId: String,
    val observationRecordId: String,
    val sourceSeat: Int,
    val subjectSeat: Int,
    val question: RegistrationQuestion,
    val status: RegistrationResolutionStatusV1,
    val selectedRoleId: RoleId? = null,
) {
    init {
        require(interactionId.isNotBlank() && observationRecordId.isNotBlank())
        require(sourceSeat > 0 && subjectSeat > 0)
        require(status != RegistrationResolutionStatusV1.UNAVAILABLE_OR_UNRECORDED)
        require(status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL || selectedRoleId == null)
    }
}

/**
 * Narrow rules-owned confirmation seam. This is intentionally independent of Compose presentation:
 * callers MUST supply an already published, typed global observation and an actually confirmed
 * manual choice. It does not derive a witness from a displayed result.
 *
 * The observation occurs before the registration annotation; its global sequence is thus part of
 * the frozen *pre-ruling* prefix. In the next UI integration slice, the Host must also prove that
 * the selected explicit ruling is compatible with the final displayed result's legal witness set.
 */
internal object StorytellerRegistrationRulingProducerV1 {
    fun confirm(
        session: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
        snapshot: TroubleBrewingGameSnapshotV1,
        input: ConfirmedRegistrationResolutionInputV1,
        allowedRoles: List<RoleDefinition>,
        decisionId: String,
    ): StorytellerProviderPriorDecisionV1 {
        val state = session.state
        require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(snapshot.gameId == state.gameId && snapshot.gameSeed == state.gameSeed)
        require(decisionId.isNotBlank())
        val record = state.epistemicObservationLog.records.singleOrNull {
            it.recordId == input.observationRecordId
        } ?: error("A registration ruling requires the actual published observation.")
        require(record.timelineBinding is ObservationTimelineBinding.Global)
        require(record.visibility == ObservationVisibility.PRIVATE)
        require(record.sourceSeat == input.sourceSeat)
        require(record.sourceAbility != null)
        require(record.reliability != ObservationReliability.NOT_ABILITY_INFORMATION)
        val source = state.gameState.players.single { it.seat == input.sourceSeat }
        val player = state.gameState.players.single { it.seat == input.subjectSeat }
        val subject = TroubleBrewingRegistrationSubject.from(player)
        val resolution = TroubleBrewingRegistrationDomain.resolve(subject, allowedRoles, input.question)
        val isFunctioning = record.reliability == ObservationReliability.RECEIVED_AS_FUNCTIONING
        val special = if (isFunctioning) resolution.special else emptyList()
        val legal = buildList {
            if (special.isEmpty()) {
                add(StorytellerProviderCandidateV1(
                    "not-applicable",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.NOT_APPLICABLE,
                    ),
                ))
            } else {
                add(StorytellerProviderCandidateV1(
                    "unresolved",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
                    ),
                ))
                add(StorytellerProviderCandidateV1(
                    "actual",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
                    ),
                ))
                add(StorytellerProviderCandidateV1(
                    "special",
                    StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                        RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
                    ),
                ))
                if (input.question != RegistrationQuestion.ALIGNMENT) {
                    special.distinctBy { it.registeredRole }.forEach { candidate ->
                        add(StorytellerProviderCandidateV1(
                            "special:${candidate.registeredRole.value}",
                            StorytellerProviderCandidatePayloadV1.RegistrationChoice(
                                RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
                                candidate.registeredRole,
                            ),
                        ))
                    }
                }
            }
        }
        val candidateId = when (input.status) {
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL ->
                input.selectedRoleId?.let { "special:${it.value}" } ?: "special"
            RegistrationResolutionStatusV1.EXPLICIT_ACTUAL -> "actual"
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED -> "unresolved"
            RegistrationResolutionStatusV1.NOT_APPLICABLE -> "not-applicable"
            RegistrationResolutionStatusV1.UNAVAILABLE_OR_UNRECORDED ->
                error("Unknown historical registration cannot be confirmed.")
        }
        require(candidateId in legal.map { it.candidateId }) {
            "Selected registration is outside the rule-owned legal domain."
        }
        val context = StorytellerProviderDecisionContextV1.RegistrationResolution(
            input.interactionId, input.observationRecordId,
            input.sourceSeat, input.subjectSeat, input.question,
        )
        val request = StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                snapshot.gameId, snapshot.script.value, context.decisionTypeId, decisionId,
            ),
            sourceRevision = StorytellerProviderRevisionV1(
                state.gameStateRevision, state.playerInputRevision,
            ),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = context,
            legalCandidates = legal,
            gameContext = journal.contextForRequest(snapshot, state),
        )
        val frozen = journal.captureBeforeDecision(request, state)
        val registeredAlignment = when (subject.effectiveRole?.value) {
            "Spy" -> Alignment.GOOD
            "Recluse" -> Alignment.EVIL
            else -> null
        }
        val registrations = if (input.status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL) {
            val reason = requireNotNull(TroubleBrewingRegistrationDomain.specialReason(subject))
            listOf(RegistrationFact(
                interactionId = input.interactionId,
                subjectSeat = input.subjectSeat,
                registeredRole = input.selectedRoleId,
                registeredAlignment = requireNotNull(registeredAlignment),
                registrationQuestion = input.question,
                reason = reason,
            ))
        } else emptyList()
        val outcomeFields = sortedMapOf(
            "interactionId" to input.interactionId,
            "observationRecordId" to input.observationRecordId,
            "sourceSeat" to input.sourceSeat.toString(),
            "subjectSeat" to input.subjectSeat.toString(),
            "question" to input.question.name,
            "status" to input.status.name,
        )
        input.selectedRoleId?.let { outcomeFields["selectedRoleId"] = it.value }
        val event = StorytellerProviderPriorDecisionV1(
            eventId = "confirmed:$decisionId",
            gameStateRevision = frozen.revision.gameStateRevision,
            playerInputRevision = frozen.revision.playerInputRevision,
            selectedCandidateId = candidateId,
            selectedOutcome = DecisionOutcomeSnapshot(context.decisionTypeId, outcomeFields),
            abilityState = when {
                isFunctioning -> AbilityState.FUNCTIONING
                source.poisoned -> AbilityState.MALFUNCTIONING_POISONED
                source.actualRole.value == "Drunk" -> AbilityState.MALFUNCTIONING_DRUNK
                else -> AbilityState.MALFUNCTIONING_POISONED
            },
            truthRelation = if (registrations.isNotEmpty()) TruthRelation.TRUE_TO_REGISTERED_STATE
                else TruthRelation.NOT_APPLICABLE,
            registrations = registrations,
        )
        journal.commit(decisionId, event)
        return event
    }

    /** Strict current-format replay guard: facts must agree with the frozen registered subject. */
    fun validateCommitted(
        frozen: FrozenStorytellerDecisionPrefixV1,
        event: StorytellerProviderPriorDecisionV1,
    ) {
        require(frozen.identity.decisionTypeId == StorytellerProviderDecisionContextV1.REGISTRATION_RESOLUTION)
        require(event.selectedOutcome.decisionType == frozen.identity.decisionTypeId)
        val fields = event.selectedOutcome.canonicalFields
        require(fields.keys == (
            setOf("interactionId", "observationRecordId", "sourceSeat", "subjectSeat", "question", "status") +
                if ("selectedRoleId" in fields) setOf("selectedRoleId") else emptySet()
        ))
        val interactionId = fields.getValue("interactionId")
        val sourceSeat = fields.getValue("sourceSeat").toInt()
        val subjectSeat = fields.getValue("subjectSeat").toInt()
        val observationId = fields.getValue("observationRecordId")
        val question = RegistrationQuestion.valueOf(fields.getValue("question"))
        val status = RegistrationResolutionStatusV1.valueOf(fields.getValue("status"))
        val selectedRole = fields["selectedRoleId"]?.let(::RoleId)
        require(interactionId.isNotBlank() && observationId.isNotBlank())
        require(sourceSeat != subjectSeat && sourceSeat > 0 && subjectSeat > 0)
        require(status != RegistrationResolutionStatusV1.UNAVAILABLE_OR_UNRECORDED)
        val observation = frozen.historyPrefix.entries.singleOrNull {
            it.entryId == observationId
        } as? com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1.Observation
            ?: error("Registration outcome must reference an already frozen typed observation.")
        require(observation.sourceSeat == sourceSeat && observation.visibility == ObservationVisibility.PRIVATE)
        require(observation.sourceAbility != null)
        val subject = frozen.snapshotIdentity.seats.single { it.seat == subjectSeat }
        val subjectRole = (subject.actualRoleId as? SnapshotField.Known<String>)?.value
            ?: error("A confirmed registration requires a known frozen subject role.")
        val reason = when (subjectRole) {
            "Spy" -> RegistrationReason.SPY_ABILITY
            "Recluse" -> RegistrationReason.RECLUSE_ABILITY
            else -> null
        }
        val expectedAlignment = when (reason) {
            RegistrationReason.SPY_ABILITY -> Alignment.GOOD
            RegistrationReason.RECLUSE_ABILITY -> Alignment.EVIL
            else -> null
        }
        val expectedCandidate = when (status) {
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL ->
                selectedRole?.let { "special:${it.value}" } ?: "special"
            RegistrationResolutionStatusV1.EXPLICIT_ACTUAL -> "actual"
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED -> "unresolved"
            RegistrationResolutionStatusV1.NOT_APPLICABLE -> "not-applicable"
            RegistrationResolutionStatusV1.UNAVAILABLE_OR_UNRECORDED -> error("Unknown status is never committed.")
        }
        require(event.selectedCandidateId == expectedCandidate)
        if (status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL) {
            require(reason != null && expectedAlignment != null)
            require(event.registrations.size == 1)
            val fact = event.registrations.single()
            require(fact.interactionId == interactionId && fact.subjectSeat == subjectSeat)
            require(fact.reason == reason && fact.registrationQuestion == question)
            require(fact.registeredAlignment == expectedAlignment)
            require(fact.registeredRole == selectedRole && fact.registeredType == null)
            require(event.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE)
            require(event.abilityState == AbilityState.FUNCTIONING)
        } else {
            require(event.registrations.isEmpty() && selectedRole == null)
            require(event.truthRelation == TruthRelation.NOT_APPLICABLE)
        }
        require(event.selectedCandidateId in frozen.legalCandidateIds)
    }
}
