// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath

internal fun SmokeContext.presentationCases(): Map<String, (MPPPath) -> Unit> =
    PresentationChecks(this).cases

private class PresentationChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "french_configuration_cli_and_console_presentations" to ::frenchConfigurationCliAndConsolePresentations,
        "configured_console_width_and_cli_override" to ::configuredConsoleWidthAndCliOverride,
        "console_greeting_totals_and_styled_command_fields" to ::consoleGreetingTotalsAndStyledCommandFields,
        "styled_presentation_preserves_engines_and_failure_output" to
            ::styledPresentationPreservesEnginesAndFailureOutput,
        "redirected_terminal_environment_preserves_stream_encoding" to
            ::redirectedTerminalEnvironmentPreservesStreamEncoding,
        "redirected_long_running_tasks_never_animate_in_either_configuration_format" to
            ::redirectedLongRunningTasksNeverAnimateInEitherConfigurationFormat,
        "styled_verbose_streams_once_and_dry_run_does_not_launch" to ::styledVerboseStreamsOnceAndDryRunDoesNotLaunch,
        "styled_summary_names_unprocessed_files_after_an_exception" to
            ::styledSummaryNamesUnprocessedFilesAfterAnException,
        "styled_summary_keeps_projects_across_presentation_changes" to
            ::styledSummaryKeepsProjectsAcrossPresentationChanges,
    )

    private fun frenchConfigurationCliAndConsolePresentations(
        root: MPPPath
    ) {

        checkFrenchLocalization(root, launcher, jar)
    }

    private fun configuredConsoleWidthAndCliOverride(
        root: MPPPath
    ) {

        rule(root, "return true;")
        for (mode in listOf("plain", "styled")) {
            for (format in listOf("lua", "yaml")) {
                val config = root.resolve("arararc.$format")
                config.writeText(if (format == "lua") "return { paths={cwd}, terminal_width=40 }"
                else "!config\npaths: ['.']\nterminalWidth: 40")
                for ((flags, width) in listOf(emptyList<String>() to CONFIGURED_CONSOLE_WIDTH,
                    listOf("--terminal-width", CLI_CONSOLE_WIDTH.toString()) to CLI_CONSOLE_WIDTH)) {
                    val output = execute(root, 0, listOf("--console", mode, "--dry-run") + flags)
                    check(output.lineSequence().any { line -> line.length == width && line.all { it == '-' } }) {
                        "Missing $width-column separator in $mode output: $output"
                    }
                }
                Files.delete(config.toJVMPath())
            }
        }
    }

    private fun consoleGreetingTotalsAndStyledCommandFields(
        root: MPPPath
    ) {

        rule(root, native("0"))
        luaRule(root, luaNative("0"))
        for (engine in listOf("lua", "yaml")) {
            for (mode in listOf("plain", "styled")) {
                val flags = listOf("--preferred-engine", engine, "--console", mode)
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
    }

    private fun styledPresentationPreservesEnginesAndFailureOutput(
        root: MPPPath
    ) {

        rule(root, native("7"))
        val environment = mapOf(
            "CI" to "true", "GITLAB_CI" to "true", "TERM" to "xterm", "FORCE_COLOR" to "3", "NO_COLOR" to null
        )
        val output =
            execute(root, FAILURE_STATUS, listOf("--console", "styled"), streams =
                SmokeStreams(environment = environment))
        contains(output, "FAILURE")
        contains(output, "SMOKE_STDERR")
        check("\u001b" !in output) { "Piped styled output contains ANSI escapes" }
        luaRule(root, "return true")
        val lua = execute(root, 0, listOf("--preferred-engine", "lua", "--console", "styled"),
            streams = SmokeStreams(environment = environment))
        check("\u001b" !in lua) { "Piped Lua styled output contains ANSI escapes" }
        contains(lua, "SUCCESS")
        contains(lua, "run.tex")
    }

    private fun redirectedTerminalEnvironmentPreservesStreamEncoding(
        root: MPPPath
    ) {

        rule(root, "return true;")
        val reference = "ação-λ.tex"
        root.resolve(reference).writeText("% arara: probe\n")
        for ((encoding, expected) in listOf("UTF-8" to reference, "US-ASCII" to "a??o-?.tex")) {
            val result = run(launcher + listOf("-Dstdout.encoding=$encoding", "-Dstderr.encoding=$encoding",
                "-Duser.home=$root", "-jar", jar.toString(), "-L", "en",
                "-d", root.toString(), "--preferred-engine", "yaml", "--dry-run", reference), root,
                environment = mapOf("TERM" to "xterm", "MSYSTEM" to "MINGW64"))
            check(result.status == 0) { result.output }
            contains(result.output, expected)
        }
    }

    private fun redirectedLongRunningTasksNeverAnimateInEitherConfigurationFormat(
        root: MPPPath
    ) {

        rule(root, native("sleep"))
        for (format in listOf("lua", "yaml")) {
            val config = root.resolve("arararc.$format")
            for (enabled in listOf(false, true)) {
                config.writeText(if (format == "yaml")
                    "!config\npaths: ['.']\nconsole: styled\nanimations: $enabled\n"
                else "return { paths = { cwd }, console = 'styled', animations = $enabled }")
                val output =
                    execute(root, APPLICATION_ERROR, listOf("--timeout", "1500"), streams =
                        SmokeStreams(environment = mapOf(
                            "TERM" to "xterm", "FORCE_COLOR" to "3", "CI" to "true"
                        )))
                check("\u001b" !in output) { "Redirected task emitted terminal controls" }
                check("⠋" !in output) { "Redirected task emitted progress frames" }
                contains(output, "FAILURE")
            }
            Files.delete(config.toJVMPath())
        }
    }

    private fun styledVerboseStreamsOnceAndDryRunDoesNotLaunch(
        root: MPPPath
    ) {

        rule(root, native("7"))
        val verbose = execute(root, FAILURE_STATUS, listOf("--console", "styled", "--verbose"))
        check(Regex("SMOKE_STDERR").findAll(verbose).count() == 1) { "Verbose output was repeated" }
        val dry = execute(root, 0, listOf("--console", "styled", "--dry-run"))
        contains(dry.filterNot(Char::isWhitespace), "SmokeProcess")
        contains(dry, "DRY RUN")
        check("SUCCESS" !in dry) { "Styled dry run claimed execution success" }
        check("SMOKE_STDOUT" !in dry) { "Styled dry run launched a command" }
    }

    private fun styledSummaryNamesUnprocessedFilesAfterAnException(
        root: MPPPath
    ) {

        root.resolve("first.tex").writeText("% arara: missingrule\n")
        root.resolve("second.tex").writeText("% arara: probe\n")
        val output = execute(root, APPLICATION_ERROR, listOf("--console", "styled", "first.tex"), "second.tex")
        val summary = output.substringAfter("Execution summary")
        contains(summary, "first.tex")
        contains(summary, "FAILURE (2)")
        contains(summary, "second.tex")
        contains(summary, "Not processed")
    }

    private fun styledSummaryKeepsProjectsAcrossPresentationChanges(
        root: MPPPath
    ) {

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
    }

}
