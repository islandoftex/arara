// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.islandoftex.kobats.bats

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

internal data class Scenario(
    val name: String,
    val dir: String,
    val description: String,
    val check: String,
    val arguments: List<String>,
    val expected: List<String>,
    val forbidden: List<String> = emptyList(),
    val logChecks: List<String> = emptyList(),
    val exitCode: Int = 0,
    val extraSetup: String = "",
    val doubleRun: Boolean = false,
    val secondRunForbidden: String = "",
    val fixture: String = "",
    val authoredContent: String = "",
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
                        "#1 to 'exists' (file or string expected, got number)",
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
                extraSetup = "rm -f arara.yaml dbcheck.pdf",
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
                arguments = listOf("project.lua"),
                expected = listOf("ProjectConfigWorks"),
                fixture = "project-config",
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
                arguments = listOf("halt-error.tex"),
                expected = listOf("QuackOne"),
                forbidden = listOf("QuackTwo"),
                exitCode = 1,
                fixture = "halt-error",
        ),
        Scenario(
                name = "file-db.bats", dir = "changes",
                description = "the file database without the TeX toolchain",
                check = "the first run runs the rule and the second run skips",
                arguments = listOf("changes.tex"),
                expected = listOf("QuackOne"),
                extraSetup = "rm -f arara.yaml",
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
                check = "the uncovered extension is rejected with the dispatch error",
                arguments = listOf("foreign-extension.xy"),
                expected = listOf("I could not find the provided file"),
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
)

private fun workingDir(dir: String): String = "cd \"\$SMOKE_DIR/$dir\""

private fun arara(arguments: List<String>): String =
        $$""""$SMOKE_JAVA" -jar "$APP" """ + arguments.joinToString(" ")

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
    }
}
