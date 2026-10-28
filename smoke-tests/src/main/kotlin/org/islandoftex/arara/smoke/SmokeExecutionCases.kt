// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import java.util.jar.JarFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.api.files.toJVMPath

internal fun SmokeContext.executionCases(): Map<String, (MPPPath) -> Unit> =
    LuaExecutionChecks(this).cases +
        RuleExecutionChecks(this).cases +
        LauncherChecks(this).cases +
        CommandChecks(this).cases +
        LuaBoundaryChecks(this).cases +
        DialogChecks(this).cases

private class LuaExecutionChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "lua_default_and_legacy_compatibility_warnings" to ::luaDefaultAndLegacyCompatibilityWarnings,
        "lua_native_exit_and_output" to ::luaNativeExitAndOutput,
        "lua_custom_exit_accepts_nonzero" to ::luaCustomExitAcceptsNonzero,
        "lua_custom_exit_rejects_zero" to ::luaCustomExitRejectsZero,
        "lua_halt_stops_the_file" to ::luaHaltStopsTheFile,
        "lua_conditional_and_scalar_interpolation" to ::luaConditionalAndScalarInterpolation,
        "lua_preference_falls_back_to_yaml_with_a_warning" to ::luaPreferenceFallsBackToYamlWithAWarning,
        "legacy_engine_remains_available_explicitly" to ::legacyEngineRemainsAvailableExplicitly,
    )

    private fun luaDefaultAndLegacyCompatibilityWarnings(
        root: MPPPath
    ) {

        checkLegacyRuleWarnings(root, launcher, jar)
    }

    private fun luaNativeExitAndOutput(
        root: MPPPath
    ) {

        luaRule(root, luaNative(FAILURE_STATUS.toString()))
        val output = execute(root, FAILURE_STATUS, listOf("--preferred-engine", "lua", "--verbose"))
        contains(output, "SMOKE_STDOUT")
        contains(output, "SMOKE_STDERR")
        contains(output, "SMOKE_CWD=$root")
    }

    private fun luaCustomExitAcceptsNonzero(
        root: MPPPath
    ) {

        luaRule(root, luaNative(FAILURE_STATUS.toString()), "code == $FAILURE_STATUS")
        execute(root, 0, listOf("--preferred-engine", "lua"))
    }

    private fun luaCustomExitRejectsZero(
        root: MPPPath
    ) {

        luaRule(root, luaNative("0"), "false")
        execute(root, 1, listOf("--preferred-engine", "lua"))
    }

    private fun luaHaltStopsTheFile(
        root: MPPPath
    ) {

        luaRule(root, "halt($FAILURE_STATUS)")
        root.resolve("run.tex").writeText("% arara: probe\n% arara: missingrule\n")
        execute(root, FAILURE_STATUS, listOf("--preferred-engine", "lua"))
    }

    private fun luaConditionalAndScalarInterpolation(
        root: MPPPath
    ) {

        root.resolve("probe.lua").writeText("""
            return {identifier='probe', name='Lua probe', commands={{command=function(ctx)
              return ctx.options[1] == 'value-3'
            end}}, arguments={{identifier='options', flag=function(ctx, value) return value end}}}
        """.trimIndent())
        root.resolve("run.tex").writeText(
            "% arara: probe: { options: 'value-{{1+2}}' } if true and not false\n"
        )
        execute(root, 0, listOf("--preferred-engine", "lua"))
        root.resolve("run.tex").writeText(
            "% arara: probe: { options: ['value-{{1+2}}'] } unless false\n"
        )
        execute(root, 0, listOf("--preferred-engine", "lua"))
    }

    private fun luaPreferenceFallsBackToYamlWithAWarning(
        root: MPPPath
    ) {

        rule(root, "return true;")
        val output = execute(root, 0, listOf("--preferred-engine", "lua"))
        contains(output, "Compatibility warning")
    }

    private fun legacyEngineRemainsAvailableExplicitly(
        root: MPPPath
    ) {

        rule(root, "return true;")
        luaRule(root, "return false")
        execute(root, 0, listOf("--preferred-engine", "yaml"))
        execute(root, 1, listOf("--preferred-engine", "lua"))
    }

}

private class RuleExecutionChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "directive_file_arrays_replicate_execution_in_both_engines" to
            ::directiveFileArraysReplicateExecutionInBothEngines,
        "missing_rules_name_the_selected_engine_format" to ::missingRulesNameTheSelectedEngineFormat,
        "rule_discovery_lists_without_loading_and_inspects_the_effective_rule" to
            ::ruleDiscoveryListsWithoutLoadingAndInspectsTheEffectiveRule,
        "whole_file_enables_body_directives_in_both_engines" to ::wholeFileEnablesBodyDirectivesInBothEngines,
        "command_line_loop_limit_caps_both_engines" to ::commandLineLoopLimitCapsBothEngines,
        "command_line_preamble_supplies_directives_in_both_engines" to
            ::commandLinePreambleSuppliesDirectivesInBothEngines,
        "nested_command_lists_launch_every_task_in_both_engines" to ::nestedCommandListsLaunchEveryTaskInBothEngines,
        "official_engine_bibliography_and_converter_rules" to ::officialEngineBibliographyAndConverterRules,
        "official_lua_directory_and_halt_rules" to ::officialLuaDirectoryAndHaltRules,
        "invalid_directive_parameters_fail_in_both_engines" to ::invalidDirectiveParametersFailInBothEngines,
    )

    private fun directiveFileArraysReplicateExecutionInBothEngines(
        root: MPPPath
    ) {

        val trace = root.resolve("files.trace")
        rule(root, "writeToFile(${quoted(trace.toString())}, reference.fileName + '|', true); return true;")
        luaRule(root, "write_to_file(${quoted(trace.toString())}, " +
            "ctx.reference.file_name .. '|', true); return true")
        root.resolve("first.tex").writeText("")
        root.resolve("second.tex").writeText("")
        root.resolve("run.tex").writeText("% arara: probe: { files: [first.tex, second.tex] }\n")
        for (engine in listOf("lua", "yaml")) {
            trace.writeText("")
            val output = execute(root, 0, listOf("--preferred-engine", engine))
            check(trace.readText() == "first.tex|second.tex|") {
                "$engine did not preserve both file references: ${trace.readText()}\n$output"
            }
        }
    }

    private fun missingRulesNameTheSelectedEngineFormat(
        root: MPPPath
    ) {

        root.resolve("run.tex").writeText("% arara: notarulequack\n")
        for (engine in listOf("lua", "yaml")) {
            contains(execute(root, APPLICATION_ERROR, listOf("--preferred-engine", engine)),
                "notarulequack.$engine")
        }
    }

    private fun ruleDiscoveryListsWithoutLoadingAndInspectsTheEffectiveRule(
        root: MPPPath
    ) {

        rule(root, "throw new RuntimeException('must not execute');")
        luaRule(root, "error('must not execute')")
        root.resolve("broken.proj.lua").writeText("error('must not execute')")
        fun inspect(expected: Int, vararg flags: String): kotlinx.serialization.json.JsonObject {
            val result = run(launcher + listOf("-Duser.home=$root", "-jar", jar.toString(),
                "-d", root.toString(), "rules", "--json") + flags, root)
            check(result.status == expected) { result.output }
            return Json.parseToJsonElement(result.output).jsonObject
        }
        val listed = inspect(0, "--filter", "PROBE").getValue("rules").jsonArray
        check(listed.single().jsonObject.getValue("format").jsonPrimitive.content == "lua")
        check(inspect(0, "--all", "--filter", "probe").getValue("rules").jsonArray.size == 2)
        check(inspect(0, "--filter", "absent").getValue("rules").jsonArray.isEmpty())
        check(inspect(0, "--info", "probe").getValue("diagnostics").jsonArray.isEmpty())
        root.resolve("probe.lua").writeText("return {}")
        check(inspect(0, "--filter", "probe").getValue("rules").jsonArray.size == 1)
        val failed = inspect(2, "--info", "probe")
        check(failed.getValue("diagnostics").jsonArray.single().jsonObject.getValue("path")
            .jsonPrimitive.content == root.resolve("probe.lua").toString())
        check(inspect(0, "-e", "yaml", "--info", "probe").getValue("diagnostics").jsonArray.isEmpty())
    }

    private fun wholeFileEnablesBodyDirectivesInBothEngines(
        root: MPPPath
    ) {

        val trace = root.resolve("body-ran.txt")
        rule(root, "writeToFile(${quoted(trace.toString())}, 'body', false); return true;")
        luaRule(root, "write_to_file(${quoted(trace.toString())}, 'body', false); return true")
        root.resolve("run.tex").writeText("\\documentclass{article}\n% arara: probe\n")
        for (engine in listOf("lua", "yaml")) {
            execute(root, APPLICATION_ERROR, listOf("--preferred-engine", engine))
            check(!trace.exists) { "Header-only parsing executed a body directive" }
            execute(root, 0, listOf("--preferred-engine", engine, "--whole-file"))
            check(trace.readText() == "body")
            Files.delete(trace.toJVMPath())
        }
    }

    private fun commandLineLoopLimitCapsBothEngines(
        root: MPPPath
    ) {

        val trace = root.resolve("loop.txt")
        rule(root, "writeToFile(${quoted(trace.toString())}, 'x', true); return true;")
        luaRule(root, "write_to_file(${quoted(trace.toString())}, 'x', true); return true")
        root.resolve("run.tex").writeText("% arara: probe while true\n")
        for (engine in listOf("lua", "yaml")) {
            trace.writeText("")
            execute(root, 0, listOf("--preferred-engine", engine, "--max-loops", "2"))
            check(trace.readText() == "xx") { "$engine ignored the command line loop limit" }
        }
    }

    private fun commandLinePreambleSuppliesDirectivesInBothEngines(
        root: MPPPath
    ) {

        rule(root, "return true;")
        luaRule(root, "return true")
        root.resolve("run.tex").writeText("\\documentclass{article}\n")
        for (format in listOf("lua", "yaml")) {
            root.resolve("arararc.$format").writeText(if (format == "yaml") {
                "!config\npaths: ['.']\npreambles: { build: '% arara: probe' }\n"
            } else "return { paths = { cwd }, preamble = { definitions = { build = {'% arara: probe'} } } }")
            for (engine in listOf("lua", "yaml")) {
                val output = execute(root, 0, listOf("--preferred-engine", engine, "--preamble", "build"))
                contains(output, "SUCCESS")
            }
            Files.delete(root.resolve("arararc.$format").toJVMPath())
        }
    }

    private fun nestedCommandListsLaunchEveryTaskInBothEngines(
        root: MPPPath
    ) {

        val yaml = native("0").removePrefix("return ").removeSuffix(";")
        val lua = luaNative("0").removePrefix("return ")
        rule(root, "return [$yaml, [$yaml]];")
        luaRule(root, "return {$lua, {$lua}}")
        for (engine in listOf("lua", "yaml")) {
            val output = execute(root, 0, listOf("--preferred-engine", engine, "--verbose"))
            for (marker in listOf("SMOKE_STDOUT", "SMOKE_STDERR")) {
                check(Regex(marker).findAll(output).count() == 2) { "$engine did not launch both tasks\n$output" }
            }
        }
    }

    private fun officialEngineBibliographyAndConverterRules(
        root: MPPPath
    ) {

        officialRules(root)
        root.resolve("run.tex").writeText(
            "% arara: pdflatex: { interaction: nonstopmode, synctex: yes }\n" +
                "% arara: bibtex\n% arara: dvips\n"
        )
        listOf("lua", "yaml").forEach { engine ->
            val output = execute(root, 0, listOf("--preferred-engine", engine, "--dry-run"))
            contains(output, "--interaction=nonstopmode")
            contains(output, "--synctex=1")
            contains(output, "bibtex")
            contains(output, "dvips")
        }
    }

    private fun officialLuaDirectoryAndHaltRules(
        root: MPPPath
    ) {

        officialRules(root)
        root.resolve("run.tex").writeText(
            "% arara: mkdir: { target: 'created directory' }\n" +
                "% arara: rmdir: { target: 'created directory' }\n" +
                "% arara: halt\n% arara: missingrule\n"
        )
        execute(root, 0, listOf("--preferred-engine", "lua"))
        check(!root.resolve("created directory").exists)
    }

    private fun invalidDirectiveParametersFailInBothEngines(
        root: MPPPath
    ) {

        rule(root, "return true;")
        luaRule(root, "return true")
        listOf("lua", "yaml").forEach { engine ->
            listOf("{ value: null }", "[one, two]", "{ value: [unterminated }").forEach { value ->
                root.resolve("run.tex").writeText("% arara: probe: $value\n")
                execute(root, APPLICATION_ERROR, listOf("--preferred-engine", engine))
            }
        }
    }

}

private class LauncherChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "packaged_native_terminal_provider_and_access" to { packagedNativeTerminalProviderAndAccess() },
        "custom_class_loading_without_kotlin_reflection" to ::customClassLoadingWithoutKotlinReflection,
        "native_access_is_limited_to_jna" to ::nativeAccessIsLimitedToJna,
        "packaged_version_and_completion" to ::packagedVersionAndCompletion,
        "invalid_cli_bounds_and_choices_fail_before_execution" to ::invalidCliBoundsAndChoicesFailBeforeExecution,
        "failed_launch_is_an_application_error" to ::failedLaunchIsAnApplicationError,
        "timeout_is_an_application_error" to ::timeoutIsAnApplicationError,
    )

    private fun packagedNativeTerminalProviderAndAccess() {

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
            check("nativeimage" !in registration) { "Unused native-image provider is registered" }
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
    }

    private fun customClassLoadingWithoutKotlinReflection(
        root: MPPPath
    ) {

        JarFile(jar.toJVMFile()).use { archive ->
            check(archive.getJarEntry("kotlin/reflect/full/KClasses.class") == null) {
                "The application unexpectedly bundles kotlin-reflect"
            }
        }
        val location = quoted(fixture.toString())
        luaRule(root, "assert(load_class($location, 'SmokeProcess').status == 'SUCCESS'); " +
            "assert(load_object($location, 'SmokeProcess').status == 'SUCCESS'); return true")
        execute(root, 0, listOf("--preferred-engine", "lua"))
        rule(root, "loaded = loadObject($location, 'SmokeProcess'); " +
            "if (loaded.first.name() != 'SUCCESS' || loaded.second.answer() != 42) " +
            "{ throw new RuntimeException('Custom class loading failed'); } return true;")
        execute(root, 0)
    }

    private fun nativeAccessIsLimitedToJna(
        root: MPPPath
    ) {

        val version = run(listOf(java.toString(), "-version"), root)
        val feature = Regex("version \"(\\d+)").find(version.output)?.groupValues?.get(1)?.toInt()
            ?: error("Cannot determine Java version: ${version.output}")
        val restriction =
            if (feature >= NATIVE_ACCESS_API_VERSION) listOf("--illegal-native-access=deny") else emptyList()
        val result = run(launcher + restriction +
            listOf("-cp", fixture.toString(), "SmokeProcess", "native-access"), root)
        check(result.status == 0) { result.output }
        contains(result.output, "NATIVE_ACCESS_ISOLATED")
    }

    private fun packagedVersionAndCompletion(
        root: MPPPath
    ) {

        val command = launcher + listOf("-jar", jar.toString())
        val version = run(command + "--version", root)
        check(version.status == 0) { version.output }
        contains(version.output, "arara")
        contains(version.output, "Island of TeX")
        val completion = run(command + listOf("--generate-completion", "bash"), root)
        check(completion.status == 0) { completion.output }
        contains(completion.output, "--preferred-engine")
        check("--engine" !in completion.output)
        contains(completion.output, "--log-format")
    }

    private fun invalidCliBoundsAndChoicesFailBeforeExecution(
        root: MPPPath
    ) {

        rule(root, "writeToFile('must-not-run.txt', 'ran'); return true;")
        val invalid = listOf(
            listOf("--max-loops", "0"),
            listOf("--terminal-width", "19"),
            listOf("--timeout", "0"),
            listOf("--preferred-engine", "unknown"),
            listOf("--console", "unknown"),
            listOf("--log-format", "unknown")
        )
        for (flags in invalid) {
            val output = execute(root, 1, flags)
            contains(output, flags.first())
            contains(output, "invalid value")
            check(!root.resolve("must-not-run.txt").exists) { "Invalid CLI arguments executed a rule" }
        }
    }

    private fun failedLaunchIsAnApplicationError(
        root: MPPPath
    ) {

        rule(root, "return getCommand([${quoted(root.resolve("missing-executable").toString())}]);")
        execute(root, APPLICATION_ERROR)
    }

    private fun timeoutIsAnApplicationError(
        root: MPPPath
    ) {

        rule(root, native("sleep"))
        execute(root, APPLICATION_ERROR, listOf("--timeout", "100"))
    }

}

private class CommandChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "native_exit_and_both_output_streams" to ::nativeExitAndBothOutputStreams,
        "successful_native_command" to ::successfulNativeCommand,
        "error_halt_keeps_its_status_and_stops_the_file" to ::errorHaltKeepsItsStatusAndStopsTheFile,
        "successful_halt_stops_the_file" to ::successfulHaltStopsTheFile,
        "localized_lua_rule_and_interpolation_errors" to ::localizedLuaRuleAndInterpolationErrors,
        "json_events_keep_native_and_application_outcomes_distinct" to
            ::jsonEventsKeepNativeAndApplicationOutcomesDistinct,
        "json_events_describe_process_failures_without_fake_exit_codes" to
            ::jsonEventsDescribeProcessFailuresWithoutFakeExitCodes,
        "ordinary_lua_is_not_executed_as_a_specification" to ::ordinaryLuaIsNotExecutedAsASpecification,
    )

    private fun nativeExitAndBothOutputStreams(
        root: MPPPath
    ) {

        rule(root, native(FAILURE_STATUS.toString()))
        val output = execute(root, FAILURE_STATUS, listOf("--verbose"))
        contains(output, "SMOKE_STDOUT")
        contains(output, "SMOKE_STDERR")
        contains(output, "SMOKE_CWD=$root")
    }

    private fun successfulNativeCommand(
        root: MPPPath
    ) {

        rule(root, native("0"))
        execute(root, 0)
    }

    private fun errorHaltKeepsItsStatusAndStopsTheFile(
        root: MPPPath
    ) {

        rule(root, "halt($FAILURE_STATUS);")
        root.resolve("run.tex").writeText("% arara: probe\n% arara: missingrule\n")
        execute(root, FAILURE_STATUS)
    }

    private fun successfulHaltStopsTheFile(
        root: MPPPath
    ) {

        rule(root, "halt(0);")
        root.resolve("run.tex").writeText("% arara: probe\n% arara: missingrule\n")
        execute(root, 0)
    }

    private fun localizedLuaRuleAndInterpolationErrors(
        root: MPPPath
    ) {

        root.resolve("probe.lua").writeText(
            "return { identifier = 'probe', name = 'Probe', commands = { { command = true } } }\n"
        )
        val flags = listOf("--preferred-engine", "lua", "--language", "de")
        contains(execute(root, APPLICATION_ERROR, flags), "commands[1].command muss eine Funktion sein.")
        luaRule(root, "return true")
        root.resolve("run.tex").writeText("% arara: probe: { options: '{{ 1' }\n")
        val output = execute(root, APPLICATION_ERROR, flags)
        contains(output, "Interpolations-Tag nicht abgeschlossen.")
        check("MVEL" !in output) { "Lua interpolation reported as MVEL: $output" }
    }

    private fun jsonEventsKeepNativeAndApplicationOutcomesDistinct(
        root: MPPPath
    ) {

        rule(root, native(FAILURE_STATUS.toString()))
        luaRule(root, luaNative(FAILURE_STATUS.toString()), "code == $FAILURE_STATUS")
        val runIds = mutableSetOf<String>()
        for (engine in listOf("lua", "yaml")) {
            val expected = if (engine == "yaml") FAILURE_STATUS else 0
            execute(root, expected, listOf("--preferred-engine", engine, "--log-format", "json",
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
    }

    private fun jsonEventsDescribeProcessFailuresWithoutFakeExitCodes(
        root: MPPPath
    ) {

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
    }

    private fun ordinaryLuaIsNotExecutedAsASpecification(
        root: MPPPath
    ) {

        rule(root, "return true;")
        root.resolve("arararc.yaml").writeText(
            "!config\npaths: ['.']\nfiletypes:\n  - extension: lua\n    pattern: '^-- +'\n"
        )
        root.resolve("ordinary.lua").writeText("-- arara: probe\nerror('must not execute')\n")
        execute(root, 0, reference = "ordinary.lua")
    }

}

private class LuaBoundaryChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "lua_path_methods_in_rules_and_conditionals" to ::luaPathMethodsInRulesAndConditionals,
        "lua_safe_mode_leaves_options_literal" to ::luaSafeModeLeavesOptionsLiteral,
        "lua_dry_run_does_not_launch" to ::luaDryRunDoesNotLaunch,
    )

    private fun luaPathMethodsInRulesAndConditionals(
        root: MPPPath
    ) {

        luaRule(root, """
            local source = to_file(ctx.reference.path)
            assert(source == current_file())
            assert(source == to_file(get_original_reference()))
            local directory = source.parent:resolve('path output')
            directory:create_directories()
            local output = directory:resolve('result.txt')
            output:write_text('first')
            output:write_text(' second', true)
            assert(output:read_text() == 'first second')
            assert(directory:list_files()[1] == output)
            return named_command('Path command', get_command(to_file(${quoted(java.toString())}), '-version'))
        """.trimIndent())
        root.resolve("run.tex").writeText(
            "% arara: probe if current_file():read_text() ~= ''\n"
        )
        contains(execute(root, 0, listOf("--preferred-engine", "lua")), "Path command")
        check(root.resolve("path output/result.txt").readText() == "first second")
    }

    private fun luaSafeModeLeavesOptionsLiteral(
        root: MPPPath
    ) {

        root.resolve("probe.lua").writeText("""
            return {identifier='probe', name='Lua probe', commands={{command=function(ctx)
              return ctx.options[1] == '{{error("must not execute")}}'
            end}}, arguments={{identifier='options', flag=function(ctx, value) return value end}}}
        """.trimIndent())
        root.resolve("run.tex").writeText(
            "% arara: probe: { options: ['{{error(\"must not execute\")}}'] }\n"
        )
        execute(root, 0, listOf("--preferred-engine", "lua", "--safe-run"))
    }

    private fun luaDryRunDoesNotLaunch(
        root: MPPPath
    ) {

        luaRule(root, "return get_command('arara-nonexistent-command')")
        execute(root, 0, listOf("--preferred-engine", "lua", "--dry-run"))
    }

}

private class DialogChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "lua_terminal_input_and_safe_class_loading" to ::luaTerminalInputAndSafeClassLoading,
        "terminal_input_uses_redirected_stdin" to ::terminalInputUsesRedirectedStdin,
        "terminal_eof_is_an_application_error" to ::terminalEofIsAnApplicationError,
        "safe_mode_blocks_class_loading" to ::safeModeBlocksClassLoading,
    )

    private fun luaTerminalInputAndSafeClassLoading(
        root: MPPPath
    ) {

        luaRule(root, "return show_input(0, 'Lua input', 'Type the answer') == 'answer'")
        contains(execute(root, 0, listOf("--preferred-engine", "lua"), streams =
            SmokeStreams(input = "answer\n")), "Lua input")
        luaRule(root, "load_class('missing.jar', 'Missing'); return true")
        execute(root, APPLICATION_ERROR, listOf("--preferred-engine", "lua", "--safe-run"))
    }

    private fun terminalInputUsesRedirectedStdin(
        root: MPPPath
    ) {

        rule(root, "return showInput(0, 'Smoke input', 'Type the answer') == 'answer';")
        contains(execute(root, 0, streams = SmokeStreams(input = "answer\n")), "Smoke input")
    }

    private fun terminalEofIsAnApplicationError(
        root: MPPPath
    ) {

        rule(root, "return showInput(0, 'Smoke input', 'Type the answer') == 'answer';")
        contains(execute(root, APPLICATION_ERROR), "terminal input ended")
    }

    private fun safeModeBlocksClassLoading(
        root: MPPPath
    ) {

        rule(root, "loadClass('missing.jar', 'Missing'); return true;")
        execute(root, APPLICATION_ERROR, listOf("--safe-run"))
    }

}
