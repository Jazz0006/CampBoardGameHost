package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveGameIdentityEnvelopeTest {
    @Test
    fun `clocktower active game identity round trip is explicit`() {
        val envelope = PersistedActiveGameIdentityEnvelope.clocktower(
            PersistedGameContentIdentity(
                kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
                variantId = "no_greater_joy",
                contentHash = "11111111111111111111111111111111",
                semanticVersion = ClocktowerPersistenceIdentityFactory.SEMANTIC_VERSION,
                sourceRevision = "builtin-2026-08-20",
            ),
        )

        val json = PersistedActiveGameIdentityJsonCodec.encode(envelope)
        val restored = PersistedActiveGameIdentityJsonCodec.decode(json)

        assertEquals(envelope, restored)
        assertEquals(GameKind.Clocktower.name, json.getString("gameKind"))
        assertTrue(json.has("clocktower"))
        assertFalse(json.has("werewolf"))
    }

    @Test
    fun `werewolf active game identity round trip includes house rules`() {
        val envelope = PersistedActiveGameIdentityEnvelope.werewolf(
            PersistedWerewolfGameIdentity(
                board = PersistedGameContentIdentity(
                    kind = PersistedVariantKind.WEREWOLF_BOARD,
                    variantId = "classic_8",
                    contentHash = "22222222222222222222222222222222",
                    semanticVersion = WerewolfPersistenceIdentityFactory.SEMANTIC_VERSION,
                    sourceRevision = "builtin-r5_5-s3",
                ),
                ruleOptions = WerewolfRuleOptions(LastWordsMode.Always),
            ),
        )

        assertEquals(
            envelope,
            PersistedActiveGameIdentityJsonCodec.decode(
                PersistedActiveGameIdentityJsonCodec.encode(envelope),
            ),
        )
    }

    @Test
    fun `undercover active game carries no variant identity`() {
        val json = PersistedActiveGameIdentityJsonCodec.encode(
            PersistedActiveGameIdentityEnvelope.undercover(),
        )

        assertEquals(GameKind.Undercover.name, json.getString("gameKind"))
        assertFalse(json.has("clocktower"))
        assertFalse(json.has("werewolf"))
        assertEquals(
            PersistedActiveGameIdentityEnvelope.undercover(),
            PersistedActiveGameIdentityJsonCodec.decode(json),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `clocktower envelope rejects missing script identity`() {
        PersistedActiveGameIdentityEnvelope(
            gameKind = GameKind.Clocktower,
            clocktower = null,
            werewolf = null,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `werewolf envelope rejects Clocktower identity`() {
        PersistedActiveGameIdentityEnvelope(
            gameKind = GameKind.Werewolf,
            clocktower = PersistedGameContentIdentity(
                kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
                variantId = "trouble_brewing",
                contentHash = "11111111111111111111111111111111",
                semanticVersion = ClocktowerPersistenceIdentityFactory.SEMANTIC_VERSION,
            ),
            werewolf = null,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decoder rejects identity payload whose nested kind contradicts game kind`() {
        val json = PersistedActiveGameIdentityJsonCodec.encode(
            PersistedActiveGameIdentityEnvelope.clocktower(
                PersistedGameContentIdentity(
                    kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
                    variantId = "trouble_brewing",
                    contentHash = "11111111111111111111111111111111",
                    semanticVersion = ClocktowerPersistenceIdentityFactory.SEMANTIC_VERSION,
                ),
            ),
        )
        json.put("gameKind", GameKind.Werewolf.name)

        PersistedActiveGameIdentityJsonCodec.decode(json)
    }
}
