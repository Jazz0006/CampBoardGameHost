package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightStepExperienceCopyContractTest {
    @Test
    fun `experience-mode semantics are owned by square-table controls instead of legacy recommendation prose`() {
        val source = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
        val pairSource = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt")

        assertFalse(source.contains("The automatic mode selected this information."))
        assertFalse(source.contains("已按当前自动模式选定信息"))
        assertFalse(source.contains("The balanced option is the default"))
        assertFalse(source.contains("平衡方案适合直接采用"))
        assertFalse(source.contains("other options apply different pressure"))
        assertFalse(source.contains("其他方案提供不同压力"))
        assertFalse(source.contains("The recommended information has been selected automatically."))
        assertFalse(source.contains("推荐信息已自动选定"))
        assertFalse(source.contains("Choose another legal option only if you want to intervene manually."))
        assertFalse(source.contains("仅在需要手动干预时选择其他合法信息"))

        assertTrue(source.contains("allowManualEditing = !automaticStorytellerInfo"))
        assertTrue(source.contains("ClocktowerPlainInformationSquareTableDialog("))
        assertTrue(pairSource.contains("allowManualEditing: Boolean"))
        assertTrue(pairSource.contains("if (allowManualEditing &&"))
        assertTrue(pairSource.contains("Choose manually"))
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
