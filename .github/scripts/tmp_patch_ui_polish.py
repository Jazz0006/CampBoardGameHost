from pathlib import Path
import subprocess

EXPECTED_BLOBS = {
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt": "2458e61afad69c463c0e79784edea194aae4c643",
    "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt": "e84572ec6dd80cafe195f6df08fafa4e0152f577",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt": "f74a3099305cc5aff3cd6f593e5a0cc69b6594bd",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt": "1038c70d5b1aa6e3fab83dff11d533a15fa63f9b",
}


def blob(path: str) -> str:
    return subprocess.check_output(["git", "hash-object", path], text=True).strip()


def replace_once(path: str, old: str, new: str) -> None:
    p = Path(path)
    text = p.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"anchor count for {path}: expected 1, got {count}\nANCHOR:\n{old}")
    p.write_text(text.replace(old, new, 1))


for path, expected in EXPECTED_BLOBS.items():
    actual = blob(path)
    if actual != expected:
        raise SystemExit(f"blob lock failed for {path}: expected {expected}, got {actual}")

night = "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt"
host = "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
chef = "app/src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt"
empath = "app/src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt"

replace_once(
    night,
    """    cards: List<PlayerCard>,\n    ghostVoteAuthority: ClocktowerGhostVoteAuthority,\n    aliveCards: List<PlayerCard>,\n""",
    """    cards: List<PlayerCard>,\n    ghostVoteAuthority: ClocktowerGhostVoteAuthority,\n    poisonedPlayerName: String?,\n    aliveCards: List<PlayerCard>,\n""",
)

replace_once(
    night,
    """    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions\n    val pairRecommendationPresentation = if (\n        phase == ClocktowerPhase.FirstNight &&\n        presentationRoleEnName in setOf(\"Washerwoman\", \"Librarian\", \"Investigator\")\n    ) {\n        clocktowerRecommendationPresentation(displayedInformationOptions)\n    } else {\n        null\n    }\n""",
    """    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions\n    val recommendationPresentation = clocktowerRecommendationPresentation(displayedInformationOptions)\n    val recommendedOptionIds = recommendationPresentation.recommendations\n        .mapTo(linkedSetOf(), ::optionId)\n    val recommendedNumericValues = recommendationPresentation.recommendations\n        .mapNotNull { option ->\n            (option.proposition as? InformationProposition.NumericResult)?.value\n                ?: option.displayPrimary?.toIntOrNull()\n        }\n        .toSet()\n""",
)

replace_once(
    night,
    """        clocktowerChefResultChoices(\n            step = step,\n            players = chefPlayers,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n            structuredNumberUiModel = structuredNumberUiModel,\n        )\n""",
    """        clocktowerChefResultChoices(\n            step = step,\n            players = chefPlayers,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n            structuredNumberUiModel = structuredNumberUiModel,\n            recommendedOptionIds = recommendedOptionIds,\n            recommendedValues = recommendedNumericValues,\n        )\n""",
)

replace_once(
    night,
    """        clocktowerEmpathResultChoices(\n            step = step,\n            players = empathPlayers,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n            structuredNumberUiModel = structuredNumberUiModel,\n        )\n""",
    """        clocktowerEmpathResultChoices(\n            step = step,\n            players = empathPlayers,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n            structuredNumberUiModel = structuredNumberUiModel,\n            recommendedOptionIds = recommendedOptionIds,\n            recommendedValues = recommendedNumericValues,\n        )\n""",
)

replace_once(
    night,
    """        clocktowerUndertakerResultChoices(\n            step = step,\n            seatCount = cards.size,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n        )\n""",
    """        clocktowerUndertakerResultChoices(\n            step = step,\n            seatCount = cards.size,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n            recommendedOptionIds = recommendedOptionIds,\n        )\n""",
)

replace_once(
    night,
    """    val clockmakerResultChoices = clocktowerClockmakerResultChoices(\n        step = step,\n        automaticStorytellerInfo = automaticStorytellerInfo,\n        automaticDisplayOption = automaticDisplayOption,\n    )\n""",
    """    val clockmakerResultChoices = clocktowerClockmakerResultChoices(\n        step = step,\n        automaticStorytellerInfo = automaticStorytellerInfo,\n        automaticDisplayOption = automaticDisplayOption,\n        recommendedOptionIds = recommendedOptionIds,\n    )\n""",
)

replace_once(
    night,
    """    val sageResultChoices = clocktowerSageResultChoices(\n        step = step,\n        seatCount = cards.size,\n        automaticStorytellerInfo = automaticStorytellerInfo,\n        automaticDisplayOption = automaticDisplayOption,\n    )\n""",
    """    val sageResultChoices = clocktowerSageResultChoices(\n        step = step,\n        seatCount = cards.size,\n        automaticStorytellerInfo = automaticStorytellerInfo,\n        automaticDisplayOption = automaticDisplayOption,\n        recommendedOptionIds = recommendedOptionIds,\n    )\n""",
)

replace_once(
    night,
    """        clocktowerRavenkeeperResultChoices(\n            step = step,\n            selectedSeat = ravenkeeperSelectedSeat,\n            seatCount = cards.size,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n        )\n""",
    """        clocktowerRavenkeeperResultChoices(\n            step = step,\n            selectedSeat = ravenkeeperSelectedSeat,\n            seatCount = cards.size,\n            automaticStorytellerInfo = automaticStorytellerInfo,\n            automaticDisplayOption = automaticDisplayOption,\n            resultFirstRegistrationCandidates = resultFirstRegistrationCandidates,\n            recommendedOptionIds = recommendedOptionIds,\n        )\n""",
)

replace_once(
    night,
    """            card.toStorytellerHostSeatPresentation(\n                seatNumber = index + 1,\n                language = language,\n                ghostVoteAuthority = ghostVoteAuthority,\n            )\n""",
    """            card.toStorytellerHostSeatPresentation(\n                seatNumber = index + 1,\n                language = language,\n                ghostVoteAuthority = ghostVoteAuthority,\n                isPoisoned = card.name == poisonedPlayerName,\n            )\n""",
)

replace_once(
    night,
    """                val resultOptions = when {\n                    step.displayOptions.isEmpty() -> emptyList()\n                    automaticStorytellerInfo -> listOfNotNull(automaticDisplayOption)\n                    else -> step.displayOptions\n                }\n                ClocktowerChambermaidSquareTableDialog(\n""",
    """                val resultOptions = when {\n                    step.displayOptions.isEmpty() -> emptyList()\n                    automaticStorytellerInfo -> listOfNotNull(automaticDisplayOption)\n                    else -> step.displayOptions\n                }\n                val recommendedResultOptions = if (automaticStorytellerInfo) {\n                    listOfNotNull(automaticDisplayOption)\n                } else {\n                    recommendationPresentation.recommendations\n                }\n                ClocktowerChambermaidSquareTableDialog(\n""",
)

replace_once(
    night,
    """                    wakeInstruction = command,\n                    resultOptions = resultOptions,\n                    language = language,\n""",
    """                    wakeInstruction = command,\n                    recommendedResultOptions = recommendedResultOptions,\n                    resultOptions = resultOptions,\n                    language = language,\n""",
)

replace_once(
    night,
    """                        recommendedOption = if (automaticStorytellerInfo) {\n                            automaticDisplayOption\n                        } else {\n                            pairRecommendationPresentation?.primary\n                        },\n""",
    """                        recommendedOptions = if (automaticStorytellerInfo) {\n                            listOfNotNull(automaticDisplayOption)\n                        } else {\n                            recommendationPresentation.recommendations\n                        },\n""",
)

replace_once(
    host,
    """                cards = cards,\n                ghostVoteAuthority = ghostVoteAuthority,\n                aliveCards = publicAliveCards,\n""",
    """                cards = cards,\n                ghostVoteAuthority = ghostVoteAuthority,\n                poisonedPlayerName = currentStep.roleEnName?.let(effectivePoisonForRole),\n                aliveCards = publicAliveCards,\n""",
)

replace_once(
    chef,
    """    structuredNumberUiModel: StructuredNumberInformationUiModel?,\n    recommendedOptionIds: Set<String> = emptySet(),\n): List<ClocktowerChefResultChoice> {\n""",
    """    structuredNumberUiModel: StructuredNumberInformationUiModel?,\n    recommendedOptionIds: Set<String> = emptySet(),\n    recommendedValues: Set<Int> = emptySet(),\n): List<ClocktowerChefResultChoice> {\n""",
)
replace_once(
    chef,
    """                recommended = choice.recommended,\n                // An impaired Chef may legally receive an arbitrary value. There is no truthful\n""",
    """                recommended = choice.value in recommendedValues ||\n                    (recommendedValues.isEmpty() && choice.recommended),\n                // An impaired Chef may legally receive an arbitrary value. There is no truthful\n""",
)

replace_once(
    empath,
    """    structuredNumberUiModel: StructuredNumberInformationUiModel?,\n    recommendedOptionIds: Set<String> = emptySet(),\n): List<ClocktowerEmpathResultChoice> {\n""",
    """    structuredNumberUiModel: StructuredNumberInformationUiModel?,\n    recommendedOptionIds: Set<String> = emptySet(),\n    recommendedValues: Set<Int> = emptySet(),\n): List<ClocktowerEmpathResultChoice> {\n""",
)
replace_once(
    empath,
    """                recommended = choice.recommended,\n                scopeSeats = scope,\n""",
    """                recommended = choice.value in recommendedValues ||\n                    (recommendedValues.isEmpty() && choice.recommended),\n                scopeSeats = scope,\n""",
)

print("guarded UI polish patch applied")
