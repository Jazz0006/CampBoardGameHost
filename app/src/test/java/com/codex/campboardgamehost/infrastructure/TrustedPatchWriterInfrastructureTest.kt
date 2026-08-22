package com.codex.campboardgamehost.infrastructure

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustedPatchWriterInfrastructureTest {
    @Test
    fun `trusted patch writer is permanent fail closed infrastructure`() {
        val source = repoFile(".github/workflows/trusted-patch-writer.yml")

        assertTrue(source.contains("issue_comment:"))
        assertTrue(source.contains("contents: write"))
        assertTrue(source.contains("github.event.issue.pull_request"))
        assertTrue(source.contains("github.actor == github.repository_owner"))
        assertTrue(source.contains("expected_head_sha"))
        assertTrue(source.contains("expected_blob_sha"))
        assertTrue(source.contains("target_path"))
        assertTrue(source.contains("patch_base64"))
        assertTrue(source.contains("git apply --check"))
        assertTrue(source.contains("git diff --check"))
        assertTrue(source.contains("git diff --name-only"))
        assertTrue(source.contains("git ls-remote"))
        assertTrue(source.contains("refs/heads/\$target_branch"))
        assertTrue(source.contains(":app:testDebugUnitTest :app:assembleDebug"))
    }

    @Test
    fun `trusted patch request parser keeps the write surface narrow`() {
        val source = repoFile("tools/trusted_patch_writer/prepare_request.py")

        assertTrue(source.contains("MAX_PATCH_BYTES = 40 * 1024"))
        assertTrue(source.contains("if key not in ALLOWED_KEYS:"))
        assertTrue(source.contains("if not owner or actor != owner:"))
        assertTrue(source.contains("if head_repo.get(\"full_name\") != repo_full_name:"))
        assertTrue(source.contains("if not target_branch or target_branch == default_branch:"))
        assertTrue(source.contains("normalized.startswith(\".github/\")"))
        assertTrue(source.contains("normalized.startswith(\"tools/trusted_patch_writer/\")"))
        assertTrue(source.contains("target_path.startswith(\"app/\")"))
        assertTrue(source.contains("target_path.startswith(\"gradle/\")"))
        assertTrue(source.contains("\"gradle.properties\""))
        assertTrue(source.contains("base64.b64decode(values[\"patch_base64\"], validate=True)"))
        assertTrue(source.contains("patch_text.count(\"diff --git \") != 1"))
        assertTrue(source.contains("head.get(\"sha\") != expected_head"))
    }

    @Test
    fun `repository declares portable source line endings`() {
        val attributes = repoFile(".gitattributes")

        assertTrue(attributes.contains("*.kt text eol=lf"))
        assertTrue(attributes.contains("*.kts text eol=lf"))
        assertTrue(attributes.contains("*.md text eol=lf"))
        assertTrue(attributes.contains("*.yml text eol=lf"))
        assertTrue(attributes.contains("*.py text eol=lf"))
        assertTrue(attributes.contains("*.bat text eol=crlf"))
    }

    private fun repoFile(path: String): String {
        val direct = File(path)
        val fromApp = File("..").resolve(path)
        val file = when {
            direct.exists() -> direct
            fromApp.exists() -> fromApp
            else -> throw AssertionError("Repository file not found: $path")
        }
        return file.readText(Charsets.UTF_8).replace("\r\n", "\n")
    }
}
