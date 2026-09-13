package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightFullScreenOwnershipTest {
    @Test
    fun `real target actions own the full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.FortuneTeller,
                displayKind = ClocktowerDisplayKind.YesNo,
            ),
        )
    }

    @Test
    fun `real information display owns the full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.NewDemonIdentity,
                displayKind = ClocktowerDisplayKind.Number,
            ),
        )
    }

    @Test
    fun `dedicated unreliable square table still owns the full screen host surface`() {
        assertTrue(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.None,
                displayKind = ClocktowerDisplayKind.None,
                hasDedicatedSquareTable = true,
            ),
        )
    }

    @Test
    fun `night step forwards dedicated square table ownership to outer surface`() {
        val source = sourceFile(
            "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
        )
        val dedicatedOwnership = source
            .substringAfter("val usesDedicatedSquareTable =")
            .substringBefore("val ownsFullScreenSurface =")

        assertTrue(dedicatedOwnership.contains("usesPairSquareTable"))
        assertTrue(dedicatedOwnership.contains("usesNumericSquareTable"))
        assertTrue(dedicatedOwnership.contains("usesUndertakerSquareTable"))
        assertTrue(source.contains("hasDedicatedSquareTable = usesDedicatedSquareTable"))
    }

    @Test
    fun `non real or empty legacy step stays in the regular night shell`() {
        assertFalse(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = false,
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
        assertFalse(
            clocktowerNightUsesFullScreenHostSurface(
                isRealAction = true,
                action = ClocktowerNightAction.NewDemonIdentity,
                displayKind = ClocktowerDisplayKind.None,
            ),
        )
    }

    private fun sourceFile(relativeText: String): String {
        val relative = Path.of(relativeText)
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("Source not found from ${Path.of("").toAbsolutePath()}: $relativeText")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
