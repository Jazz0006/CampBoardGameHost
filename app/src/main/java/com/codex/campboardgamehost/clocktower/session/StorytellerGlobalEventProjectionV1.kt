package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryPrefixV1
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.json.JSONArray
import org.json.JSONObject

/**
 * One compact, read-only game record derived from Session facts.
 * This is the AI delta and the source for future completed-game review, NOT a second event store.
 * Technical sequence is used only as a cursor; no IDs, revisions, or internal transitions reach AI.
 */
internal object StorytellerGlobalEventProjectionV1 {
    fun encode(
        prefix: StorytellerProviderHistoryPrefixV1,
        fromGlobalSequence: Long = 0L,
        confirmedGameState: GameState? = null,
    ): JSONArray {
        require(fromGlobalSequence >= 0L &&
            fromGlobalSequence <= requireNotNull(prefix.exclusiveGlobalSequence))
        val all = prefix.entries
        val executions = all.filterIsInstance<StorytellerProviderHistoryEntryV1.Action>()
            .filter { it.fact is ActionFact.Execution }
        fun isExecuted(seat: Int, phase: StorytellerPhase, round: Int): Boolean =
            executions.any { action ->
                (action.fact as ActionFact.Execution).targetSeat == seat &&
                    action.point.round == round &&
                    dayOrNight(action.point.phase) == dayOrNight(phase)
            }

        return JSONArray().also { result ->
            // Drunk is selected only when the Host accepts the setup, not when AI
            // recommends a candidate. The accepted Session role is the authority.
            // A first-night log event is derived from that confirmed truth instead
            // of persisting the same role a second time.
            if (fromGlobalSequence == 0L) {
                confirmedGameState?.players?.singleOrNull {
                    it.actualRole.value.equals("drunk", ignoreCase = true)
                }?.let { player ->
                    result.put(JSONObject().put("type", "drunk_selected")
                        .put("period", "N1").put("seat", player.seat)
                        .put("shownRole", (player.shownRole ?: player.actualRole).value))
                }
            }
            all.asSequence()
                .filter { it.point.globalSequence >= fromGlobalSequence }
                .forEach { entry ->
                    val event = when (entry) {
                        is StorytellerProviderHistoryEntryV1.Action -> {
                            val fact = entry.fact
                            when {
                                fact is ActionFact.Death &&
                                    isExecuted(fact.targetSeat, entry.point.phase, entry.point.round) -> null
                                // Dawn poison materialization is a rule transition, never a new
                                // Poisoner selection. Keep the durable fact for recovery only.
                                fact is ActionFact.Poison &&
                                    fact.actionId.startsWith("dawn-") &&
                                    "-poison-" in fact.actionId -> null
                                else -> action(fact)
                            }
                        }
                        is StorytellerProviderHistoryEntryV1.Observation -> {
                            val proposition = entry.proposition
                            if (proposition is InformationProposition.AliveAt && !proposition.alive &&
                                (all.any { item ->
                                    item is StorytellerProviderHistoryEntryV1.Action &&
                                        item.fact is ActionFact.Death &&
                                        item.fact.targetSeat == proposition.seat &&
                                        item.point.round == entry.point.round &&
                                        dayOrNight(item.point.phase) == dayOrNight(entry.point.phase)
                                } || isExecuted(proposition.seat, entry.point.phase, entry.point.round))) {
                                null
                            } else observation(entry)
                        }
                    }
                    if (event != null) {
                        event.put("period", "${dayOrNight(entry.point.phase)}${entry.point.round}")
                        result.put(event)
                    }
                }
        }
    }

    private fun dayOrNight(phase: StorytellerPhase): String = when (phase) {
        StorytellerPhase.FIRST_NIGHT, StorytellerPhase.NIGHT -> "N"
        StorytellerPhase.DAY, StorytellerPhase.DAWN -> "D"
    }

    private fun action(fact: ActionFact): JSONObject? = when (fact) {
        // Expiry and Dawn carry are Host mechanics, not separate Storyteller history.
        is ActionFact.Poison -> fact.targetSeat?.let {
            JSONObject().put("type", "poison").put("target", it)
        }
        is ActionFact.Protect -> JSONObject().put("type", "protect").put("target", fact.targetSeat)
        is ActionFact.Attack -> JSONObject().put("type", "attack").put("target", fact.targetSeat)
        is ActionFact.Execution -> JSONObject().put("type", "execution").put("seat", fact.targetSeat)
        is ActionFact.Death -> JSONObject().put("type", "death").put("seat", fact.targetSeat)
        is ActionFact.NoNightDeaths -> JSONObject().put("type", "no_night_deaths")
        is ActionFact.NoExecution -> JSONObject().put("type", "no_execution")
        is ActionFact.SlayerShot -> JSONObject().put("type", "slayer")
            .put("seat", fact.claimantSeat).put("target", fact.targetSeat)
            .put("hit", fact.hit).also { event ->
                fact.targetRegistersAsDemon?.let { event.put("targetRegistersAsDemon", it) }
            }
        is ActionFact.Nomination -> JSONObject().put("type", "nomination")
            .put("seat", fact.nominatorSeat).put("target", fact.nomineeSeat).also { event ->
                fact.nominatorRegistersAsTownsfolk?.let { event.put("nominatorRegistersAsTownsfolk", it) }
            }
        is ActionFact.Vote -> JSONObject().put("type", "vote")
            .put("seat", fact.nominatorSeat).put("target", fact.nomineeSeat)
            .put("voters", JSONArray(fact.voterSeats))
        // Private ability-trigger bookkeeping is not a second public/player event.
        is ActionFact.KlutzLearnedDeath -> null
        is ActionFact.KlutzChoice -> JSONObject().put("type", "klutz_choice")
            .put("seat", fact.klutzSeat).put("target", fact.chosenSeat)
        is ActionFact.RoleChange -> JSONObject().put("type", "role_change")
            .put("seat", fact.targetSeat).put("role", fact.role.value)
        is ActionFact.ButlerMasterChoice -> JSONObject().put("type", "butler_master")
            .put("seat", fact.butlerSeat).put("target", fact.masterSeat)
        // Choosing the Mayor himself is an accepted *no-bounce* ruling,
        // not an actual redirection event.
        is ActionFact.MayorRedirectChoice -> fact.targetSeat
            .takeIf { it != fact.mayorSeat }?.let { target ->
                JSONObject().put("type", "mayor_redirect")
                    .put("seat", fact.mayorSeat).put("target", target)
            }
        // Setup choices already exist in the current game state. The cursor does not need
        // them again; phase changes and confirmed-button details have no strategic value.
        is ActionFact.RedHerringChoice, is ActionFact.DemonBluffChoice,
        is ActionFact.PhaseAdvance -> null
    }

    private fun observation(entry: StorytellerProviderHistoryEntryV1.Observation): JSONObject {
        val result = JSONObject().put("type", "information")
        entry.sourceSeat?.let { result.put("seat", it) }
        entry.sourceAbility?.let { result.put("role", it.value) }
        if (entry.visibility == ObservationVisibility.PUBLIC) result.put("public", true)
        if (entry.recipientSeats != setOfNotNull(entry.sourceSeat)) {
            result.put("recipients", JSONArray(entry.recipientSeats.sorted()))
        }
        if (entry.reliability != ObservationReliability.RECEIVED_AS_FUNCTIONING) {
            result.put("reliability", entry.reliability.name)
        }
        when (val proposition = entry.proposition) {
            is InformationProposition.NumericResult -> {
                result.put("value", proposition.value)
                if (proposition.subjectSeats.isNotEmpty()) {
                    result.put("subjects", JSONArray(proposition.subjectSeats))
                }
            }
            is InformationProposition.BooleanResult -> {
                result.put("value", proposition.value)
                result.put("subjects", JSONArray(proposition.subjectSeats))
            }
            else -> result.put("detail", JSONObject(EpistemicSemanticJson.encode(proposition)))
        }
        return result
    }
}
