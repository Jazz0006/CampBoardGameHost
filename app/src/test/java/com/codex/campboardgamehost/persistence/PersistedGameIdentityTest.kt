package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistedGameIdentityTest {
    @Test
    fun `content identity json round trip preserves compatibility fields`() {
        val original = PersistedGameContentIdentity(
            kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
            variantId = "trouble_brewing",
            contentHash = "0123456789abcdef0123456789abcdef",
            semanticVersion = "clocktower-r5_5-v1",
            sourceRevision = "builtin-2026-08-20",
        )

        val restored = PersistedGameContentIdentityJsonCodec.decode(
            PersistedGameContentIdentityJsonCodec.encode(original),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `compatibility fails closed on variant content and semantic mismatches`() {
        val baseline = PersistedGameContentIdentity(
            kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
            variantId = "trouble_brewing",
            contentHash = "0123456789abcdef0123456789abcdef",
            semanticVersion = "clocktower-r5_5-v1",
            sourceRevision = "builtin-2026-08-20",
        )

        assertEquals(PersistedGameCompatibility.COMPATIBLE, baseline.compatibilityWith(baseline))
        assertEquals(
            PersistedGameCompatibility.VARIANT_MISMATCH,
            baseline.copy(variantId = "no_greater_joy").compatibilityWith(baseline),
        )
        assertEquals(
            PersistedGameCompatibility.CONTENT_MISMATCH,
            baseline.copy(contentHash = "fedcba9876543210fedcba9876543210").compatibilityWith(baseline),
        )
        assertEquals(
            PersistedGameCompatibility.SEMANTIC_MISMATCH,
            baseline.copy(semanticVersion = "clocktower-r5_5-v2").compatibilityWith(baseline),
        )
    }

    @Test
    fun `clocktower identity is derived from normalized script content not display name`() {
        val script = ClocktowerScriptDefinition(
            id = ScriptId("trouble_brewing"),
            name = "Localized Display Name",
            author = null,
            characterIds = listOf(RoleId("Imp")),
            firstNightOverride = null,
            otherNightOverride = null,
            bootleggerRules = emptyList(),
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
            contentHash = "11111111111111111111111111111111",
        )

        val identity = ClocktowerPersistenceIdentityFactory.fromScript(
            script = script,
            sourceRevision = "builtin-2026-08-20",
        )

        assertEquals(PersistedVariantKind.CLOCKTOWER_SCRIPT, identity.kind)
        assertEquals("trouble_brewing", identity.variantId)
        assertEquals(script.contentHash, identity.contentHash)
        assertEquals(ClocktowerPersistenceIdentityFactory.SEMANTIC_VERSION, identity.semanticVersion)
    }

    @Test
    fun `werewolf legacy setup maps exact built in composition to stable board identity`() {
        val roleRegistry = WerewolfRoleRegistry.builtIn()
        val boardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry)

        val identity = WerewolfPersistenceIdentityFactory.fromLegacySetup(
            playerCount = 8,
            werewolfCount = 2,
            includeSeer = true,
            includeWitch = true,
            includeHunter = true,
            ruleOptions = WerewolfRuleOptions(LastWordsMode.FirstTwoDays),
            roleRegistry = roleRegistry,
            boardRegistry = boardRegistry,
        )

        val expectedBoard = boardRegistry.require(WerewolfBoardId("classic_8"))
        assertEquals("classic_8", identity.board.variantId)
        assertEquals(expectedBoard.contentHash, identity.board.contentHash)
        assertEquals(LastWordsMode.FirstTwoDays, identity.ruleOptions.lastWordsMode)
    }

    @Test
    fun `werewolf legacy custom composition gets deterministic content addressed identity`() {
        val roleRegistry = WerewolfRoleRegistry.builtIn()
        val boardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry)

        val first = WerewolfPersistenceIdentityFactory.fromLegacySetup(
            playerCount = 8,
            werewolfCount = 3,
            includeSeer = true,
            includeWitch = true,
            includeHunter = false,
            ruleOptions = WerewolfRuleOptions(LastWordsMode.FirstDay),
            roleRegistry = roleRegistry,
            boardRegistry = boardRegistry,
        )
        val second = WerewolfPersistenceIdentityFactory.fromLegacySetup(
            playerCount = 8,
            werewolfCount = 3,
            includeSeer = true,
            includeWitch = true,
            includeHunter = false,
            ruleOptions = WerewolfRuleOptions(LastWordsMode.FirstDay),
            roleRegistry = roleRegistry,
            boardRegistry = boardRegistry,
        )

        assertTrue(first.board.variantId.startsWith("legacy_custom_"))
        assertEquals(first, second)
        assertNotEquals("classic_8", first.board.variantId)
    }

    @Test
    fun `werewolf identity json round trip preserves house rule options`() {
        val identity = PersistedWerewolfGameIdentity(
            board = PersistedGameContentIdentity(
                kind = PersistedVariantKind.WEREWOLF_BOARD,
                variantId = "classic_8",
                contentHash = "22222222222222222222222222222222",
                semanticVersion = WerewolfPersistenceIdentityFactory.SEMANTIC_VERSION,
                sourceRevision = "builtin-2026-08-20",
            ),
            ruleOptions = WerewolfRuleOptions(LastWordsMode.Always),
        )

        val restored = PersistedWerewolfGameIdentityJsonCodec.decode(
            PersistedWerewolfGameIdentityJsonCodec.encode(identity),
        )

        assertEquals(identity, restored)
    }

    @Test
    fun `werewolf compatibility includes house rule identity`() {
        val baseline = PersistedWerewolfGameIdentity(
            board = PersistedGameContentIdentity(
                kind = PersistedVariantKind.WEREWOLF_BOARD,
                variantId = "classic_8",
                contentHash = "22222222222222222222222222222222",
                semanticVersion = WerewolfPersistenceIdentityFactory.SEMANTIC_VERSION,
            ),
            ruleOptions = WerewolfRuleOptions(LastWordsMode.FirstDay),
        )

        assertEquals(PersistedGameCompatibility.COMPATIBLE, baseline.compatibilityWith(baseline))
        assertEquals(
            PersistedGameCompatibility.HOUSE_RULE_MISMATCH,
            baseline.copy(ruleOptions = WerewolfRuleOptions(LastWordsMode.Always)).compatibilityWith(baseline),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `legacy werewolf migration rejects impossible role composition`() {
        val roleRegistry = WerewolfRoleRegistry.builtIn()
        val boardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry)

        WerewolfPersistenceIdentityFactory.fromLegacySetup(
            playerCount = 4,
            werewolfCount = 4,
            includeSeer = true,
            includeWitch = false,
            includeHunter = false,
            ruleOptions = WerewolfRuleOptions(),
            roleRegistry = roleRegistry,
            boardRegistry = boardRegistry,
        )
    }
}
