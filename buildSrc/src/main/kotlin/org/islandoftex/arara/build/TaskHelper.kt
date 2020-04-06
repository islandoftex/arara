// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.File
import java.io.IOException

object TaskHelper {
    /**
     * Creates a shell script file for arara.
     * @param file The file reference.
     * @throws IOException The file could not be written.
     */
    @Throws(IOException::class)
    fun createScript(file: File) {
        try {
            file.writeText("""
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
      """.trimIndent())
        } catch (_: IOException) {
            throw IOException("I could not create the shell script for " +
                    "arara due to an IO error. Please make sure the " +
                    "current directory has the correct permissions " +
                    "and try again. The application will halt now.")
        }
    }
}
