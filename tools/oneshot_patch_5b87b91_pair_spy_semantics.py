from pathlib import Path
import subprocess

RED_HEAD = "5b87b91b368c9922dfd6cbf9c8abf1cfecd30439"
BRANCH = "codex/ms-setup-generic-architecture"
GENERATOR = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/recommendation/NaturalPairInformationCandidateGenerator.kt")
SEMANTICS = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSelectionSemantics.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
SCRIPT = "tools/oneshot_patch_5b87b91_pair_spy_semantics.py"
WORKFLOW = ".github/workflows/oneshot_patch_5b87b91_pair_spy_semantics.yml"
EXPECTED_BLOBS = {
    GENERATOR: "53bf4f88436d71ab53dfbe2472f11eb06b83ff79",
    SEMANTICS: "5a4a9dde5919fd8685f0eb7f9d74a3de3c4bd206",
    HOST: "aac151610f7a626ac48d62579c738eb33d6c6910",
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


if subprocess.call(["git", "merge-base", "--is-ancestor", RED_HEAD, "HEAD"]) != 0:
    raise SystemExit(f"RED checkpoint {RED_HEAD} is not an ancestor of HEAD")
for path, expected in EXPECTED_BLOBS.items():
    actual = run("git", "hash-object", str(path))
    if actual != expected:
        raise SystemExit(f"blob drift for {path}: expected {expected}, got {actual}")
if run("git", "status", "--porcelain"):
    raise SystemExit("working tree is not clean")

# 1) Shared healthy pair semantics: Spy may register as a good Townsfolk/Outsider for
# Washerwoman/Librarian. A natural truth for the same visible clue wins, so special
# registration is represented only when it is actually needed to make that clue true.
generator = GENERATOR.read_text(encoding="utf-8")
generator = replace_once(
    generator,
    """    private val investigator = RoleId("Investigator")
    private val recluse = RoleId("Recluse")
""",
    """    private val investigator = RoleId("Investigator")
    private val spy = RoleId("Spy")
    private val recluse = RoleId("Recluse")
""",
    "generator Spy role id",
)
generator = replace_once(
    generator,
    """        val targets = game.players
            .filter { it.seat != sourceSeat && it.actualType == targetType }
            .sortedBy { it.seat }
        if (abilityRole == librarian && targets.isEmpty()) {
            return listOf(noOutsiderCandidate(sourceSeat))
        }

        val naturalCandidates = targets.flatMap { target ->
            game.players
                .filter { it.seat != sourceSeat && it.seat != target.seat }
                .sortedBy { it.seat }
                .map { decoy -> naturalCandidate(sourceSeat, abilityRole, target.actualRole, target.seat, decoy.seat) }
        }
        if (abilityRole != investigator) return naturalCandidates.distinctBy { it.candidateId }

        // Recluse may register as any Minion on the current script for the Investigator
""",
    """        val targets = game.players
            .filter { it.seat != sourceSeat && it.actualType == targetType }
            .sortedBy { it.seat }
        val zeroCandidates = if (abilityRole == librarian && targets.isEmpty()) {
            listOf(noOutsiderCandidate(sourceSeat))
        } else {
            emptyList()
        }

        val naturalCandidates = targets.flatMap { target ->
            game.players
                .filter { it.seat != sourceSeat && it.seat != target.seat }
                .sortedBy { it.seat }
                .map { decoy -> naturalCandidate(sourceSeat, abilityRole, target.actualRole, target.seat, decoy.seat) }
        }
        if (abilityRole != investigator) {
            val shownGoodRoles = roleDefinitions
                .asSequence()
                .filter {
                    game.script in it.scriptIds &&
                        it.type == targetType &&
                        it.alignment == Alignment.GOOD
                }
                .map { it.id }
                .distinct()
                .sortedBy { it.value }
                .toList()
                .ifEmpty { targets.map { it.actualRole }.distinct().sortedBy { it.value } }
            val naturalOutcomes = naturalCandidates.map { it.outcome }.toSet()
            val spyCandidates = game.players
                .filter { it.seat != sourceSeat && it.actualRole == spy }
                .sortedBy { it.seat }
                .flatMap { target ->
                    shownGoodRoles.flatMap { shownRole ->
                        game.players
                            .filter { it.seat != sourceSeat && it.seat != target.seat }
                            .sortedBy { it.seat }
                            .map { decoy ->
                                spyRegistrationCandidate(
                                    sourceSeat = sourceSeat,
                                    abilityRole = abilityRole,
                                    registeredType = targetType,
                                    shownRole = shownRole,
                                    targetSeat = target.seat,
                                    decoySeat = decoy.seat,
                                )
                            }
                    }
                }
                .filterNot { it.outcome in naturalOutcomes }

            return (zeroCandidates + naturalCandidates + spyCandidates).distinctBy { it.candidateId }
        }

        // Recluse may register as any Minion on the current script for the Investigator
""",
    "generator healthy Spy semantic space",
)
generator = replace_once(
    generator,
    """    private fun recluseRegistrationCandidate(
        sourceSeat: Int,
""",
    r'''    private fun spyRegistrationCandidate(
        sourceSeat: Int,
        abilityRole: RoleId,
        registeredType: CharacterType,
        shownRole: RoleId,
        targetSeat: Int,
        decoySeat: Int,
    ): DecisionCandidate<PairInformationOutcome> {
        val registration = RegistrationFact(
            interactionId = listOf(
                "pair-information-registration-v1",
                sourceSeat,
                targetSeat,
                shownRole.value,
                RegistrationQuestion.ROLE.name,
            ).joinToString(":"),
            subjectSeat = targetSeat,
            registeredRole = shownRole,
            registeredType = registeredType,
            registeredAlignment = Alignment.GOOD,
            registrationQuestion = RegistrationQuestion.ROLE,
            reason = RegistrationReason.SPY_ABILITY,
        )
        val outcome = PairInformationOutcome(
            shownRole = shownRole,
            targetSeat = targetSeat,
            decoySeat = decoySeat,
        )
        return DecisionCandidate(
            candidateId = StableCandidateIdFactory.create(
                candidateSchemaVersion = candidateSchemaVersion,
                abilityState = AbilityState.FUNCTIONING,
                truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
                abilityRole = abilityRole,
                shownRole = shownRole,
                candidateSeats = outcome.candidateSeats,
                registrations = listOf(registration),
            ),
            candidateFamilyId = naturalTruthFamily,
            outcome = outcome,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = listOf(registration),
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = sourceSeat,
                    sourceAbility = abilityRole,
                    value = InformationValue.PlayerPair(shownRole, outcome.candidateSeats),
                ),
            ),
            metadata = metadata(abilityRole, extraTags = setOf("registered-truth", "spy-registration")),
        )
    }

    private fun recluseRegistrationCandidate(
        sourceSeat: Int,
''',
    "generator Spy registration candidate",
)
GENERATOR.write_text(generator, encoding="utf-8")

# 2) Production projector: keep the shared semantic evaluator as the authority for both
# truth classification and the hidden registration fact that made the clue truthful.
semantics = SEMANTICS.read_text(encoding="utf-8")
semantics = replace_once(
    semantics,
    """private data class FirstNightPairInformationTruth(
    val recluseRegisteredRoleEnName: String?,
)
""",
    """private data class FirstNightPairInformationTruth(
    val spyRegisteredRoleEnName: String?,
    val recluseRegisteredRoleEnName: String?,
)
""",
    "pair truth Spy metadata",
)
semantics = replace_once(
    semantics,
    """            FirstNightPairInformationTruth(
                recluseRegisteredRoleEnName = semanticCandidate.registrations
                    .firstOrNull { it.reason == RegistrationReason.RECLUSE_ABILITY }
                    ?.registeredRole
                    ?.value,
            )
""",
    """            FirstNightPairInformationTruth(
                spyRegisteredRoleEnName = semanticCandidate.registrations
                    .firstOrNull { it.reason == RegistrationReason.SPY_ABILITY }
                    ?.registeredRole
                    ?.value,
                recluseRegisteredRoleEnName = semanticCandidate.registrations
                    .firstOrNull { it.reason == RegistrationReason.RECLUSE_ABILITY }
                    ?.registeredRole
                    ?.value,
            )
""",
    "pair truth registration extraction",
)
semantics = replace_once(
    semantics,
    """        val investigator = roleEnName == "Investigator"
        option.copy(
            isTruthful = truth != null,
            misinformationPressure = if (truth != null) 0 else option.misinformationPressure.coerceAtLeast(1),
            recluseRegistersEvil = if (investigator) truth?.recluseRegisteredRoleEnName != null else option.recluseRegistersEvil,
            recluseRegisteredRoleEnName = if (investigator) truth?.recluseRegisteredRoleEnName else option.recluseRegisteredRoleEnName,
        )
""",
    """        val spyRegistrationRole = truth?.spyRegisteredRoleEnName
        val recluseRegistrationRole = truth?.recluseRegisteredRoleEnName
        option.copy(
            isTruthful = truth != null,
            misinformationPressure = if (truth != null) 0 else option.misinformationPressure.coerceAtLeast(1),
            spyRegistersGood = spyRegistrationRole?.let { true },
            spyRegisteredRoleEnName = spyRegistrationRole,
            recluseRegistersEvil = recluseRegistrationRole?.let { true },
            recluseRegisteredRoleEnName = recluseRegistrationRole,
        )
""",
    "pair truth registration projection",
)
SEMANTICS.write_text(semantics, encoding="utf-8")

# 3) Host rendering: consume the projector metadata directly for all pair roles. Do not
# reconstruct registration truth from raw role identities at the display boundary.
host = HOST.read_text(encoding="utf-8")
host = replace_once(
    host,
    """            if (ability == ClocktowerPairInformationAbility.Investigator) {
                option.copy(
                    recluseRegistersEvil = effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE,
                    recluseRegisteredRoleEnName = effect.shownRole?.enName
                        ?.takeIf { effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE },
                )
            } else {
                option
            }
""",
    """            val semantics = projectedSemanticsById.getValue(effect.id)
            option.copy(
                spyRegistersGood = semantics.spyRegistersGood,
                spyRegisteredRoleEnName = semantics.spyRegisteredRoleEnName,
                recluseRegistersEvil = semantics.recluseRegistersEvil,
                recluseRegisteredRoleEnName = semantics.recluseRegisteredRoleEnName,
            )
""",
    "Host pair registration metadata projection",
)
HOST.write_text(host, encoding="utf-8")

check("git", "diff", "--check")
print(run("git", "diff", "--", str(GENERATOR), str(SEMANTICS), str(HOST)), flush=True)

check(
    "./gradlew", ":app:testDebugUnitTest",
    "--tests", "com.codex.campboardgamehost.FirstNightPairInformationProductionSemanticsTest",
    "--tests", "com.codex.campboardgamehost.FirstNightPairInformationSelectionDomainTest",
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
check("git", "add", str(GENERATOR), str(SEMANTICS), str(HOST))
check("git", "commit", "-m", "fix(ms-s6d): preserve pair Spy registration semantics")
check("git", "push", "origin", f"HEAD:{BRANCH}")

check("git", "rm", SCRIPT, WORKFLOW)
check("git", "commit", "-m", "chore: remove one-shot S6D pair Spy semantic patch")
check("git", "push", "origin", f"HEAD:{BRANCH}")
