// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.BuildInput
import org.islandoftex.arara.api.files.BuildSymlinks
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.junit.jupiter.api.Assumptions.assumeTrue

class WorkspaceLinksTest {
    private fun fixture(test: (MPPPath, MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara-workspace-links").toMPPPath()
        try {
            val probe = root.resolve("probe").toJVMPath()
            val supported = runCatching { Files.createSymbolicLink(probe, Path.of("target")) }.isSuccess
            assumeTrue(supported, "Symbolic links require OS support and permissions")
            Files.delete(probe)
            val source = root.resolve("source").apply { createDirectories() }
            source.resolve("book.tex").writeText("source")
            test(source, root.resolve("workspace"))
        } finally {
            root.removeDirectory()
        }
    }

    private fun project(source: MPPPath, workspace: MPPPath, links: BuildSymlinks = BuildSymlinks.PRESERVE,
        mappings: List<BuildInput> = emptyList()): Project =
        Project("book", source, setOf(ProjectFile(source.resolve("book.tex"), FileType("tex", "^% +"))),
            buildDirectory = BuildDirectory(workspace, mappings = mappings, symlinks = links))

    private fun stage(project: Project) = BuildWorkspaces.prepare(project, listOf(project.executionDirectory))

    @Test
    fun rebasesRelativeAndAbsoluteLinksAndMaterializesCopiesWhenRequested() = fixture { source, workspace ->
        source.resolve("images").createDirectories()
        source.resolve("images/a.txt").writeText("image")
        Files.createSymbolicLink(source.resolve("relative").toJVMPath(), Path.of("images/a.txt"))
        Files.createSymbolicLink(source.resolve("absolute").toJVMPath(), source.resolve("images").toJVMPath())
        val project = project(source, workspace)
        stage(project)
        assertTrue(Files.isSymbolicLink(workspace.resolve("relative").toJVMPath()))
        assertEquals(Path.of("images/a.txt"), Files.readSymbolicLink(workspace.resolve("relative").toJVMPath()))
        assertEquals(Path.of("images"), Files.readSymbolicLink(workspace.resolve("absolute").toJVMPath()))
        workspace.resolve("relative").writeText("staged only")
        assertEquals("image", source.resolve("images/a.txt").readText())
        val copies = project(source, source.parent.resolve("copies"), BuildSymlinks.COPY)
        stage(copies)
        assertFalse(Files.isSymbolicLink(copies.executionDirectory.resolve("absolute").toJVMPath()))
        assertEquals("image", copies.executionDirectory.resolve("absolute/a.txt").readText())
        BuildWorkspaces.clean(project)
        assertEquals("image", source.resolve("images/a.txt").readText())
    }

    @Test
    fun externalLinksRequireMappingsInBothModes() = fixture { source, workspace ->
        val external = source.parent.resolve("external.txt").apply { writeText("external") }
        Files.createSymbolicLink(source.resolve("linked").toJVMPath(), external.toJVMPath())
        for (mode in BuildSymlinks.entries) {
            assertFailsWith<AraraException> { stage(project(source, workspace, mode)) }
        }
        val project = project(source, workspace, mappings = listOf(BuildInput(external, "vendor/external.txt")))
        stage(project)
        assertEquals(Path.of("vendor/external.txt"), Files.readSymbolicLink(workspace.resolve("linked").toJVMPath()))
        assertEquals("external", workspace.resolve("linked").readText())
    }

    @Test
    fun rejectsDirectoryLinkCyclesAndCanChangeFromPreservedLinksToCopies() = fixture { source, workspace ->
        source.resolve("first").createDirectories()
        source.resolve("second").createDirectories()
        Files.createSymbolicLink(source.resolve("first/next").toJVMPath(), Path.of("../second"))
        Files.createSymbolicLink(source.resolve("second/next").toJVMPath(), Path.of("../first"))
        for (mode in BuildSymlinks.entries) {
            assertFailsWith<AraraException> { stage(project(source, workspace, mode)) }
        }
        Files.delete(source.resolve("second/next").toJVMPath())
        source.resolve("second/input.txt").writeText("input")
        val preserved = project(source, workspace)
        stage(preserved)
        assertTrue(Files.isSymbolicLink(workspace.resolve("first/next").toJVMPath()))
        stage(project(source, workspace, BuildSymlinks.COPY))
        assertFalse(Files.isSymbolicLink(workspace.resolve("first/next").toJVMPath()))
        assertEquals("input", workspace.resolve("first/next/input.txt").readText())
    }

    @Test
    fun ignoredBrokenLinksAreSkippedAndTrackedFilesCannotBeReplacedByLinks() = fixture { source, workspace ->
        source.resolve(".gitignore").writeText("ignored*\n")
        Files.createSymbolicLink(source.resolve("ignored-link").toJVMPath(), Path.of("missing"))
        val project = project(source, workspace)
        stage(project)
        assertFalse(Files.exists(workspace.resolve("ignored-link").toJVMPath(), java.nio.file.LinkOption.NOFOLLOW_LINKS))
        Files.delete(workspace.resolve("book.tex").toJVMPath())
        val external = source.parent.resolve("external.txt").apply { writeText("keep") }
        Files.createSymbolicLink(workspace.resolve("book.tex").toJVMPath(), external.toJVMPath())
        assertFailsWith<AraraException> { stage(project) }
        assertEquals("keep", external.readText())
    }

    @Test
    fun rejectsLinkCyclesAndDestinationParentLinksWithoutTouchingTheirTargets() = fixture { source, workspace ->
        Files.createSymbolicLink(source.resolve("a").toJVMPath(), Path.of("b"))
        Files.createSymbolicLink(source.resolve("b").toJVMPath(), Path.of("a"))
        for (mode in BuildSymlinks.entries) {
            assertFailsWith<AraraException> { stage(project(source, workspace, mode)) }
        }
        Files.delete(source.resolve("a").toJVMPath())
        Files.delete(source.resolve("b").toJVMPath())
        source.resolve("assets").createDirectories()
        source.resolve("assets/input.txt").writeText("input")
        val project = project(source, workspace)
        stage(project)
        workspace.resolve("assets").removeDirectory()
        val outside = source.parent.resolve("outside").apply { createDirectories() }
        outside.resolve("input.txt").writeText("keep")
        Files.createSymbolicLink(workspace.resolve("assets").toJVMPath(), outside.toJVMPath())
        assertFailsWith<AraraException> { stage(project) }
        assertEquals("keep", outside.resolve("input.txt").readText())
        BuildWorkspaces.clean(project)
        assertEquals("keep", outside.resolve("input.txt").readText())
    }
}
