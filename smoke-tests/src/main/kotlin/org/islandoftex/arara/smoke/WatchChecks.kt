// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import java.util.concurrent.TimeUnit
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.api.files.toJVMPath

private const val WATCH_DEADLINE_SECONDS = 20L
private const val WATCH_POLL_MILLIS = 20L
private const val WATCH_INTERRUPTED = 130

/** Real packaged CLI sessions; a small fixture delivers INT through the JVM on every supported operating system. */
// Scenarios share one packaged-session lifecycle and readiness checks.
@Suppress("TooManyFunctions")
internal class WatchCheck(private val root: MPPPath, private val launcher: List<String>,
    private val jar: MPPPath, private val fixture: MPPPath) {
    private val output = root.resolve("watch-output.log")
    private val classpath = fixture.toString() + System.getProperty("path.separator") + jar
    private val application = launcher + listOf("-Duser.home=$root", "-cp", classpath,
        "WatchHarness", root.toString(), "-L", "en", "-d", root.toString(), "--watch",
        "--watch-mode", "polling", "--watch-debounce", "50", "--watch-interval", "30", "--console", "styled")

    fun queuedChanges() {
        setup()
        withSession { process ->
            await("first tool") { count("a") == 1 }
            root.resolve("a/run.tex").writeText("% arara: probe\n% edited during build\n")
            await("second build") { count("a") == 2 && count("b") == 2 && waiting(2) }
            check(count("c") == 1) { "Unrelated project rebuilt\n${output.readText()}" }
            stop(process, 0)
            check("Watch session: 2 builds; exit code 0." in output.readText()) { output.readText() }
        }
    }

    fun failureRecovery() {
        setup()
        root.resolve("a/watch-fail.log").writeText("fail")
        withSession(listOf("--log-retention", "on-failure")) { process ->
            await("failed cycle") { waiting(1) }
            check(count("b") == 0 && count("c") == 1) { "Failed dependent ran or independent project stopped" }
            Files.delete(root.resolve("a/watch-fail.log").toJVMPath())
            root.resolve("a/run.tex").writeText("% arara: probe\n% repaired\n")
            await("repaired cycle") { count("b") == 1 && waiting(2) }
            stop(process, 0)
            val log = root.resolve("session.log").readText()
            check("watch.cycle.failed" in log && "watch.cycle.completed" !in log) { log }
            check("Watch session: 2 builds; exit code 0." in output.readText()) { output.readText() }
        }
    }

    fun reloadConfiguration() {
        setup()
        withSession { process ->
            await("initial build") { waiting(1) }
            val spec = root.resolve("watch.proj.lua")
            val original = spec.readText()
            spec.writeText("return false")
            await("invalid specification") { "builds are paused" in output.readText() }
            check(count("a") == 1)
            spec.writeText(original)
            await("reloaded graph") { count("c") == 2 && waiting(2) }
            stop(process, 0)
        }
    }

    fun activeConfigurationIsFrozen() {
        setup()
        withSession { process ->
            await("first tool") { count("a") == 1 }
            val config = root.resolve("b/arararc.lua")
            val original = config.readText()
            config.writeText("return false")
            await("paused configuration reload") { "builds are paused" in output.readText() }
            check(count("b") == 1 && count("c") == 1) {
                "Configuration changed underneath the active build\n${output.readText()}"
            }
            config.writeText(original)
            await("repaired configuration") { count("c") == 2 && waiting(2) }
            stop(process, 0)
        }
    }

    fun gracefulInterrupt() {
        setup()
        withSession { process ->
            await("active tool") { count("a") == 1 }
            stop(process, 0)
            check(root.resolve("a/watch-finish.log").exists) { "First interrupt stopped the active tool" }
            check(count("c") == 1) { "First interrupt did not finish the current build" }
        }
    }

    fun forcedInterrupt() {
        setup()
        root.resolve("a/watch-delay.log").writeText("60000")
        withSession { process ->
            await("active tool") { root.resolve("a/watch-pid.log").exists }
            root.resolve("signal.log").writeText("1")
            await("graceful stop request") { "after the current build" in output.readText() }
            root.resolve("signal.log").writeText("2")
            finish(process, WATCH_INTERRUPTED)
            val handle = ProcessHandle.of(root.resolve("a/watch-pid.log").readText().toLong())
            check(handle.isEmpty || !handle.get().isAlive) { "Second interrupt left the tool running" }
            check(!root.resolve("a/watch-finish.log").exists)
        }
    }

    private fun setup() {
        val rules = root.resolve("rules").apply { createDirectories() }
        val command = listOf(launcher.first(), "-cp", fixture.toString(), "WatchHarness", "tool")
        rules.resolve("probe.lua").writeText("return {identifier='probe',name='Watch probe'," +
            "commands={{command=function(ctx) return get_command(" + command.joinToString(", ") { quoted(it) } +
            ", tostring(current_file().parent)) end}}}")
        listOf("a", "b", "c").forEach { name ->
            val directory = root.resolve(name).apply { createDirectories() }
            directory.resolve("run.tex").writeText("% arara: probe\n")
            directory.resolve("arararc.lua").writeText("return {paths={'../rules'},log={format='json'," +
                "file=" + quoted(root.resolve("session.log").toString()) + "}}")
        }
        root.resolve("watch.proj.lua").writeText("""
            return {
              {name='a', working_directory='a', files={['run.tex']={}}},
              {name='b', working_directory='b', files={['run.tex']={}}, dependencies={'a'}},
              {name='c', working_directory='c', files={['run.tex']={}}}
            }
        """.trimIndent())
    }

    private fun count(project: String): Int = root.resolve("$project/watch-count.log").let {
        if (it.exists) it.readLines().size else 0
    }

    private fun waiting(cycle: Int): Boolean = output.exists &&
        "Watch build $cycle:" in output.readText() &&
        output.readText().split("Watching for changes.").size > cycle

    private fun stop(process: Process, code: Int) {
        root.resolve("signal.log").writeText("1")
        finish(process, code)
    }

    private fun finish(process: Process, code: Int) {
        check(process.waitFor(WATCH_DEADLINE_SECONDS, TimeUnit.SECONDS)) {
            "Watch session did not exit\n${output.readText()}"
        }
        check(process.exitValue() == code) { "Expected watch exit $code\n${output.readText()}" }
    }

    private fun withSession(flags: List<String> = emptyList(), action: (Process) -> Unit) {
        val process = ProcessBuilder(application + flags + "watch.proj.lua").directory(root.toJVMFile())
            .redirectErrorStream(true).redirectOutput(output.toJVMFile()).start()
        try { action(process) } finally {
            process.descendants().use { children -> children.forEach { it.destroyForcibly() } }
            if (process.isAlive) process.destroyForcibly()
            process.waitFor(WATCH_DEADLINE_SECONDS, TimeUnit.SECONDS)
        }
    }

    private fun await(label: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WATCH_DEADLINE_SECONDS)
        while (!condition()) {
            check(System.nanoTime() < deadline) { "Timed out waiting for $label\n${output.readText()}" }
            Thread.sleep(WATCH_POLL_MILLIS)
        }
    }
}

internal fun compileWatchFixture(root: MPPPath, javac: MPPPath) {
    val source = root.resolve("WatchHarness.java")
    source.writeText(WATCH_FIXTURE)
    val result = run(listOf(javac.toString(), "--release", "11", source.toString()), root)
    check(result.status == 0) { "Cannot compile watch fixture:\n${result.output}" }
}

private val WATCH_FIXTURE = """
    import java.nio.file.*;
    public class WatchHarness {
        public static void main(String[] args) throws Exception {
            if (args[0].equals("tool")) {
                Path root = Path.of(args[1]);
                Files.writeString(root.resolve("watch-pid.log"), Long.toString(ProcessHandle.current().pid()));
                Files.writeString(root.resolve("watch-count.log"), "run\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                long delay = Files.exists(root.resolve("watch-delay.log"))
                    ? Long.parseLong(Files.readString(root.resolve("watch-delay.log"))) : 300;
                Thread.sleep(delay);
                Files.writeString(root.resolve("watch-finish.log"), "finished");
                System.exit(Files.exists(root.resolve("watch-fail.log")) ? 7 : 0);
                return;
            }
            Path requests = Path.of(args[0]).resolve("signal.log");
            Thread signals = new Thread(() -> {
                int delivered = 0;
                try {
                    Class<?> type = Class.forName("sun.misc.Signal");
                    Object signal = type.getConstructor(String.class).newInstance("INT");
                    while (true) {
                        if (Files.exists(requests)) {
                            String text = Files.readString(requests).trim();
                            int requested = text.isEmpty() ? delivered : Integer.parseInt(text);
                            while (delivered < requested) {
                                type.getMethod("raise", type).invoke(null, signal);
                                delivered++;
                            }
                        }
                        Thread.sleep(20);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.exit(91);
                }
            }, "watch-smoke-signals");
            signals.setDaemon(true);
            signals.start();
            String[] cli = java.util.Arrays.copyOfRange(args, 1, args.length);
            Class.forName("org.islandoftex.arara.cli.CLIKt").getMethod("main", String[].class)
                .invoke(null, (Object) cli);
        }
    }
""".trimIndent()
