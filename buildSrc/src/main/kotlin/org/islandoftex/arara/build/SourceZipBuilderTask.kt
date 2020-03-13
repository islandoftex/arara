// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.build

import org.gradle.api.tasks.bundling.Zip

open class SourceZipBuilderTask : Zip() {
    init {
        group = "distribution"
        description = "Create a source ZIP as required by CTAN."

        inputs.dir(project.projectDir.resolve("application/src"))
        inputs.file(project.projectDir.resolve("application/build.gradle.kts"))
        outputs.files(project.buildDir.resolve("arara-${project.version}-src.zip"))
        outputs.upToDateWhen { false }

        archiveFileName.set(project.buildDir.resolve("arara-${project.version}-src.zip").absolutePath)
        from(project.projectDir.resolve("application"))
        exclude("build")
    }
}
