// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.io.ByteArrayOutputStream
import java.nio.file.Files
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import java.util.concurrent.TimeUnit
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import kotlin.concurrent.thread

private const val PROCESS_TIMEOUT_SECONDS = 30L
private const val READER_TIMEOUT_MILLIS = 5000L
private const val FAILURE_STATUS = 7
private const val APPLICATION_ERROR = 2

private fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\")
    .replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\""

private data class Result(val status: Int, val output: String)
private data class CaseResult(val name: String, val failure: Throwable?)

/** Checks use MPPPath; ProcessBuilder requires a File at its working-directory boundary. */
private fun run(command: List<String>, directory: MPPPath, input: String = ""): Result {
    val process = ProcessBuilder(command).directory(directory.toJVMFile()).redirectErrorStream(true).start()
    val output = ByteArrayOutputStream()
    val reader = thread(isDaemon = true, name = "smoke-output") {
        process.inputStream.use { it.copyTo(output) }
    }
    try {
        process.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(input) }
        check(process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            "Process timed out: $command\n${output.toString(Charsets.UTF_8)}"
        }
        reader.join(READER_TIMEOUT_MILLIS)
        check(!reader.isAlive) { "Process output did not close: $command" }
        return Result(process.exitValue(), output.toString(Charsets.UTF_8))
    } finally {
        process.descendants().use { children -> children.forEach { it.destroyForcibly() } }
        if (process.isAlive) process.destroyForcibly()
        reader.join(READER_TIMEOUT_MILLIS)
    }
}

private class Smoke(private val jar: MPPPath, private val java: MPPPath, private val fixture: MPPPath) {
    private fun rule(root: MPPPath, expression: String) {
        root.resolve("probe.yaml").writeText(
            "!config\nidentifier: probe\nname: Smoke probe\ncommands:\n  - command: " +
                quoted("@{ $expression }") + "\narguments: []\n"
        )
    }

    private fun luaRule(root: MPPPath, body: String, exit: String = "code == 0") {
        root.resolve("probe.lua").writeText(
            "return {identifier='probe', name='Lua probe', commands={{command=function(ctx) " +
                body + " end, exit=function(code) return " + exit + " end}}}\n"
        )
    }

    private fun luaNative(mode: String): String =
        "return get_command(" + listOf(java.toString(), "-cp", fixture.toString(), "SmokeProcess", mode)
            .joinToString(", ") { quoted(it) } + ")"

    private fun native(mode: String): String = listOf(java.toString(), "-cp", fixture.toString(), "SmokeProcess", mode)
        .joinToString(", ", "return getCommand([", "]);") { quoted(it) }

    private fun execute(root: MPPPath, expected: Int, flags: List<String> = emptyList(),
                        reference: String = "run.tex", input: String = ""): String {
        val result = run(listOf(java.toString(), "-Duser.home=$root", "-jar", jar.toString(),
            "-L", "en", "-d", root.toString()) + flags + reference, root, input)
        check(result.status == expected) { "Expected exit $expected, got ${result.status}\n${result.output}" }
        return result.output
    }

    private fun contains(output: String, text: String) = check(text in output) { "Missing $text\n$output" }

    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "lua_native_exit_and_output" to { root ->
            luaRule(root, luaNative(FAILURE_STATUS.toString()))
            val output = execute(root, FAILURE_STATUS, listOf("--engine", "lua", "--verbose"))
            contains(output, "SMOKE_STDOUT")
            contains(output, "SMOKE_STDERR")
            contains(output, "SMOKE_CWD=$root")
        },
        "lua_custom_exit_accepts_nonzero" to { root ->
            luaRule(root, luaNative(FAILURE_STATUS.toString()), "code == $FAILURE_STATUS")
            execute(root, 0, listOf("--engine", "lua"))
        },
        "lua_custom_exit_rejects_zero" to { root ->
            luaRule(root, luaNative("0"), "false")
            execute(root, 1, listOf("--engine", "lua"))
        },
        "lua_halt_stops_the_file" to { root ->
            luaRule(root, "halt($FAILURE_STATUS)")
            root.resolve("run.tex").writeText("% arara: probe\n% arara: missingrule\n")
            execute(root, FAILURE_STATUS, listOf("--engine", "lua"))
        },
        "lua_conditional_and_scalar_interpolation" to { root ->
            root.resolve("probe.lua").writeText("""
                return {identifier='probe', name='Lua probe', commands={{command=function(ctx)
                  return ctx.options[1] == 'value-3'
                end}}, arguments={{identifier='options', flag=function(ctx, value) return value end}}}
            """.trimIndent())
            root.resolve("run.tex").writeText(
                "% arara: probe: { options: 'value-{{1+2}}' } if true and not false\n"
            )
            execute(root, 0, listOf("--engine", "lua"))
            root.resolve("run.tex").writeText(
                "% arara: probe: { options: ['value-{{1+2}}'] } unless false\n"
            )
            execute(root, 0, listOf("--engine", "lua"))
        },
        "lua_safe_mode_leaves_options_literal" to { root ->
            root.resolve("probe.lua").writeText("""
                return {identifier='probe', name='Lua probe', commands={{command=function(ctx)
                  return ctx.options[1] == '{{error("must not execute")}}'
                end}}, arguments={{identifier='options', flag=function(ctx, value) return value end}}}
            """.trimIndent())
            root.resolve("run.tex").writeText(
                "% arara: probe: { options: ['{{error(\"must not execute\")}}'] }\n"
            )
            execute(root, 0, listOf("--engine", "lua", "--safe-run"))
        },
        "lua_dry_run_does_not_launch" to { root ->
            luaRule(root, "return get_command('arara-nonexistent-command')")
            execute(root, 0, listOf("--engine", "lua", "--dry-run"))
        },
        "lua_does_not_fall_back_to_yaml" to { root ->
            rule(root, "return true;")
            execute(root, APPLICATION_ERROR, listOf("--engine", "lua"))
        },
        "yaml_remains_the_default" to { root ->
            rule(root, "return true;")
            luaRule(root, "return false")
            execute(root, 0)
            execute(root, 1, listOf("--engine", "lua"))
        },
        "native_exit_and_both_output_streams" to { root ->
            rule(root, native(FAILURE_STATUS.toString()))
            val output = execute(root, FAILURE_STATUS, listOf("--verbose"))
            contains(output, "SMOKE_STDOUT")
            contains(output, "SMOKE_STDERR")
            contains(output, "SMOKE_CWD=$root")
        },
        "successful_native_command" to { root ->
            rule(root, native("0"))
            execute(root, 0)
        },
        "failed_launch_is_an_application_error" to { root ->
            rule(root, "return getCommand([${quoted(root.resolve("missing-executable").toString())}]);")
            execute(root, APPLICATION_ERROR)
        },
        "timeout_is_an_application_error" to { root ->
            rule(root, native("sleep"))
            execute(root, APPLICATION_ERROR, listOf("--timeout", "100"))
        },
        "error_halt_keeps_its_status_and_stops_the_file" to { root ->
            rule(root, "halt($FAILURE_STATUS);")
            root.resolve("run.tex").writeText("% arara: probe\n% arara: missingrule\n")
            execute(root, FAILURE_STATUS)
        },
        "successful_halt_stops_the_file" to { root ->
            rule(root, "halt(0);")
            root.resolve("run.tex").writeText("% arara: probe\n% arara: missingrule\n")
            execute(root, 0)
        },
        "terminal_input_uses_redirected_stdin" to { root ->
            rule(root, "return showInput(0, 'Smoke input', 'Type the answer') == 'answer';")
            contains(execute(root, 0, input = "answer\n"), "Smoke input")
        },
        "terminal_eof_is_an_application_error" to { root ->
            rule(root, "return showInput(0, 'Smoke input', 'Type the answer') == 'answer';")
            contains(execute(root, APPLICATION_ERROR), "input is closed")
        },
        "safe_mode_blocks_class_loading" to { root ->
            rule(root, "loadClass('missing.jar', 'Missing'); return true;")
            execute(root, APPLICATION_ERROR, listOf("--safe-run"))
        },
        "ordinary_lua_is_not_executed_as_a_specification" to { root ->
            rule(root, "return true;")
            root.resolve("arararc.yaml").writeText(
                "!config\npaths: ['.']\nfiletypes:\n  - extension: lua\n    pattern: '^-- +'\n"
            )
            root.resolve("ordinary.lua").writeText("-- arara: probe\nerror('must not execute')\n")
            execute(root, 0, reference = "ordinary.lua")
        },
        "malformed_project_is_an_application_error" to { root ->
            root.resolve("invalid.proj.lua").writeText("return { files = false }\n")
            contains(execute(root, APPLICATION_ERROR, reference = "invalid.proj.lua"), "project.files")
        },
        "mixed_projects_and_loose_inputs_run_with_separate_configuration" to { root ->
            val project = root.resolve("named").apply { createDirectories() }
            initialize(project)
            rule(project, "getSession().put('named', true); return true;")
            rule(root, "getSession().get('named'); return true;")
            root.resolve("named.proj.lua").writeText(
                "return { working_directory = 'named', files = { ['run.tex'] = {} } }\n"
            )
            execute(root, 0, flags = listOf("named.proj.lua"))
        },
        "shared_project_input_is_rejected_before_execution" to { root ->
            rule(root, "halt($FAILURE_STATUS);")
            root.resolve("named.proj.lua").writeText("return { files = { ['run.tex'] = {} } }\n")
            val output = execute(root, APPLICATION_ERROR, flags = listOf("named.proj.lua"))
            contains(output.replace(Regex("\\s+"), " "), "belongs to both")
        },
        "project_loads_configuration_from_its_working_directory" to { root ->
            val project = root.resolve("project with spaces").apply { createDirectories() }
            initialize(project)
            rule(project, "return true;")
            root.resolve("project.proj.lua").writeText(
                "return { name = 'Smoke', working_directory = ${quoted(project.toString().replace('\\', '/'))}, " +
                    "files = { ['run.tex'] = { file_type = { extension = 'tex' } } } }\n"
            )
            execute(root, 0, reference = "project.proj.lua")
        }
    )
}

private fun initialize(root: MPPPath) {
    root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
    root.resolve("run.tex").writeText("% arara: probe\n")
}

private fun compileFixture(root: MPPPath, javac: MPPPath) {
    val source = root.resolve("SmokeProcess.java")
    source.writeText("""
        public class SmokeProcess {
            public static void main(String[] args) throws Exception {
                System.out.println("SMOKE_STDOUT");
                System.err.println("SMOKE_STDERR");
                System.out.println("SMOKE_CWD=" + System.getProperty("user.dir"));
                System.out.flush();
                System.err.flush();
                if (args[0].equals("sleep")) Thread.sleep(10000);
                System.exit(args[0].equals("sleep") ? 0 : Integer.parseInt(args[0]));
            }
        }
    """.trimIndent())
    val result = run(listOf(javac.toString(), "--release", "11", source.toString()), root)
    check(result.status == 0) { "Cannot compile smoke fixture:\n${result.output}" }
}

private fun XMLStreamWriter.writeCase(case: CaseResult) {
    writeStartElement("testcase")
    writeAttribute("classname", "arara.ProcessSmoke")
    writeAttribute("name", case.name)
    case.failure?.let { failure ->
        writeStartElement("failure")
        writeCharacters(failure.stackTraceToString().filter { it >= ' ' || it in "\n\r\t" })
        writeEndElement()
    }
    writeEndElement()
}

private fun report(destination: MPPPath, cases: List<CaseResult>) {
    destination.parent.createDirectories()
    Files.newOutputStream(destination.toJVMPath()).use { stream ->
        val xml = XMLOutputFactory.newFactory().createXMLStreamWriter(stream, "UTF-8")
        try {
            xml.writeStartDocument("UTF-8", "1.0")
            xml.writeStartElement("testsuite")
            xml.writeAttribute("name", "arara.process-smoke")
            xml.writeAttribute("tests", cases.size.toString())
            xml.writeAttribute("failures", cases.count { it.failure != null }.toString())
            xml.writeAttribute("errors", "0")
            cases.forEach { xml.writeCase(it) }
            xml.writeEndElement()
            xml.writeEndDocument()
        } finally {
            xml.close()
        }
    }
}

private fun temporary(body: (MPPPath) -> Unit) {
    val root = Files.createTempDirectory("arara smoke ").toRealPath().toMPPPath()
    try { body(root) } finally { root.removeDirectory() }
}

fun main(args: Array<String>) {
    require(args.size % 2 == 0) { "Expected --jar APP_JAR --java JAVA [--javac JAVAC] [--report XML]" }
    val options = args.toList().chunked(2).associate { (key, value) -> key to value }
    require(options.keys.all { it in setOf("--jar", "--java", "--javac", "--report") }) { "Unknown option" }
    val jar = MPPPath(options.getValue("--jar")).normalize()
    val java = MPPPath(options.getValue("--java")).normalize()
    val javac = options["--javac"]?.let { MPPPath(it).normalize() }
        ?: java.resolveSibling(if (java.fileName.endsWith(".exe")) "javac.exe" else "javac")
    val destination = MPPPath(options["--report"] ?: "build/test-results/processSmoke/results.xml").normalize()
    val cases = mutableListOf<CaseResult>()
    runCatching {
        temporary { fixture ->
            compileFixture(fixture, javac)
            Smoke(jar, java, fixture).cases.forEach { (name, test) ->
                val failure = runCatching { temporary { root -> initialize(root); test(root) } }.exceptionOrNull()
                cases.add(CaseResult(name, failure))
                println("${if (failure == null) "PASS" else "FAIL"} $name")
                failure?.printStackTrace()
            }
        }
    }.exceptionOrNull()?.let { cases.add(CaseResult("fixture_setup", it)) }
    report(destination, cases)
    check(cases.none { it.failure != null }) { "Process smoke tests failed; see $destination" }
}
