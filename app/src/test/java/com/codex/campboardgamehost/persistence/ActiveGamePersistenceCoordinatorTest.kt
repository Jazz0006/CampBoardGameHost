package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveGamePersistenceCoordinatorTest {
    private val troubleBrewing = script(
        id = "trouble_brewing",
        roles = listOf("Imp", "Empath", "Drunk"),
        hash = "11111111111111111111111111111111",
    )
    private val noGreaterJoy = script(
        id = "no_greater_joy",
        roles = listOf("Imp", "Clockmaker", "Sage"),
        hash = "22222222222222222222222222222222",
    )
    private val roleRegistry = WerewolfRoleRegistry.builtIn()
    private val boardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry)
    private val coordinator = ActiveGamePersistenceCoordinator(
        clocktowerScriptProvider = { selected ->
            when (selected) {
                ClocktowerScript.TroubleBrewing -> troubleBrewing
                ClocktowerScript.NoGreaterJoy -> noGreaterJoy
            }
        },
        werewolfRoleRegistry = roleRegistry,
        werewolfBoardRegistry = boardRegistry,
    )

    @Test
    fun `current schema is v2 while v1 remains an explicit migration input`() {
        assertEquals(2, ActiveGamePersistenceCoordinator.CURRENT_VERSION)
        assertTrue(ActiveGamePersistenceCoordinator.isSupportedVersion(1))
        assertTrue(ActiveGamePersistenceCoordinator.isSupportedVersion(2))
        assertFalse(ActiveGamePersistenceCoordinator.isSupportedVersion(0))
        assertFalse(ActiveGamePersistenceCoordinator.isSupportedVersion(3))
    }

    @Test
    fun `Clocktower save identity uses normalized script content rather than in-play subset`() {
        val envelope = coordinator.identityForSave(
            ActiveGamePersistenceInputs(
                gameKind = GameKind.Clocktower,
                clocktowerScript = ClocktowerScript.TroubleBrewing,
                assignedClocktowerRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
            ),
        )

        assertEquals(GameKind.Clocktower, envelope.gameKind)
        assertEquals("trouble_brewing", requireNotNull(envelope.clocktower).variantId)
        assertEquals(troubleBrewing.contentHash, envelope.clocktower?.contentHash)
        assertEquals(ClocktowerPersistenceIdentityFactory.SEMANTIC_VERSION, envelope.clocktower?.semanticVersion)
    }

    @Test
    fun `Clocktower v2 restore rejects same script id when persisted content changed`() {
        val current = coordinator.identityForSave(
            ActiveGamePersistenceInputs(
                gameKind = GameKind.Clocktower,
                clocktowerScript = ClocktowerScript.TroubleBrewing,
                assignedClocktowerRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
            ),
        )
        val stale = current.copy(
            clocktower = requireNotNull(current.clocktower).copy(
                contentHash = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            ),
        )
        val json = v2Json(
            gameKind = GameKind.Clocktower,
            legacyScript = ClocktowerScript.TroubleBrewing,
            envelope = stale,
        )

        assertFails {
            coordinator.resolveForRestore(
                json = json,
                gameKind = GameKind.Clocktower,
                assignedClocktowerRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
                assignedWerewolfRoles = emptyList(),
            )
        }
    }

    @Test
    fun `Clocktower v1 migration trusts saved script and never infers from assigned roles`() {
        val json = JSONObject()
            .put("version", 1)
            .put("currentGameKind", GameKind.Clocktower.name)
            .put("currentClocktowerScript", ClocktowerScript.TroubleBrewing.name)

        val resolution = coordinator.resolveForRestore(
            json = json,
            gameKind = GameKind.Clocktower,
            assignedClocktowerRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
            assignedWerewolfRoles = emptyList(),
        )

        assertEquals(ClocktowerScript.TroubleBrewing, resolution.clocktowerScript)
        assertTrue(resolution.allowLegacyClocktowerRulesetFallback)
        assertEquals("trouble_brewing", resolution.identity.clocktower?.variantId)

        assertFails {
            coordinator.resolveForRestore(
                json = json,
                gameKind = GameKind.Clocktower,
                assignedClocktowerRoleIds = listOf(RoleId("Imp"), RoleId("Clockmaker")),
                assignedWerewolfRoles = emptyList(),
            )
        }
    }

    @Test
    fun `Clocktower v1 save without selected script fails closed instead of player-count guessing`() {
        val json = JSONObject().put("version", 1)

        assertFails {
            coordinator.resolveForRestore(
                json = json,
                gameKind = GameKind.Clocktower,
                assignedClocktowerRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
                assignedWerewolfRoles = emptyList(),
            )
        }
    }

    @Test
    fun `Werewolf v2 restore binds board content house rules and actual assigned role deck`() {
        val inputs = ActiveGamePersistenceInputs(
            gameKind = GameKind.Werewolf,
            assignedWerewolfRoles = listOf(
                Role.Werewolf, Role.Werewolf, Role.Seer, Role.Witch, Role.Hunter,
                Role.Villager, Role.Villager, Role.Villager,
            ),
            werewolfCount = 2,
            includeSeer = true,
            includeWitch = true,
            includeHunter = true,
            lastWordsMode = LastWordsMode.FirstDay,
        )
        val envelope = coordinator.identityForSave(inputs)
        val json = v2Json(GameKind.Werewolf, envelope = envelope)
            .put("werewolfCount", 2)
            .put("includeSeer", true)
            .put("includeWitch", true)
            .put("includeHunter", true)
            .put("lastWordsMode", LastWordsMode.FirstDay.name)

        val resolution = coordinator.resolveForRestore(
            json = json,
            gameKind = GameKind.Werewolf,
            assignedClocktowerRoleIds = emptyList(),
            assignedWerewolfRoles = inputs.assignedWerewolfRoles,
        )
        assertEquals("classic_8", resolution.identity.werewolf?.board?.variantId)
        assertFalse(resolution.allowLegacyClocktowerRulesetFallback)

        val changedRule = JSONObject(json.toString())
            .put("lastWordsMode", LastWordsMode.Always.name)
        assertFails {
            coordinator.resolveForRestore(
                json = changedRule,
                gameKind = GameKind.Werewolf,
                assignedClocktowerRoleIds = emptyList(),
                assignedWerewolfRoles = inputs.assignedWerewolfRoles,
            )
        }

        val wrongDeck = inputs.assignedWerewolfRoles.toMutableList().apply {
            this[lastIndex] = Role.Seer
        }
        assertFails {
            coordinator.resolveForRestore(
                json = json,
                gameKind = GameKind.Werewolf,
                assignedClocktowerRoleIds = emptyList(),
                assignedWerewolfRoles = wrongDeck,
            )
        }
    }

    @Test
    fun `Werewolf v1 restore deterministically migrates legacy mechanical setup`() {
        val assigned = listOf(
            Role.Werewolf, Role.Werewolf, Role.Seer, Role.Witch,
            Role.Villager, Role.Villager,
        )
        val json = JSONObject()
            .put("version", 1)
            .put("werewolfCount", 2)
            .put("includeSeer", true)
            .put("includeWitch", true)
            .put("includeHunter", false)
            .put("lastWordsMode", LastWordsMode.FirstTwoDays.name)

        val resolution = coordinator.resolveForRestore(
            json = json,
            gameKind = GameKind.Werewolf,
            assignedClocktowerRoleIds = emptyList(),
            assignedWerewolfRoles = assigned,
        )

        assertEquals("classic_6", resolution.identity.werewolf?.board?.variantId)
        assertEquals(LastWordsMode.FirstTwoDays, resolution.identity.werewolf?.ruleOptions?.lastWordsMode)
    }

    @Test
    fun `v2 envelope game kind mismatch is rejected before mutable state restore`() {
        val envelope = coordinator.identityForSave(
            ActiveGamePersistenceInputs(
                gameKind = GameKind.Clocktower,
                clocktowerScript = ClocktowerScript.TroubleBrewing,
                assignedClocktowerRoleIds = listOf(RoleId("Imp")),
            ),
        )
        val json = v2Json(GameKind.Clocktower, ClocktowerScript.TroubleBrewing, envelope)

        assertFails {
            coordinator.resolveForRestore(
                json = json,
                gameKind = GameKind.Werewolf,
                assignedClocktowerRoleIds = emptyList(),
                assignedWerewolfRoles = listOf(Role.Werewolf),
            )
        }
    }

    private fun v2Json(
        gameKind: GameKind,
        legacyScript: ClocktowerScript? = null,
        envelope: PersistedActiveGameIdentityEnvelope,
    ): JSONObject = JSONObject().apply {
        put("version", 2)
        put("currentGameKind", gameKind.name)
        legacyScript?.let { put("currentClocktowerScript", it.name) }
        put(
            PersistedActiveGameIdentityJsonCodec.ROOT_KEY,
            PersistedActiveGameIdentityJsonCodec.encode(envelope),
        )
    }

    private fun script(id: String, roles: List<String>, hash: String): ClocktowerScriptDefinition =
        ClocktowerScriptDefinition(
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

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected persistence validation to fail closed.", failed)
    }
}
