#!/usr/bin/env python3
import argparse
import re
import subprocess
from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
NIGHT_UI = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
EXPECTED_BLOBS = {
    HOST: "cebcef091b4033287b17865468b5223772d0092d",
    NIGHT_UI: "a6f5a1da1e6daa1055b478567cffe2ea8c0b12c7",
}


def fail(message: str) -> None:
    raise SystemExit(message)


def git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True).strip()


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        fail(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


def regex_replace_once(text: str, pattern: str, replacement: str, label: str) -> str:
    updated, count = re.subn(pattern, replacement, text, count=1, flags=re.DOTALL)
    if count != 1:
        fail(f"{label}: expected exactly one regex anchor, found {count}")
    return updated


def verify_locked_inputs(expected_head: str) -> None:
    actual_head = git("rev-parse", "HEAD")
    if actual_head != expected_head:
        fail(f"HEAD drifted: expected {expected_head}, found {actual_head}")
    for path, expected_blob in EXPECTED_BLOBS.items():
        actual_blob = git("hash-object", str(path))
        if actual_blob != expected_blob:
            fail(f"{path} blob drifted: expected {expected_blob}, found {actual_blob}")


def patch_host(text: str) -> str:
    text = replace_once(
        text,
        "import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult\n",
        "import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult\n"
        "import com.codex.campboardgamehost.clocktower.session.usesAuthoritativePairDomain\n",
        "pair publication import",
    )

    migration_block = '''    fun firstNightMigrationRequest(displayStep: ClocktowerNightStepUi): FirstNightInformationRequest? {
        if (phase != ClocktowerPhase.FirstNight) return null
        val actor = displayStep.actor ?: return null
        val family = FirstNightInformationFamily.entries.firstOrNull { it.role.value == displayStep.roleEnName } ?: return null
        val sourceSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return null
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
                qualityTier = if (option.isDefaultRecommendation) {
                    QualityTier.RECOMMENDED
                } else {
                    QualityTier.ACCEPTABLE_WITH_WARNING
                },
                rankFixedPoint = when {
                    option.isDefaultRecommendation -> 1_000_000L
                    option.recommendationStyle == automaticStorytellerStyle -> 900_000L
                    else -> 800_000L
                },
                reasonCodes = option.reasonCodes,
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
            recommendationStyle = automaticStorytellerStyle,
        )
        // Keep the old presentation set solely for parity telemetry and non-pair fallback.
        val legacyOptions = (displayStep.legacyInformationCandidates + selectedOption)
            .distinctBy(::clocktowerInformationCandidateId)
        val legacyCandidates = legacyOptions.map(::legacyCandidate)
        val migratedCandidates = if (family.usesAuthoritativePairDomain()) {
            val game = cards.toClocktowerGameState(script, gameSeed, poisonTarget)
            val roleDefinitions = clocktowerRoleDefinitionsForScript(script)
            (displayStep.manualInformationCandidates + selectedOption)
                .distinctBy(::clocktowerInformationCandidateId)
                .map { option ->
                    FirstNightInformationCandidate(
                        id = clocktowerInformationCandidateId(option),
                        observation = ClocktowerPairManualAuthority.selectedObservation(
                            game = game,
                            roleDefinitions = roleDefinitions,
                            sourceSeat = sourceSeat,
                            abilityRole = family.role,
                            reliability = reliability,
                            selectedOption = option,
                        ),
                        qualityTier = if (option.isDefaultRecommendation) {
                            QualityTier.RECOMMENDED
                        } else {
                            QualityTier.ACCEPTABLE_WITH_WARNING
                        },
                        rankFixedPoint = when {
                            option.isDefaultRecommendation -> 1_000_000L
                            option.recommendationStyle == automaticStorytellerStyle -> 900_000L
                            else -> 800_000L
                        },
                        reasonCodes = option.reasonCodes,
                        warningCodes = option.warningCodes,
                    )
                }
        } else {
            legacyOptions.map(::legacyCandidate)
        }
        return FirstNightInformationRequest(
            decisionId = "first-night:${phase.name}:$round:${family.name}:$sourceSeat",
            family = family,
            sourceSeat = sourceSeat,
            reliability = reliability,
            selectedCandidateId = clocktowerInformationCandidateId(selectedOption),
            legacyCandidates = legacyCandidates,
            migratedCandidates = migratedCandidates,
        )
    }

    fun publishFirstNightInformation(displayStep: ClocktowerNightStepUi): Boolean {
        val request = firstNightMigrationRequest(displayStep) ?: return true
        val shadow = firstNightInformationMigration.shadow(request)
        firstNightPoolParity.recordResult(
            familyId = request.family.name.lowercase(),
            matches = shadow is FirstNightShadowResult.Ready,
        )
        val authoritativePairDomain = request.family.usesAuthoritativePairDomain()
        val prepared = if (authoritativePairDomain) {
            firstNightInformationMigration.publishAuthoritativePairDomain(request)
        } else {
            firstNightInformationMigration.publishIfShadowMatches(request)
        }
        // Re-entering a completed night step must not create a second information
        // event or replace the statement that the player already received.
        if (prepared.isDisplayed(request.decisionId)) return false
        // Pair families have completed the authority cutover, so their complete legal domain is
        // published even when it intentionally differs from the historical curated shortlist.
        if (authoritativePairDomain || shadow is FirstNightShadowResult.Ready) {
            firstNightInformationMigration = prepared.display(request.decisionId, request.selectedCandidateId)
        }
        return true
    }
'''
    text = regex_replace_once(
        text,
        r"    fun firstNightMigrationRequest\(displayStep: ClocktowerNightStepUi\): FirstNightInformationRequest\? \{.*?\n    \}\n    val a4DiagnosticAvailable =",
        migration_block + "    val a4DiagnosticAvailable =",
        "typed first-night migration request",
    )

    legal_helper = '''    fun legalPairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
        val sourceSeat = cards.indexOf(actor).plus(1).takeIf { it > 0 } ?: return emptyList()
        val reliability = when (
            effectiveAbilitySubjectForRole(ability.name, actor)?.let { subject ->
                AbilityFunctioningSemantics.stateFor(subject, ability.name)
            }
        ) {
            AbilityFunctioningState.DRUNK -> ReliabilityState.DRUNK
            AbilityFunctioningState.POISONED -> ReliabilityState.POISONED
            else -> ReliabilityState.RELIABLE
        }
        return ClocktowerPairManualAuthority.projectLegalOptions(
            game = cards.toClocktowerGameState(script, gameSeed, poisonTarget),
            roleDefinitions = clocktowerRoleDefinitionsForScript(script),
            sourceSeat = sourceSeat,
            abilityRole = RoleId(ability.name),
            reliability = reliability,
            presentationOptions = recommendedUnreliablePairInformationOptions(
                ability = ability,
                actor = actor,
                completeSelectionDomain = true,
            ),
        )
    }

'''
    text = replace_once(
        text,
        "    val informationStepBuilder = ClocktowerInformationStepBuilder(\n",
        legal_helper + "    val informationStepBuilder = ClocktowerInformationStepBuilder(\n",
        "legal pair presentation authority helper",
    )

    for ability in ("Washerwoman", "Librarian", "Investigator"):
        anchor = (
            "                                automaticSelectionOptions = { actor ->\n"
            "                                    recommendedUnreliablePairInformationOptions(\n"
            f"                                        ClocktowerPairInformationAbility.{ability},\n"
            "                                        actor,\n"
            "                                        completeSelectionDomain = true,\n"
            "                                    )\n"
            "                                },\n"
        )
        replacement = (
            "                                legalSelectionOptions = { actor ->\n"
            f"                                    legalPairInformationOptions(ClocktowerPairInformationAbility.{ability}, actor)\n"
            "                                },\n"
            + anchor
        )
        text = replace_once(text, anchor, replacement, f"{ability} legalSelectionOptions")

    text = regex_replace_once(
        text,
        r"                onShowPlayerDisplay = showPlayerDisplay@\{ displayStep ->\n                    if \(!informationDecisionPublicationAllowed\(displayStep\)\) return@showPlayerDisplay\n                    firstNightMigrationRequest\(displayStep\)\?\.let \{ request ->.*?\n                    \}\n                    recordReliablePrivateInformation\(displayStep\)",
        "                onShowPlayerDisplay = showPlayerDisplay@{ displayStep ->\n"
        "                    if (!informationDecisionPublicationAllowed(displayStep)) return@showPlayerDisplay\n"
        "                    if (!publishFirstNightInformation(displayStep)) return@showPlayerDisplay\n"
        "                    recordReliablePrivateInformation(displayStep)",
        "primary night-card publication callback",
    )
    text = replace_once(
        text,
        "                        onShowPlayerDisplay = showPlayerDisplay@{ displayStep ->\n"
        "                            if (!informationDecisionPublicationAllowed(displayStep)) return@showPlayerDisplay\n"
        "                            recordReliablePrivateInformation(displayStep)\n",
        "                        onShowPlayerDisplay = showPlayerDisplay@{ displayStep ->\n"
        "                            if (!informationDecisionPublicationAllowed(displayStep)) return@showPlayerDisplay\n"
        "                            if (!publishFirstNightInformation(displayStep)) return@showPlayerDisplay\n"
        "                            recordReliablePrivateInformation(displayStep)\n",
        "legacy night-card publication callback",
    )
    return text


def patch_night_ui(text: str) -> str:
    text = replace_once(
        text,
        "    // Presentation/manual mode deliberately keeps the curated compatibility surface.\n",
        "    // The legacy assisted recommendation surface keeps the curated compatibility pool.\n"
        "    // Pair Manual selection below consumes step.manualInformationCandidates directly.\n",
        "legacy assisted pool comment",
    )

    manual_state = '''    fun pairManualKey(option: ClocktowerDisplayOption): Pair<String?, List<Int>>? = when (val structured = option.proposition) {
        is InformationProposition.AnyOf -> {
            val roleAt = structured.alternatives.mapNotNull { it as? InformationProposition.RoleAt }
            if (roleAt.size != structured.alternatives.size) {
                null
            } else {
                val role = roleAt.map { it.role.value }.distinct().singleOrNull()
                val seats = roleAt.map { it.seat }.distinct().sorted()
                if (role == null || seats.size != 2) null else role to seats
            }
        }
        is InformationProposition.AllOf -> {
            val roleInPlay = structured.propositions.mapNotNull { it as? InformationProposition.RoleInPlay }
            if (roleInPlay.size == structured.propositions.size && roleInPlay.isNotEmpty() && roleInPlay.all { !it.inPlay }) {
                null to emptyList()
            } else {
                null
            }
        }
        else -> null
    }
    val manualPairEntries = if (
        phase == ClocktowerPhase.FirstNight &&
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator")
    ) {
        step.manualInformationCandidates.mapNotNull { option ->
            pairManualKey(option)?.let { key -> key to option }
        }
    } else {
        emptyList()
    }
    var showManualPairSelection by remember(step.actor?.name, step.title) { mutableStateOf(false) }
    var selectedManualPairRole by remember(step.actor?.name, step.title) { mutableStateOf<String?>(null) }
    var selectedManualPairFirstSeat by remember(step.actor?.name, step.title) { mutableStateOf<Int?>(null) }
'''
    text = replace_once(
        text,
        "    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions\n",
        "    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions\n" + manual_state,
        "manual pair state",
    )

    manual_ui = '''
            if (!automaticStorytellerInfo && manualPairEntries.isNotEmpty()) {
                Text(
                    if (language == "en") "Manual clue" else "手动选择线索",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (language == "en") {
                        "Manual choices come from the complete legal domain, even when recommendation coverage is empty or narrower."
                    } else {
                        "手动选项直接来自完整合法域，即使推荐为空或只覆盖其中一部分也仍然可用。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(
                    onClick = {
                        showManualPairSelection = !showManualPairSelection
                        if (!showManualPairSelection) {
                            selectedManualPairRole = null
                            selectedManualPairFirstSeat = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        if (showManualPairSelection) {
                            if (language == "en") "Close manual selection" else "收起手动选择"
                        } else {
                            if (language == "en") "Manually choose clue" else "手动选择线索"
                        },
                    )
                }
                if (showManualPairSelection) {
                    val zeroOutcome = manualPairEntries.firstOrNull { (key, _) -> key.first == null }
                    zeroOutcome?.let { (_, option) ->
                        OutlinedButton(
                            onClick = { showRecommendedDisplayOption(option) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(option.displayPrimary ?: option.label)
                        }
                    }
                    val roleEntries = manualPairEntries.filter { (key, _) -> key.first != null }
                    val roles = roleEntries.mapNotNull { (key, _) -> key.first }.distinct()
                    if (selectedManualPairRole == null) {
                        Text(
                            if (language == "en") "1. Choose the character to show" else "1. 选择要展示的角色",
                            fontWeight = FontWeight.SemiBold,
                        )
                        roles.forEach { role ->
                            val option = roleEntries.first { (key, _) -> key.first == role }.second
                            OutlinedButton(
                                onClick = {
                                    selectedManualPairRole = role
                                    selectedManualPairFirstSeat = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.displayPrimary ?: role)
                            }
                        }
                    } else {
                        val selectedRole = requireNotNull(selectedManualPairRole)
                        val selectedRoleEntries = roleEntries.filter { (key, _) -> key.first == selectedRole }
                        if (selectedManualPairFirstSeat == null) {
                            Text(
                                if (language == "en") "2. Choose the first player" else "2. 选择第一名玩家",
                                fontWeight = FontWeight.SemiBold,
                            )
                            selectedRoleEntries
                                .flatMap { (key, _) -> key.second }
                                .distinct()
                                .sorted()
                                .forEach { seat ->
                                    OutlinedButton(
                                        onClick = { selectedManualPairFirstSeat = seat },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(cards.getOrNull(seat - 1)?.seatLabel(cards) ?: if (language == "en") "Seat $seat" else "$seat 号")
                                    }
                                }
                        } else {
                            val firstSeat = requireNotNull(selectedManualPairFirstSeat)
                            Text(
                                if (language == "en") "3. Choose the second player" else "3. 选择第二名玩家",
                                fontWeight = FontWeight.SemiBold,
                            )
                            selectedRoleEntries
                                .filter { (key, _) -> firstSeat in key.second }
                                .flatMap { (key, _) -> key.second.filter { it != firstSeat } }
                                .distinct()
                                .sorted()
                                .forEach { secondSeat ->
                                    val option = selectedRoleEntries.firstOrNull { (key, _) ->
                                        key.second.contains(firstSeat) && key.second.contains(secondSeat)
                                    }?.second
                                    if (option != null) {
                                        OutlinedButton(
                                            onClick = { showRecommendedDisplayOption(option) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                        ) {
                                            Text(cards.getOrNull(secondSeat - 1)?.seatLabel(cards) ?: if (language == "en") "Seat $secondSeat" else "$secondSeat 号")
                                        }
                                    }
                                }
                            OutlinedButton(
                                onClick = { selectedManualPairFirstSeat = null },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (language == "en") "Choose a different first player" else "重新选择第一名玩家")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                selectedManualPairRole = null
                                selectedManualPairFirstSeat = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(if (language == "en") "Choose a different character" else "重新选择角色")
                        }
                    }
                }
            }
'''
    anchor = '''            if (
                structuredEmpathUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
'''
    text = replace_once(text, anchor, manual_ui + "\n" + anchor, "manual pair selector UI")
    return text


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--expected-head", required=True)
    args = parser.parse_args()
    verify_locked_inputs(args.expected_head)

    original_host = HOST.read_text()
    original_night = NIGHT_UI.read_text()
    updated_host = patch_host(original_host)
    updated_night = patch_night_ui(original_night)
    if updated_host == original_host or updated_night == original_night:
        fail("Patch produced no change for one or more locked targets")
    HOST.write_text(updated_host)
    NIGHT_UI.write_text(updated_night)

    changed = set(git("diff", "--name-only").splitlines())
    expected = {str(HOST), str(NIGHT_UI)}
    if changed != expected:
        fail(f"Unexpected changed paths: {sorted(changed)}")


if __name__ == "__main__":
    main()
