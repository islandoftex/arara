// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import java.io.ByteArrayOutputStream
import java.nio.file.Files
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
private fun run(command: List<String>, directory: MPPPath, input: String = "",
                environment: Map<String, String?> = emptyMap()): Result {
    val process = ProcessBuilder(command).directory(directory.toJVMFile()).redirectErrorStream(true)
        .apply {
            environment.forEach { (key, value) ->
                if (value == null) environment().remove(key) else environment()[key] = value
            }
        }.start()
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

    private fun officialRules(root: MPPPath) {
        listOf("pdflatex", "bibtex", "dvips", "mkdir", "rmdir", "halt").forEach { name ->
            listOf("yaml", "lua").forEach { extension ->
                val fileName = "arara-rule-$name.$extension"
                val text = checkNotNull(javaClass.getResourceAsStream("/rules/$fileName")) {
                    "Missing smoke resource $fileName"
                }.bufferedReader(Charsets.UTF_8).use { it.readText() }
                root.resolve(fileName).writeText(text)
            }
        }
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
                        reference: String = "run.tex", input: String = "",
                        environment: Map<String, String?> = emptyMap()): String {
        val result = run(listOf(java.toString(), "-Duser.home=$root", "-jar", jar.toString(),
            "-L", "en", "-d", root.toString()) + flags + reference, root, input, environment)
        check(result.status == expected) { "Expected exit $expected, got ${result.status}\n${result.output}" }
        return result.output
    }

    private fun contains(output: String, text: String) = check(text in output) { "Missing $text\n$output" }

    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "packaged_version_and_completion" to { root ->
            val command = listOf(java.toString(), "-jar", jar.toString())
            val version = run(command + "--version", root)
            check(version.status == 0) { version.output }
            contains(version.output, "arara")
            contains(version.output, "Island of TeX")
            val completion = run(command + listOf("--generate-completion", "bash"), root)
            check(completion.status == 0) { completion.output }
            contains(completion.output, "--engine")
            contains(completion.output, "--log-format")
        },
        "call_properties_override_property_files_in_both_engines" to { root ->
            root.resolve("arguments.properties").writeText("marker=from-file\n")
            listOf("yaml", "lua").forEach { engine ->
                if (engine == "lua") {
                    luaRule(root, "return get_session().get('arg:marker') == 'from-flag'")
                } else {
                    rule(root, "return getSession().get('arg:marker').equals('from-flag');")
                }
                execute(root, 0, listOf("--engine", engine, "--call-property", "marker=from-flag",
                    "--properties-file", "arguments.properties"))
            }
        },
        "lua_configuration_defines_custom_file_types" to { root ->
            root.resolve("arararc.lua").writeText(
                "return { paths = { cwd }, file_types = { { extension = 'src', pattern = '^@ +' } } }\n"
            )
            root.resolve("custom.src").writeText("@ arara: probe\n")
            rule(root, "return true;")
            contains(execute(root, 0, reference = "custom.src"), "SUCCESS")
        },
        "official_engine_bibliography_and_converter_rules" to { root ->
            officialRules(root)
            root.resolve("run.tex").writeText(
                "% arara: pdflatex: { interaction: nonstopmode, synctex: yes }\n" +
                    "% arara: bibtex\n% arara: dvips\n"
            )
            listOf("yaml", "lua").forEach { engine ->
                val output = execute(root, 0, listOf("--engine", engine, "--dry-run"))
                contains(output, "--interaction=nonstopmode")
                contains(output, "--synctex=1")
                contains(output, "bibtex")
                contains(output, "dvips")
            }
        },
        "official_lua_directory_and_halt_rules" to { root ->
            officialRules(root)
            root.resolve("run.tex").writeText(
                "% arara: mkdir: { target: 'created directory' }\n" +
                    "% arara: rmdir: { target: 'created directory' }\n" +
                    "% arara: halt\n% arara: missingrule\n"
            )
            execute(root, 0, listOf("--engine", "lua"))
            check(!root.resolve("created directory").exists)
        },
        "lua_terminal_input_and_safe_class_loading" to { root ->
            luaRule(root, "return show_input(0, 'Lua input', 'Type the answer') == 'answer'")
            contains(execute(root, 0, listOf("--engine", "lua"), input = "answer\n"), "Lua input")
            luaRule(root, "load_class('missing.jar', 'Missing'); return true")
            execute(root, APPLICATION_ERROR, listOf("--engine", "lua", "--safe-run"))
        },
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
        "invalid_directive_parameters_fail_in_both_engines" to { root ->
            rule(root, "return true;")
            luaRule(root, "return true")
            listOf("yaml", "lua").forEach { engine ->
                listOf("{ value: null }", "[one, two]", "{ value: [unterminated }").forEach { value ->
                    root.resolve("run.tex").writeText("% arara: probe: $value\n")
                    execute(root, APPLICATION_ERROR, listOf("--engine", engine))
                }
            }
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
        "verbosity_flags_override_yaml_and_lua_configuration" to { root ->
            rule(root, native("0"))
            listOf("yaml", "lua").forEach { format ->
                val config = root.resolve("arararc.$format")
                listOf(false, true).forEach { configured ->
                    config.writeText(if (format == "yaml") "!config\npaths: ['.']\nverbose: $configured\n"
                        else "return { paths = { cwd }, verbose = $configured }\n")
                    val cases = listOf(
                        emptyList<String>() to configured,
                        listOf("-v") to true,
                        listOf("--verbose") to true,
                        listOf("-s") to false,
                        listOf("--silent") to false,
                        listOf("-v", "-s") to false,
                        listOf("-s", "-v") to false
                    )
                    cases.forEach { (flags, expected) ->
                        val output = execute(root, 0, flags)
                        listOf("SMOKE_STDOUT", "SMOKE_STDERR").forEach { marker ->
                            check((marker in output) == expected) {
                                "$format verbose=$configured flags=$flags: unexpected tool output\n$output"
                            }
                        }
                    }
                }
                Files.delete(config.toJVMPath())
            }
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
        "lua_configuration_supports_both_rule_engines" to { root ->
            root.resolve("arararc.lua").writeText("""
                return { paths = { cwd }, preamble = { default = 'build',
                    definitions = { build = { '% arara: probe' } } } }
            """.trimIndent())
            root.resolve("run.tex").writeText("plain input\n")
            rule(root, "return true;")
            luaRule(root, "return true")
            contains(execute(root, 0), "Smoke probe")
            contains(execute(root, 0, listOf("--engine", "lua")), "Lua probe")
        },
        "invalid_lua_configuration_is_an_application_error" to { root ->
            root.resolve("arararc.lua").writeText("return { loops = 0 }\n")
            rule(root, "return true;")
            contains(execute(root, APPLICATION_ERROR), "arararc.lua")
        },
        "localized_configuration_and_project_errors" to { root ->
            root.resolve("arararc.lua").writeText("return { verbose = 'true' }\n")
            contains(execute(root, APPLICATION_ERROR, listOf("--language", "de")),
                "verbose muss ein boolescher Wert sein.")
            Files.delete(root.resolve("arararc.lua").toJVMPath())
            root.resolve("invalid.proj.lua").writeText("return { files = false }\n")
            contains(execute(root, APPLICATION_ERROR, listOf("--language", "de"), "invalid.proj.lua"),
                "project.files muss eine Tabelle sein.")
        },
        "localized_lua_rule_and_interpolation_errors" to { root ->
            root.resolve("probe.lua").writeText(
                "return { identifier = 'probe', name = 'Probe', commands = { { command = true } } }\n"
            )
            val flags = listOf("--engine", "lua", "--language", "de")
            contains(execute(root, APPLICATION_ERROR, flags), "commands[1].command muss eine Funktion sein.")
            luaRule(root, "return true")
            root.resolve("run.tex").writeText("% arara: probe: { options: '{{ 1' }\n")
            val output = execute(root, APPLICATION_ERROR, flags)
            contains(output, "Interpolations-Tag nicht abgeschlossen.")
            check("MVEL" !in output) { "Lua interpolation reported as MVEL: $output" }
        },
        "styled_presentation_preserves_engines_and_failure_output" to { root ->
            rule(root, native("7"))
            val environment = mapOf(
                "CI" to "true", "GITLAB_CI" to "true", "TERM" to "xterm", "FORCE_COLOR" to "3", "NO_COLOR" to null
            )
            val output = execute(root, FAILURE_STATUS, listOf("--console", "styled"), environment = environment)
            contains(output, "FAILURE")
            contains(output, "SMOKE_STDERR")
            check("\u001b" !in output) { "Piped styled output contains ANSI escapes" }
            luaRule(root, "return true")
            val lua = execute(root, 0, listOf("--engine", "lua", "--console", "styled"), environment = environment)
            check("\u001b" !in lua) { "Piped Lua styled output contains ANSI escapes" }
            contains(lua, "SUCCESS")
            contains(lua, "run.tex")
        },
        "styled_verbose_streams_once_and_dry_run_does_not_launch" to { root ->
            rule(root, native("7"))
            val verbose = execute(root, FAILURE_STATUS, listOf("--console", "styled", "--verbose"))
            check(Regex("SMOKE_STDERR").findAll(verbose).count() == 1) { "Verbose output was repeated" }
            val dry = execute(root, 0, listOf("--console", "styled", "--dry-run"))
            contains(dry, "SmokeProcess")
            check("SMOKE_STDOUT" !in dry) { "Styled dry run launched a command" }
        },
        "styled_summary_names_unprocessed_files_after_an_exception" to { root ->
            root.resolve("first.tex").writeText("% arara: missingrule\n")
            root.resolve("second.tex").writeText("% arara: probe\n")
            val output = execute(root, APPLICATION_ERROR, listOf("--console", "styled", "first.tex"), "second.tex")
            contains(output, "first.tex · FAILURE")
            contains(output, "second.tex · Not processed")
        },
        "plain_and_styled_presentations_keep_identical_diagnostic_logs" to { root ->
            rule(root, "return true;")
            fun logFor(mode: String): String {
                execute(root, 0, listOf("--console", mode, "--log"))
                return root.resolve("arara.log").readText()
                    .replace(Regex("(?m)^.*? (TRACE|DEBUG|INFO|WARN|ERROR) +- "), "$1 - ")
                    .replace(Regex("Elapsed time: [0-9.,]+ seconds"), "Elapsed time: TIME seconds")
            }
            check(logFor("plain") == logFor("styled")) { "Console mode changed the diagnostic log" }
        },
        "diagnostic_log_records_exact_rule_command_and_application_outcomes" to { root ->
            rule(root, native("7"))
            execute(root, FAILURE_STATUS, listOf("--log"))
            val log = root.resolve("arara.log").readText()
            contains(log, "Rule file: \"${root.resolve("probe.yaml")}\" (engine: yaml)")
            contains(log, "working directory: $root")
            contains(log, "System command exited with value 7 after ")
            contains(log, "Run failed with exit code 7")
            rule(root, native("sleep"))
            execute(root, APPLICATION_ERROR, listOf("--log", "--timeout", "500"))
            val timeout = root.resolve("arara.log").readText()
            contains(timeout, "System command failed (TIMEOUT) after ")
            contains(timeout, "Run failed with exit code 2")
        },
        "english_log_labels_preserve_the_localized_console" to { root ->
            rule(root, "return true;")
            val output = execute(root, 0, listOf("--log", "--language", "de"))
            contains(output, "ERFOLGREICH")
            val log = root.resolve("arara.log").readText()
            contains(log, "Task result: SUCCESS")
            contains(log, "Run completed with exit code 0")
            contains(log, "Elapsed time: ")
        },
        "session_mutations_from_both_engines_are_traced_without_values" to { root ->
            rule(root, "s=getSession(); s.put('trace', 'HIDDEN_SESSION_VALUE'); " +
                "s.put('trace', 'SECOND_HIDDEN_VALUE'); s.remove('trace'); return true;")
            luaRule(root, "local s=get_session(); s.put('trace', 'HIDDEN_SESSION_VALUE'); " +
                "s.put('trace', 'SECOND_HIDDEN_VALUE'); s.remove('trace'); return true")
            for (engine in listOf("yaml", "lua")) {
                execute(root, 0, listOf("--log", "--engine", engine))
                val log = root.resolve("arara.log").readText()
                contains(log, "Session variable 'trace' set.")
                contains(log, "Session variable 'trace' overwritten.")
                contains(log, "Session variable 'trace' removed.")
                check("HIDDEN_SESSION_VALUE" !in log && "SECOND_HIDDEN_VALUE" !in log) {
                    "Session mutation records included variable values"
                }
            }
        },
        "json_logging_is_available_in_the_packaged_jar" to { root ->
            rule(root, "return true;")
            execute(root, 0)
            check(!root.resolve("arara.log").exists) { "Default format created a log file" }
            val output = execute(root, 0, listOf("--log-format", "json"))
            contains(output, "SUCCESS")
            val records = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
            check(records.isNotEmpty()) { "JSON logging produced no events" }
            check(records.any { it["message"]?.jsonPrimitive?.content == "Run completed with exit code 0" }) {
                "JSON log has no closing run record"
            }
            root.resolve("arararc.lua").writeText(
                "return { paths = { cwd }, log = { " +
                    "file = cwd .. '/configured.jsonl', format = 'json' } }")
            execute(root, 0)
            root.resolve("configured.jsonl").readLines().forEach { Json.parseToJsonElement(it).jsonObject }
            val previous = root.resolve("configured.jsonl").readText()
            execute(root, 0, listOf("-l", "--log-format", "none"))
            check(root.resolve("configured.jsonl").readText() == previous) { "NONE did not override logging" }
            execute(root, 0, listOf("-l"))
            check(!root.resolve("configured.jsonl").readText().startsWith("{")) { "Text shorthand was ignored" }
            execute(root, 0, listOf("--log", "--log-format", "json"))
            root.resolve("configured.jsonl").readLines().forEach { Json.parseToJsonElement(it).jsonObject }
        },
        "json_events_keep_native_and_application_outcomes_distinct" to { root ->
            rule(root, native(FAILURE_STATUS.toString()))
            luaRule(root, luaNative(FAILURE_STATUS.toString()), "code == $FAILURE_STATUS")
            val runIds = mutableSetOf<String>()
            for (engine in listOf("yaml", "lua")) {
                val expected = if (engine == "yaml") FAILURE_STATUS else 0
                execute(root, expected, listOf("--engine", engine, "--log-format", "json",
                    "--console", "styled", "--language", "de"))
                val events = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
                fun event(name: String) = events.single { it["event"]?.jsonPrimitive?.content == name }
                val ruleEvent = event("rule.resolved").getValue("data").jsonObject
                check(ruleEvent.getValue("engine").jsonPrimitive.content == engine)
                check(ruleEvent.getValue("path").jsonPrimitive.content == root.resolve("probe.$engine").toString())
                val command = event("command.completed").getValue("data").jsonObject
                check(command.getValue("exitCode").jsonPrimitive.int == FAILURE_STATUS)
                check(!command.getValue("exitCode").jsonPrimitive.isString)
                check(command.getValue("durationMs").jsonPrimitive.long >= 0)
                val task = event("task.result").getValue("data").jsonObject
                check(task.getValue("success").jsonPrimitive.boolean == (expected == 0))
                val outcome = event(if (expected == 0) "run.completed" else "run.failed").getValue("data").jsonObject
                check(outcome.getValue("exitCode").jsonPrimitive.int == expected)
                val ids = events.map { it.getValue("runId").jsonPrimitive.content }.toSet()
                check(ids.size == 1 && runIds.add(ids.single())) { "Run IDs were missing or reused" }
            }
        },
        "json_events_describe_process_failures_without_fake_exit_codes" to { root ->
            rule(root, native("sleep"))
            execute(root, APPLICATION_ERROR, listOf("--log-format", "json", "--timeout", "500"))
            val events = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
            val failure = events.single { it["event"]?.jsonPrimitive?.content == "command.failed" }
            val data = failure.getValue("data").jsonObject
            check(data.getValue("failureKind").jsonPrimitive.content == "TIMEOUT")
            check("exitCode" !in data)
            check("TimeoutException" in failure.getValue("exception").jsonPrimitive.content)
            val run = events.single { it["event"]?.jsonPrimitive?.content == "run.failed" }
            check(run.getValue("data").jsonObject.getValue("exitCode").jsonPrimitive.int == APPLICATION_ERROR)
        },
        "database_changes_persist_between_processes" to { root ->
            val existing = "!database\nmap: {}\n"
            root.resolve("arara.yaml").writeText(existing)
            luaRule(root, "if not changed(current_file()) then halt($FAILURE_STATUS) end; return true")
            val flags = listOf("--engine", "lua")
            execute(root, 0, flags)
            check(root.resolve("arara.yaml").isRegularFile) { "Missing default arara.yaml cache" }
            execute(root, FAILURE_STATUS, flags)
            root.resolve("run.tex").writeText("\n% updated source\n", append = true)
            execute(root, 0, flags)
        },
        "configured_database_rejects_malformed_yaml_without_overwriting" to { root ->
            root.resolve("cache").createDirectories()
            root.resolve("arararc.lua").writeText(
                "return { paths = { cwd }, database = 'cache/custom.yaml' }\n"
            )
            luaRule(root, "changed(current_file()); return true")
            val flags = listOf("--engine", "lua")
            execute(root, 0, flags)
            val cache = root.resolve("cache/custom.yaml")
            check(cache.isRegularFile) { "Configured database path was not used" }
            check(!root.resolve("custom.yaml").exists) { "Database parent directory was discarded" }
            val malformed = "!database\nmap: [unterminated\n"
            cache.writeText(malformed)
            val output = execute(root, APPLICATION_ERROR, flags).replace(Regex("\\s+"), " ")
            contains(output, "YAML cache integrity")
            check(cache.readText() == malformed) { "Malformed cache was overwritten" }
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
        "project_and_lua_configuration_share_script_context" to { root ->
            val project = root.resolve("sources").apply { createDirectories() }
            rule(project, "return true;")
            project.resolve("run.tex").writeText("% arara: probe\n")
            project.resolve("arararc.lua").writeText("""
                assert(cwd ~= user.home and cwd:sub(-7) == 'sources')
                return { paths = { cwd } }
            """.trimIndent())
            root.resolve("context.proj.lua").writeText("""
                assert(cwd == user.home)
                return { name = user.name, working_directory = cwd .. '/sources',
                    files = { ['run.tex'] = {} } }
            """.trimIndent())
            contains(execute(root, 0, reference = "context.proj.lua"), "Smoke probe")
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
