// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CTANBuilderTask : DefaultTask() {
    init {
        group = "distribution"
        description = "Create a CTAN-ready ZIP file."

	dependsOn("assembleTDSZip")
	
        inputs.files(project.buildDir.resolve("arara.tds.zip"))
        outputs.files(project.buildDir.resolve("arara.zip"))
    }

    /**
     * The task's main action: Creating a CTAN-ready zip file.
     */
    @TaskAction
    @Suppress("TooGenericExceptionCaught")
    fun run() {
        try {
            logger.lifecycle("Testing required tools")
            logger.debug("Zip archive utility (zip)")
            TaskHelper.assertAvailability("zip", "-v")

            logger.lifecycle("Preparing the archive file for CTAN submission")

            logger.debug("Copying the TDS archive file to the temporary directory")
            project.buildDir.resolve("arara.tds.zip")
                    .copyTo(temporaryDir.resolve("arara.tds.zip"),
                            overwrite = true)

            logger.debug("Copying the temporary TDS structure")
            val tempTDSzip = temporaryDir.resolve("arara.tds.zip")
                    .copyTo(temporaryDir.resolve("arara/arara.zip"))

            logger.debug("Extracting the temporary TDS structure")
            project.copy {
                from(project.zipTree(tempTDSzip))
                into(temporaryDir.resolve("arara"))
            }

            logger.debug("Removing the temporary TDS reference")
            tempTDSzip.delete()

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
                    .copyTo(temporaryDir.resolve("arara/README.md"))

            logger.debug("Removing the original README file")
            temporaryDir.resolve("arara/doc/README.md").delete()

            logger.debug("Creating the archive file")
            TaskHelper.execute(temporaryDir, "zip", "-r", "arara.zip",
                    "arara.tds.zip", "arara")

            logger.debug("Copying archive file to top level")
            temporaryDir.resolve("arara.zip")
                    .copyTo(project.buildDir.resolve("arara.zip"), overwrite = true)
        } catch (exception: Exception) {
            logger.error(exception.message)
        }
    }
}
