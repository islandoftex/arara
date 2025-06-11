// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Create a TDS compliant directory tree. Requires the result
 * of [SourceZipBuilderTask].
 */
open class TDSTreeBuilderTask : DefaultTask() {
    init {
        group = "distribution"
        description = "Create a TDS compliant directory tree."

        // depend on shadow Jar input
        inputs.files(
            project.fileTree("cli/build/libs/")
                .include("*-with-deps*.jar")
        )
        // depend on source zips as required by CTAN
        inputs.file(project.layout.buildDirectory.file("arara-${project.version}-src.zip").get().asFile)
        inputs.file(project.layout.buildDirectory.file("arara-${project.version}-docsrc.zip").get().asFile)
        // depend on documentation (it should be compiled)
        inputs.dir("docs")
        inputs.dir("cli")
        inputs.dir("rules")
        outputs.dir(project.layout.buildDirectory.dir("tds").get().asFile.absolutePath)
    }

    /**
     * The task's main action: Creating a TDS directory hierarchy.
     */
    @TaskAction
    @Suppress("LongMethod")
    fun run() {
        val temporaryDir = project.layout.buildDirectory.dir("tds").get().asFile
        if (temporaryDir.exists()) {
            temporaryDir.deleteRecursively()
        }
        temporaryDir.mkdirs()

        logger.lifecycle("Creating the TeX Directory Structure (TDS) archive")

        logger.info("Building the documentation directory")
        logger.debug("Creating the documentation structure")
        temporaryDir.resolve("doc/support/arara").mkdirs()

        logger.debug("Copying the original documentation")
        project.copy {
            from(project.rootDir.resolve("docs"))
            into(temporaryDir.resolve("doc/support/arara"))
            exclude("build.gradle.kts")
            exclude("arara.log")
        }

        logger.debug("Compiling the documentation")
        project.copy {
            from(project.files(project.tasks.findByPath(":docs:buildDocs")))
            into(temporaryDir.resolve("doc/support/arara"))
        }

        logger.debug("Copying the zipped documentation sources")
        project.copy {
            from(project.files(project.layout.buildDirectory.file("arara-${project.version}-docsrc.zip").get().asFile))
            into(temporaryDir.resolve("doc/support/arara"))
        }

        logger.debug("Copying the top level README file")
        project.copy {
            from("README.md")
            into(temporaryDir.resolve("doc/support/arara"))
            filter {
                it.replace("# arara", "# arara v${project.version}")
            }
        }

        logger.debug("Creating the man page directory structure")
        temporaryDir.resolve("doc/man/man1").mkdirs()

        logger.debug("Creating the man page")
        TaskHelper.createManPage(
            temporaryDir
                .resolve("doc/man/man1/${project.name}.1").toPath(),
            project.version.toString()
        )

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
        project.copy {
            from(project.layout.buildDirectory.file("arara-${project.version}-src.zip").get().asFile)
            into(temporaryDir.resolve("source/support/arara"))
        }
    }
}
