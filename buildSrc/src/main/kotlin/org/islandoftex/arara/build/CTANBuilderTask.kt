package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.IOException

open class CTANBuilderTask : DefaultTask() {
  init {
    group = "distribution"
    description = "Create a CTAN-ready ZIP file."

    inputs.files("docs", "application", "rules")
    outputs.files("arara.zip", "arara.tds.zip")
  }

  @TaskAction
  fun run() {
    val root = project.rootDir

    try {
      logger.lifecycle("Testing required tools")
      logger.debug("Zip archive utility (zip)")
      assertAvailability("zip", "-v")
      logger.debug("Unzip archive extraction utility (unzip)")
      assertAvailability("unzip", "-v")

      logger.lifecycle("Creating the TeX Directory Structure (TDS) archive")

      logger.info("Building the documentation directory")
      logger.debug("Creating the documentation structure")
      temporaryDir.resolve("doc/support/arara").mkdirs()

      logger.debug("Copying the original documentation")
      // copy the content of docs into support/arara
      project.copy {
        from(root.resolve("docs"))
        into(temporaryDir.resolve("doc/support/arara"))
        exclude("build.gradle.kts")
        exclude("arara.log")
      }

      logger.debug("Compiling the documentation")
      // dependsOn(":buildManual")
      project.copy {
        from(project.files(project.tasks.findByPath(":docs:buildManual")))
        into(temporaryDir.resolve("doc/support/arara"))
      }

      logger.debug("Copying the top level README file")
      project.file("README.md")
          .copyTo(temporaryDir.resolve("doc/support/arara/README.md"))

      logger.info("Building the scripts directory")

      logger.debug("Creating the scripts structure")
      temporaryDir.resolve("scripts/arara").mkdirs()
      val ruleDir = project.mkdir(temporaryDir.resolve("scripts/arara/rules"))

      logger.debug("Copying the official rule pack directory")
      project.copy {
        from(root.resolve("rules"))
        into(ruleDir)
      }

      logger.debug("Copying the application binary")
      // dependsOn(":application:build")
      project.copy {
        from(project.files(project.tasks.findByPath(":application:shadowJar")))
        into(temporaryDir.resolve("scripts/arara"))
        rename { "arara.jar" }
      }
      
      logger.debug("Creating the shell script wrapper")
      createScript(temporaryDir.resolve("scripts/arara/arara.sh"))

      logger.info("Building the source code structure")

      logger.debug("Creating the source code structure")
      temporaryDir.resolve("source/support/arara").mkdirs()

      logger.debug("Copying the application source code directory")
      project.copy {
        from(root.resolve("application"))
        into(temporaryDir.resolve("source/support/arara"))
        exclude("build")
      }

      logger.lifecycle("Building the TDS archive file")

      logger.debug("Creating the archive file")
      execute(temporaryDir, "zip", "-r", "arara.tds.zip",
          "doc", "scripts", "source")

      logger.debug("Moving the archive file to the top level directory")
      temporaryDir.resolve("arara.tds.zip")
          .copyTo(project.file("arara.tds.zip"))

      logger.debug("Recreating the temporary directory")
      temporaryDir.deleteRecursively()
      temporaryDir.mkdir()

      logger.lifecycle("Preparing the archive file for CTAN submission")

      logger.debug("Copying the TDS archive file to the temporary directory")
      project.file("arara.tds.zip").copyTo(temporaryDir.resolve("arara.tds.zip"))

      logger.debug("Copying the temporary TDS structure")
      val tempTDSzip = project.file("arara.tds.zip")
          .copyTo(temporaryDir.resolve("arara/arara.zip"))

      logger.debug("Extracting the temporary TDS structure")
      execute(temporaryDir.resolve("arara"), "unzip", "arara.zip")

      logger.debug("Removing the temporary TDS reference")
      tempTDSzip.delete()

      logger.debug("Renaming the structure")
      temporaryDir.resolve("arara/doc").renameTo(temporaryDir.resolve("arara/doc-old"))
      temporaryDir.resolve("arara/scripts").renameTo(temporaryDir.resolve("arara/scripts-old"))
      temporaryDir.resolve("arara/source").renameTo(temporaryDir.resolve("arara/source-old"))

      logger.debug("Copying the documentation directory")
      temporaryDir.resolve("arara/doc-old/support/arara")
          .copyRecursively(temporaryDir.resolve("arara/doc"))

      logger.debug("Removing the old documentation structure")
      temporaryDir.resolve("arara/doc-old").deleteRecursively()

      logger.debug("Copying the scripts directory")
      temporaryDir.resolve("arara/scripts-old/arara")
          .copyRecursively(temporaryDir.resolve("arara/scripts"))

      logger.debug("Removing the old scripts structure")
      temporaryDir.resolve("arara/scripts-old").deleteRecursively()

      logger.debug("Copying the source code directory")
      temporaryDir.resolve("arara/source-old/support/arara")
          .copyRecursively(temporaryDir.resolve("arara/source"))

      logger.debug("Removing the old source code structure")
      temporaryDir.resolve("arara/source-old").deleteRecursively()

      logger.debug("Copying the README file to the top level")
      temporaryDir.resolve("arara/doc/README.md")
          .copyTo(temporaryDir.resolve("arara/README.md"))

      logger.debug("Removing the original README file")
      temporaryDir.resolve("arara/doc/README.md").delete()

      logger.debug("Creating the archive file")
      execute(temporaryDir, "zip", "-r", "arara.zip",
          "arara.tds.zip", "arara")

      logger.debug("Copying archive file to top level")
      temporaryDir.resolve("arara.zip").copyTo(project.file("arara.zip"))

      logger.debug("Removing temporary directory")
      temporaryDir.deleteRecursively()
    } catch (exception: Exception) {
      logger.error(exception.message)
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
          .execute().getExitValue() == 0
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
}
