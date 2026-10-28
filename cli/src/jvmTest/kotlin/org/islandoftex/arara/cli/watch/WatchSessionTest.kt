// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.WatchMode
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.files.Project

@org.junit.jupiter.api.Timeout(15)
class WatchSessionTest {

    @Test
    fun repairedFailureAndQueuedEditsFinishWithLatestGraphResult() = fixture { root ->
        val input = root.resolve("main.tex").apply { writeText("first") }
        val graph = WatchGraph(listOf(Project("p", root, emptySet())), emptySet())
        val interrupts = WatchInterrupts()
        val session = WatchSession(graph, { graph }, { _, selected, previous, cycle ->
            assertEquals(setOf("p"), selected)
            if (cycle == 1) {
                input.writeText("edited during build")
                Thread.sleep(100)
                mapOf("p" to 1)
            } else {
                assertEquals(1, previous["p"])
                interrupts.request()
                mapOf("p" to 0)
            }
        }, WatchReporter(), WatchSettings(WatchMode.POLLING, 1, 10))
        assertEquals(0, session.run(interrupts))
    }

    @Test
    fun invalidSpecificationPausesUntilCorrected() = fixture { root ->
        val specification = root.resolve("project.proj.lua").apply { writeText("bad") }
        val graph = WatchGraph(listOf(Project("p", root, emptySet())), setOf(specification))
        val fallback = graph.copy(projects = emptyList())
        val attempted = CountDownLatch(1)
        val interrupts = WatchInterrupts()
        val fixer = Thread {
            assertTrue(attempted.await(5, TimeUnit.SECONDS))
            Thread.sleep(100)
            specification.writeText("good")
        }.apply { start() }
        try {
            val session = WatchSession(fallback, {
                if (specification.readText() == "bad") {
                    attempted.countDown()
                    throw org.islandoftex.arara.api.AraraException("invalid specification")
                }
                graph
            }, { _, _, _, _ -> interrupts.request(); mapOf("p" to 0) }, WatchReporter(),
                WatchSettings(WatchMode.POLLING, 1, 10))
            assertEquals(0, session.run(interrupts))
        } finally {
            fixer.join(5000)
        }
    }

    @Test
    fun secondInterruptStopsExecutionAndProducesInterruptionStatus() = fixture { root ->
        val graph = WatchGraph(listOf(Project("p", root, emptySet())), emptySet())
        val interrupts = WatchInterrupts()
        val session = WatchSession(graph, { graph }, { _, _, _, _ ->
            interrupts.request()
            interrupts.request()
            assertTrue(Thread.currentThread().isInterrupted)
            mapOf("p" to 1)
        }, WatchReporter(), WatchSettings(WatchMode.POLLING, 1, 10))
        assertEquals(130, session.run(interrupts))
        assertTrue(!Thread.currentThread().isInterrupted)
    }

    private fun fixture(action: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara-watch-session").toMPPPath()
        try { action(root) } finally { LoggingUtils.finishRun(false); root.removeDirectory() }
    }
}
