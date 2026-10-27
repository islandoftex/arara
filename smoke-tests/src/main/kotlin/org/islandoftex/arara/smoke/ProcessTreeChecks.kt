// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.util.concurrent.TimeUnit
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile

private const val TREE_WAIT_SECONDS = 20L
private const val TREE_POLL_MILLIS = 20L

/** Exercises the packaged application's ownership of a real child/grandchild process tree. */
internal class ProcessTreeCheck(private val root: MPPPath, private val application: List<String>,
    private val tool: List<String>) {
    private val output = root.resolve("tree.output.txt")
    private val handles = mutableListOf<ProcessHandle>()

    fun interrupted() = withTree("tree-root",
        listOf("--log-format", "text", "--log-retention", "on-failure")) { process ->
        ready()
        process.destroy() // SIGTERM on Unix; TerminateProcess (without JVM hooks) on Windows.
        check(process.waitFor(TREE_WAIT_SECONDS, TimeUnit.SECONDS)) { "arara did not terminate" }
        await("descendant termination") { handles.none { it.isAlive } }
        check(root.resolve("arara.log").readText().isNotEmpty()) { "Interrupted run lost its diagnostic log" }
    }

    fun timedOut() = withTree("tree-root", listOf("--timeout", "10000")) { process ->
        ready()
        check(process.waitFor(TREE_WAIT_SECONDS, TimeUnit.SECONDS)) { "arara did not time out" }
        check(process.exitValue() == 2) { output.readText() }
        await("descendant termination after timeout") { handles.none { it.isAlive } }
    }

    fun detached() = withTree("tree-detach") { process ->
        ready(detached = true)
        check(process.waitFor(TREE_WAIT_SECONDS, TimeUnit.SECONDS)) { "arara did not finish" }
        check(process.exitValue() == 0) { output.readText() }
        check(handles.all { it.isAlive }) { "Normal completion killed a detached process" }
    }

    private fun ready(detached: Boolean = false) {
        await("process-tree readiness") { root.resolve("tree.ready").exists }
        val modes = if (detached) listOf("tree-child", "tree-leaf") else listOf("tree-root", "tree-child", "tree-leaf")
        modes.forEach { mode ->
            val pid = root.resolve("$mode.pid").readText().trim().toLong()
            handles.add(ProcessHandle.of(pid).orElseThrow())
        }
    }

    private fun withTree(mode: String, flags: List<String> = emptyList(), body: (Process) -> Unit) {
        root.resolve("probe.lua").writeText("return {identifier='probe', name='Process tree', " +
            "commands={{command=function(ctx) return get_command(" +
            (tool + listOf(mode, root.toString())).joinToString(", ") { quoted(it) } + ") end}}}\n")
        val process = ProcessBuilder(application + flags + "run.tex").directory(root.toJVMFile())
            .redirectErrorStream(true).redirectOutput(output.toJVMFile()).start()
        try {
            body(process)
        } finally {
            // Retain handles before terminating parents, including on readiness/assertion failures.
            for (modeName in listOf("tree-leaf", "tree-child", "tree-root")) {
                val file = root.resolve("$modeName.pid")
                if (file.exists) ProcessHandle.of(file.readText().trim().toLong()).ifPresent { handle ->
                    handle.destroyForcibly()
                    runCatching { handle.onExit().get(TREE_WAIT_SECONDS, TimeUnit.SECONDS) }
                }
            }
            if (process.isAlive) process.destroyForcibly()
            process.waitFor(TREE_WAIT_SECONDS, TimeUnit.SECONDS)
        }
    }

    private fun await(label: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TREE_WAIT_SECONDS)
        while (!condition()) {
            check(System.nanoTime() < deadline) { "Timed out waiting for $label\n${output.readText()}" }
            Thread.sleep(TREE_POLL_MILLIS)
        }
    }
}
