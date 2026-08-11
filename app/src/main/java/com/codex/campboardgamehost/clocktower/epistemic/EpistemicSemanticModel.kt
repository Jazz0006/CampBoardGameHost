package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Stable schema marker for persisted A1 semantic objects. */
const val EPISTEMIC_SCHEMA_VERSION: Int = 1

data class FormalPlayerState(
    val seat: Int,
    val actualRole: RoleId,
    val actualAlignment: Alignment,
    val actualType: CharacterType,
    val shownRole: RoleId? = null,
    val alive: Boolean = true,
    val poisoned: Boolean = false,
) {
    init {
        require(seat > 0) { "seat must be positive." }
    }
}

/**
 * Storyteller-truth state used by formal engines and oracle adapters.
 *
 * This object deliberately contains no localized text. It is not safe to pass
 * to a player-world engine directly because it includes actual roles and
 * storyteller-only propositions.
 */
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
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        require(schemaVersion == EPISTEMIC_SCHEMA_VERSION) { "Unsupported epistemic schema version." }
        require(snapshotId.isNotBlank()) { "snapshotId cannot be blank." }
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(round > 0) { "round must be positive." }
        require(players.isNotEmpty()) { "FormalGameState requires at least one player." }
        require(players.map { it.seat }.distinct().size == players.size) {
            "Each formal player must have a unique seat."
        }
        val seats = players.map { it.seat }.toSet()
        require((publicPropositions + storytellerOnlyPropositions).all {
            seats.containsAll(it.referencedSeats())
        }) {
            "Every proposition seat must exist in the formal state."
        }
    }

    companion object {
        fun from(
            snapshot: GameSnapshot,
            phase: StorytellerPhase,
            round: Int,
            publicPropositions: List<InformationProposition> = emptyList(),
            storytellerOnlyPropositions: List<InformationProposition> = emptyList(),
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
            )
        }
    }
}

/** A mechanically meaningful statement. It carries no UI wording or probability. */
sealed interface InformationProposition {
    data class RoleAt(val seat: Int, val role: RoleId) : InformationProposition {
        init { require(seat > 0) { "seat must be positive." } }
    }
    data class AlignmentAt(val seat: Int, val alignment: Alignment) : InformationProposition {
        init { require(seat > 0) { "seat must be positive." } }
    }
    data class CharacterTypeAt(val seat: Int, val characterType: CharacterType) : InformationProposition {
        init { require(seat > 0) { "seat must be positive." } }
    }
    data class AliveAt(val seat: Int, val alive: Boolean) : InformationProposition {
        init { require(seat > 0) { "seat must be positive." } }
    }
    data class AbilityStateAt(
        val seat: Int,
        val abilityRole: RoleId,
        val abilityState: AbilityState,
    ) : InformationProposition {
        init { require(seat > 0) { "seat must be positive." } }
    }
    data class RoleInPlay(val role: RoleId, val inPlay: Boolean = true) : InformationProposition
    data class SetupProfile(
        val townsfolk: Int,
        val outsiders: Int,
        val minions: Int,
        val demons: Int,
    ) : InformationProposition {
        init {
            require(listOf(townsfolk, outsiders, minions, demons).all { it >= 0 }) {
                "Setup counts cannot be negative."
            }
        }
    }
    data class AnyOf(val alternatives: List<InformationProposition>) : InformationProposition {
        init {
            require(alternatives.size >= 2) { "AnyOf requires at least two alternatives." }
        }
    }
    data class AllOf(val propositions: List<InformationProposition>) : InformationProposition {
        init {
            require(propositions.isNotEmpty()) { "AllOf cannot be empty." }
        }
    }
    data class Not(val proposition: InformationProposition) : InformationProposition
    data class NumericResult(
        val metric: NumericMetric,
        val sourceSeat: Int,
        val subjectSeats: List<Int> = emptyList(),
        val value: Int,
    ) : InformationProposition {
        init {
            require(sourceSeat > 0) { "sourceSeat must be positive." }
            require(subjectSeats.all { it > 0 }) { "subject seats must be positive." }
            require(subjectSeats.distinct().size == subjectSeats.size) { "subject seats must be unique." }
            require(value >= 0) { "numeric result cannot be negative." }
        }
    }
}

enum class NumericMetric {
    ADJACENT_EVIL_PAIRS,
    LIVING_EVIL_NEIGHBOURS,
    STEPS_TO_NEAREST_MINION,
    PLAYERS_WAKING_FOR_ABILITY,
}

enum class ObservationVisibility {
    PUBLIC,
    PRIVATE,
}

enum class ObservationReliability {
    /** The recipient has no mechanically granted knowledge that the ability malfunctioned. */
    RECEIVED_AS_FUNCTIONING,
    KNOWN_MALFUNCTIONING,
    NOT_ABILITY_INFORMATION,
}

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
        require(schemaVersion == EPISTEMIC_SCHEMA_VERSION) { "Unsupported epistemic schema version." }
        require(observationId.isNotBlank()) { "observationId cannot be blank." }
        require(snapshotId.isNotBlank()) { "snapshotId cannot be blank." }
        require(round > 0) { "round must be positive." }
        require(sequence >= 0) { "sequence cannot be negative." }
        require(sourceSeat == null || sourceSeat > 0) { "sourceSeat must be positive when present." }
        require(recipientSeats.all { it > 0 }) { "recipient seats must be positive." }
        require(visibility != ObservationVisibility.PRIVATE || recipientSeats.isNotEmpty()) {
            "A private observation needs at least one recipient."
        }
        require(visibility != ObservationVisibility.PUBLIC || recipientSeats.isEmpty()) {
            "Public observations use an empty recipient set."
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
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        require(schemaVersion == EPISTEMIC_SCHEMA_VERSION) { "Unsupported epistemic schema version." }
        require(decisionPointId.isNotBlank()) { "decisionPointId cannot be blank." }
        require(snapshotId.isNotBlank()) { "snapshotId cannot be blank." }
        require(round > 0) { "round must be positive." }
        require(sequence >= 0) { "sequence cannot be negative." }
        require(sourceSeat == null || sourceSeat > 0) { "sourceSeat must be positive when present." }
        require(decisionTypeId.matches(STABLE_TYPE_ID)) { "decisionTypeId must be a stable lowercase ID." }
        require(recipientSeats.isNotEmpty() && recipientSeats.all { it > 0 }) {
            "A decision point needs positive recipient seats."
        }
    }

    companion object {
        private val STABLE_TYPE_ID = Regex("[a-z0-9]+(?:[._-][a-z0-9]+)*")
    }
}

data class LegalEpistemicChoice(
    val choiceId: String,
    val observation: EpistemicObservation,
    val registrations: List<RegistrationFact> = emptyList(),
) {
    init {
        require(choiceId.isNotBlank()) { "choiceId cannot be blank." }
        require(registrations.map { it.interactionId }.distinct().size == registrations.size) {
            "Registration interaction IDs must be unique within a legal choice."
        }
    }
}

/** Complete official-legal outputs for one decision point. */
data class LegalChoiceSet(
    val choiceSetId: String,
    val decisionPointId: String,
    val rulesetRef: RulesetRef,
    val choices: List<LegalEpistemicChoice>,
    val schemaVersion: Int = EPISTEMIC_SCHEMA_VERSION,
) {
    init {
        require(schemaVersion == EPISTEMIC_SCHEMA_VERSION) { "Unsupported epistemic schema version." }
        require(choiceSetId.isNotBlank()) { "choiceSetId cannot be blank." }
        require(decisionPointId.isNotBlank()) { "decisionPointId cannot be blank." }
        require(choices.isNotEmpty()) { "LegalChoiceSet cannot be empty." }
        require(choices.map { it.choiceId }.distinct().size == choices.size) {
            "Legal choice IDs must be unique."
        }
        require(choices.map { it.observation.snapshotId }.distinct().size == 1) {
            "All legal choices must refer to the same snapshot."
        }
    }
}

/**
 * Facts available to a single player. Actual roles, poison targets and other
 * storyteller secrets have no field here and can only enter through a valid
 * observation granted to this perspective.
 */
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
        require(schemaVersion == EPISTEMIC_SCHEMA_VERSION) { "Unsupported epistemic schema version." }
        require(knowledgeSnapshotId.isNotBlank()) { "knowledgeSnapshotId cannot be blank." }
        require(formalSnapshotId.isNotBlank()) { "formalSnapshotId cannot be blank." }
        require(recipientSeat > 0) { "recipientSeat must be positive." }
        require(publicObservations.all { observation ->
            observation.snapshotId == formalSnapshotId &&
                observation.visibility == ObservationVisibility.PUBLIC
        }) { "Public knowledge must contain only public observations from this snapshot." }
        require(privateObservations.all { observation ->
            observation.snapshotId == formalSnapshotId &&
                observation.visibility == ObservationVisibility.PRIVATE &&
                recipientSeat in observation.recipientSeats
        }) { "Private knowledge must be addressed to this player and snapshot." }
        require((publicObservations + privateObservations).map { it.observationId }.distinct().size ==
            publicObservations.size + privateObservations.size) {
            "Observation IDs must be unique within a knowledge snapshot."
        }
    }
}

object SemanticStableId {
    fun create(prefix: String, canonicalPayload: String): String {
        require(prefix.matches(Regex("[a-z][a-z0-9-]*"))) { "prefix must be a stable lowercase ID." }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonicalPayload.toByteArray(StandardCharsets.UTF_8))
            .take(16)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return "$prefix-$digest"
    }
}

private fun InformationProposition.referencedSeats(): Set<Int> = when (this) {
    is InformationProposition.RoleAt -> setOf(seat)
    is InformationProposition.AlignmentAt -> setOf(seat)
    is InformationProposition.CharacterTypeAt -> setOf(seat)
    is InformationProposition.AliveAt -> setOf(seat)
    is InformationProposition.AbilityStateAt -> setOf(seat)
    is InformationProposition.RoleInPlay -> emptySet()
    is InformationProposition.SetupProfile -> emptySet()
    is InformationProposition.AnyOf -> alternatives.flatMap { it.referencedSeats() }.toSet()
    is InformationProposition.AllOf -> propositions.flatMap { it.referencedSeats() }.toSet()
    is InformationProposition.Not -> proposition.referencedSeats()
    is InformationProposition.NumericResult -> setOf(sourceSeat) + subjectSeats
}
