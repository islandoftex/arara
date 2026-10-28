// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import org.islandoftex.arara.api.files.MPPPath

internal const val FAILURE_STATUS = 7
internal const val APPLICATION_ERROR = 2
internal const val CONFIGURED_CONSOLE_WIDTH = 40
internal const val CLI_CONSOLE_WIDTH = 50
internal const val NATIVE_ACCESS_API_VERSION = 24

internal fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\")
    .replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\""

/** Files and launcher operations shared by independent packaged-application scenarios. */
internal interface SmokeFixture {
    val jar: MPPPath
    val java: MPPPath
    val fixture: MPPPath
    val jna: MPPPath
    val launcher: List<String>

    fun rule(root: MPPPath, expression: String)
    fun officialRules(root: MPPPath)
    fun luaRule(root: MPPPath, body: String, exit: String = "code == 0")
    fun luaNative(mode: String): String
    fun native(mode: String): String
    fun execute(root: MPPPath, expected: Int, flags: List<String> = emptyList(),
        reference: String = "run.tex", streams: SmokeStreams = SmokeStreams()): String
    fun contains(output: String, text: String)
    fun processTree(root: MPPPath): ProcessTreeCheck
}

internal class SmokeContext(override val jar: MPPPath, override val java: MPPPath,
    override val fixture: MPPPath, override val jna: MPPPath) : SmokeFixture {
    override val launcher = listOf(java.toString(), "--module-path", jna.toString(),
        "--add-modules=com.sun.jna", "--enable-native-access=com.sun.jna")

    override fun rule(root: MPPPath, expression: String) {
        root.resolve("probe.yaml").writeText(
            "!config\nidentifier: probe\nname: Smoke probe\ncommands:\n  - command: " +
                quoted("@{ $expression }") + "\narguments: []\n"
        )
    }

    override fun officialRules(root: MPPPath) {
        listOf("pdflatex", "bibtex", "dvips", "mkdir", "rmdir", "halt").forEach { name ->
            listOf("lua", "yaml").forEach { extension ->
                val fileName = "arara-rule-$name.$extension"
                val text = checkNotNull(javaClass.getResourceAsStream("/rules/$fileName")) {
                    "Missing smoke resource $fileName"
                }.bufferedReader(Charsets.UTF_8).use { it.readText() }
                root.resolve(fileName).writeText(text)
            }
        }
    }

    override fun luaRule(root: MPPPath, body: String, exit: String) {
        root.resolve("probe.lua").writeText(
            "return {identifier='probe', name='Lua probe', commands={{command=function(ctx) " +
                body + " end, exit=function(code) return " + exit + " end}}}\n"
        )
    }

    override fun luaNative(mode: String): String =
        "return get_command(" + listOf(java.toString(), "-cp", fixture.toString(), "SmokeProcess", mode)
            .joinToString(", ") { quoted(it) } + ")"

    override fun native(mode: String): String = listOf(java.toString(), "-cp", fixture.toString(), "SmokeProcess", mode)
        .joinToString(", ", "return getCommand([", "]);") { quoted(it) }

    override fun execute(root: MPPPath, expected: Int, flags: List<String>,
        reference: String, streams: SmokeStreams): String {
        val result = run((launcher + listOf("-Duser.home=$root", "-jar", jar.toString(),
            "-L", "en", "-d", root.toString())) +
            (if ("--preferred-engine" in flags || "-e" in flags) emptyList()
            else listOf("--preferred-engine", "yaml")) +
            flags + reference, root, streams.input, streams.environment)
        check(result.status == expected) { "Expected exit $expected, got ${result.status}\n${result.output}" }
        return result.output
    }

    override fun contains(output: String, text: String) = check(text in output) { "Missing $text\n$output" }

    override fun processTree(root: MPPPath) = ProcessTreeCheck(root,
        launcher + listOf("-Duser.home=$root", "-jar", jar.toString(), "-L", "en", "--preferred-engine", "lua"),
        listOf(java.toString(), "-cp", fixture.toString(), "SmokeProcess"))

    val cases: Map<String, (MPPPath) -> Unit> = run {
        val grouped =
            processLifecycleCases() + presentationCases() + executionCases() + configurationCases() + loggingCases()
        listOf(
            "watch_queues_changes_and_rebuilds_only_dependents",
            "watch_recovers_failures_and_retains_failed_cycle_logs",
            "watch_pauses_invalid_configuration_and_reloads",
            "watch_active_build_keeps_original_configuration",
            "watch_first_interrupt_finishes_active_build",
            "watch_second_interrupt_stops_active_tools",
            "active_descendants_stop_when_arara_is_terminated",
            "active_descendants_stop_when_a_command_times_out",
            "normal_completion_preserves_detached_processes",
            "french_configuration_cli_and_console_presentations",
            "lua_default_and_legacy_compatibility_warnings",
            "configured_console_width_and_cli_override",
            "console_greeting_totals_and_styled_command_fields",
            "directive_file_arrays_replicate_execution_in_both_engines",
            "missing_rules_name_the_selected_engine_format",
            "lua_configuration_precedes_yaml_independently_of_rule_engine",
            "rule_discovery_lists_without_loading_and_inspects_the_effective_rule",
            "custom_class_loading_without_kotlin_reflection",
            "packaged_native_terminal_provider_and_access",
            "native_access_is_limited_to_jna",
            "packaged_version_and_completion",
            "help_and_empty_invocation_ignore_invalid_configuration",
            "invalid_cli_bounds_and_choices_fail_before_execution",
            "whole_file_enables_body_directives_in_both_engines",
            "command_line_loop_limit_caps_both_engines",
            "configured_logname_receives_messages_in_both_formats",
            "command_line_preamble_supplies_directives_in_both_engines",
            "nested_command_lists_launch_every_task_in_both_engines",
            "call_properties_override_property_files_in_both_engines",
            "lua_configuration_defines_custom_file_types",
            "official_engine_bibliography_and_converter_rules",
            "official_lua_directory_and_halt_rules",
            "dialog_choices_in_both_engines_and_presentations",
            "lua_terminal_input_and_safe_class_loading",
            "project_build_directory_staging_native_cwd_and_clean",
            "lua_path_methods_in_rules_and_conditionals",
            "lua_native_exit_and_output",
            "lua_custom_exit_accepts_nonzero",
            "lua_custom_exit_rejects_zero",
            "lua_halt_stops_the_file",
            "lua_conditional_and_scalar_interpolation",
            "lua_safe_mode_leaves_options_literal",
            "lua_dry_run_does_not_launch",
            "lua_preference_falls_back_to_yaml_with_a_warning",
            "invalid_directive_parameters_fail_in_both_engines",
            "legacy_engine_remains_available_explicitly",
            "native_exit_and_both_output_streams",
            "verbosity_flags_override_yaml_and_lua_configuration",
            "successful_native_command",
            "failed_launch_is_an_application_error",
            "timeout_is_an_application_error",
            "error_halt_keeps_its_status_and_stops_the_file",
            "successful_halt_stops_the_file",
            "terminal_input_uses_redirected_stdin",
            "terminal_eof_is_an_application_error",
            "safe_mode_blocks_class_loading",
            "lua_configuration_supports_both_rule_engines",
            "invalid_lua_configuration_is_an_application_error",
            "localized_configuration_and_project_errors",
            "localized_lua_rule_and_interpolation_errors",
            "styled_presentation_preserves_engines_and_failure_output",
            "redirected_terminal_environment_preserves_stream_encoding",
            "redirected_long_running_tasks_never_animate_in_either_configuration_format",
            "styled_verbose_streams_once_and_dry_run_does_not_launch",
            "styled_summary_names_unprocessed_files_after_an_exception",
            "styled_summary_keeps_projects_across_presentation_changes",
            "plain_and_styled_presentations_keep_identical_diagnostic_logs",
            "diagnostic_log_records_exact_rule_command_and_application_outcomes",
            "english_log_labels_preserve_the_localized_console",
            "session_mutations_from_both_engines_are_traced_without_values",
            "tool_diagnostics_are_filtered_but_styled_summaries_keep_warnings",
            "log_retention_and_levels_follow_the_invocation_outcome",
            "json_logging_is_available_in_the_packaged_jar",
            "json_events_keep_native_and_application_outcomes_distinct",
            "json_events_describe_process_failures_without_fake_exit_codes",
            "database_changes_persist_between_processes",
            "configured_database_rejects_malformed_yaml_without_overwriting",
            "ordinary_lua_is_not_executed_as_a_specification",
            "malformed_project_is_an_application_error",
            "mixed_projects_and_loose_inputs_run_with_separate_configuration",
            "verbosity_overrides_apply_to_each_project_without_leaking_configuration",
            "shared_project_input_is_rejected_before_execution",
            "project_and_lua_configuration_share_script_context",
            "project_loads_configuration_from_its_working_directory"
        ).associateWith { grouped.getValue(it) }
    }
}

internal fun initialize(root: MPPPath) {
    root.resolve("arararc.yaml").writeText("!config\npaths: ['.']\n")
    root.resolve("run.tex").writeText("% arara: probe\n")
}

private fun compileFixture(root: MPPPath, javac: MPPPath) {
    compileWatchFixture(root, javac)
    val source = root.resolve("SmokeProcess.java")
    source.writeText("""
        public class SmokeProcess {
            public int answer() { return 42; }
            public static void main(String[] args) throws Exception {
                if (args[0].startsWith("tree-")) {
                    java.nio.file.Path root = java.nio.file.Path.of(args[1]);
                    java.nio.file.Files.writeString(root.resolve(args[0] + ".pid"),
                        Long.toString(ProcessHandle.current().pid()));
                    if (args[0].equals("tree-leaf")) {
                        java.nio.file.Files.writeString(root.resolve("tree.ready"), "ready");
                        Thread.sleep(60000);
                    } else {
                        String next = args[0].equals("tree-child") ? "tree-leaf" : "tree-child";
                        Process child = new ProcessBuilder(ProcessHandle.current().info().command().orElseThrow(),
                            "-cp", System.getProperty("java.class.path"), "SmokeProcess", next, args[1]).start();
                        if (!args[0].equals("tree-detach")) child.waitFor();
                    }
                    return;
                }
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
                if (args[0].equals("diagnostics")) {
                    System.out.println("INFO: native-info");
                    System.out.println("TRACE: native-trace");
                    System.err.println("WARNING: native-warning");
                    System.err.println("ERROR: native-error");
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
            SmokeContext(jar, java, fixture, jna).cases.forEach { (name, test) ->
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

internal data class SmokeStreams(val input: String = "", val environment: Map<String, String?> = emptyMap())
