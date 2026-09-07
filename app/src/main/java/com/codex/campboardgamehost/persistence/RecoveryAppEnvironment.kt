package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.json.JSONObject

/** Current short-horizon recovery contract token shared by typed read/write paths. */
internal object RecoveryCompatibilityToken {
    fun currentFor(gameKind: GameKind): String =
        "active-v${ActiveGamePersistenceCoordinator.CURRENT_VERSION}:${gameKind.name}"
}

/**
 * Production adapter for the pure recovery planner.
 *
 * Preview and PS3.3 apply should both enter through this function so role/ruleset resolution and
 * compatibility-token selection cannot drift into separate raw-JSON readers.
 */
internal fun Context.prepareCurrentRecoveryPlan(
    raw: JSONObject,
    nowMillis: Long = System.currentTimeMillis(),
): RecoveryPlanPreparation {
    val gameKindName = raw.opt("currentGameKind") as? String
        ?: return RecoveryPlanPreparation.Rejected(RecoveryRejectionReason.MalformedPayload)
    val gameKind = enumValues<GameKind>().firstOrNull { it.name == gameKindName }
        ?: return RecoveryPlanPreparation.Rejected(RecoveryRejectionReason.MalformedPayload)

    return RecoveryRestorePlanner.prepare(
        raw = raw,
        expectedCompatibilityToken = RecoveryCompatibilityToken.currentFor(gameKind),
        nowMillis = nowMillis,
        roleByName = RecoveryClocktowerRoleCatalog::roleByName,
        clocktowerRulesetResolver = { script, basis ->
            resolveCurrentRecoveryRuleset(script, basis)
        },
    )
}

private object RecoveryClocktowerRoleCatalog {
    private val rolesByName: Map<String, ClocktowerRole> = ClocktowerScript.values()
        .flatMap(::clocktowerRolesForScript)
        .associateBy(ClocktowerRole::enName)

    fun roleByName(name: String): ClocktowerRole? = rolesByName[name]
}

private fun Context.resolveCurrentRecoveryRuleset(
    script: ClocktowerScript,
    basis: ClocktowerRulesetPersistenceBasis,
) = when (script) {
    ClocktowerScript.TroubleBrewing -> runCatching {
        val rawRules = assets
            .open("rules/trouble_brewing.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        TroubleBrewingRulesetPersistence.refFor(
            knowledge = RulesetJsonLoader.parse(rawRules),
            basis = basis,
        )
    }.getOrNull()
    ClocktowerScript.NoGreaterJoy -> null
}
