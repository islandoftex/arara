// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.watch.WatchMode

class WatchScannerTest {

    @Test
    fun collectsEditsDuringExecutionAndReloadsAfterControlCreation() = fixture { root ->
        val input = root.resolve("source.tex").apply { writeText("before") }
        val config = root.resolve("arararc.lua")
        val graph = WatchGraph(listOf(Project("p", root, emptySet())), setOf(config))
        val changes = WatchChanges(0)
        WatchScanner(graph, changes, WatchMode.POLLING, 10).use { scanner ->
            scanner.start()
            input.writeText("after")
            val edit = await { changes.take() }
            assertEquals(setOf("p"), edit.projects)
            config.writeText("return {}")
            assertTrue(await { changes.take() }.reload)
            val replacement = graph.copy(projects = listOf(Project("next", root, emptySet())))
            scanner.replace(replacement)
            input.writeText("third")
            assertEquals(setOf("next"), await { changes.take() }.projects)
        }
    }

    @Test
    fun ruleCandidatesIncludeNewFilesAndRemovedRules() = fixture { root ->
        val rules = root.resolve("rules").apply { createDirectories() }
        val graph = WatchGraph(emptyList(), emptySet(), setOf(rules))
        val before = graph.observe()
        val rule = rules.resolve("probe.lua").apply { writeText("return {}") }
        val after = graph.observe()
        assertTrue(before.controls != after.controls)
        Files.delete(rule.toJVMPath())
        assertEquals(before.controls, graph.observe().controls)
    }

    private fun <T> await(value: () -> T?): T {
        val deadline = System.nanoTime() + 5_000_000_000L
        var result = value()
        while (result == null && System.nanoTime() < deadline) {
            Thread.sleep(10)
            result = value()
        }
        return assertNotNull(result, "Watch scanner did not observe the change")
    }

    private fun fixture(action: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara-watch-scanner").toMPPPath()
        try { action(root) } finally { root.removeDirectory() }
    }
}
