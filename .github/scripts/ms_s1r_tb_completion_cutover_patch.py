from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

replacements = [
    (
        """import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
""",
        """import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
""",
    ),
    (
        """    val playerCount = json.optJSONArray(\"cards\")?.length() ?: 0
    if (playerCount == 0) return null
    val round = json.optInt(\"round\", 1)
""",
        """    val playerCount = json.optJSONArray(\"cards\")?.length() ?: 0
    if (playerCount == 0) return null
    if (
        gameKind == GameKind.Clocktower &&
        enumByName<ClocktowerScript>(json.optNullableString(\"currentClocktowerScript\")) == ClocktowerScript.TroubleBrewing &&
        (!json.has(CommittedClocktowerSetupPersistence.ROOT_KEY) ||
            !json.has(TroubleBrewingSetupCompletionPersistence.ROOT_KEY))
    ) return null
    val round = json.optInt(\"round\", 1)
""",
    ),
    (
        """    var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
    var committedTroubleBrewingSetupSelection by remember {
        mutableStateOf<TroubleBrewingSetupPresetSelection?>(null)
    }
""",
        """    var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
    var committedTroubleBrewingSetupRotationRecord by remember {
        mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
    }
""",
    ),
    (
        """        committedTroubleBrewingSetupSelection?.let { selection ->
            put(
                TroubleBrewingSetupProvenancePersistence.ROOT_KEY,
                TroubleBrewingSetupProvenancePersistence.encode(selection),
            )
        }
""",
        """        committedTroubleBrewingSetupRotationRecord?.let { record ->
            put(
                TroubleBrewingSetupCompletionPersistence.ROOT_KEY,
                TroubleBrewingSetupCompletionPersistence.encode(record),
            )
        }
""",
    ),
    (
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
                restoredGameKind == GameKind.Clocktower &&
                restoredPersistence.clocktowerScript == ClocktowerScript.TroubleBrewing
            ) {
                val datasetJson = baseContext.assets
                    .open(\"setup/trouble_brewing_setup_presets_v2_final.json\")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                val dataset = TroubleBrewingSetupPresetJson.parse(datasetJson)
                TroubleBrewingSetupProvenancePersistence.decodeOrNull(json, dataset)
            } else {
                null
            }
""",
        """            val restoredCommittedClocktowerSetup = if (restoredGameKind == GameKind.Clocktower) {
                val setup = CommittedClocktowerSetupPersistence.decodeOrNull(json)
                if (restoredPersistence.clocktowerScript == ClocktowerScript.TroubleBrewing) {
                    requireNotNull(setup) {
                        \"Current Trouble Brewing save is missing its exact committed setup.\"
                    }
                }
                setup?.also { committedSetup ->
                    val restoredScript = requireNotNull(restoredPersistence.clocktowerScript) {
                        \"Clocktower committed setup restore requires a resolved script.\"
                    }
                    require(committedSetup.script == restoredScript.toRecommendationScriptId()) {
                        \"Persisted committed Clocktower setup script does not match active-game identity.\"
                    }
                    if (json.has(\"clocktowerGameSeed\")) {
                        require(committedSetup.setupSeed == json.optLong(\"clocktowerGameSeed\")) {
                            \"Persisted committed Clocktower setup seed does not match active-game seed.\"
                        }
                    }
                }
            } else {
                null
            }
            val restoredTroubleBrewingSetupRotationRecord = if (
                restoredGameKind == GameKind.Clocktower &&
                restoredPersistence.clocktowerScript == ClocktowerScript.TroubleBrewing
            ) {
                val committedSetup = requireNotNull(restoredCommittedClocktowerSetup)
                val record = requireNotNull(TroubleBrewingSetupCompletionPersistence.decodeOrNull(json)) {
                    \"Current Trouble Brewing save is missing its completion/diversity summary.\"
                }
                require(committedSetup.playerCount == record.playerCount) {
                    \"Trouble Brewing committed setup and completion summary player counts disagree.\"
                }
                require(committedSetup.provenance.providerId == record.datasetId) {
                    \"Trouble Brewing committed setup and completion summary providers disagree.\"
                }
                require(committedSetup.provenance.candidateId == record.presetId) {
                    \"Trouble Brewing committed setup and completion summary candidates disagree.\"
                }
                record
            } else {
                null
            }
""",
    ),
    (
        """            committedClocktowerSetup = restoredCommittedClocktowerSetup
            committedTroubleBrewingSetupSelection = restoredTroubleBrewingSetupSelection
""",
        """            committedClocktowerSetup = restoredCommittedClocktowerSetup
            committedTroubleBrewingSetupRotationRecord = restoredTroubleBrewingSetupRotationRecord
""",
    ),
    (
        """        committedClocktowerSetup = null
        committedTroubleBrewingSetupSelection = null
""",
        """        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
""",
    ),
    (
        """                committedTroubleBrewingSetupSelection = preparedSetup.selection
                committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(
""",
        """                committedTroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecordFactory.fromSelection(
                    preparedSetup.selection,
                )
                committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(
""",
    ),
    (
        """        if (gameOutcome == null) return true
        val selection = committedTroubleBrewingSetupSelection ?: return true
        return TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
            .recordCompletedGame(
                gameId = clocktowerGameId,
                selection = selection,
            )
""",
        """        if (gameOutcome == null) return true
        val record = committedTroubleBrewingSetupRotationRecord ?: return true
        return TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
            .recordCompletedGame(
                gameId = clocktowerGameId,
                record = record,
            )
""",
    ),
]

for index, (old, _) in enumerate(replacements, start=1):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one MS-S1R TB completion patch anchor {index}, found {count}")

for old, new in replacements:
    text = text.replace(old, new, 1)

required = [
    "TroubleBrewingSetupCompletionPersistence.encode(record)",
    "TroubleBrewingSetupCompletionPersistence.decodeOrNull(json)",
    "committedTroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecordFactory.fromSelection(",
    "record = record",
    "TroubleBrewingSetupPresetJson.parse(datasetJson)",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Missing required MS-S1R TB completion semantic token: {token}")

for forbidden in [
    "committedTroubleBrewingSetupSelection",
    "TroubleBrewingSetupProvenancePersistence",
    "TroubleBrewingSetupPresetSelection",
]:
    if forbidden in text:
        raise SystemExit(f"Legacy TB active-restore token remains in App: {forbidden}")

restore = text.split("fun restoreSavedGame()", 1)[1].split("val latestPersistActiveGameState", 1)[0]
if "trouble_brewing_setup_presets_v2_final.json" in restore:
    raise SystemExit("TB preset dataset is still loaded inside active-game restore")

path.write_text(text, encoding="utf-8", newline="\n")
