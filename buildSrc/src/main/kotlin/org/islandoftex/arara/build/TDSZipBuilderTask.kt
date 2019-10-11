package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TDSZipBuilderTask : DefaultTask() {
  init {
    group = "distribution"
    description = "Create a TDS compliant ZIP file."

    inputs.files("docs", "application", "rules")
    outputs.files(project.buildDir.resolve("arara.tds.zip"))
  }

  @TaskAction
  fun run() {
    logger.lifecycle("Testing required tools")
    logger.debug("Zip archive utility (zip)")
    TaskHelper.assertAvailability("zip", "-v")

    logger.lifecycle("Creating the TeX Directory Structure (TDS) archive")

    logger.info("Building the documentation directory")
    logger.debug("Creating the documentation structure")
    temporaryDir.resolve("doc/support/arara").mkdirs()

    logger.debug("Copying the original documentation")
    // copy the content of docs into support/arara
    project.copy {
      from(project.rootDir.resolve("docs"))
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
        .copyTo(temporaryDir.resolve("doc/support/arara/README.md"),
            overwrite = true)

    logger.info("Building the scripts directory")

    logger.debug("Creating the scripts structure")
    temporaryDir.resolve("scripts/arara").mkdirs()
    val ruleDir = project.mkdir(temporaryDir.resolve("scripts/arara/rules"))

    logger.debug("Copying the official rule pack directory")
    project.copy {
      from(project.rootDir.resolve("rules"))
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
    TaskHelper.createScript(temporaryDir.resolve("scripts/arara/arara.sh"))

    logger.info("Building the source code structure")

    logger.debug("Creating the source code structure")
    temporaryDir.resolve("source/support/arara").mkdirs()

    logger.debug("Copying the application source code directory")
    project.copy {
      from(project.rootDir.resolve("application"))
      into(temporaryDir.resolve("source/support/arara"))
      exclude("build")
    }

    logger.lifecycle("Building the TDS archive file")

    logger.debug("Creating the archive file")
    TaskHelper.execute(temporaryDir, "zip", "-r", "arara.tds.zip",
        "doc", "scripts", "source")

    logger.debug("Moving the archive file to the top level directory")
    temporaryDir.resolve("arara.tds.zip")
        .copyTo(project.buildDir.resolve("arara.tds.zip"), overwrite = true)
  }
}
