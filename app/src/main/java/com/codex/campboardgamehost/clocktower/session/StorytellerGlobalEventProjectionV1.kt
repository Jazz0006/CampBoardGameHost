package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryPrefixV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.json.JSONArray
import org.json.JSONObject

/**
 * A pure, non-authoritative LLM view of the existing session-owned chronological prefix.
 * No new event store or duplicate writer: mechanical actions and player observations retain
 * their separate meaning, exact global order, and explicit incomplete-coverage markers.
 */
internal object StorytellerGlobalEventProjectionV1 {
    fun encode(prefix: StorytellerProviderHistoryPrefixV1?): JSONObject {
        if (prefix == null) {
            return JSONObject().put("cutoffSource", "UNAVAILABLE")
                .put("coverage", "UNKNOWN").put("events", JSONArray())
        }
        val coverage = JSONObject()
        prefix.coverage.forEach { (dimension, status) ->
            coverage.put(dimension.name, JSONObject()
                .put("state", status.state.name).put("reasonCode", status.reasonCode))
        }
        return JSONObject()
            .put("cutoffSource", prefix.cutoffSource.name)
            .put("historyMode", prefix.historyMode.name)
            .put("exclusiveGlobalSequence", prefix.exclusiveGlobalSequence ?: JSONObject.NULL)
            .put("coverage", coverage)
            .put("events", JSONArray().also { array ->
                prefix.entries.forEach { entry ->
                    val event = JSONObject()
                        .put("eventId", entry.entryId)
                        .put("kind", entry.kind)
                        .put("phase", entry.point.phase.name)
                        .put("round", entry.point.round)
                        .put("localSequence", entry.point.sequence)
                        .put("globalSequence", entry.point.globalSequence)
                    when (entry) {
                        is StorytellerProviderHistoryEntryV1.Action -> {
                            event.put("epistemicClass", "HOST_CONFIRMED_ACTION")
                                .put("action", action(entry.fact))
                        }
                        is StorytellerProviderHistoryEntryV1.Observation -> {
                            event.put("epistemicClass", "PLAYER_RECEIVED_OR_PUBLIC_INFORMATION")
                                .put("sourceSeat", entry.sourceSeat ?: JSONObject.NULL)
                                .put("sourceAbility", entry.sourceAbility?.value ?: JSONObject.NULL)
                                .put("visibility", entry.visibility.name)
                                .put("recipientSeats", JSONArray(entry.recipientSeats.sorted()))
                                .put("receivedReliabilityLabel", entry.reliability.name)
                                .put("proposition", proposition(entry.proposition))
                        }
                    }
                    array.put(event)
                }
            })
    }

    /** These are typed *confirmed* actions, not the model's guessed social interpretation. */
    private fun action(fact: ActionFact): JSONObject {
        val out = JSONObject()
        fun seat(value: Int?) = value ?: JSONObject.NULL
        return when (fact) {
            is ActionFact.Poison -> out.put("type", "POISON")
                .put("targetSeat", seat(fact.targetSeat))
                .put("visibility", "HOST_PRIVATE")
            is ActionFact.Protect -> out.put("type", "PROTECT")
                .put("targetSeat", fact.targetSeat).put("visibility", "HOST_PRIVATE")
            is ActionFact.Attack -> out.put("type", "ATTACK")
                .put("targetSeat", fact.targetSeat).put("visibility", "HOST_PRIVATE")
            is ActionFact.Execution -> out.put("type", "EXECUTION")
                .put("targetSeat", fact.targetSeat).put("visibility", "PUBLIC")
            is ActionFact.Death -> out.put("type", "DEATH")
                .put("targetSeat", fact.targetSeat).put("visibility", "PUBLIC")
            is ActionFact.NoExecution -> out.put("type", "NO_EXECUTION")
                .put("visibility", "PUBLIC")
            is ActionFact.SlayerShot -> out.put("type", "SLAYER_SHOT")
                .put("claimantSeat", fact.claimantSeat)
                .put("targetSeat", fact.targetSeat)
                .put("abilityConsumed", fact.abilityConsumed)
                .put("hit", fact.hit).put("visibility", "PUBLIC")
            is ActionFact.Nomination -> out.put("type", "NOMINATION")
                .put("nominatorSeat", fact.nominatorSeat)
                .put("nomineeSeat", fact.nomineeSeat)
                .put("firstVirginNomination", fact.firstVirginNomination)
                .put("visibility", "PUBLIC")
            is ActionFact.Vote -> out.put("type", "VOTE")
                .put("nominatorSeat", fact.nominatorSeat)
                .put("nomineeSeat", fact.nomineeSeat)
                .put("voterSeats", JSONArray(fact.voterSeats))
                .put("ghostVoterSeats", JSONArray(fact.ghostVoterSeats))
                .put("visibility", "PUBLIC")
            is ActionFact.KlutzLearnedDeath -> out.put("type", "KLUTZ_LEARNED_DEATH")
                .put("seat", fact.klutzSeat).put("visibility", "PLAYER_PRIVATE")
            is ActionFact.KlutzChoice -> out.put("type", "KLUTZ_CHOICE")
                .put("seat", fact.klutzSeat).put("chosenSeat", fact.chosenSeat)
                .put("visibility", "PUBLIC")
            is ActionFact.RoleChange -> out.put("type", "ROLE_CHANGE")
                .put("targetSeat", fact.targetSeat).put("actualRoleId", fact.role.value)
                .put("alignment", fact.alignment.name).put("characterType", fact.type.name)
                .put("visibility", "HOST_PRIVATE")
            is ActionFact.PhaseAdvance -> out.put("type", "PHASE_ADVANCE")
                .put("nextPhase", fact.phase.name).put("nextRound", fact.round)
                .put("visibility", "PUBLIC")
        }
    }

    /** Encode all currently supported proposition variants without text parsing or witness invention. */
    private fun proposition(value: InformationProposition): JSONObject = when (value) {
        is InformationProposition.RoleAt -> JSONObject().put("type", "ROLE_AT")
            .put("seat", value.seat).put("roleId", value.role.value)
        is InformationProposition.ShownRoleAt -> JSONObject().put("type", "SHOWN_ROLE_AT")
            .put("seat", value.seat).put("roleId", value.role.value)
        is InformationProposition.AlignmentAt -> JSONObject().put("type", "ALIGNMENT_AT")
            .put("seat", value.seat).put("alignment", value.alignment.name)
        is InformationProposition.CharacterTypeAt -> JSONObject().put("type", "CHARACTER_TYPE_AT")
            .put("seat", value.seat).put("characterType", value.characterType.name)
        is InformationProposition.AliveAt -> JSONObject().put("type", "ALIVE_AT")
            .put("seat", value.seat).put("alive", value.alive)
        is InformationProposition.AbilityStateAt -> JSONObject().put("type", "ABILITY_STATE_AT")
            .put("seat", value.seat).put("abilityRoleId", value.abilityRole.value)
            .put("abilityState", value.abilityState.name)
        is InformationProposition.RoleInPlay -> JSONObject().put("type", "ROLE_IN_PLAY")
            .put("roleId", value.role.value).put("inPlay", value.inPlay)
        is InformationProposition.PlayerCount -> JSONObject().put("type", "PLAYER_COUNT")
            .put("value", value.value)
        is InformationProposition.SetupProfile -> JSONObject().put("type", "SETUP_PROFILE")
            .put("townsfolk", value.townsfolk).put("outsiders", value.outsiders)
            .put("minions", value.minions).put("demons", value.demons)
        is InformationProposition.AnyOf -> JSONObject().put("type", "ANY_OF")
            .put("alternatives", JSONArray().also { array ->
                value.alternatives.forEach { array.put(proposition(it)) }
            })
        is InformationProposition.AllOf -> JSONObject().put("type", "ALL_OF")
            .put("propositions", JSONArray().also { array ->
                value.propositions.forEach { array.put(proposition(it)) }
            })
        is InformationProposition.Not -> JSONObject().put("type", "NOT")
            .put("proposition", proposition(value.proposition))
        is InformationProposition.NumericResult -> JSONObject().put("type", "NUMERIC_RESULT")
            .put("metric", value.metric.name).put("sourceSeat", value.sourceSeat)
            .put("subjectSeats", JSONArray(value.subjectSeats)).put("value", value.value)
        is InformationProposition.BooleanResult -> JSONObject().put("type", "BOOLEAN_RESULT")
            .put("metric", value.metric.name).put("sourceSeat", value.sourceSeat)
            .put("subjectSeats", JSONArray(value.subjectSeats)).put("value", value.value)
        is InformationProposition.GrimoireState -> JSONObject().put("type", "GRIMOIRE_STATE")
            .put("truthBinding", value.truthBinding.name)
            .put("seats", JSONArray().also { array ->
                value.seats.forEach { seat ->
                    array.put(JSONObject().put("seat", seat.seat)
                        .put("displayedRoleId", seat.displayedRole.value)
                        .put("alive", seat.alive)
                        .put("reminderTokenRefs", JSONArray(seat.reminderTokens.map { it.toString() })))
                }
            })
    }
}
