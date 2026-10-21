package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId

/** Stable schema marker for persisted A1/A1.1 semantic objects. */
const val EPISTEMIC_SCHEMA_VERSION: Int = 2

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

}

enum class NumericMetric { ADJACENT_EVIL_PAIRS, LIVING_EVIL_NEIGHBOURS, STEPS_TO_NEAREST_MINION, PLAYERS_WAKING_FOR_ABILITY }
enum class BooleanMetric { DEMON_OR_RED_HERRING_PRESENT, PLAYER_QUESTION_ANSWER }
enum class ObservationVisibility { PUBLIC, PRIVATE }
enum class ObservationReliability { RECEIVED_AS_FUNCTIONING, KNOWN_MALFUNCTIONING, NOT_ABILITY_INFORMATION }

internal fun requireSchemaVersion(version: Int) {
    require(version == EPISTEMIC_SCHEMA_VERSION) {
        "Unsupported epistemic schema version $version; expected $EPISTEMIC_SCHEMA_VERSION. Schema v1 must be explicitly migrated."
    }
}


