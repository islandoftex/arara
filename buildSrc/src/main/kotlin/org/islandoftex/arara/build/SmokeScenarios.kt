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
    val exitCode: Int = 0,
    val extraSetup: String = "",
    val doubleRun: Boolean = false,
    val secondRunForbidden: String = "",
    val fixture: String = "",
    val authoredFile: String = "",
    val authoredTarget: String = "",
)

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
                authoredFile = "dbcheck.tex",
                authoredTarget = "dbcheck.tex",
        ),
        Scenario(
                name = "tex-yaml.bats", dir = "tex-yaml",
                description = "the official rule through the default dialect",
                check = "the official pdflatex rule typesets the document",
                arguments = listOf("--verbose", "tex-yaml.tex"),
                expected = listOf("This is pdfTeX"),
                authoredFile = "pdflatex.tex",
                authoredTarget = "tex-yaml.tex",
        ),
        Scenario(
                name = "tex-lua.bats", dir = "tex-lua",
                description = "the official rule through the Lua dialect",
                check = "the official pdflatex rule typesets the document",
                arguments = listOf("--verbose", "--engine", "lua", "tex-lua.tex"),
                expected = listOf("This is pdfTeX"),
                authoredFile = "pdflatex.tex",
                authoredTarget = "tex-lua.tex",
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
)

private fun render(scenario: Scenario): String = bats {
    header { scenario.description }
    setup {
        if (scenario.extraSetup.isEmpty()) {
            "cd \"${'$'}SMOKE_DIR/${scenario.dir}\""
        } else {
            "cd \"${'$'}SMOKE_DIR/${scenario.dir}\"\n${scenario.extraSetup}"
        }
    }
    test(scenario.check) {
        run { "\"${'$'}SMOKE_JAVA\" -jar \"${'$'}APP\" " + scenario.arguments.joinToString(" ") }
        status { it eq scenario.exitCode }
        output {
            scenario.expected.forEach { needle -> it contains needle }
            scenario.forbidden.forEach { needle -> !it contains needle }
        }
        if (scenario.doubleRun) {
            run { "\"${'$'}SMOKE_JAVA\" -jar \"${'$'}APP\" " + scenario.arguments.joinToString(" ") }
            status { it eq 0 }
            output { !it contains scenario.secondRunForbidden }
        }
    }
}
