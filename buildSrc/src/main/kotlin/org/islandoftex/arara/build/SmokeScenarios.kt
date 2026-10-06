// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.islandoftex.kobats.bats
import org.islandoftex.kobats.builder.TestCaseBuilder

// the scenarios read three variables from the environment: the
// scratch tree root, the Java binary of the leg under run, and the
// runnable jar. Each scenario renders as one runner file and moves
// to its own working directory in the generated setup, so the rule
// and configuration lookups resolve as they do for a user. The
// fixture and authored fields drive the tree provisioning, so a new
// scenario is a row in the table and nothing else.

/** Runner files that require the real TeX toolchain on the path. */
internal val texScenarioFiles: Set<String> =
        setOf("tex-db.bats", "tex-yaml.bats", "tex-lua.bats")

/** Renders every smoke scenario, keyed by the runner file name. */
internal fun smokeScenarios(): Map<String, String> = scenarios.associate { it.name to render(it) }

/**
 * One end-to-end scenario of the codified suite: the invocation,
 * the expectations and the provisioning of one scratch directory.
 */
internal data class Scenario(
    /** The runner file the scenario renders as, keyed by this name. */
    val name: String,
    /** The scratch directory the scenario runs in. */
    val dir: String,
    /** The one-line description the runner file documents itself with. */
    val description: String,
    /** The one-line statement of what the scenario proves. */
    val check: String,
    /** The command line arguments of the arara invocation. */
    val arguments: List<String>,
    /** The strings the run output has to contain. */
    val expected: List<String>,
    /** The strings the run output must not contain; defaults to none. */
    val forbidden: List<String> = emptyList(),
    /** The strings the log file has to contain; defaults to none. */
    val logChecks: List<String> = emptyList(),
    /** The process exit code the scenario expects; defaults to success. */
    val exitCode: Int = 0,
    /** An extra shell step the setup runs before the invocation. */
    val extraSetup: String = "",
    /** Whether the scenario runs the invocation twice; defaults to once. */
    val doubleRun: Boolean = false,
    /** The string the second run must not contain; checked on the double runs alone. */
    val secondRunForbidden: String = "",
    /** Whether the scenario renders as the log parity pipeline comparing two runs. */
    val logParity: Boolean = false,
    /** The execution-tests fixture directory the tree provisioning copies from. */
    val fixture: String = "",
    /** Authored file content the provisioning writes; pairs with [authoredTarget]. */
    val authoredContent: String = "",
    /** The target path the authored content writes to. */
    val authoredTarget: String = "",
)

/** The minimal document the official-rule scenarios typeset. */
private val officialRuleDocument = """
    % arara: pdflatex
    \documentclass{article}
    \begin{document}
    Hello, arara!
    \end{document}
    """.trimIndent() + "\n"

/** The document whose conditional directive exercises the file database. */
private val databaseCheckDocument = """
    % arara: pdflatex if changed('tex')
    \documentclass{article}
    \begin{document}
    Hello, arara!
    \end{document}
    """.trimIndent() + "\n"

/** The document whose options use the deprecated tag syntax of the other engine. */
private val wrongTagDocument = """
    % arara: optechoer: { options: [ 'name: @{getBasename(getOriginalReference())}' ] }
    \documentclass{article}
    \begin{document}
    Lua wrong tag test
    \end{document}
    """.trimIndent() + "\n"

/**
 * The codified suite itself: every row is one scenario the
 * generated runners execute on both legs, and a new scenario is a
 * row in this table and nothing else.
 */
internal val scenarios = listOf(
        Scenario(
                name = "run1.bats", dir = "run1",
                description = "the simple directive through the default engine",
                check = "the directive runs and reports success",
                arguments = listOf("simple-directive.tex"),
                expected = listOf("SUCCESS"),
                forbidden = listOf("kotlin-logging:"),
                fixture = "simple-directive",
        ),
        Scenario(
                name = "run2.bats", dir = "run2",
                description = "the Lua engine happy path",
                check = "the engine works through the local Lua rule",
                arguments = listOf("--engine", "lua", "lua-engine.tex"),
                expected = listOf("LuaEngineWorks"),
                forbidden = listOf("kotlin-logging:"),
                fixture = "lua-engine",
        ),
        Scenario(
                name = "run3.bats", dir = "run3",
                description = "the conditional dialect of the Lua engine",
                check = "the conditionals take the expected branches",
                arguments = listOf("--engine", "lua", "lua-engine-conditionals.tex"),
                expected = listOf("One", "Three", "Four"),
                forbidden = listOf("Two"),
                fixture = "lua-engine-conditionals",
        ),
        Scenario(
                name = "run4.bats", dir = "run4",
                description = "the typed helper boundary of the Lua engine",
                check = "the wrongly typed condition reports the Lua-style error",
                arguments = listOf("--engine", "lua", "badtype.tex"),
                expected = listOf(
                        "bad argument",
                        "#1 to 'exists'",
                        "(file or string expected, got number)",
                ),
                exitCode = 2,
                fixture = "lua-engine-conditionals",
        ),
        Scenario(
                name = "run5.bats", dir = "run5",
                description = "engine options with a staged application rule",
                check = "the rule echoes the basename of the file under run",
                arguments = listOf("--engine", "lua", "lua-engine-options.tex"),
                expected = listOf("name: lua-engine-options"),
                fixture = "lua-engine-options",
        ),
        Scenario(
                name = "tex-db.bats", dir = "tex-db",
                description = "the file database semantics through the Lua engine",
                check = "the first run typesets and the second run skips",
                arguments = listOf("--verbose", "--engine", "lua", "dbcheck.tex"),
                expected = listOf("This is pdfTeX"),
                extraSetup = "rm -f arara.db dbcheck.pdf",
                doubleRun = true,
                secondRunForbidden = "This is pdfTeX",
                authoredContent = databaseCheckDocument,
                authoredTarget = "dbcheck.tex",
        ),
        Scenario(
                name = "tex-yaml.bats", dir = "tex-yaml",
                description = "the official rule through the default dialect",
                check = "the official pdflatex rule typesets the document",
                arguments = listOf("--verbose", "tex-yaml.tex"),
                expected = listOf("This is pdfTeX"),
                authoredContent = officialRuleDocument,
                authoredTarget = "tex-yaml.tex",
        ),
        Scenario(
                name = "tex-lua.bats", dir = "tex-lua",
                description = "the official rule through the Lua dialect",
                check = "the official pdflatex rule typesets the document",
                arguments = listOf("--verbose", "--engine", "lua", "tex-lua.tex"),
                expected = listOf("This is pdfTeX"),
                authoredContent = officialRuleDocument,
                authoredTarget = "tex-lua.tex",
        ),
        Scenario(
                name = "project-spec.bats", dir = "project-spec",
                description = "the project specification and the Lua configuration",
                check = "the specification runs its files through the Lua configuration",
                arguments = listOf("project.proj.lua"),
                expected = listOf("ProjectConfigWorks"),
                fixture = "project-config",
        ),
        Scenario(
                name = "lua-input.bats", dir = "lua-input",
                description = "a plain Lua file as an ordinary input",
                check = "the configured lua file type runs the file with no project detour",
                arguments = listOf("document.lua"),
                expected = listOf("QuackOne"),
                forbidden = listOf("could not be parsed as a project"),
                fixture = "lua-input",
        ),
        Scenario(
                name = "multi-spec.bats", dir = "multi-spec",
                description = "project specifications merged from several files",
                check = "the cross-file dependency orders the merged projects and both run",
                arguments = listOf("second.proj.lua", "first.proj.lua"),
                expected = listOf("AlphaRan", "BetaRan", "Project: alpha", "Project: beta"),
                fixture = "multi-spec",
        ),
        Scenario(
                name = "mixed.bats", dir = "mixed",
                description = "a specification mixed with loose input files",
                check = "the specification's project and the anonymous project both run",
                arguments = listOf("book.proj.lua", "notes.tex"),
                expected = listOf("SpecRan", "LooseRan", "Project: book", "Project: Single files"),
                fixture = "mixed",
        ),
        Scenario(
                name = "safe-run.bats", dir = "safe-run",
                description = "the safe run rejection of unsafe helper calls",
                check = "the unsafe helper call is rejected with the mode violation",
                arguments = listOf("--engine", "lua", "--safe-run", "unsafe.tex"),
                expected = listOf("not allowed to execute"),
                exitCode = 2,
                fixture = "safe-run",
        ),
        Scenario(
                name = "preamble.bats", dir = "preamble",
                description = "the preamble directive flow",
                check = "the configured preamble directive runs",
                arguments = listOf("--preamble", "test", "main.tex"),
                expected = listOf("PreambleWorks"),
                fixture = "preamble",
        ),
        Scenario(
                name = "multi-args.bats", dir = "multi-args",
                description = "multiple files as command line arguments",
                check = "both argument files are processed",
                arguments = listOf("doc1.tex", "doc2.tex"),
                expected = listOf("doc1.tex", "doc2.tex"),
                fixture = "multiple-files",
        ),
        Scenario(
                name = "whole-file.bats", dir = "whole-file",
                description = "whole file directive extraction",
                check = "the directive beyond the header runs",
                arguments = listOf("--whole-file", "body.tex"),
                expected = listOf("SUCCESS"),
                fixture = "whole-file",
        ),
        Scenario(
                name = "parameters.bats", dir = "parameters",
                description = "call parameters reaching the rules",
                check = "the parameter value flows into the rule command",
                arguments = listOf("param.tex"),
                expected = listOf("FromTheParameter"),
                fixture = "parameters",
        ),
        Scenario(
                name = "dry-run.bats", dir = "dry-run",
                description = "the dry run mode through the entry point",
                check = "the dry run marks its entries without executing",
                arguments = listOf("--dry-run", "simple-directive.tex"),
                expected = listOf("[DR]"),
                fixture = "simple-directive",
        ),
        Scenario(
                name = "language.bats", dir = "language",
                description = "the localized labels of the application",
                check = "the success label renders in the chosen language",
                arguments = listOf("--language", "de", "simple-directive.tex"),
                expected = listOf("ERFOLGREICH"),
                fixture = "simple-directive",
        ),
        Scenario(
                name = "files-array.bats", dir = "files-array",
                description = "the directive file list replication",
                check = "both files in the array are processed",
                arguments = listOf("multiple-files.tex"),
                expected = listOf("doc1.tex", "doc2.tex"),
                fixture = "multiple-files",
        ),
        Scenario(
                name = "max-loops.bats", dir = "max-loops",
                description = "the loop cap through the command line",
                check = "the while loop terminates within the configured cap",
                arguments = listOf("--max-loops", "2", "looper.tex"),
                expected = listOf("SUCCESS"),
                fixture = "max-loops",
        ),
        Scenario(
                name = "conditionals.bats", dir = "conditionals",
                description = "the conditional dialect of the default engine",
                check = "the conditionals take the expected branches",
                arguments = listOf("conditionals.tex"),
                expected = listOf("QuackOne", "QuackFour"),
                forbidden = listOf("QuackTwo", "QuackThree"),
                fixture = "conditionals",
        ),
        Scenario(
                name = "sessions.bats", dir = "sessions",
                description = "session state through the default engine",
                check = "the session persists between the rule commands",
                arguments = listOf("sessions.tex"),
                expected = listOf("MeowQuack"),
                fixture = "sessions",
        ),
        Scenario(
                name = "lua-sessions.bats", dir = "lua-engine-sessions",
                description = "session state through the Lua engine",
                check = "the session persists between the rule commands",
                arguments = listOf("--engine", "lua", "lua-engine-sessions.tex"),
                expected = listOf("MeowQuack"),
                fixture = "lua-engine-sessions",
        ),
        Scenario(
                name = "halt.bats", dir = "halt-error",
                description = "the halt directive's error path",
                check = "the run stops after the halt with its exit code",
                arguments = listOf("--log", "halt-error.tex"),
                expected = listOf("QuackOne"),
                forbidden = listOf("QuackTwo"),
                exitCode = 1,
                logChecks = listOf("Halt requested with status 1"),
                fixture = "halt-error",
        ),
        Scenario(
                name = "halt-escape.bats", dir = "halt-escape",
                description = "the halt directive's succeeding escape",
                check = "the halted file ends and the run proceeds past it",
                arguments = listOf("--log", "escape-a.tex", "escape-b.tex"),
                expected = listOf("QuackOne", "QuackThree"),
                forbidden = listOf("QuackTwo"),
                logChecks = listOf("Halt requested with status 0"),
                fixture = "halt-escape",
        ),
        Scenario(
                name = "halt-code.bats", dir = "halt-code",
                description = "the halt's own exit code",
                check = "the requested status reaches the process unchanged",
                arguments = listOf("--log", "halt-code.tex"),
                expected = listOf("TOTAL FAILURE"),
                exitCode = 42,
                logChecks = listOf("Halt requested with status 42"),
                fixture = "halt-code",
        ),
        Scenario(
                name = "file-db.bats", dir = "changes",
                description = "the file database without the TeX toolchain",
                check = "the first run runs the rule and the second run skips",
                arguments = listOf("changes.tex"),
                expected = listOf("QuackOne"),
                extraSetup = "rm -f arara.db",
                doubleRun = true,
                secondRunForbidden = "QuackOne",
                fixture = "changes",
        ),
        Scenario(
                name = "foreign-extension.bats", dir = "foreign-extension",
                description = "dispatch by a configured foreign extension",
                check = "the configured extension is processed like a native one",
                arguments = listOf("foreign-extension.my"),
                expected = listOf("QuackOne"),
                fixture = "foreign-extension",
        ),
        Scenario(
                name = "foreign-extension-unknown.bats", dir = "foreign-extension",
                description = "the rejection of an uncovered extension",
                check = "the uncovered extension of an existing file is rejected with the type verdict",
                arguments = listOf("foreign-extension.xy"),
                expected = listOf("could not determine the file type"),
                exitCode = 2,
                fixture = "foreign-extension",
        ),
        Scenario(
                name = "logging.bats", dir = "logging",
                description = "the log file behind the log flag",
                check = "the log file records the engine and the exit value",
                arguments = listOf("--log", "simple-directive.tex"),
                expected = listOf("SUCCESS"),
                forbidden = listOf("kotlin-logging:"),
                logChecks = listOf(
                        "Rule engine set to yaml",
                        "System command exited with value 0",
                        "YAML rules are deprecated",
                ),
                fixture = "simple-directive",
        ),
        Scenario(
                name = "wrong-tag.bats", dir = "wrong-tag",
                description = "the rejection of the deprecated orb tag under Lua",
                check = "the orb tag in the options reports the engine mismatch",
                arguments = listOf("--engine", "lua", "wrongtag.tex"),
                expected = listOf("cannot interpolate"),
                exitCode = 2,
                authoredContent = wrongTagDocument,
                authoredTarget = "wrongtag.tex",
        ),
        Scenario(
                name = "lua-config.bats", dir = "lua-config",
                description = "the Lua configuration file and its in-scope values",
                check = "the rule is found through the paths computed in the script",
                arguments = listOf("--engine", "lua", "lua-config.tex"),
                expected = listOf("from: the Lua configuration"),
                fixture = "lua-config",
        ),
        Scenario(
                name = "missing-rule.bats", dir = "missing-rule",
                description = "the missing-rule error in the engine's own format",
                check = "the error names the Lua file the engine was looking for",
                arguments = listOf("--engine", "lua", "missing-rule.tex"),
                expected = listOf(
                        "I could not find a rule",
                        "notarulequack.lua",
                        "  - ",
                ),
                exitCode = 2,
                fixture = "missing-rule",
        ),
        Scenario(
                name = "snake-helpers.bats", dir = "snake-helpers",
                description = "the snake_case helper spellings through a local rule",
                check = "the idiomatic spellings run the rule end to end",
                arguments = listOf("--engine", "lua", "snake-helpers.tex"),
                expected = listOf("snake works", "snake-helpers.tex"),
                fixture = "snake-helpers",
        ),
        Scenario(
                name = "both-configs.bats", dir = "both-configs",
                description = "the configuration precedence with both formats present",
                check = "the YAML configuration wins and its rule runs",
                arguments = listOf("both-configs.tex"),
                expected = listOf("FromTheYamlConfig"),
                forbidden = listOf("FromTheLuaConfig"),
                fixture = "both-configs",
        ),
        Scenario(
                name = "lua-config-default.bats", dir = "lua-config-default",
                description = "the Lua configuration honored under the default engine",
                check = "the rule resolves through the Lua configuration's paths",
                arguments = listOf("lua-config-default.tex"),
                expected = listOf("LuaConfigUnderDefaultEngine"),
                fixture = "lua-config-default",
        ),
        Scenario(
                name = "console-explicit.bats", dir = "console-explicit",
                description = "the console flag selecting the plain presentation",
                check = "the explicit plain mode renders the standard output",
                arguments = listOf("--console", "plain", "console-explicit.tex"),
                expected = listOf("SUCCESS"),
                fixture = "console-explicit",
        ),
        Scenario(
                name = "console-config.bats", dir = "console-config",
                description = "the configuration key selecting the console presentation",
                check = "the configured plain mode runs and the log states a silent tool",
                arguments = listOf("--log", "console-config.tex"),
                expected = listOf("SUCCESS"),
                logChecks = listOf("The tool produced no output."),
                fixture = "console-config",
        ),
        Scenario(
                name = "console-config-invalid.bats", dir = "console-config-invalid",
                description = "an unoffered console mode named by the configuration",
                check = "the localized unoffered-mode error reports the name",
                arguments = listOf("console-config-invalid.tex"),
                expected = listOf(
                        "I cannot set the console to",
                        "nonsense",
                ),
                exitCode = 2,
                fixture = "console-config-invalid",
        ),
        Scenario(
                name = "console-flag-invalid.bats", dir = "console-flag-invalid",
                description = "an unoffered console mode named by the command line",
                check = "the usage error lists the offered modes",
                arguments = listOf("--console", "fancy", "console-flag-invalid.tex"),
                expected = listOf(
                        "invalid choice",
                        "choose from plain, styled",
                ),
                exitCode = 1,
                fixture = "console-flag-invalid",
        ),
        Scenario(
                name = "console-styled.bats", dir = "console-styled",
                description = "the styled presentation under a captured stream",
                check = "the true degraded structure keeps the content needles",
                arguments = listOf("--console", "styled", "console-explicit.tex"),
                expected = listOf(
                        "SUCCESS",
                        "QuackOne",
                        "TOTAL SUCCESS",
                        "Execution summary",
                        "System command",
                        "the cool TeX automation tool",
                ),
                forbidden = listOf("✓"),
                fixture = "console-explicit",
        ),

        Scenario(
                name = "console-multi-file.bats", dir = "console-multi-file",
                description = "the styled ledger across the files of a run",
                check = "the verdict and the summary count every file",
                arguments = listOf("-c", "styled", "console-explicit.tex", "second.tex"),
                expected = listOf(
                        "TOTAL SUCCESS",
                        "2 files",
                        "2 tasks",
                        "second.tex",
                ),
                fixture = "console-explicit",
                authoredTarget = "second.tex",
                authoredContent = "% arara: echoone\nQuack\n\\bye\n",
        ),
        Scenario(
                name = "console-dry-run.bats", dir = "console-dry-run",
                description = "the styled dry run verdict",
                check = "the simulated run closes with its count and no table",
                arguments = listOf(
                        "--console", "styled", "--dry-run",
                        "console-explicit.tex"
                ),
                expected = listOf("DRY RUN", "[DR]"),
                forbidden = listOf("Execution summary"),
                fixture = "console-explicit",
        ),
        Scenario(
                name = "console-failure.bats", dir = "console-failure",
                description = "the failed task quoting its own output",
                check = "the tail panel shows the tool words and the log keeps them",
                arguments = listOf("--console", "styled", "--log", "console-failure.tex"),
                expected = listOf(
                        "FAILURE",
                        "QuackFail",
                ),
                exitCode = 1,
                logChecks = listOf(
                        "BEGIN OUTPUT BUFFER",
                        "QuackFail",
                ),
                fixture = "console-failure",
        ),
        Scenario(
                name = "console-error.bats", dir = "console-error",
                description = "the failed spawn raising its error mid-task",
                check = "the error label lands on the open entry line",
                arguments = listOf("--console", "styled", "console-error.tex"),
                expected = listOf(
                        "ERROR",
                        "IO error",
                        "TOTAL FAILURE",
                ),
                exitCode = 2,
                fixture = "console-error",
        ),
        Scenario(
                name = "console-log-parity.bats", dir = "console-log-parity",
                description = "the log parity across the presentations",
                check = "the plain and the styled runs record the same log",
                arguments = listOf("console-explicit.tex"),
                expected = listOf("SUCCESS"),
                logParity = true,
                fixture = "console-explicit",
        ),
)

/**
 * Renders the working directory step of a scenario: each runner
 * moves into its own directory, so the rule and configuration
 * lookups resolve as they do for a user.
 */
private fun workingDir(dir: String): String = "cd \"\$SMOKE_DIR/$dir\""

/**
 * Renders the arara invocation of a scenario: the leg's Java
 * binary runs the runnable jar with the scenario's arguments, and
 * the double dollar keeps the shell variables for the runner to
 * expand.
 */
private fun arara(arguments: List<String>): String =
        $$""""$SMOKE_JAVA" -jar "$APP" """ + arguments.joinToString(" ")

/**
 * Renders one scenario as its runner file: the header documents
 * the scenario, the setup provisions the working directory, and
 * the test asserts the status, the expected and forbidden output
 * strings, the optional second run and the optional log reads -
 * the parity pipeline last.
 */
private fun render(scenario: Scenario): String = bats {
    header { scenario.description }
    setup {
        if (scenario.extraSetup.isEmpty()) {
            workingDir(scenario.dir)
        } else {
            workingDir(scenario.dir) + "\n" + scenario.extraSetup
        }
    }
    test(scenario.check) {
        run { arara(scenario.arguments) }
        status { it eq scenario.exitCode }
        output {
            scenario.expected.forEach { needle -> it contains needle }
            scenario.forbidden.forEach { needle -> !it contains needle }
        }
        if (scenario.doubleRun) {
            run { arara(scenario.arguments) }
            status { it eq 0 }
            output { !it contains scenario.secondRunForbidden }
        }
        if (scenario.logChecks.isNotEmpty()) {
            // the log file is read back through a second run, so its
            // recorded facts assert like ordinary command output
            run { "cat arara.log" }
            output {
                scenario.logChecks.forEach { needle -> it contains needle }
            }
        }
        if (scenario.logParity) {
            renderLogParity(scenario)
        }
    }
}

/**
 * Renders the log parity gate of a scenario: the same run under the
 * plain and the styled presentation must record the same log
 * entries. Each log names the presentation it rendered through, so
 * the two provenance lines are asserted and then left aside. The
 * normalized display-side records compare inside the diff itself -
 * a redirection outside the runner would swallow the stream into
 * the captured output and leave the compared files empty, so the
 * whole pipeline runs as one shell command whose failure shows the
 * lines that differ.
 */
private fun TestCaseBuilder.renderLogParity(scenario: Scenario) {
    run { arara(scenario.arguments + listOf("--console", "plain", "--log")) }
    status { it eq 0 }
    run { "mv arara.log arara-plain.log" }
    run { arara(scenario.arguments + listOf("--console", "styled", "--log")) }
    status { it eq 0 }
    // the provenance names the presentation each leg rendered
    // through - the captured stream of the runner keeps the styled
    // one degraded, so the annotation rides the pinned needle
    run { "grep 'Console set to plain' arara-plain.log" }
    status { it eq 0 }
    run { "grep 'Console set to styled (degraded)' arara.log" }
    status { it eq 0 }
    // the normalization folds away what varies per run: the
    // timestamps, the wall-clock durations, the file modification
    // times - and the provenance line stays out of the comparison
    val normalize =
        "-e 's/^[0-9-]+ [0-9:.]+ +[A-Z]+ +- //' " +
            "-e 's/in [0-9.]+ seconds/in N seconds/' " +
            "-e 's/Elapsed time: [0-9.]+ seconds/Elapsed time: N seconds/' " +
            "-e 's/last modified: [0-9-]+ [0-9:]+/last modified: T/'"
    val provenanceAside = "sed -E '/^Console set to/d'"
    run {
        "bash -c \"diff " +
            "<(sed -E $normalize arara-plain.log | $provenanceAside) " +
            "<(sed -E $normalize arara.log | $provenanceAside)\""
    }
    status { it eq 0 }
}
