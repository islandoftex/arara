// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TDSTreeBuilderTask : DefaultTask() {
    init {
        group = "distribution"
        description = "Create a TDS compliant directory tree."

        // depend on shadow Jar input
        inputs.files(project.fileTree("cli/build/libs/")
                .include("*-with-deps-*.jar"))
        // depend on source zip as required by CTAN
        inputs.file(project.buildDir.resolve("arara-${project.version}-src.zip"))
        // depend on documentation (it should be compiled)
        inputs.dir("docs")
        inputs.dir("cli")
        inputs.dir("rules")
        outputs.dir(project.buildDir.resolve("tds").absolutePath)
    }

    /**
     * The task's main action: Creating a TDS directory hierarchy.
     */
    @TaskAction
    fun run() {
        val temporaryDir = project.buildDir.resolve("tds")
        if (temporaryDir.exists())
            temporaryDir.deleteRecursively()
        temporaryDir.mkdirs()

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
        project.copy {
            from(project.files(project.tasks.findByPath(":docs:buildManual")))
            into(temporaryDir.resolve("doc/support/arara"))
            exclude("*.xml")
        }

        logger.debug("Copying the top level README file")
        project.copy {
            from("README.md")
            into(temporaryDir.resolve("doc/support/arara"))
            filter {
                it.replace("# arara", "# arara v${project.version}")
            }
        }

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
        project.copy {
            from(project.files(project.tasks.findByPath(":cli:shadowJar")))
            into(temporaryDir.resolve("scripts/arara"))
            rename { "arara.jar" }
        }

        logger.debug("Creating the shell script wrapper")
        TaskHelper.createScript(temporaryDir.resolve("scripts/arara/arara.sh").toPath())

        logger.info("Building the source code structure")

        logger.debug("Creating the source code structure")
        project.copy {
            from(project.buildDir.resolve("arara-${project.version}-src.zip"))
            into(temporaryDir.resolve("source/support/arara"))
        }
    }
}
