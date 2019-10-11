package org.islandoftex.arara.build

import org.gradle.internal.impldep.com.google.common.io.Files
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.IOException
import java.nio.file.Paths

object TaskHelper {
  /**
   * Checks whether the commands are available in the system path.
   * @param commands Array of commands to be checked.
   * @throws Exception One of the commands does not exist.
   */
  @Throws(Exception::class)
  fun assertAvailability(vararg commands: String) {
    try {
      execute(File("."), *commands)
    } catch (_: Exception) {
      throw Exception("The command has returned an invalid "
          + "exit value. Chances are the command is not "
          + "available in the system path. Make sure the "
          + "command exists and try again. The application "
          + "will halt now.")
    }
  }

  /**
   * Executes the command and arguments in the provided directory.
   * @param directory The working directory.
   * @param call The proper call with the command and arguments.
   * @throws Exception An error has occurred during the execution.
   */
  @Throws(Exception::class)
  fun execute(directory: File, vararg call: String) {
    val result = try {
      ProcessExecutor().command(call.toList()).directory(directory)
          .execute().exitValue == 0
    } catch (_: Exception) {
      false
    }

    if (!result) {
      throw Exception("The command call has returned an invalid "
          + "exit value. Chances are the arguments are incorrect. "
          + "Make sure the call contains valid arguments and try "
          + "again. The application will halt now.")
    }
  }

  /**
   * Creates a shell script file for arara.
   * @param file The file reference.
   * @throws Exception The file could not be written.
   */
  @Throws(Exception::class)
  fun createScript(file: File) {
    try {
      file.writeText("""
        #!/bin/sh
        # Public domain. Originally written by Norbert Preining and Karl Berry, 2018.
        # Note from Paulo: this script provides better Cygwin support than our original
        # approach, so the team decided to use it as a proper wrapper for arara as well.
        
        scriptname=`basename "$0"`
        jar="${'$'}scriptname.jar"
        jarpath=`kpsewhich --progname="${'$'}scriptname" --format=texmfscripts "${'$'}jar"`
        
        kernel=`uname -s 2>/dev/null`"
        if echo "${'$'}kernel" | grep CYGWIN >/dev/null; then
          CYGWIN_ROOT=`cygpath -w /`
          export CYGWIN_ROOT
          jarpath=`cygpath -w "${'$'}jarpath"`
        fi
        
        exec java -jar "${'$'}jarpath" "${'$'}@"
      """.trimIndent())
    } catch (_: IOException) {
      throw Exception("I could not create the shell script for "
          + "arara due to an IO error. Please make sure the "
          + "current directory has the correct permissions "
          + "and try again. The application will halt now.")
    }
  }
}
