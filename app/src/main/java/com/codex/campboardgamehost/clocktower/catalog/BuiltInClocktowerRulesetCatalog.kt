package com.codex.campboardgamehost.clocktower.catalog

import android.content.Context
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader

/**
 * Single built-in source of normalized Clocktower content for production flow and persistence.
 *
 * The catalog owns only asset loading, registry composition, normalization, validation and caching.
 * Rules resolution, flow projection, persistence compatibility and UI behavior remain separate.
 */
internal class BuiltInClocktowerRulesetCatalog(
    private val readAsset: (String) -> String,
) {
    private val rulesets: Map<ClocktowerScript, ValidatedClocktowerRuleset> by lazy(
        LazyThreadSafetyMode.SYNCHRONIZED,
        ::load,
    )

    fun ruleset(script: ClocktowerScript): ValidatedClocktowerRuleset =
        rulesets[script] ?: error("Missing built-in Clocktower ruleset '$script'.")

    private fun load(): Map<ClocktowerScript, ValidatedClocktowerRuleset> {
        val legacyKnowledge = RulesetJsonLoader.parse(readAsset("rules/trouble_brewing.json"))
        val legacyRegistry = LegacyRulesetCatalogAdapter.characterRegistry(
            knowledge = legacyKnowledge,
            roleDefinitions = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
            coverage = RuleCoverage.PARTIAL,
        )
        val baseRegistry = OfficialReminderTokenMetadata.applyTo(legacyRegistry)
        val noGreaterJoyRegistry = OfficialReminderTokenMetadata.applyTo(
            NoGreaterJoyOfficialCharacterMetadata.extend(baseRegistry),
        )

        fun loadScript(
            script: ClocktowerScript,
            assetName: String,
            registry: ClocktowerCharacterRegistry,
        ): ValidatedClocktowerRuleset = RulesetJsonLoader.parseScript(
            json = readAsset("scripts/$assetName"),
            requestedScriptId = script.toRecommendationScriptId(),
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )

        return mapOf(
            ClocktowerScript.TroubleBrewing to loadScript(
                script = ClocktowerScript.TroubleBrewing,
                assetName = "trouble_brewing.json",
                registry = baseRegistry,
            ),
            ClocktowerScript.NoGreaterJoy to loadScript(
                script = ClocktowerScript.NoGreaterJoy,
                assetName = "no_greater_joy.json",
                registry = noGreaterJoyRegistry,
            ),
        )
    }

    companion object {
        @Volatile
        private var cachedAndroidCatalog: BuiltInClocktowerRulesetCatalog? = null

        fun fromContext(context: Context): BuiltInClocktowerRulesetCatalog {
            cachedAndroidCatalog?.let { return it }
            return synchronized(this) {
                cachedAndroidCatalog ?: run {
                    val applicationContext = context.applicationContext
                    BuiltInClocktowerRulesetCatalog { assetPath ->
                        applicationContext.assets.open(assetPath)
                            .bufferedReader(Charsets.UTF_8)
                            .use { it.readText() }
                    }.also { cachedAndroidCatalog = it }
                }
            }
        }
    }
}
