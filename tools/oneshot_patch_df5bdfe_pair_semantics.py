from pathlib import Path
import subprocess

BASE_HEAD = "df5bdfe9c6dbe91c39bf61555520a9c01f1fb0b2"
SEMANTICS = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSelectionSemantics.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
SEMANTICS_BLOB = "71663a4af1745a0c6a2e794b04a9885f2f248ffa"
HOST_BLOB = "f205b5f9d601626299fb7c863a1c0879066d8323"
SCRIPT = "tools/oneshot_patch_df5bdfe_pair_semantics.py"
WORKFLOW = ".github/workflows/oneshot_patch_df5bdfe_pair_semantics.yml"


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


actual_base = run("git", "rev-parse", "HEAD~2")
if actual_base != BASE_HEAD:
    raise SystemExit(f"base drift: expected {BASE_HEAD}, got {actual_base}")
if run("git", "hash-object", str(SEMANTICS)) != SEMANTICS_BLOB:
    raise SystemExit("selection semantics blob drift")
if run("git", "hash-object", str(HOST)) != HOST_BLOB:
    raise SystemExit("host screen blob drift")

# The RED has already been independently captured by CI #1317 at BASE_HEAD.
semantics = SEMANTICS.read_text(encoding="utf-8")
semantics = replace_once(
    semantics,
    """import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
""",
    """import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
""",
    "pair semantic domain imports",
)
semantics = replace_once(
    semantics,
    """import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
""",
    """import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
""",
    "natural pair generator import",
)
insert_anchor = """internal data class ClocktowerDecisionOption(
"""
pair_projection = r'''private data class FirstNightPairInformationKey(
    val shownRole: RoleId?,
    val candidateSeats: List<Int>,
)

private data class FirstNightPairInformationTruth(
    val recluseRegisteredRoleEnName: String?,
)

private fun PairInformationOutcome.firstNightPairInformationKey(): FirstNightPairInformationKey =
    FirstNightPairInformationKey(
        shownRole = shownRole,
        candidateSeats = candidateSeats,
    )

private fun InformationProposition.firstNightPairInformationKey(): FirstNightPairInformationKey? = when (this) {
    is InformationProposition.AnyOf -> {
        val roleAt = alternatives.map { it as? InformationProposition.RoleAt ?: return null }
        val shownRole = roleAt.map { it.role }.distinct().singleOrNull() ?: return null
        FirstNightPairInformationKey(
            shownRole = shownRole,
            candidateSeats = roleAt.map { it.seat }.distinct().sorted(),
        )
    }

    is InformationProposition.AllOf -> {
        if (propositions.isEmpty() || propositions.any {
                val roleInPlay = it as? InformationProposition.RoleInPlay
                roleInPlay == null || roleInPlay.inPlay
            }
        ) {
            null
        } else {
            FirstNightPairInformationKey(shownRole = null, candidateSeats = emptyList())
        }
    }

    else -> null
}

internal fun projectFirstNightPairInformationOptions(
    phase: ClocktowerPhase,
    roleEnName: String,
    sourceSeat: Int,
    game: GameState,
    roleDefinitions: List<RoleDefinition>,
    options: List<ClocktowerDisplayOption>,
): List<ClocktowerDisplayOption> {
    if (
        phase != ClocktowerPhase.FirstNight ||
        roleEnName !in setOf("Washerwoman", "Librarian", "Investigator")
    ) {
        return options
    }

    val healthyTruths = NaturalPairInformationCandidateGenerator
        .generateHealthyInformationSpace(
            game = game,
            sourceSeat = sourceSeat,
            abilityRole = RoleId(roleEnName),
            roleDefinitions = roleDefinitions,
        )
        .groupBy { it.outcome.firstNightPairInformationKey() }
        .mapValues { (_, candidates) ->
            val semanticCandidate = candidates.firstOrNull { it.registrations.isEmpty() }
                ?: candidates.minBy { it.candidateId }
            FirstNightPairInformationTruth(
                recluseRegisteredRoleEnName = semanticCandidate.registrations
                    .firstOrNull { it.reason == RegistrationReason.RECLUSE_ABILITY }
                    ?.registeredRole
                    ?.value,
            )
        }

    return options.map { option ->
        val key = option.proposition?.firstNightPairInformationKey() ?: return@map option
        val truth = healthyTruths[key]
        val investigator = roleEnName == "Investigator"
        option.copy(
            isTruthful = truth != null,
            misinformationPressure = if (truth != null) 0 else option.misinformationPressure.coerceAtLeast(1),
            recluseRegistersEvil = if (investigator) truth?.recluseRegisteredRoleEnName != null else option.recluseRegistersEvil,
            recluseRegisteredRoleEnName = if (investigator) truth?.recluseRegisteredRoleEnName else option.recluseRegisteredRoleEnName,
        )
    }
}

'''
semantics = replace_once(semantics, insert_anchor, pair_projection + insert_anchor, "pair projector insertion")
SEMANTICS.write_text(semantics, encoding="utf-8")

host = HOST.read_text(encoding="utf-8")
old = r'''        val candidates = effects.map { effect ->
            val namedPlayers = listOfNotNull(effect.target, effect.decoy)
            val truthful = if (effect.shownRole == null) {
                cards.none { it.clocktowerTeam == roleTeam }
            } else {
                namedPlayers.any { it.clocktowerRole?.enName == effect.shownRole.enName }
            }
            val targetMetadata = effect.target?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val decoyMetadata = effect.decoy?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val goodPlayersNamed = namedPlayers.count { !isClocktowerEvil(it) }
            val shownMetadata = effect.shownRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            PairInformationCandidate(
                id = effect.id,
                registration = PairInformationRegistration.NONE,
                isTruthful = truthful,
                targetExposure = targetMetadata?.exposureSensitivity ?: 0,
                decoyExposure = decoyMetadata?.exposureSensitivity ?: 0,
                discussionValue = (targetMetadata?.discussionValue ?: 0) +
                    (decoyMetadata?.discussionValue ?: 0) +
                    (shownMetadata?.discussionValue ?: 0),
                misinformationPressure = if (truthful) {
                    0
                } else {
                    (2 +
                        (if (goodPlayersNamed == 2) 1 else 0) +
                        (if ((shownMetadata?.exposureSensitivity ?: 0) >= 4) 1 else 0))
                        .coerceIn(0, 5)
                },
                historyPressure = informationHistoryPressure(effect.target) + informationHistoryPressure(effect.decoy),
            )
        }
        val effectsById = effects.associateBy(PairInformationEffect::id)
'''
new = r'''        fun propositionFor(effect: PairInformationEffect): InformationProposition =
            if (effect.shownRole != null && effect.target != null && effect.decoy != null) {
                InformationProposition.AnyOf(listOf(
                    InformationProposition.RoleAt(cards.indexOf(effect.target) + 1, RoleId(effect.shownRole.enName)),
                    InformationProposition.RoleAt(cards.indexOf(effect.decoy) + 1, RoleId(effect.shownRole.enName)),
                ))
            } else {
                InformationProposition.AllOf(roles.map { InformationProposition.RoleInPlay(RoleId(it.enName), false) })
            }
        val sourceSeat = cards.indexOf(actor) + 1
        val projectedSemanticsById = projectFirstNightPairInformationOptions(
            phase = phase,
            roleEnName = ability.name,
            sourceSeat = sourceSeat,
            game = cards.toClocktowerGameState(script, gameSeed, poisonTarget),
            roleDefinitions = clocktowerRoleDefinitionsForScript(script),
            options = effects.map { effect ->
                ClocktowerDisplayOption(
                    label = effect.id,
                    displayKind = ClocktowerDisplayKind.EitherOne,
                    displayTitle = ability.name,
                    displayPrimary = null,
                    displaySecondary = null,
                    displayFooter = null,
                    proposition = propositionFor(effect),
                    isTruthful = false,
                    misinformationPressure = 1,
                )
            },
        ).associateBy(ClocktowerDisplayOption::label)
        val projectedEffects = effects.map { effect ->
            val semantics = projectedSemanticsById.getValue(effect.id)
            effect.copy(
                registration = if (semantics.recluseRegistersEvil == true) {
                    PairInformationRegistration.RECLUSE_AS_EVIL_ROLE
                } else {
                    PairInformationRegistration.NONE
                },
            )
        }
        val candidates = projectedEffects.map { effect ->
            val namedPlayers = listOfNotNull(effect.target, effect.decoy)
            val truthful = projectedSemanticsById.getValue(effect.id).isTruthful
            val targetMetadata = effect.target?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val decoyMetadata = effect.decoy?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val goodPlayersNamed = namedPlayers.count { !isClocktowerEvil(it) }
            val shownMetadata = effect.shownRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            PairInformationCandidate(
                id = effect.id,
                registration = effect.registration,
                isTruthful = truthful,
                targetExposure = targetMetadata?.exposureSensitivity ?: 0,
                decoyExposure = decoyMetadata?.exposureSensitivity ?: 0,
                discussionValue = (targetMetadata?.discussionValue ?: 0) +
                    (decoyMetadata?.discussionValue ?: 0) +
                    (shownMetadata?.discussionValue ?: 0),
                misinformationPressure = if (truthful) {
                    0
                } else {
                    (2 +
                        (if (goodPlayersNamed == 2) 1 else 0) +
                        (if ((shownMetadata?.exposureSensitivity ?: 0) >= 4) 1 else 0))
                        .coerceIn(0, 5)
                },
                historyPressure = informationHistoryPressure(effect.target) + informationHistoryPressure(effect.decoy),
            )
        }
        val effectsById = projectedEffects.associateBy(PairInformationEffect::id)
'''
host = replace_once(host, old, new, "unreliable pair pre-selection semantic projection")
HOST.write_text(host, encoding="utf-8")

check("git", "diff", "--check")
print(run("git", "diff", "--", str(SEMANTICS), str(HOST)), flush=True)

check("./gradlew", ":app:testDebugUnitTest", "--tests", "com.codex.campboardgamehost.FirstNightPairInformationProductionSemanticsTest", "--no-daemon")
check("./gradlew", ":app:testDebugUnitTest", "--tests", "com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGeneratorTest", "--tests", "com.codex.campboardgamehost.clocktower.recommendation.PairInformationRecommenderTest", "--no-daemon")
check("./gradlew", ":app:testDebugUnitTest", "--no-daemon")

check("git", "config", "user.name", "github-actions[bot]")
check("git", "config", "user.email", "41898282+github-actions[bot]@users.noreply.github.com")
check("git", "add", str(SEMANTICS), str(HOST))
check("git", "commit", "-m", "fix(ms-s6d): unify first-night pair truth semantics")
check("git", "push", "origin", "HEAD:codex/ms-setup-generic-architecture")
check("git", "rm", SCRIPT, WORKFLOW)
check("git", "commit", "-m", "chore: remove one-shot S6D pair semantic patch")
check("git", "push", "origin", "HEAD:codex/ms-setup-generic-architecture")
