from pathlib import Path
import subprocess

RED_HEAD = "a185589e4400ced6983d25603a013f4f36d2e235"
BRANCH = "codex/ms-setup-generic-architecture"
PRESENTATION = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerHostPresentationModels.kt")
BUILDER = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt")
NIGHT_UI = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
SCRIPT = "tools/oneshot_patch_a185589_pair_domain.py"
WORKFLOW = ".github/workflows/oneshot_patch_a185589_pair_domain.yml"
EXPECTED_BLOBS = {
    PRESENTATION: "67443fcee6f98e0cf78c486847f6c5af990b5501",
    BUILDER: "cd7e5540939d484f539588241c3bcb7976da02b9",
    NIGHT_UI: "883e880b6b1d160ae9216b48a19f3034d44d01f3",
    HOST: "f73876fc9c35cf7ec29ecb71d1865a7b1794e324",
}


def run(*args: str) -> str:
    print("+", " ".join(args), flush=True)
    return subprocess.check_output(args, text=True).strip()


def check(*args: str) -> None:
    print("+", " ".join(args), flush=True)
    subprocess.check_call(args)


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


def replace_span(text: str, start: str, end: str, replacement: str, label: str) -> str:
    if text.count(start) != 1 or text.count(end) != 1:
        raise SystemExit(
            f"{label}: expected unique span markers, got start={text.count(start)} end={text.count(end)}"
        )
    start_index = text.index(start)
    end_index = text.index(end, start_index)
    return text[:start_index] + replacement + text[end_index:]


if subprocess.call(["git", "merge-base", "--is-ancestor", RED_HEAD, "HEAD"]) != 0:
    raise SystemExit(f"RED checkpoint {RED_HEAD} is not an ancestor of HEAD")
for path, expected in EXPECTED_BLOBS.items():
    actual = run("git", "hash-object", str(path))
    if actual != expected:
        raise SystemExit(f"blob drift for {path}: expected {expected}, got {actual}")
if run("git", "status", "--porcelain"):
    raise SystemExit("working tree is not clean")

# 1) Presentation model: keep the complete automatic semantic domain distinct
# from the curated/manual compatibility surface.
presentation = PRESENTATION.read_text(encoding="utf-8")
presentation = replace_once(
    presentation,
    """    val legacyInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    val decisionOptions: List<ClocktowerDecisionOption> = emptyList(),
""",
    """    val legacyInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    /**
     * Candidate domain used by automatic first-night information selection. Pair-information
     * abilities may populate this with the complete legal semantic domain while keeping
     * [legacyInformationCandidates] curated for assisted/manual presentation.
     */
    val automaticInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    val decisionOptions: List<ClocktowerDecisionOption> = emptyList(),
""",
    "presentation automatic candidate domain",
)
PRESENTATION.write_text(presentation, encoding="utf-8")

# 2) Step builder: accept an optional expanded automatic-only domain and fall back exactly to
# the existing candidate surface for every caller that does not supply one.
builder = BUILDER.read_text(encoding="utf-8")
builder = replace_once(
    builder,
    """        displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        reliableDisplayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
""",
    """        displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        automaticSelectionOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        reliableDisplayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
""",
    "builder automatic callback parameter",
)
builder = replace_once(
    builder,
    """        val unreliableOptions = actor?.takeIf { actorAbilityUnreliable }?.let(displayOptions).orEmpty()
        val reliableRecommendations = actor?.takeUnless { actorAbilityUnreliable }?.let(reliableDisplayOptions).orEmpty()
""",
    """        val unreliableOptions = actor?.takeIf { actorAbilityUnreliable }?.let(displayOptions).orEmpty()
        val automaticSelectionDomain = actor
            ?.takeIf { actorAbilityUnreliable && automaticStorytellerInfo }
            ?.let(automaticSelectionOptions)
            .orEmpty()
        val reliableRecommendations = actor?.takeUnless { actorAbilityUnreliable }?.let(reliableDisplayOptions).orEmpty()
""",
    "builder automatic candidate resolution",
)
builder = replace_once(
    builder,
    """                ).joinToString("|")
            }
        return ClocktowerNightStepUi(
""",
    """                ).joinToString("|")
            }
        val automaticInformationCandidates = automaticSelectionDomain
            .takeIf { it.isNotEmpty() }
            ?.distinctBy(::clocktowerInformationCandidateId)
            ?: completeLegacyCandidates
        return ClocktowerNightStepUi(
""",
    "builder automatic candidate fallback",
)
builder = replace_once(
    builder,
    """            legacyInformationCandidates = completeLegacyCandidates,
            roleEnName = enName,
""",
    """            legacyInformationCandidates = completeLegacyCandidates,
            automaticInformationCandidates = automaticInformationCandidates,
            roleEnName = enName,
""",
    "builder step projection",
)
BUILDER.write_text(builder, encoding="utf-8")

# 3) First-night UI: build a full AUTO pool and a curated ASSISTED pool. Numeric roles still
# pass through their already-accepted registration-aware projection on both sources.
night_ui = NIGHT_UI.read_text(encoding="utf-8")
night_ui = replace_span(
    night_ui,
    "    val firstNightNumericSourceSeat =",
    "    val displayedInformationOptions =",
    r'''    fun projectFirstNightCandidateSource(
        source: List<ClocktowerDisplayOption>,
    ): List<ClocktowerDisplayOption> {
        val numericSourceSeat = source
            .asSequence()
            .mapNotNull { (it.proposition as? InformationProposition.NumericResult)?.sourceSeat }
            .firstOrNull()
        return numericSourceSeat?.let { sourceSeat ->
            val poisonedPlayerName = cards
                .getOrNull(sourceSeat - 1)
                ?.name
                ?.takeIf { step.informationReliability == InformationReliability.POISONED }
            projectFirstNightNumericInformationOptions(
                phase = phase,
                roleEnName = step.roleEnName.orEmpty(),
                sourceSeat = sourceSeat,
                players = cards.toClocktowerPlayerStates(poisonedPlayerName = poisonedPlayerName),
                options = source,
            )
        } ?: source
    }
    val projectedFirstNightInformationCandidates =
        projectFirstNightCandidateSource(step.legacyInformationCandidates)
    val projectedAutomaticFirstNightInformationCandidates = projectFirstNightCandidateSource(
        step.automaticInformationCandidates.ifEmpty { step.legacyInformationCandidates },
    )
    val firstNightAutomaticPool = projectedAutomaticFirstNightInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: "first-night-information",
            automaticStyle = automaticStorytellerStyle,
        ) }
    // Presentation/manual mode deliberately keeps the curated compatibility surface.
    val firstNightPool = projectedFirstNightInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: "first-night-information",
            automaticStyle = automaticStorytellerStyle,
        ) }
    var firstNightPoolBenchmarkRuns by remember(
        phase,
        step.roleEnName,
        projectedAutomaticFirstNightInformationCandidates,
        automaticStorytellerStyle,
    ) { mutableStateOf(0) }
    var firstNightPoolBenchmarkReport by remember { mutableStateOf<UnifiedSelectionPoolDeviceBenchmarkReport?>(null) }
    var firstNightPoolBenchmarkError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(firstNightPoolBenchmarkRuns) {
        if (firstNightPoolBenchmarkRuns == 0) return@LaunchedEffect
        firstNightPoolBenchmarkReport = null
        firstNightPoolBenchmarkError = null
        runCatching {
            val options = requireNotNull(projectedAutomaticFirstNightInformationCandidates.takeIf {
                phase == ClocktowerPhase.FirstNight && it.isNotEmpty()
            })
            val family = step.roleEnName ?: "first-night-information"
            withContext(Dispatchers.Default) {
                UnifiedSelectionPoolDeviceBenchmark.run(
                    poolFactory = {
                        unifiedFirstNightInformationPool(options, family, automaticStorytellerStyle)
                    },
                    playerCount = cards.size,
                    phase = StorytellerPhase.FIRST_NIGHT,
                    style = automaticStorytellerStyle,
                    styleOf = ClocktowerDisplayOption::recommendationStyle,
                )
            }
        }.onSuccess { report ->
            firstNightPoolBenchmarkReport = report
            Log.i(
                UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG,
                report.toLogLine(step.roleEnName ?: "first-night-information"),
            )
        }.onFailure { error ->
            firstNightPoolBenchmarkError = error.message ?: error.javaClass.simpleName
            Log.e(UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG, "Unified first-night pool diagnostic failed", error)
        }
    }
    val automaticInformationOptions = firstNightAutomaticPool
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?: step.recommendedDisplayOptions
    val assistedInformationOptions = firstNightPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: step.recommendedDisplayOptions
''',
    "split first-night AUTO/ASSISTED domains",
)
NIGHT_UI.write_text(night_ui, encoding="utf-8")

# 4) Host pair production: retain the legacy recommendation shortlist for assisted/manual mode,
# but expose every classified legal effect to the automatic generic selector.
host = HOST.read_text(encoding="utf-8")
host = replace_once(
    host,
    """    fun recommendedUnreliablePairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
""",
    """    fun recommendedUnreliablePairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
        completeSelectionDomain: Boolean = false,
    ): List<ClocktowerDisplayOption> {
""",
    "pair helper domain switch",
)
new_pair_tail = r'''        val effectsById = projectedEffects.associateBy(PairInformationEffect::id)
        val recommendations = recommendationCoordinator.recommendPair(candidates)
        val recommendationsByCandidateId = recommendations.associateBy { it.candidateId }
        val candidatesById = candidates.associateBy(PairInformationCandidate::id)
        val candidateIds = if (completeSelectionDomain) {
            candidates.map(PairInformationCandidate::id)
        } else {
            recommendations.map { it.candidateId }
        }
        return candidateIds.mapNotNull { candidateId ->
            val candidate = candidatesById[candidateId] ?: return@mapNotNull null
            val effect = effectsById[candidateId] ?: return@mapNotNull null
            val recommendation = recommendationsByCandidateId[candidateId]
            val noRoleText = when (ability) {
                ClocktowerPairInformationAbility.Librarian -> text("没有外来者", "No Outsiders")
                ClocktowerPairInformationAbility.Investigator -> text("没有爪牙", "No Minions")
                ClocktowerPairInformationAbility.Washerwoman -> text("没有镇民", "No Townsfolk")
            }
            val roleText = effect.shownRole?.nameFor(language) ?: noRoleText
            val seats = if (effect.target != null && effect.decoy != null) {
                "${seatNumberFor(effect.target)}   ${seatNumberFor(effect.decoy)}"
            } else {
                null
            }
            val warningIds = recommendation?.warningIds.orEmpty()
            val warning = if (warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            val style = recommendation?.style ?: automaticStorytellerStyle
            val recommendationPrefix = recommendation
                ?.let { "${recommendationStyleLabel(it.style)}：" }
                .orEmpty()
            val option = displayOption(
                label = "$recommendationPrefix$roleText${seats?.let { " · $it" }.orEmpty()}$warning",
                kind = ClocktowerDisplayKind.EitherOne,
                title = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman -> text("洗衣妇信息", "Washerwoman information")
                    ClocktowerPairInformationAbility.Librarian -> text("图书管理员信息", "Librarian information")
                    ClocktowerPairInformationAbility.Investigator -> text("调查员信息", "Investigator information")
                },
                primary = roleText,
                secondary = seats,
                footer = if (seats == null) "" else text("在下面两位玩家之中", "One of these two players"),
                proposition = if (effect.shownRole != null && effect.target != null && effect.decoy != null) {
                    InformationProposition.AnyOf(listOf(
                        InformationProposition.RoleAt(cards.indexOf(effect.target) + 1, RoleId(effect.shownRole.enName)),
                        InformationProposition.RoleAt(cards.indexOf(effect.decoy) + 1, RoleId(effect.shownRole.enName)),
                    ))
                } else {
                    InformationProposition.AllOf(roles.map { InformationProposition.RoleInPlay(RoleId(it.enName), false) })
                },
                recommendationStyle = style,
                isTruthful = candidate.isTruthful,
                misinformationPressure = candidate.misinformationPressure,
                isDefaultRecommendation = recommendation?.style == RecommendationStyle.BALANCED,
                reasonCodes = if (recommendation == null) emptyList() else listOf("dynamic.pair-score"),
                warningCodes = warningIds,
            )
            if (ability == ClocktowerPairInformationAbility.Investigator) {
                option.copy(
                    recluseRegistersEvil = effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE,
                    recluseRegisteredRoleEnName = effect.shownRole?.enName
                        ?.takeIf { effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE },
                )
            } else {
                option
            }
        }
    }

'''
host = replace_span(
    host,
    "        val effectsById = projectedEffects.associateBy(PairInformationEffect::id)\n        return recommendationCoordinator.recommendPair(candidates).mapNotNull { recommendation ->",
    "    val informationStepBuilder = ClocktowerInformationStepBuilder(",
    new_pair_tail,
    "pair shortlist/full-domain split",
)
for ability in ("Washerwoman", "Librarian", "Investigator"):
    if ability == "Investigator":
        old = """                                displayOptions = { actor ->
                                    listOfNotNull(recommendedDrunkInvestigatorOption(actor)) +
                                        recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                                },
                                reliableDisplayOptions = { actor ->
"""
        new = """                                displayOptions = { actor ->
                                    listOfNotNull(recommendedDrunkInvestigatorOption(actor)) +
                                        recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                                },
                                automaticSelectionOptions = { actor ->
                                    recommendedUnreliablePairInformationOptions(
                                        ClocktowerPairInformationAbility.Investigator,
                                        actor,
                                        completeSelectionDomain = true,
                                    )
                                },
                                reliableDisplayOptions = { actor ->
"""
    else:
        old = f"""                                displayOptions = {{ actor ->
                                    recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.{ability}, actor)
                                }},
                                reliableDisplayOptions = {{ actor ->
"""
        new = f"""                                displayOptions = {{ actor ->
                                    recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.{ability}, actor)
                                }},
                                automaticSelectionOptions = {{ actor ->
                                    recommendedUnreliablePairInformationOptions(
                                        ClocktowerPairInformationAbility.{ability},
                                        actor,
                                        completeSelectionDomain = true,
                                    )
                                }},
                                reliableDisplayOptions = {{ actor ->
"""
    host = replace_once(host, old, new, f"{ability} automatic full domain wiring")
HOST.write_text(host, encoding="utf-8")

check("git", "diff", "--check")
print(run("git", "diff", "--", str(PRESENTATION), str(BUILDER), str(NIGHT_UI), str(HOST)), flush=True)

check(
    "./gradlew", ":app:testDebugUnitTest",
    "--tests", "com.codex.campboardgamehost.FirstNightPairInformationSelectionDomainTest",
    "--tests", "com.codex.campboardgamehost.FirstNightPairInformationProductionSemanticsTest",
    "--no-daemon",
)
check(
    "./gradlew", ":app:testDebugUnitTest",
    "--tests", "com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGeneratorTest",
    "--tests", "com.codex.campboardgamehost.clocktower.recommendation.RegistrationPairPolicyTest",
    "--no-daemon",
)
check("./gradlew", ":app:testDebugUnitTest", "--no-daemon")

check("git", "config", "user.name", "github-actions[bot]")
check("git", "config", "user.email", "41898282+github-actions[bot]@users.noreply.github.com")
check("git", "add", str(PRESENTATION), str(BUILDER), str(NIGHT_UI), str(HOST))
check("git", "commit", "-m", "fix(ms-s6d): preserve full pair auto semantic domain")
check("git", "push", "origin", f"HEAD:{BRANCH}")

check("git", "rm", SCRIPT, WORKFLOW)
check("git", "commit", "-m", "chore: remove one-shot S6D pair domain patch")
check("git", "push", "origin", f"HEAD:{BRANCH}")
