package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Collections

/** Stable schema marker for persisted A1/A1.1 semantic objects. */
const val EPISTEMIC_SCHEMA_VERSION: Int = 2

enum class GrimoireTruthBinding {
    /** Legacy producers know only display-role/alive payloads and make no completeness claim for reminders. */
    LEGACY_DISPLAY_ONLY,

    /** Produced from an explicit complete physical-Grimoire truth snapshot. */
    VERIFIED_EXACT,
}

sealed interface InformationProposition {
    data class RoleAt(val seat: Int, val role: RoleId) : InformationProposition { init { require(seat > 0) } }
    data class ShownRoleAt(val seat: Int, val role: RoleId) : InformationProposition { init { require(seat > 0) } }
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

    /** Grimoire contents visible to the Spy at one wake interaction, with explicit truth provenance. */
    class GrimoireState(
        seats: List<GrimoireSeatView>,
        val truthBinding: GrimoireTruthBinding = GrimoireTruthBinding.LEGACY_DISPLAY_ONLY,
    ) : InformationProposition {
        val seats: List<GrimoireSeatView> = Collections.unmodifiableList(seats.toList())

        init {
            require(this.seats.isNotEmpty()) { "GrimoireState cannot be empty." }
            require(this.seats.map { it.seat }.distinct().size == this.seats.size) { "Grimoire seats must be unique." }
            require(this.seats.map { it.seat } == this.seats.map { it.seat }.sorted()) { "Grimoire seats must use canonical seat order." }
            val tokens = this.seats.flatMap(GrimoireSeatView::reminderTokens)
            require(tokens.distinct().size == tokens.size) {
                "The same physical reminder-token reference cannot appear on multiple Grimoire seats."
            }
        }

        override fun equals(other: Any?): Boolean = other is GrimoireState &&
            seats == other.seats && truthBinding == other.truthBinding

        override fun hashCode(): Int = 31 * seats.hashCode() + truthBinding.hashCode()

        override fun toString(): String = "GrimoireState(seats=$seats, truthBinding=$truthBinding)"
    }
}

class GrimoireSeatView(
    val seat: Int,
    val displayedRole: RoleId,
    val alive: Boolean,
    reminderTokens: List<GrimoireReminderTokenRef> = emptyList(),
) {
    val reminderTokens: List<GrimoireReminderTokenRef> = Collections.unmodifiableList(reminderTokens.toList())

    init {
        require(seat > 0)
        require(this.reminderTokens.distinct().size == this.reminderTokens.size) {
            "The same physical reminder-token reference cannot appear twice on one seat."
        }
        require(this.reminderTokens == this.reminderTokens.sorted()) {
            "Rule-backed reminder tokens must use canonical order."
        }
    }

    override fun equals(other: Any?): Boolean = other is GrimoireSeatView &&
        seat == other.seat &&
        displayedRole == other.displayedRole &&
        alive == other.alive &&
        reminderTokens == other.reminderTokens

    override fun hashCode(): Int {
        var result = seat
        result = 31 * result + displayedRole.hashCode()
        result = 31 * result + alive.hashCode()
        result = 31 * result + reminderTokens.hashCode()
        return result
    }

    override fun toString(): String =
        "GrimoireSeatView(seat=$seat, displayedRole=$displayedRole, alive=$alive, reminderTokens=$reminderTokens)"
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
    val timelineBinding: ObservationTimelineBinding = ObservationTimelineBinding.LegacyLocal,
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
        timelineBinding.requireReplayFieldsMatch(phase, round, sequence)
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


