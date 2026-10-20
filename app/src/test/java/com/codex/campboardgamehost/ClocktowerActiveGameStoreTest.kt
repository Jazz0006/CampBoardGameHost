package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.setup.toSetupDiversityHistory
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment
import org.json.JSONObject
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClocktowerActiveGameStoreTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { File("src/main/assets/$it").readText(Charsets.UTF_8) }
    private val rules = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private fun session(vararg roles: String): ClocktowerGameSession {
        val defs = clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing).associateBy { it.id.value }
        val game = GameState(ScriptId("trouble_brewing"), roles.mapIndexed { i, r ->
            val d = requireNotNull(defs[r]); PlayerState(i+1, "P${i+1}", d.id, d.alignment, d.type)
        }, 17)
        return ClocktowerGameSession.createProduction("save-test", 17, game, ClocktowerSemanticHistoryMode.GLOBAL_V1,
            mapOf(2 to StorytellerPlayerContextInputV1(PlayerExperienceLevelV1.EXPERT, listOf(RoleId("Mayor")))))
    }
    private fun file(name: String): File = File(RuntimeEnvironment.getApplication().noBackupFilesDir, name).also {
        it.delete(); File(it.path+".new").delete(); File(it.path+".bak").delete()
    }
    private fun toDay(s: ClocktowerGameSession) = s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", s.state.phase,
        s.state.round, s.nextInputSequence, StorytellerPhase.DAY, 1), s.state.gameStateRevision)
    private fun metadata() = ClocktowerActiveGameMetadata.encode(StorytellerOperationMode.MANUAL, null, null, null, null, null, null)

    @Test fun `fresh store and Session read actual disk state and continue the first unconfirmed information role`() {
        val f = file("current.json")
        val s = session("Chef", "Empath", "Soldier", "Saint", "Imp")
        val store = ClocktowerActiveGameStore(f)
        s.persistWith { store.save(it, metadata()) }
        val original = s.state
        assertEquals("Chef", original.nextNightInteraction(rules)?.roleId?.value)
        // UI preview has no persistent event. A new Session still requires the same real step.
        val before = ClocktowerActiveGameStore(f).load()!!.state
        assertEquals(original, before)
        s.commitGlobalEpistemicObservation(EpistemicObservationDraft("chef-shown", s.state.phase, 1, s.nextInputSequence, 1,
            RoleId("Chef"), ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, emptyList(), 0)))
        val restored = ClocktowerGameSession.restore(ClocktowerActiveGameStore(f).load()!!.state)
        assertEquals("Empath", restored.state.nextNightInteraction(rules)?.roleId?.value)
        assertEquals(s.state, restored.state)
        assertEquals(setOf(1), restored.state.epistemicObservationLog.records.single().recipientSeats)
        assertEquals(PlayerExperienceLevelV1.EXPERT, restored.state.storytellerPlayerContextBySeat[2]?.experienceLevel)
        assertFalse(f.parentFile!!.path.contains("cache"))
    }
    @Test fun `interrupted replacement file cannot replace the previous complete confirmed Session`() {
        val f = file("interrupted.json")
        val s = session("Chef", "Empath", "Soldier", "Saint", "Imp")
        ClocktowerActiveGameStore(f).save(s.state, metadata())
        File(f.path+".new").writeText("{partial")
        val loaded = ClocktowerActiveGameStore(f).load()!!
        assertEquals(s.state, loaded.state)
        ClocktowerActiveGameStore(f).clear()
        assertNull(ClocktowerActiveGameStore(f).load())
    }
    @Test fun `failed physical write leaves memory and previous complete file unchanged`() {
        val f = file("full.json")
        val s = session("Chef", "Empath", "Soldier", "Saint", "Imp")
        val store = ClocktowerActiveGameStore(f)
        store.save(s.state, metadata())
        val before = s.state
        var broken = false
        s.persistWith { candidate ->
            if(broken) {
                // Simulate disk failure AFTER bytes began replacing a real existing file.
                val atomic = android.util.AtomicFile(f)
                val out = atomic.startWrite()
                out.write("partial".toByteArray())
                atomic.failWrite(out)
                throw IOException("disk write interrupted")
            }
            store.save(candidate, metadata())
        }
        broken = true
        assertThrows(IOException::class.java) { toDay(s) }
        assertSame(before, s.state)
        assertEquals(before, ClocktowerActiveGameStore(f).load()!!.state)
    }
    @Test fun `attack death and forced succession persist together and restore pending private notification`() {
        val f = file("succession.json")
        val s = session("Imp", "Scarlet Woman", "Empath", "Soldier", "Saint")
        toDay(s); s.confirmEndDay(1)
        s.persistWith { ClocktowerActiveGameStore(f).save(it, metadata()) }
        s.confirmNightAttack(1, s.state.phase, s.state.round, s.state.nextNightInteraction(rules)!!.id.value, 2)
        val restored = ClocktowerGameSession.restore(ClocktowerActiveGameStore(f).load()!!.state)
        assertEquals(s.state, restored.state)
        assertEquals("other_night:event:imp:new_demon_identity", restored.state.nextNightInteraction(rules)?.id?.value)
        restored.confirmImpNotification()
        assertEquals("other_night:role:Empath", restored.state.nextNightInteraction(rules)?.id?.value)
    }
    @Test fun `AI accepted advisory memory is restored without a second game authority or any pending request`() {
        val s = session("Imp", "Chef", "Empath", "Soldier", "Saint")
        val memory = HostOpeningStrategyMemoryV2(listOf("tension"), listOf(HostOpeningStrategicIntentionV2("trigger", "approach", "risk")), listOf("unknown"))
        val opening = HostOpeningSemanticAcceptanceV2(OpeningSelectionV2(s.state.gameId, 0, 0, "roster", null, null, null, emptyMap()), emptyList(), memory)
        val cursor = StorytellerStrategicContinuationV2.MemoryCursor.initial(opening)
        val meta = ClocktowerActiveGameMetadata.encode(StorytellerOperationMode.AI_AUTOMATIC, null, null, opening, cursor, "response-accepted", "model")
        val f = file("advice.json")
        ClocktowerActiveGameStore(f).save(s.state, meta)
        val loaded = ClocktowerActiveGameStore(f).load()!!
        assertEquals(opening, ClocktowerActiveGameMetadata.opening(loaded.metadata))
        assertEquals(cursor, ClocktowerActiveGameMetadata.cursor(loaded.metadata))
        assertEquals("response-accepted", loaded.metadata.getString("responseId"))
        assertFalse(loaded.metadata.has("pendingRequest"))
        assertEquals(s.state, loaded.state)
    }
    @Test fun `corrupt saved chronology fails closed and preserves the file`() {
        val f = file("corrupt.json")
        val s = session("Imp", "Chef", "Empath", "Soldier", "Saint")
        ClocktowerActiveGameStore(f).save(s.state, metadata())
        val content = JSONObject(f.readText())
        content.getJSONObject("session").put("cursor", -1)
        f.writeText(content.toString())
        assertThrows(IllegalArgumentException::class.java) { ClocktowerActiveGameStore(f).load() }
        assertTrue(f.exists())
    }
    @Test fun `restore retains actual poison protection and attack chronology without recomputing outcomes`() {
        val f = file("mechanics.json")
        val s = session("Poisoner", "Monk", "Imp", "Soldier", "Empath")
        toDay(s); s.confirmEndDay(1)
        s.persistWith { ClocktowerActiveGameStore(f).save(it, metadata()) }
        fun next() = s.state.nextNightInteraction(rules)!!.id.value
        s.confirmNightPoison(2, s.state.phase, s.state.round, next())
        s.confirmNightProtection(5, s.state.phase, s.state.round, next())
        s.confirmNightAttack(5, s.state.phase, s.state.round, next())
        val restored = ClocktowerActiveGameStore(f).load()!!.state
        assertEquals(s.state, restored)
        assertTrue(restored.gameState.playerAt(2)!!.poisoned)
        assertFalse(restored.gameState.playerAt(5)!!.alive)
        assertEquals(listOf("Poison", "Protect", "Attack", "Death"), restored.actionTimeline.entries.takeLast(4).map { it.fact.javaClass.simpleName })
    }

    @Test fun `restored setup metadata still records both visible roster and each returning starting identity`() {
        val s = session("Imp", "Poisoner", "Chef", "Empath", "Soldier")
        val identities = s.state.gameState.players.map { p ->
            val external = requireNotNull(rules.characterRegistry.findByRoleId(p.actualRole)).externalId
            com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity(p.name, external, external,
                com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory.valueOf(p.actualType.name))
        }
        val rotation = com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord("fixture", 2, "fixture-5", 5,
            identities.filter { it.actualRoleId != "imp" }.map { it.actualRoleId }.toSet(), setOf("poisoner"), null, identities)
        val f = file("diversity.json")
        val metadata = ClocktowerActiveGameMetadata.encode(StorytellerOperationMode.MANUAL, null, rotation, null, null, null, null)
        ClocktowerActiveGameStore(f).save(s.state, metadata)
        val restored = requireNotNull(ClocktowerActiveGameMetadata.rotation(ClocktowerActiveGameStore(f).load()!!.metadata))
        assertEquals(rotation, restored)
        var raw: String? = null
        val history = TroubleBrewingSetupRotationHistoryStore({ raw }, { raw = it; true })
        assertTrue(history.recordCompletedGame(s.state.gameId, restored))
        val persisted = history.historyFor("fixture", 2, 5)
        assertEquals(identities, persisted.recentGames.single().playerStartingIdentities)
        assertEquals(5, persisted.toSetupDiversityHistory(rules.characterRegistry).recentSetups.single().actualRoles.size)
    }

}
