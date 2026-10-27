// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.watch

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.BuildDirectory
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.WatchOptions
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.files.Project

class WatchInputsTest {

    @Test
    fun detectsContentsAdditionsAndDeletionsButNotTimestampOnlyChanges() = fixture { root ->
        val source = root.resolve("source.tex").apply { writeText("first") }
        val inputs = WatchInputs(listOf(Project("p", root, emptySet())))
        val first = inputs.capture().getValue("p")
        Files.setLastModifiedTime(source.toJVMPath(), java.nio.file.attribute.FileTime.fromMillis(1))
        assertTrue(inputs.capture().getValue("p").changesSince(first).isEmpty())
        source.writeText("other")
        assertEquals(setOf(source), inputs.capture().getValue("p").changesSince(first))
        Files.delete(source.toJVMPath())
        assertEquals(setOf(source), inputs.capture().getValue("p").changesSince(first))
    }

    @Test
    fun separatesWatchOverridesFromBuildSelectionAndExcludesState() = fixture { root ->
        root.resolve(".gitignore").writeText("ignored/\n")
        root.resolve("ignored").createDirectories()
        root.resolve("ignored/chapter.tex").writeText("a")
        root.resolve("main.aux").writeText("generated")
        root.resolve("figure.pdf").writeText("asset")
        val workspace = root.resolve("output").apply { createDirectories() }
        workspace.resolve("copied.tex").writeText("copy")
        val project = Project("p", root, emptySet(), buildDirectory = BuildDirectory(workspace),
            watch = WatchOptions(include = listOf("ignored/**", "figure.pdf")))
        val inputs = WatchInputs(listOf(project))
        val before = inputs.capture().getValue("p")
        root.resolve("ignored/chapter.tex").writeText("b")
        root.resolve("main.aux").writeText("new output")
        root.resolve("figure.pdf").writeText("new asset")
        workspace.resolve("copied.tex").writeText("new copy")
        assertEquals(setOf(root.resolve("ignored/chapter.tex"), root.resolve("figure.pdf")),
            inputs.capture().getValue("p").changesSince(before))
    }

    @Test
    fun sharedExternalInputsAreAttributedToEveryProject() = fixture { root ->
        val external = root.resolve("shared.bib").apply { writeText("a") }
        val one = root.resolve("one").apply { createDirectories() }
        val two = root.resolve("two").apply { createDirectories() }
        val inputs = WatchInputs(listOf(Project("a", one, emptySet(), watch = WatchOptions(listOf(external))),
            Project("b", two, emptySet(), watch = WatchOptions(listOf(external)))))
        val before = inputs.capture()
        external.writeText("b")
        assertTrue(inputs.capture().all { (name, snapshot) -> snapshot.changesSince(before.getValue(name)) == setOf(external) })
    }

    @Test
    fun followsSymlinksWithoutDirectoryCyclesAndDetectsReplacement() = fixture { root ->
        if (System.getProperty("os.name").startsWith("Windows")) return@fixture
        val directory = root.resolve("source").apply { createDirectories() }
        directory.resolve("a.tex").writeText("a")
        val link = root.resolve("link")
        Files.createSymbolicLink(link.toJVMPath(), directory.toJVMPath())
        Files.createSymbolicLink(directory.resolve("cycle").toJVMPath(), root.toJVMPath())
        val inputs = WatchInputs(listOf(Project("p", root, emptySet())))
        val before = inputs.capture().getValue("p")
        Files.delete(link.toJVMPath())
        Files.createSymbolicLink(link.toJVMPath(), root.resolve("missing").toJVMPath())
        val after = inputs.capture().getValue("p")
        assertTrue(link in after.changesSince(before))
        assertFalse(after.problems.isEmpty())
    }

    @Test
    fun linksCannotReintroduceExcludedWorkspacesOrLogs() = fixture { root ->
        if (System.getProperty("os.name").startsWith("Windows")) return@fixture
        val workspace = root.resolve("output").apply { createDirectories() }
        val output = workspace.resolve("generated.tex").apply { writeText("first") }
        val log = root.resolve("arara.log").apply { writeText("first") }
        Files.createSymbolicLink(root.resolve("alias").toJVMPath(), workspace.toJVMPath())
        Files.createSymbolicLink(root.resolve("log-alias.tex").toJVMPath(), log.toJVMPath())
        val project = Project("p", root, emptySet(), buildDirectory = BuildDirectory(workspace))
        val inputs = WatchInputs(listOf(project), setOf(log))
        val before = inputs.capture().getValue("p")
        output.writeText("second")
        log.writeText("second")
        assertTrue(inputs.capture().getValue("p").changesSince(before).isEmpty())
    }

    @Test
    fun externalDirectoriesRespectTheirOwnIgnoreFilesAndGeneratedDefaults() = fixture { root ->
        val source = root.resolve("source").apply { createDirectories() }
        val shared = root.resolve("shared").apply { createDirectories() }
        shared.resolve(".gitignore").writeText("ignored.bib\n")
        val ignored = shared.resolve("ignored.bib").apply { writeText("before") }
        val generated = shared.resolve("output.log").apply { writeText("before") }
        val bibliography = shared.resolve("refs.bib").apply { writeText("before") }
        val project = Project("p", source, emptySet(), watch = WatchOptions(listOf(shared)))
        val inputs = WatchInputs(listOf(project))
        val before = inputs.capture().getValue("p")
        ignored.writeText("after")
        generated.writeText("after")
        bibliography.writeText("after")
        assertEquals(setOf(bibliography), inputs.capture().getValue("p").changesSince(before))
    }

    private fun fixture(action: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara-watch").toMPPPath()
        try { action(root) } finally { root.removeDirectory() }
    }
}
