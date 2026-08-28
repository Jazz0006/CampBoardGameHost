package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production ownership guard for durable action history.
 *
 * Action ordering, commit semantics, replay, and persistence behavior belong to typed tests. This
 * file protects only that the non-callable App boundary still uses the canonical owners.
 */
class ClocktowerHistoricalActionProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `App owns one action timeline and commits drafts through canonical session authority`() {
        assertTrue(appSource.contains("mutableStateOf(ActionFactTimeline())"))
        assertTrue(appSource.contains("fun recordClocktowerAction("))
        assertTrue(appSource.contains("draft: ActionFactDraft"))
        assertTrue(appSource.contains("ClocktowerGameSession.commitGlobalActionFact("))
    }

    @Test
    fun `App save restore and reset use canonical action timeline persistence`() {
        assertTrue(appSource.contains("ClocktowerSemanticHistoryPersistence.encodeActionTimeline(clocktowerActionTimeline)"))
        assertTrue(appSource.contains("ClocktowerSemanticHistoryPersistence.decodeActionTimeline(json)"))
        assertTrue(appSource.contains("clocktowerActionTimeline = ActionFactTimeline()"))
    }

    @Test
    fun `confirmed gameplay transitions emit typed action drafts`() {
        listOf(
            "ActionFactDraft.Poison(",
            "ActionFactDraft.Attack(",
            "ActionFactDraft.Protect(",
            "ActionFactDraft.Execution(",
            "ActionFactDraft.Death(",
            "ActionFactDraft.RoleChange(",
            "ActionFactDraft.PhaseAdvance(",
        ).forEach { draftType ->
            assertTrue("Missing production action draft: $draftType", appSource.contains(draftType))
        }
    }
}
