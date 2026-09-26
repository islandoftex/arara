// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.islandoftex.kobats.bats

// the scenarios read three variables from the environment: the
// scratch tree root, the Java binary of the leg under run, and the
// runnable jar. Each scenario renders as one runner file and moves
// to its own working directory in the generated setup, so the rule
// and configuration lookups resolve as they do for a user.

/** Runner files that require the real TeX toolchain on the path. */
internal val texScenarioFiles: Set<String> =
        setOf("tex-db.bats", "tex-yaml.bats", "tex-lua.bats")

/** Renders every smoke scenario, keyed by the runner file name. */
internal fun smokeScenarios(): Map<String, String> = scenarios.associate { it.name to render(it) }

private data class Scenario(
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
)

private val scenarios = listOf(
        Scenario(
                name = "run1.bats", dir = "run1",
                description = "the simple directive through the default engine",
                check = "the directive runs and reports success",
                arguments = listOf("simple-directive.tex"),
                expected = listOf("SUCCESS"),
                forbidden = listOf("kotlin-logging:"),
        ),
        Scenario(
                name = "run2.bats", dir = "run2",
                description = "the Lua engine happy path",
                check = "the engine works through the local Lua rule",
                arguments = listOf("--engine", "lua", "lua-engine.tex"),
                expected = listOf("LuaEngineWorks"),
                forbidden = listOf("kotlin-logging:"),
        ),
        Scenario(
                name = "run3.bats", dir = "run3",
                description = "the conditional dialect of the Lua engine",
                check = "the conditionals take the expected branches",
                arguments = listOf("--engine", "lua", "lua-engine-conditionals.tex"),
                expected = listOf("One", "Three", "Four"),
                forbidden = listOf("Two"),
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
        ),
        Scenario(
                name = "run5.bats", dir = "run5",
                description = "engine options with a staged application rule",
                check = "the rule echoes the basename of the file under run",
                arguments = listOf("--engine", "lua", "lua-engine-options.tex"),
                expected = listOf("name: lua-engine-options"),
        ),
        Scenario(
                name = "tex-db.bats", dir = "tex-db",
                description = "the file database semantics through the Lua engine",
                check = "the first run typesets and the second run skips",
                arguments = listOf("--verbose", "--engine", "lua", "dbcheck.tex"),
                expected = listOf("This is pdfTeX"),
                extraSetup = "rm -f arara.yaml dbcheck.pdf",
                doubleRun = true,
        ),
        Scenario(
                name = "tex-yaml.bats", dir = "tex-yaml",
                description = "the official rule through the default dialect",
                check = "the official pdflatex rule typesets the document",
                arguments = listOf("--verbose", "tex-yaml.tex"),
                expected = listOf("This is pdfTeX"),
        ),
        Scenario(
                name = "tex-lua.bats", dir = "tex-lua",
                description = "the official rule through the Lua dialect",
                check = "the official pdflatex rule typesets the document",
                arguments = listOf("--verbose", "--engine", "lua", "tex-lua.tex"),
                expected = listOf("This is pdfTeX"),
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
            output { !it contains "This is pdfTeX" }
        }
    }
}
