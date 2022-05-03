// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Zip

/**
 * Zip relevant documentation source files to meet CTAN's requirements.
 */
open class DocumentationSourceZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a documentation source ZIP as required by CTAN."

        inputs.dir(project.projectDir.resolve("website/public"))
        outputs.files(project.buildDir.resolve("arara-${project.version}-docsrc.zip"))
        outputs.upToDateWhen { false }

        archiveFileName.set(project.buildDir.resolve("arara-${project.version}-docsrc.zip").absolutePath)
        from(project.projectDir.resolve("website/public"))
        exclude("build")

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
