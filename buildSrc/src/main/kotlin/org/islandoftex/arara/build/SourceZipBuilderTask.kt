// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Zip

/**
 * Zip relevant source files to meet CTAN's requirements.
 */
open class SourceZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a source ZIP as required by CTAN."

        inputs.dir(project.projectDir.resolve("api/src"))
        inputs.file(project.projectDir.resolve("api/build.gradle.kts"))
        inputs.dir(project.projectDir.resolve("core/src"))
        inputs.file(project.projectDir.resolve("core/build.gradle.kts"))
        inputs.dir(project.projectDir.resolve("cli/src"))
        inputs.file(project.projectDir.resolve("cli/build.gradle.kts"))
        inputs.dir(project.projectDir.resolve("lua/src"))
        inputs.file(project.projectDir.resolve("lua/build.gradle.kts"))
        outputs.files(project.layout.buildDirectory.file("arara-${project.version}-src.zip").get().asFile)
        outputs.upToDateWhen { false }

        archiveFileName.set(project.layout.buildDirectory.file("arara-${project.version}-src.zip").get().asFile.absolutePath)
        from(project.projectDir.resolve("api"))
        from(project.projectDir.resolve("core"))
        from(project.projectDir.resolve("cli"))
        from(project.projectDir.resolve("lua"))
        exclude("build")

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
