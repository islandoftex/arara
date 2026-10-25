// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

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
            Files.writeString(
                file,
                """
                #!/bin/sh
                # Public domain. Originally written by Norbert Preining and Karl Berry, 2018.
                # Note from Paulo: this script provides better Cygwin support than our original
                # approach, so the team decided to use it as a proper wrapper for arara as well.

                scriptname=`basename "$0" .sh`
                jar="${'$'}scriptname.jar"
                jarpath=`kpsewhich --progname="${'$'}scriptname" --format=texmfscripts "${'$'}jar"`

                modulepath=`dirname "${'$'}jarpath"`/jna.jar
                kernel=`uname -s 2>/dev/null`
                if echo "${'$'}kernel" | grep CYGWIN >/dev/null; then
                  CYGWIN_ROOT=`cygpath -w /`
                  export CYGWIN_ROOT
                  modulepath=`cygpath -w "${'$'}modulepath"`
                  jarpath=`cygpath -w "${'$'}jarpath"`
                fi

                exec java --module-path "${'$'}modulepath" --add-modules=com.sun.jna \
                  --enable-native-access=com.sun.jna -jar "${'$'}jarpath" "${'$'}@"
                """.trimIndent() + "\n"
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
     * @param date The formatted release or build date.
     * @throws IOException The file could not be written.
     */
    @Throws(IOException::class)
    @Suppress("LongMethod")
    fun createManPage(file: Path, version: String, date: String) {
        try {
            Files.writeString(
                file,
                """
                .TH ARARA 1 "$date" "v$version"
                .SH NAME
                arara \- a TeX automation tool based on rules and directives.
                .SH SYNOPSIS
                .B arara
                .RI [ options ]
                .IR documents ...
                .SH DESCRIPTION
                \fBarara\fP is a TeX automation tool based on rules and directives. It gives
                you a way to enhance your TeX experience. The tool is an effort to provide a
                concise way to automate the daily TeX workflow for users and also package
                writers. Users might write their own rules when the provided ones do not suffice.
                .PP
                arara takes a list of documents as input. They will be processed according
                to their directives. Options apply to the execution of all documents.
                .SH OPTIONS
                .IP \fB-e\fP / \fB--preferred-engine\fP lua|yaml
                Prefer Lua or YAML rules (default: lua); fall back to the other format.
                YAML/MVEL rules are deprecated and will be discontinued in v9.
                .IP \fB--log\fP
                Write a text log (shorthand for --log-format=text).
                .IP \fB--log-format\fP none|text|json
                Select the diagnostic log format (default: none).
                .IP \fB--verbose\fP / \fB--silent\fP
                Print or suppress command output.
                .IP \fB--dry-run\fP
                Go through all the motions of running a command but with no actual calls.
                .IP \fB--safe-run\fP
                Run in safe mode and disable potentially harmful features.
                .IP \fB--whole-file\fP
                Extract directives in the file, not only in the header.
                .IP \fB--preamble\fP name
                Set the file preamble as named based on the configuration file.
                .IP \fB--timeout\fP milliseconds
                Sets an execution timeout for spawned processes.
                .IP \fB--language\fP code
                Set the localization to the language specified by code.
                .IP \fB--max-loops\fP number
                Set the number > 0 of loops for looping directives.
                .IP \fB--working-directory\fP path
                Set the working directory for the whole execution.
                .IP \fB--call-property\fP value
                Pass a property as \fCkey=value\fP parameter into the application
                to be used within the session.
                .IP \fB--properties-file\fP value
                Pass a properties file to the application to be used within the session.
                .IP \fB--generate-completion\fP shell
                Generate a completion script for arara.
                .IP \fB--version\fP
                Show the version and exit.
                .IP \fB--help\fP
                Show a help message and exit.
                .SH BUGS
                Issue tracker at
                .UR https://gitlab.com/islandoftex/arara/-/issues
                .UE .
                """.trimIndent() + "\n"
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
