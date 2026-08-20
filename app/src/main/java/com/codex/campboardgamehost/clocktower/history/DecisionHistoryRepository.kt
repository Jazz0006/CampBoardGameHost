package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryProjection
import com.codex.campboardgamehost.clocktower.domain.MisinformationLedger
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationLedger
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.TruthRelation

internal class DecisionHistoryRepository(
    private val archive: DecisionHistoryArchive,
) {
    fun project(): DecisionHistoryProjection {
        val eventById = archive.events.associateBy { it.eventId }
        require(eventById.size == archive.events.size) { "Decision event IDs must be unique." }
        validateCorrections(eventById)
        val replacedIds = archive.corrections.map { it.replacedEventId }.toSet()
        val effective = archive.events.filter {
            it.status == DecisionEventStatus.APPLIED && it.eventId !in replacedIds
        }
        return DecisionHistoryProjection(
            effectiveEvents = effective,
            pressureBySeat = pressureProjection(effective),
            misinformationLedger = misinformationProjection(effective),
            registrationLedgerBySeat = registrationProjection(effective),
        )
    }

    private fun validateCorrections(eventById: Map<String, StorytellerDecisionEvent>) {
        require(archive.corrections.map { it.eventId }.distinct().size == archive.corrections.size) {
            "Correction event IDs must be unique."
        }
        require(archive.corrections.map { it.replacedEventId }.distinct().size == archive.corrections.size) {
            "A decision event cannot have multiple correction branches."
        }
        require(archive.corrections.map { it.replacementEventId }.distinct().size == archive.corrections.size) {
            "Correction chains cannot merge into the same replacement event."
        }
        archive.corrections.forEach { correction ->
            require(correction.replacedEventId in eventById && correction.replacementEventId in eventById) {
                "Correction endpoints must reference stored decision events."
            }
        }
        archive.corrections.forEach { start ->
            val visited = mutableSetOf<String>()
            var current: String? = start.replacedEventId
            while (current != null) {
                require(visited.add(current)) { "Correction chains cannot contain cycles." }
                current = archive.corrections.firstOrNull { it.replacedEventId == current }?.replacementEventId
            }
        }
    }

    private fun pressureProjection(events: List<StorytellerDecisionEvent>): Map<Int, PlayerInformationPressure> {
        val seats = events.flatMap { it.pressureDelta.keys }.distinct().sorted()
        return seats.associateWith { seat ->
            val deltas = events.mapNotNull { it.pressureDelta[seat] }
            PlayerInformationPressure(
                seat = seat,
                directSuspicion = deltas.filter { it > 0 }.sum(),
                confirmation = deltas.filter { it < 0 }.sumOf { -it },
                recentTargetCount = deltas.count { it != 0 },
                highImpactTargetCount = deltas.count { kotlin.math.abs(it) >= 4 },
            )
        }
    }

    private fun misinformationProjection(events: List<StorytellerDecisionEvent>): MisinformationLedger {
        val opportunities = events.filter { it.truthRelation != TruthRelation.NOT_APPLICABLE }
        val falseEvents = opportunities.filter {
            it.truthRelation == TruthRelation.FALSE_TO_ACTUAL_STATE || it.truthRelation == TruthRelation.PARTIALLY_TRUE
        }
        var consecutiveFalse = 0
        for (event in opportunities.asReversed()) {
            if (event in falseEvents) consecutiveFalse++ else break
        }
        return MisinformationLedger(
            totalOpportunities = opportunities.size,
            falseInformationCount = falseEvents.size,
            highImpactFalseCount = falseEvents.count { event ->
                event.pressureDelta.values.any { kotlin.math.abs(it) >= 4 }
            },
            consecutiveFalseCount = consecutiveFalse,
            truthfulWhileImpairedCount = opportunities.count {
                it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE && it.abilityState != AbilityState.FUNCTIONING
            },
        )
    }

    private fun registrationProjection(events: List<StorytellerDecisionEvent>): Map<Int, RegistrationLedger> {
        val bySeat = events.flatMap { event -> event.registrations.map { event to it } }.groupBy { it.second.subjectSeat }
        return bySeat.mapValues { (_, entries) ->
            val facts = entries.map { it.second }
            RegistrationLedger(
                evilRegistrationCount = facts.count { it.registeredAlignment == Alignment.EVIL },
                goodRegistrationCount = facts.count { it.registeredAlignment == Alignment.GOOD },
                minionRegistrationCount = facts.count { it.registeredType == CharacterType.MINION },
                demonRegistrationCount = facts.count { it.registeredType == CharacterType.DEMON },
                highImpactRegistrationCount = entries.count { (event, _) ->
                    event.pressureDelta.values.any { kotlin.math.abs(it) >= 4 }
                },
                consecutiveSameRegistrationCount = trailingSameRegistrationCount(facts),
            )
        }
    }

    private fun trailingSameRegistrationCount(facts: List<RegistrationFact>): Int {
        val last = facts.lastOrNull() ?: return 0
        return facts.asReversed().takeWhile {
            it.registeredAlignment == last.registeredAlignment &&
                it.registeredType == last.registeredType &&
                it.registeredRole == last.registeredRole
        }.size
    }

    companion object {
        fun extractSeatNumbers(values: List<String?>, maximumSeat: Int): Set<Int> {
            if (maximumSeat <= 0) return emptySet()
            return values.filterNotNull()
                .flatMap { value -> Regex("\\d+").findAll(value).map { it.value.toIntOrNull() }.toList() }
                .filterNotNull()
                .filter { it in 1..maximumSeat }
                .toSet()
        }
    }
}
