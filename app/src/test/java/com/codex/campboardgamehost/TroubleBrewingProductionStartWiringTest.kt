package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Coarse App-root cutover guard; setup/deal semantics are owned by typed TBSP tests. */
class TroubleBrewingProductionStartWiringTest {
    @Test
    fun `Trouble Brewing production routes through curated start before legacy generator`() {
        val start = functionBlock(appSource(), "fun startClocktowerGame()")

        val curatedStart = start.indexOf("startTroubleBrewingGame()")
        val legacyGenerator = start.indexOf("generateClocktowerAssignments(playerNames.size, script)")

        assertTrue("Trouble Brewing must route through its curated production start owner.", curatedStart >= 0)
        assertTrue("The curated cutover must occur before the legacy generator can run.", legacyGenerator >= 0 && curatedStart < legacyGenerator)
    }

    @Test
    fun `curated Trouble Brewing start delegates to typed setup owners without synchronous recommendation`() {
        val helper = functionBlock(appSource(), "fun startTroubleBrewingGame()")

        assertTrue(helper.contains("TroubleBrewingProductionSetupPreparer.prepare("))
        assertTrue(helper.contains("TroubleBrewingDealRoleResolver.resolve("))
        assertFalse(helper.contains("generateClocktowerAssignments"))
        assertFalse(
            "Identity dealing must not synchronously wait for setup recommendation.",
            helper.contains("ClocktowerRecommendationCoordinator"),
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
