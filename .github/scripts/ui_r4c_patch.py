from pathlib import Path
import subprocess

EXPECTED_BASE = "975bcfa7f4b145fcad0a8ab1287454233e678a4f"
HOST_PATH = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
NIGHT_PATH = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
EXPECTED_HOST_BLOB = "cf2adc83eb459935dfd8a4b53f00c7a946f1f05f"
EXPECTED_NIGHT_BLOB = "98aebb41c79fb8007a0205268306a850dd77cb41"


def blob(path: Path) -> str:
    return subprocess.check_output(["git", "hash-object", str(path)], text=True).strip()


def require_count(text: str, anchor: str, count: int, label: str) -> None:
    actual = text.count(anchor)
    if actual != count:
        raise SystemExit(f"{label}: expected {count} anchor(s), found {actual}: {anchor!r}")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    require_count(text, old, 1, label)
    return text.replace(old, new, 1)


def insert_before_once(text: str, anchor: str, insertion: str, label: str) -> str:
    require_count(text, anchor, 1, label)
    return text.replace(anchor, insertion + anchor, 1)


if blob(HOST_PATH) != EXPECTED_HOST_BLOB:
    raise SystemExit("ClocktowerHostScreen.kt blob changed; refusing patch")
if blob(NIGHT_PATH) != EXPECTED_NIGHT_BLOB:
    raise SystemExit("ClocktowerNightStepUi.kt blob changed; refusing patch")

host = HOST_PATH.read_text(encoding="utf-8")
night = NIGHT_PATH.read_text(encoding="utf-8")

# ---- Host: Fortune Teller evaluation becomes parameterized by the hidden registration witness. ----
ft_start = "    val fortuneTellerMatched = if (fortuneTellerFirst != null && fortuneTellerSecond != null) {"
ft_end = "    val fortuneTellerResult = fortuneTellerMatched?.let { matched ->"
require_count(host, ft_start, 1, "fortune teller start")
require_count(host, ft_end, 1, "fortune teller end")
start_index = host.index(ft_start)
end_index = host.index(ft_end, start_index)
ft_replacement = '''    fun fortuneTellerMatches(recluseRegistersAsDemon: Boolean): Boolean? {
        if (fortuneTellerFirst == null || fortuneTellerSecond == null) return null
        val targets = setOf(fortuneTellerFirst, fortuneTellerSecond)
        val fortuneTellerInteractionId = ClocktowerProductionNightStepIdentity
            .role(RoleId("Fortune Teller"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        val fortuneTellerEffectiveState = lazy {
            effectiveNightStateAt(
                fortuneTellerInteractionId,
                ClocktowerInteractionBoundary.BEFORE,
            )
        }
        return cards.any { card ->
            val seat = cards.indexOf(card) + 1
            val currentRoleIsDemon =
                seat > 0 &&
                    clocktowerFortuneTellerRoleAuthority(
                        phase = phase,
                        baseRole = baseRoleIdsBySeat[seat],
                        otherNightRole = {
                            fortuneTellerEffectiveState.value.currentRoleId(seat)
                        },
                    )
                        ?.let(roleDefinitionsById::get)
                        ?.type == CharacterType.DEMON

            card.name in targets && (
                currentRoleIsDemon ||
                    card.name == redHerring ||
                    (card.name == recluseCard?.name && recluseRegistersAsDemon)
                )
        }
    }
    val fortuneTellerMatched = fortuneTellerMatches(
        recluseRegistersEvil(fortuneTellerRecluseRegistrationKey, "Fortune Teller"),
    )
'''
host = host[:start_index] + ft_replacement + host[end_index:]

# ---- Host: shared result-first candidate builders, before the phase-specific materializers. ----
night_steps_anchor = "    val nightSteps = if (phase == ClocktowerPhase.FirstNight) {"
helpers = '''    fun resultFirstNumericRegistrationOptions(
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
        val spySelectable = spyKey != null && spyCanRegister(roleEnName)
        val recluseSelectable = recluseKey != null && recluseCanRegister(roleEnName)
        if (!spySelectable && !recluseSelectable) return emptyList()
        val sourceSeat = cards.indexOf(actor) + 1
        if (sourceSeat <= 0) return emptyList()
        val witnesses = clocktowerAlignmentRegistrationWitnesses(
            currentSpyRegistersGood = spyRegistersGood(spyKey, roleEnName),
            spySelectable = spySelectable,
            currentRecluseRegistersEvil = recluseRegistersEvil(recluseKey, roleEnName),
            recluseSelectable = recluseSelectable,
        )
        return distinctClocktowerFinalInformationResults(
            witnesses.map { witness ->
                val value = valueFor(witness)
                ClocktowerDisplayOption(
                    label = value.toString(),
                    displayKind = ClocktowerDisplayKind.Number,
                    displayTitle = title,
                    displayPrimary = value.toString(),
                    displaySecondary = null,
                    displayFooter = footer,
                    proposition = InformationProposition.NumericResult(
                        metric = metric,
                        sourceSeat = sourceSeat,
                        subjectSeats = subjectSeats,
                        value = value,
                    ),
                    spyRegistersGood = witness.spyRegistersGood,
                    recluseRegistersEvil = witness.recluseRegistersEvil,
                    isTruthful = true,
                )
            },
        )
    }

    fun resultFirstFortuneTellerRegistrationOptions(actor: PlayerCard): List<ClocktowerDisplayOption> {
        val key = fortuneTellerRecluseRegistrationKey ?: return emptyList()
        if (!recluseCanRegister("Fortune Teller")) return emptyList()
        val sourceSeat = cards.indexOf(actor) + 1
        val subjectSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
            cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
        }
        if (sourceSeat <= 0 || subjectSeats.size != 2 || subjectSeats.distinct().size != 2) return emptyList()
        val current = recluseRegistersEvil(key, "Fortune Teller")
        val demonRole = completeTroubleBrewingRoles.firstOrNull { it.team == ClocktowerTeam.Demon }
        return distinctClocktowerFinalInformationResults(
            listOf(current, !current).mapNotNull { recluseEvil ->
                val value = fortuneTellerMatches(recluseEvil) ?: return@mapNotNull null
                val resultText = if (value) text("有", "Yes") else text("没有", "No")
                ClocktowerDisplayOption(
                    label = resultText,
                    displayKind = ClocktowerDisplayKind.YesNo,
                    displayTitle = text("占卜师信息", "Fortune Teller information"),
                    displayPrimary = resultText,
                    displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                        .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                        .joinToString("   ") { seatNumberText(it) }
                        .takeIf { it.isNotBlank() },
                    displayFooter = text("查询这两名玩家", "Checking these two players"),
                    proposition = InformationProposition.BooleanResult(
                        BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        sourceSeat,
                        subjectSeats,
                        value,
                    ),
                    recluseRegistersEvil = recluseEvil,
                    recluseRegisteredRoleEnName = demonRole?.enName?.takeIf { recluseEvil },
                    isTruthful = true,
                )
            },
        )
    }

    fun resultFirstRoleRevealRegistrationOptions(
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
        val candidates = mutableListOf<ClocktowerDisplayOption>()

        fun add(
            role: ClocktowerRole,
            spyGood: Boolean? = null,
            spyRole: String? = null,
            recluseEvil: Boolean? = null,
            recluseRole: String? = null,
        ) {
            candidates += ClocktowerDisplayOption(
                label = role.nameFor(language),
                displayKind = ClocktowerDisplayKind.RoleReveal,
                displayTitle = title,
                displayPrimary = role.nameFor(language),
                displaySecondary = null,
                displayFooter = footer,
                proposition = InformationProposition.RoleAt(targetSeat, RoleId(role.enName)),
                spyRegistersGood = spyGood,
                spyRegisteredRoleEnName = spyRole,
                recluseRegistersEvil = recluseEvil,
                recluseRegisteredRoleEnName = recluseRole,
                isTruthful = true,
            )
        }

        if (resolvedTarget.name == spyCard?.name && spyKey != null && spyCanRegister(roleEnName)) {
            val allowed = completeTroubleBrewingRoles.filter { it.team in spyTeams && it.enName != "Spy" }
            val currentGood = spyRegistersGood(spyKey, roleEnName)
            if (currentGood) {
                registeredRole(spyKey, spyTeams, roleEnName)?.let { add(it, true, it.enName) }
            }
            resolvedTarget.clocktowerRole?.let { add(it, false) }
            allowed.forEach { role -> add(role, true, role.enName) }
        } else if (
            resolvedTarget.name == recluseCard?.name &&
            recluseKey != null &&
            recluseCanRegister(roleEnName)
        ) {
            val allowed = completeTroubleBrewingRoles.filter { it.team in recluseTeams }
            val currentEvil = recluseRegistersEvil(recluseKey, roleEnName)
            if (currentEvil) {
                recluseRegisteredRole(recluseKey, recluseTeams, roleEnName)?.let { add(it, recluseEvil = true, recluseRole = it.enName) }
            }
            resolvedTarget.clocktowerRole?.let { add(it, recluseEvil = false) }
            allowed.forEach { role -> add(role, recluseEvil = true, recluseRole = role.enName) }
        }
        return distinctClocktowerFinalInformationResults(candidates)
    }

'''
host = insert_before_once(host, night_steps_anchor, helpers, "result-first helper insertion")

# Chef: exact healthy final numeric results under every legal alignment registration witness.
chef_anchor = "                                spyRegistrationKey = chefRegistrationKey,"
require_count(host, chef_anchor, 1, "chef registration")
chef_legal = '''                                legalSelectionOptions = { actor ->
                                    if (chefAbilityUnreliable) {
                                        emptyList()
                                    } else {
                                        resultFirstNumericRegistrationOptions(
                                            title = text("厨师信息", "Chef information"),
                                            actor = actor,
                                            roleEnName = "Chef",
                                            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                                            subjectSeats = cards.indices.map { it + 1 },
                                            footer = text("邪恶玩家相邻对数", "Adjacent evil pairs"),
                                            spyKey = chefRegistrationKey,
                                            recluseKey = chefRecluseRegistrationKey,
                                        ) { witness ->
                                            chefEvilPairs(cards) { card ->
                                                when {
                                                    card.name == spyCard?.name && witness.spyRegistersGood != null -> !witness.spyRegistersGood
                                                    card.name == recluseCard?.name && witness.recluseRegistersEvil != null -> witness.recluseRegistersEvil
                                                    else -> isClocktowerEvil(card)
                                                }
                                            }
                                        }
                                    }
                                },
'''
host = host.replace(chef_anchor, chef_legal + chef_anchor, 1)

# Empath exists in both first-night and other-night materializers.
empath_anchor = "                                spyRegistrationKey = empathRegistrationKey,"
require_count(host, empath_anchor, 2, "empath registration")
empath_legal = '''                                legalSelectionOptions = { actor ->
                                    if (empathAbilityUnreliable) {
                                        emptyList()
                                    } else {
                                        resultFirstNumericRegistrationOptions(
                                            title = text("共情者信息", "Empath information"),
                                            actor = actor,
                                            roleEnName = "Empath",
                                            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                                            subjectSeats = empathNeighbors.map { cards.indexOf(it) + 1 },
                                            footer = text("邪恶存活邻居数量", "Evil living neighbors"),
                                            spyKey = empathRegistrationKey,
                                            recluseKey = empathRecluseRegistrationKey,
                                        ) { witness ->
                                            empathNeighbors.count { card ->
                                                when {
                                                    card.name == spyCard?.name && witness.spyRegistersGood != null -> !witness.spyRegistersGood
                                                    card.name == recluseCard?.name && witness.recluseRegistersEvil != null -> witness.recluseRegistersEvil
                                                    else -> isClocktowerEvil(card)
                                                }
                                            }
                                        }
                                    }
                                },
'''
host = host.replace(empath_anchor, empath_legal + empath_anchor)

# Fortune Teller exists on both night schedules.
ft_registration_anchor = "                                recluseRegistrationKey = fortuneTellerRecluseRegistrationKey,"
require_count(host, ft_registration_anchor, 2, "fortune teller registration")
ft_legal = '''                                legalSelectionOptions = { actor ->
                                    if (stepInformationAbilityUnreliable(actor, "Fortune Teller")) {
                                        emptyList()
                                    } else {
                                        resultFirstFortuneTellerRegistrationOptions(actor)
                                    }
                                },
'''
# There is no stable public local named stepInformationAbilityUnreliable; use the existing actor reliability predicate directly.
ft_legal = ft_legal.replace(
    'stepInformationAbilityUnreliable(actor, "Fortune Teller")',
    'actorIsUnreliable("Fortune Teller", actor)',
)
host = host.replace(ft_registration_anchor, ft_legal + ft_registration_anchor)

# Undertaker and Ravenkeeper role-reveal legal domains.
undertaker_anchor = "                        spyRegistrationKey = undertakerRegistrationKey,"
require_count(host, undertaker_anchor, 1, "undertaker registration")
undertaker_legal = '''                        legalSelectionOptions = { actor ->
                            if (actorIsUnreliable("Undertaker", actor)) {
                                emptyList()
                            } else {
                                resultFirstRoleRevealRegistrationOptions(
                                    title = text("送葬者信息", "Undertaker information"),
                                    roleEnName = "Undertaker",
                                    target = undertakerTarget,
                                    footer = text("今天被处决：${playerSeatLabel(cards, lastExecutedName)}", "Executed today: ${playerSeatLabel(cards, lastExecutedName)}"),
                                    spyKey = undertakerRegistrationKey,
                                    spyTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                    recluseKey = undertakerRecluseRegistrationKey,
                                    recluseTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                                )
                            }
                        },
'''
host = host.replace(undertaker_anchor, undertaker_legal + undertaker_anchor, 1)

raven_anchor = "                        spyRegistrationKey = ravenkeeperRegistrationKey,"
require_count(host, raven_anchor, 1, "ravenkeeper registration")
raven_legal = '''                        legalSelectionOptions = { actor ->
                            if (actorIsUnreliable("Ravenkeeper", actor)) {
                                emptyList()
                            } else {
                                resultFirstRoleRevealRegistrationOptions(
                                    title = text("守鸦人信息", "Ravenkeeper information"),
                                    roleEnName = "Ravenkeeper",
                                    target = ravenkeeperTargetCard,
                                    footer = ravenkeeperTarget?.let { text("查询目标：${playerSeatLabel(cards, it)}", "Checked player: ${playerSeatLabel(cards, it)}") }.orEmpty(),
                                    spyKey = ravenkeeperRegistrationKey,
                                    spyTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                    recluseKey = ravenkeeperRecluseRegistrationKey,
                                    recluseTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                                )
                            }
                        },
'''
host = host.replace(raven_anchor, raven_legal + raven_anchor, 1)

# ---- NightStep: consume exact final-result domains and hide redundant registration controls. ----
show_manual_anchor = "    var showManualPairSelection by remember("
result_state = '''    val usesResultFirstRegistration =
        !automaticStorytellerInfo && step.usesResultFirstRegistrationDomain()
    val resultFirstRegistrationCandidates = if (usesResultFirstRegistration) {
        distinctClocktowerFinalInformationResults(step.manualInformationCandidates)
    } else {
        emptyList()
    }
    val nonPairResultFirstCandidates = resultFirstRegistrationCandidates.takeUnless {
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator") ||
            step.action == ClocktowerNightAction.FortuneTeller
    }.orEmpty()

'''
night = insert_before_once(night, show_manual_anchor, result_state, "result-first state")

ft_results_start = "    val fortuneTellerLegalResults = structuredFortuneTellerUiModel"
ft_results_end = "    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel"
require_count(night, ft_results_start, 1, "night FT result start")
require_count(night, ft_results_end, 1, "night FT result end")
start_index = night.index(ft_results_start)
end_index = night.index(ft_results_end, start_index)
ft_results_replacement = '''    val resultFirstFortuneTellerOptions = resultFirstRegistrationCandidates.filter { option ->
        val proposition = option.proposition as? InformationProposition.BooleanResult
        proposition?.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT
    }
    val fortuneTellerLegalResults = if (resultFirstFortuneTellerOptions.isNotEmpty()) {
        resultFirstFortuneTellerOptions.mapNotNullTo(linkedSetOf()) { option ->
            (option.proposition as? InformationProposition.BooleanResult)?.value
        }
    } else {
        structuredFortuneTellerUiModel
            ?.choices
            ?.mapTo(linkedSetOf()) { choice -> choice.value }
            .orEmpty()
    }
    val fortuneTellerRecommendedResult = structuredFortuneTellerUiModel
        ?.choices
        ?.firstOrNull { choice -> choice.recommended && choice.value in fortuneTellerLegalResults }
        ?.value
'''
night = night[:start_index] + ft_results_replacement + night[end_index:]

show_ft_anchor = "    fun showStructuredFortuneTellerResult(value: Boolean) {\n"
show_ft_insert = '''    fun showStructuredFortuneTellerResult(value: Boolean) {
        resultFirstFortuneTellerOptions.firstOrNull { option ->
            (option.proposition as? InformationProposition.BooleanResult)?.value == value
        }?.let { option ->
            showRecommendedDisplayOption(option)
            return
        }
'''
night = replace_once(night, show_ft_anchor, show_ft_insert, "FT result handler")

night = replace_once(
    night,
    "            if (step.spyRegistrationKey != null && spyCard != null) {",
    "            if (!usesResultFirstRegistration && step.spyRegistrationKey != null && spyCard != null && spyCanRegister) {",
    "spy panel suppression",
)
night = replace_once(
    night,
    "            if (step.recluseRegistrationKey != null && recluseCard != null) {",
    "            if (!usesResultFirstRegistration && step.recluseRegistrationKey != null && recluseCard != null && recluseCanRegister) {",
    "recluse panel suppression",
)

night = replace_once(
    night,
    "                    secondaryActionEnabled = step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None,",
    "                    secondaryActionEnabled = resultFirstRegistrationCandidates.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None,",
    "ravenkeeper direct reveal guard",
)

night = replace_once(
    night,
    "                displayedInformationOptions.isNotEmpty() &&\n                step.action != ClocktowerNightAction.FortuneTeller",
    "                resultFirstRegistrationCandidates.isEmpty() &&\n                displayedInformationOptions.isNotEmpty() &&\n                step.action != ClocktowerNightAction.FortuneTeller",
    "generic recommendation guard",
)

result_section_anchor = "            if (manualPairCandidates.isNotEmpty()) {"
result_section = '''            if (nonPairResultFirstCandidates.isNotEmpty()) {
                Text(
                    if (language == "en") "Choose the final information" else "选择最终展示信息",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (language == "en") {
                        "Any Spy or Recluse registration needed for the chosen result is resolved automatically."
                    } else {
                        "选择结果即可；该结果所需的间谍或隐士登记会自动完成。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                nonPairResultFirstCandidates.forEach { option ->
                    OutlinedButton(
                        onClick = { showRecommendedDisplayOption(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                }
            }

'''
night = insert_before_once(night, result_section_anchor, result_section, "result-first controls")

night = replace_once(
    night,
    "                        roleLabel = { roleId -> roleId },",
    "                        roleLabel = { roleId -> clocktowerRoleLabel(com.codex.campboardgamehost.clocktower.domain.RoleId(roleId), language) },",
    "pair role localization",
)

night = replace_once(
    night,
    "                structuredNumberUiModel == null &&\n                structuredFortuneTellerUiModel == null &&\n                firstNightPool == null && step.displayOptions.isNotEmpty() &&",
    "                resultFirstRegistrationCandidates.isEmpty() &&\n                structuredNumberUiModel == null &&\n                structuredFortuneTellerUiModel == null &&\n                firstNightPool == null && step.displayOptions.isNotEmpty() &&",
    "unreliable display guard",
)
night = replace_once(
    night,
    "            } else if (structuredNumberUiModel == null && step.recommendedDisplayOptions.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid) {",
    "            } else if (resultFirstRegistrationCandidates.isEmpty() && structuredNumberUiModel == null && step.recommendedDisplayOptions.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid) {",
    "direct reveal guard",
)

HOST_PATH.write_text(host, encoding="utf-8", newline="\n")
NIGHT_PATH.write_text(night, encoding="utf-8", newline="\n")

print("UI-R4C large-file patch applied")
