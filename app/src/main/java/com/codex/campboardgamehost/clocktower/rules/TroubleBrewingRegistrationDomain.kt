package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

internal data class TroubleBrewingRegistrationSubject(
    val seat: Int,
    val actualRole: RoleId,
    val actualAlignment: Alignment,
    val actualType: CharacterType,
    val effectiveRole: RoleId? = actualRole,
    val poisoned: Boolean = false,
) {
    init {
        require(seat > 0) { "Registration subject seat must be positive." }
    }

    companion object {
        fun from(
            player: PlayerState,
            effectiveRole: RoleId? = player.actualRole,
            poisoned: Boolean = player.poisoned,
        ): TroubleBrewingRegistrationSubject = TroubleBrewingRegistrationSubject(
            seat = player.seat,
            actualRole = player.actualRole,
            actualAlignment = player.actualAlignment,
            actualType = player.actualType,
            effectiveRole = effectiveRole,
            poisoned = poisoned,
        )
    }
}

internal data class TroubleBrewingRegistrationCandidate(
    val subjectSeat: Int,
    val registeredRole: RoleId,
    val registeredType: CharacterType,
    val registeredAlignment: Alignment,
    val specialReason: RegistrationReason? = null,
) {
    val usesSpecialAbility: Boolean
        get() = specialReason != null

    fun registrationFact(
        interactionId: String,
        question: RegistrationQuestion,
    ): RegistrationFact? = specialReason?.let { reason ->
        RegistrationFact(
            interactionId = interactionId,
            subjectSeat = subjectSeat,
            registeredRole = registeredRole,
            registeredType = registeredType,
            registeredAlignment = registeredAlignment,
            registrationQuestion = question,
            reason = reason,
        )
    }
}

internal data class TroubleBrewingRegistrationResolution(
    val actual: TroubleBrewingRegistrationCandidate,
    val special: List<TroubleBrewingRegistrationCandidate>,
) {
    val candidates: List<TroubleBrewingRegistrationCandidate>
        get() = listOf(actual) + special

    val canUseSpecialAbility: Boolean
        get() = special.isNotEmpty()
}

/**
 * Single Trouble Brewing owner for Spy/Recluse registration legality and typed fact projection.
 *
 * Callers provide effective interaction-time subject facts and the roles that the detecting
 * mechanic can ask about. This owner applies the Spy/Recluse rule, impairment gate, candidate
 * normalization and registration-fact semantics. Scoring and presentation remain downstream.
 */
internal object TroubleBrewingRegistrationDomain {
    fun specialReason(subject: TroubleBrewingRegistrationSubject): RegistrationReason? {
        if (subject.poisoned) return null
        return when (subject.effectiveRole?.value) {
            "Spy" -> RegistrationReason.SPY_ABILITY
            "Recluse" -> RegistrationReason.RECLUSE_ABILITY
            else -> null
        }
    }

    fun resolve(
        subject: TroubleBrewingRegistrationSubject,
        allowedRoles: List<RoleDefinition>,
        question: RegistrationQuestion,
    ): TroubleBrewingRegistrationResolution {
        val actual = TroubleBrewingRegistrationCandidate(
            subjectSeat = subject.seat,
            registeredRole = subject.actualRole,
            registeredType = subject.actualType,
            registeredAlignment = subject.actualAlignment,
        )
        val reason = specialReason(subject)
            ?: return TroubleBrewingRegistrationResolution(actual, emptyList())
        val legalRoles = allowedRoles
            .asSequence()
            .filter { role -> role.isLegalFor(reason) }
            .distinctBy { role ->
                when (question) {
                    RegistrationQuestion.ALIGNMENT -> role.alignment.name
                    RegistrationQuestion.CHARACTER_TYPE -> role.type.name
                    RegistrationQuestion.ROLE,
                    RegistrationQuestion.SPECIFIC_MINION,
                    RegistrationQuestion.DEMON,
                    RegistrationQuestion.ABILITY_EFFECT,
                    -> role.id.value
                }
            }
            .toList()
        return TroubleBrewingRegistrationResolution(
            actual = actual,
            special = legalRoles.map { role ->
                TroubleBrewingRegistrationCandidate(
                    subjectSeat = subject.seat,
                    registeredRole = role.id,
                    registeredType = role.type,
                    registeredAlignment = role.alignment,
                    specialReason = reason,
                )
            },
        )
    }

    private fun RoleDefinition.isLegalFor(reason: RegistrationReason): Boolean = when (reason) {
        RegistrationReason.SPY_ABILITY ->
            alignment == Alignment.GOOD && type in setOf(CharacterType.TOWNSFOLK, CharacterType.OUTSIDER)
        RegistrationReason.RECLUSE_ABILITY ->
            alignment == Alignment.EVIL && type in setOf(CharacterType.MINION, CharacterType.DEMON)
        RegistrationReason.OTHER -> false
    }
}
