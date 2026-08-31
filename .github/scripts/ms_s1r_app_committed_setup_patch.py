from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

replacements = [
    (
        """import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
""",
        """import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
""",
    ),
    (
        """import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
""",
        """import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
""",
    ),
    (
        """import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
""",
        """import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingCommittedSetupAdapter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
""",
    ),
    (
        """    var clocktowerGameId by remember { mutableStateOf(\"\") }
    var clocktowerGameSeed by remember { mutableStateOf(0L) }
    var committedTroubleBrewingSetupSelection by remember {
""",
        """    var clocktowerGameId by remember { mutableStateOf(\"\") }
    var clocktowerGameSeed by remember { mutableStateOf(0L) }
    var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
    var committedTroubleBrewingSetupSelection by remember {
""",
    ),
    (
        """        committedTroubleBrewingSetupSelection?.let { selection ->
            put(
                TroubleBrewingSetupProvenancePersistence.ROOT_KEY,
                TroubleBrewingSetupProvenancePersistence.encode(selection),
            )
        }
        put(\"undercoverCount\", undercoverCount)
""",
        """        committedClocktowerSetup?.let { setup ->
            put(
                CommittedClocktowerSetupPersistence.ROOT_KEY,
                CommittedClocktowerSetupPersistence.encode(setup),
            )
        }
        committedTroubleBrewingSetupSelection?.let { selection ->
            put(
                TroubleBrewingSetupProvenancePersistence.ROOT_KEY,
                TroubleBrewingSetupProvenancePersistence.encode(selection),
            )
        }
        put(\"undercoverCount\", undercoverCount)
""",
    ),
    (
        """            val restoredTroubleBrewingSetupSelection = if (
""",
        """            val restoredCommittedClocktowerSetup = if (restoredGameKind == GameKind.Clocktower) {
                CommittedClocktowerSetupPersistence.decodeOrNull(json)?.also { setup ->
                    val restoredScript = requireNotNull(restoredPersistence.clocktowerScript) {
                        \"Clocktower committed setup restore requires a resolved script.\"
                    }
                    require(setup.script == restoredScript.toRecommendationScriptId()) {
                        \"Persisted committed Clocktower setup script does not match active-game identity.\"
                    }
                    if (json.has(\"clocktowerGameSeed\")) {
                        require(setup.setupSeed == json.optLong(\"clocktowerGameSeed\")) {
                            \"Persisted committed Clocktower setup seed does not match active-game seed.\"
                        }
                    }
                }
            } else {
                null
            }
            val restoredTroubleBrewingSetupSelection = if (
""",
    ),
    (
        """            committedTroubleBrewingSetupSelection = restoredTroubleBrewingSetupSelection
            clocktowerGameId = json.optString(\"clocktowerGameId\")
""",
        """            committedClocktowerSetup = restoredCommittedClocktowerSetup
            committedTroubleBrewingSetupSelection = restoredTroubleBrewingSetupSelection
            clocktowerGameId = json.optString(\"clocktowerGameId\")
""",
    ),
    (
        """        clearSavedGameState()
        committedTroubleBrewingSetupSelection = null
        currentGameKind = nextGameKind
""",
        """        clearSavedGameState()
        committedClocktowerSetup = null
        committedTroubleBrewingSetupSelection = null
        currentGameKind = nextGameKind
""",
    ),
    (
        """                committedTroubleBrewingSetupSelection = preparedSetup.selection
                persistActiveGameStateIfNeeded()
""",
        """                committedTroubleBrewingSetupSelection = preparedSetup.selection
                committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(
                    dealPlan = preparedSetup.dealPlan,
                    resolvedAssignments = resolvedAssignments,
                )
                persistActiveGameStateIfNeeded()
""",
    ),
]

for index, (old, _) in enumerate(replacements, start=1):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one MS-S1R patch anchor {index}, found {count}")

for old, new in replacements:
    text = text.replace(old, new, 1)

required = [
    "var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }",
    "CommittedClocktowerSetupPersistence.encode(setup)",
    "CommittedClocktowerSetupPersistence.decodeOrNull(json)",
    "committedClocktowerSetup = restoredCommittedClocktowerSetup",
    "committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(",
    "resolvedAssignments = resolvedAssignments",
    "TroubleBrewingSetupProvenancePersistence.decodeOrNull(json, dataset)",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Missing required MS-S1R semantic token: {token}")

path.write_text(text, encoding="utf-8", newline="\n")
