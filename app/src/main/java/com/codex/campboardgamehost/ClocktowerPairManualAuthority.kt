package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationDecision
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.rules.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.session.PendingPairInformationDecision

/**
 * Manual presentation adapter for Washerwoman/Librarian/Investigator information.
 *
 * [PairInformationLegalDomain] is the only authority for which outcomes are selectable. Existing
 * display options are presentation templates only: they provide localized labels and layout, but
 * they are never allowed to add or remove a legal outcome. Selection commit resolves the chosen
 * structured proposition back through the same legal domain so registration facts cannot be
 * reconstructed from localized UI text.
 */
internal object ClocktowerPairManualAuthority {
    /** Interpret already-authoritative options once, before they reach the Manual renderer. */
    fun selectionPresentation(options: List<ClocktowerDisplayOption>): ClocktowerPairManualPresentation {
        val pairs = mutableListOf<ClocktowerPairManualCandidate>()
        var zeroCase: ClocktowerDisplayOption? = null
        options.forEach { option ->
            val key = option.pairInformationKeyOrNull() ?: return@forEach
            if (key.shownRole != null) {
                pairs += ClocktowerPairManualCandidate(option, key.shownRole.value, key.candidateSeats)
            } else if (zeroCase == null) {
                zeroCase = option
            }
        }
        return ClocktowerPairManualPresentation(options.toList(), pairs, zeroCase)
    }

    /**
     * Maps a recommendation projection back into the complete Manual legal presentation using only
     * its structured pair proposition. Recommendation labels and ranking metadata are presentation
     * concerns and are deliberately ignored here.
     */
    fun canonicalManualOption(
        presentation: ClocktowerPairManualPresentation,
        recommendedOption: ClocktowerDisplayOption?,
    ): ClocktowerDisplayOption? {
        val key = recommendedOption?.pairInformationKeyOrNull() ?: return null
        return presentation.canonicalOptionFor(key)
    }

    /** Map one rules-owned legal candidate back into the current canonical Manual presentation. */
    fun canonicalManualOption(
        presentation: ClocktowerPairManualPresentation,
        candidate: PairInformationLegalCandidate?,
    ): ClocktowerDisplayOption? = candidate
        ?.outcome
        ?.pairInformationKey()
        ?.let(presentation::canonicalOptionFor)

    /** Resolve a presentation selection to the stable ID owned by an engine/session pending decision. */
    fun selectedCandidateId(
        decision: PendingPairInformationDecision,
        selectedOption: ClocktowerDisplayOption,
    ): String {
        val key = requireNotNull(selectedOption.pairInformationKeyOrNull()) {
            "Pair-information selection requires a structured pair proposition."
        }
        return requireNotNull(
            decision.candidateIdFor(
                shownRole = key.shownRole,
                candidateSeats = key.candidateSeats,
            ),
        ) {
            "Selected pair-information outcome is not in the current legal domain: $key"
        }
    }

    fun projectLegalOptions(
        context: TroubleBrewingFirstNightPairDecisionContext,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        presentationOptions: List<ClocktowerDisplayOption>,
    ): List<ClocktowerDisplayOption> = projectLegalOptions(
        game = context.naturalPairGameState,
        roleDefinitions = context.roleDefinitions,
        sourceSeat = sourceSeat,
        abilityRole = abilityRole,
        reliability = reliability,
        presentationOptions = presentationOptions,
    )

    fun projectLegalOptions(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        presentationOptions: List<ClocktowerDisplayOption>,
    ): List<ClocktowerDisplayOption> {
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
        val templatesByKey = presentationOptions
            .mapNotNull { option -> option.pairInformationKeyOrNull()?.let { it to option } }
            .groupBy({ it.first }, { it.second })

        return legalCandidates.map { candidate ->
            val key = candidate.outcome.pairInformationKey()
            val template = requireNotNull(templatesByKey[key]?.firstOrNull()) {
                "Missing pair-information presentation template for $abilityRole / $key"
            }
            val spyRegistration = candidate.registrations
                .firstOrNull { it.reason == RegistrationReason.SPY_ABILITY }
            val recluseRegistration = candidate.registrations
                .firstOrNull { it.reason == RegistrationReason.RECLUSE_ABILITY }
            template.copy(
                isTruthful = candidate.semanticTruth == SemanticTruth.TRUE,
                misinformationPressure = if (candidate.semanticTruth == SemanticTruth.TRUE) {
                    0
                } else {
                    template.misinformationPressure.coerceAtLeast(1)
                },
                spyRegistersGood = spyRegistration?.let { true },
                spyRegisteredRoleEnName = spyRegistration?.registeredRole?.value,
                recluseRegistersEvil = recluseRegistration?.let { true },
                recluseRegisteredRoleEnName = recluseRegistration?.registeredRole?.value,
            )
        }
    }

    fun selectedObservation(
        context: TroubleBrewingFirstNightPairDecisionContext,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        selectedOption: ClocktowerDisplayOption,
    ): AbilityObservation = selectedObservation(
        game = context.naturalPairGameState,
        roleDefinitions = context.roleDefinitions,
        sourceSeat = sourceSeat,
        abilityRole = abilityRole,
        reliability = reliability,
        selectedOption = selectedOption,
    )

    fun selectedObservation(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        selectedOption: ClocktowerDisplayOption,
    ): AbilityObservation {
        val selectedKey = requireNotNull(selectedOption.pairInformationKeyOrNull()) {
            "Pair-information selection requires a structured pair proposition."
        }
        val selectedCandidate = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        ).singleOrNull { candidate -> candidate.outcome.pairInformationKey() == selectedKey }
            ?: error("Selected pair-information outcome is not in the current legal domain: $selectedKey")

        return selectedCandidate.toAbilityObservation(
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
    }
}

internal fun PairInformationLegalCandidate.toAbilityObservation(
    sourceSeat: Int,
    abilityRole: RoleId,
    reliability: ReliabilityState,
): AbilityObservation = AbilityObservation(
    sourceSeat = sourceSeat,
    perceivedRole = abilityRole,
    shownRole = outcome.shownRole,
    candidateSeats = outcome.candidateSeats,
    reliability = reliability,
    semanticTruth = semanticTruth,
    registrations = registrations.map { registration ->
        RegistrationDecision(
            playerSeat = registration.subjectSeat,
            affectedAbility = abilityRole,
            registeredAlignment = registration.registeredAlignment,
            registeredType = registration.registeredType,
            registeredRole = registration.registeredRole,
            reason = registration.reason,
        )
    },
)

private data class PairInformationPresentationKey(
    val shownRole: RoleId?,
    val candidateSeats: List<Int>,
)

private fun PairInformationOutcome.pairInformationKey(): PairInformationPresentationKey =
    PairInformationPresentationKey(
        shownRole = shownRole,
        candidateSeats = candidateSeats,
    )

// Block body is intentional because malformed structured propositions fail closed with early returns.
private fun ClocktowerDisplayOption.pairInformationKeyOrNull(): PairInformationPresentationKey? {
    return when (val structured = proposition) {
        is InformationProposition.AnyOf -> {
            val roleAt = structured.alternatives.map { alternative ->
                alternative as? InformationProposition.RoleAt ?: return null
            }
            val shownRole = roleAt.map { it.role }.distinct().singleOrNull() ?: return null
            val seats = roleAt.map { it.seat }.distinct().sorted()
            if (seats.size != 2) return null
            PairInformationPresentationKey(
                shownRole = shownRole,
                candidateSeats = seats,
            )
        }

        is InformationProposition.AllOf -> {
            val roleInPlay = structured.propositions.map { proposition ->
                proposition as? InformationProposition.RoleInPlay ?: return null
            }
            if (roleInPlay.isEmpty() || roleInPlay.any { it.inPlay }) return null
            PairInformationPresentationKey(
                shownRole = null,
                candidateSeats = emptyList(),
            )
        }

        else -> null
    }
}

/** Original options participate in equality to preserve candidate-change selection resets,
 * including changes to ignored malformed options. They are not exposed to the renderer.
 */
internal data class ClocktowerPairManualPresentation(
    private val sourceOptions: List<ClocktowerDisplayOption>,
    val candidates: List<ClocktowerPairManualCandidate>,
    val zeroCaseOption: ClocktowerDisplayOption?,
)

private fun ClocktowerPairManualPresentation.canonicalOptionFor(
    key: PairInformationPresentationKey,
): ClocktowerDisplayOption? = if (key.shownRole == null) {
    zeroCaseOption
} else {
    candidates.firstOrNull { candidate ->
        candidate.roleId == key.shownRole.value && candidate.seats == key.candidateSeats
    }?.option
}

internal data class ClocktowerPairManualCandidate(
    val option: ClocktowerDisplayOption,
    val roleId: String,
    val seats: List<Int>,
)
