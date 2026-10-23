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
import java.nio.file.Files
import java.util.jar.JarFile

private const val FAILURE_STATUS = 7
private const val APPLICATION_ERROR = 2

private fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\")
    .replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\""

private class Smoke(private val jar: MPPPath, private val java: MPPPath,
    private val fixture: MPPPath, private val jna: MPPPath) {
    private val launcher = listOf(java.toString(), "--module-path", jna.toString(),
        "--add-modules=com.sun.jna", "--enable-native-access=com.sun.jna")

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
        val result = run((launcher + listOf("-Duser.home=$root", "-jar", jar.toString(),
            "-L", "en", "-d", root.toString())) + flags + reference, root, input, environment)
        check(result.status == expected) { "Expected exit $expected, got ${result.status}\n${result.output}" }
        return result.output
    }

    private fun contains(output: String, text: String) = check(text in output) { "Missing $text\n$output" }

    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "console_greeting_totals_and_styled_command_fields" to { root ->
            rule(root, native("0"))
            luaRule(root, luaNative("0"))
            for (engine in listOf("yaml", "lua")) {
                for (mode in listOf("plain", "styled")) {
                    val flags = listOf("--engine", engine, "--console", mode)
                    val output = execute(root, 0, flags + "--verbose")
                    check("kotlin-logging:" !in output) { "Logging bootstrap leaked into console output" }
                    contains(output, "the cool TeX automation tool.")
                    contains(output, "TOTAL SUCCESS")
                    contains(output, "1 file, 1 task")
                    check(Regex("SMOKE_STDOUT").findAll(output).count() == 1)
                    if (mode == "styled") contains(output, "System command:")
                    val dry = execute(root, 0, flags + "--dry-run")
                    contains(dry, "DRY RUN")
                    check("SUCCESS" !in dry)
                    check("SMOKE_STDOUT" !in dry)
                    check("\u001b" !in output && "\u001b" !in dry)
                }
            }
        },
        "directive_file_arrays_replicate_execution_in_both_engines" to { root ->
            val trace = root.resolve("files.trace")
            rule(root, "writeToFile(${quoted(trace.toString())}, reference.fileName + '|', true); return true;")
            luaRule(root, "write_to_file(${quoted(trace.toString())}, " +
                "ctx.reference.file_name .. '|', true); return true")
            root.resolve("first.tex").writeText("")
            root.resolve("second.tex").writeText("")
            root.resolve("run.tex").writeText("% arara: probe: { files: [first.tex, second.tex] }\n")
            for (engine in listOf("yaml", "lua")) {
                trace.writeText("")
                val output = execute(root, 0, listOf("--engine", engine))
                check(trace.readText() == "first.tex|second.tex|") {
                    "$engine did not preserve both file references: ${trace.readText()}\n$output"
                }
            }
        },
        "missing_rules_name_the_selected_engine_format" to { root ->
            root.resolve("run.tex").writeText("% arara: notarulequack\n")
            for (engine in listOf("yaml", "lua")) {
                contains(execute(root, APPLICATION_ERROR, listOf("--engine", engine)), "notarulequack.$engine")
            }
        },
        "lua_configuration_precedes_yaml_independently_of_rule_engine" to { root ->
            root.resolve("arararc.yaml").writeText("!config\nloops: invalid\n")
            root.resolve("arararc.lua").writeText("return { paths = { cwd } }\n")
            rule(root, "return true;")
            luaRule(root, "return true")
            for (engine in listOf("yaml", "lua")) execute(root, 0, listOf("--engine", engine))
        },
        "packaged_native_terminal_provider_and_access" to { _ ->
            JarFile(jar.toJVMFile()).use { archive ->
                val provider = "com.github.ajalt.mordant.terminal.terminalinterface.jna.TerminalInterfaceProviderJna"
                check(archive.getJarEntry(provider.replace('.', '/') + ".class") != null) {
                    "The shaded jar lost the native terminal provider"
                }
                val service = checkNotNull(archive.getJarEntry(
                    "META-INF/services/com.github.ajalt.mordant.terminal.TerminalInterfaceProvider"
                )) { "Missing terminal provider service registration" }
                val registration = archive.getInputStream(service).bufferedReader().use { it.readText() }
                check(provider in registration) { "Native terminal provider is not registered" }
                check(archive.getJarEntry("com/sun/jna/Native.class") == null) { "JNA must be a separate module" }
                check(archive.manifest.mainAttributes.getValue("Enable-Native-Access") == null) {
                    "The application must not grant native access to all classpath code"
                }
            }
            JarFile(jna.toJVMFile()).use { archive ->
                check(archive.manifest.mainAttributes.getValue("Automatic-Module-Name") == "com.sun.jna")
                check(archive.getJarEntry("com/sun/jna/Native.class") != null)
                check(archive.entries().asSequence().any { it.name.endsWith("libjnidispatch.so") })
            }
        },
        "native_access_is_limited_to_jna" to { root ->
            val version = run(listOf(java.toString(), "-version"), root)
            val feature = Regex("version \"(\\d+)").find(version.output)?.groupValues?.get(1)?.toInt()
                ?: error("Cannot determine Java version: ${version.output}")
            val restriction = if (feature >= 24) listOf("--illegal-native-access=deny") else emptyList()
            val result = run(launcher + restriction +
                listOf("-cp", fixture.toString(), "SmokeProcess", "native-access"), root)
            check(result.status == 0) { result.output }
            contains(result.output, "NATIVE_ACCESS_ISOLATED")
        },
        "packaged_version_and_completion" to { root ->
            val command = launcher + listOf("-jar", jar.toString())
            val version = run(command + "--version", root)
            check(version.status == 0) { version.output }
            contains(version.output, "arara")
            contains(version.output, "Island of TeX")
            val completion = run(command + listOf("--generate-completion", "bash"), root)
            check(completion.status == 0) { completion.output }
            contains(completion.output, "--engine")
            contains(completion.output, "--log-format")
        },
        "help_and_empty_invocation_ignore_invalid_configuration" to { root ->
            root.resolve("arararc.yaml").writeText("!config\nloops: not-a-number\n")
            for (flags in listOf(emptyList(), listOf("--help"))) {
                val result = run((launcher + listOf("-Duser.home=$root", "-jar", jar.toString())) + flags, root)
                check(result.status == 0) { result.output }
                contains(result.output, "--engine")
                contains(result.output, "--whole-file")
                check(!root.resolve("arara.log").exists)
            }
        },
        "invalid_cli_bounds_and_choices_fail_before_execution" to { root ->
            rule(root, "writeToFile('must-not-run.txt', 'ran'); return true;")
            val invalid = listOf(
                listOf("--max-loops", "0"),
                listOf("--timeout", "0"),
                listOf("--engine", "unknown"),
                listOf("--console", "unknown"),
                listOf("--log-format", "unknown")
            )
            for (flags in invalid) {
                val output = execute(root, 1, flags)
                contains(output, flags.first())
                contains(output, "invalid value")
                check(!root.resolve("must-not-run.txt").exists) { "Invalid CLI arguments executed a rule" }
            }
        },
        "whole_file_enables_body_directives_in_both_engines" to { root ->
            val trace = root.resolve("body-ran.txt")
            rule(root, "writeToFile(${quoted(trace.toString())}, 'body', false); return true;")
            luaRule(root, "write_to_file(${quoted(trace.toString())}, 'body', false); return true")
            root.resolve("run.tex").writeText("\\documentclass{article}\n% arara: probe\n")
            for (engine in listOf("yaml", "lua")) {
                execute(root, APPLICATION_ERROR, listOf("--engine", engine))
                check(!trace.exists) { "Header-only parsing executed a body directive" }
                execute(root, 0, listOf("--engine", engine, "--whole-file"))
                check(trace.readText() == "body")
                Files.delete(trace.toJVMPath())
            }
        },
        "command_line_loop_limit_caps_both_engines" to { root ->
            val trace = root.resolve("loop.txt")
            rule(root, "writeToFile(${quoted(trace.toString())}, 'x', true); return true;")
            luaRule(root, "write_to_file(${quoted(trace.toString())}, 'x', true); return true")
            root.resolve("run.tex").writeText("% arara: probe while true\n")
            for (engine in listOf("yaml", "lua")) {
                trace.writeText("")
                execute(root, 0, listOf("--engine", engine, "--max-loops", "2"))
                check(trace.readText() == "xx") { "$engine ignored the command line loop limit" }
            }
        },
        "configured_logname_receives_messages_in_both_formats" to { root ->
            rule(root, "return true;")
            for (format in listOf("yaml", "lua")) {
                val name = "$format-diagnostics.log"
                root.resolve("arararc.$format").writeText(if (format == "yaml") {
                    "!config\npaths: ['.']\nlogname: '$name'\n"
                } else "return { paths = { cwd }, log = { file = '$name' } }")
                execute(root, 0, listOf("--log-format", "text"))
                contains(root.resolve(name).readText(), "Run completed with exit code 0")
            }
        },
        "command_line_preamble_supplies_directives_in_both_engines" to { root ->
            rule(root, "return true;")
            luaRule(root, "return true")
            root.resolve("run.tex").writeText("\\documentclass{article}\n")
            for (format in listOf("yaml", "lua")) {
                root.resolve("arararc.$format").writeText(if (format == "yaml") {
                    "!config\npaths: ['.']\npreambles: { build: '% arara: probe' }\n"
                } else "return { paths = { cwd }, preamble = { definitions = { build = {'% arara: probe'} } } }")
                for (engine in listOf("yaml", "lua")) {
                    val output = execute(root, 0, listOf("--engine", engine, "--preamble", "build"))
                    contains(output, "SUCCESS")
                }
            }
        },
        "nested_command_lists_launch_every_task_in_both_engines" to { root ->
            val yaml = native("0").removePrefix("return ").removeSuffix(";")
            val lua = luaNative("0").removePrefix("return ")
            rule(root, "return [$yaml, [$yaml]];")
            luaRule(root, "return {$lua, {$lua}}")
            for (engine in listOf("yaml", "lua")) {
                val output = execute(root, 0, listOf("--engine", engine, "--verbose"))
                for (marker in listOf("SMOKE_STDOUT", "SMOKE_STDERR")) {
                    check(Regex(marker).findAll(output).count() == 2) { "$engine did not launch both tasks\n$output" }
                }
            }
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
        "dialog_choices_in_both_engines_and_presentations" to { root ->
            rule(root, "return showOptions(4, 'Options', 'Pick one', 'Only') == 0 && " +
                "showList(4, 'List', 'Optional', 'Only') == 0;")
            luaRule(root, "return show_options(4, 'Options', 'Pick one', 'Only') == 0 and " +
                "show_list(4, 'List', 'Optional', 'Only') == 0")
            listOf("yaml", "lua").forEach { engine ->
                listOf("plain", "styled").forEach { console ->
                    execute(root, 0, listOf("--engine", engine, "--console", console), input = "0\n0\n")
                }
            }
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
            contains(execute(root, APPLICATION_ERROR), "terminal input ended")
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
        "redirected_terminal_environment_preserves_stream_encoding" to { root ->
            rule(root, "return true;")
            val reference = "ação-λ.tex"
            root.resolve(reference).writeText("% arara: probe\n")
            for ((encoding, expected) in listOf("UTF-8" to reference, "US-ASCII" to "a??o-?.tex")) {
                val result = run(launcher + listOf("-Dstdout.encoding=$encoding", "-Dstderr.encoding=$encoding",
                    "-Duser.home=$root", "-jar", jar.toString(), "-L", "en", "-d", root.toString(),
                    "--dry-run", reference), root, environment = mapOf("TERM" to "xterm", "MSYSTEM" to "MINGW64"))
                check(result.status == 0) { result.output }
                contains(result.output, expected)
            }
        },
        "redirected_long_running_tasks_never_animate_in_either_configuration_format" to { root ->
            rule(root, native("sleep"))
            for (format in listOf("yaml", "lua")) {
                val config = root.resolve("arararc.$format")
                for (enabled in listOf(false, true)) {
                    config.writeText(if (format == "yaml")
                        "!config\npaths: ['.']\nconsole: styled\nanimations: $enabled\n"
                        else "return { paths = { cwd }, console = 'styled', animations = $enabled }")
                    val output = execute(root, APPLICATION_ERROR, listOf("--timeout", "1500"), environment = mapOf(
                        "TERM" to "xterm", "FORCE_COLOR" to "3", "CI" to "true"
                    ))
                    check("\u001b" !in output) { "Redirected task emitted terminal controls" }
                    check("⠋" !in output) { "Redirected task emitted progress frames" }
                    contains(output, "FAILURE")
                }
                Files.delete(config.toJVMPath())
            }
        },
        "styled_verbose_streams_once_and_dry_run_does_not_launch" to { root ->
            rule(root, native("7"))
            val verbose = execute(root, FAILURE_STATUS, listOf("--console", "styled", "--verbose"))
            check(Regex("SMOKE_STDERR").findAll(verbose).count() == 1) { "Verbose output was repeated" }
            val dry = execute(root, 0, listOf("--console", "styled", "--dry-run"))
            contains(dry, "SmokeProcess")
            contains(dry, "DRY RUN")
            check("SUCCESS" !in dry) { "Styled dry run claimed execution success" }
            check("SMOKE_STDOUT" !in dry) { "Styled dry run launched a command" }
        },
        "styled_summary_names_unprocessed_files_after_an_exception" to { root ->
            root.resolve("first.tex").writeText("% arara: missingrule\n")
            root.resolve("second.tex").writeText("% arara: probe\n")
            val output = execute(root, APPLICATION_ERROR, listOf("--console", "styled", "first.tex"), "second.tex")
            val summary = output.substringAfter("Execution summary")
            contains(summary, "first.tex")
            contains(summary, "FAILURE (2)")
            contains(summary, "second.tex")
            contains(summary, "Not processed")
        },
        "styled_summary_keeps_projects_across_presentation_changes" to { root ->
            for (name in listOf("plain", "styled")) {
                val directory = root.resolve(name).apply { createDirectories() }
                directory.resolve("arararc.yaml").writeText("!config\npaths: ['.']\nconsole: $name\n")
                directory.resolve("main.tex").writeText("% arara: probe\n% arara: probe if false\n")
                rule(directory, "return true;")
            }
            root.resolve("projects.proj.lua").writeText("""
                return {
                  { name = "plain", working_directory = "plain", files = { ["main.tex"] = {} } },
                  { name = "styled", working_directory = "styled", dependencies = {"plain"},
                    files = { ["main.tex"] = {} } }
                }
            """.trimIndent())
            val output = execute(root, 0, reference = "projects.proj.lua")
            contains(output, "TOTAL SUCCESS · 2 files, 2 tasks")
            val summary = output.substringAfter("Execution summary")
            contains(summary, "Project: plain")
            contains(summary, "Project: styled")
            check(Regex("main.tex").findAll(summary).count() == 2) { "Summary lost a project's file\n$output" }
            check("\u001b" !in output) { "Piped project summary contains ANSI escapes" }
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
        "verbosity_overrides_apply_to_each_project_without_leaking_configuration" to { root ->
            val project = root.resolve("verbose project").apply { createDirectories() }
            initialize(project)
            project.resolve("arararc.lua").writeText("return { paths = { cwd }, verbose = true }\n")
            root.resolve("mixed.proj.lua").writeText(
                "return { working_directory = 'verbose project', files = { ['run.tex'] = {} } }\n"
            )
            rule(root, native("0"))
            rule(project, native("0"))
            luaRule(root, luaNative("0"))
            luaRule(project, luaNative("0"))
            listOf("yaml", "lua").forEach { engine ->
                listOf(
                    emptyList<String>() to 1,
                    listOf("--silent") to 0,
                    listOf("--verbose") to 2,
                    listOf("--silent", "--verbose") to 0
                ).forEach { (flags, expected) ->
                    val output = execute(root, 0, listOf("--engine", engine) + flags + "mixed.proj.lua")
                    val directories = output.lineSequence().filter { it.startsWith("SMOKE_CWD=") }.toSet()
                    val expectedDirectories = when (expected) {
                        0 -> emptySet()
                        1 -> setOf("SMOKE_CWD=$project")
                        else -> setOf("SMOKE_CWD=$project", "SMOKE_CWD=$root")
                    }
                    check(directories == expectedDirectories) {
                        "$engine flags=$flags: unexpected verbose project directories $directories\n$output"
                    }
                    listOf("SMOKE_STDOUT", "SMOKE_STDERR").forEach { marker ->
                        val count = Regex(marker).findAll(output).count()
                        check(count == expected) {
                            "$engine flags=$flags: expected $expected tool output records, got $count\n$output"
                        }
                    }
                }
            }
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
                if (args[0].equals("native-access")) {
                    Module jna = Class.forName("com.sun.jna.Native").getModule();
                    if (!"com.sun.jna".equals(jna.getName())) throw new AssertionError("Unnamed JNA");
                    java.lang.reflect.Method access = Module.class.getMethod("isNativeAccessEnabled");
                    if (!(Boolean) access.invoke(jna) ||
                        (Boolean) access.invoke(SmokeProcess.class.getModule())) {
                        throw new AssertionError("Native access is not isolated");
                    }
                    System.out.println("NATIVE_ACCESS_ISOLATED");
                    return;
                }
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

fun main(args: Array<String>) {
    require(args.size % 2 == 0) { "Expected --jar APP_JAR --java JAVA [--javac JAVAC] [--report XML]" }
    val options = args.toList().chunked(2).associate { (key, value) -> key to value }
    val allowed = setOf("--jar", "--java", "--javac", "--report", "--jna", "--suite", "--work-dir")
    require(options.keys.all { it in allowed }) { "Unknown option" }
    when (options["--suite"] ?: "process") {
        "tex" -> return runTexIntegration(options)
        "process" -> require("--work-dir" !in options) { "--work-dir requires --suite tex" }
        else -> error("Unknown suite: ${options["--suite"]}")
    }
    val jar = MPPPath(options.getValue("--jar")).normalize()
    val java = MPPPath(options.getValue("--java")).normalize()
    val javac = options["--javac"]?.let { MPPPath(it).normalize() }
        ?: java.resolveSibling(if (java.fileName.endsWith(".exe")) "javac.exe" else "javac")
    val destination = MPPPath(options["--report"] ?: "build/test-results/processSmoke/results.xml").normalize()
    val cases = mutableListOf<CaseResult>()
    runCatching {
        temporary { fixture ->
            compileFixture(fixture, javac)
            val jna = MPPPath(options["--jna"] ?: jar.resolveSibling("jna.jar").toString())
            Smoke(jar, java, fixture, jna).cases.forEach { (name, test) ->
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
