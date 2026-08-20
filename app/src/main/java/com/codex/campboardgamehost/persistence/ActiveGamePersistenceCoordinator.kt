package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.catalog.NoGreaterJoyOfficialCharacterMetadata
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.json.JSONObject

internal data class ActiveGamePersistenceInputs(
    val gameKind: GameKind,
    val clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
    val assignedClocktowerRoleIds: List<RoleId> = emptyList(),
    val assignedWerewolfRoles: List<Role> = emptyList(),
    val werewolfCount: Int = 0,
    val includeSeer: Boolean = false,
    val includeWitch: Boolean = false,
    val includeHunter: Boolean = false,
    val lastWordsMode: LastWordsMode = LastWordsMode.FirstDay,
)

internal data class ActiveGamePersistenceResolution(
    val identity: PersistedActiveGameIdentityEnvelope,
    val clocktowerScript: ClocktowerScript? = null,
    val allowLegacyClocktowerRulesetFallback: Boolean = false,
)

internal class ActiveGamePersistenceCoordinator(
    private val clocktowerScriptProvider: (ClocktowerScript) -> ClocktowerScriptDefinition,
    private val werewolfRoleRegistry: WerewolfRoleRegistry,
    private val werewolfBoardRegistry: WerewolfBoardRegistry,
) {
    fun identityForSave(inputs: ActiveGamePersistenceInputs): PersistedActiveGameIdentityEnvelope = when (inputs.gameKind) {
        GameKind.Undercover -> PersistedActiveGameIdentityEnvelope.undercover()
        GameKind.Clocktower -> {
            val script = clocktowerScriptProvider(inputs.clocktowerScript)
            validateClocktowerAssignments(script, inputs.assignedClocktowerRoleIds)
            PersistedActiveGameIdentityEnvelope.clocktower(
                ClocktowerPersistenceIdentityFactory.fromScript(
                    script = script,
                    sourceRevision = BUILTIN_SCRIPT_SOURCE_REVISION,
                ),
            )
        }
        GameKind.Werewolf -> PersistedActiveGameIdentityEnvelope.werewolf(
            currentWerewolfIdentity(
                playerCount = inputs.assignedWerewolfRoles.size,
                werewolfCount = inputs.werewolfCount,
                includeSeer = inputs.includeSeer,
                includeWitch = inputs.includeWitch,
                includeHunter = inputs.includeHunter,
                lastWordsMode = inputs.lastWordsMode,
                assignedRoles = inputs.assignedWerewolfRoles,
            ),
        )
    }

    fun resolveForRestore(
        json: JSONObject,
        gameKind: GameKind,
        assignedClocktowerRoleIds: List<RoleId>,
        assignedWerewolfRoles: List<Role>,
    ): ActiveGamePersistenceResolution {
        val version = json.optInt("version", 0)
        require(isSupportedVersion(version)) { "Unsupported active game state version '$version'." }
        json.optString("currentGameKind").takeIf { it.isNotBlank() }?.let { savedKind ->
            require(savedKind == gameKind.name) { "Active-game payload game kind does not match restored game kind." }
        }
        return when (version) {
            LEGACY_VERSION -> resolveVersion1(
                json = json,
                gameKind = gameKind,
                assignedClocktowerRoleIds = assignedClocktowerRoleIds,
                assignedWerewolfRoles = assignedWerewolfRoles,
            )
            CURRENT_VERSION -> resolveVersion2(
                json = json,
                gameKind = gameKind,
                assignedClocktowerRoleIds = assignedClocktowerRoleIds,
                assignedWerewolfRoles = assignedWerewolfRoles,
            )
            else -> error("Unsupported active game state version '$version'.")
        }
    }

    private fun resolveVersion1(
        json: JSONObject,
        gameKind: GameKind,
        assignedClocktowerRoleIds: List<RoleId>,
        assignedWerewolfRoles: List<Role>,
    ): ActiveGamePersistenceResolution = when (gameKind) {
        GameKind.Undercover -> ActiveGamePersistenceResolution(
            identity = PersistedActiveGameIdentityEnvelope.undercover(),
        )
        GameKind.Clocktower -> {
            val savedScript = enumValueOrNull<ClocktowerScript>(json.optString("currentClocktowerScript"))
                ?: throw IllegalArgumentException("Legacy Clocktower save is missing a valid selected script.")
            val targetScript = clocktowerScriptProvider(savedScript)
            val migratedIdentity = ClocktowerLegacyPersistenceIdentityFactory.fromVersion1(
                savedScript = savedScript,
                targetScript = targetScript,
                assignedRoleIds = assignedClocktowerRoleIds,
            )
            ActiveGamePersistenceResolution(
                identity = PersistedActiveGameIdentityEnvelope.clocktower(migratedIdentity),
                clocktowerScript = savedScript,
                allowLegacyClocktowerRulesetFallback = true,
            )
        }
        GameKind.Werewolf -> ActiveGamePersistenceResolution(
            identity = PersistedActiveGameIdentityEnvelope.werewolf(
                currentWerewolfIdentity(
                    playerCount = assignedWerewolfRoles.size,
                    werewolfCount = json.requiredInt("werewolfCount"),
                    includeSeer = json.requiredBoolean("includeSeer"),
                    includeWitch = json.requiredBoolean("includeWitch"),
                    includeHunter = json.requiredBoolean("includeHunter"),
                    lastWordsMode = json.requiredEnum("lastWordsMode"),
                    assignedRoles = assignedWerewolfRoles,
                ),
            ),
        )
    }

    private fun resolveVersion2(
        json: JSONObject,
        gameKind: GameKind,
        assignedClocktowerRoleIds: List<RoleId>,
        assignedWerewolfRoles: List<Role>,
    ): ActiveGamePersistenceResolution {
        val identityJson = json.optJSONObject(PersistedActiveGameIdentityJsonCodec.ROOT_KEY)
            ?: throw IllegalArgumentException("Version 2 active-game save is missing content identity.")
        val persisted = PersistedActiveGameIdentityJsonCodec.decode(identityJson)
        require(persisted.gameKind == gameKind) { "Persisted content identity game kind mismatch." }

        return when (gameKind) {
            GameKind.Undercover -> ActiveGamePersistenceResolution(identity = persisted)
            GameKind.Clocktower -> {
                val persistedClocktower = requireNotNull(persisted.clocktower) {
                    "Version 2 Clocktower save is missing script identity."
                }
                val selectedScript = clocktowerScriptForVariantId(persistedClocktower.variantId)
                json.optString("currentClocktowerScript").takeIf { it.isNotBlank() }?.let { legacyName ->
                    val legacyScript = enumValueOrNull<ClocktowerScript>(legacyName)
                        ?: throw IllegalArgumentException("Invalid legacy Clocktower script mirror in v2 save.")
                    require(legacyScript == selectedScript) {
                        "Legacy Clocktower script mirror disagrees with v2 content identity."
                    }
                }
                val currentScript = clocktowerScriptProvider(selectedScript)
                validateClocktowerAssignments(currentScript, assignedClocktowerRoleIds)
                val currentIdentity = ClocktowerPersistenceIdentityFactory.fromScript(
                    script = currentScript,
                    sourceRevision = BUILTIN_SCRIPT_SOURCE_REVISION,
                )
                require(persistedClocktower.compatibilityWith(currentIdentity) == PersistedGameCompatibility.COMPATIBLE) {
                    "Clocktower save content identity is incompatible with the current script implementation."
                }
                ActiveGamePersistenceResolution(
                    identity = persisted,
                    clocktowerScript = selectedScript,
                    allowLegacyClocktowerRulesetFallback = false,
                )
            }
            GameKind.Werewolf -> {
                val persistedWerewolf = requireNotNull(persisted.werewolf) {
                    "Version 2 Werewolf save is missing board identity."
                }
                val currentIdentity = currentWerewolfIdentity(
                    playerCount = assignedWerewolfRoles.size,
                    werewolfCount = json.requiredInt("werewolfCount"),
                    includeSeer = json.requiredBoolean("includeSeer"),
                    includeWitch = json.requiredBoolean("includeWitch"),
                    includeHunter = json.requiredBoolean("includeHunter"),
                    lastWordsMode = json.requiredEnum("lastWordsMode"),
                    assignedRoles = assignedWerewolfRoles,
                )
                require(persistedWerewolf.compatibilityWith(currentIdentity) == PersistedGameCompatibility.COMPATIBLE) {
                    "Werewolf save identity is incompatible with the current board or house rules."
                }
                ActiveGamePersistenceResolution(identity = persisted)
            }
        }
    }

    private fun validateClocktowerAssignments(
        script: ClocktowerScriptDefinition,
        assignedRoleIds: List<RoleId>,
    ) {
        require(assignedRoleIds.isNotEmpty()) { "Clocktower persistence requires assigned role IDs." }
        val allowed = script.characterIds.toSet()
        require(assignedRoleIds.all { it in allowed }) {
            "Clocktower assigned roles do not belong to the selected script."
        }
    }

    private fun currentWerewolfIdentity(
        playerCount: Int,
        werewolfCount: Int,
        includeSeer: Boolean,
        includeWitch: Boolean,
        includeHunter: Boolean,
        lastWordsMode: LastWordsMode,
        assignedRoles: List<Role>,
    ): PersistedWerewolfGameIdentity {
        require(playerCount > 0) { "Werewolf persistence requires assigned roles." }
        validateWerewolfAssignments(
            assignedRoles = assignedRoles,
            werewolfCount = werewolfCount,
            includeSeer = includeSeer,
            includeWitch = includeWitch,
            includeHunter = includeHunter,
        )
        return WerewolfPersistenceIdentityFactory.fromLegacySetup(
            playerCount = playerCount,
            werewolfCount = werewolfCount,
            includeSeer = includeSeer,
            includeWitch = includeWitch,
            includeHunter = includeHunter,
            ruleOptions = WerewolfRuleOptions(lastWordsMode),
            roleRegistry = werewolfRoleRegistry,
            boardRegistry = werewolfBoardRegistry,
        )
    }

    private fun validateWerewolfAssignments(
        assignedRoles: List<Role>,
        werewolfCount: Int,
        includeSeer: Boolean,
        includeWitch: Boolean,
        includeHunter: Boolean,
    ) {
        val roleIds = assignedRoles.map { role ->
            werewolfRoleRegistry.roleIdFor(role)
                ?: throw IllegalArgumentException("Werewolf save contains an unsupported assigned role '$role'.")
        }
        val actual = roleIds.groupingBy { it }.eachCount()
        val specialCount = listOf(includeSeer, includeWitch, includeHunter).count { it }
        val villagerCount = assignedRoles.size - werewolfCount - specialCount
        require(villagerCount >= 0) { "Werewolf mechanical setup exceeds assigned player count." }
        val expected = buildMap<WerewolfRoleId, Int> {
            if (werewolfCount > 0) put(WerewolfRoleIds.WEREWOLF, werewolfCount)
            if (includeSeer) put(WerewolfRoleIds.SEER, 1)
            if (includeWitch) put(WerewolfRoleIds.WITCH, 1)
            if (includeHunter) put(WerewolfRoleIds.HUNTER, 1)
            if (villagerCount > 0) put(WerewolfRoleIds.VILLAGER, villagerCount)
        }
        require(actual == expected) {
            "Werewolf assigned role deck does not match persisted mechanical setup."
        }
    }

    private fun clocktowerScriptForVariantId(variantId: String): ClocktowerScript = when (variantId) {
        "trouble_brewing" -> ClocktowerScript.TroubleBrewing
        "no_greater_joy" -> ClocktowerScript.NoGreaterJoy
        else -> throw IllegalArgumentException("Unknown persisted Clocktower script id '$variantId'.")
    }

    companion object {
        const val LEGACY_VERSION = 1
        const val CURRENT_VERSION = 2
        private const val BUILTIN_SCRIPT_SOURCE_REVISION = "builtin-script-assets-r5_5"

        fun isSupportedVersion(version: Int): Boolean = version == LEGACY_VERSION || version == CURRENT_VERSION

        fun fromContext(context: Context): ActiveGamePersistenceCoordinator {
            val roleRegistry = WerewolfRoleRegistry.builtIn()
            return ActiveGamePersistenceCoordinator(
                clocktowerScriptProvider = { script -> BuiltInClocktowerPersistenceCatalog.script(context, script) },
                werewolfRoleRegistry = roleRegistry,
                werewolfBoardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry),
            )
        }
    }
}

private object BuiltInClocktowerPersistenceCatalog {
    @Volatile
    private var cached: Map<ClocktowerScript, ClocktowerScriptDefinition>? = null

    fun script(context: Context, script: ClocktowerScript): ClocktowerScriptDefinition =
        catalog(context)[script] ?: error("Missing built-in Clocktower persistence script '$script'.")

    private fun catalog(context: Context): Map<ClocktowerScript, ClocktowerScriptDefinition> {
        cached?.let { return it }
        return synchronized(this) {
            cached ?: load(context.applicationContext).also { cached = it }
        }
    }

    private fun load(context: Context): Map<ClocktowerScript, ClocktowerScriptDefinition> {
        val legacyKnowledge = context.assets.open("rules/trouble_brewing.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .let(RulesetJsonLoader::parse)
        val baseRegistry = LegacyRulesetCatalogAdapter.characterRegistry(
            knowledge = legacyKnowledge,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            coverage = RuleCoverage.PARTIAL,
        )
        val noGreaterJoyRegistry = NoGreaterJoyOfficialCharacterMetadata.extend(baseRegistry)

        fun loadScript(
            script: ClocktowerScript,
            assetName: String,
            registry: com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry,
        ): ClocktowerScriptDefinition {
            val json = context.assets.open("scripts/$assetName")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            return RulesetJsonLoader.parseScript(
                json = json,
                requestedScriptId = script.toRecommendationScriptId(),
                registry = registry,
                source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
            ).script
        }

        return mapOf(
            ClocktowerScript.TroubleBrewing to loadScript(
                ClocktowerScript.TroubleBrewing,
                "trouble_brewing.json",
                baseRegistry,
            ),
            ClocktowerScript.NoGreaterJoy to loadScript(
                ClocktowerScript.NoGreaterJoy,
                "no_greater_joy.json",
                noGreaterJoyRegistry,
            ),
        )
    }
}

private inline fun <reified T : Enum<T>> enumValueOrNull(name: String?): T? =
    name?.takeIf { it.isNotBlank() }?.let { value -> enumValues<T>().firstOrNull { it.name == value } }

private fun JSONObject.requiredInt(key: String): Int {
    require(has(key) && !isNull(key)) { "Missing required persisted integer '$key'." }
    return getInt(key)
}

private fun JSONObject.requiredBoolean(key: String): Boolean {
    require(has(key) && !isNull(key)) { "Missing required persisted boolean '$key'." }
    return getBoolean(key)
}

private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(key: String): T {
    require(has(key) && !isNull(key)) { "Missing required persisted enum '$key'." }
    return enumValueOrNull<T>(getString(key))
        ?: throw IllegalArgumentException("Invalid persisted enum '$key'.")
}
