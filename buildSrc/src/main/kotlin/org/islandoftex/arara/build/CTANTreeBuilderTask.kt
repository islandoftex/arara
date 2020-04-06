// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CTANTreeBuilderTask : DefaultTask() {
    init {
        group = "distribution"
        description = "Create a CTAN compliant directory tree."

        // depend on the TDS zip
        inputs.files(project.buildDir.resolve("arara.tds.zip"))
        outputs.dir(project.buildDir.resolve("ctan").absolutePath)
    }

    /**
     * The task's main action: Creating a TDS directory hierarchy.
     */
    @TaskAction
    fun run() {
        logger.lifecycle("Preparing the archive file for CTAN submission")

        val temporaryDir = project.buildDir.resolve("ctan")
        if (temporaryDir.exists())
            temporaryDir.deleteRecursively()
        temporaryDir.mkdirs()

        logger.debug("Copying the TDS archive file to the temporary directory")
        val tdsZip = project.buildDir.resolve("arara.tds.zip")
                .copyTo(temporaryDir.resolve("arara.tds.zip"),
                        overwrite = true)

        logger.debug("Extracting the temporary TDS structure")
        project.copy {
            from(project.zipTree(tdsZip))
            into(temporaryDir.resolve("arara"))
        }

        logger.debug("Renaming the structure")
        temporaryDir.resolve("arara/doc")
                .renameTo(temporaryDir.resolve("arara/doc-old"))
        temporaryDir.resolve("arara/scripts")
                .renameTo(temporaryDir.resolve("arara/scripts-old"))
        temporaryDir.resolve("arara/source")
                .renameTo(temporaryDir.resolve("arara/source-old"))

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
                .renameTo(temporaryDir.resolve("arara/README.md"))
    }
}
