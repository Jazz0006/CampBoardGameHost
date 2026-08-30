package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingProductionStartWiringTest {
    @Test
    fun `Trouble Brewing start exits into curated setup before legacy random generator`() {
        val source = appSource()
        val start = functionBlock(source, "fun startClocktowerGame()")

        val troubleBrewingBranch = start.indexOf("if (script == ClocktowerScript.TroubleBrewing)")
        val curatedStart = start.indexOf("startTroubleBrewingGame()")
        val legacyGenerator = start.indexOf("generateClocktowerAssignments(playerNames.size, script)")

        assertTrue("Trouble Brewing must have an explicit production cutover branch.", troubleBrewingBranch >= 0)
        assertTrue("Trouble Brewing must delegate to its curated production start owner.", curatedStart >= 0)
        assertTrue("The TB cutover must occur before the legacy generator can run.", curatedStart < legacyGenerator)
    }

    @Test
    fun `curated Trouble Brewing start uses validated selector deal owners and does not synchronously recommend`() {
        val helper = functionBlock(appSource(), "fun startTroubleBrewingGame()")

        assertTrue(helper.contains("TroubleBrewingSetupPresetJson.parse("))
        assertTrue(helper.contains("TroubleBrewingSetupRotationHistoryStore.fromContext("))
        assertTrue(helper.contains("TroubleBrewingProductionSetupPreparer.prepare("))
        assertTrue(helper.contains("TroubleBrewingDealRoleResolver.resolve("))
        assertFalse(
            "Identity dealing must not synchronously wait for setup recommendation.",
            helper.contains("ClocktowerRecommendationCoordinator"),
        )
        assertFalse(
            "Curated TB start must not call the broad random assignment generator.",
            helper.contains("generateClocktowerAssignments"),
        )
    }

    private fun appSource(): String = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private fun functionBlock(source: String, signature: String): String {
        val start = source.indexOf(signature)
        require(start >= 0) { "Missing source function '$signature'." }
        val openingBrace = source.indexOf('{', start)
        require(openingBrace >= 0) { "Missing opening brace for '$signature'." }
        var depth = 0
        for (index in openingBrace until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) return source.substring(start, index + 1)
                }
            }
        }
        error("Unclosed source function '$signature'.")
    }
}
