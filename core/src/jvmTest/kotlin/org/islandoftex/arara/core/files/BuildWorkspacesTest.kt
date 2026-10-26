// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.BuildInput
import org.islandoftex.arara.api.files.BuildSelection
import org.islandoftex.arara.api.files.BuildSymlinks
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

class BuildWorkspacesTest {
    private fun fixture(test: (MPPPath, MPPPath) -> Unit) {
        val temporary = Files.createTempDirectory("arara-workspace").toMPPPath()
        val source = temporary.resolve("source").apply { createDirectories() }
        source.resolve("book.tex").writeText("% arara: pdflatex\n")
        try {
            test(source, temporary.resolve("workspace"))
        } finally {
            temporary.removeDirectory()
        }
    }

    private fun project(source: MPPPath, workspace: MPPPath, inputs: List<String>? = null,
        mappings: List<BuildInput> = emptyList(), links: BuildSymlinks = BuildSymlinks.PRESERVE): Project =
        Project("book", source, setOf(ProjectFile(source.resolve("book.tex"), FileType("tex", "^% +"))),
            buildDirectory = BuildDirectory(workspace, inputs?.map {
                if (it.any { character -> character in "*?[" }) BuildSelection.Glob(it)
                else BuildSelection.Path(MPPPath(it))
            }, mappings, links))

    private fun stage(project: Project) = BuildWorkspaces.prepare(project, listOf(project.executionDirectory))

    @Test
    fun inputsUpdateIncrementallyWithoutConsumingConditionalHashesOrDeletingOutputs() = fixture { source, workspace ->
        val project = project(source, workspace)
        val input = source.resolve("asset.bin")
        input.writeText("first")
        val conditional = source.resolve("changes.yaml")
        assertTrue(FileHandling.hasChanged(input, conditional))
        val prepared = stage(project)
        assertEquals(prepared.files.single().path, BuildWorkspaces.prepare(prepared, listOf(workspace)).files.single().path)
        val second = BuildWorkspaces.prepare(project.copy(name = "second", buildDirectory =
            BuildDirectory(source.parent.resolve("second-workspace"))), emptyList())
        assertFailsWith<AraraException> {
            org.islandoftex.arara.core.dependencies.ProjectValidation.executionOrder(listOf(prepared, second))
        }
        assertEquals(source, prepared.workingDirectory)
        assertEquals(workspace, prepared.executionDirectory)
        assertEquals(source.resolve("book.tex"), prepared.files.single().sourcePath)
        assertEquals(workspace.resolve("book.tex"), prepared.files.single().path)
        val staged = workspace.resolve("asset.bin")
        val timestamp = FileTime.fromMillis(1_000_000)
        Files.setLastModifiedTime(staged.toJVMPath(), timestamp)
        workspace.resolve("book.pdf").writeText("output")
        stage(project)
        assertEquals(timestamp, Files.getLastModifiedTime(staged.toJVMPath()))
        input.writeText("second")
        stage(project)
        assertEquals("second", staged.readText())
        assertTrue(FileHandling.hasChanged(input, conditional))
        assertFalse(FileHandling.hasChanged(input, conditional))
        Files.delete(staged.toJVMPath())
        stage(project)
        assertEquals("second", staged.readText())
        Files.delete(input.toJVMPath())
        stage(project)
        assertFalse(staged.exists)
        assertEquals("output", workspace.resolve("book.pdf").readText())
        BuildWorkspaces.clean(project)
        assertFalse(workspace.exists)
        stage(project)
        assertTrue(workspace.resolve("book.tex").exists)
    }

    @Test
    fun defaultsHonorNestedGitRulesAndDockerReinclusionsButAlwaysStageTargets() = fixture { source, workspace ->
        source.resolve(".gitignore").writeText("*.tmp\nignored/\n!ignored/keep.txt\nbook.tex\n/root-only\n")
        source.resolve(".dockerignore").writeText("docker/\n!docker/keep.txt\n")
        for (name in listOf("root-only", "no.tmp", "yes.txt", "ignored/keep.txt", "docker/keep.txt",
            "docker/no.txt", "nested/root-only", "nested/yes.tmp", "nested/keep.tmp", ".git/config")) {
            source.resolve(name).parent.createDirectories()
            source.resolve(name).writeText(name)
        }
        source.resolve("nested/.gitignore").writeText("!keep.tmp\n")
        val project = project(source, workspace)
        stage(project)
        for (name in listOf("yes.txt", "book.tex", "nested/root-only", "nested/keep.tmp", "docker/keep.txt")) {
            assertTrue(workspace.resolve(name).exists, name)
        }
        for (name in listOf("no.tmp", "root-only", "ignored/keep.txt", "docker/no.txt", "nested/yes.tmp", ".git")) {
            assertFalse(workspace.resolve(name).exists, name)
        }
        source.resolve(".gitignore").writeText("yes.txt\n")
        stage(project)
        assertFalse(workspace.resolve("yes.txt").exists)
    }

    @Test
    fun explicitSelectionsOverrideIgnoresAndSupportDirectoriesGlobsAndExternalMappings() = fixture { source, workspace ->
        source.resolve(".gitignore").writeText("*\n")
        source.resolve("chapters").createDirectories()
        source.resolve("chapters/a.tex").writeText("chapter")
        source.resolve("refs.bib").writeText("bibliography")
        source.resolve("unselected.txt").writeText("leave out")
        val external = source.parent.resolve("shared").apply { createDirectories() }
        external.resolve("style.sty").writeText("style")
        val project = project(source, workspace, listOf("chapters", "*.bib"), listOf(BuildInput(external, "styles")))
        stage(project)
        for (name in listOf("book.tex", "chapters/a.tex", "refs.bib", "styles/style.sty")) {
            assertTrue(workspace.resolve(name).exists, name)
        }
        assertFalse(workspace.resolve("unselected.txt").exists)
        val noExtraInputs = project(source, source.parent.resolve("minimal"), emptyList())
        stage(noExtraInputs)
        assertEquals(listOf(".arara-workspace", "book.tex"), noExtraInputs.executionDirectory.listEntries()
            .map { it.fileName }.sorted())
        assertFailsWith<AraraException> { stage(project(source, source.parent.resolve("bad"), listOf("../shared"))) }
        assertFailsWith<AraraException> { stage(project(source, source.parent.resolve("bad"), listOf("missing.*"))) }
    }

    @Test
    fun rejectsOverlapsEscapesCollisionsAndUnmanagedDirectories() = fixture { source, workspace ->
        assertFailsWith<AraraException> { BuildWorkspaces.validate(listOf(project(source, source))) }
        assertFailsWith<AraraException> { BuildWorkspaces.validate(listOf(project(source, source.parent))) }
        assertFailsWith<AraraException> { BuildWorkspaces.validate(listOf(project(source, workspace),
            project(source, workspace.resolve("nested")).copy(name = "other"))) }
        val unsafe = listOf("../escape", ".arara-workspace/owner", workspace.toString(), ".")
        unsafe.forEach { name ->
            assertFailsWith<AraraException> {
                stage(project(source, workspace, emptyList(), listOf(BuildInput(source.resolve("book.tex"), name))))
            }
        }
        workspace.createDirectories()
        workspace.resolve("existing.txt").writeText("keep")
        assertFailsWith<AraraException> { stage(project(source, workspace)) }
        assertFailsWith<AraraException> { BuildWorkspaces.clean(project(source, workspace)) }
        assertEquals("keep", workspace.resolve("existing.txt").readText())
        Files.delete(workspace.resolve("existing.txt").toJVMPath())
        val project = project(source, workspace)
        stage(project)
        workspace.resolve("generated.txt").writeText("output")
        source.resolve("generated.txt").writeText("new input")
        assertFailsWith<AraraException> { stage(project) }
        assertEquals("output", workspace.resolve("generated.txt").readText())
        assertFailsWith<AraraException> { BuildWorkspaces.clean(project.copy(name = "different")) }
    }

    @Test
    fun excludesAllBuildDirectoriesAndKeepsOutputsInStaleInputDirectories() = fixture { source, _ ->
        val workspace = source.resolve("build/book")
        val other = source.resolve("build/other").apply { createDirectories() }
        other.resolve("output.txt").writeText("other project output")
        source.resolve("images").createDirectories()
        source.resolve("images/input.txt").writeText("input")
        val project = project(source, workspace)
        BuildWorkspaces.validate(listOf(project))
        BuildWorkspaces.prepare(project, listOf(workspace, other))
        assertFalse(workspace.resolve("build/book").exists)
        assertFalse(workspace.resolve("build/other").exists)
        workspace.resolve("images/output.txt").writeText("output")
        source.resolve("images").removeDirectory()
        BuildWorkspaces.prepare(project, listOf(workspace, other))
        assertEquals("output", workspace.resolve("images/output.txt").readText())
        assertFalse(workspace.resolve("images/input.txt").exists)
    }

    @Test
    fun typedPathsRemainLiteralWhileGlobsSelectPatterns() = fixture { source, workspace ->
        source.resolve("notes[1].txt").writeText("literal brackets")
        source.resolve("notes1.txt").writeText("glob match")
        val literal = project(source, workspace).copy(buildDirectory = BuildDirectory(workspace,
            inputs = listOf(BuildSelection.Path(MPPPath("notes[1].txt")))))
        stage(literal)
        assertEquals("literal brackets", workspace.resolve("notes[1].txt").readText())
        assertFalse(workspace.resolve("notes1.txt").exists)
        val glob = project(source, source.parent.resolve("glob")).let {
            it.copy(buildDirectory = BuildDirectory(it.executionDirectory,
                inputs = listOf(BuildSelection.Glob("notes[1].txt"))))
        }
        stage(glob)
        assertTrue(glob.executionDirectory.resolve("notes1.txt").exists)
        assertFalse(glob.executionDirectory.resolve("notes[1].txt").exists)
        val absolute = literal.copy(buildDirectory = BuildDirectory(workspace,
            inputs = listOf(BuildSelection.Path(source.resolve("notes[1].txt")))))
        stage(absolute)
        assertTrue(workspace.resolve("notes[1].txt").exists)
        val external = literal.copy(buildDirectory = BuildDirectory(workspace,
            inputs = listOf(BuildSelection.Path(source.parent.resolve("external")))))
        assertFailsWith<AraraException> { stage(external) }
    }

    @Test
    fun corruptOwnershipAndCollidingMappingsFailWithoutOverwritingInputs() = fixture { source, workspace ->
        val project = project(source, workspace)
        source.resolve(".git").createDirectories()
        source.resolve(".git/book.tex").writeText("metadata")
        val metadata = project.copy(files = setOf(ProjectFile(source.resolve(".git/book.tex"),
            FileType("tex", "^% +"))))
        assertFailsWith<AraraException> { stage(metadata) }
        assertFalse(workspace.exists)
        stage(project)
        workspace.resolve(".arara-workspace/inputs.yaml").writeText("not: valid: yaml")
        assertFailsWith<AraraException> { stage(project) }
        assertFailsWith<AraraException> { BuildWorkspaces.clean(project) }
        assertTrue(workspace.resolve("book.tex").exists)
        source.resolve("other.tex").writeText("other")
        val collision = project(source, source.parent.resolve("collision"), emptyList(), listOf(
            BuildInput(source.resolve("book.tex"), "same.tex"),
            BuildInput(source.resolve("other.tex"), "same.tex")))
        assertFailsWith<AraraException> { stage(collision) }
        assertFalse(collision.executionDirectory.exists)
    }

    @Test
    fun pathOnlyPreparationDoesNotCreateDirectoriesOrReadMissingGeneratedFiles() = fixture { source, workspace ->
        val project = project(source, workspace).copy(files = setOf(ProjectFile(source.resolve("generated.tex"),
            FileType("tex", "^% +"))))
        val prepared = BuildWorkspaces.prepare(project, listOf(workspace), stage = false)
        assertEquals(workspace.resolve("generated.tex"), prepared.files.single().path)
        assertFalse(workspace.exists)
    }
}
