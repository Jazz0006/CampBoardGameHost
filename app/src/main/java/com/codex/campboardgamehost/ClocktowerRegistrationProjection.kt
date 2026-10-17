package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationResolution
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject

/** Read-only presentation queries. Rule legality remains in TroubleBrewingRegistrationDomain. */
internal class ClocktowerRegistrationProjection(
    private val cards: List<PlayerCard>,
    private val script: ClocktowerScript,
    private val registrationState: ClocktowerRegistrationInteractionState,
    private val effectivePoisonForRole: (String) -> String?,
    private val effectiveRoleForRegistration: (String, PlayerCard) -> RoleId?,
) {
    private val spyCard = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }
    private val recluseCard = cards.firstOrNull { it.clocktowerRole?.enName == "Recluse" }

    private fun registrationSubject(
        queryingRoleEnName: String,
        card: PlayerCard,
    ): TroubleBrewingRegistrationSubject? {
        val actualRole = card.clocktowerRole ?: return null
        val seat = cards.indexOf(card).plus(1).takeIf { it > 0 } ?: return null
        val actualType = when (actualRole.team) {
            ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
            ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
            ClocktowerTeam.Minion -> CharacterType.MINION
            ClocktowerTeam.Demon -> CharacterType.DEMON
        }
        val actualAlignment = when (actualRole.team) {
            ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD
            ClocktowerTeam.Minion, ClocktowerTeam.Demon -> com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL
        }
        return TroubleBrewingRegistrationSubject(
            seat = seat,
            actualRole = RoleId(actualRole.enName),
            actualAlignment = actualAlignment,
            actualType = actualType,
            effectiveRole = effectiveRoleForRegistration(queryingRoleEnName, card),
            poisoned = effectivePoisonForRole(queryingRoleEnName) == card.name,
        )
    }
    fun registrationResolution(
        key: String?,
        queryingRoleEnName: String,
        card: PlayerCard?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
        question: RegistrationQuestion = if (detail == ClocktowerRegistrationDetail.AlignmentOnly) {
            RegistrationQuestion.ALIGNMENT
        } else {
            RegistrationQuestion.ROLE
        },
    ): TroubleBrewingRegistrationResolution? {
        if (key == null || card == null || teams.isEmpty()) return null
        val subject = registrationSubject(queryingRoleEnName, card) ?: return null
        val allowedTypes = teams.mapTo(mutableSetOf()) { team ->
            when (team) {
                ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
                ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                ClocktowerTeam.Minion -> CharacterType.MINION
                ClocktowerTeam.Demon -> CharacterType.DEMON
            }
        }
        return TroubleBrewingRegistrationDomain.resolve(
            subject = subject,
            allowedRoles = clocktowerRoleDefinitionsForScript(script).filter { it.type in allowedTypes },
            question = question,
        )
    }
    fun spyCanRegister(queryingRoleEnName: String): Boolean = spyCard
        ?.let { registrationSubject(queryingRoleEnName, it) }
        ?.let(TroubleBrewingRegistrationDomain::specialReason) == RegistrationReason.SPY_ABILITY
    fun legalRegistrationRoles(
        key: String?,
        queryingRoleEnName: String,
        card: PlayerCard?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    ): List<ClocktowerRole> {
        val legalRoleIds = registrationResolution(key, queryingRoleEnName, card, teams, detail)
            ?.special
            ?.mapTo(mutableSetOf()) { it.registeredRole }
            .orEmpty()
        return completeTroubleBrewingRoles.filter { RoleId(it.enName) in legalRoleIds }
    }
    fun spyRegistersGood(key: String?, queryingRoleEnName: String): Boolean =
        key != null && spyCanRegister(queryingRoleEnName) && registrationState.spyIsGood(key)
    fun registeredRole(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String): ClocktowerRole? {
        if (!spyRegistersGood(key, queryingRoleEnName)) return spyCard?.clocktowerRole
        val allowed = legalRegistrationRoles(key, queryingRoleEnName, spyCard, teams)
        return allowed.firstOrNull { it.enName == registrationState.spyRole(key) }
    }
    fun recluseCanRegister(queryingRoleEnName: String): Boolean = recluseCard
        ?.let { registrationSubject(queryingRoleEnName, it) }
        ?.let(TroubleBrewingRegistrationDomain::specialReason) == RegistrationReason.RECLUSE_ABILITY
    fun recluseRegistersEvil(key: String?, queryingRoleEnName: String): Boolean =
        key != null && recluseCanRegister(queryingRoleEnName) && registrationState.recluseIsEvil(key)
    fun recluseRegisteredRole(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String): ClocktowerRole? {
        if (!recluseRegistersEvil(key, queryingRoleEnName)) return recluseCard?.clocktowerRole
        val allowed = legalRegistrationRoles(key, queryingRoleEnName, recluseCard, teams)
        return allowed.firstOrNull { it.enName == registrationState.recluseRole(key) }
    }
}
