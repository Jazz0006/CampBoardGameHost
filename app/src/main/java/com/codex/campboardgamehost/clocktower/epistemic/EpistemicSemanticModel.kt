package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Stable schema marker for persisted A1/A1.1 semantic objects. */
const val EPISTEMIC_SCHEMA_VERSION: Int = 2

data class FormalPlayerState(
    val seat: Int,
    val actualRole: RoleId,
    val actualAlignment: Alignment,
    val actualType: CharacterType,
    val shownRole: RoleId? = null,
    val alive: Boolean = true,
    val poisoned: Boolean = false,
) {
    init { require(seat > 0) { "seat must be positive." } }
}

data class FormalGameState(
    val snapshotId: String,
    val gameId: String,
    val gameStateRevision: Long,
    val rulesetRef: RulesetRef,
    val phase: StorytellerPhase,
    val round: Int,
    val players: List<FormalPlayerState>,
    val publicPropositions: List<InformationProposition> = emptyList(),
    val storytellerOnlyPropositions: List<InformationProposition> = emptyList(),
    /** Ordered mechanical history used only by the B4 shadow timeline. */
    val timeline: List<ActionFact> = emptyList(),
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(snapshotId.isNotBlank()) { "snapshotId cannot be blank." }
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(round > 0) { "round must be positive." }
        require(players.isNotEmpty()) { "FormalGameState requires at least one player." }
        require(players.map { it.seat }.distinct().size == players.size) { "Each formal player must have a unique seat." }
        val seats = players.map { it.seat }.toSet()
        require((publicPropositions + storytellerOnlyPropositions).all { seats.containsAll(it.referencedSeats()) }) {
            "Every proposition seat must exist in the formal state."
        }
        require(timeline.map(ActionFact::actionId).distinct().size == timeline.size) { "Timeline action IDs must be unique." }
        require(timeline.map(ActionFact::sequence).distinct().size == timeline.size) { "Timeline action sequences must be unique." }
    }

    fun eligibleRedHerringSeats(): Set<Int> = players
        .filter { it.actualAlignment == Alignment.GOOD }
        .mapTo(linkedSetOf()) { it.seat }

    companion object {
        fun from(
            snapshot: GameSnapshot,
            phase: StorytellerPhase,
            round: Int,
            publicPropositions: List<InformationProposition> = emptyList(),
            storytellerOnlyPropositions: List<InformationProposition> = emptyList(),
            timeline: List<ActionFact> = emptyList(),
        ): FormalGameState {
            val players = snapshot.gameState.players.map { player ->
                FormalPlayerState(
                    seat = player.seat,
                    actualRole = player.actualRole,
                    actualAlignment = player.actualAlignment,
                    actualType = player.actualType,
                    shownRole = player.shownRole,
                    alive = player.alive,
                    poisoned = player.poisoned,
                )
            }
            val idPayload = listOf(
                snapshot.gameId,
                snapshot.gameStateRevision.toString(),
                snapshot.rulesetRef.scriptContentHash,
                phase.name,
                round.toString(),
                players.joinToString(";") {
                    "${it.seat}:${it.actualRole.value}:${it.actualAlignment.name}:${it.actualType.name}:" +
                        "${it.shownRole?.value}:${it.alive}:${it.poisoned}"
                },
                publicPropositions.joinToString(";") { EpistemicSemanticJson.encode(it) },
                storytellerOnlyPropositions.joinToString(";") { EpistemicSemanticJson.encode(it) },
                timeline.sortedWith(compareBy<ActionFact>({ it.sequence }, { it.actionId })).joinToString(";") { it.b4CanonicalPayload() },
            ).joinToString("|")
            return FormalGameState(
                snapshotId = SemanticStableId.create("snapshot", idPayload),
                gameId = snapshot.gameId,
                gameStateRevision = snapshot.gameStateRevision,
                rulesetRef = snapshot.rulesetRef,
                phase = phase,
                round = round,
                players = players,
                publicPropositions = publicPropositions,
                storytellerOnlyPropositions = storytellerOnlyPropositions,
                timeline = timeline.sortedWith(compareBy<ActionFact>({ it.sequence }, { it.actionId })),
            )
        }
    }
}

internal fun ActionFact.b4CanonicalPayload(): String = when (this) {
    is ActionFact.Poison -> "poison:$actionId:$sequence:${targetSeat ?: "none"}"
    is ActionFact.Protect -> "protect:$actionId:$sequence:$targetSeat"
    is ActionFact.Attack -> "attack:$actionId:$sequence:$targetSeat"
    is ActionFact.Execution -> "execution:$actionId:$sequence:$targetSeat"
    is ActionFact.Death -> "death:$actionId:$sequence:$targetSeat"
    is ActionFact.RoleChange -> "role-change:$actionId:$sequence:$targetSeat:${role.value}:${alignment.name}:${type.name}"
    is ActionFact.PhaseAdvance -> "phase:$actionId:$sequence:${phase.name}:$round"
}

sealed interface InformationProposition {
    data class RoleAt(val seat: Int, val role: RoleId) : InformationProposition { init { require(seat > 0) } }
    data class AlignmentAt(val seat: Int, val alignment: Alignment) : InformationProposition { init { require(seat > 0) } }
    data class CharacterTypeAt(val seat: Int, val characterType: CharacterType) : InformationProposition { init { require(seat > 0) } }
    data class AliveAt(val seat: Int, val alive: Boolean) : InformationProposition { init { require(seat > 0) } }
    data class AbilityStateAt(val seat: Int, val abilityRole: RoleId, val abilityState: AbilityState) : InformationProposition {
        init { require(seat > 0) }
    }
    data class RoleInPlay(val role: RoleId, val inPlay: Boolean = true) : InformationProposition
    data class PlayerCount(val value: Int) : InformationProposition {
        init { require(value in 5..15) { "Trouble Brewing player count must be between 5 and 15." } }
    }
    data class SetupProfile(val townsfolk: Int, val outsiders: Int, val minions: Int, val demons: Int) : InformationProposition {
        init { require(listOf(townsfolk, outsiders, minions, demons).all { it >= 0 }) { "Setup counts cannot be negative." } }
    }
    data class AnyOf(val alternatives: List<InformationProposition>) : InformationProposition {
        init { require(alternatives.size >= 2) { "AnyOf requires at least two alternatives." } }
    }
    data class AllOf(val propositions: List<InformationProposition>) : InformationProposition {
        init { require(propositions.isNotEmpty()) { "AllOf cannot be empty." } }
    }
    data class Not(val proposition: InformationProposition) : InformationProposition
    data class NumericResult(
        val metric: NumericMetric,
        val sourceSeat: Int,
        val subjectSeats: List<Int> = emptyList(),
        val value: Int,
    ) : InformationProposition {
        init {
            require(sourceSeat > 0)
            require(subjectSeats.all { it > 0 } && subjectSeats.distinct().size == subjectSeats.size)
            require(value >= 0)
        }
    }
    data class BooleanResult(
        val metric: BooleanMetric,
        val sourceSeat: Int,
        val subjectSeats: List<Int>,
        val value: Boolean,
    ) : InformationProposition {
        init {
            require(sourceSeat > 0)
            require(subjectSeats.isNotEmpty() && subjectSeats.all { it > 0 })
            require(subjectSeats.distinct().size == subjectSeats.size)
        }
    }

    /** Exact grimoire contents visible to the Spy at one wake interaction. */
    data class GrimoireState(val seats: List<GrimoireSeatView>) : InformationProposition {
        init {
            require(seats.isNotEmpty()) { "GrimoireState cannot be empty." }
            require(seats.map { it.seat }.distinct().size == seats.size) { "Grimoire seats must be unique." }
            require(seats.map { it.seat } == seats.map { it.seat }.sorted()) { "Grimoire seats must use canonical seat order." }
        }
    }
}

data class GrimoireSeatView(
    val seat: Int,
    val displayedRole: RoleId,
    val alive: Boolean,
    val reminderTokens: List<String> = emptyList(),
) {
    init {
        require(seat > 0)
        require(reminderTokens.all { STABLE_TOKEN_ID.matches(it) }) { "Reminder token IDs must be stable lowercase IDs." }
        require(reminderTokens.distinct().size == reminderTokens.size) { "Reminder token IDs must be unique per seat." }
        require(reminderTokens == reminderTokens.sorted()) { "Reminder token IDs must use canonical order." }
    }

    companion object { private val STABLE_TOKEN_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*") }
}

enum class NumericMetric { ADJACENT_EVIL_PAIRS, LIVING_EVIL_NEIGHBOURS, STEPS_TO_NEAREST_MINION, PLAYERS_WAKING_FOR_ABILITY }
enum class BooleanMetric { DEMON_OR_RED_HERRING_PRESENT }
enum class ObservationVisibility { PUBLIC, PRIVATE }
enum class ObservationReliability { RECEIVED_AS_FUNCTIONING, KNOWN_MALFUNCTIONING, NOT_ABILITY_INFORMATION }

data class EpistemicObservation(
    val observationId: String,
    val snapshotId: String,
    val phase: StorytellerPhase,
    val round: Int,
    val sequence: Int,
    val sourceSeat: Int?,
    val sourceAbility: RoleId?,
    val visibility: ObservationVisibility,
    val recipientSeats: Set<Int>,
    val reliability: ObservationReliability,
    val proposition: InformationProposition,
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(observationId.isNotBlank() && snapshotId.isNotBlank())
        require(round > 0 && sequence >= 0)
        require(sourceSeat == null || sourceSeat > 0)
        require(recipientSeats.all { it > 0 })
        require(visibility != ObservationVisibility.PRIVATE || recipientSeats.isNotEmpty())
        require(visibility != ObservationVisibility.PUBLIC || recipientSeats.isEmpty())
        require(proposition !is InformationProposition.GrimoireState ||
            (visibility == ObservationVisibility.PRIVATE && sourceAbility?.value == "Spy")) {
            "GrimoireState must be a private observation sourced from the Spy ability."
        }
    }
}

data class StorytellerDecisionPoint(
    val decisionPointId: String,
    val snapshotId: String,
    val phase: StorytellerPhase,
    val round: Int,
    val sequence: Int,
    val sourceSeat: Int?,
    val sourceAbility: RoleId,
    val decisionTypeId: String,
    val recipientSeats: Set<Int>,
    val queryPropositions: List<InformationProposition> = emptyList(),
    val candidateFamilyId: CandidateFamilyId? = null,
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(decisionPointId.isNotBlank() && snapshotId.isNotBlank())
        require(round > 0 && sequence >= 0)
        require(sourceSeat == null || sourceSeat > 0)
        require(STABLE_TYPE_ID.matches(decisionTypeId))
        require(recipientSeats.isNotEmpty() && recipientSeats.all { it > 0 })
    }
    companion object { private val STABLE_TYPE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*") }
}

data class LegalEpistemicChoice(
    val choiceId: String,
    val interactionId: String,
    val observation: EpistemicObservation,
    val registrations: List<RegistrationFact> = emptyList(),
) {
    init {
        require(choiceId.isNotBlank())
        require(interactionId.matches(Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*"))) {
            "interactionId must be a stable lowercase ID."
        }
        require(registrations.all { it.interactionId == interactionId }) {
            "Every selected registration must be bound to the legal choice interaction."
        }
        require(registrations.map { it.subjectSeat to it.registrationQuestion }.distinct().size == registrations.size) {
            "A subject may have only one selected registration per question in an interaction."
        }
    }
}

data class LegalChoiceSet(
    val choiceSetId: String,
    val decisionPointId: String,
    val rulesetRef: RulesetRef,
    val choices: List<LegalEpistemicChoice>,
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(choiceSetId.isNotBlank() && decisionPointId.isNotBlank())
        require(choices.isNotEmpty())
        require(choices.map { it.choiceId }.distinct().size == choices.size)
        require(choices.map { it.observation.snapshotId }.distinct().size == 1)
    }
}

data class PlayerKnowledgeSnapshot(
    val knowledgeSnapshotId: String,
    val formalSnapshotId: String,
    val recipientSeat: Int,
    val perceivedRole: RoleId,
    val publicObservations: List<EpistemicObservation> = emptyList(),
    val privateObservations: List<EpistemicObservation> = emptyList(),
    val setupKnowledge: List<InformationProposition> = emptyList(),
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        requireSchemaVersion(schemaVersion)
        require(knowledgeSnapshotId.isNotBlank() && formalSnapshotId.isNotBlank())
        require(recipientSeat > 0)
        require(publicObservations.all { it.snapshotId == formalSnapshotId && it.visibility == ObservationVisibility.PUBLIC })
        require(privateObservations.all {
            it.snapshotId == formalSnapshotId && it.visibility == ObservationVisibility.PRIVATE && recipientSeat in it.recipientSeats
        })
        require((publicObservations + privateObservations).map { it.observationId }.distinct().size ==
            publicObservations.size + privateObservations.size)
    }
}

object SemanticStableId {
    fun create(prefix: String, canonicalPayload: String): String {
        require(prefix.matches(Regex("[a-z][a-z0-9-]*")))
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonicalPayload.toByteArray(StandardCharsets.UTF_8))
            .take(16)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return "$prefix-$digest"
    }
}

internal fun requireSchemaVersion(version: Int) {
    require(version == EPISTEMIC_SCHEMA_VERSION) {
        "Unsupported epistemic schema version $version; expected $EPISTEMIC_SCHEMA_VERSION. Schema v1 must be explicitly migrated."
    }
}

private fun InformationProposition.referencedSeats(): Set<Int> = when (this) {
    is InformationProposition.RoleAt -> setOf(seat)
    is InformationProposition.AlignmentAt -> setOf(seat)
    is InformationProposition.CharacterTypeAt -> setOf(seat)
    is InformationProposition.AliveAt -> setOf(seat)
    is InformationProposition.AbilityStateAt -> setOf(seat)
    is InformationProposition.RoleInPlay -> emptySet()
    is InformationProposition.PlayerCount -> emptySet()
    is InformationProposition.SetupProfile -> emptySet()
    is InformationProposition.AnyOf -> alternatives.flatMap { it.referencedSeats() }.toSet()
    is InformationProposition.AllOf -> propositions.flatMap { it.referencedSeats() }.toSet()
    is InformationProposition.Not -> proposition.referencedSeats()
    is InformationProposition.NumericResult -> setOf(sourceSeat) + subjectSeats
    is InformationProposition.BooleanResult -> setOf(sourceSeat) + subjectSeats
    is InformationProposition.GrimoireState -> seats.mapTo(linkedSetOf()) { it.seat }
}
