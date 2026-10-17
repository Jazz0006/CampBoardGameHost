package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric

/** Read-only candidate preparation; legality and result deduplication retain their existing owners. */
internal class ClocktowerRegistrationOptionPreparation(
    private val cards: List<PlayerCard>,
    private val registration: ClocktowerRegistrationProjection,
    private val text: (String, String) -> String,
    private val roleLabel: (ClocktowerRole) -> String,
) {
    private val spyCard = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }
    private val recluseCard = cards.firstOrNull { it.clocktowerRole?.enName == "Recluse" }

    fun numericOptions(
        title: String,
        actor: PlayerCard,
        roleEnName: String,
        metric: NumericMetric,
        subjectSeats: List<Int>,
        footer: String,
        spyKey: String?,
        recluseKey: String?,
        valueFor: (ClocktowerAlignmentRegistrationWitness) -> Int,
    ): List<ClocktowerDisplayOption> {
        val spySelectable = spyKey != null && registration.spyCanRegister(roleEnName)
        val recluseSelectable = recluseKey != null && registration.recluseCanRegister(roleEnName)
        if (!spySelectable && !recluseSelectable) return emptyList()
        val sourceSeat = cards.indexOf(actor) + 1
        if (sourceSeat <= 0) return emptyList()
        val witnesses = clocktowerAlignmentRegistrationWitnesses(
            currentSpyRegistersGood = registration.spyRegistersGood(spyKey, roleEnName),
            spySelectable = spySelectable,
            currentRecluseRegistersEvil = registration.recluseRegistersEvil(recluseKey, roleEnName),
            recluseSelectable = recluseSelectable,
        )
        return ClocktowerRegistrationResultPresentation.numericOptions(
            title = title,
            sourceSeat = sourceSeat,
            metric = metric,
            subjectSeats = subjectSeats,
            footer = footer,
            witnesses = witnesses,
            valueFor = valueFor,
        )
    }

    fun fortuneTellerOptions(
        actor: PlayerCard,
        registrationKey: String?,
        subjectSeats: List<Int>,
        secondary: String?,
        matches: (Boolean) -> Boolean?,
    ): List<ClocktowerDisplayOption> {
        val key = registrationKey ?: return emptyList()
        if (!registration.recluseCanRegister("Fortune Teller")) return emptyList()
        val sourceSeat = cards.indexOf(actor) + 1
        if (sourceSeat <= 0 || subjectSeats.size != 2 || subjectSeats.distinct().size != 2) return emptyList()
        val current = registration.recluseRegistersEvil(key, "Fortune Teller")
        val demonRole = registration.legalRegistrationRoles(
            key,
            "Fortune Teller",
            recluseCard,
            listOf(ClocktowerTeam.Demon),
        ).firstOrNull()
        return ClocktowerRegistrationResultPresentation.fortuneTellerOptions(
            sourceSeat = sourceSeat,
            subjectSeats = subjectSeats,
            secondary = secondary,
            currentRecluseRegistersEvil = current,
            demonRegistrationRole = demonRole,
            text = text,
            matches = matches,
        )
    }

    fun roleRevealOptions(
        title: String,
        roleEnName: String,
        target: PlayerCard?,
        footer: String,
        spyKey: String?,
        spyTeams: List<ClocktowerTeam>,
        recluseKey: String?,
        recluseTeams: List<ClocktowerTeam>,
    ): List<ClocktowerDisplayOption> {
        val resolvedTarget = target ?: return emptyList()
        val targetSeat = cards.indexOf(resolvedTarget) + 1
        if (targetSeat <= 0) return emptyList()
        val ruling = when {
            resolvedTarget.name == spyCard?.name && spyKey != null && registration.spyCanRegister(roleEnName) -> {
                val selected = registration.spyRegistersGood(spyKey, roleEnName)
                ClocktowerRegistrationResultPresentation.RoleRevealRuling(
                    specialRegistration = ClocktowerRegistrationResultPresentation.SpecialRegistration.Spy,
                    specialSelected = selected,
                    actualRole = resolvedTarget.clocktowerRole,
                    selectedRole = if (selected) registration.registeredRole(spyKey, spyTeams, roleEnName) else null,
                    legalSpecialRoles = registration.legalRegistrationRoles(spyKey, roleEnName, spyCard, spyTeams),
                )
            }
            resolvedTarget.name == recluseCard?.name && recluseKey != null && registration.recluseCanRegister(roleEnName) -> {
                val selected = registration.recluseRegistersEvil(recluseKey, roleEnName)
                ClocktowerRegistrationResultPresentation.RoleRevealRuling(
                    specialRegistration = ClocktowerRegistrationResultPresentation.SpecialRegistration.Recluse,
                    specialSelected = selected,
                    actualRole = resolvedTarget.clocktowerRole,
                    selectedRole = if (selected) registration.recluseRegisteredRole(recluseKey, recluseTeams, roleEnName) else null,
                    legalSpecialRoles = registration.legalRegistrationRoles(recluseKey, roleEnName, recluseCard, recluseTeams),
                )
            }
            else -> return emptyList()
        }
        return ClocktowerRegistrationResultPresentation.roleRevealOptions(
            title = title,
            targetSeat = targetSeat,
            footer = footer,
            ruling = ruling,
            roleLabel = roleLabel,
        )
    }
}
