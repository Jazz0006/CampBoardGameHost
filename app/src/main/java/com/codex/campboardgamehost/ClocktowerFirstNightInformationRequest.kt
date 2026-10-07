package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationCandidate
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationRequest
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.PairInformationDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.PairInformationDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.usesAuthoritativePairDomain

/** Host-to-migration adapter. Preserves legacy parity templates; pair legality stays upstream. */
internal fun clocktowerFirstNightInformationRequest(
    displayStep: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    gameSeed: Long,
    poisonTarget: String?,
    language: String,
    firstNightPairDecisionContext: TroubleBrewingFirstNightPairDecisionContext? = null,
): FirstNightInformationRequest? {
    if (phase != ClocktowerPhase.FirstNight) return null
    val actor = displayStep.actor ?: return null
    val family = FirstNightInformationFamily.entries.firstOrNull { it.role.value == displayStep.roleEnName } ?: return null
    val sourceSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return null
    val decisionId = "first-night:${phase.name}:$round:${family.name}:$sourceSeat"
    val reliability = when (displayStep.informationReliability) {
        InformationReliability.RELIABLE -> ReliabilityState.RELIABLE
        InformationReliability.DRUNK -> ReliabilityState.DRUNK
        InformationReliability.POISONED -> ReliabilityState.POISONED
    }
    fun legacyCandidate(option: ClocktowerDisplayOption): FirstNightInformationCandidate {
        val primary = option.displayPrimary
        return FirstNightInformationCandidate(clocktowerInformationCandidateId(option), AbilityObservation(
            sourceSeat = sourceSeat,
            perceivedRole = family.role,
            shownRole = primary?.takeIf { option.displayKind == ClocktowerDisplayKind.EitherOne }
                ?.let { shown -> clocktowerRolesForScript(script).firstOrNull { it.nameFor(language) == shown }?.enName ?: shown }
                ?.let(::RoleId),
            candidateSeats = DecisionHistoryRepository.extractSeatNumbers(
                listOf(option.displaySecondary, option.displayFooter), cards.size,
            ).toList(),
            shownNumber = primary?.toIntOrNull(),
            shownAnswer = primary?.let { answer ->
                when (answer) {
                    "有", "Yes" -> YesNoAnswer.YES
                    "没有", "No" -> YesNoAnswer.NO
                    else -> null
                }
            },
            reliability = reliability,
            semanticTruth = if (option.isTruthful) SemanticTruth.TRUE else SemanticTruth.FALSE,
        ),
            qualityTier = QualityTier.RECOMMENDED,
            reasonCodes = emptyList(),
            warningCodes = option.warningCodes,
        )
    }
    val selectedOption = ClocktowerDisplayOption(
        label = "selected",
        displayKind = displayStep.displayKind,
        displayTitle = displayStep.displayTitle,
        displayPrimary = displayStep.displayPrimary ?: displayStep.tellPlayer,
        displaySecondary = displayStep.displaySecondary,
        displayFooter = displayStep.displayFooter,
        proposition = displayStep.displayProposition,
        isTruthful = displayStep.selectedInformationTruthful != false,
    )
    // Keep the old presentation set solely for parity telemetry and non-pair fallback.
    val legacyOptions = (displayStep.legacyInformationCandidates + selectedOption)
        .distinctBy(::clocktowerInformationCandidateId)
    val legacyCandidates = legacyOptions.map(::legacyCandidate)
    val migrated = if (family.usesAuthoritativePairDomain()) {
        val pairContext = if (script == ClocktowerScript.TroubleBrewing) {
            requireNotNull(firstNightPairDecisionContext) {
                "Trouble Brewing pair publication requires snapshot-backed first-night context."
            }
        } else {
            null
        }
        val legacyGame = if (pairContext == null) {
            cards.toClocktowerGameState(script, gameSeed, poisonTarget)
        } else {
            null
        }
        val legacyRoleDefinitions = if (pairContext == null) {
            clocktowerRoleDefinitionsForScript(script)
        } else {
            null
        }
        val pendingPairDecision = pairContext?.let { context ->
            val gameStateRevision = (context.snapshot.position.gameStateRevision as? SnapshotField.Known<Long>)?.value
                ?: error("Pair-information runtime snapshot requires a known game-state revision.")
            val playerInputRevision = (context.snapshot.position.playerInputRevision as? SnapshotField.Known<Long>)?.value
                ?: error("Pair-information runtime snapshot requires a known player-input revision.")
            PairInformationDecisionBoundary.create(
                requestIdentity = InformationDecisionRequestIdentity(
                    gameId = context.snapshot.gameId,
                    requestId = decisionId,
                ),
                revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
                game = context.naturalPairGameState,
                roleDefinitions = context.roleDefinitions,
                sourceSeat = sourceSeat,
                abilityRole = family.role,
                reliability = reliability,
            )
        }
        fun candidateIdFor(option: ClocktowerDisplayOption): String =
            pendingPairDecision?.let { decision ->
                ClocktowerPairManualAuthority.selectedCandidateId(
                    decision = decision,
                    selectedOption = option,
                )
            } ?: clocktowerInformationCandidateId(option)

        (displayStep.manualInformationCandidates + selectedOption)
            .distinctBy(::clocktowerInformationCandidateId)
            .map { option ->
                val candidateId = candidateIdFor(option)
                FirstNightInformationCandidate(
                    id = candidateId,
                    observation = if (pendingPairDecision != null) {
                        when (val confirmation = pendingPairDecision.confirm(
                            candidateId = candidateId,
                            currentRevision = pendingPairDecision.revision,
                        )) {
                            is PairInformationDecisionConfirmation.Confirmed -> confirmation.observation
                            is PairInformationDecisionConfirmation.Blocked ->
                                error("Current Manual pair-information selection was blocked: ${confirmation.reason}")
                        }
                    } else {
                        ClocktowerPairManualAuthority.selectedObservation(
                            game = requireNotNull(legacyGame),
                            roleDefinitions = requireNotNull(legacyRoleDefinitions),
                            sourceSeat = sourceSeat,
                            abilityRole = family.role,
                            reliability = reliability,
                            selectedOption = option,
                        )
                    },
                    qualityTier = QualityTier.RECOMMENDED,
                    reasonCodes = emptyList(),
                    warningCodes = option.warningCodes,
                )
            }
            .let { candidates -> candidates to candidateIdFor(selectedOption) }
    } else {
        legacyOptions.map(::legacyCandidate) to clocktowerInformationCandidateId(selectedOption)
    }
    return FirstNightInformationRequest(
        decisionId = decisionId,
        family = family,
        sourceSeat = sourceSeat,
        reliability = reliability,
        selectedCandidateId = migrated.second,
        legacyCandidates = legacyCandidates,
        migratedCandidates = migrated.first,
    )
}
