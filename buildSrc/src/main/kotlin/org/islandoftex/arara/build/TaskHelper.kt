// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.islandoftex.scribeswan.LineType
import org.islandoftex.scribeswan.bold
import org.islandoftex.scribeswan.code
import org.islandoftex.scribeswan.manpage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Collection of auxiliary functions to aid generating CTAN zips.
 */
object TaskHelper {
    /**
     * Creates a shell script file for arara.
     * @param file The file reference.
     * @throws IOException The file could not be written.
     */
    @Throws(IOException::class)
    fun createScript(file: Path) {
        try {
            Files.write(
                file,
                """
                #!/bin/sh
                # Public domain. Originally written by Norbert Preining and Karl Berry, 2018.
                # Note from Paulo: this script provides better Cygwin support than our original
                # approach, so the team decided to use it as a proper wrapper for arara as well.

                scriptname=`basename "$0" .sh`
                jar="${'$'}scriptname.jar"
                jarpath=`kpsewhich --progname="${'$'}scriptname" --format=texmfscripts "${'$'}jar"`

                kernel=`uname -s 2>/dev/null`
                if echo "${'$'}kernel" | grep CYGWIN >/dev/null; then
                  CYGWIN_ROOT=`cygpath -w /`
                  export CYGWIN_ROOT
                  jarpath=`cygpath -w "${'$'}jarpath"`
                fi

                exec java -jar "${'$'}jarpath" "${'$'}@"
                """.trimIndent().lines()
            )
            Files.setPosixFilePermissions(
                file,
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_EXECUTE
                )
            )
        } catch (_: IOException) {
            throw IOException(
                "I could not create the shell script for " +
                    "arara due to an IO error. Please make sure the " +
                    "current directory has the correct permissions " +
                    "and try again. The application will halt now."
            )
        }
    }

    /**
     * Creates a man page for arara.
     * @param file The file reference.
     * @param version The program's version.
     * @throws IOException The file could not be written.
     */
    @Throws(IOException::class)
    @Suppress("LongMethod")
    fun createManPage(file: Path, version: String) {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        try {
            val mp = manpage {
                header(
                        title = "arara",
                        section = "1",
                        date = today,
                        source = "v$version",
                )

                name(
                        name = "arara",
                        description = "a TeX automation tool based on rules and directives.",
                )

                synopsis {
                    line(LineType.BOLD_FACE, "arara")
                    line(LineType.REFERENCE_INPUT, "[ options ]")
                    line(LineType.INPUT_REFERENCE, "documents...")
                }

                description {
                    paragraph(
                            "${bold("arara")} is a TeX automation tool based on rules and " +
                                    "directives. It gives you a way to enhance your TeX experience. The tool is an " +
                                    "effort to provide a concise way to automate the daily TeX workflow for users " +
                                    "and also package writers. Users might write their own rules when the provided " +
                                    "ones do not suffice.",
                    )

                    paragraph(
                            "arara takes a list of documents as input. They will be processed according " +
                                    "to their directives. Options apply to the execution of all documents.",
                    )
                }

                options {
                    option("--log") {
                        "Generate a log output."
                    }

                    option(listOf("--verbose", "--silent")) {
                        "Print or suppress command output."
                    }

                    option("--dry-run") {
                        "Go through all the motions of running a command but with no actual calls."
                    }

                    option("--safe-run") {
                        "Run in safe mode and disable potentially harmful features."
                    }

                    option("--whole-file") {
                        "Extract directives in the file, not only in the header."
                    }

                    option("--preamble") {
                        "Set the file preamble as named based on the configuration file."
                    }

                    option("--timeout", "milliseconds") {
                        "Sets an execution timeout for spawned processes."
                    }

                    option("--language", "code") {
                        "Set the localization to the language specified by code."
                    }

                    option("--max-loops", "number") {
                        "Set the number > 0 of loops for looping directives."
                    }

                    option("--working-directory", "path") {
                        "Set the working directory for the whole execution."
                    }

                    option("--call-property", "value") {
                        "Pass a property as ${code("key=value")} parameter into the " +
                                "application to be used within the session."
                    }

                    option("--properties-file", "value") {
                        "Pass a properties file to the application to be used within the session."
                    }

                    option("--generate-completion", "shell") {
                        "Generate a completion script for arara."
                    }

                    option("--version") {
                        "Show the version and exit."
                    }

                    option("--help") {
                        "Show a help message and exit."
                    }
                }

                bugs {
                    paragraph("Issue tracker at")
                    line(LineType.UNIFORM_RESOURCE, "https://gitlab.com/islandoftex/arara/-/issues")
                    line(LineType.UNIFORM_RESOURCE_END, ".")
                }
            }.render()


            Files.write(
                file,
                mp.lines()
            )
        } catch (_: IOException) {
            throw IOException(
                "I could not create the man page for " +
                    "arara due to an IO error. Please make sure the " +
                    "current directory has the correct permissions " +
                    "and try again. The application will halt now."
            )
        }
    }
}
