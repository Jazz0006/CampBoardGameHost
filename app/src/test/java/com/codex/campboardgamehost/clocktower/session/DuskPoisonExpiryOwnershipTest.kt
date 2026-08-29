package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

/**
 * P1 RED: day-to-night poison expiry must stop generating retry-unsafe dynamic history identity
 * inside App root. Typed convergence coverage is added when the materialization seam is introduced.
 */
class DuskPoisonExpiryOwnershipTest {
    @Test
    fun `app root no longer owns dynamic poison-expire history identity`() {
        val appSource = File(
            "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
        ).readText(Charsets.UTF_8)

        assertFalse(
            "All Day -> Night poison expiry entry points must converge through one stable typed " +
                "materialization owner; App root must not generate poison-expire action IDs.",
            appSource.contains("kind = \"poison-expire\""),
        )
    }
}
