package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerLegacyPersistenceMigrationTest {
    @Test
    fun `version one Trouble Brewing maps explicitly to normalized Trouble Brewing identity`() {
        val script = script(
            id = "trouble_brewing",
            hash = "11111111111111111111111111111111",
            roles = listOf("Imp", "Empath"),
        )

        val identity = ClocktowerLegacyPersistenceIdentityFactory.fromVersion1(
            savedScript = ClocktowerScript.TroubleBrewing,
            targetScript = script,
            assignedRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
        )

        assertEquals("trouble_brewing", identity.variantId)
        assertEquals(script.contentHash, identity.contentHash)
        assertEquals(ClocktowerPersistenceIdentityFactory.SEMANTIC_VERSION, identity.semanticVersion)
        assertEquals(
            ClocktowerLegacyPersistenceIdentityFactory.MIGRATION_SOURCE_REVISION,
            identity.sourceRevision,
        )
    }

    @Test
    fun `version one No Greater Joy maps explicitly to normalized No Greater Joy identity`() {
        val script = script(
            id = "no_greater_joy",
            hash = "22222222222222222222222222222222",
            roles = listOf("Imp", "Clockmaker"),
        )

        val identity = ClocktowerLegacyPersistenceIdentityFactory.fromVersion1(
            savedScript = ClocktowerScript.NoGreaterJoy,
            targetScript = script,
            assignedRoleIds = listOf(RoleId("Imp"), RoleId("Clockmaker")),
        )

        assertEquals("no_greater_joy", identity.variantId)
        assertEquals(script.contentHash, identity.contentHash)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `version one migration rejects saved script and target content mismatch`() {
        ClocktowerLegacyPersistenceIdentityFactory.fromVersion1(
            savedScript = ClocktowerScript.TroubleBrewing,
            targetScript = script(
                id = "no_greater_joy",
                hash = "22222222222222222222222222222222",
                roles = listOf("Imp", "Clockmaker"),
            ),
            assignedRoleIds = listOf(RoleId("Imp")),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `version one migration rejects assigned role outside saved script`() {
        ClocktowerLegacyPersistenceIdentityFactory.fromVersion1(
            savedScript = ClocktowerScript.TroubleBrewing,
            targetScript = script(
                id = "trouble_brewing",
                hash = "11111111111111111111111111111111",
                roles = listOf("Imp", "Empath"),
            ),
            assignedRoleIds = listOf(RoleId("Imp"), RoleId("Clockmaker")),
        )
    }

    private fun script(
        id: String,
        hash: String,
        roles: List<String>,
    ) = ClocktowerScriptDefinition(
        id = ScriptId(id),
        name = id,
        author = null,
        characterIds = roles.map(::RoleId),
        firstNightOverride = null,
        otherNightOverride = null,
        bootleggerRules = emptyList(),
        source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        contentHash = hash,
    )
}
