// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.islandoftex.scribeswan.ManPage
import org.islandoftex.scribeswan.bold
import org.islandoftex.scribeswan.italic

/**
 * Writes arara's man page on ScribeSwan's builder, for the CTAN
 * packaging pipeline.
 *
 * The content mirrors the 7.2.0 page dressed for the Lua era: the new
 * `--engine` and `--console` options join the roster, and an examples
 * section carries the whole idea in two directives. The date rides the
 * changelog's own line for this version — falling back to today while
 * the entry is Unreleased — so the released page always carries the
 * release day, by itself, with no stamping ceremony.
 *
 * The function reads the changelog through the build's working
 * directory and receives the version from the packaging pipeline, so
 * it captures nothing from the build script and stays configuration
 * cache safe.
 *
 * @param version the package version, threaded by the pipeline
 */
fun ManPage.araraManPage(version: String) {
    // the date, from the changelog's own line for this version —
    // or today, while the entry is Unreleased
    val iso = Regex("^## \\[$version\\] - (\\d{4}-\\d{2}-\\d{2})$")
        .find(File("CHANGELOG.md").readText())
        ?.groupValues?.get(1)
    val date = (iso?.let(LocalDate::parse) ?: LocalDate.now())
        .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH))

    header(
        title = "arara",
        section = "1",
        date = date,
        source = "arara v$version",
        manual = "User Commands",
    )
    name("arara", "a TeX automation tool based on rules and directives")
    synopsis {
        paragraph("${bold("arara")} [options] ${italic("documents")}...")
    }
    araraDescription()
    araraOptions()
    araraExamples()
    bugs {
        paragraph("Issue tracker at")
        link("https://gitlab.com/islandoftex/arara/-/issues")
    }
}

// the description section, so the page builder above reads as the
// outline of the very document it writes
private fun ManPage.araraDescription() {
    description {
        paragraph(
            "${bold("arara")} is a TeX automation tool based on rules and " +
                "directives. It gives you a way to enhance your TeX experience. " +
                "The tool is an effort to provide a concise way to automate the " +
                "daily TeX workflow for users and also package writers. Users " +
                "might write their own rules when the provided ones do not " +
                "suffice: rules speak Lua, and the YAML era continues in " +
                "graceful retirement.",
        )
        paragraph(
            "arara takes a list of documents as input. They will be " +
                "processed according to their directives. Options apply to " +
                "the execution of all documents.",
        )
    }
}

// the command line roster, one entry per option
private fun ManPage.araraOptions() {
    options {
        option("--log") { description("Generate a log output.") }
        option("--verbose", "--silent") { description("Print or suppress command output.") }
        option("--dry-run") {
            description("Go through all the motions of running a command but with no actual calls.")
        }
        option("--safe-run") {
            description("Run in safe mode and disable potentially harmful features.")
        }
        option("--whole-file") {
            description("Extract directives in the file, not only in the header.")
        }
        option("--preamble", argument = "name") {
            description("Set the file preamble as named based on the configuration file.")
        }
        option("--timeout", argument = "milliseconds") {
            description("Set the execution timeout for spawned processes.")
        }
        option("--language", argument = "code") {
            description("Set the localization to the language specified by code.")
        }
        option("--max-loops", argument = "number") {
            description("Set the number greater than zero of loops for looping directives.")
        }
        option("--working-directory", argument = "path") {
            description("Set the working directory for the whole execution.")
        }
        option("--call-property", argument = "value") {
            description(
                "Pass a property as ${bold("key=value")} parameter into the " +
                    "application to be used within the session.",
            )
        }
        option("--properties-file", argument = "path") {
            description("Pass a properties file to the application to be used within the session.")
        }
        option("--generate-completion", argument = "shell") {
            description("Generate a completion script for arara.")
        }
        option("--engine", argument = "engine") {
            description("Select the rule engine: ${bold("yaml")} (deprecated) or ${bold("lua")}.")
        }
        option("--console", argument = "mode") {
            description("Select how the tool presents itself: ${bold("plain")} or ${bold("styled")}.")
        }
        option("--version") { description("Show the version and exit.") }
        option("--help") { description("Show a help message and exit.") }
    }
}

// the examples section: the whole idea in two directives
private fun ManPage.araraExamples() {
    examples {
        literal(
            """
            % arara: pdflatex
            % arara: bibtex
            % arara: pdflatex

            ${'$'} arara thesis.tex
            """.trimIndent(),
        )
    }
}
