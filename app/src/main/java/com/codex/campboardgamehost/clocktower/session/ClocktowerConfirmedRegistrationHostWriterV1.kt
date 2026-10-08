package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerConfirmedRegistrationPublicationV1
import com.codex.campboardgamehost.isRulesConsistent
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RegistrationResolutionStatusV1
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility

/**
 * Production Host writer called ONLY after an actual displayed information record was committed.
 * Pure UI planning cannot mutate the journal; the engine/rules and durable observation authorize
 * both the first confirmation and a later explicit correction of the *same* displayed result.
 */
internal object ClocktowerConfirmedRegistrationHostWriterV1 {
    fun commit(
        publication: ClocktowerConfirmedRegistrationPublicationV1,
        session: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
        snapshot: TroubleBrewingGameSnapshotV1,
        legalRoles: List<RoleDefinition>,
    ): List<StorytellerProviderPriorDecisionV1> {
        val state = session.state
        if (state.semanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1 ||
            snapshot.gameId != state.gameId || snapshot.gameSeed != state.gameSeed
        ) return emptyList()
        val observation = state.epistemicObservationLog.records.singleOrNull {
            it.recordId == publication.observationRecordId
        } ?: return emptyList()
        val timeline = observation.timelineBinding as? ObservationTimelineBinding.Global
            ?: return emptyList()
        if (timeline.point.globalSequence != state.nextTimelineGlobalSequence - 1L ||
            observation.sourceSeat != publication.sourceSeat ||
            observation.proposition != publication.shownProposition ||
            observation.visibility != ObservationVisibility.PRIVATE ||
            observation.sourceAbility == null ||
            observation.reliability != ObservationReliability.RECEIVED_AS_FUNCTIONING
        ) return emptyList()

        // Fail BEFORE the first mutable capture/commit. Both special-registration legality and
        // consistency with a single complete result witness must hold for all selected subjects.
        require(publication.isRulesConsistent(state.gameState, legalRoles)) {
            "The confirmed ruling must match the rules-owned domain and a complete result witness."
        }

        val committed = mutableListOf<StorytellerProviderPriorDecisionV1>()
        publication.choices.forEach { choice ->
            val previous = journal.effectiveNow().lastOrNull { prior ->
                prior.selectedOutcome.decisionType == "registration-resolution" &&
                    prior.selectedOutcome.canonicalFields["interactionId"] == publication.interactionId &&
                    prior.selectedOutcome.canonicalFields["observationRecordId"] == publication.observationRecordId &&
                    prior.selectedOutcome.canonicalFields["subjectSeat"] == choice.subjectSeat.toString() &&
                    prior.selectedOutcome.canonicalFields["question"] == choice.question.name
            }
            // A missing UI toggle is NOT consent to erase a durable explicit ruling after
            // Compose state reset or Recovery. Positive unresolved is recorded only once;
            // replacing an existing ruling needs an actual explicit new selection.
            if (previous != null &&
                choice.status == RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED
            ) return@forEach
            if (previous?.selectedOutcome?.canonicalFields?.get("status") == choice.status.name &&
                previous.selectedOutcome.canonicalFields["selectedRoleId"] == choice.selectedRole?.value
            ) return@forEach

            val prefix = "registration:${publication.interactionId}:${publication.observationRecordId}:" +
                "${choice.subjectSeat}:${choice.question.name}"
            val ordinal = journal.archive().records.count { item ->
                item is StorytellerCausalJournalRecordV1.Captured &&
                    item.frozen.identity.decisionId.startsWith("$prefix:revision:")
            }
            val decisionId = "$prefix:revision:$ordinal"
            val event = StorytellerRegistrationRulingProducerV1.confirm(
                session = session,
                journal = journal,
                snapshot = snapshot,
                input = ConfirmedRegistrationResolutionInputV1(
                    interactionId = publication.interactionId,
                    observationRecordId = publication.observationRecordId,
                    sourceSeat = publication.sourceSeat,
                    subjectSeat = choice.subjectSeat,
                    question = choice.question,
                    status = choice.status,
                    selectedRoleId = choice.selectedRole,
                ),
                allowedRoles = legalRoles,
                decisionId = decisionId,
            )
            previous?.let { prior ->
                journal.correct(
                    correctionId = "revised:${event.eventId}",
                    replacedEventId = prior.eventId,
                    replacementEventId = event.eventId,
                )
            }
            committed += event
        }
        return committed
    }
}
