package com.codex.campboardgamehost.clocktower.domain

sealed interface InformationValue {
    data class Role(val roleId: RoleId) : InformationValue
    data class RoleSet(val roleIds: List<RoleId>) : InformationValue {
        init {
            require(roleIds.isNotEmpty()) { "RoleSet cannot be empty." }
            require(roleIds.distinct().size == roleIds.size) { "RoleSet roles must be unique." }
        }
    }
    data class Number(val value: Int) : InformationValue
    data class Category(val id: String) : InformationValue {
        init {
            require(id.isNotBlank()) { "Information category ID cannot be blank." }
        }
    }
    data class YesNo(val answer: YesNoAnswer) : InformationValue
    data class NoCharacters(val characterType: CharacterType) : InformationValue
    data class PlayerPair(
        val shownRole: RoleId?,
        val seats: List<Int>,
    ) : InformationValue {
        init {
            require(seats.size == 2 && seats.distinct().size == 2) {
                "PlayerPair must contain exactly two distinct seats."
            }
            require(seats.all { it > 0 }) { "PlayerPair seats must be positive." }
        }
    }
}

enum class PlayerStatus {
    ALIVE,
    POISONED,
    DRUNK,
    PROTECTED,
    ABILITY_SPENT,
}

sealed interface EffectDraft {
    data class PlayerInformation(
        val recipientSeat: Int,
        val sourceAbility: RoleId,
        val value: InformationValue,
    ) : EffectDraft {
        init {
            require(recipientSeat > 0) { "recipientSeat must be positive." }
        }
    }

    data class Death(
        val subjectSeat: Int,
        val sourceAbility: RoleId,
    ) : EffectDraft {
        init {
            require(subjectSeat > 0) { "subjectSeat must be positive." }
        }
    }

    data class CharacterChange(
        val subjectSeat: Int,
        val fromRole: RoleId,
        val toRole: RoleId,
    ) : EffectDraft {
        init {
            require(subjectSeat > 0) { "subjectSeat must be positive." }
            require(fromRole != toRole) { "CharacterChange must change the role." }
        }
    }

    data class AlignmentChange(
        val subjectSeat: Int,
        val fromAlignment: Alignment,
        val toAlignment: Alignment,
    ) : EffectDraft {
        init {
            require(subjectSeat > 0) { "subjectSeat must be positive." }
            require(fromAlignment != toAlignment) { "AlignmentChange must change alignment." }
        }
    }

    data class StatusChange(
        val subjectSeat: Int,
        val status: PlayerStatus,
        val active: Boolean,
    ) : EffectDraft {
        init {
            require(subjectSeat > 0) { "subjectSeat must be positive." }
        }
    }

    data class Reminder(
        val subjectSeat: Int,
        val reminderCode: String,
    ) : EffectDraft {
        init {
            require(subjectSeat > 0) { "subjectSeat must be positive." }
            require(reminderCode.isNotBlank()) { "reminderCode cannot be blank." }
        }
    }
}
