package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId

/**
 * Explicit one-time identity bridge for ACTIVE_GAME_STATE_VERSION=1 Clocktower saves.
 *
 * The saved legacy enum is authoritative for which script was selected. Assigned roles are used
 * only to validate that the saved game is consistent with that script; they never infer or replace
 * the script identity. The normalized target script supplies the content hash that will be written
 * when the save is upgraded to the new persistence schema.
 */
internal object ClocktowerLegacyPersistenceIdentityFactory {
    const val MIGRATION_SOURCE_REVISION = "active-game-v1-explicit-migration"

    fun fromVersion1(
        savedScript: ClocktowerScript,
        targetScript: ClocktowerScriptDefinition,
        assignedRoleIds: List<RoleId>,
    ): PersistedGameContentIdentity {
        val expectedScriptId = when (savedScript) {
            ClocktowerScript.TroubleBrewing -> ScriptId("trouble_brewing")
            ClocktowerScript.NoGreaterJoy -> ScriptId("no_greater_joy")
        }
        require(targetScript.id == expectedScriptId) {
            "Legacy Clocktower script does not match the normalized target script."
        }
        val allowedRoles = targetScript.characterIds.toSet()
        require(assignedRoleIds.all { it in allowedRoles }) {
            "Legacy Clocktower save contains a role outside its saved script."
        }
        return ClocktowerPersistenceIdentityFactory.fromScript(
            script = targetScript,
            sourceRevision = MIGRATION_SOURCE_REVISION,
        )
    }
}
